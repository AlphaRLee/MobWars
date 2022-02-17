package io.github.alpharlee.mobwars.mobs;

import com.rlee.mobwars.Game;
import com.rlee.mobwars.MWEntityAction;
import com.rlee.mobwars.Team;

import net.minecraft.server.v1_10_R1.EntityHuman;
import net.minecraft.server.v1_10_R1.EntityInsentient;
import net.minecraft.server.v1_10_R1.EntityVillager;
import net.minecraft.server.v1_10_R1.GenericAttributes;
import net.minecraft.server.v1_10_R1.PathfinderGoalFloat;
import net.minecraft.server.v1_10_R1.PathfinderGoalInteract;
import net.minecraft.server.v1_10_R1.PathfinderGoalInteractVillagers;
import net.minecraft.server.v1_10_R1.PathfinderGoalLookAtPlayer;
import net.minecraft.server.v1_10_R1.PathfinderGoalLookAtTradingPlayer;
import net.minecraft.server.v1_10_R1.PathfinderGoalMoveTowardsRestriction;
import net.minecraft.server.v1_10_R1.PathfinderGoalTakeFlower;
import net.minecraft.server.v1_10_R1.PathfinderGoalTradeWithPlayer;
import net.minecraft.server.v1_10_R1.World;

public class MWEntityVillager extends EntityVillager implements SupportEntity
{
	public Game game;
	public Team team = Team.NO_TEAM;
	public MWEntityAction mwAction = MWEntityAction.WAITING;
	
	public MWEntityVillager(World world, Game game, Team team)
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
	
	public void setTeam(Team team)
	{
		this.team = team;
	}

	public Team getTeam()
	{
		return this.team;
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
        //this.goalSelector.a(1, new PathfinderGoalAvoidTarget(this, EntityZombie.class, 8.0F, 0.6D, 0.6D));
        this.goalSelector.a(1, new PathfinderGoalTradeWithPlayer(this));
        this.goalSelector.a(1, new PathfinderGoalLookAtTradingPlayer(this));
        //this.goalSelector.a(2, new PathfinderGoalMoveIndoors(this));
        //this.goalSelector.a(3, new PathfinderGoalRestrictOpenDoor(this));
        //this.goalSelector.a(4, new PathfinderGoalOpenDoor(this, true));
        this.goalSelector.a(5, new PathfinderGoalMoveTowardsRestriction(this, 0.6D));
        //this.goalSelector.a(6, new PathfinderGoalMakeLove(this));
        this.goalSelector.a(7, new PathfinderGoalTakeFlower(this));
        this.goalSelector.a(9, new PathfinderGoalInteract(this, EntityHuman.class, 3.0F, 1.0F));
        this.goalSelector.a(9, new PathfinderGoalInteractVillagers(this));
        //this.goalSelector.a(9, new PathfinderGoalRandomStroll(this, 0.6D));
        this.goalSelector.a(10, new PathfinderGoalLookAtPlayer(this, EntityInsentient.class, 8.0F));
    }
	
	@Override
	protected void initAttributes() {
        super.initAttributes();
        //Edit default follow range (assumed to be 16) to 100 blocks away to pursue targets/destinations up to 100 blocks
        this.getAttributeInstance(GenericAttributes.FOLLOW_RANGE).setValue(100.0D);
    }	
}
