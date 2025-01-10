package fr.sorbonne_u.components.equipments.fridge.sil;

import java.util.concurrent.TimeUnit;

import fr.sorbonne_u.components.equipments.fridge.measures.FridgeSensorData;
import fr.sorbonne_u.components.interfaces.DataOfferedCI;
import fr.sorbonne_u.components.interfaces.DataRequiredCI;
import fr.sorbonne_u.components.interfaces.OfferedCI;
import fr.sorbonne_u.components.interfaces.RequiredCI;
import fr.sorbonne_u.components.utils.Measure;

public interface FridgeSensorDataCI extends DataOfferedCI, DataRequiredCI {

	public static interface	FridgeSensorCI extends OfferedCI, RequiredCI {
		public FridgeSensorData<Measure<Boolean>> coolingPullSensor() throws Exception;
		public FridgeSensorData<Measure<Double>> targetTemperaturePullSensor() throws Exception;
		public FridgeSensorData<Measure<Double>> currentTemperaturePullSensor() throws Exception;
		public FridgeSensorData<Measure<Boolean>> doorStatePullSensor() throws Exception;

		public void startTemperaturesPushSensor(long controlPeriod, TimeUnit tu) throws Exception;
	}

	public static interface FridgeSensorRequiredPullCI extends FridgeSensorCI, DataRequiredCI.PullCI
	{
	}

	public static interface	 FridgeSensorOfferedPullCI extends FridgeSensorCI, DataOfferedCI.PullCI
	{
	}
}
