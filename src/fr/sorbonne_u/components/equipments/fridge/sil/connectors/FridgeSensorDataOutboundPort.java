package fr.sorbonne_u.components.equipments.fridge.sil.connectors;

import java.util.concurrent.TimeUnit;
import java.util.function.BiFunction;

import fr.sorbonne_u.components.ComponentI;
import fr.sorbonne_u.components.equipments.fridge.measures.FridgeSensorData;
import fr.sorbonne_u.components.equipments.fridge.sil.FridgeController;
import fr.sorbonne_u.components.equipments.fridge.sil.FridgeSensorDataCI;
import fr.sorbonne_u.components.interfaces.DataRequiredCI;
import fr.sorbonne_u.components.ports.AbstractDataOutboundPort;
import fr.sorbonne_u.components.utils.Measure;

public class FridgeSensorDataOutboundPort extends AbstractDataOutboundPort implements FridgeSensorDataCI.FridgeSensorRequiredPullCI {

	private static final long serialVersionUID = 1L;
	
	protected static final BiFunction<Class<?  extends DataRequiredCI.PullCI>,
									  Class<?  extends DataRequiredCI.PullCI>,
									  Class<?  extends DataRequiredCI.PullCI>> p =
			(a, b) -> {
				System.out.println(DataRequiredCI.PullCI.class.isAssignableFrom(a));
				System.out.println(DataRequiredCI.PullCI.class.isAssignableFrom(b));
				return a;
			};

	public FridgeSensorDataOutboundPort(ComponentI owner) throws Exception {
		super(DataRequiredCI.PullCI.class, DataRequiredCI.PushCI.class, owner);
	}

	public FridgeSensorDataOutboundPort(String uri, ComponentI owner) throws Exception {
		super(uri, FridgeSensorDataCI.FridgeSensorRequiredPullCI.class,
				DataRequiredCI.PushCI.class, owner);
	}
	
	@Override
	public void receive(fr.sorbonne_u.components.interfaces.DataRequiredCI.DataI d) throws Exception {
		this.getOwner().runTask(
				o -> ((FridgeController)o).receiveDataFromFridge(d));
	}

	@Override
	public FridgeSensorData<Measure<Boolean>> coolingPullSensor() throws Exception {
		return ((FridgeSensorDataCI.FridgeSensorRequiredPullCI)this.getConnector()).
					coolingPullSensor();
	}

	@Override
	public FridgeSensorData<Measure<Double>> targetTemperaturePullSensor() throws Exception {
		return ((FridgeSensorDataCI.FridgeSensorRequiredPullCI)this.getConnector()).
				targetTemperaturePullSensor();
	}

	@Override
	public FridgeSensorData<Measure<Double>> currentTemperaturePullSensor() throws Exception {
		return ((FridgeSensorDataCI.FridgeSensorRequiredPullCI)this.getConnector()).
				currentTemperaturePullSensor();
	}

	@Override
	public FridgeSensorData<Measure<Boolean>> doorStatePullSensor() throws Exception {
		return ((FridgeSensorDataCI.FridgeSensorRequiredPullCI)this.getConnector()).
				doorStatePullSensor();
	}

	@Override
	public void startTemperaturesPushSensor(long controlPeriod, TimeUnit tu) throws Exception {
		((FridgeSensorDataCI.FridgeSensorRequiredPullCI)this.getConnector()).
			startTemperaturesPushSensor(controlPeriod, tu);
	}

}
