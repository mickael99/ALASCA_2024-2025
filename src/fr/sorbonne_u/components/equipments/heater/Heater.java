package fr.sorbonne_u.components.equipments.heater;

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
import fr.sorbonne_u.components.interfaces.DataOfferedCI;
import fr.sorbonne_u.components.utils.ExecutionType;
import fr.sorbonne_u.components.utils.Measure;
import fr.sorbonne_u.components.utils.MeasurementUnit;
import fr.sorbonne_u.components.utils.SimulationType;
import fr.sorbonne_u.devs_simulation.architectures.Architecture;
import fr.sorbonne_u.devs_simulation.models.time.Duration;
import fr.sorbonne_u.devs_simulation.models.time.Time;
import fr.sorbonne_u.exceptions.InvariantChecking;
import fr.sorbonne_u.exceptions.InvariantException;
import fr.sorbonne_u.exceptions.PostconditionException;
import fr.sorbonne_u.exceptions.PreconditionException;
import fr.sorbonne_u.utils.aclocks.AcceleratedClock;
import fr.sorbonne_u.utils.aclocks.ClocksServer;
import java.util.HashMap;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import fr.sorbonne_u.components.AbstractComponent;
import fr.sorbonne_u.components.equipments.heater.connections.*;
import fr.sorbonne_u.components.equipments.heater.measures.HeaterCompoundMeasure;
import fr.sorbonne_u.components.equipments.heater.measures.HeaterSensorData;
import fr.sorbonne_u.components.equipments.heater.measures.HeaterStateMeasure;
import fr.sorbonne_u.components.equipments.heater.mil.HeaterStateModel;
import fr.sorbonne_u.components.equipments.heater.mil.HeaterTemperatureModel;
import fr.sorbonne_u.components.equipments.heater.mil.LocalSimulationArchitectures;
import fr.sorbonne_u.components.equipments.heater.mil.events.DoNotHeat;
import fr.sorbonne_u.components.equipments.heater.mil.events.Heat;
import fr.sorbonne_u.components.equipments.heater.mil.events.SetPowerHeater;
import fr.sorbonne_u.components.equipments.heater.mil.events.SetPowerHeater.PowerValue;
import fr.sorbonne_u.components.equipments.heater.mil.events.SwitchOffHeater;
import fr.sorbonne_u.components.equipments.heater.mil.events.SwitchOnHeater;

// -----------------------------------------------------------------------------
/**
 * The class <code>Heater</code> a heater component.
 *
 * <p><strong>Description</strong></p>
 * 
 * <p>
 * The component is relatively complex as it attempts to show in one component
 * several possibilities, like the capability to create several local simulation
 * architectures, the definition of a well-structured sensor and actuator
 * interfaces as well as the capability to run in different modes (unit test,
 * integration test, MIL simulations and SIL simulations) each with their own
 * test scenario.
 * </p>
 * 
 * 
 * <p>
 * The heater is an adjustable appliance, hence it connects with the household
 * energy manager to allow it to manage its power consumption. It also connects
 * to the electric meter to take its (simulated) electric power consumption into
 * account.
 * </p>
 * <p>
 * This implementation of the heater is complicated by the objective to show the
 * entire spectrum of possible execution and simulation modes. There are three
 * execution types defined by {@code ExecutionType}:
 * </p>
 * <ol>
 * <li>{@code STANDARD} means that the component would execute in normal
 *   operational conditions, on the field (which is not used in this project
 *   but would be in an industrial one).</li>
 * <li>{@code UNIT_TEST} means that the component executes in unit tests where
 *   it is the sole appliance but cooperates with the {@code HeaterController}
 *   and {@code HeaterUser} components.</li>
 * <li>{@code INTEGRATION_TEST} means that the component executes in integration
 *   tests where other appliances coexist and where it must also cooperate with
 *   the {@code HEM} and {@code ElectricMeter} components.</li>
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
 * In this implementation of the {@code Heater} component, the standard
 * execution type is not really implemented as the software is not embedded in
 * any real appliance. In unit tests with no simulation, the component is
 * totally passive as its methods are called by the {@code HeaterController} and
 * {@code HeaterUser} components. In MIL and MIL real time simulations, the
 * component simply creates and installs its local simulation architecture,
 * which execution will be started by a supervisor component. It is also the
 * case for SIL simulations in integration tests.
 * </p>
 * <p>
 * For SIL simulations in unit tests, the component presents a rather special
 * case as it is the only component that runs a simulator, hence there is no
 * global component simulation architecture and therefore no need for
 * supervisor and coordinator components. Hence, the local SIL simulation
 * architecture uses only the models pertaining to the hair dryer itself:
 * </p>
 * <p><img src="../../../../../../../images/hem-2024-e3/HeaterUnitTestLocalArchitecture.png"/></p>
 * <p>
 * After creating (in {@code initialise}) its local SIL simulation architecture
 * and installing the local simulation plug-in (in {@code start}), the component
 * also creates, initialises and triggers the execution of the simulator in the
 * method {@code execute}.
 * </p>
 * <p>
 * For SIL simulations in integration tests, the {@code HeaterElectricityModel}
 * cannot share its continuous variable {@code currentIntensity} with the
 * {@code ElectricMeterElectricityModel} across component borders. Hence, the
 * {@code HeaterElectricityModel} is rather moved to the {@code ElectricMeter}
 * component simulator, to co-localise it with the
 * {@code ElectricMeterElectricityModel}, hence the {@code HairDryerStateModel}
 * remaining in the {@code HairDryer} component simulator will emits the hair
 * dryer events to the {@code HairDryerElectricityModel} across the border of
 * components, leaving the {@code HairDryer} and entering the
 * {@code ElectricMeter}.
 * </p>
 * 
 * <p><strong>Glass-box Invariants</strong></p>
 * 
 * <pre>
 * invariant	{@code MAX_POWER_LEVEL > 0.0}
 * invariant	{@code currentState != null}
 * invariant	{@code targetTemperature >= -50.0 && targetTemperature <= 50.0}
 * invariant	{@code currentPowerLevel >= 0.0 && currentPowerLevel <= MAX_POWER_LEVEL}
 * invariant	{@code currentExecutionType != null}
 * invariant	{@code currentSimulationType != null}
 * invariant	{@code !currentExecutionType.isStandard() || currentSimulationType.isNoSimulation()}
 * invariant	{@code currentSimulationType.isNoSimulation() || (globalArchitectureURI != null && !globalArchitectureURI.isEmpty())}
 * invariant	{@code currentSimulationType.isNoSimulation() || (localArchitectureURI != null && !localArchitectureURI.isEmpty())}
 * invariant	{@code !currentSimulationType.isSimulated() || simulationTimeUnit != null}
 * invariant	{@code !currentSimulationType.isRealTimeSimulation() || accFactor > 0.0}
 * </pre>
 * 
 * <p><strong>Black-box Invariants</strong></p>
 * 
 * <pre>
 * invariant	{@code REFLECTION_INBOUND_PORT_URI != null && !REFLECTION_INBOUND_PORT_URI.isEmpty()}
 * invariant	{@code TEMPERATURE_UNIT != null}
 * invariant	{@code USER_INBOUND_PORT_URI != null && !USER_INBOUND_PORT_URI.isEmpty()}
 * invariant	{@code INTERNAL_CONTROL_INBOUND_PORT_URI != null && !INTERNAL_CONTROL_INBOUND_PORT_URI.isEmpty()}
 * invariant	{@code EXTERNAL_CONTROL_INBOUND_PORT_URI != null && !EXTERNAL_CONTROL_INBOUND_PORT_URI.isEmpty()}
 * invariant	{@code SENSOR_INBOUND_PORT_URI != null && !SENSOR_INBOUND_PORT_URI.isEmpty()}
 * invariant	{@code ACTUATOR_INBOUND_PORT_URI != null && !ACTUATOR_INBOUND_PORT_URI.isEmpty()}
 * invariant	{@code X_RELATIVE_POSITION >= 0}
 * invariant	{@code Y_RELATIVE_POSITION >= 0}
 * </pre>
 * 
 * <p>Created on : 2023-09-18</p>
 * 
 * @author	<a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
 */
