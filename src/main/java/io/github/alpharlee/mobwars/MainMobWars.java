package io.github.alpharlee.mobwars;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class MainMobWars extends JavaPlugin 
{
	protected EventListener eventListener; //EventListener Object, initialized in onEnable()
	protected CommandHandler commandHandler; //CommandHandler object, initialized in onEnable()
	protected MWRunnable mwRunnable; //MWRunnable that handles all the timer tasks
	
	//Default number
	public static double USE_DEFAULT = -999;
	
	public ArrayList<Game> games = new ArrayList<Game>();
	
	public Map<Player, Game> gamesByPlayer = new HashMap<Player, Game>();
	//public Map<Projectile, Player> PlayersBySelector = new HashMap<Projectile, Player>();
	
	/////////////////////////////////////////////////DEBUG/////////////////////
	//Test values
	public Game testGame;
	public static TargetCriteria testCriteria = TargetCriteria.NEAREST;
	public static boolean showDebugMessages = false;
	
	@Override
	public void onEnable()
	{
		//Create new files if not existent already
		//this.createFiles();
		
		//Create test game
		this.testGame = new Game(this);
		this.games.add(this.testGame);
		
		//Create a command handler
		this.commandHandler = new CommandHandler(this);
		//Create an event listener
		this.eventListener = new EventListener(this);
		
		//Register customized entities
		MWEntityType.registerEntities();
		
		//Initialize the master task timer
		this.mwRunnable = new MWRunnable(this);
		//Start the timer (via this plugin, first param is delay in Longs, second is repeat delay in longs)
		this.mwRunnable.runTaskTimer(this, 1L, 1L);
	}
	
	@Override
	public void onDisable()
	{
		//Terminate the master task timer
		this.mwRunnable.cancel();
		
		//Remove customized entities
		MWEntityType.unregisterEntities();
		
		//Wipe all players from all games
		for (Game game : this.gamesByPlayer.values())
		{
			game.removeAllPlayers();
		}
	}
	
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args)
	{
		boolean result = false;
		
		if(cmd.getName().equalsIgnoreCase("mobwars"))
		{	
			if (sender instanceof Player)
			{
				Player player = (Player) sender;
				
				result = commandHandler.handlePlayerCommand(player, args);
			}
		}
		
		return result;
	}
	
	public void setGameOfPlayer(Player player, Game game)
	{
		this.gamesByPlayer.put(player, game);
	}
	
	//Get the game that the player is part of
	public Game getGameFromPlayer(Player player)
	{
		return this.gamesByPlayer.get(player);
	}
	
	public void removePlayerFromGame(Player player)
	{
		this.gamesByPlayer.remove(player);
	}
	
	public void clearGamesByPlayer()
	{
		this.gamesByPlayer.clear();
	}
	
	//----------------------------------------------------
	//DEBUG! REMOVE WHEN DONE
	//----------------------------------------------------
	static public void reportDebug(String message)
	{
		if (MainMobWars.showDebugMessages)
		{
			Bukkit.broadcastMessage(message);
		}
	}
}
