package fr.sorbonne_u.components.equipments.smartLighting.mil.events;

import fr.sorbonne_u.components.equipments.smartLighting.mil.SmartLightingOperationI;
import fr.sorbonne_u.components.equipments.smartLighting.mil.SmartLightingStateModel;
import fr.sorbonne_u.devs_simulation.exceptions.NeoSim4JavaException;
import fr.sorbonne_u.devs_simulation.models.events.Event;
import fr.sorbonne_u.devs_simulation.models.events.EventI;
import fr.sorbonne_u.devs_simulation.models.interfaces.AtomicModelI;
import fr.sorbonne_u.devs_simulation.models.time.Time;

public class IncreaseLighting extends Event implements SmartLightingEventI {
  // ------------------------------------------------------------------------
  // Constants
  // ------------------------------------------------------------------------

  private static final long serialVersionUID = 1L;

  // ------------------------------------------------------------------------
  // Constructors
  // ------------------------------------------------------------------------

  public IncreaseLighting(Time timeOfOccurrence) {
    super(timeOfOccurrence, null);
  }

  // ------------------------------------------------------------------------
  // Methods
  // ------------------------------------------------------------------------

  @Override
  public boolean hasPriorityOver(EventI e) {
    if (e instanceof TurnOffSmartLighting) {
      return false;
    } else {
      return true;
    }
  }

  @Override
  public void executeOn(AtomicModelI model) {
    assert model instanceof AtomicModelI
        : new NeoSim4JavaException(
            "IncreaseLighting: Precondition violation: model instanceof AtomicModelI");
    SmartLightingOperationI smartLighting = (SmartLightingOperationI) model;
    assert smartLighting.getState() == SmartLightingStateModel.State.ON
        : new NeoSim4JavaException(
            "IncreaseLighting: Precondition violation: smartLighting.getState() == SmartLightingStateModel.State.ON but is "
                + smartLighting.getState());
    smartLighting.setState(SmartLightingStateModel.State.INCREASE);
  }
}
//  -----------------------------------------------------------------------------
