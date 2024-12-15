package fr.sorbonne_u.components.equipments.iron.sil;

import fr.sorbonne_u.components.equipments.iron.IronImplementationI.IronTemperature;

public interface IronOperationI {
	public void turnOn() throws Exception;
	public void turnOff() throws Exception;
	
	public void setTemperature(IronTemperature t) throws Exception;
	
	public void enableSteamMode() throws Exception;
	public void disableSteamMode() throws Exception;	
	
	public void enableEnergySavingMode() throws Exception;
	public void disableEnergySavingMode() throws Exception;
}
