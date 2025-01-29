package fr.sorbonne_u.components.equipments.windTurbine;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertFalse;

import fr.sorbonne_u.components.AbstractComponent;
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
import fr.sorbonne_u.components.equipments.windTurbine.mil.LocalSimulationArchitectures;
import fr.sorbonne_u.components.equipments.windTurbine.mil.events.*;

@RequiredInterfaces(required = {WindTurbineCI.class, ClocksServerWithSimulationCI.class})
@ModelExternalEvents(imported = {
		 StartWindTurbineEvent.class,
		 StopWindTurbineEvent.class})
public class WindTurbineTester extends AbstractCyPhyComponent {
	
	// -------------------------------------------------------------------------
	// Constants and variables
	// -------------------------------------------------------------------------
	
	public static final String REFLECTION_INBOUND_PORT_URI = "WIND_TURBINE_TESTER_RIP_URI";
	
	public static boolean VERBOSE = true;
	public static int X_RELATIVE_POSITION = 1;
	public static int Y_RELATIVE_POSITION = 1;
	
	protected String inboundPortURI;
	protected WindTurbineOutboundPort outboundPort;
	
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

	protected static boolean glassBoxInvariants(WindTurbineTester instance) {
		assert	instance != null :
				new PreconditionException("instance != null");

		boolean ret = true;
		ret &= InvariantChecking.checkGlassBoxInvariant(
					instance.currentExecutionType != null,
					WindTurbineTester.class, instance,
					"currentExecutionType != null");
		ret &= InvariantChecking.checkGlassBoxInvariant(
					instance.currentSimulationType != null,
					WindTurbineTester.class, instance,
					"currentSimulationType != null");
		ret &= InvariantChecking.checkGlassBoxInvariant(
					!instance.currentExecutionType.isStandard() ||
							instance.currentSimulationType.isNoSimulation(),
					WindTurbineTester.class, instance,
					"!currentExecutionType.isStandard() || "
					+ "currentSimulationType.isNoSimulation()");
		ret &= InvariantChecking.checkGlassBoxInvariant(
					instance.currentSimulationType.isNoSimulation() ||
						(instance.globalArchitectureURI != null &&
							!instance.globalArchitectureURI.isEmpty()),
					WindTurbineTester.class, instance,
					"currentSimulationType.isNoSimulation() || "
					+ "(globalArchitectureURI != null && "
					+ "!globalArchitectureURI.isEmpty())");
		ret &= InvariantChecking.checkGlassBoxInvariant(
					instance.currentSimulationType.isNoSimulation() ||
						(instance.localArchitectureURI != null &&
							!instance.localArchitectureURI.isEmpty()),
					WindTurbineTester.class, instance,
					"currentSimulationType.isNoSimulation() || "
					+ "(localArchitectureURI != null && "
					+ "!localArchitectureURI.isEmpty())");
		ret &= InvariantChecking.checkGlassBoxInvariant(
					!instance.currentSimulationType.isSimulated() ||
										instance.simulationTimeUnit != null,
					WindTurbineTester.class, instance,
					"!currentSimulationType.isSimulated() || "
					+ "simulationTimeUnit != null");
		ret &= InvariantChecking.checkGlassBoxInvariant(
					!instance.currentSimulationType.isRealTimeSimulation() ||
													instance.accFactor > 0.0,
					WindTurbineTester.class, instance,
					"!currentSimulationType.isRealTimeSimulation() || "
					+ "accFactor > 0.0");
		return ret;
	}

	protected static boolean blackBoxInvariants(WindTurbineTester instance) {
		assert	instance != null :
				new PreconditionException("instance != null");

		boolean ret = true;
		ret &= InvariantChecking.checkBlackBoxInvariant(
					X_RELATIVE_POSITION >= 0,
					WindTurbineTester.class, instance,
					"X_RELATIVE_POSITION >= 0");
		ret &= InvariantChecking.checkBlackBoxInvariant(
					Y_RELATIVE_POSITION >= 0,
					WindTurbineTester.class, instance,
					"Y_RELATIVE_POSITION >= 0");
		return ret;
	}
	
	// -------------------------------------------------------------------------
	// Constructors
	// -------------------------------------------------------------------------
	
	protected WindTurbineTester(
		String windTurbineInboundPortURI,
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

		this.initialise(windTurbineInboundPortURI);
	}
	
