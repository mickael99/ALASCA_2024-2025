package fr.sorbonne_u.components.equipments.windTurbine.mil;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import fr.sorbonne_u.components.equipments.windTurbine.mil.events.SetWindSpeedEvent;
import fr.sorbonne_u.devs_simulation.hioa.annotations.ExportedVariable;
import fr.sorbonne_u.devs_simulation.hioa.annotations.ModelExportedVariable;
import fr.sorbonne_u.devs_simulation.hioa.models.AtomicHIOA;
import fr.sorbonne_u.devs_simulation.hioa.models.vars.Value;
import fr.sorbonne_u.devs_simulation.models.annotations.ModelExternalEvents;
import fr.sorbonne_u.devs_simulation.models.events.EventI;
import fr.sorbonne_u.devs_simulation.models.time.Duration;
import fr.sorbonne_u.devs_simulation.models.time.Time;
import fr.sorbonne_u.devs_simulation.simulators.interfaces.AtomicSimulatorI;
import fr.sorbonne_u.devs_simulation.utils.Pair;
import fr.sorbonne_u.devs_simulation.utils.StandardLogger;

@ModelExternalEvents(exported = {
        SetWindSpeedEvent.class
})
@ModelExportedVariable(name = "externalWindSpeed", type = Double.class)
public class ExternalWindModel extends AtomicHIOA {
	
	// -------------------------------------------------------------------------
	// Constants and variables
	// -------------------------------------------------------------------------

	private static final long serialVersionUID = 1L;

	public static final String URI = ExternalWindModel.class.getSimpleName();

    public static final double MIN_EXTERNAL_WIND_SPEED = 0.0;
    public static final double MAX_EXTERNAL_WIND_SPEED = 150.0;

    public static final double PERIOD = 0.1;
    protected static final double STEP = .01;

    protected final Duration evaluationStep;

    @ExportedVariable(type = Double.class)
    protected final Value<Double> externalWindSpeed = new Value<Double>(this);
    protected double cycleTime;

    protected boolean windSpeedHasChanged;
    
    
    // -------------------------------------------------------------------------
 	// Constructors
 	// -------------------------------------------------------------------------
    
    public ExternalWindModel(String uri, TimeUnit simulatedTimeUnit, AtomicSimulatorI simulationEngine) throws Exception {
		super(uri, simulatedTimeUnit, simulationEngine);
		this.evaluationStep = new Duration(STEP, this.getSimulatedTimeUnit());
		this.getSimulationEngine().setLogger(new StandardLogger());
	}
    
    
    // -------------------------------------------------------------------------
  	// Methods
  	// -------------------------------------------------------------------------
    
    @Override
    public void initialiseState(Time initialTime) {
        super.initialiseState(initialTime);
        
        this.cycleTime = 0.0;
        this.windSpeedHasChanged = false;
    }
    
    @Override
	public Pair<Integer, Integer> fixpointInitialiseVariables() {
		if (!this.externalWindSpeed.isInitialised()) {
			this.externalWindSpeed.initialise(MIN_EXTERNAL_WIND_SPEED);

			this.getSimulationEngine().toggleDebugMode();
			this.logMessage("simulation begins.\n");
			StringBuffer message =
					new StringBuffer("current external wind speed: ");
			message.append(this.externalWindSpeed.getValue());
			message.append(" at ");
			message.append(this.getCurrentStateTime());
			message.append("\n");
			this.logMessage(message.toString());

			return new Pair<>(1, 0);
		}
	
		return new Pair<>(0, 0);
	}
    
    @Override
	public void initialiseVariables() {
        super.initialiseVariables();
    }
    
    @Override
    public ArrayList<EventI> output() {
        ArrayList<EventI> events = null;
        if(windSpeedHasChanged) {
            events = new ArrayList<>();
            events.add(new SetWindSpeedEvent(this.externalWindSpeed.getTime()));
            
            windSpeedHasChanged = false;
        }
        
        return events;
    }
    
	@Override
	public Duration timeAdvance() {
		return this.evaluationStep;
	}

	@Override
    public void userDefinedInternalTransition(Duration elapsedTime) {
        super.userDefinedInternalTransition(elapsedTime);

        // Compute the current time in the cycle
        this.cycleTime += elapsedTime.getSimulatedDuration();
        if (this.cycleTime > PERIOD) {
            this.cycleTime -= PERIOD;
        }

        // Compute the new wind speed
        double c = Math.cos((1.0 + this.cycleTime/(PERIOD/2.0))*Math.PI);
        double windSpeed =
                MIN_EXTERNAL_WIND_SPEED
                + (MAX_EXTERNAL_WIND_SPEED - MIN_EXTERNAL_WIND_SPEED)
                * ((1.0 + c) / 2.0);

        this.externalWindSpeed.setNewValue(windSpeed, this.getCurrentStateTime());
        this.windSpeedHasChanged = true;

        // Tracing
        StringBuffer message = new StringBuffer("current external wind speed: ");
        message.append(this.externalWindSpeed.getValue());
        message.append(" at ");
        message.append(this.externalWindSpeed.getTime());
        message.append("\n");
        this.logMessage(message.toString());
    }

    @Override
    public boolean useFixpointInitialiseVariables() {
        return true;
    }
	
	@Override
    public void endSimulation(Time endTime) {
        this.logMessage("Simulation ends!\n");
        
        super.endSimulation(endTime);
    }
}
