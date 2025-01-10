package fr.sorbonne_u.components.equipments.fridge.sil;

import fr.sorbonne_u.components.interfaces.OfferedCI;
import fr.sorbonne_u.components.interfaces.RequiredCI;

public interface FridgeActuatorCI extends OfferedCI, RequiredCI {

	public void startCooling() throws Exception;
	public void	stopCooling() throws Exception;
	public void closeDoor() throws Exception;
}
