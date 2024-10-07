package fr.sorbonne_u.components.equipments.meter.connections;

import fr.sorbonne_u.components.connectors.AbstractConnector;
import fr.sorbonne_u.components.equipments.meter.interfaces.ElectricMeterConsumptionCI;

public class ElectricMeterConsumptionConnector extends AbstractConnector implements ElectricMeterConsumptionCI {

	@Override
	public void addElectricConsumption(double quantity) throws Exception {
		((ElectricMeterConsumptionCI)this.offering).addElectricConsumption(quantity);
	}

}
