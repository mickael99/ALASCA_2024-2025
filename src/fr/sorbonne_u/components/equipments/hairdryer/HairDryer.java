package fr.sorbonne_u.components.equipments.hairdryer;

// Copyright Jacques Malenfant, Sorbonne Universite.
// Jacques.Malenfant@lip6.fr
//
// This software is a computer program whose purpose is to implement a mock-up
// of household energy management system.
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

import fr.sorbonne_u.components.annotations.OfferedInterfaces;
import fr.sorbonne_u.components.annotations.RequiredInterfaces;
import fr.sorbonne_u.components.cyphy.AbstractCyPhyComponent;
import fr.sorbonne_u.components.cyphy.plugins.devs.AtomicSimulatorPlugin;
import fr.sorbonne_u.components.cyphy.plugins.devs.RTAtomicSimulatorPlugin;
import fr.sorbonne_u.components.cyphy.utils.aclocks.AcceleratedAndSimulationClock;
import fr.sorbonne_u.components.cyphy.utils.aclocks.ClocksServerWithSimulationCI;
import fr.sorbonne_u.components.cyphy.utils.aclocks.ClocksServerWithSimulationConnector;
import fr.sorbonne_u.components.cyphy.utils.aclocks.ClocksServerWithSimulationOutboundPort;
import fr.sorbonne_u.components.exceptions.BCMException;
import fr.sorbonne_u.components.exceptions.ComponentShutdownException;
import fr.sorbonne_u.components.exceptions.ComponentStartException;
import fr.sorbonne_u.components.utils.ExecutionType;
import fr.sorbonne_u.components.utils.SimulationType;
import fr.sorbonne_u.exceptions.ImplementationInvariantException;
import fr.sorbonne_u.exceptions.InvariantChecking;
import fr.sorbonne_u.exceptions.InvariantException;
import fr.sorbonne_u.exceptions.PostconditionException;
import fr.sorbonne_u.exceptions.PreconditionException;
import fr.sorbonne_u.utils.aclocks.ClocksServer;
import fr.sorbonne_u.components.equipments.hairdryer.connections.*;
import fr.sorbonne_u.components.equipments.hairdryer.mil.HairDryerStateModel;
import fr.sorbonne_u.components.equipments.hairdryer.mil.LocalSimulationArchitectures;
import fr.sorbonne_u.components.equipments.hairdryer.mil.events.SetHighHairDryer;
import fr.sorbonne_u.components.equipments.hairdryer.mil.events.SetLowHairDryer;
import fr.sorbonne_u.components.equipments.hairdryer.mil.events.SwitchOffHairDryer;
import fr.sorbonne_u.components.equipments.hairdryer.mil.events.SwitchOnHairDryer;
import fr.sorbonne_u.devs_simulation.architectures.Architecture;
import fr.sorbonne_u.devs_simulation.models.time.Duration;
import fr.sorbonne_u.devs_simulation.models.time.Time;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

