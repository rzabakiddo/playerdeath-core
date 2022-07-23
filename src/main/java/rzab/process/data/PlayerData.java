package rzab.process.data;

import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;
import rzab.process.LifeStage;

import java.util.HashSet;

public class PlayerData {

	public Player player;
	public ArmorStand entityBlock;
	public boolean healed;
	public int expBefore;
	public BukkitTask playerThread;
	public HashSet<BukkitTask> otherTasks = new HashSet<>();
	public LifeStage currentStage;
	public int timeLeft;

	public PlayerData(Player player) {
		this.player = player;
		this.currentStage = LifeStage.ALIVE;
	}
}
