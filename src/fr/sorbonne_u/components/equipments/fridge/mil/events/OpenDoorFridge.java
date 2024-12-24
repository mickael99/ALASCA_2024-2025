package fr.sorbonne_u.components.equipments.fridge.mil.events;

import fr.sorbonne_u.components.equipments.fridge.mil.FridgeElectricityModel;
import fr.sorbonne_u.devs_simulation.es.events.ES_Event;
import fr.sorbonne_u.devs_simulation.models.events.EventI;
import fr.sorbonne_u.devs_simulation.models.interfaces.AtomicModelI;
import fr.sorbonne_u.devs_simulation.models.time.Time;

public class OpenDoorFridge extends ES_Event implements FridgeEventI {

	private static final long serialVersionUID = 1L;

	public OpenDoorFridge(Time timeOfOccurrence) {
		super(timeOfOccurrence, null);
	}

	@Override
	public boolean hasPriorityOver(EventI e) {
		if(e instanceof SwitchOnFridge || e instanceof DoNotCoolFridge || e instanceof CoolFridge)
			return false;
		return true;
	}

	@Override
	public void	executeOn(AtomicModelI model) {
		assert	model instanceof FridgeElectricityModel :
				new AssertionError(
						"Precondition violation: model instanceof "
						+ "FridgeElectricityModel");

		FridgeElectricityModel fridge = (FridgeElectricityModel)model;
		
		fridge.setState(FridgeElectricityModel.State.DOOR_OPEN, this.getTimeOfOccurrence());
	}
}