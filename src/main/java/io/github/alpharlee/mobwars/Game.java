package io.github.alpharlee.mobwars;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.entity.Player;

import com.rlee.mobwars.mobs.MWEntity;
import com.rlee.mobwars.userinterface.ItemHandler;
import com.rlee.mobwars.userinterface.ParticleDisplayer;

import net.md_5.bungee.api.ChatColor;
import net.minecraft.server.v1_10_R1.EntityInsentient;

/**
 * Handles all interaction between players, MWEntities, map and minigame dynamics (eg. joining game, leaving game)
 * @author RichardLee
 *
 */
public class Game
{
	///////////////////////////////////INCOMPLETE////////////////////////
	/*
	* Checklist of required components:
	* join game
	* leave game
	* 	-leave game by interrupt
	* TP players to map
	* Save player inventory/give player inventory
	* 
	* Remove dead entities from their teams
	*/
	
	//Map of teams that all active players are part of (designed with intent of quickly finding the player's team
	public Map<Player, Team> teamsByPlayer = new HashMap<Player, Team>();
	//Map of entities that are part of the team (designed with intent of quickly commanding entity to move/attack)
	public Map<Team, ArrayList<EntityInsentient>> entitiesByTeam = new HashMap<Team, ArrayList<EntityInsentient>>();
	
	//Map of all players to their selectors
	public Map<Player, Selector> selectorsByPlayer = new HashMap<Player, Selector>();
	
	public MainMobWars main;
	public EntitySpawnHandler spawnHandler;
	public EntityMoveHandler moveHandler;
	public EntityAttackHandler attackHandler;
	public ItemHandler itemHandler;
	public ParticleDisplayer particleDisplayer;
	
	/**
	 * Default constructor
	 */
	public Game(MainMobWars main)
	{	
		this.initializeVariables();
		this.main = main;
		this.spawnHandler = new EntitySpawnHandler(this);
		this.moveHandler = new EntityMoveHandler(this);
		this.attackHandler = new EntityAttackHandler(this);
		this.itemHandler = new ItemHandler(this);
		this.particleDisplayer = new ParticleDisplayer(this);
	}
	
	public EntitySpawnHandler getSpawnHandler()
	{
		return this.spawnHandler;
	}
	
	public EntityMoveHandler getMoveHandler()
	{
		return this.moveHandler;
	}
	
	public EntityAttackHandler getAttackHandler()
	{
		return this.attackHandler;
	}
	
	public ItemHandler getItemHandler()
	{
		return this.itemHandler;
	}
	
	public ParticleDisplayer getParticleDisplayer()
	{
		return this.particleDisplayer;
	}
	
	/**
	 * Initializer for all variables inside the class, including maps, array lists, etc.
	 */
	public void initializeVariables()
	{
		//Initialize this.EntitiesByTeam by adding an arraylist for every team
		for (Team team : Team.values())
		{
			this.entitiesByTeam.put(team, new ArrayList<EntityInsentient>());
		}
	}
	
	/**
	 * Add the player to the game and set the player to the team with the least number of players in it.
	 * Will do nothing if player is already in the game
	 * @param player Player to add
	 */
	public void addPlayer(Player player)
	{
		Map<Team, Integer> playersPerTeam = new HashMap<Team, Integer>();
		//CAUTION: Using iteratedTeam for two values: first is to establish playersPerTeam map and second is to use as base comparator
		Team iteratedTeam;
		int leastPlayers = (int) MainMobWars.USE_DEFAULT;
		
		if (this.teamsByPlayer.containsKey(player))
		{
			//Do nothing if this player is already added
			return;
		}
		
		//Initialize the playersPerTeam map
		for (Team team : Team.values())
		{
			//Do not set NO_TEAM
			if (team != Team.NO_TEAM)
			{
				playersPerTeam.put(team, 0);
			}
		}
		
		for (Entry<Player, Team> entry : this.teamsByPlayer.entrySet())
		{
			iteratedTeam = entry.getValue();

			//Increment the team's number of members
			playersPerTeam.put(iteratedTeam, playersPerTeam.get(iteratedTeam) + 1);
		}
		
		iteratedTeam = null;
		
		for (Entry<Team, Integer> entry : playersPerTeam.entrySet())
		{
			//If the leastPlayers count is greater than the entry count or not set at all yet
			if (leastPlayers > entry.getValue() || leastPlayers == (int) MainMobWars.USE_DEFAULT)
			{ 
				//Set the lowestPlayerTeam (iteratedTeam) to the current iterated key
				//Notice that if the leastPlayers count is equal to the iterated players count, the former team will be preferred
				iteratedTeam = entry.getKey();
				leastPlayers = entry.getValue();
			}
		}
		
		//Add the player to the team with lowest players already
		this.addPlayer(player, iteratedTeam);
	}
	
	/**
	 * Add the player to the game and set their default team. Will override current team if player is part of a team
	 * @param player Target player
	 * @param team target team
	 */
	public void addPlayer(Player player, Team team)
	{
		this.setPlayersTeam(player, team);
		this.main.setGameOfPlayer(player, this);
		this.selectorsByPlayer.put(player, new Selector(this, player));
	}
	
	/**
	 * Remove the player from the game
	 * @param player Player to remove
	 */
	public void removePlayer(Player player)
	{
		this.removePlayerFromTeam(player);
		this.main.removePlayerFromGame(player);
		this.selectorsByPlayer.remove(player);
		
		player.sendMessage(ChatColor.RED + "You have left your game. Please come again soon!");
	}
	
