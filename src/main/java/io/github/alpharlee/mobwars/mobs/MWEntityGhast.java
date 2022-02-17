package io.github.alpharlee.mobwars.mobs;

import java.lang.reflect.Constructor;

import org.bukkit.Bukkit;

import com.rlee.mobwars.Game;
import com.rlee.mobwars.MWEntityAction;
import com.rlee.mobwars.MainMobWars;
import com.rlee.mobwars.Team;

import net.minecraft.server.v1_10_R1.AxisAlignedBB;
import net.minecraft.server.v1_10_R1.BlockPosition;
import net.minecraft.server.v1_10_R1.ControllerMove;
import net.minecraft.server.v1_10_R1.EntityGhast;
import net.minecraft.server.v1_10_R1.EntityHuman;
import net.minecraft.server.v1_10_R1.EntityLargeFireball;
import net.minecraft.server.v1_10_R1.EntityLiving;
import net.minecraft.server.v1_10_R1.EntitySpider;
import net.minecraft.server.v1_10_R1.PathfinderGoal;
import net.minecraft.server.v1_10_R1.PathfinderGoalMeleeAttack;
import net.minecraft.server.v1_10_R1.PathfinderGoalMoveTowardsTarget;
import net.minecraft.server.v1_10_R1.PathfinderGoalNearestAttackableTarget;
import net.minecraft.server.v1_10_R1.Vec3D;
import net.minecraft.server.v1_10_R1.World;

public class MWEntityGhast extends EntityGhast implements StrikerEntity
{
	public Game game;
	public Team team = Team.NO_TEAM;
	public MWEntityAction mwAction = MWEntityAction.WAITING;
	
	public double moveToX = MainMobWars.USE_DEFAULT;
	public double moveToY = MainMobWars.USE_DEFAULT;
	public double moveToZ = MainMobWars.USE_DEFAULT;
	
	public MWEntityGhast(World world, Game game, Team team)
	{
		super(world);
		this.game = game;
		this.team = team;
	}
	
	public double getMoveToX() 
	{
		return this.moveToX;
	}

	public double getMoveToY() 
	{
		return this.moveToY;
	}

	public double getMoveToZ() 
	{
		return this.moveToZ;
	}
	
	public void setMoveToX(double moveTo) 
	{
		this.moveToX = moveTo;
	}
	
	public void setMoveToY(double moveTo) 
	{
		this.moveToY = moveTo;
	}
	
	public void setMoveToZ(double moveTo) 
	{
		this.moveToZ = moveTo;
	}
	
