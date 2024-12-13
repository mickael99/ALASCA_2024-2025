package fr.sorbonne_u.components.equipments.iron.sil;

import fr.sorbonne_u.components.equipments.iron.IronImplementationI.IronEnergySavingMode;
import fr.sorbonne_u.components.equipments.iron.IronImplementationI.IronSteam;
import fr.sorbonne_u.components.equipments.iron.IronImplementationI.IronTemperature;

public interface IronOperationI {
	public void turnOn() throws Exception; 
	public void turnOff() throws Exception; 
	
	public void setTemperature(IronTemperature t) throws Exception; 
	public void setSteam(IronSteam s) throws Exception; 
	public void setEnergySavingMode(IronEnergySavingMode e) throws Exception;
}