	protected void initialise(String windTurbineInboundPortURI) throws Exception {
		assert	windTurbineInboundPortURI != null &&
				!windTurbineInboundPortURI.isEmpty() :
					new PreconditionException(
						"windTurbineInboundPortURI != null && "
						+ "!windTurbineInboundPortURI.isEmpty()");
		
		// Connections
		this.inboundPortURI = windTurbineInboundPortURI;
		
		this.outboundPort = new WindTurbineOutboundPort(this);
		this.outboundPort.publishPort();
		
		// Simualtion
		switch (this.currentSimulationType) {
			case MIL_SIMULATION:
				Architecture architecture = 
							LocalSimulationArchitectures.
									createWindTurbineUserMILLocalArchitecture(
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
									createWindTurbineUserMILRT_LocalArchitecture(
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
			this.tracer.get().setTitle("Wind turbine tester component");
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
							WindTurbineConnector.class.getCanonicalName());
			
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
			this.logMessage("Wind turbine tester gets the clock.");
			AcceleratedAndSimulationClock clock =
				clocksServerOutboundPort.getClockWithSimulation(this.clockURI);
			this.doPortDisconnection(clocksServerOutboundPort.getPortURI());
			clocksServerOutboundPort.unpublishPort();
			clocksServerOutboundPort.destroyPort();
			
			this.logMessage("Wind turbine tester waits until start.");
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
				System.out.println("run test sil scenario a verifier dans wind turbine lors du test d'integration");
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
	
	protected void testIsActivate() {
		if(VERBOSE)
			this.traceMessage("testIsActivate() \n");
		
		try {
			assertFalse(this.outboundPort.isActivate());
		} catch(Exception e) {
			assertTrue(false);
		}
		
		if(VERBOSE)
			this.traceMessage("done... \n");
	}
	
	protected void testActivate() {
		if(VERBOSE)
			this.traceMessage("testActivate() \n");
		
		try {
			this.outboundPort.activate();
			assertTrue(this.outboundPort.isActivate());
		} catch(Exception e) {
			assertTrue(false);
		}

		
		if(VERBOSE)
			this.traceMessage("done... \n");
	}
	
	protected void testStop() {
		if(VERBOSE)
			this.traceMessage("testStop() \n");
		
		try {
			this.outboundPort.stop();
			assertFalse(this.outboundPort.isActivate());
		} catch(Exception e) {
			assertTrue(false);
		}
		
		if(VERBOSE)
			this.traceMessage("done... \n");
	}
	
	protected void runAllTests() {		
		this.testIsActivate();
		this.testActivate();
		this.testStop();
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
							logMessage("Wind turbine tester starts the tests.");
							runAllTests();
							logMessage("wind turbine tests end.");
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
		
		this.outboundPort.activate();
	}
	
	protected void runSILTestScenario(AcceleratedAndSimulationClock clock) {
		assert	clock != null : new PreconditionException("clock != null");
		assert	!clock.startTimeNotReached() :
				new PreconditionException("!clock.startTimeNotReached()");

		Instant simulationStartInstant = clock.getSimulationStartInstant();
		Instant currentInstant = clock.currentInstant();

		
		
		Instant start1Instant = simulationStartInstant.plusSeconds(10L);
		assert	start1Instant.isAfter(currentInstant) :
				new BCMException("start1Instant.isAfter(currentInstant)");

		Instant stopInstant = simulationStartInstant.plusSeconds(4000L);
		assert	stopInstant.isAfter(start1Instant) :
				new BCMException("stopInstant.isAfter(start1Instant)");
		
		Instant start2Instant = simulationStartInstant.plusSeconds(4500L);
		assert	start2Instant.isAfter(stopInstant) :
				new BCMException("start2Instant.isAfter(stopInstant)");
		
		
		long delayTostart1 = clock.nanoDelayUntilInstant(start1Instant);
		WindTurbineOutboundPort o = this.outboundPort;
		this.scheduleTaskOnComponent(
				new AbstractComponent.AbstractTask() {
					@Override
					public void run() {
						try {
							traceMessage("Wind turbine starts\n.");
							o.activate();
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}, delayTostart1, TimeUnit.NANOSECONDS);
		
		long delayToStop = clock.nanoDelayUntilInstant(stopInstant);
		this.scheduleTaskOnComponent(
				new AbstractComponent.AbstractTask() {
					@Override
					public void run() {
						try {
							traceMessage("Wind turbine stops\n.");
							o.stop();
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}, delayToStop, TimeUnit.NANOSECONDS);
		
		long delayTostart2 = clock.nanoDelayUntilInstant(start2Instant);
		this.scheduleTaskOnComponent(
				new AbstractComponent.AbstractTask() {
					@Override
					public void run() {
						try {
							traceMessage("Wind turbine starts\n.");
							o.activate();
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}, delayTostart2, TimeUnit.NANOSECONDS);
	}
}
