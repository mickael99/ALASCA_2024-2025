package fr.sorbonne_u.components.equipments.fridge;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import fr.sorbonne_u.components.annotations.OfferedInterfaces;
import fr.sorbonne_u.components.annotations.RequiredInterfaces;
import fr.sorbonne_u.components.cyphy.AbstractCyPhyComponent;
import fr.sorbonne_u.components.cyphy.plugins.devs.AtomicSimulatorPlugin;
import fr.sorbonne_u.components.cyphy.plugins.devs.RTAtomicSimulatorPlugin;
import fr.sorbonne_u.components.cyphy.utils.aclocks.AcceleratedAndSimulationClock;
import fr.sorbonne_u.components.cyphy.utils.aclocks.ClocksServerWithSimulationCI;
import fr.sorbonne_u.components.cyphy.utils.aclocks.ClocksServerWithSimulationConnector;
import fr.sorbonne_u.components.cyphy.utils.aclocks.ClocksServerWithSimulationOutboundPort;
import fr.sorbonne_u.components.exceptions.BCMException;
import fr.sorbonne_u.components.exceptions.ComponentShutdownException;
import fr.sorbonne_u.components.exceptions.ComponentStartException;
import fr.sorbonne_u.components.interfaces.DataOfferedCI;
import fr.sorbonne_u.components.utils.ExecutionType;
import fr.sorbonne_u.components.utils.Measure;
import fr.sorbonne_u.components.utils.MeasurementUnit;
import fr.sorbonne_u.components.utils.SimulationType;
import fr.sorbonne_u.devs_simulation.architectures.Architecture;
import fr.sorbonne_u.devs_simulation.models.time.Duration;
import fr.sorbonne_u.devs_simulation.models.time.Time;
import fr.sorbonne_u.exceptions.InvariantChecking;
import fr.sorbonne_u.exceptions.InvariantException;
import fr.sorbonne_u.exceptions.PostconditionException;
import fr.sorbonne_u.exceptions.PreconditionException;
import fr.sorbonne_u.utils.aclocks.AcceleratedClock;
import fr.sorbonne_u.utils.aclocks.ClocksServer;
import java.util.HashMap;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import fr.sorbonne_u.components.AbstractComponent;
import fr.sorbonne_u.components.equipments.fridge.connections.FridgeExternalControlInboundPort;
import fr.sorbonne_u.components.equipments.fridge.connections.FridgeInternalControlInboundPort;
import fr.sorbonne_u.components.equipments.fridge.connections.FridgeUserInboundPort;
import fr.sorbonne_u.components.equipments.fridge.interfaces.FridgeExternalControlCI;
import fr.sorbonne_u.components.equipments.fridge.interfaces.FridgeInternalControlCI;
import fr.sorbonne_u.components.equipments.fridge.interfaces.FridgeInternalControlI;
import fr.sorbonne_u.components.equipments.fridge.interfaces.FridgeUserCI;
import fr.sorbonne_u.components.equipments.fridge.interfaces.FridgeUserI;
import fr.sorbonne_u.components.equipments.fridge.measures.FridgeCompoundMeasure;
import fr.sorbonne_u.components.equipments.fridge.measures.FridgeSensorData;
import fr.sorbonne_u.components.equipments.fridge.measures.FridgeStateMeasure;
import fr.sorbonne_u.components.equipments.fridge.mil.FridgeElectricityModel.FridgeState;
import fr.sorbonne_u.components.equipments.fridge.mil.FridgeStateModel;
import fr.sorbonne_u.components.equipments.fridge.mil.FridgeTemperatureModel;
import fr.sorbonne_u.components.equipments.fridge.mil.events.CloseDoorFridge;
import fr.sorbonne_u.components.equipments.fridge.mil.events.CoolFridge;
import fr.sorbonne_u.components.equipments.fridge.mil.events.DoNotCoolFridge;
import fr.sorbonne_u.components.equipments.fridge.mil.events.OpenDoorFridge;
import fr.sorbonne_u.components.equipments.fridge.mil.events.SetPowerFridge;
import fr.sorbonne_u.components.equipments.fridge.mil.events.SwitchOffFridge;
import fr.sorbonne_u.components.equipments.fridge.mil.events.SwitchOnFridge;
import fr.sorbonne_u.components.equipments.fridge.sil.FridgeActuatorCI;
import fr.sorbonne_u.components.equipments.fridge.sil.FridgeSensorDataCI;
import fr.sorbonne_u.components.equipments.fridge.sil.connectors.FridgeActuatorInboundPort;
import fr.sorbonne_u.components.equipments.fridge.sil.connectors.FridgeSensorDataInboundPort;
import fr.sorbonne_u.components.equipments.heater.mil.events.SetPowerHeater.PowerValue;
import fr.sorbonne_u.components.equipments.hem.registration.RegistrationOutboundPort;
import fr.sorbonne_u.components.equipments.fridge.mil.LocalSimulationArchitectures;


@OfferedInterfaces(offered = {FridgeExternalControlCI.class, 
							  FridgeInternalControlCI.class, 
							  FridgeUserCI.class,
							  FridgeSensorDataCI.FridgeSensorOfferedPullCI.class, 
							  FridgeActuatorCI.class})
