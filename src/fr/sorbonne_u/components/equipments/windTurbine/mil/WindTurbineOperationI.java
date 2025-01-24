package fr.sorbonne_u.components.equipments.windTurbine.mil;

import fr.sorbonne_u.components.equipments.windTurbine.WindTurbineI.WindTurbineState;

public interface WindTurbineOperationI {
	public void activate();
	public void stop();
	public WindTurbineState getState();
}
