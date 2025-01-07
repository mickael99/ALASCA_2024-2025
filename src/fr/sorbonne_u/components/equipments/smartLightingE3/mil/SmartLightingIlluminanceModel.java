package fr.sorbonne_u.components.equipments.smartLightingE3.mil;

import fr.sorbonne_u.components.cyphy.plugins.devs.AtomicSimulatorPlugin;
import fr.sorbonne_u.components.equipments.hem.mil.HEM_ReportI;
import fr.sorbonne_u.components.equipments.smartLightingE3.mil.events.DecreaseLighting;
import fr.sorbonne_u.components.equipments.smartLightingE3.mil.events.IncreaseLighting;
import fr.sorbonne_u.components.equipments.smartLightingE3.mil.events.SmartLightingEventI;
import fr.sorbonne_u.components.equipments.smartLightingE3.mil.events.StopAdjustingLighting;
import fr.sorbonne_u.devs_simulation.exceptions.MissingRunParameterException;
import fr.sorbonne_u.devs_simulation.exceptions.NeoSim4JavaException;
import fr.sorbonne_u.devs_simulation.hioa.annotations.ImportedVariable;
import fr.sorbonne_u.devs_simulation.hioa.annotations.InternalVariable;
import fr.sorbonne_u.devs_simulation.hioa.annotations.ModelImportedVariable;
import fr.sorbonne_u.devs_simulation.hioa.models.AtomicHIOA;
import fr.sorbonne_u.devs_simulation.hioa.models.vars.Value;
import fr.sorbonne_u.devs_simulation.models.annotations.ModelExternalEvents;
import fr.sorbonne_u.devs_simulation.models.events.Event;
import fr.sorbonne_u.devs_simulation.models.events.EventI;
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
        IncreaseLighting.class,
        DecreaseLighting.class,
        StopAdjustingLighting.class
})
@ModelImportedVariable(name = "externalIlluminance", type = Double.class)
@ModelImportedVariable(name = "currentPowerLevel", type = Double.class)
public class SmartLightingIlluminanceModel extends AtomicHIOA {

    // ------------------------------------------------------------------------
    // Inner classes and types
    // ------------------------------------------------------------------------
    public static enum State {
        DECREASE,
        INCREASE,
        ON,
    }

    // ------------------------------------------------------------------------
    // Constants and variables
    // ------------------------------------------------------------------------
    private static final long serialVersionUID = 1L;
    public static String URI = SmartLightingIlluminanceModel.class.getSimpleName();
    
    public static final String MIL_URI = SmartLightingIlluminanceModel.class.getSimpleName() + "-MIL";
    public static final String MIL_RT_URI = SmartLightingIlluminanceModel.class.getSimpleName() + "-MIL-RT";
    public static final String SIL_URI = SmartLightingIlluminanceModel.class.getSimpleName() + "-SIL";

    public static double INITIAL_ILLUMINANCE = 1000.0;
    // The tolerance for the update of the illuminance value.
    protected static double		ILLUMINANCE_UPDATE_TOLERANCE = 1.0;
    // The parameter for the transition between two illuminance values.
    protected static double     ILLUMINANCE_TRANSITION_PARAM = 0.05;
    protected static double		STEP = 60.0/3600.0;	// 60 seconds

    protected State currentState = State.ON;

    protected final Duration    integrationStep;
    protected double			illuminanceAcc;
    protected Time              start;
    protected double			meanIlluminance;

    // -------------------------------------------------------------------------
    // HIOA model variables
    // -------------------------------------------------------------------------
    @ImportedVariable(type = Double.class)
    protected Value<Double> externalIlluminance;

    @ImportedVariable(type = Double.class)
    protected Value<Double> currentPowerLevel;

    @InternalVariable(type = Double.class)
    protected final Value<Double> currentIlluminance = new Value<Double>(this);
    // ------------------------------------------------------------------------
    // Invariants
    // ------------------------------------------------------------------------
    protected static boolean glassBoxInvariants(
            SmartLightingIlluminanceModel instance
    ) {
        assert instance != null :
                new NeoSim4JavaException("SmartLightingIlluminanceModel: Precondition violation: instance != null");

        boolean ret = true;
        ret &= InvariantChecking.checkGlassBoxInvariant(
                ILLUMINANCE_UPDATE_TOLERANCE > 0.0,
                SmartLightingIlluminanceModel.class,
                instance,
                "ILLUMINANCE_UPDATE_TOLERANCE > 0.0"
               );
        ret &= InvariantChecking.checkGlassBoxInvariant(
                ILLUMINANCE_TRANSITION_PARAM > 0.0,
                SmartLightingIlluminanceModel.class,
                instance,
                "ILLUMINANCE_TRANSITION_PARAM > 0.0"
               );
        ret &= InvariantChecking.checkGlassBoxInvariant(
                STEP > 0.0,
                SmartLightingIlluminanceModel.class,
                instance,
                "STEP > 0.0"
               );
        ret &= InvariantChecking.checkGlassBoxInvariant(
                instance.currentState != null,
                SmartLightingIlluminanceModel.class,
                instance,
                "instance.currentState != null"
               );
        ret &= InvariantChecking.checkGlassBoxInvariant(
                !instance.isStateInitialised() || instance.start != null,
                SmartLightingIlluminanceModel.class,
                instance,
                "!isStateInitialised() || start != null"
               );
        ret &= InvariantChecking.checkGlassBoxInvariant(
                instance.currentIlluminance != null,
                SmartLightingIlluminanceModel.class,
                instance,
                "instance.currentIlluminance != null"
               );
        return ret;
    }

