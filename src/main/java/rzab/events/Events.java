package rzab.events;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.*;
import org.spigotmc.event.entity.EntityDismountEvent;
import rzab.PDeath;
import rzab.process.LiveStage;
import rzab.process.data.PlayerData;

import java.util.Objects;

public class Events implements Listener {

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onDeath(PlayerDeathEvent e) {
		PDeath.getInstance().getProcess().playerDied(PDeath.getInstance().getData(e.getEntity()));
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
		if(pd.playerThread!=null)
			Bukkit.getScheduler().cancelTask(pd.playerThread.getTaskId());
		if(pd.currentStage== LiveStage.DYING) {
			pd.player.setTotalExperience(pd.expBefore);
			pd.player.setHealth(0);
		}
		PDeath.getInstance().getDManager().remove(e.getPlayer());
	}

	@EventHandler
	public void onInteract(PlayerInteractAtEntityEvent e){
		if(e.getRightClicked() instanceof Player) {
			if(Objects.equals(e.getPlayer().getInventory().getItemInMainHand().getData().getItemType(), Material.getMaterial(PDeath.getInstance().reviveItem()))) {
				PlayerData playerData = PDeath.getInstance().getData((Player) e.getRightClicked());
				if (playerData.currentStage == LiveStage.DYING) {
					playerData.player.setHealth(playerData.player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getDefaultValue());
					playerData.currentStage = LiveStage.ALIVE;
					if(playerData.entityBlock!=null){
						playerData.entityBlock.remove();
						playerData.entityBlock=null;
					}
				}
			}
		}
	}
}
