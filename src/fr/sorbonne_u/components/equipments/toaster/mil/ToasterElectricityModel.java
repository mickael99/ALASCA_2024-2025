package fr.sorbonne_u.components.equipments.toaster.mil;

import fr.sorbonne_u.components.equipments.toaster.mil.events.SetToasterBrowningLevel;
import fr.sorbonne_u.components.equipments.toaster.mil.events.ToasterEventI;
import fr.sorbonne_u.components.equipments.toaster.mil.events.TurnOffToaster;
import fr.sorbonne_u.components.equipments.toaster.mil.events.TurnOnToaster;
import fr.sorbonne_u.components.hem2024e2.HEM_ReportI;
import fr.sorbonne_u.components.utils.Electricity;
import fr.sorbonne_u.devs_simulation.exceptions.MissingRunParameterException;
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
import fr.sorbonne_u.devs_simulation.utils.InvariantChecking;
import fr.sorbonne_u.devs_simulation.utils.Pair;
import fr.sorbonne_u.devs_simulation.utils.StandardLogger;

import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@ModelExternalEvents(imported = {
        TurnOnToaster.class,
        TurnOffToaster.class,
        SetToasterBrowningLevel.class
})
@ModelExportedVariable(name = "currentIntensity", type = Double.class)
@ModelExportedVariable(name = "currentBrowningLevel", type = ToasterElectricityModel.ToasterBrowningLevel.class)
public class ToasterElectricityModel extends AtomicHIOA {

    // -------------------------------------------------------------------------
    // Inner classes and types
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

    //-------------------------------------------------------------------------
    // Constants and variables
    //-------------------------------------------------------------------------

    private static final long serialVersionUID = 1L;
    public static final String URI = ToasterElectricityModel.class.getSimpleName();
    protected static double DEFROSING_CONSUMPTION = 400; // Watts
    protected static double LOW_CONSUMPTION = 600; // Watts
    protected static double MEDIUM_CONSUMPTION = 1000; // Watts
    protected static double HIGH_CONSUMPTION = 1500; // Watts
    protected static double TENSION = 220; // Volts

    protected ToasterState currentState = ToasterState.OFF;

    protected boolean consumptionHasChanged = false;
    protected double totalConsumption;

    //-------------------------------------------------------------------------
    // HIOA model variables
    //-------------------------------------------------------------------------

    @ExportedVariable(type = ToasterBrowningLevel.class)
    protected final Value<ToasterBrowningLevel> currentBrowningLevel = new Value<ToasterBrowningLevel>(this);

    @ExportedVariable(type = Double.class)
    protected final Value<Double> currentIntensity = new Value<Double>(this);

    //-------------------------------------------------------------------------
    // Invariants
    //-------------------------------------------------------------------------

    protected static boolean glassBoxInvariants(ToasterElectricityModel instance) {
        assert instance != null :
                new AssertionError("Precondition violation: instance != null");
        boolean ret = true;
        ret &= InvariantChecking.checkGlassBoxInvariant(
                DEFROSING_CONSUMPTION > 0.0,
                ToasterElectricityModel.class,
                instance,
                "DEFROSING_CONSUMPTION > 0.0"
        );
        ret &= InvariantChecking.checkGlassBoxInvariant(
                LOW_CONSUMPTION >= DEFROSING_CONSUMPTION,
                ToasterElectricityModel.class,
                instance,
                "LOW_CONSUMPTION >= DEFROSING_CONSUMPTION"
        );
        ret &= InvariantChecking.checkGlassBoxInvariant(
                MEDIUM_CONSUMPTION >= LOW_CONSUMPTION,
                ToasterElectricityModel.class,
                instance,
                "MEDIUM_CONSUMPTION >= LOW_CONSUMPTION"
        );
        ret &= InvariantChecking.checkGlassBoxInvariant(
                HIGH_CONSUMPTION >= MEDIUM_CONSUMPTION,
                ToasterElectricityModel.class,
                instance,
                "HIGH_CONSUMPTION >= MEDIUM_CONSUMPTION"
        );
        ret &= InvariantChecking.checkGlassBoxInvariant(
                TENSION > 0.0,
                ToasterElectricityModel.class,
                instance,
                "TENSION > 0.0"
        );
        ret &= InvariantChecking.checkGlassBoxInvariant(
                instance.totalConsumption >= 0.0,
                ToasterElectricityModel.class,
                instance,
                "instance.totalConsumption >= 0.0"
        );
        ret &= InvariantChecking.checkGlassBoxInvariant(
                instance.currentState != null,
                ToasterElectricityModel.class,
                instance,
                "instance.currentState != null"
        );
        ret &= InvariantChecking.checkGlassBoxInvariant(
                instance.currentBrowningLevel != null,
                ToasterElectricityModel.class,
                instance,
                "instance.currentBrowningLevel != null"
        );
        ret &= InvariantChecking.checkGlassBoxInvariant(
                !instance.currentIntensity.isInitialised() ||
                        instance.currentIntensity.getValue() >= 0.0,
                ToasterElectricityModel.class,
                instance,
                "!currentIntensity.isInitialised() || "
                        + "currentIntensity.getValue() >= 0.0");
        return ret;
    }

