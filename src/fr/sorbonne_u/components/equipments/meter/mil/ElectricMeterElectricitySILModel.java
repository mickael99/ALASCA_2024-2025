package fr.sorbonne_u.components.equipments.meter.mil;


import java.util.concurrent.TimeUnit;

import fr.sorbonne_u.components.utils.MeasurementUnit;
import fr.sorbonne_u.devs_simulation.models.time.Duration;
import fr.sorbonne_u.devs_simulation.simulators.interfaces.AtomicSimulatorI;
import fr.sorbonne_u.components.utils.Measure;

public class			ElectricMeterElectricitySILModel
extends		ElectricMeterElectricityModel
{
	// -------------------------------------------------------------------------
	// Constants and variables
	// -------------------------------------------------------------------------

	private static final long serialVersionUID = 1L;
	public static final String	SIL_URI = ElectricMeterElectricityModel.class.
													getSimpleName() + "-SIL";

	// -------------------------------------------------------------------------
	// Constructors
	// -------------------------------------------------------------------------

	public				ElectricMeterElectricitySILModel(
		String uri,
		TimeUnit simulatedTimeUnit,
		AtomicSimulatorI simulationEngine
		) throws Exception
	{
		super(uri, simulatedTimeUnit, simulationEngine);
	}

	// -------------------------------------------------------------------------
	// Methods
	// -------------------------------------------------------------------------

	@Override
	public void			userDefinedInternalTransition(Duration elapsedTime)
	{
		super.userDefinedInternalTransition(elapsedTime);
		this.updateTotalConsumption(elapsedTime);
		double oldConsumption = this.currentPowerConsumption.getValue();
		double currentConsumption = this.computePowerConsumption();
		this.currentPowerConsumption.setNewValue(currentConsumption, this.getCurrentStateTime());

		this.ownerComponent.setCurrentPowerConsumption(
							new Measure<Double>(currentConsumption, MeasurementUnit.AMPERES));
		
		if (Math.abs(oldConsumption - currentConsumption) > 0.000001) {
			StringBuffer message =
						new StringBuffer("current power consumption: ");
			message.append(this.currentPowerConsumption.getValue());
			message.append(" at ");
			message.append(this.getCurrentStateTime());
			message.append('\n');
			this.logMessage(message.toString());
		}
		
		this.updateTotalProduction(elapsedTime);
		double oldProduction = this.currentPowerProduction.getValue();
		double currentProduction = this.computePowerProduction();
		this.currentPowerProduction.setNewValue(oldProduction, this.getCurrentStateTime());

		this.ownerComponent.setCurrentPowerProduction(
							new Measure<Double>(currentProduction, MeasurementUnit.AMPERES));
		
		if (Math.abs(oldProduction - currentProduction) > 0.000001) {
			StringBuffer message =
						new StringBuffer("current power production: ");
			message.append(this.currentPowerProduction.getValue());
			message.append(" at ");
			message.append(this.getCurrentStateTime());
			message.append('\n');
			this.logMessage(message.toString());
		}
	}
}
