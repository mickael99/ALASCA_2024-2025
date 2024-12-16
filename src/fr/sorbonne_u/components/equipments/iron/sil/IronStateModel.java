package fr.sorbonne_u.components.equipments.iron.sil;

import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import fr.sorbonne_u.components.cyphy.plugins.devs.AtomicSimulatorPlugin;
import fr.sorbonne_u.components.equipments.iron.IronImplementationI.IronTemperature;
import fr.sorbonne_u.components.equipments.iron.mil.IronElectricityModel;
import fr.sorbonne_u.components.equipments.iron.mil.IronElectricityModel.IronState;
import fr.sorbonne_u.components.equipments.iron.mil.events.AbstractIronEvent;
import fr.sorbonne_u.components.equipments.iron.mil.events.DisableEnergySavingModeIron;
import fr.sorbonne_u.components.equipments.iron.mil.events.DisableSteamModeIron;
import fr.sorbonne_u.components.equipments.iron.mil.events.EnableCottonModeIron;
import fr.sorbonne_u.components.equipments.iron.mil.events.EnableDelicateModeIron;
import fr.sorbonne_u.components.equipments.iron.mil.events.EnableEnergySavingModeIron;
import fr.sorbonne_u.components.equipments.iron.mil.events.EnableLinenModeIron;
import fr.sorbonne_u.components.equipments.iron.mil.events.EnableSteamModeIron;
import fr.sorbonne_u.components.equipments.iron.mil.events.TurnOffIron;
import fr.sorbonne_u.components.equipments.iron.mil.events.TurnOnIron;
import fr.sorbonne_u.devs_simulation.exceptions.MissingRunParameterException;
import fr.sorbonne_u.devs_simulation.exceptions.NeoSim4JavaException;
import fr.sorbonne_u.devs_simulation.models.AtomicModel;
import fr.sorbonne_u.devs_simulation.models.annotations.ModelExternalEvents;
import fr.sorbonne_u.devs_simulation.models.events.EventI;
import fr.sorbonne_u.devs_simulation.models.time.Duration;
import fr.sorbonne_u.devs_simulation.models.time.Time;
import fr.sorbonne_u.devs_simulation.simulators.interfaces.AtomicSimulatorI;
import fr.sorbonne_u.devs_simulation.utils.InvariantChecking;
import fr.sorbonne_u.devs_simulation.utils.StandardLogger;

@ModelExternalEvents(
	imported = 	{DisableEnergySavingModeIron.class,
				 EnableEnergySavingModeIron.class,
				 DisableSteamModeIron.class,
				 EnableSteamModeIron.class,
				 EnableDelicateModeIron.class,
				 EnableCottonModeIron.class,
				 EnableLinenModeIron.class,
				 TurnOnIron.class,
				 TurnOffIron.class},
	exported = 	{DisableEnergySavingModeIron.class,
				 EnableEnergySavingModeIron.class,
				 DisableSteamModeIron.class,
				 EnableSteamModeIron.class,
				 EnableDelicateModeIron.class,
				 EnableCottonModeIron.class,
				 EnableLinenModeIron.class,
				 TurnOnIron.class,
				 TurnOffIron.class}
	)
public class IronStateModel extends AtomicModel implements IronOperationI {

	// -------------------------------------------------------------------------
	// Constants and variables
	// -------------------------------------------------------------------------
	
	private static final long serialVersionUID = 1L;
	
	public static final String MIL_URI = IronStateModel.class.getSimpleName() + "-MIL";
	public static final String MIL_RT_URI = IronStateModel.class.getSimpleName() + "-MIL_RT";
	public static final String SIL_URI = IronStateModel.class. getSimpleName() + "-SIL";

	protected IronState currentState;
	protected boolean isSteamMode;
	protected boolean isEnergySavingMode;
	protected AbstractIronEvent lastReceived;
	
	
	// -------------------------------------------------------------------------
	// Invariants
	// -------------------------------------------------------------------------

	protected static boolean glassBoxInvariants(IronStateModel instance) {
		assert	instance != null :
				new NeoSim4JavaException("Precondition violation: "
						+ "instance != null");

		boolean ret = true;
		return ret;
	}
		
