package fr.sorbonne_u.components.equipments.meter.mil;

import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import fr.sorbonne_u.components.cyphy.plugins.devs.AtomicSimulatorPlugin;
import fr.sorbonne_u.components.equipments.hem.mil.HEM_ReportI;
import fr.sorbonne_u.components.equipments.meter.ElectricMeter;
import fr.sorbonne_u.components.utils.Electricity;
import fr.sorbonne_u.devs_simulation.exceptions.MissingRunParameterException;
import fr.sorbonne_u.devs_simulation.hioa.annotations.ImportedVariable;
import fr.sorbonne_u.devs_simulation.hioa.annotations.InternalVariable;
import fr.sorbonne_u.devs_simulation.hioa.annotations.ModelImportedVariable;
import fr.sorbonne_u.devs_simulation.hioa.models.AtomicHIOA;
import fr.sorbonne_u.devs_simulation.hioa.models.vars.Value;
import fr.sorbonne_u.devs_simulation.models.events.EventI;
import fr.sorbonne_u.devs_simulation.models.time.Duration;
import fr.sorbonne_u.devs_simulation.models.time.Time;
import fr.sorbonne_u.devs_simulation.simulators.interfaces.AtomicSimulatorI;
import fr.sorbonne_u.devs_simulation.simulators.interfaces.SimulationReportI;
import fr.sorbonne_u.devs_simulation.utils.Pair;
import fr.sorbonne_u.devs_simulation.utils.StandardLogger;
import fr.sorbonne_u.exceptions.PreconditionException;

//@ModelImportedVariable(name = "currentFridgeConsumption", type = Double.class)
//@ModelImportedVariable(name = "currentIronConsumption", type = Double.class)
//@ModelImportedVariable(name = "currentBatteryConsumption", type = Double.class)
//@ModelImportedVariable(name = "currentBatteryProduction", type = Double.class)
//@ModelImportedVariable(name = "currentWindTurbineProduction", type = Double.class)
public class ElectricMeterElectricityModel extends AtomicHIOA {

	// -------------------------------------------------------------------------
	// Constants and variables
	// -------------------------------------------------------------------------

    private static final long serialVersionUID = 1L;
	
	public static final String	MIL_URI = ElectricMeterElectricityModel.class.
													getSimpleName() + "-MIL";
	public static final String	MIL_RT_URI = ElectricMeterElectricityModel.class.
													   getSimpleName() + "-MIL_RT";
	public static final String	SIL_URI = ElectricMeterElectricityModel.class.
													getSimpleName() + "-SIL";
	protected static final double STEP = 0.1; // In seconds
	protected final Duration evaluationStep;
	
	public static final double TENSION = 220.0;
	
	protected ElectricMeter ownerComponent;
	protected ElectricMeterElectricityReport	finalReport;
	
	// House devices (consumption only)
	//@ImportedVariable(type = Double.class)
	//protected Value<Double> currentToasterConsumption;
	 
//	@ImportedVariable(type = Double.class)
//	protected Value<Double> currentIronConsumption = new Value<Double>(this);
//	 
//	@ImportedVariable(type = Double.class)
//	protected Value<Double> currentFridgeConsumption = new Value<Double>(this);
//	
//	@ImportedVariable(type = Double.class)
//	protected Value<Double> currentBatteryConsumption = new Value<Double>(this);
	
	 
//	@ImportedVariable(type = Double.class)
//	protected Value<Double> currentSmartLightingConsumption = new Value<Double>(this);
	
	// Production devices
//	@ImportedVariable(type = Double.class)
//	protected Value<Double> currentWindTurbineProduction = new Value<Double>(this);
//	
//	@ImportedVariable(type = Double.class)
//	protected Value<Double> currentBatteryProduction = new Value<Double>(this);
//	
//	@ImportedVariable(type = Double.class)
//	protected Value<Double> currentGeneratorProduction = new Value<Double>(this);
	
	
	// Total values
	@InternalVariable(type = Double.class)
	protected Value<Double> currentConsumption = new Value<Double>(this);
	
