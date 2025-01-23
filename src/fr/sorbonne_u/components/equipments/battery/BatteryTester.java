package fr.sorbonne_u.components.equipments.battery;

import fr.sorbonne_u.components.AbstractComponent;
import fr.sorbonne_u.components.annotations.RequiredInterfaces;
import fr.sorbonne_u.components.cyphy.AbstractCyPhyComponent;
import fr.sorbonne_u.components.cyphy.plugins.devs.AtomicSimulatorPlugin;
import fr.sorbonne_u.components.cyphy.plugins.devs.RTAtomicSimulatorPlugin;
import fr.sorbonne_u.components.cyphy.utils.aclocks.AcceleratedAndSimulationClock;
import fr.sorbonne_u.components.cyphy.utils.aclocks.ClocksServerWithSimulationCI;
import fr.sorbonne_u.components.cyphy.utils.aclocks.ClocksServerWithSimulationConnector;
import fr.sorbonne_u.components.cyphy.utils.aclocks.ClocksServerWithSimulationOutboundPort;
import fr.sorbonne_u.components.equipments.battery.BatteryI.BATTERY_STATE;
import fr.sorbonne_u.components.equipments.battery.mil.LocalSimulationArchitectures;
import fr.sorbonne_u.components.equipments.battery.mil.events.*;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;
import java.util.concurrent.TimeUnit;

@RequiredInterfaces(required = {BatteryCI.class, ClocksServerWithSimulationCI.class})
@ModelExternalEvents(imported = {
					 SetStandByBatteryEvent.class,
					 SetConsumeBatteryEvent.class,
					 SetProductBatteryEvent.class})
public class BatteryTester extends AbstractCyPhyComponent {
	
	public static boolean VERBOSE = true;
	public static int X_RELATIVE_POSITION = 1;
	public static int Y_RELATIVE_POSITION = 1;
	
	public static final String REFLECTION_INBOUND_PORT_URI = "BATTERY-TESTER-RIP-URI";
	
	protected BatteryOutboundPort outboundPort;
	protected String inboundPortURI;
	
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

	protected static boolean glassBoxInvariants(BatteryTester instance) {
		assert	instance != null :
				new PreconditionException("instance != null");

		boolean ret = true;
		ret &= InvariantChecking.checkGlassBoxInvariant(
					instance.currentExecutionType != null,
					BatteryTester.class, instance,
					"currentExecutionType != null");
		ret &= InvariantChecking.checkGlassBoxInvariant(
					instance.currentSimulationType != null,
					BatteryTester.class, instance,
					"currentSimulationType != null");
		ret &= InvariantChecking.checkGlassBoxInvariant(
					!instance.currentExecutionType.isStandard() ||
							instance.currentSimulationType.isNoSimulation(),
					BatteryTester.class, instance,
					"!currentExecutionType.isStandard() || "
					+ "currentSimulationType.isNoSimulation()");
		ret &= InvariantChecking.checkGlassBoxInvariant(
					instance.currentSimulationType.isNoSimulation() ||
						(instance.globalArchitectureURI != null &&
							!instance.globalArchitectureURI.isEmpty()),
					BatteryTester.class, instance,
					"currentSimulationType.isNoSimulation() || "
					+ "(globalArchitectureURI != null && "
					+ "!globalArchitectureURI.isEmpty())");
		ret &= InvariantChecking.checkGlassBoxInvariant(
					instance.currentSimulationType.isNoSimulation() ||
						(instance.localArchitectureURI != null &&
							!instance.localArchitectureURI.isEmpty()),
					BatteryTester.class, instance,
					"currentSimulationType.isNoSimulation() || "
					+ "(localArchitectureURI != null && "
					+ "!localArchitectureURI.isEmpty())");
		ret &= InvariantChecking.checkGlassBoxInvariant(
					!instance.currentSimulationType.isSimulated() ||
										instance.simulationTimeUnit != null,
					BatteryTester.class, instance,
					"!currentSimulationType.isSimulated() || "
					+ "simulationTimeUnit != null");
		ret &= InvariantChecking.checkGlassBoxInvariant(
					!instance.currentSimulationType.isRealTimeSimulation() ||
													instance.accFactor > 0.0,
					BatteryTester.class, instance,
					"!currentSimulationType.isRealTimeSimulation() || "
					+ "accFactor > 0.0");
		return ret;
	}

	protected static boolean blackBoxInvariants(BatteryTester instance) {
		assert	instance != null :
				new PreconditionException("instance != null");

		boolean ret = true;
		ret &= InvariantChecking.checkBlackBoxInvariant(
					X_RELATIVE_POSITION >= 0,
					BatteryTester.class, instance,
					"X_RELATIVE_POSITION >= 0");
		ret &= InvariantChecking.checkBlackBoxInvariant(
					Y_RELATIVE_POSITION >= 0,
					BatteryTester.class, instance,
					"Y_RELATIVE_POSITION >= 0");
		return ret;
	}
	
	// -------------------------------------------------------------------------
	// Constructors
	// -------------------------------------------------------------------------
	
	protected BatteryTester(
		String batteryInboundPortURI,
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

		this.initialise(batteryInboundPortURI);
	}

