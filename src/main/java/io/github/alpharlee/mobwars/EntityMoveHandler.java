package io.github.alpharlee.mobwars;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.ChatColor;
import org.bukkit.Location;

import com.rlee.mobwars.mobs.MWEntity;
import com.rlee.mobwars.mobs.MWEntityGhast;

import net.minecraft.server.v1_10_R1.EntityInsentient;

/**
 * Handle all movement operations for all MWEntities
 * @author RLee
 *
 */
public class EntityMoveHandler
{
	public Game game;
	
	public Map<EntityInsentient, Location> destinationByEntity = new HashMap<EntityInsentient, Location>();
	
	public EntityMoveHandler(Game game)
	{
		this.game = game;
	}
	
	public Game getGame()
	{
		return this.game;
	}
	
	/**
	 * Move the MWEntity to the requested location
	 * MWEntities will lose their attack targets during this operation
	 * @param entity entity Entity to be moved. Must be a MWEntity
	 * @param loc Target location
	 * @param activeMovement Set to true to register target location
	 */
	public void moveEntity(EntityInsentient entity, Location loc, boolean activeMovement)
	{
		this.moveEntity(entity, loc.getX(), loc.getY(), loc.getZ(), activeMovement);
	}
	
	/**
	 * Move the MWEntity to the requested location.
	 * MWEntities will lose their attack targets during this operation
	 * Set registerDestination to true to register their target location in this.destinationByEntity
	 * @param entity Entity to be moved. Must be a MWEntity
	 * @param x X coordinate to travel to
	 * @param y Y coordinate to travel to
	 * @param z Z coordinate to travel to
	 * @param activeMovement Set to true to move in active fashion, set to false to move in a passive fashion
	 */
	public void moveEntity(EntityInsentient entity, double x, double y, double z, boolean activeMovement)
	{	
		MWEntity mwEntity;
		MWEntityGhast mwGhast;
		
		double ghastHoverHeight = 10D;
		
		if (entity instanceof MWEntity)
		{
			mwEntity = (MWEntity) entity;
		}
		else
		{
			//Do not move entity if not MW-related
			return;
		}
		
		if (activeMovement)
		{
			MainMobWars.reportDebug(ChatColor.GREEN + "Began moving! Called stop attack!");
			
			//Set the entity to a move status
			//PLEASE NOTE: The attackhandler.entityStopAttack() checks for the MWEntityAction. StackOverflow prone to happen if the mwEntity action is misplaced
			mwEntity.setAction(MWEntityAction.ACTIVE_MOVING);
			
			//Clear the attack boards only if the activeMovement flag is toggled
			this.getGame().getAttackHandler().entityStopAttack(entity);
		}
		
		if (activeMovement)
		{
			mwEntity.setAction(MWEntityAction.ACTIVE_MOVING);			
		}
		else
		{
			mwEntity.setAction(MWEntityAction.PASSIVE_MOVING);
		}
		
		//Ghasts require a special way to control their movements
		if (mwEntity instanceof MWEntityGhast)
		{
			mwGhast = (MWEntityGhast) mwEntity;
			//Move the ghast to the selected location, plus a fixed raise above the target block
			//------------------------------------------------------------------------------
			//KLUDGE: Later must introduce a proper pathfinder into ghasts, they require explicit directions to navigate right now
			//------------------------------------------------------------------------------
			mwGhast.setMoveToLoc(x, y + ghastHoverHeight, z);
		}
		else
		{
			//All other entities
			//Use the EntityInsentient.getNavigation.a() method. KLUDGE: Default speed is set to 1.0D
			//WARNING: Prone to break in version updates!
			entity.getNavigation().a(x, y, z, 1.0D);
		}
		
		//Declare where this entity is moving to
		if (activeMovement)
		{
			if (mwEntity instanceof MWEntityGhast)
			{
				this.registerDestination(entity, x, y + ghastHoverHeight, z);
			}
			else
			{
				this.registerDestination(entity, x, y, z);
			}
		}
	}
	
	/**
	 * Move a group of entities towards a specific location
	 * @param entities Entities to move. If member is not MWEntity, will not move
	 * @param loc Location to move to
	 * @param activeMovement Set to true to register target location
	 */
	public void moveGroup(ArrayList<EntityInsentient> entities, Location loc, boolean activeMovement)
	{
		for (EntityInsentient entity : entities)
		{
			this.moveEntity(entity, loc, activeMovement);
		}
	}
	
