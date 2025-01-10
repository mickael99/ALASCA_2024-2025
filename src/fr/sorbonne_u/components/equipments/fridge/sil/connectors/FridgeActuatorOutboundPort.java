package fr.sorbonne_u.components.equipments.fridge.sil.connectors;

import fr.sorbonne_u.components.ComponentI;
import fr.sorbonne_u.components.equipments.fridge.sil.FridgeActuatorCI;
import fr.sorbonne_u.components.ports.AbstractOutboundPort;

public class FridgeActuatorOutboundPort extends AbstractOutboundPort implements FridgeActuatorCI {

	private static final long serialVersionUID = 1L;

	public FridgeActuatorOutboundPort(ComponentI owner) throws Exception {
		super(FridgeActuatorCI.class, owner);
	}

	public FridgeActuatorOutboundPort(String uri, ComponentI owner) throws Exception {
		super(uri, FridgeActuatorCI.class, owner);
	}
	
	@Override
	public void startCooling() throws Exception {
		((FridgeActuatorCI)this.getConnector()).startCooling();
	}

	@Override
	public void stopCooling() throws Exception {
		((FridgeActuatorCI)this.getConnector()).stopCooling();
	}

	@Override
	public void closeDoor() throws Exception {
		((FridgeActuatorCI)this.getConnector()).closeDoor();
	}

}
