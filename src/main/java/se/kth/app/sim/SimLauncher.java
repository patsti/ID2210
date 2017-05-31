/*
 * 2016 Royal Institute of Technology (KTH)
 *
 * LSelector is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package se.kth.app.sim;

import java.io.IOException;
import java.util.Scanner;

import se.sics.kompics.simulator.SimulationScenario;
import se.sics.kompics.simulator.run.LauncherComp;

/**
 * @author Alex Ormenisan <aaor@kth.se>
 */
public class SimLauncher {
    public static void main(String[] args) {
    	Scanner reader = new Scanner(System.in);  // Reading from System.in
    	while(true){
	    	System.out.println("Press 1 for G-Set:\n"
	    			+ "Press 2 for 2P-Set: \n"
	    			+ "Press 3 for OR-Set: \n"
	    			+ "Press 4 for Graph Simulation: \n"
	    			+ "Press 5 to exit:");
	    	int sets = reader.nextInt(); // Scans the next token of the input as an int.
	    	if(sets == 4){
	    		test4();
	    	}else if(sets == 5){
	    		return;
	    	}else{
		    	System.out.println("Press 1 for simple simulation:\n"
		    			+ "Press 2 for simulation with churn: \n"
		    			+ "Press 3 for simulation with more churn:");
		    	int input = reader.nextInt(); // Scans the next token of the input as an int.
		    	if(input == 1){
		    		test1(sets-1);
		    	} else if(input == 2){
		    		test2(sets-1);
		    	} else if(input == 3){
		    		test3(sets-1);
		    	} else{
		    		System.out.println("Wrong input: " + String.valueOf(input));
		    	}
	    	}
    	}
    }
    
    public static void test1(int set){
    	SimulationScenario.setSeed(ScenarioSetup.scenarioSeed);
        SimulationScenario simpleBootScenario = ScenarioGen.simpleBoot(set);
        simpleBootScenario.simulate(LauncherComp.class);
    }
    
    public static void test2(int set){
    	SimulationScenario.setSeed(ScenarioSetup.scenarioSeed);
    	SimulationScenario churnScenario = ScenarioGen.simulateChurn1(set);
    	churnScenario.simulate(LauncherComp.class);
    }
    
    public static void test3(int set){
    	SimulationScenario.setSeed(ScenarioSetup.scenarioSeed);
    	SimulationScenario churnScenario = ScenarioGen.simulateChurn2(set);
    	churnScenario.simulate(LauncherComp.class);
    }
    
    public static void test4(){
    	SimulationScenario.setSeed(ScenarioSetup.scenarioSeed);
        SimulationScenario churnScenario = ScenarioGen.simulateGraph2P2P(1);
        churnScenario.simulate(LauncherComp.class);
    }
}