	/**
	 * Handles ALL MW Entities in the game
	 * Sets the entity's status to waiting when they stop moving or the distance to their targetblock is lesser than the preset tolerated range
	 * Unregisters the entity's destination once they have reached their target
	 */
	public void allStopAtDestination()
	{
		MWEntity mwEntity;
		//MWEntity mwEntityNeighbor;
		
		Location entityLoc;
		final double minDistanceConst = 0.6/0.6; //0.5 denotes the distance from the target, and 0.6 denotes the standard width of an entity (based off of zombie/skeleton width)
		double entityWidth = 0.6;
		double minDistanceSquared = 0;
		//double minNeighborDistance = 0.3D;
		//double minNeighborDistanceSquared = minNeighborDistance * minNeighborDistance;
		
		for (Entry<EntityInsentient,Location> entry : this.destinationByEntity.entrySet())
		{
			if (entry.getKey() instanceof MWEntity)
			{
				mwEntity = (MWEntity) entry.getKey();
				entityLoc = MWEntityType.getEntityLocation(entry.getKey());
				
				entityWidth = entry.getKey().width;
				
				//Scale the minDistanceSquared based off of the width of the entity
				minDistanceSquared = minDistanceConst * entityWidth  * minDistanceConst * entityWidth;
				
				//If the entity is within the minimum tolerance distance to the target block
				if ((mwEntity.getAction() == MWEntityAction.ACTIVE_MOVING || mwEntity.getAction() == MWEntityAction.PASSIVE_MOVING) && entityLoc.distanceSquared(entry.getValue()) <= minDistanceSquared)
				{
					//MainMobWars.reportDebug(ChatColor.DARK_PURPLE + "Stopped Movement! MWEntityAction.WAITING");
					MainMobWars.reportDebug(ChatColor.LIGHT_PURPLE + "At destination, dist: " + entityLoc.distanceSquared(entry.getValue()) + " <= " + minDistanceSquared);
					
					//Set the entity to stop and declare that it has arrived at its destination
					this.registerDestination(entry.getKey(), entityLoc);
					mwEntity.setAction(MWEntityAction.WAITING);
				}
				/*
				 * Challenge: prevent entities from bumping into one another to get to the same square
				else
				{
					for (EntityInsentient neighbor : this.getGame().getTeamList(mwEntity.getTeam()))
					{
						if (neighbor instanceof MWEntity && ((MWEntity) neighbor).getAction() == MWEntityAction.AT_DESTINATION)
						{
							
						}
					}
				}
				 */
			}
		}
	}
	
	
	/**
	 * Command an entity to passively move towards its registered destination
	 * @param entity Entity to move
	 */
	public void passiveMoveToDestination(EntityInsentient entity)
	{
		MWEntity mwEntity;
		
		if (entity instanceof MWEntity)
		{
			mwEntity = (MWEntity) entity;
		}
		else
		{
			//Do not move entity if not MW-related
			return;
		}
		
		//Passively start moving the entity
		this.moveEntity(entity, this.destinationByEntity.get(entity), false);
		mwEntity.setAction(MWEntityAction.PASSIVE_MOVING);
	}
	
	/**
	 * Force all entities who are passively moving to continuously keep moving towards their destination
	 */
	public void allPassiveMove()
	{
		//----------------------------------------------------------------
		//KLUDGE: Are you SURE that this is the best answer?
		//There has to be a better answer than frequently spamming the lines
		//----------------------------------------------------------------
		
		MWEntity mwEntity;
		
		for (EntityInsentient entity : this.destinationByEntity.keySet())
		{
			if (entity instanceof MWEntity)
			{
				mwEntity = (MWEntity) entity;
			}
			else
			{
				//Not a MWEntity, do not command it to move
				mwEntity = null;
				continue;
			}
			
			if (mwEntity.getAction() == MWEntityAction.PASSIVE_MOVING)
			{
				this.passiveMoveToDestination(entity);
			}
		}
	}
	
	/**
	 * Register this entity's target destination in this.destinationByEntity
	 * @param entity Entity to register destination of
	 * @param loc Location to register destination to
	 */
	public void registerDestination(EntityInsentient entity, Location loc)
	{
		this.destinationByEntity.put(entity, loc);
	}
	
	public void registerDestination(EntityInsentient entity, double x, double y, double z)
	{
		this.destinationByEntity.put(entity, new Location(MWEntityType.getBukkitLivingFromNMSLiving(entity).getWorld() , x, y, z));
	}
	
	/**
	 * Removes the entity and its destination from this.destinationByEntity
	 * @param entity Entity to remove
	 */
	public void unregisterDestination(EntityInsentient entity)
	{
		this.destinationByEntity.remove(entity);
	}
}
