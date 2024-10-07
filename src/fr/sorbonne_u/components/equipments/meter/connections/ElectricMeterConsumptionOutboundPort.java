package fr.sorbonne_u.components.equipments.meter.connections;

import fr.sorbonne_u.components.ComponentI;
import fr.sorbonne_u.components.equipments.meter.interfaces.ElectricMeterConsumptionCI;
import fr.sorbonne_u.components.ports.AbstractOutboundPort;

public class ElectricMeterConsumptionOutboundPort extends AbstractOutboundPort implements ElectricMeterConsumptionCI {

	// -------------------------------------------------------------------------
	// Constants and variables
	// -------------------------------------------------------------------------

	private static final long serialVersionUID = 1L;

	
	// -------------------------------------------------------------------------
	// Constructors
	// -------------------------------------------------------------------------

	public ElectricMeterConsumptionOutboundPort(ComponentI owner) throws Exception {
		super(ElectricMeterConsumptionCI.class, owner);
	}

	public ElectricMeterConsumptionOutboundPort(String uri, ComponentI owner) throws Exception {
		super(uri, ElectricMeterConsumptionCI.class, owner);
	}

	
	// -------------------------------------------------------------------------
	// Methods
	// -------------------------------------------------------------------------
	
	@Override
	public void addElectricConsumption(double quantity) throws Exception {
		((ElectricMeterConsumptionCI)this.getConnector()).addElectricConsumption(quantity);
	}

}
