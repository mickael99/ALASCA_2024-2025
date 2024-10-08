package fr.sorbonne_u.components.equipments.fridge.interfaces;

public interface FridgeUserAndExternalControlI extends FridgeUserExternalAndInternalControlI{
	
	void close() throws Exception;
    double getMaxCoolingPower() throws Exception;
    double getCurrentCoolingPower() throws Exception;
    void setCurrentCoolingPower(double power) throws Exception;
    boolean isAlarmTriggered() throws Exception;
    void setTargetTemperature(double temperature) throws Exception;
    
    boolean isOpen() throws Exception;
    
    double getTargetTemperature() throws Exception;
    double getCurrentTemperature() throws Exception;
}
