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
import fr.sorbonne_u.devs_simulation.architectures.Architecture;
import fr.sorbonne_u.exceptions.ImplementationInvariantException;
import fr.sorbonne_u.exceptions.InvariantChecking;
import fr.sorbonne_u.exceptions.InvariantException;
import fr.sorbonne_u.exceptions.PreconditionException;
import fr.sorbonne_u.utils.aclocks.ClocksServer;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.time.Instant;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import fr.sorbonne_u.components.equipments.hairdryer.connections.*;
import fr.sorbonne_u.components.equipments.hairdryer.connections.HairDryerImplementationI.HairDryerMode;
import fr.sorbonne_u.components.equipments.hairdryer.connections.HairDryerImplementationI.HairDryerState;
import fr.sorbonne_u.components.equipments.hairdryer.mil.HairDryerOperationI;
import fr.sorbonne_u.components.equipments.hairdryer.mil.LocalSimulationArchitectures;
// -----------------------------------------------------------------------------
/**
 * The class <code>HairDryerUser</code> implements a component performing
 * tests for the class <code>HairDryer</code> as a BCM component.
 *
 * <p><strong>Description</strong></p>
 * 
 * <p>
 * The idea behind this component is to replace the user of a hair dryer to
 * perform actions that he/she would do using the "interface" (buttons) of
 * the actual appliance. Therefore, the component is meant to implement
 * scenarios to test the hair dryer implementation but also to simulate hair
 * dryer users in a more global integration tests and simulations to evaluate
 * and tune the entire HEM application.
 * </p>
 * <p>
 * This implementation of {@code HairDryerUser} is complicated by the objective
 * to show the entire spectrum of possible execution and simulation modes. There
 * are three execution types defined by {@code ExecutionType}:
 * </p>
 * <ol>
 * <li>{@code STANDARD} has no interest for {@code HairDryerUser} as in the real
 *   world, a human user would perform its actions, hence this component would
 *   not be deployed in a standard execution. Using this execution type will
 *   trigger a precondition exception.</li>
 * <li>{@code UNIT_TEST} means that the component executes in unit tests where
 *   it is the sole appliance but cooperates with the {@code HairDryer}
 *   component.</li>
 * <li>{@code INTEGRATION_TEST} means that the component executes in integration
 *   tests where other appliances coexist.</li>
 * </ol>
 * <p>
 * There are also four distinct types of simulations defined by
 * {@code SimulationType}:
 * </p>
 * <ol>
 * <li>{@code NO_SIMULATION} means that the component does not execute a
 *   simulator, a type necessarily used in {@code UNIT_TEST}.</li>
 * <li>{@code MIL_SIMULATION} means that only MIL simulators will run; it is
 *   meant to be used early stages of a project in {@code UNIT_TEST} and
 *   {@code INTEGRATION_TEST} before implementing the code of the components.
 *   </li>
 * <li>{@code MIL_RT_SIMULATION} is similar to {@code MIL_SIMULATION} but
 *   simulates in real time or an accelerated real time; it is more a step
 *   towards SIL simulations than an actual interesting simulation type
 *   in an industrial project.</li>
 * <li>{@code SIL_SIMULATION} has a particular interpretation for
 *   {@code HairDryerUser} as rather than using a simulator, it can directly
 *   implement test scenarios in its component code, scheduling actions as tasks
 *   at predefined instants to cooperate indirectly with the simulator in the
 *   {@code HairDryer} component through the latter code. When using this
 *   simulation, the {@code HairDryerUser} component executes only its
 *   code but contrary to unit tests without simulation, the test scenarios
 *   schedule its actions in the same time line as the one of the
 *   {@code HairDryer} SIL simulation.</li>
 * </ol>
 * <p>
 * </p>
 * 
 * <p><strong>Glass-box Invariants</strong></p>
 * 
 * <pre>
 * invariant	{@code REFLECTION_INBOUND_PORT_URI != null && !REFLECTION_INBOUND_PORT_URI.isEmpty()}
 * invariant	{@code X_RELATIVE_POSITION >= 0}
 * invariant	{@code Y_RELATIVE_POSITION >= 0}
 * invariant	{@code hairDryerInboundPortURI != null && !hairDryerInboundPortURI.isEmpty()}
 * invariant	{@code currentExecutionType != null}
 * invariant	{@code currentSimulationType != null}
 * invariant	{@code !currentExecutionType.isStandard() || currentSimulationType.isNoSimulation()}
 * invariant	{@code currentSimulationType.isNoSimulation() || (globalArchitectureURI != null && !globalArchitectureURI.isEmpty())}
 * invariant	{@code currentSimulationType.isNoSimulation() || (localArchitectureURI != null && !localArchitectureURI.isEmpty())}
 * invariant	{@code !currentSimulationType.isSILSimulation() || accFactor > 0.0}
 * </pre>
 * 
 * <p><strong>Black-box Invariants</strong></p>
 * 
 * <pre>
 * invariant	{@code true}	// no more invariant
 * </pre>
 * 
 * <p>Created on : 2023-09-19</p>
 * 
 * @author	<a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
 */