@RequiredInterfaces(required = {/*RegistrationCI.class, */DataOfferedCI.PushCI.class, ClocksServerWithSimulationCI.class})
public class Fridge extends AbstractCyPhyComponent implements FridgeInternalControlI, FridgeUserI {
	
	
	// -------------------------------------------------------------------------
	// Constants and variables
	// -------------------------------------------------------------------------
	
	public static enum FridgeSensorMeasures {
		STATUS,
		TARGET_TEMPERATURE,
		CURRENT_TEMPERATURE,
		COMPOUND_TEMPERATURES
	}
	
	public static final String URI = "FRIDGE-URI";
	public static final String REFLECTION_INBOUND_PORT_URI = "FRIDGE-RIP-URI";
	
	// Tracing
	public static boolean VERBOSE = true;
	public static int X_RELATIVE_POSITION = 0;			
	public static int Y_RELATIVE_POSITION = 0;
	
	// State
	protected FridgeState currentState;
	
	// Power level
	public static final double MAX_COOLING_POWER = 500.0;
	protected double currentCoolingPower;
	
	// Temperature
	public static final MeasurementUnit	TEMPERATURE_UNIT = MeasurementUnit.CELSIUS;
	protected static final double MIN_TEMPERATURE = 0.0;
	protected static final double MAX_TEMPERATURE = 8.0;
	protected static final double FAKE_CURRENT_TEMPERATURE = 6.0;
	public static final double STANDARD_TARGET_TEMPERATURE = 2.0;
	protected double targetTemperature;
	
	// Connections
	public static final String USER_INBOUND_PORT_URI = "FRIDGE-USER-INBOUND-PORT-URI";
	public static final String INTERNAL_CONTROL_INBOUND_PORT_URI = "FRIDGE-INTERNAL-CONTROL-INBOUND-PORT-URI";
	public static final String EXTERNAL_CONTROL_INBOUND_PORT_URI = "FRIDGE-EXTERNAL-CONTROL-INBOUND-PORT-URI";
	public static final String SENSOR_INBOUND_PORT_URI = "FRIDGE-SENSOR-INBOUND-PORT-URI";
	public static final String ACTUATOR_INBOUND_PORT_URI = "Fridge-ACTUATOR-INBOUND-PORT-URI";
	
	protected FridgeUserInboundPort userInbound;
	protected FridgeInternalControlInboundPort internalInbound;
	protected FridgeExternalControlInboundPort externalInbound;
	protected FridgeSensorDataInboundPort sensorInboundPort;
	protected FridgeActuatorInboundPort actuatorInboundPort;
	
	// Registration
	public static boolean TEST_REGISTRATION = false;
	protected RegistrationOutboundPort registrationPort;
	protected boolean isHEMConnectionRequired;
	protected static final String XML_PATH = "fridge-descriptor.xml";
	
	// Execution / Simulation
	protected final String clockURI;
	protected final CompletableFuture<AcceleratedClock>	clock;
	protected final ExecutionType currentExecutionType;
	protected final SimulationType currentSimulationType;
	protected AtomicSimulatorPlugin asp;
	protected final String globalArchitectureURI;
	protected final String localArchitectureURI;
	protected final TimeUnit simulationTimeUnit;														
	protected double accFactor;
	protected static final String CURRENT_TEMPERATURE_NAME = "currentTemperature";
	
	
	// -------------------------------------------------------------------------
	// Invariants
	// -------------------------------------------------------------------------

	protected static boolean glassBoxInvariants(Fridge f) {
		assert	f != null : new PreconditionException("f != null");

		boolean ret = true;
		ret &= InvariantChecking.checkGlassBoxInvariant(
					MAX_COOLING_POWER > 0.0,
					Fridge.class, f,
					"MAX_COOLING_POWER > 0.0");
		ret &= InvariantChecking.checkGlassBoxInvariant(
					f.currentState != null,
					Fridge.class, f,
					"f.currentState != null");
		ret &= InvariantChecking.checkGlassBoxInvariant(
					f.targetTemperature >= -50.0 && f.targetTemperature <= 50.0,
					Fridge.class, f,
					"f.targetTemperature >= -50.0 && f.targetTemperature <= 50.0");
		ret &= InvariantChecking.checkGlassBoxInvariant(
					f.currentCoolingPower >= 0.0 &&
							f.currentCoolingPower <= MAX_COOLING_POWER,
					Fridge.class, f,
					"f.currentCoolingPower >= 0.0 && "
								+ "f.currentCoolingPower <= MAX_COOLING_POWER");
		ret &= InvariantChecking.checkGlassBoxInvariant(
					f.currentExecutionType != null,
					Fridge.class, f,
					"currentExecutionType != null");
		ret &= InvariantChecking.checkGlassBoxInvariant(
					f.currentSimulationType != null,
					Fridge.class, f,
					"currentSimulationType != null");
		ret &= InvariantChecking.checkGlassBoxInvariant(
					!f.currentExecutionType.isStandard() ||
							f.currentSimulationType.isNoSimulation(),
					Fridge.class, f,
					"!currentExecutionType.isStandard() || "
					+ "currentSimulationType.isNoSimulation()");
		ret &= InvariantChecking.checkGlassBoxInvariant(
					f.currentSimulationType.isNoSimulation() ||
						(f.globalArchitectureURI != null &&
							!f.globalArchitectureURI.isEmpty()),
					Fridge.class, f,
					"currentSimulationType.isNoSimulation() || "
					+ "(globalArchitectureURI != null && "
					+ "!globalArchitectureURI.isEmpty())");
		ret &= InvariantChecking.checkGlassBoxInvariant(
					f.currentSimulationType.isNoSimulation() ||
						(f.localArchitectureURI != null &&
							!f.localArchitectureURI.isEmpty()),
					Fridge.class, f,
					"currentSimulationType.isNoSimulation() || "
					+ "(localArchitectureURI != null && "
					+ "!localArchitectureURI.isEmpty())");
		ret &= InvariantChecking.checkGlassBoxInvariant(
					!f.currentSimulationType.isSimulated() ||
											f.simulationTimeUnit != null,
					Fridge.class, f,
					"!currentSimulationType.isSimulated() || "
					+ "simulationTimeUnit != null");
		ret &= InvariantChecking.checkGlassBoxInvariant(
					!f.currentSimulationType.isRealTimeSimulation() ||
													f.accFactor > 0.0,
					Fridge.class, f,
					"!currentSimulationType.isRealTimeSimulation() || "
					+ "accFactor > 0.0");
		return ret;
	}

