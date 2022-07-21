package rzab.events;

import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.spigotmc.event.entity.EntityDismountEvent;
import rzab.PDeath;

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
		PDeath.getInstance().getDManager().remove(e.getPlayer());
	}
}
