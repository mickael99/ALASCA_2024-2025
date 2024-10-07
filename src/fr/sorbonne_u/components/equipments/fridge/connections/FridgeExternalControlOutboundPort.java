package fr.sorbonne_u.components.equipments.fridge.connections;

import fr.sorbonne_u.components.ComponentI;
import fr.sorbonne_u.components.equipments.fridge.interfaces.FridgeExternalControlCI;
import fr.sorbonne_u.components.ports.AbstractOutboundPort;

public class FridgeExternalControlOutboundPort extends AbstractOutboundPort implements FridgeExternalControlCI {

	// -------------------------------------------------------------------------
	// Constants and variables
	// -------------------------------------------------------------------------

	private static final long serialVersionUID = 1L;

	
	// -------------------------------------------------------------------------
	// Constructors
	// -------------------------------------------------------------------------

	public FridgeExternalControlOutboundPort(ComponentI owner) throws Exception {
		super(FridgeExternalControlCI.class, owner);
	}

	public FridgeExternalControlOutboundPort(String uri, ComponentI owner) throws Exception {
		super(uri, FridgeExternalControlCI.class, owner);
	}

	// -------------------------------------------------------------------------
	// Methods
	// -------------------------------------------------------------------------

	@Override
	public boolean isOpen() throws Exception {
		return ((FridgeExternalControlCI)this.getConnector()).isOpen();
	}

	@Override
	public void close() throws Exception {
		((FridgeExternalControlCI)this.getConnector()).close();
	}

	@Override
	public double getMaxCoolingPower() throws Exception {
		return ((FridgeExternalControlCI)this.getConnector()).getMaxCoolingPower();
	}

	@Override
	public double getCurrentCoolingPower() throws Exception {
		return ((FridgeExternalControlCI)this.getConnector()).getCurrentCoolingPower();
	}

	@Override
	public void setCurrentCoolingPower(double power) throws Exception {
		((FridgeExternalControlCI)this.getConnector()).setCurrentCoolingPower(power);
	}

	@Override
	public boolean isAlarmTriggered() throws Exception {
		return ((FridgeExternalControlCI)this.getConnector()).isAlarmTriggered();
	}

	@Override
	public void setTargetTemperature(double temperature) throws Exception {
		((FridgeExternalControlCI)this.getConnector()).setTargetTemperature(temperature);
	}

}
