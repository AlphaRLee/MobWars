package io.github.alpharlee.mobwars;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.event.entity.EntityTargetEvent.TargetReason;

import com.rlee.mobwars.mobs.AutoStrikerEntity;
import com.rlee.mobwars.mobs.MWEntity;
import com.rlee.mobwars.mobs.SupportEntity;

import net.minecraft.server.v1_10_R1.EntityInsentient;
import net.minecraft.server.v1_10_R1.EntityLiving;

/**
 * Manages all attack elements for the MWEntities
 * 
 * @author RLee
 *
 */
public class EntityAttackHandler
{
	public Game game;
	
	// Map carrying all entities currently being targeted by team(s)
	public Map<EntityLiving, ArrayList<Team>> attackingTeamsByTarget = new HashMap<EntityLiving, ArrayList<Team>>();

	public EntityAttackHandler(Game game)
	{
		this.game = game;
	}

	public Game getGame()
	{
		return this.game;
	}

	/**
	 * Set an entity to attempt to attack another entity
	 * @param entity Entity to set attack for. Must be MWEntity
	 * @param target Entity being targeted
	 */
	public void entityAttackTarget(EntityInsentient entity, EntityLiving target)
	{
		MWEntity mwEntity;
		
		if (entity instanceof MWEntity)
		{
			mwEntity = (MWEntity) entity;
		}
		else
		{
			//Do nothing if not MWEntity
			return;
		}
		
		MainMobWars.reportDebug(ChatColor.DARK_AQUA + "Began attacking! Called stop attack!");
		
		//Stop the last attack if applicable
		this.entityStopAttack(entity);
		
		if (target != null)
		{
			// Attack the target
			entity.setGoalTarget(target, TargetReason.CUSTOM, true);
			//Set the status of this entity's attack
			mwEntity.setAction(MWEntityAction.ACTIVE_ATTACKING);
			
			MainMobWars.reportDebug(ChatColor.DARK_AQUA + "Action: " + mwEntity.getAction().getName());
			
			this.registerTarget(target, mwEntity.getTeam());
		}
	}
	
	/**
	 * Sets the MWEntity's target to null, causing them to stop their attack.
	 * Sets the MWEntity to idle
	 * @param entity Entity to lose attack. Must be MWEntity
	 */
	public void entityStopAttack(EntityInsentient entity)
	{
		MWEntity mwEntity;
		
		EntityLiving targetEntity = entity.getGoalTarget();
		EntityLiving teammateTarget;
		Boolean targetAttackedByTeam = false;
		
		EntityMoveHandler moveHandler;
		
		if (entity instanceof MWEntity)
		{
			mwEntity = (MWEntity) entity;
			moveHandler = mwEntity.getGame().getMoveHandler();
		}
		else
		{
			//Do nothing if not MWEntity
			return;
		}
		
		MainMobWars.reportDebug(ChatColor.DARK_GREEN + "Stop attack starting! Entity action: " + mwEntity.getAction().getName());
		
		//Stop the attack
		//Do not fire the event associated with it and set the reason for forgetting to be custom
		//Do not fire the event to prevent recursive iterations
		entity.setGoalTarget(null, TargetReason.CUSTOM, false);
		
		if (targetEntity != null)
		{	
			//Test for other teammates attacking this target
			for (EntityInsentient teammate : this.getGame().getTeamList(mwEntity.getTeam()))
			{
				teammateTarget = teammate.getGoalTarget();
				
				if (teammateTarget != null && teammateTarget.equals(targetEntity))
				{
					//Confirmed that one teammate is still attacking this target
					targetAttackedByTeam = true;
					break;
				}
			}
			
			MainMobWars.reportDebug(ChatColor.GOLD + "Stop attack 1! Entity action: " + mwEntity.getAction().getName());
			
			
			if (!targetAttackedByTeam)
			{
				if (this.attackingTeamsByTarget.get(targetEntity) != null)
				{
					//Remove the attacking team's status on this target
					this.attackingTeamsByTarget.get(targetEntity).remove(mwEntity.getTeam());
					
					//Remove the entry altogether if no team is attacking this entity
					if (this.attackingTeamsByTarget.get(targetEntity).isEmpty())
					{
						this.attackingTeamsByTarget.remove(targetEntity);
					}
				}
			}
			
			MainMobWars.reportDebug(ChatColor.GOLD + "Stop attack 2! Entity action: " + mwEntity.getAction().getName());
		}
		
		//If the entity was formerly in pursuit, set its new waiting position at where it stopped its attack
		if (mwEntity.getAction() == MWEntityAction.ACTIVE_ATTACKING)
		{
			//Set the status of the former attacker
			mwEntity.setAction(MWEntityAction.WAITING);
			moveHandler.registerDestination(entity, entity.locX, entity.locY, entity.locZ);
		}
		else if (mwEntity.getAction() == MWEntityAction.PASSIVE_ATTACKING)
		{
			MainMobWars.reportDebug(ChatColor.LIGHT_PURPLE + "Stop attack finished, engaging passiveMoveToDestination");
			
			//Set the status of the former attacker
			mwEntity.setAction(MWEntityAction.WAITING);
			moveHandler.passiveMoveToDestination(entity);
		}
		else
		{
			MainMobWars.reportDebug(ChatColor.DARK_GREEN + "Stop attack finished, now waiting");
			
			//Set the status of the former attacker
			mwEntity.setAction(MWEntityAction.WAITING);
		}
	}
	
