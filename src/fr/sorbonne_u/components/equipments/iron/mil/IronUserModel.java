package fr.sorbonne_u.components.equipments.iron.mil;

import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.commons.math3.random.RandomDataGenerator;

import fr.sorbonne_u.components.equipments.iron.mil.events.DisableEnergySavingModeIron;
import fr.sorbonne_u.components.equipments.iron.mil.events.DisableSteamModeIron;
import fr.sorbonne_u.components.equipments.iron.mil.events.EnableCottonModeIron;
import fr.sorbonne_u.components.equipments.iron.mil.events.EnableDelicateModeIron;
import fr.sorbonne_u.components.equipments.iron.mil.events.EnableEnergySavingModeIron;
import fr.sorbonne_u.components.equipments.iron.mil.events.EnableLinenModeIron;
import fr.sorbonne_u.components.equipments.iron.mil.events.EnableSteamModeIron;
import fr.sorbonne_u.components.equipments.iron.mil.events.TurnOffIron;
import fr.sorbonne_u.components.equipments.iron.mil.events.TurnOnIron;
import fr.sorbonne_u.devs_simulation.es.events.ES_EventI;
import fr.sorbonne_u.devs_simulation.es.models.AtomicES_Model;
import fr.sorbonne_u.devs_simulation.exceptions.MissingRunParameterException;
import fr.sorbonne_u.devs_simulation.models.annotations.ModelExternalEvents;
import fr.sorbonne_u.devs_simulation.models.events.EventI;
import fr.sorbonne_u.devs_simulation.models.interfaces.ModelI;
import fr.sorbonne_u.devs_simulation.models.time.Duration;
import fr.sorbonne_u.devs_simulation.models.time.Time;
import fr.sorbonne_u.devs_simulation.simulators.interfaces.AtomicSimulatorI;
import fr.sorbonne_u.devs_simulation.simulators.interfaces.SimulationReportI;
import fr.sorbonne_u.devs_simulation.utils.InvariantChecking;
import fr.sorbonne_u.devs_simulation.utils.StandardLogger;

@ModelExternalEvents(imported = {DisableEnergySavingModeIron.class,
		 EnableEnergySavingModeIron.class,
		 DisableSteamModeIron.class,
		 EnableSteamModeIron.class,
		 EnableDelicateModeIron.class,
		 EnableCottonModeIron.class,
		 EnableLinenModeIron.class,
		 TurnOnIron.class,
		 TurnOffIron.class})
public class IronUserModel extends AtomicES_Model {

	// -------------------------------------------------------------------------
	// Constants and variables
	// -------------------------------------------------------------------------
	
	private static final long serialVersionUID = 1L;											
	public static final String URI = IronUserModel.class.getSimpleName();

	protected static double	STEP_MEAN_DURATION = 5.0/60.0;
	protected static double	DELAY_MEAN_DURATION = 4.0;

	protected final RandomDataGenerator	rg ;
	
	
	// -------------------------------------------------------------------------
	// Constructors
	// -------------------------------------------------------------------------
	
	public IronUserModel(String uri, TimeUnit simulatedTimeUnit, AtomicSimulatorI simulationEngine) throws Exception {
		super(uri, simulatedTimeUnit, simulationEngine);

		this.rg = new RandomDataGenerator();
		this.getSimulationEngine().setLogger(new StandardLogger());

		assert glassBoxInvariants(this) :
				new AssertionError("Glass-box invariants violation!");
		assert blackBoxInvariants(this) :
				new AssertionError("Black-box invariants violation!");
	}
	
	
	// -------------------------------------------------------------------------
	// Invariants
	// -------------------------------------------------------------------------

	protected static boolean glassBoxInvariants(IronUserModel instance) {
		assert	instance != null :
				new AssertionError("Precondition violation: instance != null");

		boolean ret = true;
		ret &= InvariantChecking.checkGlassBoxInvariant(
				STEP_MEAN_DURATION > 0.0,
				IronUserModel.class,
				instance,
				"STEP_MEAN_DURATION > 0.0");
		ret &= InvariantChecking.checkGlassBoxInvariant(
				DELAY_MEAN_DURATION > 0.0,
				IronUserModel.class,
				instance,
				"DELAY_MEAN_DURATION > 0.0");
		ret &= InvariantChecking.checkGlassBoxInvariant(
				instance.rg != null,
				IronUserModel.class,
				instance,
				"rg != null");
		return ret;
	}
	
