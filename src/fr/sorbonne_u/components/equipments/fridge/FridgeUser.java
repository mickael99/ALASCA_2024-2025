package fr.sorbonne_u.components.equipments.fridge;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertFalse;

import fr.sorbonne_u.components.AbstractComponent;
import fr.sorbonne_u.components.annotations.RequiredInterfaces;
import fr.sorbonne_u.components.equipments.fridge.connections.FridgeExternalControlConnector;
import fr.sorbonne_u.components.equipments.fridge.connections.FridgeExternalControlOutboundPort;
import fr.sorbonne_u.components.equipments.fridge.connections.FridgeInternalControlConnector;
import fr.sorbonne_u.components.equipments.fridge.connections.FridgeInternalControlOutboundPort;
import fr.sorbonne_u.components.equipments.fridge.connections.FridgeUserConnector;
import fr.sorbonne_u.components.equipments.fridge.connections.FridgeUserOutboundPort;
import fr.sorbonne_u.components.equipments.fridge.interfaces.FridgeExternalControlCI;
import fr.sorbonne_u.components.equipments.fridge.interfaces.FridgeInternalControlCI;
import fr.sorbonne_u.components.equipments.fridge.interfaces.FridgeUserCI;
import fr.sorbonne_u.components.equipments.fridge.mil.FridgeElectricityModel.FridgeState;
import fr.sorbonne_u.components.equipments.fridge.mil.events.CloseDoorFridge;
import fr.sorbonne_u.components.equipments.fridge.mil.events.CoolFridge;
import fr.sorbonne_u.components.equipments.fridge.mil.events.DoNotCoolFridge;
import fr.sorbonne_u.components.equipments.fridge.mil.events.OpenDoorFridge;
import fr.sorbonne_u.components.equipments.fridge.mil.events.SetPowerFridge;
import fr.sorbonne_u.components.equipments.fridge.mil.events.SwitchOffFridge;
import fr.sorbonne_u.components.equipments.fridge.mil.events.SwitchOnFridge;
import fr.sorbonne_u.components.equipments.fridge.mil.LocalSimulationArchitectures;
import fr.sorbonne_u.components.exceptions.BCMException;
import fr.sorbonne_u.components.exceptions.ComponentShutdownException;
import fr.sorbonne_u.components.exceptions.ComponentStartException;
import fr.sorbonne_u.components.utils.ExecutionType;
import fr.sorbonne_u.components.utils.SimulationType;
import fr.sorbonne_u.devs_simulation.architectures.Architecture;
import fr.sorbonne_u.devs_simulation.models.annotations.ModelExternalEvents;
import fr.sorbonne_u.exceptions.ImplementationInvariantException;
import fr.sorbonne_u.exceptions.InvariantChecking;
import fr.sorbonne_u.exceptions.InvariantException;
import fr.sorbonne_u.exceptions.PreconditionException;
import fr.sorbonne_u.utils.aclocks.AcceleratedClock;
import fr.sorbonne_u.utils.aclocks.ClocksServer;
import fr.sorbonne_u.components.cyphy.AbstractCyPhyComponent;
import fr.sorbonne_u.components.cyphy.plugins.devs.AtomicSimulatorPlugin;
import fr.sorbonne_u.components.cyphy.plugins.devs.RTAtomicSimulatorPlugin;
import fr.sorbonne_u.components.cyphy.utils.aclocks.AcceleratedAndSimulationClock;
import fr.sorbonne_u.components.cyphy.utils.aclocks.ClocksServerWithSimulationCI;
import fr.sorbonne_u.components.cyphy.utils.aclocks.ClocksServerWithSimulationConnector;
import fr.sorbonne_u.components.cyphy.utils.aclocks.ClocksServerWithSimulationOutboundPort;

@RequiredInterfaces(required = {FridgeExternalControlCI.class, 
								FridgeInternalControlCI.class, 
								FridgeUserCI.class,
								ClocksServerWithSimulationCI.class})
@ModelExternalEvents(imported = {SwitchOnFridge.class,
		 SwitchOffFridge.class,
		 CoolFridge.class,
		 DoNotCoolFridge.class,
		 SetPowerFridge.class,
		 OpenDoorFridge.class,
		 CloseDoorFridge.class})
public class FridgeUser extends AbstractCyPhyComponent {
	
	// -------------------------------------------------------------------------
	// Constants and variables
	// -------------------------------------------------------------------------
	
