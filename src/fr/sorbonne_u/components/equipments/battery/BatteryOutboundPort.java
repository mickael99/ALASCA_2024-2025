package fr.sorbonne_u.components.equipments.battery;

import fr.sorbonne_u.components.ComponentI;
import fr.sorbonne_u.components.ports.AbstractOutboundPort;

public class BatteryOutboundPort extends AbstractOutboundPort implements BatteryCI {

	private static final long serialVersionUID = 1L;

	public BatteryOutboundPort(ComponentI owner) throws Exception {
		super(BatteryCI.class, owner);
	}

	public BatteryOutboundPort(String uri, ComponentI owner) throws Exception {
		super(uri, BatteryCI.class, owner);
	}

	@Override
	public STATE getState() throws Exception {
		return ((BatteryCI)this.getConnector()).getState();
	}

	@Override
	public void setState(STATE state) throws Exception {
		((BatteryCI)this.getConnector()).setState(state);
	}
	
	@Override
	public double getBatteryLevel() throws Exception {
		return ((BatteryCI)this.getConnector()).getBatteryLevel();
	}
}
