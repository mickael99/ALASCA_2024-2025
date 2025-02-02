package fr.sorbonne_u.components.equipments.smartLightingE3.mil.events;

import fr.sorbonne_u.components.equipments.smartLightingE3.mil.SmartLightingElectricityModel;
import fr.sorbonne_u.devs_simulation.es.events.ES_Event;
import fr.sorbonne_u.devs_simulation.exceptions.NeoSim4JavaException;
import fr.sorbonne_u.devs_simulation.models.events.EventI;
import fr.sorbonne_u.devs_simulation.models.interfaces.AtomicModelI;
import fr.sorbonne_u.devs_simulation.models.time.Time;

public class TurnOnSmartLighting extends ES_Event implements SmartLightingEventI {
  // ------------------------------------------------------------------------
  // Constants
  // ------------------------------------------------------------------------

  private static final long serialVersionUID = 1L;

  // ------------------------------------------------------------------------
  // Constructors
  // ------------------------------------------------------------------------

  public TurnOnSmartLighting(Time timeOfOccurrence) {
    super(timeOfOccurrence, null);
  }

  // ------------------------------------------------------------------------
  // Methods
  // ------------------------------------------------------------------------

  @Override
  public boolean hasPriorityOver(EventI e) {
    return true;
  }

  @Override
  public void executeOn(AtomicModelI model) {
    assert model instanceof SmartLightingElectricityModel;
    new NeoSim4JavaException(
        "TurnOnSmartLighting: Precondition violation: model instanceof "
            + "HeaterElectricityModel");

    SmartLightingElectricityModel m = (SmartLightingElectricityModel) model;
    assert m.getState() == SmartLightingElectricityModel.State.OFF;
    new NeoSim4JavaException(
        "TurnOnSmartLighting: Precondition violation: m.getState() == "
            + "HeaterElectricityModel.State.OFF but is "
            + m.getState());

    m.setState(SmartLightingElectricityModel.State.ON, this.getTimeOfOccurrence());
  }
}
// -----------------------------------------------------------------------------
