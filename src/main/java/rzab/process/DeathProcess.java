package rzab.process;

import org.bukkit.Bukkit;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import rzab.PDeath;
import rzab.process.data.PlayerData;

import java.util.ArrayList;
import java.util.Objects;

import static java.lang.Thread.*;

public class DeathProcess {
	public void playerDied(PlayerData p) {
		switch (p.currentStage) {
		case ALIVE:
			p.expBefore = p.player.getTotalExperience();
			p.currentStage = LiveStage.DYING;
			p.playerThread = Bukkit.getScheduler().runTaskAsynchronously(PDeath.getInstance(), () -> {
				p.timeLeft = PDeath.getInstance().deathTime();
				if (PDeath.getInstance().dyingLife() == 0) {
					p.player.setHealth(0);
					currentThread().interrupt();
					return;
				}
				final ArmorStand[] armorStand = {null};

				Bukkit.getScheduler().runTaskLater(PDeath.getInstance(), () -> {
					armorStand[0] = PDeath.getInstance().getProcess().armStand(p.player);
					 if (PDeath.getInstance().dyingLife() == -1)
						 p.player.spigot().respawn();
					 if (PDeath.getInstance().dyingLife() > 0)
						 p.player.setHealth(PDeath.getInstance().deathTime());
					p.player.teleport(armorStand[0]);
					armorStand[0].addPassenger(p.player);
				},2L);


				while (p.timeLeft > 0) {
					p.player.setExp((float) p.timeLeft / (float) PDeath.getInstance().deathTime());
					p.player.setLevel(p.timeLeft);
					p.timeLeft--;
					try {
						//noinspection BusyWait
						sleep(1000L);
					} catch (InterruptedException e) {
						Bukkit.getScheduler().runTask(PDeath.getInstance(), () -> {
							try {
								armorStand[0].remove();
							}catch (Exception exc) {

							}
							p.player.setTotalExperience(p.player.getTotalExperience());
						});
						return;
					}
				}
				Bukkit.getScheduler().runTask(PDeath.getInstance(), () -> {
					try {
						armorStand[0].remove();
					}catch (Exception exc) {

					}
				});
				p.player.setExp(0);
				p.player.setLevel(0);
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
		if(p.currentStage!=LiveStage.DYING)
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
