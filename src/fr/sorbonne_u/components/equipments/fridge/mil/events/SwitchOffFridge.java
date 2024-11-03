package fr.sorbonne_u.components.equipments.fridge.mil.events;

import fr.sorbonne_u.components.equipments.fridge.mil.FridgeElectricityModel;
import fr.sorbonne_u.components.equipments.fridge.mil.FridgeTemperatureModel;
import fr.sorbonne_u.devs_simulation.es.events.ES_Event;
import fr.sorbonne_u.devs_simulation.models.events.EventI;
import fr.sorbonne_u.devs_simulation.models.interfaces.AtomicModelI;
import fr.sorbonne_u.devs_simulation.models.time.Time;

public class SwitchOffFridge extends ES_Event implements FridgeEventI {
	
	private static final long serialVersionUID = 1L;

	public SwitchOffFridge(Time timeOfOccurrence) {
		super(timeOfOccurrence, null);
	}

	@Override
	public boolean hasPriorityOver(EventI e) {
		return false;
	}

	@Override
	public void executeOn(AtomicModelI model) {
		assert	model instanceof FridgeElectricityModel ||
									model instanceof FridgeElectricityModel :
				new AssertionError(
						"Precondition violation: model instanceof "
						+ "HeaterElectricityModel || "
						+ "model instanceof HeaterTemperatureModel");

		if (model instanceof FridgeElectricityModel) {
			FridgeElectricityModel fridge = (FridgeElectricityModel)model;
			assert	fridge.getState() != FridgeElectricityModel.State.ON :
				new AssertionError(
						"model not in the right state, should not be "
								+ "FridgeElectricityModel.State.ON but is "
								+ fridge.getState());
			fridge.setState(FridgeElectricityModel.State.OFF,
							this.getTimeOfOccurrence());
		} 
		else {
			FridgeTemperatureModel heater = (FridgeTemperatureModel)model;
			heater.setState(FridgeTemperatureModel.State.NOT_COOLING);
		}
	}
}
