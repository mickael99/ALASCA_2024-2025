package fr.sorbonne_u.components;

import java.time.Instant;
import java.util.concurrent.TimeUnit;

import fr.sorbonne_u.components.cvm.AbstractCVM;
import fr.sorbonne_u.components.cyphy.utils.aclocks.ClocksServerWithSimulation;
import fr.sorbonne_u.components.equipments.battery.Battery;
import fr.sorbonne_u.components.equipments.battery.BatteryTester;
import fr.sorbonne_u.components.equipments.battery.mil.BatteryCoupledModel;
import fr.sorbonne_u.components.equipments.battery.mil.BatteryUserModel;
import fr.sorbonne_u.components.equipments.fridge.Fridge;
import fr.sorbonne_u.components.equipments.fridge.FridgeUser;
import fr.sorbonne_u.components.equipments.fridge.mil.FridgeCoupledModel;
import fr.sorbonne_u.components.equipments.fridge.mil.FridgeUnitTestModel;
import fr.sorbonne_u.components.equipments.fridge.sil.FridgeController;
import fr.sorbonne_u.components.equipments.fridge.sil.FridgeController.ControlMode;
import fr.sorbonne_u.components.equipments.hem.HEM;
import fr.sorbonne_u.components.equipments.iron.Iron;
import fr.sorbonne_u.components.equipments.iron.IronUser;
import fr.sorbonne_u.components.equipments.iron.mil.IronStateModel;
import fr.sorbonne_u.components.equipments.iron.mil.IronUserModel;
import fr.sorbonne_u.components.equipments.meter.ElectricMeter;
import fr.sorbonne_u.components.equipments.meter.ElectricMeterUnitTester;
import fr.sorbonne_u.components.equipments.meter.mil.ElectricMeterCoupledModel;
import fr.sorbonne_u.components.equipments.windTurbine.WindTurbine;
import fr.sorbonne_u.components.equipments.windTurbine.WindTurbineTester;
import fr.sorbonne_u.components.equipments.windTurbine.mil.WindTurbineCoupledModel;
import fr.sorbonne_u.components.equipments.windTurbine.mil.WindTurbineUserModel;
import fr.sorbonne_u.components.utils.ExecutionType;
import fr.sorbonne_u.components.utils.SimulationType;
import fr.sorbonne_u.devs_simulation.exceptions.NeoSim4JavaException;
import fr.sorbonne_u.exceptions.PreconditionException;
import fr.sorbonne_u.exceptions.VerboseException;
import fr.sorbonne_u.utils.aclocks.ClocksServer;


public class CVMIntegrationTest extends AbstractCVM {

	// -------------------------------------------------------------------------
	// Constants and variables
	// -------------------------------------------------------------------------
	
	public static long DELAY_TO_START = 3000L;
	public static long EXECUTION_DURATION = 5000L;
	public static long DELAY_TO_STOP = 2000L;
	public static long END_SLEEP_DURATION = 1000000L;

	public static long DELAY_TO_START_SIMULATION = 3000L;
	public static double SIMULATION_START_TIME = 0.0;
	public static double SIMULATION_DURATION = 10.0;
	public static TimeUnit SIMULATION_TIME_UNIT = TimeUnit.HOURS;
	public static double ACCELERATION_FACTOR = 360.0;

	public static ExecutionType	CURRENT_EXECUTION_TYPE = ExecutionType.INTEGRATION_TEST;
	public static SimulationType CURRENT_SIMULATION_TYPE = SimulationType.SIL_SIMULATION;

	public static String CLOCK_URI = "hem-clock";
	public static String START_INSTANT = "2023-11-22T00:00:00.00Z";
	public static ControlMode CONTROL_MODE = ControlMode.PUSH;
	
	
	// -------------------------------------------------------------------------
	// Constructors
	// -------------------------------------------------------------------------
	
