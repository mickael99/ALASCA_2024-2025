package fr.sorbonne_u.components.equipments.windTurbine.mil;

import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.commons.math3.random.RandomDataGenerator;

import fr.sorbonne_u.components.cyphy.plugins.devs.AtomicSimulatorPlugin;
import fr.sorbonne_u.components.equipments.windTurbine.mil.events.StartWindTurbineEvent;
import fr.sorbonne_u.components.equipments.windTurbine.mil.events.StopWindTurbineEvent;
import fr.sorbonne_u.devs_simulation.es.events.ES_EventI;
import fr.sorbonne_u.devs_simulation.es.models.AtomicES_Model;
import fr.sorbonne_u.devs_simulation.exceptions.MissingRunParameterException;
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

	public static final String MIL_URI = WindTurbineUserModel.class.getSimpleName() + "-MIL";
	public static final String MIL_RT_URI = WindTurbineUserModel.class.getSimpleName() + "-MIL-RT";
	public static final String SIL = WindTurbineUserModel.class.getSimpleName() + "-SIL";

    protected static double STEP_MEAN_DURATION = 10.0;
    
    
    // -------------------------------------------------------------------------
 	// Constructors
 	// -------------------------------------------------------------------------
    
    public WindTurbineUserModel(String uri, TimeUnit simulatedTimeUnit, AtomicSimulatorI simulationEngine) throws Exception {
        super(uri, simulatedTimeUnit, simulationEngine);
		this.getSimulationEngine().setLogger(new StandardLogger());
    }
    
    
	// -------------------------------------------------------------------------
 	// Methods
 	// -------------------------------------------------------------------------
    
    @Override
    public void initialiseState(Time initialTime) {
        super.initialiseState(initialTime);

        // The first event
        scheduleEvent(new StartWindTurbineEvent(initialTime));

        nextTimeAdvance = timeAdvance();
        timeOfNextEvent = getCurrentStateTime().add(getNextTimeAdvance());

        this.getSimulationEngine().toggleDebugMode();
        logMessage("Simulation starts...\n");
    }
    
    @Override
    public ArrayList<EventI> output() {        
        return super.output();
    }

    @Override
    public void endSimulation(Time endTime) {
        logMessage("Simulation ends!\n");
        super.endSimulation(endTime);
    }
    
    @Override
	public void setSimulationRunParameters(Map<String, Object> simParams) throws MissingRunParameterException {
		if (simParams.containsKey(
						AtomicSimulatorPlugin.OWNER_RUNTIME_PARAMETER_NAME)) 
		{
			this.getSimulationEngine().setLogger(AtomicSimulatorPlugin.createComponentLogger(simParams));
		}
	}
}
