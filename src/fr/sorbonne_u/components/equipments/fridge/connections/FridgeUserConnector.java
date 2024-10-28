package fr.sorbonne_u.components.equipments.fridge.connections;

import fr.sorbonne_u.components.connectors.AbstractConnector;
import fr.sorbonne_u.components.equipments.fridge.Fridge.FridgeState;
import fr.sorbonne_u.components.equipments.fridge.interfaces.FridgeUserCI;

public class FridgeUserConnector extends AbstractConnector implements FridgeUserCI {

	@Override
	public FridgeState getState() throws Exception {
		return ((FridgeUserCI)this.offering).getState();
	}

	@Override
	public void switchOn() throws Exception {
		((FridgeUserCI)this.offering).switchOn();
	}

	@Override
	public void switchOff() throws Exception {
		((FridgeUserCI)this.offering).switchOff();
	}

	@Override
	public double getMaxCoolingPower() throws Exception {
		return ((FridgeUserCI)this.offering).getMaxCoolingPower();
	}

	@Override
	public double getCurrentCoolingPower() throws Exception {
		return ((FridgeUserCI)this.offering).getCurrentCoolingPower();
	}

	@Override
	public void setCurrentCoolingPower(double power) throws Exception {
		((FridgeUserCI)this.offering).setCurrentCoolingPower(power);
	}

	@Override
	public void setTargetTemperature(double temperature) throws Exception {
		((FridgeUserCI)this.offering).setTargetTemperature(temperature);
	}

	@Override
	public double getTargetTemperature() throws Exception {
		return ((FridgeUserCI)this.offering).getTargetTemperature();
	}

	@Override
	public double getCurrentTemperature() throws Exception {
		return ((FridgeUserCI)this.offering).getCurrentTemperature();
	}

}
