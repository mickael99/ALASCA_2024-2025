package fr.sorbonne_u.components.equipments.windTurbine.mil;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import org.apache.commons.math3.random.RandomDataGenerator;

import fr.sorbonne_u.components.equipments.windTurbine.mil.events.StartWindTurbineEvent;
import fr.sorbonne_u.components.equipments.windTurbine.mil.events.StopWindTurbineEvent;
import fr.sorbonne_u.devs_simulation.es.events.ES_EventI;
import fr.sorbonne_u.devs_simulation.es.models.AtomicES_Model;
import fr.sorbonne_u.devs_simulation.models.annotations.ModelExternalEvents;
import fr.sorbonne_u.devs_simulation.models.events.EventI;
import fr.sorbonne_u.devs_simulation.models.time.Duration;
import fr.sorbonne_u.devs_simulation.models.time.Time;
import fr.sorbonne_u.devs_simulation.simulators.interfaces.AtomicSimulatorI;
import fr.sorbonne_u.devs_simulation.utils.StandardLogger;

@ModelExternalEvents(exported = {
        StartWindTurbineEvent.class,
        StopWindTurbineEvent.class
})
public class WindTurbineUserModel extends AtomicES_Model {

	// -------------------------------------------------------------------------
	// Attributes
	// -------------------------------------------------------------------------
	
	private static final long serialVersionUID = 1L;

	public static final String URI = WindTurbineUserModel.class.getSimpleName();

    protected static double STEP_MEAN_DURATION = 1.0;
    protected RandomDataGenerator generator;
    
    
    // -------------------------------------------------------------------------
 	// Constructors
 	// -------------------------------------------------------------------------
    
    public WindTurbineUserModel(String uri, TimeUnit simulatedTimeUnit, AtomicSimulatorI simulationEngine) throws Exception {
        super(uri, simulatedTimeUnit, simulationEngine);
        generator = new RandomDataGenerator();
		this.getSimulationEngine().setLogger(new StandardLogger());
    }
    
    
	// -------------------------------------------------------------------------
 	// Methods
 	// -------------------------------------------------------------------------
    
    @Override
    public void initialiseState(Time initialTime) {
        super.initialiseState(initialTime);
        generator.reSeedSecure();

        // The first event
        Time nextTime = this.computeTimeOfNextEvent(getCurrentStateTime());
        scheduleEvent(new StartWindTurbineEvent(nextTime));

        nextTimeAdvance = timeAdvance();
        timeOfNextEvent = getCurrentStateTime().add(getNextTimeAdvance());

        this.getSimulationEngine().toggleDebugMode();
        logMessage("Simulation starts...\n");
    }
    
    protected void generateNextEvent() {
        EventI current = eventList.peek();
        assert current != null;
        Time nextTime = this.computeTimeOfNextEvent(current.getTimeOfOccurrence());

        ES_EventI next = null;
        if(current instanceof StartWindTurbineEvent) 
            next = new StartWindTurbineEvent(nextTime);
        else if(current instanceof StopWindTurbineEvent) 
            next = new StopWindTurbineEvent(nextTime);
        
        scheduleEvent(next);
    }
    
    protected Time computeTimeOfNextEvent(Time from) {
    	double delay = Math.max(generator.nextGaussian(STEP_MEAN_DURATION, STEP_MEAN_DURATION/2.0), 0.1);
        return from.add(new Duration(delay, this.getSimulatedTimeUnit()));
	}
    
    @Override
    public ArrayList<EventI> output() {
        if(eventList.peek() != null) 
        	generateNextEvent();
        
        return super.output();
    }

    @Override
    public void endSimulation(Time endTime) {
        logMessage("Simulation ends!\n");
        super.endSimulation(endTime);
    }
}
