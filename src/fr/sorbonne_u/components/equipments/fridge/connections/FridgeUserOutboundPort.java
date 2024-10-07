package fr.sorbonne_u.components.equipments.fridge.connections;

import fr.sorbonne_u.components.ComponentI;
import fr.sorbonne_u.components.equipments.fridge.Fridge.FridgeState;
import fr.sorbonne_u.components.equipments.fridge.interfaces.FridgeUserCI;
import fr.sorbonne_u.components.ports.AbstractOutboundPort;

public class FridgeUserOutboundPort extends AbstractOutboundPort implements FridgeUserCI {

	// -------------------------------------------------------------------------
	// Constants and variables
	// -------------------------------------------------------------------------

	private static final long serialVersionUID = 1L;

	
	// -------------------------------------------------------------------------
	// Constructors
	// -------------------------------------------------------------------------

	public FridgeUserOutboundPort(ComponentI owner) throws Exception {
		super(FridgeUserCI.class, owner);
	}

	public FridgeUserOutboundPort(String uri, ComponentI owner) throws Exception {
		super(uri, FridgeUserCI.class, owner);
	}

	
	// -------------------------------------------------------------------------
	// Methods
	// -------------------------------------------------------------------------
	
	@Override
	public FridgeState getState() throws Exception {
		return ((FridgeUserCI)this.getConnector()).getState();
	}

	@Override
	public void switchOn() throws Exception {
		((FridgeUserCI)this.getConnector()).switchOn();
	}

	@Override
	public void switchOff() throws Exception {
		((FridgeUserCI)this.getConnector()).switchOff();
	}

	@Override
	public boolean isOpen() throws Exception {
		return ((FridgeUserCI)this.getConnector()).isOpen();
	}

	@Override
	public void open() throws Exception {
		((FridgeUserCI)this.getConnector()).open();
	}

	@Override
	public void close() throws Exception {
		((FridgeUserCI)this.getConnector()).close();
	}

	@Override
	public boolean isAlarmTriggered() throws Exception {
		return ((FridgeUserCI)this.getConnector()).isAlarmTriggered();
	}

	@Override
	public double getMaxCoolingPower() throws Exception {
		return ((FridgeUserCI)this.getConnector()).getMaxCoolingPower();
	}

	@Override
	public double getCurrentCoolingPower() throws Exception {
		return ((FridgeUserCI)this.getConnector()).getCurrentCoolingPower();
	}

	@Override
	public void setCurrentCoolingPower(double power) throws Exception {
		((FridgeUserCI)this.getConnector()).setCurrentCoolingPower(power);
	}

	@Override
	public void setTargetTemperature(double temperature) throws Exception {
		((FridgeUserCI)this.getConnector()).setTargetTemperature(temperature);;
	}

	@Override
	public double getTargetTemperature() throws Exception {
		return ((FridgeUserCI)this.getConnector()).getTargetTemperature();
	}

	@Override
	public double getCurrentTemperature() throws Exception {
		return ((FridgeUserCI)this.getConnector()).getCurrentTemperature();
	}

}
