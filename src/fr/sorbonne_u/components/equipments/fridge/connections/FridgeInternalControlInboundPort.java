package fr.sorbonne_u.components.equipments.fridge.connections;

import fr.sorbonne_u.components.ComponentI;
import fr.sorbonne_u.components.equipments.fridge.interfaces.FridgeInternalControlCI;
import fr.sorbonne_u.components.equipments.fridge.interfaces.FridgeInternalControlI;
import fr.sorbonne_u.components.equipments.fridge.interfaces.FridgeUserAndControlI;
import fr.sorbonne_u.components.ports.AbstractInboundPort;

public class FridgeInternalControlInboundPort extends AbstractInboundPort implements FridgeInternalControlCI {

	
	// -------------------------------------------------------------------------
	// Constants and variables
	// -------------------------------------------------------------------------

	private static final long serialVersionUID = 1L;

	
	// -------------------------------------------------------------------------
	// Constructors
	// -------------------------------------------------------------------------

	public FridgeInternalControlInboundPort(ComponentI owner) throws Exception {
		super(FridgeInternalControlCI.class, owner);

		assert	owner instanceof FridgeInternalControlI;
		assert	owner instanceof FridgeUserAndControlI;
	}

	public FridgeInternalControlInboundPort(String uri, ComponentI owner) throws Exception {
		super(uri, FridgeInternalControlCI.class, owner);

		assert	owner instanceof FridgeInternalControlI;
		assert	owner instanceof FridgeUserAndControlI;
	}
	
	
	// -------------------------------------------------------------------------
	// Methods
	// -------------------------------------------------------------------------
	
	@Override
	public boolean isCooling() throws Exception {
		return this.getOwner().handleRequest(o -> ((FridgeInternalControlI)o).isCooling());
	}

	@Override
	public void startCooling() throws Exception {
		this.getOwner().handleRequest(
				o -> {	((FridgeInternalControlI)o).startCooling();
						return null;
				});
	}

	@Override
	public void stopCooling() throws Exception {
		this.getOwner().handleRequest(
				o -> {	((FridgeInternalControlI)o).stopCooling();
						return null;
				});
	}

	@Override
	public double getTargetTemperature() throws Exception {
		return this.getOwner().handleRequest(o -> ((FridgeInternalControlI)o).getTargetTemperature());
	}

	@Override
	public double getCurrentTemperature() throws Exception {
		return this.getOwner().handleRequest(o -> ((FridgeInternalControlI)o).getCurrentTemperature());
	}

}
