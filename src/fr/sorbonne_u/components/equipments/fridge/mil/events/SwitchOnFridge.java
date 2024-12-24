package fr.sorbonne_u.components.equipments.fridge.mil.events;

import fr.sorbonne_u.components.equipments.fridge.mil.FridgeOperationI;
import fr.sorbonne_u.components.equipments.fridge.mil.FridgeElectricityModel.FridgeState;
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
	public void executeOn(AtomicModelI model) {
		assert	model instanceof FridgeOperationI:
					new AssertionError(
						"Precondition violation: model instanceof "
						+ "FridgeOperationI");

		FridgeOperationI fridge = (FridgeOperationI)model;
		fridge.setState(FridgeState.ON);
	}
}
