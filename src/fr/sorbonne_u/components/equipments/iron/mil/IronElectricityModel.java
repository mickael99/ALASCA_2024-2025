package fr.sorbonne_u.components.equipments.iron.mil;

import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import fr.sorbonne_u.components.cyphy.plugins.devs.AtomicSimulatorPlugin;
import fr.sorbonne_u.components.equipments.hem.mil.HEM_ReportI;
import fr.sorbonne_u.components.equipments.iron.mil.events.AbstractIronEvent;
import fr.sorbonne_u.components.utils.Electricity;
import fr.sorbonne_u.devs_simulation.exceptions.MissingRunParameterException;
import fr.sorbonne_u.devs_simulation.exceptions.NeoSim4JavaException;
import fr.sorbonne_u.devs_simulation.hioa.annotations.ExportedVariable;
import fr.sorbonne_u.devs_simulation.hioa.annotations.ModelExportedVariable;
import fr.sorbonne_u.devs_simulation.hioa.models.AtomicHIOA;
import fr.sorbonne_u.devs_simulation.hioa.models.vars.Value;
import fr.sorbonne_u.devs_simulation.models.annotations.ModelExternalEvents;
import fr.sorbonne_u.devs_simulation.models.events.Event;
import fr.sorbonne_u.devs_simulation.models.events.EventI;
import fr.sorbonne_u.devs_simulation.models.interfaces.ModelI;
import fr.sorbonne_u.devs_simulation.models.time.Duration;
import fr.sorbonne_u.devs_simulation.models.time.Time;
import fr.sorbonne_u.devs_simulation.simulators.interfaces.AtomicSimulatorI;
import fr.sorbonne_u.devs_simulation.simulators.interfaces.SimulationReportI;
import fr.sorbonne_u.devs_simulation.utils.InvariantChecking;
import fr.sorbonne_u.devs_simulation.utils.StandardLogger;
import fr.sorbonne_u.components.equipments.iron.interfaces.IronImplementationI.IronState;
import fr.sorbonne_u.components.equipments.iron.mil.events.*;

@ModelExternalEvents(imported = {DisableEnergySavingModeIron.class,
								 EnableEnergySavingModeIron.class,
								 DisableSteamModeIron.class,
								 EnableSteamModeIron.class,
								 EnableDelicateModeIron.class,
								 EnableCottonModeIron.class,
								 EnableLinenModeIron.class,
								 TurnOnIron.class,
								 TurnOffIron.class})
@ModelExportedVariable(name = "currentIntensity", type = Double.class)
public class IronElectricityModel extends AtomicHIOA implements IronOperationI {

	// -------------------------------------------------------------------------
	// Attributes
	// -------------------------------------------------------------------------
	
	protected boolean isSteamMode = false;
	protected boolean isEnergySavingMode = false;
	
	// URI
	private static final long serialVersionUID = 1L;
	public static final String MIL_URI = IronElectricityModel.class.getSimpleName() + "-MIL";
	public static final String MIL_RT_URI = IronElectricityModel.class.getSimpleName() + "-MIL-RT";
	public static final String SIL_URI = IronElectricityModel.class.getSimpleName() + "-MIL-RT";
	
	// Modes energy consumption
	protected static double DELICATE_CONSUMPTION = 600.0;
	protected static double COTTON_CONSUMPTION = 800.0;
	protected static double LINEN_CONSUMPTION = 1000.0;
	protected static double ENERGY_SAVING_CONSUMPTION = 0.2; // 20% of consumption
	protected static double STEAM_CONSUMPTION = 100.0;
	
	protected static double TENSION = 230.0;
	
	// Consumption
	protected static final IronState INITIALISE_CURRENT_STATE = IronState.OFF;
	protected IronState currentState;
	protected boolean consumptionHasChanged = false;
	protected double totalConsumption = 0.0;
	
	@ExportedVariable(type = Double.class)
	protected final Value<Double> currentIntensity = new Value<Double>(this);
	
	// -------------------------------------------------------------------------
	// Constructor
	// -------------------------------------------------------------------------
	
