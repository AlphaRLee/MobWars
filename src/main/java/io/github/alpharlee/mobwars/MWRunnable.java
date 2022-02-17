package io.github.alpharlee.mobwars;

import java.util.Map.Entry;

import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import com.rlee.mobwars.mobs.StrikerEntity;

import net.minecraft.server.v1_10_R1.EntityInsentient;

public class MWRunnable extends BukkitRunnable
{
	private MainMobWars main;
	private int timerCount = 0;
	
	public double attackerBoundRadius = 10D;
	public boolean allowMultiAttack = true;
	
	public int showParticleFrequency = 5;
	
	public int maxTotalParticleDistance = 100;
	public double particleDistance = 1;
	
	public MWRunnable(MainMobWars main)
	{
		this.main = main;
	}
	
	@Override
	public void run()
	{
		Player player;
		Selector selector;
		
		//Perform the repeating tasks for every single game and every single entity within
		for (Game game : main.games)
		{
			game.getMoveHandler().allStopAtDestination();
			game.getMoveHandler().allPassiveMove();
			
			for (EntityInsentient entity : game.getAllMWEntities())
			{
				//---------------------------------------------------------------------------------------
				//KLUDGE: More control over these numbers please
				//----------------------------------------------------------------------------------------
				game.getAttackHandler().entityPassiveAttack(entity, MainMobWars.testCriteria, attackerBoundRadius, allowMultiAttack);
			}
			
			if (timerCount % this.showParticleFrequency == 0)
			{
				for (Entry<Player, Selector> selectorByPlayer : game.selectorsByPlayer.entrySet())
				{
					player = selectorByPlayer.getKey();
					selector = selectorByPlayer.getValue();
					
					if (player != null && selector != null)
					{
						//Display the particle destinations for the selected entities
						game.getParticleDisplayer().showEntityTargetGoal(player, selector.getSelectedTeam(selector.selectedEntities, game.getPlayersTeam(player), false), maxTotalParticleDistance, particleDistance);
					}
				}
			}
		}
		
		//Iterate the timer, and if applicable, reset it to 0 every 20 ticks
		timerCount++;
		timerCount %= 20;
	}

}
