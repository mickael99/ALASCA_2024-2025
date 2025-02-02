package fr.sorbonne_u.components.equipments.smartLightingE3.mil;

import fr.sorbonne_u.components.cyphy.plugins.devs.AtomicSimulatorPlugin;
import fr.sorbonne_u.devs_simulation.exceptions.MissingRunParameterException;
import fr.sorbonne_u.devs_simulation.exceptions.NeoSim4JavaException;
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
import java.util.Map;
import java.util.concurrent.TimeUnit;

// -------------------------------------------------------------------------
/**
 * The class <code>ExternalIlluminanceModel</code> defines a simulation model for the environment,
 * namely the external illuminance of the house.
 *
 * <p><strong>Description</strong>
 *
 * <p>The model makes the illuminance vary over some period representing typically a day. The
 * illuminance is computed as a sinusoidal function of the time of the day. The illuminance is at
 * its minimum at 6:00 and 18:00 and at its maximum at 12:00. The illuminance is computed as
 * follows: illuminance = MIN_EXTERNAL_ILLUMINANCE + (MAX_EXTERNAL_ILLUMINANCE -
 * MIN_EXTERNAL_ILLUMINANCE) * sin((cycleTime - 6) * PI / 12) where cycleTime is the current time of
 * the day in hours. The cycleTime is computed as the sum of the current time and the elapsed time
 * since the last evaluation step. The cycleTime is reset to 0 when it reaches 24. The
 * MIN_EXTERNAL_ILLUMINANCE and MAX_EXTERNAL_ILLUMINANCE are the minimum and maximum illuminance
 * values. The MIN_EXTERNAL_ILLUMINANCE is 0 and the MAX_EXTERNAL_ILLUMINANCE is 100000.
 *
 * <p>Created on : 2024-11-09
 *
 * @author <a href="mailto:yukai_luo@yahoo.com">Yukai Luo</a>
 */
@ModelExportedVariable(name = "externalIlluminance", type = Double.class)
// -------------------------------------------------------------------------

public class ExternalIlluminanceModel extends AtomicHIOA {

  // ------------------------------------------------------------------------
  // Constants and variables
  // ------------------------------------------------------------------------

  private static final long serialVersionUID = 1L;

  public static final String MIL_URI = ExternalIlluminanceModel.class.getSimpleName() + "-MIL";
  public static final String MIL_RT_URI =
      ExternalIlluminanceModel.class.getSimpleName() + "-MIL-RT";
  public static final String SIL_URI = ExternalIlluminanceModel.class.getSimpleName() + "-SIL";

  public static final String URI = ExternalIlluminanceModel.class.getSimpleName();

  protected static double MIN_EXTERNAL_ILLUMINANCE = 0.0;
  protected static double MAX_EXTERNAL_ILLUMINANCE = 100000.0;
  protected static double PERIOD = 24.0;
  protected static double STEP = 60.0 / 3600.0; // 60 seconds

  protected final Duration evaluationStep;
  protected double cycleTime;

  // -------------------------------------------------------------------------
  // HIOA model variables
  // -------------------------------------------------------------------------

  /** current external temperature in Celsius. */
  @ExportedVariable(type = Double.class)
  protected final Value<Double> externalIlluminance = new Value<Double>(this);

  // ------------------------------------------------------------------------
  // Invariants
  // ------------------------------------------------------------------------

  protected static boolean glassBoxInvariants(ExternalIlluminanceModel instance) {
    assert instance != null : new AssertionError("Precondition violation: instance != null");

    boolean ret = true;
    ret &=
        InvariantChecking.checkGlassBoxInvariant(
            instance.cycleTime >= 0.0 && instance.cycleTime <= PERIOD,
            ExternalIlluminanceModel.class,
            instance,
            "cycleTime >= 0.0 && instance.cycleTime <= PERIOD");
    ret &=
        InvariantChecking.checkGlassBoxInvariant(
            STEP > 0.0, ExternalIlluminanceModel.class, instance, "STEP > 0.0");
    ret &=
        InvariantChecking.checkGlassBoxInvariant(
            instance.evaluationStep.getSimulatedDuration() > 0.0,
            ExternalIlluminanceModel.class,
            instance,
            "evaluationStep.getSimulatedDuration() > 0.0");
    return ret;
  }

