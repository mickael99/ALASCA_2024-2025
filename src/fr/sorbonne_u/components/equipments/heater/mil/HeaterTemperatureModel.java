package fr.sorbonne_u.components.equipments.heater.mil;

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

import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import fr.sorbonne_u.components.cyphy.plugins.devs.AtomicSimulatorPlugin;
import fr.sorbonne_u.devs_simulation.exceptions.MissingRunParameterException;
import fr.sorbonne_u.devs_simulation.exceptions.NeoSim4JavaException;
import fr.sorbonne_u.devs_simulation.hioa.annotations.ImportedVariable;
import fr.sorbonne_u.devs_simulation.hioa.annotations.InternalVariable;
import fr.sorbonne_u.devs_simulation.hioa.annotations.ModelImportedVariable;
import fr.sorbonne_u.devs_simulation.hioa.models.AtomicHIOA;
import fr.sorbonne_u.devs_simulation.hioa.models.vars.DerivableValue;
import fr.sorbonne_u.devs_simulation.hioa.models.vars.Value;
import fr.sorbonne_u.devs_simulation.models.annotations.ModelExternalEvents;
import fr.sorbonne_u.devs_simulation.models.events.Event;
import fr.sorbonne_u.devs_simulation.models.events.EventI;
import fr.sorbonne_u.devs_simulation.models.time.Duration;
import fr.sorbonne_u.devs_simulation.models.time.Time;
import fr.sorbonne_u.devs_simulation.simulators.interfaces.AtomicSimulatorI;
import fr.sorbonne_u.devs_simulation.simulators.interfaces.SimulationReportI;
import fr.sorbonne_u.devs_simulation.utils.Pair;
import fr.sorbonne_u.devs_simulation.utils.StandardLogger;
import fr.sorbonne_u.exceptions.InvariantChecking;
import fr.sorbonne_u.components.equipments.heater.mil.HeaterStateModel.State;
import fr.sorbonne_u.components.equipments.heater.mil.events.*;
import fr.sorbonne_u.components.equipments.hem.mil.HEM_ReportI;

// -----------------------------------------------------------------------------
/**
 * The class <code>HeaterTemperatureModel</code> defines a simulation model
 * for the temperature inside a room equipped with a heater.
 *
 * <p><strong>Description</strong></p>
 * 
 * <p>
 * The model is implemented as an atomic HIOA model. A differential equation
 * defines the temperature variation over time. It uses a very simple
 * mathematical model where the derivative is proportional to the difference
 * between the current temperature and the temperature that influences the
 * current one. In fact, there are two temperatures that influences the current
 * temperature of the room:
 * </p>
 * <ol>
 * <li>the temperature outside the house (room) where the coefficient
 *   applied to the difference between the outside temperature and the
 *   current temperature models the thermal insulation of the walls
 *   ({@code INSULATION_TRANSFER_CONSTANT});</li>
 * <li>the temperature of the heater when it heats where the coefficient
 *   applied to the difference between the heater temperature
 *   ({@code STANDARD_HEATING_TEMP}) and the current temperature models the
 *   heat diffusion over the house (room)
 *   ({@code HEATING_TRANSFER_CONSTANT}); the heat diffusion is not constant
 *   but rather proportional to the current power level of the heater.</li>
 * </ol>
 * <p>
 * The resulting differential equation is integrated using the Euler method
 * with a predefined integration step. The initial state of the model is
 * a state not heating and the initial temperature given by
 * {@code INITIAL_TEMPERATURE}.
 * </p>
 * <p>
 * Whether the current temperature evolves under the influence of the outside
 * temperature only or also the heating temperature depends upon the state,
 * which in turn is modified through the reception of imported events
 * {@code Heat} and {@code DoNotHeat}. The external temperature is imported
 * from another model simulating the environment. The current temperature is
 * exported to be used by other models.
 * </p>
 * 
 * <ul>
 * <li>Imported events:
 *   {@code SwitchOffHeater},
 *   {@code Heat},
 *   {@code DoNotHeat},
 *   {{@code SetPowerHeater}</li>
 * <li>Exported events: none</li>
 * <li>Imported variables: none</li>
 * <li>Exported variables:
 *   name = {@code externalTemperature}, type = {@code Double}</li>
 * </ul>
 * 
 * <p><strong>White-box Invariant</strong></p>
 * 
 * <pre>
 * invariant	{@code TEMPERATURE_UPDATE_TOLERANCE >= 0.0}
 * invariant	{@code POWER_HEAT_TRANSFER_TOLERANCE >= 0.0}
 * invariant	{@code INSULATION_TRANSFER_CONSTANT > 0.0}
 * invariant	{@code MIN_HEATING_TRANSFER_CONSTANT > 0.0}
 * invariant	{@code STEP > 0.0}
 * invariant	{@code currentState != null}
 * invariant	{@code integrationStep.getSimulatedDuration() > 0.0}
 * invariant	{@code !isStateInitialised() || start != null}
 * invariant	{@code currentHeatingPower == null || !currentHeatingPower.isInitialised() || currentHeatingPower.getValue() >= 0.0}
 * invariant	{@code currentTemperature != null}
 * </pre>
 * 
 * <p><strong>Black-box Invariant</strong></p>
 * 
 * <pre>
 * invariant	{@code MIL_URI != null && !MIL_URI.isEmpty()}
 * invariant	{@code MIL_RT_URI != null && !MIL_RT_URI.isEmpty()}
 * invariant	{@code SIL_URI != null && !SIL_URI.isEmpty()}
 * </pre>
 * 
 * <p>Created on : 2023-09-29</p>
 * 
 * @author	<a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
 */
