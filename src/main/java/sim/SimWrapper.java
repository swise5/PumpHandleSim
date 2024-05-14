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
			BufferedWriter exportFile = new BufferedWriter(new FileWriter(filename, true));
			
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

		// read in params
		int maxTime = Integer.parseInt(args[0]);
		String outputFilename = args[1];
		
		// set the seed
		Long seed;
		try {
			seed = Long.parseLong(args[2]);			
		} catch (Exception e){
			seed = System.currentTimeMillis();
		}
		
		int width, height, people;
		try {
			width = Integer.parseInt(args[3]);
			height = Integer.parseInt(args[4]);
			people = Integer.parseInt(args[5]);
		} catch (Exception e) {
			width = 10; height = 10; people = 20;
		}
				
		// run the simulation
		PumpHandleSim sim = new PumpHandleSim(seed, width, height, people, 1);
		sim.start();
		while(!sim.schedule.scheduleComplete() && sim.schedule.getTime() < maxTime)
			sim.schedule.step(sim);

		// output the results
		outputResults(outputFilename, sim);
	}
	
}