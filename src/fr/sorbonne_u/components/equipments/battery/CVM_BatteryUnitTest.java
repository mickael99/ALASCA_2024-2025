package fr.sorbonne_u.components.equipments.battery;

import java.time.Instant;
import java.util.concurrent.TimeUnit;

import fr.sorbonne_u.components.AbstractComponent;
import fr.sorbonne_u.components.CoordinatorComponent;
import fr.sorbonne_u.components.cvm.AbstractCVM;
import fr.sorbonne_u.components.cyphy.utils.aclocks.ClocksServerWithSimulation;
import fr.sorbonne_u.components.equipments.battery.mil.BatteryCoupledModel;
import fr.sorbonne_u.components.equipments.battery.mil.BatteryUserModel;
import fr.sorbonne_u.components.equipments.battery.sil.BatteryUnitTestsSupervisor;
import fr.sorbonne_u.components.utils.ExecutionType;
import fr.sorbonne_u.components.utils.SimulationType;
import fr.sorbonne_u.devs_simulation.exceptions.NeoSim4JavaException;
import fr.sorbonne_u.exceptions.VerboseException;
import fr.sorbonne_u.utils.aclocks.ClocksServer;

public class CVM_BatteryUnitTest extends AbstractCVM {
	public static long DELAY_TO_START = 3000L;
	public static long EXECUTION_DURATION = 5000L;
	public static long DELAY_TO_STOP = 2000L;
	public static long END_SLEEP_DURATION = 1000000L;

	public static long DELAY_TO_START_SIMULATION = 3000L;
	public static double SIMULATION_START_TIME = 0.0;
	public static double SIMULATION_DURATION = 10.0;
	public static TimeUnit SIMULATION_TIME_UNIT = TimeUnit.HOURS;
	public static double ACCELERATION_FACTOR = 360.0;

	public static ExecutionType	CURRENT_EXECUTION_TYPE = ExecutionType.UNIT_TEST;
	public static SimulationType CURRENT_SIMULATION_TYPE =  //SimulationType.NO_SIMULATION;
//															SimulationType.MIL_SIMULATION;
//															SimulationType.MIL_RT_SIMULATION;
															SimulationType.SIL_SIMULATION;

	public static String CLOCK_URI = "hem-clock";
	public static String START_INSTANT = "2024-10-18T00:00:00.00Z";
	
	public CVM_BatteryUnitTest() throws Exception {
		ClocksServer.VERBOSE = true;
		ClocksServer.X_RELATIVE_POSITION = 0;
		ClocksServer.Y_RELATIVE_POSITION = 0;
		
		BatteryUnitTestsSupervisor.VERBOSE = true;
		BatteryUnitTestsSupervisor.X_RELATIVE_POSITION = 1;
		BatteryUnitTestsSupervisor.Y_RELATIVE_POSITION = 0;
		
		CoordinatorComponent.VERBOSE = true;
		CoordinatorComponent.X_RELATIVE_POSITION = 2;
		CoordinatorComponent.Y_RELATIVE_POSITION = 0;

		Battery.VERBOSE = true;
		Battery.X_RELATIVE_POSITION = 1;
		Battery.Y_RELATIVE_POSITION = 1;
		
		BatteryTester.VERBOSE = true;
		BatteryTester.X_RELATIVE_POSITION = 0;
		BatteryTester.Y_RELATIVE_POSITION = 1;
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

		String globalArchitectureURI = "";
		String batteryLocalArchitectureURI = "";
		String batteryUserLocalArchitectureURI = "";

		long current = System.currentTimeMillis();
		long unixEpochStartTimeInMillis = current + DELAY_TO_START;

		switch (CURRENT_SIMULATION_TYPE) {
			case MIL_SIMULATION:
				globalArchitectureURI = BatteryUnitTestsSupervisor.MIL_ARCHITECTURE_URI;
				batteryLocalArchitectureURI = BatteryCoupledModel.MIL_URI;
				batteryUserLocalArchitectureURI = BatteryUserModel.MIL_URI;
				break;
				
			case MIL_RT_SIMULATION:
				globalArchitectureURI = BatteryUnitTestsSupervisor.MIL_RT_ARCHITECTURE_URI;
				batteryLocalArchitectureURI = BatteryCoupledModel.MIL_RT_URI;
				batteryUserLocalArchitectureURI = BatteryUserModel.MIL_RT_URI;
				break;
				
			case SIL_SIMULATION:
				globalArchitectureURI = BatteryUnitTestsSupervisor.SIL_ARCHITECTURE_URI;
				batteryLocalArchitectureURI =  BatteryCoupledModel.SIL_URI;
				batteryUserLocalArchitectureURI = "not-used";
				break;
			case NO_SIMULATION:
			
			default:
		}

		// Deploy the components

		AbstractComponent.createComponent(
				Battery.class.getCanonicalName(),
				new Object[]{Battery.REFLECTION_INBOUND_PORT_URI,
							 Battery.INBOUND_PORT_URI,
							 CURRENT_EXECUTION_TYPE,
							 CURRENT_SIMULATION_TYPE,
							 globalArchitectureURI,
							 batteryLocalArchitectureURI,
							 SIMULATION_TIME_UNIT,
							 ACCELERATION_FACTOR,
							 CLOCK_URI});
		AbstractComponent.createComponent(
				BatteryTester.class.getCanonicalName(),
				new Object[]{Battery.INBOUND_PORT_URI,
							 CURRENT_EXECUTION_TYPE,
							 CURRENT_SIMULATION_TYPE,
							 globalArchitectureURI,
							 batteryUserLocalArchitectureURI,
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
						SIMULATION_TIME_UNIT});

		if (CURRENT_SIMULATION_TYPE.isMilSimulation() ||
								CURRENT_SIMULATION_TYPE.isMILRTSimulation()) {
			AbstractComponent.createComponent(
					CoordinatorComponent.class.getCanonicalName(),
					new Object[]{});
			AbstractComponent.createComponent(
					BatteryUnitTestsSupervisor.class.getCanonicalName(),
					new Object[]{CURRENT_SIMULATION_TYPE,
								 globalArchitectureURI});
		}

		super.deploy();
	}
	
	public static void	main(String[] args) {
		VerboseException.VERBOSE = true;
		VerboseException.PRINT_STACK_TRACE = true;
		NeoSim4JavaException.VERBOSE = true;
		NeoSim4JavaException.PRINT_STACK_TRACE = true;

		try {
			CVM_BatteryUnitTest cvm = new CVM_BatteryUnitTest();
			
			long executionDurationInMillis = 0L;
			switch (CURRENT_SIMULATION_TYPE) {
				case MIL_SIMULATION:
					executionDurationInMillis =
						DELAY_TO_START + DELAY_TO_START_SIMULATION 
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
