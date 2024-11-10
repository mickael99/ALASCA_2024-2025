package fr.sorbonne_u.components.equipments.smartLighting.mil;

import fr.sorbonne_u.components.equipments.hem.mil.HEM_ReportI;
import fr.sorbonne_u.components.equipments.smartLighting.mil.events.*;
import fr.sorbonne_u.components.utils.Electricity;
import fr.sorbonne_u.devs_simulation.exceptions.MissingRunParameterException;
import fr.sorbonne_u.devs_simulation.hioa.annotations.ExportedVariable;
import fr.sorbonne_u.devs_simulation.hioa.annotations.ModelExportedVariable;
import fr.sorbonne_u.devs_simulation.hioa.annotations.ModelImportedVariable;
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
        TurnOffSmartLighting.class,
        IncreaseLighting.class,
        DecreaseLighting.class,
        StopAdjustingLighting.class,
        TurnOnSmartLighting.class,
        SetPowerSmartLighting.class
})
@ModelExportedVariable(name = "currentIntensity", type = Double.class)
@ModelExportedVariable(name = "currentPowerLevel", type = Double.class)
// -------------------------------------------------------------------------
public class SmartLightingElectricityModel extends AtomicHIOA {

    // ------------------------------------------------------------------------
    // Inner classes and types
    // ------------------------------------------------------------------------

    public enum State {
        ON, OFF, INCREASE, DECREASE
    }

    // ------------------------------------------------------------------------
    // Constants and variables
    // ------------------------------------------------------------------------

    private static final long serialVersionUID = 1L;
    public static final String URI = SmartLightingElectricityModel.class.getSimpleName();
    public static double NOT_LIGHTING_POWER = 1.0;
    public static double MAX_LIGHTING_POWER = 500.0;
    protected static double TENSION = 220.0;

    protected State currentState = State.OFF;

    protected boolean comsumptionHasChanged = false;
    protected double totalConsumption;

    // -------------------------------------------------------------------------
    // HIOA model variables
    // -------------------------------------------------------------------------

    @ExportedVariable(type = Double.class)
    protected final Value<Double> currentPowerLevel =
            new Value<Double>(this);

    @ExportedVariable(type = Double.class)
    protected final Value<Double> currentIntensity =
            new Value<Double>(this);

    // -------------------------------------------------------------------------
    // Invariants
    // -------------------------------------------------------------------------

    protected static boolean glassBoxInvariants(SmartLightingElectricityModel instance) {
        assert instance != null :
                new AssertionError("Precondition violation: instance != null");

        boolean ret = true;
        ret &= InvariantChecking.checkGlassBoxInvariant(
                NOT_LIGHTING_POWER >= 0.0,
                SmartLightingElectricityModel.class,
                instance,
                "NOT_LIGHTING_POWER >= 0.0");
        ret &= InvariantChecking.checkGlassBoxInvariant(
                MAX_LIGHTING_POWER >= NOT_LIGHTING_POWER,
                SmartLightingElectricityModel.class,
                instance,
                "MAX_LIGHTING_POWER >= NOT_LIGHTING_POWER");
        ret &= InvariantChecking.checkGlassBoxInvariant(
                MAX_LIGHTING_POWER >= 0.0,
                SmartLightingElectricityModel.class,
                instance,
                "MAX_LIGHTING_POWER >= 0.0");
        ret &= InvariantChecking.checkGlassBoxInvariant(
                TENSION >= 0.0,
                SmartLightingElectricityModel.class,
                instance,
                "TENSION >= 0.0");
        ret &= InvariantChecking.checkGlassBoxInvariant(
                instance.totalConsumption >= 0.0,
                SmartLightingElectricityModel.class,
                instance,
                "instance.totalConsumption >= 0.0");
        ret &= InvariantChecking.checkGlassBoxInvariant(
                instance.currentState != null,
                SmartLightingElectricityModel.class,
                instance,
                "instance.currentPowerLevel.v >= 0.0");
        ret &= InvariantChecking.checkGlassBoxInvariant(
                instance.currentPowerLevel == null ||
                    (!instance.currentPowerLevel.isInitialised() ||
                        instance.currentPowerLevel.getValue() >= 0.0),
                SmartLightingElectricityModel.class,
                instance,
                "instance.currentPowerLevel == null ||" +
                "                    (!instance.currentPowerLevel.isInitialised() ||" +
                "                        instance.currentPowerLevel.getValue() >= 0.0)");
        return ret;
    }

