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


//@ModelImportedVariable(name = "currentHairDryerIntensity",
//					   type = Double.class)
@ModelImportedVariable(name = "currentIronIntensity",
						type = Double.class)
@ModelImportedVariable(name = "currentFridgeIntensity",
						type = Double.class)
@ModelImportedVariable(name = "currentWindTurbineProduction",
								type = Double.class)
@ModelImportedVariable(name = "currentBatteryProduction",
						type = Double.class)
@ModelImportedVariable(name = "currentBatteryConsumption",
						type = Double.class)
//-----------------------------------------------------------------------------
public class			ElectricMeterElectricityModel
extends		AtomicHIOA
{
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

	public static final double		TENSION = 220.0;

	protected static final double	STEP = 10.0/3600.0;	// 10 seconds
	protected final Duration		evaluationStep;

	protected ElectricMeter						ownerComponent;
	protected ElectricMeterElectricityReport	finalReport;

	// -------------------------------------------------------------------------
	// HIOA model variables
	// -------------------------------------------------------------------------

	// Consumption
	@ImportedVariable(type = Double.class)
	protected Value<Double> currentIronIntensity;
	
	@ImportedVariable(type = Double.class)
	protected Value<Double> currentFridgeIntensity;
	
	@ImportedVariable(type = Double.class)
	protected Value<Double> currentBatteryConsumption;
	
//	@ImportedVariable(type = Double.class)
//	protected Value<Double>			currentSmartLightIntensity;
	
	// Production
	
//	@ImportedVariable(type = Double.class)
//	protected Value<Double>			currentGeneratorProduction;
	
	@ImportedVariable(type = Double.class)
	protected Value<Double>			currentWindTurbineProduction;
	
	@ImportedVariable(type = Double.class)
	protected Value<Double>			currentBatteryProduction;

	// Internal variables
	
	/** current total power consumption of the house in amperes.			*/
	@InternalVariable(type = Double.class)
	protected final Value<Double>	currentPowerConsumption =
												new Value<Double>(this);
	/** current total consumption of the house in kwh.						*/
	@InternalVariable(type = Double.class)
	protected final Value<Double>	totalConsumption =
												new Value<Double>(this);
	@InternalVariable(type = Double.class)
	protected final Value<Double>	currentPowerProduction =
												new Value<Double>(this);
	/** current total consumption of the house in kwh.						*/
	@InternalVariable(type = Double.class)
	protected final Value<Double>	totalProduction =
												new Value<Double>(this);

	// -------------------------------------------------------------------------
	// Constructors
	// -------------------------------------------------------------------------

	public				ElectricMeterElectricityModel(
		String uri,
		TimeUnit simulatedTimeUnit,
		AtomicSimulatorI simulationEngine
		) throws Exception
	{
		super(uri, simulatedTimeUnit, simulationEngine);
		this.evaluationStep = new Duration(STEP, this.getSimulatedTimeUnit());
		this.getSimulationEngine().setLogger(new StandardLogger());
	}

	// -------------------------------------------------------------------------
	// Methods
	// -------------------------------------------------------------------------

	protected void updateTotalConsumption(Duration d) {
		double c = this.totalConsumption.getValue();
		c += Electricity.computeConsumption(
							d, TENSION*this.currentPowerConsumption.getValue());
		Time t = this.totalConsumption.getTime().add(d);
		this.totalConsumption.setNewValue(c, t);
	}
	
	protected void updateTotalProduction(Duration d) {
		double p = this.totalProduction.getValue();
		p += Electricity.computeProduction(
							d, TENSION*this.currentPowerProduction.getValue());
		Time t = this.totalProduction.getTime().add(d);
		totalProduction.setNewValue(p, t);
	}

	protected double computePowerConsumption() {
		double consumption = 
			//		(this.currentSmartLightingConsumption == null || this.currentSmartLightingConsumption.getValue() == null
			//				? 0.0 : currentSmartLightingConsumption.getValue()) +
			        (this.currentIronIntensity == null || this.currentIronIntensity.getValue() == null
			        		? 0.0 : currentIronIntensity.getValue()) +
			        (this.currentFridgeIntensity == null || this.currentFridgeIntensity.getValue() == null
			        		? 0.0 : currentFridgeIntensity.getValue()) +
			        (this.currentBatteryConsumption == null || this.currentBatteryConsumption.getValue() == null 
			        		? 0.0 : currentBatteryConsumption.getValue());
		
		return consumption;
	}
	
	public double computePowerProduction() {

        double production = 
                        (this.currentWindTurbineProduction == null || this.currentWindTurbineProduction.getValue() == null
                        		? 0.0 : currentWindTurbineProduction.getValue()) +
                        (this.currentBatteryProduction == null || this.currentBatteryProduction.getValue() == null
                        		? 0.0 : currentBatteryProduction.getValue());
                        //+
//                        (this.currentGeneratorProduction == null || this.currentGeneratorProduction.getValue() == null
//                		? 0.0 : currentGeneratorProduction.getValue());
        return production;
    }

	// -------------------------------------------------------------------------
	// DEVS simulation protocol
	// -------------------------------------------------------------------------

	@Override
	public boolean		useFixpointInitialiseVariables()
	{
		return true;
	}

	@Override
	public Pair<Integer, Integer>	fixpointInitialiseVariables()
	{
		int justInitialised = 0;
		int notInitialisedYet = 0;

		if (!this.currentPowerConsumption.isInitialised()
						&& this.currentIronIntensity.isInitialised() 
						&& this.currentBatteryConsumption.isInitialised()
						&& this.currentFridgeIntensity.isInitialised()) {
			double i = this.computePowerConsumption();
			this.currentPowerConsumption.initialise(i);
			this.totalConsumption.initialise(0.0);
			justInitialised += 1;
		} else if (!this.currentPowerConsumption.isInitialised()) {
			notInitialisedYet += 1;
		}
		
		if (!this.currentPowerProduction.isInitialised()
				&& this.currentWindTurbineProduction.isInitialised()
				&& this.currentBatteryProduction.isInitialised()) {
			double i = this.computePowerProduction();
			this.currentPowerProduction.initialise(i);
			this.totalProduction.initialise(0.0);
			justInitialised += 1;
		} else if (!this.currentPowerProduction.isInitialised()) {
			notInitialisedYet += 1;
		}
		return new Pair<>(justInitialised, notInitialisedYet);
	}

	@Override
	public ArrayList<EventI>	output()
	{
		return null;
	}

	@Override
	public Duration		timeAdvance()
	{
		return this.evaluationStep;
	}

	@Override
	public void			userDefinedInternalTransition(Duration elapsedTime)
	{
		super.userDefinedInternalTransition(elapsedTime);

		this.updateTotalConsumption(elapsedTime);
		double oldConsumption = this.currentPowerConsumption.getValue();
		double currentConsumption = this.computePowerConsumption();
		this.currentPowerConsumption.setNewValue(currentConsumption, this.getCurrentStateTime());
		
		if (Math.abs(oldConsumption - currentConsumption) > 0.000001) {
			StringBuffer message =
						new StringBuffer("current power consumption: ");
			message.append(this.currentPowerConsumption.getValue());
			message.append(" at ");
			message.append(this.getCurrentStateTime());
			this.logMessage(message.toString());
		}
		
		this.updateTotalProduction(elapsedTime);
		double oldProduction = this.currentPowerProduction.getValue();
		double currentProduction = this.computePowerProduction();
		this.currentPowerProduction.setNewValue(currentProduction, this.getCurrentStateTime());
		
		if (Math.abs(oldProduction - currentProduction) > 0.000001) {
			StringBuffer message =
						new StringBuffer("current power production: ");
			message.append(this.currentPowerProduction.getValue());
			message.append(" at ");
			message.append(this.getCurrentStateTime());
			this.logMessage(message.toString());
		}
	}

	@Override
	public void			endSimulation(Time endTime)
	{
		this.updateTotalConsumption(
				endTime.subtract(this.totalConsumption.getTime()));

		ElectricMeterElectricityReport report = (ElectricMeterElectricityReport)this.getFinalReport();
		this.logMessage(report.printout(""));

		this.logMessage("simulation ends.");
		super.endSimulation(endTime);
	}

	// -------------------------------------------------------------------------
	// Optional DEVS simulation protocol: simulation run parameters
	// -------------------------------------------------------------------------

	@Override
	public void			setSimulationRunParameters(
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

	// -------------------------------------------------------------------------
	// Optional DEVS simulation protocol: simulation report
	// -------------------------------------------------------------------------

	public static class		ElectricMeterElectricityReport
	implements	SimulationReportI, HEM_ReportI
	{
		private static final long serialVersionUID = 1L;
		protected String	modelURI;
		protected double	totalConsumption; // in kwh
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
		return new ElectricMeterElectricityReport(  this.getURI(), 
													this.totalConsumption.getValue(), 
													this.totalProduction.getValue());
	}
}
