package fr.sorbonne_u.components.equipments.fridge.connections;

import fr.sorbonne_u.components.connectors.AbstractConnector;
import fr.sorbonne_u.components.equipments.fridge.interfaces.FridgeExternalControlCI;

public class FridgeExternalControlConnector extends AbstractConnector implements FridgeExternalControlCI{

	@Override
	public boolean isOpen() throws Exception {
		return ((FridgeExternalControlCI)this.offering).isOpen();
	}

	@Override
	public void close() throws Exception {
		((FridgeExternalControlCI)this.offering).close();
	}

	@Override
	public double getMaxCoolingPower() throws Exception {
		return ((FridgeExternalControlCI)this.offering).getMaxCoolingPower();
	}

	@Override
	public double getCurrentCoolingPower() throws Exception {
		return ((FridgeExternalControlCI)this.offering).getCurrentCoolingPower();
	}

	@Override
	public void setCurrentCoolingPower(double power) throws Exception {
		((FridgeExternalControlCI)this.offering).setCurrentCoolingPower(power);
	}

	@Override
	public boolean isAlarmTriggered() throws Exception {
		return ((FridgeExternalControlCI)this.offering).isAlarmTriggered();
	}

	@Override
	public void setTargetTemperature(double temperature) throws Exception {
		((FridgeExternalControlCI)this.offering).setTargetTemperature(temperature);
	}
	
	public double getTargetTemperature() throws Exception {
		return ((FridgeExternalControlCI)this.offering).getTargetTemperature();
	}
    public double getCurrentTemperature() throws Exception {
		return ((FridgeExternalControlCI)this.offering).getCurrentTemperature();
    }

}
