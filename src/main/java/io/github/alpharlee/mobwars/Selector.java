package io.github.alpharlee.mobwars;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.util.Vector;

import com.rlee.mobwars.mobs.MWEntity;
import com.rlee.mobwars.mobs.StrikerEntity;
import com.rlee.mobwars.mobs.SupportEntity;

import net.md_5.bungee.api.ChatColor;
import net.minecraft.server.v1_10_R1.EntityInsentient;

public class Selector
{
	public Game game;
	public Player player;
	
	public Location pos1 = null;
	public Location pos2 = null;
	
	private Location posLow = null;
	private Location posLowXHighZ = null;
	private Location posHighXLowZ = null;
	private Location posHigh = null;
	
	//General list of all entities under selection
	public ArrayList<EntityInsentient> selectedEntities = new ArrayList<EntityInsentient>();
	//List of all offensive-based entities on player's team under selection
	public ArrayList<EntityInsentient> selectedStrikers = new ArrayList<EntityInsentient>();
	//List of all support-based entities on player's team under selection
	public ArrayList<EntityInsentient> selectedSupporters = new ArrayList<EntityInsentient>();
	
	public SelectState state = SelectState.NONE;
	private SelectPosSource posSource = SelectPosSource.NONE;
	
	public static Set<Material> transparentMaterials = new HashSet<Material>();
	
	static
	{
		Material materialArray[] = 
			{
				Material.AIR,
				Material.STATIONARY_WATER, Material.WATER,
				//Material.GLASS, Material.STAINED_GLASS, Material.THIN_GLASS, Material.STAINED_GLASS_PANE, //Decide: Is glass a solid material or not?
				Material.LONG_GRASS, Material.DOUBLE_PLANT, Material.CROPS, Material.CARROT, Material.BEETROOT_BLOCK,
				Material.SAPLING
			};
		
		for (Material material : materialArray)
		{
			transparentMaterials.add(material);
		}
	}
	
	public Selector(Game game, Player player)
	{
		this.game = game;
		this.player = player;
	}
	
	public Game getGame()
	{
		return this.game;
	}
	
	public SelectState getState()
	{
		return this.state;
	}
	
	public void setState(SelectState state)
	{
		this.state = state;
	}
	
	private SelectPosSource getPosSource()
	{
		return this.posSource;
	}
	
	private void setPosSource(SelectPosSource source)
	{
		this.posSource = source;
	}
	
	public ArrayList<EntityInsentient> getSelectedEntities()
	{
		return this.selectedEntities;
	}
	
	public void clearSelectedEntities()
	{
		this.selectedEntities.clear();
		this.selectedStrikers.clear();
		this.selectedSupporters.clear();
		this.setState(SelectState.NONE);
	}
	
	public boolean useSelect(Action action)
	{	
		//Single selected entity.
		EntityInsentient selectedSingleEntity;
		String singleEntitysString = "";
		
		int maxSearchDistance = 100;
		double blockIterateDistance = 0.25;
		
		Location selectedLocation;
		
		//If the player somehow is not associated with a team or a selector
		//If the player performed a physical action (eg. Stepped on pressure plate)
		if (action == Action.PHYSICAL)
		{
			//Terminate
			return false;
		}
		
		//Clear the pos if the last select wource was different (eg. command)
		if (this.getPosSource() != SelectPosSource.SELECT && this.getPosSource() != SelectPosSource.NONE)
		{
			MainMobWars.reportDebug(ChatColor.AQUA + "Select: clearPos() called! Pos source: " + this.getPosSource().name());
			
			this.clearPos();
		}
		
		selectedSingleEntity = this.getNearestEntityInSight(maxSearchDistance, blockIterateDistance);
		
		//If the player was looking at an entity
		if (selectedSingleEntity != null)
		{
			singleEntitysString = MWEntityType.getEntityName(selectedSingleEntity, true, false);
			
			if (this.player.isSneaking())
			{
				//Player is looking at entity and sneaking
				//Toggle if that entity is in or out
				if (this.selectedEntities.contains(selectedSingleEntity))
				{
					this.removeEntityFromSelected(selectedSingleEntity);
					this.player.sendMessage(singleEntitysString + ChatColor.DARK_GRAY + "has been unselected. (Why did you forsake it?)");
				}
				else
				{
					this.addEntityToSelected(selectedSingleEntity);
					this.player.sendMessage(singleEntitysString + ChatColor.GOLD + "has been added to selection!");
				}
			}
			else
			{				
				//Player is looking at entity and not sneaking
				//Select just that one entity
				this.clearSelectedEntities();
				
				//Set player's selection to this specific entity
				this.addEntityToSelected(selectedSingleEntity);
				
				player.sendMessage(singleEntitysString + ChatColor.GOLD + "has been selected!");
			}

			//pos1 and pos2 are irrelevant now
			this.clearPos();
		}
		else
		{
			//Player was looking at a block
			//selectedLocation = this.player.getTargetBlock(Selector.transparentMaterials, maxSearchDistance).getLocation();
			selectedLocation = this.getPlayersTargetLocation(maxSearchDistance, blockIterateDistance);
			
			//--------------------------------------------------------------------------------------------
			// Is support going to be added for sneak-clicking while multi-selecting?
			//--------------------------------------------------------------------------------------------
			
			if (selectedLocation != null)
			{
				//No team selected, set selector to region-based
				this.clearSelectedEntities();
				this.setPos(selectedLocation);
				this.setPosSource(SelectPosSource.SELECT);
			}
		}
		
		//Recommend that the event be cancelled (method successfully operated)
		return true;
	}
	
