package fr.sorbonne_u.components.equipments.generator.mil;

import fr.sorbonne_u.components.cyphy.plugins.devs.AtomicSimulatorPlugin;
import fr.sorbonne_u.components.equipments.generator.mil.events.AbstractGeneratorEvents;
import fr.sorbonne_u.components.equipments.generator.mil.events.ActivateGeneratorEvent;
import fr.sorbonne_u.components.equipments.generator.mil.events.StopGeneratorEvent;
import fr.sorbonne_u.devs_simulation.exceptions.MissingRunParameterException;
import fr.sorbonne_u.devs_simulation.exceptions.NeoSim4JavaException;
import fr.sorbonne_u.devs_simulation.models.AtomicModel;
import fr.sorbonne_u.devs_simulation.models.annotations.ModelExternalEvents;
import fr.sorbonne_u.devs_simulation.models.events.EventI;
import fr.sorbonne_u.devs_simulation.models.time.Duration;
import fr.sorbonne_u.devs_simulation.models.time.Time;
import fr.sorbonne_u.devs_simulation.simulators.interfaces.AtomicSimulatorI;
import fr.sorbonne_u.devs_simulation.utils.InvariantChecking;
import fr.sorbonne_u.devs_simulation.utils.StandardLogger;

import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@ModelExternalEvents(
        imported = {
                ActivateGeneratorEvent.class, StopGeneratorEvent.class},
        exported = {
                ActivateGeneratorEvent.class, StopGeneratorEvent.class}
)
public class GeneratorStateModel extends AtomicModel implements GeneratorOperationI {

    private static final long serialVersionUID = 1L;
    public static final String URI = GeneratorStateModel.class.getSimpleName();
    public static final String MIL_URI = GeneratorStateModel.class.getSimpleName() + "-MIL";
    public static final String MIL_RT_URI = GeneratorStateModel.class.getSimpleName() + "-MIL/SIL_RT";
    public static final String SIL_URI = GeneratorStateModel.class.getSimpleName() + "-MIL/SIL_RT";

    protected Boolean isRunning;
    protected AbstractGeneratorEvents lastReceived;

    // -------------------------------------------------------------------------
    // Invairants
    // -------------------------------------------------------------------------

    protected static boolean glassBoxInvariants(GeneratorStateModel model) {
        assert model != null:
                new NeoSim4JavaException("The model is null!");

        boolean ret = true;
        return ret;
    }

    protected static boolean blackBoxInvariants(GeneratorStateModel instance) {
        assert instance != null:
                new NeoSim4JavaException("The instance is null!");

        boolean ret = true;
        ret &= InvariantChecking.checkBlackBoxInvariant(
                MIL_URI != null && !MIL_URI.isEmpty(),
                GeneratorStateModel.class,
                instance,
                "MIL_URI != null && !MIL_URI.isEmpty()");
        ret &= InvariantChecking.checkBlackBoxInvariant(
                MIL_RT_URI != null && !MIL_RT_URI.isEmpty(),
                GeneratorStateModel.class,
                instance,
                "MIL_RT_URI != null && !MIL_RT_URI.isEmpty()");
        ret &= InvariantChecking.checkBlackBoxInvariant(
                SIL_URI != null && !SIL_URI.isEmpty(),
                GeneratorStateModel.class,
                instance,
                "SIL_URI != null && !SIL_URI.isEmpty()");
        return ret;
    }

    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------

    public GeneratorStateModel(String uri,
                               TimeUnit simulatedTimeUnit,
                               AtomicSimulatorI simulationEngine
                               ) {
        super(uri, simulatedTimeUnit, simulationEngine);
        this.getSimulationEngine().setLogger(new StandardLogger());

        assert glassBoxInvariants(this) :
                new NeoSim4JavaException("GeneratorStateModel.glassBoxInvariants(this)");
        assert blackBoxInvariants(this) :
                new NeoSim4JavaException("GeneratorStateModel.blackBoxInvariants(this)");
    }

    // -------------------------------------------------------------------------
    // Methods
    // -------------------------------------------------------------------------
    @Override
    public void activate() {
        if (!this.isRunning) {
            this.isRunning = true;
        }
    }

    @Override
    public void stop() {
        if (this.isRunning) {
            this.isRunning = false;
        }
    }

    // -------------------------------------------------------------------------
    // DEVS simulation protocol
    // -------------------------------------------------------------------------


    @Override
    public void			initialiseState(Time initialTime)
    {
        super.initialiseState(initialTime);

        this.lastReceived = null;
        this.isRunning = false;

        this.getSimulationEngine() .toggleDebugMode();
        this.logMessage("simulation begins.");
    }

    @Override
    public ArrayList<EventI> output()
    {
        assert	this.lastReceived != null;

        ArrayList<EventI> ret = new ArrayList<EventI>();
        ret.add(this.lastReceived);
        this.lastReceived = null;
        return ret;
    }

    @Override
    public Duration timeAdvance() {
        if (this.lastReceived != null) {
            // trigger an immediate internal transition
            return Duration.zero(this.getSimulatedTimeUnit());
        } else {
            // wait until the next external event that will trigger an internal
            // transition
            return Duration.INFINITY;
        }
    }


    @Override
    public void			userDefinedExternalTransition(Duration elapsedTime)
    {
        super.userDefinedExternalTransition(elapsedTime);

        // get the vector of current external events
        ArrayList<EventI> currentEvents = this.getStoredEventAndReset();
        // when this method is called, there is at least one external event,
        // and for the hair dryer model, there will be exactly one by
        // construction.
        assert	currentEvents != null && currentEvents.size() == 1;

        // this will trigger an internal transition by the fact that
        // lastReceived will not be null; the internal transition does nothing
        // on the model state except to put lastReceived to null again, but
        // this will also trigger output and the sending of the event to
        // the electricity model to also change its state
        this.lastReceived = (AbstractGeneratorEvents) currentEvents.get(0);

        // tracing
        StringBuffer message = new StringBuffer(this.uri);
        message.append(" executes the external event ");
        message.append(this.lastReceived);
        this.logMessage(message.toString());
    }

    @Override
    public void			endSimulation(Time endTime)
    {
        this.logMessage("simulation ends.");
        super.endSimulation(endTime);
    }

    // -------------------------------------------------------------------------
    // Optional DEVS simulation protocol: simulation run parameters
    // -------------------------------------------------------------------------

    /**
     * @see fr.sorbonne_u.devs_simulation.models.Model#setSimulationRunParameters(java.util.Map)
     */
    @Override
    public void			setSimulationRunParameters(
            Map<String, Object> simParams
                                                     ) throws MissingRunParameterException
    {
        super.setSimulationRunParameters(simParams);

        // this gets the reference on the owner component which is required
        // to have simulation models able to make the component perform some
        // operations or tasks or to get the value of variables held by the
        // component when necessary.
        if (simParams.containsKey(
                AtomicSimulatorPlugin.OWNER_RUNTIME_PARAMETER_NAME)) {
            // by the following, all of the logging will appear in the owner
            // component logger
            this.getSimulationEngine().setLogger(
                    AtomicSimulatorPlugin.createComponentLogger(simParams));
        }
    }


}