	protected void initialise(String batteryInboundPortURI) throws Exception {
		assert	batteryInboundPortURI != null &&
				!batteryInboundPortURI.isEmpty() :
					new PreconditionException(
						"batteryInboundPortURI != null && "
						+ "!batteryInboundPortURI.isEmpty()");
		
		// Connections
		this.inboundPortURI = batteryInboundPortURI;
		
		this.outboundPort = new BatteryOutboundPort(this);
		this.outboundPort.publishPort();
		
		// Simualtion
		switch (this.currentSimulationType) {
			case MIL_SIMULATION:
				Architecture architecture = 
							LocalSimulationArchitectures.
									createBatteryUserMILLocalArchitecture(
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
									createBatteryUserMILRT_LocalArchitecture(
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
			this.tracer.get().setTitle("Battery tester component");
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
							this.outboundPort.getPortURI(),
							this.inboundPortURI,
							BatteryConnector.class.getCanonicalName());
			
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
			this.logMessage("Battery tester gets the clock.");
			AcceleratedAndSimulationClock clock =
				clocksServerOutboundPort.getClockWithSimulation(this.clockURI);
			this.doPortDisconnection(clocksServerOutboundPort.getPortURI());
			clocksServerOutboundPort.unpublishPort();
			clocksServerOutboundPort.destroyPort();
			
			this.logMessage("Battery tester waits until start.");
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
	}
	
	@Override
	public synchronized void finalise() throws Exception {
		this.doPortDisconnection(this.outboundPort.getPortURI());
		super.finalise();
	}

	@Override
	public synchronized void	shutdown() throws ComponentShutdownException {
		try {
			this.outboundPort.unpublishPort();
		} catch (Exception e) {
			throw new ComponentShutdownException(e) ;
		}
		super.shutdown();
	}
	
	
	// -------------------------------------------------------------------------
	// Component services implementation
	// -------------------------------------------------------------------------
	
	protected void testGetState() throws Exception {
		if(VERBOSE)
			this.traceMessage("testGetState() \n");
		try {
			assertEquals(BATTERY_STATE.STANDBY, this.outboundPort.getState());
		} catch(Exception e) {
			assertTrue(false);
		}
		if(VERBOSE)
			this.traceMessage("done... \n");
	}
	
	protected void testSetState() throws Exception {
		if(VERBOSE)
			this.traceMessage("testSetState() \n");
		
		try {
			System.out.println(this.outboundPort.getState().toString());
			this.outboundPort.setState(BATTERY_STATE.PRODUCT);
			System.out.println(this.outboundPort.getState().toString());
			assertEquals(BATTERY_STATE.PRODUCT, this.outboundPort.getState());
		} catch(Exception e) {
			assertTrue(false);
		}
		
		if(VERBOSE)
			this.traceMessage("done... \n");
	}
	
	protected void testGetBatteryLevel() throws Exception {
		if(VERBOSE)
			this.traceMessage("testGetPowerLevel() \n");
		
		this.outboundPort.getBatteryLevel();
		
		if(VERBOSE)
			this.traceMessage("done... \n");
	}
	
	protected void runAllTests() throws Exception {
		if(VERBOSE)
			this.traceMessage("Tests start... \n");
		
		this.testGetState();
		this.testSetState();
		this.testGetBatteryLevel();
		
		if(VERBOSE)
			this.traceMessage("Battery tests end... \n");
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
							logMessage("Battery tester starts the tests.");
							runAllTests();
							logMessage("Battery tester tests end.");
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
		
		// The battery is always turned off
	}
	
	protected void runSILTestScenario(AcceleratedAndSimulationClock clock) {
		assert	clock != null : new PreconditionException("clock != null");
		assert	!clock.startTimeNotReached() :
				new PreconditionException("!clock.startTimeNotReached()");

		Instant simulationStartInstant = clock.getSimulationStartInstant();
		Instant currentInstant = clock.currentInstant();

		
		
		Instant consumeInstant = simulationStartInstant.plusSeconds(3600L);
		assert	consumeInstant.isAfter(currentInstant) :
				new BCMException("consumeInstant.isAfter(currentInstant)");

		Instant productInstant = simulationStartInstant.plusSeconds(7200L);
		assert	productInstant.isAfter(consumeInstant) :
				new BCMException("productInstant.isAfter(consumeInstant)");
		
		Instant standByInstant = simulationStartInstant.plusSeconds(10080L);
		assert	standByInstant.isAfter(productInstant) :
			new BCMException("standByInstant.isAfter(productInstant)");
		
		
		
		long delayToconsume = clock.nanoDelayUntilInstant(consumeInstant);
		BatteryOutboundPort o = this.outboundPort;
		this.scheduleTaskOnComponent(
				new AbstractComponent.AbstractTask() {
					@Override
					public void run() {
						try {
							traceMessage("Battery tester set consume mode\n.");
							o.setState(BATTERY_STATE.CONSUME);
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}, delayToconsume, TimeUnit.NANOSECONDS);
		
		long delayToProduct = clock.nanoDelayUntilInstant(productInstant);
		this.scheduleTaskOnComponent(
				new AbstractComponent.AbstractTask() {
					@Override
					public void run() {
						try {
							traceMessage("Battery tester set product mode\n.");
							o.setState(BATTERY_STATE.PRODUCT);
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}, delayToProduct, TimeUnit.NANOSECONDS);
		
		long delayToStandBy = clock.nanoDelayUntilInstant(standByInstant);
		this.scheduleTaskOnComponent(
				new AbstractComponent.AbstractTask() {
					@Override
					public void run() {
						try {
							traceMessage("Battery tester set stand by mode\n.");
							o.setState(BATTERY_STATE.STANDBY);
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}, delayToStandBy, TimeUnit.NANOSECONDS);
	}
}