	public boolean useCommand(Action action)
	{	
		Team team = this.getGame().teamsByPlayer.get(player);
		
		ArrayList<EntityInsentient> currentSelected;
		ArrayList<EntityInsentient> currentStrikers = (ArrayList<EntityInsentient>) this.selectedStrikers.clone();
		ArrayList<EntityInsentient> currentSupporters = (ArrayList<EntityInsentient>) this.selectedSupporters.clone();
		
		SelectState currentState = this.state;
		
		//Single selected entity.
		EntityInsentient selectedSingleEntity;
		Location selectedLocation;
		
		int maxSearchDistance = 100;
		double blockIterateDistance = 0.25;
		
		EntityAttackHandler attackHandler = this.getGame().getAttackHandler();
		EntityMoveHandler moveHandler = this.getGame().getMoveHandler();
		
		//If the player somehow is not associated with a team or a selector
		//If the player performed a physical action (eg. Stepped on pressure plate)
		if (team == null || action == Action.PHYSICAL)
		{
			//Terminate
			return false;
		}
		
		//Clear the pos if the last select wource was different (eg. select)
		if (this.getPosSource() != SelectPosSource.COMMAND && this.getPosSource() != SelectPosSource.NONE)
		{
			MainMobWars.reportDebug(ChatColor.DARK_AQUA + "Command: clearPos() called! Pos source: " + this.getPosSource().name());
			this.clearPos();
		}
		
		//Alter SelectState.ALL to become TEAM_ONLY
		//Maintain TEAM_ONLY
		//Terminate on all other cases
		switch (currentState)
		{
		case TEAM_ONLY:
			
			//All clear here, ready to proceed
			break;
		
		case ALL:
			//Handle the currentState. currentSelected is handled later
			currentState = SelectState.TEAM_ONLY;
			break;
			
		default:
			//Player did not have any of their team selected
			//Terminate
			player.sendMessage(ChatColor.RED + "Please use the select wand to select mobs on your team first!");
			return true;
		}
		
		currentSelected = this.getSelectedTeam(this.selectedEntities, team, false);
		
		selectedSingleEntity = this.getNearestEntityInSight(maxSearchDistance, blockIterateDistance);
		
		//--------------------------------------------------------------------------------------------
		// Is support going to be added for sneak-clicking while using commands?
		//--------------------------------------------------------------------------------------------
		
		//If the player was looking at an entity
		if (selectedSingleEntity != null)
		{
			//Player is looking at entity
			
			switch (action)
			{
			case LEFT_CLICK_AIR: case LEFT_CLICK_BLOCK: 
				
				//Test if entity is a teammate
				if (selectedSingleEntity instanceof MWEntity && ((MWEntity) selectedSingleEntity).getTeam().equals(team))
				{
					//Engage in supporting this specific entity
					//Do not move the strikers
					attackHandler.groupAttackTarget(currentSupporters, selectedSingleEntity);
				}
				else
				{
					//Entity is either not MWEntity or not a teammate
					//Attack the target and move the supporters along with the main group
					attackHandler.groupAttackTarget(currentStrikers, selectedSingleEntity);
					moveHandler.moveGroup(currentSupporters, MWEntityType.getEntityLocation(selectedSingleEntity), true);
				}
				
				break;
				
			case RIGHT_CLICK_AIR: case RIGHT_CLICK_BLOCK:
				
				//Move the selected entities towards the single selected entity
				moveHandler.moveGroup(currentSelected, MWEntityType.getEntityLocation(selectedSingleEntity), true);
				
				break;
				
			default:
				//Theoretically impossible. Wrong case
				return false;	
			}
			
			//Clear pos now that target is selected
			this.clearPos();
			
			player.sendMessage(MWEntityType.getEntityName(selectedSingleEntity, true, false) + ChatColor.DARK_AQUA + "has been targeted!");
		}
		else
		{
			//Player was looking at a block
			selectedLocation = this.player.getTargetBlock(Selector.transparentMaterials, maxSearchDistance).getLocation();
			//selectedLocation = this.getPlayersTargetLocation(maxSearchDistance, blockIterateDistance);
			
			if (selectedLocation != null)
			{
				//Add 0.5 to the block to get the top of it
				selectedLocation.add(0, 0.5, 0);
				
				//Differentiate between attacking region or moving to location
				switch (action)
				{
				case LEFT_CLICK_AIR: case LEFT_CLICK_BLOCK:
					
					this.setPos(selectedLocation);
					this.setPosSource(SelectPosSource.COMMAND);
					
					//State of the newly selected region (NOT the currently selected entities)
					switch (this.state)
					{
					case ALL:
						
						//-----------------------------------------------------------------------------------------------------------
						//KLUDGE: ATTACKING BASED OFF OF STATIC CRITERIA, EDIT FOR CUSTOMIZABILITY
						//---------------------------------------------------------------------------------------------------------
						//Set the strikers to attack the non-teammates while the supporters "attack" the teammates
						attackHandler.groupAttackGroup(currentStrikers, MWEntityType.getEntityLivingListFromInsentients(this.getSelectedTeam(this.selectedEntities, team, true)), MainMobWars.testCriteria);
						attackHandler.groupAttackGroup(currentSupporters, MWEntityType.getEntityLivingListFromInsentients(this.getSelectedTeam(this.selectedEntities, team, false)), MainMobWars.testCriteria);
						
						break;
						
					case NONE: default:
						//Do nothing, player has not finished selecting yet
						break;
						
					case NON_TEAM_ONLY:
						
						//-----------------------------------------------------------------------------------------------------------
						//KLUDGE: ATTACKING BASED OFF OF STATIC CRITERIA, EDIT FOR CUSTOMIZABILITY
						//---------------------------------------------------------------------------------------------------------
						//Set the strikers to attack the non-teammates while the supporters "attack" the strikers
						attackHandler.groupAttackGroup(currentStrikers, MWEntityType.getEntityLivingListFromInsentients(this.getSelectedTeam(this.selectedEntities, team, true)), MainMobWars.testCriteria);
						attackHandler.groupAttackGroup(currentSupporters, MWEntityType.getEntityLivingListFromInsentients(currentStrikers), MainMobWars.testCriteria);
						
						break;
						
					case TEAM_ONLY:
						
						//-----------------------------------------------------------------------------------------------------------
						//KLUDGE: ATTACKING BASED OFF OF STATIC CRITERIA, EDIT FOR CUSTOMIZABILITY
						//---------------------------------------------------------------------------------------------------------
						//Set the supporters to "attack" their teammates
						//Do not move the attackers
						attackHandler.groupAttackGroup(currentSupporters, MWEntityType.getEntityLivingListFromInsentients(this.getSelectedTeam(this.selectedEntities, team, false)), MainMobWars.testCriteria);
						
						break;
					}
					
					//Clear pos only if the player has successfully selected something
					if (this.getState() != SelectState.NONE)
					{
						this.clearPos();
					}
					
				break;
				
				case RIGHT_CLICK_AIR: case RIGHT_CLICK_BLOCK:
					
					//Move the selected entities towards the block
					moveHandler.moveGroup(currentSelected, selectedLocation.add(0.5D, 0.5D, 0.5D), true);
					this.clearPos();
					break;
				
				default:
					//Theoretically impossible
					return false;
				}
			}
			
			//Restore this.selectedEntities to the attacking set
			this.selectedEntities = currentSelected;
			this.selectedStrikers = currentStrikers;
			this.selectedSupporters = currentSupporters;
			
			this.setState(currentState);
		}

		//Recommend that the event be cancelled (method successfully operated)
		return true;
	}
	