	public IronElectricityModel(String uri, TimeUnit simulatedTimeUnit, AtomicSimulatorI simulationEngine) throws Exception {
		super(uri, simulatedTimeUnit, simulationEngine);
		
		this.getSimulationEngine().setLogger(new StandardLogger());
		
		assert	glassBoxInvariants(this) :
			new NeoSim4JavaException("Glass-box invariants violation!");
		assert	blackBoxInvariants(this) :
			new NeoSim4JavaException("Black-box invariant violation!");
	}
	
	
	// -------------------------------------------------------------------------
	// Invariants
	// -------------------------------------------------------------------------
	
	protected static boolean glassBoxInvariants(IronElectricityModel instance) {
		assert instance != null :
				new NeoSim4JavaException("Precondition violation: instance != null");

		boolean ret = true;
		ret &= InvariantChecking.checkGlassBoxInvariant(
				INITIALISE_CURRENT_STATE != null,
				IronElectricityModel.class,
				instance,
				"INITIALISE_CURRENT_STATE == null");
		ret &= InvariantChecking.checkGlassBoxInvariant(
				DELICATE_CONSUMPTION > 0.0,
				IronElectricityModel.class,
				instance,
				"DELICATE_CONSUMPTION > 0.0");
		ret &= InvariantChecking.checkGlassBoxInvariant(
				DELICATE_CONSUMPTION <= COTTON_CONSUMPTION,
				IronElectricityModel.class,
				instance,
				"DELICATE_CONSUMPTION <= COTTON_CONSUMPTION");
		ret &= InvariantChecking.checkGlassBoxInvariant(
				COTTON_CONSUMPTION <= LINEN_CONSUMPTION,
				IronElectricityModel.class,
				instance,
				"COTTON_CONSUMPTION <= LINEN_CONSUMPTION");
		ret &= InvariantChecking.checkGlassBoxInvariant(
				ENERGY_SAVING_CONSUMPTION >= 0.0 && ENERGY_SAVING_CONSUMPTION <= 1.0,
				IronElectricityModel.class,
				instance,
				"ENERGY_SAVING_CONSUMPTION >= 0.0");
		ret &= InvariantChecking.checkGlassBoxInvariant(
				STEAM_CONSUMPTION >= 0.0,
				IronElectricityModel.class,
				instance,
				"STEAM_CONSUMPTION >= 0.0");
		ret &= InvariantChecking.checkGlassBoxInvariant(
				TENSION > 0.0,
				IronElectricityModel.class,
				instance,
				"TENSION > 0.0");
		ret &= InvariantChecking.checkGlassBoxInvariant(
				instance.totalConsumption >= 0.0,
				IronElectricityModel.class,
				instance,
				"totalConsumption >= 0.0");
		ret &= InvariantChecking.checkGlassBoxInvariant(
				!instance.currentIntensity.isInitialised() ||
									instance.currentIntensity.getValue() >= 0.0,
				IronElectricityModel.class,
				instance,
				"!currentIntensity.isInitialised() || "
				+ "currentIntensity.getValue() >= 0.0");
		
		return ret;
	}
	
