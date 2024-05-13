package main.java.sim;

import java.awt.Color;

import sim.engine.SimState;
import sim.engine.Steppable;
import sim.engine.Stoppable;
import sim.field.grid.Grid2D;
import sim.util.Bag;
import sim.util.Int2D;

public class Infection implements Steppable {

	private Person host;
	private InfectionStatus status;
	private InfectionStatus nextStatus;
	
	private Stoppable spreading;
	
	private int infectiousDistance = 3;
	private double spreadProbability = .3;
	
	public enum InfectionStatus {
	    EXPOSED,
	    INFECTIOUS, 
	    IMMUNE,
	    DEAD; 
	}
	
	
	
	public Infection(Person myHost, InfectionStatus myStatus) {
		host = myHost;
		status = myStatus;
		host.myInfection = this;
	}
	
	public Person getHost() { return host; }

	@Override
	public void step(SimState state) {
		
		// progress
		if (nextStatus != null) {
			status = nextStatus;
			nextStatus = null;
		}
		
		// update, if appropriate
		if(host.isAlive())
			state.schedule.scheduleOnce(state.schedule.getTime() + timeToNextUpdate(state), this);
	}
	
	private double timeToNextUpdate(SimState state) {
		switch(status) {
		
			case EXPOSED:
				host.setColor(Color.yellow);
				nextStatus = InfectionStatus.INFECTIOUS;
				return 3;
		
			case INFECTIOUS:				
				host.setColor(Color.red);
				
				// set up a spreading mechanism to run until recovery
				spreading = state.schedule.scheduleRepeating(new Steppable() {

					@Override
					public void step(SimState state) {
						infectThoseAroundMe((PumpHandleSim)state);
					}
					
				});
				// next, the person will either die... 
				if(state.random.nextBoolean())
					nextStatus = InfectionStatus.DEAD;
				else // ...or gain immunity
					nextStatus = InfectionStatus.IMMUNE;				
				
				return 10;
			
			case IMMUNE:
				spreading.stop();
				host.setColor(Color.gray);
				return state.schedule.AFTER_SIMULATION;
			
			case DEAD:
				spreading.stop();
				host.setColor(Color.black);
				host.die((PumpHandleSim)state);
				status = InfectionStatus.DEAD;
				return Integer.MAX_VALUE;
				
			default:
				host.setColor(Color.pink);
				return 1;
		}
		
	}
	
	
	void infectThoseAroundMe(PumpHandleSim world) {
		
		// collect the other Persons near the host to see who is exposed
		Int2D hostPos = host.getLocation();
		Bag nearby = world.personGrid.getHexagonalNeighbors(hostPos.x, hostPos.y, infectiousDistance, Grid2D.TOROIDAL, true);
		
		for(Object o: nearby) { // iterate through others to whom the disease might spread
			if(o == host) continue; // we're already infecting that person
			else if (((Person) o).isInfected()) continue; // simplifying assumption that can only have this kind of disease once at a time
			
			// potentially spread the disease
			if(world.random.nextDouble() < spreadProbability) {
				
				// they've been exposed - update!
				Person p = (Person) o;
				Infection i = new Infection(p, InfectionStatus.EXPOSED);
				world.schedule.scheduleOnce(i);
			}
		}
	}
}