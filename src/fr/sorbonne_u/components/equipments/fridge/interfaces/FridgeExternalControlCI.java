package fr.sorbonne_u.components.equipments.fridge.interfaces;

import fr.sorbonne_u.components.interfaces.OfferedCI;
import fr.sorbonne_u.components.interfaces.RequiredCI;

public interface FridgeExternalControlCI extends FridgeUserAndExternalControlI, OfferedCI, RequiredCI {

	void close() throws Exception; 
	
    double getMaxCoolingPower() throws Exception; 
    
    double getCurrentCoolingPower() throws Exception;
    void setCurrentCoolingPower(double power) throws Exception;
    
    boolean isAlarmTriggered() throws Exception;
    
    void setTargetTemperature(double temperature) throws Exception;
    
    public double getTargetTemperature() throws Exception;
    public double getCurrentTemperature() throws Exception;
}
