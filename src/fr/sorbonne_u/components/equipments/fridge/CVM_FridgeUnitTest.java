package fr.sorbonne_u.components.equipments.fridge;

import java.time.Instant;
import java.util.concurrent.TimeUnit;

import fr.sorbonne_u.components.AbstractComponent;
import fr.sorbonne_u.components.CoordinatorComponent;
import fr.sorbonne_u.components.cvm.AbstractCVM;
import fr.sorbonne_u.components.cyphy.utils.aclocks.ClocksServerWithSimulation;
import fr.sorbonne_u.components.equipments.fridge.mil.FridgeCoupledModel;
import fr.sorbonne_u.components.equipments.fridge.mil.FridgeUnitTestModel;
import fr.sorbonne_u.components.equipments.fridge.sil.FridgeController;
import fr.sorbonne_u.components.equipments.fridge.sil.FridgeUnitTestsSupervisor;
import fr.sorbonne_u.components.equipments.fridge.sil.FridgeController.ControlMode;
import fr.sorbonne_u.components.utils.ExecutionType;
import fr.sorbonne_u.components.utils.SimulationType;
import fr.sorbonne_u.devs_simulation.exceptions.NeoSim4JavaException;
import fr.sorbonne_u.exceptions.VerboseException;
import fr.sorbonne_u.utils.aclocks.ClocksServer;

public class CVM_FridgeUnitTest extends AbstractCVM {

	// -------------------------------------------------------------------------
	// Constants and variables
	// -------------------------------------------------------------------------

	public static long DELAY_TO_START = 3000L;
	public static long EXECUTION_DURATION = 5000L;
	public static long DELAY_TO_STOP = 2000L;
	public static long END_SLEEP_DURATION = 1000000L;

	public static long DELAY_TO_START_SIMULATION = 3000L;
	public static double SIMULATION_START_TIME = 0.0;
	public static double SIMULATION_DURATION = 3.0;
	public static TimeUnit SIMULATION_TIME_UNIT = TimeUnit.HOURS;
	public static double ACCELERATION_FACTOR = 360.0;

	public static ExecutionType CURRENT_EXECUTION_TYPE = ExecutionType.UNIT_TEST;
	public static SimulationType CURRENT_SIMULATION_TYPE =
											//SimulationType.NO_SIMULATION;
											//SimulationType.MIL_SIMULATION;
											//SimulationType.MIL_RT_SIMULATION;
											SimulationType.SIL_SIMULATION;
	public static ControlMode CONTROL_MODE = ControlMode.PUSH;

	public static String CLOCK_URI = "hem-clock";
	public static String START_INSTANT = "2024-10-18T00:00:00.00Z";
	
