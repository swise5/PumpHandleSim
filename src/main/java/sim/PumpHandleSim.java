package main.java.sim;

import sim.engine.*;
import sim.field.grid.*;
import sim.util.Bag;
import sim.util.Int2D;

class PumpHandleSim extends SimState {
	
	SparseGrid2D personGrid;
	
	int gridWidth, gridHeight;
	int numPeople;
	
	
	public PumpHandleSim(long seed) {
		this(seed, 20, 20, 20);
	}

	public PumpHandleSim(long seed, int grid_width, int grid_height, int num_people) {
		super(seed);
		gridWidth = grid_width;
		gridHeight = grid_height;
		
		// initialise objects to hold environment
		personGrid = new SparseGrid2D(grid_width, grid_height);
		this.numPeople = num_people;
	}
	
	public void start() {
		super.start();
		
		// initialise individual PEOPLE
		for(int i = 0; i < numPeople; i++) {
			
			// find a random, unoccupied spot
			Int2D proposedLocation = pickRandomLocation(gridWidth, gridHeight);
			while(personGrid.getObjectsAtLocation(proposedLocation) != null)
				proposedLocation = pickRandomLocation(gridWidth, gridHeight);
			
			// create the person
			Person person = new Person(proposedLocation);
			
			// add them to the space
			personGrid.setObjectLocation(person, proposedLocation);
			
			// add them to the schedule and save the stopper (so that they can stop updating if they die)
			person.setStoppable(schedule.scheduleRepeating(person));
		}
		
		// pick a random person and give them the disease
		Person indexCase = (Person) personGrid.allObjects.get(0);
		Infection i = new Infection(indexCase, Infection.InfectionStatus.EXPOSED);
		schedule.scheduleOnce(i);

	}
	
	public static void main(String [] args) {
		PumpHandleSim sim = new PumpHandleSim(System.currentTimeMillis());
		sim.start();
		for(int i = 0; i < 10; i++)
			sim.schedule.step(sim);
		sim.finish();
	}
	
	// UTILITIES
	
	private Int2D pickRandomLocation(int width, int height) {
		return new Int2D(random.nextInt(width), random.nextInt(height));
	}
	

}