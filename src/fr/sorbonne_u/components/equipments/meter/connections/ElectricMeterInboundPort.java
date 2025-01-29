package fr.sorbonne_u.components.equipments.meter.connections;

import fr.sorbonne_u.components.ComponentI;
import fr.sorbonne_u.components.equipments.meter.interfaces.ElectricMeterCI;
import fr.sorbonne_u.components.equipments.meter.interfaces.ElectricMeterImplementationI;
import fr.sorbonne_u.components.ports.AbstractInboundPort;
import fr.sorbonne_u.components.utils.Measure;
import fr.sorbonne_u.components.utils.SensorData;

public class			ElectricMeterInboundPort
extends		AbstractInboundPort
implements	ElectricMeterCI
{
	// -------------------------------------------------------------------------
	// Constants and variables
	// -------------------------------------------------------------------------

	private static final long serialVersionUID = 1L;

	// -------------------------------------------------------------------------
	// Constructors
	// -------------------------------------------------------------------------

	public				ElectricMeterInboundPort(ComponentI owner)
	throws Exception
	{
		super(ElectricMeterCI.class, owner);
	}

	public				ElectricMeterInboundPort(String uri, ComponentI owner)
	throws Exception
	{
		super(uri, ElectricMeterCI.class, owner);
	}

	// -------------------------------------------------------------------------
	// Methods
	// -------------------------------------------------------------------------

	@Override
	public SensorData<Measure<Double>>	getCurrentConsumption() throws Exception
	{
		System.out.println("je suis dans le port interne");
		return this.getOwner().handleRequest(
				o -> ((ElectricMeterImplementationI)o).getCurrentConsumption());
	}

	@Override
	public SensorData<Measure<Double>>	getCurrentProduction() throws Exception
	{
		return this.getOwner().handleRequest(
				o -> ((ElectricMeterImplementationI)o).getCurrentProduction());
	}
}
// -----------------------------------------------------------------------------