// -----------------------------------------------------------------------------
/**
 * The class <code>HairDryer</code> implements the hair dryer component.
 *
 * <p><strong>Description</strong></p>
 * 
 * <p>
 * The hair dryer is an uncontrollable appliance, hence it does not connect
 * with the household energy manager. However, it connects to the electric meter
 * to take its (simulated) power consumption into account.
 * </p>
 * <p>
 * This implementation of the hair dryer is complicated by the objective to show
 * the entire spectrum of possible execution and simulation modes. There are
 * three execution types defined by {@code ExecutionType}:
 * </p>
 * <ol>
 * <li>{@code STANDARD} means that the component would execute in normal
 *   operational conditions, on the field (which is not used in this project
 *   but would be in an industrial one).</li>
 * <li>{@code UNIT_TEST} means that the component executes in unit tests where
 *   it is the sole appliance but cooperates with the {@code HairDryerUser}
 *   component.</li>
 * <li>{@code INTEGRATION_TEST} means that the component executes in integration
 *   tests where other appliances coexist and where it must cooperates with the
 *   {@code HairDryerUser} and also the {@code ElectricMeter} components.</li>
 * </ol>
 * <p>
 * There are also four distinct types of simulations defined by
 * {@code SimulationType}:
 * </p>
 * <ol>
 * <li>{@code NO_SIMULATION} means that the component does not execute a
 *   simulator, a type necessarily used in {@code STANDARD} executions but also
 *   for {@code UNIT_TEST} with no simulation.</li>
 * <li>{@code MIL_SIMULATION} means that only MIL simulators will run; it is
 *   meant to be used early stages of a project in {@code UNIT_TEST} and
 *   {@code INTEGRATION_TEST} before implementing the code of the components.
 *   </li>
 * <li>{@code MIL_RT_SIMULATION} is similar to {@code MIL_SIMULATION} but
 *   simulates in real time or an accelerated real time; it is more a step
 *   towards SIL simulations than an actual interesting simulation type
 *   in an industrial project.</li>
 * <li>{@code SIL_SIMULATION} means that the simulators are implemented so
 *   that they can execute with the component software in software-in-the-loop
 *   simulations; it can be used in {@code UNIT_TEST} executions to test
 *   the component software in isolation and then in {@code INTEGRATION_TEST}
 *   executions to test the entire application at once.</li>
 * </ol>
 * <p>
 * In this implementation of the {@code HairDryer} component, the standard
 * execution type is not really implemented as the software is not embedded in
 * any real appliance. In unit tests with no simulation, the component is
 * totally passive as its methods are called by the {@code HairDryerUser}
 * component. In MIL and MIL real time simulations, the component simply creates
 * and installs its local simulation architecture, which execution will be
 * started by a supervisor component. It is also the case for SIL simulations
 * in integration tests.
 * </p>
 * <p>
 * For SIL simulations in unit tests, the component presents a rather special
 * case as it is the only component that runs a simulator, hence there is no
 * global component simulation architecture and therefore no need for
 * supervisor and coordinator components. Hence, the local SIL simulation
 * architecture uses only the models pertaining to the hair dryer itself:
 * </p>
 * <p><img src="../../../../../../../images/hem-2024-e3/HairDryerUnitTestLocalArchitecture.png"/></p>
 * <p>
 * After creating (in {@code initialise}) its local SIL simulation architecture
 * and installing the local simulation plug-in (in {@code start}), the component
 * also creates, initialises and triggers the execution of the simulator in the
 * method {@code execute}.
 * </p>
 * <p>
 * For SIL simulations in integration tests, the {@code HairDryerElectricityModel}
 * cannot share its continuous variable {@code currentIntensity} with the
 * {@code ElectricMeterElectricityModel} across component borders. Hence, the
 * {@code HairDryerElectricityModel} is rather moved to the {@code ElectricMeter}
 * component simulator, to co-localise it with the
 * {@code ElectricMeterElectricityModel}, hence the {@code HairDryerStateModel}
 * remaining in the {@code HairDryer} component simulator will emits the hair
 * dryer events to the {@code HairDryerElectricityModel} across the border of
 * the {@code HairDryer} and {@code ElectricMeter} components.
 * </p>
 * 
 * <p><strong>Glass-box Invariants</strong></p>
 * 
 * <pre>
 * invariant	{@code currentState != null}
 * invariant	{@code currentMode != null}
 * invariant	{@code currentExecutionType != null}
 * invariant	{@code currentSimulationType != null}
 * invariant	{@code !currentExecutionType.isStandard() || currentSimulationType.isNoSimulation()}
 * invariant	{@code currentSimulationType.isNoSimulation() || (globalArchitectureURI != null && !globalArchitectureURI.isEmpty())}
 * invariant	{@code currentSimulationType.isNoSimulation() || (localArchitectureURI != null && !localArchitectureURI.isEmpty())}
 * invariant	{@code !currentSimulationType.isSimulated() || simulatedStartTime >= 0.0}
 * invariant	{@code !currentSimulationType.isSimulated() || simulationDuration > 0.0}
 * invariant	{@code !currentSimulationType.isSimulated() || simulationTimeUnit != null}
 * invariant	{@code !currentSimulationType.isRealTimeSimulation() || accFactor > 0.0}
 * </pre>
 * 
 * <p><strong>Black-box Invariants</strong></p>
 * 
 * <pre>
 * invariant	{@code REFLECTION_INBOUND_PORT_URI != null && !REFLECTION_INBOUND_PORT_URI.isEmpty()}
 * invariant	{@code INBOUND_PORT_URI != null && !INBOUND_PORT_URI.isEmpty()}
 * invariant	{@code X_RELATIVE_POSITION >= 0}
 * invariant	{@code Y_RELATIVE_POSITION >= 0}
 * invariant	{@code INITIAL_STATE != null}
 * invariant	{@code INITIAL_MODE != null}
 * </pre>
 * 
 * <p>Created on : 2023-09-19</p>
 * 
 * @author	<a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
 */
