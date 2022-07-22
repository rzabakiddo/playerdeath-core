package rzab.process;

import org.bukkit.Bukkit;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;

import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import rzab.PDeath;
import rzab.process.data.PlayerData;

import static java.lang.Thread.*;
import static rzab.process.LiveStage.*;

public class DeathProcess {
    public void playerDied(PlayerData p) {
        if (p.entityBlock != null) {
            p.entityBlock.remove();
            p.entityBlock = null;
        }
        switch (p.currentStage) {
            case ALIVE:
                p.currentStage = LiveStage.DYING;
                p.expBefore = p.player.getTotalExperience();
                PDeath.getInstance().getProcess().armStand(p);
                p.player.teleport(p.entityBlock);
                p.entityBlock.addPassenger(p.player);
                p.otherTasks.add(new BukkitRunnable() {
                    @Override
                    public void run() {
                        p.player.spigot().respawn();
                        p.stop=true;
                        Bukkit.broadcastMessage("switch");
                    }
                }.runTaskLater(PDeath.getInstance(), 2L));
                p.playerThread = new BukkitRunnable() {

                    @Override
                    public synchronized void cancel() throws IllegalStateException {
                        if(p.currentStage==ALIVE)
                            p.player.setTotalExperience(p.expBefore);
                        super.cancel();
                    }

                    @Override
                    public void run() {
                        p.timeLeft = PDeath.getInstance().deathTime();
                        while (p.timeLeft > 0) {
                            if(isCancelled())
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
                        p.otherTasks.add(new BukkitRunnable() {
                            @Override
                            public void run() {
                                if(p.entityBlock!=null)
                                    p.entityBlock.remove();
                                p.player.setHealth(0);
                                p.player.setExp(0);
                                p.player.setLevel(0);
                                cancel();
                            }
                        }.runTask(PDeath.getInstance()));
                    }
                }.runTaskLaterAsynchronously(PDeath.getInstance(),2L);
                break;
            case DYING:
                p.currentStage = DEAD;
                break;
        }
    }

    public void playerRespawn(PlayerData p) {
        Bukkit.broadcastMessage("cream-test " + p.stop);
        if (p.playerThread != null && !p.stop) {
            p.playerThread.cancel();
            p.playerThread = null;
        }
        if(p.stop)
            p.stop=false;
        p.otherTasks.forEach(BukkitTask::cancel);
        p.otherTasks.clear();
        Bukkit.broadcastMessage(p.currentStage.name());
        if (p.currentStage != LiveStage.DYING)
            p.currentStage = LiveStage.ALIVE;
    }

    public void armStand(PlayerData p) {
        p.entityBlock = (ArmorStand) p.player.getWorld().spawnEntity(p.player.getLocation().subtract(0, 0x1.cp0, 0), EntityType.ARMOR_STAND);
        p.entityBlock.setVisible(false);
        p.entityBlock.setGravity(false);
        p.entityBlock.setInvulnerable(true);
    }

}
