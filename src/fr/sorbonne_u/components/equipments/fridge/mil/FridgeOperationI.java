package fr.sorbonne_u.components.equipments.fridge.mil;

import fr.sorbonne_u.components.equipments.fridge.mil.FridgeElectricityModel.FridgeState;
import fr.sorbonne_u.devs_simulation.models.time.Time;

public interface FridgeOperationI {
	public void setCurrentCoolingPower(double newPower, Time t);
	public void setState(FridgeState s);
	public FridgeState getState();
}
