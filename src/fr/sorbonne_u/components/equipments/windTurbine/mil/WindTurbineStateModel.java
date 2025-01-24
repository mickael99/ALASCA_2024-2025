package fr.sorbonne_u.components.equipments.windTurbine.mil;

import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import fr.sorbonne_u.components.cyphy.plugins.devs.AtomicSimulatorPlugin;
import fr.sorbonne_u.components.equipments.windTurbine.WindTurbineI.WindTurbineState;
import fr.sorbonne_u.components.equipments.windTurbine.mil.events.AbstractWindTurbineEvent;
import fr.sorbonne_u.components.equipments.windTurbine.mil.events.StartWindTurbineEvent;
import fr.sorbonne_u.components.equipments.windTurbine.mil.events.StopWindTurbineEvent;
import fr.sorbonne_u.devs_simulation.exceptions.MissingRunParameterException;
import fr.sorbonne_u.devs_simulation.exceptions.NeoSim4JavaException;
import fr.sorbonne_u.devs_simulation.models.AtomicModel;
import fr.sorbonne_u.devs_simulation.models.annotations.ModelExternalEvents;
import fr.sorbonne_u.devs_simulation.models.events.Event;
import fr.sorbonne_u.devs_simulation.models.events.EventI;
import fr.sorbonne_u.devs_simulation.models.time.Duration;
import fr.sorbonne_u.devs_simulation.models.time.Time;
import fr.sorbonne_u.devs_simulation.simulators.interfaces.AtomicSimulatorI;
import fr.sorbonne_u.devs_simulation.utils.StandardLogger;
import fr.sorbonne_u.exceptions.InvariantChecking;

@ModelExternalEvents(exported = {
				        StartWindTurbineEvent.class,
				        StopWindTurbineEvent.class
					 },
					 imported = {
						StartWindTurbineEvent.class,
					    StopWindTurbineEvent.class
					 })
public class WindTurbineStateModel extends AtomicModel implements WindTurbineOperationI {

	// -------------------------------------------------------------------------
	// Constants and variables
	// -------------------------------------------------------------------------
	
	private static final long serialVersionUID = 1L;
	
	public static final String	MIL_URI = WindTurbineStateModel.class.getSimpleName() + "-MIL";
	public static final String	MIL_RT_URI = WindTurbineStateModel.class.getSimpleName() + "-MIL-RT";
	public static final String	SIL_URI = WindTurbineStateModel.class.getSimpleName() + "-SIL";
	
	protected WindTurbineState currentState;
	public static final WindTurbineState INITIAL_CURRENT_STATE = WindTurbineState.STANDBY;
	protected EventI toBeReemitted;

	
	// -------------------------------------------------------------------------
	// Invariants
	// -------------------------------------------------------------------------

	protected static boolean glassBoxInvariants(WindTurbineStateModel instance) {
		assert	instance != null :
				new NeoSim4JavaException("Precondition violation: "
						+ "instance != null");

		boolean ret = true;
		ret &= InvariantChecking.checkGlassBoxInvariant(
					instance.currentState != null,
					WindTurbineStateModel.class,
					instance,
					"currentState != null");
		return ret;
	}

	protected static boolean blackBoxInvariants(WindTurbineStateModel instance) {
		assert	instance != null :
				new NeoSim4JavaException("Precondition violation: "
						+ "instance != null");

		boolean ret = true;
		ret &= InvariantChecking.checkBlackBoxInvariant(
					MIL_URI != null && !MIL_URI.isEmpty(),
					WindTurbineStateModel.class,
					instance,
					"MIL_URI != null && !MIL_URI.isEmpty()");
		ret &= InvariantChecking.checkBlackBoxInvariant(
					MIL_RT_URI != null && !MIL_RT_URI.isEmpty(),
					WindTurbineStateModel.class,
					instance,
					"MIL_RT_URI != null && !MIL_RT_URI.isEmpty()");
		ret &= InvariantChecking.checkBlackBoxInvariant(
					SIL_URI != null && !SIL_URI.isEmpty(),
					WindTurbineStateModel.class,
					instance,
					"SIL_URI != null && !SIL_URI.isEmpty()");
		return ret;
	}
	
	
	// -------------------------------------------------------------------------
	// Constructors
	// -------------------------------------------------------------------------

	public WindTurbineStateModel(String uri, TimeUnit simulatedTimeUnit, AtomicSimulatorI simulationEngine) {
		super(uri, simulatedTimeUnit, simulationEngine);
		this.getSimulationEngine().setLogger(new StandardLogger());
		
		this.currentState = INITIAL_CURRENT_STATE;
		
		assert	glassBoxInvariants(this) :
				new NeoSim4JavaException(
						"WindTurbineStateModel.glassBoxInvariants(this)");
		assert	blackBoxInvariants(this) :
				new NeoSim4JavaException(
						"WindTurbineStateModel.blackBoxInvariants(this)");
	}
	
	@Override
	public void initialiseState(Time initialTime) {
		super.initialiseState(initialTime);

		this.getSimulationEngine().toggleDebugMode();
		this.logMessage("simulation begins.");

		assert	glassBoxInvariants(this) :
				new NeoSim4JavaException(
						"WindTurbineStateModel.glassBoxInvariants(this)");
		assert	blackBoxInvariants(this) :
				new NeoSim4JavaException(
						"WindTurbineStateModel.blackBoxInvariants(this)");
	}
	
	
	// -------------------------------------------------------------------------
	// Methods
	// -------------------------------------------------------------------------
	
	@Override
	public Duration timeAdvance() {
		if(this.toBeReemitted == null) 
			return Duration.INFINITY;
		else 
			return Duration.zero(getSimulatedTimeUnit());
	}
	
	@Override
	public ArrayList<EventI> output() {
		if (this.toBeReemitted != null) {
			ArrayList<EventI> ret = new ArrayList<EventI>();
			ret.add(this.toBeReemitted);
			this.toBeReemitted = null;
			
			return ret;
		} 
		else 
			return null;	
	}
	
	@Override
	public void userDefinedExternalTransition(Duration elapsedTime) {
		super.userDefinedExternalTransition(elapsedTime);

		ArrayList<EventI> currentEvents = this.getStoredEventAndReset();
		assert	currentEvents != null && currentEvents.size() == 1;

		this.toBeReemitted = (Event)currentEvents.get(0);
		assert this.toBeReemitted instanceof AbstractWindTurbineEvent;
		this.toBeReemitted.executeOn(this);

		assert	glassBoxInvariants(this) :
				new NeoSim4JavaException(
						"WindTurbineStateModel.glassBoxInvariants(this)");
		assert	blackBoxInvariants(this) :
				new NeoSim4JavaException(
						"WindTurbineStateModel.blackBoxInvariants(this)");
	}

	@Override
	public void activate() {
		this.currentState = WindTurbineState.ACTIVE;
	}

	@Override
	public void stop() {
		this.currentState = WindTurbineState.STANDBY;
	}

	@Override
	public WindTurbineState getState() {
		return this.currentState;
	}
	
	// -------------------------------------------------------------------------
	// Optional DEVS simulation protocol: simulation run parameters
	// -------------------------------------------------------------------------

	@Override
	public void setSimulationRunParameters(Map<String, Object> simParams) throws MissingRunParameterException {
		if (simParams.containsKey(
						AtomicSimulatorPlugin.OWNER_RUNTIME_PARAMETER_NAME)) 
		{
			this.getSimulationEngine().setLogger(
						AtomicSimulatorPlugin.createComponentLogger(simParams));
		}
	}
}
