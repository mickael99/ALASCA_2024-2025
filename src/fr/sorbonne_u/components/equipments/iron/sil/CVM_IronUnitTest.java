package fr.sorbonne_u.components.equipments.iron.sil;

import java.util.concurrent.TimeUnit;

import fr.sorbonne_u.components.cvm.AbstractCVM;
import fr.sorbonne_u.components.cyphy.examples.molene.components.CoordinatorComponent;
import fr.sorbonne_u.components.equipments.iron.Iron;
import fr.sorbonne_u.components.utils.ExecutionType;
import fr.sorbonne_u.components.utils.SimulationType;
import fr.sorbonne_u.utils.aclocks.ClocksServer;

public class CVM_IronUnitTest extends AbstractCVM {

	public static long DELAY_TO_START = 3000L;
	public static long EXECUTION_DURATION = 5000L;
	public static long DELAY_TO_STOP = 2000L;
	public static long END_SLEEP_DURATION = 10000L;

	public static long DELAY_TO_START_SIMULATION = 3000L;
	public static double SIMULATION_START_TIME = 0.0;
	public static double SIMULATION_DURATION = 3.0;
	public static TimeUnit SIMULATION_TIME_UNIT = TimeUnit.HOURS;
	public static double ACCELERATION_FACTOR = 360.0;

	public static ExecutionType	CURRENT_EXECUTION_TYPE = ExecutionType.UNIT_TEST;
	public static SimulationType CURRENT_SIMULATION_TYPE = SimulationType.MIL_SIMULATION;

	public static String CLOCK_URI = "iron-clock";
	public static String START_INSTANT = "2024-10-18T00:00:00.00Z";
	
	public CVM_IronUnitTest() throws Exception {
		ClocksServer.VERBOSE = true;
		ClocksServer.X_RELATIVE_POSITION = 0;
		ClocksServer.Y_RELATIVE_POSITION = 0;
		
		IronUnitTestsSupervisor.VERBOSE = true;
		IronUnitTestsSupervisor.X_RELATIVE_POSITION = 1;
		IronUnitTestsSupervisor.Y_RELATIVE_POSITION = 0;
		
//		CoordinatorComponent.VERBOSE = true;
//		CoordinatorComponent.X_RELATIVE_POSITION = 2;
//		CoordinatorComponent.Y_RELATIVE_POSITION = 0;

		Iron.VERBOSE = true;
		Iron.X_RELATIVE_POSITION = 1;
		Iron.Y_RELATIVE_POSITION = 1;
		
		IronUser.VERBOSE = true;
		IronUser.X_RELATIVE_POSITION = 0;
		IronUser.Y_RELATIVE_POSITION = 1;
	}
}
