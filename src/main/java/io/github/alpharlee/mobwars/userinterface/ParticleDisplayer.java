package io.github.alpharlee.mobwars.userinterface;

import java.util.ArrayList;

import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import com.rlee.mobwars.Game;
import com.rlee.mobwars.MWEntityAction;
import com.rlee.mobwars.MWEntityType;
import com.rlee.mobwars.MainMobWars;
import com.rlee.mobwars.Selector;
import com.rlee.mobwars.mobs.MWEntity;

import net.md_5.bungee.api.ChatColor;
import net.minecraft.server.v1_10_R1.EntityInsentient;

public class ParticleDisplayer
{
	Game game;
	
	public ParticleDisplayer (Game game)
	{
		this.game = game;
	}
	
	public void showEntityTargetGoal(Player player, ArrayList<EntityInsentient> entityList, int maxTotalParticleDistance, double particleDistance)
	{
		MWEntity mwEntity;
		
		Location iteratedLoc = null;
		Location targetLoc = null;
		Vector iteratedDirection;
		
		double distance;
		
		boolean showParticle = true;
		Particle particle = null;
		/*
		//Weighted out of 255 (divided by 255 at the show particle part)
		float red = 1;
		float green = 1;
		float blue = 1;
		*/
		
		if (particleDistance <= 0)
		{
			//Cannot have a particle distance of 0 or less
			return;
		}
		
		entityLoop:
		for (EntityInsentient entity : entityList)
		{
			showParticle = true;
			particle = null;
			
			/*
			red = 1;
			green = 1;
			blue = 1;
			*/
			
			if (entity instanceof MWEntity)
			{
				mwEntity = (MWEntity) entity;
			}
			else
			{
				//Skip over this entity
				continue entityLoop;
			}
			
			if (mwEntity.getAction() == MWEntityAction.WAITING)
			{
				showParticle = false;
			}
			else
			{
				//Note: Using eye location, not main location
				iteratedLoc = MWEntityType.getBukkitLivingFromNMSLiving(entity).getEyeLocation();
				
				/*
				red = mwEntity.getAction().getRedValue();
				green = mwEntity.getAction().getGreenValue();
				blue = mwEntity.getAction().getBlueValue();
				*/
				
				particle = mwEntity.getAction().getParticle();
				
				//MainMobWars.reportDebug("RGB: " + red + ", " + green + ", " + blue);
				//MainMobWars.reportDebug("EntityAction: " + mwEntity.getAction().getName());
				
				switch (mwEntity.getAction())
				{
				case ACTIVE_MOVING: case PASSIVE_MOVING:
					
					//MainMobWars.reportDebug("[P] Moving detected! EntityAction: " + mwEntity.getAction().getName());
					
					//Don't write into the targetLoc, use a copy instead
					targetLoc = this.game.moveHandler.destinationByEntity.get(entity).clone();
					
					/*
					//If the selected location is inside a solid block (or the block is NOT transparent)
					if (!Selector.transparentMaterials.contains(player.getWorld().getBlockAt(targetLoc)))
					{
						//Lift up the visible targetLoc to be above ground
						targetLoc.add(0, 1, 0);
					}
					*/
					
					break;
				
				case ACTIVE_ATTACKING: case PASSIVE_ATTACKING:
					
					if (entity.getGoalTarget() != null)
					{
						//MainMobWars.reportDebug("[P] Attacking detected! EntityAction: " + mwEntity.getAction().getName());
						
						targetLoc = MWEntityType.getBukkitLivingFromNMSLiving(entity.getGoalTarget()).getEyeLocation().clone();
					}
					else
					{
						//ERROR: No entity selected!
						showParticle = false;
					}
					
					break;
			
				default:
					showParticle = false;
					break;
				}
				
				//Check if targetLoc is valid.
				if (showParticle && iteratedLoc != null && targetLoc != null)
				{
					distance = iteratedLoc.distance(targetLoc);
					
					//MainMobWars.reportDebug("Distance to targetLoc: " + distance);
					
					if (distance <= 0 /* || distance < particleViewDistance*/)
					{
						//Distance is too small to show, no need to move anywhere
						//Also prevents divide by 0 error
						continue entityLoop;
					}
					
					
					/*
					 * Get the iterated direction between the iterated location and the target location
					 * Start by cloning the targetLoc so that it is not edited in the operation
					 * Subtract the iteratedLoc from it to get a vector starting at the iteratedLoc and ending at the targetLoc
					 * Divide the vector by the distance to convert it into a unit vector
					 * Multiply it by the iterated particle distance
					 */
					iteratedDirection = targetLoc.clone().subtract(iteratedLoc).toVector().multiply(particleDistance/distance);
					
					//MainMobWars.reportDebug(ChatColor.BLUE + "targetLoc: " + targetLoc.getX() + ", " + targetLoc.getY() + ", " + targetLoc.getZ());
					//MainMobWars.reportDebug(ChatColor.DARK_GREEN + "IteratedLoc: " + iteratedLoc.getX() + ", " + iteratedLoc.getY() + ", " + iteratedLoc.getZ());
					//MainMobWars.reportDebug(ChatColor.BLUE + "IteratedDirection: " + iteratedDirection.getX() + ", " + iteratedDirection.getY() + ", " + iteratedDirection.getZ());
					
					//Iterate for the frequency of particles. 
					//Note that the iterator is one less than the multiplied distance to prevent the particle
					//from spawning at the entity, while still spawning at the target
					for (int i = 0; i < distance/particleDistance; i++)
					{
						if (i <= maxTotalParticleDistance/particleDistance)
						{
						
						//Note that order is important: The addition must occur before the particle spawn
						iteratedLoc.add(iteratedDirection);
						
						//MainMobWars.reportDebug(ChatColor.YELLOW + "IteratedLoc: " + iteratedLoc.getX() + ", " + iteratedLoc.getY() + ", " + iteratedLoc.getZ());
						
						/**
						 * Play an effect to the player with a coloured particle
						 * @param1: location
						 * @param2: effect
						 * @param3: id
						 * @param4: data
						 * @param5: xOffset (doubles as red, weighted out of 1, cannot be 0)
						 * @param6: yOffset (doubles as green, weightd out of 1, cannot be 0)
						 * @param7: zOffset (doubles as blue, weighted out of 1, cannot be 0)
						 * @param8: speed
						 * @param9: particle quantity
						 * @param10: view distance
						 */
						//player.spigot().playEffect(iteratedLoc, Effect.COLOURED_DUST, 0, 1, (float) red/255, (float) green/255, (float) blue/255, 1, 0, particleViewDistance);
					
						/**
						 * Show to a particle to the player
						 * @param1: particle
						 * @param2: location
						 * @param3: count
						 * @param4: xOffset (doubles as red, weighted out of 1, cannot be 0)
						 * @param5: yOffset (doubles as green, weightd out of 1, cannot be 0)
						 * @param6: zOffset (doubles as blue, weighted out of 1, cannot be 0)
						 * @param7: Extra data (usually speed)
						 */
						player.spawnParticle(particle, iteratedLoc, 0, 0, 0, 0, 0);
						
						}
						else
						{
							//Too many particles spawned, stop spawning now
							break;
						}
					}
				}
				else
				{
					//Do not bother to continue displaying particles for this entity
					continue entityLoop;
				}
			}
		}
	}
}
