package fr.sorbonne_u.components.equipments.fridge;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashMap;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import fr.sorbonne_u.components.annotations.OfferedInterfaces;
import fr.sorbonne_u.components.annotations.RequiredInterfaces;
import fr.sorbonne_u.components.equipments.fridge.connections.FridgeExternalControlInboundPort;
import fr.sorbonne_u.components.equipments.fridge.connections.FridgeInternalControlInboundPort;
import fr.sorbonne_u.components.equipments.fridge.connections.FridgeUserInboundPort;
import fr.sorbonne_u.components.equipments.fridge.interfaces.FridgeInternalControlI;
import fr.sorbonne_u.components.equipments.fridge.interfaces.FridgeUserI;
import fr.sorbonne_u.components.equipments.fridge.measures.FridgeSensorData;
import fr.sorbonne_u.components.equipments.fridge.mil.FridgeElectricityModel.FridgeState;
import fr.sorbonne_u.components.equipments.fridge.mil.FridgeTemperatureModel;
import fr.sorbonne_u.components.equipments.fridge.mil.LocalSimulationArchitectures;
import fr.sorbonne_u.components.equipments.hem.registration.RegistrationOutboundPort;
import fr.sorbonne_u.components.equipments.fridge.interfaces.*;
import fr.sorbonne_u.components.exceptions.BCMException;
import fr.sorbonne_u.components.exceptions.ComponentShutdownException;
import fr.sorbonne_u.components.exceptions.ComponentStartException;
import fr.sorbonne_u.components.interfaces.DataOfferedCI;
import fr.sorbonne_u.components.utils.ExecutionType;
import fr.sorbonne_u.components.utils.Measure;
import fr.sorbonne_u.exceptions.InvariantChecking;
import fr.sorbonne_u.exceptions.InvariantException;
import fr.sorbonne_u.exceptions.PreconditionException;
import fr.sorbonne_u.utils.aclocks.AcceleratedClock;
import fr.sorbonne_u.utils.aclocks.ClocksServer;
import fr.sorbonne_u.components.equipments.hem.registration.RegistrationCI;
import fr.sorbonne_u.components.equipments.fridge.sil.FridgeActuatorCI;
import fr.sorbonne_u.components.equipments.fridge.sil.FridgeSensorDataCI;
import fr.sorbonne_u.components.equipments.fridge.sil.connectors.FridgeActuatorInboundPort;
import fr.sorbonne_u.components.equipments.fridge.sil.connectors.FridgeSensorDataInboundPort;
import fr.sorbonne_u.components.cyphy.AbstractCyPhyComponent;
import fr.sorbonne_u.components.cyphy.plugins.devs.AtomicSimulatorPlugin;
import fr.sorbonne_u.components.cyphy.plugins.devs.RTAtomicSimulatorPlugin;
import fr.sorbonne_u.components.cyphy.utils.aclocks.AcceleratedAndSimulationClock;
import fr.sorbonne_u.components.cyphy.utils.aclocks.ClocksServerWithSimulationCI;
import fr.sorbonne_u.components.cyphy.utils.aclocks.ClocksServerWithSimulationConnector;
import fr.sorbonne_u.components.cyphy.utils.aclocks.ClocksServerWithSimulationOutboundPort;
import fr.sorbonne_u.components.utils.MeasurementUnit;
import fr.sorbonne_u.components.utils.SimulationType;
import fr.sorbonne_u.devs_simulation.architectures.Architecture;
import fr.sorbonne_u.devs_simulation.models.time.Duration;
import fr.sorbonne_u.devs_simulation.models.time.Time;

@OfferedInterfaces(offered = {FridgeExternalControlCI.class, FridgeInternalControlCI.class, FridgeUserCI.class,
								FridgeSensorDataCI.FridgeSensorOfferedPullCI.class, FridgeActuatorCI.class})
