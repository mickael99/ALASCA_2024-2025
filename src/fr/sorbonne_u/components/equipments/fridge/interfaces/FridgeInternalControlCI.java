package fr.sorbonne_u.components.equipments.fridge.interfaces;

import fr.sorbonne_u.components.interfaces.OfferedCI;
import fr.sorbonne_u.components.interfaces.RequiredCI;

public interface FridgeInternalControlCI extends FridgeInternalControlI, OfferedCI, RequiredCI {
	
	boolean isCooling() throws Exception; 
    void startCooling() throws Exception; 
    void stopCooling() throws Exception; 
        
    double getTargetTemperature() throws Exception;
    double getCurrentTemperature() throws Exception;
}
