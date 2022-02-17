package io.github.alpharlee.mobwars.structures;

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.rlee.mobwars.Game;
import com.rlee.mobwars.MainMobWars;
import com.rlee.mobwars.Team;

public abstract class Structure
{
	public Game game;
	public Team team;
	
	//List of all blocks composing of this structure. All blocks are stored with ABSOLUTE coordinates
	public ArrayList<Block> blocks = new ArrayList<Block>();
	//Center of this structure, on the floor
	//Always at ABSOLUTE coordinates (despite the centerX centerY and centerZ being relative coordinates)
	public Location center; 
	
	private int length = (int) MainMobWars.USE_DEFAULT; //Length denotes the size along the Z-axis (even after rotation)
	private int width = (int) MainMobWars.USE_DEFAULT; //Width denotes the size along the X-axis (even after rotation)
	private int height = (int) MainMobWars.USE_DEFAULT; //Height denotes the size along the Y-axis
	
	//Written to ABSOLUTE coordinates
	private int lowX = (int) MainMobWars.USE_DEFAULT; 
	private int lowY = (int) MainMobWars.USE_DEFAULT; //Lowest coordinate of the structure (inclusive)
	private int lowZ = (int) MainMobWars.USE_DEFAULT; 
	
	//Center coordinate of the structure, manually chosen
	//Theoretically allows for non-central blocks to be chosen as center
	//Written in RELATIVE coordinates, NOT absolute coordinates
	private int centerX = (int) MainMobWars.USE_DEFAULT; 
	private int centerY = (int) MainMobWars.USE_DEFAULT; 
	private int centerZ = (int) MainMobWars.USE_DEFAULT; 
	
	
	public Structure(Game game, Team team)
	{
		this.game = game;
		this.team = team;
	}
	
	public ArrayList<Block> getBlocks()
	{
		return this.blocks;
	}
	
	public void setBlocks(ArrayList<Block> blocks)
	{
		this.blocks = blocks;
	}
	
	public Location getCenter()
	{
		return this.center;
	}
	
	public void setCenter(Location loc)
	{
		this.center = loc;
	}
	
	public String getJsonStructureString()
	{
		Map<String, Object> structureMap = new HashMap<String, Object>();
		JSONObject jsonStructure = new JSONObject();
		StringWriter jsonWriter = new StringWriter();
		
		//Set the length, width and height
		this.setDimensions();
		
		structureMap.put("team", this.team);
		
		structureMap.put("length", this.length);
		structureMap.put("width", this.width);
		structureMap.put("height", this.height);
		
		structureMap.put("lowX", this.lowX);
		structureMap.put("lowY", this.lowY);
		structureMap.put("lowZ", this.lowZ);
		
		structureMap.put("centerX", this.centerX);
		structureMap.put("centerY", this.centerY);
		structureMap.put("centerZ", this.centerZ);
		
		structureMap.put("blocks", this.blocks);
		
		jsonStructure.putAll(structureMap);
		
		try
		{
			jsonStructure.writeJSONString(jsonWriter);
			return jsonWriter.toString();
		} 
		catch (IOException e)
		{
			//Something went wrong
			e.printStackTrace();
			return "";
		}
	}
	
	public void setFromJsonStructureString(String jsonString)
	{
		try
		{
			//--------------------------------------------------------------------------------
			//KLUDGE: Ineffective method to set all fields manually? Is there a better way?
			//--------------------------------------------------------------------------------
			
			JSONParser parser = new JSONParser();
			JSONObject jsonStructure = (JSONObject) parser.parse(jsonString);
			
			this.team = (Team) jsonStructure.get("team");
			
			this.length = (int) jsonStructure.get("length");
			this.width = (int) jsonStructure.get("width");
			this.height = (int) jsonStructure.get("height");
			
			this.lowX = (int) jsonStructure.get("lowX");
			this.lowY = (int) jsonStructure.get("lowY");
			this.lowZ = (int) jsonStructure.get("lowZ");
			
			this.centerX = (int) jsonStructure.get("centerX");
			this.centerY = (int) jsonStructure.get("centerY");
			this.centerZ = (int) jsonStructure.get("centerZ");
			
			this.blocks = (ArrayList<Block>) jsonStructure.get("blocks");
		} 
		catch (ParseException e)
		{
			Bukkit.getLogger().log(Level.SEVERE, "Structure parser failed at: " + e.getPosition());
			e.printStackTrace();
		}
	}
	
