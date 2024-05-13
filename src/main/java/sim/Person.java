package main.java.sim;

import java.awt.Color;
import java.awt.Graphics2D;

import sim.engine.SimState;
import sim.engine.Steppable;
import sim.engine.Stoppable;
import sim.portrayal.DrawInfo2D;
import sim.portrayal.SimplePortrayal2D;
import sim.util.Bag;
import sim.util.Int2D;

public class Person extends SimplePortrayal2D implements Steppable {

	private Int2D loc, home;
	boolean alive = true;
	Color myColor = Color.green;
	Stoppable myStopper;
	Infection myInfection = null;
	
	// basic constructor
	public Person(int x_loc, int y_loc) {
		this(new Int2D(x_loc, y_loc));
	}
	
	public Person(Int2D loc) {
		this.loc = loc;
		this.home = new Int2D(loc.x, loc.y); // shallow copy that sucker
		myColor = Color.green;
	}
	
	@Override
	public void step(SimState state) {
		PumpHandleSim world = (PumpHandleSim) state;

		// wander
		int new_x = (1 - world.random.nextInt(3) + loc.x) % world.gridWidth, 
			new_y = (1 - world.random.nextInt(3) + loc.y) % world.gridHeight; // between -1 and 1
		loc = new Int2D(new_x, new_y);
		world.personGrid.setObjectLocation(this, loc);
		
		// infect others, if infectious
	}
	
	public void die(PumpHandleSim world) {
		world.personGrid.remove(this);
		alive = false;
		myStopper.stop(); // remove from the future schedule
		System.out.println("ALERT: Person " + this.hashCode() + " has DIED");
	}
	
	public void setColor(Color c) { myColor = c; }
	
	static final double displaySize = 10;
	public final void draw(Object object, Graphics2D graphics, DrawInfo2D info)
    {
//		double diamx = info.draw.width*VirusInfectionDemo.DIAMETER;
//	    double diamy = info.draw.height*VirusInfectionDemo.DIAMETER;
	
		
	    graphics.setColor( myColor );
	    graphics.fillOval((int)(info.draw.x-displaySize/2),(int)(info.draw.y-displaySize/2),(int)(displaySize),(int)(displaySize));
    }
	
	public boolean isAlive() { return alive; }
	public Int2D getLocation() { return loc; }
	public String getType() { return "Person"; }
	public void setStoppable(Stoppable stopper) { myStopper = stopper; }
	public boolean isInfected() { return myInfection == null; }
}