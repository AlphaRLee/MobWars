package io.github.alpharlee.mobwars.userinterface;

import com.rlee.mobwars.Game;
import com.rlee.mobwars.MWEntityType;
import com.rlee.mobwars.MainMobWars;
import com.rlee.mobwars.Selector;
import com.rlee.mobwars.Team;
import com.rlee.mobwars.Selector.SelectState;

import net.md_5.bungee.api.ChatColor;
import net.minecraft.server.v1_10_R1.EntityInsentient;

import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_10_R1.inventory.CraftItemStack;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.material.SpawnEgg;

public class ItemHandler
{
	public Game game;
	
	public static ItemStack selectWand = new ItemStack(Material.BLAZE_ROD);
	public static ItemStack commandWand = new ItemStack(Material.SPECTRAL_ARROW);
	
	static
	{
		String selectWandLore[] = 
			{
				ChatColor.GOLD + "Select your mobs",
				ChatColor.GOLD + "to command",
				"",
				ChatColor.BLUE + "CLICK" + ChatColor.WHITE + " mob to select it",
				ChatColor.BLUE + "CLICK" + ChatColor.WHITE + " 2 corners to select all mobs inside",
				ChatColor.DARK_PURPLE + "SHIFT-CLICK" + ChatColor.WHITE + " mob to add/remove"
			};
		
		String commandWandLore[] =
			{
					ChatColor.GOLD + "Command your group to move",
					ChatColor.GOLD + "or attack",
					"",
					ChatColor.GREEN + "RIGHT CLICK" + ChatColor.WHITE + " block to move mobs to block",
					ChatColor.RED + "LEFT CLICK" + ChatColor.WHITE + " target mob to attack",
					ChatColor.RED + "LEFT CLICK" + ChatColor.WHITE + " 2 corners to target all mobs inside",
					"",
					ChatColor.ITALIC + "You must select mobs",
										"from your team first"
			};
		
		setItemStackMeta(selectWand, 
				ChatColor.GOLD + "Select Wand: "
						+ ChatColor.BLUE + ChatColor.BOLD + "CLICK" + ChatColor.RESET + " - " + ChatColor.GOLD + "Select ", 
				selectWandLore, true);
		
		setItemStackMeta(commandWand,
				ChatColor.GOLD + "Command Wand: "
						+ ChatColor.RED + ChatColor.BOLD + "L-CLICK" + ChatColor.RESET + " - " + ChatColor.GOLD + "Attack " 
						+ ChatColor.GREEN + ChatColor.BOLD + "R-CLICK" + ChatColor.RESET + " - " + ChatColor.GOLD + "Move",
				commandWandLore, true);
	}
	
	public ItemHandler(Game game)
	{
		this.game = game;	
	}
	
	public Game getGame()
	{
		return this.game;
	}
	
	/**
	 * Set the meta for an item stack (cosmetic). Item will default to unbreakable and always hide enchants and unbreakable flags.
	 * @param item ItemStack to set meta for
	 * @param displayName Display name
	 * @param lore Lore
	 * @param addEnchantShine Set to true to add unbreaking X to item (give a shiny luster)
	 */
	public static void setItemStackMeta(ItemStack item, String displayName, String lore[], boolean addEnchantShine)
	{
		Map<Enchantment, Integer> enchants = new HashMap<Enchantment, Integer>();
		
		if (addEnchantShine)
		{
			//Random enchant. Not terribly important which one, since it will be hidden anyway
			enchants.put(Enchantment.DURABILITY, 10);
		}
		
		setItemStackMeta(item, displayName, lore, enchants, true, true);
	}
	
	/**
	 * Set the meta for an item stack
	 * @param item ItemStack to set meta for
	 * @param displayName Display name
	 * @param lore Lore
	 * @param enchants Key represents desired enchant, values represents level. Will always aloow enchants beyond the maximum conventional level
	 * @param unbreakable Set to true for unbreakable
	 * @param hideFlags Set to true to hide enchants and unbreakable flag
	 */
	public static void setItemStackMeta(ItemStack item, String displayName, String lore[], Map<Enchantment, Integer> enchants, boolean unbreakable, boolean hideFlags)
	{
		ItemMeta itemMeta = item.getItemMeta();
		
		itemMeta.setDisplayName(displayName);
		itemMeta.setLore(getArrayListFromArray(lore));
		
		for (Entry<Enchantment, Integer> entry : enchants.entrySet())
		{
			itemMeta.addEnchant(entry.getKey(), entry.getValue(), true);
		}
		
		itemMeta.spigot().setUnbreakable(unbreakable);
		
		if (hideFlags)
		{
			itemMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
			itemMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
		}
		
		item.setItemMeta(itemMeta);
	}
	
