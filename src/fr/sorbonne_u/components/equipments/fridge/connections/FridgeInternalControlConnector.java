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
	public boolean isOpen() throws Exception {
		return ((FridgeInternalControlI)this.offering).isOpen();
	}

	@Override
	public void triggeredAlarm() throws Exception {
		((FridgeInternalControlI)this.offering).triggeredAlarm();
	}

	@Override
	public void stopAlarm() throws Exception {
		((FridgeInternalControlCI)this.offering).stopAlarm();
	}

	@Override
	public double getTargetTemperature() throws Exception {
		return ((FridgeInternalControlI)this.offering).getTargetTemperature();
	}

	@Override
	public double getCurrentTemperature() throws Exception {
		return ((FridgeInternalControlI)this.offering).getCurrentTemperature();
	}

}