	/**
	 * Allow a MWEntity to freely attack other entities that enter its boundary radius. If multiple targets are present, the first target to arrive is prioritized over the target best fitting the TargetCriteria
	 * @param entity Entity attacking. Must be MWEntity
	 * @param criterion TargetCriteria to choose target based off of all applicable entities
	 * @param boundRadius Radius that this entity is bound to based off of its last 
	 * @param allowMultiAttack Allows entity to engage in a target that is already under attack by a teammate
	 */
	public void entityPassiveAttack(EntityInsentient entity, TargetCriteria criterion, double boundRadius, Boolean allowMultiAttack)
	{
		//MWEntity version of this entity
		MWEntity mwEntity;
		
		EntityLiving targetEntity;
		
		Location entityLoc = MWEntityType.getEntityLocation(entity);
		Location targetLoc;
		
		//Get all MWEntities to scan. Crop those not nearby
		ArrayList<EntityLiving> nearbyEntities;
		ArrayList<EntityLiving> notNearbyEntities = new ArrayList<EntityLiving>();
		
		Double radiusSquared = boundRadius * boundRadius;
		
		if (entity instanceof MWEntity)
		{
			mwEntity = (MWEntity) entity;
		}
		else 
		{
			//Entity invalid
			return;
		}
		
		//Test if entity is idle or returning to its idle position
		//Do not engage in attack if entity already is in pursuit
		//Entity must also be part of the AutoStrikerEntity interface
		if (mwEntity instanceof AutoStrikerEntity)
		{
			//Remove target if they left the bound radius
			if (mwEntity.getAction() == MWEntityAction.PASSIVE_ATTACKING && entity.getGoalTarget() != null)
			{
				targetLoc = MWEntityType.getEntityLocation(entity.getGoalTarget());
				
				//Forget any targets that are beyond this entity's radius and remove all targets that lie outside the idle location
				if (entityLoc.distanceSquared(targetLoc) > radiusSquared || this.getGame().getMoveHandler().destinationByEntity.get(entity).distanceSquared(targetLoc) > radiusSquared)
				{
					MainMobWars.reportDebug(ChatColor.LIGHT_PURPLE + "Target out of passive range! Called stop attack!");
					
					this.entityStopAttack(entity);
				}
			}
			
			if (mwEntity.getAction() == MWEntityAction.WAITING || mwEntity.getAction() == MWEntityAction.PASSIVE_MOVING)
			{
				//Set the nearbyEntities to only teammates if this entity is a support entity
				if (mwEntity instanceof SupportEntity)
				{
					//------------------------------------------------------------------------------------------------------------------
					//KLUDGE: because of the if (mwEntity instanceof AutoStrikerEntity) case, this will never be possible
					//------------------------------------------------------------------------------------------------------------------
					
					nearbyEntities = (ArrayList<EntityLiving>) this.getGame().getTeamList(mwEntity.getTeam()).clone();
				}
				else
				{
					//Set the nearbyEntities to anything but teammates if this entity is not a support
					nearbyEntities = (ArrayList<EntityLiving>) this.getGame().getAllMWEntities().clone();
					nearbyEntities.removeAll(this.getGame().getTeamList(mwEntity.getTeam()));
				}
				
				targetLoc = null;
				
				for (EntityLiving target : nearbyEntities)
				{
					targetLoc = MWEntityType.getEntityLocation(target);
					
					//Remove all targets that are beyond this entity's radius and remove all targets that lie outside the idle location
					if (entityLoc.distanceSquared(targetLoc) > radiusSquared || this.getGame().getMoveHandler().destinationByEntity.get(entity).distanceSquared(targetLoc) > radiusSquared)
					{
						notNearbyEntities.add(target);
					}
				}
				
				nearbyEntities.removeAll(notNearbyEntities);
				
				//Set the target from the refined list of nearbyEntities
				targetEntity = this.getBestTargetFromArray(entityLoc, mwEntity.getTeam(), nearbyEntities, criterion, boundRadius, allowMultiAttack);
				
				if (targetEntity != null)
				{
					// Attack the target
					entity.setGoalTarget(targetEntity, TargetReason.TARGET_ATTACKED_ENTITY, true);
					//Set the status of this entity's attack
					mwEntity.setAction(MWEntityAction.PASSIVE_ATTACKING);
					
					this.registerTarget(targetEntity, mwEntity.getTeam());
				}
			}
		}
	}
	