    protected static boolean blackBoxInvariants(ToasterElectricityModel instance) {
        assert instance != null :
                new AssertionError("Precondition violation: instance != null");
        boolean ret = true;
        ret &= InvariantChecking.checkBlackBoxInvariant(
                URI != null && !URI.isEmpty(),
                ToasterElectricityModel.class,
                instance,
                "URI != null && !URI.isEmpty()"
        );
        ret &= InvariantChecking.checkBlackBoxInvariant(
                DEFROSING_CONSUMPTION_RPNAME != null && !DEFROSING_CONSUMPTION_RPNAME.isEmpty(),
                ToasterElectricityModel.class,
                instance,
                "DEFROSING_CONSUMPTION_RPNAME != null && !DEFROSING_CONSUMPTION_RPNAME.isEmpty()"
        );
        ret &= InvariantChecking.checkBlackBoxInvariant(
                LOW_CONSUMPTION_RPNAME != null && !LOW_CONSUMPTION_RPNAME.isEmpty(),
                ToasterElectricityModel.class,
                instance,
                "LOW_CONSUMPTION_RPNAME != null && !LOW_CONSUMPTION_RPNAME.isEmpty()"
        );
        ret &= InvariantChecking.checkBlackBoxInvariant(
                MEDIUM_CONSUMPTION_RPNAME != null && !MEDIUM_CONSUMPTION_RPNAME.isEmpty(),
                ToasterElectricityModel.class,
                instance,
                "MEDIUM_CONSUMPTION_RPNAME != null && !MEDIUM_CONSUMPTION_RPNAME.isEmpty()"
        );
        ret &= InvariantChecking.checkBlackBoxInvariant(
                HIGH_CONSUMPTION_RPNAME != null && !HIGH_CONSUMPTION_RPNAME.isEmpty(),
                ToasterElectricityModel.class,
                instance,
                "HIGH_CONSUMPTION_RPNAME != null && !HIGH_CONSUMPTION_RPNAME.isEmpty()"
        );
        ret &= InvariantChecking.checkBlackBoxInvariant(
                TENSION_RPNAME != null && !TENSION_RPNAME.isEmpty(),
                ToasterElectricityModel.class,
                instance,
                "TENSION_RPNAME != null && !TENSION_RPNAME.isEmpty()"
        );

        return ret;
    }

    //-------------------------------------------------------------------------
    // Constructors
    //-------------------------------------------------------------------------

    public ToasterElectricityModel(String uri,
                                   TimeUnit simulatedTimeUnit,
                                   AtomicSimulatorI simulationEngine) throws Exception {
        super(uri, simulatedTimeUnit, simulationEngine);
        this.getSimulationEngine().setLogger(new StandardLogger());

        assert glassBoxInvariants(this) :
                new AssertionError("Glass-box invariants violation!");
        assert blackBoxInvariants(this) :
                new AssertionError("Black-box invariant violation!");
    }

    //-------------------------------------------------------------------------
    // Methods
    //-------------------------------------------------------------------------

    public void setToasterState(ToasterState state, Time t) {
        ToasterState old = this.currentState;
        this.currentState = state;
        if (old != state) {
            this.consumptionHasChanged = true;
        }

        assert	glassBoxInvariants(this) :
                new AssertionError("White-box invariants violation!");
        assert	blackBoxInvariants(this) :
                new AssertionError("Black-box invariants violation!");    }

    public ToasterState getToasterState() {
        return this.currentState;
    }

    public void setToasterBrowningLevel(ToasterBrowningLevel bl, Time t) {
        ToasterBrowningLevel old = this.currentBrowningLevel.getValue();
        this.currentBrowningLevel.setNewValue(bl, t);
        if (old != bl) {
            this.consumptionHasChanged = true;
        }

        assert	glassBoxInvariants(this) :
                new AssertionError("White-box invariants violation!");
        assert	blackBoxInvariants(this) :
                new AssertionError("Black-box invariants violation!");
    }

