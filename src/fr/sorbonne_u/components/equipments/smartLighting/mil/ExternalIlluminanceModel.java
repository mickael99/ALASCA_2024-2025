package fr.sorbonne_u.components.equipments.smartLighting.mil;

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

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;
// -------------------------------------------------------------------------
/**
 * The class <code>ExternalIlluminanceModel</code> defines a simulation model
 * for the environment, namely the external illuminance of the house.
 *
 * <p><strong>Description</strong></p>
 *
 * <p>
 * The model makes the illuminance vary over some period representing typically
 * a day. The illuminance is computed as a sinusoidal function of the time of
 * the day. The illuminance is at its minimum at 6:00 and 18:00 and at its
 * maximum at 12:00. The illuminance is computed as follows:
 *     illuminance = MIN_EXTERNAL_ILLUMINANCE + (MAX_EXTERNAL_ILLUMINANCE - MIN_EXTERNAL_ILLUMINANCE) * sin((cycleTime - 6) * PI / 12)
 *     where cycleTime is the current time of the day in hours.
 *     The cycleTime is computed as the sum of the current time and the elapsed time since the last evaluation step.
 *     The cycleTime is reset to 0 when it reaches 24.
 *     The MIN_EXTERNAL_ILLUMINANCE and MAX_EXTERNAL_ILLUMINANCE are the minimum and maximum illuminance values.
 *     The MIN_EXTERNAL_ILLUMINANCE is 0 and the MAX_EXTERNAL_ILLUMINANCE is 100000.
 * </p>
 * <p>Created on : 2024-11-09</p>
 *
 * @author	<a href="mailto:yukai_luo@yahoo.com">Yukai Luo</a>
 */

@ModelExportedVariable(name = "externalIlluminance", type = Double.class)
//-------------------------------------------------------------------------

public class ExternalIlluminanceModel extends AtomicHIOA {

    // ------------------------------------------------------------------------
    // Constants and variables
    // ------------------------------------------------------------------------

    private static final long serialVersionUID = 1L;
    public static final String URI = ExternalIlluminanceModel.class.getSimpleName();

    protected static double MIN_EXTERNAL_ILLUMINANCE = 0.0;
    protected static double MAX_EXTERNAL_ILLUMINANCE = 100000.0;
    protected static double PERIOD = 24.0;
    protected static double			STEP = 60.0/3600.0;	// 60 seconds

    protected final Duration evaluationStep;
    protected double				cycleTime;

    // -------------------------------------------------------------------------
    // HIOA model variables
    // -------------------------------------------------------------------------

    /** current external temperature in Celsius.							*/
    @ExportedVariable(type = Double.class)
    protected final Value<Double> externalIlluminance =
            new Value<Double>(this);


    // ------------------------------------------------------------------------
    // Invariants
    // ------------------------------------------------------------------------

    protected static boolean	glassBoxInvariants(
            ExternalIlluminanceModel instance
                                                  )
    {
        assert	instance != null :
                new AssertionError("Precondition violation: instance != null");

        boolean ret = true;
        ret &= InvariantChecking.checkGlassBoxInvariant(
                instance.cycleTime >= 0.0 && instance.cycleTime <= PERIOD,
                ExternalIlluminanceModel.class,
                instance,
                "cycleTime >= 0.0 && instance.cycleTime <= PERIOD");
        ret &= InvariantChecking.checkGlassBoxInvariant(
                STEP > 0.0,
                ExternalIlluminanceModel.class,
                instance,
                "STEP > 0.0");
        ret &= InvariantChecking.checkGlassBoxInvariant(
                instance.evaluationStep.getSimulatedDuration() > 0.0,
                ExternalIlluminanceModel.class,
                instance,
                "evaluationStep.getSimulatedDuration() > 0.0");
        return ret;
    }

    protected static boolean	blackBoxInvariants(
            ExternalIlluminanceModel instance
                                                  )
    {
        assert	instance != null :
                new AssertionError("Precondition violation: instance != null");

        boolean ret = true;
        ret &= InvariantChecking.checkBlackBoxInvariant(
                URI != null && !URI.isEmpty(),
                ExternalIlluminanceModel.class,
                instance,
                "URI != null && !URI.isEmpty()");
        ret &= InvariantChecking.checkBlackBoxInvariant(
                MAX_EXTERNAL_ILLUMINANCE > MIN_EXTERNAL_ILLUMINANCE,
                ExternalIlluminanceModel.class,
                instance,
                "MAX_EXTERNAL_TEMPERATURE > MIN_EXTERNAL_TEMPERATURE");
        ret &= InvariantChecking.checkBlackBoxInvariant(
                PERIOD > 0.0,
                ExternalIlluminanceModel.class,
                instance,
                "PERIOD > 0.0");
        return ret;
    }

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------

