package fr.sorbonne_u.components.equipments.generator.interfaces;

import fr.sorbonne_u.components.interfaces.OfferedCI;
import fr.sorbonne_u.components.interfaces.RequiredCI;

public interface GeneratorCI extends OfferedCI, RequiredCI, GeneratorImplementationI {

	@Override
	boolean isRunning() throws Exception;
	
	@Override
	public void activate() throws Exception;
	
	@Override
	public void stop() throws Exception;

	@Override
	public double getEnergyProduction() throws Exception;
	
	@Override
	public double getFuelLevel() throws Exception;
	
	@Override
	 public void fill(double quantity) throws Exception;
}
