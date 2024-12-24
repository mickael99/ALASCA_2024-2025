package fr.sorbonne_u.components.equipments.fridge.mil;

import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import fr.sorbonne_u.components.equipments.fridge.mil.events.FridgeEventI;
import fr.sorbonne_u.components.equipments.hem.mil.HEM_ReportI;
import fr.sorbonne_u.components.utils.Electricity;
import fr.sorbonne_u.devs_simulation.exceptions.MissingRunParameterException;
import fr.sorbonne_u.devs_simulation.exceptions.NeoSim4JavaException;
import fr.sorbonne_u.devs_simulation.hioa.annotations.ExportedVariable;
import fr.sorbonne_u.devs_simulation.hioa.annotations.InternalVariable;
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
import fr.sorbonne_u.devs_simulation.utils.Pair;
import fr.sorbonne_u.devs_simulation.utils.StandardLogger;
import fr.sorbonne_u.components.cyphy.plugins.devs.AtomicSimulatorPlugin;
import fr.sorbonne_u.components.equipments.fridge.mil.events.*;


@ModelExternalEvents(imported = {SwitchOnFridge.class,
								 SwitchOffFridge.class,
								 SetPowerFridge.class,
								 CoolFridge.class,
								 DoNotCoolFridge.class,
								 OpenDoorFridge.class,
								 CloseDoorFridge.class})
@ModelExportedVariable(name = "currentIntensity", type = Double.class)
public class FridgeElectricityModel extends AtomicHIOA implements FridgeOperationI{

	// -------------------------------------------------------------------------
	// Constants and variables
	// -------------------------------------------------------------------------
	
	public static enum FridgeState {
		OFF,
		ON,
		COOLING,
		DOOR_OPEN
	}
	
	private static final long serialVersionUID = 1L;
	
	public static final String	MIL_URI = FridgeElectricityModel.class.getSimpleName() + "-MIL";
	public static final String	MIL_RT_URI = FridgeElectricityModel.class.getSimpleName() + "-MIL-RT";
	public static final String	SIL_URI = FridgeElectricityModel.class.getSimpleName() + "-SIL";

	protected static double	IDLE_POWER = 5.0;
	public static double MAX_COOLING_POWER = 500.0;
	protected static double DOOR_OPEN_EXTRA_CONSUMPTION_FACTOR = 1.2;
	protected static double TENSION = 220.0;

	protected FridgeState currentState = FridgeState.OFF;
	protected boolean consumptionHasChanged = false;
	protected double totalConsumption;

	
	@InternalVariable(type = Double.class)
	protected final Value<Double> currentCoolingPower = new Value<Double>(this);
	
	@ExportedVariable(type = Double.class)
	protected final Value<Double> currentIntensity = new Value<Double>(this);
	
	
	// -------------------------------------------------------------------------
	// Constructors
	// -------------------------------------------------------------------------

	public FridgeElectricityModel(String uri, TimeUnit simulatedTimeUnit, AtomicSimulatorI simulationEngine) throws Exception {
		super(uri, simulatedTimeUnit, simulationEngine);
		this.getSimulationEngine().setLogger(new StandardLogger());

		assert	glassBoxInvariants(this) :
				new NeoSim4JavaException("White-box invariants violation!");
		assert	blackBoxInvariants(this) :
				new NeoSim4JavaException("Black-box invariants violation!");
	}
	
	
	// -------------------------------------------------------------------------
	// Invariants
	// -------------------------------------------------------------------------

