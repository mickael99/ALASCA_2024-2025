package fr.sorbonne_u.components.equipments.fridge.sil.connectors;

import fr.sorbonne_u.components.connectors.AbstractConnector;
import fr.sorbonne_u.components.equipments.fridge.sil.FridgeActuatorCI;

public class FridgeActuatorConnector extends AbstractConnector implements FridgeActuatorCI {

	@Override
	public void startCooling() throws Exception {
		((FridgeActuatorCI)this.offering).startCooling();
	}

	@Override
	public void stopCooling() throws Exception {
		((FridgeActuatorCI)this.offering).stopCooling();
	}

	@Override
	public void closeDoor() throws Exception {
		((FridgeActuatorCI)this.offering).closeDoor();
	}

}
