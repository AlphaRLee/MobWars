package io.github.alpharlee.mobwars;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.ChatColor;
import org.bukkit.Color;

public enum Team
{

	RED("RED", Color.RED),
	BLUE("BLUE", Color.BLUE),
	GREEN("GREEN", Color.GREEN),
	YELLOW("YELLOW", Color.YELLOW),
	ORANGE("ORANGE", Color.ORANGE),
	PURPLE("PURPLE", Color.PURPLE),
	PINK("PINK", Color.FUCHSIA),
	GRAY("GRAY", Color.GRAY),
	WHITE("WHITE", Color.WHITE),
	BLACK("BLACK", Color.BLACK),
	NO_TEAM("", Color.WHITE);
	
	private String teamName;
	private Color teamColor;
	
	private static Map<String, Team> teamByName = new HashMap<String, Team>();
	
	static
	{
		for (Team team : Team.values())
		{
			teamByName.put(team.getName().toLowerCase(), team);
		}
	}
	
	private Team (String tN, Color tC) 
	{
		this.teamName = tN;
		this.teamColor = tC;
	}
	
	/**
	 * Returns the team name in upper case letters
	 * @return "" if value is NOTEAM
	 */
	public String getName() 
	{
		return this.teamName;
	}
	
	/**
	 * Returns the bukkit.color of the team
	 * @return WHITE if value is NOTEAM
	 */
	public Color getColor() 
	{
		return this.teamColor;
	}
	
	/**
	 * Returns the bukkit.color as ChatColor of team
	 * @return WHITE if value is NOTEAM
	 */
	public ChatColor getChatColor() 
	{
		//return this == Team.NO_TEAM ? ChatColor.WHITE : ChatColor.valueOf(this.getName());
		
		switch (this)
		{
		case NO_TEAM:
			return ChatColor.WHITE;
			
		case ORANGE:
			return ChatColor.GOLD;
			
		case PURPLE:
			return ChatColor.DARK_PURPLE;
			
		case PINK:
			return ChatColor.LIGHT_PURPLE;
			
		default:
			return ChatColor.valueOf(this.getName());
		}
	}
	
	/**
	 * If value is NOTEAM, returns value for white
	 * @return RGB value of color as 1 int
	 */
	public int getColorAsInt() 
	{
		return this.teamColor.asRGB();
	}
	
	public static Team getTeamByName(String name) 
	{
		return teamByName.get(name.toLowerCase());
	}

}
