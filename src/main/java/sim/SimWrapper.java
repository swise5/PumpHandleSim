package sim;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.ArrayList;

public class SimWrapper {
	
	public static boolean verbose = false;
	
	public static void outputResults(String filename, PumpHandleSim sim) {
		try {
			if(verbose)
				System.out.println("Printing out SIMULATION INFORMATION to " + filename);
		
			// Create new buffered writer to store this information in
			BufferedWriter exportFile = new BufferedWriter(new FileWriter(filename, false));
			
			// write a new heading 
			//exportFile.write("Seed\tNumberOfAgents\tSimuilationDuration" + "\n");
			
			exportFile.write(formatOutputArray(sim.numCases)+ "\n");
			exportFile.write(formatOutputArray(sim.numDeaths) + "\n");
			exportFile.close();
		
		} catch (Exception e) {
			System.err.println("File input error: " + filename);
		}
	}
	
	public static String formatOutputArray(ArrayList <Integer> output) {
		return output.toString().replaceAll("\\[|\\]", "");
	}
	
	public static void main(String [] args) {

		// set the seed
		Long seed;
		try {
			seed = Long.parseLong(args[0]);
		} catch (Exception e){
			seed = System.currentTimeMillis();
		}
		
		// read in params
		int maxTime = Integer.parseInt(args[1]);
		String propertiesFilename = args[2];
		String outputFilename = args[3];
		
		// run the simulation
		PumpHandleSim sim = new PumpHandleSim(seed, propertiesFilename);
		sim.start();
		while(!sim.schedule.scheduleComplete() && sim.schedule.getTime() < maxTime)
			sim.schedule.step(sim);

		// output the results
		outputResults(outputFilename, sim);
	}
	
}