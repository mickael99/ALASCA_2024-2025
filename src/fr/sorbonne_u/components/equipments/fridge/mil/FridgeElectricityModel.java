package fr.sorbonne_u.components.equipments.fridge.mil;

import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import fr.sorbonne_u.components.equipments.fridge.mil.events.FridgeEventI;
import fr.sorbonne_u.components.equipments.hem.mil.HEM_ReportI;
import fr.sorbonne_u.components.utils.Electricity;
import fr.sorbonne_u.devs_simulation.exceptions.MissingRunParameterException;
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
import fr.sorbonne_u.devs_simulation.utils.Pair;
import fr.sorbonne_u.devs_simulation.utils.StandardLogger;
import fr.sorbonne_u.components.equipments.fridge.mil.events.*;


@ModelExternalEvents(imported = {SwitchOnFridge.class,
								 SwitchOffFridge.class,
								 SetPowerFridge.class,
								 Cool.class,
								 DoNotCool.class})
@ModelExportedVariable(name = "currentIntensity", type = Double.class)
@ModelExportedVariable(name = "currentHeatingPower", type = Double.class)
public class FridgeElectricityModel extends AtomicHIOA {

	// -------------------------------------------------------------------------
	// Constants and variables
	// -------------------------------------------------------------------------
	
	public static enum	State {
		ON,
		COOLING,
		OFF
	}
	
	private static final long serialVersionUID = 1L;
	public static final String URI = FridgeElectricityModel.class.getSimpleName();

	protected static double	IDLE_POWER = 5.0;
	public static double MAX_COOLING_POWER = 500.0;
	protected static double TENSION = 220.0;

	protected State currentState = State.OFF;
	protected boolean consumptionHasChanged = false;
	protected double totalConsumption;

	
	@ExportedVariable(type = Double.class)
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
				new AssertionError("White-box invariants violation!");
		assert	blackBoxInvariants(this) :
				new AssertionError("Black-box invariants violation!");
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
				URI != null && !URI.isEmpty(),
				FridgeElectricityModel.class,
				instance,
				"URI != null && !URI.isEmpty()");
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
	
	public void	setState(State s, Time t) {
		State old = this.currentState;
		this.currentState = s;
		if (old != s) 
			this.consumptionHasChanged = true;					

		assert	glassBoxInvariants(this) :
				new AssertionError("White-box invariants violation!");
		assert	blackBoxInvariants(this) :
				new AssertionError("Black-box invariants violation!");
	}
	
	public State getState() {
		return this.currentState;
	}
	
	public void setCurrentCoolingPower(double newPower, Time t) {
		assert	newPower >= 0.0 &&
				newPower <= FridgeElectricityModel.MAX_COOLING_POWER :
			new AssertionError(
					"Precondition violation: newPower >= 0.0 && "
					+ "newPower <= HeaterElectricityModel.MAX_HEATING_POWER,"
					+ " but newPower = " + newPower);

		double oldPower = this.currentCoolingPower.getValue();
		this.currentCoolingPower.setNewValue(newPower, t);
		if (newPower != oldPower) 
			this.consumptionHasChanged = true;
		
		assert	glassBoxInvariants(this) :
				new AssertionError("White-box invariants violation!");
		assert	blackBoxInvariants(this) :
				new AssertionError("Black-box invariants violation!");
	}
	
	@Override
	public void	initialiseState(Time initialTime) {
		super.initialiseState(initialTime);

		this.currentState = State.OFF;
		this.consumptionHasChanged = false;
		this.totalConsumption = 0.0;

		this.getSimulationEngine().toggleDebugMode();
		this.logMessage("simulation begins.\n");

		assert	glassBoxInvariants(this) :
				new AssertionError("White-box invariants violation!");
		assert	blackBoxInvariants(this) :
				new AssertionError("Black-box invariants violation!");
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
				new AssertionError("White-box invariants violation!");
		assert	blackBoxInvariants(this) :
				new AssertionError("Black-box invariants violation!");

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
				new AssertionError("White-box invariants violation!");
		assert	blackBoxInvariants(this) :
				new AssertionError("Black-box invariants violation!");

		return ret;
	}
	
	@Override
	public void userDefinedInternalTransition(Duration elapsedTime) {
	    super.userDefinedInternalTransition(elapsedTime);

	    Time t = this.getCurrentStateTime();
	    if (this.currentState == State.ON) 
	        this.currentIntensity.setNewValue(FridgeElectricityModel.IDLE_POWER / 
	        									FridgeElectricityModel.TENSION, t);
	    else if (this.currentState == State.COOLING) 
	        this.currentIntensity.setNewValue(this.currentCoolingPower.getValue() / 
	                							FridgeElectricityModel.TENSION,t);
	    else {
	        assert this.currentState == State.OFF;
	        this.currentIntensity.setNewValue(0.0, t);
	    }

	    StringBuffer sb = new StringBuffer("new consumption: ");
	    sb.append(this.currentIntensity.getValue());
	    sb.append(" amperes at ");
	    sb.append(this.currentIntensity.getTime());
	    sb.append(" seconds.\n");
	    this.logMessage(sb.toString());

	    assert glassBoxInvariants(this) :
	            new AssertionError("Glass-box invariants violation!");
	    assert blackBoxInvariants(this) :
	            new AssertionError("Black-box invariants violation!");
	}
	
	@Override
	public void userDefinedExternalTransition(Duration elapsedTime) {
	    super.userDefinedExternalTransition(elapsedTime);

	    ArrayList<EventI> currentEvents = this.getStoredEventAndReset();
	    assert currentEvents != null && currentEvents.size() == 1;

	    Event ce = (Event)currentEvents.get(0);
	    assert ce instanceof FridgeEventI;

	    this.totalConsumption +=
	            Electricity.computeConsumption(elapsedTime, TENSION * this.currentIntensity.getValue());

	    StringBuffer sb = new StringBuffer("execute the external event: ");
	    sb.append(ce.eventAsString());
	    sb.append(".\n");
	    this.logMessage(sb.toString());

	    ce.executeOn(this);

	    assert glassBoxInvariants(this) :
	            new AssertionError("Glass-box invariants violation!");
	    assert blackBoxInvariants(this) :
	            new AssertionError("Black-box invariants violation!");
	}
	
	@Override
	public void endSimulation(Time endTime) {
		Duration d = endTime.subtract(this.getCurrentStateTime());
		this.totalConsumption +=
				Electricity.computeConsumption(d, TENSION * this.currentIntensity.getValue());

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
				new AssertionError("White-box invariants violation!");
		assert	blackBoxInvariants(this) :
				new AssertionError("Black-box invariants violation!");
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