@OfferedInterfaces(offered={HairDryerUserCI.class})
@RequiredInterfaces(required = {ClocksServerWithSimulationCI.class})
public class			HairDryer
extends		AbstractCyPhyComponent
implements	HairDryerImplementationI
{
	// -------------------------------------------------------------------------
	// Constants and variables
	// -------------------------------------------------------------------------

	/** when true, methods trace their actions.								*/
	public static boolean				VERBOSE = false;
	/** when tracing, x coordinate of the window relative position.			*/
	public static int					X_RELATIVE_POSITION = 0;
	/** when tracing, y coordinate of the window relative position.			*/
	public static int					Y_RELATIVE_POSITION = 0;

	/** URI of the hair dryer inbound port used in tests.					*/
	public static final String			REFLECTION_INBOUND_PORT_URI =
														"HAIR-DRYER-RIP-URI";	
	/** URI of the hair dryer inbound port used in tests.					*/
	public static final String			INBOUND_PORT_URI =
												"HAIR-DRYER-INBOUND-PORT-URI";

	/** initial state in which the hair dryer is put.						*/
	public static final HairDryerState	INITIAL_STATE = HairDryerState.OFF;
	/** initial mode in which the hair dryer is put.						*/
	public static final HairDryerMode	INITIAL_MODE = HairDryerMode.LOW;

	/** current state (on, off) of the hair dryer.							*/
	protected HairDryerState			currentState;
	/** current mode of operation (low, high) of the hair dryer.			*/
	protected HairDryerMode				currentMode;

	/** inbound port offering the <code>HairDryerCI</code> interface.		*/
	protected HairDryerInboundPort		hdip;

	// Execution/Simulation

	/** current type of execution.											*/
	protected final ExecutionType		currentExecutionType;
	/** current type of simulation.											*/
	protected final SimulationType		currentSimulationType;

	/** URI of the clock to be used to synchronise the test scenarios and
	 *  the simulation.														*/
	protected final String				clockURI;
	/** plug-in holding the local simulation architecture and simulators.	*/
	protected AtomicSimulatorPlugin		asp;
	/** URI of the global simulation architecture to be created or the
	 *  empty string if the component does not execute as a simulation.		*/
	protected final String				globalArchitectureURI;
	/** URI of the local simulation architecture used to compose the global
	 *  simulation architecture or the empty string if the component does
	 *  not execute as a simulation.										*/
	protected final String				localArchitectureURI;
	/** time unit in which {@code simulatedStartTime} and
	 *  {@code simulationDuration} are expressed.							*/
	protected final TimeUnit			simulationTimeUnit;
	/** acceleration factor to be used when running the real time
	 *  simulation.															*/
	protected double					accFactor;

	// -------------------------------------------------------------------------
	// Invariants
	// -------------------------------------------------------------------------

	/**
	 * return true if the glass-box invariants are observed, false otherwise.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code hd != null}
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @param hd	instance to be tested.
	 * @return		true if the glass-box invariants are observed, false otherwise.
	 */
	protected static boolean	glassBoxInvariants(HairDryer hd)
	{
		assert 	hd != null : new PreconditionException("hd != null");

		boolean ret = true;
		ret &= InvariantChecking.checkGlassBoxInvariant(
					hd.currentState != null,
					HairDryer.class, hd,
					"currentState != null");
		ret &= InvariantChecking.checkGlassBoxInvariant(
					hd.currentMode != null,
					HairDryer.class, hd,
					"currentMode != null");
		ret &= InvariantChecking.checkGlassBoxInvariant(
					hd.currentExecutionType != null,
					HairDryer.class, hd,
					"currentExecutionType != null");
		ret &= InvariantChecking.checkGlassBoxInvariant(
					hd.currentSimulationType != null,
					HairDryer.class, hd,
					"hcurrentSimulationType != null");
		ret &= InvariantChecking.checkGlassBoxInvariant(
					!hd.currentExecutionType.isStandard() ||
								hd.currentSimulationType.isNoSimulation(),
					HairDryer.class, hd,
					"!currentExecutionType.isStandard() || "
					+ "currentSimulationType.isNoSimulation()");
		ret &= InvariantChecking.checkGlassBoxInvariant(
					hd.currentSimulationType.isNoSimulation() ||
						(hd.globalArchitectureURI != null &&
							!hd.globalArchitectureURI.isEmpty()),
					HairDryer.class, hd,
					"currentSimulationType.isNoSimulation() || "
					+ "(globalArchitectureURI != null && "
					+ "!globalArchitectureURI.isEmpty())");
		ret &= InvariantChecking.checkGlassBoxInvariant(
					hd.currentSimulationType.isNoSimulation() ||
						(hd.localArchitectureURI != null &&
							!hd.localArchitectureURI.isEmpty()),
					HairDryer.class, hd,
					"currentSimulationType.isNoSimulation() || "
					+ "(localArchitectureURI != null && "
					+ "!localArchitectureURI.isEmpty())");
		ret &= InvariantChecking.checkGlassBoxInvariant(
					!hd.currentSimulationType.isSimulated() ||
												hd.simulationTimeUnit != null,
					HairDryer.class, hd,
					"!currentSimulationType.isSimulated() || "
					+ "simulationTimeUnit != null");
		ret &= InvariantChecking.checkGlassBoxInvariant(
					!hd.currentSimulationType.isRealTimeSimulation() ||
														hd.accFactor > 0.0,
					HairDryer.class, hd,
					"!hd.currentSimulationType.isRealTimeSimulation() || "
					+ "hd.accFactor > 0.0");
		return ret;
	}

	/**
	 * return true if the black-box invariants are observed, false otherwise.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code hd != null}
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @param hd	instance to be tested.
	 * @return		true if the black-box invariants are observed, false otherwise.
	 */
	protected static boolean	blackBoxInvariants(HairDryer hd)
	{
		assert 	hd != null : new PreconditionException("hd != null");

		boolean ret = true;
		ret &= InvariantChecking.checkBlackBoxInvariant(
					X_RELATIVE_POSITION >= 0,
					HairDryer.class, hd,
					"X_RELATIVE_POSITION >= 0");
		ret &= InvariantChecking.checkBlackBoxInvariant(
					Y_RELATIVE_POSITION >= 0,
					HairDryer.class, hd,
					"Y_RELATIVE_POSITION >= 0");
		ret &= InvariantChecking.checkBlackBoxInvariant(
					REFLECTION_INBOUND_PORT_URI != null &&
								!REFLECTION_INBOUND_PORT_URI.isEmpty(),
					HairDryer.class, hd,
					"REFLECTION_INBOUND_PORT_URI != null && "
							+ "!REFLECTION_INBOUND_PORT_URI.isEmpty()");
		ret &= InvariantChecking.checkBlackBoxInvariant(
					INBOUND_PORT_URI != null && !INBOUND_PORT_URI.isEmpty(),
					HairDryer.class, hd,
					"INBOUND_PORT_URI != null && !INBOUND_PORT_URI.isEmpty()");
		ret &= InvariantChecking.checkBlackBoxInvariant(
					INITIAL_STATE != null,
					HairDryer.class, hd,
					"INITIAL_STATE != null");
		ret &= InvariantChecking.checkBlackBoxInvariant(
					INITIAL_MODE != null,
					HairDryer.class, hd,
					"INITIAL_MODE != null");
		return ret;
	}

	// -------------------------------------------------------------------------
	// Constructors
	// -------------------------------------------------------------------------

	/**
	 * create a hair dryer component for standard executions.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code true}	// no precondition.
	 * post	{@code getState() == HairDryerState.OFF}
	 * post	{@code getMode() == HairDryerMode.LOW}
	 * </pre>
	 * 
	 * @throws Exception	<i>to do</i>.
	 */
	protected			HairDryer() throws Exception
	{
		this(INBOUND_PORT_URI);
	}

	/**
	 * create a hair dryer component for standard executions.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code hairDryerInboundPortURI != null && !hairDryerInboundPortURI.isEmpty()}
	 * post	{@code getState() == HairDryerState.OFF}
	 * post	{@code getMode() == HairDryerMode.LOW}
	 * </pre>
	 * 
	 * @param hairDryerInboundPortURI	URI of the hair dryer inbound port.
	 * @throws Exception				<i>to do</i>.
	 */
	protected			HairDryer(String hairDryerInboundPortURI)
	throws Exception
	{
		this(hairDryerInboundPortURI, ExecutionType.STANDARD);
	}

	/**
	 * create a hair dryer component for test executions without simulation.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code hairDryerInboundPortURI != null && !hairDryerInboundPortURI.isEmpty()}
	 * pre	{@code currentExecutionType != null}
	 * pre	{@code currentExecutionType.isTest()}
	 * post	{@code getState() == HairDryerState.OFF}
	 * post	{@code getMode() == HairDryerMode.LOW}
	 * </pre>
	 * 
	 * @param hairDryerInboundPortURI	URI of the hair dryer inbound port.
	 * @param currentExecutionType		execution type for the next run.
	 * @throws Exception				<i>to do</i>.
	 */
	protected			HairDryer(
		String hairDryerInboundPortURI,
		ExecutionType currentExecutionType
		) throws Exception
	{
		this(REFLECTION_INBOUND_PORT_URI, hairDryerInboundPortURI,
			 currentExecutionType, SimulationType.NO_SIMULATION,
			 null, null, null, 0.0, null);

		assert	currentExecutionType.isTest() :
				new PreconditionException("currentExecutionType.isTest()");
	}

	/**
	 * create a hair dryer component for test executions with simulation.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code reflectionInboundPortURI != null && !reflectionInboundPortURI.isEmpty()}
	 * pre	{@code hairDryerInboundPortURI != null && !hairDryerInboundPortURI.isEmpty()}
	 * pre	{@code currentExecutionType != null}
	 * pre	{@code currentExecutionType.isStandard() || clockURI != null && !clockURI.isEmpty()}
	 * pre	{@code !currentExecutionType.isStandard() || currentSimulationType.isNoSimulation()}
	 * pre	{@code currentSimulationType.isNoSimulation() || (globalArchitectureURI != null && !globalArchitectureURI.isEmpty())}
	 * pre	{@code currentSimulationType.isNoSimulation() || (localArchitectureURI != null && !localArchitectureURI.isEmpty())}
	 * pre	{@code !currentSimulationType.isSimulated() || simulationTimeUnit != null}
	 * pre	{@code !currentSimulationType.isRealTimeSimulation() || accFactor > 0.0}
	 * post	{@code getState() == HairDryerState.OFF}
	 * post	{@code getMode() == HairDryerMode.LOW}
	 * </pre>
	 *
	 * @param reflectionInboundPortURI	URI of the reflection inbound port of the component.
	 * @param hairDryerInboundPortURI	URI of the hair dryer inbound port.
	 * @param currentExecutionType		execution type for the next run.
	 * @param currentSimulationType		simulation type for the next run.
	 * @param globalArchitectureURI		URI of the global simulation architecture to be created or the empty string if the component does not execute as a simulation.
	 * @param localArchitectureURI		URI of the local simulation architecture to be used in composing the global simulation architecture.
	 * @param simulationTimeUnit		time unit in which simulated times and durations are expressed.
	 * @param accFactor					acceleration factor for the simulation.
	 * @param clockURI					URI of the clock to be used to synchronise the test scenarios and the simulation.
	 * @throws Exception				<i>to do</i>.
	 */
	protected			HairDryer(
		String reflectionInboundPortURI,
		String hairDryerInboundPortURI,
		ExecutionType currentExecutionType,
		SimulationType currentSimulationType,
		String globalArchitectureURI,
		String localArchitectureURI,
		TimeUnit simulationTimeUnit,
		double accFactor,
		String clockURI
		) throws Exception
	{
		// one thread for the method execute and one to answer the calls to
		// the component services
		super(reflectionInboundPortURI, 2, 0);

		assert	hairDryerInboundPortURI != null &&
											!hairDryerInboundPortURI.isEmpty() :
				new PreconditionException(
						"hairDryerInboundPortURI != null && "
						+ "!hairDryerInboundPortURI.isEmpty()");
		assert	currentExecutionType != null :
				new PreconditionException("currentExecutionType != null");
		assert	currentExecutionType.isStandard() ||
									clockURI != null && !clockURI.isEmpty() :
				new PreconditionException(
						"currentExecutionType.isStandard() || "
						+ "clockURI != null && !clockURI.isEmpty()");
		assert	!currentExecutionType.isStandard() ||
										currentSimulationType.isNoSimulation() :
				new PreconditionException(
						"!currentExecutionType.isStandard() || "
						+ "currentSimulationType.isNoSimulation()");
		assert	currentSimulationType.isNoSimulation() ||
						(globalArchitectureURI != null &&
											!globalArchitectureURI.isEmpty()) :
				new PreconditionException(
						"currentSimulationType.isNoSimulation() || "
						+ "(globalArchitectureURI != null && "
						+ "!globalArchitectureURI.isEmpty())");
		assert	currentSimulationType.isNoSimulation() ||
						(localArchitectureURI != null && 
											!localArchitectureURI.isEmpty()) :
				new PreconditionException(
						"currentSimulationType.isNoSimulation() || "
						+ "(localArchitectureURI != null && "
						+ "!localArchitectureURI.isEmpty())");
		assert	!currentSimulationType.isSimulated() ||
													simulationTimeUnit != null :
				new PreconditionException(
						"!currentSimulationType.isSimulated() || "
						+ "simulationTimeUnit != null");
		assert	!currentSimulationType.isRealTimeSimulation() ||
															accFactor > 0.0 :
				new PreconditionException(
						"!currentSimulationType.isRealTimeSimulation() || "
						+ "accFactor > 0.0");

		this.currentExecutionType = currentExecutionType;
		this.currentSimulationType = currentSimulationType;
		this.globalArchitectureURI = globalArchitectureURI;
		this.localArchitectureURI = localArchitectureURI;
		this.simulationTimeUnit = simulationTimeUnit;
		this.accFactor = accFactor;
		this.clockURI = clockURI;

		this.initialise(hairDryerInboundPortURI);

		assert	HairDryer.glassBoxInvariants(this) :
				new ImplementationInvariantException(
						"HairDryer.glassBoxInvariants(this)");
		assert	HairDryer.blackBoxInvariants(this) :
				new InvariantException("HairDryer.blackBoxInvariants(this)");
	}

	/**
	 * initialise the hair dryer component.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code hairDryerInboundPortURI != null}
	 * pre	{@code !hairDryerInboundPortURI.isEmpty()}
	 * post	{@code getState() == HairDryerState.OFF}
	 * post	{@code getMode() == HairDryerMode.LOW}
	 * </pre>
	 * 
	 * @param hairDryerInboundPortURI	URI of the hair dryer inbound port.
	 * @throws Exception				<i>to do</i>.
	 */
	protected void		initialise(String hairDryerInboundPortURI)
	throws Exception
	{
		assert	hairDryerInboundPortURI != null :
				new PreconditionException("hairDryerInboundPortURI != null");
		assert	!hairDryerInboundPortURI.isEmpty() :
				new PreconditionException(
						"!hairDryerInboundPortURI.isEmpty()");

		this.currentState = INITIAL_STATE;
		this.currentMode = INITIAL_MODE;
		this.hdip = new HairDryerInboundPort(hairDryerInboundPortURI, this);
		this.hdip.publishPort();

		// create the simulation architecture given the current type of
		// simulation i.e., for the current execution
		switch (this.currentSimulationType) {
		case MIL_SIMULATION:
			Architecture architecture = null;
			if (this.currentExecutionType.isUnitTest()) {
				architecture =
					LocalSimulationArchitectures.
						createHairDryerMILLocalArchitecture4UnitTest(
													this.localArchitectureURI,
													this.simulationTimeUnit);
			} else {
				assert	this.currentExecutionType.isIntegrationTest();
				architecture =
						LocalSimulationArchitectures.
							createHairDryerMILArchitecture4IntegrationTest(
													this.localArchitectureURI,
													this.simulationTimeUnit);
			}
			assert	architecture.getRootModelURI().equals(
												this.localArchitectureURI) :
					new BCMException(
							"local simulation architecture "
							+ this.localArchitectureURI
							+ " does not exist!");
			this.addLocalSimulatorArchitecture(architecture);
			this.global2localSimulationArchitectureURIS.
					put(this.globalArchitectureURI, this.localArchitectureURI);
			break;
		case MIL_RT_SIMULATION:
			// in MIL RT simulations, the HairDryer component uses the same
			// simulators as in SIL simulations
		case SIL_SIMULATION:
			architecture = null;
			if (this.currentExecutionType.isUnitTest()) {
				architecture =
					LocalSimulationArchitectures.
								createHairDryerMIL_RT_Architecture4UnitTest(
									this.localArchitectureURI,
									this.simulationTimeUnit,
									this.accFactor);
			} else {
				assert	this.currentExecutionType.isIntegrationTest();
				architecture =
						LocalSimulationArchitectures.
							createHairDryerMIL_RT_Architecture4IntegrationTest(
													this.localArchitectureURI,
													this.simulationTimeUnit,
													this.accFactor);

			}
			
			assert	architecture.getRootModelURI().equals(
													this.localArchitectureURI) :
					new BCMException(
							"local simulation architecture "
							+ this.localArchitectureURI
							+ " does not exist!");
			this.addLocalSimulatorArchitecture(architecture);
			this.global2localSimulationArchitectureURIS.
					put(this.globalArchitectureURI, this.localArchitectureURI);
			break;
		default:
		}

		if (VERBOSE) {
			this.tracer.get().setTitle("Hair dryer component");
			this.tracer.get().setRelativePosition(X_RELATIVE_POSITION,
												  Y_RELATIVE_POSITION);
			this.toggleTracing();
		}
	}

	// -------------------------------------------------------------------------
	// Component life-cycle
	// -------------------------------------------------------------------------

	/**
	 * @see fr.sorbonne_u.components.AbstractComponent#start()
	 */
	@Override
	public synchronized void	start() throws ComponentStartException
	{
		super.start();

		// create the simulation plug-in given the current type of simulation
		// and its local architecture i.e., for the current execution
		try {
			switch (this.currentSimulationType) {
			case MIL_SIMULATION:
				this.asp = new AtomicSimulatorPlugin();
				String uri = this.global2localSimulationArchitectureURIS.
												get(this.globalArchitectureURI);
				Architecture architecture =
					(Architecture) this.localSimulationArchitectures.get(uri);
				this.asp.setPluginURI(uri);
				this.asp.setSimulationArchitecture(architecture);
				this.installPlugin(this.asp);
				break;
			case MIL_RT_SIMULATION:
				// for the HairDryer, real time MIL and SIL use the same
				// simulation models
				this.asp = new RTAtomicSimulatorPlugin();
				uri = this.global2localSimulationArchitectureURIS.
												get(this.globalArchitectureURI);
				architecture =
					(Architecture) this.localSimulationArchitectures.get(uri);
				((RTAtomicSimulatorPlugin)this.asp).setPluginURI(uri);
				((RTAtomicSimulatorPlugin)this.asp).
										setSimulationArchitecture(architecture);
				this.installPlugin(this.asp);
				break;
			case SIL_SIMULATION:
				// for the HairDryer, real time MIL and SIL use the same
				// simulation models
				this.asp = new RTAtomicSimulatorPlugin();
				uri = this.global2localSimulationArchitectureURIS.
												get(this.globalArchitectureURI);
				architecture =
					(Architecture) this.localSimulationArchitectures.get(uri);
				((RTAtomicSimulatorPlugin)this.asp).setPluginURI(uri);
				((RTAtomicSimulatorPlugin)this.asp).
										setSimulationArchitecture(architecture);
				this.installPlugin(this.asp);
				break;
			case NO_SIMULATION:
			default:
			}		
		} catch (Exception e) {
			throw new ComponentStartException(e) ;
		}		
	}

	/**
	 * @see fr.sorbonne_u.components.AbstractComponent#execute()
	 */
	@Override
	public void			execute() throws Exception
	{
		if (this.currentExecutionType.isUnitTest() &&
								this.currentSimulationType.isSILSimulation()) {
			// First, the component must synchronise with other components
			// to start the execution of the test scenario; we use a
			// time-triggered synchronisation scheme with the accelerated clock
			ClocksServerWithSimulationOutboundPort clocksServerOutboundPort =
					new ClocksServerWithSimulationOutboundPort(this);
			clocksServerOutboundPort.publishPort();
			this.doPortConnection(
					clocksServerOutboundPort.getPortURI(),
					ClocksServer.STANDARD_INBOUNDPORT_URI,
					ClocksServerWithSimulationConnector.class.getCanonicalName());
			this.logMessage("HairDryer gets the clock.");
			AcceleratedAndSimulationClock acceleratedClock =
				clocksServerOutboundPort.getClockWithSimulation(this.clockURI);
			this.doPortDisconnection(clocksServerOutboundPort.getPortURI());
			clocksServerOutboundPort.unpublishPort();

			this.asp.createSimulator();
			this.asp.setSimulationRunParameters(new HashMap<>());
			this.asp.initialiseSimulation(
						new Time(acceleratedClock.getSimulatedStartTime(),
								 this.simulationTimeUnit),
						new Duration(acceleratedClock.getSimulatedDuration(),
									 this.simulationTimeUnit));
			// schedule the start of the SIL (real time) simulation
			this.asp.startRTSimulation(
					TimeUnit.NANOSECONDS.toMillis(
							acceleratedClock.getSimulationStartEpochNanos()),
					acceleratedClock.getSimulatedStartTime(),
					acceleratedClock.getSimulatedDuration());
			// wait until the simulation ends
			acceleratedClock.waitUntilSimulationEnd();
			// give some time for the end of simulation catering tasks
			Thread.sleep(200L);
			this.logMessage(this.asp.getFinalReport().toString());
		}
	}

	/**
	 * @see fr.sorbonne_u.components.AbstractComponent#shutdown()
	 */
	@Override
	public synchronized void	shutdown() throws ComponentShutdownException
	{
		try {
			this.hdip.unpublishPort();
		} catch (Exception e) {
			throw new ComponentShutdownException(e) ;
		}
		super.shutdown();
	}

	// -------------------------------------------------------------------------
	// Component services implementation
	// -------------------------------------------------------------------------

	/**
	 * @see fr.sorbonne_u.components.hem2024e1.equipments.hairdryer.HairDryerImplementationI#getState()
	 */
	@Override
	public HairDryerState	getState() throws Exception
	{
		if (HairDryer.VERBOSE) {
			this.traceMessage("Hair dryer returns its state : " +
													this.currentState + ".\n");
		}

		return this.currentState;
	}

	/**
	 * @see fr.sorbonne_u.components.hem2024e1.equipments.hairdryer.HairDryerImplementationI#getMode()
	 */
	@Override
	public HairDryerMode	getMode() throws Exception
	{
		if (HairDryer.VERBOSE) {
			this.traceMessage("Hair dryer returns its mode : " +
													this.currentMode + ".\n");
		}

		return this.currentMode;
	}

	/**
	 * @see fr.sorbonne_u.components.hem2024e1.equipments.hairdryer.HairDryerImplementationI#turnOn()
	 */
	@Override
	public void			turnOn() throws Exception
	{
		assert	this.getState() == HairDryerState.OFF :
				new PreconditionException("getState() == HairDryerState.OFF");

		if (HairDryer.VERBOSE) {
			this.traceMessage("Hair dryer is turned on.\n");
		}

		this.currentState = HairDryerState.ON;
		this.currentMode = HairDryerMode.LOW;

		assert	this.getState() == HairDryerState.ON :
				new PostconditionException("getState() == HairDryerState.ON");

		if (this.currentSimulationType.isSILSimulation()) {
			// For SIL simulation, an operation done in the component code
			// must be reflected in the simulation; to do so, the component
			// code triggers an external event sent to the HairDryerStateModel
			// to make it change its state to on.
			((RTAtomicSimulatorPlugin)this.asp).triggerExternalEvent(
												HairDryerStateModel.SIL_URI,
												t -> new SwitchOnHairDryer(t));
		}
	}

	/**
	 * @see fr.sorbonne_u.components.hem2024e1.equipments.hairdryer.HairDryerImplementationI#turnOff()
	 */
	@Override
	public void			turnOff() throws Exception
	{
		assert	this.getState() == HairDryerState.ON :
				new PreconditionException("getState() == HairDryerState.ON");

		if (HairDryer.VERBOSE) {
			this.traceMessage("Hair dryer is turned off.\n");
		}

		this.currentState = HairDryerState.OFF;
		this.currentMode = HairDryerMode.LOW;

		assert	this.getState() == HairDryerState.OFF :
				new PostconditionException("getState() == HairDryerState.OFF");

		if (this.currentSimulationType.isSILSimulation()) {
			// For SIL simulation, an operation done in the component code
			// must be reflected in the simulation; to do so, the component
			// code triggers an external event sent to the HairDryerStateModel
			// to make it change its state to off.
			((RTAtomicSimulatorPlugin)this.asp).triggerExternalEvent(
												HairDryerStateModel.SIL_URI,
												t -> new SwitchOffHairDryer(t));
		}
	}

	/**
	 * @see fr.sorbonne_u.components.hem2024e1.equipments.hairdryer.HairDryerImplementationI#setHigh()
	 */
	@Override
	public void			setHigh() throws Exception
	{
		assert	this.getState() == HairDryerState.ON :
				new PreconditionException("getState() == HairDryerState.ON");
		assert	this.getMode() == HairDryerMode.LOW :
				new PreconditionException("getMode() == HairDryerMode.LOW");

		if (HairDryer.VERBOSE) {
			this.traceMessage("Hair dryer is set high.\n");
		}

		this.currentMode = HairDryerMode.HIGH;

		assert	this.getState() == HairDryerState.ON :
				new PostconditionException("getState() == HairDryerState.ON");
		assert	this.getMode() == HairDryerMode.HIGH :
				new PostconditionException("getMode() == HairDryerMode.HIGH");

		if (this.currentSimulationType.isSILSimulation()) {
			// For SIL simulation, an operation done in the component code
			// must be reflected in the simulation; to do so, the component
			// code triggers an external event sent to the HairDryerStateModel
			// to make it change its mode to high.
			((RTAtomicSimulatorPlugin)this.asp).triggerExternalEvent(
												HairDryerStateModel.SIL_URI,
												t -> new SetHighHairDryer(t));
		}
	}

	/**
	 * @see fr.sorbonne_u.components.hem2024e1.equipments.hairdryer.HairDryerImplementationI#setLow()
	 */
	@Override
	public void			setLow() throws Exception
	{
		assert	this.getState() == HairDryerState.ON :
				new PreconditionException("getState() == HairDryerState.ON");
		assert	this.getMode() == HairDryerMode.HIGH :
				new PreconditionException("getMode() == HairDryerMode.HIGH");

		if (HairDryer.VERBOSE) {
			this.traceMessage("Hair dryer is set low.\n");
		}

		this.currentMode = HairDryerMode.LOW;

		assert	this.getState() == HairDryerState.ON :
				new PostconditionException("getState() == HairDryerState.ON");
		assert	this.getMode() == HairDryerMode.LOW :
				new PostconditionException("getMode() == HairDryerMode.LOW");

		if (this.currentSimulationType.isSILSimulation()) {
			// For SIL simulation, an operation done in the component code
			// must be reflected in the simulation; to do so, the component
			// code triggers an external event sent to the HairDryerStateModel
			// to make it change its mode to low.
			((RTAtomicSimulatorPlugin)this.asp).triggerExternalEvent(
												HairDryerStateModel.SIL_URI,
												t -> new SetLowHairDryer(t));
		}
	}
}
// -----------------------------------------------------------------------------
