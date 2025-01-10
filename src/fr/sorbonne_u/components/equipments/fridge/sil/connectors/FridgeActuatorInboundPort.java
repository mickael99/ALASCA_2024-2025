package fr.sorbonne_u.components.equipments.fridge.sil.connectors;

import fr.sorbonne_u.components.ComponentI;
import fr.sorbonne_u.components.equipments.fridge.interfaces.FridgeInternalControlI;
import fr.sorbonne_u.components.equipments.fridge.sil.FridgeActuatorCI;
import fr.sorbonne_u.components.ports.AbstractInboundPort;
import fr.sorbonne_u.exceptions.PreconditionException;

public class FridgeActuatorInboundPort extends AbstractInboundPort implements FridgeActuatorCI {

	private static final long serialVersionUID = 1L;

	public FridgeActuatorInboundPort(ComponentI owner) throws Exception {
		super(FridgeActuatorCI.class, owner);
		assert	owner instanceof FridgeInternalControlI :
			new PreconditionException(
					"owner instanceof FridgeInternalControlI");
	}

	public FridgeActuatorInboundPort(String uri, ComponentI owner) throws Exception {
		super(uri, FridgeActuatorCI.class, owner);
		assert	owner instanceof FridgeInternalControlI :
			new PreconditionException(
					"owner instanceof FridgeInternalControlI");
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
	public void closeDoor() throws Exception {
		this.getOwner().handleRequest(
				o -> {	((FridgeInternalControlI)o).closeDoor();
						return null;
					 });
	}

}
