package fr.sorbonne_u.components.equipments.generator;

import fr.sorbonne_u.components.AbstractComponent;
import fr.sorbonne_u.components.CoordinatorComponent;
import fr.sorbonne_u.components.cvm.AbstractCVM;
import fr.sorbonne_u.components.cyphy.utils.aclocks.ClocksServerWithSimulation;
import fr.sorbonne_u.components.equipments.generator.mil.GeneratorCoupledModel;
import fr.sorbonne_u.components.equipments.generator.mil.GeneratorUserModel;
import fr.sorbonne_u.components.utils.ExecutionType;
import fr.sorbonne_u.components.utils.SimulationType;
import fr.sorbonne_u.devs_simulation.exceptions.NeoSim4JavaException;
import fr.sorbonne_u.exceptions.VerboseException;
import fr.sorbonne_u.utils.aclocks.ClocksServer;

import java.time.Instant;
import java.util.concurrent.TimeUnit;

public class CVM_GeneratorUnitTest extends AbstractCVM {

	public static long				DELAY_TO_START = 3000L;
	public static long				EXECUTION_DURATION = 5000L;
	public static long				DELAY_TO_STOP = 2000L;
	public static long				END_SLEEP_DURATION = 10000L;

	public static long				DELAY_TO_START_SIMULATION = 3000L;
	public static double 			SIMULATION_START_TIME = 0.0;
	public static double			SIMULATION_DURATION = 3.0;
	public static TimeUnit SIMULATION_TIME_UNIT = TimeUnit.HOURS;
	/** for real time simulations, the acceleration factor applied to the
	 *  the simulated time to get the execution time of the simulations. 	*/
	public static double			ACCELERATION_FACTOR = 360.0;

	/** the type of execution, to select among the values of the
	 *  enumeration {@code ExecutionType}.									*/
	public static ExecutionType CURRENT_EXECUTION_TYPE =
//			ExecutionType.STANDARD;
			ExecutionType.UNIT_TEST;
	/** the type of execution, to select among the values of the
	 *  enumeration {@code ExecutionType}.									*/
	public static SimulationType CURRENT_SIMULATION_TYPE =
//			SimulationType.NO_SIMULATION;
//			SimulationType.MIL_SIMULATION;
//			SimulationType.MIL_RT_SIMULATION;
			SimulationType.SIL_SIMULATION;

	public static String			CLOCK_URI = "hem-clock";
	public static String			START_INSTANT = "2024-10-18T00:00:00.00Z";


	public CVM_GeneratorUnitTest() throws Exception
	{
		ClocksServer.VERBOSE = true;
		ClocksServer.X_RELATIVE_POSITION = 0;
		ClocksServer.Y_RELATIVE_POSITION = 0;
		GeneratorUnitTestSupervisor.VERBOSE = true;
		GeneratorUnitTestSupervisor.X_RELATIVE_POSITION = 1;
		GeneratorUnitTestSupervisor.Y_RELATIVE_POSITION = 0;
		CoordinatorComponent.VERBOSE = true;
		CoordinatorComponent.X_RELATIVE_POSITION = 2;
		CoordinatorComponent.Y_RELATIVE_POSITION = 0;

		Generator.VERBOSE = true;
		Generator.X_RELATIVE_POSITION = 1;
		Generator.Y_RELATIVE_POSITION = 1;
		GeneratorUser.VERBOSE = true;
		GeneratorUser.X_RELATIVE_POSITION = 0;
		GeneratorUser.Y_RELATIVE_POSITION = 1;
	}


