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
	@ImportedVariable(type = Double.class)
	protected Value<Double> currentToasterConsumption;
	 
	@ImportedVariable(type = Double.class)
	protected Value<Double> currentIronConsumption;
	 
	@ImportedVariable(type = Double.class)
	 protected Value<Double> currentFridgeConsumption;
	 
	@ImportedVariable(type = Double.class)
	protected Value<Double> currentSmartLightingConsumption;
	
	// Production devices
	@ImportedVariable(type = Double.class)
	protected Value<Double> currentWindTurbineProduction;
	
	// Battery
	@ImportedVariable(type = Double.class)
	protected Value<Double> currentBatteryConsumption;
	
	@ImportedVariable(type = Double.class)
	protected Value<Double> currentBatteryProduction;
	
	// Total values
	@InternalVariable(type = Double.class)
	protected Value<Double> currentConsumption;
	
	@InternalVariable(type = Double.class)
	protected Value<Double> currentProduction;
	
	
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

        currentConsumption.setNewValue(0.0, initialTime);
        currentProduction.setNewValue(0.0, initialTime);

        this.getSimulationEngine().toggleDebugMode();
        
        logMessage("Simulation begins. \n");
    }
	
	public Double getCurrentProduction() {
		return this.currentProduction.getValue();
	}
	
	public Double getCurrentConsumption() {
		return this.currentConsumption.getValue();
	}
	
	public void updateConsumption(Time t) {
        double consumption =
                        (currentToasterConsumption == null ? 0.0 : currentToasterConsumption.getValue())
                        + (currentIronConsumption == null ? 0.0 : currentIronConsumption.getValue())
                        + (currentFridgeConsumption == null ? 0.0 : currentFridgeConsumption.getValue())
                        + (currentBatteryConsumption == null ? 0.0 : currentBatteryConsumption.getValue())
                        + (currentSmartLightingConsumption == null ? 0.0 : currentSmartLightingConsumption.getValue());
        
        currentConsumption.setNewValue(consumption, t);
    }
	
	public void updateProduction(Time t) {

        double production =
                        (currentWindTurbineProduction == null ? 0.0 : currentWindTurbineProduction.getValue())
                        + (currentBatteryProduction == null ? 0.0 : currentBatteryProduction.getValue());
        
        currentProduction.setNewValue(production, t);
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
        
        logMessage("Current global consumption : " + currentConsumption.getValue() + " watts. \n");
        logMessage("Current global production : " + currentProduction.getValue() + " watts. \n");
    }

    @Override
    public void endSimulation(Time endTime) {
        this.updateConsumption(endTime);
        this.updateProduction(endTime);
        
        logMessage("Simulation ends!\n");
        
        super.endSimulation(endTime);
    }
}
