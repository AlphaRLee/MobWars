package io.github.alpharlee.mobwars.mobs;

import java.lang.reflect.Field;

import org.bukkit.Bukkit;

import com.rlee.mobwars.Game;
import com.rlee.mobwars.MWEntityAction;
import com.rlee.mobwars.MainMobWars;
import com.rlee.mobwars.Team;

import net.md_5.bungee.api.ChatColor;
import net.minecraft.server.v1_10_R1.EntityInsentient;
import net.minecraft.server.v1_10_R1.EntitySheep;
import net.minecraft.server.v1_10_R1.GenericAttributes;
import net.minecraft.server.v1_10_R1.PathfinderGoalEatTile;
import net.minecraft.server.v1_10_R1.PathfinderGoalFloat;
import net.minecraft.server.v1_10_R1.PathfinderGoalLookAtPlayer;
import net.minecraft.server.v1_10_R1.PathfinderGoalRandomLookaround;
import net.minecraft.server.v1_10_R1.World;
import pathfinders.MWPathfinderGoalEatTile;

public class MWEntitySheep extends EntitySheep implements SupportEntity
{
	public Game game;
	public Team team = Team.NO_TEAM;
	public MWEntityAction mwAction = MWEntityAction.WAITING;
	
	//Unknown values copied from EntitySheep.class
	protected int bB = 0;
	protected MWPathfinderGoalEatTile eatTilePathfinder;
	
	public MWEntitySheep(World world, Game game, Team team)
	{
		super(world);
		this.game = game;
		this.team = team;
	}
	
	public Game getGame()
	{
		return this.game;
	}
	
	public void setGame(Game game)
	{
		this.game = game;
	}
	
	public Team getTeam()
	{
		return this.team;
	}
	
	public void setTeam(Team team)
	{
		this.team = team;
	}
	
	public MWEntityAction getAction()
	{
		return this.mwAction;
	}
	
	public void setAction(MWEntityAction action)
	{
		this.mwAction = action;
	}
	
	@Override
	 protected void r() 
	{	
		try
		{
	        this.eatTilePathfinder = new MWPathfinderGoalEatTile(this);
	        
	        //Establish the super.bC to be this pathfinder
	        for (Field field : this.getClass().getSuperclass().getDeclaredFields())
	        {
	        	if (field.getType().getSimpleName().equals(PathfinderGoalEatTile.class.getSimpleName()))
	        	{
	        		field.setAccessible(true);
	        		
	        		field.set((EntitySheep) this, this.eatTilePathfinder);
	        	}
	        }
	        
	        this.goalSelector.a(0, new PathfinderGoalFloat(this));
	        //this.goalSelector.a(1, new PathfinderGoalPanic(this, 1.25D));
	        //this.goalSelector.a(2, new PathfinderGoalBreed(this, 1.0D));
	        //this.goalSelector.a(3, new PathfinderGoalTempt(this, 1.1D, Items.WHEAT, false));
	        //this.goalSelector.a(4, new PathfinderGoalFollowParent(this, 1.1D));
	        this.goalSelector.a(5, this.eatTilePathfinder);
	        //this.goalSelector.a(6, new PathfinderGoalRandomStroll(this, 1.0D));
	        this.goalSelector.a(7, new PathfinderGoalLookAtPlayer(this, EntityInsentient.class, 6.0F));
	        this.goalSelector.a(8, new PathfinderGoalRandomLookaround(this));
		}
		catch (Exception e)
		{
			super.r();
			Bukkit.broadcastMessage(ChatColor.RED + "Pathfinder instantiation of MW Shep failed! Please see stack trace");
			e.printStackTrace();
		}
    }
	
	@Override
	protected void initAttributes() 
	{
        super.initAttributes();
        //Edit default follow range (assumed to be 16) to 100 blocks away to pursue targets/destinations up to 100 blocks
        this.getAttributeInstance(GenericAttributes.FOLLOW_RANGE).setValue(100.0D);
        this.getAttributeInstance(GenericAttributes.maxHealth).setValue(30.0);
    }
	
	@Override
	protected void M()
	{	
		this.bB = this.eatTilePathfinder.f();
		super.M();
	}
	
	@Override
	public void n() 
	{
        if (this.world.isClientSide) {
            this.bB = Math.max(0, this.bB - 1);
        }

        super.n();
    }
}
