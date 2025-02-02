package fr.sorbonne_u.components.equipments.smartLightingE3.mil.events;

import fr.sorbonne_u.components.equipments.smartLightingE3.mil.SmartLightingElectricityModel;
import fr.sorbonne_u.components.equipments.smartLightingE3.mil.SmartLightingIlluminanceModel;
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
    assert model instanceof SmartLightingElectricityModel
        || model instanceof SmartLightingIlluminanceModel;
    new NeoSim4JavaException(
        "TurnOffSmartLighting: Precondition violation: model instanceof "
            + "HeaterElectricityModel || "
            + "HeaterIlluminanceModel");

    SmartLightingElectricityModel m = (SmartLightingElectricityModel) model;
    assert m.getState() != SmartLightingElectricityModel.State.OFF;
    new NeoSim4JavaException(
        "TurnOffSmartLighting: Precondition violation: m.getState() != "
            + "HeaterIlluminanceModel.State.OFF but is "
            + m.getState());

    m.setState(SmartLightingElectricityModel.State.OFF, this.getTimeOfOccurrence());
  }
}
