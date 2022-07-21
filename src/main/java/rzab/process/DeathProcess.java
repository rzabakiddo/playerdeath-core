package rzab.process;

import org.bukkit.Bukkit;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import rzab.PDeath;
import rzab.process.data.PlayerData;

import java.util.Objects;

import static java.lang.Thread.*;

public class DeathProcess {

	public void playerDied(PlayerData p) {
		switch (p.currentStage) {
		case ALIVE:
			Bukkit.getScheduler().runTaskLater(PDeath.getInstance(), () -> p.player.setHealth(Objects.requireNonNull(p.player.getAttribute(Attribute.GENERIC_MAX_HEALTH)).getDefaultValue()), 1L);

			p.currentStage = LiveStage.DYING;
			p.playerThread = Bukkit.getScheduler().runTaskAsynchronously(PDeath.getInstance(), () -> {
				p.timeLeft = PDeath.getInstance().deathTime();
				if (PDeath.getInstance().dyingLife() == 0) {
					p.player.setHealth(0);
					currentThread().interrupt();
					return;
				}
				if (PDeath.getInstance().dyingLife() > 0)
					p.player.setHealth(PDeath.getInstance().deathTime());
				Bukkit.getScheduler().runTask(PDeath.getInstance(), () -> PDeath.getInstance().getProcess().armStand(p.player).addPassenger(p.player));

				while (p.timeLeft > 0) {
					p.player.setExp((float) p.timeLeft / (float) PDeath.getInstance().deathTime());
					p.player.setLevel(p.timeLeft);
					p.timeLeft--;
					try {
						//noinspection BusyWait
						sleep(1000L);
					} catch (InterruptedException e) {
						return;
					}
				}
				Bukkit.getScheduler().runTask(PDeath.getInstance(), () -> p.player.setHealth(0));
			});
			break;
		case DYING:
			p.currentStage = LiveStage.DEAD;
			if (p.playerThread != null)
				Bukkit.getScheduler().cancelTask(p.playerThread.getTaskId());
			break;
			default:
			break;
		}
	}

	public void playerRespawn(PlayerData p) {
		p.currentStage = LiveStage.ALIVE;
	}

	public ArmorStand armStand(Player p) {
		ArmorStand entity = (ArmorStand) p.getWorld().spawnEntity(p.getLocation(), EntityType.ARMOR_STAND);
		entity.teleport(entity.getLocation().subtract(0, Math.abs(entity.getEyeLocation().getY()-entity.getLocation().getY()), 0));
		entity.setVisible(false);
		entity.setGravity(false);
		entity.setInvulnerable(true);
		return entity;
	}

}
