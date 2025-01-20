package fr.sorbonne_u.components.equipments.battery;

import fr.sorbonne_u.components.interfaces.OfferedCI;
import fr.sorbonne_u.components.interfaces.RequiredCI;

public interface BatteryCI extends OfferedCI, RequiredCI, BatteryI {

	public BATTERY_STATE getState() throws Exception;
	public void setState(BATTERY_STATE state) throws Exception;
	public double getBatteryLevel() throws Exception;
}