	public void setPos(Location loc)
	{
		this.setState(SelectState.NONE);
		
		if (loc == null)
		{
			this.clearPos();
			return;
		}
		
		//If pos1 exists and it is in the same world as loc
		if (this.pos1 != null && loc.getWorld().equals(this.pos1.getWorld()))
		{
			//Set pos2 to loc
			this.pos2 = loc;
			
			this.player.sendMessage(ChatColor.AQUA + "Pos2 set to (x: " + this.pos2.getBlockX() + ", z: " + this.pos2.getBlockZ() + ")");
			
			this.sortPos();
			//----------------------------------------------------------------------------------------------------
			//KLUDGE: Is this too broad? Should players have control over the selection settings?-------------------------
			//----------------------------------------------------------------------------------------------------
			this.setSelectedEntitiesToRegion(false, false);
			
			this.clearPos();
		}
		else
		{
			//pos1 does not exist or pos1 is in a different world than the current selection
			//Set pos1 to the desired location and remove pos2
			this.pos1 = loc;
			this.pos2 = null;	
			this.player.spawnParticle(Particle.FIREWORKS_SPARK, loc.add(0, 0.25, 0), 0);
			this.player.sendMessage(ChatColor.AQUA + "Pos1 set to (x: " + this.pos1.getBlockX() + ", z: " + this.pos1.getBlockZ() + ")");
		}
	}
	