	public CVM_FridgeUnitTest() throws Exception {
		ClocksServer.VERBOSE = true;
		ClocksServer.X_RELATIVE_POSITION = 0;
		ClocksServer.Y_RELATIVE_POSITION = 0;
		
		FridgeUnitTestsSupervisor.VERBOSE = true;
		FridgeUnitTestsSupervisor.X_RELATIVE_POSITION = 1;
		FridgeUnitTestsSupervisor.Y_RELATIVE_POSITION = 0;
		
		CoordinatorComponent.VERBOSE = true;
		CoordinatorComponent.X_RELATIVE_POSITION = 2;
		CoordinatorComponent.Y_RELATIVE_POSITION = 0;

		Fridge.VERBOSE = true;
		Fridge.X_RELATIVE_POSITION = 1;
		Fridge.Y_RELATIVE_POSITION = 1;
		
		FridgeController.VERBOSE = true;
		FridgeController.X_RELATIVE_POSITION = 2;
		FridgeController.Y_RELATIVE_POSITION = 1;
		
		FridgeUser.VERBOSE = true;
		FridgeUser.X_RELATIVE_POSITION = 0;
		FridgeUser.Y_RELATIVE_POSITION = 1;
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
		String fridgeLocalArchitectureURI = "";
		String fridgeUserLocalArchitectureURI = "";

		long current = System.currentTimeMillis();
		long unixEpochStartTimeInMillis = current + DELAY_TO_START;

		switch (CURRENT_SIMULATION_TYPE) {
		case MIL_SIMULATION:
			globalArchitectureURI = FridgeUnitTestsSupervisor.MIL_ARCHITECTURE_URI;
			fridgeLocalArchitectureURI = FridgeCoupledModel.MIL_URI;
			fridgeUserLocalArchitectureURI = FridgeUnitTestModel.MIL_URI;
			break;
		case MIL_RT_SIMULATION:
			globalArchitectureURI = FridgeUnitTestsSupervisor.MIL_RT_ARCHITECTURE_URI;
			fridgeLocalArchitectureURI = FridgeCoupledModel.MIL_RT_URI;
			fridgeUserLocalArchitectureURI = FridgeUnitTestModel.MIL_RT_URI;
			break;
		case SIL_SIMULATION:
			globalArchitectureURI = FridgeUnitTestsSupervisor.SIL_ARCHITECTURE_URI;
			fridgeLocalArchitectureURI =  FridgeCoupledModel.SIL_URI;
			fridgeUserLocalArchitectureURI = "not-used";
			break;
		case NO_SIMULATION:
		default:
		}

		// Deploy the components

		AbstractComponent.createComponent(
				Fridge.class.getCanonicalName(),
				new Object[]{Fridge.REFLECTION_INBOUND_PORT_URI,
							 Fridge.USER_INBOUND_PORT_URI,
							 Fridge.INTERNAL_CONTROL_INBOUND_PORT_URI,
							 Fridge.EXTERNAL_CONTROL_INBOUND_PORT_URI,
							 Fridge.SENSOR_INBOUND_PORT_URI,
							 Fridge.ACTUATOR_INBOUND_PORT_URI,
							 CURRENT_EXECUTION_TYPE,
							 CURRENT_SIMULATION_TYPE,
							 globalArchitectureURI,
							 fridgeLocalArchitectureURI,
							 SIMULATION_TIME_UNIT,
							 ACCELERATION_FACTOR,
							 CLOCK_URI});
		AbstractComponent.createComponent(
				FridgeController.class.getCanonicalName(),
				new Object[]{Fridge.SENSOR_INBOUND_PORT_URI,
							 Fridge.ACTUATOR_INBOUND_PORT_URI,
							 FridgeController.STANDARD_HYSTERESIS,
							 FridgeController.STANDARD_CONTROL_PERIOD,
							 CONTROL_MODE,
							 CURRENT_EXECUTION_TYPE,
							 CURRENT_SIMULATION_TYPE,
							 CLOCK_URI});
		AbstractComponent.createComponent(
				FridgeUser.class.getCanonicalName(),
				new Object[]{Fridge.USER_INBOUND_PORT_URI,
							 Fridge.INTERNAL_CONTROL_INBOUND_PORT_URI,
							 Fridge.EXTERNAL_CONTROL_INBOUND_PORT_URI,
							 CURRENT_EXECUTION_TYPE,
							 CURRENT_SIMULATION_TYPE,
							 globalArchitectureURI,
							 fridgeUserLocalArchitectureURI,
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
					FridgeUnitTestsSupervisor.class.getCanonicalName(),
					new Object[]{CURRENT_SIMULATION_TYPE,
								 globalArchitectureURI});
		}

		super.deploy();
	}
	
	public static void	main(String[] args)
	{
		VerboseException.VERBOSE = true;
		VerboseException.PRINT_STACK_TRACE = true;
		NeoSim4JavaException.VERBOSE = true;
		NeoSim4JavaException.PRINT_STACK_TRACE = true;

		try {
			CVM_FridgeUnitTest cvm = new CVM_FridgeUnitTest();
			
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