	/**
	 * Set a group of entities to attack a specific target
	 * @param attackers Attacking group. If any of the attackers are not MWEntities, they will fail to attack
	 * @param target Target to attack
	 */
	public void groupAttackTarget(ArrayList<EntityInsentient> attackers, EntityLiving target)
	{
		for (EntityInsentient attacker : attackers)
		{
			this.entityAttackTarget(attacker, target);
		}
	}
		
	/**
	 * Assign one group of MWEntities to attack another group of entities based off of the criteria. 
	 * The best target is given to the attacker that best fits the criteria.
	 * The second best attacker aims at the second best target and so on.
	 * If the attacking group is larger than the target group, the least qualified will distribute to aid the best, second, third etc. targets
	 * @param attackers ArrayList of MWEntities attacking. If a member is not a MWEntity, it will not attack
	 * @param targets ArrayList of entities being attacked
	 * @param criterion Criteria to best choose attacker by
	 */
	public void groupAttackGroup(ArrayList<EntityInsentient> attackers, ArrayList<EntityLiving> targets, TargetCriteria criterion)
	{
		//If the criterion is Nearest or Farthest
		if (criterion.isSourceDependent())
		{
			this.groupAttackGroupDependent(attackers, targets, criterion);
		}
		else
		{
			//criterion is source independent (eg. healthiest, least healthy)
			this.groupAttackGroupIndependent(attackers, targets, criterion);
		}
		
	}
	
