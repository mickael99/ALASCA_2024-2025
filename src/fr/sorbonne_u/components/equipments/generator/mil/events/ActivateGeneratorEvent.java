package fr.sorbonne_u.components.equipments.generator.mil.events;

import fr.sorbonne_u.components.equipments.generator.mil.GeneratorElectricityModel;
import fr.sorbonne_u.components.equipments.generator.mil.GeneratorFuelModel;
import fr.sorbonne_u.devs_simulation.models.events.EventI;
import fr.sorbonne_u.devs_simulation.models.interfaces.AtomicModelI;
import fr.sorbonne_u.devs_simulation.models.time.Time;

public class ActivateGeneratorEvent extends AbstractGeneratorEvents {

	private static final long serialVersionUID = 1L;

	public ActivateGeneratorEvent(Time timeOfOccurrence) {
		super(timeOfOccurrence, null);
	}
	
	@Override
    public boolean hasPriorityOver(EventI e) {
        return true;
    }

    @Override
    public void executeOn(AtomicModelI model) {
    	assert model instanceof GeneratorElectricityModel || model instanceof GeneratorFuelModel;
    	
        if(model instanceof GeneratorElectricityModel) {
            GeneratorElectricityModel m = (GeneratorElectricityModel) model;
            if(!m.isRunning()) {
                m.activate();
                m.setProductionHasChanged(true);
            }
        }
        else {
            GeneratorFuelModel m = (GeneratorFuelModel) model;
            if(!m.isRunning()) {
                m.activate();
            }
        }
    }
}
