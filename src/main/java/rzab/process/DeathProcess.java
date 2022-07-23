package rzab.process;

import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;

import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import rzab.PDeath;
import rzab.process.data.PlayerData;

import static java.lang.Thread.*;
import static rzab.process.LifeStage.*;

public class DeathProcess {
    public void playerDied(PlayerData p) {
        if (p.entityBlock != null) {
            p.entityBlock.remove();
            p.entityBlock = null;
        }
        if (p.playerThread != null) {
            p.playerThread.cancel();
            p.playerThread = null;
        }
        switch (p.currentStage) {
            case ALIVE:
                p.currentStage = LifeStage.DYING;
                p.otherTasks.add(new BukkitRunnable() {
                    @Override
                    public void run() {
                        p.player.spigot().respawn();
                    }
                }.runTaskLater(PDeath.getInstance(), 2L));
                p.playerThread = new BukkitRunnable() {

                    @Override
                    public void run() {
                        p.timeLeft = PDeath.getInstance().deathTime();
                        p.otherTasks.add(new BukkitRunnable() {
                            @Override
                            public void run() {
                                p.expBefore = p.player.getTotalExperience();
                                PDeath.getInstance().getProcess().armStand(p);
                                p.player.teleport(p.entityBlock);
                                p.entityBlock.addPassenger(p.player);
                            }
                        }.runTask(PDeath.getInstance()));
                        while (p.timeLeft > 0) {
                            if (isCancelled())
                                break;
                            p.player.setExp((float) p.timeLeft / (float) PDeath.getInstance().deathTime());
                            p.player.setLevel(p.timeLeft);
                            try {
                                //noinspection BusyWait
                                sleep(1000L);
                            } catch (InterruptedException e) {
                                p.otherTasks.add(new BukkitRunnable() {
                                    @Override
                                    public void run() {
                                        if (p.entityBlock != null)
                                            p.entityBlock.remove();
                                        p.player.setTotalExperience(p.player.getTotalExperience());
                                        cancel();
                                    }
                                }.runTask(PDeath.getInstance()));
                                break;
                            }

                            p.timeLeft--;
                        }
                        if (p.currentStage == ALIVE) {
                            p.player.setTotalExperience(p.expBefore);
                            p.expBefore = 0;
                        }
                        p.otherTasks.add(new BukkitRunnable() {
                            @Override
                            public void run() {
                                if (p.healed) {
                                    p.healed = false;
                                    return;
                                }
                                if (p.entityBlock != null)
                                    p.entityBlock.remove();
                                p.player.setHealth(0);
                                p.player.setExp(0);
                                p.player.setLevel(0);
                            }
                        }.runTask(PDeath.getInstance()));
                    }
                }.runTaskAsynchronously(PDeath.getInstance());
                break;
            case DYING:
                p.currentStage = DEAD;
                break;
        }
    }

    public void playerRespawn(PlayerData p) {
        p.otherTasks.forEach(BukkitTask::cancel);
        p.otherTasks.clear();
        if (p.currentStage != LifeStage.DYING)
            p.currentStage = LifeStage.ALIVE;
    }

    public void armStand(PlayerData p) {
        p.entityBlock = (ArmorStand) p.player.getWorld().spawnEntity(p.player.getLocation().subtract(0, 0x1.cp0, 0), EntityType.ARMOR_STAND);
        p.entityBlock.setVisible(false);
        p.entityBlock.setGravity(false);
        p.entityBlock.setInvulnerable(true);
    }

}
