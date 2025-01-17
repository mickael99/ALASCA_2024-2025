package fr.sorbonne_u.components;

import java.util.concurrent.TimeUnit;

import fr.sorbonne_u.components.cvm.AbstractCVM;
import fr.sorbonne_u.components.utils.ExecutionType;
import fr.sorbonne_u.components.utils.SimulationType;

public class CVMIntegrationTest extends AbstractCVM {

	public static long DELAY_TO_START = 3000L;
	public static long EXECUTION_DURATION = 5000L;
	public static long DELAY_TO_STOP = 2000L;
	public static long END_SLEEP_DURATION = 10000L;

	public static long DELAY_TO_START_SIMULATION = 5000L;
	public static double SIMULATION_START_TIME = 0.0;
	public static double SIMULATION_DURATION = 3.0;
	public static TimeUnit SIMULATION_TIME_UNIT = TimeUnit.HOURS;
	public static double ACCELERATION_FACTOR = 180.0;

	public static ExecutionType	CURRENT_EXECUTION_TYPE = ExecutionType.INTEGRATION_TEST;
	public static SimulationType CURRENT_SIMULATION_TYPE = SimulationType.SIL_SIMULATION;

	public static String			CLOCK_URI = "hem-clock";
	public static String			START_INSTANT = "2023-11-22T00:00:00.00Z";

	// public static ControlMode		CONTROL_MODE = ControlMode.PULL;
	
	public CVMIntegrationTest() throws Exception {
		
	}
}
