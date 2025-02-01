package fr.sorbonne_u.components.equipments.battery.mil;

import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import fr.sorbonne_u.components.cyphy.plugins.devs.AtomicSimulatorPlugin;
import fr.sorbonne_u.components.equipments.battery.BatteryI.BATTERY_STATE;
import fr.sorbonne_u.components.equipments.battery.mil.events.SetProductBatteryEvent;
import fr.sorbonne_u.components.equipments.battery.mil.events.SetStandByBatteryEvent;
import fr.sorbonne_u.components.equipments.hem.mil.HEM_ReportI;
import fr.sorbonne_u.components.equipments.battery.mil.events.AbstractBatteryEvent;
import fr.sorbonne_u.components.equipments.battery.mil.events.SetConsumeBatteryEvent;
import fr.sorbonne_u.devs_simulation.exceptions.MissingRunParameterException;
import fr.sorbonne_u.devs_simulation.exceptions.NeoSim4JavaException;
import fr.sorbonne_u.devs_simulation.hioa.annotations.ExportedVariable;
import fr.sorbonne_u.devs_simulation.hioa.annotations.ModelExportedVariable;
import fr.sorbonne_u.devs_simulation.hioa.models.AtomicHIOA;
import fr.sorbonne_u.devs_simulation.hioa.models.vars.Value;
import fr.sorbonne_u.devs_simulation.models.annotations.ModelExternalEvents;
import fr.sorbonne_u.devs_simulation.models.events.Event;
import fr.sorbonne_u.devs_simulation.models.events.EventI;
import fr.sorbonne_u.devs_simulation.models.interfaces.ModelI;
import fr.sorbonne_u.devs_simulation.models.time.Duration;
import fr.sorbonne_u.devs_simulation.models.time.Time;
import fr.sorbonne_u.devs_simulation.simulators.interfaces.AtomicSimulatorI;
import fr.sorbonne_u.devs_simulation.simulators.interfaces.SimulationReportI;
import fr.sorbonne_u.devs_simulation.utils.StandardLogger;
import fr.sorbonne_u.exceptions.InvariantChecking;

@ModelExternalEvents(imported = {
        SetProductBatteryEvent.class,
        SetConsumeBatteryEvent.class,
        SetStandByBatteryEvent.class
})
@ModelExportedVariable(name = "currentProduction", type = Double.class)
@ModelExportedVariable(name = "currentConsumption", type = Double.class)
public class BatteryElectricityModel extends AtomicHIOA implements BatteryOperationI {

	// -------------------------------------------------------------------------
	// Constants and variables
	// -------------------------------------------------------------------------
	
	private static final long serialVersionUID = 1L;
	
	public static final String MIL_URI = BatteryElectricityModel.class.getSimpleName() + "-MIL";
	public static final String MIL_RT_URI = BatteryElectricityModel.class.getSimpleName() + "-MIL-RT";
	public static final String SIL_URI = BatteryElectricityModel.class.getSimpleName() + "-SIL";
	
	protected static double PRODUCTION = 1200.0;
	protected static double CONSUMPTION = 1000.0;
	protected final static double TENSION = 220.0;
	
	protected BATTERY_STATE currentState;
	protected double totalConsumption = 0.0;
	protected double totalProduction = 0.0;
	protected boolean hasChanged;
	protected Time lastInternalTransitionTime;
	
	@ExportedVariable(type = Double.class)
	protected final Value<Double> currentProduction = new Value<Double>(this);

    @ExportedVariable(type = Double.class)
    protected final Value<Double> currentConsumption = new Value<Double>(this);
	
    
    // -------------------------------------------------------------------------
 	// Constructors
 	// -------------------------------------------------------------------------
    
    public BatteryElectricityModel(String uri, TimeUnit simulatedTimeUnit, AtomicSimulatorI simulationEngine) throws Exception {
		super(uri, simulatedTimeUnit, simulationEngine);
		this.getSimulationEngine().setLogger(new StandardLogger());
		
		this.currentState = BATTERY_STATE.STANDBY;
		
		assert	glassBoxInvariants(this) :
			new NeoSim4JavaException(
					"BatteryElectricityModel.glassBoxInvariants(this)");
		assert	blackBoxInvariants(this) :
			new NeoSim4JavaException(
					"BatteryElectricityModel.blackBoxInvariants(this)");
	}
    
    
    // -------------------------------------------------------------------------
 	// Invariants
 	// -------------------------------------------------------------------------

