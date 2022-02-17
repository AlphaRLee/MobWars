package io.github.alpharlee.mobwars.mobs;

import com.rlee.mobwars.Game;
import com.rlee.mobwars.MWEntityAction;
import com.rlee.mobwars.Team;

import net.minecraft.server.v1_10_R1.EntityChicken;
import net.minecraft.server.v1_10_R1.EntityInsentient;
import net.minecraft.server.v1_10_R1.GenericAttributes;
import net.minecraft.server.v1_10_R1.PathfinderGoalFloat;
import net.minecraft.server.v1_10_R1.PathfinderGoalLookAtPlayer;
import net.minecraft.server.v1_10_R1.PathfinderGoalRandomLookaround;
import net.minecraft.server.v1_10_R1.World;
import pathfinders.MWPathfinderGoalChickenEnrage;

public class MWEntityChicken extends EntityChicken implements MWEntity
{
	public Game game;
	public Team team = Team.NO_TEAM;
	public MWEntityAction mwAction = MWEntityAction.WAITING;
	
	public float enrageChance = 0.1F;
	public double reinforcementRadius = 15;
	public float attackDamage = 1.0F;
	
	public MWEntityChicken(World world, Game game, Team team)
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
		this.goalSelector.a(0, new PathfinderGoalFloat(this));
        //this.goalSelector.a(1, new PathfinderGoalPanic(this, 1.4D));
        //this.goalSelector.a(2, new PathfinderGoalBreed(this, 1.0D));
        //this.goalSelector.a(3, new PathfinderGoalTempt(this, 1.0D, false, EntityChicken.bF));
        //this.goalSelector.a(4, new PathfinderGoalFollowParent(this, 1.1D));
        //this.goalSelector.a(5, new PathfinderGoalRandomStroll(this, 1.0D));
		this.goalSelector.a(2, new MWPathfinderGoalChickenEnrage(this, false));
        this.goalSelector.a(6, new PathfinderGoalLookAtPlayer(this, EntityInsentient.class, 6.0F));
        this.goalSelector.a(7, new PathfinderGoalRandomLookaround(this));
    }
	
	@Override
	protected void initAttributes() 
	{
        super.initAttributes();
        //Edit default follow range (assumed to be 16) to 100 blocks away to pursue targets/destinations up to 100 blocks
        this.getAttributeInstance(GenericAttributes.FOLLOW_RANGE).setValue(100.0D);
        this.getAttributeInstance(GenericAttributes.maxHealth).setValue(8.0);
    }
	
	/**
	 * Get the likelihood that this chicken will be enraged when attacked.
	 * Values range between 0 and 1.0, where 1.0 is a 100% chance this chicken (and other nearby chickens) will attack when hurt
	 * @return Enrage chance
	 */
	public float getEnrageChance()
	{
		return this.enrageChance;
	}
	
	/**
	 * Set the likelihood that this chicken will be enraged when attacked.
	 * Values range between 0 and 1.0, where 1.0 is a 100% chance this chicken (and other nearby chickens) will attack when hurt
	 * @param chance Chance this chicken will attack when injured. If set below 0, value is set to 0. If set above 1, value is set to 1
	 */
	public void setEnrageChance(float chance)
	{
		this.enrageChance = Math.max(Math.min(chance, 1F), 0F);
	}
}
