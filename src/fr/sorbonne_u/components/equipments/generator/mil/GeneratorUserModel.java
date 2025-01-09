package fr.sorbonne_u.components.equipments.generator.mil;

import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import fr.sorbonne_u.components.cyphy.plugins.devs.AtomicSimulatorPlugin;
import fr.sorbonne_u.devs_simulation.exceptions.MissingRunParameterException;
import fr.sorbonne_u.devs_simulation.exceptions.NeoSim4JavaException;
import fr.sorbonne_u.devs_simulation.models.interfaces.ModelI;
import fr.sorbonne_u.devs_simulation.utils.InvariantChecking;
import org.apache.commons.math3.random.RandomDataGenerator;

import fr.sorbonne_u.components.equipments.generator.mil.events.ActivateGeneratorEvent;
import fr.sorbonne_u.components.equipments.generator.mil.events.StopGeneratorEvent;
import fr.sorbonne_u.devs_simulation.es.events.ES_EventI;
import fr.sorbonne_u.devs_simulation.es.models.AtomicES_Model;
import fr.sorbonne_u.devs_simulation.models.annotations.ModelExternalEvents;
import fr.sorbonne_u.devs_simulation.models.events.EventI;
import fr.sorbonne_u.devs_simulation.models.time.Duration;
import fr.sorbonne_u.devs_simulation.models.time.Time;
import fr.sorbonne_u.devs_simulation.simulators.interfaces.AtomicSimulatorI;
import fr.sorbonne_u.devs_simulation.utils.StandardLogger;

@ModelExternalEvents(exported = {
        ActivateGeneratorEvent.class,
        StopGeneratorEvent.class
})
public class GeneratorUserModel extends AtomicES_Model {

	private static final long serialVersionUID = 1L;
	
	public static final String URI = GeneratorUserModel.class.getSimpleName();
    public static final String MIL_URI = GeneratorUserModel.class.getSimpleName() + "-MIL";
    public static final String MIL_RT_URI = GeneratorUserModel.class.getSimpleName() + "-MIL-RT";
    public static final String SIL_URI = GeneratorUserModel.class.getSimpleName() + "-MIL_RT";

    protected static double STEP_MEAN_DURATION = 5.0/60.0;
    protected static double		DELAY_MEAN_DURATION = 4.0;

    protected RandomDataGenerator generator;

    // -------------------------------------------------------------------------
    // Invariants
    // -------------------------------------------------------------------------

    protected static boolean glassBoxInvariants(GeneratorUserModel model) {
        assert model != null:
                new NeoSim4JavaException("The model is null!");

        boolean ret = true;
        ret &= InvariantChecking.checkGlassBoxInvariant(
                STEP_MEAN_DURATION > 0.0,
                GeneratorUserModel.class,
                model,
                "STEP_MEAN_DURATION > 0.0");
        ret &= InvariantChecking.checkGlassBoxInvariant(
                DELAY_MEAN_DURATION > 0.0,
                GeneratorUserModel.class,
                model,
                "DELAY_MEAN_DURATION > 0.0");
        ret &= InvariantChecking.checkGlassBoxInvariant(
                model.generator != null,
                GeneratorUserModel.class,
                model,
                "rg != null");
        return ret;

    }

