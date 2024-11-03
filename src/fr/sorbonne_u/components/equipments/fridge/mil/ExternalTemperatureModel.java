package fr.sorbonne_u.components.equipments.fridge.mil;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import fr.sorbonne_u.devs_simulation.hioa.annotations.ExportedVariable;
import fr.sorbonne_u.devs_simulation.hioa.annotations.ModelExportedVariable;
import fr.sorbonne_u.devs_simulation.hioa.models.AtomicHIOA;
import fr.sorbonne_u.devs_simulation.hioa.models.vars.Value;
import fr.sorbonne_u.devs_simulation.models.events.EventI;
import fr.sorbonne_u.devs_simulation.models.time.Duration;
import fr.sorbonne_u.devs_simulation.models.time.Time;
import fr.sorbonne_u.devs_simulation.simulators.interfaces.AtomicSimulatorI;
import fr.sorbonne_u.devs_simulation.simulators.interfaces.SimulationReportI;
import fr.sorbonne_u.devs_simulation.utils.InvariantChecking;
import fr.sorbonne_u.devs_simulation.utils.Pair;
import fr.sorbonne_u.devs_simulation.utils.StandardLogger;

@ModelExportedVariable(name = "externalTemperature", type = Double.class)
public class ExternalTemperatureModel extends AtomicHIOA {

	// -------------------------------------------------------------------------
	// Constants and variables
	// -------------------------------------------------------------------------
	private static final long serialVersionUID = 1L;
	public static final String		URI = ExternalTemperatureModel.class.
															getSimpleName();
					
	protected static double			MIN_EXTERNAL_TEMPERATURE = 0.0;
	protected static double			MAX_EXTERNAL_TEMPERATURE = 40.0;

	protected static double			PERIOD = 24.0;
	protected static double			STEP = 15.0 / 24.0;	// 15 minutes

	protected final Duration		evaluationStep;
	protected double				cycleTime;
	
	@ExportedVariable(type = Double.class)
	protected final Value<Double>	externalTemperature =
												new Value<Double>(this);

	
	// -------------------------------------------------------------------------
	// Constructors
	// -------------------------------------------------------------------------

	public ExternalTemperatureModel(String uri, TimeUnit simulatedTimeUnit, AtomicSimulatorI simulationEngine) throws Exception {
		super(uri, simulatedTimeUnit, simulationEngine);
		this.evaluationStep = new Duration(STEP, this.getSimulatedTimeUnit());
		this.getSimulationEngine().setLogger(new StandardLogger());

		assert glassBoxInvariants(this):
				new AssertionError("White-box invariants violation!");
		assert blackBoxInvariants(this):
				new AssertionError("Black-box invariants violation!");
	}
	
	
	// -------------------------------------------------------------------------
	// Invariants
	// -------------------------------------------------------------------------

	/**
	 * return true if the glass-box invariants are observed, false otherwise.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code instance != null}
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @param instance	instance to be tested.
	 * @return			true if the glass-box invariants are observed, false otherwise.
	 */
	protected static boolean glassBoxInvariants(ExternalTemperatureModel instance) {
		assert	instance != null :
				new AssertionError("Precondition violation: instance != null");

		boolean ret = true;
		ret &= InvariantChecking.checkGlassBoxInvariant(
					instance.cycleTime >= 0.0 && instance.cycleTime <= PERIOD,
					ExternalTemperatureModel.class,
					instance,
					"cycleTime >= 0.0 && instance.cycleTime <= PERIOD");
		ret &= InvariantChecking.checkGlassBoxInvariant(
					STEP > 0.0,
					ExternalTemperatureModel.class,
					instance,
					"STEP > 0.0");
		ret &= InvariantChecking.checkGlassBoxInvariant(
					instance.evaluationStep.getSimulatedDuration() > 0.0,
					ExternalTemperatureModel.class,
					instance,
					"evaluationStep.getSimulatedDuration() > 0.0");
		return ret;
	}

