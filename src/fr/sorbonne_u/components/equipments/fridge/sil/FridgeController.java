package fr.sorbonne_u.components.equipments.fridge.sil;

import java.util.concurrent.CompletableFuture;

import HeaterController.ControlMode;
import fr.sorbonne_u.components.AbstractComponent;
import fr.sorbonne_u.components.annotations.OfferedInterfaces;
import fr.sorbonne_u.components.annotations.RequiredInterfaces;
import fr.sorbonne_u.components.cyphy.utils.aclocks.ClocksServerWithSimulationCI;
import fr.sorbonne_u.components.equipments.fridge.mil.FridgeElectricityModel.FridgeState;
import fr.sorbonne_u.components.equipments.fridge.sil.FridgeSensorDataCI.FridgeSensorRequiredPullCI;
import fr.sorbonne_u.components.equipments.fridge.sil.connectors.FridgeActuatorOutboundPort;
import fr.sorbonne_u.components.equipments.fridge.sil.connectors.FridgeSensorDataOutboundPort;
import fr.sorbonne_u.components.interfaces.DataRequiredCI;
import fr.sorbonne_u.components.interfaces.DataRequiredCI.DataI;
import fr.sorbonne_u.components.utils.ExecutionType;
import fr.sorbonne_u.components.utils.SimulationType;
import fr.sorbonne_u.utils.aclocks.AcceleratedClock;

@RequiredInterfaces(required={FridgeSensorRequiredPullCI.class,
							  FridgeActuatorCI.class,
							  ClocksServerWithSimulationCI.class})
@OfferedInterfaces(offered={DataRequiredCI.PushCI.class})
public class FridgeController extends AbstractComponent implements FridgePushImplementationI {

	// -------------------------------------------------------------------------
	// Constants and variables
	// -------------------------------------------------------------------------
	
	public static enum ControlMode {
		PULL,
		PUSH
	}
	
	public static boolean VERBOSE = true;
	public static int X_RELATIVE_POSITION = 0;
	public static int Y_RELATIVE_POSITION = 0;
	public static boolean DEBUG = true;
	
	public static final double STANDARD_HYSTERESIS = 0.1;
	public static final double STANDARD_CONTROL_PERIOD = 60.0;

	protected String sensorIBP_URI;
	protected String actuatorIBPURI;

	protected FridgeSensorDataOutboundPort		sensorOutboundPort;
	protected FridgeActuatorOutboundPort		actuatorOutboundPort;

	protected double							hysteresis;
	protected double							controlPeriod;
	protected final ControlMode					controlMode;
	protected long								actualControlPeriod;
	protected FridgeState						currentState;
	protected final Object						stateLock;

	// Execution/Simulation

	protected final ExecutionType				currentExecutionType;
	protected final SimulationType				currentSimulationType;

	protected final String						clockURI;
	protected final CompletableFuture<AcceleratedClock>	clock;
	
	
	@Override
	public void receiveDataFromFridge(DataI sd) {
		// TODO Auto-generated method stub

	}

}