	/**
	 * Set a group of entities to attack another group of entities based on the attacker with the best value
	 * to the best target and the second best targeting the second best and so on.
	 * If the attacker list is larger than the target list, the best "leftover" attacker will attack its best target, the second attack the second, and so on.
	 * @param attackers Group of entities that will attack
	 * @param targets Group of entities that is targeted
	 * @param criterion TargetCriteria that must be source dependent
	 */
	private void groupAttackGroupDependent(ArrayList<EntityInsentient> attackers, ArrayList<EntityLiving> targets, TargetCriteria criterion)
	{	
		Map<EntityInsentient, LinkedHashMap<EntityLiving, Double>> sortedTargetsValuesByAttacker = new HashMap<EntityInsentient, LinkedHashMap<EntityLiving, Double>>();
		
		EntityInsentient bestAttacker = null;
		EntityLiving bestTarget;
		
		ArrayList<EntityLiving> entitiesAlreadyTargeted = new ArrayList<EntityLiving>();
		
		if (!criterion.isSourceDependent())
		{
			//Wrong criteria
			return;
		}
		
		//Initialize the sorted map
		for (EntityInsentient attacker : attackers)
		{
			sortedTargetsValuesByAttacker.put(attacker, this.sortEntitiesByCriteria(MWEntityType.getEntityLocation(attacker), targets, criterion));
		}
		
		for (int i = 0; i < attackers.size(); i++)
		{	

			//If all the targets are already under attack by fellow members of the group attack
			if (entitiesAlreadyTargeted.size() >= targets.size())
			{
				//Reset the list so that extra attackers will have their best pick
				entitiesAlreadyTargeted.clear();
			}
			
			bestAttacker = this.getBestAttacker(sortedTargetsValuesByAttacker, entitiesAlreadyTargeted, criterion.isGreatestValue());
			bestTarget = this.getFirstFreeTarget(new ArrayList<EntityLiving>(sortedTargetsValuesByAttacker.get(bestAttacker).keySet()), entitiesAlreadyTargeted);
			
			//Set the entity to attack the first applicable target
			this.entityAttackTarget(bestAttacker, bestTarget);
			
			//Remove this attacker
			sortedTargetsValuesByAttacker.remove(bestAttacker);
			//Set the target status to alreadyTargeted
			entitiesAlreadyTargeted.add(bestTarget);
		}
	}
	
	/**
	 * Get the first sortedTarget that is not already targeted (not already present in the entitiesAlreadyTargeted list)
	 * @param sortedTargets Targets to search through. For optimal effects, this list should be sorted by some fashion
	 * @param entitiesAlreadyTargeted List of entities already targeted  to compare against
	 * @return First entity from sortedTargets that does not appear in entitiesAlreadyTargeted. Returns null if no entity was found
	 */
	private EntityLiving getFirstFreeTarget(ArrayList<EntityLiving> sortedTargets, List<EntityLiving> entitiesAlreadyTargeted)
	{
		int i = this.getFirstFreeTargetIndex(sortedTargets, entitiesAlreadyTargeted);
		
		//If an entry was found
		if (i != (int) MainMobWars.USE_DEFAULT)
		{
			return sortedTargets.get(i);
		}
		else
		{
			//No entry was found
			return null;
		}
	}
	
	/**
	 * Get the index of the first sortedTarget that is not already targeted (not already present in the entitiesAlreadyTargeted list)
	 * @param sortedTargets Targets to search through. For optimal effects, this list should be sorted by some fashion
	 * @param entitiesAlreadyTargeted List of entities already targeted  to compare against
	 * @return Index of the first entity from sortedTargets that does not appear in entitiesAlreadyTargeted. Returns (int) MainMobWars.USE_DEFAULT if no entity was found
	 */
	private int getFirstFreeTargetIndex(ArrayList<EntityLiving> sortedTargets, List<EntityLiving> entitiesAlreadyTargeted)
	{
		for (int i = 0; i < sortedTargets.size(); i++)
		{
			if (!entitiesAlreadyTargeted.contains(sortedTargets.get(i)))
			{
				return i;
			}
		}
		
		return (int) MainMobWars.USE_DEFAULT;
	}
	