	protected static boolean glassBoxInvariants(FridgeElectricityModel instance) {
		assert	instance != null :
				new AssertionError("Precondition violation: instance != null");

		boolean ret = true;
		ret &= InvariantChecking.checkGlassBoxInvariant(
					IDLE_POWER >= 0.0,
					FridgeElectricityModel.class,
					instance,
					"IDLE_POWER >= 0.0");
		ret &= InvariantChecking.checkGlassBoxInvariant(
					MAX_COOLING_POWER > IDLE_POWER,
					FridgeElectricityModel.class,
					instance,
					"MAX_COOLING_POWER > IDLE_POWER");
		ret &= InvariantChecking.checkGlassBoxInvariant(
					TENSION > 0.0,
					FridgeElectricityModel.class,
					instance,
					"TENSION > 0.0");
		ret &= InvariantChecking.checkGlassBoxInvariant(
					instance.currentState != null,
					FridgeElectricityModel.class,
					instance,
					"currentState != null");
		ret &= InvariantChecking.checkGlassBoxInvariant(
					instance.totalConsumption >= 0.0,
					FridgeElectricityModel.class,
					instance,
					"totalConsumption >= 0.0");
		ret &= InvariantChecking.checkGlassBoxInvariant(
					!instance.currentCoolingPower.isInitialised() ||
								instance.currentCoolingPower.getValue() >= 0.0,
					FridgeElectricityModel.class,
					instance,
					"!currentCoolingPower.isInitialised() || "
							+ "currentCoolingPower.getValue() >= 0.0");
		ret &= InvariantChecking.checkGlassBoxInvariant(
					!instance.currentIntensity.isInitialised() ||
									instance.currentIntensity.getValue() >= 0.0,
					FridgeElectricityModel.class,
					instance,
					"!currentIntensity.isInitialised() || "
							+ "currentIntensity.getValue() >= 0.0");
		return ret;
	}

	protected static boolean blackBoxInvariants(FridgeElectricityModel instance) {
		assert	instance != null :
				new AssertionError("Precondition violation: instance != null");

		boolean ret = true;
		ret &= InvariantChecking.checkBlackBoxInvariant(
				MIL_URI != null && !MIL_URI.isEmpty(),
				FridgeElectricityModel.class,
				instance,
				"MIL_URI != null && !mil_URI.isEmpty()");
		ret &= InvariantChecking.checkBlackBoxInvariant(
				SIL_URI != null && !SIL_URI.isEmpty(),
				FridgeElectricityModel.class,
				instance,
				"SIL_URI != null && !sil_URI.isEmpty()");
		ret &= InvariantChecking.checkBlackBoxInvariant(
				MIL_RT_URI != null && !MIL_RT_URI.isEmpty(),
				FridgeElectricityModel.class,
				instance,
				"MIL_RT_URI != null && !mil_rt_URI.isEmpty()");
		ret &= InvariantChecking.checkBlackBoxInvariant(
				IDLE_POWER_RUNPNAME != null &&
									!IDLE_POWER_RUNPNAME.isEmpty(),
				FridgeElectricityModel.class,
				instance,
				"IDLE_POWER_RUNPNAME != null && "
				+ "!IDLE_POWER_RUNPNAME.isEmpty()");
		ret &= InvariantChecking.checkBlackBoxInvariant(
				MAX_COOLING_POWER_RUNPNAME != null &&
									!MAX_COOLING_POWER_RUNPNAME.isEmpty(),
				FridgeElectricityModel.class,
				instance,
				"MAX_COOLING_POWER_RUNPNAME != null && "
				+ "!MAX_COOLING_POWER_RUNPNAME.isEmpty()");
		ret &= InvariantChecking.checkBlackBoxInvariant(
				TENSION_RUNPNAME != null && !TENSION_RUNPNAME.isEmpty(),
				FridgeElectricityModel.class,
				instance,
				"TENSION_RUNPNAME != null && !TENSION_RUNPNAME.isEmpty()");
		return ret;
	}
	
	
	// -------------------------------------------------------------------------
	// Methods
	// -------------------------------------------------------------------------
	
	@Override
	public void	setState(FridgeState s) {
		FridgeState old = this.currentState;
		this.currentState = s;
		if (old != s) 
			this.consumptionHasChanged = true;					

		assert	glassBoxInvariants(this) :
				new NeoSim4JavaException("White-box invariants violation!");
		assert	blackBoxInvariants(this) :
				new NeoSim4JavaException("Black-box invariants violation!");
	}
	
	@Override
	public FridgeState getState() {
		return this.currentState;
	}
	
