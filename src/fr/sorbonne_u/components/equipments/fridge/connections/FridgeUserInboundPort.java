package fr.sorbonne_u.components.equipments.fridge.connections;

import fr.sorbonne_u.components.ComponentI;
import fr.sorbonne_u.components.equipments.fridge.interfaces.FridgeUserAndControlI;
import fr.sorbonne_u.components.equipments.fridge.interfaces.FridgeUserAndExternalControlI;
import fr.sorbonne_u.components.equipments.fridge.interfaces.FridgeUserCI;
import fr.sorbonne_u.components.equipments.fridge.interfaces.FridgeUserI;
import fr.sorbonne_u.components.equipments.fridge.mil.FridgeElectricityModel.FridgeState;
import fr.sorbonne_u.components.ports.AbstractInboundPort;

public class FridgeUserInboundPort extends AbstractInboundPort implements FridgeUserCI {
	
	// -------------------------------------------------------------------------
	// Constants and variables
	// -------------------------------------------------------------------------

	private static final long serialVersionUID = 1L;

	// -------------------------------------------------------------------------
	// Constructors
	// -------------------------------------------------------------------------

	public FridgeUserInboundPort(ComponentI owner) throws Exception
	{
		super(FridgeUserCI.class, owner);

		assert	owner instanceof FridgeUserI;
		assert	owner instanceof FridgeUserAndControlI;
		assert	owner instanceof FridgeUserAndExternalControlI;
	}

	public FridgeUserInboundPort(String uri, ComponentI owner) throws Exception
	{
		super(uri, FridgeUserCI.class, owner);

		assert	owner instanceof FridgeUserI;
		assert	owner instanceof FridgeUserAndControlI;
		assert	owner instanceof FridgeUserAndExternalControlI;
	}
	
	
	// -------------------------------------------------------------------------
	// Methods
	// -------------------------------------------------------------------------

	@Override
	public FridgeState getState() throws Exception {
		return this.getOwner().handleRequest(o -> ((FridgeUserI)o).getState());
	}

	@Override
	public void switchOn() throws Exception {
		this.getOwner().handleRequest(
				o -> {	((FridgeUserI)o).switchOn();;
						return null;
				});
	}

	@Override
	public void switchOff() throws Exception {
		this.getOwner().handleRequest(
				o -> {	((FridgeUserI)o).switchOff();;
						return null;
				});
	}

	@Override
	public double getMaxCoolingPower() throws Exception {
		return this.getOwner().handleRequest(o -> ((FridgeUserI)o).getMaxCoolingPower());
	}

	@Override
	public double getCurrentCoolingPower() throws Exception {
		return this.getOwner().handleRequest(o -> ((FridgeUserI)o).getCurrentCoolingPower());
	}

	@Override
	public void setCurrentCoolingPower(double power) throws Exception {
		this.getOwner().handleRequest(
				o -> {	((FridgeUserI)o).setCurrentCoolingPower(power);
						return null;
				});
	}

	@Override
	public void setTargetTemperature(double temperature) throws Exception {
		this.getOwner().handleRequest(
				o -> {	((FridgeUserI)o).setTargetTemperature(temperature);
						return null;
				});
	}

	@Override
	public double getTargetTemperature() throws Exception {
		return this.getOwner().handleRequest(o -> ((FridgeUserI)o).getTargetTemperature());
	}

	@Override
	public double getCurrentTemperature() throws Exception {
		return this.getOwner().handleRequest(o -> ((FridgeUserI)o).getCurrentTemperature());
	}
}
