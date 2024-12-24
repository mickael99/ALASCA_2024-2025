package fr.sorbonne_u.components.equipments.iron.sil;

import java.time.Instant;
//import java.time.Instant;
import java.util.concurrent.TimeUnit;

import fr.sorbonne_u.components.AbstractComponent;
import fr.sorbonne_u.components.CoordinatorComponent;
import fr.sorbonne_u.components.cvm.AbstractCVM;
import fr.sorbonne_u.components.cyphy.utils.aclocks.ClocksServerWithSimulation;
//import fr.sorbonne_u.components.cyphy.utils.aclocks.ClocksServerWithSimulation;
import fr.sorbonne_u.components.equipments.iron.Iron;
import fr.sorbonne_u.components.equipments.iron.mil.IronCoupledModel;
import fr.sorbonne_u.components.equipments.iron.mil.IronUserModel;
import fr.sorbonne_u.components.utils.ExecutionType;
import fr.sorbonne_u.components.utils.SimulationType;
import fr.sorbonne_u.devs_simulation.exceptions.NeoSim4JavaException;
import fr.sorbonne_u.exceptions.VerboseException;
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
	public static SimulationType CURRENT_SIMULATION_TYPE = SimulationType.SIL_SIMULATION;

	public static String CLOCK_URI = "iron-clock";
	public static String START_INSTANT = "2024-10-18T00:00:00.00Z";
	
	public CVM_IronUnitTest() throws Exception {
		ClocksServer.VERBOSE = true;
		ClocksServer.X_RELATIVE_POSITION = 0;
		ClocksServer.Y_RELATIVE_POSITION = 0;
		
		IronUnitTestsSupervisor.VERBOSE = true;
		IronUnitTestsSupervisor.X_RELATIVE_POSITION = 1;
		IronUnitTestsSupervisor.Y_RELATIVE_POSITION = 0;
		
		CoordinatorComponent.VERBOSE = true;
		CoordinatorComponent.X_RELATIVE_POSITION = 2;
		CoordinatorComponent.Y_RELATIVE_POSITION = 0;

		Iron.VERBOSE = true;
		Iron.X_RELATIVE_POSITION = 1;
		Iron.Y_RELATIVE_POSITION = 1;
		
		IronUser.VERBOSE = true;
		IronUser.X_RELATIVE_POSITION = 0;
		IronUser.Y_RELATIVE_POSITION = 1;
	}
	
	@Override
	public void deploy() throws Exception {
		assert	!CURRENT_EXECUTION_TYPE.isIntegrationTest() :
				new RuntimeException(
						"!CURRENT_EXECUTION_TYPE.isIntegrationTest()");
		assert	!CURRENT_EXECUTION_TYPE.isStandard() ||
									CURRENT_SIMULATION_TYPE.isNoSimulation() :
				new RuntimeException(
						"!CURRENT_EXECUTION_TYPE.isStandard() || "
						+ "CURRENT_SIMULATION_TYPE.isNoSimulation()");

		String architectureURI = "";
		String ironLocalArchitectureURI = "";
		String ironUserLocalArchitectureURI = "";

		 long current = System.currentTimeMillis();
		 long unixEpochStartTimeInMillis = current + DELAY_TO_START;

		switch (CURRENT_SIMULATION_TYPE) {
			case MIL_SIMULATION:
				architectureURI = IronUnitTestsSupervisor.MIL_ARCHITECTURE_URI;
				ironLocalArchitectureURI = IronCoupledModel.MIL_URI;
				ironUserLocalArchitectureURI = IronUserModel.MIL_URI;
				break;
				
			case MIL_RT_SIMULATION:
				architectureURI = IronUnitTestsSupervisor.MIL_RT_ARCHITECTURE_URI;
				ironLocalArchitectureURI = IronCoupledModel.MIL_RT_URI;
				ironUserLocalArchitectureURI = IronUserModel.MIL_RT_URI;
				break;
				
			case SIL_SIMULATION:
				architectureURI = IronUnitTestsSupervisor.SIL_ARCHITECTURE_URI;
				ironLocalArchitectureURI = IronCoupledModel.SIL_URI;
				ironUserLocalArchitectureURI = "not-used";
				break;
				
			case NO_SIMULATION:
				
			default:
		}

		AbstractComponent.createComponent(
				Iron.class.getCanonicalName(),
				new Object[]{Iron.REFLECTION_INBOUND_PORT_URI,
							 Iron.INBOUND_PORT_URI,
							 CURRENT_EXECUTION_TYPE,
							 CURRENT_SIMULATION_TYPE,
							 architectureURI,
							 ironLocalArchitectureURI,
							 SIMULATION_TIME_UNIT,
							 ACCELERATION_FACTOR,
							 CLOCK_URI});
		
		AbstractComponent.createComponent(
				IronUser.class.getCanonicalName(),
				new Object[]{IronUser.REFLECTION_INBOUND_PORT_URI,
							 Iron.INBOUND_PORT_URI,
							 CURRENT_EXECUTION_TYPE,
							 CURRENT_SIMULATION_TYPE,
							 architectureURI,
							 ironUserLocalArchitectureURI,
							 SIMULATION_TIME_UNIT,
							 ACCELERATION_FACTOR,
							 CLOCK_URI});

		AbstractComponent.createComponent(
			    ClocksServerWithSimulation.class.getCanonicalName(),
			    new Object[]{
			        CLOCK_URI,
			        TimeUnit.MILLISECONDS.toNanos(unixEpochStartTimeInMillis),
			        Instant.parse(START_INSTANT),
			        ACCELERATION_FACTOR,
			        DELAY_TO_START_SIMULATION,
			        SIMULATION_START_TIME,
			        SIMULATION_DURATION,
			        SIMULATION_TIME_UNIT
			    });

		if (CURRENT_SIMULATION_TYPE.isMilSimulation() ||
								CURRENT_SIMULATION_TYPE.isMILRTSimulation()) {
			AbstractComponent.createComponent(
					CoordinatorComponent.class.getCanonicalName(),
					new Object[]{});
			AbstractComponent.createComponent(
					IronUnitTestsSupervisor.class.getCanonicalName(),
					new Object[]{CURRENT_SIMULATION_TYPE,
								 architectureURI});
		}

		super.deploy();
	}
	
	public static void main(String[] args) {
		VerboseException.VERBOSE = true;
		VerboseException.PRINT_STACK_TRACE = true;
		NeoSim4JavaException.VERBOSE = true;
		NeoSim4JavaException.PRINT_STACK_TRACE = true;

		try {
			CVM_IronUnitTest cvm = new CVM_IronUnitTest();

			long executionDurationInMillis = 0L;
			switch (CURRENT_SIMULATION_TYPE) {
				case MIL_SIMULATION:
					executionDurationInMillis = DELAY_TO_START + DELAY_TO_START_SIMULATION 
													+ EXECUTION_DURATION + DELAY_TO_STOP;
					break;
				case MIL_RT_SIMULATION:
					
				case SIL_SIMULATION:
					executionDurationInMillis =
						DELAY_TO_START + DELAY_TO_START_SIMULATION
							+ ((long)(((double)SIMULATION_TIME_UNIT.toMillis(1))
									* (SIMULATION_DURATION/ACCELERATION_FACTOR)))
							+ DELAY_TO_STOP;
					break;
					
				case NO_SIMULATION:
					executionDurationInMillis =
						DELAY_TO_START + EXECUTION_DURATION + DELAY_TO_STOP;
					break;
					
				default:
			}
			
			System.out.println("starting for " + executionDurationInMillis);
			cvm.startStandardLifeCycle(executionDurationInMillis);
			Thread.sleep(END_SLEEP_DURATION);

			System.exit(0);
		} catch (Exception e) {
			throw new RuntimeException(e) ;
		}
	}
}