	protected static boolean blackBoxInvariants(Fridge f)
	{
		assert	f != null : new PreconditionException("f != null");

		boolean ret = true;
		ret &= InvariantChecking.checkBlackBoxInvariant(
					REFLECTION_INBOUND_PORT_URI != null &&
									!REFLECTION_INBOUND_PORT_URI.isEmpty(),
					Fridge.class, f,
					"REFLECTION_INBOUND_PORT_URI != null && "
								+ "!REFLECTION_INBOUND_PORT_URI.isEmpty()");
		ret &= InvariantChecking.checkBlackBoxInvariant(
					TEMPERATURE_UNIT != null,
					Fridge.class, f,
					"TEMPERATURE_UNIT != null");
		ret &= InvariantChecking.checkBlackBoxInvariant(
					USER_INBOUND_PORT_URI != null &&
											!USER_INBOUND_PORT_URI.isEmpty(),
					Fridge.class, f,
					"USER_INBOUND_PORT_URI != null && "
					+ "!USER_INBOUND_PORT_URI.isEmpty()");
		ret &= InvariantChecking.checkBlackBoxInvariant(
					INTERNAL_CONTROL_INBOUND_PORT_URI != null &&
								!INTERNAL_CONTROL_INBOUND_PORT_URI.isEmpty(),
					Fridge.class, f,
					"INTERNAL_CONTROL_INBOUND_PORT_URI != null && "
							+ "!INTERNAL_CONTROL_INBOUND_PORT_URI.isEmpty()");
		ret &= InvariantChecking.checkBlackBoxInvariant(
					EXTERNAL_CONTROL_INBOUND_PORT_URI != null &&
								!EXTERNAL_CONTROL_INBOUND_PORT_URI.isEmpty(),
					Fridge.class, f,
					"EXTERNAL_CONTROL_INBOUND_PORT_URI != null &&"
							+ "!EXTERNAL_CONTROL_INBOUND_PORT_URI.isEmpty()");
		ret &= InvariantChecking.checkBlackBoxInvariant(
					SENSOR_INBOUND_PORT_URI != null &&
										!SENSOR_INBOUND_PORT_URI.isEmpty(),
					Fridge.class, f,
					"SENSOR_INBOUND_PORT_URI != null && "
					+ "!SENSOR_INBOUND_PORT_URI.isEmpty()");
		ret &= InvariantChecking.checkBlackBoxInvariant(
					ACTUATOR_INBOUND_PORT_URI != null &&
										!ACTUATOR_INBOUND_PORT_URI.isEmpty(),
					Fridge.class, f,
					"ACTUATOR_INBOUND_PORT_URI != null && "
					+ "!ACTUATOR_INBOUND_PORT_URI.isEmpty()");
		ret &= InvariantChecking.checkBlackBoxInvariant(
					X_RELATIVE_POSITION >= 0,
					Fridge.class, f,
					"X_RELATIVE_POSITION >= 0");
		ret &= InvariantChecking.checkBlackBoxInvariant(
					Y_RELATIVE_POSITION >= 0,
					Fridge.class, f,
					"Y_RELATIVE_POSITION >= 0");
		return ret;
	}
		
		
	// -------------------------------------------------------------------------
	// Constructors
	// -------------------------------------------------------------------------
	
	protected Fridge() throws Exception {
		this(USER_INBOUND_PORT_URI, INTERNAL_CONTROL_INBOUND_PORT_URI,
				 EXTERNAL_CONTROL_INBOUND_PORT_URI, SENSOR_INBOUND_PORT_URI,
				 ACTUATOR_INBOUND_PORT_URI);
	}
	
	protected Fridge(String fridgeUserInboundPortURI, String fridgeInternalControlInboundPortURI,
						String fridgeExternalControlInboundPortURI, String fridgeSensorInboundPortURI,
						String fridgeActuatorInboundPortURI) throws Exception
	{
		this(REFLECTION_INBOUND_PORT_URI,
			 fridgeUserInboundPortURI,
			 fridgeInternalControlInboundPortURI,
			 fridgeExternalControlInboundPortURI,
			 fridgeSensorInboundPortURI,
			 fridgeActuatorInboundPortURI,
			 ExecutionType.STANDARD,
			 SimulationType.NO_SIMULATION,
			 null, null, null, 0.0, null);
	}
	