    protected static boolean blackBoxInvariants(SmartLightingElectricityModel instance) {
        assert instance != null :
                new AssertionError("Precondition violation: instance != null");

        boolean ret = true;
        ret &= InvariantChecking.checkBlackBoxInvariant(
                URI != null && !URI.isEmpty(),
                SmartLightingElectricityModel.class,
                instance,
                "URI != null && !URI.isEmpty()");
        ret &= InvariantChecking.checkBlackBoxInvariant(
                NOT_LIGHTING_POWER_RUNPNAME != null
                    && !NOT_LIGHTING_POWER_RUNPNAME.isEmpty(),
                SmartLightingElectricityModel.class,
                instance,
                "NOT_LIGHTING_POWER_RUNPNAME != null &&" +
                    " !NOT_LIGHTING_POWER_RUNPNAME.isEmpty()"
               );
        ret &= InvariantChecking.checkBlackBoxInvariant(
                MAX_LIGHTING_POWER_RUNPNAME != null
                    && !MAX_LIGHTING_POWER_RUNPNAME.isEmpty(),
                SmartLightingElectricityModel.class,
                instance,
                "MAX_LIGHTING_POWER_RUNPNAME != null &&" +
                    " !MAX_LIGHTING_POWER_RUNPNAME.isEmpty()"
               );
        ret &= InvariantChecking.checkBlackBoxInvariant(
                TENSION_RUNPNAME != null && !TENSION_RUNPNAME.isEmpty(),
                SmartLightingElectricityModel.class,
                instance,
                "TENSION_RUNPNAME != null && !TENSION_RUNPNAME.isEmpty()"
               );
        return ret;
    }

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------

    public SmartLightingElectricityModel(
             String uri,
             TimeUnit simulatedTimeUnit,
             AtomicSimulatorI simulationEngine
            ) throws Exception {
        super(uri, simulatedTimeUnit, simulationEngine);
        this.getSimulationEngine().setLogger(new StandardLogger());

        assert  glassBoxInvariants(this) :
                new AssertionError("White box invariants violation!");
        assert  blackBoxInvariants(this) :
                new AssertionError("Black box invariants violation!");
    }

    // ------------------------------------------------------------------------
    // Methods
    // ------------------------------------------------------------------------

    public void setState(State state, Time time) {
        State old = this.currentState;
        this.currentState = state;
        if (old != state) {
            this.comsumptionHasChanged = true;
        }

        assert glassBoxInvariants(this) :
                new AssertionError("White box invariants violation!");
        assert blackBoxInvariants(this) :
                new AssertionError("Black box invariants violation!");
    }

    public State getState() {
        return this.currentState;
    }

    public void setCurrentLightingPower(double newPower, Time time) {
        assert newPower >= 0.0 &&
                newPower <= MAX_LIGHTING_POWER :
                new AssertionError("Precondition violation: newPower >= 0.0 &&" +
                                   "newPower <= MAX_LIGHTING_POWER"+
                                   "but new power = " + newPower);

        double oldPower = this.currentPowerLevel.getValue();
        this.currentPowerLevel.setNewValue(newPower, time);
        if (oldPower != newPower) {
            this.comsumptionHasChanged = true;
        }

        assert glassBoxInvariants(this) :
                new AssertionError("White box invariants violation!");
        assert blackBoxInvariants(this) :
                new AssertionError("Black box invariants violation!");
    }

    // -------------------------------------------------------------------------
    // DEVS simulation protocol
    // -------------------------------------------------------------------------

