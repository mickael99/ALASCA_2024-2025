package fr.sorbonne_u.components.equipments.fridge.interfaces;

public interface FridgeUserAndControlI extends FridgeUserExternalAndInternalControlI{
	
	double getTargetTemperature() throws Exception;
    double getCurrentTemperature() throws Exception;
}
