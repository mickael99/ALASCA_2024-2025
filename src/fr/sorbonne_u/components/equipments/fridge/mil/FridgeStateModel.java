package fr.sorbonne_u.components.equipments.fridge.mil;

import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import fr.sorbonne_u.components.cyphy.plugins.devs.AtomicSimulatorPlugin;
import fr.sorbonne_u.components.equipments.fridge.mil.FridgeElectricityModel.FridgeState;
import fr.sorbonne_u.components.equipments.fridge.mil.events.*;
import fr.sorbonne_u.devs_simulation.exceptions.MissingRunParameterException;
import fr.sorbonne_u.devs_simulation.exceptions.NeoSim4JavaException;
import fr.sorbonne_u.devs_simulation.models.AtomicModel;
import fr.sorbonne_u.devs_simulation.models.annotations.ModelExternalEvents;
import fr.sorbonne_u.devs_simulation.models.events.Event;
import fr.sorbonne_u.devs_simulation.models.events.EventI;
import fr.sorbonne_u.devs_simulation.models.time.Duration;
import fr.sorbonne_u.devs_simulation.models.time.Time;
import fr.sorbonne_u.devs_simulation.simulators.interfaces.AtomicSimulatorI;
import fr.sorbonne_u.devs_simulation.utils.InvariantChecking;
import fr.sorbonne_u.devs_simulation.utils.StandardLogger;

@ModelExternalEvents(imported = {SwitchOnFridge.class,
								 SwitchOffFridge.class,
								 SetPowerFridge.class,
								 CoolFridge.class,
								 DoNotCoolFridge.class,
								 OpenDoorFridge.class,
								 CloseDoorFridge.class},
					exported = { SwitchOnFridge.class,
								 SwitchOffFridge.class,
								 SetPowerFridge.class,
								 CoolFridge.class,
								 DoNotCoolFridge.class,
								 OpenDoorFridge.class,
								 CloseDoorFridge.class})
public class FridgeStateModel extends AtomicModel implements FridgeOperationI {

	// -------------------------------------------------------------------------
	// Constants and variables
	// -------------------------------------------------------------------------

	private static final long serialVersionUID = 1L;
	
	public static final String	MIL_URI = FridgeStateModel.class.getSimpleName() + "-MIL";
	public static final String	MIL_RT_URI = FridgeStateModel.class.getSimpleName() + "-MIL-RT";
	public static final String	SIL_URI = FridgeStateModel.class.getSimpleName() + "-SIL";

	protected FridgeState currentState = FridgeState.OFF;
	protected EventI toBeReemitted;
	
	
	// -------------------------------------------------------------------------
	// Constructors
	// -------------------------------------------------------------------------

	public FridgeStateModel(String uri, TimeUnit simulatedTimeUnit, AtomicSimulatorI simulationEngine) {
		super(uri, simulatedTimeUnit, simulationEngine);
		this.getSimulationEngine().setLogger(new StandardLogger());

		assert glassBoxInvariants(this) :
				new NeoSim4JavaException("FridgeStateModel.glassBoxInvariants(this)");
		assert blackBoxInvariants(this) :
				new NeoSim4JavaException("FridgeStateModel.blackBoxInvariants(this)");
	}
		
	
	// -------------------------------------------------------------------------
	// Invariants
	// -------------------------------------------------------------------------

	protected static boolean glassBoxInvariants(FridgeStateModel instance) {
		assert instance != null :
				new NeoSim4JavaException("Precondition violation: " + "instance != null");

		boolean ret = true;
		ret &= InvariantChecking.checkGlassBoxInvariant(
					instance.currentState != null,
					FridgeStateModel.class,
					instance,
					"currentState != null");
		return ret;
	}

	protected static boolean blackBoxInvariants(FridgeStateModel instance) {
		assert	instance != null :
				new NeoSim4JavaException("Precondition violation: " + "instance != null");

		boolean ret = true;
		ret &= InvariantChecking.checkBlackBoxInvariant(
					MIL_URI != null && !MIL_URI.isEmpty(),
					FridgeStateModel.class,
					instance,
					"MIL_URI != null && !MIL_URI.isEmpty()");
		ret &= InvariantChecking.checkBlackBoxInvariant(
					MIL_RT_URI != null && !MIL_RT_URI.isEmpty(),
					FridgeStateModel.class,
					instance,
					"MIL_RT_URI != null && !MIL_RT_URI.isEmpty()");
		ret &= InvariantChecking.checkBlackBoxInvariant(
					SIL_URI != null && !SIL_URI.isEmpty(),
					FridgeStateModel.class,
					instance,
					"SIL_URI != null && !SIL_URI.isEmpty()");
		return ret;
	}

		
	@Override
	public Duration timeAdvance() {
		if (this.toBeReemitted == null) 
			return Duration.INFINITY;
		
		return Duration.zero(getSimulatedTimeUnit());
	}

	@Override
	public void setCurrentCoolingPower(double newPower, Time t) {
		
	}

	@Override
	public void setState(FridgeState s) {
		if(this.currentState == FridgeState.DOOR_OPEN && s == FridgeState.COOLING)  {
			this.logMessage("Trying to enabled cool mode but the fridge is open.");
			return;
		}
		
		this.currentState = s;
	}

	@Override
	public FridgeState getState() {
		return this.currentState;
	}
	
	@Override
	public void initialiseState(Time initialTime) {
		super.initialiseState(initialTime);

		this.getSimulationEngine().toggleDebugMode();
		this.logMessage("simulation begins.");

		assert	glassBoxInvariants(this) :
				new NeoSim4JavaException("FridgeStateModel.glassBoxInvariants(this)");
		assert	blackBoxInvariants(this) :
				new NeoSim4JavaException("FridgeStateModel.blackBoxInvariants(this)");
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

		this.toBeReemitted = (Event) currentEvents.get(0);
		assert	this.toBeReemitted instanceof FridgeEventI;
		this.toBeReemitted.executeOn(this);

		assert	glassBoxInvariants(this) :
				new NeoSim4JavaException("FridgeStateModel.glassBoxInvariants(this)");
		assert	blackBoxInvariants(this) :
				new NeoSim4JavaException("FridgeStateModel.blackBoxInvariants(this)");
	}
	
	@Override
	public void	endSimulation(Time endTime) {
		this.logMessage("simulation ends.");
	}

	// -------------------------------------------------------------------------
	// Optional DEVS simulation protocol: simulation run parameters
	// -------------------------------------------------------------------------

	@Override
	public void setSimulationRunParameters(Map<String, Object> simParams) throws MissingRunParameterException
	{
		if (simParams.containsKey(AtomicSimulatorPlugin.OWNER_RUNTIME_PARAMETER_NAME)) 
		{
			this.getSimulationEngine().setLogger(
						AtomicSimulatorPlugin.createComponentLogger(simParams));
		}
	}
}