    /**
     * @see fr.sorbonne_u.devs_simulation.hioa.models.AtomicHIOA#initialiseState(fr.sorbonne_u.devs_simulation.models.time.Time)
     */
    @Override
    public void initialiseState(Time initialTime) {
        super.initialiseState(initialTime);

        this.currentState = State.OFF;
        this.comsumptionHasChanged = false;
        this.totalConsumption = 0.0;

        this.getSimulationEngine().toggleDebugMode();
        this.logMessage("simulation begins.\n");

        assert glassBoxInvariants(this) :
                new AssertionError("White box invariants violation!");
        assert blackBoxInvariants(this) :
                new AssertionError("Black box invariants violation!");
    }

    /**
     * @see fr.sorbonne_u.devs_simulation.hioa.models.interfaces.VariableInitialisationI#useFixpointInitialiseVariables()
     */
    @Override
    public boolean		useFixpointInitialiseVariables()
    {
        return true;
    }

    /**
     * @see fr.sorbonne_u.devs_simulation.hioa.models.interfaces.VariableInitialisationI#fixpointInitialiseVariables()
     */
    @Override
    public Pair<Integer, Integer> fixpointInitialiseVariables()
    {
        Pair<Integer, Integer> ret = null;

        if(!this.currentIntensity.isInitialised() ||
                !this.currentPowerLevel.isInitialised()) {
            this.currentIntensity.initialise(0.0);
            this.currentPowerLevel.initialise(MAX_LIGHTING_POWER);

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

        assert glassBoxInvariants(this) :
                new AssertionError("White box invariants violation!");
        assert blackBoxInvariants(this) :
                new AssertionError("Black box invariants violation!");

        return ret;
    }

    /**
     * @see fr.sorbonne_u.devs_simulation.models.interfaces.AtomicModelI#output()
     */
    @Override
    public ArrayList<EventI> output()
    {
        return null;
    }

    /**
     * @see fr.sorbonne_u.devs_simulation.models.interfaces.ModelI#timeAdvance()
     */
    @Override
    public Duration timeAdvance()
    {
        Duration ret = null;

        if (this.comsumptionHasChanged) {
            this.comsumptionHasChanged = false;
            ret = Duration.zero(this.getSimulatedTimeUnit());
        } else {
            ret = Duration.INFINITY;
        }

        assert	glassBoxInvariants(this) :
                new AssertionError("White-box invariants violation!");
        assert	blackBoxInvariants(this) :
                new AssertionError("Black-box invariants violation!");

        return ret;
    }

    /**
     * @see fr.sorbonne_u.devs_simulation.models.AtomicModel#userDefinedInternalTransition(fr.sorbonne_u.devs_simulation.models.time.Duration)
     */
    @Override
    public void			userDefinedInternalTransition(Duration elapsedTime)
    {
        super.userDefinedInternalTransition(elapsedTime);

        Time t = this.getCurrentStateTime();
        if (this.currentState == State.ON) {
            this.currentIntensity.setNewValue(
                    SmartLightingElectricityModel.NOT_LIGHTING_POWER/
                        SmartLightingElectricityModel.TENSION,
                    t);
        } else if (this.currentState == State.INCREASE ||
                this.currentState == State.DECREASE) {
            this.currentIntensity.setNewValue(
                    this.currentPowerLevel.getValue()/
                    SmartLightingElectricityModel.TENSION,
                    t);
        } else {
            assert	this.currentState == State.OFF;
            this.currentIntensity.setNewValue(0.0, t);
        }

        StringBuffer sb = new StringBuffer("new consumption: ");
        sb.append(this.currentIntensity.getValue());
        sb.append(" amperes at ");
        sb.append(this.currentIntensity.getTime());
        sb.append(" seconds.\n");
        this.logMessage(sb.toString());

        assert	glassBoxInvariants(this) :
                new AssertionError("White-box invariants violation!");
        assert	blackBoxInvariants(this) :
                new AssertionError("Black-box invariants violation!");
    }

    /**
     * @see fr.sorbonne_u.devs_simulation.models.AtomicModel#userDefinedExternalTransition(fr.sorbonne_u.devs_simulation.models.time.Duration)
     */
    @Override
    public void userDefinedExternalTransition(Duration elapsedTime) {
        super.userDefinedExternalTransition(elapsedTime);

        ArrayList<EventI> currentEvents = this.getStoredEventAndReset();

        assert	currentEvents != null && currentEvents.size() == 1;

        Event ce = (Event) currentEvents.get(0);
        assert	ce instanceof SmartLightingEventI;

        this.totalConsumption +=
                Electricity.computeConsumption(
                        elapsedTime,
                        TENSION*this.currentIntensity.getValue());

        StringBuffer sb = new StringBuffer("execute the external event: ");
        sb.append(ce.eventAsString());
        sb.append(".\n");
        this.logMessage(sb.toString());

        ce.executeOn(this);


        assert	glassBoxInvariants(this) :
                new AssertionError("White-box invariants violation!");
        assert	blackBoxInvariants(this) :
                new AssertionError("Black-box invariants violation!");
    }

    /**
     * @see fr.sorbonne_u.devs_simulation.hioa.models.AtomicHIOA#endSimulation(fr.sorbonne_u.devs_simulation.models.time.Time)
     */
    @Override
    public void			endSimulation(Time endTime)
    {
        Duration d = endTime.subtract(this.getCurrentStateTime());
        this.totalConsumption +=
                Electricity.computeConsumption(
                        d,
                        TENSION*this.currentIntensity.getValue());

        this.logMessage("simulation ends.\n");
        super.endSimulation(endTime);
    }

    // -------------------------------------------------------------------------
    // Optional DEVS simulation protocol: simulation run parameters
    // -------------------------------------------------------------------------

    public static final String	NOT_LIGHTING_POWER_RUNPNAME = "NOT_LIGHTING_POWER";
    public static final String	MAX_LIGHTING_POWER_RUNPNAME = "MAX_LIGHTING_POWER";
    public static final String	TENSION_RUNPNAME = "TENSION";

    /**
     * @see fr.sorbonne_u.devs_simulation.models.interfaces.ModelI#setSimulationRunParameters(Map)
     */
    @Override
    public void			setSimulationRunParameters(
            Map<String, Object> simParams
                                                     ) throws MissingRunParameterException
    {
        super.setSimulationRunParameters(simParams);

        String notHeatingName =
                ModelI.createRunParameterName(getURI(), NOT_LIGHTING_POWER_RUNPNAME);
        if (simParams.containsKey(notHeatingName)) {
            NOT_LIGHTING_POWER = (double) simParams.get(notHeatingName);
        }
        String heatingName =
                ModelI.createRunParameterName(getURI(), MAX_LIGHTING_POWER_RUNPNAME);
        if (simParams.containsKey(heatingName)) {
            MAX_LIGHTING_POWER = (double) simParams.get(heatingName);
        }
        String tensionName =
                ModelI.createRunParameterName(getURI(), TENSION_RUNPNAME);
        if (simParams.containsKey(tensionName)) {
            TENSION = (double) simParams.get(tensionName);
        }

        assert	glassBoxInvariants(this) :
                new AssertionError("White-box invariants violation!");
        assert	blackBoxInvariants(this) :
                new AssertionError("Black-box invariants violation!");
    }

    // -------------------------------------------------------------------------
    // Optional DEVS simulation protocol: simulation report
    // -------------------------------------------------------------------------
    public static class SmartLightingElectricityReport
            implements SimulationReportI, HEM_ReportI{
        private static final long serialVersionUID = 1L;
        protected String	modelURI;
        protected double	totalConsumption;

        public SmartLightingElectricityReport(
                String modelURI,
                double totalConsumption
                 ) {
            super();
            this.modelURI = modelURI;
            this.totalConsumption = totalConsumption;
        }

        @Override
        public String getModelURI() {
            return this.modelURI;
        }

        @Override
        public String printout(String indent){
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

    /**
     * @see fr.sorbonne_u.devs_simulation.models.interfaces.ModelI#getFinalReport()
     */
    @Override
    public SimulationReportI	getFinalReport()
    {
        return new SmartLightingElectricityReport(
                this.getURI(),
                this.totalConsumption
        );
    }
}
// -----------------------------------------------------------------------------