	protected static boolean blackBoxInvariants(IronElectricityModel instance) {
		assert instance != null :
				new NeoSim4JavaException("Precondition violation: instance != null");

		boolean ret = true;
		ret &= InvariantChecking.checkBlackBoxInvariant(
				MIL_URI != null && !MIL_URI.isEmpty(),
				IronElectricityModel.class,
				instance,
				"MIL_URI != null && !MIL_URI.isEmpty()");
		ret &= InvariantChecking.checkBlackBoxInvariant(
				SIL_URI != null && !SIL_URI.isEmpty(),
				IronElectricityModel.class,
				instance,
				"SIL_URI != null && !SIL_URI.isEmpty()");
		ret &= InvariantChecking.checkBlackBoxInvariant(
				DELICATE_CONSUMPTION_RPNAME != null &&
										!DELICATE_CONSUMPTION_RPNAME.isEmpty(),
				IronElectricityModel.class,
				instance,
				"DELICATE_CONSUMPTION_RPNAME != null && "
								+ "!DELICATE_CONSUMPTION_RPNAME.isEmpty()");
		ret &= InvariantChecking.checkBlackBoxInvariant(
				COTTON_CONSUMPTION_RPNAME != null &&
									!COTTON_CONSUMPTION_RPNAME.isEmpty(),
				IronElectricityModel.class,
				instance,
				"COTTON_CONSUMPTION_RPNAME != null && "
							+ "!COTTON_CONSUMPTION_RPNAME.isEmpty()");
		ret &= InvariantChecking.checkBlackBoxInvariant(
				LINEN_CONSUMPTION_RPNAME != null &&
									!LINEN_CONSUMPTION_RPNAME.isEmpty(),
				IronElectricityModel.class,
				instance,
				"LINEN_CONSUMPTION_RPNAME != null && "
							+ "!LINEN_CONSUMPTION_RPNAME.isEmpty()");
		ret &= InvariantChecking.checkBlackBoxInvariant(
				ENERGY_SAVING_CONSUMPTION_RPNAME != null &&
									!ENERGY_SAVING_CONSUMPTION_RPNAME.isEmpty(),
				IronElectricityModel.class,
				instance,
				"ENERGY_SAVING_CONSUMPTION_RPNAME != null && "
							+ "!ENERGY_SAVING_CONSUMPTION_RPNAME.isEmpty()");
		ret &= InvariantChecking.checkBlackBoxInvariant(
				STEAM_CONSUMPTION_RPNAME != null &&
									!STEAM_CONSUMPTION_RPNAME.isEmpty(),
				IronElectricityModel.class,
				instance,
				"STEAM_CONSUMPTION_RPNAME != null && "
							+ "!STEAM_CONSUMPTION_RPNAME.isEmpty()");
		ret &= InvariantChecking.checkBlackBoxInvariant(
				TENSION_RPNAME != null && !TENSION_RPNAME.isEmpty(),
				IronElectricityModel.class,
				instance,
				"TENSION_RPNAME != null && !TENSION_RPNAME.isEmpty()");
		return ret;
	}
	
	
	// -------------------------------------------------------------------------
	// Methods
	// -------------------------------------------------------------------------
	
	@Override
	public void turnOn()  {
		if(this.currentState == IronState.OFF) {
			this.currentState = IronState.DELICATE;
			this.toggleConsumptionHasChanged();
		}
	}


	@Override
	public void turnOff()  {
		if(this.currentState != IronState.OFF) {
			this.currentState = IronState.OFF;
			this.toggleConsumptionHasChanged();
		}
	}
	
	@Override
	public void setState(IronState s) {
		if(s == IronState.OFF)
			this.turnOff();
		else {
			if(s != this.currentState) {
				this.currentState = s;
				this.toggleConsumptionHasChanged();
			}
		}
	}
	
	@Override
	public void enableSteamMode() {
		if(!this.isSteamMode) {
			this.isSteamMode = true;
			this.toggleConsumptionHasChanged();
		}
	}
	
	@Override
	public void disableSteamMode() {
		if(this.isSteamMode) {
			this.isSteamMode = false;
			this.toggleConsumptionHasChanged();
		}
	}
	
	@Override
	public void enableEnergySavingMode() {
		if(!this.isEnergySavingMode) {
			this.isEnergySavingMode = true;
			this.toggleConsumptionHasChanged();
		}
	}
	
	@Override
	public void disableEnergySavingMode() {
		if(this.isEnergySavingMode) {
			this.isEnergySavingMode = false;
			this.toggleConsumptionHasChanged();
		}
	}
	
	public void toggleConsumptionHasChanged() {
		if(this.consumptionHasChanged) 
			this.consumptionHasChanged = false;
		else 
			this.consumptionHasChanged = true;
	}
	
	
	// -------------------------------------------------------------------------
	// DEVS simulation protocol
	// -------------------------------------------------------------------------
	
	@Override
	public void initialiseState(Time startTime) {
		super.initialiseState(startTime);
		
		this.currentState = INITIALISE_CURRENT_STATE;
		this.consumptionHasChanged = false;
		this.totalConsumption = 0.0;
		this.isEnergySavingMode = false;
		this.isSteamMode = false;
		
		this.getSimulationEngine().toggleDebugMode();
		this.logMessage("simulation begins.\n");

		assert	glassBoxInvariants(this) :
				new NeoSim4JavaException("Glass-box invariants violation!");
		assert	blackBoxInvariants(this) :
				new NeoSim4JavaException("Black-box invariant violation!");
	}
	