	public static final String	REFLECTION_INBOUND_PORT_URI = "FridgeUser-RIP-URI";
	
	// Tracing
	public static boolean VERBOSE = true;
	public static int X_RELATIVE_POSITION = 1;
	public static int Y_RELATIVE_POSITION = 0;
	
	// Unit test
	protected boolean isUnitTest = true;
	
	// Inbound ports URI
	protected String userInboundPortURI;
	protected String internalInboundPortURI;
	protected String externalInboundPortURI;
	
	// Outbound ports
	protected FridgeUserOutboundPort userOutboundPort;
	protected FridgeInternalControlOutboundPort internalOutboundPort;
	protected FridgeExternalControlOutboundPort externalOutboundPort;
	
	// Execution/Simulation
	protected final ExecutionType currentExecutionType;
	protected final SimulationType currentSimulationType;
	protected AtomicSimulatorPlugin asp;
	protected final String globalArchitectureURI;
	protected final String localArchitectureURI;
	protected final TimeUnit simulationTimeUnit;
	protected double accFactor;
	protected final String clockURI;
	
	
	// -------------------------------------------------------------------------
	// Invariants
	// -------------------------------------------------------------------------

	protected static boolean glassBoxInvariants(FridgeUser instance) {
		assert	instance != null :
				new PreconditionException("instance != null");

		boolean ret = true;
		ret &= InvariantChecking.checkGlassBoxInvariant(
					instance.userInboundPortURI != null &&
								!instance.userInboundPortURI.isEmpty(),
					FridgeUser.class, instance,
					"userInboundPortURI != null && "
					+ "!userInboundPortURI.isEmpty()");
		ret &= InvariantChecking.checkGlassBoxInvariant(
					instance.internalInboundPortURI != null &&
						!instance.internalInboundPortURI.isEmpty(),
					FridgeUser.class, instance,
					"internalInboundPortURI != null && "
					+ "!internalInboundPortURI.isEmpty()");
		ret &= InvariantChecking.checkGlassBoxInvariant(
					instance.externalInboundPortURI != null &&
						!instance.externalInboundPortURI.isEmpty(),
					FridgeUser.class, instance,
					"externalInboundPortURI != null && "
					+ "!externalInboundPortURI.isEmpty()");
		ret &= InvariantChecking.checkGlassBoxInvariant(
					instance.currentExecutionType != null,
					FridgeUser.class, instance,
					"currentExecutionType != null");
		ret &= InvariantChecking.checkGlassBoxInvariant(
					instance.currentSimulationType != null,
					FridgeUser.class, instance,
					"hcurrentSimulationType != null");
		ret &= InvariantChecking.checkGlassBoxInvariant(
					!instance.currentExecutionType.isStandard() ||
							instance.currentSimulationType.isNoSimulation(),
					FridgeUser.class, instance,
					"!currentExecutionType.isStandard() || "
					+ "currentSimulationType.isNoSimulation()");
		ret &= InvariantChecking.checkGlassBoxInvariant(
					instance.currentSimulationType.isNoSimulation() ||
						(instance.globalArchitectureURI != null &&
							!instance.globalArchitectureURI.isEmpty()),
					FridgeUser.class, instance,
					"currentSimulationType.isNoSimulation() || "
					+ "(globalArchitectureURI != null && "
					+ "!globalArchitectureURI.isEmpty())");
		ret &= InvariantChecking.checkGlassBoxInvariant(
					instance.currentSimulationType.isNoSimulation() ||
						(instance.localArchitectureURI != null &&
							!instance.localArchitectureURI.isEmpty()),
					FridgeUser.class, instance,
					"currentSimulationType.isNoSimulation() || "
					+ "(localArchitectureURI != null && "
					+ "!localArchitectureURI.isEmpty())");
		ret &= InvariantChecking.checkGlassBoxInvariant(
					!instance.currentSimulationType.isSimulated() ||
										instance.simulationTimeUnit != null,
					FridgeUser.class, instance,
					"!currentSimulationType.isSimulated() || "
					+ "simulationTimeUnit != null");
		ret &= InvariantChecking.checkGlassBoxInvariant(
					!instance.currentSimulationType.isRealTimeSimulation() ||
													instance.accFactor > 0.0,
					FridgeUser.class, instance,
					"!currentSimulationType.isRealTimeSimulation() || "
					+ "accFactor > 0.0");
		return ret;
	}