	@InternalVariable(type = Double.class)
	protected Value<Double> totalConsumption = new Value<Double>(this);
	
	@InternalVariable(type = Double.class)
	protected Value<Double> currentProduction = new Value<Double>(this);
	
	@InternalVariable(type = Double.class)
	protected Value<Double> totalProduction = new Value<Double>(this);
	
	
	// -------------------------------------------------------------------------
	// Constructors
	// -------------------------------------------------------------------------
	
	public ElectricMeterElectricityModel(String uri, TimeUnit simulatedTimeUnit, AtomicSimulatorI simulationEngine) {
		super(uri, simulatedTimeUnit, simulationEngine);
		this.evaluationStep = new Duration(STEP, getSimulatedTimeUnit());
		this.getSimulationEngine().setLogger(new StandardLogger());
	}
	
	
	// -------------------------------------------------------------------------
	// Methods
	// -------------------------------------------------------------------------
	
	@Override
    public void initialiseState(Time initialTime) {
        super.initialiseState(initialTime);

        this.currentConsumption.initialise(0.0);
        this.currentProduction.initialise(0.0);

        this.getSimulationEngine().toggleDebugMode();
        
        logMessage("Simulation begins. \n");
    }
	
	public Double getCurrentProduction() {
		return this.currentProduction.getValue();
	}
	
	public Double getCurrentConsumption() {
		return this.currentConsumption.getValue();
	}
	
	public void updateTotalConsumption(Duration d) {
        double consumption = this.totalConsumption.getValue();
        consumption += Electricity.computeConsumption(d, TENSION * this.currentConsumption.getValue());
        Time t = this.totalConsumption.getTime().add(d);
        this.totalConsumption.setNewValue(consumption, t);
	}
	
	public double computeCurrentConsumption() {
        double consumption = 0;
//		        		(this.currentSmartLightingConsumption == null || this.currentSmartLightingConsumption.getValue() == null
//		        				? 0.0 : currentSmartLightingConsumption.getValue()) +
//                        (this.currentIronConsumption == null || this.currentIronConsumption.getValue() == null
//                        		? 0.0 : currentIronConsumption.getValue()) +
//                        (this.currentFridgeConsumption == null || this.currentFridgeConsumption.getValue() == null
//                        		? 0.0 : currentFridgeConsumption.getValue()) +
//                        (this.currentBatteryConsumption == null || this.currentBatteryConsumption.getValue() == null 
//                        		? 0.0 : currentBatteryConsumption.getValue());
        
        return consumption;
	}
	
//	public void updateTotalProduction(Duration d) {
//		
//	}
	
	public void updateTotalProduction(Duration d) {
        double production = this.totalProduction.getValue();
        production += Electricity.computeProduction(d, TENSION * this.currentProduction.getValue());
        Time t = this.totalProduction.getTime().add(d);
        this.totalProduction.setNewValue(production, t);
	}
	
	public double computeCurrentProduction() {

        double production = 0;
//                        (this.currentWindTurbineProduction == null || this.currentWindTurbineProduction.getValue() == null
//                        		? 0.0 : currentWindTurbineProduction.getValue()) +
//                        (this.currentBatteryProduction == null || this.currentBatteryProduction.getValue() == null
//                        		? 0.0 : currentBatteryProduction.getValue());
                        //+
//                        (this.currentGeneratorProduction == null || this.currentGeneratorProduction.getValue() == null
//                		? 0.0 : currentGeneratorProduction.getValue());
        
        return production;
    }
	
	@Override
	public boolean useFixpointInitialiseVariables() {
		return true;
	}
	
