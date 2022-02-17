package io.github.alpharlee.mobwars.mobs;

import com.rlee.mobwars.Game;
import com.rlee.mobwars.MWEntityAction;
import com.rlee.mobwars.MainMobWars;
import com.rlee.mobwars.Team;

import net.minecraft.server.v1_10_R1.EntityHuman;
import net.minecraft.server.v1_10_R1.EntityInsentient;
import net.minecraft.server.v1_10_R1.EntityIronGolem;
import net.minecraft.server.v1_10_R1.EntitySkeleton;
import net.minecraft.server.v1_10_R1.EntityWolf;
import net.minecraft.server.v1_10_R1.EntityZombie;
import net.minecraft.server.v1_10_R1.GenericAttributes;
import net.minecraft.server.v1_10_R1.PathfinderGoalAvoidTarget;
import net.minecraft.server.v1_10_R1.PathfinderGoalFleeSun;
import net.minecraft.server.v1_10_R1.PathfinderGoalFloat;
import net.minecraft.server.v1_10_R1.PathfinderGoalHurtByTarget;
import net.minecraft.server.v1_10_R1.PathfinderGoalLookAtPlayer;
import net.minecraft.server.v1_10_R1.PathfinderGoalNearestAttackableTarget;
import net.minecraft.server.v1_10_R1.PathfinderGoalRandomLookaround;
import net.minecraft.server.v1_10_R1.PathfinderGoalRandomStroll;
import net.minecraft.server.v1_10_R1.PathfinderGoalRestrictSun;
import net.minecraft.server.v1_10_R1.World;

public class MWEntitySkeleton extends EntitySkeleton implements AutoStrikerEntity
{	
	public Game game;
	public Team team = Team.NO_TEAM;
	public MWEntityAction mwAction = MWEntityAction.WAITING;
	
	public MWEntitySkeleton(World world, Game game, Team team)
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
        this.goalSelector.a(1, new PathfinderGoalFloat(this));
        this.goalSelector.a(2, new PathfinderGoalRestrictSun(this));
        this.goalSelector.a(3, new PathfinderGoalFleeSun(this, 1.0D));
        this.goalSelector.a(3, new PathfinderGoalAvoidTarget(this, EntityWolf.class, 6.0F, 1.0D, 1.2D));
        
        //this.goalSelector.a(5, new PathfinderGoalWalkToTile(this, 1.0D));
        
        //this.goalSelector.a(5, new PathfinderGoalRandomStroll(this, 1.0D));
        this.goalSelector.a(6, new PathfinderGoalLookAtPlayer(this, EntityInsentient.class, 8.0F));
        this.goalSelector.a(6, new PathfinderGoalRandomLookaround(this));
        //this.targetSelector.a(1, new PathfinderGoalHurtByTarget(this, false, new Class[0]));
        //this.targetSelector.a(2, new PathfinderGoalNearestAttackableTarget(this, EntityZombie.class, true));
	}
	
	@Override
	protected void initAttributes() 
	{
        super.initAttributes();
        //Edit default follow range (assumed to be 16) to 100 blocks away to pursue targets/destinations up to 100 blocks
        this.getAttributeInstance(GenericAttributes.FOLLOW_RANGE).setValue(100.0D);
    }
}
