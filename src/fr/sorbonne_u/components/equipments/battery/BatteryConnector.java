package fr.sorbonne_u.components.equipments.battery;

import fr.sorbonne_u.components.connectors.AbstractConnector;

public class BatteryConnector extends AbstractConnector implements BatteryCI {
	
	@Override
	public BATTERY_STATE getState() throws Exception{
		return ((BatteryCI)this.offering).getState();
	}

	@Override
	public void setState(BATTERY_STATE state) throws Exception {
		((BatteryCI)this.offering).setState(state);
	}
	
	@Override
	public double getBatteryLevel() throws Exception {
		return ((BatteryCI)this.offering).getBatteryLevel();
	}
}
