package io.github.alpharlee.mobwars;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Particle;

/**
 * Action dictating what the current MW entity is doing
 * @author RLee
 *
 */
public enum MWEntityAction
{
	ACTIVE_MOVING("ActiveMoving", Particle.VILLAGER_HAPPY /*, 204, 204, 204*/),
	ACTIVE_ATTACKING("ActiveAttacking", Particle.FLAME /*, 255, 51, 51*/),
	//AT_DESTINATION("AtDestionation"),
	WAITING("Waiting", Particle.WATER_BUBBLE /*,0, 0, 0*/),
	PASSIVE_MOVING("PassiveMoving", Particle.CRIT_MAGIC /*, 128, 128, 128*/),
	PASSIVE_ATTACKING("PassiveAttacking", Particle.REDSTONE /*, 204, 1, 1*/);
	
	private String name;
	private Particle particle;
	
	/*
	private int redValue;
	private int greenValue;
	private int blueValue;
	*/
	
	private static Map<String, MWEntityAction> actionByName = new HashMap<String, MWEntityAction>();
	
	static
	{
		for (MWEntityAction action : MWEntityAction.values())
		{
			actionByName.put(action.getName().toLowerCase(), action);
		}
	}
	
	private MWEntityAction(String name, Particle particle)
	{
		this.name = name;
		this.particle = particle;
	}
	
	/*
	
	private MWEntityAction(String name, int red, int blue, int green)
	{
		this.name = name;
		this.redValue = red;
		this.greenValue = green;
		this.blueValue = blue;
	}
	
	*/
	
	public String getName()
	{
		return this.name;
	}
	
	/**
	 * Case insensitive way to retrieve enum
	 * @param name Name of the enum. Do not include underscores
	 * @return MWEntityAction corresponding to the name
	 * Eg. Setting the name parameter to "ActiveMoving" will return the enum MWEntityAction.ACTIVE_MOVING
	 */
	public MWEntityAction getActionByName(String name)
	{
		return actionByName.get(name.toLowerCase());
	}
	
	public Particle getParticle()
	{
		return this.particle;
	}
	
	/*
	public int getRedValue()
	{
		return this.redValue;
	}
	
	public int getGreenValue()
	{
		return this.greenValue;
	}
	
	public int getBlueValue()
	{
		return this.blueValue;
	}
	*/
}
