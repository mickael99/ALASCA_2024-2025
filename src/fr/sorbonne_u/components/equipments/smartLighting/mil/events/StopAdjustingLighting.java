package fr.sorbonne_u.components.equipments.smartLighting.mil.events;

import fr.sorbonne_u.components.equipments.smartLighting.mil.SmartLightingOperationI;
import fr.sorbonne_u.components.equipments.smartLighting.mil.SmartLightingStateModel;
import fr.sorbonne_u.devs_simulation.exceptions.NeoSim4JavaException;
import fr.sorbonne_u.devs_simulation.models.events.Event;
import fr.sorbonne_u.devs_simulation.models.events.EventI;
import fr.sorbonne_u.devs_simulation.models.interfaces.AtomicModelI;
import fr.sorbonne_u.devs_simulation.models.time.Time;

public class StopAdjustingLighting extends Event implements SmartLightingEventI {
  // ------------------------------------------------------------------------
  // Constants
  // ------------------------------------------------------------------------

  private static final long serialVersionUID = 1L;

  // ------------------------------------------------------------------------
  // Constructors
  // ------------------------------------------------------------------------

  public StopAdjustingLighting(Time timeOfOccurrence) {
    super(timeOfOccurrence, null);
  }

  // ------------------------------------------------------------------------
  // Methods
  // ------------------------------------------------------------------------

  @Override
  public boolean hasPriorityOver(EventI e) {
      return e instanceof TurnOffSmartLighting || e instanceof SetPowerSmartLighting;
  }

  @Override
  public void executeOn(AtomicModelI model) {
    assert model instanceof AtomicModelI
        : new NeoSim4JavaException(
            "StopAdjustingLighting: Precondition violation: model instanceof AtomicModelI");
    SmartLightingOperationI smartLighting = (SmartLightingOperationI) model;
    assert smartLighting.getState() == SmartLightingStateModel.State.DECREASE
            || smartLighting.getState() == SmartLightingStateModel.State.INCREASE
        : new NeoSim4JavaException(
            "StopAdjustingLighting: Precondition violation: smartLighting.getState() == SmartLightingStateModel.State.DECREASE || "
                + "smartLighting.getState() == SmartLightingStateModel.State.INCREASE but is "
                + smartLighting.getState());

    smartLighting.setState(SmartLightingStateModel.State.ON);
  }
}
