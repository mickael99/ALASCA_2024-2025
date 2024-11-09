package fr.sorbonne_u.components.equipments.windTurbine.mil.events;

import fr.sorbonne_u.components.equipments.windTurbine.mil.WindTurbineElectricityModel;
import fr.sorbonne_u.devs_simulation.models.events.EventI;
import fr.sorbonne_u.devs_simulation.models.interfaces.AtomicModelI;
import fr.sorbonne_u.devs_simulation.models.time.Time;

public class StartWindTurbineEvent extends AbstractWindTurbineEvent {

	private static final long serialVersionUID = 1L;

	public StartWindTurbineEvent(Time timeOfOccurrence) {
		super(timeOfOccurrence, null);
	}
	
	@Override
    public boolean hasPriorityOver(EventI e) {
        return true;
    }

    @Override
    public void executeOn(AtomicModelI model) {
    	assert model instanceof WindTurbineElectricityModel;
        WindTurbineElectricityModel m = (WindTurbineElectricityModel) model;
        
        if(m.getState() == WindTurbineElectricityModel.WindTurbineState.STANDBY) {
            m.setState(WindTurbineElectricityModel.WindTurbineState.ACTIVE);
            m.setHasChanged(true);
        }
    }
}