	/**
	 * Sort positions to declare the values of this.posLowXZ to this.posHighXZ
	 * Will only declare if this.pos1 and this.pos2 are valid locations
	 */
	private void sortPos()
	{
		World world;
		double lowX;
		double highX;
		double y;
		double lowZ;
		double highZ;
		
		if (!this.arePosValid())
		{
			this.clearPos();
			return;
		}
		
		world = pos1.getWorld();
		
		if (this.pos1.getX() <= this.pos2.getX())
		{
			lowX = this.pos1.getX();
			highX = this.pos2.getX();
		}
		else
		{
			lowX = this.pos2.getX();
			highX = this.pos1.getX();
		}
	
		//Set y to the greatest value between pos1 and pos2
		y = this.pos1.getY() >= this.pos2.getY() ? this.pos1.getY() : this.pos2.getY();	
		
		if (this.pos1.getZ() <= this.pos2.getZ())
		{
			lowZ = this.pos1.getZ();
			highZ = this.pos2.getZ();
		}
		else
		{
			lowZ = this.pos2.getZ();
			highZ = this.pos1.getZ();
		}
		
		this.posLow = new Location(world, lowX, y, lowZ);
		this.posLowXHighZ = new Location(world, lowX, y, highZ);
		this.posHighXLowZ = new Location(world, highX, y, lowZ);
		this.posHigh = new Location(world, highX, y, highZ);
	}
	
	/**
	 * Checks if this.pos1 and this.pos2 exist and are in the same world
	 * @return True if above condition is met
	 */
	private boolean arePosValid()
	{
		return (this.pos1 != null && this.pos2 != null && this.pos1.getWorld().equals(this.pos2.getWorld()));
	}
		