	/**
	 * Remove all players from the game
	 * This will cause all entities to spontaneously die as well
	 */
	public void removeAllPlayers()
	{
		for (Player player : this.teamsByPlayer.keySet())
		{
			this.removePlayer(player);
		}
	}
	
	/**
	 * Get the team of this player. If player is not found on this.teamsByPlayer, will return Team.NO_TEAM
	 * @param player Player to get team of
	 * @return Player's team, or Team.NO_TEAM if player is not on a team
	 */
	public Team getPlayersTeam(Player player)
	{
		return this.teamsByPlayer.get(player) != null ? this.teamsByPlayer.get(player) : Team.NO_TEAM;
	}
	
	/**
	 * Set the team of the player. Will remove all entities on that player's team if there is no other player on that team
	 * @param player Target player
	 * @param team Target team
	 */
	public void setPlayersTeam(Player player, Team team)
	{
		//If the player is NOT trying to join the team they are currently on
		if (this.getPlayersTeam(player) != team)
		{
			this.removePlayerFromTeam(player);
			this.teamsByPlayer.put(player, team);
			
			if (this.getPlayersTeam(player) != null)
			{
				player.sendMessage(ChatColor.GREEN + "You have changed to " + team.getChatColor() + team.getName() + ChatColor.GREEN + " team!");
			}
		}
		else
		{
			player.sendMessage(ChatColor.RED + "You're already on that team, silly! Now why would you want to change that badly?");
		}
	}
	
	/**
	 * Remove the player from their team. If no other player is on the same team, then clear all the entities from that team
	 * @param player Player to remove
	 */
	public void removePlayerFromTeam(Player player)
	{
		Team formerTeam = this.teamsByPlayer.remove(player);
		Boolean teamHasPlayer = false;
		
		if (formerTeam != null)
		{
			//Test if anyone is still on this team
			for (Team team : this.teamsByPlayer.values())
			{
				if (formerTeam.equals(team))
				{
					teamHasPlayer = true;
					break;
				}
			}
			
			//If no one was detected still on the team
			if (!teamHasPlayer)
			{	
				//Clear the team entirely
				this.clearEntitiesFromTeam(formerTeam);
			}
		}
	}
	
	/**
	 * Add an NMS EntityInsentient to its team list. Convenience method around casting
	 * Only applicable if entity is a MWEntity. No result otherwise
	 * @param entity NMS EntityInsentient to add
	 * @param team Team for entity to join
	 */
	public void addEntityToTeam(EntityInsentient entity, Team team)
	{
		if (entity instanceof MWEntity)
		{
			this.addEntityToTeam((MWEntity) entity, team);
		}
	}
	
	/**
	 * Add a MWEntity to its team list
	 * Will override the entity's current team
	 * @param entity MWEntity to add
	 * @param team Team for entity to join
	 */
	public void addEntityToTeam(MWEntity entity, Team team)
	{
		ArrayList<EntityInsentient> teamList = this.entitiesByTeam.get(team);
		
		this.removeEntityFromTeam(entity);
		
		//Test if entity is NOT already on team
		if (!teamList.contains(entity))
		{
			//Add the entity to the team
			//KLUDGE: A bit of backwards casting, won't you admit?
			teamList.add((EntityInsentient) entity);
			entity.setTeam(team);
		}
		//Entity already on this team, do not add
	}
	
	/**
	 * Remove the NMS EntityInsentient from its team and effectively the game. Convenience class to avoid casting
	 * Will have no effect if entity is not a MWEntity
	 * @param entity NMS ENtityInsentient to remove
	 */
	public void removeEntityFromTeam(EntityInsentient entity)
	{
		if (entity instanceof MWEntity)
		{
			this.removeEntityFromTeam((MWEntity) entity);
		}
	}
	
	/**
	 * Remove the MWEntity from its team and effectively the game
	 * @param entity MWEntity to remove
	 */
	public void removeEntityFromTeam(MWEntity entity)
	{
		this.entitiesByTeam.get(entity.getTeam()).remove(entity);
	}
	
	/**
	 * Remove all MWEntities from a team
	 * All entities will be killed then removed from their respective teams
	 * @param team Team to remove entities from
	 */
	public void clearEntitiesFromTeam(Team team)
	{
		ArrayList<EntityInsentient> teamList = this.entitiesByTeam.get(team);
		
		for (EntityInsentient iteratedEntity : teamList)
		{
			this.getSpawnHandler().removeMWEntity(iteratedEntity, false);
		}
		
		teamList.clear();
	}
	
	/**
	 * Get a master list of all MWEntities in this game
	 * @return ArrayList of all MWEntities (as NMS EntityInsentient-types), including any entities belonging to NO_TEAM
	 */
	public ArrayList<EntityInsentient> getAllMWEntities()
	{
		ArrayList<EntityInsentient> entityList = new ArrayList<EntityInsentient>();
		
		for (ArrayList<EntityInsentient> teamList : this.entitiesByTeam.values())
		{
			entityList.addAll(teamList);
		}
		
		return entityList;
	}
	
	/**
	 * Return a list of all MWEntities on the desired team, per game
	 * @param team Team to search for
	 * @return All MWEntities (as NMS ENtityInsentient-types) on this team
	 */
	public ArrayList<EntityInsentient> getTeamList(Team team)
	{
		return this.entitiesByTeam.get(team);
	}
}
