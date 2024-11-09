package fr.sorbonne_u.components.equipments.battery.mil;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;
import org.apache.commons.math3.random.RandomDataGenerator;
import fr.sorbonne_u.components.equipments.battery.mil.events.SetConsumeBatteryEvent;
import fr.sorbonne_u.components.equipments.battery.mil.events.SetProductBatteryEvent;
import fr.sorbonne_u.devs_simulation.es.events.ES_EventI;
import fr.sorbonne_u.devs_simulation.es.models.AtomicES_Model;
import fr.sorbonne_u.devs_simulation.models.annotations.ModelExternalEvents;
import fr.sorbonne_u.devs_simulation.models.events.EventI;
import fr.sorbonne_u.devs_simulation.models.time.Time;
import fr.sorbonne_u.devs_simulation.simulators.interfaces.AtomicSimulatorI;
import fr.sorbonne_u.devs_simulation.utils.StandardLogger;
import fr.sorbonne_u.devs_simulation.models.time.Duration;

@ModelExternalEvents(exported = {
        SetProductBatteryEvent.class,
        SetConsumeBatteryEvent.class
})
public class BatteryUserModel extends AtomicES_Model {

	// -------------------------------------------------------------------------
	// Constants and variables
	// -------------------------------------------------------------------------
	
	private static final long serialVersionUID = 1L;
	public static final String URI = BatteryUserModel.class.getSimpleName();
	
    protected static double STEP_MEAN_DURATION = 10.0;
    protected RandomDataGenerator generator;
    
    
    // -------------------------------------------------------------------------
  	// Constructors
  	// -------------------------------------------------------------------------
    
    public BatteryUserModel(String uri, TimeUnit simulatedTimeUnit, AtomicSimulatorI simulationEngine) throws Exception {
		super(uri, simulatedTimeUnit, simulationEngine);
		this.generator = new RandomDataGenerator();
		this.getSimulationEngine().setLogger(new StandardLogger());
	}
    
    
    // -------------------------------------------------------------------------
   	// Methods
   	// -------------------------------------------------------------------------
    
    protected Time computeTimeOfNextEvent(Time from) {
        double delay = Math.max(generator.nextGaussian(STEP_MEAN_DURATION, STEP_MEAN_DURATION/2.0), 0.1);
        return from.add(new Duration(delay, this.getSimulatedTimeUnit()));
    }
    
    protected void generateNextEvent() {
        EventI current = eventList.peek();
        assert current != null;
        Time nextTime = computeTimeOfNextEvent(current.getTimeOfOccurrence());

        ES_EventI next = null;
        if(current instanceof SetProductBatteryEvent) {
            next = new SetProductBatteryEvent(nextTime);
        }
        else if(current instanceof SetConsumeBatteryEvent) {
            next = new SetConsumeBatteryEvent(nextTime);
        }

        scheduleEvent(next);
    }

    @Override
    public void initialiseState(Time initialTime) {
        super.initialiseState(initialTime);

        generator.reSeedSecure();

        Time nextTime = this.computeTimeOfNextEvent(getCurrentStateTime());
        scheduleEvent(new SetProductBatteryEvent(nextTime));

        nextTimeAdvance = timeAdvance();
        timeOfNextEvent = getCurrentStateTime().add(getNextTimeAdvance());

        this.getSimulationEngine().toggleDebugMode();
        logMessage("Simulation starts...\n");
    }
    
    @Override
    public ArrayList<EventI> output() {
        if(eventList.peek() != null) generateNextEvent();

        return super.output();
    }

    @Override
    public void endSimulation(Time endTime) {
        logMessage("Simulation ends!\n");
        super.endSimulation(endTime);
    }
}
