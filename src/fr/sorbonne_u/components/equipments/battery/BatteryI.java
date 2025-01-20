package fr.sorbonne_u.components.equipments.battery;

public interface BatteryI {

	public static enum BATTERY_STATE {
		STANDBY,
		CONSUME,
		PRODUCT
	}
	
	public BATTERY_STATE getState() throws Exception;
	public void setState(BATTERY_STATE state) throws Exception;
	
	public double getBatteryLevel() throws Exception;
}
