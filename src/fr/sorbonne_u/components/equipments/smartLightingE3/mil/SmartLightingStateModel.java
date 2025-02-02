package fr.sorbonne_u.components.equipments.smartLightingE3.mil;

import fr.sorbonne_u.components.cyphy.plugins.devs.AtomicSimulatorPlugin;
import fr.sorbonne_u.components.equipments.smartLightingE3.mil.events.*;
import fr.sorbonne_u.devs_simulation.exceptions.MissingRunParameterException;
import fr.sorbonne_u.devs_simulation.exceptions.NeoSim4JavaException;
import fr.sorbonne_u.devs_simulation.models.AtomicModel;
import fr.sorbonne_u.devs_simulation.models.annotations.ModelExternalEvents;
import fr.sorbonne_u.devs_simulation.models.events.Event;
import fr.sorbonne_u.devs_simulation.models.events.EventI;
import fr.sorbonne_u.devs_simulation.models.time.Duration;
import fr.sorbonne_u.devs_simulation.simulators.interfaces.AtomicSimulatorI;
import fr.sorbonne_u.devs_simulation.utils.InvariantChecking;
import fr.sorbonne_u.devs_simulation.utils.StandardLogger;

import fr.sorbonne_u.devs_simulation.models.time.Time;

import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@ModelExternalEvents(
    imported = {
      TurnOffSmartLighting.class,
      IncreaseLighting.class,
      DecreaseLighting.class,
      StopAdjustingLighting.class,
      TurnOnSmartLighting.class,
      SetPowerSmartLighting.class
    },
    exported = {
      TurnOffSmartLighting.class,
      IncreaseLighting.class,
      DecreaseLighting.class,
      StopAdjustingLighting.class,
      TurnOnSmartLighting.class,
      SetPowerSmartLighting.class
    })
public class SmartLightingStateModel extends AtomicModel implements SmartLightingOperationI {
  // -------------------------------------------------------------------------
  // Inner classes and types
  // -------------------------------------------------------------------------
  public enum State {
    ON,
    OFF,
    INCREASE,
    DECREASE
  }

  // -------------------------------------------------------------------------
  // Constants and variables
  // -------------------------------------------------------------------------

  private static final long serialVersionUID = 1L;

  public static final String MIL_URI = SmartLightingStateModel.class.getSimpleName() + "-MIL";
  public static final String MIL_RT_URI = SmartLightingStateModel.class.getSimpleName() + "-MIL-RT";
  public static final String SIL_URI = SmartLightingStateModel.class.getSimpleName() + "-SIL";

  protected State currentState = State.OFF;
  protected EventI toBeReemitted;

  // -------------------------------------------------------------------------
  // Invariants
  // -------------------------------------------------------------------------

  protected static boolean glassBoxInvariants(SmartLightingStateModel instance) {
    assert instance != null
        : new NeoSim4JavaException(
            "SmartLightingStateModel: Precondition violation: " + "instance != null");

    boolean ret = true;
    ret &=
        InvariantChecking.checkGlassBoxInvariant(
            instance.currentState != null,
            SmartLightingStateModel.class,
            instance,
            "currentState != null");
    return ret;
  }

  protected static boolean blackBoxInvariants(SmartLightingStateModel instance) {
    assert instance != null
        : new NeoSim4JavaException(
            "SmartLightingStateModel: Precondition violation: " + "instance != null");

    boolean ret = true;
    ret &=
        InvariantChecking.checkBlackBoxInvariant(
            MIL_URI != null && !MIL_URI.isEmpty(),
            SmartLightingStateModel.class,
            instance,
            "MIL_URI != null && !MIL_URI.isEmpty()");
    ret &=
        InvariantChecking.checkBlackBoxInvariant(
            MIL_RT_URI != null && !MIL_RT_URI.isEmpty(),
            SmartLightingStateModel.class,
            instance,
            "MIL_RT_URI != null && !MIL_RT_URI.isEmpty()");
    ret &=
        InvariantChecking.checkBlackBoxInvariant(
            SIL_URI != null && !SIL_URI.isEmpty(),
            SmartLightingStateModel.class,
            instance,
            "SIL_URI != null && !SIL_URI.isEmpty()");
    return ret;
  }

  // -------------------------------------------------------------------------
  // Constructors
  // -------------------------------------------------------------------------
  public SmartLightingStateModel(
      String uri, TimeUnit simulatedTimeUnit, AtomicSimulatorI simulationEngine) throws Exception {
    super(uri, simulatedTimeUnit, simulationEngine);
    this.getSimulationEngine().setLogger(new StandardLogger());

    assert glassBoxInvariants(this)
        : new NeoSim4JavaException(
            "SmartLightingStateModel: Constructor violation: " + "glassBoxInvariants(this)");
    assert blackBoxInvariants(this)
        : new NeoSim4JavaException(
            "SmartLightingStateModel: Constructor violation: " + "blackBoxInvariants(this)");
  }

  // -------------------------------------------------------------------------
  // Methods
  // -------------------------------------------------------------------------

  @Override
  public void setState(State s) {
    this.currentState = s;
  }

  @Override
  public void setCurrentPower(double power, Time t) {}

  @Override
  public State getState() {
    return this.currentState;
  }

  @Override
  public void initialiseState(Time initialTime) {
    super.initialiseState(initialTime);

    this.getSimulationEngine().toggleDebugMode();
    this.logMessage("simulation begins.");

    assert glassBoxInvariants(this)
        : new NeoSim4JavaException("SmartLightingStateModel.glassBoxInvariants(this)");
    assert blackBoxInvariants(this)
        : new NeoSim4JavaException("SmartLightingStateModel.blackBoxInvariants(this)");
  }

  @Override
  public Duration timeAdvance() {
    if (this.toBeReemitted == null) {
      return Duration.INFINITY;
    } else {
      return Duration.zero(getSimulatedTimeUnit());
    }
  }

  @Override
  public ArrayList<EventI> output() {
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
  public void userDefinedExternalTransition(Duration elapsedTime) {
    super.userDefinedExternalTransition(elapsedTime);

    // get the vector of current external events
    ArrayList<EventI> currentEvents = this.getStoredEventAndReset();
    // when this method is called, there is at least one external event,
    // and for the heater model, there will be exactly one by
    // construction.
    assert currentEvents != null && currentEvents.size() == 1;

    this.toBeReemitted = (Event) currentEvents.get(0);
    assert this.toBeReemitted instanceof SmartLightingEventI;
    this.toBeReemitted.executeOn(this);

    assert glassBoxInvariants(this)
        : new NeoSim4JavaException("SmartLightingStateModel.glassBoxInvariants(this)");
    assert blackBoxInvariants(this)
        : new NeoSim4JavaException("SmartLightingStateModel.blackBoxInvariants(this)");
  }

  @Override
  public void endSimulation(Time endTime) {
    this.logMessage("simulation ends.");
  }

  // -------------------------------------------------------------------------
  // Optional DEVS simulation protocol: simulation run parameters
  // -------------------------------------------------------------------------

  /**
   * @see
   *     fr.sorbonne_u.devs_simulation.models.interfaces.ModelI#setSimulationRunParameters(java.util.Map)
   */
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
}
