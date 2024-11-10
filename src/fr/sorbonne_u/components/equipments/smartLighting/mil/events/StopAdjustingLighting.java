package fr.sorbonne_u.components.equipments.smartLighting.mil.events;

import fr.sorbonne_u.components.equipments.smartLighting.mil.SmartLightingElectricityModel;
import fr.sorbonne_u.components.equipments.smartLighting.mil.SmartLightingIlluminanceModel;
import fr.sorbonne_u.devs_simulation.es.events.ES_Event;
import fr.sorbonne_u.devs_simulation.models.events.EventI;
import fr.sorbonne_u.devs_simulation.models.interfaces.AtomicModelI;
import fr.sorbonne_u.devs_simulation.models.time.Time;

public class StopAdjustingLighting extends ES_Event implements SmartLightingEventI {
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
        if (e instanceof TurnOffSmartLighting || e instanceof SetPowerSmartLighting) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void executeOn(AtomicModelI model) {
        assert model instanceof SmartLightingElectricityModel ||
               model instanceof SmartLightingIlluminanceModel;
        new AssertionError(
                "Precondition violation: model instanceof "
                + "HeaterElectricityModel || "
                + "HeaterIlluminanceModel"
        );

        if (model instanceof SmartLightingElectricityModel) {
            SmartLightingElectricityModel m = (SmartLightingElectricityModel) model;
            assert m.getState() == SmartLightingElectricityModel.State.INCREASE ||
                   m.getState() == SmartLightingElectricityModel.State.DECREASE;
            new AssertionError(
                    "Precondition violation: m.getState() == "
                    + "HeaterIlluminanceModel.State.ON or DECREASE but is "
                    + m.getState()
            );

            m.setState(SmartLightingElectricityModel.State.ON, this.getTimeOfOccurrence());
        } else {
            SmartLightingIlluminanceModel m = (SmartLightingIlluminanceModel) model;
            assert m.getState() == SmartLightingIlluminanceModel.State.INCREASE ||
                   m.getState() == SmartLightingIlluminanceModel.State.DECREASE;
            new AssertionError(
                    "Precondition violation: m.getState() == "
                    + "HeaterIlluminanceModel.State.ON or DECREASE but is "
                    + m.getState()
            );

            m.setState(SmartLightingIlluminanceModel.State.ON);
        }
    }
}