	public CVMIntegrationTest() throws Exception {
		ClocksServer.VERBOSE = true;
		ClocksServer.X_RELATIVE_POSITION = 0;
		ClocksServer.Y_RELATIVE_POSITION = 0;
		GlobalSupervisor.VERBOSE = true;
		GlobalSupervisor.X_RELATIVE_POSITION = 1;
		GlobalSupervisor.Y_RELATIVE_POSITION = 0;
		CoordinatorComponent.VERBOSE = true;
		CoordinatorComponent.X_RELATIVE_POSITION = 2;
		CoordinatorComponent.Y_RELATIVE_POSITION = 0;

		HEM.VERBOSE = true;
		HEM.X_RELATIVE_POSITION = 0;
		HEM.Y_RELATIVE_POSITION = 1;
		
		ElectricMeter.VERBOSE = true;
		ElectricMeter.X_RELATIVE_POSITION = 3;
		ElectricMeter.Y_RELATIVE_POSITION = 2;
		ElectricMeterUnitTester.VERBOSE = true;
		ElectricMeterUnitTester.X_RELATIVE_POSITION = 3;
		ElectricMeterUnitTester.Y_RELATIVE_POSITION = 3;
		
		Iron.VERBOSE = true;
		Iron.X_RELATIVE_POSITION = 1;
		Iron.Y_RELATIVE_POSITION = 2;
		IronUser.VERBOSE = true;
		IronUser.X_RELATIVE_POSITION = 0;
		IronUser.Y_RELATIVE_POSITION = 2;
		
		Fridge.VERBOSE = true;
		Fridge.X_RELATIVE_POSITION = 1;
		Fridge.Y_RELATIVE_POSITION = 3;
		FridgeUser.VERBOSE = true;
		FridgeUser.X_RELATIVE_POSITION = 0;
		FridgeUser.Y_RELATIVE_POSITION = 3;
		FridgeController.VERBOSE = true;
		FridgeController.X_RELATIVE_POSITION = 2;
		FridgeController.Y_RELATIVE_POSITION = 3;
		
		WindTurbine.VERBOSE = true;
		WindTurbine.X_RELATIVE_POSITION = 1;
		WindTurbine.Y_RELATIVE_POSITION = 1;
		WindTurbineTester.VERBOSE = true;
		WindTurbineTester.X_RELATIVE_POSITION = 2;
		WindTurbineTester.Y_RELATIVE_POSITION = 1;
		
		Battery.VERBOSE = true;
		Battery.X_RELATIVE_POSITION = 3;
		Battery.Y_RELATIVE_POSITION = 0;
		BatteryTester.VERBOSE = true;
		BatteryTester.X_RELATIVE_POSITION = 3;
		BatteryTester.Y_RELATIVE_POSITION = 1;
	}
	
	
	// -------------------------------------------------------------------------
	// Component life-cycle
	// -------------------------------------------------------------------------
	
