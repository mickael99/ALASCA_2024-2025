package fr.sorbonne_u.components.equipments.battery;

import fr.sorbonne_u.components.interfaces.OfferedCI;
import fr.sorbonne_u.components.interfaces.RequiredCI;

public interface BatteryCI extends OfferedCI, RequiredCI, BatteryI {

	public STATE getState() throws Exception;
	public void setState(STATE state) throws Exception;
}
