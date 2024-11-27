package fr.sorbonne_u.components.equipments.generator.mil;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import fr.sorbonne_u.components.equipments.generator.mil.events.AbstractGeneratorEvents;
import fr.sorbonne_u.components.equipments.generator.mil.events.ActivateGeneratorEvent;
import fr.sorbonne_u.components.equipments.generator.mil.events.StopGeneratorEvent;
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
        ActivateGeneratorEvent.class,
        StopGeneratorEvent.class
})
public class GeneratorElectricityModel extends AtomicHIOA {

    private static final long serialVersionUID = 1L;
	
    public static final String URI = GeneratorElectricityModel.class.getSimpleName();
    protected static final double PRODUCTION = 100.0;
    
    protected boolean isRunning; 
    protected boolean hasChanged;
    
    @ImportedVariable(type = Double.class)
    protected Value<Double> currentFuelLevel;
    
    @ExportedVariable(type = Double.class)
    protected final Value<Double> currentProduction = new Value<Double>(this);
    
	
    public GeneratorElectricityModel(String uri, TimeUnit simulatedTimeUnit, AtomicSimulatorI simulationEngine) {
		super(uri, simulatedTimeUnit, simulationEngine);
		
		this.getSimulationEngine().setLogger(new StandardLogger());
	}
    
    public boolean isRunning() {
        return this.isRunning;
    }
    
    public boolean hasChanged() {
        return this.hasChanged;
    }
    
    public void activate() {
    	this.isRunning = true;
    }
    
    public void stop() {
    	this.isRunning = false;
    }
    
    public void setHasChanged(boolean hasChanged) {
        this.hasChanged = hasChanged;
    }
    
    @Override
	public void initialiseVariables() {
		super.initialiseVariables();

		this.currentProduction.initialise(0.0);
		this.isRunning = false;
		this.hasChanged = false;
		
		this.getSimulationEngine().toggleDebugMode();
		this.logMessage("simulation begins.\n");
	}
    
    @Override
    public ArrayList<EventI> output() {
        return null;
    }
    
    @Override
    public Duration timeAdvance() {
        if(hasChanged) {
            hasChanged = false;
            return new Duration(0.0, getSimulatedTimeUnit());
        }
        return Duration.INFINITY;
    }
    
    @Override
    public void userDefinedInternalTransition(Duration elapsedTime) {
        super.userDefinedInternalTransition(elapsedTime);

        if(isRunning) {
            if(this.currentFuelLevel.getValue() > 0.0) 
            	this.currentProduction.setNewValue(PRODUCTION, this.getCurrentStateTime());
        } 
        else 
        	this.currentProduction.setNewValue(0.0, this.getCurrentStateTime());


        this.logMessage("Current production " + currentProduction.getValue() + " at " + currentProduction.getTime() +
                " | Fuel level " + currentFuelLevel.getValue() + " l" + "\n");
    }

    @Override
    public void userDefinedExternalTransition(Duration elapsedTime) {
        ArrayList<EventI> currentEvents = this.getStoredEventAndReset();
        assert	currentEvents != null && currentEvents.size() == 1;
        Event currentEvent = (Event) currentEvents.get(0);

        assert currentEvent instanceof AbstractGeneratorEvents;
        currentEvent.executeOn(this);

        super.userDefinedExternalTransition(elapsedTime);
    }
    
    @Override
    public void endSimulation(Time endTime) {
        logMessage("Simulation ends!\n");
        super.endSimulation(endTime);
    }
}