    protected static  boolean blackBoxInvariants(
            SmartLightingIlluminanceModel instance
    ) {
        assert instance != null :
                new NeoSim4JavaException("Precondition violation: instance != null");

        boolean ret = true;
        ret &= InvariantChecking.checkBlackBoxInvariant(
                MIL_URI != null && !MIL_URI.isEmpty(),
                SmartLightingIlluminanceModel.class,
                instance,
                "MIL_URI != null && !MIL_URI.isEmpty()"
               );
        ret &= InvariantChecking.checkBlackBoxInvariant(
                MIL_RT_URI != null && !MIL_RT_URI.isEmpty(),
                SmartLightingIlluminanceModel.class,
                instance,
                "MIL_RT_URI != null && !MIL_RT_URI.isEmpty()"
               );
        ret &= InvariantChecking.checkBlackBoxInvariant(
                SIL_URI != null && !SIL_URI.isEmpty(),
                SmartLightingIlluminanceModel.class,
                instance,
                "SIL_URI != null && !SIL_URI.isEmpty()"
               );
        ret &= InvariantChecking.checkBlackBoxInvariant(
                URI != null && !URI.isEmpty(),
                SmartLightingIlluminanceModel.class,
                instance,
                "URI != null && !URI.isEmpty()"
               );
        return ret;
    }

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------
    public				SmartLightingIlluminanceModel(
            String uri,
            TimeUnit simulatedTimeUnit,
            AtomicSimulatorI simulationEngine
           ) throws Exception
    {
        super(uri, simulatedTimeUnit, simulationEngine);
        this.integrationStep = new Duration(STEP, simulatedTimeUnit);
        this.getSimulationEngine().setLogger(new StandardLogger());

        assert	glassBoxInvariants(this) :
                new NeoSim4JavaException("SmartLightingIlluminanceModel: White-box invariants violation!");
        assert	blackBoxInvariants(this) :
                new NeoSim4JavaException("SmartLightingIlluminanceModel: Black-Box invariants violation!");
    }

    // ------------------------------------------------------------------------
    // Methods
    // ------------------------------------------------------------------------

    public void setState(State s){
        this.currentState = s;

        assert glassBoxInvariants(this) :
            new NeoSim4JavaException("SmartLightingIlluminanceModel: White-box invariants violation!");
        assert blackBoxInvariants(this) :
            new NeoSim4JavaException("SmartLightingIlluminanceModel: Black-Box invariants violation!");
    }

    public State getState(){
        return this.currentState;
    }

    protected double computeNewIlluminance(double deltaI){
        Time t = this.currentIlluminance.getTime();
        double oldI = this.currentIlluminance.evaluateAt(t);
        double newI = oldI;

        if(deltaI > ILLUMINANCE_UPDATE_TOLERANCE){
            newI = oldI + ILLUMINANCE_UPDATE_TOLERANCE;
        }
        this.illuminanceAcc += (oldI + newI) / 2.0 * deltaI;
        return newI;
    }

    // -------------------------------------------------------------------------
    // DEVS simulation protocol
    // -------------------------------------------------------------------------

    /**
     * @see AtomicHIOA#initialiseState(Time)
     */
    @Override
    public void initialiseState(Time initialTime)
    {
        this.illuminanceAcc = 0.0;
        this.start = initialTime;
        this.getSimulationEngine().toggleDebugMode();
        this.logMessage("simulation begins.\n");

        super.initialiseState(initialTime);

        assert	glassBoxInvariants(this) :
                new NeoSim4JavaException("SmartLightingIlluminanceModel: White-box invariants violation!");
        assert	blackBoxInvariants(this) :
                new NeoSim4JavaException("SmartLightingIlluminanceModel: Black-Box invariants violation!");
    }

    /**
     * @see fr.sorbonne_u.devs_simulation.models.interfaces.ModelI#isStateInitialised()
     */
    @Override
    public boolean		useFixpointInitialiseVariables()
    {
        return true;
    }

