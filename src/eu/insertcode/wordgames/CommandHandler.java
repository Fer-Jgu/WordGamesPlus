package eu.insertcode.wordgames;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;

import eu.insertcode.wordgames.games.HoverGame;
import eu.insertcode.wordgames.games.ReorderGame;
import eu.insertcode.wordgames.games.TimedGame;
import eu.insertcode.wordgames.games.UnmuteGame;
import eu.insertcode.wordgames.games.WordGame;
import eu.insertcode.wordgames.games.WordGame.Reward;

import static org.bukkit.ChatColor.DARK_GREEN;
import static org.bukkit.ChatColor.DARK_RED;
import static org.bukkit.ChatColor.GOLD;
import static org.bukkit.ChatColor.GREEN;

public class CommandHandler implements CommandExecutor {
	private Main plugin;
	
	CommandHandler(Main instance) {
		plugin = instance;
	}
	
	@Override
	public boolean onCommand(CommandSender s, Command cmd, String label, String[] args) {
		try {
			if (args[0].equalsIgnoreCase("help")) {
				return onCommandHelp(s);
			}
			if (args[0].equalsIgnoreCase("stop")) {
				return onCommandStop(s);
			}
			if (args[0].equalsIgnoreCase("reload")) {
				return onCommandReload(s);
			}
			
			/*
			 * wordgames (type) [amount] (reward)
			 */
			if (args.length >= 3) {
				//If the wordgames limit has been reached,
				if (plugin.wordGames.size() >= plugin.getConfig().getInt("gameOptions.maxPlayingGames")
						&& plugin.getConfig().getInt("gameOptions.maxPlayingGames") != 0) {
					return errorMessage(s, "error.tooManyGames");
				}
				
				//Test which wordgame the user is trying to create
				if (args[0].equalsIgnoreCase("hover")) {
					return HoverGame.hasStartPermission(s) ? createGame(s, args, Type.HOVER) : errorMessage(s, "error.noPermissions");
				}
				if (args[0].equalsIgnoreCase("reorder")) {
					return ReorderGame.hasStartPermission(s) ? createGame(s, args, Type.REORDER) : errorMessage(s, "error.noPermissions");
				}
				if (args[0].equalsIgnoreCase("unmute")) {
					return UnmuteGame.hasStartPermission(s) ? createGame(s, args, Type.UNMUTE) : errorMessage(s, "error.noPermissions");
				}
				if (args[0].equalsIgnoreCase("timed")) {
					return TimedGame.hasStartPermission(s) ? createGame(s, args, Type.TIMED) : errorMessage(s, "error.noPermissions");
				}
				
				return errorMessage(s, "error.typeNotFound");
			}
		} catch (IndexOutOfBoundsException e) {
			return onCommandHelp(s);
		}
		return onCommandHelp(s);
	}
	
	private boolean onCommandHelp(CommandSender s) {
		s.sendMessage(GREEN + "/wordgames help" + DARK_GREEN + "  to show this message.");
		
		if (s.hasPermission("wordgamesplus.reload"))
			s.sendMessage(GREEN + "/wordgames reload" + DARK_GREEN + "  to reload the configuration.");
		
		if (s.hasPermission("wordgamesplus.stop"))
			s.sendMessage(GREEN + "/wordgames stop" + DARK_GREEN + "  to stop any and all playing games.");
		
		
		if (s.hasPermission("wordgamesplus.start") || s.hasPermission("wordgamesplus.start.reorder"))
			s.sendMessage(GREEN + "/wordgames reorder <word> [number] <reward>" + DARK_GREEN + "  to start the 'reorder' minigame.");
		
		if (s.hasPermission("wordgamesplus.start") || s.hasPermission("wordgamesplus.start.hover"))
			s.sendMessage(GREEN + "/wordgames hover <word> [number] <reward>" + DARK_GREEN + " to start the 'hover' minigame.");
		
		if (s.hasPermission("wordgamesplus.start") || s.hasPermission("wordgamesplus.start.unmute"))
			s.sendMessage(GREEN + "/wordgames unmute <word> [number] <reward>" + DARK_GREEN + " to start the 'unmute' minigame.");
		
		if (s.hasPermission("wordgamesplus.start") || s.hasPermission("wordgamesplus.start.timed"))
			s.sendMessage(GREEN + "/wordgames timed <word> [number] <reward>" + DARK_GREEN + " to start the 'timed' minigame.");
		
		
		s.sendMessage(GOLD + "[" + DARK_RED + "WordGames+" + GOLD + "]" + DARK_GREEN + " Plugin version: " + plugin.getDescription().getVersion());
		return true;
	}
	
	private boolean onCommandStop(CommandSender s) {
		//If the sender the required permissions
		if (!s.hasPermission("wordgamesplus.stop")) {
			return errorMessage(s, "error.noPermissions");
		}
		
		//If a game is playing.
		if (plugin.wordGames.size() != 0) {
			//Broadcast the stop
			for (String msg : Main.getColouredMessages("games.stop"))
				Bukkit.broadcastMessage(msg);
			//Stop all broadcasts
			for (WordGame game : plugin.wordGames) {
				game.stopAutoBroadcaster();
			}
			
			//Stop all the games
			plugin.wordGames = new ArrayList<>();
		} else {
			//No games are playing, tell sender!
			return errorMessage(s, "error.notPlaying");
		}
		return true;
	}
	
	private boolean onCommandReload(CommandSender s) {
		//If the sender the required permissions
		if (!s.hasPermission("wordgamesplus.reload")) {
			return errorMessage(s, "error.noPermissions");
		}
		
		//Reload & send a message
		plugin.reload();
		s.sendMessage(Main.getColouredMessages("reload"));
		return true;
	}
	
	private boolean errorMessage(CommandSender s, String path) {
		s.sendMessage(Main.getColouredMessages(path));
		return true;
	}
	
	private boolean createGame(CommandSender s, String[] args, Type type) {
		String rewardString;
		int amount;
		String wordToType;
		//If the user filled an reward amount in.
		if (args.length < 4) {
			rewardString = args[2];
			amount = 1;
		} else {
			//Try to parse the amount
			//Never trust user input! :)
			try {
				amount = Integer.parseInt(args[2]);
			} catch (NumberFormatException e) {
				//Something went wrong, no need to panic! I got this.
				return errorMessage(s, "error.wrongInput");
			}
			rewardString = args[3];
		}
		
		Reward reward = new Reward(amount, rewardString);
		wordToType = args[1];
		switch (type) {
			case REORDER:
				plugin.wordGames.add(new ReorderGame(plugin, wordToType, reward));
				return true;
			case HOVER:
				plugin.wordGames.add(new HoverGame(plugin, wordToType, reward));
				return true;
			case UNMUTE:
				plugin.wordGames.add(new UnmuteGame(plugin, wordToType, reward));
				return true;
			case TIMED:
				plugin.wordGames.add(new TimedGame(plugin, wordToType, reward));
				return true;
			default:
				return false;
		}
	}
	
	private enum Type {HOVER, REORDER, UNMUTE, TIMED}
}
