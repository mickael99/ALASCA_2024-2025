package fr.sorbonne_u.components.equipments.windTurbine;

public interface WindTurbineI {
	
	public static enum STATE {
		ACTIVE,
		STANDBY
	}
	
	public boolean isActivate() throws Exception;
	
	public void activate() throws Exception;
	public void stop() throws Exception;

	public double getCurrentProduction() throws Exception;
}
