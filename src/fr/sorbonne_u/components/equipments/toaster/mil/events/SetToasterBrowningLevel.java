package fr.sorbonne_u.components.equipments.toaster.mil.events;

import fr.sorbonne_u.components.equipments.toaster.mil.ToasterElectricityModel;
import fr.sorbonne_u.devs_simulation.es.events.ES_Event;
import fr.sorbonne_u.devs_simulation.models.events.EventI;
import fr.sorbonne_u.devs_simulation.models.events.EventInformationI;
import fr.sorbonne_u.devs_simulation.models.interfaces.AtomicModelI;
import fr.sorbonne_u.devs_simulation.models.time.Time;

public class SetToasterBrowningLevel extends ES_Event implements ToasterEventI {

    // -------------------------------------------------------------------------
    // Inner types and classes
    // -------------------------------------------------------------------------
    public static class BrowningLevelValue implements EventInformationI{
        private static final long serialVersionUID = 1L;
        protected final ToasterElectricityModel.ToasterBrowningLevel browningLevel;

        public BrowningLevelValue(ToasterElectricityModel.ToasterBrowningLevel browningLevel) {
            super();
            this.browningLevel = browningLevel;
        }

        public ToasterElectricityModel.ToasterBrowningLevel getBrowningLevel() {
            return this.browningLevel;
        }

        @Override
        public String toString() {
            StringBuffer sb = new StringBuffer(this.getClass().getSimpleName());
            sb.append('[');
            sb.append(this.browningLevel);
            sb.append(']');
            return sb.toString();
        }
    }

    // -------------------------------------------------------------------------
    // Constants and variables
    // -------------------------------------------------------------------------
    private static final long serialVersionUID = 1L;
    protected final BrowningLevelValue browningLevel;

    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------
    public SetToasterBrowningLevel(Time timeOfOccurrence, EventInformationI content) {
        super(timeOfOccurrence, content);

        assert content != null && content instanceof BrowningLevelValue :
                new AssertionError(
                        "Precondition violation: event content is null or"
                        + " not a BrowningLevelValue " + content);

        this.browningLevel = (BrowningLevelValue) content;
    }

    // -------------------------------------------------------------------------
    // Methods
    // -------------------------------------------------------------------------

    public boolean hasPriorityOver(EventI model) {
        if (model instanceof TurnOffToaster) {
            return true;
        } else {
            return false;
        }
    }

    public void executeOn(AtomicModelI modelI){
        assert modelI instanceof ToasterElectricityModel :
                new AssertionError(
                        "Precondition violation: model instanceof "
                                + "ToasterElectricityModel");
        ToasterElectricityModel model = (ToasterElectricityModel) modelI;
        assert model.getToasterState() == ToasterElectricityModel.ToasterState.ON :
                new AssertionError(
                        "model not in the right state, should be "
                                + "ToasterElectricityModel.ToasterState.ON but is "
                                + model.getToasterState());
        model.setToasterBrowningLevel(this.browningLevel.getBrowningLevel(), this.getTimeOfOccurrence());
    }
}