	/**
	 * Sets the entities currently in between this.posLow and this.posHigh to members of this.selectedEntities. 
	 * Entities that are teammates to the player and in the selected region will be sorted into this.selectedStrikers and this.selectedSupporters.
	 * Also will set this.state to reflect the current entities within.
	 * @param restrictToPlayersTeam If true, only teammates will be scanned for. Cannot be simultaneously true with excludePlayerTeam
	 * @param excludePlayersTeam If true, no teammates will be scanned for. Cannot be simultaneously true with restrictToPlayersTeam
	 * @return ArrayList instance of this.selectedEntities. If criteria not met, will return empty arrayList.
	 */
	public ArrayList<EntityInsentient> setSelectedEntitiesToRegion(boolean restrictToPlayersTeam, boolean excludePlayersTeam)
	{
		ArrayList<EntityInsentient> entityList;
		Team team = this.getGame().teamsByPlayer.get(this.player);
		
		//Size of selected (used for telling players)
		int selectedSize = 0;
		
		this.clearSelectedEntities();
		
		//Return an empty list if:
		//-pos1 and pos2 are invalid
		//restrictToPlayersTeam and excludePlayersTeam are both true
		//player's team is invalid (restrictToPlayersTeam or excludePlayersTeam must be set to true)
		if (!arePosValid() 
				|| (restrictToPlayersTeam && excludePlayersTeam) 
				|| ((restrictToPlayersTeam || excludePlayersTeam) && team == null))
		{
			return selectedEntities;
		}
		
		entityList = (restrictToPlayersTeam ? this.getGame().getTeamList(team) : this.getGame().getAllMWEntities());
		
		//Remove the player's team from the check
		//(All potential breaking conditions handled above)
		if (excludePlayersTeam)
		{
			entityList.removeAll(this.getGame().getTeamList(team));
		}
		
		//Pos should be theoretically set already. But better safe than sorry
		this.sortPos();
		
		for (EntityInsentient entity : entityList)
		{			
			//If the X and Z coordinates lie between this.posLow and this.posHigh and the entity is in the same world as this.posLow
			if (this.posLow.getWorld().equals(MWEntityType.getBukkitLivingFromNMSLiving(entity).getWorld()) 
					&& entity.locX >= this.posLow.getX() && entity.locX <= this.posHigh.getX()
					&& entity.locZ >= this.posLow.getZ() && entity.locZ <= this.posHigh.getZ())
			{
				this.addEntityToSelected(entity);
			}
		}
			
		selectedSize = this.selectedEntities.size();
		
		switch (selectedSize)
		{
		case 0:
			player.sendMessage(ChatColor.DARK_RED + "0" + ChatColor.AQUA + " entities selected.");
			break;
			
		case 1:
			player.sendMessage(ChatColor.GOLD + "1" + ChatColor.AQUA + " entity selected.");
			break;
			
		default:
			player.sendMessage(ChatColor.GOLD + "" + selectedSize + ChatColor.AQUA + " entities selected.");
			break;
		}
		
		if (team != null)
		{
			selectedSize = this.getSelectedTeam(this.selectedEntities, team, false).size();
			
			switch (selectedSize)
			{					
			case 1:
				player.sendMessage(team.getChatColor() +"1 " + team.getName() + ChatColor.AQUA + " entity selected.");
				break;
				
			default:
				player.sendMessage(team.getChatColor() +"" + selectedSize + " " + team.getName() + ChatColor.AQUA + " entities selected.");
				break;
			}
		}
		
		return this.selectedEntities;
	}

	/**
	 * Set the state based off the current entities inside the this.selectedEntities
	 */
	public void setStateByEntities()
	{
		Team team = this.getGame().teamsByPlayer.get(this.player);
		
		boolean containsPlayersTeam = false;
		boolean containsOtherTeams = false;
		
		if (team == null)
		{
			//Invalid team, terminate
			return;
		}
		
		for (EntityInsentient entity : this.selectedEntities)
		{	
			if (entity instanceof MWEntity && ((MWEntity) entity).getTeam() == team)
			{
				//At least one selected entity is on the player's team
				containsPlayersTeam = true;
			}
			else
			{
				//At least one selected entity is not on the player's team
				containsOtherTeams = true;
			}
			
			//Found that both entities from player's team and other teams exist, do not continue searching
			if (containsPlayersTeam && containsOtherTeams)
			{
				break;
			}
		}
		
		if (!containsPlayersTeam && !containsOtherTeams)
		{
			this.setState(SelectState.NONE);
		}
		else if (containsPlayersTeam && !containsOtherTeams)
		{
			this.setState(SelectState.TEAM_ONLY);
		}
		else if (!containsPlayersTeam && containsOtherTeams)
		{
			this.setState(SelectState.NON_TEAM_ONLY);
		}
		else if (containsPlayersTeam && containsOtherTeams)
		{
			this.setState(SelectState.ALL);
		}
		else
		{
			//Technically impossible.
			this.setState(SelectState.NONE);
		}
	}
	
