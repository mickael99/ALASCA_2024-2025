package fr.sorbonne_u.components.equipments.iron.mil.events;

import fr.sorbonne_u.components.equipments.iron.mil.IronElectricityModel;
import fr.sorbonne_u.devs_simulation.models.events.EventI;
import fr.sorbonne_u.devs_simulation.models.interfaces.AtomicModelI;
import fr.sorbonne_u.devs_simulation.models.time.Time;

public class TurnOnIron extends AbstractIronEvent {

	private static final long serialVersionUID = 1L;

	public TurnOnIron(Time timeOfOccurrence) {
		super(timeOfOccurrence, null);
	}

	@Override
	public boolean hasPriorityOver(EventI e) {
		return true;
	}

	@Override
	public void	executeOn(AtomicModelI model) {
		assert	model instanceof IronElectricityModel :
				new AssertionError(
						"Precondition violation: model instanceof "
						+ "IronElectricityModel");

		IronElectricityModel m = (IronElectricityModel)model;
		if (m.getState() == IronElectricityModel.IronState.OFF) {
			m.setState(IronElectricityModel.IronState.DELICATE);
			m.toggleConsumptionHasChanged();
		}
	}
}
