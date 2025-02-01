package fr.sorbonne_u.components.equipments.battery.mil;

import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import fr.sorbonne_u.components.cyphy.plugins.devs.AtomicSimulatorPlugin;
import fr.sorbonne_u.components.equipments.battery.BatteryI.BATTERY_STATE;
import fr.sorbonne_u.components.equipments.battery.mil.events.AbstractBatteryEvent;
import fr.sorbonne_u.components.equipments.battery.mil.events.SetConsumeBatteryEvent;
import fr.sorbonne_u.components.equipments.battery.mil.events.SetProductBatteryEvent;
import fr.sorbonne_u.components.equipments.battery.mil.events.SetStandByBatteryEvent;
import fr.sorbonne_u.devs_simulation.exceptions.MissingRunParameterException;
import fr.sorbonne_u.devs_simulation.exceptions.NeoSim4JavaException;
import fr.sorbonne_u.devs_simulation.hioa.annotations.InternalVariable;
import fr.sorbonne_u.devs_simulation.hioa.models.AtomicHIOA;
import fr.sorbonne_u.devs_simulation.hioa.models.vars.Value;
import fr.sorbonne_u.devs_simulation.models.annotations.ModelExternalEvents;
import fr.sorbonne_u.devs_simulation.models.events.Event;
import fr.sorbonne_u.devs_simulation.models.events.EventI;
import fr.sorbonne_u.devs_simulation.models.interfaces.ModelI;
import fr.sorbonne_u.devs_simulation.models.time.Duration;
import fr.sorbonne_u.devs_simulation.models.time.Time;
import fr.sorbonne_u.devs_simulation.simulators.interfaces.AtomicSimulatorI;
import fr.sorbonne_u.devs_simulation.utils.StandardLogger;
import fr.sorbonne_u.exceptions.InvariantChecking;

@ModelExternalEvents(imported = {
        SetProductBatteryEvent.class,
        SetConsumeBatteryEvent.class,
        SetStandByBatteryEvent.class
})
public class BatteryChargeLevelModel extends AtomicHIOA implements BatteryOperationI {

	// -------------------------------------------------------------------------
	// Constants and variables
	// -------------------------------------------------------------------------
	
	private static final long serialVersionUID = 1L;
	
	public static final String MIL_URI = BatteryChargeLevelModel.class.getSimpleName() + "-MIL";
	public static final String MIL_RT_URI = BatteryChargeLevelModel.class.getSimpleName() + "-MIL-RT";
	public static final String SIL_URI = BatteryChargeLevelModel.class.getSimpleName() + "-SIL-RT";
	
    protected static final double STEP = 0.1;
    
    protected static final double INITIAL_BATTERY_LEVEL = 0.5;
    // Drain 15% of its total capacity in hour
	protected static double DISCHARGE_SPEED = 0.15;
	
	// Recharges 35% of its total capacity in hour
	protected static double CHARGE_SPEED = 0.35; 
	
	protected final Duration evaluationStep;

    private BATTERY_STATE currentState;

    @InternalVariable(type = Double.class)
    protected final Value<Double> currentChargeLevel = new Value<Double>(this);
		
		
	// -------------------------------------------------------------------------
	// Constructors
	// -------------------------------------------------------------------------
    
    public BatteryChargeLevelModel(String uri, TimeUnit simulatedTimeUnit, AtomicSimulatorI simulationEngine) throws Exception {
        super(uri, simulatedTimeUnit, simulationEngine);
        
        this.evaluationStep = new Duration(STEP, getSimulatedTimeUnit());
        this.currentState = BATTERY_STATE.STANDBY;
        
        this.getSimulationEngine().setLogger(new StandardLogger());
        
        assert	glassBoxInvariants(this) :
			new NeoSim4JavaException(
					"BatteryChargeLevelModel.glassBoxInvariants(this)");
        assert	blackBoxInvariants(this) :
			new NeoSim4JavaException(
					"BatteryChargeLevelModel.blackBoxInvariants(this)");
    }
    
    
    // -------------------------------------------------------------------------
	// Invariants
	// -------------------------------------------------------------------------

	protected static boolean glassBoxInvariants(BatteryChargeLevelModel instance) {
		assert	instance != null :
				new NeoSim4JavaException("Precondition violation: "
						+ "instance != null");

		boolean ret = true;
		ret &= InvariantChecking.checkGlassBoxInvariant(
					STEP >= 0.0,
					BatteryChargeLevelModel.class,
					instance,
					"STEP >= 0.0");
		ret &= InvariantChecking.checkGlassBoxInvariant(
				CHARGE_SPEED > 0.0,
				BatteryChargeLevelModel.class,
					instance,
					"CHARGE_SPEED > 0.0");
		ret &= InvariantChecking.checkGlassBoxInvariant(
					instance.currentState != null,
					BatteryChargeLevelModel.class,
					instance,
					"currentState != null");
		return ret;
	}

	protected static boolean blackBoxInvariants(BatteryChargeLevelModel instance) {
		assert	instance != null :
				new NeoSim4JavaException("Precondition violation: "
						+ "instance != null");

		boolean ret = true;
		ret &= InvariantChecking.checkBlackBoxInvariant(
					MIL_URI != null && !MIL_URI.isEmpty(),
					BatteryChargeLevelModel.class,
					instance,
					"MIL_URI != null && !MIL_URI.isEmpty()");
		ret &= InvariantChecking.checkBlackBoxInvariant(
					MIL_RT_URI != null && !MIL_RT_URI.isEmpty(),
					BatteryChargeLevelModel.class,
					instance,
					"MIL_RT_URI != null && !MIL_RT_URI.isEmpty()");
		ret &= InvariantChecking.checkBlackBoxInvariant(
					SIL_URI != null && !SIL_URI.isEmpty(),
					BatteryChargeLevelModel.class,
					instance,
					"SIL_URI != null && !SIL_URI.isEmpty()");
		return ret;
	}

    
    // -------------------------------------------------------------------------
 	// Methods
 	// -------------------------------------------------------------------------
    
