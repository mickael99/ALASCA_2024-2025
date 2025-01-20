package fr.sorbonne_u.components.equipments.battery.mil;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import fr.sorbonne_u.components.equipments.battery.BatteryI.BATTERY_STATE;
import fr.sorbonne_u.components.equipments.battery.mil.events.AbstractBatteryEvent;
import fr.sorbonne_u.components.equipments.battery.mil.events.SetConsumeBatteryEvent;
import fr.sorbonne_u.components.equipments.battery.mil.events.SetProductBatteryEvent;
import fr.sorbonne_u.components.equipments.battery.mil.events.SetStandByBatteryEvent;
import fr.sorbonne_u.devs_simulation.hioa.annotations.ExportedVariable;
import fr.sorbonne_u.devs_simulation.hioa.models.AtomicHIOA;
import fr.sorbonne_u.devs_simulation.hioa.models.vars.Value;
import fr.sorbonne_u.devs_simulation.models.annotations.ModelExternalEvents;
import fr.sorbonne_u.devs_simulation.models.events.Event;
import fr.sorbonne_u.devs_simulation.models.events.EventI;
import fr.sorbonne_u.devs_simulation.models.time.Duration;
import fr.sorbonne_u.devs_simulation.models.time.Time;
import fr.sorbonne_u.devs_simulation.simulators.interfaces.AtomicSimulatorI;
import fr.sorbonne_u.devs_simulation.utils.StandardLogger;

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
	
    protected static final double STEP = 0.1;
    
    // Drain 15% of its total capacity in hour
	protected static final double DISCHARGE_SPEED = 0.15;
	
	// Recharges 35% of its total capacity in hour
	protected static final double CHARGE_SPEED = 0.35; 
	
	protected final Duration evaluationStep;

    private BATTERY_STATE currentState;

    @ExportedVariable(type = Double.class)
    protected final Value<Double> currentChargeLevel = new Value<Double>(this);
		
		
	// -------------------------------------------------------------------------
	// Constructors
	// -------------------------------------------------------------------------
    
    public BatteryChargeLevelModel(String uri, TimeUnit simulatedTimeUnit, AtomicSimulatorI simulationEngine) throws Exception {
        super(uri, simulatedTimeUnit, simulationEngine);
        evaluationStep = new Duration(STEP, getSimulatedTimeUnit());
        this.getSimulationEngine().setLogger(new StandardLogger());
    }

    
    // -------------------------------------------------------------------------
 	// Methods
 	// -------------------------------------------------------------------------
    
    @Override
    public BATTERY_STATE getCurrentState() {
    	return this.currentState;
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

        currentState = BATTERY_STATE.CONSUME;
        this.currentChargeLevel.initialise(1.0);

        this.getSimulationEngine().toggleDebugMode();
        logMessage("Simulations starts...\n");
    }
    
    @Override
    public ArrayList<EventI> output() {
    	double v = this.currentChargeLevel.getValue();
    	
    	ArrayList<EventI> res = new ArrayList<>();
        if(v <= 0.0) {
            res.add(new SetProductBatteryEvent(currentChargeLevel.getTime()));
            return res;
        }
        else if(v >= 1) {
        	res.add(new SetConsumeBatteryEvent(currentChargeLevel.getTime()));
            return res;
        }
        
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
     
        this.currentChargeLevel.setNewValue(this.currentChargeLevel.getValue(), getCurrentStateTime());

        // Tracing
        String stateString = currentState == BATTERY_STATE.PRODUCT ? "charging" : "discharging";
        logMessage("Battery is " + stateString + " | Charge level : " + currentChargeLevel.getValue() + " at " + currentChargeLevel.getTime() + "\n");
    }
	
	@Override
    public void userDefinedExternalTransition(Duration elapsedTime) {
        ArrayList<EventI> currentEvents = this.getStoredEventAndReset();
        assert	currentEvents != null && currentEvents.size() == 1;
        Event currentEvent = (Event) currentEvents.get(0);

        assert currentEvent instanceof AbstractBatteryEvent;
        currentEvent.executeOn(this);

        super.userDefinedExternalTransition(elapsedTime);
    }
	
	@Override
    public void endSimulation(Time endTime) {
        logMessage("Simulation ends!\n");
        super.endSimulation(endTime);
    }
}
