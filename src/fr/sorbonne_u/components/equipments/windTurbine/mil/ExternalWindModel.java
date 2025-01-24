package fr.sorbonne_u.components.equipments.windTurbine.mil;

import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import fr.sorbonne_u.components.cyphy.plugins.devs.AtomicSimulatorPlugin;
import fr.sorbonne_u.devs_simulation.exceptions.MissingRunParameterException;
import fr.sorbonne_u.devs_simulation.exceptions.NeoSim4JavaException;
import fr.sorbonne_u.devs_simulation.hioa.annotations.ExportedVariable;
import fr.sorbonne_u.devs_simulation.hioa.annotations.ModelExportedVariable;
import fr.sorbonne_u.devs_simulation.hioa.models.AtomicHIOA;
import fr.sorbonne_u.devs_simulation.hioa.models.vars.Value;
import fr.sorbonne_u.devs_simulation.models.events.EventI;
import fr.sorbonne_u.devs_simulation.models.interfaces.ModelI;
import fr.sorbonne_u.devs_simulation.models.time.Duration;
import fr.sorbonne_u.devs_simulation.models.time.Time;
import fr.sorbonne_u.devs_simulation.simulators.interfaces.AtomicSimulatorI;
import fr.sorbonne_u.devs_simulation.utils.StandardLogger;
import fr.sorbonne_u.exceptions.InvariantChecking;

@ModelExportedVariable(name = "externalWindSpeed", type = Double.class)
public class ExternalWindModel extends AtomicHIOA {
	
	// -------------------------------------------------------------------------
	// Constants and variables
	// -------------------------------------------------------------------------

	private static final long serialVersionUID = 1L;

	public static final String MIL_URI = ExternalWindModel.class.getSimpleName() + "-MIL";
	public static final String MIL_RT_URI = ExternalWindModel.class.getSimpleName() + "-MIL-RT";
	public static final String SIL_URI = ExternalWindModel.class.getSimpleName() + "-SIL";

	// In m/s
    public static double MIN_EXTERNAL_WIND_SPEED = 0.0;
    public static double MAX_EXTERNAL_WIND_SPEED = 25.0;
    protected static final double INITIAL_WIND_SPEED = 10.0;

    protected static final double PERIOD = 24.0;
    public static final double STEP = 1.0;

    protected final Duration evaluationStep;

    @ExportedVariable(type = Double.class)
    protected final Value<Double> externalWindSpeed = new Value<Double>(this);
    protected double cycleTime;    
    
    // -------------------------------------------------------------------------
	// Invariants
	// -------------------------------------------------------------------------

	protected static boolean glassBoxInvariants(ExternalWindModel instance) {
		assert	instance != null :
				new NeoSim4JavaException("Precondition violation: "
						+ "instance != null");

		boolean ret = true;
		ret &= InvariantChecking.checkGlassBoxInvariant(
					STEP >= 0.0,
					ExternalWindModel.class,
					instance,
					"STEP > 0.0");
		ret &= InvariantChecking.checkGlassBoxInvariant(
					PERIOD > 0.0,
					ExternalWindModel.class,
					instance,
					"PERIOD > 0.0");
		return ret;
	}

	protected static boolean blackBoxInvariants(ExternalWindModel instance) {
		assert	instance != null :
				new NeoSim4JavaException("Precondition violation: "
						+ "instance != null");

		boolean ret = true;
		ret &= InvariantChecking.checkBlackBoxInvariant(
					MIL_URI != null && !MIL_URI.isEmpty(),
					ExternalWindModel.class,
					instance,
					"MIL_URI != null && !MIL_URI.isEmpty()");
		ret &= InvariantChecking.checkBlackBoxInvariant(
					MIL_RT_URI != null && !MIL_RT_URI.isEmpty(),
					ExternalWindModel.class,
					instance,
					"MIL_RT_URI != null && !MIL_RT_URI.isEmpty()");
		ret &= InvariantChecking.checkBlackBoxInvariant(
					SIL_URI != null && !SIL_URI.isEmpty(),
					ExternalWindModel.class,
					instance,
					"SIL_URI != null && !SIL_URI.isEmpty()");
		return ret;
	}
	
	
    // -------------------------------------------------------------------------
 	// Constructors
 	// -------------------------------------------------------------------------
    
    public ExternalWindModel(String uri, TimeUnit simulatedTimeUnit, AtomicSimulatorI simulationEngine) throws Exception {
		super(uri, simulatedTimeUnit, simulationEngine);
		
		this.evaluationStep = new Duration(STEP, this.getSimulatedTimeUnit());
		this.cycleTime = 0.0;
	        
		this.getSimulationEngine().setLogger(new StandardLogger());
		
		assert	glassBoxInvariants(this) :
			new NeoSim4JavaException(
					"ExternalWindModel.glassBoxInvariants(this)");
        assert	blackBoxInvariants(this) :
			new NeoSim4JavaException(
					"ExternalWindModel.blackBoxInvariants(this)");
	}
    
    
    // -------------------------------------------------------------------------
  	// Methods
  	// -------------------------------------------------------------------------
    
    @Override
    public void initialiseState(Time initialTime) {
        super.initialiseState(initialTime);

        this.externalWindSpeed.initialise(INITIAL_WIND_SPEED);
        
        this.getSimulationEngine().toggleDebugMode();
        logMessage("Simulations starts...\n");
        
        assert	glassBoxInvariants(this) :
			new NeoSim4JavaException(
					"ExternalWindModel.glassBoxInvariants(this)");
        assert	blackBoxInvariants(this) :
			new NeoSim4JavaException(
					"ExternalWindModel.blackBoxInvariants(this)");
    }
    
    @Override
	public void initialiseVariables() {
        super.initialiseVariables();
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

        // Tracing
        StringBuffer message = new StringBuffer("current external wind speed: ");
        message.append(this.externalWindSpeed.getValue());
        message.append(" at ");
        message.append(this.externalWindSpeed.getTime());
        message.append("\n");
        this.logMessage(message.toString());
    }
	
	@Override
    public void endSimulation(Time endTime) {
        this.logMessage("Simulation ends!\n");
        
        super.endSimulation(endTime);
    }
	
	
	public static final String MIN_EXTERNAL_WIND_SPEED_RUNPNAME = "MIN_EXTERNAL_WIND_SPEED";
	public static final String MAX_EXTERNAL_WIND_SPEED_RUNPNAME = "MAX_EXTERNAL_WIND_SPEED_RUNPNAME";
	
	@Override
	public void setSimulationRunParameters(Map<String, Object> simParams) throws MissingRunParameterException {
		super.setSimulationRunParameters(simParams);

		if (simParams.containsKey(
						AtomicSimulatorPlugin.OWNER_RUNTIME_PARAMETER_NAME)) 
		{
			this.getSimulationEngine().setLogger(
						AtomicSimulatorPlugin.createComponentLogger(simParams));
		}

		String minSpeed = ModelI.createRunParameterName(getURI(), MIN_EXTERNAL_WIND_SPEED_RUNPNAME);
		if (simParams.containsKey(minSpeed)) 
			MIN_EXTERNAL_WIND_SPEED = (double)simParams.get(minSpeed);
		
		String maxSpeed = ModelI.createRunParameterName(getURI(), MAX_EXTERNAL_WIND_SPEED_RUNPNAME);
		if (simParams.containsKey(maxSpeed)) 
			MAX_EXTERNAL_WIND_SPEED = (double) simParams.get(maxSpeed);
	}
}
