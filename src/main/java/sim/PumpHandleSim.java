package sim;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Properties;
import java.awt.Color;
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
	HashMap <String, Integer> numInitialCases;
	HashMap <String, Integer> numInitialImmune;
	
	//
	// ENVIRONMENT
	//

	SparseGrid2D personGrid;
	
	//
	// RECORDING
	//
	
	ArrayList <Integer> numCases, numDeaths;
	int newCasesThisTick = 0, newDeathsThisTick = 0, totalCases = 0, totalDeaths = 0;
	Infection indexCase = null;
	
	// OUTPUT

	public boolean verbose = false;
	
	//
	//
	// CONSTRUCTORS
	//
	//
	
	public PumpHandleSim(long seed) {
		this(seed, Thread.currentThread().getContextClassLoader().getResource("default.properties").getPath());//.currentThread().getContextClassLoader().getResource("").getPath() + "default.properties");
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
			
			// set up the initial infections
			numInitialCases = new HashMap <String, Integer> ();
			numInitialImmune = new HashMap <String, Integer> ();
			for(Object o: simProps.keySet()) {
				String key = (String) o;
				
				// read in the infections
				if(key.startsWith("infection_"))
					readInInfectionSpecificInfo(key, (String)simProps.getProperty(key), numInitialCases);

				
				else if(key.startsWith("immunity_"))
					readInInfectionSpecificInfo(key, (String)simProps.getProperty(key), numInitialImmune);
				
				//...and any immunities
			}
			
			
			
			//numInitialCases = Integer.parseInt(simProps.getProperty("numInitialCases"));

/*			// set up immunity
			if(simProps.containsKey("numInitialImmune"))
				numInitialImmune = Integer.parseInt(simProps.getProperty("numInitialImmune"));
			else if(simProps.containsKey("percInitialImmune"))
				numInitialImmune = (int)(Double.parseDouble(simProps.getProperty("percInitialImmune")) * numPeople); 
			else
				numInitialImmune = 0;
				*/
			
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(0);
		}
		
	}
	
	void readInInfectionSpecificInfo(String key, String value, HashMap <String, Integer> holder){
		String [] keyBits = key.split("_");
		String disease = keyBits[1]; // extract disease variant name
		String numOrPerc = keyBits[2]; // number or percentage?

		if(numOrPerc.equals("num"))
			holder.put(disease, Integer.parseInt(value));
		else if(numOrPerc.equals("perc"))
			holder.put(disease, (int)(Double.parseDouble(value) * numPeople));
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
		// set up IMMUNITY
		//
		for(Entry<String, Integer> entry: numInitialImmune.entrySet()) {
			
			// extract information about the disease
			String myDisease = entry.getKey();
			int numImmune = entry.getValue();
			
			// check: we can't instantiate more cases of disease than people
			if(numImmune > numPeople) {
				System.out.println("ERROR: cannot have more immune than people in the simulation");
				System.exit(0);
			}
			
			// set up a helper variable
			HashSet <Person> consideredImmunePersons = new HashSet <Person> ();
			
			// create this number of cases for the given disease
			for(int i = 0; i < numImmune; i++) {
				
				// find someone new to immunise
				Person potentialImmune = (Person) personGrid.allObjects.get(random.nextInt(numPeople));
				while(consideredImmunePersons.contains(potentialImmune)) // find someone we've not yet considered
					potentialImmune = (Person) personGrid.allObjects.get(random.nextInt(numPeople));
				
				// create the immunity
				potentialImmune.gainImmunityTo(myDisease);
				consideredImmunePersons.add(potentialImmune);
				potentialImmune.setColor(Color.gray);
			}
		}
		
		//
		// set up DISEASE
		//

		for(Entry<String, Integer> entry: numInitialCases.entrySet()) {
			
			// extract information about the disease
			String myDisease = entry.getKey();
			int numCases = entry.getValue();
			
			// check: we can't instantiate more cases of disease than people
			if(numCases > numPeople) {
				System.out.println("ERROR: cannot have more cases than people in the simulation");
				System.exit(0);
			}
			
			// set up a helper variable
			HashSet <Person> consideredHosts = new HashSet <Person> ();
			
			// create this number of cases for the given disease
			for(int i = 0; i < numCases; i++) {
				
				// find someone new to infect
				Person potentialCase = (Person) personGrid.allObjects.get(random.nextInt(numPeople));
				while(consideredHosts.contains(potentialCase)) // find someone we've not yet considered
					potentialCase = (Person) personGrid.allObjects.get(random.nextInt(numPeople));
				
				// store the new case in the local record
				consideredHosts.add(potentialCase);
				if(potentialCase.isImmuneTo(myDisease))
					continue; // don't actually create the infection, if they're immune to this specifically!

				// create the infection
				Infection infection = new Infection(myDisease, potentialCase, Infection.InfectionStatus.EXPOSED);
				schedule.scheduleOnce(infection);
				
			}
		}

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