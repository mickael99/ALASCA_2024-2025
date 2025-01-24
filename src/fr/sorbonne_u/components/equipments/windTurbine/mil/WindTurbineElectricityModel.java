package fr.sorbonne_u.components.equipments.windTurbine.mil;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;
import fr.sorbonne_u.components.equipments.windTurbine.WindTurbineI.WindTurbineState;
import fr.sorbonne_u.components.equipments.windTurbine.mil.events.AbstractWindTurbineEvent;
import fr.sorbonne_u.components.equipments.windTurbine.mil.events.StartWindTurbineEvent;
import fr.sorbonne_u.components.equipments.windTurbine.mil.events.StopWindTurbineEvent;
import fr.sorbonne_u.devs_simulation.exceptions.NeoSim4JavaException;
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
import fr.sorbonne_u.exceptions.InvariantChecking;

@ModelExternalEvents(imported = {
        StartWindTurbineEvent.class,
        StopWindTurbineEvent.class
})
public class WindTurbineElectricityModel extends AtomicHIOA implements WindTurbineOperationI{

	// -------------------------------------------------------------------------
	// Constants and variables
	// -------------------------------------------------------------------------
	
	private static final long serialVersionUID = 1L;
	
	public static final String MIL_URI = WindTurbineElectricityModel.class.getSimpleName() + "-MIL";
	public static final String MIL_RT_URI = WindTurbineElectricityModel.class.getSimpleName() + "-MIL-RT";
	public static final String SIL_URI = WindTurbineElectricityModel.class.getSimpleName() + "-SIL";
	
    protected WindTurbineState currentState;
    protected double totalProduction = 0.0;
    protected boolean hasChanged;

    @ImportedVariable(type = Double.class)
    protected Value<Double> externalWindSpeed;

    @ExportedVariable(type = Double.class)
    protected final Value<Double> currentProduction = new Value<Double>(this);
    
    
    // -------------------------------------------------------------------------
 	// Invariants
 	// -------------------------------------------------------------------------

 	protected static boolean glassBoxInvariants(WindTurbineElectricityModel instance) {
 		assert	instance != null :
 				new NeoSim4JavaException("Precondition violation: "
 						+ "instance != null");

 		boolean ret = true;
 		ret &= InvariantChecking.checkGlassBoxInvariant(
 					instance.totalProduction >= 0.0,
 					WindTurbineElectricityModel.class,
 					instance,
 					"totalProduction >= 0.0");
 		ret &= InvariantChecking.checkGlassBoxInvariant(
 					instance.currentState != null,
 					WindTurbineElectricityModel.class,
 					instance,
 					"currentState != null");
 		return ret;
 	}

 	protected static boolean blackBoxInvariants(WindTurbineElectricityModel instance) {
 		assert	instance != null :
 				new NeoSim4JavaException("Precondition violation: "
 						+ "instance != null");

 		boolean ret = true;
 		ret &= InvariantChecking.checkBlackBoxInvariant(
 					MIL_URI != null && !MIL_URI.isEmpty(),
 					WindTurbineElectricityModel.class,
 					instance,
 					"MIL_URI != null && !MIL_URI.isEmpty()");
 		ret &= InvariantChecking.checkBlackBoxInvariant(
 					MIL_RT_URI != null && !MIL_RT_URI.isEmpty(),
 					WindTurbineElectricityModel.class,
 					instance,
 					"MIL_RT_URI != null && !MIL_RT_URI.isEmpty()");
 		ret &= InvariantChecking.checkBlackBoxInvariant(
 					SIL_URI != null && !SIL_URI.isEmpty(),
 					WindTurbineElectricityModel.class,
 					instance,
 					"SIL_URI != null && !SIL_URI.isEmpty()");
 		return ret;
 	}
    
    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------
    
    public WindTurbineElectricityModel(String uri, TimeUnit simulatedTimeUnit, AtomicSimulatorI simulationEngine) {
		super(uri, simulatedTimeUnit, simulationEngine);
		this.getSimulationEngine().setLogger(new StandardLogger());
		
		this.currentState = WindTurbineStateModel.INITIAL_CURRENT_STATE;
		
		assert	glassBoxInvariants(this) :
			new NeoSim4JavaException(
					"WindTurbineStateModel.glassBoxInvariants(this)");
		assert	blackBoxInvariants(this) :
			new NeoSim4JavaException(
					"WindTurbineStateModel.blackBoxInvariants(this)");
	}
    
  
    // -------------------------------------------------------------------------
    // Methods
    // -------------------------------------------------------------------------
    
    public WindTurbineState getState() {
    	return this.currentState;
    }
    
    public void setState(WindTurbineState s) {
    	this.currentState = s;
    }
    
    public boolean hasChanged() {
    	return this.hasChanged;
    }
    
    public void setHasChanged(boolean h) {
    	this.hasChanged = h;
    }
    
	@Override
	public Duration timeAdvance() {
		if(this.hasChanged) {
            this.hasChanged = false;
            return Duration.zero(this.getSimulatedTimeUnit());
        }
		
        return Duration.INFINITY;
	}
	
	@Override
	public void initialiseVariables() {
		super.initialiseVariables();

		this.currentProduction.initialise(0.0);
		this.currentState = WindTurbineState.STANDBY;
		this.hasChanged = false;
		
		this.getSimulationEngine().toggleDebugMode();
		this.logMessage("simulation begins.\n");
	}
	
	@Override
    public ArrayList<EventI> output() {
        return null;
    }

	@Override
    public void userDefinedInternalTransition(Duration elapsedTime) {
        super.userDefinedInternalTransition(elapsedTime);

        if(this.getState() == WindTurbineState.ACTIVE) {
            currentProduction.setNewValue(externalWindSpeed.getValue() * 5000 / 150.0, 
            								this.getCurrentStateTime());
        } 
        else 
        	currentProduction.setNewValue(0.0, this.getCurrentStateTime());

        logMessage("Current production " + currentProduction.getValue() + " at " + currentProduction.getTime() + "\n");
    }
	
	 @Override
    public void userDefinedExternalTransition(Duration elapsedTime) {
        ArrayList<EventI> currentEvents = this.getStoredEventAndReset();
        assert	currentEvents != null && currentEvents.size() == 1;
        Event currentEvent = (Event) currentEvents.get(0);

        assert currentEvent instanceof AbstractWindTurbineEvent;
        currentEvent.executeOn(this);

        super.userDefinedExternalTransition(elapsedTime);
    }
	 
	 @Override
    public void endSimulation(Time endTime) {
        logMessage("simulations ends!\n");
        super.endSimulation(endTime);
    }


	@Override
	public void activate() {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void stop() {
		// TODO Auto-generated method stub
		
	}
}
