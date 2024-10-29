package fr.sorbonne_u.components.equipments.battery;

import fr.sorbonne_u.components.connectors.AbstractConnector;

public class BatteryConnector extends AbstractConnector implements BatteryCI {
	
	@Override
	public STATE getState() throws Exception{
		return ((BatteryCI)this.offering).getState();
	}

	@Override
	public void setState(STATE state) throws Exception {
		((BatteryCI)this.offering).setState(state);
	}
}