	@Override
	public void initialiseVariables() {
		super.initialiseVariables();

		this.currentIntensity.initialise(0.0);

		assert glassBoxInvariants(this) :
				new NeoSim4JavaException("Glass-box invariants violation!");
		assert blackBoxInvariants(this) :
				new NeoSim4JavaException("Black-box invariant violation!");
	}
	
	@Override
	public ArrayList<EventI> output() {
		return null;
	}
	
	
	@Override
	public Duration timeAdvance() {
		Duration ret = null;
		
		if(this.consumptionHasChanged) {
			this.toggleConsumptionHasChanged();
			ret = new Duration(0.0, this.getSimulatedTimeUnit());
		}
		else
			ret = Duration.INFINITY;
		
		assert	glassBoxInvariants(this) :
			new NeoSim4JavaException("Glass-box invariants violation!");
		assert	blackBoxInvariants(this) :
			new NeoSim4JavaException("Black-box invariant violation!");
	
		return ret;
	}
	
	@Override
	public void userDefinedInternalTransition(Duration elapsedTime) {
		super.userDefinedInternalTransition(elapsedTime);
		
		Time t = this.getCurrentStateTime();
		
		double intensity = 0.0;
		
		switch(this.currentState) {
			case OFF : intensity = 0.0; break;
			case DELICATE: intensity = DELICATE_CONSUMPTION; break;
			case COTTON: intensity = COTTON_CONSUMPTION; break;
			case LINEN: intensity = LINEN_CONSUMPTION; break;
		}
		
		if(this.currentState != IronState.OFF && this.isSteamMode)
			intensity += STEAM_CONSUMPTION;
		
		if(this.currentState != IronState.OFF && this.isEnergySavingMode)
			intensity *= (1 - ENERGY_SAVING_CONSUMPTION);
		
		this.currentIntensity.setNewValue(intensity / TENSION, t);
		
		// Tracing
		StringBuffer message =
				new StringBuffer("executes an internal transition ");
		message.append("with current consumption ");
		message.append(this.currentIntensity.getValue());
		message.append(" at ");
		message.append(this.currentIntensity.getTime());
		message.append(".\n");
		this.logMessage(message.toString());

		assert	glassBoxInvariants(this) :
				new NeoSim4JavaException("Glass-box invariants violation!");
		assert	blackBoxInvariants(this) :
				new NeoSim4JavaException("Black-box invariant violation!");
	}
	
	@Override
	public void userDefinedExternalTransition(Duration elapsedTime) {
		super.userDefinedExternalTransition(elapsedTime);

		ArrayList<EventI> currentEvents = this.getStoredEventAndReset();
		assert currentEvents != null && currentEvents.size() == 1;
		
		Event ce = (Event) currentEvents.get(0);
		this.totalConsumption +=
				Electricity.computeConsumption(
									elapsedTime,
									TENSION * this.currentIntensity.getValue());
		
		// Tracing
		StringBuffer message =
				new StringBuffer("executes an external transition ");
		message.append(ce.toString());
		message.append(")\n");
		this.logMessage(message.toString());

		assert ce instanceof AbstractIronEvent :
				new RuntimeException(
						ce + " is not an event that an IronElectricityModel"
						+ " can receive and process.");
		
		// Execute the event
		ce.executeOn(this);
		assert	glassBoxInvariants(this) :
				new NeoSim4JavaException("Glass-box invariants violation!");
		assert	blackBoxInvariants(this) :
				new NeoSim4JavaException("Black-box invariant violation!");
	}
	
	@Override
	public void	endSimulation(Time endTime) {
		Duration d = endTime.subtract(this.getCurrentStateTime());
		this.totalConsumption +=
				Electricity.computeConsumption(
									d,
									TENSION * this.currentIntensity.getValue());

		this.logMessage("simulation ends with total consumption -> " + this.totalConsumption + "\n");
		super.endSimulation(endTime);
	}
	
	
	// -------------------------------------------------------------------------
	// Optional DEVS simulation protocol: simulation run parameters
	// -------------------------------------------------------------------------
	
