package rzab.events;

import org.bukkit.Material;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.*;
import org.spigotmc.event.entity.EntityDismountEvent;
import rzab.PDeath;
import rzab.process.LifeStage;
import rzab.process.data.PlayerData;

import java.util.Objects;

public class Events implements Listener {

    @EventHandler
    public void onDamage(EntityDamageByEntityEvent e){
        if(e.getEntity() instanceof Player && e.getDamager() instanceof Player){
            PlayerData playerData = PDeath.getInstance().getData((Player) e.getEntity());
            if(playerData.currentStage== LifeStage.DYING){
                e.setCancelled(true);
                playerData.player.setHealth(0);
            }
        }
    }
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onDeath(PlayerDeathEvent e) {
        PlayerData playerData = PDeath.getInstance().getData(e.getEntity());
        if (playerData.currentStage == LifeStage.ALIVE) {
            e.setDroppedExp(0);
            e.setKeepInventory(true);
            e.getDrops().clear();
            e.setDeathMessage("");
        } else {
            e.setDroppedExp(playerData.expBefore);
        }
        PDeath.getInstance().getProcess().playerDied(playerData);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onRespawn(PlayerRespawnEvent e) {
        PDeath.getInstance().getProcess().playerRespawn(PDeath.getInstance().getData(e.getPlayer()));
    }

    @EventHandler
    public void onDismount(EntityDismountEvent e) {
        if (e.getEntity() instanceof Player) {
            if (e.getDismounted() instanceof ArmorStand) {
                e.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        PDeath.getInstance().getDManager().add(e.getPlayer());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        PlayerData pd = PDeath.getInstance().getData(e.getPlayer());
        if (pd.playerThread != null)
            pd.playerThread.cancel();
        if (pd.currentStage == LifeStage.DYING) {
            pd.player.setTotalExperience(pd.expBefore);
            pd.player.setHealth(0);
        }
        PDeath.getInstance().getDManager().remove(e.getPlayer());
    }

    @EventHandler
    public void onInteract(PlayerInteractAtEntityEvent e) {
        if (e.getRightClicked() instanceof Player) {
            if (Objects.equals(e.getPlayer().getInventory().getItemInMainHand().getData().getItemType(), Material.getMaterial(PDeath.getInstance().reviveItem().toUpperCase()))) {
                PlayerData playerData = PDeath.getInstance().getData((Player) e.getRightClicked());
                if (playerData.currentStage == LifeStage.DYING) {
                    playerData.player.setHealth(playerData.player.getMaxHealth());
                    playerData.currentStage = LifeStage.ALIVE;
                    if (playerData.playerThread != null) {
                        playerData.playerThread.cancel();
                        playerData.playerThread = null;
                    }
                    if (playerData.entityBlock != null) {
                        playerData.entityBlock.remove();
                        playerData.entityBlock = null;
                    }
                    playerData.healed=true;
                    playerData.player.teleport(playerData.player.getLocation().add(0,.75,0));
                }
            }
        }
    }
}
