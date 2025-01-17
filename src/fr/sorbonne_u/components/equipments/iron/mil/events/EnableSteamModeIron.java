package fr.sorbonne_u.components.equipments.iron.mil.events;

import fr.sorbonne_u.components.equipments.iron.mil.IronOperationI;
import fr.sorbonne_u.devs_simulation.models.events.EventI;
import fr.sorbonne_u.devs_simulation.models.interfaces.AtomicModelI;
import fr.sorbonne_u.devs_simulation.models.time.Time;

public class EnableSteamModeIron extends AbstractIronEvent {
	
	private static final long serialVersionUID = 1L;

	public EnableSteamModeIron(Time timeOfOccurrence) {
		super(timeOfOccurrence, null);
	}

	@Override
	public boolean	hasPriorityOver(EventI e)
	{
		if(e instanceof TurnOnIron || e instanceof EnableDelicateModeIron
				|| e instanceof EnableCottonModeIron || e instanceof EnableLinenModeIron) 
		{
			return false;
		}
		
		return true;
	}

	@Override
	public void	executeOn(AtomicModelI model)
	{
		assert	model instanceof IronOperationI :
			new AssertionError(
					"Precondition violation: model instanceof "
					+ "IronOperationI");
		
		((IronOperationI)model).enableSteamMode();
	}
}
