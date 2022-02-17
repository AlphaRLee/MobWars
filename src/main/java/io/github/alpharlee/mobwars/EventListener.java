package io.github.alpharlee.mobwars;

import org.bukkit.Bukkit;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.entity.EntityTargetEvent.TargetReason;
import org.bukkit.event.entity.EntityTargetLivingEntityEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;

import com.rlee.mobwars.mobs.MWEntity;
import com.rlee.mobwars.mobs.MWEntityCreeper;
import com.rlee.mobwars.userinterface.ItemHandler;

import net.md_5.bungee.api.ChatColor;

public class EventListener implements Listener
{
	protected MainMobWars main = null; //Direct container to MainMobWars 
	
	public EventListener (MainMobWars mainPlugin)
	{
		//Set mainMW to the MainMobWars initiator
		main = mainPlugin;
		
		main.getServer().getPluginManager().registerEvents(this, main);
	}
	
	@EventHandler
	public void onPlayerQuit (PlayerQuitEvent event)
	{
		Player player = event.getPlayer();
		
		if (this.main.getGameFromPlayer(player) != null)
		{
			this.main.getGameFromPlayer(player).removePlayer(player);
		}
	}
	
	/**
	 * Handle all player item interactions, including "menu" items and spawn eggs
	 * @param event
	 */
	@EventHandler (priority = EventPriority.HIGH)
	public void onPlayerInteract (PlayerInteractEvent event)
	{		
		boolean shouldCancel = false;
		
		Player player = event.getPlayer();
		ItemStack mainHandItem = player.getInventory().getItemInMainHand();
		
		Game game = this.main.getGameFromPlayer(player);
		ItemHandler itemHandler;
		
		if (game != null)
		{
			itemHandler = game.getItemHandler();
			
			if (itemHandler != null)
			{
				shouldCancel = itemHandler.useItem(player, mainHandItem, event.getAction());
			}
		}
		
		//Cancel the event only if commanded to. Do not force it to not cancel (causes problems with other plugins if plugin is forced not to be disabled)
		if (shouldCancel)
		{
			event.setCancelled(true);
		}
	}
	
	/**
	 * Handle all player item drop interactions
	 * @param event
	 */
	@EventHandler
	public void onPlayerDropItem (PlayerDropItemEvent event)
	{
		boolean shouldCancel = false;
		
		Player player = event.getPlayer();
		ItemStack mainHandItem = event.getItemDrop().getItemStack();
		
		Game game = this.main.getGameFromPlayer(player);
		ItemHandler itemHandler;
		
		if (game != null)
		{
			itemHandler = game.getItemHandler();
			
			if (itemHandler != null)
			{
				shouldCancel = itemHandler.dropItem(player, mainHandItem);
			}
		}
		
		//Cancel the event only if commanded to. Do not force it to not cancel (causes problems with other plugins)
		if (shouldCancel)
		{
			event.setCancelled(true);
		}
	}
	