@ModelExternalEvents(imported = {SwitchOffHeater.class,
								 SetPowerHeater.class,
		 						 Heat.class,
		 						 DoNotHeat.class})
@ModelImportedVariable(name = "externalTemperature", type = Double.class)
// -----------------------------------------------------------------------------
public class			HeaterTemperatureModel
extends		AtomicHIOA
implements	HeaterOperationI
{
	// -------------------------------------------------------------------------
	// Constants and variables
	// -------------------------------------------------------------------------

	private static final long		serialVersionUID = 1L;

	// The following variables should be considered constant but can be changed
	// before the first model instance is created to adapt the simulation
	// scenario.

	/** URI for a MIL model; works when only one instance is created.		*/
	public static String		MIL_URI = HeaterTemperatureModel.class.
													getSimpleName() + "-MIL";
	/** URI for a MIL real time model; works when only one instance is
	 *  created.															*/
	public static String		MIL_RT_URI = HeaterTemperatureModel.class.
													getSimpleName() + "-MIL-RT";
	/** URI for a SIL model; works when only one instance is created.		*/
	public static String		SIL_URI = HeaterTemperatureModel.class.
													getSimpleName() + "-SIL";

	/** temperature of the room (house) when the simulation begins.			*/
	public static double		INITIAL_TEMPERATURE = 19.005;
	/** wall insulation heat transfer constant in the differential equation.*/
	protected static double 	INSULATION_TRANSFER_CONSTANT = 12.5;
	/** heating transfer constant in the differential equation when the
	 *  heating power is maximal.											*/
	protected static double		MIN_HEATING_TRANSFER_CONSTANT = 40.0;
	/** temperature of the heating plate in the heater.						*/
	protected static double		STANDARD_HEATING_TEMP = 300.0;
	/** update tolerance for the temperature <i>i.e.</i>, shortest elapsed
	 *  time since the last update under which the temperature is not
	 *  changed by the update to avoid too large computation errors.		*/
	protected static double		TEMPERATURE_UPDATE_TOLERANCE = 0.0001;
	/** the minimal power under which the temperature derivative must be 0.	*/
	protected static double		POWER_HEAT_TRANSFER_TOLERANCE = 0.0001;
	/** integration step for the differential equation(assumed in hours).	*/
	protected static double		STEP = 60.0/3600.0;	// 60 seconds

	/** current state of the heater; from the temperature point of view
	 *  State.OFF is assimilated to State.ON <i>i.e.</i>, not heating.		*/
	protected State				currentState = State.ON;

	// Simulation run variables

	/** integration step as a duration, including the time unit.			*/
	protected final Duration	integrationStep;
	/** accumulator to compute the mean external temperature for the
	 *  simulation report.													*/
	protected double			temperatureAcc;
	/** the simulation time of start used to compute the mean temperature.	*/
	protected Time				start;
	/** the mean temperature over the simulation duration for the simulation
	 *  report.																*/
	protected double			meanTemperature;

	// -------------------------------------------------------------------------
	// HIOA model variables
	// -------------------------------------------------------------------------

	/** current external temperature in Celsius.							*/
	@ImportedVariable(type = Double.class)
	protected Value<Double>					externalTemperature;
	/** the current heating power between 0 and
	 *  {@code HeaterElectricityModel.MAX_HEATING_POWER}.					*/
	@InternalVariable(type = Double.class)
	protected Value<Double>					currentHeatingPower =
												new Value<Double>(this);
	/** current temperature in the room.									*/
	@InternalVariable(type = Double.class)
	protected final DerivableValue<Double>	currentTemperature =
												new DerivableValue<Double>(this);

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
	protected static boolean	glassBoxInvariants(
		HeaterTemperatureModel instance
		)
	{
		assert	instance != null :
				new NeoSim4JavaException("Precondition violation: "
						+ "instance != null");

		boolean ret = true;
		ret &= InvariantChecking.checkGlassBoxInvariant(
				TEMPERATURE_UPDATE_TOLERANCE >= 0.0,
				HeaterTemperatureModel.class,
				instance,
				"TEMPERATURE_UPDATE_TOLERANCE >= 0.0");
		ret &= InvariantChecking.checkGlassBoxInvariant(
				POWER_HEAT_TRANSFER_TOLERANCE >= 0.0,
				HeaterTemperatureModel.class,
				instance,
				"POWER_HEAT_TRANSFER_TOLERANCE >= 0.0");
		ret &= InvariantChecking.checkGlassBoxInvariant(
				INSULATION_TRANSFER_CONSTANT > 0.0,
				HeaterTemperatureModel.class,
				instance,
				"INSULATION_TRANSFER_CONSTANT > 0.0");
		ret &= InvariantChecking.checkGlassBoxInvariant(
				MIN_HEATING_TRANSFER_CONSTANT > 0.0,
				HeaterTemperatureModel.class,
				instance,
				"MIN_HEATING_TRANSFER_CONSTANT > 0.0");
		ret &= InvariantChecking.checkGlassBoxInvariant(
				STEP > 0.0,
				HeaterTemperatureModel.class,
				instance,
				"STEP > 0.0");
		ret &= InvariantChecking.checkGlassBoxInvariant(
				instance.currentState != null,
				HeaterTemperatureModel.class,
				instance,
				"currentState != null");
		ret &= InvariantChecking.checkGlassBoxInvariant(
				instance.integrationStep.getSimulatedDuration() > 0.0,
				HeaterTemperatureModel.class,
				instance,
				"integrationStep.getSimulatedDuration() > 0.0");
		ret &= InvariantChecking.checkGlassBoxInvariant(
				!instance.isStateInitialised() || instance.start != null,
				HeaterTemperatureModel.class,
				instance,
				"!isStateInitialised() || start != null");
		ret &= InvariantChecking.checkGlassBoxInvariant(
				instance.currentHeatingPower == null ||
					(!instance.currentHeatingPower.isInitialised() ||
								instance.currentHeatingPower.getValue() >= 0.0),
				HeaterTemperatureModel.class,
				instance,
				"currentHeatingPower == null || "
				+ "(!currentHeatingPower.isInitialised() || "
				+ "currentHeatingPower.getValue() >= 0.0)");
		ret &= InvariantChecking.checkGlassBoxInvariant(
				instance.currentTemperature != null,
				HeaterTemperatureModel.class,
				instance,
				"currentTemperature != null");
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
	protected static boolean	blackBoxInvariants(
		HeaterTemperatureModel instance
		)
	{
		assert	instance != null :
				new NeoSim4JavaException("Precondition violation: "
						+ "instance != null");

		boolean ret = true;
		ret &= InvariantChecking.checkBlackBoxInvariant(
					MIL_URI != null && !MIL_URI.isEmpty(),
					HeaterTemperatureModel.class,
					instance,
					"MIL_URI != null && !MIL_URI.isEmpty()");
		ret &= InvariantChecking.checkBlackBoxInvariant(
					MIL_RT_URI != null && !MIL_RT_URI.isEmpty(),
					HeaterTemperatureModel.class,
					instance,
					"MIL_RT_URI != null && !MIL_RT_URI.isEmpty()");
		ret &= InvariantChecking.checkBlackBoxInvariant(
					SIL_URI != null && !SIL_URI.isEmpty(),
					HeaterTemperatureModel.class,
					instance,
					"SIL_URI != null && !SIL_URI.isEmpty()");
		return ret;
	}

	// -------------------------------------------------------------------------
	// Constructors
	// -------------------------------------------------------------------------

	/**
	 * create a <code>HeaterTemperatureModel</code> instance.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code uri == null || !uri.isEmpty()}
	 * pre	{@code simulatedTimeUnit != null}
	 * pre	{@code simulationEngine == null || !simulationEngine.isModelSet()}
	 * pre	{@code simulationEngine == null || simulationEngine instanceof AtomicEngine}
	 * post	{@code !isDebugModeOn()}
	 * post	{@code getURI() != null && !getURI().isEmpty()}
	 * post	{@code uri == null || getURI().equals(uri)}
	 * post	{@code getSimulatedTimeUnit().equals(simulatedTimeUnit)}
	 * post	{@code getSimulationEngine().equals(simulationEngine)}
	 * </pre>
	 *
	 * @param uri				URI of the model.
	 * @param simulatedTimeUnit	time unit used for the simulation time.
	 * @param simulationEngine	simulation engine to which the model is attached.
	 * @throws Exception		<i>to do</i>.
	 */
	public				HeaterTemperatureModel(
		String uri,
		TimeUnit simulatedTimeUnit,
		AtomicSimulatorI simulationEngine
		) throws Exception
	{
		super(uri, simulatedTimeUnit, simulationEngine);
		this.integrationStep = new Duration(STEP, simulatedTimeUnit);
		this.getSimulationEngine().setLogger(new StandardLogger());

		assert	glassBoxInvariants(this) :
				new NeoSim4JavaException(
						"HeaterTemperatureModel.glassBoxInvariants(this)");
		assert	blackBoxInvariants(this) :
				new NeoSim4JavaException(
						"HeaterTemperatureModel.blackBoxInvariants(this)");
	}

	// -------------------------------------------------------------------------
	// Internal methods
	// -------------------------------------------------------------------------

	/**
	 * compute the current heat transfer constant given the current heating
	 * power of the heater.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code true}	// no precondition.
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @return	the current heat transfer constant.
	 */
	protected double	currentHeatTransfertConstant()
	{
		// the following formula is just a mathematical trick to get a heat
		// transfer constant that grows as the power gets lower, hence the
		// derivative given by the differential equation will be lower when
		// the power gets lower, what is physically awaited.
		double c = 1.0/(MIN_HEATING_TRANSFER_CONSTANT*
									HeaterElectricityModel.MAX_HEATING_POWER);
		return 1.0/(c*this.currentHeatingPower.getValue());
	}

	/**
	 * compute the current derivative of the room temperature.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code true}	// no precondition.
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @param current	current temperature of the room.
	 * @return			the current derivative.
	 */
	protected double	computeDerivatives(Double current)
	{
		double currentTempDerivative = 0.0;
		if (this.currentState == State.HEATING) {
			// the heating contribution: temperature difference between the
			// heating temperature and the room temperature divided by the
			// heat transfer constant taking into account the size of the
			// room
			if (this.currentHeatingPower.getValue() >
												POWER_HEAT_TRANSFER_TOLERANCE) {
				currentTempDerivative =
						(STANDARD_HEATING_TEMP - current)/
											this.currentHeatTransfertConstant();
			}
		}

		// the cooling contribution: difference between the external temperature
		// and the temperature of the room divided by the insulation transfer
		// constant taking into account the surface of the walls.
		Time t = this.getCurrentStateTime();
		currentTempDerivative +=
				(this.externalTemperature.evaluateAt(t) - current)/
												INSULATION_TRANSFER_CONSTANT;
		return currentTempDerivative;
	}

	/**
	 * compute the current temperature given that a duration of {@code deltaT}
	 * has elapsed since the last update.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code deltaT >= 0.0}
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @param deltaT	the duration of the step since the last update.
	 * @return			the new temperature in celsius.
	 */
	protected double	computeNewTemperature(double deltaT)
	{
		Time t = this.currentTemperature.getTime();
		double oldTemp = this.currentTemperature.evaluateAt(t);
		double newTemp;

		if (deltaT > TEMPERATURE_UPDATE_TOLERANCE) {
			// update the room temperature using the Euler integration of the
			// differential equation
			double derivative = this.currentTemperature.getFirstDerivative();
			newTemp = oldTemp + derivative*deltaT;
		} else {
			newTemp = oldTemp;
		}

		// accumulate the temperature*time to compute the mean temperature
		this.temperatureAcc += ((oldTemp + newTemp)/2.0) * deltaT;
		return newTemp;
	}

	// -------------------------------------------------------------------------
	// Methods
	// -------------------------------------------------------------------------

	/**
	 * @see fr.sorbonne_u.components.hem2024e3.equipments.heater.mil.HeaterOperationI#setState(fr.sorbonne_u.components.hem2024e3.equipments.heater.mil.HeaterStateModel.State)
	 */
	@Override
	public void			setState(State s)
	{
		if (s == State.OFF) {
			// from the temperature point of view State.OFF is assimilated to
			// State.ON <i>i.e.</i>, not heating.
			this.currentState = State.ON;
		} else {
			this.currentState = s;
		}

		assert	glassBoxInvariants(this) :
				new NeoSim4JavaException(
						"HeaterTemperatureModel.glassBoxInvariants(this)");
		assert	blackBoxInvariants(this) :
				new NeoSim4JavaException(
						"HeaterTemperatureModel.blackBoxInvariants(this)");
	}

	/**
	 * @see fr.sorbonne_u.components.hem2024e3.equipments.heater.mil.HeaterOperationI#getState()
	 */
	@Override
	public State		getState()
	{
		return this.currentState;
	}

	/**
	 * @see fr.sorbonne_u.components.hem2024e3.equipments.heater.mil.HeaterOperationI#setCurrentHeatingPower(double, fr.sorbonne_u.devs_simulation.models.time.Time)
	 */
	@Override
	public void			setCurrentHeatingPower(double newPower, Time t)
	{
		assert	newPower >= 0.0 &&
				newPower <= HeaterElectricityModel.MAX_HEATING_POWER :
			new NeoSim4JavaException(
					"Precondition violation: newPower >= 0.0 && "
					+ "newPower <= HeaterElectricityModel.MAX_HEATING_POWER,"
					+ " but newPower = " + newPower);

		this.currentHeatingPower.setNewValue(newPower, t);


		assert	glassBoxInvariants(this) :
				new NeoSim4JavaException(
						"HeaterTemperatureModel.glassBoxInvariants(this)");
		assert	blackBoxInvariants(this) :
				new NeoSim4JavaException(
						"HeaterTemperatureModel.blackBoxInvariants(this)");
	}

	/**
	 * For SIL simulations, return the current value of the
	 * {@code currentTemperature}.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code true}	// no precondition.
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @return	the current value of the {@code currentTemperature}.
	 */
	public double		getCurrentTemperature()
	{
		return this.currentTemperature.getValue();
	}

	// -------------------------------------------------------------------------
	// DEVS simulation protocol
	// -------------------------------------------------------------------------

	/**
	 * 
	 * @see fr.sorbonne_u.devs_simulation.hioa.models.AtomicHIOA#initialiseState(fr.sorbonne_u.devs_simulation.models.time.Time)
	 */
	@Override
	public void			initialiseState(Time initialTime)
	{
		this.temperatureAcc = 0.0;
		this.start = initialTime;

		// from the temperature point of view State.OFF is assimilated to
		// State.ON <i>i.e.</i>, not heating.
		this.currentState = State.ON;

		this.getSimulationEngine().toggleDebugMode();
		this.logMessage("simulation begins.");

		super.initialiseState(initialTime);

		assert	glassBoxInvariants(this) :
				new NeoSim4JavaException(
						"HeaterTemperatureModel.glassBoxInvariants(this)");
		assert	blackBoxInvariants(this) :
				new NeoSim4JavaException(
						"HeaterTemperatureModel.blackBoxInvariants(this)");
	}

	/**
	 * @see fr.sorbonne_u.devs_simulation.hioa.models.interfaces.VariableInitialisationI#useFixpointInitialiseVariables()
	 */
	@Override
	public boolean		useFixpointInitialiseVariables()
	{
		return true;
	}

	/**
	 * @see fr.sorbonne_u.devs_simulation.hioa.models.interfaces.VariableInitialisationI#fixpointInitialiseVariables()
	 */
	@Override
	public Pair<Integer, Integer>	fixpointInitialiseVariables()
	{
		int justInitialised = 0;
		int notInitialisedYet = 0;

		// Only one variable must be initialised, the current temperature, and
		// it depends upon only one variable, the external temperature.
		if (!this.currentTemperature.isInitialised() &&
									this.externalTemperature.isInitialised()) {
			this.currentHeatingPower.initialise(
								HeaterElectricityModel.INITIAL_HEATING_POWER);
			// If the current temperature is not initialised yet but the
			// external temperature is, then initialise the current temperature
			// and say one more variable is initialised at this execution.
			double derivative = this.computeDerivatives(INITIAL_TEMPERATURE);
			this.currentTemperature.initialise(INITIAL_TEMPERATURE, derivative);
			justInitialised += 2;
		} else if (!this.currentTemperature.isInitialised()) {
			// If the external temperature is not initialised and the current
			// temperature either, then say one more variable has not been
			// initialised yet at this execution, forcing another execution
			// to reach the fix point.
			notInitialisedYet += 2;
		}

		assert	glassBoxInvariants(this) :
				new NeoSim4JavaException(
						"HeaterTemperatureModel.glassBoxInvariants(this)");
		assert	blackBoxInvariants(this) :
				new NeoSim4JavaException(
						"HeaterTemperatureModel.blackBoxInvariants(this)");

		return new Pair<>(justInitialised, notInitialisedYet);
	}

	/**
	 * @see fr.sorbonne_u.devs_simulation.models.interfaces.AtomicModelI#output()
	 */
	@Override
	public ArrayList<EventI>	output()
	{
		return null;
	}

	/**
	 * @see fr.sorbonne_u.devs_simulation.models.interfaces.ModelI#timeAdvance()
	 */
	@Override
	public Duration		timeAdvance()
	{
		return this.integrationStep;
	}

	/**
	 * @see fr.sorbonne_u.devs_simulation.models.AtomicModel#userDefinedInternalTransition(fr.sorbonne_u.devs_simulation.models.time.Duration)
	 */
	@Override
	public void			userDefinedInternalTransition(Duration elapsedTime)
	{
		// First, update the temperature (i.e., the value of the continuous
		// variable) until the current time i.e., perform an integration step
		double newTemp =
				this.computeNewTemperature(elapsedTime.getSimulatedDuration());
		// Next, compute the new derivative
		double newDerivative = this.computeDerivatives(newTemp);
		// Finally, set the new temperature value and derivative
		this.currentTemperature.setNewValue(
						newTemp,
						newDerivative,
						new Time(this.getCurrentStateTime().getSimulatedTime(),
								 this.getSimulatedTimeUnit()));

		// Tracing
		String mark = this.currentState == State.HEATING ? " (h)" : " (-)";
		StringBuffer message = new StringBuffer();
		message.append(this.currentTemperature.getTime().getSimulatedTime());
		message.append(mark);
		message.append(" : ");
		message.append(this.currentTemperature.getValue());
		this.logMessage(message.toString());

		super.userDefinedInternalTransition(elapsedTime);

		assert	glassBoxInvariants(this) :
				new NeoSim4JavaException(
						"HeaterTemperatureModel.glassBoxInvariants(this)");
		assert	blackBoxInvariants(this) :
				new NeoSim4JavaException(
						"HeaterTemperatureModel.blackBoxInvariants(this)");
	}

	/**
	 * @see fr.sorbonne_u.devs_simulation.models.AtomicModel#userDefinedExternalTransition(fr.sorbonne_u.devs_simulation.models.time.Duration)
	 */
	@Override
	public void			userDefinedExternalTransition(Duration elapsedTime)
	{
		// get the vector of current external events
		ArrayList<EventI> currentEvents = this.getStoredEventAndReset();
		// when this method is called, there is at least one external event,
		// and for the heater model, there will be exactly one by
		// construction.
		assert	currentEvents != null && currentEvents.size() == 1;

		Event ce = (Event) currentEvents.get(0);
		assert	ce instanceof HeaterEventI;

		StringBuffer sb = new StringBuffer("executing the external event: ");
		sb.append(ce.eventAsString());
		sb.append(".");
		this.logMessage(sb.toString());

		// First, update the temperature (i.e., the value of the continuous
		// variable) until the current time.
		double newTemp =
				this.computeNewTemperature(elapsedTime.getSimulatedDuration());
		// Then, update the current state of the heater.
		ce.executeOn(this);
		// Next, compute the new derivative
		double newDerivative = this.computeDerivatives(newTemp);
		// Finally, set the new temperature value and derivative
		this.currentTemperature.setNewValue(
					newTemp,
					newDerivative,
					new Time(this.getCurrentStateTime().getSimulatedTime()
										+ elapsedTime.getSimulatedDuration(),
							 this.getSimulatedTimeUnit()));

		super.userDefinedExternalTransition(elapsedTime);

		assert	glassBoxInvariants(this) :
				new NeoSim4JavaException(
						"HeaterTemperatureModel.glassBoxInvariants(this)");
		assert	blackBoxInvariants(this) :
				new NeoSim4JavaException(
						"HeaterTemperatureModel.blackBoxInvariants(this)");
	}

	/**
	 * @see fr.sorbonne_u.devs_simulation.hioa.models.AtomicHIOA#endSimulation(fr.sorbonne_u.devs_simulation.models.time.Time)
	 */
	@Override
	public void			endSimulation(Time endTime)
	{
		this.meanTemperature =
				this.temperatureAcc/
						endTime.subtract(this.start).getSimulatedDuration();

		this.logMessage("simulation ends.");
		super.endSimulation(endTime);
	}

	// -------------------------------------------------------------------------
	// Optional DEVS simulation protocol: simulation report
	// -------------------------------------------------------------------------

	/**
	 * The class <code>HeaterTemperatureReport</code> implements the
	 * simulation report for the <code>HeaterTemperatureModel</code>.
	 *
	 * <p><strong>Description</strong></p>
	 * 
	 * <p><strong>White-box Invariant</strong></p>
	 * 
	 * <pre>
	 * invariant	{@code true}	// no more invariant
	 * </pre>
	 * 
	 * <p><strong>Black-box Invariant</strong></p>
	 * 
	 * <pre>
	 * invariant	{@code true}	// no more invariant
	 * </pre>
	 * 
	 * <p>Created on : 2023-09-29</p>
	 * 
	 * @author	<a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
	 */
	public static class		HeaterTemperatureReport
	implements	SimulationReportI, HEM_ReportI
	{
		private static final long serialVersionUID = 1L;
		protected String	modelURI;
		protected double	meanTemperature;

		public			HeaterTemperatureReport(
			String modelURI,
			double meanTemperature
			)
		{
			super();
			this.modelURI = modelURI;
			this.meanTemperature = meanTemperature;
		}

		@Override
		public String	getModelURI()
		{
			return this.modelURI;
		}

		@Override
		public String	printout(String indent)
		{
			StringBuffer ret = new StringBuffer(indent);
			ret.append("---\n");
			ret.append(indent);
			ret.append('|');
			ret.append(this.modelURI);
			ret.append(" report\n");
			ret.append(indent);
			ret.append('|');
			ret.append("mean temperature = ");
			ret.append(this.meanTemperature);
			ret.append(".\n");
			ret.append(indent);
			ret.append("---\n");
			return ret.toString();
		}
	}

	// -------------------------------------------------------------------------
	// Optional DEVS simulation protocol: simulation run parameters
	// -------------------------------------------------------------------------

	/**
	 * @see fr.sorbonne_u.devs_simulation.models.interfaces.ModelI#setSimulationRunParameters(java.util.Map)
	 */
	@Override
	public void			setSimulationRunParameters(
		Map<String, Object> simParams
		) throws MissingRunParameterException
	{
		// this gets the reference on the owner component which is required
		// to have simulation models able to make the component perform some
		// operations or tasks or to get the value of variables held by the
		// component when necessary.
		if (simParams.containsKey(
						AtomicSimulatorPlugin.OWNER_RUNTIME_PARAMETER_NAME)) {
			// by the following, all of the logging will appear in the owner
			// component logger
			this.getSimulationEngine().setLogger(
						AtomicSimulatorPlugin.createComponentLogger(simParams));
		}
	}

	// -------------------------------------------------------------------------
	// Optional DEVS simulation protocol: simulation report
	// -------------------------------------------------------------------------

	/**
	 * @see fr.sorbonne_u.devs_simulation.models.interfaces.ModelI#getFinalReport()
	 */
	@Override
	public SimulationReportI	getFinalReport()
	{
		return new HeaterTemperatureReport(this.getURI(), this.meanTemperature);
	}
}
// -----------------------------------------------------------------------------