	@Override
	public void			deploy() throws Exception
	{
		assert	!CURRENT_EXECUTION_TYPE.isUnitTest() :
				new PreconditionException(
						"!CURRENT_EXECUTION_TYPE.isUnitTest()");

		String globalArchitectureURI = "";
		String ironLocalArchitectureURI = "";
		String ironUserLocalArchitectureURI = "";
		String fridgeLocalArchitectureURI = "";
		String fridgeUserLocalArchitectureURI = "";
		String windTurbineArchitectureURI = "";
		String windTurbineUserLocalArchitectureURI = "";
		String batteryArchitectureURI = "";
		String batteryUserLocalArchitectureURI = "";
		String meterLocalArchitectureURI = "";

		long current = System.currentTimeMillis();
		long unixEpochStartTimeInMillis = current + DELAY_TO_START;
		Instant	startInstant = Instant.parse(START_INSTANT);

		switch (CURRENT_SIMULATION_TYPE) {
		case MIL_SIMULATION:
			globalArchitectureURI = GlobalSupervisor.MIL_SIM_ARCHITECTURE_URI;
			ironLocalArchitectureURI = IronStateModel.MIL_URI;
			ironUserLocalArchitectureURI = IronUserModel.MIL_URI;
			fridgeLocalArchitectureURI = FridgeCoupledModel.MIL_URI;
			fridgeUserLocalArchitectureURI = FridgeUnitTestModel.MIL_URI;
			windTurbineArchitectureURI = WindTurbineCoupledModel.MIL_URI;
			windTurbineUserLocalArchitectureURI = WindTurbineUserModel.MIL_URI;
			batteryArchitectureURI = BatteryCoupledModel.MIL_URI;
			batteryUserLocalArchitectureURI = BatteryUserModel.MIL_URI;
			meterLocalArchitectureURI = ElectricMeterCoupledModel.MIL_URI;
			break;
			
		case MIL_RT_SIMULATION:
			globalArchitectureURI = GlobalSupervisor.MIL_SIM_ARCHITECTURE_URI;
			ironLocalArchitectureURI = IronStateModel.MIL_RT_URI;
			ironUserLocalArchitectureURI = IronUserModel.MIL_RT_URI;
			fridgeLocalArchitectureURI = FridgeCoupledModel.MIL_RT_URI;
			fridgeUserLocalArchitectureURI = FridgeUnitTestModel.MIL_RT_URI;
			windTurbineArchitectureURI = WindTurbineCoupledModel.MIL_RT_URI;
			windTurbineUserLocalArchitectureURI = WindTurbineUserModel.MIL_RT_URI;
			batteryArchitectureURI = BatteryCoupledModel.MIL_RT_URI;
			batteryUserLocalArchitectureURI = BatteryUserModel.MIL_RT_URI;
			meterLocalArchitectureURI = ElectricMeterCoupledModel.MIL_RT_URI;
			break;
			
		case SIL_SIMULATION:
			globalArchitectureURI = GlobalSupervisor.SIL_SIM_ARCHITECTURE_URI;
			ironLocalArchitectureURI = IronStateModel.SIL_URI;
			ironUserLocalArchitectureURI = "not-used";
			fridgeLocalArchitectureURI = FridgeCoupledModel.SIL_URI;
			fridgeUserLocalArchitectureURI = "not-used";
			windTurbineArchitectureURI = WindTurbineCoupledModel.SIL_URI;
			windTurbineUserLocalArchitectureURI = "not-used";
			batteryArchitectureURI = BatteryCoupledModel.SIL_URI;
			batteryUserLocalArchitectureURI = "not-used";
			meterLocalArchitectureURI = ElectricMeterCoupledModel.SIL_URI;
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
							 globalArchitectureURI,
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
							 globalArchitectureURI,
							 ironUserLocalArchitectureURI,
							 SIMULATION_TIME_UNIT,
							 ACCELERATION_FACTOR,
							 CLOCK_URI});

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
				WindTurbine.class.getCanonicalName(),
				new Object[]{WindTurbine.REFLECTION_INBOUND_PORT_URI,
							 WindTurbine.INBOUND_PORT_URI,
							 CURRENT_EXECUTION_TYPE,
							 CURRENT_SIMULATION_TYPE,
							 globalArchitectureURI,
							 windTurbineArchitectureURI,
							 SIMULATION_TIME_UNIT,
							 ACCELERATION_FACTOR,
							 CLOCK_URI});
		AbstractComponent.createComponent(
				WindTurbineTester.class.getCanonicalName(),
				new Object[]{WindTurbine.INBOUND_PORT_URI,
							 CURRENT_EXECUTION_TYPE,
							 CURRENT_SIMULATION_TYPE,
							 globalArchitectureURI,
							 windTurbineUserLocalArchitectureURI,
							 SIMULATION_TIME_UNIT,
							 ACCELERATION_FACTOR,
							 CLOCK_URI});
		
		AbstractComponent.createComponent(
				Battery.class.getCanonicalName(),
				new Object[]{Battery.REFLECTION_INBOUND_PORT_URI,
							 Battery.INBOUND_PORT_URI,
							 CURRENT_EXECUTION_TYPE,
							 CURRENT_SIMULATION_TYPE,
							 globalArchitectureURI,
							 batteryArchitectureURI,
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
				ElectricMeter.class.getCanonicalName(),
				new Object[]{ElectricMeter.REFLECTION_INBOUND_PORT_URI,
							 ElectricMeter.ELECTRIC_METER_INBOUND_PORT_URI,
							 CURRENT_EXECUTION_TYPE,
							 CURRENT_SIMULATION_TYPE,
							 globalArchitectureURI,
							 meterLocalArchitectureURI,
							 SIMULATION_TIME_UNIT,
							 ACCELERATION_FACTOR,
						 	 CLOCK_URI});

		AbstractComponent.createComponent(
				HEM.class.getCanonicalName(),
				new Object[]{CURRENT_SIMULATION_TYPE});

		AbstractComponent.createComponent(
				ClocksServerWithSimulation.class.getCanonicalName(),
				new Object[]{
						CLOCK_URI,
						TimeUnit.MILLISECONDS.toNanos(
										 		unixEpochStartTimeInMillis),
						startInstant,
						ACCELERATION_FACTOR,
						DELAY_TO_START_SIMULATION,
						SIMULATION_START_TIME,
						SIMULATION_DURATION,
						SIMULATION_TIME_UNIT});

		if (CURRENT_SIMULATION_TYPE.isSimulated()) {
			AbstractComponent.createComponent(
					CoordinatorComponent.class.getCanonicalName(),
					new Object[]{});
			AbstractComponent.createComponent(
					GlobalSupervisor.class.getCanonicalName(),
					new Object[]{CURRENT_SIMULATION_TYPE,
								 globalArchitectureURI});
		}

		super.deploy();
	}
	
	public static void main(String[] args) {
		VerboseException.VERBOSE = true;
		VerboseException.PRINT_STACK_TRACE = true;
		NeoSim4JavaException.VERBOSE = true;
		NeoSim4JavaException.PRINT_STACK_TRACE = true;

		try {
			CVMIntegrationTest cvm = new CVMIntegrationTest();
			
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
