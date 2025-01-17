package fr.sorbonne_u.components.equipments.heater;

// Copyright Jacques Malenfant, Sorbonne Universite.
// Jacques.Malenfant@lip6.fr
//
// This software is a computer program whose purpose is to provide a basic
// household management systems as an example of a cyber-physical system.
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

import fr.sorbonne_u.components.AbstractComponent;
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
import fr.sorbonne_u.utils.aclocks.AcceleratedClock;
import fr.sorbonne_u.utils.aclocks.ClocksServer;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.time.Instant;
import java.util.concurrent.TimeUnit;
import fr.sorbonne_u.components.equipments.heater.connections.*;
import fr.sorbonne_u.components.equipments.heater.mil.LocalSimulationArchitectures;

// -----------------------------------------------------------------------------
/**
 * The class <code>HeaterTester</code> implements a component performing 
 * tests for the class <code>Heater</code> as a BCM component.
 *
 * <p><strong>Description</strong></p>
 * 
 * <p>
 * In unit test runs, the component performs a series of tests on the heater
 * component. In integration test and SIL simulation runs, it simply switch
 * on and off the heater, leaving to the other components (the heater controller
 * and the HEM) the responsibility to test the heater component services and
 * internal behaviour.
 * </p>
 *
 * <p><strong>Invariant</strong></p>
 * 
 * <pre>
 * invariant	{@code heaterUserInboundPortURI != null && !heaterUserInboundPortURI.isEmpty()}
 * invariant	{@code heaterInternalControlInboundPortURI != null && !heaterInternalControlInboundPortURI.isEmpty()}
 * invariant	{@code heaterExternalControlInboundPortURI != null && !heaterExternalControlInboundPortURI.isEmpty()}
 * invariant	{@code currentExecutionType != null}
 * invariant	{@code currentSimulationType != null}
 * invariant	{@code !currentExecutionType.isStandard() || currentSimulationType.isNoSimulation()}
 * invariant	{@code currentSimulationType.isNoSimulation() || (globalArchitectureURI != null && !globalArchitectureURI.isEmpty())}
 * invariant	{@code currentSimulationType.isNoSimulation() || (localArchitectureURI != null && !localArchitectureURI.isEmpty())}
 * invariant	{@code !currentSimulationType.isSimulated() || simulationTimeUnit != null}
 * invariant	{@code !currentSimulationType.isRealTimeSimulation() || accFactor > 0.0}
 * </pre>
 * 
 * <p><strong>Black-box Invariant</strong></p>
 * 
 * <pre>
 * invariant	{@code X_RELATIVE_POSITION >= 0}
 * invariant	{@code Y_RELATIVE_POSITION >= 0}
 * </pre>
 * 
 * <p>Created on : 2021-09-13</p>
 * 
 * @author	<a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
 */
@RequiredInterfaces(required={HeaterUserCI.class,
							  HeaterInternalControlCI.class,
							  HeaterExternalControlCI.class,
							  ClocksServerWithSimulationCI.class})
