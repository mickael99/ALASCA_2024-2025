package fr.sorbonne_u.components.equipments.generator.ports;

import fr.sorbonne_u.components.ComponentI;
import fr.sorbonne_u.components.equipments.generator.interfaces.GeneratorHEMCI;
import fr.sorbonne_u.components.ports.AbstractOutboundPort;

public class GeneratorHEMOutboundPort extends AbstractOutboundPort implements GeneratorHEMCI {

	private static final long serialVersionUID = 1L;

	public	GeneratorHEMOutboundPort(ComponentI owner) throws Exception {
		super(GeneratorHEMCI.class, owner);
	}

	public GeneratorHEMOutboundPort(String uri, ComponentI owner) throws Exception {
		super(uri, GeneratorHEMCI.class, owner);
	}
	
	@Override
	public boolean isRunning() throws Exception {
		return ((GeneratorHEMCI)this.getConnector()).isRunning();
	}

	@Override
	public void activate() throws Exception {
		((GeneratorHEMCI)this.getConnector()).activate();
	}

	@Override
	public void stop() throws Exception {
		((GeneratorHEMCI)this.getConnector()).stop();
	}
}