  protected static boolean blackBoxInvariants(ExternalIlluminanceModel instance) {
    assert instance != null : new AssertionError("Precondition violation: instance != null");

    boolean ret = true;
    ret &=
        InvariantChecking.checkBlackBoxInvariant(
            MIL_URI != null && !MIL_URI.isEmpty(),
            ExternalIlluminanceModel.class,
            instance,
            "MIL_URI != null && !MIL_URI.isEmpty()");
    ret &=
        InvariantChecking.checkBlackBoxInvariant(
            MIL_RT_URI != null && !MIL_RT_URI.isEmpty(),
            ExternalIlluminanceModel.class,
            instance,
            "MIL_RT_URI != null && !MIL_RT_URI.isEmpty()");
    ret &=
        InvariantChecking.checkBlackBoxInvariant(
            SIL_URI != null && !SIL_URI.isEmpty(),
            ExternalIlluminanceModel.class,
            instance,
            "SIL_URI != null && !SIL_URI.isEmpty()");
    ret &=
        InvariantChecking.checkBlackBoxInvariant(
            URI != null && !URI.isEmpty(),
            ExternalIlluminanceModel.class,
            instance,
            "URI != null && !URI.isEmpty()");
    ret &=
        InvariantChecking.checkBlackBoxInvariant(
            MAX_EXTERNAL_ILLUMINANCE > MIN_EXTERNAL_ILLUMINANCE,
            ExternalIlluminanceModel.class,
            instance,
            "MAX_EXTERNAL_TEMPERATURE > MIN_EXTERNAL_TEMPERATURE");
    ret &=
        InvariantChecking.checkBlackBoxInvariant(
            PERIOD > 0.0, ExternalIlluminanceModel.class, instance, "PERIOD > 0.0");
    return ret;
  }

  // ------------------------------------------------------------------------
  // Constructors
  // ------------------------------------------------------------------------

  public ExternalIlluminanceModel(
      String uri, TimeUnit simulatedTimeUnit, AtomicSimulatorI simulationEngine) {
    super(uri, simulatedTimeUnit, simulationEngine);
    this.evaluationStep = new Duration(STEP, this.getSimulatedTimeUnit());
    this.getSimulationEngine().setLogger(new StandardLogger());

    assert glassBoxInvariants(this)
        : new NeoSim4JavaException("ExternalIlluminanceModel: White-box invariants violation!");
    assert blackBoxInvariants(this)
        : new NeoSim4JavaException("ExternalIlluminanceModel: Black-box invariants violation!");
  }

  // -------------------------------------------------------------------------
  // DEVS simulation protocol
  // -------------------------------------------------------------------------

  /**
   * @see AtomicHIOA#initialiseState(Time)
   */
  @Override
  public void initialiseState(Time initialTime) {
    super.initialiseState(initialTime);

    this.cycleTime = 0.0;

    assert glassBoxInvariants(this)
        : new NeoSim4JavaException("ExternalIlluminanceModel: White-box invariants violation!");
    assert blackBoxInvariants(this)
        : new NeoSim4JavaException("ExternalIlluminanceModel: Black-box invariants violation!");
  }

  /**
   * @see
   *     fr.sorbonne_u.devs_simulation.hioa.models.interfaces.VariableInitialisationI#useFixpointInitialiseVariables()
   */
  @Override
  public boolean useFixpointInitialiseVariables() {
    return true;
  }

  /**
   * @see
   *     fr.sorbonne_u.devs_simulation.hioa.models.interfaces.VariableInitialisationI#fixpointInitialiseVariables()
   */
  @Override
  public Pair<Integer, Integer> fixpointInitialiseVariables() {
    Pair<Integer, Integer> ret = null;

    if (!this.externalIlluminance.isInitialised()) {
      this.externalIlluminance.initialise(MIN_EXTERNAL_ILLUMINANCE);

      this.logMessage("simulation begins.\n");
      StringBuffer message = new StringBuffer("current external temperature: ");
      message.append(this.externalIlluminance.getValue());
      message.append(" at ");
      message.append(this.getCurrentStateTime());
      message.append("\n");
      this.logMessage(message.toString());

      ret = new Pair<>(1, 0);
    } else {
      ret = new Pair<>(0, 0);
    }

    assert glassBoxInvariants(this)
        : new NeoSim4JavaException("ExternalIlluminanceModel: White-box invariants violation!");
    assert blackBoxInvariants(this)
        : new NeoSim4JavaException("ExternalIlluminanceModel: Black-box invariants violation!");

    return ret;
  }