	protected Fridge(String reflectionInboundPortURI, String fridgeUserInboundPortURI,
						String fridgeInternalControlInboundPortURI, String fridgeExternalControlInboundPortURI,
						String fridgeSensorInboundPortURI, String fridgeActuatorInboundPortURI,
						ExecutionType currentExecutionType, SimulationType currentSimulationType,
						String globalArchitectureURI, String localArchitectureURI,
						TimeUnit simulationTimeUnit, double accFactor, String clockURI) throws Exception
	{
		super(reflectionInboundPortURI, 2, 1);

		assert	currentExecutionType != null :
				new PreconditionException("currentExecutionType != null");
		assert	!currentExecutionType.isStandard() ||
									clockURI != null && !clockURI.isEmpty() :
				new PreconditionException(
						"!currentExecutionType.isStandard() || "
						+ "clockURI != null && !clockURI.isEmpty()");
		assert	!currentExecutionType.isStandard() ||
									currentSimulationType.isNoSimulation() :
				new PreconditionException(
						"!currentExecutionType.isStandard() || "
						+ "currentSimulationType.isNoSimulation()");
		assert	currentSimulationType.isNoSimulation() ||
							(globalArchitectureURI != null &&
										!globalArchitectureURI.isEmpty()) :
				new PreconditionException(
						"currentSimulationType.isNoSimulation() ||  "
						+ "(globalArchitectureURI != null && "
						+ "!globalArchitectureURI.isEmpty())");
		assert	currentSimulationType.isNoSimulation() ||
							(localArchitectureURI != null &&
											!localArchitectureURI.isEmpty()) :
				new PreconditionException(
						"currentSimulationType.isNoSimulation() ||  "
						+ "(localArchitectureURI != null && "
						+ "!localArchitectureURI.isEmpty())");
		assert	!currentSimulationType.isSimulated() ||
													simulationTimeUnit != null :
				new PreconditionException(
						"!currentSimulationType.isSimulated() || "
						+ "simulationTimeUnit != null");
		assert	!currentSimulationType.isRealTimeSimulation() ||
															accFactor > 0.0 :
				new PreconditionException(
						"!currentExecutionType.isRealTimeSimulation() || "
						+ "accFactor > 0.0");

		this.currentExecutionType = currentExecutionType;
		this.currentSimulationType = currentSimulationType;
		this.globalArchitectureURI = globalArchitectureURI;
		this.localArchitectureURI = localArchitectureURI;
		this.simulationTimeUnit = simulationTimeUnit;
		this.accFactor = accFactor;
		this.clockURI = clockURI;
		this.clock = new CompletableFuture<AcceleratedClock>();

		this.initialise(fridgeUserInboundPortURI,
						fridgeInternalControlInboundPortURI,
						fridgeExternalControlInboundPortURI,
						fridgeSensorInboundPortURI,
						fridgeActuatorInboundPortURI);

		assert	Fridge.glassBoxInvariants(this) :
				new InvariantException("Fridge.glassBoxInvariants(this)");
		assert	Fridge.blackBoxInvariants(this) :
				new InvariantException("Fridge.blackBoxInvariants(this)");
	}
	