    protected static boolean	blackBoxInvariants(GeneratorUserModel instance)
    {
        assert	instance != null :
                new NeoSim4JavaException("Precondition violation: "
                                         + "instance != null");

        boolean ret = true;
        ret &= InvariantChecking.checkBlackBoxInvariant(
                MIL_URI != null && !MIL_URI.isEmpty(),
                GeneratorUserModel.class,
                instance,
                "MIL_URI != null && !MIL_URI.isEmpty()");
        ret &= InvariantChecking.checkBlackBoxInvariant(
                MIL_RT_URI != null && !MIL_RT_URI.isEmpty(),
                GeneratorUserModel.class,
                instance,
                "MIL_RT_URI != null && !MIL_RT_URI.isEmpty()");
        ret &= InvariantChecking.checkBlackBoxInvariant(
                SIL_URI != null && !SIL_URI.isEmpty(),
                GeneratorUserModel.class,
                instance,
                "SIL_URI != null && !SIL_URI.isEmpty()");
        ret &= InvariantChecking.checkBlackBoxInvariant(
                MEAN_STEP_RPNAME != null && !MEAN_STEP_RPNAME.isEmpty(),
                GeneratorUserModel.class,
                instance,
                "MEAN_STEP_RPNAME != null && !MEAN_STEP_RPNAME.isEmpty()");
        ret &= InvariantChecking.checkBlackBoxInvariant(
                MEAN_DELAY_RPNAME != null && !MEAN_DELAY_RPNAME.isEmpty(),
                GeneratorUserModel.class,
                instance,
                "MEAN_DELAY_RPNAME != null && !MEAN_DELAY_RPNAME.isEmpty()");
        return ret;
    }

    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------
    
    
    public GeneratorUserModel(String uri, TimeUnit simulatedTimeUnit, AtomicSimulatorI simulationEngine) throws Exception {
        super(uri, simulatedTimeUnit, simulationEngine);
        this.generator = new RandomDataGenerator();
		this.getSimulationEngine().setLogger(new StandardLogger());

        assert glassBoxInvariants(this) :
                new NeoSim4JavaException("GeneratorUserModel.glassBoxInvariants(this)");
        assert blackBoxInvariants(this) :
                new NeoSim4JavaException("GeneratorUserModel.blackBoxInvariants(this)");
    }

    // -------------------------------------------------------------------------
    // Methods
    // -------------------------------------------------------------------------

    protected void generateNextEvent() {
        EventI current = eventList.peek();
        assert current != null;
        Time nextTime = this.computeTimeOfNextEvent(current.getTimeOfOccurrence());

        ES_EventI next = null;
        if(current instanceof ActivateGeneratorEvent) {
            next = new StopGeneratorEvent(nextTime);
        }
        else if(current instanceof StopGeneratorEvent) {
            next = new ActivateGeneratorEvent(nextTime);
        }
        scheduleEvent(next);
    }

    protected Time computeTimeOfNextEvent(Time from) {
        double delay = Math.max(generator.nextGaussian(STEP_MEAN_DURATION, STEP_MEAN_DURATION/2.0), 0.1);
        return from.add(new Duration(delay, this.getSimulatedTimeUnit()));
    }

    // -------------------------------------------------------------------------
    // DEVS simulation protocol
    // -------------------------------------------------------------------------

    @Override
    public void initialiseState(Time initialTime) {
        super.initialiseState(initialTime);

        generator.reSeedSecure();

        Time nextTime = computeTimeOfNextEvent(getCurrentStateTime());
        scheduleEvent(new ActivateGeneratorEvent(nextTime));

        nextTimeAdvance = timeAdvance();
        timeOfNextEvent = getCurrentStateTime().add(getNextTimeAdvance());

        this.getSimulationEngine().toggleDebugMode();;
        logMessage("Simulation starts...\n");
    }
    
    @Override
    public ArrayList<EventI> output() {
        if(eventList.peek() != null) 
        	generateNextEvent();

        return super.output();
    }

    @Override
    public void endSimulation(Time endTime) {
        logMessage("Simulation ends!\n");
        super.endSimulation(endTime);
    }

    // -------------------------------------------------------------------------
    // Optional DEVS simulation protocol: simulation run parameters
    // -------------------------------------------------------------------------

    public static final String		MEAN_STEP_RPNAME = "STEP_MEAN_DURATION";
    public static final String		MEAN_DELAY_RPNAME = "STEP_MEAN_DURATION";

    /**
     * @see fr.sorbonne_u.devs_simulation.models.interfaces.ModelI#setSimulationRunParameters(Map)
     */
    @Override
    public void			setSimulationRunParameters(
            Map<String, Object> simParams
                                                     ) throws MissingRunParameterException
    {
        super.setSimulationRunParameters(simParams);

        if (simParams.containsKey(
                AtomicSimulatorPlugin.OWNER_RUNTIME_PARAMETER_NAME)) {

            this.getSimulationEngine().setLogger(
                    AtomicSimulatorPlugin.createComponentLogger(simParams));
        }

        String stepName =
                ModelI.createRunParameterName(getURI(), MEAN_STEP_RPNAME);
        if (simParams.containsKey(stepName)) {
            STEP_MEAN_DURATION = (double) simParams.get(stepName);
        }
        String delayName =
                ModelI.createRunParameterName(getURI(), MEAN_DELAY_RPNAME);
        if (simParams.containsKey(delayName)) {
            DELAY_MEAN_DURATION = (double) simParams.get(delayName);
        }
    }
}
