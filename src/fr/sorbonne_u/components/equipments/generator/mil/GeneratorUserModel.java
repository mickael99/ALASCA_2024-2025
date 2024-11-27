package fr.sorbonne_u.components.equipments.generator.mil;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import org.apache.commons.math3.random.RandomDataGenerator;

import fr.sorbonne_u.components.equipments.generator.mil.events.ActivateGeneratorEvent;
import fr.sorbonne_u.components.equipments.generator.mil.events.StopGeneratorEvent;
import fr.sorbonne_u.devs_simulation.es.events.ES_EventI;
import fr.sorbonne_u.devs_simulation.es.models.AtomicES_Model;
import fr.sorbonne_u.devs_simulation.models.annotations.ModelExternalEvents;
import fr.sorbonne_u.devs_simulation.models.events.EventI;
import fr.sorbonne_u.devs_simulation.models.time.Duration;
import fr.sorbonne_u.devs_simulation.models.time.Time;
import fr.sorbonne_u.devs_simulation.simulators.interfaces.AtomicSimulatorI;
import fr.sorbonne_u.devs_simulation.utils.StandardLogger;

@ModelExternalEvents(exported = {
        ActivateGeneratorEvent.class,
        StopGeneratorEvent.class
})
public class GeneratorUserModel extends AtomicES_Model {

	private static final long serialVersionUID = 1L;
	
	public static final String URI = GeneratorUserModel.class.getSimpleName();
    protected static double STEP_MEAN_DURATION = 20.0;

    protected RandomDataGenerator generator;
    
    
    public GeneratorUserModel(String uri, TimeUnit simulatedTimeUnit, AtomicSimulatorI simulationEngine) throws Exception {
        super(uri, simulatedTimeUnit, simulationEngine);
        this.generator = new RandomDataGenerator();
		this.getSimulationEngine().setLogger(new StandardLogger());
    }
    
    
    @Override
    public void initialiseState(Time initialTime) {
        super.initialiseState(initialTime);

        generator.reSeedSecure();

        Time nextTime = computeTimeOfNextEvent(getCurrentStateTime());
        scheduleEvent(new ActivateGeneratorEvent(nextTime));

        nextTimeAdvance = timeAdvance();
        timeOfNextEvent = getCurrentStateTime().add(getNextTimeAdvance());

        this.getSimulationEngine().toggleDebugMode();;
        logMessage("Simulation starts...\n");
    }
    
    
    protected void generateNextEvent() {
        EventI current = eventList.peek();
        assert current != null;
        Time nextTime = this.computeTimeOfNextEvent(current.getTimeOfOccurrence());

        ES_EventI next = null;
        if(current instanceof ActivateGeneratorEvent) {
            next = new StopGeneratorEvent(nextTime);
        }
        else if(current instanceof StopGeneratorEvent) {
            next = new ActivateGeneratorEvent(nextTime);
        }
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
