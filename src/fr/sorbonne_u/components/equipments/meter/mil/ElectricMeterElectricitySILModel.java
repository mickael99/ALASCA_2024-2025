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
		this.updateCumulativeConsumption(elapsedTime);
		double old = this.currentPowerConsumption.getValue();
		double i = this.computePowerConsumption();
		this.currentPowerConsumption.setNewValue(i, this.getCurrentStateTime());

		this.ownerComponent.setCurrentPowerConsumption(
							new Measure<Double>(i, MeasurementUnit.AMPERES));
		
		if (Math.abs(old - i) > 0.000001) {
			StringBuffer message =
						new StringBuffer("current power consumption: ");
			message.append(this.currentPowerConsumption.getValue());
			message.append(" at ");
			message.append(this.getCurrentStateTime());
			message.append('\n');
			this.logMessage(message.toString());
		}
	}
}
