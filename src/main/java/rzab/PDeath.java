package rzab;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import rzab.events.*;
import rzab.process.DeathProcess;
import rzab.process.LifeStage;
import rzab.process.data.DataManager;
import rzab.process.data.PlayerData;

public class PDeath extends JavaPlugin {

    private String reviveItem;
    private int deathTime;
    private DataManager dataManager;
    private DeathProcess deathProcess;
    private static PDeath instance;

    @Override
    public void onEnable() {
        instance = this;
        this.saveDefaultConfig();
        FileConfiguration config = this.getConfig();
        reviveItem = config.getString("revive-item", "golden_apple");
        deathTime = config.getInt("death-time", 60);
        dataManager = new DataManager();
        deathProcess = new DeathProcess();
        // Event register
        this.getServer().getPluginManager().registerEvents(new Events(), this);
        this.getCommand("revive").setExecutor(new ReviveCommand());
        super.onEnable();
    }

    public static PDeath getInstance() {
        return instance;
    }

    public String reviveItem() {
        return reviveItem;
    }

    public int deathTime() {
        return deathTime;
    }

    public DataManager getDManager() {
        return dataManager;
    }

    public DeathProcess getProcess() {
        return deathProcess;
    }

    public PlayerData getData(Player p) {
        return dataManager.getPlayer(p);
    }

    @Override
    public void onDisable() {
        Bukkit.getScheduler().cancelTasks(this);
        super.onDisable();
    }

    public class ReviveCommand implements CommandExecutor {

        @Override
        public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
            if (strings.length > 3) {
                return false;
            } else if (strings.length == 3) {
                if (strings[0].equalsIgnoreCase("set")) {
                    if (Bukkit.getOnlinePlayers().stream().filter(player -> player.getName().equalsIgnoreCase(strings[1])).findFirst() != null) {
                        try {
                            if (LifeStage.valueOf(strings[2].toUpperCase()) != null && strings[2].length() > 0) {
                                PlayerData pd = PDeath.instance.getData(Bukkit.getPlayer(strings[1]));
                                LifeStage liveStage = LifeStage.valueOf(strings[2].toUpperCase());
                                if (liveStage == LifeStage.DYING) {
                                    if (pd.currentStage == LifeStage.ALIVE) {
                                        pd.player.setHealth(0);
                                        commandSender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&aSuccessfully changed " + pd.player.getName() + " live stage to DYING."));
                                    } else {
                                        commandSender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&c" + pd.player.getName() + " life state cannot be changed because his life state is " + pd.currentStage.name() + " must be ALIVE"));
                                    }
                                } else if (liveStage == LifeStage.ALIVE) {
                                    if (pd.currentStage == LifeStage.DYING) {
                                        pd.player.setHealth(pd.player.getMaxHealth());
                                        pd.currentStage = LifeStage.ALIVE;
                                        if (pd.playerThread != null) {
                                            pd.playerThread.cancel();
                                            pd.playerThread = null;
                                        }
                                        if (pd.entityBlock != null) {
                                            pd.entityBlock.remove();
                                            pd.entityBlock = null;
                                        }
                                        pd.healed = true;
                                        pd.player.teleport(pd.player.getLocation().add(0, .75, 0));
                                    } else {
                                        commandSender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&c" + pd.player.getName() + " life state cannot be changed because his life state is " + pd.currentStage.name() + " must be DYING"));
                                    }
                                } else if (liveStage == LifeStage.DEAD) {
                                    if (pd.currentStage == LifeStage.DYING) {
                                        pd.player.setHealth(0);
                                    } else if (pd.currentStage == LifeStage.ALIVE) {
                                        pd.currentStage = LifeStage.DYING;
                                        pd.player.setHealth(0);
                                    } else {
                                        commandSender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&c" + pd.player.getName() + " life state cannot be changed because his life state is " + pd.currentStage.name() + " must be DYING or ALIVE"));
                                    }
                                }
                                return true;
                            }
                        } catch (IllegalArgumentException e) {
                            commandSender.sendMessage("Unknown lifestage: " + strings[2]);
                        }
                    } else
                        commandSender.sendMessage("Unknown player: " + strings[1]);
                } else
                    commandSender.sendMessage("Unknown value: " + strings[0]);
                return false;
            } else if (strings.length == 1) {
                if (strings[0].equalsIgnoreCase("list")) {
                    commandSender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&a&lAlive players:"));
                    Bukkit.getOnlinePlayers().forEach(player -> {
                        if (PDeath.instance.getData(player).currentStage == LifeStage.ALIVE) {
                            commandSender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&a" + player.getName()));
                        }
                    });
                    commandSender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&a&lDying players:"));
                    Bukkit.getOnlinePlayers().forEach(player -> {
                        if (PDeath.instance.getData(player).currentStage == LifeStage.DYING) {
                            commandSender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&a" + player.getName()));
                        }
                    });
                    commandSender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&a&lDead players:"));
                    Bukkit.getOnlinePlayers().forEach(player -> {
                        if (PDeath.instance.getData(player).currentStage == LifeStage.DEAD) {
                            commandSender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&a" + player.getName()));
                        }
                    });
                    return true;
                } else if (strings[0].equalsIgnoreCase("help")) {
                    commandSender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&a&lAvailable commands:"));
                    commandSender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&a/revive - revives you:"));
                    commandSender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&a/revive help - shows this menu:"));
                    commandSender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&a/revive list - show list of players in every life stage:"));
                    commandSender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&a/revive set <player> <alive|dying|dead> - changes the player's life stage:"));
                    return true;
                }
                return false;
            } else if(strings.length==0){
                PlayerData pd = PDeath.instance.getData(Bukkit.getPlayer(commandSender.getName()));
                if (pd.currentStage == LifeStage.DYING) {
                    pd.player.setHealth(pd.player.getMaxHealth());
                    pd.currentStage = LifeStage.ALIVE;
                    if (pd.playerThread != null) {
                        pd.playerThread.cancel();
                        pd.playerThread = null;
                    }
                    if (pd.entityBlock != null) {
                        pd.entityBlock.remove();
                        pd.entityBlock = null;
                    }
                    pd.healed = true;
                    pd.player.teleport(pd.player.getLocation().add(0, .75, 0));
                } else {
                    commandSender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&c" + pd.player.getName() + " life state cannot be changed because his life state is " + pd.currentStage.name() + " must be DYING"));
                }
                return true;
            }
            return false;
        }
    }

}