public class			HeaterUser
extends		AbstractCyPhyComponent
{
	// -------------------------------------------------------------------------
	// Constants and variables
	// -------------------------------------------------------------------------

	/** URI of the heater inbound port used in tests.						*/
	public static final String	REFLECTION_INBOUND_PORT_URI =
														"HeaterUser-RIP-URI";	

	/** when true, methods trace their actions.								*/
	public static boolean		VERBOSE = false;
	/** when tracing, x coordinate of the window relative position.			*/
	public static int			X_RELATIVE_POSITION = 0;
	/** when tracing, y coordinate of the window relative position.			*/
	public static int			Y_RELATIVE_POSITION = 0;

	/** URI of the user component interface inbound port.					*/
	protected String			heaterUserInboundPortURI;
	/** URI of the internal control component interface inbound port.		*/
	protected String			heaterInternalControlInboundPortURI;
	/** URI of the external control component interface inbound port.		*/
	protected String			heaterExternalControlInboundPortURI;

	/** user component interface inbound port.								*/
	protected HeaterUserOutboundPort			hop;
	/** internal control component interface inbound port.					*/
	protected HeaterInternalControlOutboundPort	hicop;
	/** external control component interface inbound port.					*/
	protected HeaterExternalControlOutboundPort	hecop;
	/** a sensor outbound port to show a well structured sensor interface.	*/

	// Execution/Simulation

	/** current type of execution.											*/
	protected final ExecutionType		currentExecutionType;
	/** current type of simulation.											*/
	protected final SimulationType		currentSimulationType;

	/** plug-in holding the local simulation architecture and simulators.	*/
	protected AtomicSimulatorPlugin		asp;
	/** URI of the global simulation architecture to be created or the empty
	 *  string if the component does not execute as a simulation.			*/
	protected final String				globalArchitectureURI;
	/** URI of the local simulation architecture used to compose the global
	 *  simulation architecture.											*/
	protected final String				localArchitectureURI;
	/** time unit in which {@code simulatedStartTime} and
	 *  {@code simulationDuration} are expressed.							*/
	protected final TimeUnit			simulationTimeUnit;
	/** acceleration factor to be used when running the real time
	 *  simulation.															*/
	protected double					accFactor;

	/** URI of the clock to be used to synchronise the test scenarios and
	 *  the simulation.														*/
	protected final String				clockURI;

	// -------------------------------------------------------------------------
	// Invariants
	// -------------------------------------------------------------------------

	/**
	 * return true if the glass-box invariants are observed, false otherwise.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code instance != null}
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @param instance	instance to be tested.
	 * @return			true if the glass-box invariants are observed, false otherwise.
	 */
	protected static boolean	glassBoxInvariants(HeaterUser instance)
	{
		assert	instance != null :
				new PreconditionException("instance != null");

		boolean ret = true;
		ret &= InvariantChecking.checkGlassBoxInvariant(
					instance.heaterUserInboundPortURI != null &&
								!instance.heaterUserInboundPortURI.isEmpty(),
					HeaterUser.class, instance,
					"heaterUserInboundPortURI != null && "
					+ "!heaterUserInboundPortURI.isEmpty()");
		ret &= InvariantChecking.checkGlassBoxInvariant(
					instance.heaterInternalControlInboundPortURI != null &&
						!instance.heaterInternalControlInboundPortURI.isEmpty(),
					HeaterUser.class, instance,
					"heaterInternalControlInboundPortURI != null && "
					+ "!heaterInternalControlInboundPortURI.isEmpty()");
		ret &= InvariantChecking.checkGlassBoxInvariant(
					instance.heaterExternalControlInboundPortURI != null &&
						!instance.heaterExternalControlInboundPortURI.isEmpty(),
					HeaterUser.class, instance,
					"heaterExternalControlInboundPortURI != null && "
					+ "!heaterExternalControlInboundPortURI.isEmpty()");
		ret &= InvariantChecking.checkGlassBoxInvariant(
					instance.currentExecutionType != null,
					HeaterUser.class, instance,
					"currentExecutionType != null");
		ret &= InvariantChecking.checkGlassBoxInvariant(
					instance.currentSimulationType != null,
					HeaterUser.class, instance,
					"hcurrentSimulationType != null");
		ret &= InvariantChecking.checkGlassBoxInvariant(
					!instance.currentExecutionType.isStandard() ||
							instance.currentSimulationType.isNoSimulation(),
					HeaterUser.class, instance,
					"!currentExecutionType.isStandard() || "
					+ "currentSimulationType.isNoSimulation()");
		ret &= InvariantChecking.checkGlassBoxInvariant(
					instance.currentSimulationType.isNoSimulation() ||
						(instance.globalArchitectureURI != null &&
							!instance.globalArchitectureURI.isEmpty()),
					HeaterUser.class, instance,
					"currentSimulationType.isNoSimulation() || "
					+ "(globalArchitectureURI != null && "
					+ "!globalArchitectureURI.isEmpty())");
		ret &= InvariantChecking.checkGlassBoxInvariant(
					instance.currentSimulationType.isNoSimulation() ||
						(instance.localArchitectureURI != null &&
							!instance.localArchitectureURI.isEmpty()),
					HeaterUser.class, instance,
					"currentSimulationType.isNoSimulation() || "
					+ "(localArchitectureURI != null && "
					+ "!localArchitectureURI.isEmpty())");
		ret &= InvariantChecking.checkGlassBoxInvariant(
					!instance.currentSimulationType.isSimulated() ||
										instance.simulationTimeUnit != null,
					HeaterUser.class, instance,
					"!currentSimulationType.isSimulated() || "
					+ "simulationTimeUnit != null");
		ret &= InvariantChecking.checkGlassBoxInvariant(
					!instance.currentSimulationType.isRealTimeSimulation() ||
													instance.accFactor > 0.0,
					HeaterUser.class, instance,
					"!currentSimulationType.isRealTimeSimulation() || "
					+ "accFactor > 0.0");
		return ret;
	}

	/**
	 * return true if the black-box invariants are observed, false otherwise.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code instance != null}
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @param instance	instance to be tested.
	 * @return			true if the black-box invariants are observed, false otherwise.
	 */
	protected static boolean	blackBoxInvariants(HeaterUser instance)
	{
		assert	instance != null :
				new PreconditionException("instance != null");

		boolean ret = true;
		ret &= InvariantChecking.checkBlackBoxInvariant(
					X_RELATIVE_POSITION >= 0,
					HeaterUser.class, instance,
					"X_RELATIVE_POSITION >= 0");
		ret &= InvariantChecking.checkBlackBoxInvariant(
					Y_RELATIVE_POSITION >= 0,
					HeaterUser.class, instance,
					"Y_RELATIVE_POSITION >= 0");
		return ret;
	}

	// -------------------------------------------------------------------------
	// Constructors
	// -------------------------------------------------------------------------

	/**
	 * create a heater user component.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code heaterUserInboundPortURI != null && !heaterUserInboundPortURI.isEmpty()}
	 * pre	{@code heaterInternalControlInboundPortURI != null && !heaterInternalControlInboundPortURI.isEmpty()}
	 * pre	{@code heaterExternalControlInboundPortURI != null && !heaterExternalControlInboundPortURI.isEmpty()}
	 * pre	{@code currentExecutionType != null}
	 * pre	{@code currentExecutionType.isStandard() || clockURI != null && !clockURI.isEmpty()}
	 * pre	{@code !currentExecutionType.isStandard() || currentSimulationType.isNoSimulation()}
	 * pre	{@code currentSimulationType.isNoSimulation() || (simArchitectureURI != null && !simArchitectureURI.isEmpty())}
	 * pre	{@code currentSimulationType.isNoSimulation() || (localSimulatorURI != null && !localSimulatorURI.isEmpty())}
	 * pre	{@code !currentSimulationType.isSimulated() || simulationTimeUnit != null}
	 * pre	{@code !currentSimulationType.isRealTimeSimulation() || accFactor > 0.0}
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @param heaterUserInboundPortURI				URI of the user component interface inbound port.
	 * @param heaterInternalControlInboundPortURI	URI of the internal control component interface inbound port.
	 * @param heaterExternalControlInboundPortURI	URI of the external control component interface inbound port.
	 * @param currentExecutionType					execution type for the next run.
	 * @param currentSimulationType					simulation type for the next run.
	 * @param globalArchitectureURI					URI of the global simulation architecture to be created or the empty string if the component does not execute as a simulation.
	 * @param localArchitectureURI					URI of the local simulation architecture to be used in the global simulation architecture.
	 * @param simulationTimeUnit					time unit in which {@code simulatedStartTime} and {@code simulationDuration} are expressed.
	 * @param accFactor								acceleration factor for the simulation.
	 * @param clockURI								URI of the clock to be used to synchronise the test scenarios and the simulation.
	 * @throws Exception							<i>to do</i>.
	 */
	protected			HeaterUser(
		String heaterUserInboundPortURI,
		String heaterInternalControlInboundPortURI,
		String heaterExternalControlInboundPortURI,
		ExecutionType currentExecutionType,
		SimulationType currentSimulationType,
		String globalArchitectureURI,
		String localArchitectureURI,
		TimeUnit simulationTimeUnit,
		double accFactor,
		String clockURI
		) throws Exception
	{
		super(REFLECTION_INBOUND_PORT_URI, 1, 1);

		System.out.println("global -> " + globalArchitectureURI);
		System.out.println("local -> " + localArchitectureURI);
		
		assert	currentExecutionType != null :
				new PreconditionException("currentExecutionType != null");
		assert	!currentExecutionType.isStandard() ||
								clockURI != null && !clockURI.isEmpty() :
				new PreconditionException(
						"!currentExecutionType.isStandard() || "
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
		assert	!currentSimulationType.isSimulated() ||
												simulationTimeUnit != null :
				new PreconditionException(
						"!currentSimulationType.isSimulated() || "
						+ "simulationTimeUnit != null");
		assert	!currentSimulationType.isRealTimeSimulation() ||
														accFactor > 0.0 :
				new PreconditionException(
						"!currentExecutionType.isRealTimeSimulation() || "
						+ "accFactor > 0.0");

		this.currentExecutionType = currentExecutionType;
		this.currentSimulationType = currentSimulationType;
		this.globalArchitectureURI = globalArchitectureURI;
		this.localArchitectureURI = localArchitectureURI;
		this.simulationTimeUnit = simulationTimeUnit;
		this.accFactor = accFactor;
		this.clockURI = clockURI;

		this.initialise(heaterUserInboundPortURI,
						heaterInternalControlInboundPortURI,
						heaterExternalControlInboundPortURI);
	}

	/**
	 * initialise a heater test component.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code heaterUserInboundPortURI != null && !heaterUserInboundPortURI.isEmpty()}
	 * pre	{@code heaterInternalControlInboundPortURI != null && !heaterInternalControlInboundPortURI.isEmpty()}
	 * pre	{@code heaterExternalControlInboundPortURI != null && !heaterExternalControlInboundPortURI.isEmpty()}
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @param heaterUserInboundPortURI				URI of the user component interface inbound port.
	 * @param heaterInternalControlInboundPortURI	URI of the internal control component interface inbound port.
	 * @param heaterExternalControlInboundPortURI	URI of the external control component interface inbound port.
	 * @throws Exception							<i>to do</i>.
	 */
	protected void		initialise(
		String heaterUserInboundPortURI,
		String heaterInternalControlInboundPortURI,
		String heaterExternalControlInboundPortURI
		) throws Exception
	{
		assert	heaterUserInboundPortURI != null &&
										!heaterUserInboundPortURI.isEmpty() :
				new PreconditionException(
						"heaterUserInboundPortURI != null && "
						+ "!heaterUserInboundPortURI.isEmpty()");
		assert	heaterInternalControlInboundPortURI != null &&
								!heaterInternalControlInboundPortURI.isEmpty() :
		new PreconditionException(
				"heaterInternalControlInboundPortURI != null && "
				+ "!heaterInternalControlInboundPortURI.isEmpty()");
		assert	heaterExternalControlInboundPortURI != null &&
								!heaterExternalControlInboundPortURI.isEmpty() :
		new PreconditionException(
				"heaterExternalControlInboundPortURI != null && "
				+ "!heaterExternalControlInboundPortURI.isEmpty()");

		this.heaterUserInboundPortURI = heaterUserInboundPortURI;
		this.hop = new HeaterUserOutboundPort(this);
		this.hop.publishPort();

		this.heaterInternalControlInboundPortURI =
											heaterInternalControlInboundPortURI;
		this.heaterExternalControlInboundPortURI =
											heaterExternalControlInboundPortURI;

		if (this.currentExecutionType.isUnitTest()) {
			this.hicop = new HeaterInternalControlOutboundPort(this);
			this.hicop.publishPort();
			this.hecop = new HeaterExternalControlOutboundPort(this);
			this.hecop.publishPort();
		}

		// create the simulation architecture given the current type of
		// simulation i.e., for the current execution
		switch (this.currentSimulationType) {
		case MIL_SIMULATION:
			Architecture architecture = 
						LocalSimulationArchitectures.
								createHeaterUserMILLocalArchitecture(
													this.localArchitectureURI,
													this.simulationTimeUnit);
			assert	architecture.getRootModelURI().equals(
													this.localArchitectureURI) :
					new BCMException(
							"local simulator " + this.localArchitectureURI
							+ " does not exist!");
			this.addLocalSimulatorArchitecture(architecture);
			this.global2localSimulationArchitectureURIS.
					put(this.globalArchitectureURI, this.localArchitectureURI);
			break;
		case MIL_RT_SIMULATION:
			architecture = LocalSimulationArchitectures.
								createHeaterUserMILRT_LocalArchitecture(
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
			// no simulator for HeaterUser in SIL simulations, just running
			// its code
		case NO_SIMULATION:
		default:
		}

		if (VERBOSE) {
			this.tracer.get().setTitle("Heater user component");
			this.tracer.get().setRelativePosition(X_RELATIVE_POSITION,
												  Y_RELATIVE_POSITION);
			this.toggleTracing();
		}

		// Invariant checking
		assert	glassBoxInvariants(this) :
				new ImplementationInvariantException("glassBoxInvariants(this)");
		assert	blackBoxInvariants(this) :
				new InvariantException("blackBoxInvariants(this)");
	}

	// -------------------------------------------------------------------------
	// Component services implementation
	// -------------------------------------------------------------------------

	protected void		testSwitchOnSwitchOff()
	{
		this.traceMessage("testSwitchOnSwitchOff...\n");
		try {
			this.hop.switchOn();
		} catch (Exception e) {
			this.traceMessage("...KO.\n" + e);
			assertTrue(false);
		}
		try {
			// allow the time to execute the internal asynchronous calls
			Thread.sleep(100L);
		} catch (InterruptedException e1) {
			throw new RuntimeException(e1) ;
		}
		try {
			this.hop.switchOff();
		} catch (Exception e) {
			this.traceMessage("...KO.\n" + e);
			assertTrue(false);
		}
		this.traceMessage("...testSwitchOnSwitchOff() done.\n");
	}

	protected void		testOn()
	{
		this.traceMessage("testOn()...\n");
		try {
			assertEquals(false, this.hop.on());
		} catch (Exception e) {
			this.traceMessage("...KO.\n" + e);
			assertTrue(false);
		}
		try {
			this.hop.switchOn();
			assertEquals(true, this.hop.on());
			try {
				// allow the time to execute the internal asynchronous calls
				Thread.sleep(100L);
			} catch (InterruptedException e1) {
				throw new RuntimeException(e1) ;
			}
			this.hop.switchOff();
		} catch (Exception e) {
			this.traceMessage("...KO.\n" + e);
			assertTrue(false);
		}
		this.traceMessage("...testOn() done.\n");
	}

	protected void		testTargetTemperature()
	{
		this.traceMessage("testTargetTemperature()...\n");
		try {
			this.hop.setTargetTemperature(10.0);
			assertEquals(10.0, this.hop.getTargetTemperature());
			this.hop.setTargetTemperature(Heater.STANDARD_TARGET_TEMPERATURE);
		} catch (Exception e) {
			this.traceMessage("...KO.\n" + e);
			assertTrue(false);
		}
		this.traceMessage("...testTargetTemperature() done.\n");

	}

	protected void		testCurrentTemperature()
	{
		this.traceMessage("testCurrentTemperature()...\n");
		try {
			this.hop.switchOn();
			assertEquals(Heater.FAKE_CURRENT_TEMPERATURE,
						 this.hop.getCurrentTemperature());
			try {
				// allow the time to execute the internal asynchronous calls
				Thread.sleep(100L);
			} catch (InterruptedException e1) {
				throw new RuntimeException(e1) ;
			}
			this.hop.switchOff();
		} catch (Exception e) {
			this.traceMessage("...KO.\n" + e);
			assertTrue(false);
		}
		this.traceMessage("...testCurrentTemperature() done.\n");
	}

	protected void		testPowerLevel()
	{
		this.traceMessage("testPowerLevel()...\n");
		try {
			assertEquals(Heater.MAX_POWER_LEVEL,
						 this.hop.getMaxPowerLevel());
			this.hop.switchOn();
			this.hop.setCurrentPowerLevel(Heater.MAX_POWER_LEVEL/2.0);
			assertEquals(Heater.MAX_POWER_LEVEL/2.0,
						 this.hop.getCurrentPowerLevel());
			try {
				// allow the time to execute the internally called methods
				Thread.sleep(100L);
			} catch (InterruptedException e1) {
				throw new RuntimeException(e1) ;
			}
			this.hop.switchOff();
		} catch (Exception e) {
			this.traceMessage("...KO.\n" + e);
			assertTrue(false);
		}
		this.traceMessage("...testPowerLevel() done.\n");
	}

	protected void		testInternalControl()
	{
		this.traceMessage("testInternalControl()...\n");
		try {
			assertEquals(Heater.STANDARD_TARGET_TEMPERATURE,
						 this.hicop.getTargetTemperature());
			this.hop.switchOn();
			assertEquals(true, this.hop.on());
			assertEquals(Heater.FAKE_CURRENT_TEMPERATURE,
						 this.hicop.getCurrentTemperature());
			// given the initial values, when the heater is switched on, the
			// heater controller immediately asks for heating, hence to test
			// explicitly the internal controls, the heating must first be
			// stopped before testing again a startHeating
			try {
				// allow the time to execute the internally called methods
				Thread.sleep(100L);
			} catch (InterruptedException e1) {
				throw new RuntimeException(e1) ;
			}
			this.hicop.stopHeating();
			assertEquals(false, this.hicop.heating());
			this.hicop.startHeating();
			assertEquals(true, this.hicop.heating());
			try {
				// allow the time to execute the internally called methods
				Thread.sleep(100L);
			} catch (InterruptedException e1) {
				throw new RuntimeException(e1) ;
			}
			this.hop.switchOff();
		} catch (Exception e) {
			this.traceMessage("...KO.\n");
			assertTrue(false);
		}
		this.traceMessage("...testInternalControl() done.\n");
	}

	protected void		testExternalControl()
	{
		this.traceMessage("testExternalControl()...\n");
		try {
			assertEquals(Heater.MAX_POWER_LEVEL,
						 this.hecop.getMaxPowerLevel());
			this.hecop.setCurrentPowerLevel(Heater.MAX_POWER_LEVEL/2.0);
			assertEquals(Heater.MAX_POWER_LEVEL/2.0,
						 this.hecop.getCurrentPowerLevel());
		} catch (Exception e) {
			this.traceMessage("...KO.\n" + e);
			assertTrue(false);
		}
		this.traceMessage("...testExternalControl() done.\n");
	}

	protected void		runAllTests()
	{
		this.testSwitchOnSwitchOff();
		this.testOn();
		this.testTargetTemperature();
		this.testCurrentTemperature();
		this.testPowerLevel();
		this.testInternalControl();
		this.testExternalControl();
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

		try {
			this.doPortConnection(
					this.hop.getPortURI(),
					this.heaterUserInboundPortURI,
					HeaterUserConnector.class.getCanonicalName());
			if (this.currentExecutionType.isUnitTest()) {
				this.doPortConnection(
						this.hicop.getPortURI(),
						heaterInternalControlInboundPortURI,
						HeaterInternalControlConnector.class.getCanonicalName());
				this.doPortConnection(
						this.hecop.getPortURI(),
						heaterExternalControlInboundPortURI,
						HeaterExternalControlConnector.class.getCanonicalName());
			}

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
				// no simulator for HeaterUser in SIL simulations, just running
				// its code
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
	public synchronized void	execute() throws Exception
	{
		if (!this.currentExecutionType.isStandard() &&
							!this.currentSimulationType.isMilSimulation() &&
							!this.currentSimulationType.isMILRTSimulation()) {
			ClocksServerWithSimulationOutboundPort clocksServerOutboundPort =
							new ClocksServerWithSimulationOutboundPort(this);
			clocksServerOutboundPort.publishPort();
			this.doPortConnection(
					clocksServerOutboundPort.getPortURI(),
					ClocksServer.STANDARD_INBOUNDPORT_URI,
					ClocksServerWithSimulationConnector.class.getCanonicalName());
			this.logMessage("Heater tester gets the clock.");
			AcceleratedAndSimulationClock clock =
				clocksServerOutboundPort.getClockWithSimulation(this.clockURI);
			this.doPortDisconnection(clocksServerOutboundPort.getPortURI());
			clocksServerOutboundPort.unpublishPort();
			clocksServerOutboundPort.destroyPort();

			this.logMessage("Heater user waits until start.");
			clock.waitUntilStart();

			if (this.currentSimulationType.isNoSimulation()) {
				if (this.currentExecutionType.isUnitTest()) {
					this.runUnitTestsNoSimulation(clock);
				} else {
					this.runIntegrationTestNoSimulation(clock);
				}
			} else {
				assert	this.currentExecutionType.isTest() :
						new BCMException("currentExecutionType.isTest()");
				assert	this.currentSimulationType.isSILSimulation() :
						new BCMException(
								"currentSimulationType.isSILSimulation()");

				clock.waitUntilSimulationStart();
				runSILTestScenario(clock);
			}
		}
	}

	/**
	 * @see fr.sorbonne_u.components.AbstractComponent#finalise()
	 */
	@Override
	public synchronized void	finalise() throws Exception
	{
		this.logMessage("Heater user ends.");
		this.doPortDisconnection(this.hop.getPortURI());
		if (this.currentExecutionType.isUnitTest()) {
			this.doPortDisconnection(this.hicop.getPortURI());
			this.doPortDisconnection(this.hecop.getPortURI());
		}

		super.finalise();
	}

	/**
	 * @see fr.sorbonne_u.components.AbstractComponent#shutdown()
	 */
	@Override
	public synchronized void	shutdown() throws ComponentShutdownException
	{
		try {
			this.hop.unpublishPort();
			if (this.currentExecutionType.isUnitTest()) {
				this.hicop.unpublishPort();
				this.hecop.unpublishPort();
			}
		} catch (Exception e) {
			throw new ComponentShutdownException(e) ;
		}
		super.shutdown();
	}

	// -------------------------------------------------------------------------
	// Component internal methods
	// -------------------------------------------------------------------------

	/**
	 * run the unit tests when no simulation is used (pure software tests).
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code clock != null}
	 * pre	{@code !clock.startTimeNotReached()}
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @param clock	accelerated clock giving the time reference for the run.
	 */
	protected void		runUnitTestsNoSimulation(
		AcceleratedClock clock
		)
	{
		assert	clock != null : new PreconditionException("clock != null");
		assert	!clock.startTimeNotReached() :
				new PreconditionException("!clock.startTimeNotReached()");

		Instant startInstant = clock.getStartInstant();
		Instant startTests = startInstant.plusSeconds(60L);
		long delayToTestsStart = clock.nanoDelayUntilInstant(startTests);
		this.scheduleTaskOnComponent(
				new AbstractComponent.AbstractTask() {
					@Override
					public void run() {
						try {
							logMessage("Heater tester starts the tests.");
							runAllTests();
							logMessage("Heater tester tests end.");
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}, delayToTestsStart, TimeUnit.NANOSECONDS);
	}

	/**
	 * run the integration tests when no simulation is used (pure software tests).
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code clock != null}
	 * pre	{@code !clock.startTimeNotReached()}
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @param clock	accelerated clock giving the time reference for the run.
	 * @throws Exception 
	 */
	protected void		runIntegrationTestNoSimulation(
		AcceleratedClock clock
		) throws Exception
	{
		assert	clock != null : new PreconditionException("clock != null");
		assert	!clock.startTimeNotReached() :
				new PreconditionException("!clock.startTimeNotReached()");
		
		this.hop.switchOn();
	}

	/**
	 * run software tests when SIL simulation is used.
	 * 
	 * <p><strong>Description</strong></p>
	 * 
	 * <p>
	 * The scenario switches the heater on 10 seconds (in simulated time
	 * reference) after the beginning of the test and switches it off 10
	 * seconds (in simulated time reference) before the end of the test.
	 * </p>
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code clock != null}
	 * pre	{@code !clock.startTimeNotReached()}
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @param clock	accelerated clock giving the time reference for the run.
	 */
	protected void		runSILTestScenario(
		AcceleratedAndSimulationClock clock
		)
	{
		assert	clock != null : new PreconditionException("clock != null");
		assert	!clock.startTimeNotReached() :
				new PreconditionException("!clock.startTimeNotReached()");

		Instant simulationStartInstant = clock.getSimulationStartInstant();
		Instant currentInstant = clock.currentInstant();
		// switch on after ten seconds (in simulated time)
		Instant switchOnInstant = simulationStartInstant.plusSeconds(600L);
		assert	switchOnInstant.isAfter(currentInstant) :
				new BCMException("switchOnInstant.isAfter(currentInstant)");
		// switch off ten seconds before the end of the simulation
		// (in simulated time)
		Instant switchOffInstant =
						clock.getSimulationEndInstant().minusSeconds(600L);

		long delayToSwitchOn = clock.nanoDelayUntilInstant(switchOnInstant);
		HeaterUserOutboundPort o = this.hop;
		this.scheduleTaskOnComponent(
				new AbstractComponent.AbstractTask() {
					@Override
					public void run() {
						try {
							traceMessage(
								"Heater user switches the heater on\n.");
							o.switchOn();
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}, delayToSwitchOn, TimeUnit.NANOSECONDS);
		long delayToSwitchOff = clock.nanoDelayUntilInstant(switchOffInstant);
		this.scheduleTaskOnComponent(
				new AbstractComponent.AbstractTask() {
					@Override
					public void run() {
						try {
							traceMessage(
									"Heater user switches the heater off\n.");
							o.switchOff();
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}, delayToSwitchOff, TimeUnit.NANOSECONDS);
	}
}
// -----------------------------------------------------------------------------
