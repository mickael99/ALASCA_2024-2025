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
	
	public IronState getState() throws Exception; //ok
	public void turnOn() throws Exception; //ok
	public void turnOff() throws Exception; //ok
	
	public IronTemperature getTemperature() throws Exception; //ok
	public void setTemperature(IronTemperature t) throws Exception; //ok
	
	public IronSteam getSteam() throws Exception; //ok
	public void setSteam(IronSteam s) throws Exception; //ok
	
	public IronEnergySavingMode getEnergySavingMode() throws Exception;
	public void setEnergySavingMode(IronEnergySavingMode e) throws Exception;
}
	