@OfferedInterfaces(offered={HeaterUserCI.class, HeaterInternalControlCI.class,
							HeaterExternalControlCI.class,
							HeaterSensorDataCI.HeaterSensorOfferedPullCI.class,
							HeaterActuatorCI.class})
@RequiredInterfaces(required={DataOfferedCI.PushCI.class,
							  ClocksServerWithSimulationCI.class})
public class			Heater
extends		AbstractCyPhyComponent
implements	HeaterUserI,
			HeaterInternalControlI
{
	// -------------------------------------------------------------------------
	// Inner interfaces and types
	// -------------------------------------------------------------------------

	/**
	 * The enumeration <code>HeaterState</code> describes the operation
	 * states of the heater.
	 *
	 * <p><strong>Description</strong></p>
	 * 
	 * <p><strong>Black-box Invariant</strong></p>
	 * 
	 * <pre>
	 * invariant	{@code true}	// no more invariant
	 * </pre>
	 * 
	 * <p>Created on : 2021-09-10</p>
	 * 
	 * @author	<a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
	 */
	public static enum		HeaterState
	{
		/** heater is on.													*/
		ON,
		/** heater is heating.												*/
		HEATING,
		/** heater is off.													*/
		OFF
	}

	/**
	 * The enumeration <code>HeaterSensorMeasures</code> describes the measures
	 * that the heater component performs.
	 *
	 * <p><strong>Description</strong></p>
	 * 
	 * TODO: not used yet!
	 * 
	 * <p><strong>Black-box Invariant</strong></p>
	 * 
	 * <pre>
	 * invariant	{@code true}	// no more invariant
	 * </pre>
	 * 
	 * <p>Created on : 2023-11-28</p>
	 * 
	 * @author	<a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
	 */
	public static enum		HeaterSensorMeasures
	{
		/** heating status: a boolean sensor where true means currently
		 *  heating, false currently not heating.							*/
		HEATING_STATUS,
		/** the current target temperature.									*/
		TARGET_TEMPERATURE,
		/** the current room temperature.									*/
		CURRENT_TEMPERATURE,
		/** both the target and the current temperatures.					*/
		COMPOUND_TEMPERATURES
	}

	// -------------------------------------------------------------------------
	// Constants and variables
	// -------------------------------------------------------------------------

	/** URI of the heater inbound port used in tests.						*/
	public static final String		REFLECTION_INBOUND_PORT_URI =
															"Heater-RIP-URI";	
	/** max power level of the heater, in watts.							*/
	protected static final double	MAX_POWER_LEVEL = 2000.0;
	/** standard target temperature for the heater.							*/
	protected static final double	STANDARD_TARGET_TEMPERATURE = 19.0;
	public static final MeasurementUnit	TEMPERATURE_UNIT =
														MeasurementUnit.CELSIUS;

	/** URI of the heater port for user interactions.						*/
	public static final String		USER_INBOUND_PORT_URI =
												"HEATER-USER-INBOUND-PORT-URI";
	/** URI of the heater port for internal control.						*/
	public static final String		INTERNAL_CONTROL_INBOUND_PORT_URI =
									"HEATER-INTERNAL-CONTROL-INBOUND-PORT-URI";
	/** URI of the heater port for internal control.						*/
	public static final String		EXTERNAL_CONTROL_INBOUND_PORT_URI =
									"HEATER-EXTERNAL-CONTROL-INBOUND-PORT-URI";
	public static final String		SENSOR_INBOUND_PORT_URI =
											"HEATER-SENSOR-INBOUND-PORT-URI";
	public static final String		ACTUATOR_INBOUND_PORT_URI =
											"HEATER-ACTUATOR-INBOUND-PORT-URI";

	/** when true, methods trace their actions.								*/
	public static boolean			VERBOSE = false;
	/** when tracing, x coordinate of the window relative position.			*/
	public static int				X_RELATIVE_POSITION = 0;
	/** when tracing, y coordinate of the window relative position.			*/
	public static int				Y_RELATIVE_POSITION = 0;

	/** fake current 	*/
	public static final double		FAKE_CURRENT_TEMPERATURE = 10.0;

	/** current state (on, off) of the heater.								*/
	protected HeaterState						currentState;
	/**	current power level of the heater.									*/
	protected double							currentPowerLevel;
	/** inbound port offering the <code>HeaterUserCI</code> interface.		*/
	protected HeaterUserInboundPort				hip;
	/** inbound port offering the <code>HeaterInternalControlCI</code>
	 *  interface.															*/
	protected HeaterInternalControlInboundPort	hicip;
	/** inbound port offering the <code>HeaterExternalControlCI</code>
	 *  interface.															*/
	protected HeaterExternalControlInboundPort	hecip;
	/** target temperature for the heating.	*/
	protected double							targetTemperature;

	// Sensors/actuators

	/** the inbound port through which the sensors are called.				*/
	protected HeaterSensorDataInboundPort	sensorInboundPort;
	/** the inbound port through which the actuators are called.			*/
	protected HeaterActuatorInboundPort		actuatorInboundPort;

	// Execution/Simulation

	/** URI of the clock to be used to synchronise the test scenarios and
	 *  the simulation.														*/
	protected final String				clockURI;
	/** accelerated clock governing the timing of actions in the test
	 *  scenarios; here the clock is needed to compute the real time
	 *  control period taking into account the acceleration factor
	 *  for the current execution.											*/
	protected final CompletableFuture<AcceleratedClock>	clock;

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

	protected static final String		CURRENT_TEMPERATURE_NAME =
														"currentTemperature";

	// -------------------------------------------------------------------------
	// Invariants
	// -------------------------------------------------------------------------

	/**
	 * return true if the glass-box invariants are observed, false otherwise.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code h != null}
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @param h	instance to be tested.
	 * @return	true if the glass-box invariants are observed, false otherwise.
	 */
	protected static boolean	glassBoxInvariants(Heater h)
	{
		assert	h != null : new PreconditionException("h != null");

		boolean ret = true;
		ret &= InvariantChecking.checkGlassBoxInvariant(
					MAX_POWER_LEVEL > 0.0,
					Heater.class, h,
					"MAX_POWER_LEVEL > 0.0");
		ret &= InvariantChecking.checkGlassBoxInvariant(
					h.currentState != null,
					Heater.class, h,
					"h.currentState != null");
		ret &= InvariantChecking.checkGlassBoxInvariant(
					h.targetTemperature >= -50.0 && h.targetTemperature <= 50.0,
					Heater.class, h,
					"h.targetTemperature >= -50.0 && h.targetTemperature <= 50.0");
		ret &= InvariantChecking.checkGlassBoxInvariant(
					h.currentPowerLevel >= 0.0 &&
									h.currentPowerLevel <= MAX_POWER_LEVEL,
					Heater.class, h,
					"h.currentPowerLevel >= 0.0 && "
								+ "h.currentPowerLevel <= MAX_POWER_LEVEL");
		ret &= InvariantChecking.checkGlassBoxInvariant(
					h.currentExecutionType != null,
					Heater.class, h,
					"currentExecutionType != null");
		ret &= InvariantChecking.checkGlassBoxInvariant(
					h.currentSimulationType != null,
					Heater.class, h,
					"hcurrentSimulationType != null");
		ret &= InvariantChecking.checkGlassBoxInvariant(
					!h.currentExecutionType.isStandard() ||
							h.currentSimulationType.isNoSimulation(),
					Heater.class, h,
					"!currentExecutionType.isStandard() || "
					+ "currentSimulationType.isNoSimulation()");
		ret &= InvariantChecking.checkGlassBoxInvariant(
					h.currentSimulationType.isNoSimulation() ||
						(h.globalArchitectureURI != null &&
							!h.globalArchitectureURI.isEmpty()),
					Heater.class, h,
					"currentSimulationType.isNoSimulation() || "
					+ "(globalArchitectureURI != null && "
					+ "!globalArchitectureURI.isEmpty())");
		ret &= InvariantChecking.checkGlassBoxInvariant(
					h.currentSimulationType.isNoSimulation() ||
						(h.localArchitectureURI != null &&
							!h.localArchitectureURI.isEmpty()),
					Heater.class, h,
					"currentSimulationType.isNoSimulation() || "
					+ "(localArchitectureURI != null && "
					+ "!localArchitectureURI.isEmpty())");
		ret &= InvariantChecking.checkGlassBoxInvariant(
					!h.currentSimulationType.isSimulated() ||
											h.simulationTimeUnit != null,
					Heater.class, h,
					"!currentSimulationType.isSimulated() || "
					+ "simulationTimeUnit != null");
		ret &= InvariantChecking.checkGlassBoxInvariant(
					!h.currentSimulationType.isRealTimeSimulation() ||
													h.accFactor > 0.0,
					Heater.class, h,
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
	 * pre	{@code h != null}
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @param h	instance to be tested.
	 * @return	true if the black-box invariants are observed, false otherwise.
	 */
	protected static boolean	blackBoxInvariants(Heater h)
	{
		assert	h != null : new PreconditionException("h != null");

		boolean ret = true;
		ret &= InvariantChecking.checkBlackBoxInvariant(
					REFLECTION_INBOUND_PORT_URI != null &&
									!REFLECTION_INBOUND_PORT_URI.isEmpty(),
					Heater.class, h,
					"REFLECTION_INBOUND_PORT_URI != null && "
								+ "!REFLECTION_INBOUND_PORT_URI.isEmpty()");
		ret &= InvariantChecking.checkBlackBoxInvariant(
					TEMPERATURE_UNIT != null,
					Heater.class, h,
					"TEMPERATURE_UNIT != null");
		ret &= InvariantChecking.checkBlackBoxInvariant(
					USER_INBOUND_PORT_URI != null &&
											!USER_INBOUND_PORT_URI.isEmpty(),
					Heater.class, h,
					"USER_INBOUND_PORT_URI != null && "
					+ "!USER_INBOUND_PORT_URI.isEmpty()");
		ret &= InvariantChecking.checkBlackBoxInvariant(
					INTERNAL_CONTROL_INBOUND_PORT_URI != null &&
								!INTERNAL_CONTROL_INBOUND_PORT_URI.isEmpty(),
					Heater.class, h,
					"INTERNAL_CONTROL_INBOUND_PORT_URI != null && "
							+ "!INTERNAL_CONTROL_INBOUND_PORT_URI.isEmpty()");
		ret &= InvariantChecking.checkBlackBoxInvariant(
					EXTERNAL_CONTROL_INBOUND_PORT_URI != null &&
								!EXTERNAL_CONTROL_INBOUND_PORT_URI.isEmpty(),
					Heater.class, h,
					"EXTERNAL_CONTROL_INBOUND_PORT_URI != null &&"
							+ "!EXTERNAL_CONTROL_INBOUND_PORT_URI.isEmpty()");
		ret &= InvariantChecking.checkBlackBoxInvariant(
					SENSOR_INBOUND_PORT_URI != null &&
										!SENSOR_INBOUND_PORT_URI.isEmpty(),
					Heater.class, h,
					"SENSOR_INBOUND_PORT_URI != null && "
					+ "!SENSOR_INBOUND_PORT_URI.isEmpty()");
		ret &= InvariantChecking.checkBlackBoxInvariant(
					ACTUATOR_INBOUND_PORT_URI != null &&
										!ACTUATOR_INBOUND_PORT_URI.isEmpty(),
					Heater.class, h,
					"ACTUATOR_INBOUND_PORT_URI != null && "
					+ "!ACTUATOR_INBOUND_PORT_URI.isEmpty()");
		ret &= InvariantChecking.checkBlackBoxInvariant(
					X_RELATIVE_POSITION >= 0,
					Heater.class, h,
					"X_RELATIVE_POSITION >= 0");
		ret &= InvariantChecking.checkBlackBoxInvariant(
					Y_RELATIVE_POSITION >= 0,
					Heater.class, h,
					"Y_RELATIVE_POSITION >= 0");
		return ret;
	}

	// -------------------------------------------------------------------------
	// Constructors
	// -------------------------------------------------------------------------

	/**
	 * create a new heater component for standard executions.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code true}	// no precondition.
	 * post	{@code !on()}
	 * </pre>
	 * 
	 * @throws Exception <i>to do</i>.
	 */
	protected			Heater() throws Exception
	{
		this(USER_INBOUND_PORT_URI, INTERNAL_CONTROL_INBOUND_PORT_URI,
			 EXTERNAL_CONTROL_INBOUND_PORT_URI, SENSOR_INBOUND_PORT_URI,
			 ACTUATOR_INBOUND_PORT_URI);
	}

	/**
	 * create a new heater for standard executions.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code heaterUserInboundPortURI != null && !heaterUserInboundPortURI.isEmpty()}
	 * pre	{@code heaterInternalControlInboundPortURI != null && !heaterInternalControlInboundPortURI.isEmpty()}
	 * pre	{@code heaterExternalControlInboundPortURI != null && !heaterExternalControlInboundPortURI.isEmpty()}
	 * pre	{@code heaterSensorInboundPortURI != null && !heaterSensorInboundPortURI.isEmpty()}
	 * pre	{@code heaterActuatorInboundPortURI != null && !heaterActuatorInboundPortURI.isEmpty()}
	 * post	{@code !on()}
	 * </pre>
	 * 
	 * @param heaterUserInboundPortURI				URI of the inbound port to call the heater component for user interactions.
	 * @param heaterInternalControlInboundPortURI	URI of the inbound port to call the heater component for internal control.
	 * @param heaterExternalControlInboundPortURI	URI of the inbound port to call the heater component for external control.
	 * @param heaterSensorInboundPortURI			URI of the inbound port to call the heater component sensors.
	 * @param heaterActuatorInboundPortURI			URI of the inbound port to call the heater component actuators.
	 * @throws Exception							<i>to do</i>.
	 */
	protected			Heater(
		String heaterUserInboundPortURI,
		String heaterInternalControlInboundPortURI,
		String heaterExternalControlInboundPortURI,
		String heaterSensorInboundPortURI,
		String heaterActuatorInboundPortURI
		) throws Exception
	{
		this(REFLECTION_INBOUND_PORT_URI,
			 heaterUserInboundPortURI,
			 heaterInternalControlInboundPortURI,
			 heaterExternalControlInboundPortURI,
			 heaterSensorInboundPortURI,
			 heaterActuatorInboundPortURI,
			 ExecutionType.STANDARD,
			 SimulationType.NO_SIMULATION,
			 null, null, null, 0.0, null);
	}

	/**
	 * create a new heater for any type of executions and any type of
	 * simulations.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code reflectionInboundPortURI != null && !reflectionInboundPortURI.isEmpty()}
	 * pre	{@code heaterUserInboundPortURI != null && !heaterUserInboundPortURI.isEmpty()}
	 * pre	{@code heaterInternalControlInboundPortURI != null && !heaterInternalControlInboundPortURI.isEmpty()}
	 * pre	{@code heaterExternalControlInboundPortURI != null && !heaterExternalControlInboundPortURI.isEmpty()}
	 * pre	{@code heaterSensorInboundPortURI != null && !heaterSensorInboundPortURI.isEmpty()}
	 * pre	{@code heaterActuatorInboundPortURI != null && !heaterActuatorInboundPortURI.isEmpty()}
	 * pre	{@code currentExecutionType != null}
	 * pre	{@code currentExecutionType.isStandard() || clockURI != null && !clockURI.isEmpty()}
	 * pre	{@code !currentExecutionType.isStandard() || currentSimulationType.isNoSimulation()}
	 * pre	{@code currentSimulationType.isNoSimulation() || (globalArchitectureURI != null && !globalArchitectureURI.isEmpty())}
	 * pre	{@code currentSimulationType.isNoSimulation() || (localArchitectureURI != null && !localArchitectureURI.isEmpty())}
	 * pre	{@code !currentSimulationType.isSimulated() || simulationTimeUnit != null}
	 * pre	{@code !currentSimulationType.isRealTimeSimulation() || accFactor > 0.0}
	 * post	{@code !on()}
	 * </pre>
	 * 
	 * @param reflectionInboundPortURI				URI of the reflection inbound port of the component.
	 * @param heaterUserInboundPortURI				URI of the inbound port to call the heater component for user interactions.
	 * @param heaterInternalControlInboundPortURI	URI of the inbound port to call the heater component for internal control.
	 * @param heaterExternalControlInboundPortURI	URI of the inbound port to call the heater component for external control.
	 * @param heaterSensorInboundPortURI			URI of the inbound port to call the heater component sensors.
	 * @param heaterActuatorInboundPortURI			URI of the inbound port to call the heater component actuators.
	 * @param currentExecutionType					execution type for the next run.
	 * @param currentSimulationType					simulation type for the next run.
	 * @param globalArchitectureURI					URI of the global simulation architecture to be created or the empty string if the component does not execute as a simulation.
	 * @param localArchitectureURI					URI of the local simulation architecture to be used in the global simulation architecture.
	 * @param simulationTimeUnit					time unit in which {@code simulatedStartTime} and {@code simulationDuration} are expressed.
	 * @param accFactor								acceleration factor for the simulation.
	 * @param clockURI								URI of the clock to be used to synchronise the test scenarios and the simulation.
	 * @throws Exception							<i>to do</i>.
	 */
	protected			Heater(
		String reflectionInboundPortURI,
		String heaterUserInboundPortURI,
		String heaterInternalControlInboundPortURI,
		String heaterExternalControlInboundPortURI,
		String heaterSensorInboundPortURI,
		String heaterActuatorInboundPortURI,
		ExecutionType currentExecutionType,
		SimulationType currentSimulationType,
		String globalArchitectureURI,
		String localArchitectureURI,
		TimeUnit simulationTimeUnit,
		double accFactor,
		String clockURI
		) throws Exception
	{
		super(reflectionInboundPortURI, 2, 1);

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
		this.clock = new CompletableFuture<AcceleratedClock>();

		this.initialise(heaterUserInboundPortURI,
						heaterInternalControlInboundPortURI,
						heaterExternalControlInboundPortURI,
						heaterSensorInboundPortURI,
						heaterActuatorInboundPortURI);

		assert	Heater.glassBoxInvariants(this) :
				new InvariantException("Heater.glassBoxInvariants(this)");
		assert	Heater.blackBoxInvariants(this) :
				new InvariantException("Heater.blackBoxInvariants(this)");
	}

	/**
	 * initialise a new thermostated heater.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code heaterUserInboundPortURI != null && !heaterUserInboundPortURI.isEmpty()}
	 * pre	{@code heaterInternalControlInboundPortURI != null && !heaterInternalControlInboundPortURI.isEmpty()}
	 * pre	{@code heaterExternalControlInboundPortURI != null && !heaterExternalControlInboundPortURI.isEmpty()}
	 * pre	{@code heaterSensorInboundPortURI != null && !heaterSensorInboundPortURI.isEmpty()}
	 * pre	{@code heaterActuatorInboundPortURI != null && !heaterActuatorInboundPortURI.isEmpty()}
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @param heaterUserInboundPortURI				URI of the inbound port to call the heater component for user interactions.
	 * @param heaterInternalControlInboundPortURI	URI of the inbound port to call the heater component for internal control.
	 * @param heaterExternalControlInboundPortURI	URI of the inbound port to call the heater component for external control.
	 * @param heaterSensorInboundPortURI			URI of the inbound port to call the heater component sensors.
	 * @param heaterActuatorInboundPortURI			URI of the inbound port to call the heater component actuators.
	 * @throws Exception							<i>to do</i>.
	 */
	protected void		initialise(
		String heaterUserInboundPortURI,
		String heaterInternalControlInboundPortURI,
		String heaterExternalControlInboundPortURI,
		String heaterSensorInboundPortURI,
		String heaterActuatorInboundPortURI
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
		assert	heaterSensorInboundPortURI != null &&
									!heaterSensorInboundPortURI.isEmpty() :
				new PreconditionException(
						"heaterSensorInboundPortURI != null &&"
						+ "!heaterSensorInboundPortURI.isEmpty()");
		assert	heaterActuatorInboundPortURI != null &&
									!heaterActuatorInboundPortURI.isEmpty() :
				new PreconditionException(
						"heaterActuatorInboundPortURI != null && "
						+ "!heaterActuatorInboundPortURI.isEmpty()");

		this.currentState = HeaterState.OFF;
		this.currentPowerLevel = MAX_POWER_LEVEL;
		this.targetTemperature = STANDARD_TARGET_TEMPERATURE;

		this.hip = new HeaterUserInboundPort(heaterUserInboundPortURI, this);
		this.hip.publishPort();
		this.hicip = new HeaterInternalControlInboundPort(
									heaterInternalControlInboundPortURI, this);
		this.hicip.publishPort();
		this.hecip = new HeaterExternalControlInboundPort(
									heaterExternalControlInboundPortURI, this);
		this.hecip.publishPort();
		this.sensorInboundPort = new HeaterSensorDataInboundPort(
									heaterSensorInboundPortURI, this);
		this.sensorInboundPort.publishPort();
		this.actuatorInboundPort = new HeaterActuatorInboundPort(
									heaterActuatorInboundPortURI, this);
		this.actuatorInboundPort.publishPort();

		// create the simulation architecture given the current type of
		// simulation i.e., for the current execution
		switch (this.currentSimulationType) {
		case MIL_SIMULATION:
			Architecture architecture = null;
			if (this.currentExecutionType.isUnitTest()) {
				architecture =
					LocalSimulationArchitectures.
						createHeaterMILLocalArchitecture4UnitTest(
													this.localArchitectureURI,
													this.simulationTimeUnit);
			} else {
				architecture =
						LocalSimulationArchitectures.
							createHeaterMILLocalArchitecture4IntegrationTest(
													this.localArchitectureURI,
													this.simulationTimeUnit);
			}
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
		case SIL_SIMULATION:
			architecture = null;
			if (this.currentExecutionType.isUnitTest()) {
				architecture =
					LocalSimulationArchitectures.
						createHeater_RT_LocalArchitecture4UnitTest(
													this.currentSimulationType,
													this.localArchitectureURI,
													this.simulationTimeUnit,
													this.accFactor);
			} else {
				architecture =
					LocalSimulationArchitectures.
						createHeater_RT_LocalArchitecture4IntegrationTest(
													this.currentSimulationType,
													this.localArchitectureURI,
													this.simulationTimeUnit,
													this.accFactor);
			}
			assert	architecture.getRootModelURI().equals(
												this.localArchitectureURI) :
					new BCMException(
							"local simulator " + this.localArchitectureURI
							+ " does not exist!");
			this.addLocalSimulatorArchitecture(architecture);
			this.global2localSimulationArchitectureURIS.
				put(this.globalArchitectureURI, this.localArchitectureURI);
			break;
		case NO_SIMULATION:
		default:
		}

		if (VERBOSE) {
			this.tracer.get().setTitle("Heater component");
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
				// For SIL simulations, we use the ModelStateAccessI protocol
				// to provide the access to the current temperature computed
				// by the HeaterTemperatureModel.
				this.asp = new RTAtomicSimulatorPlugin() {
					private static final long serialVersionUID = 1L;
					/**
					 * @see fr.sorbonne_u.components.cyphy.plugins.devs.AtomicSimulatorPlugin#getModelStateValue(java.lang.String, java.lang.String)
					 */
					@Override
					public Object	getModelStateValue(
						String modelURI,
						String name
						) throws Exception
					{
						assert	modelURI.equals(HeaterTemperatureModel.SIL_URI);
						assert	name.equals(CURRENT_TEMPERATURE_NAME);
						return ((HeaterTemperatureModel)
										this.atomicSimulators.get(modelURI).
												getSimulatedModel()).
														getCurrentTemperature();
					}
				};
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
		if (!this.currentExecutionType.isStandard()) {
			ClocksServerWithSimulationOutboundPort clockServerOBP =
							new ClocksServerWithSimulationOutboundPort(this);
			clockServerOBP.publishPort();
			this.doPortConnection(
					clockServerOBP.getPortURI(),
					ClocksServer.STANDARD_INBOUNDPORT_URI,
					ClocksServerWithSimulationConnector.class.getCanonicalName());
			AcceleratedAndSimulationClock clock =
					clockServerOBP.getClockWithSimulation(this.clockURI);
			this.doPortDisconnection(clockServerOBP.getPortURI());
			clockServerOBP.unpublishPort();
			clockServerOBP.destroyPort();

			// the computable future clock is used to make sure that the
			// accelerated clock is available when the internal controller
			// wants to start the temperature sensor in push mode, as the
			// clock is needed to time stamp the sensor data properly
			this.clock.complete(clock);

			if (this.currentExecutionType.isUnitTest() &&
								this.currentSimulationType.isSILSimulation()) {
				this.asp.createSimulator();
				this.asp.setSimulationRunParameters(new HashMap<>());
				this.asp.initialiseSimulation(
								new Time(clock.getSimulatedStartTime(),
										 clock.getSimulatedTimeUnit()),
								new Duration(clock.getSimulatedDuration(),
											 clock.getSimulatedTimeUnit()));
				// compute the real time of start of the simulation using the
				// accelerated clock
				long simulationStartTimeInMillis = 
						TimeUnit.NANOSECONDS.toMillis(
										clock.getSimulationStartEpochNanos());
				assert	simulationStartTimeInMillis > System.currentTimeMillis() :
						new BCMException(
								"simulationStartTimeInMillis > "
								+ "System.currentTimeMillis()");
				this.asp.startRTSimulation(simulationStartTimeInMillis,
										   clock.getSimulatedStartTime(),
										   clock.getSimulatedDuration());
				// rather than waiting and sleeping, a task could also be
				// scheduled to free the current thread
				clock.waitUntilSimulationEnd();
				// leave some time for the simulators to perform end of
				// simulation catering tasks
				Thread.sleep(250L);
				this.logMessage(this.asp.getFinalReport().toString());
			}
		}
	}

	/**
	 * @see fr.sorbonne_u.components.AbstractComponent#shutdown()
	 */
	@Override
	public synchronized void	shutdown() throws ComponentShutdownException
	{
		try {
			this.hip.unpublishPort();
			this.hicip.unpublishPort();
			this.hecip.unpublishPort();
			this.sensorInboundPort.unpublishPort();
			this.actuatorInboundPort.unpublishPort();
		} catch (Exception e) {
			throw new ComponentShutdownException(e) ;
		}
		super.shutdown();
	}

	// -------------------------------------------------------------------------
	// Component services implementation
	// -------------------------------------------------------------------------

	/**
	 * @see fr.sorbonne_u.components.hem2024e1.equipments.heater.HeaterUserI#on()
	 */
	@Override
	public boolean		on() throws Exception
	{
		if (Heater.VERBOSE) {
			this.traceMessage("Heater#on() current state: " +
											this.currentState + ".\n");
		}
		return this.currentState == HeaterState.ON ||
									this.currentState == HeaterState.HEATING;
	}

	/**
	 * @see fr.sorbonne_u.components.hem2024e1.equipments.heater.HeaterUserI#switchOn()
	 */
	@Override
	public void			switchOn() throws Exception
	{
		assert	!on() : new PreconditionException("!on()");

		if (Heater.VERBOSE) {
			this.traceMessage("Heater switches on.\n");
		}

		this.currentState = HeaterState.ON;

		if (this.currentSimulationType.isSILSimulation()) {
			// For SIL simulation, an operation done in the component code
			// must be reflected in the simulation; to do so, the component
			// code triggers an external event sent to the HairDryerStateModel
			// to make it change its state to on.
			((RTAtomicSimulatorPlugin)this.asp).triggerExternalEvent(
												HeaterStateModel.SIL_URI,
												t -> new SwitchOnHeater(t));
		}

		// this will send to the heater internal controller the signal that the
		// heater is now on, hence starting the control loop
		this.sensorInboundPort.send(
				new HeaterSensorData<HeaterStateMeasure>(
						new HeaterStateMeasure(this.currentState)));

		assert	 on() : new PostconditionException("on()");
	}

	/**
	 * @see fr.sorbonne_u.components.hem2024e1.equipments.heater.HeaterUserI#switchOff()
	 */
	@Override
	public void			switchOff() throws Exception
	{
		assert	on() : new PreconditionException("on()");

		if (Heater.VERBOSE) {
			this.traceMessage("Heater switches off.\n");
		}

		this.currentState = HeaterState.OFF;

		if (this.currentSimulationType.isSILSimulation()) {
			// For SIL simulation, an operation done in the component code
			// must be reflected in the simulation; to do so, the component
			// code triggers an external event sent to the HairDryerStateModel
			// to make it change its state to on.
			((RTAtomicSimulatorPlugin)this.asp).triggerExternalEvent(
												HeaterStateModel.SIL_URI,
												t -> new SwitchOffHeater(t));
		}

		// this will send to the heater internal controller the signal that the
		// heater is now off, hence stopping the control loop
		this.sensorInboundPort.send(
				new HeaterSensorData<HeaterStateMeasure>(
						new HeaterStateMeasure(this.currentState)));

		assert	 !on() : new PostconditionException("!on()");
	}

	/**
	 * @see fr.sorbonne_u.components.hem2024e1.equipments.heater.HeaterUserI#setTargetTemperature(double)
	 */
	@Override
	public void			setTargetTemperature(double target) throws Exception
	{
		assert	target >= -50.0 && target <= 50.0 :
				new PreconditionException("target >= -50.0 && target <= 50.0");

		if (Heater.VERBOSE) {
			this.traceMessage("Heater sets a new target "
										+ "temperature: " + target + ".\n");
		}

		this.targetTemperature = target;

		assert	getTargetTemperature() == target :
				new PostconditionException("getTargetTemperature() == target");
	}

	/**
	 * @see fr.sorbonne_u.components.hem2024e1.equipments.heater.HeaterTemperatureI#getTargetTemperature()
	 */
	@Override
	public double		getTargetTemperature() throws Exception
	{
		double ret = this.targetTemperature;

		if (Heater.VERBOSE) {
			this.traceMessage("Heater returns its target"
											+ " temperature " + ret + ".\n");
		}

		assert	ret >= -50.0 && ret <= 50.0 :
				new PostconditionException(
						"return >= -50.0 && return <= 50.0");

		return ret;
	}

	/**
	 * @see fr.sorbonne_u.components.hem2024e1.equipments.heater.HeaterTemperatureI#getCurrentTemperature()
	 */
	@Override
	public double		getCurrentTemperature() throws Exception
	{
		assert	on() : new PreconditionException("on()");

		// Temporary implementation; would need a temperature sensor.
		double currentTemperature = FAKE_CURRENT_TEMPERATURE;

		if (this.currentSimulationType.isSILSimulation()) {
			// For SIL simulation, the value is got from the
			// HeaterTemperatureModel through the ModelStateAccessI interface
			currentTemperature = 
					(double)((RTAtomicSimulatorPlugin)this.asp).
										getModelStateValue(
												HeaterTemperatureModel.SIL_URI,
												CURRENT_TEMPERATURE_NAME);
		}

		if (Heater.VERBOSE) {
			this.traceMessage("Heater returns the current"
							+ " temperature " + currentTemperature + ".\n");
		}

		return currentTemperature;
	}

	/**
	 * @see fr.sorbonne_u.components.hem2024e1.equipments.heater.HeaterInternalControlI#heating()
	 */
	@Override
	public boolean		heating() throws Exception
	{
		assert	on() : new PreconditionException("on()");

		if (Heater.VERBOSE) {
			this.traceMessage("Heater returns its heating status " + 
						(this.currentState == HeaterState.HEATING) + ".\n");
		}

		return this.currentState == HeaterState.HEATING;
	}

	/**
	 * @see fr.sorbonne_u.components.hem2024e1.equipments.heater.HeaterInternalControlI#startHeating()
	 */
	@Override
	public void			startHeating() throws Exception
	{
		assert	on() : new PreconditionException("on()");
		assert	!heating() : new PreconditionException("!heating()");

		if (Heater.VERBOSE) {
			this.traceMessage("Heater starts heating.\n");
		}

		this.currentState = HeaterState.HEATING;

		if (this.currentSimulationType.isSILSimulation()) {
			// For SIL simulation, an operation done in the component code
			// must be reflected in the simulation; to do so, the component
			// code triggers an external event sent to the HairDryerStateModel
			// to make it change its state to on.
			((RTAtomicSimulatorPlugin)this.asp).triggerExternalEvent(
												HeaterStateModel.SIL_URI,
												t -> new Heat(t));
		}

		// this will send to the heater internal controller the signal that the
		// heater is now heating
		this.sensorInboundPort.send(
				new HeaterSensorData<HeaterStateMeasure>(
						new HeaterStateMeasure(this.currentState)));

		assert	heating() : new PostconditionException("heating()");
	}

	/**
	 * @see fr.sorbonne_u.components.hem2024e1.equipments.heater.HeaterInternalControlI#stopHeating()
	 */
	@Override
	public void			stopHeating() throws Exception
	{
		assert	on() : new PreconditionException("on()");
		assert	heating() : new PreconditionException("heating()");

		if (Heater.VERBOSE) {
			this.traceMessage("Heater stops heating.\n");
		}

		this.currentState = HeaterState.ON;

		if (this.currentSimulationType.isSILSimulation()) {
			// For SIL simulation, an operation done in the component code
			// must be reflected in the simulation; to do so, the component
			// code triggers an external event sent to the HairDryerStateModel
			// to make it change its state to on.
			((RTAtomicSimulatorPlugin)this.asp).triggerExternalEvent(
												HeaterStateModel.SIL_URI,
												t -> new DoNotHeat(t));
		}

		// this will send to the heater internal controller the signal that the
		// heater has now stopped heating
		this.sensorInboundPort.send(
				new HeaterSensorData<HeaterStateMeasure>(
						new HeaterStateMeasure(this.currentState)));

		assert	!heating() : new PostconditionException("!heating()");
	}

	/**
	 * @see fr.sorbonne_u.components.hem2024e1.equipments.heater.HeaterExternalControlI#getMaxPowerLevel()
	 */
	@Override
	public double		getMaxPowerLevel() throws Exception
	{
		if (Heater.VERBOSE) {
			this.traceMessage("Heater returns its max power level " + 
					MAX_POWER_LEVEL + ".\n");
		}

		return MAX_POWER_LEVEL;
	}

	/**
	 * @see fr.sorbonne_u.components.hem2024e1.equipments.heater.HeaterExternalControlI#setCurrentPowerLevel(double)
	 */
	@Override
	public void			setCurrentPowerLevel(double powerLevel)
	throws Exception
	{
		assert	powerLevel >= 0.0 :
				new PreconditionException("powerLevel >= 0.0");

		if (Heater.VERBOSE) {
			this.traceMessage("Heater sets its power level to " + 
														powerLevel + ".\n");
		}

		if (powerLevel <= getMaxPowerLevel()) {
			this.currentPowerLevel = powerLevel;
		} else {
			this.currentPowerLevel = MAX_POWER_LEVEL;
		}

		if (this.currentSimulationType.isSILSimulation()) {
			// For SIL simulation, an operation done in the component code
			// must be reflected in the simulation; to do so, the component
			// code triggers an external event sent to the HairDryerStateModel
			// to make it change its state to on.
			PowerValue pv = new PowerValue(this.currentPowerLevel);
			((RTAtomicSimulatorPlugin)this.asp).triggerExternalEvent(
												HeaterStateModel.SIL_URI,
												t -> new SetPowerHeater(t, pv));
		}

		assert	powerLevel > getMaxPowerLevel() ||
										getCurrentPowerLevel() == powerLevel :
				new PostconditionException(
						"powerLevel > getMaxPowerLevel() || "
						+ "getCurrentPowerLevel() == powerLevel");
	}

	/**
	 * @see fr.sorbonne_u.components.hem2024e1.equipments.heater.HeaterExternalControlI#getCurrentPowerLevel()
	 */
	@Override
	public double		getCurrentPowerLevel() throws Exception
	{
		double ret = this.currentPowerLevel;

		if (Heater.VERBOSE) {
			this.traceMessage("Heater returns its current power level " + 
																ret + ".\n");
		}

		assert	ret >= 0.0 && ret <= getMaxPowerLevel() :
				new PostconditionException(
							"return >= 0.0 && return <= getMaxPowerLevel()");

		return this.currentPowerLevel;
	}

	// -------------------------------------------------------------------------
	// Component sensors
	// -------------------------------------------------------------------------

	/**
	 * return the heating status of the heater as a sensor data.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code on()}
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @return				the heating status of the heater as a sensor data.
	 * @throws Exception	<i>to do</i>.
	 */
	public HeaterSensorData<Measure<Boolean>>	heatingPullSensor()
	throws Exception
	{
		return new HeaterSensorData<Measure<Boolean>>(
										new Measure<Boolean>(this.heating()));
	}

	/**
	 * return the target temperature as a sensor data.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code true}	// no precondition.
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @return				the target temperature as a sensor data.
	 * @throws Exception	<i>to do</i>.
	 */
	public HeaterSensorData<Measure<Double>>	targetTemperaturePullSensor()
	throws Exception
	{
		return new HeaterSensorData<Measure<Double>>(
						new Measure<Double>(this.getTargetTemperature(),
											MeasurementUnit.CELSIUS));
	}

	/**
	 * return the current temperature as a sensor data.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code on()}
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @return				the current temperature as a sensor data.
	 * @throws Exception	<i>to do</i>.
	 */
	public HeaterSensorData<Measure<Double>>	currentTemperaturePullSensor()
	throws Exception
	{
		return new HeaterSensorData<Measure<Double>>(
						new Measure<Double>(this.getCurrentTemperature(),
											MeasurementUnit.CELSIUS));
	}

	/**
	 * start a sequence of temperatures pushes with the given period.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code controlPeriod > 0}
	 * pre	{@code tu != null}
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @param controlPeriod	period at which the pushes must be made.
	 * @param tu			time unit in which {@code controlPeriod} is expressed.
	 * @throws Exception	<i>to do</i>.
	 */
	public void			startTemperaturesPushSensor(
		long controlPeriod,
		TimeUnit tu
		) throws Exception
	{
		long actualControlPeriod = -1L;
		if (this.currentExecutionType.isStandard()) {
			actualControlPeriod = (long)(controlPeriod * tu.toNanos(1));
		} else {
			// this will synchronise the start of the push sensor with the
			// availability of the clock, required to compute the actual push
			// period with the correct acceleration factor
			AcceleratedClock ac = this.clock.get();
			// the accelerated period is in nanoseconds, hence first convert
			// the period to nanoseconds, perform the division and then
			// convert to long (hence providing a better precision than
			// first dividing and then converting to nanoseconds...)
			actualControlPeriod =
					(long)((controlPeriod * tu.toNanos(1))/
											ac.getAccelerationFactor());
			// sanity checking, the standard Java scheduler has a
			// precision no less than 10 milliseconds...
			if (actualControlPeriod < TimeUnit.MILLISECONDS.toNanos(10)) {
				System.out.println(
					"Warning: accelerated control period is "
							+ "too small ("
							+ actualControlPeriod +
							"), unexpected scheduling problems may"
							+ " occur!");
			}
		}
		this.temperaturesPushSensorTask(actualControlPeriod);
	}

	/**
	 * if the heater is not off, perform one push and schedule the next.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code actualControlPeriod > 0}
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @param actualControlPeriod	period at which the push sensor must be triggered.
	 * @throws Exception			<i>to do</i>.
	 */
	protected void		temperaturesPushSensorTask(long actualControlPeriod)
	throws Exception
	{
		assert	actualControlPeriod > 0 :
				new PreconditionException("actualControlPeriod > 0");

		if (this.currentState != HeaterState.OFF) {
			this.traceMessage("Heater performs a new temperatures push.\n");
			this.temperaturesPushSensor();
			if (this.currentExecutionType.isStandard()
					|| this.currentSimulationType.isSILSimulation()
					|| this.currentSimulationType.isHILSimulation()) {
				// schedule the next execution of the loop only if the
				// current execution is standard or if it is a real time
				// simulation with code execution i.e., SIL or HIL
				// otherwise, perform only one call to push sensors to
				// test the functionality
				this.scheduleTaskOnComponent(
					new AbstractComponent.AbstractTask() {
						@Override
						public void run() {
							try {
								temperaturesPushSensorTask(actualControlPeriod);
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
					},
					actualControlPeriod,
					TimeUnit.NANOSECONDS);
			}
		}
	}

	/**
	 * sends the compound measure of the target and the current temperatures
	 * through the push sensor interface.
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
	protected void		temperaturesPushSensor() throws Exception
	{
		this.sensorInboundPort.send(
					new HeaterSensorData<HeaterCompoundMeasure>(
						new HeaterCompoundMeasure(
							this.targetTemperaturePullSensor().getMeasure(),
							this.currentTemperaturePullSensor().getMeasure())));
	}
}
// -----------------------------------------------------------------------------
