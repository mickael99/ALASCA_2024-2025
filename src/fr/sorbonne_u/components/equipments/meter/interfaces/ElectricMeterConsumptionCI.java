package fr.sorbonne_u.components.equipments.meter.interfaces;

import fr.sorbonne_u.components.interfaces.OfferedCI;
import fr.sorbonne_u.components.interfaces.RequiredCI;

public interface ElectricMeterConsumptionCI extends RequiredCI, OfferedCI, ElectricMeterConsumptionI {
	
	public void addElectricConsumption(double quantity) throws Exception;
}
