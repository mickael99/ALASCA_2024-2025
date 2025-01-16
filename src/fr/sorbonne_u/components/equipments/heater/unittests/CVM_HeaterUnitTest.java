package fr.sorbonne_u.components.equipments.heater.unittests;

// Copyright Jacques Malenfant, Sorbonne Universite.
// Jacques.Malenfant@lip6.fr
//
// This software is a computer program whose purpose is to provide a
// basic component programming model to program with components
// real time distributed applications in the Java programming language.
//
// This software is governed by the CeCILL-C license under French law and
// abiding by the rules of distribution of free software.  You can use,
// modify and/ or redistribute the software under the terms of the
// CeCILL-C license as circulated by CEA, CNRS and INRIA at the following
// URL "http://www.cecill.info".
//
// As a counterpart to the access to the source code and  rights to copy,
// modify and redistribute granted by the license, users are provided only
// with a limited warranty  and the software's author,  the holder of the
// economic rights,  and the successive licensors  have only  limited
// liability. 
//
// In this respect, the user's attention is drawn to the risks associated
// with loading,  using,  modifying and/or developing or reproducing the
// software by the user in light of its specific status of free software,
// that may mean  that it is complicated to manipulate,  and  that  also
// therefore means  that it is reserved for developers  and  experienced
// professionals having in-depth computer knowledge. Users are therefore
// encouraged to load and test the software's suitability as regards their
// requirements in conditions enabling the security of their systems and/or 
// data to be ensured and,  more generally, to use and operate it in the 
// same conditions as regards security. 
//
// The fact that you are presently reading this means that you have had
// knowledge of the CeCILL-C license and that you accept its terms.

import java.time.Instant;
import java.util.concurrent.TimeUnit;
import fr.sorbonne_u.components.AbstractComponent;
import fr.sorbonne_u.components.CoordinatorComponent;
import fr.sorbonne_u.components.cvm.AbstractCVM;
import fr.sorbonne_u.components.cyphy.utils.aclocks.ClocksServerWithSimulation;
import fr.sorbonne_u.components.equipments.heater.Heater;
import fr.sorbonne_u.components.equipments.heater.HeaterController;
import fr.sorbonne_u.components.equipments.heater.HeaterController.ControlMode;
import fr.sorbonne_u.components.equipments.heater.HeaterUser;
import fr.sorbonne_u.components.equipments.heater.mil.HeaterCoupledModel;
import fr.sorbonne_u.components.equipments.heater.mil.HeaterUnitTesterModel;
import fr.sorbonne_u.components.utils.ExecutionType;
import fr.sorbonne_u.components.utils.SimulationType;
import fr.sorbonne_u.devs_simulation.exceptions.NeoSim4JavaException;
import fr.sorbonne_u.exceptions.VerboseException;
import fr.sorbonne_u.utils.aclocks.ClocksServer;

