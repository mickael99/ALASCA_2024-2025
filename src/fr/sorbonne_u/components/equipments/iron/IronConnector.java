package fr.sorbonne_u.components.equipments.iron;

import fr.sorbonne_u.components.connectors.AbstractConnector;

public class IronConnector extends AbstractConnector implements IronUserCI {

	@Override
	public IronState getState() throws Exception {
		return ((IronUserCI)this.offering).getState();
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
	public IronTemperature getTemperature() throws Exception {
		return ((IronUserCI)this.offering).getTemperature();
	}

	@Override
	public void setTemperature(IronTemperature t) throws Exception {
		((IronUserCI)this.offering).setTemperature(t);
	}

	@Override
	public IronSteam getSteam() throws Exception {
		return ((IronUserCI)this.offering).getSteam();
	}

	@Override
	public void setSteam(IronSteam s) throws Exception {
		((IronUserCI)this.offering).setSteam(s);
	}

	@Override
	public IronEnergySavingMode getEnergySavingMode() throws Exception {
		return ((IronUserCI)this.offering).getEnergySavingMode();
	}

	@Override
	public void setEnergySavingMode(IronEnergySavingMode e) throws Exception {
		((IronUserCI)this.offering).setEnergySavingMode(e);
	}

}
