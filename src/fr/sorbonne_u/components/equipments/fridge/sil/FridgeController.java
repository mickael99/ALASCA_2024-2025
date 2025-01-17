package fr.sorbonne_u.components.equipments.fridge.sil;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import fr.sorbonne_u.components.AbstractComponent;
import fr.sorbonne_u.components.annotations.OfferedInterfaces;
import fr.sorbonne_u.components.annotations.RequiredInterfaces;
import fr.sorbonne_u.components.cyphy.utils.aclocks.AcceleratedAndSimulationClock;
import fr.sorbonne_u.components.cyphy.utils.aclocks.ClocksServerWithSimulationCI;
import fr.sorbonne_u.components.cyphy.utils.aclocks.ClocksServerWithSimulationConnector;
import fr.sorbonne_u.components.cyphy.utils.aclocks.ClocksServerWithSimulationOutboundPort;
import fr.sorbonne_u.components.equipments.fridge.measures.FridgeCompoundMeasure;
import fr.sorbonne_u.components.equipments.fridge.measures.FridgeMeasureI;
import fr.sorbonne_u.components.equipments.fridge.measures.FridgeSensorData;
import fr.sorbonne_u.components.equipments.fridge.measures.FridgeStateMeasure;
import fr.sorbonne_u.components.equipments.fridge.mil.FridgeElectricityModel.FridgeState;
import fr.sorbonne_u.components.equipments.fridge.sil.FridgeSensorDataCI.FridgeSensorRequiredPullCI;
import fr.sorbonne_u.components.equipments.fridge.sil.connectors.FridgeActuatorConnector;
import fr.sorbonne_u.components.equipments.fridge.sil.connectors.FridgeActuatorOutboundPort;
import fr.sorbonne_u.components.equipments.fridge.sil.connectors.FridgeSensorDataConnector;
import fr.sorbonne_u.components.equipments.fridge.sil.connectors.FridgeSensorDataOutboundPort;
import fr.sorbonne_u.components.exceptions.ComponentShutdownException;
import fr.sorbonne_u.components.exceptions.ComponentStartException;
import fr.sorbonne_u.components.interfaces.DataRequiredCI;
import fr.sorbonne_u.components.utils.ExecutionType;
import fr.sorbonne_u.components.utils.SimulationType;
import fr.sorbonne_u.exceptions.ImplementationInvariantException;
import fr.sorbonne_u.exceptions.InvariantChecking;
import fr.sorbonne_u.exceptions.InvariantException;
import fr.sorbonne_u.exceptions.PreconditionException;
import fr.sorbonne_u.utils.aclocks.AcceleratedClock;
import fr.sorbonne_u.utils.aclocks.ClocksServer;

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
	
	public static final double STANDARD_HYSTERESIS = 0.5;
	public static final double STANDARD_CONTROL_PERIOD = 60.0;
	private static final long MAX_DOOR_OPEN_DURATION = 15L;

	protected String sensorIBP_URI;
	protected String actuatorIBPURI;
	protected FridgeSensorDataOutboundPort sensorOutboundPort;
	protected FridgeActuatorOutboundPort actuatorOutboundPort;

	protected double hysteresis;
	protected double controlPeriod;
	protected final ControlMode controlMode;
	protected long actualControlPeriod;
	
	protected FridgeState currentState;
	protected final Object stateLock;
	
	protected long doorOpenTimeStamp = -1;

	protected final ExecutionType currentExecutionType;
	protected final SimulationType currentSimulationType;

	protected final String clockURI;
	protected final CompletableFuture<AcceleratedClock>	clock;
	
	
	// -------------------------------------------------------------------------
	// Invariants
	// -------------------------------------------------------------------------

	protected static boolean glassBoxInvariants(FridgeController instance) {
		assert	instance != null :
				new PreconditionException("instance != null");

		boolean ret = true;
		ret &= InvariantChecking.checkGlassBoxInvariant(
					instance.hysteresis > 0.0,
					FridgeController.class,
					instance,
					"hysteresis > 0.0");
		ret &= InvariantChecking.checkGlassBoxInvariant(
					instance.controlPeriod > 0,
					FridgeController.class,
					instance,
					"controlPeriod > 0");
		ret &= InvariantChecking.checkGlassBoxInvariant(
					instance.currentExecutionType.isStandard() ||
						instance.clockURI != null && !instance.clockURI.isEmpty(),
					FridgeController.class,
					instance,
					"currentExecutionType.isStandard() || "
					+ "clockURI != null && !clockURI.isEmpty()");
		ret &= InvariantChecking.checkGlassBoxInvariant(
					instance.sensorIBP_URI != null &&
											!instance.sensorIBP_URI.isEmpty(),
					FridgeController.class, instance,
					"sensorIBP_URI != null && !sensorIBP_URI.isEmpty()");
		ret &= InvariantChecking.checkGlassBoxInvariant(
					instance.actuatorIBPURI != null &&
											!instance.actuatorIBPURI.isEmpty(),
					FridgeController.class, instance,
					"actuatorIBPURI != null && !actuatorIBPURI.isEmpty()");
		return ret;
	}

	protected static boolean blackBoxInvariants(FridgeController instance) {
		assert	instance != null :
				new PreconditionException("instance != null");

		boolean ret = true;
		ret &= InvariantChecking.checkBlackBoxInvariant(
					X_RELATIVE_POSITION >= 0,
					FridgeController.class, instance,
					"X_RELATIVE_POSITION >= 0");
		ret &= InvariantChecking.checkBlackBoxInvariant(
					Y_RELATIVE_POSITION >= 0,
					FridgeController.class, instance,
					"Y_RELATIVE_POSITION >= 0");
		ret &= InvariantChecking.checkBlackBoxInvariant(
					STANDARD_HYSTERESIS > 0.0,
					FridgeController.class,
					instance,
					"STANDARD_HYSTERESIS > 0.0");
		ret &= InvariantChecking.checkBlackBoxInvariant(
					STANDARD_CONTROL_PERIOD > 0,
					FridgeController.class,
					instance,
					"STANDARD_CONTROL_PERIOD > 0");
		return ret;
	}
	
	// -------------------------------------------------------------------------
	// Constructors
	// -------------------------------------------------------------------------

	protected FridgeController(String sensorIBP_URI, String actuatorIBP_URI) throws Exception {
		this(sensorIBP_URI, actuatorIBP_URI, STANDARD_HYSTERESIS, STANDARD_CONTROL_PERIOD, ControlMode.PULL);
	}

	protected FridgeController(String sensorIBP_URI, String actuatorIBP_URI, double hysteresis, double controlPeriod, 
								ControlMode controlMode ) throws Exception
	{
		this(sensorIBP_URI, actuatorIBP_URI, hysteresis, controlPeriod,
			 ControlMode.PULL, ExecutionType.STANDARD,
			 SimulationType.NO_SIMULATION, null);
	}

	protected FridgeController(String sensorIBP_URI, String actuatorIBP_URI, double hysteresis, double controlPeriod,
								ControlMode controlMode, ExecutionType currentExecutionType) throws Exception
	{
		this(sensorIBP_URI, actuatorIBP_URI, hysteresis, controlPeriod,
			 ControlMode.PULL, currentExecutionType,
			 SimulationType.NO_SIMULATION, null);

		assert	currentExecutionType.isTest() :
				new PreconditionException("currentExecutionType.isTest()");
	}

	protected FridgeController(String sensorIBP_URI, String actuatorIBP_URI, double hysteresis, double controlPeriod,
								ControlMode controlMode, ExecutionType currentExecutionType, SimulationType currentSimulationType,
								String clockURI) throws Exception
	{
		super(2, 1);

		assert	sensorIBP_URI != null && !sensorIBP_URI.isEmpty() :
				new PreconditionException(
						"sensorIBP_URI != null && !sensorIBP_URI.isEmpty()");
		assert	actuatorIBP_URI != null && !actuatorIBP_URI.isEmpty() :
				new PreconditionException(
					"actuatorIBP_URI != null && !actuatorIBP_URI.isEmpty()");
		assert	hysteresis > 0.0 :
				new PreconditionException("hysteresis > 0.0");
		assert	controlPeriod > 0 :
				new PreconditionException("controlPeriod > 0");
		assert	currentExecutionType.isStandard() ||
									clockURI != null && !clockURI.isEmpty() :
				new PreconditionException(
						"currentExecutionType.isStandard() || "
						+ "clockURI != null && !clockURI.isEmpty()");

		this.sensorIBP_URI = sensorIBP_URI;
		this.actuatorIBPURI = actuatorIBP_URI;
		this.hysteresis = hysteresis;
		this.controlPeriod = controlPeriod;
		this.controlMode = controlMode;
		this.currentExecutionType = currentExecutionType;
		this.currentSimulationType = currentSimulationType;
		this.clockURI = clockURI;
		this.clock = new CompletableFuture<>();

		this.actualControlPeriod = (long)(this.controlPeriod * TimeUnit.SECONDS.toNanos(1));
		this.stateLock = new Object();

		this.sensorOutboundPort = new FridgeSensorDataOutboundPort(this);
		this.sensorOutboundPort.publishPort();
		this.actuatorOutboundPort = new FridgeActuatorOutboundPort(this);
		this.actuatorOutboundPort.publishPort();

		if (VERBOSE || DEBUG) {
			this.tracer.get().setTitle("Fridge controller component");
			this.tracer.get().setRelativePosition(X_RELATIVE_POSITION, Y_RELATIVE_POSITION);
			this.toggleTracing();
		}

		assert	glassBoxInvariants(this) :
				new ImplementationInvariantException("glassBoxInvariants(this)");
		assert	blackBoxInvariants(this) :
				new InvariantException("blackBoxInvariants(this)");
	}
	
	
	// -------------------------------------------------------------------------
	// Component life-cycle
	// -------------------------------------------------------------------------

	@Override
	public synchronized void start() throws ComponentStartException {
		super.start();

		try {
			this.doPortConnection(
					this.sensorOutboundPort.getPortURI(),
					sensorIBP_URI,
					FridgeSensorDataConnector.class.getCanonicalName());
			this.doPortConnection(
					this.actuatorOutboundPort.getPortURI(),
					this.actuatorIBPURI,
					FridgeActuatorConnector.class.getCanonicalName());

			synchronized (this.stateLock) {
				this.currentState = FridgeState.OFF;
			}

			if (VERBOSE) 
				this.traceMessage("Fridge controller starts.\n");
			
		} catch (Exception e) {
			throw new ComponentStartException(e);
		}
	}

	@Override
	public void execute() throws Exception {
		if (!this.currentExecutionType.isStandard()) {
			ClocksServerWithSimulationOutboundPort clockServerOBP =
							new ClocksServerWithSimulationOutboundPort(this);
			clockServerOBP.publishPort();
			this.doPortConnection(
					clockServerOBP.getPortURI(),
					ClocksServer.STANDARD_INBOUNDPORT_URI,
					ClocksServerWithSimulationConnector.class.getCanonicalName());
			AcceleratedAndSimulationClock clock =
					clockServerOBP.getClockWithSimulation(this.clockURI);
			this.doPortDisconnection(clockServerOBP.getPortURI());
			clockServerOBP.unpublishPort();
			clockServerOBP.destroyPort();
			
			this.actualControlPeriod = (long)((this.controlPeriod * TimeUnit.SECONDS.toNanos(1))/
												clock.getAccelerationFactor());
			this.clock.complete(clock);

			if (VERBOSE) {
				if (this.currentSimulationType.isMilSimulation()
							|| this.currentSimulationType.isMILRTSimulation()) {
					this.traceMessage(
						"FridgeController has no MIL or MIL RT simulator yet.\n");
				} else if (this.currentSimulationType.isSILSimulation() ||
						this.currentSimulationType.isHILSimulation()) {
					this.traceMessage(
						"FridgeController does not use a simulator in SIL or"
						+ " HIL simulations.\n");
				}
			}
		}
	}

	@Override
	public synchronized void finalise() throws Exception {
		if (VERBOSE) 
			this.traceMessage("Fidge controller ends.\n");
		
		this.doPortDisconnection(this.sensorOutboundPort.getPortURI());
		this.doPortDisconnection(this.actuatorOutboundPort.getPortURI());
		
		super.finalise();
	}

	@Override
	public synchronized void shutdown() throws ComponentShutdownException {
		try {
			this.sensorOutboundPort.unpublishPort();
			this.actuatorOutboundPort.unpublishPort();
		} catch (Exception e) {
			throw new ComponentShutdownException(e) ;
		}
		super.shutdown();
	}
	
	
	// -------------------------------------------------------------------------
	// Component internal methods
	// -------------------------------------------------------------------------
	
	@SuppressWarnings("unchecked")
	@Override
	public void	receiveDataFromFridge(DataRequiredCI.DataI sd) {
		assert	sd != null : new PreconditionException("sd != null");
		if(DEBUG) 
			this.traceMessage("receives fridge sensor data: " + sd + ".\n");
		
		FridgeMeasureI fm = ((FridgeSensorData<FridgeMeasureI>)sd).getMeasure();
		
		if(fm.isStateMeasure()) {
			FridgeState fridgeState = ((FridgeStateMeasure)fm).getData();
			if (this.clock != null) {
				try {
					this.clock.get();
					if (this.actualControlPeriod < TimeUnit.MILLISECONDS.toNanos(10)) {
						System.out.println(
								"Warning: accelerated control period is "
										+ "too small ("
										+ this.actualControlPeriod +
										"), unexpected scheduling problems may"
										+ " occur!");
					}
				} catch (Exception e) {
					throw new RuntimeException(e) ;
				}
			}

			synchronized(this.stateLock) {
				FridgeState oldState = this.currentState;
				this.currentState = fridgeState;
				
				//Check if the door is open
				if(fridgeState == FridgeState.DOOR_OPEN && oldState != FridgeState.DOOR_OPEN) 
					this.doorOpenTimeStamp = System.nanoTime();	
				
				if(fridgeState != FridgeState.DOOR_OPEN)
					this.doorOpenTimeStamp = -1;
				
				if (fridgeState != FridgeState.OFF && oldState == FridgeState.OFF ) {
					if (this.controlMode == ControlMode.PULL) {
						if (VERBOSE) {
							this.traceMessage("start pull control.\n");
						}
						
						if (this.currentExecutionType.isTest() && this.currentSimulationType.isNoSimulation()) {
							this.pullControlLoop();
						} 
						else {
							this.scheduleTaskOnComponent(
								new AbstractComponent.AbstractTask() {
									
									@Override
									public void run() {
										((FridgeController)this.getTaskOwner()).pullControlLoop();
									}
								},
								this.actualControlPeriod, 
								TimeUnit.NANOSECONDS);
						}
					} 
					else {
						if (VERBOSE) {
							this.traceMessage("start push control.\n");
						}
						long cp = (long)(TimeUnit.SECONDS.toMillis(1) * this.controlPeriod);
						try {
							this.sensorOutboundPort.startTemperaturesPushSensor(cp, TimeUnit.MILLISECONDS);
						} 
						catch (Exception e) {
							throw new RuntimeException(e) ;
						}
					}
				}
			}
		} 
		else {
			assert	fm.isTemperatureMeasures();
			this.pushControlLoop((FridgeCompoundMeasure)fm);
		}
	}

	protected void pushControlLoop(FridgeCompoundMeasure fm) {
		try {
			FridgeState s = FridgeState.OFF;
			synchronized (this.stateLock) {
				s = this.currentState;
			}
			
			if (s != FridgeState.OFF) {
				double current = fm.getCurrentTemperature();
				double target = fm.getTargetTemperature();
				this.oneControlStep(current, target, s);
			} 
			else {
				if (VERBOSE) {
					this.traceMessage("control is off.\n");
				}
			}
		} catch (Exception e) {
			throw new RuntimeException(e) ;
		}
	}

	protected void pullControlLoop() {
		this.traceMessage("executes a new pull control step.\n");
		try {
			FridgeState s = FridgeState.OFF;
			synchronized (this.stateLock) {
				s = this.currentState;
			}
			
			if (s != FridgeState.OFF) {
				@SuppressWarnings("unchecked")
				FridgeSensorData<FridgeCompoundMeasure> td =
						(FridgeSensorData<FridgeCompoundMeasure>)
										this.sensorOutboundPort.request();

				if (DEBUG) 
					this.traceMessage(td + "\n");
				
				double current = td.getMeasure().getCurrentTemperature();
				double target = td.getMeasure().getTargetTemperature();
				this.oneControlStep(current, target, s);

				if (this.currentExecutionType.isStandard()
							|| this.currentSimulationType.isSILSimulation()
							|| this.currentSimulationType.isHILSimulation()) {
					this.scheduleTask(
							o -> ((FridgeController)o).pullControlLoop(),
							this.actualControlPeriod, 
							TimeUnit.NANOSECONDS);
				}
			} 
			else {
				if (VERBOSE) {
					this.traceMessage("exit the control.\n");
				}
			}
		} catch (Exception e) {
			throw new RuntimeException(e) ;
		}
	}
	
	protected void oneControlStep(double current, double target, FridgeState s) throws Exception {
		StringBuffer sb = new StringBuffer();
		
		// Door management
		if(s == FridgeState.DOOR_OPEN) {
			if(this.doorOpenTimeStamp > 0) {
				long currentTime = System.nanoTime();
				long doorOpenDuration = TimeUnit.NANOSECONDS.toSeconds(currentTime - this.doorOpenTimeStamp);
				if(doorOpenDuration > MAX_DOOR_OPEN_DURATION) {
					this.actuatorOutboundPort.closeDoor();
					if(VERBOSE) {
						this.traceMessage("Door closed automatically after " + doorOpenDuration + " seconds.\n");
					}
					this.doorOpenTimeStamp = -1;
				}
			}
		}
		// If the door is open, we can't handle the fridge cooling
		else {
			if (current > target + this.hysteresis) {
				if (FridgeState.COOLING != s) {
					this.actuatorOutboundPort.startCooling();
				}
				if (VERBOSE) {
					StringBuffer temp = new StringBuffer();
					temp.append(current);
					temp.append(" > ");
					temp.append(target);
					if (FridgeState.COOLING != s) {
						sb.append("start cooling with ");
						sb.append(temp);
						sb.append(" - ");
					} 
					else {
						sb.append("still cooling with ");
						sb.append(temp);
						sb.append(" + ");
					}
					sb.append(this.hysteresis);
					sb.append(" at ");
					sb.append(this.clock.get().currentInstant());
					sb.append(".\n");
					this.traceMessage(sb.toString());
				}
			} 
			else if (current < target - this.hysteresis) {
				if (FridgeState.COOLING == s) {
					this.actuatorOutboundPort.stopCooling();;
				}
				StringBuffer temp = new StringBuffer();
				temp.append(current);
				temp.append(" < ");
				temp.append(target);
				if (VERBOSE) {
					if (FridgeState.COOLING == s) {
						sb.append("stop cooling with ");
						sb.append(temp);
						sb.append(" + ");
					} 
					else {
						sb.append("still not cooling with ");
						sb.append(temp);
						sb.append(" - ");
					}
					sb.append(this.hysteresis);
					sb.append(" at ");
					sb.append(this.clock.get().currentInstant());
					sb.append(".\n");
					this.traceMessage(sb.toString());
				}
			} 
			else {
				if (VERBOSE) {
					if (FridgeState.COOLING == s) {
						sb.append("still cooling with ");
						sb.append(current);
						sb.append(" > ");
						sb.append(target);
						sb.append(" + ");
					} 
					else {
						sb.append("still not cooling with ");
						sb.append(current);
						sb.append(" < ");
						sb.append(target);
						sb.append(" - ");
				}
				sb.append(this.hysteresis);
				sb.append(" at ");
				sb.append(this.clock.get().currentInstant());
				sb.append(".\n");
					this.traceMessage(sb.toString());
				}
			}
		}
	}
}
