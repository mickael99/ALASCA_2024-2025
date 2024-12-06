package fr.sorbonne_u.components.equipments.generator.interfaces;

public interface GeneratorImplementationI {
	
	 boolean isRunning() throws Exception;
	 
	 public void activate() throws Exception;
	 public void stop() throws Exception;

	 public double getEnergyProduction() throws Exception;
	 public double getFuelLevel() throws Exception;
	 public void fill(double quantity) throws Exception;
}
