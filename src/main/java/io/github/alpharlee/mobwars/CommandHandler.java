package io.github.alpharlee.mobwars;

import org.apache.commons.lang.WordUtils;
import org.bukkit.ChatColor;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import com.rlee.mobwars.mobs.MWEntityChicken;
import com.rlee.mobwars.userinterface.ItemHandler;

/**
 * Handler for all incoming commands 
 * starting with mobstudio or ms
 * @author RLee
 *
 */
public class CommandHandler
{
	MainMobWars main;
	
	public CommandHandler (MainMobWars mainPlugin)
	{
		this.main = mainPlugin;
	}
	
	/**
	 * Handle player commands that start with the "/mobwars" or "/mw" command
	 * @param player Player sending command
	 * @param args All arguments attached to the command
	 * @return N/A
	 */
	public boolean handlePlayerCommand(Player player, String[] args)
	{
		boolean result = false;
		
		if(args.length > 0)
		{
			//Convert all args to lowercase
			for (int i = 0; i < args.length; i++)
			{
				args[i] = args[i].toLowerCase();
			}
			
			//DEBUG FUNCTION
			if (args[0].contains("test"))
			{
				player.sendMessage(ChatColor.DARK_RED + "This is a test function!");
			}
			
			//Get the first argument
			switch (args[0])
			{
			case "givetest":
				
				if (args.length > 1)
				{
					switch (args[1])
					{
					case "select": case "selectwand": case "select_wand":
						
						player.getInventory().addItem(ItemHandler.selectWand);
						player.sendMessage(ChatColor.GREEN + "Wand received! Start clicking stuff! Remember to join the game!");
						
						break;
						
					case "command": case "commandwand":
						
						player.getInventory().addItem(ItemHandler.commandWand);
						player.sendMessage(ChatColor.GREEN + "Cmd Wand received! Start clicking stuff! Remember to select group!");
						
						break;
					}
				}
			
				break;
				
			case "jointest":
				
				if (args.length > 1)
				{
					this.main.testGame.addPlayer(player, Team.getTeamByName(args[1]));
				}
				else
				{
					this.main.testGame.addPlayer(player);
				}

				player.sendMessage(ChatColor.GREEN + "Welcome to the test game! You're now on " + this.main.testGame.getPlayersTeam(player).getName() + " team");
				
				break;
				
			case "leave":
				
				if (this.main.getGameFromPlayer(player) != null)
				{
					this.main.getGameFromPlayer(player).removePlayer(player);
				}
				else
				{
					player.sendMessage(ChatColor.RED + "We know you miss us so much, but you're not in any game right now.");
				}
			
				break;
				
			case "spawntest":
				
				//--------------------------------------------------------------
				//KLUDGE: Add a proper permissions check!
				//--------------------------------------------------------------
				if (!player.hasPermission("MobWars.spawnEntity"))
				{
					player.sendMessage(ChatColor.RED + "We love you and all, but we are still in alpha testing. Ask an admin for MW permissions");
					break;
				}
				
				if (args.length > 1)
				{	
					this.main.getGameFromPlayer(player).getSpawnHandler().spawnMWEntity(args[1], this.main.testGame, 
							(args.length > 2 ? Team.getTeamByName(args[2]) : this.main.getGameFromPlayer(player).getPlayersTeam(player)), player.getLocation());
					
					player.sendMessage(ChatColor.GREEN + "Finished " + args[1] + " spawn test");
				}
				else
				{
					player.sendMessage(ChatColor.RED + "Error: Please specify which mob you would like to spawn");
				}
				
				break;
			
			case "setteamtest":
				
				if (args.length > 1)
				{
					this.main.getGameFromPlayer(player).setPlayersTeam(player, Team.getTeamByName(args[1]));
				}
				else
				{
					player.sendMessage(ChatColor.RED + "Error: Please specify which team to change to");
				}
				
				break;
				
			case "crittest":
				
				MainMobWars.testCriteria = TargetCriteria.getByName(args[1]);
				
				player.sendMessage(ChatColor.LIGHT_PURPLE + "Test criteria changed to: " + MainMobWars.testCriteria.getName());
				
				break;
			
			case "chickenragetest": case "chickragetest":
				
				if (args.length > 1)
				{
					if (main.getGameFromPlayer(player) != null)
					{
						//KLUDGE: Super sloppy laziness
						int chickenCount = 0;
						
						for (net.minecraft.server.v1_10_R1.EntityInsentient entity : main.getGameFromPlayer(player).selectorsByPlayer.get(player).selectedEntities)
						{
							if (entity instanceof MWEntityChicken)
							{
								((MWEntityChicken) entity).setEnrageChance(Float.valueOf(args[1]));
								chickenCount++;
							}
						}
						
						player.sendMessage(ChatColor.DARK_GREEN + "" + chickenCount + " chickens enrage chance set to " + args[1]);
						player.sendMessage(ChatColor.DARK_GREEN + "G-F#-D#-A-G#-E-G#-C");
					}
				}
				
				break;
				
			case "showdebug":
				
				if (args.length > 1)
				{
					MainMobWars.showDebugMessages = Boolean.getBoolean(args[1]);
				}
				else
				{
					//Invert the current message status
					MainMobWars.showDebugMessages = !MainMobWars.showDebugMessages;
				}
				
				player.sendMessage(ChatColor.DARK_RED + "MW Debug messages shown: " + Boolean.toString(MainMobWars.showDebugMessages));
				
				break;
				
				//Display help screen
			case "help": case "?": 
				
				this.showHelpMenu(player);
				
				break;
				
			default:
				
				//Refer to help screen
				player.sendMessage(ChatColor.RED + "Type /mw help or /mw ? for help menu");	
				
				break;
			}			
		}
		else
		{
			/////////////////KLUDGE: Create function dedicated to help commands ////////////////////////
			player.sendMessage(ChatColor.RED + "Type /mw help or /mw ? for help menu");
		}
		
		return result;
	}
	
	private void showHelpMenu(Player player)
	{
		int COMMAND_NAME = 0;
		int COMMAND_DESCRIPTION = 1;
		
		String[][] helpArray = 
			{
				{"spawntest <entity> <team>", "Spawn an entity on the specified team. Defaults to your team if blank"},
				{"givetest <item>", "Get the tools used in MobWars (eg. /mw givetest select, /mw givetest command)"},
				{"jointest <team>", "Join the test game on the specified team. Defaults to first open team if blank"},
				{"setteamtest <team>", "Change to the specified team"},
				{"leave", "Leave the current MobWars game you are in. But we'd hate to see you leave"}
			};
		
		player.sendMessage(ChatColor.RED + "Under construction! Seek aid from the almighty Jelly Dragon ;)");
		player.sendMessage(ChatColor.GRAY + "But since I like you, I'll share a couple tips");
		
		/*
		for (int i = 0; i < helpArray[COMMAND_NAME].length; i++)
		{
			player.sendMessage(ChatColor.GOLD + "/mw " + helpArray[i][COMMAND_NAME] + ChatColor.WHITE + " - " + helpArray[i][COMMAND_DESCRIPTION]);
		}
		*/
		
		for (String[] stringArr : helpArray)
		{
			for (int i = 0; i < stringArr.length; i++)
			{
				player.sendMessage(ChatColor.GOLD + "/mw " + stringArr[COMMAND_NAME] + ChatColor.WHITE + " - " + stringArr[COMMAND_DESCRIPTION]);
			}
		}
	}
}
