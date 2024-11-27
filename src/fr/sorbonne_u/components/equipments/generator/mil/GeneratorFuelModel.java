package fr.sorbonne_u.components.equipments.generator.mil;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import fr.sorbonne_u.components.equipments.generator.mil.events.AbstractGeneratorEvents;
import fr.sorbonne_u.components.equipments.generator.mil.events.ActivateGeneratorEvent;
import fr.sorbonne_u.components.equipments.generator.mil.events.StopGeneratorEvent;
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
        ActivateGeneratorEvent.class,
        StopGeneratorEvent.class
},
exported = {
		StopGeneratorEvent.class
})
public class GeneratorFuelModel extends AtomicHIOA {

	private static final long serialVersionUID = 1L;

	public static final String URI = GeneratorFuelModel.class.getSimpleName();
    protected static final double STEP = 0.1;

    /** The generator fuel consumption in l/h */
    protected static final double FUEL_CONSUMPTION = 100.10;

    protected static final double MAX_CAPACITY = 200.0;
    
    protected final Duration evaluationStep;

    private boolean isRunning;

    @ExportedVariable(type = Double.class)
    protected final Value<Double> currentFuelLevel = new Value<Double>(this);
    
    
    public GeneratorFuelModel(String uri, TimeUnit simulatedTimeUnit, AtomicSimulatorI simulationEngine) throws Exception {
		super(uri, simulatedTimeUnit, simulationEngine);
		this.evaluationStep = new Duration(STEP, this.getSimulatedTimeUnit());
		this.getSimulationEngine().setLogger(new StandardLogger());
	}
    
    @Override
    public void initialiseState(Time initialTime) {
    	System.out.println("fuel model initialise state");
    	
        super.initialiseState(initialTime);
        
        this.isRunning = false;
        
        this.currentFuelLevel.initialise(MAX_CAPACITY);
		this.getSimulationEngine().toggleDebugMode();
		this.logMessage("simulation begins.\n");
		StringBuffer message =
				new StringBuffer("current fuel level: ");
		message.append(this.currentFuelLevel.getValue());
		message.append(" at ");
		message.append(this.getCurrentStateTime());
		message.append("\n");
		this.logMessage(message.toString());
    }
    
    public boolean isRunning() {
        return this.isRunning;
    }
    
    public void activate() {
    	this.isRunning = true;
    }
    
    public void stop() {
    	this.isRunning = false;
    }
    
    public void consume(Duration d) {
        double duration = d.getSimulatedDuration();
        currentFuelLevel.setNewValue(
        		this.currentFuelLevel.getValue() - Math.max((duration / 3600) * FUEL_CONSUMPTION, 0.0), 
        		currentStateTime);
    }
    
    @Override
    public ArrayList<EventI> output() {
        if(currentFuelLevel.getValue() <= 0.0) {
            ArrayList<EventI> res = new ArrayList<>();
            res.add(new StopGeneratorEvent(currentFuelLevel.getTime()));
            return res;
        }
        return null;
    }
    
	@Override
	public Duration timeAdvance() {
		return evaluationStep;
	}

	@Override
    public void userDefinedInternalTransition(Duration elapsedTime) {
        super.userDefinedInternalTransition(elapsedTime);

        if(isRunning) {
            if(currentFuelLevel.getValue() > 0.0) {
                consume(elapsedTime);
            }

            logMessage("Generator is " + (isRunning ? "on" : "off") + " | Fuel level : " + currentFuelLevel.getValue() + " at " + currentFuelLevel.getTime() + "\n");
        }
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
