package fr.sorbonne_u.components.equipments.fridge.sil;

import fr.sorbonne_u.components.interfaces.DataRequiredCI;

public interface FridgePushImplementationI {
	public void receiveDataFromHeater(DataRequiredCI.DataI sd);
}
