package fr.sorbonne_u.components.equipments.iron.connections;

import fr.sorbonne_u.components.ComponentI;
import fr.sorbonne_u.components.equipments.iron.interfaces.IronUserCI;
import fr.sorbonne_u.components.ports.AbstractOutboundPort;

public class IronOutboundPort extends AbstractOutboundPort implements IronUserCI {

	private static final long serialVersionUID = 1L;

	public	IronOutboundPort(ComponentI owner) throws Exception {
		super(IronUserCI.class, owner);
	}

	public IronOutboundPort(String uri, ComponentI owner) throws Exception {
		super(uri, IronUserCI.class, owner);
	}
	
	@Override
	public IronState getState() throws Exception {
		return ((IronUserCI)this.getConnector()).getState();
	}

	@Override
	public void turnOn() throws Exception {
		((IronUserCI)this.getConnector()).turnOn();
	}

	@Override
	public void turnOff() throws Exception {
		((IronUserCI)this.getConnector()).turnOff();
	}

	@Override
	public boolean isTurnOn() throws Exception {
		return ((IronUserCI)this.getConnector()).isTurnOn();
	}

	@Override
	public void setState(IronState s) throws Exception {
		((IronUserCI)this.getConnector()).setState(s);
	}

	@Override
	public boolean isSteamModeEnable() throws Exception {
		return ((IronUserCI)this.getConnector()).isSteamModeEnable();
	}

	@Override
	public void EnableSteamMode() throws Exception {
		((IronUserCI)this.getConnector()).EnableSteamMode();
	}

	@Override
	public void DisableSteamMode() throws Exception {
		((IronUserCI)this.getConnector()).DisableSteamMode();
	}

	@Override
	public boolean isEnergySavingModeEnable() throws Exception {
		return ((IronUserCI)this.getConnector()).isEnergySavingModeEnable();
	}

	@Override
	public void EnableEnergySavingMode() throws Exception {
		((IronUserCI)this.getConnector()).EnableEnergySavingMode();
	}

	@Override
	public void DisableEnergySavingMode() throws Exception {
		((IronUserCI)this.getConnector()).DisableEnergySavingMode();
	}

}
