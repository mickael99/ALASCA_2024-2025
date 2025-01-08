package fr.sorbonne_u.components.equipments.toaster.mil;

import fr.sorbonne_u.components.cyphy.plugins.devs.AtomicSimulatorPlugin;
import fr.sorbonne_u.components.equipments.toaster.mil.events.SetToasterBrowningLevel;
import fr.sorbonne_u.components.equipments.toaster.mil.events.ToasterEventI;
import fr.sorbonne_u.components.equipments.toaster.mil.events.TurnOffToaster;
import fr.sorbonne_u.components.equipments.toaster.mil.events.TurnOnToaster;
import fr.sorbonne_u.devs_simulation.exceptions.MissingRunParameterException;
import fr.sorbonne_u.devs_simulation.exceptions.NeoSim4JavaException;
import fr.sorbonne_u.devs_simulation.hioa.models.AtomicHIOA;
import fr.sorbonne_u.devs_simulation.models.annotations.ModelExternalEvents;
import fr.sorbonne_u.devs_simulation.models.events.Event;
import fr.sorbonne_u.devs_simulation.models.events.EventI;
import fr.sorbonne_u.devs_simulation.models.time.Duration;
import fr.sorbonne_u.devs_simulation.models.time.Time;
import fr.sorbonne_u.devs_simulation.simulators.interfaces.AtomicSimulatorI;
import fr.sorbonne_u.devs_simulation.utils.InvariantChecking;
import fr.sorbonne_u.devs_simulation.utils.StandardLogger;

import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@ModelExternalEvents(imported = {
        TurnOnToaster.class,
        TurnOffToaster.class,
        SetToasterBrowningLevel.class
                                },
        exported = {
                TurnOnToaster.class,
                TurnOffToaster.class,
                SetToasterBrowningLevel.class
        })
public class ToasterStateModel extends AtomicHIOA implements ToasterOperationI {

    // -------------------------------------------------------------------------
    // Inner classes
    // -------------------------------------------------------------------------

    public static enum ToasterState {
        ON,
        OFF
    }

    public static enum ToasterBrowningLevel {
        DEFROST,
        LOW,
        MEDIUM,
        HIGH
    }
    // -------------------------------------------------------------------------
    // Constants and variables
    // -------------------------------------------------------------------------

    private static final long serialVersionUID = 1L;
    public static final String MIL_URI = ToasterStateModel.class.getSimpleName() + "-MIL";
    public static final String MIL_RT_URI = ToasterStateModel.class.getSimpleName() + "-MIL_RT";
    public static final String SIL_URI = ToasterStateModel.class.getSimpleName() + "-SIL";

    protected ToasterState currentState;
    protected ToasterBrowningLevel currentBrowningLevel;
    protected EventI toBeReemitted;

    // -------------------------------------------------------------------------
    // Invariant
    // -------------------------------------------------------------------------

    protected static boolean glassBoxInvariant(ToasterStateModel model) {
        assert model != null:
                new NeoSim4JavaException("model is null");

        boolean ret = true;
        ret &= InvariantChecking.checkGlassBoxInvariant(
                model.currentState != null,
                ToasterStateModel.class,
                model,
                "currentstate is null");
        ret &= InvariantChecking.checkGlassBoxInvariant(
                model.currentBrowningLevel != null,
                ToasterStateModel.class,
                model,
                "currentbrowningLevel is null");
        return ret;
    }

    protected static boolean blackBoxInvariant(ToasterStateModel model) {
        assert model != null:
                new NeoSim4JavaException("model is null");

        boolean ret = true;
        ret &= InvariantChecking.checkBlackBoxInvariant(
                model.MIL_URI != null,
                ToasterStateModel.class,
                model,
                "MIL_URI is null");
        ret &= InvariantChecking.checkBlackBoxInvariant(
                model.MIL_RT_URI != null,
                ToasterStateModel.class,
                model,
                "MIL_RT_URI is null");
        ret &= InvariantChecking.checkBlackBoxInvariant(
                model.SIL_URI != null,
                ToasterStateModel.class,
                model,
                "SIL_URI is null");
        return ret;
    }

    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------

    public ToasterStateModel(String uri, TimeUnit simulatedTimeUnit, AtomicSimulatorI simulationEngine) {
        super(uri, simulatedTimeUnit, simulationEngine);
        this.getSimulationEngine().setLogger(new StandardLogger());

        assert glassBoxInvariant(this):
                new NeoSim4JavaException("ToasterStateModel constructor glass-box invariant is false");
        assert blackBoxInvariant(this):
                new NeoSim4JavaException("ToasterStateModel constructor black-box invariant is false");
    }

    // -------------------------------------------------------------------------
    // Methods
    // -------------------------------------------------------------------------
    public void setToasterState(ToasterState state, Time t) {
        this.currentState = state;

        assert	glassBoxInvariants(this) :
                new AssertionError("White-box invariants violation!");
        assert	blackBoxInvariants(this) :
                new AssertionError("Black-box invariants violation!");    }

    public ToasterState getToasterState() {
        return this.currentState;
    }

    public void setToasterBrowningLevel(ToasterBrowningLevel bl, Time t) {
        this.currentBrowningLevel = bl;

        assert	glassBoxInvariants(this) :
                new AssertionError("White-box invariants violation!");
        assert	blackBoxInvariants(this) :
                new AssertionError("Black-box invariants violation!");
    }

    public ToasterBrowningLevel getToasterBrowningLevel() {
        return this.currentBrowningLevel;
    }

    @Override
    public Duration		timeAdvance()
    {
        if (this.toBeReemitted == null) {
            return Duration.INFINITY;
        } else {
            return Duration.zero(getSimulatedTimeUnit());
        }
    }

    @Override
    public ArrayList<EventI> output()
    {
        if (this.toBeReemitted != null) {
            ArrayList<EventI> ret = new ArrayList<EventI>();
            ret.add(this.toBeReemitted);
            this.toBeReemitted = null;
            return ret;
        } else {
            return null;
        }
    }

    @Override
    public void			userDefinedExternalTransition(Duration elapsedTime)
    {
        super.userDefinedExternalTransition(elapsedTime);

        // get the vector of current external events
        ArrayList<EventI> currentEvents = this.getStoredEventAndReset();
        // when this method is called, there is at least one external event,
        // and for the heater model, there will be exactly one by
        // construction.
        assert	currentEvents != null && currentEvents.size() == 1;

        this.toBeReemitted = (Event) currentEvents.get(0);
        assert	this.toBeReemitted instanceof ToasterEventI;
        this.toBeReemitted.executeOn(this);

        assert	glassBoxInvariants(this) :
                new NeoSim4JavaException(
                        "ToasterStateModel.glassBoxInvariants(this)");
        assert	blackBoxInvariants(this) :
                new NeoSim4JavaException(
                        "ToasterStateModel.blackBoxInvariants(this)");
    }

    @Override
    public void			endSimulation(Time endTime)
    {
        this.logMessage("simulation ends.");
    }

    // -------------------------------------------------------------------------
    // Optional DEVS simulation protocol: simulation run parameters
    // -------------------------------------------------------------------------

    /**
     * @see fr.sorbonne_u.devs_simulation.models.interfaces.ModelI#setSimulationRunParameters(java.util.Map)
     */
    @Override
    public void			setSimulationRunParameters(
            Map<String, Object> simParams
                                                     ) throws MissingRunParameterException
    {
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
