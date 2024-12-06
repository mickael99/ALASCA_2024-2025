package fr.sorbonne_u.components.equipments.generator.connectors;

import fr.sorbonne_u.components.connectors.AbstractConnector;
import fr.sorbonne_u.components.equipments.generator.interfaces.GeneratorCI;
import fr.sorbonne_u.components.equipments.generator.interfaces.GeneratorHEMCI;

public class GeneratorHEMConnector extends AbstractConnector implements GeneratorHEMCI {

	@Override
	public boolean isRunning() throws Exception {
		return ((GeneratorCI)this.offering).isRunning();
	}

	@Override
	public void activate() throws Exception {
		((GeneratorCI)this.offering).activate();
	}

	@Override
	public void stop() throws Exception {
		((GeneratorCI)this.offering).stop();
	}
}