	protected static boolean blackBoxInvariants(ExternalTemperatureModel instance) {

		assert	instance != null :
				new AssertionError("Precondition violation: instance != null");

		boolean ret = true;
		ret &= InvariantChecking.checkBlackBoxInvariant(
					URI != null && !URI.isEmpty(),
					ExternalTemperatureModel.class,
					instance,
					"URI != null && !URI.isEmpty()");
		ret &= InvariantChecking.checkBlackBoxInvariant(
					MAX_EXTERNAL_TEMPERATURE > MIN_EXTERNAL_TEMPERATURE,
					ExternalTemperatureModel.class,
					instance,
					"MAX_EXTERNAL_TEMPERATURE > MIN_EXTERNAL_TEMPERATURE");
		ret &= InvariantChecking.checkBlackBoxInvariant(
					PERIOD > 0.0,
					ExternalTemperatureModel.class,
					instance,
					"PERIOD > 0.0");
		return ret;
	}
	
	
	// -------------------------------------------------------------------------
	// METHODS
	// -------------------------------------------------------------------------
	
	@Override
	public void	initialiseState(Time initialTime) {
		super.initialiseState(initialTime);

		this.cycleTime = 0.0;

		assert	glassBoxInvariants(this) :
			new AssertionError("White-box invariants violation!");
		assert	blackBoxInvariants(this) :
				new AssertionError("Black-box invariants violation!");
	}
	
	@Override
	public boolean useFixpointInitialiseVariables() {
		return true;
	}
	
	@Override
	public Pair<Integer, Integer> fixpointInitialiseVariables() {
		Pair<Integer, Integer> ret = null;

		if (!this.externalTemperature.isInitialised()) {
			this.externalTemperature.initialise(MIN_EXTERNAL_TEMPERATURE);

			this.logMessage("simulation begins.\n");
			StringBuffer message =
					new StringBuffer("current external temperature: ");
			message.append(this.externalTemperature.getValue());
			message.append(" at ");
			message.append(this.getCurrentStateTime());
			message.append("\n");
			this.logMessage(message.toString());

			ret = new Pair<>(1, 0);
		} else {
			ret = new Pair<>(0, 0);
		}

		assert	glassBoxInvariants(this) :
				new AssertionError("White-box invariants violation!");
		assert	blackBoxInvariants(this) :
				new AssertionError("Black-box invariants violation!");

		return ret;
	}
	
	@Override
	public void	initialiseVariables() {
		super.initialiseVariables();

		assert	glassBoxInvariants(this) :
				new AssertionError("Glass-box invariants violation!");
		assert	blackBoxInvariants(this) :
				new AssertionError("Black-box invariants violation!");
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
	public void	userDefinedInternalTransition(Duration elapsedTime) {
		super.userDefinedInternalTransition(elapsedTime);

		this.cycleTime += elapsedTime.getSimulatedDuration();
		if (this.cycleTime > PERIOD) {
			this.cycleTime -= PERIOD;
		}

		double c = Math.cos((1.0 + this.cycleTime/(PERIOD/2.0))*Math.PI);
		double newTemp =
				MIN_EXTERNAL_TEMPERATURE +
					(MAX_EXTERNAL_TEMPERATURE - MIN_EXTERNAL_TEMPERATURE)*
																((1.0 + c)/2.0);
		this.externalTemperature.setNewValue(newTemp,
											 this.getCurrentStateTime());


		StringBuffer message =
				new StringBuffer("current external temperature: ");
		message.append(this.externalTemperature.getValue());
		message.append(" at ");
		message.append(this.getCurrentStateTime());
		message.append("\n");
		this.logMessage(message.toString());

		assert	glassBoxInvariants(this) :
				new AssertionError("White-box invariants violation!");
		assert	blackBoxInvariants(this) :
				new AssertionError("Black-box invariants violation!");
	}
	
	@Override
	public void	endSimulation(Time endTime) {
		this.logMessage("simulation ends.\n");
		super.endSimulation(endTime);
	}
	
	@Override
	public SimulationReportI getFinalReport() {
		return null;
	}
}
