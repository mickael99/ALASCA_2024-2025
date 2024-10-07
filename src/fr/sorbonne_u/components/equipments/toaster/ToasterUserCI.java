package fr.sorbonne_u.components.equipments.toaster;

import fr.sorbonne_u.components.interfaces.OfferedCI;
import fr.sorbonne_u.components.interfaces.RequiredCI;

public interface ToasterUserCI extends OfferedCI, RequiredCI, ToasterImplementationI {
	
	@Override
	public ToasterState getState() throws Exception;
	
	@Override
	public ToasterBrowningLevel getBrowningLevel() throws Exception;
	
	@Override
	public int getSliceCount() throws Exception;
	
	@Override
	public void turnOn() throws Exception;
	
	@Override
	public void turnOff() throws Exception;
	
	@Override
	public void setSliceCount(int sliceCount) throws Exception;
		
	@Override
	public void setBrowningLevel(ToasterBrowningLevel bl) throws Exception;
}
