package fr.sorbonne_u.components.equipments.toaster.mil.events;

import fr.sorbonne_u.components.equipments.toaster.mil.ToasterElectricityModel;
import fr.sorbonne_u.devs_simulation.es.events.ES_Event;
import fr.sorbonne_u.devs_simulation.models.events.EventInformationI;
import fr.sorbonne_u.devs_simulation.models.interfaces.AtomicModelI;
import fr.sorbonne_u.devs_simulation.models.time.Time;

public class AbstractToasterEvent extends ES_Event {

    //----------------------------------------------------------------------------
    // Constants and variables
    //----------------------------------------------------------------------------

    private static final long serialVersionUID = 1L;

    //----------------------------------------------------------------------------
    // Constructors
    //----------------------------------------------------------------------------

    public AbstractToasterEvent(Time timeOfOccurrence,
                                EventInformationI content
    ) {
        super(timeOfOccurrence, content);
    }
}
// -----------------------------------------------------------------------------
