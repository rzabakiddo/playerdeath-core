package rzab.process.data;

import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import rzab.process.LiveStage;

public class PlayerData {

	public Player player;

	public int expBefore;
	public BukkitTask playerThread;
	public LiveStage currentStage;
	public int timeLeft;

	public PlayerData(Player player) {
		this.player = player;
		this.currentStage = LiveStage.ALIVE;
	}
}
