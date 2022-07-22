package rzab;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import rzab.events.*;
import rzab.process.DeathProcess;
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
}