	protected static boolean blackBoxInvariants(IronUserModel instance) {
		assert	instance != null :
				new AssertionError("Precondition violation: instance != null");

		boolean ret = true;
		ret &= InvariantChecking.checkBlackBoxInvariant(
				URI != null && !URI.isEmpty(),
				IronUserModel.class,
				instance,
				"URI != null && !URI.isEmpty()");
		ret &= InvariantChecking.checkBlackBoxInvariant(
				MEAN_STEP_RPNAME != null && !MEAN_STEP_RPNAME.isEmpty(),
				IronUserModel.class,
				instance,
				"MEAN_STEP_RPNAME != null && !MEAN_STEP_RPNAME.isEmpty()");
		ret &= InvariantChecking.checkBlackBoxInvariant(
				MEAN_DELAY_RPNAME != null && !MEAN_DELAY_RPNAME.isEmpty(),
				IronUserModel.class,
				instance,
				"MEAN_DELAY_RPNAME != null && !MEAN_DELAY_RPNAME.isEmpty()");
		return ret;
	}
	
	
	// -------------------------------------------------------------------------
	// Methods
	// -------------------------------------------------------------------------
	
	@Override
	public void	initialiseState(Time initialTime) {
		super.initialiseState(initialTime);

		this.rg.reSeedSecure();
		Time t = this.computeTimeOfNextEvent(this.getCurrentStateTime());

		this.scheduleEvent(new TurnOnIron(t));
		this.nextTimeAdvance = this.timeAdvance();
		this.timeOfNextEvent = this.getCurrentStateTime().add(this.getNextTimeAdvance());

		this.getSimulationEngine().toggleDebugMode();
		this.logMessage("simulation begins.\n");
	}
	
	@Override
	public ArrayList<EventI> output() {
		if (this.eventList.peek() != null) 
			this.generateNextEvent();

		return super.output();
	}
	
	@Override
	public void	endSimulation(Time endTime) {
		this.logMessage("simulation ends.\n");
		super.endSimulation(endTime);
	}
	
	protected void generateNextEvent() {
		EventI current = this.eventList.peek();
		ES_EventI nextEvent = null;
		
		if(current instanceof TurnOffIron) {
			Time t2 = this.computeTimeOfNextUsage(current.getTimeOfOccurrence());
			nextEvent = new TurnOnIron(t2);
		}
		else {
			Time t = this.computeTimeOfNextEvent(current.getTimeOfOccurrence());
			if(current instanceof TurnOnIron)
				nextEvent = new EnableCottonModeIron(t);
			if(current instanceof EnableCottonModeIron)
				nextEvent = new EnableLinenModeIron(t);
			if(current instanceof EnableLinenModeIron)
				nextEvent = new EnableEnergySavingModeIron(t);
			if(current instanceof EnableEnergySavingModeIron)
				nextEvent = new EnableSteamModeIron(t);
			if(current instanceof EnableSteamModeIron)
				nextEvent = new DisableEnergySavingModeIron(t);
			if(current instanceof DisableEnergySavingModeIron)
				nextEvent = new DisableSteamModeIron(t);
			if(current instanceof DisableSteamModeIron)
				nextEvent = new EnableDelicateModeIron(t);
			if(current instanceof EnableDelicateModeIron)
				nextEvent = new TurnOffIron(t);
		}
		
		this.scheduleEvent(nextEvent);
	}
	
	protected Time computeTimeOfNextUsage(Time from) {
		assert	from != null;
 
		double delay = Math.max(this.rg.nextGaussian(DELAY_MEAN_DURATION,
													 DELAY_MEAN_DURATION/10.0),
								0.1);

		Time t = from.add(new Duration(delay, this.getSimulatedTimeUnit()));
		return t;
	}
	
	protected Time computeTimeOfNextEvent(Time from) {
		assert	from != null;

		double delay = Math.max(this.rg.nextGaussian(STEP_MEAN_DURATION,
													 STEP_MEAN_DURATION/2.0),
								0.1);

		Time t = from.add(new Duration(delay, this.getSimulatedTimeUnit()));
		return t;
	}
	
	// -------------------------------------------------------------------------
	// Optional DEVS simulation protocol: simulation run parameters
	// -------------------------------------------------------------------------

	public static final String MEAN_STEP_RPNAME = "STEP_MEAN_DURATION";	
	public static final String MEAN_DELAY_RPNAME = "STEP_MEAN_DURATION";
	
	@Override
	public void	setSimulationRunParameters(Map<String, Object> simParams) throws MissingRunParameterException {
		super.setSimulationRunParameters(simParams);

		String stepName = ModelI.createRunParameterName(getURI(), MEAN_STEP_RPNAME);
		if (simParams.containsKey(stepName)) 
			STEP_MEAN_DURATION = (double) simParams.get(stepName);
		
		String delayName = ModelI.createRunParameterName(getURI(), MEAN_DELAY_RPNAME);
		if (simParams.containsKey(delayName)) 
			DELAY_MEAN_DURATION = (double) simParams.get(delayName);
	}

	@Override
	public SimulationReportI getFinalReport() {
		return null;
	}
}