	protected static boolean blackBoxInvariants(IronStateModel instance) {
		assert	instance != null :
				new NeoSim4JavaException("Precondition violation: "
						+ "instance != null");

		boolean ret = true;
		ret &= InvariantChecking.checkBlackBoxInvariant(
				MIL_URI != null && !MIL_URI.isEmpty(),
				IronStateModel.class,
				instance,
				"MIL_URI != null && !MIL_URI.isEmpty()");
		ret &= InvariantChecking.checkBlackBoxInvariant(
				MIL_RT_URI != null && !MIL_RT_URI.isEmpty(),
				IronStateModel.class,
				instance,
				"MIL_RT_URI != null && !MIL_RT_URI.isEmpty()");
		ret &= InvariantChecking.checkBlackBoxInvariant(
				SIL_URI != null && !SIL_URI.isEmpty(),
				IronStateModel.class,
				instance,
				"SIL_URI != null && !SIL_URI.isEmpty()");
		return ret;
	}
	
	
	// -------------------------------------------------------------------------
	// Constructors
	// -------------------------------------------------------------------------
	
	public IronStateModel(String uri, TimeUnit simulatedTimeUnit, AtomicSimulatorI simulationEngine) {
		super(uri, simulatedTimeUnit, simulationEngine);
		
		this.getSimulationEngine().setLogger(new StandardLogger());

		assert	glassBoxInvariants(this) :
				new NeoSim4JavaException(
						"IronStateModel.glassBoxInvariants(this)");
		assert	blackBoxInvariants(this) :
				new NeoSim4JavaException(
						"IronStateModel.blackBoxInvariants(this)");
	}
	
	@Override
	public void turnOn() throws Exception {
		if(this.currentState == IronElectricityModel.IronState.OFF)
			this.currentState = IronElectricityModel.IronState.DELICATE;
	}

	@Override
	public void turnOff() throws Exception {
		if(this.currentState != IronElectricityModel.IronState.OFF)
			this.currentState = IronElectricityModel.IronState.OFF;
	}
	
	@Override
	public void setTemperature(IronTemperature t) throws Exception {
		IronState s = null;
		
		switch(t) {
			case DELICATE: s = IronState.DELICATE; break;
			case LINEN: s = IronState.LINEN; break;
			case COTTON: s = IronState.COTTON; break;
			default: break;
		}
		
		if(this.currentState != s)
			this.currentState = s;
	}

	@Override
	public void enableSteamMode() {
		this.isSteamMode = true;
	}

	@Override
	public void disableSteamMode() {
		this.isSteamMode = false;
	}

	@Override
	public void enableEnergySavingMode() {
		this.isEnergySavingMode = true;
	}

	@Override
	public void disableEnergySavingMode() {
		this.isEnergySavingMode = false;
	}
	
	
	// -------------------------------------------------------------------------
	// DEVS simulation protocol
	// -------------------------------------------------------------------------
	
	@Override
	public void initialiseState(Time initialTime) {
		super.initialiseState(initialTime);

		this.lastReceived = null;
		this.currentState = IronState.OFF;
		this.isSteamMode = false;
		this.isEnergySavingMode = false;

		this.getSimulationEngine() .toggleDebugMode();
		this.logMessage("simulation begins.");
	}
	
	@Override
	public ArrayList<EventI> output() {
		assert	this.lastReceived != null;

		ArrayList<EventI> ret = new ArrayList<EventI>();
		ret.add(this.lastReceived);
		this.lastReceived = null;
		
		return ret;
	}

	@Override
	public Duration timeAdvance() {
		if (this.lastReceived != null) 
			return Duration.zero(this.getSimulatedTimeUnit());
		else 
			return Duration.INFINITY;
	}
	
	@Override
	public void	userDefinedExternalTransition(Duration elapsedTime) {
		super.userDefinedExternalTransition(elapsedTime);

		ArrayList<EventI> currentEvents = this.getStoredEventAndReset();
		assert	currentEvents != null && currentEvents.size() == 1;

		this.lastReceived = (AbstractIronEvent) currentEvents.get(0);

		StringBuffer message = new StringBuffer(this.uri);
		message.append(" executes the external event ");
		message.append(" - Received event: ").append(this.lastReceived);
	    message.append(", Current state: ").append(this.currentState);
	    message.append(", Steam Mode: ").append(this.isSteamMode);
	    message.append(", Energy Saving Mode: ").append(this.isEnergySavingMode);
		this.logMessage(message.toString());
	}
	
	@Override
	public void endSimulation(Time endTime) {
		this.logMessage("simulation ends.");
		super.endSimulation(endTime);
	}
	
	// -------------------------------------------------------------------------
	// Optional DEVS simulation protocol: simulation run parameters
	// -------------------------------------------------------------------------
	
	@Override
	public void setSimulationRunParameters(Map<String, Object> simParams) throws MissingRunParameterException {
		super.setSimulationRunParameters(simParams);

		if (simParams.containsKey(
						AtomicSimulatorPlugin.OWNER_RUNTIME_PARAMETER_NAME)) 
		{
			this.getSimulationEngine().setLogger(
						AtomicSimulatorPlugin.createComponentLogger(simParams));
		}
	}
}
