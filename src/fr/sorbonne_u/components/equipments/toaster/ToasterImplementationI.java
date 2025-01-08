package fr.sorbonne_u.components.equipments.toaster;

import fr.sorbonne_u.components.equipments.toaster.mil.ToasterStateModel;
import fr.sorbonne_u.components.equipments.toaster.mil.events.SetToasterBrowningLevel;

public interface ToasterImplementationI {
	
	public static enum ToasterState {
		ON,
		OFF
	}
	
	public static enum ToasterBrowningLevel {
		DEFROST,
		LOW,
		MEDIUM,
		HIGH
	}
	
	public ToasterStateModel.ToasterState getState() throws Exception;
	
	public ToasterStateModel.ToasterBrowningLevel getBrowningLevel() throws Exception;
	
	public int getSliceCount() throws Exception; 

	public void turnOn() throws Exception;
	
	public void turnOff() throws Exception; 
	
	public void setSliceCount(int sliceCount) throws Exception; 
		
	public void setBrowningLevel(ToasterStateModel.ToasterBrowningLevel bl) throws Exception;
}
