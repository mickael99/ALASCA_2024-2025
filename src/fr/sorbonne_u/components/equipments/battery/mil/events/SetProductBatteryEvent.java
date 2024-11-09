package fr.sorbonne_u.components.equipments.battery.mil.events;

import fr.sorbonne_u.components.equipments.battery.BatteryI;
import fr.sorbonne_u.components.equipments.battery.mil.BatteryChargeLevelModel;
import fr.sorbonne_u.components.equipments.battery.mil.BatteryElectricityModel;
import fr.sorbonne_u.devs_simulation.models.events.EventI;
import fr.sorbonne_u.devs_simulation.models.interfaces.AtomicModelI;
import fr.sorbonne_u.devs_simulation.models.time.Time;

public class SetProductBatteryEvent extends AbstractBatteryEvent {

	private static final long serialVersionUID = 1L;

	public SetProductBatteryEvent(Time timeOfOccurrence) {
		super(timeOfOccurrence, null);
	}

	@Override
    public boolean hasPriorityOver(EventI e) {
        return true;
    }
	
	@Override
    public void executeOn(AtomicModelI model) {
        assert model instanceof BatteryElectricityModel || model instanceof BatteryChargeLevelModel;
        if(model instanceof BatteryElectricityModel) {
        	BatteryElectricityModel m = (BatteryElectricityModel)model;
            if(m.getCurrentState() != BatteryI.STATE.PRODUCT) {
                m.setProduction();;
                m.setHasChanged(true);
            }
        }
        else {
        	BatteryChargeLevelModel m = (BatteryChargeLevelModel)model;
            if(m.getCurrentState() != BatteryI.STATE.PRODUCT) 
                m.setProduction();       
        }
    }
}