// -----------------------------------------------------------------------------
/**
 * The class <code>CVM_HeaterUnitTest</code> defines an execution CVM script
 * to test the heater appliance in isolation.
 *
 * <p><strong>Description</strong></p>
 * 
 * <p>
 * Appliances unit tests can be performed in different execution and simulation
 * types that {@code CVM_HeaterUnitTest} must organise. Basically, the
 * execution must organise the following steps:
 * </p>
 * <ol>
 * <li>Creation and initialisation of the components.</li>
 * <li>Start of the test scenarios.</li>
 * <li>Start of the simulation scenarios after the creation and initialisation
 *   of the local simulation architectures in every component and of the global
 *   component simulation architecture by the supervisor component.</li>
 * <li>When simulating, the recovery of the simulation reports if needed.</li>
 * <li>The cutoff of the CVM, when everything else has finished.</li>
 * </ol>
 * <p>
 * The alignment of the components required to perform the above actions in
 * synchrony follows a time-triggered synchronisation scheme using two means:
 * </p>
 * <ol>
 * <li>The accelerated clock time-triggered synchronisation offered by
 *   BCM4Java.</li>
 * <li>The NeoSim4Java time triggered start of the individual simulators in
 *   real time simulations (MIL real time and SIL).</li>
 * </ol>
 * <p>
 * The points of synchronisation are defined by delays from the start of the
 * application before their occurrence and durations of the activities:
 * </p>
 * <ol>
 * <li>{@code DELAY_TO_START} is the delay given to the creation and
 *   initialisation of the components before starting the execution of the
 *   test scenarios. It is ensured by an accelerated clock shared among
 *   the components. It is expressed in milliseconds.</li>
 * <li>{@code EXECUTION_DURATION} is the duration of the execution of the
 *   test scenarios when no simulation are used. It is also expressed in
 *   milliseconds.</li>
 * <li>{@code DELAY_TO_START_SIMULATION} is the delay after the beginning of
 *   the test scenarios but before the start of the simulators.  It is also
 *   expressed in milliseconds.</li>
 * <li>{@code SIMULATION_DURATION} is the duration of the global simulation
 *   <i>i.e.</i>, for all of the simulators in all of the components. It is
 *   expressed in simulation time unit (here hours).</li>
 * <li>{@code DELAY_TO_STOP} is the delay after the end of the execution
 *   <i>i.e.</i>, after the end of the {@code EXECUTION_DURATION} or
 *   {@code SIMULATION_DURATION}, and before shutting down the components and
 *   the component virtual machine.</li>
 * </ol>
 * <p>
 * In real time simulations, an {@code ACCELERATION_FACTOR} can be applied to
 * the simulation duration, which must also be followed by the test scenarios
 * and the computation of the application execution duration. Both the BCM4Java
 * accelerated clock and the NeoSim4Java real time facilities cope with such an
 * acceleration factor, but it is of course required to have the same factor in
 * both when performing SIL simulations and tests.
 * </p>
 * 
 * <p>Simulation architectures</p>
 * 
 * <p>
 * For the heater unit tests, there are two component simulation
 * architectures that can be used. The one for MIL and MIL real time simulations
 * is:
 * </p>
 * <p><img src="../../../../../../../../images/hem-2024-e3/HeaterUnitTestMILComponentArchitecture.png"/></p>
 * <p>
 * See {@code HeaterUnitTestsSupervisor} for the explanations. The second
 * component simulation architecture is for SIL simulations where there is no
 * longer a need for a simulator in {@code HeaterUser} as it will rather
 * execute its code directly to test {@code Heater}. There is no need either
 * then for a supervisor component:
 * </p>
 * <p><img src="../../../../../../../../images/hem-2024-e3/HeaterUnitTestSILComponentArchitecture.png"/></p>
 * 
 * <p><strong>Glass-box Invariants</strong></p>
 * 
 * <pre>
 * invariant	{@code true}	// no more invariant
 * </pre>
 * 
 * <p><strong>Black-box Invariants</strong></p>
 * 
 * <pre>
 * invariant	{@code true}	// no more invariant
 * </pre>
 * 
 * <p>Created on : 2023-11-13</p>
 * 
 * @author	<a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
 */
