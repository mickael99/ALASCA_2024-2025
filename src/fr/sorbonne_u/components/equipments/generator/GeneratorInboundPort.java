package fr.sorbonne_u.components.equipments.generator;

import fr.sorbonne_u.components.ComponentI;
import fr.sorbonne_u.components.ports.AbstractInboundPort;

public class GeneratorInboundPort extends AbstractInboundPort implements GeneratorCI {

	private static final long serialVersionUID = 1L;

	public GeneratorInboundPort(ComponentI owner) throws Exception
	{
		super(GeneratorCI.class, owner);
		assert owner instanceof GeneratorImplementationI;
	}
	
	public GeneratorInboundPort(String uri, ComponentI owner) throws Exception
	{
		super(uri, GeneratorCI.class, owner);
		assert	owner instanceof GeneratorImplementationI;
	}
	
	@Override
	public boolean isRunning() throws Exception {
		return this.getOwner().handleRequest(
				o -> ((GeneratorImplementationI)o).isRunning());
	}

	@Override
	public void activate() throws Exception {
		this.getOwner().handleRequest(
				o -> {	((GeneratorImplementationI)o).activate();
						return null;
				});
	}

	@Override
	public void stop() throws Exception {
		this.getOwner().handleRequest(
				o -> {	((GeneratorImplementationI)o).stop();
						return null;
				});
	}

	@Override
	public double getEnergyProduction() throws Exception {
		return this.getOwner().handleRequest(
				o -> ((GeneratorImplementationI)o).getEnergyProduction());
	}

	@Override
	public double getFuelLevel() throws Exception {
		return this.getOwner().handleRequest(
				o -> ((GeneratorImplementationI)o).getFuelLevel());
	}

	@Override
	 public void fill(double quantity) throws Exception {
		this.getOwner().handleRequest(
				o -> {	((GeneratorImplementationI)o).fill(quantity);
						return null;
				});
	}

}