    @Override
    public BATTERY_STATE getCurrentState() {
    	return this.currentState;
    }
    
    public double getCurrentChargeLevel() {
    	return this.currentChargeLevel.getValue();
    }
    
    @Override
    public void setProduction() {
    	this.currentState = BATTERY_STATE.PRODUCT;
    }
    
    @Override
    public void setConsumption() {
    	this.currentState = BATTERY_STATE.CONSUME;
    }
    
    @Override
    public void setStandBy() {
    	this.currentState = BATTERY_STATE.STANDBY;
    }
    
    public void charge(Duration d) {
    	double duration = d.getSimulatedDuration();
    	
    	double currentLevel = this.currentChargeLevel.getValue();
    	double newLevel = Math.min(currentLevel + (CHARGE_SPEED * duration), 1.0);
    	
    	this.currentChargeLevel.setNewValue(newLevel, this.getCurrentStateTime());
    }
    
    public void discharge(Duration d) {
        double duration = d.getSimulatedDuration();
        
        double currentLevel = this.currentChargeLevel.getValue();
        double newChargeLevel = Math.max(currentLevel - (DISCHARGE_SPEED * duration), 0.0);
        
        this.currentChargeLevel.setNewValue(newChargeLevel, this.getCurrentStateTime());
    }

    @Override
    public void initialiseState(Time initialTime) {
        super.initialiseState(initialTime);

        this.currentChargeLevel.initialise(INITIAL_BATTERY_LEVEL);

        this.getSimulationEngine().toggleDebugMode();
        logMessage("Simulations starts...\n");
        
        assert	glassBoxInvariants(this) :
			new NeoSim4JavaException(
					"BatteryChargeLevelModel.glassBoxInvariants(this)");
        assert	blackBoxInvariants(this) :
			new NeoSim4JavaException(
					"BatteryChargeLevelModel.blackBoxInvariants(this)");
    }
    
    @Override
    public ArrayList<EventI> output() {        
        return null;
    }
    
	@Override
	public Duration timeAdvance() {
		return this.evaluationStep;
	}

	@Override
    public void userDefinedInternalTransition(Duration elapsedTime) {
        super.userDefinedInternalTransition(elapsedTime);

        double v = this.currentChargeLevel.getValue();
        if(v >= 0.0 && this.currentState == BATTERY_STATE.CONSUME) 
            this.discharge(elapsedTime);
        else if (v <= 1.0 && this.currentState == BATTERY_STATE.PRODUCT) 
            this.charge(elapsedTime);
     
        // Tracing
        String stateString;
        if(this.currentState == BATTERY_STATE.STANDBY)
        	stateString = "stand by";
        else if (this.currentState == BATTERY_STATE.CONSUME)
        	stateString = "discharging";
        else 
        	stateString = "charging";
        
        logMessage("Battery is " + stateString + " | Charge level : " + currentChargeLevel.getValue() + " at " + currentChargeLevel.getTime() + "\n");
    
        assert	glassBoxInvariants(this) :
			new NeoSim4JavaException(
					"BatteryChargeLevelModel.glassBoxInvariants(this)");
        assert	blackBoxInvariants(this) :
			new NeoSim4JavaException(
					"BatteryChargeLevelModel.blackBoxInvariants(this)");
	}
	
	@Override
    public void userDefinedExternalTransition(Duration elapsedTime) {
        ArrayList<EventI> currentEvents = this.getStoredEventAndReset();
        assert	currentEvents != null && currentEvents.size() == 1;
        Event currentEvent = (Event) currentEvents.get(0);

        assert currentEvent instanceof AbstractBatteryEvent;
        currentEvent.executeOn(this);

        super.userDefinedExternalTransition(elapsedTime);
    
        assert	glassBoxInvariants(this) :
			new NeoSim4JavaException(
					"BatteryChargeLevelModel.glassBoxInvariants(this)");
        assert	blackBoxInvariants(this) :
			new NeoSim4JavaException(
					"BatteryChargeLevelModel.blackBoxInvariants(this)");
	}
	
	@Override
    public void endSimulation(Time endTime) {
        logMessage("Simulation ends!\n");
        super.endSimulation(endTime);
    }
	
	public static final String DISCHARGE_SPEED_RUNPNAME = "DISCHARGE_SPEED";
	public static final String CHARGE_SPEED_RUNPNAME = "CHARGE_SPEED";
	
	@Override
	public void setSimulationRunParameters(Map<String, Object> simParams) throws MissingRunParameterException {
		super.setSimulationRunParameters(simParams);

		if (simParams.containsKey(
						AtomicSimulatorPlugin.OWNER_RUNTIME_PARAMETER_NAME)) 
		{
			this.getSimulationEngine().setLogger(
						AtomicSimulatorPlugin.createComponentLogger(simParams));
		}

		String dischargeSpeed = ModelI.createRunParameterName(getURI(), DISCHARGE_SPEED_RUNPNAME);
		if (simParams.containsKey(dischargeSpeed)) 
			DISCHARGE_SPEED = (double)simParams.get(dischargeSpeed);
		
		String chargeSpeed = ModelI.createRunParameterName(getURI(), CHARGE_SPEED_RUNPNAME);
		if (simParams.containsKey(chargeSpeed)) 
			CHARGE_SPEED = (double) simParams.get(chargeSpeed);
	}
}
