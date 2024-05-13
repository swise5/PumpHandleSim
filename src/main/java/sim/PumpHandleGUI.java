package main.java.sim;

import java.awt.Color;

import javax.swing.JFrame;

import sim.display.Controller;
import sim.display.Display2D;
import sim.display.GUIState;
import sim.engine.SimState;
import sim.field.grid.SparseGrid2D;
import sim.portrayal.grid.SparseGridPortrayal2D;
import sim.portrayal.simple.CircledPortrayal2D;
import sim.portrayal.simple.LabelledPortrayal2D;
import sim.portrayal.simple.OvalPortrayal2D;

public class PumpHandleGUI extends GUIState {

	// holder
	PumpHandleSim sim;
	
	
	// visualising space
	SparseGridPortrayal2D people = new SparseGridPortrayal2D();
	
	// objects for visualisation
	public Display2D display;
	public JFrame displayFrame;

	/** CONSTRUCTORS */
	
	public PumpHandleGUI(SimState state) {
		super(state);
		sim = (PumpHandleSim) state;
	}

	public PumpHandleGUI() {
		super(new PumpHandleSim(System.currentTimeMillis()));
	}
	
	public Object getSimulationInspectedObject() { return state; }
	
	/** Begins the simulation */
	public void start() {
		super.start();
		setupPortrayals();
	}

	/** Loads the simulation from a point */
	public void load(SimState state) {
		super.load(state);
		setupPortrayals();
	}
	

	public void setupPortrayals(){
		
		PumpHandleSim world = (PumpHandleSim) state;
		
		people.setField(world.personGrid);
		
		
		display.reset();
		display.setBackdrop(Color.white);//new Color(10,10,10));

		// redraw the display
		display.repaint();

	}
	
	public void init(Controller c) {
		super.init(c);

		// the map visualization
		
		display = new Display2D(1000, 1000, this);
		//display.setBackdrop(new Color(10,10,10));
		
		displayFrame = display.createFrame();
		c.registerFrame(displayFrame); // register the frame so it appears in the "Display" list
		displayFrame.setVisible(true);		

		display.attach(people, "Students");

	}
	
	public void quit() {
		super.quit();

		if (displayFrame != null)
			displayFrame.dispose();
		displayFrame = null; // let gc
		display = null; // let gc
	}

	/** Returns the name of the simulation */
	public static String getName() { return "PumpHandleSim"; }
	
	public static void main(String [] args){
		new PumpHandleGUI().createController();
	}
}
