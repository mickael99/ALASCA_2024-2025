package fr.sorbonne_u.components.equipments.iron;

import fr.sorbonne_u.components.interfaces.OfferedCI;
import fr.sorbonne_u.components.interfaces.RequiredCI;

public interface IronUserCI extends OfferedCI, RequiredCI, IronImplementationI {
	
	@Override
	public IronState getState() throws Exception;
	
	@Override
	public void turnOn() throws Exception;
	
	@Override
	public void turnOff() throws Exception;
	
	@Override
	public IronTemperature getTemperature() throws Exception;
	
	@Override
	public void setTemperature(IronTemperature t) throws Exception;
	
	@Override
	public IronSteam getSteam() throws Exception;
	
	@Override
	public void setSteam(IronSteam s) throws Exception;
	
	@Override
	public IronEnergySavingMode getEnergySavingMode() throws Exception;
	
	@Override
	public void setEnergySavingMode(IronEnergySavingMode e) throws Exception;
}
