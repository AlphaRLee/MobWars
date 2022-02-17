package io.github.alpharlee.mobwars;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.logging.Level;

import org.apache.commons.lang.WordUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_10_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_10_R1.entity.CraftLivingEntity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Skeleton;
import org.bukkit.entity.Skeleton.SkeletonType;
import org.bukkit.inventory.ItemStack;

import com.rlee.mobwars.mobs.MWEntity;

import net.md_5.bungee.api.ChatColor;
import net.minecraft.server.v1_10_R1.EntityInsentient;

/**
 * Handles the spawning and despawning of MW Entities
 * @author RLee
 *
 */
public class EntitySpawnHandler
{
	public Game game;
	
	public EntitySpawnHandler(Game game)
	{
		this.game = game;
	}
	
	public Game getGame()
	{
		return this.game;
	}
	
	public EntityInsentient spawnMWEntity(String entityName, Game game, Team team, Location loc)
	{
		EntityInsentient entity;
		LivingEntity bukkitEntity;
		
		entityName = entityName.toLowerCase();
		EntityType entityType;
		
		SkeletonType skeletonType = SkeletonType.NORMAL;
		
		//-----------------------------------------------------------------------------------------
		//Challenge: Find a way to introduce zombies and their flags
		//-----------------------------------------------------------------------------------------
		//Get all flags to set for this zombie
		//ArrayList<String> zombieFlags = new ArrayList<String>();
		
		//Automatically alter entityName for EntityType.fromName() method
		switch (entityName)
		{
		case "skel": case "skellie": case "skelly":
			//Nick name support
			entityType = EntityType.SKELETON;
			skeletonType = SkeletonType.NORMAL;
			break;
		
		case "witherskeleton": case "wither_skeleton": 
			case "witherskellie": case "witherskelly": case "witherskel":
			
			entityType = EntityType.SKELETON;
			skeletonType = SkeletonType.WITHER;	
			break;	
			
		case "stray":
			
			entityType = EntityType.SKELETON;
			skeletonType = SkeletonType.STRAY;
			break;
		
		/*
		case "husk":
			
			break;
		*/
			
		case "pigzombie": case "zombiepigman": case "pig_zombie":
			entityType = EntityType.PIG_ZOMBIE;
			break;
			
		case "cavespider": case "cave_spider":
			entityType = EntityType.CAVE_SPIDER;
			break;
		
		case "magmacube": case "lavaslime":
			entityType = EntityType.MAGMA_CUBE;
			break;
			
		//Omitting EnderDragon. It's dangerous and scary anyway
		case "wither": case "witherboss":
			entityType = EntityType.WITHER;
			break;
		
		case "ocelot":
			entityType = EntityType.OCELOT;
			break;
			
		case "irongolem": case "villagergolem":
			entityType = EntityType.IRON_GOLEM;
			break;
			
		case "polarbear":
			entityType = EntityType.POLAR_BEAR;
			break;
			
		default:
			entityName = WordUtils.capitalize(entityName);	
			entityType = null;
			break;
		}
		
		//Get the entity by either the declared type above or from the name reference
		entity = spawnMWEntity(entityType != null ? entityType : EntityType.fromName(entityName), game, team, loc);
		
		if (entity != null)
		{
			bukkitEntity = MWEntityType.getBukkitLivingFromNMSLiving(entity);
			
			if (entityType == EntityType.SKELETON || entityName == "skeleton")
			{
				((Skeleton) bukkitEntity).setSkeletonType(skeletonType);
			}
			
			//-----------------------------------------------------------------------------------------
			//KLUDGE: This method is already repeated in the main spawn command
			//Purpose of this time is to override the wither skeleton's item
			//-----------------------------------------------------------------------------------------
			this.setDefaultEquipment(bukkitEntity);
		}
		
		return entity;
	}
	
