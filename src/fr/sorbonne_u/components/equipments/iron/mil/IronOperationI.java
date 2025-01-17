package fr.sorbonne_u.components.equipments.iron.mil;

import fr.sorbonne_u.components.equipments.iron.interfaces.IronImplementationI.IronState;

public interface IronOperationI {
	public void turnOn();
	public void turnOff();
	
	public void setState(IronState s); 
	
	public void enableSteamMode();
	public void disableSteamMode();	
	
	public void enableEnergySavingMode();
	public void disableEnergySavingMode();
}