    public ToasterBrowningLevel getToasterBrowningLevel() {
        return this.currentBrowningLevel.getValue();
    }

    public void toggleConsumptionHasChanged() {
        if (this.consumptionHasChanged) {
            this.consumptionHasChanged = false;
        } else {
            this.consumptionHasChanged = true;
        }
    }

    // -------------------------------------------------------------------------
    // DEVS simulation protocol
    // -------------------------------------------------------------------------

    @Override
    public void initialiseState(Time startTime) {
        super.initialiseState(startTime);

        this.currentState = ToasterState.OFF;
        this.totalConsumption = 0.0;
        this.consumptionHasChanged = false;

        this.getSimulationEngine().toggleDebugMode();
        this.logMessage("simulation begins./n");

        assert glassBoxInvariants(this) :
                new AssertionError("Glass-box invariants violation!");
        assert blackBoxInvariants(this) :
                new AssertionError("Black-box invariant violation!");
    }

    @Override
    public void initialiseVariables() {
        this.currentIntensity.initialise(0.0);
        this.toggleConsumptionHasChanged();

        assert glassBoxInvariants(this) :
                new AssertionError("Glass-box invariants violation!");
        assert blackBoxInvariants(this) :
                new AssertionError("Black-box invariant violation!");
    }

    @Override
    public boolean		useFixpointInitialiseVariables()
    {
        return true;
    }