	@Override
	public Pair<Integer, Integer> fixpointInitialiseVariables() {
		int justInitialised = 0;
		int notInitialisedYet = 0;

		// Consumption
		if (!this.currentConsumption.isInitialised()) {
//						&& this.currentFridgeConsumption.isInitialised()
//						&& this.currentIronConsumption.isInitialised()
//						&& this.currentBatteryConsumption.isInitialised()) {
			double consumption = this.computeCurrentConsumption();
			this.currentConsumption.initialise(consumption);
			this.totalConsumption.initialise(0.0);
			justInitialised += 1;
		} else if (!this.currentConsumption.isInitialised()) {
			notInitialisedYet += 1;
		}
		
//		// Production
//		if (!this.currentProduction.isInitialised() 
//				&& this.currentBatteryProduction.isInitialised()
////				&& this.currentGeneratorProduction.isInitialised()
//				&& this.currentWindTurbineProduction.isInitialised()) {
//			double production = this.computeCurrentProduction();
//			this.currentProduction.initialise(production);
//			this.totalProduction.initialise(0.0);
//			justInitialised += 1;
//		} else if (!this.currentProduction.isInitialised()) {
//			notInitialisedYet += 1;
//		}
		return new Pair<>(justInitialised, notInitialisedYet);
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
        
        this.updateTotalConsumption(elapsedTime);
        this.updateTotalProduction(elapsedTime);
        
        double oldConsumption = this.totalConsumption.getValue();
        double oldProduction = this.totalProduction.getValue();
        
        double currentConsumption = this.computeCurrentConsumption();
        double currentProduction = this.computeCurrentProduction();
        
        if (Math.abs(oldConsumption - currentConsumption) > 0.000001) {
			StringBuffer message =
						new StringBuffer("current power consumption: ");
			message.append(this.currentConsumption.getValue());
			message.append(" at ");
			message.append(this.getCurrentStateTime());
			this.logMessage(message.toString());
		}
        
        if (Math.abs(oldProduction - currentProduction) > 0.000001) {
			StringBuffer message =
						new StringBuffer("current power production: ");
			message.append(this.currentProduction.getValue());
			message.append(" at ");
			message.append(this.getCurrentStateTime());
			this.logMessage(message.toString());
		}
    }

    @Override
    public void endSimulation(Time endTime) {
    	this.updateTotalConsumption(
    			endTime.subtract(this.totalConsumption.getTime()));
    	
    	this.updateTotalProduction(
    			endTime.subtract(this.totalProduction.getTime()));
    	
    	this.finalReport = new ElectricMeterElectricityReport(
				this.getURI(),
				this.totalConsumption.getValue(),
				this.totalProduction.getValue());
    	
        logMessage("Simulation ends!\n");
        super.endSimulation(endTime);
    }
    
    @Override
	public void setSimulationRunParameters(
		Map<String, Object> simParams
		) throws MissingRunParameterException
	{
		super.setSimulationRunParameters(simParams);

		assert	simParams != null && !simParams.isEmpty() :
				new PreconditionException(
								"simParams != null && !simParams.isEmpty()");

		if (simParams.containsKey(
						AtomicSimulatorPlugin.OWNER_RUNTIME_PARAMETER_NAME)) {
			this.ownerComponent = 
				(ElectricMeter) simParams.get(
						AtomicSimulatorPlugin.OWNER_RUNTIME_PARAMETER_NAME);
			this.getSimulationEngine().setLogger(
						AtomicSimulatorPlugin.createComponentLogger(simParams));
		}
	}
    
    public static class		ElectricMeterElectricityReport
	implements	SimulationReportI, HEM_ReportI
	{
		private static final long serialVersionUID = 1L;
		protected String	modelURI;
		protected double	totalConsumption;
		protected double 	totalProduction;

		public			ElectricMeterElectricityReport(
			String modelURI,
			double totalConsumption,
			double totalProduction
			)
		{
			super();
			this.modelURI = modelURI;
			this.totalConsumption = totalConsumption;
			this.totalProduction = totalProduction;
		}

		@Override
		public String	getModelURI()
		{
			return this.modelURI;
		}

		@Override
		public String	printout(String indent)
		{
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

	@Override
	public SimulationReportI	getFinalReport()
	{
		return this.finalReport;
	}
}