	protected static boolean blackBoxInvariants(FridgeUser instance) {
		assert	instance != null :
				new PreconditionException("instance != null");

		boolean ret = true;
		ret &= InvariantChecking.checkBlackBoxInvariant(
					X_RELATIVE_POSITION >= 0,
					FridgeUser.class, instance,
					"X_RELATIVE_POSITION >= 0");
		ret &= InvariantChecking.checkBlackBoxInvariant(
					Y_RELATIVE_POSITION >= 0,
					FridgeUser.class, instance,
					"Y_RELATIVE_POSITION >= 0");
		return ret;
	}
		
	// -------------------------------------------------------------------------
	// Constructors
	// -------------------------------------------------------------------------
	
	protected FridgeUser(
			String fridgeUserInboundPortURI,
			String fridgeInternalControlInboundPortURI,
			String fridgeExternalControlInboundPortURI,
			ExecutionType currentExecutionType,
			SimulationType currentSimulationType,
			String globalArchitectureURI,
			String localArchitectureURI,
			TimeUnit simulationTimeUnit,
			double accFactor,
			String clockURI
			) throws Exception
		{
			super(REFLECTION_INBOUND_PORT_URI, 1, 1);

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

			this.initialise(fridgeUserInboundPortURI,
							fridgeInternalControlInboundPortURI,
							fridgeExternalControlInboundPortURI);
		}

