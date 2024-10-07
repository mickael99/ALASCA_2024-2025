package fr.sorbonne_u.components.equipments.meter.interfaces;

import fr.sorbonne_u.components.interfaces.OfferedCI;
import fr.sorbonne_u.components.interfaces.RequiredCI;

public interface ElectricMeterProductionCI extends OfferedCI, RequiredCI, ElectricMeterProductionI {

	public void addElectricProduction(double quantity) throws Exception;
}
