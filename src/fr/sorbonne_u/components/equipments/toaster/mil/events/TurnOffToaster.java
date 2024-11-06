package fr.sorbonne_u.components.equipments.toaster.mil.events;


import fr.sorbonne_u.components.equipments.toaster.mil.ToasterElectricityModel;
import fr.sorbonne_u.devs_simulation.models.events.EventI;
import fr.sorbonne_u.devs_simulation.models.interfaces.AtomicModelI;
import fr.sorbonne_u.devs_simulation.models.time.Time;

public class TurnOffToaster extends AbstractToasterEvent {
    // -------------------------------------------------------------------------
    // Constants and variables
    // -------------------------------------------------------------------------
    private static final long serialVersionUID = 1L;

    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------
    public TurnOffToaster(Time timeOfOccurrence) {
        super(timeOfOccurrence, null);
    }

    // -------------------------------------------------------------------------
    // Methods
    // -------------------------------------------------------------------------

    @Override
    public boolean hasPriorityOver(EventI e) {
        return false;
    }

    @Override
    public void executeOn(AtomicModelI model) {
        assert model instanceof ToasterElectricityModel :
                new AssertionError("Precondition violation: model instanceof "
                        + "ToasterElectricityModel");

        ToasterElectricityModel tem = (ToasterElectricityModel) model;
        if (tem.getToasterState() == ToasterElectricityModel.ToasterState.ON) {
            tem.setToasterState(ToasterElectricityModel.ToasterState.OFF);
            tem.toggleConsumptionHasChanged();
        }
    }
}
