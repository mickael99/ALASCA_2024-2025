package fr.sorbonne_u.components.equipments.fridge.mil.events;

import fr.sorbonne_u.components.equipments.fridge.mil.FridgeElectricityModel;
import fr.sorbonne_u.devs_simulation.es.events.ES_Event;
import fr.sorbonne_u.devs_simulation.models.events.EventI;
import fr.sorbonne_u.devs_simulation.models.interfaces.AtomicModelI;
import fr.sorbonne_u.devs_simulation.models.time.Time;

public class SwitchOnFridge extends ES_Event implements FridgeEventI {
	
	private static final long serialVersionUID = 1L;

	public SwitchOnFridge(Time timeOfOccurrence) {
		super(timeOfOccurrence, null);
	}

	@Override
	public boolean hasPriorityOver(EventI e) {
		return true;
	}

	@Override
	public void	executeOn(AtomicModelI model) {
		assert	model instanceof FridgeElectricityModel :
				new AssertionError(
						"Precondition violation: model instanceof "
						+ "HeaterElectricityModel");

		FridgeElectricityModel fridge = (FridgeElectricityModel)model;
		assert	fridge.getState() == FridgeElectricityModel.State.OFF :
				new AssertionError(
						"model not in the right state, should be "
						+ "FridgeElectricityModel.State.OFF but is "
						+ fridge.getState());
		fridge.setState(FridgeElectricityModel.State.ON,
						this.getTimeOfOccurrence());
	}
}