@RequiredInterfaces(required = {HairDryerUserCI.class,
								ClocksServerWithSimulationCI.class})
public class			HairDryerUser
extends		AbstractCyPhyComponent
implements	HairDryerOperationI
{
	// -------------------------------------------------------------------------
	// Constants and variables
	// -------------------------------------------------------------------------

	/** when true, operations are traced.									*/
	public static boolean				VERBOSE = false ;
	/** when tracing, x coordinate of the window relative position.			*/
	public static int					X_RELATIVE_POSITION = 0;
	/** when tracing, y coordinate of the window relative position.			*/
	public static int					Y_RELATIVE_POSITION = 0;

	/** standard reflection, inbound port URI for the {@code HairDryerUser}
	 *  component.															*/
	public static final String			REFLECTION_INBOUND_PORT_URI =
													"HAIR-DRYER-USER-RIP-URI";

	/** outbound port to connect to the {@code HairDryer} component.		*/
	protected HairDryerOutboundPort		hdop;
	/** service inbound port URI of the {@code HairDryer} component.		*/
	protected String					hairDryerInboundPortURI;

	// Execution/Simulation

	/** current type of execution.											*/
	protected final ExecutionType		currentExecutionType;
	/** current type of simulation.											*/
	protected final SimulationType		currentSimulationType;

	/** URI of the clock to be used to synchronise the test scenarios and
	 *  the simulation.														*/
	protected final String				clockURI;
	/** URI of the global simulation architecture to be created or the
	 *  empty string if the component does not execute as a simulation.		*/
	protected final String				globalArchitectureURI;
	/** URI of the local simulation architecture used to compose the global
	 *  simulation architecture or the empty string if the component does
	 *  not execute as a simulation.										*/
	protected final String				localArchitectureURI;
	/** time unit in which simulated times and durations are expressed.		*/
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
	protected static boolean	glassBoxInvariants(HairDryerUser hd)
	{
		assert 	hd != null : new PreconditionException("hd != null");

		boolean ret = true;
		ret &= InvariantChecking.checkGlassBoxInvariant(
					REFLECTION_INBOUND_PORT_URI != null &&
									!REFLECTION_INBOUND_PORT_URI.isEmpty(),
					HairDryerUser.class, hd,
					"REFLECTION_INBOUND_PORT_URI != null && "
								+ "!REFLECTION_INBOUND_PORT_URI.isEmpty()");
		ret &= InvariantChecking.checkGlassBoxInvariant(
					X_RELATIVE_POSITION >= 0,
					HairDryerUser.class, hd,
					"X_RELATIVE_POSITION >= 0");
		ret &= InvariantChecking.checkGlassBoxInvariant(
					Y_RELATIVE_POSITION >= 0,
					HairDryerUser.class, hd,
					"Y_RELATIVE_POSITION >= 0");		
		ret &= InvariantChecking.checkGlassBoxInvariant(
					hd.currentExecutionType != null,
					HairDryerUser.class, hd,
					"currentExecutionType != null");
		ret &= InvariantChecking.checkGlassBoxInvariant(
					hd.currentSimulationType != null,
					HairDryerUser.class, hd,
					"hcurrentSimulationType != null");
		ret &= InvariantChecking.checkGlassBoxInvariant(
					!hd.currentExecutionType.isStandard() ||
								hd.currentSimulationType.isNoSimulation(),
					HairDryerUser.class, hd,
					"!currentExecutionType.isStandard() || "
					+ "currentSimulationType.isNoSimulation()");
		ret &= InvariantChecking.checkGlassBoxInvariant(
					hd.currentSimulationType.isNoSimulation() ||
							(hd.globalArchitectureURI != null &&
										!hd.globalArchitectureURI.isEmpty()),
					HairDryerUser.class, hd,
					"currentSimulationType.isNoSimulation() || "
					+ "(globalArchitectureURI != null && "
					+ "!globalArchitectureURI.isEmpty())");
		ret &= InvariantChecking.checkGlassBoxInvariant(
					hd.currentSimulationType.isNoSimulation() ||
							(hd.localArchitectureURI != null &&
										!hd.localArchitectureURI.isEmpty()),
					HairDryerUser.class, hd,
					"currentSimulationType.isNoSimulation() || "
					+ "(localArchitectureURI != null && "
					+ "!localArchitectureURI.isEmpty())");
		ret &= InvariantChecking.checkGlassBoxInvariant(
					!hd.currentSimulationType.isSILSimulation() ||
														hd.accFactor > 0.0,
					HairDryerUser.class, hd,
					"!hd.currentSimulationType.isSILSimulation() || "
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
	protected static boolean	blackBoxInvariants(HairDryerUser hd)
	{
		assert 	hd != null : new PreconditionException("hd != null");

		boolean ret = true;
		return ret;
	}

	// -------------------------------------------------------------------------
	// Constructors
	// -------------------------------------------------------------------------

	/**
	 * create a hair dryer component with the standard URIs and executions.
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
	protected			HairDryerUser() throws Exception
	{
		this(HairDryer.INBOUND_PORT_URI);
	}

	/**
	 * create a hair dryer user component for standard executions (without
	 * simulation).
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code hairDryerInboundPortURI != null}
	 * pre	{@code !hairDryerInboundPortURI.isEmpty()}
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @param hairDryerInboundPortURI	URI of the hair dryer inbound port.
	 * @throws Exception				<i>to do</i>.
	 */
	protected			HairDryerUser(String hairDryerInboundPortURI) 
	throws Exception
	{
		this(hairDryerInboundPortURI, ExecutionType.STANDARD);
	}

	/**
	 * create a hair dryer user component for test executions without simulation.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code currentExecutionType.isStandard() || currentExecutionType.isIntegrationTest()}
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @param hairDryerInboundPortURI	URI of the hair dryer inbound port.
	 * @param currentExecutionType		execution type for the next run.
	 * @throws Exception				<i>to do</i>.
	 */
	protected			HairDryerUser(
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
	 * create a hair dryer user component for test executions with simulation.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code hairDryerInboundPortURI != null && !hairDryerInboundPortURI.isEmpty()}
	 * pre	{@code currentExecutionType != null}
	 * pre	{@code !currentExecutionType.isStandard() || currentSimulationType.isNoSimulation()}
	 * pre	{@code currentSimulationType.isNoSimulation() || (globalArchitectureURI != null && !globalArchitectureURI.isEmpty())}
	 * pre	{@code currentSimulationType.isNoSimulation() || (localArchitectureURI != null && !localArchitectureURI.isEmpty())}
	 * pre	{@code !currentSimulationType.isSILSimulation() || accFactor > 0.0}
	 * </pre>
	 *
	 * @param reflectionInboundPortURI	URI of the reflection inbound port of the component.
	 * @param hairDryerInboundPortURI	URI of the hair dryer inbound port.
	 * @param currentExecutionType		execution type for the next run.
	 * @param currentSimulationType		simulation type for the next run.
	 * @param globalArchitectureURI		URI of the global simulation architecture to be created or the empty string if the component does not execute as a simulation.
	 * @param localArchitectureURI		URI of the local simulation architecture to be used in composing the global simulation architecture.
	 * @param simulationTimeUnit		time unit in which {@code simulatedStartTime} and {@code simulationDuration} are expressed.
	 * @param accFactor					acceleration factor for the simulation.
	 * @throws Exception				<i>to do</i>.
	 */
	protected			HairDryerUser(
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
		// one thread for execute and one schedulable for SIL scenarios
		super(reflectionInboundPortURI, 1, 1);

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
						"currentSimulationType.isNoSimulation() ||  "
						+ "(globalArchitectureURI != null && "
						+ "!globalArchitectureURI.isEmpty())");
		assert	currentSimulationType.isNoSimulation() ||
								(localArchitectureURI != null &&
											!localArchitectureURI.isEmpty()) :
				new PreconditionException(
						"currentSimulationType.isNoSimulation() ||  "
						+ "(localArchitectureURI != null && "
						+ "!localArchitectureURI.isEmpty())");
		assert	!currentSimulationType.isSILSimulation() || accFactor > 0.0 :
				new PreconditionException(
						"!currentSimulationType.isSILSimulation() || "
						+ "accFactor > 0.0");

		if (currentExecutionType.isStandard()) {
			throw new RuntimeException(
					"HairDryerUser: standard execution is not implemented.");
		}

		this.currentExecutionType = currentExecutionType;
		this.currentSimulationType = currentSimulationType;
		this.globalArchitectureURI = globalArchitectureURI;
		this.localArchitectureURI = localArchitectureURI;
		this.simulationTimeUnit = simulationTimeUnit;
		this.accFactor = accFactor;
		this.clockURI = clockURI;

		this.initialise(hairDryerInboundPortURI);

		assert	HairDryerUser.glassBoxInvariants(this) :
				new ImplementationInvariantException(
						"HairDryerUser.glassBoxInvariants(this)");
		assert	HairDryerUser.blackBoxInvariants(this) :
				new InvariantException("HairDryerUser.blackBoxInvariants(this)");
	}

	/**
	 * initialise the component.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code true}	// no precondition.
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @param hairDryerInboundPortURI	URI of the hair dryer inbound port.
	 * @throws Exception				<i>to do</i>.
	 */
	protected void		initialise(
		String hairDryerInboundPortURI
		) throws Exception
	{
		this.hairDryerInboundPortURI = hairDryerInboundPortURI;
		this.hdop = new HairDryerOutboundPort(this);
		this.hdop.publishPort();

		// create the local simulation architecture given the type of
		// simulation for the current run
		switch (this.currentSimulationType) {
		case MIL_SIMULATION:
			Architecture architecture =
					LocalSimulationArchitectures.
						createHairDryerUserMIL_Architecture(
											this.localArchitectureURI,
											this.simulationTimeUnit);
			assert	architecture.getRootModelURI().
										equals(this.localArchitectureURI) :
					new AssertionError(
							"local simulation architecture "
							+ this.localArchitectureURI
							+ " does not exist!");
			this.addLocalSimulatorArchitecture(architecture);
			this.global2localSimulationArchitectureURIS.
					put(this.globalArchitectureURI, this.localArchitectureURI);
			break;
		case MIL_RT_SIMULATION:
			architecture =
					LocalSimulationArchitectures.
						createHairDryerUserMIL_RT_Architecture(
											this.localArchitectureURI,
											this.simulationTimeUnit,
											this.accFactor);
			assert	architecture.getRootModelURI().equals(
												this.localArchitectureURI) :
					new BCMException(
							"local simulator " + this.localArchitectureURI
							+ " does not exist!");
			this.addLocalSimulatorArchitecture(architecture);
			this.global2localSimulationArchitectureURIS.
					put(this.globalArchitectureURI, this.localArchitectureURI);
			break;
		case SIL_SIMULATION:
		case NO_SIMULATION:
		default:
			// in SIL simulations, the HairDryerUser component do not use
			// simulators, only global clock driven actions
		}		

		if (VERBOSE) {
			this.tracer.get().setTitle("Hair dryer user component");
			this.tracer.get().setRelativePosition(X_RELATIVE_POSITION,
												  Y_RELATIVE_POSITION);
			this.toggleTracing();
		}
	}

	// -------------------------------------------------------------------------
	// Component internal testing method triggered by the SIL simulator
	// -------------------------------------------------------------------------

	/**
	 * @see fr.sorbonne_u.components.hem2024e3.equipments.hairdryer.mil.HairDryerOperationI#turnOn()
	 */
	@Override
	public void			turnOn()
	{
		if (VERBOSE) {
			this.logMessage("HairDryerUser#turnOn().");
		}
		try {
			this.hdop.turnOn();
		} catch (Exception e) {
			throw new RuntimeException(e) ;
		}
	}

	/**
	 * @see fr.sorbonne_u.components.hem2024e3.equipments.hairdryer.mil.HairDryerOperationI#turnOff()
	 */
	@Override
	public void			turnOff()
	{
		if (VERBOSE) {
			this.logMessage("HairDryerUser#turnOff().");
		}
		try {
			this.hdop.turnOff();
		} catch (Exception e) {
			throw new RuntimeException(e) ;
		}
	}

	/**
	 * @see fr.sorbonne_u.components.hem2024e3.equipments.hairdryer.mil.HairDryerOperationI#setHigh()
	 */
	@Override
	public void			setHigh()
	{
		if (VERBOSE) {
			this.logMessage("HairDryerUser#setHigh().");
		}
		try {
			this.hdop.setHigh();
		} catch (Exception e) {
			throw new RuntimeException(e) ;
		}
	}

	/**
	 * @see fr.sorbonne_u.components.hem2024e3.equipments.hairdryer.mil.HairDryerOperationI#setLow()
	 */
	@Override
	public void			setLow()
	{
		if (VERBOSE) {
			this.logMessage("HairDryerUser#setLow().");
		}
		try {
			this.hdop.setLow();
		} catch (Exception e) {
			throw new RuntimeException(e) ;
		}
	}

	// -------------------------------------------------------------------------
	// Component internal tests
	// -------------------------------------------------------------------------

	public void			testGetState()
	{
		this.logMessage("testGetState()... ");
		try {
			assertEquals(HairDryerState.OFF, this.hdop.getState());
		} catch (Exception e) {
			this.logMessage("...KO.");
			assertTrue(false);
		}
		this.logMessage("...done.");
	}

	public void			testGetMode()
	{
		this.logMessage("testGetMode()... ");
		try {
			assertEquals(HairDryerMode.LOW, this.hdop.getMode());
		} catch (Exception e) {
			assertTrue(false);
		}
		this.logMessage("...done.");
	}

	public void			testTurnOnOff()
	{
		this.logMessage("testTurnOnOff()... ");
		try {
			assertEquals(HairDryerState.OFF, this.hdop.getState());
			this.hdop.turnOn();
			assertEquals(HairDryerState.ON, this.hdop.getState());
			assertEquals(HairDryerMode.LOW, this.hdop.getMode());
		} catch (Exception e) {
			assertTrue(false);
		}
		try {
			assertThrows(ExecutionException.class,
						 () -> this.hdop.turnOn());
		} catch (Exception e) {
			assertTrue(false);
		}
		try {
			this.hdop.turnOff();
			assertEquals(HairDryerState.OFF, this.hdop.getState());
		} catch (Exception e) {
			assertTrue(false);
		}
		try {
			assertThrows(ExecutionException.class,
						 () -> this.hdop.turnOff());
		} catch (Exception e) {
			assertTrue(false);
		}
		this.logMessage("...done.");
	}

	public void			testSetLowHigh()
	{
		this.logMessage("testSetLowHigh()... ");
		try {
			this.hdop.turnOn();
			this.hdop.setHigh();
			assertEquals(HairDryerState.ON, this.hdop.getState());
			assertEquals(HairDryerMode.HIGH, this.hdop.getMode());
		} catch (Exception e) {
			assertTrue(false);
		}
		try {
			assertThrows(ExecutionException.class,
						 () -> this.hdop.setHigh());
		} catch (Exception e) {
			assertTrue(false);
		}
		try {
			this.hdop.setLow();
			assertEquals(HairDryerState.ON, this.hdop.getState());
			assertEquals(HairDryerMode.LOW, this.hdop.getMode());
		} catch (Exception e) {
			assertTrue(false);
		}
		try {
			assertThrows(ExecutionException.class,
						 () -> this.hdop.setLow());
		} catch (Exception e) {
			assertTrue(false);
		}
		try {
			this.hdop.turnOff();
		} catch (Exception e) {
			assertTrue(false);
		}
		this.logMessage("...done.");
	}

	protected void			runAllTests()
	{
		this.testGetState();
		this.testGetMode();
		this.testTurnOnOff();
		this.testSetLowHigh();
	}

	// -------------------------------------------------------------------------
	// Component life-cycle
	// -------------------------------------------------------------------------

	/**
	 * @see fr.sorbonne_u.components.AbstractComponent#start()
	 */
	@Override
	public synchronized void	start()
	throws ComponentStartException
	{
		super.start();

		
		try {
			this.doPortConnection(
					this.hdop.getPortURI(),
					this.hairDryerInboundPortURI,
					HairDryerConnector.class.getCanonicalName());

			switch (this.currentSimulationType) {
			case MIL_SIMULATION:
				AtomicSimulatorPlugin asp = new AtomicSimulatorPlugin();
				String uri = this.global2localSimulationArchitectureURIS.
												get(this.globalArchitectureURI);
				Architecture architecture =
					(Architecture) this.localSimulationArchitectures.get(uri);
				asp.setPluginURI(uri);
				asp.setSimulationArchitecture(architecture);
				this.installPlugin(asp);
				break;
			case MIL_RT_SIMULATION:
				RTAtomicSimulatorPlugin rtasp = new RTAtomicSimulatorPlugin();
				uri = this.global2localSimulationArchitectureURIS.
											get(this.globalArchitectureURI);
				architecture =
					(Architecture) this.localSimulationArchitectures.get(uri);
				rtasp.setPluginURI(uri);
				rtasp.setSimulationArchitecture(architecture);
				this.installPlugin(rtasp);
				break;
			case SIL_SIMULATION:
				// in SIL simulations, the HairDryerUser component do not use
				// simulators, only global clock driven actions
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
	public synchronized void execute() throws Exception
	{
		this.logMessage("HairDryerUser executes.");
		if (this.currentExecutionType.isTest() &&
				(this.currentSimulationType.isNoSimulation() ||
							this.currentSimulationType.isSILSimulation())) {
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
			this.logMessage("HairDryerUser gets the clock.");
			AcceleratedAndSimulationClock acceleratedClock =
				clocksServerOutboundPort.getClockWithSimulation(this.clockURI);
			this.doPortDisconnection(clocksServerOutboundPort.getPortURI());
			clocksServerOutboundPort.unpublishPort();

			// Now, wait for the other components synchronising on the
			// accelerated clock
			this.logMessage("HairDryerUser waits until start time.");
			acceleratedClock.waitUntilStart();
			this.logMessage("HairDryerUser starts.");

			if (this.currentSimulationType.isNoSimulation()) {
				// In test execution types with no simulation, the component
				// executes a series of calls to the hair dryer to test all of
				// its methods.
				this.logMessage("HairDryerUser tests begin without simulation.");
				this.runAllTests();
				this.logMessage("HairDryerUser tests end.");
			} else {
				// synchronise with the start of the SIL simulation
				acceleratedClock.waitUntilSimulationStart();
				// execute the SIL test scenario
				silTestScenario(acceleratedClock);
			}
		}
	}

	/**
	 * @see fr.sorbonne_u.components.AbstractComponent#finalise()
	 */
	@Override
	public synchronized void	finalise() throws Exception
	{
		this.doPortDisconnection(this.hdop.getPortURI());

		super.finalise();
	}

	/**
	 * @see fr.sorbonne_u.components.AbstractComponent#shutdown()
	 */
	@Override
	public synchronized void	shutdown() throws ComponentShutdownException
	{
		try {
			this.hdop.unpublishPort();
		} catch (Exception e) {
			throw new ComponentShutdownException(e) ;
		}
		super.shutdown();
	}

	// -------------------------------------------------------------------------
	// SIL test scenarios
	// -------------------------------------------------------------------------

	/**
	 * this method implements a time-triggered test scenario that aims to
	 * simulate the actions of a hair dryer user (or a group of hair dryer
	 * users).
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code true}	// no precondition.
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 */
	protected void			silTestScenario(
		AcceleratedAndSimulationClock acceleratedClock
		)
	{
		assert	!acceleratedClock.simulationStartTimeNotReached() :
				new BCMException("!acceleratedClock.startTimeNotReached()");

		// Define the instants of the different actions in the scenario.
		Instant simulationStartInstant =
								acceleratedClock.getSimulationStartInstant();
		Instant switchOn = simulationStartInstant.plusSeconds(3600L);
		Instant setHigh = simulationStartInstant.plusSeconds(4500L);
		Instant setLow = simulationStartInstant.plusSeconds(6300L);
		Instant switchOff = simulationStartInstant.plusSeconds(7200L);

		// For each action, compute the waiting time for this action using the
		// above instant and the clock, and then schedule the rask that will
		// perform the action at the appropriate time.
		long delayInNanos = acceleratedClock.nanoDelayUntilInstant(switchOn);
		this.logMessage(
				"HairDryer#silTestScenario waits for " + delayInNanos
				+ " " + TimeUnit.NANOSECONDS + " i.e., "
				+ TimeUnit.NANOSECONDS.toMillis(delayInNanos)
												+ " " + TimeUnit.MILLISECONDS
				+ " to reach " + switchOn);
		this.scheduleTask(
				o -> { logMessage("HairDryerUser SIL test scenario begins.");
					   ((HairDryerUser)o).turnOn();
					 },
				delayInNanos, TimeUnit.NANOSECONDS);
		delayInNanos = acceleratedClock.nanoDelayUntilInstant(setHigh);
		this.logMessage(
				"HairDryer#silTestScenario waits for " + delayInNanos
				+ " " + TimeUnit.NANOSECONDS + " i.e., "
				+ TimeUnit.NANOSECONDS.toMillis(delayInNanos)
												+ " " + TimeUnit.MILLISECONDS
				+ " to reach " + setHigh);
		this.scheduleTask(
				o -> ((HairDryerUser)o).setHigh(),
				delayInNanos, TimeUnit.NANOSECONDS);
		delayInNanos = acceleratedClock.nanoDelayUntilInstant(setLow);
		this.logMessage(
				"HairDryer#silTestScenario waits for " + delayInNanos
				+ " " + TimeUnit.NANOSECONDS + " i.e., "
				+ TimeUnit.NANOSECONDS.toMillis(delayInNanos)
												+ " " + TimeUnit.MILLISECONDS
				+ " to reach " + setLow);
		this.scheduleTask(
				o -> ((HairDryerUser)o).setLow(),
				delayInNanos, TimeUnit.NANOSECONDS);
		delayInNanos = acceleratedClock.nanoDelayUntilInstant(switchOff);
		this.logMessage(
				"HairDryer#silTestScenario waits for " + delayInNanos
				+ " " + TimeUnit.NANOSECONDS + " i.e., "
				+ TimeUnit.NANOSECONDS.toMillis(delayInNanos)
												+ " " + TimeUnit.MILLISECONDS
				+ " to reach " + switchOff);
		this.scheduleTask(
				o -> { ((HairDryerUser)o).turnOff() ;
					   logMessage("HairDryerUser SIL test scenario ends.");
					 },
				delayInNanos, TimeUnit.NANOSECONDS);
	}
}
// -----------------------------------------------------------------------------
