package io.github.alpharlee.mobwars.mobs;

import com.rlee.mobwars.Game;
import com.rlee.mobwars.MWEntityAction;
import com.rlee.mobwars.Team;

public interface MWEntity
{
	public Game getGame();
	
	public void setGame(Game game);
	
	public Team getTeam();
	
	public void setTeam(Team team);
	
	public MWEntityAction getAction();
	
	public void setAction(MWEntityAction action);
}
