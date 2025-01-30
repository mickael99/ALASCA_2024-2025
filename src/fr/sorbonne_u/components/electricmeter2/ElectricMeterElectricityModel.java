package fr.sorbonne_u.components.electricmeter2;


import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import fr.sorbonne_u.components.cyphy.plugins.devs.AtomicSimulatorPlugin;
import fr.sorbonne_u.components.equipments.hem.mil.HEM_ReportI;
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

//	@ImportedVariable(type = Double.class)
//	protected Value<Double>			currentHairDryerIntensity;
	
	@ImportedVariable(type = Double.class)
	protected Value<Double>			currentIronIntensity;

	/** current total power consumption of the house in amperes.			*/
	@InternalVariable(type = Double.class)
	protected final Value<Double>	currentPowerConsumption =
												new Value<Double>(this);
	/** current total consumption of the house in kwh.						*/
	@InternalVariable(type = Double.class)
	protected final Value<Double>	currentCumulativeConsumption =
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

	protected void		updateCumulativeConsumption(Duration d)
	{
		double c = this.currentCumulativeConsumption.getValue();
		c += Electricity.computeConsumption(
							d, TENSION*this.currentPowerConsumption.getValue());
		Time t = this.currentCumulativeConsumption.getTime().add(d);
		this.currentCumulativeConsumption.setNewValue(c, t);
	}

	protected double		computePowerConsumption()
	{
		double i = this.currentIronIntensity.getValue();

		return i;
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
						&& this.currentIronIntensity.isInitialised()) {
			double i = this.computePowerConsumption();
			this.currentPowerConsumption.initialise(i);
			this.currentCumulativeConsumption.initialise(0.0);
			justInitialised += 2;
		} else if (!this.currentPowerConsumption.isInitialised()) {
			notInitialisedYet += 2;
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

		this.updateCumulativeConsumption(elapsedTime);
		double old = this.currentPowerConsumption.getValue();
		double i = this.computePowerConsumption();
		this.currentPowerConsumption.setNewValue(i, this.getCurrentStateTime());
		
		if (Math.abs(old - i) > 0.000001) {
			StringBuffer message =
						new StringBuffer("current power consumption: ");
			message.append(this.currentPowerConsumption.getValue());
			message.append(" at ");
			message.append(this.getCurrentStateTime());
			this.logMessage(message.toString());
		}
	}

	@Override
	public void			endSimulation(Time endTime)
	{
		this.updateCumulativeConsumption(
				endTime.subtract(this.currentCumulativeConsumption.getTime()));

		this.finalReport = new ElectricMeterElectricityReport(
								this.getURI(),
								this.currentCumulativeConsumption.getValue());

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

		public			ElectricMeterElectricityReport(
			String modelURI,
			double totalConsumption
			)
		{
			super();
			this.modelURI = modelURI;
			this.totalConsumption = totalConsumption;
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
			return ret.toString();
		}		
	}

	@Override
	public SimulationReportI	getFinalReport()
	{
		return this.finalReport;
	}
}
