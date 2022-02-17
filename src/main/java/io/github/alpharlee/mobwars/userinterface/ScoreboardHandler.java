package io.github.alpharlee.mobwars.userinterface;

import java.util.ArrayList;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;

import com.rlee.mobwars.Game;

import net.md_5.bungee.api.ChatColor;
import net.minecraft.server.v1_10_R1.EntityInsentient;

public class ScoreboardHandler
{
	public Game game;
	public Player player;
	
	public ScoreboardManager manager = Bukkit.getScoreboardManager();
	
	public ScoreboardHandler (Game game, Player player)
	{
		this.game = game;
		this.player = player;
	}
	
	public void showSelected(ArrayList<EntityInsentient> selectedEntities)
	{
		Scoreboard board = this.manager.getNewScoreboard();
		Objective objective = board.registerNewObjective("selectedHealth", "dummy");
		
		objective.setDisplayName(ChatColor.GOLD + "Selected Mobs " + ChatColor.WHITE + " | " + ChatColor.RED + "Health");
		objective.setDisplaySlot(DisplaySlot.SIDEBAR);
		
		//----------------------------Incomplete-----------------------------------------------------------
	}
}