	@Override
	public void setCurrentCoolingPower(double newPower, Time t) {
		assert	newPower >= 0.0 &&
				newPower <= FridgeElectricityModel.MAX_COOLING_POWER :
			new NeoSim4JavaException(
					"Precondition violation: newPower >= 0.0 && "
					+ "newPower <= FridgeElectricityModel.MAX_COOLING_POWER,"
					+ " but newPower = " + newPower);

		double oldPower = this.currentCoolingPower.getValue();
		this.currentCoolingPower.setNewValue(newPower, t);
		if (newPower != oldPower) 
			this.consumptionHasChanged = true;
		
		assert	glassBoxInvariants(this) :
				new NeoSim4JavaException("White-box invariants violation!");
		assert	blackBoxInvariants(this) :
				new NeoSim4JavaException("Black-box invariants violation!");
	}
	
	@Override
	public void	initialiseState(Time initialTime) {
		super.initialiseState(initialTime);

		this.currentState = FridgeState.OFF;
		this.consumptionHasChanged = false;
		this.totalConsumption = 0.0;

		this.getSimulationEngine().toggleDebugMode();
		this.logMessage("simulation begins.\n");

		assert	glassBoxInvariants(this) :
				new NeoSim4JavaException("White-box invariants violation!");
		assert	blackBoxInvariants(this) :
				new NeoSim4JavaException("Black-box invariants violation!");
	}
	
	@Override
	public boolean useFixpointInitialiseVariables() {
		return true;
	}
	
	@Override
	public Pair<Integer, Integer> fixpointInitialiseVariables() {
		Pair<Integer, Integer> ret = null;

		if (!this.currentIntensity.isInitialised() ||
								!this.currentCoolingPower.isInitialised()) {
			this.currentIntensity.initialise(0.0);
			this.currentCoolingPower.initialise(MAX_COOLING_POWER);

			StringBuffer sb = new StringBuffer("new consumption: ");
			sb.append(this.currentIntensity.getValue());
			sb.append(" amperes at ");
			sb.append(this.currentIntensity.getTime());
			sb.append(" seconds.\n");
			this.logMessage(sb.toString());
			ret = new Pair<>(2, 0);
		} else 
			ret = new Pair<>(0, 0);
		
		assert	glassBoxInvariants(this) :
				new NeoSim4JavaException("White-box invariants violation!");
		assert	blackBoxInvariants(this) :
				new NeoSim4JavaException("Black-box invariants violation!");

		return ret;
	}
	
	@Override
	public ArrayList<EventI> output() {
		return null;
	}
	
	@Override
	public Duration timeAdvance() {
		Duration ret = null;

		if (this.consumptionHasChanged) {
			this.consumptionHasChanged = false;
			ret = Duration.zero(this.getSimulatedTimeUnit());
		} else 
			ret = Duration.INFINITY;

		assert	glassBoxInvariants(this) :
				new NeoSim4JavaException("White-box invariants violation!");
		assert	blackBoxInvariants(this) :
				new NeoSim4JavaException("Black-box invariants violation!");

		return ret;
	}
	
	protected void updateTotalConsumption(Duration elapsedTime) {
	    switch (this.currentState) {
	        case ON:
	            this.totalConsumption += Electricity.computeConsumption(
	                elapsedTime, TENSION * IDLE_POWER);
	            break;

	        case COOLING:
	            this.totalConsumption += Electricity.computeConsumption(
	                elapsedTime, TENSION * this.currentCoolingPower.getValue());
	            break;
	         
	        case DOOR_OPEN:
	        	this.totalConsumption += Electricity.computeConsumption(
		                elapsedTime, TENSION * IDLE_POWER * DOOR_OPEN_EXTRA_CONSUMPTION_FACTOR);
		            break;

	        case OFF:
	            // No consumption if the fridge is turning off
	            break;
	    }
	}
	
