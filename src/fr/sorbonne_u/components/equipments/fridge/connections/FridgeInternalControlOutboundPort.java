package fr.sorbonne_u.components.equipments.fridge.connections;

import fr.sorbonne_u.components.ComponentI;
import fr.sorbonne_u.components.equipments.fridge.interfaces.FridgeInternalControlCI;
import fr.sorbonne_u.components.ports.AbstractOutboundPort;

public class FridgeInternalControlOutboundPort extends AbstractOutboundPort implements FridgeInternalControlCI {

	// -------------------------------------------------------------------------
	// Constants and variables
	// -------------------------------------------------------------------------

	private static final long serialVersionUID = 1L;

	
	// -------------------------------------------------------------------------
	// Constructors
	// -------------------------------------------------------------------------

	public FridgeInternalControlOutboundPort(ComponentI owner) throws Exception {
		super(FridgeInternalControlCI.class, owner);
	}

	public FridgeInternalControlOutboundPort(String uri, ComponentI owner) throws Exception {
		super(uri, FridgeInternalControlCI.class, owner);
	}

	
	// -------------------------------------------------------------------------
	// Methods
	// -------------------------------------------------------------------------
	
	@Override
	public boolean isCooling() throws Exception {
		return ((FridgeInternalControlCI)this.getConnector()).isCooling();
	}

	@Override
	public void startCooling() throws Exception {
		((FridgeInternalControlCI)this.getConnector()).startCooling();
	}

	@Override
	public void stopCooling() throws Exception {
		((FridgeInternalControlCI)this.getConnector()).stopCooling();
	}

	@Override
	public double getTargetTemperature() throws Exception {
		return ((FridgeInternalControlCI)this.getConnector()).getTargetTemperature();

	}

	@Override
	public double getCurrentTemperature() throws Exception {
		return ((FridgeInternalControlCI)this.getConnector()).getCurrentTemperature();

	}

	@Override
	public void closeDoor() throws Exception {
		((FridgeInternalControlCI)this.getConnector()).closeDoor();
	}

}