	@EventHandler (priority = EventPriority.NORMAL)
	public void onEntityDamagedByEntity (EntityDamageByEntityEvent event)
	{
Game game;
		
		net.minecraft.server.v1_10_R1.EntityLiving nmsEntity = null;
		MWEntity mwEntity = null;
		
		net.minecraft.server.v1_10_R1.EntityLiving nmsDamager = null;
		MWEntity mwDamager = null;
		
		Player player;
		ItemHandler itemHandler;
		
		Team damagerTeam;
		Team team;
		
		boolean shouldCancel = false;
		
		if (event.getEntity() instanceof LivingEntity)
		{
			nmsEntity = MWEntityType.getNMSLivingFromBukkitLiving((LivingEntity) event.getEntity());
			
			if (nmsEntity != null && nmsEntity instanceof MWEntity)
			{
				mwEntity = (MWEntity) nmsEntity;
				
				team = mwEntity.getTeam();
				
				if (event.getDamager() instanceof Player)
				{
					player = (Player) event.getDamager();
					game = this.main.getGameFromPlayer(player);
					
					if (game != null)
					{
						itemHandler = game.getItemHandler();
						
						if (itemHandler != null)
						{
							shouldCancel = itemHandler.useItem(player, player.getInventory().getItemInMainHand(), Action.LEFT_CLICK_AIR);
						}
					}
				}
				//KLUDGE: Not direclty embedding code, only using to cast attacker to nms
				else if (event.getDamager() instanceof LivingEntity)
				{
					nmsDamager = MWEntityType.getNMSLivingFromBukkitLiving((LivingEntity) event.getDamager());
				}
				else if (event.getDamager() instanceof Projectile && ((Projectile) event.getDamager()).getShooter() instanceof LivingEntity)
				{
					//Set the nmsDamager to be the shooter of the projectile
					nmsDamager = MWEntityType.getNMSLivingFromBukkitLiving((LivingEntity) ((Projectile) event.getDamager()).getShooter());
				}
				
				//If the nmsDamager was declared somewhere, test to see if it's a MWEntity
				if (nmsDamager != null && nmsDamager instanceof MWEntity)
				{	
					mwDamager = (MWEntity) nmsDamager;
					damagerTeam = mwDamager.getTeam();
					
					//NO_TEAM members are not affiliated to one another
					//Test to see if the damager is a teammate of the attacked entity
					if (damagerTeam!= Team.NO_TEAM && damagerTeam == team)
					{
						//This entity is a teammate
						//Disable event
						shouldCancel = true;
					}
				}
			}
		}
		
		//Cancel the event only if commanded to. Do not force it to not cancel (causes problems with other plugins)
		if (shouldCancel)
		{
			event.setCancelled(true);
		}
	}
	
	@EventHandler
	public void onEntityDie (EntityDeathEvent event)
	{
		net.minecraft.server.v1_10_R1.EntityInsentient nmsEntity = MWEntityType.getNMSInsentientFromBukkitLiving((LivingEntity) event.getEntity());
		MWEntity mwEntity;
		
		//Remove the MWEntity if applicable
		if (nmsEntity != null && nmsEntity instanceof MWEntity)
		{	
			mwEntity = (MWEntity) nmsEntity;
			
			mwEntity.getGame().getSpawnHandler().removeMWEntity(nmsEntity, true);
		}
	}
	
	@EventHandler
	public void onEntityTarget (EntityTargetLivingEntityEvent event)
	{	
		net.minecraft.server.v1_10_R1.EntityInsentient nmsEntity = null;
		MWEntity mwEntity;
		
		if (event.getEntity() instanceof LivingEntity)
		{
			nmsEntity = MWEntityType.getNMSInsentientFromBukkitLiving((LivingEntity) event.getEntity());
		}
		
		if (nmsEntity != null && nmsEntity instanceof MWEntity)
		{
			mwEntity = (MWEntity) nmsEntity;
			
			//If the entity  lost its focus
			if (event.getReason() == TargetReason.TARGET_DIED)
			{	
				//Technically redundant. EntityDeath event triggers all attackers to cease attack
				mwEntity.getGame().getAttackHandler().entityStopAttack(nmsEntity);	
			}
		}
	}
	
	/**
	 * Unregister MW Creeper after explosion
	 * @param event EntityExplodeEvent called when creeper explodes
	 */
	@EventHandler
	public void onEntityExplode (EntityExplodeEvent event)
	{
		net.minecraft.server.v1_10_R1.EntityInsentient nmsEntity = null;
		MWEntity mwEntity;
		
		if (event.getEntity() instanceof LivingEntity)
		{
			nmsEntity = MWEntityType.getNMSInsentientFromBukkitLiving((LivingEntity) event.getEntity());
		}
		
		if (nmsEntity != null && nmsEntity instanceof MWEntity)
		{
			mwEntity = (MWEntity) nmsEntity;
			
			if (mwEntity instanceof MWEntityCreeper)
			{
				Bukkit.broadcastMessage(ChatColor.DARK_GREEN + "Creeper exploded!");
				
				mwEntity.getGame().getSpawnHandler().removeMWEntity(nmsEntity, true);
			}
		}
	}
}
