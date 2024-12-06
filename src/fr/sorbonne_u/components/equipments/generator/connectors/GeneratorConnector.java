package fr.sorbonne_u.components.equipments.generator.connectors;

import fr.sorbonne_u.components.connectors.AbstractConnector;
import fr.sorbonne_u.components.equipments.generator.interfaces.GeneratorCI;

public class GeneratorConnector extends AbstractConnector implements GeneratorCI {

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

	@Override
	public double getEnergyProduction() throws Exception {
		return ((GeneratorCI)this.offering).getEnergyProduction();
	}

	@Override
	public double getFuelLevel() throws Exception {
		return ((GeneratorCI)this.offering).getFuelLevel();
	}

	@Override
	public void fill(double quantity) throws Exception {
		((GeneratorCI)this.offering).fill(quantity);
	}

}