	/**
	 * Convenience method: Convert an array into an arraylist
	 * @param args Array of strings
	 * @return ArrayList instance of array
	 */
	private static ArrayList<String> getArrayListFromArray (String args[])
	{
		ArrayList<String> returnList = new ArrayList<String>();
		
		if (args.length > 0)
		{
			for (int i = 0; i < args.length; i++)
			{
				returnList.add(args[i]);
			}
		}
		
		return returnList;
	}
	
	/**
	 * Handles the player interacting with an item
	 * @param player Player
	 * @param item Item being interacted with
	 * @param action Item interaction (Eg. Action.LEFT_CLICK_AIR)
	 * @return true if the handler ran successfully and the event source is recommended to be cancelled. 
	 */
	public boolean useItem(Player player, ItemStack item, Action action)
	{
		boolean suggestCancel = false;
		
		Selector selector = this.getGame().selectorsByPlayer.get(player);
		
		EntityType spawnType;
		
		//Selector-based items. Verify selector exists
		if (selector != null)
		{
			if (item.isSimilar(ItemHandler.selectWand))
			{
				suggestCancel = selector.useSelect(action);
			}
			else if (item.isSimilar(ItemHandler.commandWand))
			{
				suggestCancel = selector.useCommand(action);
			}
		}
		
		if (item.getType() == Material.MONSTER_EGG)
		{
			//--------------------------------------------------------------
			//KLUDGE: Add a proper permissions check!
			//--------------------------------------------------------------
			if (!player.hasPermission("MobWars.spawnEntity"))
			{
				player.sendMessage(ChatColor.RED + "We love you and all, but we are still in alpha testing. Ask an admin for MW permissions");
				player.sendMessage(ChatColor.RED + "In the meanwhile, enjoy a vanilla mob! They're still wonderful companions and deserve lots of love");
				return false;
			}
			
			if (action == Action.RIGHT_CLICK_BLOCK)
			{
				//----------------------------------------------------------------------------------------
				//KLUDGE: This is a convenience method and is prone to breaking. Disable when done
				//----------------------------------------------------------------------------------------
				player.sendMessage(ChatColor.RED + "This is a temporary function!");
				
				//Get the entity type by casting the item as its nms counterpart and finding the entity tag string
				spawnType = EntityType.fromName(CraftItemStack.asNMSCopy(item).getTag().getCompound("EntityTag").getString("id"));
				
				switch (spawnType)
				{
				case RABBIT:			
					spawnType = EntityType.GIANT;
					player.sendMessage(ChatColor.BLUE + "How did YOU get in there?!?");
				break;
				
				default:
					//Do nothing
					break;
				}
				
				//Spawn the requested entity
				this.getGame().getSpawnHandler().spawnMWEntity(spawnType, this.getGame(), this.getGame().getPlayersTeam(player),
						player.getTargetBlock(Selector.transparentMaterials, 10).getLocation().add(0.5, 1, 0.5));
				
				suggestCancel = true;
			}
		}
		
		return suggestCancel;
	}
	
	/**
	 * Handles the player dropping an item
	 * @param player Player
	 * @param item Item being dropped
	 * @return true if the handler ran successfully and the event is recommended to be cancelled. 
	 */
	public boolean dropItem(Player player, ItemStack item)
	{
		boolean suggestCancel = false;
		
		Selector selector = this.getGame().selectorsByPlayer.get(player);
		
		//Selector-based items. Verify selector exists
		if (selector != null)
		{
			if (item.isSimilar(ItemHandler.selectWand) || item.isSimilar(ItemHandler.commandWand))
			{
				selector.clearPos();
				selector.clearSelectedEntities();
				
				//------------------------------------------------------------------------
				//KLUDGE: Move this message to somewhere inside the selector
				//------------------------------------------------------------------------
				player.sendMessage(ChatColor.DARK_AQUA + "Mob selection cleared");
				
				suggestCancel = true;
			}
			
		}
		
		return suggestCancel;
	}
}