    //TODO: Ask about this method
    @Override
    public Pair<Integer, Integer> fixpointInitialiseVariables()
    {
        Pair<Integer, Integer> ret = null;

        if (!this.currentIntensity.isInitialised() ||
                !this.currentBrowningLevel.isInitialised()) {

            this.currentIntensity.initialise(0.0);
            this.currentBrowningLevel.initialise(ToasterBrowningLevel.DEFROST);

            StringBuffer sb = new StringBuffer("new consumption: ");
            sb.append(this.currentIntensity.getValue());
            sb.append(" amperes at ");
            sb.append(this.currentIntensity.getTime());
            sb.append(" seconds.\n");
            this.logMessage(sb.toString());
            ret = new Pair<>(2, 0);
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
    public ArrayList<EventI> output() {
        // the model does not export events.
        return null;
    }

    @Override
    public Duration timeAdvance() {
        Duration ret = null;
        // to trigger an internal transition after an external transition, the
        // variable consumptionHasChanged is set to true, hence when it is true
        // return a zero delay otherwise return an infinite delay (no internal
        // transition expected)
        if (this.consumptionHasChanged) {
            // after triggering the internal transition, toggle the boolean
            // to prepare for the next internal transition.
            this.toggleConsumptionHasChanged();
            ret = new Duration(0.0, this.getSimulatedTimeUnit());
        } else {
            ret = Duration.INFINITY;
        }

        assert glassBoxInvariants(this) :
                new AssertionError("Glass-box invariants violation!");
        assert blackBoxInvariants(this) :
                new AssertionError("Black-box invariant violation!");

        return ret;
    }

    @Override
    public void userDefinedInternalTransition(Duration elapsedTime) {

        super.userDefinedInternalTransition(elapsedTime);

        Time t = this.getCurrentStateTime();
        if (this.currentState == ToasterState.ON) {
            if (this.currentBrowningLevel.getValue() == ToasterBrowningLevel.DEFROST) {
                this.currentIntensity.setNewValue(DEFROSING_CONSUMPTION / TENSION, t);
            } else if (this.currentBrowningLevel.getValue() == ToasterBrowningLevel.LOW) {
                this.currentIntensity.setNewValue(LOW_CONSUMPTION / TENSION, t);
            } else if (this.currentBrowningLevel.getValue() == ToasterBrowningLevel.MEDIUM) {
                this.currentIntensity.setNewValue(MEDIUM_CONSUMPTION / TENSION, t);
            } else if (this.currentBrowningLevel.getValue() == ToasterBrowningLevel.HIGH) {
                this.currentIntensity.setNewValue(HIGH_CONSUMPTION / TENSION, t);
            }
        } else {
            assert this.currentState == ToasterState.OFF;
            this.currentIntensity.setNewValue(0.0, t);
        }
        // Tracing
        StringBuffer message =
                new StringBuffer("executes an internal transition ");
        message.append("with current consumption ");
        message.append(this.currentIntensity.getValue());
        message.append(" at ");
        message.append(this.currentIntensity.getTime());
        message.append(".\n");
        this.logMessage(message.toString());

        assert glassBoxInvariants(this) :
                new AssertionError("Glass-box invariants violation!");
        assert blackBoxInvariants(this) :
                new AssertionError("Black-box invariant violation!");
    }

    @Override
    public void userDefinedExternalTransition(Duration elapsedTime) {

        super.userDefinedExternalTransition(elapsedTime);

        // get the vector of currently received external events
        ArrayList<EventI> currentEvents = this.getStoredEventAndReset();
        // when this method is called, there is at least one external event,
        // and for the current hair dryer model, there must be exactly one by
        // construction.
        assert currentEvents != null && currentEvents.size() == 1;

        Event ce = (Event) currentEvents.get(0);
        assert ce instanceof ToasterEventI;

        // optional: compute the total consumption (in kwh) for the simulation
        // report.
        this.totalConsumption +=
                Electricity.computeConsumption(
                        elapsedTime,
                        TENSION * this.currentIntensity.getValue());

        // Tracing
        StringBuffer sb = new StringBuffer("execute the external event: ");
        sb.append(ce.eventAsString());
        sb.append(".\n");
        this.logMessage(sb.toString());

        ce.executeOn(this);

        assert glassBoxInvariants(this) :
                new AssertionError("Glass-box invariants violation!");
        assert blackBoxInvariants(this) :
                new AssertionError("Black-box invariant violation!");
    }

    public void endSimulation(Time endTime) {
        Duration d = endTime.subtract(this.getCurrentStateTime());
        this.totalConsumption +=
                Electricity.computeConsumption(
                        d,
                        TENSION * this.currentIntensity.getValue());

        this.logMessage("simulation ends.\n");
        super.endSimulation(endTime);
    }

    // -------------------------------------------------------------------------
    // Optional DEVS simulation protocol: simulation run parameters
    // -------------------------------------------------------------------------
    public static final String DEFROSING_CONSUMPTION_RPNAME = URI + "DEFROSING_CONSUMPTION";
    public static final String LOW_CONSUMPTION_RPNAME = URI + "LOW_CONSUMPTION";
    public static final String MEDIUM_CONSUMPTION_RPNAME = URI + "MEDIUM_CONSUMPTION";
    public static final String HIGH_CONSUMPTION_RPNAME = URI + "HIGH_CONSUMPTION";
    public static final String TENSION_RPNAME = URI + "TENSION";

    @Override
    public void setSimulationRunParameters(Map<String, Object> simParams) throws MissingRunParameterException {
        super.setSimulationRunParameters(simParams);

        String defrosingName = ModelI.createRunParameterName(getURI(), DEFROSING_CONSUMPTION_RPNAME);
        if(simParams.containsKey(defrosingName)) {
            DEFROSING_CONSUMPTION = (double) simParams.get(defrosingName);
        }

        String lowName = ModelI.createRunParameterName(getURI(), LOW_CONSUMPTION_RPNAME);
        if(simParams.containsKey(lowName)) {
            LOW_CONSUMPTION = (double) simParams.get(lowName);
        }

        String mediumName = ModelI.createRunParameterName(getURI(), MEDIUM_CONSUMPTION_RPNAME);
        if(simParams.containsKey(mediumName)) {
            MEDIUM_CONSUMPTION = (double) simParams.get(mediumName);
        }

        String highName = ModelI.createRunParameterName(getURI(), HIGH_CONSUMPTION_RPNAME);
        if(simParams.containsKey(highName)) {
            HIGH_CONSUMPTION = (double) simParams.get(highName);
        }

        String tensionName = ModelI.createRunParameterName(getURI(), TENSION_RPNAME);
        if(simParams.containsKey(tensionName)) {
            TENSION = (double) simParams.get(tensionName);
        }

        assert glassBoxInvariants(this) :
                new AssertionError("Glass-box invariants violation!");
        assert blackBoxInvariants(this) :
                new AssertionError("Black-box invariant violation!");
    }

    // -------------------------------------------------------------------------
    // Optional DEVS simulation protocol: simulation report
    // -------------------------------------------------------------------------
    public static class ToasterElectricityReport implements SimulationReportI, HEM_ReportI {
        private static final long serialVersionUID = 1L;
        protected String modelURI;
        protected double totalConsumption; // in kwh

        public ToasterElectricityReport(String modelURI, double totalConsumption) {
            super();
            this.modelURI = modelURI;
            this.totalConsumption = totalConsumption;
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
            return ret.toString();
        }

        @Override
        public String getModelURI() {
            return this.modelURI;
        }
    }

    @Override
    public SimulationReportI getFinalReport(){
        return new ToasterElectricityReport(this.getURI(), this.totalConsumption);
    }
}
// -----------------------------------------------------------------------------
