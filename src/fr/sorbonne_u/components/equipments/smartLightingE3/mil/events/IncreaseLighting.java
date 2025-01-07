package fr.sorbonne_u.components.equipments.smartLightingE3.mil.events;

import fr.sorbonne_u.components.equipments.smartLightingE3.mil.SmartLightingElectricityModel;
import fr.sorbonne_u.components.equipments.smartLightingE3.mil.SmartLightingIlluminanceModel;
import fr.sorbonne_u.devs_simulation.es.events.ES_Event;
import fr.sorbonne_u.devs_simulation.exceptions.NeoSim4JavaException;
import fr.sorbonne_u.devs_simulation.models.events.EventI;
import fr.sorbonne_u.devs_simulation.models.interfaces.AtomicModelI;
import fr.sorbonne_u.devs_simulation.models.time.Time;

public class IncreaseLighting extends ES_Event implements SmartLightingEventI {
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
        assert model instanceof SmartLightingElectricityModel ||
               model instanceof SmartLightingIlluminanceModel;
        new NeoSim4JavaException(
                "IncreaseLighting: Precondition violation: model instanceof "
                + "HeaterElectricityModel || "
                + "HeaterIlluminanceModel"
        );

        if (model instanceof SmartLightingElectricityModel) {
            SmartLightingElectricityModel m = (SmartLightingElectricityModel) model;
            assert m.getState() == SmartLightingElectricityModel.State.ON ||
                   m.getState() == SmartLightingElectricityModel.State.DECREASE;
            new NeoSim4JavaException(
                    "IncreaseLighting: Precondition violation: m.getState() == "
                    + "HeaterIlluminanceModel.State.ON or DECREASE but is "
                    + m.getState()
            );

            m.setState(SmartLightingElectricityModel.State.INCREASE, this.getTimeOfOccurrence());
        } else {
            SmartLightingIlluminanceModel m = (SmartLightingIlluminanceModel) model;
            assert m.getState() == SmartLightingIlluminanceModel.State.ON ||
                   m.getState() == SmartLightingIlluminanceModel.State.DECREASE;
            new NeoSim4JavaException(
                    "IncreaseLighting: Precondition violation: m.getState() == "
                    + "HeaterIlluminanceModel.State.ON or DECREASE but is "
                    + m.getState()
            );

            m.setState(SmartLightingIlluminanceModel.State.INCREASE);
        }
    }
}
//  -----------------------------------------------------------------------------