	@Override
	public void			deploy() throws Exception
	{
		assert	!CURRENT_EXECUTION_TYPE.isIntegrationTest() :
				new RuntimeException(
						"!CURRENT_EXECUTION_TYPE.isIntegrationTest()");
		assert	!CURRENT_EXECUTION_TYPE.isStandard() ||
				  CURRENT_SIMULATION_TYPE.isNoSimulation() :
				new RuntimeException(
						"!CURRENT_EXECUTION_TYPE.isStandard() || "
						+ "CURRENT_SIMULATION_TYPE.isNoSimulation()");

		// Set the main execution run parameters, depending on the type of
		// execution that is required.

		// URI of the global simulation architecture for the current run,
		// if relevant.
		String architectureURI = "";
		// URI of the Generator local simulation architecture for the current
		// run, if relevant.
		String hairDryerLocalArchitectureURI = "";
		// URI of the GeneratorUser local simulation architecture for the
		// current run, if relevant.
		String hairDryerUserLocalArchitectureURI = "";

		long current = System.currentTimeMillis();
		// start time of the components in Unix epoch time in milliseconds.
		long unixEpochStartTimeInMillis = current + DELAY_TO_START;

		// URI of the simulation models and architectures differ among
		// simulation types merely to offer the possibility to the components
		// to create and store all of their possible simulation architectures
		// at the same time, hence avoiding confusion among them.
		switch (CURRENT_SIMULATION_TYPE) {
			case MIL_SIMULATION:
				architectureURI = GeneratorUnitTestSupervisor.MIL_ARCHITECTURE_URI;
				hairDryerLocalArchitectureURI = GeneratorCoupledModel.MIL_URI;
				hairDryerUserLocalArchitectureURI = GeneratorUserModel.MIL_URI;
				break;
			case MIL_RT_SIMULATION:
				architectureURI = GeneratorUnitTestSupervisor.MIL_RT_ARCHITECTURE_URI;
				hairDryerLocalArchitectureURI = GeneratorCoupledModel.MIL_RT_URI;
				hairDryerUserLocalArchitectureURI = GeneratorUserModel.MIL_RT_URI;
				break;
			case SIL_SIMULATION:
				architectureURI = GeneratorUnitTestSupervisor.SIL_ARCHITECTURE_URI;
				hairDryerLocalArchitectureURI =  GeneratorCoupledModel.SIL_URI;
				hairDryerUserLocalArchitectureURI = "not-used";
				break;
			case NO_SIMULATION:
			default:
		}

		// Deploy the components

		AbstractComponent.createComponent(
				Generator.class.getCanonicalName(),
				new Object[]{Generator.REFLECTION_INBOUND_PORT_URI,
							 Generator.INBOUND_PORT_URI,
							 CURRENT_EXECUTION_TYPE,
							 CURRENT_SIMULATION_TYPE,
							 architectureURI,
							 hairDryerLocalArchitectureURI,
							 SIMULATION_TIME_UNIT,
							 ACCELERATION_FACTOR,
							 CLOCK_URI});
		AbstractComponent.createComponent(
				GeneratorUser.class.getCanonicalName(),
				new Object[]{GeneratorUser.REFLECTION_INBOUND_PORT_URI,
							 Generator.INBOUND_PORT_URI,
							 CURRENT_EXECUTION_TYPE,
							 CURRENT_SIMULATION_TYPE,
							 architectureURI,
							 hairDryerUserLocalArchitectureURI,
							 SIMULATION_TIME_UNIT,
							 ACCELERATION_FACTOR,
							 CLOCK_URI});

		AbstractComponent.createComponent(
				ClocksServerWithSimulation.class.getCanonicalName(),
				new Object[]{
						// URI of the clock to retrieve it
						CLOCK_URI,
						// start time in Unix epoch time
						TimeUnit.MILLISECONDS.toNanos(
								unixEpochStartTimeInMillis),
						// start instant synchronised with the start time
						Instant.parse(START_INSTANT),
						ACCELERATION_FACTOR,
						DELAY_TO_START_SIMULATION,
						SIMULATION_START_TIME,
						SIMULATION_DURATION,
						SIMULATION_TIME_UNIT});

		if (CURRENT_SIMULATION_TYPE.isMilSimulation() ||
			CURRENT_SIMULATION_TYPE.isMILRTSimulation()) {
			// A supervisor component and a coordinator component are only
			// needed for MIL and MIL real time simulation because for the
			// hair dryer unit tests with SIL simulation executes only one
			// component simulator in the component Generator, which itself
			// starts the simulation without the need for supervision and
			// coordination.
			AbstractComponent.createComponent(
					CoordinatorComponent.class.getCanonicalName(),
					new Object[]{});
			AbstractComponent.createComponent(
					GeneratorUnitTestSupervisor.class.getCanonicalName(),
					new Object[]{CURRENT_SIMULATION_TYPE,
								 architectureURI});
		}

		super.deploy();
	}

	public static void	main(String[] args)
	{
		// Force the exceptions in BCM4Java and NeoSim4Java more explicit
		VerboseException.VERBOSE = true;
		VerboseException.PRINT_STACK_TRACE = true;
		NeoSim4JavaException.VERBOSE = true;
		NeoSim4JavaException.PRINT_STACK_TRACE = true;

		try {
			CVM_GeneratorUnitTest cvm = new CVM_GeneratorUnitTest();
			// compute the execution duration in milliseconds from the
			// simulation duration in hours and the acceleration factor
			// i.e., the simulation duration times 3600 seconds per hour
			// times 1000 milliseconds per second divided by the acceleration
			// factor
			long executionDurationInMillis = 0L;
			switch (CURRENT_SIMULATION_TYPE) {
				case MIL_SIMULATION:
					// In MIL simulations, simulations runs quickly enough that it
					// is sufficient to use EXECUTION_DURATION as the duration of
					// the application execution
					executionDurationInMillis =
							DELAY_TO_START + DELAY_TO_START_SIMULATION
							+ EXECUTION_DURATION + DELAY_TO_STOP;
					break;
				case MIL_RT_SIMULATION:
				case SIL_SIMULATION:
					// IN MIL real time and SIL simulations, the duration of the
					// application execution must be the same as the duration of the
					// execution of the simulation, which is computed as the
					// simulation duration expressed in the simulation time unit
					// to which is applied the acceleration factor and then converted
					// to a time in milliseconds; for example a simulation duration
					// of one hour (simulation time unit) with acceleration factor
					// 60 will take one minute execution time converted to 60000
					// milliseconds.
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
			// delay to look at the results before closing the trace windows
			Thread.sleep(END_SLEEP_DURATION);
			// force the exit
			System.exit(0);
		} catch (Exception e) {
			throw new RuntimeException(e) ;
		}
	}

}
