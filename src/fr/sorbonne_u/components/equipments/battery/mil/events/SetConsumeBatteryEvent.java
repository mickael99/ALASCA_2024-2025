package fr.sorbonne_u.components.equipments.battery.mil.events;

import fr.sorbonne_u.components.equipments.battery.mil.BatteryOperationI;
import fr.sorbonne_u.devs_simulation.models.events.EventI;
import fr.sorbonne_u.devs_simulation.models.interfaces.AtomicModelI;
import fr.sorbonne_u.devs_simulation.models.time.Time;
import fr.sorbonne_u.components.equipments.battery.BatteryI.BATTERY_STATE;

public class SetConsumeBatteryEvent extends AbstractBatteryEvent {

	private static final long serialVersionUID = 1L;

	public SetConsumeBatteryEvent(Time timeOfOccurrence) {
		super(timeOfOccurrence, null);
	}

	@Override
    public boolean hasPriorityOver(EventI e) {
        return false;
    }
	
	@Override
    public void executeOn(AtomicModelI model) {
        assert model instanceof BatteryOperationI;
        BatteryOperationI b = (BatteryOperationI)model;
        
        if(b.getCurrentState() != BATTERY_STATE.CONSUME) 
            b.setConsumption();
    }
}
