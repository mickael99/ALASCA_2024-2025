package fr.sorbonne_u.components.equipments.iron;


public interface IronImplementationI {
	
	public static enum IronState {
		OFF,
		DELICATE,  
	    COTTON,    
	    LINEN	 
	}
	
	public IronState getState() throws Exception; 
	public boolean isTurnOn() throws Exception;
	public void turnOn() throws Exception; 
	public void turnOff() throws Exception;
	
	public void setState(IronState s) throws Exception; 
	
	public boolean isSteamModeEnable() throws Exception; 
	public void EnableSteamMode() throws Exception; 
	public void DisableSteamMode() throws Exception;
	
	public boolean isEnergySavingModeEnable() throws Exception; 
	public void EnableEnergySavingMode() throws Exception; 
	public void DisableEnergySavingMode() throws Exception;
}
	