	protected void initialise(String fridgeUserInboundPortURI, String fridgeInternalControlInboundPortURI, String fridgeExternalControlInboundPortURI) throws Exception {
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
		
		// Connections
		this.userInboundPortURI = fridgeUserInboundPortURI;
		this.userOutboundPort = new FridgeUserOutboundPort(this);
		this.userOutboundPort.publishPort();
		
		this.internalInboundPortURI = fridgeInternalControlInboundPortURI;
		
		this.externalInboundPortURI = fridgeExternalControlInboundPortURI;
		
		if (this.currentExecutionType.isUnitTest()) {
			this.internalOutboundPort = new FridgeInternalControlOutboundPort(this);
			this.internalOutboundPort.publishPort();
			
			this.externalOutboundPort = new FridgeExternalControlOutboundPort(this);
			this.externalOutboundPort.publishPort();
		}
		switch (this.currentSimulationType) {
			case MIL_SIMULATION:
				Architecture architecture = 
							LocalSimulationArchitectures.
									createFridgeUserMILLocalArchitecture(
														this.localArchitectureURI,
														this.simulationTimeUnit);
				assert	architecture.getRootModelURI().equals(
														this.localArchitectureURI) :
						new BCMException(
								"local simulator " + this.localArchitectureURI
								+ " does not exist!");
				this.logMessage("Architecture built");
				this.addLocalSimulatorArchitecture(architecture);
				this.global2localSimulationArchitectureURIS.
						put(this.globalArchitectureURI, this.localArchitectureURI);
				break;
				
			case MIL_RT_SIMULATION:
				architecture = LocalSimulationArchitectures.
									createFridgeUserMILRT_LocalArchitecture(
														this.localArchitectureURI,
														this.simulationTimeUnit,
														this.accFactor);
				assert	architecture.getRootModelURI().equals(
														this.localArchitectureURI) :
						new BCMException(
								"local simulator " + this.localArchitectureURI
								+ " does not exist!");
				this.addLocalSimulatorArchitecture(architecture);
				this.global2localSimulationArchitectureURIS.
						put(this.globalArchitectureURI, this.localArchitectureURI);
				break;
				
			case SIL_SIMULATION:

			case NO_SIMULATION:
			default:
		}

		// Tracing
		if(VERBOSE) {
			this.tracer.get().setTitle("Fridge user component");
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
			if(this.currentExecutionType.isUnitTest()) {
				this.doPortConnection(
						this.internalOutboundPort.getPortURI(),
						this.internalInboundPortURI,
						FridgeInternalControlConnector.class.getCanonicalName());
				this.doPortConnection(
						this.externalOutboundPort.getPortURI(),
						this.externalInboundPortURI,
						FridgeExternalControlConnector.class.getCanonicalName());
			}
			this.doPortConnection(
					this.userOutboundPort.getPortURI(),
					this.userInboundPortURI,
					FridgeUserConnector.class.getCanonicalName());
			
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
					this.logMessage("Plugin installed");
					break;
					
				case MIL_RT_SIMULATION:
					this.asp = new RTAtomicSimulatorPlugin();
					uri = this.global2localSimulationArchitectureURIS.
													get(this.globalArchitectureURI);
					architecture =
						(Architecture) this.localSimulationArchitectures.get(uri);
					((RTAtomicSimulatorPlugin)this.asp).setPluginURI(uri);
					((RTAtomicSimulatorPlugin)this.asp).
											setSimulationArchitecture(architecture);
					this.installPlugin(this.asp);
					break;
					
				case SIL_SIMULATION:
					
				case NO_SIMULATION:
					
				default:
			}
		} catch (Exception e) {
			throw new ComponentStartException(e) ;
		}
	}

	@Override
	public synchronized void execute() throws Exception {
		
		if (!this.currentExecutionType.isStandard() &&
				!this.currentSimulationType.isMilSimulation() &&
				!this.currentSimulationType.isMILRTSimulation()) {
			ClocksServerWithSimulationOutboundPort clocksServerOutboundPort =
							new ClocksServerWithSimulationOutboundPort(this);
			clocksServerOutboundPort.publishPort();
			this.doPortConnection(
					clocksServerOutboundPort.getPortURI(),
					ClocksServer.STANDARD_INBOUNDPORT_URI,
					ClocksServerWithSimulationConnector.class.getCanonicalName());
			this.logMessage("Fridge tester gets the clock.");
			AcceleratedAndSimulationClock clock =
				clocksServerOutboundPort.getClockWithSimulation(this.clockURI);
			this.doPortDisconnection(clocksServerOutboundPort.getPortURI());
			clocksServerOutboundPort.unpublishPort();
			clocksServerOutboundPort.destroyPort();
			
			this.logMessage("Fridge user waits until start.");
			clock.waitUntilStart();
			
			if (this.currentSimulationType.isNoSimulation()) {
				if (this.currentExecutionType.isUnitTest()) {
					this.runUnitTestsNoSimulation(clock);
				} else {
					this.runIntegrationTestNoSimulation(clock);
				}
			} else {
				assert	this.currentExecutionType.isTest() :
						new BCMException("currentExecutionType.isTest()");
				assert	this.currentSimulationType.isSILSimulation() :
						new BCMException(
								"currentSimulationType.isSILSimulation()");
			
				clock.waitUntilSimulationStart();
				runSILTestScenario(clock);
			}
		}
		
		
//		this.runAllTest();
//		System.out.println("Fridge Tester ends");
	}

	@Override
	public synchronized void finalise() throws Exception {
		if (this.currentExecutionType.isUnitTest()) {
			this.doPortDisconnection(this.externalOutboundPort.getPortURI());
			this.doPortDisconnection(this.internalOutboundPort.getPortURI());
		}
		this.doPortDisconnection(this.userOutboundPort.getPortURI());
		
		super.finalise();
	}

	@Override
	public synchronized void shutdown() throws ComponentShutdownException {
		try {
			this.userOutboundPort.unpublishPort();
			if (this.currentExecutionType.isUnitTest()) {
				this.externalOutboundPort.unpublishPort();
				this.internalOutboundPort.unpublishPort();
			}
		} catch (Exception e) {
			throw new ComponentShutdownException(e) ;
		}
		super.shutdown();
	}
	
	
	// -------------------------------------------------------------------------
	// Component services implementation
	// -------------------------------------------------------------------------
	
	
	protected void testGetState() { 
		this.traceMessage("testGetState...\n");
		
		try {
			assertEquals(this.userOutboundPort.getState(), FridgeState.OFF);
		} catch (Exception e) {
			this.traceMessage("...KO.\n" + e);
			assertTrue(false);
		}
		
		this.traceMessage("testGetState done \n");
	}
	
	protected void testGetTargetTemperature() { 
		this.traceMessage("testGetTargetTemperature...\n");
		
		try {
			assertEquals(Fridge.STANDARD_TARGET_TEMPERATURE, this.userOutboundPort.getTargetTemperature());
			assertEquals(Fridge.STANDARD_TARGET_TEMPERATURE, this.internalOutboundPort.getTargetTemperature());
		} catch (Exception e) {
			this.traceMessage("...KO.\n" + e);
			assertTrue(false);
		}
		
		this.traceMessage("testGetTargetTemperature done \n");
	}
	
	protected void testGetMaxCoolingPower() { 
		this.traceMessage("testGetMaxCoolingPower...\n");
		
		try {
			assertEquals(Fridge.MAX_COOLING_POWER, this.userOutboundPort.getMaxCoolingPower());
			assertEquals(Fridge.MAX_COOLING_POWER, this.externalOutboundPort.getMaxCoolingPower());
		} catch (Exception e) {
			this.traceMessage("...KO.\n" + e);
			assertTrue(false);
		}
		
		this.traceMessage("testGetMaxCoolingPower done \n");
	}
	
	protected void testGetCurrentCoolingPower() { 
		this.traceMessage("testGetCurrentCoolingPower...\n");
		
		try {
			this.externalOutboundPort.setCurrentCoolingPower(200.0);
			assertEquals(200.0, this.userOutboundPort.getCurrentCoolingPower());
			assertEquals(200.0, this.externalOutboundPort.getCurrentCoolingPower());
		} catch (Exception e) {
			this.traceMessage("...KO.\n" + e);
			assertTrue(false);
		}
		
		this.traceMessage("testGetCurrentCoolingPower done \n");
	}
	
	protected void testIsCooling() { 
		this.traceMessage("testIsCooling...\n");
		
		try {
			assertFalse(this.internalOutboundPort.isCooling());
		} catch (Exception e) {
			this.traceMessage("...KO.\n" + e);
			assertTrue(false);
		}
		
		this.traceMessage("testIsCooling done \n");	
	}
	
	protected void testSwitchOn() { 
		this.traceMessage("testSwitchOn...\n");
		
		try {
			this.userOutboundPort.switchOn();
			assertEquals(this.userOutboundPort.getState(), FridgeState.ON);
		} catch (Exception e) {
			this.traceMessage("...KO.\n" + e);
			assertTrue(false);
		}
		
		this.traceMessage("testSwitchOn done \n");
	}
	
	protected void testSwitchOff() { 
		this.traceMessage("testSwitchOff...\n");
		
		try {
			this.userOutboundPort.switchOff();
			assertEquals(this.userOutboundPort.getState(), FridgeState.OFF);
		} catch (Exception e) {
			this.traceMessage("...KO.\n" + e);
			assertTrue(false);
		}
		
		this.traceMessage("testSwitchOff done \n");
	}
	
	protected void testSetCurrentCoolingPower() { 
		this.traceMessage("testSetCurrentCoolingPower...\n");
		
		try {
			this.userOutboundPort.setCurrentCoolingPower(300.0);
			assertEquals(300.0, this.userOutboundPort.getCurrentCoolingPower());
			
			this.externalOutboundPort.setCurrentCoolingPower(200.0);
			assertEquals(200.0, this.externalOutboundPort.getCurrentCoolingPower());
		} catch (Exception e) {
			this.traceMessage("...KO.\n" + e);
			assertTrue(false);
		}
		
		this.traceMessage("testSetCurrentCoolingPower done \n");
	}
	
	protected void testSetTargetTemperature() { 
		this.traceMessage("testSetTargetTemperature...\n");
		
		try {
			this.userOutboundPort.setTargetTemperature(3.0);
			assertEquals(3.0, this.userOutboundPort.getTargetTemperature());
			
			this.externalOutboundPort.setTargetTemperature(2.0);
			assertEquals(2.0, this.userOutboundPort.getTargetTemperature());
		} catch (Exception e) {
			this.traceMessage("...KO.\n" + e);
			assertTrue(false);
		}
		
		this.traceMessage("testSetTargetTemperature done \n");
	}
	
	protected void testStartCooling() { 
		this.traceMessage("testStartCooling...\n");
		
		try {
			this.internalOutboundPort.startCooling();
			assertEquals(FridgeState.COOLING, this.userOutboundPort.getState());
		} catch (Exception e) {
			this.traceMessage("...KO.\n" + e);
			assertTrue(false);
		}
		
		this.traceMessage("testStartCooling done \n");
	}
	
	protected void testStopCooling() { 
		this.traceMessage("testStopCooling...\n");
		
		try {
			this.internalOutboundPort.stopCooling();
			assertEquals(FridgeState.ON, this.userOutboundPort.getState());
		} catch (Exception e) {
			this.traceMessage("...KO.\n" + e);
			assertTrue(false);
		}
		
		this.traceMessage("testStopCooling done \n");	
	}
	
	protected void testOpenDoorCloseDoor() {
		this.traceMessage("testOpenDoorCloseDoor...\n");
		
		try {
			assertFalse(this.userOutboundPort.getState() == FridgeState.DOOR_OPEN);
			this.userOutboundPort.openDoor();
			assertTrue(this.userOutboundPort.getState() == FridgeState.DOOR_OPEN);
			this.internalOutboundPort.closeDoor();
			assertFalse(this.userOutboundPort.getState() == FridgeState.DOOR_OPEN);
			assertTrue(FridgeState.ON == this.userOutboundPort.getState());
		} catch (Exception e) {
			this.traceMessage("...KO.\n" + e);
			assertTrue(false);
		}
		
		this.traceMessage("testOpenDoorCloseDoor done \n");
	}
	
	protected void testSwitchoff() {
		this.traceMessage("testSwitchoff...\n");
		
		try {
			this.userOutboundPort.switchOff();
			assertEquals(FridgeState.OFF, this.userOutboundPort.getState());
		} catch (Exception e) {
			this.traceMessage("...KO.\n" + e);
			assertTrue(false);
		}
		
		this.traceMessage("testSwitchoff done \n");
	}
	
	protected void runAllTests() {
		testGetState();
		testGetTargetTemperature(); 
		testGetMaxCoolingPower();		
		testGetCurrentCoolingPower();
		testIsCooling();
		testSwitchOn();
		testSetCurrentCoolingPower();
		testSetTargetTemperature();
		testStartCooling();
		testStopCooling();
		testOpenDoorCloseDoor();
		testSwitchoff();
		
		this.traceMessage("Tests end...");
	}
	
	protected void runUnitTestsNoSimulation(AcceleratedClock clock) {
		assert	clock != null : new PreconditionException("clock != null");
		assert	!clock.startTimeNotReached() :
				new PreconditionException("!clock.startTimeNotReached()");

		Instant startInstant = clock.getStartInstant();
		Instant startTests = startInstant.plusSeconds(60L);
		long delayToTestsStart = clock.nanoDelayUntilInstant(startTests);
		this.scheduleTaskOnComponent(
				new AbstractComponent.AbstractTask() {
					@Override
					public void run() {
						try {
							logMessage("Fridge tester starts the tests.");
							runAllTests();
							logMessage("Fridge tester tests end.");
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}, delayToTestsStart, TimeUnit.NANOSECONDS);
	}
	
	protected void runIntegrationTestNoSimulation(AcceleratedClock clock) throws Exception {
		assert	clock != null : new PreconditionException("clock != null");
		assert	!clock.startTimeNotReached() :
				new PreconditionException("!clock.startTimeNotReached()");
		
		this.userOutboundPort.switchOn();
	}
	
	protected void runSILTestScenario(AcceleratedAndSimulationClock clock) {
		assert	clock != null : new PreconditionException("clock != null");
		assert	!clock.startTimeNotReached() :
				new PreconditionException("!clock.startTimeNotReached()");

		Instant simulationStartInstant = clock.getSimulationStartInstant();
		Instant currentInstant = clock.currentInstant();

		Instant switchOnInstant = simulationStartInstant.plusSeconds(600L);
		assert	switchOnInstant.isAfter(currentInstant) :
				new BCMException("switchOnInstant.isAfter(currentInstant)");

		Instant switchOffInstant =
						clock.getSimulationEndInstant().minusSeconds(600L);

		long delayToSwitchOn = clock.nanoDelayUntilInstant(switchOnInstant);
		FridgeUserOutboundPort o = this.userOutboundPort;
		this.scheduleTaskOnComponent(
				new AbstractComponent.AbstractTask() {
					@Override
					public void run() {
						try {
							traceMessage(
								"Fridge user switches the fridge on\n.");
							o.switchOn();
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}, delayToSwitchOn, TimeUnit.NANOSECONDS);
		long delayToSwitchOff = clock.nanoDelayUntilInstant(switchOffInstant);
		this.scheduleTaskOnComponent(
				new AbstractComponent.AbstractTask() {
					@Override
					public void run() {
						try {
							traceMessage(
									"Fridge user switches the fridge off\n.");
							o.switchOff();
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}, delayToSwitchOff, TimeUnit.NANOSECONDS);
	}
}
