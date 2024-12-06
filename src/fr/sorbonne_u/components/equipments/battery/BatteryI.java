package fr.sorbonne_u.components.equipments.battery;

public interface BatteryI {

	public static enum STATE {
		STANDBY,
		CONSUME,
		PRODUCT
	}
	
	public STATE getState() throws Exception;
	public void setState(STATE state) throws Exception;
	
	public double getBatteryLevel() throws Exception;
}
