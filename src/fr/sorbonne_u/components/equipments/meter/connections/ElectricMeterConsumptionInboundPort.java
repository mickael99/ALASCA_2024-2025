package fr.sorbonne_u.components.equipments.meter.connections;

import fr.sorbonne_u.components.ComponentI;
import fr.sorbonne_u.components.equipments.meter.interfaces.ElectricMeterConsumptionCI;
import fr.sorbonne_u.components.equipments.meter.interfaces.ElectricMeterConsumptionI;
import fr.sorbonne_u.components.ports.AbstractInboundPort;

public class ElectricMeterConsumptionInboundPort extends AbstractInboundPort implements ElectricMeterConsumptionCI {

	private static final long serialVersionUID = 1L;

	public ElectricMeterConsumptionInboundPort(ComponentI owner) throws Exception {
		super(ElectricMeterConsumptionCI.class, owner);
	}

	public ElectricMeterConsumptionInboundPort(String uri, ComponentI owner) throws Exception {
		super(uri, ElectricMeterConsumptionCI.class, owner);
	}

	@Override
	public void addElectricConsumption(double quantity) throws Exception {
		this.getOwner().handleRequest(
				o -> { ((ElectricMeterConsumptionI)o).addElectricConsumption(quantity);
						return null;
				});
	}

}
