package fr.sorbonne_u.components.equipments.smartLighting.mil.events;

import fr.sorbonne_u.components.equipments.smartLighting.mil.SmartLightingOperationI;
import fr.sorbonne_u.components.equipments.smartLighting.mil.SmartLightingStateModel;
import fr.sorbonne_u.devs_simulation.es.events.ES_Event;
import fr.sorbonne_u.devs_simulation.exceptions.NeoSim4JavaException;
import fr.sorbonne_u.devs_simulation.models.events.EventI;
import fr.sorbonne_u.devs_simulation.models.interfaces.AtomicModelI;
import fr.sorbonne_u.devs_simulation.models.time.Time;

public class TurnOffSmartLighting extends ES_Event implements SmartLightingEventI {
  // ------------------------------------------------------------------------
  // Constants
  // ------------------------------------------------------------------------

  private static final long serialVersionUID = 1L;

  // ------------------------------------------------------------------------
  // Constructors
  // ------------------------------------------------------------------------

  public TurnOffSmartLighting(Time timeOfOccurrence) {
    super(timeOfOccurrence, null);
  }

  // ------------------------------------------------------------------------
  // Methods
  // ------------------------------------------------------------------------

  @Override
  public boolean hasPriorityOver(EventI e) {
    return false;
  }

  @Override
  public void executeOn(AtomicModelI model) {
    assert model instanceof AtomicModelI
        : new NeoSim4JavaException(
            "TurnOffSmartLighting: Precondition violation: model instanceof AtomicModelI");
    SmartLightingOperationI smartLighting = (SmartLightingOperationI) model;
    assert smartLighting.getState() != SmartLightingStateModel.State.OFF
        : new NeoSim4JavaException(
            "TurnOffSmartLighting: Precondition violation: smartLighting.getState() != SmartLightingStateModel.State.OFF ");
    smartLighting.setState(SmartLightingStateModel.State.OFF);
  }
}
