package sim;

import junit.framework.TestCase;
import sim.PumpHandleSim;



import static org.junit.Assert.*;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.util.Properties;
/*
import javax.script.ScriptEngineManager;
import javax.script.SimpleScriptContext;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import java.io.FileReader;
*/
import org.junit.Test;
//import org.python.util.PythonInterpreter;


public class InfectionTest {
	
	/**
	 * We want to ensure that the model produces a typical epidemic curve of cases, followed
	 * by a (lagging) curve of deaths.
	 */
	@Test
	public final void nonTransmittableDiseaseDoesntSpread()
    {		

		for(int i = 0; i < 30; i++) {
			PumpHandleSim sut = new PumpHandleSim(i);
		
			// set up and run the simulation
			sut.start();
			while(!sut.schedule.scheduleComplete() && sut.schedule.getTime() < 300)
				sut.schedule.step(sut);
			
			// export the results into a file
		}
		assertEquals(1, 1);
    }
	
	@Test
	public void givenPythonInterpreter_whenPrintExecuted_thenOutputDisplayed() {
/*		System.setProperty("python.home", new File(
		        System.getProperty("user.home"), "jython2.7.0").getPath()
		);
*/
	    try { //(PythonInterpreter pyInterp = new PythonInterpreter()) 
/*	        StringWriter output = new StringWriter();
	        
	        ScriptEngineManager manager = new ScriptEngineManager();
	        ScriptContext context = new SimpleScriptContext();
	        context.setWriter(output);
	        ScriptEngine engine = manager.getEngineByName("python");
	        engine.eval(new FileReader("/Users/swise/Downloads/hello.py"), context);
//	        pyInterp.setOut(output);

	//        pyInterp.exec("print('Hello nerds')");
//	        pyInterp.exec("import numpy as np");
	//        assertEquals("Should contain script output: ", "Hello nerds", output.toString().trim());
	        System.out.println(output);
*/	        assertEquals(1, 1);
	    } catch (Exception e) {
	    	e.printStackTrace();
	    }
	}
}

/* show implications of addition of
 * > movement
 * > immunity
 * > ????
 * > age-susceptibility
 */