 	protected static boolean glassBoxInvariants(BatteryElectricityModel instance) {
 		assert	instance != null :
 				new NeoSim4JavaException("Precondition violation: "
 						+ "instance != null");

 		boolean ret = true;
 		ret &= InvariantChecking.checkGlassBoxInvariant(
 					PRODUCTION >= 0.0,
 					BatteryElectricityModel.class,
 					instance,
 					"PRODUCTION >= 0.0");
 		ret &= InvariantChecking.checkGlassBoxInvariant(
 					CONSUMPTION > 0.0,
 					BatteryElectricityModel.class,
 					instance,
 					"CONSUMPTION > 0.0");
 		ret &= InvariantChecking.checkGlassBoxInvariant(
 					instance.currentState != null,
 				    BatteryElectricityModel.class,
 					instance,
 					"currentState != null");
 		return ret;
 	}

 	protected static boolean blackBoxInvariants(BatteryElectricityModel instance) {
 		assert	instance != null :
 				new NeoSim4JavaException("Precondition violation: "
 						+ "instance != null");

 		boolean ret = true;
 		ret &= InvariantChecking.checkBlackBoxInvariant(
 					MIL_URI != null && !MIL_URI.isEmpty(),
 					BatteryElectricityModel.class,
 					instance,
 					"MIL_URI != null && !MIL_URI.isEmpty()");
 		ret &= InvariantChecking.checkBlackBoxInvariant(
 					MIL_RT_URI != null && !MIL_RT_URI.isEmpty(),
 					BatteryElectricityModel.class,
 					instance,
 					"MIL_RT_URI != null && !MIL_RT_URI.isEmpty()");
 		ret &= InvariantChecking.checkBlackBoxInvariant(
 					SIL_URI != null && !SIL_URI.isEmpty(),
 					BatteryElectricityModel.class,
 					instance,
 					"SIL_URI != null && !SIL_URI.isEmpty()");
 		return ret;
 	}
    
    // -------------------------------------------------------------------------
 	// Methods
 	// -------------------------------------------------------------------------
    
    @Override
    public BATTERY_STATE getCurrentState() {
    	return this.currentState;
    }
    
    @Override
    public void setProduction() {
    	this.currentState = BATTERY_STATE.PRODUCT;
    	
    	this.setHasChanged(true);
    }
    
    @Override
    public void setConsumption() {
    	this.currentState = BATTERY_STATE.CONSUME;
    	
    	this.setHasChanged(true);
    }
    
    @Override
    public void setStandBy() {
    	this.currentState = BATTERY_STATE.STANDBY;
    	
    	this.setHasChanged(true);
    }
    
    public void setHasChanged(boolean hc) {
    	this.hasChanged = hc;
    }
    
    @Override
    public void initialiseState(Time initialTime) {
        super.initialiseState(initialTime);
        
        if (!this.currentProduction.isInitialised() ||
				!this.currentConsumption.isInitialised()) {
			this.currentProduction.initialise(0.0);
			this.currentConsumption.initialise(0.0);
			
			StringBuffer sbp = new StringBuffer("new production: ");
			sbp.append(this.currentProduction.getValue());
			sbp.append(" amperes at ");
			sbp.append(this.currentProduction.getTime());
			sbp.append(" seconds.\n");
			
			StringBuffer sbc = new StringBuffer("new consumption: ");
			sbc.append(this.currentConsumption.getValue());
			sbc.append(" amperes at ");
			sbc.append(this.currentConsumption.getTime());
			sbc.append(" seconds.\n");
			
			this.logMessage(sbp.toString());
			this.logMessage(sbc.toString());
		} 
		
        this.hasChanged = false;
        this.lastInternalTransitionTime = initialTime;

        this.getSimulationEngine().toggleDebugMode();
        logMessage("Simulation starts...\n");
        
        assert	glassBoxInvariants(this) :
			new NeoSim4JavaException(
					"BatteryElectricityModel.glassBoxInvariants(this)");
        assert	blackBoxInvariants(this) :
			new NeoSim4JavaException(
					"BatteryElectricityModel.blackBoxInvariants(this)");
    }
    
    @Override
    public ArrayList<EventI> output() {
        return null;
    }
    
	@Override
	public Duration timeAdvance() {
		if(hasChanged) {
            this.hasChanged = false;
            return Duration.zero(this.getSimulatedTimeUnit());
        }
		
        return Duration.INFINITY;
	}

