package fr.sorbonne_u.components.equipments.battery;

import fr.sorbonne_u.components.ComponentI;
import fr.sorbonne_u.components.ports.AbstractInboundPort;

public class BatteryInboundPort extends AbstractInboundPort implements BatteryCI {
	
	private static final long serialVersionUID = 1L;

	public BatteryInboundPort(ComponentI owner) throws Exception {
		super(BatteryCI.class, owner);
	}

	public BatteryInboundPort(String uri, ComponentI owner) throws Exception {
		super(uri, BatteryCI.class, owner);
	}

	@Override
	public BATTERY_STATE getState() throws Exception{
		return this.getOwner().handleRequest(
				o -> ((BatteryI)o).getState());
	}

	@Override
	public void setState(BATTERY_STATE state) throws Exception {
		this.getOwner().handleRequest(
				o -> {	((BatteryI)o).setState(state);
						return null;
				});
	}
	
	@Override
	public double getBatteryLevel() throws Exception {
		return this.getOwner().handleRequest(
				o -> ((BatteryI)o).getBatteryLevel());
	}
}
