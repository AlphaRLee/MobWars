package io.github.alpharlee.mobwars;

import java.util.HashMap;
import java.util.Map;

public enum TargetCriteria
{
	NEAREST("Nearest", false, true),
	FARTHEST("Farthest", true, true),
	LEAST_HEALTH("LeastHealth", false, false),
	MOST_HEALTH("MostHealth", true, false);
	
	private String name = "";
	private boolean greatestValue = false;
	private boolean sourceDependent = false;
	private static Map<String, TargetCriteria> criteriaByName = new HashMap<String, TargetCriteria>();
	
	static
	{
		for (TargetCriteria criterion : TargetCriteria.values())
		{
			criteriaByName.put(criterion.getName().toLowerCase(), criterion);
		}
	}
	
	private TargetCriteria(String name, boolean greatestValue, boolean sourceDependent)
	{
		this.name = name;
		this.greatestValue = greatestValue;
		this.sourceDependent = sourceDependent;
	}
	
	public String getName()
	{
		return this.name;
	}
	
	public boolean isGreatestValue()
	{
		return this.greatestValue;
	}
	
	public boolean isSourceDependent()
	{
		return this.sourceDependent;
	}
	
	/**
	 * Returns the TargetCriteria by name. Not case sensitive
	 * @param name Desired TargetCriteria name
	 * @return TargetCriteria
	 */
	public static TargetCriteria getByName(String name)
	{
		return criteriaByName.get(name.toLowerCase());
	}
}
