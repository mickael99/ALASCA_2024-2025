package fr.sorbonne_u.components.equipments.windTurbine;

import fr.sorbonne_u.components.ComponentI;
import fr.sorbonne_u.components.ports.AbstractInboundPort;

public class WindTurbineInboundPort extends AbstractInboundPort implements WindTurbineCI {

	private static final long serialVersionUID = 1L;

	public WindTurbineInboundPort(ComponentI owner) throws Exception {
		super(WindTurbineCI.class, owner);
	}

	public WindTurbineInboundPort(String uri, ComponentI owner) throws Exception {
		super(uri, WindTurbineCI.class, owner);
	}

	@Override
	public boolean isActivate() throws Exception {
		return this.getOwner().handleRequest(
				o -> ((WindTurbineI)o).isActivate());
	}

	@Override
	public void activate() throws Exception {
		this.getOwner().handleRequest(
				o -> {	((WindTurbineI)o).activate();
						return null;
				});
	}

	@Override
	public void stop() throws Exception {
		this.getOwner().handleRequest(
				o -> {	((WindTurbineI)o).stop();
						return null;
				});
	}

	@Override
	public double getCurrentProduction() throws Exception {
		return this.getOwner().handleRequest(
				o -> ((WindTurbineI)o).getCurrentProduction());
	}
}
