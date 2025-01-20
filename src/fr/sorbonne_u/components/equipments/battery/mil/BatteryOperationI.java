package fr.sorbonne_u.components.equipments.battery.mil;

import fr.sorbonne_u.components.equipments.battery.BatteryI.BATTERY_STATE;

public interface BatteryOperationI {
	
	public void setProduction();
    public void setConsumption();
    public void setStandBy();
    public BATTERY_STATE getCurrentState();
}