public class			CVM_HeaterUnitTest
extends		AbstractCVM
{
	// -------------------------------------------------------------------------
	// Constants and variables
	// -------------------------------------------------------------------------

	/** delay before starting the test scenarios, leaving time to build
	 *  and initialise the components and their simulators; this delay is
	 *  estimated given the complexity of the initialisation but could also
	 *  need to be revised if the computer on which the application is run
	 *  is less powerful.													*/
	public static long				DELAY_TO_START = 3000L;
	/** duration of the execution when simulation is *not* used.			*/
	public static long				EXECUTION_DURATION = 5000L;
	/** delay before the CVM must stop the execution after the execution
	 *  of the tests scenarios and, possibly, the attached simulations.		*/
	public static long				DELAY_TO_STOP = 2000L;
	/** duration of the sleep at the end of the execution before exiting
	 *  the JVM.															*/
	public static long				END_SLEEP_DURATION = 10000L;

	/** delay to start the real time simulations on every atomic model at the
	 *  same moment (the order is delivered to the models during this delay;
	 *  this delay must be ample enough to give the time to notify all models
	 *  of their start time and to initialise them before starting, a value
	 *  that depends upon the complexity of the simulation architecture to be
	 *  traversed, the power of the computer on which it is run and the
	 *  type of component deployment (deployments on several JVM and even more
	 *  several computers require a larger delay.							*/
	public static long				DELAY_TO_START_SIMULATION = 3000L;
	/** start time of the simulation, in simulated logical time, if
	 *  relevant.															*/
	public static double 			SIMULATION_START_TIME = 0.0;
	/** duration of the simulation, in simulated time.						*/
	public static double			SIMULATION_DURATION = 3.0;
	/** time unit in which {@code SIMULATION_DURATION} is expressed.		*/
	public static TimeUnit			SIMULATION_TIME_UNIT = TimeUnit.HOURS;
	/** for real time simulations, the acceleration factor applied to the
	 *  the simulated time to get the execution time of the simulations. 	*/
	public static double			ACCELERATION_FACTOR = 360.0;

	/** the type of execution, to select among the values of the
	 *  enumeration {@code ExecutionType}.									*/
	public static ExecutionType		CURRENT_EXECUTION_TYPE =
											//ExecutionType.STANDARD;
											ExecutionType.UNIT_TEST;
	/** the type of execution, to select among the values of the
	 *  enumeration {@code ExecutionType}.									*/
	public static SimulationType	CURRENT_SIMULATION_TYPE =
											//SimulationType.NO_SIMULATION;
											SimulationType.MIL_SIMULATION;
											//SimulationType.MIL_RT_SIMULATION;
											//SimulationType.SIL_SIMULATION;
	/** the control mode of the heater controller for the next run.			*/
	public static ControlMode		CONTROL_MODE = ControlMode.PUSH;

	/** for unit tests and SIL simulation tests, a {@code Clock} is
	 *  used to get a time-triggered synchronisation of the actions of
	 *  the components in the test scenarios.								*/
	public static String			CLOCK_URI = "hem-clock";
	/** start instant in test scenarios.									*/
	public static String			START_INSTANT = "2024-10-18T00:00:00.00Z";

	// -------------------------------------------------------------------------
	// Constructors
	// -------------------------------------------------------------------------

	/**
	 * create an instance of CVM.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code true}	// no precondition.
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @throws Exception	<i>to do</i>.
	 */
	public				CVM_HeaterUnitTest() throws Exception
	{
		ClocksServer.VERBOSE = true;
		ClocksServer.X_RELATIVE_POSITION = 0;
		ClocksServer.Y_RELATIVE_POSITION = 0;
		HeaterUnitTestsSupervisor.VERBOSE = true;
		HeaterUnitTestsSupervisor.X_RELATIVE_POSITION = 1;
		HeaterUnitTestsSupervisor.Y_RELATIVE_POSITION = 0;
		CoordinatorComponent.VERBOSE = true;
		CoordinatorComponent.X_RELATIVE_POSITION = 2;
		CoordinatorComponent.Y_RELATIVE_POSITION = 0;

		Heater.VERBOSE = true;
		Heater.X_RELATIVE_POSITION = 1;
		Heater.Y_RELATIVE_POSITION = 1;
		HeaterController.VERBOSE = true;
		HeaterController.X_RELATIVE_POSITION = 2;
		HeaterController.Y_RELATIVE_POSITION = 1;
		HeaterUser.VERBOSE = true;
		HeaterUser.X_RELATIVE_POSITION = 0;
		HeaterUser.Y_RELATIVE_POSITION = 1;
	}

	// -------------------------------------------------------------------------
	// Component life-cycle
	// -------------------------------------------------------------------------

	/**
	 * @see fr.sorbonne_u.components.cvm.AbstractCVM#deploy()
	 */
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
		String globalArchitectureURI = "";
		// URI of the HairDryer local simulation architecture for the current
		// run, if relevant.
		String heaterLocalArchitectureURI = "";
		// URI of the HairDryerUser local simulation architecture for the
		// current run, if relevant.
		String heaterUserLocalArchitectureURI = "";

		long current = System.currentTimeMillis();
		// start time of the components in Unix epoch time in milliseconds.
		long unixEpochStartTimeInMillis = current + DELAY_TO_START;

		// URI of the simulation models and architectures differ among
		// simulation types merely to offer the possibility to the components
		// to create and store all of their possible simulation architectures
		// at the same time, hence avoiding confusion among them.
		switch (CURRENT_SIMULATION_TYPE) {
		case MIL_SIMULATION:
			globalArchitectureURI = HeaterUnitTestsSupervisor.MIL_ARCHITECTURE_URI;
			heaterLocalArchitectureURI = HeaterCoupledModel.MIL_URI;
			heaterUserLocalArchitectureURI = HeaterUnitTesterModel.MIL_URI;
			break;
		case MIL_RT_SIMULATION:
			globalArchitectureURI = HeaterUnitTestsSupervisor.MIL_RT_ARCHITECTURE_URI;
			heaterLocalArchitectureURI = HeaterCoupledModel.MIL_RT_URI;
			heaterUserLocalArchitectureURI = HeaterUnitTesterModel.MIL_RT_URI;
			break;
		case SIL_SIMULATION:
			globalArchitectureURI = HeaterUnitTestsSupervisor.SIL_ARCHITECTURE_URI;
			heaterLocalArchitectureURI =  HeaterCoupledModel.SIL_URI;
			heaterUserLocalArchitectureURI = "not-used";
			break;
		case NO_SIMULATION:
		default:
		}

		// Deploy the components

		AbstractComponent.createComponent(
				Heater.class.getCanonicalName(),
				new Object[]{Heater.REFLECTION_INBOUND_PORT_URI,
							 Heater.USER_INBOUND_PORT_URI,
							 Heater.INTERNAL_CONTROL_INBOUND_PORT_URI,
							 Heater.EXTERNAL_CONTROL_INBOUND_PORT_URI,
							 Heater.SENSOR_INBOUND_PORT_URI,
							 Heater.ACTUATOR_INBOUND_PORT_URI,
							 CURRENT_EXECUTION_TYPE,
							 CURRENT_SIMULATION_TYPE,
							 globalArchitectureURI,
							 heaterLocalArchitectureURI,
							 SIMULATION_TIME_UNIT,
							 ACCELERATION_FACTOR,
							 CLOCK_URI});
		AbstractComponent.createComponent(
				HeaterController.class.getCanonicalName(),
				new Object[]{Heater.SENSOR_INBOUND_PORT_URI,
							 Heater.ACTUATOR_INBOUND_PORT_URI,
							 HeaterController.STANDARD_HYSTERESIS,
							 HeaterController.STANDARD_CONTROL_PERIOD,
							 CONTROL_MODE,
							 CURRENT_EXECUTION_TYPE,
							 CURRENT_SIMULATION_TYPE,
							 CLOCK_URI});
		AbstractComponent.createComponent(
				HeaterUser.class.getCanonicalName(),
				new Object[]{Heater.USER_INBOUND_PORT_URI,
							 Heater.INTERNAL_CONTROL_INBOUND_PORT_URI,
							 Heater.EXTERNAL_CONTROL_INBOUND_PORT_URI,
							 CURRENT_EXECUTION_TYPE,
							 CURRENT_SIMULATION_TYPE,
							 globalArchitectureURI,
							 heaterUserLocalArchitectureURI,
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
			// component simulator in the component HairDryer, which itself
			// starts the simulation without the need for supervision and
			// coordination.
			AbstractComponent.createComponent(
					CoordinatorComponent.class.getCanonicalName(),
					new Object[]{});
			AbstractComponent.createComponent(
					HeaterUnitTestsSupervisor.class.getCanonicalName(),
					new Object[]{CURRENT_SIMULATION_TYPE,
								 globalArchitectureURI});
		}

		super.deploy();
	}

	/**
	 * start the execution.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code true}	// no precondition.
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @param args	command-line arguments.
	 */
	public static void	main(String[] args)
	{
		// Force the exceptions in BCM4Java and NeoSim4Java more explicit
		VerboseException.VERBOSE = true;
		VerboseException.PRINT_STACK_TRACE = true;
		NeoSim4JavaException.VERBOSE = true;
		NeoSim4JavaException.PRINT_STACK_TRACE = true;

		try {
			CVM_HeaterUnitTest cvm = new CVM_HeaterUnitTest();
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
// -----------------------------------------------------------------------------