	@Override
	public void userDefinedInternalTransition(Duration elapsedTime) {
	    super.userDefinedInternalTransition(elapsedTime);

	    Time t = this.getCurrentStateTime();
	    if (this.currentState == FridgeState.ON) 
	        this.currentIntensity.setNewValue(FridgeElectricityModel.IDLE_POWER / 
	        									FridgeElectricityModel.TENSION, t);
	    else if (this.currentState == FridgeState.COOLING) 
	        this.currentIntensity.setNewValue(this.currentCoolingPower.getValue() / 
	                							FridgeElectricityModel.TENSION,t);
	    else {
	        assert this.currentState == FridgeState.OFF;
	        this.currentIntensity.setNewValue(0.0, t);
	    }

	    if(this.currentIntensity.getValue() == null)
	    	System.out.println("c'est nul");
	    StringBuffer sb = new StringBuffer("new consumption: ");
	    sb.append(this.currentIntensity.getValue());
	    sb.append(" amperes at ");
	    sb.append(this.currentIntensity.getTime());
	    sb.append(" seconds.\n");
	    this.logMessage(sb.toString());

	    assert glassBoxInvariants(this) :
	            new NeoSim4JavaException("Glass-box invariants violation!");
	    assert blackBoxInvariants(this) :
	            new NeoSim4JavaException("Black-box invariants violation!");
	}
	
	@Override
	public void userDefinedExternalTransition(Duration elapsedTime) {
	    super.userDefinedExternalTransition(elapsedTime);

	    ArrayList<EventI> currentEvents = this.getStoredEventAndReset();
	    assert currentEvents != null && currentEvents.size() == 1;

	    Event ce = (Event)currentEvents.get(0);
	    assert ce instanceof FridgeEventI;

	    this.updateTotalConsumption(elapsedTime);

	    StringBuffer sb = new StringBuffer("execute the external event: ");
	    sb.append(ce.eventAsString());
	    sb.append(".\n");
	    this.logMessage(sb.toString());

	    ce.executeOn(this);

	    assert glassBoxInvariants(this) :
	            new NeoSim4JavaException("Glass-box invariants violation!");
	    assert blackBoxInvariants(this) :
	            new NeoSim4JavaException("Black-box invariants violation!");
	}
	
	@Override
	public void endSimulation(Time endTime) {
		Duration d = endTime.subtract(this.getCurrentStateTime());
		
		this.updateTotalConsumption(d);

		this.logMessage("simulation ends.\n");
		
		super.endSimulation(endTime);
	}

	
	// -------------------------------------------------------------------------
	// Optional DEVS simulation protocol: simulation report
	// -------------------------------------------------------------------------
	
	public static final String	IDLE_POWER_RUNPNAME = "IDLE_POWER_RUNPNAME";
	public static final String	MAX_COOLING_POWER_RUNPNAME = "MAX_COOLING_POWER_RUNPNAME";
	public static final String	TENSION_RUNPNAME = "TENSION";
	
	@Override
	public void setSimulationRunParameters(Map<String, Object> simParams) throws MissingRunParameterException {
		super.setSimulationRunParameters(simParams);

		if(simParams.containsKey(AtomicSimulatorPlugin.OWNER_RUNTIME_PARAMETER_NAME)) {
			this.getSimulationEngine().setLogger(
					AtomicSimulatorPlugin.createComponentLogger(simParams));
		}
		
		String idleName =
			ModelI.createRunParameterName(getURI(), IDLE_POWER_RUNPNAME);
		if (simParams.containsKey(idleName)) 
			IDLE_POWER = (double)simParams.get(idleName);
		
		String coolingName =
			ModelI.createRunParameterName(getURI(), MAX_COOLING_POWER_RUNPNAME);
		if (simParams.containsKey(coolingName)) 
			MAX_COOLING_POWER = (double) simParams.get(coolingName);
		
		String tensionName =
			ModelI.createRunParameterName(getURI(), TENSION_RUNPNAME);
		if (simParams.containsKey(tensionName)) 
			TENSION = (double) simParams.get(tensionName);
		
		assert	glassBoxInvariants(this) :
				new NeoSim4JavaException("White-box invariants violation!");
		assert	blackBoxInvariants(this) :
				new NeoSim4JavaException("Black-box invariants violation!");
	}
	
	public static class FridgeElectricityReport implements SimulationReportI, HEM_ReportI {
		private static final long serialVersionUID = 1L;
		protected String modelURI;
		protected double totalConsumption; 

		public FridgeElectricityReport(String modelURI, double totalConsumption) {
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
	}

	@Override
	public SimulationReportI getFinalReport() {
		return new FridgeElectricityReport(this.getURI(), this.totalConsumption);
	}
}