	/**
	 * Get the best attacker from a list of attackers (key set of the sortedTargetsValuesByAttacker), based off of the sorted values given to it.
	 * Will attempt to prevent using entites that are already targeted. If no entity is available to attack, null will be returned
	 * @param sortedTargetsValuesByAttacker Map where the key is the attackers and the value is a LinkedHashMap carrying the sorted targets and respective values
	 * @param entitiesAlreadyTargeted List of entities that have already been targeted. No entity from this list will be used to measure by
	 * @param useGreaterValue If set to true, the highest value will be considered the best. If set to false, the lowest value will be considered the best
	 * @return the best EntityInsentient (best value). Null if no entity is or the all targetedEntities are on the entitiesAlreadyTargeted list
	 */
	private EntityInsentient getBestAttacker(Map<EntityInsentient, LinkedHashMap<EntityLiving, Double>> sortedTargetsValuesByAttacker, List<EntityLiving> entitiesAlreadyTargeted, boolean useGreaterValue)
	{
		ArrayList<EntityLiving> sortedTargets;
		ArrayList<Double> sortedValues;
		
		EntityInsentient bestAttacker = null;
		double bestAttackerValue = MainMobWars.USE_DEFAULT;
		
		int firstFreeTargetIndex = (int) MainMobWars.USE_DEFAULT;
		double attackerValue = MainMobWars.USE_DEFAULT;
		
		for (EntityInsentient attacker : sortedTargetsValuesByAttacker.keySet())
		{				
			sortedTargets = new ArrayList<EntityLiving>(sortedTargetsValuesByAttacker.get(attacker).keySet());
			sortedValues = new ArrayList<Double>(sortedTargetsValuesByAttacker.get(attacker).values());
			
			firstFreeTargetIndex = this.getFirstFreeTargetIndex(sortedTargets, entitiesAlreadyTargeted);
			
			if (firstFreeTargetIndex == (int) MainMobWars.USE_DEFAULT)
			{
				//Do not continue if there are no entities still available for targeting
				//(Requires reset for entitiesAlreadyTargeted in this.groupAttackGroupDependent() )
				return null;
			}
			
			attackerValue = sortedValues.get(firstFreeTargetIndex);
			
			//Initialize bestAttacker
			if (bestAttacker == null)
			{
				bestAttacker = attacker;
				//Set the value to the first applicable entry
				bestAttackerValue = attackerValue;
			}
			else
			{
				if (useGreaterValue)
				{
					bestAttacker = bestAttackerValue >= attackerValue ? bestAttacker : attacker;
				}
				else
				{
					bestAttacker = bestAttackerValue <= attackerValue ? bestAttacker : attacker;
				}
			}
		}
		
		return bestAttacker;
	}
	
	/**
	 * Set a group of entities to attack another group of entities based on the best qualified TargetCriteria members facing off, second best facing second best and so on.
	 * Criteria must be source independent (ie. Does not rely on other entities for reference)
	 * @param attackers Group of entities that will attack
	 * @param targets Group of entities that is targeted
	 * @param criterion TargetCriteria that must be source independent
	 */
	private void groupAttackGroupIndependent(ArrayList<EntityInsentient> attackers, ArrayList<EntityLiving> targets, TargetCriteria criterion)
	{
		ArrayList<EntityLiving> livingAttackers = new ArrayList<EntityLiving>();
		
		//Get a sorted list of attackers and targets (use list for iterating purposes)
		List<EntityLiving> sortedAttackers;
		List<EntityLiving> sortedTargets = new ArrayList<EntityLiving>(this.sortEntitiesByCriteria(null, targets, criterion).keySet());
		
		if (criterion.isSourceDependent())
		{
			//Wrong criterion
			return;
		}
		
		//Cast all members of the attackers to EntityLiving
		for (EntityInsentient attacker : attackers)
		{
			livingAttackers.add(attacker);
		}
		
		sortedAttackers = new ArrayList<EntityLiving>(this.sortEntitiesByCriteria(null, livingAttackers, criterion).keySet());
		
		//Set every attacker to aim for the targets, based off their arrangement
		for (int i = 0; i < sortedAttackers.size(); i++)
		{	
			this.entityAttackTarget((EntityInsentient) sortedAttackers.get(i), sortedTargets.get(i % sortedTargets.size()));
		}
	}
	
