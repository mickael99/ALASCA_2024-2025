package fr.sorbonne_u.components.equipments.meter.connections;

import fr.sorbonne_u.components.ComponentI;
import fr.sorbonne_u.components.equipments.meter.interfaces.ElectricMeterProductionCI;
import fr.sorbonne_u.components.ports.AbstractOutboundPort;

public class ElectricMeterProductionOutboundPort extends AbstractOutboundPort implements ElectricMeterProductionCI {

	// -------------------------------------------------------------------------
	// Constants and variables
	// -------------------------------------------------------------------------

	private static final long serialVersionUID = 1L;

	
	// -------------------------------------------------------------------------
	// Constructors
	// -------------------------------------------------------------------------

	public ElectricMeterProductionOutboundPort(ComponentI owner) throws Exception {
		super(ElectricMeterProductionCI.class, owner);
	}

	public ElectricMeterProductionOutboundPort(String uri, ComponentI owner) throws Exception {
		super(uri, ElectricMeterProductionCI.class, owner);
	}

	
	// -------------------------------------------------------------------------
	// Methods
	// -------------------------------------------------------------------------
	@Override
	public void addElectricProduction(double quantity) throws Exception {
		((ElectricMeterProductionCI)this.getConnector()).addElectricProduction(quantity);
	}

}
