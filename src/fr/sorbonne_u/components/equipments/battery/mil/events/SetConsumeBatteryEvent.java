package fr.sorbonne_u.components.equipments.battery.mil.events;

import fr.sorbonne_u.components.equipments.battery.mil.BatteryChargeLevelModel;
import fr.sorbonne_u.components.equipments.battery.mil.BatteryElectricityModel;
import fr.sorbonne_u.devs_simulation.models.events.EventI;
import fr.sorbonne_u.devs_simulation.models.interfaces.AtomicModelI;
import fr.sorbonne_u.devs_simulation.models.time.Time;
import fr.sorbonne_u.components.equipments.battery.BatteryI;

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
        assert model instanceof BatteryElectricityModel || model instanceof BatteryChargeLevelModel;
        if(model instanceof BatteryElectricityModel) {
        	BatteryElectricityModel m = (BatteryElectricityModel)model;
            if(m.getCurrentState() != BatteryI.STATE.CONSUME) {
                m.setConsumption();
                m.setHasChanged(true);
            }
        }
        else {
        	BatteryChargeLevelModel m = (BatteryChargeLevelModel) model;
            if(m.getCurrentState() != BatteryI.STATE.CONSUME) 
                m.setConsumption();       
        }
    }
}