	/**
	 * Sorts the entities based off of the criteria supplied, returned in the fashion of an LinkedHashMap.
	 * The entry composes of the entity, followed by the requested value from the criteria.
	 * The first entry will be of the entity that best qualifies for this condition, the second for the second best entity, and so on.
	 * @param entities EntityLiving list to sort
	 * @param criterion TargetCriteria to organize entities by
	 * @return ArrayList of entries of entities and their respective values.
	 */
	public LinkedHashMap<EntityLiving, Double> sortEntitiesByCriteria(Location sourceLoc, ArrayList<EntityLiving> entities, TargetCriteria criterion)
	{
		@SuppressWarnings("unchecked")
		ArrayList<EntityLiving> entitiesCopy = (ArrayList<EntityLiving>) entities.clone();
		LinkedHashMap<EntityLiving, Double> valueByEntity = new LinkedHashMap<EntityLiving, Double>();
		
		EntityLiving bestEntity;
		
		for (int i = 0; i < entities.size(); i++)
		{
			//Get the best entity from this list
			//with no dependence on attacking team, radius, or allowMultiAttack
			bestEntity = this.getBestTargetFromArray(sourceLoc, Team.NO_TEAM, entitiesCopy, criterion, MainMobWars.USE_DEFAULT, true);
			
			valueByEntity.put(bestEntity, this.getEntityCriteriaValue(sourceLoc, bestEntity, criterion));
			
			entitiesCopy.remove(bestEntity);
		}
		
		//////////////////////////////////////////////////////////////////////////////////////////////////
		/*
		 * Consider utilizing a treeMap if this method proves ineffective
		 * (treeMaps will automatically sort the information given)
		 */
		
		return valueByEntity;
	}
	
	/**
	 * Get the entity that is best qualified for the specified criterion and
	 * within appropriate radius
	 * 
	 * @param sourceLoc
	 *            Source location to reference if the entity is within radius
	 *            and to reference nearest/farthest entity
	 * @param attackingTeam
	 *            Team belonging to the entity that is targeting this array
	 * @param entityList
	 *            List of all entities being considered
	 * @param criterion
	 * @param radius
	 * @param allowMultiAttack
	 * @return Entity that best meets the criteria
	 */
	public EntityLiving getBestTargetFromArray(Location sourceLoc, Team attackingTeam, ArrayList<EntityLiving> entityList, TargetCriteria criterion, Double radius, Boolean allowMultiAttack)
	{
		EntityLiving bestTarget = null;
		EntityLiving currentTarget = null;

		Double radiusSquared = radius * radius;
		Double targetDistSquared = MainMobWars.USE_DEFAULT;

		for (EntityLiving target : entityList)
		{
			currentTarget = target;
			targetDistSquared = sourceLoc.distanceSquared(MWEntityType.getEntityLocation(target));

			// Check if the current target is within a valid range and the
			// current target is not already targeted if allowMultipleTargets is
			// disabled
			if ((targetDistSquared <= radiusSquared || radius == MainMobWars.USE_DEFAULT)
					&& (!allowMultiAttack || !this.isTeamAttackingEntity(currentTarget, attackingTeam)))
			{
				if (bestTarget == null)
				{
					// Initialize the bestTarget to the first entity only if the
					// currentTarget is within range
					bestTarget = currentTarget;
				} else
				{
					// Best target already found, compare against current target
					bestTarget = this.getBestTarget(sourceLoc, bestTarget, currentTarget, criterion);
				}
			}
		}

		return bestTarget;
	}

	/**
	 * Get whether the current team is attacking the targeted entity or not
	 * 
	 * @param target
	 *            Targeted entity
	 * @param attackingTeam
	 *            Attacking team
	 * @return True if the team is attacking, false if not
	 */
	public boolean isTeamAttackingEntity(EntityLiving target, Team attackingTeam)
	{
		if (this.isEntityBeingAttacked(target))
		{
			// Return true if this entity is being attacked by the team
			return this.attackingTeamsByTarget.get(target).contains(attackingTeam);
		} else
		{
			// Entity not even targeted
			return false;
		}
	}

	/**
	 * Get whether this entity is being attacked by any target or not
	 * 
	 * @param target
	 *            Entity tested to be under attack
	 * @return True if the entity is being targeted, false if not
	 */
	public boolean isEntityBeingAttacked(EntityLiving target)
	{
		return this.attackingTeamsByTarget.containsKey(target);
	}