    /**
     * @see fr.sorbonne_u.devs_simulation.models.interfaces.ModelI#initialiseVariables()
     */
    @Override
    public Pair<Integer, Integer> fixpointInitialiseVariables()
    {
        int justInitialised = 0;
        int notInitialisedYet = 0;

        if (!this.currentIlluminance.isInitialised() &&
            this.externalIlluminance.isInitialised()) {
            this.currentIlluminance.initialise(
                    INITIAL_ILLUMINANCE * ILLUMINANCE_TRANSITION_PARAM
                    );
            justInitialised++;
        } else if (!this.currentIlluminance.isInitialised()) {
            notInitialisedYet++;
        }

        assert	glassBoxInvariants(this) :
                new NeoSim4JavaException("SmartLightingIlluminanceModel: White-box invariants violation!");
        assert	blackBoxInvariants(this) :
                new NeoSim4JavaException("SmartLightingIlluminanceModel: Black-Box invariants violation!");

        return new Pair<>(justInitialised, notInitialisedYet);
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
    public Duration		timeAdvance()
    {
        return this.integrationStep;
    }

    /**
     * @see fr.sorbonne_u.devs_simulation.models.AtomicModel#userDefinedInternalTransition(Duration)
     */
    @Override
    public void			userDefinedInternalTransition(Duration elapsedTime)
    {
        double newTemp =
                this.computeNewIlluminance(elapsedTime.getSimulatedDuration());
        this.currentIlluminance.setNewValue(
                newTemp,
                new Time(this.getCurrentStateTime().getSimulatedTime(),
                         this.getSimulatedTimeUnit()));

        // Tracing
        String mark = "";
        if (this.currentState == State.ON) {
            mark = "(o)";
        } else if (this.currentState == State.DECREASE) {
            mark = "(-)";
        } else {
            mark = "(+)";
        }
        StringBuffer message = new StringBuffer();
        message.append(this.currentIlluminance.getTime().getSimulatedTime());
        message.append(mark);
        message.append(" : ");
        message.append(this.currentIlluminance.getValue());
        message.append('\n');
        this.logMessage(message.toString());

        super.userDefinedInternalTransition(elapsedTime);

        assert	glassBoxInvariants(this) :
                new NeoSim4JavaException("SmartLightingIlluminanceModel: White-box invariants violation!");
        assert	blackBoxInvariants(this) :
                new NeoSim4JavaException("SmartLightingIlluminanceModel: Black-Box invariants violation!");
    }

    /**
     * @see fr.sorbonne_u.devs_simulation.models.AtomicModel#userDefinedExternalTransition(Duration)
     */
    @Override
    public void			userDefinedExternalTransition(Duration elapsedTime)
    {
        ArrayList<EventI> currentEvents = this.getStoredEventAndReset();
        assert	currentEvents != null && currentEvents.size() == 1;

        Event ce = (Event) currentEvents.get(0);
        assert	ce instanceof SmartLightingEventI;

        StringBuffer sb = new StringBuffer("executing the external event: ");
        sb.append(ce.eventAsString());
        sb.append(".\n");
        this.logMessage(sb.toString());

        double newTemp =
                this.computeNewIlluminance(elapsedTime.getSimulatedDuration());
        ce.executeOn(this);
        this.currentIlluminance.setNewValue(
                newTemp,
                new Time(this.getCurrentStateTime().getSimulatedTime()
                         + elapsedTime.getSimulatedDuration(),
                         this.getSimulatedTimeUnit()));

        super.userDefinedExternalTransition(elapsedTime);

        assert	glassBoxInvariants(this) :
                new NeoSim4JavaException("SmartLightingIlluminanceModel: White-box invariants violation!");
        assert	blackBoxInvariants(this) :
                new NeoSim4JavaException("SmartLightingIlluminanceModel: Black-Box invariants violation!");
    }

    /**
     * @see AtomicHIOA#endSimulation(Time)
     */
    @Override
    public void			endSimulation(Time endTime)
    {
        this.meanIlluminance =
                this.illuminanceAcc/
                endTime.subtract(this.start).getSimulatedDuration();

        this.logMessage("simulation ends.\n");
        super.endSimulation(endTime);
    }

    // -------------------------------------------------------------------------
    // Optional DEVS simulation protocol: simulation report
    // -------------------------------------------------------------------------

    public static class SmartLightingIlluminanceReport
    implements SimulationReportI, HEM_ReportI{
        private static final long serialVersionUID = 1L;
        protected String	modelURI;
        protected double	meanIlluminance;

        public SmartLightingIlluminanceReport(
                String modelURI,
                double meanIlluminance
                )
        {
            super();
            this.modelURI = modelURI;
            this.meanIlluminance = meanIlluminance;
        }

        @Override
        public String getModelURI()
        {
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
            ret.append("mean temperature = ");
            ret.append(this.meanIlluminance);
            ret.append(".\n");
            ret.append(indent);
            ret.append("---\n");
            return ret.toString();
        }
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

    /**
     * @see fr.sorbonne_u.devs_simulation.models.interfaces.ModelI#getFinalReport()
     */
    @Override
    public SimulationReportI getFinalReport()
    {
        return new SmartLightingIlluminanceReport(
                this.getURI(),
                this.meanIlluminance
        );
    }
}
// -----------------------------------------------------------------------------