	public void setMoveToLoc(double x, double y, double z)
	{
		this.moveToX = x;
		this.moveToY = y;
		this.moveToZ = z;
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
	
	/**
	 * Access the static ControllerMWGhast class
	 * @return
	 */
	public ControllerMWGhast getControllerMWGhast()
	{
		return (ControllerMWGhast) this.moveController;
	}
	
	@Override
	protected void r() {
		
		try
		{
		
			//Use reflection to access static nested classes
			//KLUDGE: This method relies on the name of the pathfinders. Be warned it is prone to change in version updates!
			Class<? extends PathfinderGoal> PfGGhastMoveTowardsTarget = (Class<? extends PathfinderGoal>) Class.forName("net.minecraft.server.v1_10_R1.EntityGhast$PathfinderGoalGhastMoveTowardsTarget");
			//Class<? extends PathfinderGoal> PfGGhastAttackTarget = (Class<? extends PathfinderGoal>) Class.forName("net.minecraft.server.v1_10_R1.EntityGhast$PathfinderGoalGhastAttackTarget");
			
			//Create desired constructors for classes
			Constructor<? extends PathfinderGoal> PfGGhastMoveTowardsTargetConst = PfGGhastMoveTowardsTarget.getConstructor(EntityGhast.class);
			//Constructor<? extends PathfinderGoal> PfGGhastAttackTargetConst = PfGGhastAttackTarget.getConstructor(EntityGhast.class);
			
			//Allow access to these constructors
			PfGGhastMoveTowardsTargetConst.setAccessible(true);
			//PfGGhastAttackTargetConst.setAccessible(true);
			
			//Note that IdleMove has a higher priority than pursuing target (Originally GhastMoveTowardsTarget set to 7, IdleMove set to 5)
	        this.goalSelector.a(5, new PathfinderGoalMWGhastMoveToLoc(this));
	        this.goalSelector.a(5, PfGGhastMoveTowardsTargetConst.newInstance(this));
	        this.goalSelector.a(4, new PathfinderGoalMWGhastAttackTarget(this));
	        //this.goalSelector.a(4, PfGGhastAttackTargetConst.newInstance(this));
	        //this.targetSelector.a(1, new PathfinderGoalTargetNearestPlayer(this));
        
		}
		catch (Exception e)
		{
			super.r();
			
			Bukkit.getLogger().info("ERROR! MWGhast pathfinder issue! Please copy stack trace and contact developers");
			e.printStackTrace();
		}
    }
	
	static class PathfinderGoalMWGhastAttackTarget extends PathfinderGoal {

        private final MWEntityGhast ghast;
        public int a;

        public PathfinderGoalMWGhastAttackTarget(MWEntityGhast mwEntityghast) {
            this.ghast = mwEntityghast;
        }

        /**
         * Checks if target is alive and not null
         * If target is dead and not null, target will be set to null
         * Returns true if target is not null and is alive
         * Returns false otherwise
         */
        public boolean a() {
        	EntityLiving entityLiving = this.ghast.getGoalTarget();
        	
        	if (entityLiving != null && entityLiving.isAlive())
        	{
        		return true;
        	}
        	else
        	{
        		//Remove the target if the target is either already null or dead
        		this.ghast.setGoalTarget(null);
        		return false;
        	}
        }

        public void c() {
            this.a = 0;
        }

        public void d() {
            this.ghast.a(false);
        }

        //Attack module
        public void e() {
            EntityLiving entityliving = this.ghast.getGoalTarget();
            double d0 = 64.0D;

            //Insert: Added check to see if ghast target was null or not, using this.a() and tested if target is alive or not
            if (this.a() && entityliving.h(this.ghast) < 4096.0D && this.ghast.hasLineOfSight(entityliving)) {
                World world = this.ghast.world;

                ++this.a;
                if (this.a == 10) {
                    world.a((EntityHuman) null, 1015, new BlockPosition(this.ghast), 0);
                }

                if (this.a == 20) {
                    double d1 = 4.0D;
                    Vec3D vec3d = this.ghast.f(1.0F);
                    double d2 = entityliving.locX - (this.ghast.locX + vec3d.x * 4.0D);
                    double d3 = entityliving.getBoundingBox().b + (double) (entityliving.length / 2.0F) - (0.5D + this.ghast.locY + (double) (this.ghast.length / 2.0F));
                    double d4 = entityliving.locZ - (this.ghast.locZ + vec3d.z * 4.0D);

                    world.a((EntityHuman) null, 1016, new BlockPosition(this.ghast), 0);
                    EntityLargeFireball entitylargefireball = new EntityLargeFireball(world, this.ghast, d2, d3, d4);

                    // CraftBukkit - set bukkitYield when setting explosionpower
                    entitylargefireball.bukkitYield = entitylargefireball.yield = this.ghast.getPower();
                    entitylargefireball.locX = this.ghast.locX + vec3d.x * 4.0D;
                    entitylargefireball.locY = this.ghast.locY + (double) (this.ghast.length / 2.0F) + 0.5D;
                    entitylargefireball.locZ = this.ghast.locZ + vec3d.z * 4.0D;
                    world.addEntity(entitylargefireball);
                    this.a = -40;
                }
            } else if (this.a > 0) {
                --this.a;
            }

            this.ghast.a(this.a > 10);
        }
    }
	
	static class PathfinderGoalMWGhastMoveToLoc extends PathfinderGoal {

        private final MWEntityGhast ghast;

        public double minDistance = 2.0D;
        public double maxDistance = 100.0D;
        
        public PathfinderGoalMWGhastMoveToLoc(MWEntityGhast mwEntityghast) {
            this.ghast = mwEntityghast;
            this.a(1);
        }

        /**
         * Evaluate if the ghast should or should not be allowed to move to its location
         */
        public boolean a() {
        	
        	//Distance measurements of ghast from target
        	Double distX = this.ghast.getMoveToX() - this.ghast.locX;
        	Double distY = this.ghast.getMoveToY() - this.ghast.locY;
        	Double distZ = this.ghast.getMoveToZ() - this.ghast.locZ;
        	
        	Double distSquared = distX * distX + distY * distY + distZ * distZ;
        	
        	//Return true (continue to this.c()) if the ghast is outside the tolerated distance (default to 0.5D) and inside the maximum move distance (default to 100.0D)
            return distSquared > this.minDistance * this.minDistance && distSquared <= maxDistance * maxDistance;
        }

        //Unknown function
        public boolean b() 
        {
            return false;
        }

        //Appears to command the ghast to its final location
        public void c() 
        {
            this.ghast.getControllerMove().a(this.ghast.getMoveToX(), this.ghast.getMoveToY(), this.ghast.getMoveToZ(), 1.0D);
        }
    }

	
	//DEAD CODE: Class unnecessary
	//DEAD CODE
	//DEAD CODE
	//DEAD CODE
	//DEAD CODE
	//DEAD CODE
	/**
	 * Copied variant of static class EntityGhast.ControllerGhast
	 * Function to override default move procedures and allow movement to designated locations
	 * @author RLee
	 *
	 */
    public static class ControllerMWGhast extends ControllerMove {

    	//Many of the field names are kept in their original obfuscated form until their purpose is revealed
        private final MWEntityGhast mwEntityGhast;
        
        //Furthest distance acceptable for the ghast to be from its designated target
        public double distanceTolerance = 1.0D;

        public ControllerMWGhast(MWEntityGhast mwGhast) 
        {
            this(mwGhast, 0.5D);
        }

        public ControllerMWGhast(MWEntityGhast mwGhast, double distanceTolerance)
        {
        	super(mwGhast);
        	this.mwEntityGhast = mwGhast;
        	this.distanceTolerance = distanceTolerance;
        	
        	//Force default state onto this.c to prevent pre-assigned movement
        	this.c = MainMobWars.USE_DEFAULT;
        }
        
        /**
         * Execute an attempt at a forced movement on the ghast
         * @param x X coordinate to move to
         * @param y Y coordinate to move to
         * @param z Z coordinate to move to
         */
        public void moveToLoc(double x, double y, double z)
        {
        	this.b = x;
            this.c = y;
            this.d = z;
            
            //Set status as ready to move
            this.h = ControllerMove.Operation.MOVE_TO;
            
            this.c();
        }
        
        public void c() {
        	
            if (this.h == ControllerMove.Operation.MOVE_TO) 
            {
                double toX = this.b - this.mwEntityGhast.locX;
                double toY = this.c - this.mwEntityGhast.locY;
                double toZ = this.d - this.mwEntityGhast.locZ;
                double distance = toX * toX + toY * toY + toZ * toZ;

                
                //If the distance to the target is within the tolerated radius AND
                //If the coordinate desired is not the default (unset) coordinate
                if (distance > this.distanceTolerance * this.distanceTolerance && this.c != MainMobWars.USE_DEFAULT) 
                {
                    distance = Math.sqrt(distance);
                    if (this.b(this.b, this.c, this.d, distance)) 
                    {
                    	//All operations appear to take exactly 10 ticks to traverse
                        this.mwEntityGhast.motX += toX / distance * 0.1D;
                        this.mwEntityGhast.motY += toY / distance * 0.1D;
                        this.mwEntityGhast.motZ += toZ / distance * 0.1D;
                    } 
                    else 
                    {
                        this.h = ControllerMove.Operation.WAIT;
                    }
                }

            }
        }

        //Appears to validate if region can be accessed without interruption
        //NOTE: This infers that the ghast provides no automated pathfinders around obstacles
        private boolean b(double toX, double toY, double toZ, double distance) 
        {
            double d4 = (toX - this.mwEntityGhast.locX) / distance;
            double d5 = (toY - this.mwEntityGhast.locY) / distance;
            double d6 = (toZ - this.mwEntityGhast.locZ) / distance;
            AxisAlignedBB axisalignedbb = this.mwEntityGhast.getBoundingBox();

            //Iterates all integers between 1 and the distance
            for (int i = 1; (double) i < distance; ++i) 
            {
            	//Terminate procedure if no safe location found
                axisalignedbb = axisalignedbb.c(d4, d5, d6);
                if (!this.mwEntityGhast.world.getCubes(this.mwEntityGhast, axisalignedbb).isEmpty()) 
                {
                    return false;
                }
            }

            return true;
        }
    }



	
}
