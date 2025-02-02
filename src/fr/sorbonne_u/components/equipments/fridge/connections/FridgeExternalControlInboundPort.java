package fr.sorbonne_u.components.equipments.fridge.connections;

import fr.sorbonne_u.components.ComponentI;
import fr.sorbonne_u.components.equipments.fridge.interfaces.FridgeExternalControlCI;
import fr.sorbonne_u.components.equipments.fridge.interfaces.FridgeUserAndExternalControlI;
import fr.sorbonne_u.components.ports.AbstractInboundPort;

public class FridgeExternalControlInboundPort extends AbstractInboundPort implements FridgeExternalControlCI {

	// -------------------------------------------------------------------------
	// Constants and variables
	// -------------------------------------------------------------------------

	private static final long serialVersionUID = 1L;

	
	// -------------------------------------------------------------------------
	// Constructors
	// -------------------------------------------------------------------------

	public FridgeExternalControlInboundPort(ComponentI owner) throws Exception {
		super(FridgeExternalControlCI.class, owner);

		assert	owner instanceof FridgeUserAndExternalControlI;
	}

	public FridgeExternalControlInboundPort(String uri, ComponentI owner) throws Exception {
		super(uri, FridgeExternalControlCI.class, owner);

		assert	owner instanceof FridgeUserAndExternalControlI;
	}

	
	// -------------------------------------------------------------------------
	// Methods
	// -------------------------------------------------------------------------

	@Override
	public double getMaxCoolingPower() throws Exception {
		return this.getOwner().handleRequest(o -> ((FridgeUserAndExternalControlI)o).getMaxCoolingPower());

	}

	@Override
	public double getCurrentCoolingPower() throws Exception {
		return this.getOwner().handleRequest(o -> ((FridgeUserAndExternalControlI)o).getCurrentCoolingPower());

	}

	@Override
	public void setCurrentCoolingPower(double power) throws Exception {
		this.getOwner().handleRequest(
				o -> {	((FridgeUserAndExternalControlI)o).setCurrentCoolingPower(power);
						return null;
				});
	}

	@Override
	public void setTargetTemperature(double temperature) throws Exception {
		this.getOwner().handleRequest(
				o -> {	((FridgeUserAndExternalControlI)o).setTargetTemperature(temperature);
						return null;
				});
	}

	@Override
	public double getTargetTemperature() throws Exception {
		return this.getOwner().handleRequest(o -> ((FridgeUserAndExternalControlI)o).getTargetTemperature());
	}

	@Override
	public double getCurrentTemperature() throws Exception {
		return this.getOwner().handleRequest(o -> ((FridgeUserAndExternalControlI)o).getCurrentTemperature());
	}

}
