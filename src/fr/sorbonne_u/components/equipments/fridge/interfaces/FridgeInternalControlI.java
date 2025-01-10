package fr.sorbonne_u.components.equipments.fridge.interfaces;

public interface FridgeInternalControlI extends FridgeUserAndControlI {
	
	boolean isCooling() throws Exception;
    void startCooling() throws Exception;
    void stopCooling() throws Exception;
	void closeDoor() throws Exception;
}
