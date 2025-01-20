package fr.sorbonne_u.components.equipments.battery.mil.events;

import fr.sorbonne_u.components.equipments.battery.BatteryI.BATTERY_STATE;
import fr.sorbonne_u.components.equipments.battery.mil.BatteryOperationI;
import fr.sorbonne_u.devs_simulation.models.events.EventI;
import fr.sorbonne_u.devs_simulation.models.interfaces.AtomicModelI;
import fr.sorbonne_u.devs_simulation.models.time.Time;

public class SetStandByBatteryEvent extends AbstractBatteryEvent {
	
	private static final long serialVersionUID = 1L;

	public SetStandByBatteryEvent(Time timeOfOccurrence) {
		super(timeOfOccurrence, null);
	}

	@Override
    public boolean hasPriorityOver(EventI e) {
        return true;
    }
	
	@Override
    public void executeOn(AtomicModelI model) {
        assert model instanceof BatteryOperationI;
        BatteryOperationI b = (BatteryOperationI)model;
        
        if(b.getCurrentState() != BATTERY_STATE.STANDBY) 
            b.setStandBy();
    }
}
