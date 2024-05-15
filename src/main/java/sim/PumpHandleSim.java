package sim;

import java.util.ArrayList;
import java.util.Properties;
import java.io.FileInputStream;

import sim.engine.*;
import sim.field.grid.*;
import sim.util.Bag;
import sim.util.Int2D;

public class PumpHandleSim extends SimState {

	//
	// SETUP
	//
	
	int gridWidth, gridHeight;
	int numPeople;
	int numInitialCases;
	
	//
	// ENVIRONMENT
	//

	SparseGrid2D personGrid;
	
	//
	// RECORDING
	//
	
	ArrayList <Integer> numCases, numDeaths;
	int newCasesThisTick = 0, newDeathsThisTick = 0, totalCases = 0, totalDeaths = 0;
	
	// OUTPUT

	public boolean verbose = false;
	
	//
	//
	// CONSTRUCTORS
	//
	//
	
	public PumpHandleSim(long seed) {
		this(seed, Thread.currentThread().getContextClassLoader().getResource("").getPath() + "default.properties");
	}
	
	public PumpHandleSim(long seed, String propertiesFile){//, int grid_width, int grid_height, int num_people, int num_infections_seeded) {
		super(seed);
		
		//
		// read in properties
		//
		

		Properties simProps = new Properties();
		try {
			simProps.load(new FileInputStream(propertiesFile));
			
			// set up the environment
			gridWidth = Integer.parseInt(simProps.getProperty("gridWidth"));
			gridHeight = Integer.parseInt(simProps.getProperty("gridHeight"));
			personGrid = new SparseGrid2D(gridWidth, gridHeight);

			// populate the environment
			if(simProps.containsKey("numPeople"))
				numPeople = Integer.parseInt(simProps.getProperty("numPeople"));
			else if(simProps.containsKey("percPeople"))
				numPeople = (int)(Double.parseDouble(simProps.getProperty("percPeople")) * gridWidth * gridHeight);
			else
				numPeople = gridWidth * gridHeight; // otherwise, assume complete coverage
			
			// set up the initial infection
			numInitialCases = Integer.parseInt(simProps.getProperty("numInitialCases"));
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(0);
		}
		
	}
	
	//
	//
	// START: setting up the simulation
	//
	//

	public void start() {
		super.start();
		
		//
		// set up empty holders
		//
		numCases = new ArrayList <Integer> ();
		numDeaths = new ArrayList <Integer> (); 		
		
		//
		// initialise individual PEOPLE
		//
		for(int i = 0; i < numPeople; i++) {
			
			// find a random spot
			Int2D proposedLocation = pickRandomLocation(gridWidth, gridHeight);
			//while(personGrid.getObjectsAtLocation(proposedLocation) != null) // we can restrict to unoccupied spots!
			//	proposedLocation = pickRandomLocation(gridWidth, gridHeight);
			
			// create the person
			Person person = new Person(proposedLocation);
			
			// add them to the space
			personGrid.setObjectLocation(person, proposedLocation);
			
			// add them to the schedule and save the stopper (so that they can stop updating if they die)
			person.setStoppable(schedule.scheduleRepeating(person));
		}
		
		//
		// set up DISEASE
		//
		Person indexCase = (Person) personGrid.allObjects.get(0);
		Infection i = new Infection(indexCase, Infection.InfectionStatus.EXPOSED);
		schedule.scheduleOnce(i);

		schedule.addAfter(new Steppable() {

			@Override
			public void step(SimState arg0) {
				
				// save latest information
				numCases.add(newCasesThisTick);
				numDeaths.add(newDeathsThisTick);
				totalCases += newCasesThisTick;
				totalDeaths += newDeathsThisTick;
				
				// reset for the next scheduled step
				newCasesThisTick = 0;
				newDeathsThisTick = 0;
			}
			
		});
	}
	
	//
	//
	// FINISH: cleaning up and exploring final reports
	//
	//

	public void finish() {
		super.finish();
		System.out.println("Total deaths: " + this.totalDeaths + " resulting from " + this.totalCases + " cases");
		System.out.println("CASES:" + this.numCases.toString());
		System.out.println("DEATHS:" + this.numDeaths.toString());
	}
	
	//
	//
	// MAIN: a stand-alone way to run the simulation in the terminal
	//
	//

	public static void main(String [] args) {
		PumpHandleSim sim = new PumpHandleSim(System.currentTimeMillis());
		sim.start();
		for(int i = 0; i < 10; i++)
			sim.schedule.step(sim);
		sim.finish();
	}
	
	//
	//
	// UTILITIES: useful functions
	//
	//
	
	private Int2D pickRandomLocation(int width, int height) {
		return new Int2D(random.nextInt(width), random.nextInt(height));
	}
	

}