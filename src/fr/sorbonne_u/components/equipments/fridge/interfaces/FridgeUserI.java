package fr.sorbonne_u.components.equipments.fridge.interfaces;

import fr.sorbonne_u.components.equipments.fridge.mil.FridgeElectricityModel.FridgeState;

public interface FridgeUserI extends FridgeUserAndControlI, FridgeUserAndExternalControlI {
	
	public FridgeState getState() throws Exception;
	public void switchOn() throws Exception;
	public void switchOff() throws Exception;
    
}
