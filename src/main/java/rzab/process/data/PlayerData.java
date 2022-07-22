package rzab.process.data;

import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;
import rzab.process.LiveStage;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class PlayerData {

	public Player player;
	public boolean stop;
	public ArmorStand entityBlock;
	public int expBefore;
	public BukkitTask playerThread;
	public HashSet<BukkitTask> otherTasks = new HashSet<>();
	public LiveStage currentStage;
	public int timeLeft;

	public PlayerData(Player player) {
		this.player = player;
		this.currentStage = LiveStage.ALIVE;
	}
}
