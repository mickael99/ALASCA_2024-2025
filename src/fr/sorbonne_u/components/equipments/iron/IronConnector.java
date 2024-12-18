package fr.sorbonne_u.components.equipments.iron;

import fr.sorbonne_u.components.connectors.AbstractConnector;

public class IronConnector extends AbstractConnector implements IronUserCI {

	@Override
	public IronState getState() throws Exception {
		return ((IronUserCI)this.offering).getState();
	}
	
	@Override
	public boolean isTurnOn() throws Exception {
		return ((IronUserCI)this.offering).isTurnOn();	
	}

	@Override
	public void turnOn() throws Exception {
		((IronUserCI)this.offering).turnOn();
	}

	@Override
	public void turnOff() throws Exception {
		((IronUserCI)this.offering).turnOff();
	}

	@Override
	public void setState(IronState s) throws Exception {
		((IronUserCI)this.offering).setState(s);
	}

	@Override
	public boolean isSteamModeEnable() throws Exception {
		return ((IronUserCI)this.offering).isSteamModeEnable();
	}

	@Override
	public void EnableSteamMode() throws Exception {
		((IronUserCI)this.offering).EnableSteamMode();
	}

	@Override
	public void DisableSteamMode() throws Exception {
		((IronUserCI)this.offering).DisableSteamMode();
	}

	@Override
	public boolean isEnergySavingModeEnable() throws Exception {
		return ((IronUserCI)this.offering).isEnergySavingModeEnable();
	}

	@Override
	public void EnableEnergySavingMode() throws Exception {
		((IronUserCI)this.offering).EnableEnergySavingMode();
	}

	@Override
	public void DisableEnergySavingMode() throws Exception {
		((IronUserCI)this.offering).DisableEnergySavingMode();
	}
}