	@Override
    public void userDefinedInternalTransition(Duration elapsedTime) {
        super.userDefinedInternalTransition(elapsedTime);
        
        Time t = this.getCurrentStateTime();
        double timeElapsed = t.subtract(this.lastInternalTransitionTime).getSimulatedDuration();
        this.lastInternalTransitionTime = t;
        
        switch (this.currentState) {
            case PRODUCT:
                this.currentProduction.setNewValue(PRODUCTION / TENSION, t);
                this.currentConsumption.setNewValue(0.0, t); 

                break;

            case CONSUME:
            	this.currentProduction.setNewValue(0.0, t);
                this.currentConsumption.setNewValue(CONSUMPTION / TENSION, t);

                break;
                
			default:
				this.currentProduction.setNewValue(0.0, t);
				this.currentConsumption.setNewValue(0.0, t);
				break;
        }
        
        System.out.println("etat -> " + this.currentState.toString());
        System.out.println("consommation -> " + this.currentConsumption.getValue());
        System.out.println("production -> " + this.currentProduction.getValue());
        
        this.totalConsumption += this.currentConsumption.getValue() * timeElapsed;
        this.totalProduction += this.currentProduction.getValue() * timeElapsed;
     
        
        logMessage("Current production " + this.currentProduction.getValue() + " at " + this.currentProduction.getTime()
        		+ " current consumption" + this.currentConsumption.getValue() + " at " + this.currentProduction.getTime());
        
        assert	glassBoxInvariants(this) :
			new NeoSim4JavaException(
					"BatteryElectricityModel.glassBoxInvariants(this)");
        assert	blackBoxInvariants(this) :
			new NeoSim4JavaException(
					"BatteryElectricityModel.blackBoxInvariants(this)");
    }
	
	 @Override
    public void userDefinedExternalTransition(Duration elapsedTime) {
        ArrayList<EventI> currentEvents = this.getStoredEventAndReset();
        assert currentEvents != null && currentEvents.size() == 1;
        Event currentEvent = (Event) currentEvents.get(0);

        assert currentEvent instanceof AbstractBatteryEvent;
        currentEvent.executeOn(this);
        
        super.userDefinedExternalTransition(elapsedTime);
        
        assert	glassBoxInvariants(this) :
			new NeoSim4JavaException(
					"BatteryElectricityModel.glassBoxInvariants(this)");
        assert	blackBoxInvariants(this) :
			new NeoSim4JavaException(
					"BatteryElectricityModel.blackBoxInvariants(this)");
    }
	 
	@Override
    public void endSimulation(Time endTime) {
		 BatteryElectricityReport report = (BatteryElectricityReport)this.getFinalReport();
		 logMessage(report.printout(""));
		 
		 logMessage("Simulation ends!\n");
		 super.endSimulation(endTime);
    }
	 
	 
	// -------------------------------------------------------------------------
	// Optional DEVS simulation protocol: simulation run parameters
	// -------------------------------------------------------------------------

	public static final String	PRODUCTION_RUNPNAME = "PRODUCTION";
	public static final String	CONSUMPTION_RUNPNAME = "CONSUMPTION";


	@Override
	public void setSimulationRunParameters(Map<String, Object> simParams) throws MissingRunParameterException {
		super.setSimulationRunParameters(simParams);

		if (simParams.containsKey(
						AtomicSimulatorPlugin.OWNER_RUNTIME_PARAMETER_NAME)) 
		{
			this.getSimulationEngine().setLogger(
					AtomicSimulatorPlugin.createComponentLogger(simParams));
		}

		String productionName = ModelI.createRunParameterName(getURI(), PRODUCTION_RUNPNAME);
		if (simParams.containsKey(productionName)) 
			PRODUCTION = (double)simParams.get(productionName);
		
		String consumptionName = ModelI.createRunParameterName(getURI(), CONSUMPTION_RUNPNAME);
		if (simParams.containsKey(consumptionName)) 
			CONSUMPTION = (double) simParams.get(consumptionName);
	}
	
	
	// -------------------------------------------------------------------------
	// Optional DEVS simulation protocol: simulation report
	// -------------------------------------------------------------------------

	public static class	BatteryElectricityReport implements SimulationReportI, HEM_ReportI {
		private static final long serialVersionUID = 1L;
		protected String modelURI;
		protected double totalConsumption;
		protected double totalProduction;


		public BatteryElectricityReport(String modelURI, double totalConsumption, double totalProduction) {
			super();
			this.modelURI = modelURI;
			this.totalConsumption = totalConsumption;
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
			ret.append("total consumption in kwh = ");
			ret.append(this.totalConsumption);
			ret.append(".\n");
			ret.append(indent);
			ret.append("---\n");
			
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
		return new BatteryElectricityReport(this.getURI(), this.totalConsumption, this.totalProduction);
	}
}
