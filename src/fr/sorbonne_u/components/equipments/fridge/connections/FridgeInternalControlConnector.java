package fr.sorbonne_u.components.equipments.fridge.connections;

import fr.sorbonne_u.components.connectors.AbstractConnector;
import fr.sorbonne_u.components.equipments.fridge.interfaces.FridgeInternalControlCI;
import fr.sorbonne_u.components.equipments.fridge.interfaces.FridgeInternalControlI;

public class FridgeInternalControlConnector extends AbstractConnector implements FridgeInternalControlCI {

	@Override
	public boolean isCooling() throws Exception {
		return ((FridgeInternalControlCI)this.offering).isCooling();
	}

	@Override
	public void startCooling() throws Exception {
		((FridgeInternalControlI)this.offering).startCooling();
	}

	@Override
	public void stopCooling() throws Exception {
		((FridgeInternalControlI)this.offering).stopCooling();
	}
	
	@Override
	public double getTargetTemperature() throws Exception {
		return ((FridgeInternalControlI)this.offering).getTargetTemperature();
	}

	@Override
	public double getCurrentTemperature() throws Exception {
		return ((FridgeInternalControlI)this.offering).getCurrentTemperature();
	}

	@Override
	public void closeDoor() throws Exception {
		((FridgeInternalControlI)this.offering).closeDoor();
	}

}
