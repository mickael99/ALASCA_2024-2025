package fr.sorbonne_u.components.equipments.fridge.interfaces;

public interface FridgeUserAndControlI {
	
	double getTargetTemperature() throws Exception;
    double getCurrentTemperature() throws Exception;
}
