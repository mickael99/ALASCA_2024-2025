package fr.sorbonne_u.components.equipments.meter.mil;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import fr.sorbonne_u.devs_simulation.hioa.annotations.ImportedVariable;
import fr.sorbonne_u.devs_simulation.hioa.annotations.InternalVariable;
import fr.sorbonne_u.devs_simulation.hioa.models.AtomicHIOA;
import fr.sorbonne_u.devs_simulation.hioa.models.vars.Value;
import fr.sorbonne_u.devs_simulation.models.events.EventI;
import fr.sorbonne_u.devs_simulation.models.time.Duration;
import fr.sorbonne_u.devs_simulation.models.time.Time;
import fr.sorbonne_u.devs_simulation.simulators.interfaces.AtomicSimulatorI;
import fr.sorbonne_u.devs_simulation.utils.StandardLogger;

public class MeterElectricityModel extends AtomicHIOA {

	// -------------------------------------------------------------------------
	// Constants and variables
	// -------------------------------------------------------------------------

    private static final long serialVersionUID = 1L;
	
    public static final String URI = MeterElectricityModel.class.getSimpleName();
	protected static final double STEP = 0.1; // In seconds
	protected final Duration evaluationStep;
	
	// House devices (consumption only)
	//@ImportedVariable(type = Double.class)
	//protected Value<Double> currentToasterConsumption;
	 
	@ImportedVariable(type = Double.class)
	protected Value<Double> currentIronConsumption = new Value<Double>(this);
	 
	@ImportedVariable(type = Double.class)
	protected Value<Double> currentFridgeConsumption = new Value<Double>(this);
	
	@ImportedVariable(type = Double.class)
	protected Value<Double> currentBatteryConsumption = new Value<Double>(this);
	
	 
	@ImportedVariable(type = Double.class)
	protected Value<Double> currentSmartLightingConsumption = new Value<Double>(this);
	
	// Production devices
	@ImportedVariable(type = Double.class)
	protected Value<Double> currentWindTurbineProduction = new Value<Double>(this);
	
	@ImportedVariable(type = Double.class)
	protected Value<Double> currentBatteryProduction = new Value<Double>(this);
	
	@ImportedVariable(type = Double.class)
	protected Value<Double> currentGeneratorProduction = new Value<Double>(this);
	
	// Total values
	@InternalVariable(type = Double.class)
	protected Value<Double> currentConsumption = new Value<Double>(this);
	
	@InternalVariable(type = Double.class)
	protected Value<Double> currentProduction = new Value<Double>(this);
	
	
	// -------------------------------------------------------------------------
	// Constructors
	// -------------------------------------------------------------------------
	
	public MeterElectricityModel(String uri, TimeUnit simulatedTimeUnit, AtomicSimulatorI simulationEngine) {
		super(uri, simulatedTimeUnit, simulationEngine);
		this.evaluationStep = new Duration(STEP, getSimulatedTimeUnit());
		this.getSimulationEngine().setLogger(new StandardLogger());
	}
	
	
	// -------------------------------------------------------------------------
	// Methods
	// -------------------------------------------------------------------------
	
	@Override
    public void initialiseState(Time initialTime) {
        super.initialiseState(initialTime);

        this.currentConsumption.initialise(0.0);
        this.currentProduction.initialise(0.0);

        this.getSimulationEngine().toggleDebugMode();
        
        logMessage("Simulation begins. \n");
    }
	
	public Double getCurrentProduction() {
		return this.currentProduction.getValue();
	}
	
	public Double getCurrentConsumption() {
		return this.currentConsumption.getValue();
	}
	
	public boolean updateConsumption(Time t) {
        double consumption =
		        		(this.currentSmartLightingConsumption == null || this.currentSmartLightingConsumption.getValue() == null
		        				? 0.0 : currentSmartLightingConsumption.getValue()) +
                        (this.currentIronConsumption == null || this.currentFridgeConsumption.getValue() == null
                        		? 0.0 : currentIronConsumption.getValue()) +
                        (this.currentFridgeConsumption == null || this.currentFridgeConsumption.getValue() == null
                        		? 0.0 : currentFridgeConsumption.getValue()) +
                        (this.currentBatteryConsumption == null || this.currentBatteryConsumption.getValue() == null 
                        		? 0.0 : currentBatteryConsumption.getValue());
        
        // Update consumption
        if(consumption != this.currentConsumption.getValue()) {
        	currentConsumption.setNewValue(consumption, t);
        	return true;
        }
        
        return false;
    }
	
	public boolean updateProduction(Time t) {

        double production =
                        (this.currentWindTurbineProduction == null || this.currentWindTurbineProduction.getValue() == null
                        		? 0.0 : currentWindTurbineProduction.getValue()) +
                        (this.currentBatteryProduction == null || this.currentBatteryProduction.getValue() == null
                        		? 0.0 : currentBatteryProduction.getValue()) +
                        (this.currentGeneratorProduction == null || this.currentGeneratorProduction.getValue() == null
                		? 0.0 : currentGeneratorProduction.getValue());
        
        if(production != this.currentProduction.getValue()) {
        	currentProduction.setNewValue(production, t);
        	return true;
        }
        
        return false;
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
        
        Time t = this.getCurrentStateTime();
        if(updateConsumption(t) || this.updateProduction(t)) {
        	logMessage("Current global consumption : " + currentConsumption.getValue() + " watts. \n");
            logMessage("Current global production : " + currentProduction.getValue() + " watts. \n");
        }
    }

    @Override
    public void endSimulation(Time endTime) {
        logMessage("Simulation ends!\n");
        
        super.endSimulation(endTime);
    }
}