	protected void initialise(String fridgeUserInboundPortURI, String fridgeInternalControlInboundPortURI,
								String fridgeExternalControlInboundPortURI, String fridgeSensorInboundPortURI,
								String fridgeActuatorInboundPortURI) throws Exception
	{
		assert	fridgeUserInboundPortURI != null &&
										!fridgeUserInboundPortURI.isEmpty() :
				new PreconditionException(
						"fridgeUserInboundPortURI != null && "
						+ "!fridgeUserInboundPortURI.isEmpty()");
		assert	fridgeInternalControlInboundPortURI != null &&
								!fridgeInternalControlInboundPortURI.isEmpty() :
				new PreconditionException(
						"fridgeInternalControlInboundPortURI != null && "
						+ "!fridgeInternalControlInboundPortURI.isEmpty()");
		assert	fridgeExternalControlInboundPortURI != null &&
								!fridgeExternalControlInboundPortURI.isEmpty() :
				new PreconditionException(
						"fridgeExternalControlInboundPortURI != null && "
						+ "!fridgeExternalControlInboundPortURI.isEmpty()");
		assert	fridgeSensorInboundPortURI != null &&
									!fridgeSensorInboundPortURI.isEmpty() :
				new PreconditionException(
						"fridgeSensorInboundPortURI != null &&"
						+ "!fridgeSensorInboundPortURI.isEmpty()");
		assert	fridgeActuatorInboundPortURI != null &&
									!fridgeActuatorInboundPortURI.isEmpty() :
				new PreconditionException(
						"fridgeActuatorInboundPortURI != null && "
						+ "!fridgeActuatorInboundPortURI.isEmpty()");

		this.currentState = FridgeState.OFF;
		this.currentCoolingPower= MAX_COOLING_POWER;
		this.targetTemperature = STANDARD_TARGET_TEMPERATURE;

		// Connections
		this.userInbound = new FridgeUserInboundPort(fridgeUserInboundPortURI, this);
		this.userInbound.publishPort();
		
		this.internalInbound = new FridgeInternalControlInboundPort(fridgeInternalControlInboundPortURI, this);
		this.internalInbound.publishPort();
		
		this.externalInbound = new FridgeExternalControlInboundPort(fridgeExternalControlInboundPortURI, this);
		this.externalInbound.publishPort();
		
		this.sensorInboundPort = new FridgeSensorDataInboundPort(fridgeSensorInboundPortURI, this);		
		this.sensorInboundPort.publishPort();
		
		this.actuatorInboundPort = new FridgeActuatorInboundPort(fridgeActuatorInboundPortURI, this);
		this.actuatorInboundPort.publishPort();

		//Create simulation architecture
		switch (this.currentSimulationType) {
			case MIL_SIMULATION:
				Architecture architecture = null;
				if (this.currentExecutionType.isUnitTest()) {
					architecture =
						LocalSimulationArchitectures.
							createFridgeMILLocalArchitecture4UnitTest(
														this.localArchitectureURI,
														this.simulationTimeUnit);
				} else {
					architecture =
							LocalSimulationArchitectures.
								createFridgeMILLocalArchitecture4IntegrationTest(
														this.localArchitectureURI,
														this.simulationTimeUnit);
				}
				assert	architecture.getRootModelURI().equals(
														this.localArchitectureURI) :
						new BCMException(
								"local simulator " + this.localArchitectureURI
								+ " does not exist!");
				this.addLocalSimulatorArchitecture(architecture);
				this.global2localSimulationArchitectureURIS.
						put(this.globalArchitectureURI, this.localArchitectureURI);
				break;
				
			case MIL_RT_SIMULATION:
				
			case SIL_SIMULATION:
				architecture = null;
				if (this.currentExecutionType.isUnitTest()) {
					architecture =
						LocalSimulationArchitectures.
							createFridge_RT_LocalArchitecture4UnitTest(
														this.currentSimulationType,
														this.localArchitectureURI,
														this.simulationTimeUnit,
														this.accFactor);
				} else {
					architecture =
						LocalSimulationArchitectures.
							createFridge_RT_LocalArchitecture4IntegrationTest(
														this.currentSimulationType,
														this.localArchitectureURI,
														this.simulationTimeUnit,
														this.accFactor);
				}
				assert	architecture.getRootModelURI().equals(
													this.localArchitectureURI) :
						new BCMException(
								"local simulator " + this.localArchitectureURI
								+ " does not exist!");
				this.addLocalSimulatorArchitecture(architecture);
				this.global2localSimulationArchitectureURIS.
					put(this.globalArchitectureURI, this.localArchitectureURI);
				break;
				
			case NO_SIMULATION:
				
			default:
		}

		if (VERBOSE) {
			this.tracer.get().setTitle("Fridge component");
			this.tracer.get().setRelativePosition(X_RELATIVE_POSITION,  Y_RELATIVE_POSITION);
			this.toggleTracing();		
		}
	}
	
	// -------------------------------------------------------------------------
	// Component life-cycle
	// -------------------------------------------------------------------------

	
	@Override
	public synchronized void start() throws ComponentStartException {
		super.start();

		// create the simulation plug-in given the current type of simulation
		// and its local architecture i.e., for the current execution
		try {
			switch (this.currentSimulationType) {
				case MIL_SIMULATION:
					this.asp = new AtomicSimulatorPlugin();
					String uri = this.global2localSimulationArchitectureURIS.
													get(this.globalArchitectureURI);
					Architecture architecture =
						(Architecture) this.localSimulationArchitectures.get(uri);
					this.asp.setPluginURI(uri);
					this.asp.setSimulationArchitecture(architecture);
					this.installPlugin(this.asp);
					break;
					
				case MIL_RT_SIMULATION:
					this.asp = new RTAtomicSimulatorPlugin();
					uri = this.global2localSimulationArchitectureURIS.
													get(this.globalArchitectureURI);
					architecture = (Architecture) this.localSimulationArchitectures.get(uri);
					((RTAtomicSimulatorPlugin)this.asp).setPluginURI(uri);
					((RTAtomicSimulatorPlugin)this.asp).
											setSimulationArchitecture(architecture);
					this.installPlugin(this.asp);
					break;
					
				case SIL_SIMULATION:
					this.asp = new RTAtomicSimulatorPlugin() {
						private static final long serialVersionUID = 1L;
						
						@Override
						public Object	getModelStateValue(
							String modelURI,
							String name
							) throws Exception
						{
							assert	modelURI.equals(FridgeTemperatureModel.SIL_URI);
							assert	name.equals(CURRENT_TEMPERATURE_NAME);
							return ((FridgeTemperatureModel)
											this.atomicSimulators.get(modelURI).
													getSimulatedModel()).
															getCurrentTemperature();
						}
					};
					uri = this.global2localSimulationArchitectureURIS.
													get(this.globalArchitectureURI);
					architecture =
							(Architecture) this.localSimulationArchitectures.get(uri);
					((RTAtomicSimulatorPlugin)this.asp).setPluginURI(uri);
					((RTAtomicSimulatorPlugin)this.asp).
											setSimulationArchitecture(architecture);
					this.installPlugin(this.asp);
					break;
					
				case NO_SIMULATION:
					
				default:
			}		
		} catch (Exception e) {
			throw new ComponentStartException(e) ;
		}		
		
		
//		super.start();
//
//		try {
//			// Registration
//			if(this.isHEMConnectionRequired) {
//				this.doPortConnection(
//						this.registrationPort.getPortURI(),
//						HEM.URI_REGISTRATION_INBOUND_PORT,
//						RegistrationConnector.class.getCanonicalName());
//			}
//		} catch (Exception e) {
//			throw new ComponentStartException(e) ;
//		}
	}
	
