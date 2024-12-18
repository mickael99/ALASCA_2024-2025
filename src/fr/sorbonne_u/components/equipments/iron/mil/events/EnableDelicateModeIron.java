package fr.sorbonne_u.components.equipments.iron.mil.events;

import fr.sorbonne_u.components.equipments.iron.IronImplementationI.IronState;
import fr.sorbonne_u.components.equipments.iron.mil.IronOperationI;
import fr.sorbonne_u.devs_simulation.models.events.EventI;
import fr.sorbonne_u.devs_simulation.models.interfaces.AtomicModelI;
import fr.sorbonne_u.devs_simulation.models.time.Time;

public class EnableDelicateModeIron extends AbstractIronEvent {
	
	private static final long serialVersionUID = 1L;

	public EnableDelicateModeIron(Time timeOfOccurrence) {
		super(timeOfOccurrence, null);
	}

	@Override
	public boolean hasPriorityOver(EventI e) {
		if(e instanceof TurnOnIron)
			return false;
		return true;
	}

	@Override
	public void	executeOn(AtomicModelI model) {
		assert	model instanceof IronOperationI :
			new AssertionError(
					"Precondition violation: model instanceof "
					+ "IronOperationI");

		((IronOperationI)model).setState(IronState.DELICATE);
	}
}