	/**
	 * Adds entity to this.selectedEntities.
	 * Further sorts the entity into this.selectedSupporters and this.selectedStrikers, 
	 * if the entity is a MWEntity and on the same team as the player.
	 * Adjusts this.state according to the entity just inserted
	 * @param entity Entity to insert
	 */
	public void addEntityToSelected(EntityInsentient entity)
	{
		MWEntity mwEntity;
		Team team = this.getGame().teamsByPlayer.get(this.player);
		boolean isOnTeam = false;
		
		//Add the entity
		this.selectedEntities.add(entity);
		
		//Classify the entity further:
		//Requires entity to be MWEntity and entity on same team as player
		if (entity instanceof MWEntity && team != null)
		{
			mwEntity = (MWEntity) entity;
			
			if (mwEntity.getTeam() == team)
			{
				isOnTeam = true;
				
				if (mwEntity instanceof StrikerEntity)
				{
					//Set as member of selected offensive entities
					this.selectedStrikers.add(entity);
				}
				else if (mwEntity instanceof SupportEntity)
				{
					//Set as member of selected Support entities
					this.selectedSupporters.add(entity);
				}
				
			}
		}

		//Manually set the state. Faster than using this.setStateByEntities()
		switch (this.getState())
		{
		case NONE:
			
			if (isOnTeam)
			{
				this.setState(SelectState.TEAM_ONLY);
			}
			else
			{
				this.setState(SelectState.NON_TEAM_ONLY);
			}
			break;
			
		case TEAM_ONLY:
			
			if (!isOnTeam)
			{
				this.setState(SelectState.ALL);
			}
			break;
			
		case NON_TEAM_ONLY:
			
			if (isOnTeam)
			{
				this.setState(SelectState.ALL);
			}
			break;
			
		default:
			//Current state is not changed by presence of entity
			break;
		}
	}

	/**
	 * Remove entity from this.selectedEntities.
	 * Automatically sort out the state based off this entity's absence
	 * @param entity
	 */
	public void removeEntityFromSelected(EntityInsentient entity)
	{
		this.selectedEntities.remove(entity);
		this.selectedStrikers.remove(entity);
		this.selectedSupporters.remove(entity);
		
		this.setStateByEntities();
	}
	
	/**
	 * Filter out the given array down to a specific team, or to every entity but that specific team (only when the invert flag is set to true)
	 * @param entities Entities to filter. This method will NOT alter anything in the original list
	 * @param team Team to search for
	 * @param invert Set to true to only search for entities NOT on the specific team
	 * @return ArrayList of all the entities
	 */
	public ArrayList<EntityInsentient> getSelectedTeam(ArrayList<EntityInsentient> entities, Team team, boolean invert)
	{
		ArrayList<EntityInsentient> selectedTeam = new ArrayList<EntityInsentient>();
		
		for (EntityInsentient entity : entities)
		{	
			if (entity instanceof MWEntity && ((MWEntity) entity).getTeam().equals(team))
			{
				//Entity is on team
				if (!invert)
				{
					selectedTeam.add(entity);
				}
			}
			else
			{
				//Entity is not on team
				if (invert)
				{
					selectedTeam.add(entity);
				}
			}
		}
		
		return selectedTeam;
	}
	
	/**
	 * Sets all positions to null
	 * Does not affect this.state
	 */
	public void clearPos()
	{
		this.pos1 = null;
		this.pos2 = null;
		
		this.posLow = null;
		this.posLowXHighZ = null;
		this.posHighXLowZ = null;
		this.posHigh = null;
		
		this.setPosSource(SelectPosSource.NONE);
	}
	