	/**
	 * Compare two living entities to see which follows the TargetCriteria
	 * better
	 * 
	 * @param sourceLoc
	 *            Source location to compare two entities' distance to
	 * @param entity1
	 *            Entities to compare
	 * @param entity2
	 * @param criterion
	 *            Criterion for the best of the targeted entities to satisfy
	 * @return Returns entity that best qualifies condition
	 */
	public EntityLiving getBestTarget(Location sourceLoc, EntityLiving entity1, EntityLiving entity2, TargetCriteria criterion)
	{
//		Location loc1 = MWEntityType.getBukkitLivingEntityFromNMSLiving(entity1).getLocation();
//		Location loc2 = MWEntityType.getBukkitLivingEntityFromNMSLiving(entity2).getLocation();

//		switch (criterion)
//		{
//		case NEAREST:
//
//			return sourceLoc.distanceSquared(loc1) <= sourceLoc.distanceSquared(loc2) ? entity1 : entity2;
//			
//		case FARTHEST:
//			
//			return sourceLoc.distanceSquared(loc1) >= sourceLoc.distanceSquared(loc2) ? entity1 : entity2;
//
//		case MOST_HEALTHY:
//
//			return entity1.getHealth() >= entity2.getHealth() ? entity1 : entity2;
//
//		case LEAST_HEALTHY:
//
//			return entity1.getHealth() <= entity2.getHealth() ? entity1 : entity2;
//
//		default:
//			// Theoretically impossible
//			return null;
//		}
		
		//If the greatest value is the one desired (Eg. Farthest and Most health)
		if (criterion.isGreatestValue())
		{
			//Compare the two values and return the greater
			return this.getEntityCriteriaValue(sourceLoc, entity1, criterion) >= this.getEntityCriteriaValue(sourceLoc, entity2, criterion) ? entity1 : entity2;
		}
		else
		{
			//Compare the two values and return the lesser
			return this.getEntityCriteriaValue(sourceLoc, entity1, criterion) <= this.getEntityCriteriaValue(sourceLoc, entity2, criterion) ? entity1 : entity2;	
		}
		

	}
	
	/**
	 * Return the value measured from the specified criterion for the specific entity
	 * PLEASE NOTE: All distances are squared. To get the actual distance, the square root operator must be applied
	 * @param sourceLoc Source location to reference distance measurements to
	 * @param entity EntityLiving to calculate values for
	 * @param criterion TargetCriteria to calculate based off of
	 * @return double representing the measured value. For the case of distance, measures the distance between the sourceLoc and the entity
	 */
	public double getEntityCriteriaValue(Location sourceLoc, EntityLiving entity, TargetCriteria criterion)
	{
		Location entityLoc = MWEntityType.getEntityLocation(entity);
		
		switch (criterion)
		{
		case NEAREST: case FARTHEST:

			return sourceLoc.distanceSquared(entityLoc);

		case MOST_HEALTH: case LEAST_HEALTH:

			return (double) entity.getHealth();

		default:
			// Theoretically impossible
			return MainMobWars.USE_DEFAULT;
		}
	}
	
	/**
	 * Register that this target is being attacked in this.attackingTeamsByTarget
	 * @param target Entity being targeted to register. May not be null
	 * @param attackingTeam Team attacking this target
	 */
	public void registerTarget(EntityLiving target, Team attackingTeam)
	{
		if (target == null)
		{
			return;
		}
			
		// Test that the target is NOT being attacked by this entity's team
		if (!this.isEntityBeingAttacked(target))
		{
			// Add the entry if it does not exist yet
			this.attackingTeamsByTarget.put(target, new ArrayList<Team>());
		}

		if (!this.isTeamAttackingEntity(target, attackingTeam))
		{
			this.attackingTeamsByTarget.get(target).add(attackingTeam);
		}
	}
	
	/**
	 * Unregister that this target is being attacked from this.attackingTeamsByTarget
	 * Sets all attackers to stop aiming for this target
	 * @param target Entity to unregister
	 */
	public void unregisterTarget(EntityLiving target)
	{
		ArrayList<Team> attackingTeams = this.attackingTeamsByTarget.remove(target);
		
		if (attackingTeams != null && !attackingTeams.isEmpty())
		{
			for (Team team : attackingTeams)
			{
				for (EntityInsentient attacker : this.getGame().getTeamList(team))
				{
					if (attacker != null && attacker.getGoalTarget() != null && attacker.getGoalTarget().equals(target))
					{
						this.entityStopAttack(attacker);
					}
				}
			}
		}
	}
}
