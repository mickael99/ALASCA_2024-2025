package fr.sorbonne_u.components.equipments.battery.mil;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import fr.sorbonne_u.components.equipments.battery.BatteryI;
import fr.sorbonne_u.components.equipments.battery.mil.events.SetProductBatteryEvent;
import fr.sorbonne_u.components.equipments.battery.mil.events.AbstractBatteryEvent;
import fr.sorbonne_u.components.equipments.battery.mil.events.SetConsumeBatteryEvent;
import fr.sorbonne_u.devs_simulation.hioa.annotations.ExportedVariable;
import fr.sorbonne_u.devs_simulation.hioa.annotations.ImportedVariable;
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
        SetConsumeBatteryEvent.class
})
public class BatteryElectricityModel extends AtomicHIOA {

	// -------------------------------------------------------------------------
	// Constants and variables
	// -------------------------------------------------------------------------
	
	private static final long serialVersionUID = 1L;
	public static final String URI = BatteryElectricityModel.class.getSimpleName();
	
	protected static final double PRODUCTION = 1200.0;
	protected static final double CONSUMPTION = 1000.0;
	
	protected BatteryI.STATE currentState;
	protected boolean hasChanged;
	
	// Between 0 and 1
	@ImportedVariable(type = Double.class)
	protected Value<Double> currentChargeLevel;
	
	@ExportedVariable(type = Double.class)
	protected final Value<Double> currentProduction = new Value<Double>(this);

    @ExportedVariable(type = Double.class)
    protected final Value<Double> currentConsumption = new Value<Double>(this);
	
    
    // -------------------------------------------------------------------------
 	// Constructors
 	// -------------------------------------------------------------------------
    
    public BatteryElectricityModel(String uri, TimeUnit simulatedTimeUnit, AtomicSimulatorI simulationEngine) throws Exception {
		super(uri, simulatedTimeUnit, simulationEngine);
		this.getSimulationEngine().setLogger(new StandardLogger());
	}
    
    
    // -------------------------------------------------------------------------
 	// Methods
 	// -------------------------------------------------------------------------
    
    public BatteryI.STATE getCurrentState() {
    	return this.currentState;
    }
    
    public void setProduction() {
    	this.currentState = BatteryI.STATE.PRODUCT;
    }
    
    public void setConsumption() {
    	this.currentState = BatteryI.STATE.CONSUME;
    }
    
    public void setHasChanged(boolean hc) {
    	this.hasChanged = hc;
    }
    
    @Override
    public void initialiseState(Time initialTime) {
        super.initialiseState(initialTime);
        
        if (!this.currentProduction.isInitialised() ||
				!this.currentConsumption.isInitialised()) {
			this.currentProduction.initialise(0.0);
			this.currentConsumption.initialise(0.0);
			
			StringBuffer sbp = new StringBuffer("new production: ");
			sbp.append(this.currentProduction.getValue());
			sbp.append(" amperes at ");
			sbp.append(this.currentProduction.getTime());
			sbp.append(" seconds.\n");
			
			StringBuffer sbc = new StringBuffer("new consumption: ");
			sbc.append(this.currentConsumption.getValue());
			sbc.append(" amperes at ");
			sbc.append(this.currentConsumption.getTime());
			sbc.append(" seconds.\n");
			
			this.logMessage(sbp.toString());
			this.logMessage(sbc.toString());
		} 
		
        this.currentState = BatteryI.STATE.CONSUME;
        this.hasChanged = false;

        this.getSimulationEngine().toggleDebugMode();
        logMessage("Simulation starts...\n");
    }
    
    @Override
    public ArrayList<EventI> output() {
        return null;
    }
    
	@Override
	public Duration timeAdvance() {
		if(hasChanged) {
            hasChanged = false;
            return Duration.zero(this.getSimulatedTimeUnit());
        }
		
        return Duration.INFINITY;
	}

	@Override
    public void userDefinedInternalTransition(Duration elapsedTime) {
        super.userDefinedInternalTransition(elapsedTime);
        Time t = this.getCurrentStateTime();
        
        switch (this.currentState) {
            case PRODUCT:
                this.currentProduction.setNewValue(PRODUCTION, t);
                this.currentConsumption.setNewValue(0.0, t);
                break;

            case CONSUME:
            	this.currentProduction.setNewValue(0.0, t);
                this.currentConsumption.setNewValue(CONSUMPTION, t);
                break;
        }

        logMessage("Current production " + this.currentProduction.getValue() + " at " + this.currentProduction.getTime()
        		+ " current consumption" + this.currentConsumption.getValue() + " at " + this.currentProduction.getTime()
                + " | Charge level " + this.currentChargeLevel.getValue() * 100 + "%" + "\n");
    }
	
	 @Override
    public void userDefinedExternalTransition(Duration elapsedTime) {
        ArrayList<EventI> currentEvents = this.getStoredEventAndReset();
        assert currentEvents != null && currentEvents.size() == 1;
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
