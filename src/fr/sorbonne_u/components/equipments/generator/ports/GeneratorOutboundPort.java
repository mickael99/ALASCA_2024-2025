package fr.sorbonne_u.components.equipments.generator.ports;

import fr.sorbonne_u.components.ComponentI;
import fr.sorbonne_u.components.equipments.generator.interfaces.GeneratorCI;
import fr.sorbonne_u.components.ports.AbstractOutboundPort;

public class GeneratorOutboundPort extends AbstractOutboundPort implements GeneratorCI {

	private static final long serialVersionUID = 1L;

	public	GeneratorOutboundPort(ComponentI owner) throws Exception {
		super(GeneratorCI.class, owner);
	}

	public GeneratorOutboundPort(String uri, ComponentI owner) throws Exception {
		super(uri, GeneratorCI.class, owner);
	}
	@Override
	public boolean isRunning() throws Exception {
		return ((GeneratorCI)this.getConnector()).isRunning();
	}

	@Override
	public void activate() throws Exception {
		((GeneratorCI)this.getConnector()).activate();
	}

	@Override
	public void stop() throws Exception {
		((GeneratorCI)this.getConnector()).stop();
	}

	@Override
	public double getEnergyProduction() throws Exception {
		return ((GeneratorCI)this.getConnector()).getEnergyProduction();
	}

	@Override
	public double getFuelLevel() throws Exception {
		return ((GeneratorCI)this.getConnector()).getFuelLevel();
	}

	@Override
	public void fill(double quantity) throws Exception {
		((GeneratorCI)this.getConnector()).fill(quantity);
	}
}