	public static final String DELICATE_CONSUMPTION_RPNAME =
												MIL_URI + ":DELICATE_CONSUMPTION";
	public static final String COTTON_CONSUMPTION_RPNAME =
												MIL_URI + ":COTTON_CONSUMPTION_CONSUMPTION";
	public static final String LINEN_CONSUMPTION_RPNAME =
												MIL_URI + ":LINEN_CONSUMPTION";
	public static final String ENERGY_SAVING_CONSUMPTION_RPNAME =
												MIL_URI + ":ENERGY_SAVING_CONSUMPTION";
	public static final String STEAM_CONSUMPTION_RPNAME =
												MIL_URI + ":STEAM_CONSUMPTION";
	public static final String TENSION_RPNAME = MIL_URI + ":TENSION";
	
	
	@Override
	public void setSimulationRunParameters(Map<String, Object> simParams) throws MissingRunParameterException {
		super.setSimulationRunParameters(simParams);

		if (simParams.containsKey(AtomicSimulatorPlugin.OWNER_RUNTIME_PARAMETER_NAME)) 
			this.getSimulationEngine().setLogger(AtomicSimulatorPlugin.createComponentLogger(simParams));

		
		String delicateName =
			ModelI.createRunParameterName(getURI(), DELICATE_CONSUMPTION_RPNAME);
		if (simParams.containsKey(delicateName)) 
			DELICATE_CONSUMPTION = (double)simParams.get(delicateName);
		
		String cottonName =
			ModelI.createRunParameterName(getURI(), COTTON_CONSUMPTION_RPNAME);
		if (simParams.containsKey(cottonName)) 
			COTTON_CONSUMPTION = (double)simParams.get(cottonName);
		
		String linenName =
				ModelI.createRunParameterName(getURI(), LINEN_CONSUMPTION_RPNAME);
		if (simParams.containsKey(linenName)) 
			LINEN_CONSUMPTION = (double)simParams.get(linenName);
			
		String savingModeName =
				ModelI.createRunParameterName(getURI(), ENERGY_SAVING_CONSUMPTION_RPNAME);
		if (simParams.containsKey(savingModeName)) 
			ENERGY_SAVING_CONSUMPTION = (double)simParams.get(savingModeName);
		
		String steamName =
				ModelI.createRunParameterName(getURI(), STEAM_CONSUMPTION_RPNAME);
		if (simParams.containsKey(steamName)) 
			STEAM_CONSUMPTION = (double)simParams.get(steamName);
		
		String tensionName =
				ModelI.createRunParameterName(getURI(), TENSION_RPNAME);
		if (simParams.containsKey(tensionName)) {
			TENSION = (double) simParams.get(tensionName);
		}

		assert	glassBoxInvariants(this) :
				new NeoSim4JavaException("Glass-box invariants violation!");
		assert	blackBoxInvariants(this) :
				new NeoSim4JavaException("Black-box invariant violation!");
	}

	
	// -------------------------------------------------------------------------
	// Optional DEVS simulation protocol: simulation report
	// -------------------------------------------------------------------------
	
	public static class	IronElectricityReport implements SimulationReportI, HEM_ReportI {
		private static final long serialVersionUID = 1L;
		protected String modelURI;
		protected double totalConsumption;

		public IronElectricityReport(String modelURI, double totalConsumption) {
			super();
			this.modelURI = modelURI;
			this.totalConsumption = totalConsumption;
		}

		@Override
		public String getModelURI() {
			return this.modelURI;
		}

		@Override
		public String printout(String indent) {
			StringBuffer ret = new StringBuffer(indent);
			ret.append("---\n");
			ret.append(indent);
			ret.append('|');
			ret.append(this.modelURI);
			ret.append(" report\n");
			ret.append(indent);
			ret.append('|');
			ret.append("total consumption in kwh = ");
			ret.append(this.totalConsumption);
			ret.append(".\n");
			ret.append(indent);
			ret.append("---\n");
			return ret.toString();
		}

		@Override
		public String toString() {
			return this.printout("");
		}
	}

	@Override
	public SimulationReportI getFinalReport() {
		return new IronElectricityReport(this.getURI(), this.totalConsumption);
	}
}