  /**
   * @see
   *     fr.sorbonne_u.devs_simulation.hioa.models.interfaces.VariableInitialisationI#initialiseVariables()
   */
  @Override
  public void initialiseVariables() {
    super.initialiseVariables();

    assert glassBoxInvariants(this)
        : new NeoSim4JavaException("ExternalIlluminanceModel: Glass-box invariants violation!");
    assert blackBoxInvariants(this)
        : new NeoSim4JavaException("ExternalIlluminanceModel: Black-box invariants violation!");
  }

  /**
   * @see fr.sorbonne_u.devs_simulation.models.interfaces.AtomicModelI#output()
   */
  @Override
  public ArrayList<EventI> output() {
    return null;
  }

  /**
   * @see fr.sorbonne_u.devs_simulation.models.interfaces.ModelI#timeAdvance()
   */
  @Override
  public Duration timeAdvance() {
    return this.evaluationStep;
  }

  /**
   * @see AtomicHIOA#endSimulation(Time)
   */
  @Override
  public void endSimulation(Time endTime) {
    this.logMessage("simulation ends.\n");
    super.endSimulation(endTime);
  }

  /**
   * @see fr.sorbonne_u.devs_simulation.models.AtomicModel#userDefinedInternalTransition(Duration)
   */
  @Override
  public void userDefinedInternalTransition(Duration elapsedTime) {
    super.userDefinedInternalTransition(elapsedTime);

    // compute the current time in the cycle
    this.cycleTime += elapsedTime.getSimulatedDuration();
    if (this.cycleTime > PERIOD) {
      this.cycleTime -= PERIOD;
    }
    double newIlluminance;
    // compute the new illuminance
    if (cycleTime < 6 || cycleTime > 18) {
      newIlluminance = MIN_EXTERNAL_ILLUMINANCE;
    } else {
      double adjustedCycleTime = cycleTime - 6;
      double sineValue = Math.sin(adjustedCycleTime * Math.PI / 12);
      newIlluminance =
          MIN_EXTERNAL_ILLUMINANCE
              + (MAX_EXTERNAL_ILLUMINANCE - MIN_EXTERNAL_ILLUMINANCE) * sineValue;
    }
    this.externalIlluminance.setNewValue(newIlluminance, this.getCurrentStateTime());

    // Tracing
    StringBuffer message = new StringBuffer("current external illuminance: ");
    message.append(this.externalIlluminance.getValue());
    message.append(" at ");
    message.append(this.getCurrentStateTime());
    message.append("\n");
    this.logMessage(message.toString());

    assert glassBoxInvariants(this) : new NeoSim4JavaException("White-box invariants violation!");
    assert blackBoxInvariants(this) : new NeoSim4JavaException("Black-box invariants violation!");
  }

  // -------------------------------------------------------------------------
  // Optional DEVS simulation protocol: simulation run parameters
  // -------------------------------------------------------------------------

  @Override
  public void setSimulationRunParameters(Map<String, Object> simParams)
      throws MissingRunParameterException {
    // this gets the reference on the owner component which is required
    // to have simulation models able to make the component perform some
    // operations or tasks or to get the value of variables held by the
    // component when necessary.
    if (simParams.containsKey(AtomicSimulatorPlugin.OWNER_RUNTIME_PARAMETER_NAME)) {
      // by the following, all of the logging will appear in the owner
      // component logger
      this.getSimulationEngine().setLogger(AtomicSimulatorPlugin.createComponentLogger(simParams));
    }
  }

  // -------------------------------------------------------------------------
  // Optional DEVS simulation protocol: simulation report
  // -------------------------------------------------------------------------

  /**
   * @see fr.sorbonne_u.devs_simulation.models.interfaces.ModelI#getFinalReport()
   */
  @Override
  public SimulationReportI getFinalReport() {
    return null;
  }
}
