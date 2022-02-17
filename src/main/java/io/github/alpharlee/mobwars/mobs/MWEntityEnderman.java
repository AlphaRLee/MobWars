package io.github.alpharlee.mobwars.mobs;

import com.rlee.mobwars.Game;
import com.rlee.mobwars.MWEntityAction;
import com.rlee.mobwars.Team;

import net.minecraft.server.v1_10_R1.EntityEnderman;
import net.minecraft.server.v1_10_R1.EntityInsentient;
import net.minecraft.server.v1_10_R1.GenericAttributes;
import net.minecraft.server.v1_10_R1.PathfinderGoalFloat;
import net.minecraft.server.v1_10_R1.PathfinderGoalLookAtPlayer;
import net.minecraft.server.v1_10_R1.PathfinderGoalMeleeAttack;
import net.minecraft.server.v1_10_R1.PathfinderGoalRandomLookaround;
import net.minecraft.server.v1_10_R1.World;

public class MWEntityEnderman extends EntityEnderman implements AutoStrikerEntity
{
	public Game game;
	public Team team = Team.NO_TEAM;
	public MWEntityAction mwAction = MWEntityAction.WAITING;
	
	public MWEntityEnderman(World world, Game game, Team team)
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
        this.goalSelector.a(2, new PathfinderGoalMeleeAttack(this, 1.0D, false));
        //this.goalSelector.a(7, new PathfinderGoalRandomStroll(this, 1.0D));
        this.goalSelector.a(8, new PathfinderGoalLookAtPlayer(this, EntityInsentient.class, 8.0F));
        this.goalSelector.a(8, new PathfinderGoalRandomLookaround(this));
        //this.goalSelector.a(10, new EntityEnderman.PathfinderGoalEndermanPlaceBlock(this));
        //this.goalSelector.a(11, new EntityEnderman.PathfinderGoalEndermanPickupBlock(this));
        //this.targetSelector.a(1, new EntityEnderman.PathfinderGoalPlayerWhoLookedAtTarget(this));
        //this.targetSelector.a(2, new PathfinderGoalHurtByTarget(this, false, new Class[0]));
        /*
        this.targetSelector.a(3, new PathfinderGoalNearestAttackableTarget(this, EntityEndermite.class, 10, true, false, new Predicate() {
            public boolean a(@Nullable EntityEndermite entityendermite) {
                return entityendermite.o();
            }
		
            public boolean apply(Object object) {
                return this.a((EntityEndermite) object);
            }
        }));
    	 */
    }
	
	@Override
	protected void initAttributes() 
	{
        super.initAttributes();
        //Edit default follow range (assumed to be 16) to 100 blocks away to pursue targets/destinations up to 100 blocks
        this.getAttributeInstance(GenericAttributes.FOLLOW_RANGE).setValue(100.0D);
    }
}