    public ExternalIlluminanceModel(String uri,
                                    TimeUnit simulatedTimeUnit,
                                    AtomicSimulatorI simulationEngine
                                   ) {
        super(uri, simulatedTimeUnit, simulationEngine);
        this.evaluationStep = new Duration(STEP, this.getSimulatedTimeUnit());
        this.getSimulationEngine().setLogger(new StandardLogger());

        assert	glassBoxInvariants(this) :
                new AssertionError("White-box invariants violation!");
        assert	blackBoxInvariants(this) :
                new AssertionError("Black-box invariants violation!");
    }

    // -------------------------------------------------------------------------
    // DEVS simulation protocol
    // -------------------------------------------------------------------------

    /**
     * @see fr.sorbonne_u.devs_simulation.hioa.models.AtomicHIOA#initialiseState(fr.sorbonne_u.devs_simulation.models.time.Time)
     */
    @Override
    public void			initialiseState(Time initialTime)
    {
        super.initialiseState(initialTime);

        this.cycleTime = 0.0;

        assert	glassBoxInvariants(this) :
                new AssertionError("White-box invariants violation!");
        assert	blackBoxInvariants(this) :
                new AssertionError("Black-box invariants violation!");
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

        if (!this.externalIlluminance.isInitialised()) {
            this.externalIlluminance.initialise(MIN_EXTERNAL_ILLUMINANCE);

            this.logMessage("simulation begins.\n");
            StringBuffer message =
                    new StringBuffer("current external temperature: ");
            message.append(this.externalIlluminance.getValue());
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

    /**
     * @see fr.sorbonne_u.devs_simulation.hioa.models.interfaces.VariableInitialisationI#initialiseVariables()
     */
    @Override
    public void			initialiseVariables()
    {
        super.initialiseVariables();

        assert	glassBoxInvariants(this) :
                new AssertionError("Glass-box invariants violation!");
        assert	blackBoxInvariants(this) :
                new AssertionError("Black-box invariants violation!");
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
        return this.evaluationStep;
    }

    /**
     * @see fr.sorbonne_u.devs_simulation.hioa.models.AtomicHIOA#endSimulation(fr.sorbonne_u.devs_simulation.models.time.Time)
     */
    @Override
    public void			endSimulation(Time endTime)
    {
        this.logMessage("simulation ends.\n");
        super.endSimulation(endTime);
    }

    /**
     * @see fr.sorbonne_u.devs_simulation.models.AtomicModel#userDefinedInternalTransition(fr.sorbonne_u.devs_simulation.models.time.Duration)
     */
    @Override
    public void			userDefinedInternalTransition(Duration elapsedTime)
    {
        super.userDefinedInternalTransition(elapsedTime);

        // compute the current time in the cycle
        this.cycleTime += elapsedTime.getSimulatedDuration();
        if (this.cycleTime > PERIOD) {
            this.cycleTime -= PERIOD;
        }
        double newIlluminance;
        // compute the new illuminance
        if(cycleTime < 6 || cycleTime > 18) {
            newIlluminance = MIN_EXTERNAL_ILLUMINANCE;
        } else {
            double adjustedCycleTime = cycleTime - 6;
            double sineValue = Math.sin(adjustedCycleTime * Math.PI / 12);
            newIlluminance = MIN_EXTERNAL_ILLUMINANCE + (MAX_EXTERNAL_ILLUMINANCE - MIN_EXTERNAL_ILLUMINANCE) * sineValue;
        }
        this.externalIlluminance.setNewValue(newIlluminance,
                                             this.getCurrentStateTime());

        // Tracing
        StringBuffer message =
                new StringBuffer("current external illuminance: ");
        message.append(this.externalIlluminance.getValue());
        message.append(" at ");
        message.append(this.getCurrentStateTime());
        message.append("\n");
        this.logMessage(message.toString());

        assert	glassBoxInvariants(this) :
                new AssertionError("White-box invariants violation!");
        assert	blackBoxInvariants(this) :
                new AssertionError("Black-box invariants violation!");
    }

    // -------------------------------------------------------------------------
    // Optional DEVS simulation protocol: simulation report
    // -------------------------------------------------------------------------

    /**
     * @see fr.sorbonne_u.devs_simulation.models.interfaces.ModelI#getFinalReport()
     */
    @Override
    public SimulationReportI getFinalReport()
    {
        return null;
    }
}
