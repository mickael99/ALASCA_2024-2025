package fr.sorbonne_u.components.equipments.iron;


public interface IronImplementationI {
	
	public static enum IronState {
		ON,
		OFF
	}
	
	public static enum IronTemperature {
	    DELICATE,  // 600
	    COTTON,    // 800
	    LINEN	   // 1000
	}
	
	public enum IronSteam {
	    ACTIVE,   // +100
	    INACTIVE
	}

	public enum IronEnergySavingMode {
	    ACTIVE,  // -50
	    INACTIVE
	}
	
	public IronState getState() throws Exception; 
	public void turnOn() throws Exception; 
	public void turnOff() throws Exception; 
	
	public IronTemperature getTemperature() throws Exception; 
	public void setTemperature(IronTemperature t) throws Exception; 
	
	public IronSteam getSteam() throws Exception; 
	public void setSteam(IronSteam s) throws Exception; 
	
	public IronEnergySavingMode getEnergySavingMode() throws Exception;
	public void setEnergySavingMode(IronEnergySavingMode e) throws Exception;
}
	
