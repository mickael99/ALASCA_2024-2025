package fr.sorbonne_u.components.equipments.fridge.sil.connectors;

import java.util.concurrent.TimeUnit;

import fr.sorbonne_u.components.connectors.DataConnector;
import fr.sorbonne_u.components.equipments.fridge.measures.FridgeSensorData;
import fr.sorbonne_u.components.equipments.fridge.sil.FridgeSensorDataCI;
import fr.sorbonne_u.components.utils.Measure;

public class FridgeSensorDataConnector extends DataConnector implements FridgeSensorDataCI.FridgeSensorRequiredPullCI {

	@Override
	public FridgeSensorData<Measure<Boolean>> coolingPullSensor() throws Exception {
		return ((FridgeSensorDataCI.FridgeSensorOfferedPullCI)this.offering).coolingPullSensor();
	}

	@Override
	public FridgeSensorData<Measure<Double>> targetTemperaturePullSensor() throws Exception {
		return ((FridgeSensorDataCI.FridgeSensorOfferedPullCI)this.offering).targetTemperaturePullSensor();
	}

	@Override
	public FridgeSensorData<Measure<Double>> currentTemperaturePullSensor() throws Exception {
		return ((FridgeSensorDataCI.FridgeSensorOfferedPullCI)this.offering).currentTemperaturePullSensor();
	}

	@Override
	public FridgeSensorData<Measure<Boolean>> doorStatePullSensor() throws Exception {
		return ((FridgeSensorDataCI.FridgeSensorOfferedPullCI)this.offering).doorStatePullSensor();
	}

	@Override
	public void startTemperaturesPushSensor(long controlPeriod, TimeUnit tu) throws Exception {
		((FridgeSensorDataCI.FridgeSensorOfferedPullCI)this.offering).startTemperaturesPushSensor(controlPeriod, tu);
	}

}