	/**
	 * Get approximate location player is looking at on a non-transparent block
	 * @param maxDistance Max distance to search for
	 * @param blockIterateDistance Distance to travel to search by block. Decreased value increases accuracy but increases computing cost
	 * @return
	 */
	public Location getPlayersTargetLocation(int maxDistance, double blockIterateDistance)
	{
		Location iteratedLoc;
		Vector iteratedDirection;

		maxDistance = Math.abs(maxDistance);
		blockIterateDistance = Math.abs(blockIterateDistance);
		
		//Invalid iterate distance
		if (blockIterateDistance == 0)
		{
			return null;
		}
		
		iteratedLoc = player.getEyeLocation();
		iteratedDirection = player.getEyeLocation().getDirection().multiply(blockIterateDistance);
		
		for (int i = 0; i <= maxDistance/blockIterateDistance; i++)
		{
			iteratedLoc.add(iteratedDirection);
			
			//this.player.spawnParticle(Particle.CRIT, iteratedLoc, 0);
			
			//Iterate until a solid block is encountered
			if (!Selector.transparentMaterials.contains(this.player.getWorld().getBlockAt(iteratedLoc).getType()))
			{
				return iteratedLoc;
			}
		}
		
		//No destination found
		return null;
	}
	
	/**
	 * Get the nearest MW entity to the player that is in the player's line of sight.
	 * Air, water, tall-grass, crops, etc are all considered transparent
	 * @param maxDistance How many blocks away to search
	 * @param blockIterateDistance What distance to iterate by
	 * @return Nearest entity, or null if none found
	 */
	private EntityInsentient getNearestEntityInSight(int maxDistance, double blockIterateDistance)
	{				
		Location iteratedLoc;
		Vector iteratedDirection;
		Location entityLoc;
		
		maxDistance = Math.abs(maxDistance);
		blockIterateDistance = Math.abs(blockIterateDistance);
		
		//Invalid iterate distance
		if (blockIterateDistance == 0)
		{
			return null;
		}
		
		iteratedLoc = player.getEyeLocation();
		iteratedDirection = player.getEyeLocation().getDirection().multiply(blockIterateDistance);
		
		//Deliberately start at 1 instead of 0 so as not to spawn a particle in the player's face
		for (int i = 1; i <= maxDistance/blockIterateDistance; i++)
		{
			iteratedLoc.add(iteratedDirection);
			
			this.player.spawnParticle(Particle.CRIT, iteratedLoc, 0);
			
			//Iterate until a solid block is encountered
			if (!Selector.transparentMaterials.contains(this.player.getWorld().getBlockAt(iteratedLoc).getType()))
			{
				break;
			}
			
			for (EntityInsentient entity : this.getGame().getAllMWEntities())
			{
				//Length somehow represents the height of the entity
				//Width represents the width
				entityLoc = MWEntityType.getEntityLocation(entity);
				//Test if entity envelops one of the blocks or not
				if (iteratedLoc.getWorld().equals(entityLoc.getWorld()) 
						&& iteratedLoc.getX() >= entityLoc.getX() - entity.width/2 && iteratedLoc.getX() <= entityLoc.getX() + entity.width/2
						&& iteratedLoc.getY() >= entityLoc.getY() && iteratedLoc.getY() <= entityLoc.getY() + entity.length
						&& iteratedLoc.getZ() >= entityLoc.getZ() - entity.width/2 && iteratedLoc.getZ() <= entityLoc.getZ() + entity.width/2)
				{		
					MainMobWars.reportDebug(ChatColor.YELLOW + "Entity action: " + ((MWEntity) entity).getAction().getName());
					Location destLoc = this.getGame().getMoveHandler().destinationByEntity.get(entity);
					MainMobWars.reportDebug(ChatColor.AQUA + "Entity destination X: " + destLoc.getX() + ", Y: " + destLoc.getY() + ", Z: " + destLoc.getZ());
					
					return entity;
				}
			}
		}
		
		return null;
	}
	
	public enum SelectState
	{
		TEAM_ONLY,
		NON_TEAM_ONLY,
		ALL,
		NONE;
	}
	
	/**
	 * Last source that selected the current pos
	 * @author RLee
	 *
	 */
	private enum SelectPosSource
	{
		COMMAND,
		SELECT,
		NONE;
	}
}
