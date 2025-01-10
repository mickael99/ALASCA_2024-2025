package fr.sorbonne_u.components.equipments.fridge.sil.connectors;

import java.util.concurrent.TimeUnit;

import fr.sorbonne_u.components.ComponentI;
import fr.sorbonne_u.components.equipments.fridge.Fridge;
import fr.sorbonne_u.components.equipments.fridge.interfaces.FridgeInternalControlI;
import fr.sorbonne_u.components.equipments.fridge.measures.FridgeCompoundMeasure;
import fr.sorbonne_u.components.equipments.fridge.measures.FridgeSensorData;
import fr.sorbonne_u.components.equipments.fridge.sil.FridgeSensorDataCI;
import fr.sorbonne_u.components.interfaces.DataOfferedCI;
import fr.sorbonne_u.components.ports.AbstractDataInboundPort;
import fr.sorbonne_u.components.utils.Measure;
import fr.sorbonne_u.exceptions.PreconditionException;

public class FridgeSensorDataInboundPort extends AbstractDataInboundPort implements FridgeSensorDataCI.FridgeSensorOfferedPullCI {

	
	private static final long serialVersionUID = 1L;

	public FridgeSensorDataInboundPort(ComponentI owner) throws Exception {
		super(FridgeSensorDataCI.FridgeSensorOfferedPullCI.class,
			  DataOfferedCI.PushCI.class, owner);

		assert	owner instanceof FridgeInternalControlI :
				new PreconditionException(
						"owner instanceof FridgeInternalControlI");
	}

	public FridgeSensorDataInboundPort(String uri, ComponentI owner) throws Exception {
		super(uri, FridgeSensorDataCI.FridgeSensorOfferedPullCI.class,
			  DataOfferedCI.PushCI.class, owner);

		assert	owner instanceof FridgeInternalControlI :
				new PreconditionException(
						"owner instanceof FridgeInternalControlI");
	}
	
	@Override
	public DataOfferedCI.DataI get() throws Exception {
		return new FridgeSensorData<FridgeCompoundMeasure>(
				new FridgeCompoundMeasure(
						this.targetTemperaturePullSensor().getMeasure(),
						this.currentTemperaturePullSensor().getMeasure()));
	}

	@Override
	public FridgeSensorData<Measure<Boolean>> coolingPullSensor() throws Exception {
		return this.getOwner().handleRequest(
				o -> ((Fridge)o).coolingPullSensor());
	}

	@Override
	public FridgeSensorData<Measure<Double>> targetTemperaturePullSensor() throws Exception {
		return this.getOwner().handleRequest(
				o -> ((Fridge)o).targetTemperaturePullSensor());
	}

	@Override
	public FridgeSensorData<Measure<Double>> currentTemperaturePullSensor() throws Exception {
		return this.getOwner().handleRequest(
				o -> ((Fridge)o).currentTemperaturePullSensor());
	}

	@Override
	public FridgeSensorData<Measure<Boolean>> doorStatePullSensor() throws Exception {
		return this.getOwner().handleRequest(
				o -> ((Fridge)o).doorStatePullSensor());
	}

	@Override
	public void startTemperaturesPushSensor(long controlPeriod, TimeUnit tu) throws Exception {
		this.getOwner().handleRequest(
				o -> { ((Fridge)o).startTemperaturesPushSensor(controlPeriod, tu);
						return null;
					 });
	}

}
