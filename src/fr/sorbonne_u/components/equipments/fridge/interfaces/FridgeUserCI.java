package fr.sorbonne_u.components.equipments.fridge.interfaces;

import fr.sorbonne_u.components.equipments.fridge.Fridge.FridgeState;
import fr.sorbonne_u.components.interfaces.OfferedCI;
import fr.sorbonne_u.components.interfaces.RequiredCI;

public interface FridgeUserCI extends OfferedCI, RequiredCI, FridgeUserI {
	
	public FridgeState getState() throws Exception;
	public void switchOn() throws Exception;
	public void switchOff() throws Exception;
        
	public double getMaxCoolingPower() throws Exception;
	public double getCurrentCoolingPower() throws Exception;
	public void setCurrentCoolingPower(double power) throws Exception;
    
	public void setTargetTemperature(double temperature) throws Exception;
	public double getTargetTemperature() throws Exception;
    
	public double getCurrentTemperature() throws Exception;
}
