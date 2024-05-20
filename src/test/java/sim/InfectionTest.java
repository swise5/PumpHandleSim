package sim;


import static org.junit.Assert.*;

import java.util.Map.Entry;

import org.junit.Test;


public class InfectionTest {
	
	/**
	 * Ensure basic model functionality
	 */
	@Test
	public final void nontransmissibleDiseaseDoesntSpread()
    {		
		
		// set up and run the simulation
		
		PumpHandleSim sut = new PumpHandleSim(1234);
		sut.start();
		
		// make the disease non-transmissible
		sut.indexCase.changeSpreadProbability(0);

		// run the model
		while(!sut.schedule.scheduleComplete() && sut.schedule.getTime() < 10000)
			sut.schedule.step(sut);

		// check to make sure it never spreads
		assertEquals(sut.totalCases, 0);
    }
	
	@Test
	public final void highlyTransmissibleDiseaseSpreads()
    {		
		
		// set up and run the simulation
		
		PumpHandleSim sut = new PumpHandleSim(1234);
		sut.start();
		
		// make the disease perfectly transmissible
		sut.indexCase.changeSpreadProbability(1.);

		// run the model
		while(!sut.schedule.scheduleComplete() && sut.schedule.getTime() < 10000)
			sut.schedule.step(sut);

		// check to make sure it DOES spread
		int numInitialCases = 0;
		for(Entry<String, Integer> e: sut.numInitialCases.entrySet())
			numInitialCases += e.getValue();
		
		assertEquals(sut.totalCases, sut.numPeople - numInitialCases); // we don't count the initial cases
    }
}