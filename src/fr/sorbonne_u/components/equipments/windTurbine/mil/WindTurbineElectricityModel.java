package fr.sorbonne_u.components.equipments.windTurbine.mil;

import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import fr.sorbonne_u.components.cyphy.plugins.devs.AtomicSimulatorPlugin;
import fr.sorbonne_u.components.equipments.hem.mil.HEM_ReportI;
import fr.sorbonne_u.components.equipments.windTurbine.WindTurbineI.WindTurbineState;
import fr.sorbonne_u.components.equipments.windTurbine.mil.events.AbstractWindTurbineEvent;
import fr.sorbonne_u.components.equipments.windTurbine.mil.events.StartWindTurbineEvent;
import fr.sorbonne_u.components.equipments.windTurbine.mil.events.StopWindTurbineEvent;
import fr.sorbonne_u.devs_simulation.exceptions.MissingRunParameterException;
import fr.sorbonne_u.devs_simulation.exceptions.NeoSim4JavaException;
import fr.sorbonne_u.devs_simulation.hioa.annotations.ExportedVariable;
import fr.sorbonne_u.devs_simulation.hioa.annotations.ImportedVariable;
import fr.sorbonne_u.devs_simulation.hioa.annotations.ModelExportedVariable;
import fr.sorbonne_u.devs_simulation.hioa.annotations.ModelImportedVariable;
import fr.sorbonne_u.devs_simulation.hioa.models.AtomicHIOA;
import fr.sorbonne_u.devs_simulation.hioa.models.vars.Value;
import fr.sorbonne_u.devs_simulation.models.annotations.ModelExternalEvents;
import fr.sorbonne_u.devs_simulation.models.events.Event;
import fr.sorbonne_u.devs_simulation.models.events.EventI;
import fr.sorbonne_u.devs_simulation.models.time.Duration;
import fr.sorbonne_u.devs_simulation.models.time.Time;
import fr.sorbonne_u.devs_simulation.simulators.interfaces.AtomicSimulatorI;
import fr.sorbonne_u.devs_simulation.simulators.interfaces.SimulationReportI;
import fr.sorbonne_u.devs_simulation.utils.StandardLogger;
import fr.sorbonne_u.exceptions.InvariantChecking;

@ModelExternalEvents(imported = {
        StartWindTurbineEvent.class,
        StopWindTurbineEvent.class
})
@ModelImportedVariable(name = "externalWindSpeed", type = Double.class)
@ModelExportedVariable(name = "currentProduction", type = Double.class)
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
    protected final Duration evaluationStep;

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
		
		this.evaluationStep = new Duration(ExternalWindModel.STEP, this.getSimulatedTimeUnit());
		
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
    
    @Override
    public WindTurbineState getState() {
    	return this.currentState;
    }
    
    @Override
	public void activate() {
		this.currentState = WindTurbineState.ACTIVE;
	}


	@Override
	public void stop() {
		this.currentState = WindTurbineState.STANDBY;
	}
    
	@Override
	public Duration timeAdvance() {
		return this.evaluationStep;
	}
	
	 @Override
    public void initialiseState(Time initialTime) {
        super.initialiseState(initialTime);
        
        if (!this.currentProduction.isInitialised()) {
			this.currentProduction.initialise(0.0);
			
			StringBuffer sbp = new StringBuffer("new production: ");
			sbp.append(this.currentProduction.getValue());
			sbp.append(" amperes at ");
			sbp.append(this.currentProduction.getTime());
			sbp.append(" seconds.\n");
			
			this.logMessage(sbp.toString());
		} 
				
        this.getSimulationEngine().toggleDebugMode();
        logMessage("Simulation starts...\n");
        
        assert	glassBoxInvariants(this) :
			new NeoSim4JavaException(
					"WindTurbineElectricityModel.glassBoxInvariants(this)");
        assert	blackBoxInvariants(this) :
			new NeoSim4JavaException(
					"WindTurbineElectricityModel.blackBoxInvariants(this)");
	 }
	
	@Override
    public ArrayList<EventI> output() {
        return null;
    }

	@Override
    public void userDefinedInternalTransition(Duration elapsedTime) {
        super.userDefinedInternalTransition(elapsedTime);

        if(this.getState() == WindTurbineState.ACTIVE) {
        	if (externalWindSpeed != null && externalWindSpeed.getValue() != null) {
        		currentProduction.setNewValue(externalWindSpeed.getValue() * 5000 / 150.0, 
						this.getCurrentStateTime());
        	}
        	else 
        		currentProduction.setNewValue(0.0, this.getCurrentStateTime());
            
            this.totalProduction += currentProduction.getValue() * elapsedTime.getSimulatedDuration();
        } 
        else 
        	currentProduction.setNewValue(0.0, this.getCurrentStateTime());
        
//        try {
//        	System.out.println("production -> " + this.currentProduction.getValue());
//			Thread.sleep(5000);
//		} catch (InterruptedException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
        System.out.println("state -> " + this.currentState);
        System.out.println("Current production " + currentProduction.getValue() + " at " + currentProduction.getTime() + "\n");
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
        
        assert	glassBoxInvariants(this) :
			new NeoSim4JavaException(
					"WindTurbineElectricityModel.glassBoxInvariants(this)");
        assert	blackBoxInvariants(this) :
			new NeoSim4JavaException(
					"WindTurbineElectricityModel.blackBoxInvariants(this)");
    }
	 
	@Override
    public void endSimulation(Time endTime) {
		WindTurbineReport report = (WindTurbineReport)this.getFinalReport();
		logMessage(report.printout(""));
		 
		logMessage("simulations ends!\n");
		super.endSimulation(endTime);
    }
	
	
	// -------------------------------------------------------------------------
	// Optional DEVS simulation protocol: simulation run parameters
	// -------------------------------------------------------------------------

	@Override
	public void setSimulationRunParameters(Map<String, Object> simParams) throws MissingRunParameterException {
		super.setSimulationRunParameters(simParams);

		if (simParams.containsKey(
						AtomicSimulatorPlugin.OWNER_RUNTIME_PARAMETER_NAME)) 
		{
			this.getSimulationEngine().setLogger(
					AtomicSimulatorPlugin.createComponentLogger(simParams));
		}
	}
	
	
	// -------------------------------------------------------------------------
	// Optional DEVS simulation protocol: simulation report
	// -------------------------------------------------------------------------

	public static class	WindTurbineReport implements SimulationReportI, HEM_ReportI {
		private static final long serialVersionUID = 1L;
		protected String modelURI;
		protected double totalProduction;


		public WindTurbineReport(String modelURI, double totalProduction) {
			super();
			this.modelURI = modelURI;
			this.totalProduction = totalProduction;
		}

		@Override
		public String getModelURI() {
			return this.modelURI;
		}

		@Override
		public String printout(String indent) {
			StringBuffer ret = new StringBuffer(indent);
			ret.append("---\n");
			ret.append(indent);
			ret.append('|');
			ret.append(this.modelURI);
			ret.append(" report\n");
			ret.append(indent);
			ret.append('|');
			ret.append("total production in kwh = ");
			ret.append(this.totalProduction);
			ret.append(".\n");
			ret.append(indent);
			ret.append("---\n");
			return ret.toString();
		}		
	}

	// -------------------------------------------------------------------------
	// Optional DEVS simulation protocol: simulation report
	// -------------------------------------------------------------------------

	@Override
	public SimulationReportI getFinalReport() {
		return new WindTurbineReport(this.getURI(), this.totalProduction);
	}
}
