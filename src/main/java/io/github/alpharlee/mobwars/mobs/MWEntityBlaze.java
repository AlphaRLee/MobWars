package io.github.alpharlee.mobwars.mobs;

import java.lang.reflect.Constructor;

import com.rlee.mobwars.Game;
import com.rlee.mobwars.MWEntityAction;
import com.rlee.mobwars.Team;

import net.minecraft.server.v1_10_R1.EntityBlaze;
import net.minecraft.server.v1_10_R1.EntityGhast;
import net.minecraft.server.v1_10_R1.EntityHuman;
import net.minecraft.server.v1_10_R1.EntityInsentient;
import net.minecraft.server.v1_10_R1.EntityWolf;
import net.minecraft.server.v1_10_R1.EntityZombie;
import net.minecraft.server.v1_10_R1.GenericAttributes;
import net.minecraft.server.v1_10_R1.PathfinderGoal;
import net.minecraft.server.v1_10_R1.PathfinderGoalAvoidTarget;
import net.minecraft.server.v1_10_R1.PathfinderGoalFleeSun;
import net.minecraft.server.v1_10_R1.PathfinderGoalFloat;
import net.minecraft.server.v1_10_R1.PathfinderGoalLookAtPlayer;
import net.minecraft.server.v1_10_R1.PathfinderGoalMoveTowardsRestriction;
import net.minecraft.server.v1_10_R1.PathfinderGoalRandomLookaround;
import net.minecraft.server.v1_10_R1.PathfinderGoalRestrictSun;
import net.minecraft.server.v1_10_R1.World;

public class MWEntityBlaze extends EntityBlaze implements AutoStrikerEntity
{
	public Game game;
	public Team team = Team.NO_TEAM;
	public MWEntityAction mwAction = MWEntityAction.WAITING;
	
	public MWEntityBlaze(World world, Game game, Team team)
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
			//Use reflection to access static nested classes
			//KLUDGE: This method relies on the name of the pathfinders. Be warned it is prone to change in version updates!
			Class<? extends PathfinderGoal> PfGBlazeFireball = (Class<? extends PathfinderGoal>) Class.forName("net.minecraft.server.v1_10_R1.EntityBlaze$PathfinderGoalBlazeFireball");
			
			//Create desired constructors for classes
			Constructor<? extends PathfinderGoal> PfGBlazeFireballConst;
			
			PfGBlazeFireballConst = PfGBlazeFireball.getConstructor(EntityBlaze.class);
			
			//Allow access to these constructors
			PfGBlazeFireballConst.setAccessible(true);
			
			
			this.goalSelector.a(4, PfGBlazeFireballConst.newInstance(this));
	        
			//this.goalSelector.a(4, new EntityBlaze.PathfinderGoalBlazeFireball(this));
	        this.goalSelector.a(5, new PathfinderGoalMoveTowardsRestriction(this, 1.0D));
	        //this.goalSelector.a(7, new PathfinderGoalRandomStroll(this, 1.0D));
	        this.goalSelector.a(8, new PathfinderGoalLookAtPlayer(this, EntityInsentient.class, 8.0F));
	        this.goalSelector.a(8, new PathfinderGoalRandomLookaround(this));
	        //this.targetSelector.a(1, new PathfinderGoalHurtByTarget(this, true, new Class[0]));
	        //this.targetSelector.a(2, new PathfinderGoalNearestAttackableTarget(this, EntityHuman.class, true));
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	@Override
	protected void initAttributes() 
	{
        super.initAttributes();
        //Edit default follow range (assumed to be 16) to 100 blocks away to pursue targets/destinations up to 100 blocks
        this.getAttributeInstance(GenericAttributes.FOLLOW_RANGE).setValue(100.0D);
    }
}
