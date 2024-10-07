package fr.sorbonne_u.components.equipments.meter.connections;

import fr.sorbonne_u.components.connectors.AbstractConnector;
import fr.sorbonne_u.components.equipments.meter.interfaces.ElectricMeterProductionCI;

public class ElectricMeterProductionConnector extends AbstractConnector implements ElectricMeterProductionCI {

	@Override
	public void addElectricProduction(double quantity) throws Exception {
		((ElectricMeterProductionCI)this.offering).addElectricProduction(quantity);
	}

}