	@Override
	public synchronized void execute() throws Exception {
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

			this.clock.complete(clock);

			if (this.currentExecutionType.isUnitTest() &&
								this.currentSimulationType.isSILSimulation()) {
				this.asp.createSimulator();
				this.asp.setSimulationRunParameters(new HashMap<>());
				this.asp.initialiseSimulation(
								new Time(clock.getSimulatedStartTime(),
										 clock.getSimulatedTimeUnit()),
								new Duration(clock.getSimulatedDuration(),
											 clock.getSimulatedTimeUnit()));

				long simulationStartTimeInMillis = 
						TimeUnit.NANOSECONDS.toMillis(
										clock.getSimulationStartEpochNanos());
				assert	simulationStartTimeInMillis > System.currentTimeMillis() :
						new BCMException(
								"simulationStartTimeInMillis > "
								+ "System.currentTimeMillis()");
				this.asp.startRTSimulation(simulationStartTimeInMillis,
										   clock.getSimulatedStartTime(),
										   clock.getSimulatedDuration());

				clock.waitUntilSimulationEnd();

				Thread.sleep(250L);
				this.logMessage(this.asp.getFinalReport().toString());
			}
		}
		
//		if (this.isHEMConnectionRequired && TEST_REGISTRATION) 
//			this.runAllRegistrationTest();
	}
	
	@Override
	public synchronized void shutdown() throws ComponentShutdownException
	{
		try {
			this.userInbound.unpublishPort();
			this.internalInbound.unpublishPort();
			this.externalInbound.unpublishPort();
			this.sensorInboundPort.unpublishPort();
			this.actuatorInboundPort.unpublishPort();
			
			// Registration
//			if(this.isHEMConnectionRequired)
//				this.registrationPort.unpublishPort();
			
		} catch (Exception e) {
			throw new ComponentShutdownException(e) ;
		}
		super.shutdown();
	}
	
	@Override
	public synchronized void finalise() throws Exception {
		// Registration
//		if(this.isHEMConnectionRequired)
//			this.doPortDisconnection(this.registrationPort.getPortURI());
		
		super.finalise();
	}
	
	
	// -------------------------------------------------------------------------
	// Component services implementation
	// -------------------------------------------------------------------------
	
	@Override
	public double getTargetTemperature() throws Exception { 
		if (VERBOSE)
			this.traceMessage("Fridge returns its target temperature -> " + this.targetTemperature + "\n.");
		
		double ret = this.targetTemperature;
		
		assert	ret >= MIN_TEMPERATURE && ret <= MAX_TEMPERATURE :
			new PostconditionException(
					"ret >= MIN_TEMPERATURE && ret <= MAX_TEMPERATURE");
		
		return ret;
	}

	@Override
	public double getCurrentTemperature() throws Exception { 
		assert	this.getState() != FridgeState.OFF : new PreconditionException("this.getState() != FridgeState.OFF");
		
		double currentTemperature = FAKE_CURRENT_TEMPERATURE;
		
		if (this.currentSimulationType.isSILSimulation()) {
			currentTemperature = (double)((RTAtomicSimulatorPlugin)this.asp).
												getModelStateValue(
														FridgeTemperatureModel.SIL_URI,
														CURRENT_TEMPERATURE_NAME);
			if (VERBOSE)
				this.traceMessage("Fridge returns its current temperature -> " + currentTemperature + "\n.");
		}
		
		return currentTemperature;
	}

	@Override
	public double getMaxCoolingPower() throws Exception { 
		if (VERBOSE)
			this.traceMessage("Fridge returns its max cooling power -> " + MAX_COOLING_POWER + "\n.");
		
		return MAX_COOLING_POWER;
	}

	@Override
	public double getCurrentCoolingPower() throws Exception { 
		if (VERBOSE)
				this.traceMessage("Fridge returns its current cooling power -> " + this.currentCoolingPower + "\n.");
		
		return this.currentCoolingPower;
	}

	@Override
	public void setCurrentCoolingPower(double power) throws Exception { 
		if (VERBOSE)
			this.traceMessage("Trying to set current cooling power -> " + power + "\n.");
				
		assert this.currentState != FridgeState.OFF :
			new PreconditionException("Impossible to set current cooling power because the fridge is off");
		assert power >= 0 && power <= MAX_COOLING_POWER :
			new PreconditionException("The cooling power is not between 0 and " + MAX_COOLING_POWER + " -> " + power);

		this.currentCoolingPower = power;
		
		if (this.currentSimulationType.isSILSimulation()) {
			PowerValue pv = new PowerValue(this.currentCoolingPower);
			((RTAtomicSimulatorPlugin)this.asp).triggerExternalEvent(
												FridgeStateModel.SIL_URI,
												t -> new SetPowerFridge(t, pv));
		}
		
		assert	this.currentCoolingPower > this.getCurrentCoolingPower() :
					new PostconditionException(
							"this.currentCoolingPower > this.getCurrentCoolingPower()");
			
		if (VERBOSE)
			this.traceMessage("Current cooling power is changing -> " + this.currentCoolingPower + "\n.");
	}

	@Override
	public void setTargetTemperature(double temperature) throws Exception { 
		if (VERBOSE)
			this.traceMessage("Trying to set target temperature -> " + temperature + "\n.");
		
		assert temperature >= MIN_TEMPERATURE && temperature < FAKE_CURRENT_TEMPERATURE :
			new PreconditionException("The target temperature is not between " + MIN_TEMPERATURE + " and " + FAKE_CURRENT_TEMPERATURE + " -> " + temperature);
		
		this.targetTemperature = temperature;
		
		
		assert	getTargetTemperature() == this.targetTemperature:
			new PostconditionException("getTargetTemperature() == this.targetTemperature");
		
		if (VERBOSE)
			this.traceMessage("Target temperature is changing -> " + this.targetTemperature + "\n.");
	}

	@Override
	public FridgeState getState() throws Exception { 
		if (VERBOSE) 
			this.traceMessage("Fridge state -> " + this.currentState.toString() + "\n.");
		
		return this.currentState;
	}

	@Override
	public void switchOn() throws Exception { 
		this.currentState = FridgeState.ON;
		
		if (VERBOSE) 
			this.traceMessage("Fridge is turning on \n.");
		
		if(this.currentSimulationType.isSILSimulation()) 
			((RTAtomicSimulatorPlugin)this.asp).triggerExternalEvent(
												FridgeStateModel.SIL_URI, 
												t -> new SwitchOnFridge(t));	
		
		this.sensorInboundPort.send(
								new FridgeSensorData<FridgeStateMeasure>(
										new FridgeStateMeasure(this.currentState)));
		
		assert this.getState() == FridgeState.ON : new PostconditionException("this.getState() == FridgeState.ON");
	}

	@Override
	public void switchOff() throws Exception { 
		this.currentState = FridgeState.OFF;
		
		if (VERBOSE) 
			this.traceMessage("Fridge is turning off \n.");
		
		if(this.currentSimulationType.isSILSimulation()) 
			((RTAtomicSimulatorPlugin)this.asp).triggerExternalEvent(
												FridgeStateModel.SIL_URI, 
												t -> new SwitchOffFridge(t));	
		
		this.sensorInboundPort.send(
								new FridgeSensorData<FridgeStateMeasure>(
										new FridgeStateMeasure(this.currentState)));
		
		assert this.getState() == FridgeState.OFF : new PostconditionException("this.getState() == FridgeState.OFF");
	}

	@Override
	public boolean isCooling() throws Exception {	
		if (VERBOSE) {
			if(this.currentState == FridgeState.COOLING)
				this.traceMessage("Fridge is cooling \n.");
			else
				this.traceMessage("Fridge is not coolong \n.");
		}
			
		if(this.currentState == FridgeState.COOLING)
			return true;
		return false;
	}

	@Override
	public void startCooling() throws Exception { 
		if(VERBOSE)
			this.traceMessage("Trying to start cooling \n.");
		
		assert this.currentState != FridgeState.OFF :
			new PreconditionException("Impossible to start cooling because the fridge is off");
		
		this.currentState = FridgeState.COOLING;
		
		if (this.currentSimulationType.isSILSimulation()) 
			((RTAtomicSimulatorPlugin)this.asp).triggerExternalEvent(
												FridgeStateModel.SIL_URI,
												t -> new CoolFridge(t));
		

		this.sensorInboundPort.send(new FridgeSensorData<FridgeStateMeasure>(
										new FridgeStateMeasure(this.currentState)));

		assert	this.isCooling() : new PostconditionException("this.isCooling()");
		
		if(VERBOSE)
			this.traceMessage("The fridge is cooling \n.");
	}

	@Override
	public void stopCooling() throws Exception { 
		if(VERBOSE)
			this.traceMessage("Trying to stop cooling \n.");
		
		assert this.currentState != FridgeState.OFF :
			new PreconditionException("Impossible to stop cooling because the fridge is off");
		
		this.currentState = FridgeState.ON;
		
		if (this.currentSimulationType.isSILSimulation()) 
			((RTAtomicSimulatorPlugin)this.asp).triggerExternalEvent(
												FridgeStateModel.SIL_URI,
												t -> new DoNotCoolFridge(t));
		

		this.sensorInboundPort.send(new FridgeSensorData<FridgeStateMeasure>(
										new FridgeStateMeasure(this.currentState)));

		assert	!this.isCooling() : new PostconditionException("!this.isCooling()");
		
		if(VERBOSE)
			this.traceMessage("The fridge stop cooling \n.");
	}
	
	public boolean isDoorOpen() throws Exception {
		return this.getState() == FridgeState.DOOR_OPEN;
	}
	
	@Override
	public void closeDoor() throws Exception {
		System.out.println("le frigo ferme la porte");
		if(VERBOSE)
			this.traceMessage("Close the door \n.");
		
		this.currentState = FridgeState.ON;
		
		if (this.currentSimulationType.isSILSimulation()) 
				((RTAtomicSimulatorPlugin)this.asp).triggerExternalEvent(
													FridgeStateModel.SIL_URI,
													t -> new CloseDoorFridge(t));
		
		this.sensorInboundPort.send(new FridgeSensorData<FridgeStateMeasure>(
										new FridgeStateMeasure(this.currentState)));
		
		assert this.getState() != FridgeState.DOOR_OPEN :
			new PostconditionException("this.getState() != FridgeState.DOOR_OPEN");
	}
	
	@Override
	public void openDoor() throws Exception {
		if(VERBOSE)
			this.traceMessage("Open the door \n.");
		
		System.out.println("porte ouverte");
		this.currentState = FridgeState.DOOR_OPEN;
		
		if (this.currentSimulationType.isSILSimulation()) 
				((RTAtomicSimulatorPlugin)this.asp).triggerExternalEvent(
													FridgeStateModel.SIL_URI,
													t -> new OpenDoorFridge(t));
		
		this.sensorInboundPort.send(new FridgeSensorData<FridgeStateMeasure>(
										new FridgeStateMeasure(this.currentState)));
		
		assert this.getState() == FridgeState.DOOR_OPEN :
			new PostconditionException("this.getState() == FridgeState.DOOR_OPEN");
	}
	
	
	// -------------------------------------------------------------------------
	// Component Tests Registration
	// -------------------------------------------------------------------------
	
	protected void testRegistered() {
		if(VERBOSE)
			this.traceMessage("Test registered\n");
		try {
			assertFalse(this.registrationPort.registered(URI));
		}
		catch(Exception e) {
			this.traceMessage("...KO.\n" + e);
			assertTrue(false);
		}
		if(VERBOSE)
			this.traceMessage("Done...\n");
	}
	
	protected void testRegister() {
		if(VERBOSE)
			this.traceMessage("Test register\n");
		try {
			assertTrue(this.registrationPort.register(URI, this.externalInbound.getPortURI(), XML_PATH));
			assertTrue(this.registrationPort.registered(URI));
		}
		catch(Exception e) {
			this.traceMessage("...KO.\n" + e);
			assertTrue(false);
		}
		if(VERBOSE) 
			this.traceMessage("Done...\n");
	}
	
	protected void testUnregister() {
		if(VERBOSE)
			this.traceMessage("Test unregister\n");
		try {
			this.registrationPort.unregister(URI);
			assertFalse(this.registrationPort.registered(URI));
		}
		catch(Exception e) {
			this.traceMessage("...KO.\n" + e);
			assertTrue(false);
		}
		if(VERBOSE)
			this.traceMessage("Done...\n");
	}
	
	protected void runAllRegistrationTest() throws Exception {
		// Switch on to make the tests
		this.switchOn();
		
		this.testRegistered();
		this.testRegister();
		this.testUnregister();
	}
	
	
	// -------------------------------------------------------------------------
	// Component sensors
	// -------------------------------------------------------------------------
	
	public FridgeSensorData<Measure<Boolean>> coolingPullSensor() throws Exception {
		return new FridgeSensorData<Measure<Boolean>>(
										new Measure<Boolean>(this.isCooling()));
	}
	
	public FridgeSensorData<Measure<Double>> targetTemperaturePullSensor() throws Exception {
		return new FridgeSensorData<Measure<Double>>(
				new Measure<Double>(this.getTargetTemperature(),
									MeasurementUnit.CELSIUS));
	}

	public FridgeSensorData<Measure<Double>> currentTemperaturePullSensor() throws Exception {
		return new FridgeSensorData<Measure<Double>>(
				new Measure<Double>(this.getCurrentTemperature(),
									MeasurementUnit.CELSIUS));
	}

	public FridgeSensorData<Measure<Boolean>> doorStatePullSensor() throws Exception {
		return new FridgeSensorData<Measure<Boolean>>(
				new Measure<Boolean>(this.isDoorOpen()));
	}

	public void startTemperaturesPushSensor(long controlPeriod, TimeUnit tu) throws Exception {
		long actualControlPeriod = -1L;
		if (this.currentExecutionType.isStandard()) 
			actualControlPeriod = (long)(controlPeriod * tu.toNanos(1));
		else {
			AcceleratedClock ac = this.clock.get();
			actualControlPeriod = (long)((controlPeriod * tu.toNanos(1))/
											ac.getAccelerationFactor());
			if (actualControlPeriod < TimeUnit.MILLISECONDS.toNanos(10)) {
				System.out.println(
					"Warning: accelerated control period is "
							+ "too small ("
							+ actualControlPeriod +
							"), unexpected scheduling problems may"
							+ " occur!");
			}
		}
		this.temperaturesPushSensorTask(actualControlPeriod);
	}
	
	protected void temperaturesPushSensorTask (long actualControlPeriod) throws Exception {
		assert	actualControlPeriod > 0 :
				new PreconditionException("actualControlPeriod > 0");

		if (this.currentState != FridgeState.OFF) {
			this.traceMessage("Fridge performs a new temperatures push.\n");
			this.temperaturesPushSensor();
			if (this.currentExecutionType.isStandard()
					|| this.currentSimulationType.isSILSimulation()
					|| this.currentSimulationType.isHILSimulation()) {
				this.scheduleTaskOnComponent(
					new AbstractComponent.AbstractTask() {
						@Override
						public void run() {
							try {
								temperaturesPushSensorTask(actualControlPeriod);
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
					},
					actualControlPeriod,
					TimeUnit.NANOSECONDS);
			}
		}
	}
	
	protected void temperaturesPushSensor() throws Exception {
		this.sensorInboundPort.send(
					new FridgeSensorData<FridgeCompoundMeasure>(
						new FridgeCompoundMeasure(
							this.targetTemperaturePullSensor().getMeasure(),
							this.currentTemperaturePullSensor().getMeasure())));
	}
}
