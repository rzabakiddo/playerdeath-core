package rzab.process.data;

import java.util.HashSet;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class DataManager {
	private final Set<PlayerData> players = new HashSet<>();

	public DataManager() {
		Bukkit.getOnlinePlayers().forEach(this::add);
	}

	public PlayerData getPlayer(Player player) {
		return players.stream().filter(dataPlayer -> dataPlayer.player == player).findFirst().orElse(null);
	}

	public void add(Player player) {
		players.add(new PlayerData(player));
	}

	public void remove(Player player) {
		players.removeIf(dataPlayer -> dataPlayer.player == player);
	}
}
