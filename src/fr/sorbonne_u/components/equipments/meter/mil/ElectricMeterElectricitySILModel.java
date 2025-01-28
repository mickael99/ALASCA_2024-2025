package fr.sorbonne_u.components.equipments.meter.mil;

import fr.sorbonne_u.components.utils.Measure;
import fr.sorbonne_u.components.utils.MeasurementUnit;
import fr.sorbonne_u.devs_simulation.models.time.Duration;
import fr.sorbonne_u.devs_simulation.simulators.interfaces.AtomicSimulatorI;

import java.util.concurrent.TimeUnit;

public class ElectricMeterElectricitySILModel extends ElectricMeterElectricityModel{

    //------------------------------------------------------------
    // Constants and variables
    //------------------------------------------------------------

    private static final long serialVersionUID = 1L;
    public static final String SIL_URI = ElectricMeterElectricityModel.class.getSimpleName() + "-SIL";

    //------------------------------------------------------------
    // Constructor
    //------------------------------------------------------------

    public	ElectricMeterElectricitySILModel(
            String uri,
            TimeUnit simulatedTimeUnit,
            AtomicSimulatorI simulationEngine
                                                          ) throws Exception
    {
        super(uri, simulatedTimeUnit, simulationEngine);
    }

    //------------------------------------------------------------
    // Methods
    //------------------------------------------------------------

    @Override
    public void userDefinedInternalTransition(Duration elapsedTime) {
    	super.userDefinedInternalTransition(elapsedTime);

    	// Consumption
		this.updateTotalConsumption(elapsedTime);
		double oldConsumption = this.currentConsumption.getValue();
		double newConsumption = this.computeCurrentConsumption();
		this.currentConsumption.setNewValue(newConsumption, this.getCurrentStateTime());

		this.ownerComponent.setCurrentPowerConsumption(
							new Measure<Double>(newConsumption, MeasurementUnit.AMPERES));
		
		if (Math.abs(oldConsumption - newConsumption) > 0.000001) {
			StringBuffer message =
						new StringBuffer("current power consumption: ");
			message.append(this.currentConsumption.getValue());
			message.append(" at ");
			message.append(this.getCurrentStateTime());
			message.append('\n');
			this.logMessage(message.toString());
		}
		
		// Production
		this.updateTotalConsumption(elapsedTime);
		double oldProduction = this.currentProduction.getValue();
		double newProduction = this.computeCurrentProduction();
		this.currentProduction.setNewValue(newProduction, this.getCurrentStateTime());

		this.ownerComponent.setCurrentPowerProduction(
							new Measure<Double>(newProduction, MeasurementUnit.AMPERES));
		
		if (Math.abs(oldProduction - newProduction) > 0.000001) {
			StringBuffer message =
						new StringBuffer("current power production: ");
			message.append(this.currentProduction.getValue());
			message.append(" at ");
			message.append(this.getCurrentStateTime());
			message.append('\n');
			this.logMessage(message.toString());
		}
    }
}
