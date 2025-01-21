package fr.sorbonne_u.components.equipments.battery.mil;

import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import fr.sorbonne_u.components.cyphy.plugins.devs.AtomicSimulatorPlugin;
import fr.sorbonne_u.components.equipments.battery.mil.events.SetConsumeBatteryEvent;
import fr.sorbonne_u.components.equipments.battery.mil.events.SetProductBatteryEvent;
import fr.sorbonne_u.components.equipments.battery.mil.events.SetStandByBatteryEvent;
import fr.sorbonne_u.devs_simulation.es.models.AtomicES_Model;
import fr.sorbonne_u.devs_simulation.exceptions.MissingRunParameterException;
import fr.sorbonne_u.devs_simulation.models.annotations.ModelExternalEvents;
import fr.sorbonne_u.devs_simulation.models.events.EventI;
import fr.sorbonne_u.devs_simulation.models.time.Time;
import fr.sorbonne_u.devs_simulation.simulators.interfaces.AtomicSimulatorI;
import fr.sorbonne_u.devs_simulation.utils.StandardLogger;
import fr.sorbonne_u.devs_simulation.models.time.Duration;

@ModelExternalEvents(exported = {
        SetProductBatteryEvent.class,
        SetConsumeBatteryEvent.class,
        SetStandByBatteryEvent.class
})
public class BatteryUserModel extends AtomicES_Model {

	// -------------------------------------------------------------------------
	// Constants and variables
	// -------------------------------------------------------------------------
	
	private static final long serialVersionUID = 1L;
	
	public static final String MIL_URI = BatteryUserModel.class.getSimpleName() + "-MIL";
	public static final String MIL_RT_URI = BatteryUserModel.class.getSimpleName() + "-MIL-RT";
	public static final String SIL_URI = BatteryUserModel.class.getSimpleName() + "-SIL";
	
    protected static double STEP_MEAN_DURATION = 5.0;
    
    protected int step = 1;
    
    
    // -------------------------------------------------------------------------
  	// Constructors
  	// -------------------------------------------------------------------------
    
    public BatteryUserModel(String uri, TimeUnit simulatedTimeUnit, AtomicSimulatorI simulationEngine) throws Exception {
		super(uri, simulatedTimeUnit, simulationEngine);
		this.getSimulationEngine().setLogger(new StandardLogger());
	}
    
    
    // -------------------------------------------------------------------------
   	// Methods
   	// -------------------------------------------------------------------------
    
    @Override
    public void initialiseState(Time initialTime) {
        super.initialiseState(initialTime);

        this.getSimulationEngine().toggleDebugMode();
        logMessage("Simulation starts...\n");
    }
    
    @Override
    public Duration timeAdvance() {
    	return new Duration(STEP_MEAN_DURATION, this.getSimulatedTimeUnit());
    }
    
    @Override
    public ArrayList<EventI> output() {
    	EventI nextEvent; 
    	if (step % 3 == 1) 
            nextEvent = new SetConsumeBatteryEvent(this.getTimeOfNextEvent());
        else if (step % 3 == 2) 
            nextEvent = new SetProductBatteryEvent(this.getTimeOfNextEvent());
        else  
            nextEvent = new SetStandByBatteryEvent(this.getTimeOfNextEvent());
        
    	ArrayList<EventI> ret = new ArrayList<EventI>();
		ret.add(nextEvent);
		this.logMessage("emitting " + nextEvent + ".");
		
		return ret;	
    }
    
    @Override
	public void userDefinedInternalTransition(Duration elapsedTime) {
		super.userDefinedInternalTransition(elapsedTime);

		this.step++;
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