@RequiredInterfaces(required = {RegistrationCI.class, DataOfferedCI.PushCI.class, ClocksServerWithSimulationCI.class})
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
	public static final boolean VERBOSE = true;
	public static int X_RELATIVE_POSITION = 0;			
	public static int Y_RELATIVE_POSITION = 0;
	
	// State
	protected FridgeState currentState;
	
	// Power level
	protected static final double MAX_COOLING_POWER = 500.0;
	protected double currentCoolingPower;
	
	// Temperature
	public static final MeasurementUnit	TEMPERATURE_UNIT = MeasurementUnit.CELSIUS;
	protected static final double MIN_TEMPERATURE = 0.0;
	protected static final double MAX_TEMPERATURE = 8.0;
	protected static final double FAKE_CURRENT_TEMPERATURE = 6.0;
	protected static final double STANDARD_TARGET_TEMPERATURE = 4.0;
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
	
	/*
	protected Fridge(boolean isHEMConnectionRequired) throws Exception {
		this(USER_INBOUND_PORT_URI, INTERNAL_CONTROL_INBOUND_PORT_URI, EXTERNAL_CONTROL_INBOUND_PORT_URI, isHEMConnectionRequired);
	}
	
	protected Fridge(String userInboundURI, String internalInboundURI, String externalInboundURI, boolean isHEMConnectionRequired) throws Exception {
		super(2, 0);
		this.initialise(userInboundURI, internalInboundURI, externalInboundURI, isHEMConnectionRequired);
	}
	
	protected Fridge(String reflectionInboundPortURI, String userInboundURI, String internalInboundURI, String externalInboundURI, boolean isHEMConnectionRequired) throws Exception {
		super(reflectionInboundPortURI, 1, 0);
		this.initialise(userInboundURI, internalInboundURI, externalInboundURI, isHEMConnectionRequired);
	}*/
	
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
	/*
	protected void initialise(String userInboundURI, String internalInboundURI, String externalInboundURI, boolean isHEMConnectionRequired) throws Exception {
		assert userInboundURI != null && !userInboundURI.isEmpty();
		assert internalInboundURI != null && !internalInboundURI.isEmpty();
		assert externalInboundURI != null && !externalInboundURI.isEmpty();
		
		// Variables
		this.currentState = FridgeState.OFF;
		this.currentCoolingPower = MAX_COOLING_POWER;
		this.targetTemperature = STANDARD_TARGET_TEMPERATURE;
		
		// Connections
		this.userInbound = new FridgeUserInboundPort(userInboundURI, this);
		this.userInbound.publishPort();
		
		this.internalInbound = new FridgeInternalControlInboundPort(internalInboundURI, this);
		this.internalInbound.publishPort();
		
		this.externalInbound = new FridgeExternalControlInboundPort(externalInboundURI, this);
		this.externalInbound.publishPort();
		
		// Registration
		this.isHEMConnectionRequired = isHEMConnectionRequired;
		
		if(this.isHEMConnectionRequired) {
			this.registrationPort = new RegistrationOutboundPort(this);
			this.registrationPort.publishPort();
		}
		
		// Tracing
		if (VERBOSE) {
			this.tracer.get().setTitle("Fridge component");
			this.tracer.get().setRelativePosition(X_RELATIVE_POSITION,
												  Y_RELATIVE_POSITION);
			this.toggleTracing();		
		}	
	}
	*/
	
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
		
		return this.targetTemperature;
	}

	@Override
	public double getCurrentTemperature() throws Exception { 
		if (VERBOSE)
			this.traceMessage("Fridge returns its current temperature -> " + FAKE_CURRENT_TEMPERATURE + "\n.");
		
		return FAKE_CURRENT_TEMPERATURE;
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
		
		if (VERBOSE)
			this.traceMessage("Current cooling power is changing -> " + this.currentCoolingPower + "\n.");
	}

	@Override
	public void setTargetTemperature(double temperature) throws Exception { 
		if (VERBOSE)
			this.traceMessage("Trying to set target temperature -> " + temperature + "\n.");
		
		assert this.currentState != FridgeState.OFF :
			new PreconditionException("Impossible to set target temperature because the fridge is off");
		assert temperature >= MIN_TEMPERATURE && temperature < FAKE_CURRENT_TEMPERATURE :
			new PreconditionException("The target temperature is not between " + MIN_TEMPERATURE + " and " + FAKE_CURRENT_TEMPERATURE + " -> " + temperature);
		
		this.targetTemperature = temperature;
		
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
	}

	@Override
	public void switchOff() throws Exception { 
		this.currentState = FridgeState.OFF;
		
		if (VERBOSE) 
			this.traceMessage("Fridge is turning off \n.");
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
		
		if(VERBOSE)
			this.traceMessage("The fridge stop cooling \n.");
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

	@Override
	public void closeDoor() throws Exception {
		// TODO Auto-generated method stub
		
	}
	
	public FridgeSensorData<Measure<Boolean>> coolingPullSensor() throws Exception {
		return null;
	}
	
	public FridgeSensorData<Measure<Double>> targetTemperaturePullSensor() throws Exception {
		return null;
	}

	public FridgeSensorData<Measure<Double>> currentTemperaturePullSensor() throws Exception {
		return null;
	}

	public FridgeSensorData<Measure<Boolean>> doorStatePullSensor() throws Exception {
		return null;
	}

	public void startTemperaturesPushSensor(long controlPeriod, TimeUnit tu) throws Exception {
		
	}
}
