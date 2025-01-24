package fr.sorbonne_u.components.equipments.windTurbine;

import fr.sorbonne_u.components.connectors.AbstractConnector;

public class WindTurbineConnector extends AbstractConnector implements WindTurbineCI {

	@Override
	public boolean isActivate() throws Exception {
		return ((WindTurbineCI)this.offering).isActivate();
	}

	@Override
	public void activate() throws Exception {
		((WindTurbineCI)this.offering).activate();
	}

	@Override
	public void stop() throws Exception {
		((WindTurbineCI)this.offering).stop();
	}
}
