package sim;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.HashSet;

import sim.engine.SimState;
import sim.engine.Steppable;
import sim.engine.Stoppable;
import sim.portrayal.DrawInfo2D;
import sim.portrayal.SimplePortrayal2D;
import sim.util.Bag;
import sim.util.Int2D;

public class Person extends SimplePortrayal2D implements Steppable {

	private Int2D loc;
	boolean alive = true;
	Color myColor = Color.green;
	Stoppable myStopper;
	Infection myInfection = null;
	HashSet <Class> immuneTo;
	
	// basic constructor
	public Person(int x_loc, int y_loc) {
		this(new Int2D(x_loc, y_loc));
	}
	
	public Person(Int2D loc) {
		this.loc = loc; 		// set up initial position
		myColor = Color.green; 	// default setup: no infection, susceptible
		immuneTo = new HashSet <Class> (); // not initially immune to anything
	}
	
	@Override
	public void step(SimState state) {
		PumpHandleSim world = (PumpHandleSim) state;

		// wander
		wander(world);		
	}
	
	public void wander(PumpHandleSim world) {
		int new_x = (1 - world.random.nextInt(3) + loc.x + world.gridWidth) % world.gridWidth, 
			new_y = (1 - world.random.nextInt(3) + loc.y + world.gridHeight) % world.gridHeight; // between -1 and 1
		
			loc = new Int2D(new_x, new_y);
			world.personGrid.setObjectLocation(this, loc);

	}
	
	public void die(PumpHandleSim world) {
		
		// remove me from the world and make note of it
		world.personGrid.remove(this);
		world.newDeathsThisTick++;
		
		// update my own attributes
		alive = false;
		
		// remove from the future schedule
		myStopper.stop();
		
		// in verbose mode, report the death in terminal
		if(world.verbose)
			System.out.println("ALERT: Person " + this.hashCode() + " has DIED");
	}
	
	public void setColor(Color c) { myColor = c; }
	
	static final double displaySize = 10;
	public final void draw(Object object, Graphics2D graphics, DrawInfo2D info)
    {
	    graphics.setColor( myColor );
	    graphics.fillOval((int)(info.draw.x-displaySize/2),(int)(info.draw.y-displaySize/2),(int)(displaySize),(int)(displaySize));
    }
	
	public boolean isAlive() { return alive; }
	public Int2D getLocation() { return loc; }
	public String getType() { return "Person"; }
	public void setStoppable(Stoppable stopper) { myStopper = stopper; }
	public boolean isInfected() { return myInfection != null; }
	public boolean isImmuneTo(Infection i) { return immuneTo.contains(i.getClass()); }
	public void gainImmunityTo(Infection i) { immuneTo.add(i.getClass()); }
}