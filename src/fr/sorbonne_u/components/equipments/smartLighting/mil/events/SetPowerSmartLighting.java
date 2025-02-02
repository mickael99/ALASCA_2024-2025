package fr.sorbonne_u.components.equipments.smartLighting.mil.events;

import fr.sorbonne_u.components.equipments.smartLighting.mil.SmartLightingElectricityModel;
import fr.sorbonne_u.components.equipments.smartLighting.mil.SmartLightingOperationI;
import fr.sorbonne_u.components.equipments.smartLighting.mil.SmartLightingStateModel;
import fr.sorbonne_u.devs_simulation.es.events.ES_Event;
import fr.sorbonne_u.devs_simulation.exceptions.NeoSim4JavaException;
import fr.sorbonne_u.devs_simulation.models.events.EventI;
import fr.sorbonne_u.devs_simulation.models.events.EventInformationI;
import fr.sorbonne_u.devs_simulation.models.interfaces.AtomicModelI;
import fr.sorbonne_u.devs_simulation.models.time.Time;

public class SetPowerSmartLighting extends ES_Event implements SmartLightingEventI {
  // ------------------------------------------------------------------------
  // Inner classes
  // ------------------------------------------------------------------------
  public static class PowerValue implements EventInformationI {
    private static final long serialVersionUID = 1L;
    /* a power in watts.												*/
    protected final double power;

    public PowerValue(double power) {
      super();

      assert power >= 0.0 && power <= SmartLightingElectricityModel.MAX_LIGHTING_POWER
          : new NeoSim4JavaException(
              "SetPowerSmartLighting: Precondition violation: power >= 0.0 && "
                  + "power <= SmartLightingElectricityModel.MAX_LIGHTING_POWER,"
                  + " but power = "
                  + power);

      this.power = power;
    }

    public double getPower() {
      return this.power;
    }

    @Override
    public String toString() {
      StringBuffer sb = new StringBuffer(this.getClass().getSimpleName());
      sb.append('[');
      sb.append(this.power);
      sb.append(']');
      return sb.toString();
    }
  }

  // ------------------------------------------------------------------------
  // Constants
  // ------------------------------------------------------------------------

  private static final long serialVersionUID = 1L;
  protected final PowerValue powerValue;

  // ------------------------------------------------------------------------
  // Constructors
  // ------------------------------------------------------------------------

  public SetPowerSmartLighting(Time timeOfOccurrence, EventInformationI content) {
    super(timeOfOccurrence, content);

    assert content != null && content instanceof PowerValue;
    new NeoSim4JavaException(
        "SetPowerSmartLighting: Precondition violation: content != null && "
            + "content instanceof PowerValue"
            + content);
    this.powerValue = (PowerValue) content;
  }

  // ------------------------------------------------------------------------
  // Methods
  // ------------------------------------------------------------------------

  @Override
  public boolean hasPriorityOver(EventI e) {
    if (e instanceof TurnOffSmartLighting) {
      return true;
    } else {
      return false;
    }
  }

  @Override
  public void executeOn(AtomicModelI model) {
    assert model instanceof AtomicModelI
        : new NeoSim4JavaException(
            "SetPowerSmartLighting: Precondition violation: model instanceof AtomicModelI");

    SmartLightingOperationI smartLighting = (SmartLightingOperationI) model;
    assert smartLighting.getState() != SmartLightingStateModel.State.OFF
        : new NeoSim4JavaException(
            "SetPowerSmartLighting: Precondition violation: smartLighting.getState() != SmartLightingOperationI.State.OFF");
    smartLighting.setCurrentPower(this.powerValue.getPower(), this.getTimeOfOccurrence());
  }
}
