package fr.sorbonne_u.components.equipments.fridge.mil.events;

import fr.sorbonne_u.components.equipments.fridge.mil.FridgeElectricityModel;
import fr.sorbonne_u.components.equipments.fridge.mil.FridgeTemperatureModel;
import fr.sorbonne_u.devs_simulation.models.events.Event;
import fr.sorbonne_u.devs_simulation.models.events.EventI;
import fr.sorbonne_u.devs_simulation.models.interfaces.AtomicModelI;
import fr.sorbonne_u.devs_simulation.models.time.Time;

public class Cool extends Event implements FridgeEventI {

	private static final long serialVersionUID = 1L;

	public Cool(Time timeOfOccurrence) {
		super(timeOfOccurrence, null);
	}

	@Override
	public boolean hasPriorityOver(EventI e) {
		if (e instanceof SwitchOnFridge || e instanceof DoNotCool) 
			return false;
		else 
			return true;
	}

	@Override
	public void executeOn(AtomicModelI model) {
		assert	model instanceof FridgeElectricityModel ||
									model instanceof FridgeTemperatureModel :
				new AssertionError(
						"Precondition violation: model instanceof "
						+ "FridgeElectricityModel || "
						+ "model instanceof FridgeTemperatureModel");

		if (model instanceof FridgeElectricityModel) {
			FridgeElectricityModel fridge = (FridgeElectricityModel)model;
			assert	fridge.getState() == FridgeElectricityModel.State.ON:
					new AssertionError(
							"model not in the right state, should be "
							+ "FridgeElectricityModel.State.ON but is "
							+ fridge.getState());
			fridge.setState(FridgeElectricityModel.State.COOLING,
							this.getTimeOfOccurrence());
		} 
		else {
			FridgeTemperatureModel fridge = (FridgeTemperatureModel)model;
			assert	fridge.getState() == FridgeTemperatureModel.State.NOT_COOLING:
					new AssertionError(
							"model not in the right state, should be "
							+ "FridgeTemperatureModel.State.NOT_COOLING but is "
							+ fridge.getState());
			fridge.setState(FridgeTemperatureModel.State.COOLING);
		}
	}
}
