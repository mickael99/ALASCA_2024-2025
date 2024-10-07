package fr.sorbonne_u.components.equipments.iron;

import fr.sorbonne_u.components.ComponentI;
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
	public IronTemperature getTemperature() throws Exception {
		return ((IronUserCI)this.getConnector()).getTemperature();	
	}

	@Override
	public void setTemperature(IronTemperature t) throws Exception {
		((IronUserCI)this.getConnector()).setTemperature(t);
	}

	@Override
	public IronSteam getSteam() throws Exception {
		return ((IronUserCI)this.getConnector()).getSteam();
	}

	@Override
	public void setSteam(IronSteam s) throws Exception {
		((IronUserCI)this.getConnector()).setSteam(s);
	}

	@Override
	public IronEnergySavingMode getEnergySavingMode() throws Exception {
		return ((IronUserCI)this.getConnector()).getEnergySavingMode();
	}

	@Override
	public void setEnergySavingMode(IronEnergySavingMode e) throws Exception {
		((IronUserCI)this.getConnector()).setEnergySavingMode(e);
	}

}
