package fr.sorbonne_u.components.equipments.windTurbine.mil.events;

import fr.sorbonne_u.components.equipments.windTurbine.WindTurbineI.WindTurbineState;
import fr.sorbonne_u.components.equipments.windTurbine.mil.WindTurbineOperationI;
import fr.sorbonne_u.devs_simulation.models.events.EventI;
import fr.sorbonne_u.devs_simulation.models.interfaces.AtomicModelI;
import fr.sorbonne_u.devs_simulation.models.time.Time;
import fr.sorbonne_u.exceptions.PreconditionException;

public class StartWindTurbineEvent extends AbstractWindTurbineEvent {

	private static final long serialVersionUID = 1L;

	public StartWindTurbineEvent(Time timeOfOccurrence) {
		super(timeOfOccurrence, null);
	}
	
	@Override
    public boolean hasPriorityOver(EventI e) {
        return false;
    }

    @Override
    public void executeOn(AtomicModelI model) {
    	assert model instanceof WindTurbineOperationI;
    	WindTurbineOperationI w = (WindTurbineOperationI)model;
    	
    	w.activate();
    }
}
