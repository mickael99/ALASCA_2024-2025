package fr.sorbonne_u.components.equipments.toaster;

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
	
	public ToasterState getState() throws Exception; 
	
	public ToasterBrowningLevel getBrowningLevel() throws Exception; 
	
	public int getSliceCount() throws Exception; 

	public void turnOn() throws Exception;
	
	public void turnOff() throws Exception; 
	
	public void setSliceCount(int sliceCount) throws Exception; 
		
	public void setBrowningLevel(ToasterBrowningLevel bl) throws Exception;
}
