package fr.sorbonne_u.components.equipments.windTurbine;

import fr.sorbonne_u.components.ComponentI;
import fr.sorbonne_u.components.ports.AbstractOutboundPort;

public class WindTurbineOutboundPort extends AbstractOutboundPort implements WindTurbineCI {

	private static final long serialVersionUID = 1L;

	public WindTurbineOutboundPort(ComponentI owner) throws Exception {
		super(WindTurbineCI.class, owner);
	}

	public WindTurbineOutboundPort(String uri, ComponentI owner) throws Exception {
		super(uri, WindTurbineCI.class, owner);
	}

	@Override
	public boolean isActivate() throws Exception {
		return ((WindTurbineCI)this.getConnector()).isActivate();
	}

	@Override
	public void activate() throws Exception {
		((WindTurbineCI)this.getConnector()).activate();
	}

	@Override
	public void stop() throws Exception {
		((WindTurbineCI)this.getConnector()).stop();
	}
}