	/**
	 * Spawn a MWEntity based off of the entityType and team at the designated location
	 * @param entityType Entity to spawn
	 * @param game Game to join this entity into
	 * @param team Team this entity is on
	 * @param loc Location to spawn entity at
	 * @return NMS EntityInsentient representation of this entity, or null if not available
	 */
	public EntityInsentient spawnMWEntity(EntityType entityType, Game game, Team team, Location loc)
	{
		net.minecraft.server.v1_10_R1.World nmsWorld = ((CraftWorld) loc.getWorld()).getHandle();
		
		try
		{	
			//Spawn any class via reflection
			//Assumes all classes have identical constructors
			Class<? extends EntityInsentient> mwEntityClass = MWEntityType.getMWTypeByEntityType(entityType).getCustomClass();
			Constructor<? extends EntityInsentient> mwEntityClassConst;
			
			EntityInsentient nmsEntity;
			LivingEntity bukkitEntity; 
			
			//Initialize only if the class has been found
			if (mwEntityClass != null)
			{
				mwEntityClassConst = mwEntityClass.getConstructor(net.minecraft.server.v1_10_R1.World.class, Game.class, Team.class);
				
				nmsEntity = mwEntityClassConst.newInstance(nmsWorld, game, team);
				bukkitEntity = MWEntityType.getBukkitLivingFromNMSLiving(nmsEntity);
				
				nmsEntity.setPosition(loc.getX(), loc.getY(), loc.getZ());
				
				nmsWorld.addEntity(nmsEntity);
								
				//Set the name to team name (bold and colored) and entity type (default)
				bukkitEntity.setCustomName(MWEntityType.getEntityName(nmsEntity, false, true));
				bukkitEntity.setCustomNameVisible(true);
				
				bukkitEntity.setRemoveWhenFarAway(false);
				
				this.setDefaultEquipment(bukkitEntity);
				
				//Add entity to its respective team
				this.getGame().addEntityToTeam(nmsEntity, team);
				
				//Spawn it as idling and its base location at its spawn location
				((MWEntity) nmsEntity).setAction(MWEntityAction.WAITING);
				this.getGame().getMoveHandler().registerDestination(nmsEntity, loc);
				
				return nmsEntity;
			}
			else
			{
				Bukkit.getLogger().info("ERROR! EntityType " + entityType.toString() + " not found as MWEntity. Please contact developers");
				return null;
			}
		} 
		catch (Exception e)
		{
			Bukkit.broadcastMessage(ChatColor.RED + "Error spawning MWEntity " + entityType.toString() + ", please have an administrator refer to console");
			Bukkit.getLogger().log(Level.SEVERE, "ERROR: MWEntity " + entityType.toString() + "spawn failure. Please copy stack trace and contact developers", e);
		
			return null;
		}	
	}
	
	/**
	 * Arm the entity with the appropriate items from first spawn
	 * @param entity Bukkit version of NMSEntity to equip
	 */
	public void setDefaultEquipment(LivingEntity entity)
	{
		//Vars for specific entity types
		Skeleton skeleton;
		
		switch (entity.getType())
		{
		
		//Equip skeletons with either bows or stone swords
		case SKELETON:
			
			skeleton = (Skeleton) entity;
			
			switch (skeleton.getSkeletonType())
			{
			case WITHER:
				
				skeleton.getEquipment().setItemInMainHand(new ItemStack(Material.STONE_SWORD));
				
				break;
			
			case NORMAL: case STRAY: default:
				
				skeleton.getEquipment().setItemInMainHand(new ItemStack(Material.BOW));
				
				break;
			}
			
			break;
		
		//Do not equip zombies
		case ZOMBIE:
			
			break;
		
		//Equip pig zombies with golden swords
		case PIG_ZOMBIE:
			
			entity.getEquipment().setItemInMainHand(new ItemStack(Material.GOLD_SWORD));
			
			break;
			
		default:
			//Do not equip entity with anything
			break;
			
		}
	}
	
	/**
	 * Called when a MWEntity is to be removed (eg. Entity death event). 
	 * Will remove entity entirely from its game, including killing it
	 * @param entity Entity to remove
	 * @param removeFromRoster Set to true to remove the entity from this.game.entitiesByTeam
	 * 	Set to false to prevent concurrent iteration errors (as in trying to remove entities en masse)
	 */
	public void removeMWEntity(EntityInsentient entity, boolean removeFromRoster)
	{
		MWEntity mwEntity;
		EntityAttackHandler attackHandler = this.getGame().getAttackHandler();
		EntityMoveHandler moveHandler = this.getGame().getMoveHandler();
		
		if (entity instanceof MWEntity)
		{
			mwEntity = (MWEntity) entity;
		}
		else
		{
			//Not a MWEntity
			return;
		}
		
		mwEntity.setAction(MWEntityAction.WAITING);
		
		attackHandler.entityStopAttack(entity);
		attackHandler.unregisterTarget(entity);
		
		moveHandler.unregisterDestination(entity);
		
		for (Selector selector : this.getGame().selectorsByPlayer.values())
		{
			selector.removeEntityFromSelected(entity);
		}
		
		if (removeFromRoster)
		{
			this.getGame().removeEntityFromTeam(entity);
		}
		
		entity.die();
	}
}