	/**
	 * Spawn the structure where the center is at the specified location
	 * @param loc Location to set the center of this structure to
	 */
	public void spawn(Location loc)
	{
		World world = loc.getWorld();
		Block targetBlock = null;
		Location blockLoc;
		
		this.setCenter(loc);
		
		KLUDGE: CHECK REGION IS SAFE TO SPAWN IN
		
		for (Block block : this.blocks)
		{
			targetBlock = null;
			blockLoc = block.getLocation();
			
			/*
			 * Get the new absolute location of the block, based around the center
			 * 1: Subtract off old absolute coordinates (-1 to subtract only exclusive coordinates)
			 * 2: Subtract off the distance from the center (Get offset away from center)
			 * 3: Add the targetLoc's center
			 */
			blockLoc.subtract(this.lowX - 1, this.lowY - 1, this.lowZ - 1).subtract(this.centerX, this.centerY, this.centerZ).add(loc);
			
			targetBlock = world.getBlockAt(blockLoc);
			
			if (targetBlock != null)
			{
				Check out this link for using schmatics
				https://bukkit.org/threads/schematic-saving-loading-and-pasting.336055/
			}
		}
			
		
		KLUDGE: SAVE OVERWRITTEN REGION TO RESTORE ELSEWHERE
	}
	
	/**
	 * Set the length, width, height and the lowest cooridinates of this build
	 */
	private void setDimensions()
	{
		int lowestX = (int) Math.abs(MainMobWars.USE_DEFAULT);
		int lowestY = (int) Math.abs(MainMobWars.USE_DEFAULT); //Notice how lowest values is set to +999 not -999
		int lowestZ = (int) Math.abs(MainMobWars.USE_DEFAULT);
		int highestX = (int) MainMobWars.USE_DEFAULT;
		int highestY = (int) MainMobWars.USE_DEFAULT; 
		int highestZ = (int) MainMobWars.USE_DEFAULT;
		
		Location blockLocation;
		
		for (Block block : this.blocks)
		{
			blockLocation = block.getLocation();
			
			if (blockLocation.getBlockX() < lowestX)
			{
				lowestX = blockLocation.getBlockX();
			}
			else if (blockLocation.getBlockX() > highestX)
			{
				highestX = blockLocation.getBlockX();
			}
			
			if (blockLocation.getBlockY() < lowestY)
			{
				lowestY = blockLocation.getBlockY();
			}
			else if (blockLocation.getBlockY() > highestY)
			{
				highestY = blockLocation.getBlockY();
			}
			
			if (blockLocation.getBlockZ() < lowestZ)
			{
				lowestZ = blockLocation.getBlockZ();
			}
			else if (blockLocation.getBlockZ() > highestZ)
			{
				highestZ = blockLocation.getBlockZ();
			}
		}
		
		//Validate that a region was found
		if (highestY > MainMobWars.USE_DEFAULT)
		{
			this.length = highestZ - lowestZ + 1;
			this.width = highestX - lowestX + 1; //Accomodate for the subtraction chopping off the final block by adding 1
			this.height = highestY - lowestY + 1;
			
			this.lowX = lowestX;
			this.lowY = lowestY;
			this.lowZ = lowestZ;
			
			if (this.center != null)
			{
				//Write to relative coordinates
				//Determine based off of chosen center - lowest NW exclusive coordinate
				this.centerX = this.center.getBlockX() - (this.lowX - 1);
				this.centerY = this.center.getBlockY() - (this.lowY - 1);
				this.centerZ = this.center.getBlockZ() - (this.lowZ - 1);
			}
		}
	}
}
