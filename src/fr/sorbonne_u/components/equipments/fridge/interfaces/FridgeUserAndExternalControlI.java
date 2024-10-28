package fr.sorbonne_u.components.equipments.fridge.interfaces;

public interface FridgeUserAndExternalControlI {
	
    double getMaxCoolingPower() throws Exception;
    double getCurrentCoolingPower() throws Exception;
    void setCurrentCoolingPower(double power) throws Exception;
    void setTargetTemperature(double temperature) throws Exception;
        
    double getTargetTemperature() throws Exception;
    double getCurrentTemperature() throws Exception;
}
