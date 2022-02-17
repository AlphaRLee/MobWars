package io.github.alpharlee.mobwars.mobs;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;

import org.bukkit.Bukkit;

import com.rlee.mobwars.Game;
import com.rlee.mobwars.MWEntityAction;
import com.rlee.mobwars.MainMobWars;
import com.rlee.mobwars.Team;

import net.minecraft.server.v1_10_R1.EntityHuman;
import net.minecraft.server.v1_10_R1.EntitySkeleton;
import net.minecraft.server.v1_10_R1.EntitySpider;
import net.minecraft.server.v1_10_R1.EntityZombie;
import net.minecraft.server.v1_10_R1.GenericAttributes;
import net.minecraft.server.v1_10_R1.PathfinderGoalFloat;
import net.minecraft.server.v1_10_R1.PathfinderGoalHurtByTarget;
import net.minecraft.server.v1_10_R1.PathfinderGoalLeapAtTarget;
import net.minecraft.server.v1_10_R1.PathfinderGoalLookAtPlayer;
import net.minecraft.server.v1_10_R1.PathfinderGoalMeleeAttack;
import net.minecraft.server.v1_10_R1.PathfinderGoalNearestAttackableTarget;
import net.minecraft.server.v1_10_R1.PathfinderGoalRandomLookaround;
import net.minecraft.server.v1_10_R1.PathfinderGoalRandomStroll;
import net.minecraft.server.v1_10_R1.World;

public class MWEntitySpider extends EntitySpider implements AutoStrikerEntity
{	
	public Game game;
	public Team team = Team.NO_TEAM;
	public MWEntityAction mwAction = MWEntityAction.WAITING;
	
	public MWEntitySpider(World world, Game game, Team team)
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
	
	@SuppressWarnings("unchecked")
	@Override
	protected void r() 
	{
		try
		{
			//Use reflection to access static nested classes
			//KLUDGE: This method relies on the name of the pathfinders. Be warned it is prone to change in version updates!
			Class<? extends PathfinderGoalMeleeAttack> PfGSpiderMeleeAttack = (Class<? extends PathfinderGoalMeleeAttack>) Class.forName("net.minecraft.server.v1_10_R1.EntitySpider$PathfinderGoalSpiderMeleeAttack");
			//Class<? extends PathfinderGoalNearestAttackableTarget> PfGSpiderNearestAttackableTarget = (Class<? extends PathfinderGoalNearestAttackableTarget>) Class.forName("net.minecraft.server.v1_10_R1.EntitySpider$PathfinderGoalSpiderNearestAttackableTarget");
			
			//Create desired constructors for classes
			Constructor<? extends PathfinderGoalMeleeAttack> PfGSpiderMeleeAttackConst = PfGSpiderMeleeAttack.getConstructor(EntitySpider.class);
			//Constructor<? extends PathfinderGoalNearestAttackableTarget> PfGSpiderNearestAttackableTargetConst = PfGSpiderNearestAttackableTarget.getConstructor(EntitySpider.class, Class.class);
			
			//Allow access to these constructors
			PfGSpiderMeleeAttackConst.setAccessible(true);
			//PfGSpiderNearestAttackableTargetConst.setAccessible(true);
			
			
	        this.goalSelector.a(1, new PathfinderGoalFloat(this));
	        this.goalSelector.a(3, new PathfinderGoalLeapAtTarget(this, 0.4F));
	        
	        this.goalSelector.a(4, PfGSpiderMeleeAttackConst.newInstance(this));
	        
	        //this.goalSelector.a(5, new PathfinderGoalRandomStroll(this, 0.8D));
	        this.goalSelector.a(6, new PathfinderGoalLookAtPlayer(this, EntityHuman.class, 8.0F));
	        this.goalSelector.a(6, new PathfinderGoalRandomLookaround(this));
	        //this.targetSelector.a(1, new PathfinderGoalHurtByTarget(this, false, new Class[0]));
	        
	        //this.targetSelector.a(2, PfGSpiderNearestAttackableTargetConst.newInstance(this, EntityZombie.class));
	        //this.targetSelector.a(3, PfGSpiderNearestAttackableTargetConst.newInstance(this, EntitySkeleton.class));
	        
		}
		catch (Exception e)
		{
			super.r();
			
			Bukkit.getLogger().info("Spider error! Exception: ");
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
