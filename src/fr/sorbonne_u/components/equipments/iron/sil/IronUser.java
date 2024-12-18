package fr.sorbonne_u.components.equipments.iron.sil;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;
import java.util.concurrent.TimeUnit;

import fr.sorbonne_u.components.annotations.RequiredInterfaces;
import fr.sorbonne_u.components.cyphy.AbstractCyPhyComponent;
import fr.sorbonne_u.components.cyphy.plugins.devs.AtomicSimulatorPlugin;
import fr.sorbonne_u.components.cyphy.plugins.devs.RTAtomicSimulatorPlugin;
import fr.sorbonne_u.components.cyphy.utils.aclocks.AcceleratedAndSimulationClock;
import fr.sorbonne_u.components.cyphy.utils.aclocks.ClocksServerWithSimulationCI;
import fr.sorbonne_u.components.cyphy.utils.aclocks.ClocksServerWithSimulationConnector;
import fr.sorbonne_u.components.cyphy.utils.aclocks.ClocksServerWithSimulationOutboundPort;
import fr.sorbonne_u.components.equipments.iron.Iron;
import fr.sorbonne_u.components.equipments.iron.IronConnector;
import fr.sorbonne_u.components.equipments.iron.IronOutboundPort;
import fr.sorbonne_u.components.equipments.iron.IronUserCI;
import fr.sorbonne_u.components.equipments.iron.IronImplementationI.IronEnergySavingMode;
import fr.sorbonne_u.components.equipments.iron.IronImplementationI.IronState;
import fr.sorbonne_u.components.equipments.iron.IronImplementationI.IronSteam;
import fr.sorbonne_u.components.equipments.iron.IronImplementationI.IronTemperature;
import fr.sorbonne_u.components.equipments.iron.mil.IronOperationI;
import fr.sorbonne_u.components.equipments.iron.mil.LocalSimulationArchitectures;
import fr.sorbonne_u.components.exceptions.BCMException;
import fr.sorbonne_u.components.exceptions.ComponentShutdownException;
import fr.sorbonne_u.components.exceptions.ComponentStartException;
import fr.sorbonne_u.components.utils.ExecutionType;
import fr.sorbonne_u.components.utils.SimulationType;
import fr.sorbonne_u.devs_simulation.architectures.Architecture;
import fr.sorbonne_u.devs_simulation.utils.InvariantChecking;
import fr.sorbonne_u.exceptions.ImplementationInvariantException;
import fr.sorbonne_u.exceptions.InvariantException;
import fr.sorbonne_u.exceptions.PreconditionException;
import fr.sorbonne_u.utils.aclocks.ClocksServer;

@RequiredInterfaces(required = {IronUserCI.class, ClocksServerWithSimulationCI.class})
public class IronUser extends AbstractCyPhyComponent implements IronOperationI {

	// -------------------------------------------------------------------------
	// Constants and variables
	// -------------------------------------------------------------------------

	public static boolean VERBOSE = false ;
	public static int X_RELATIVE_POSITION = 0;
	public static int Y_RELATIVE_POSITION = 0;
													
	public static final String REFLECTION_INBOUND_PORT_URI = "IRON-USER-RIP-URI";
	
	protected IronOutboundPort outboundPort;

	protected String inboundPortURI;

	// Execution/Simulation

	protected final ExecutionType currentExecutionType;
	protected final SimulationType currentSimulationType;
												
	protected final String clockURI;

	protected final String globalArchitectureURI;
	protected final String localArchitectureURI;
	protected final TimeUnit simulationTimeUnit;
	protected double accFactor;
	
	
	// -------------------------------------------------------------------------
	// Invariants
	// -------------------------------------------------------------------------

	protected static boolean glassBoxInvariants(IronUser ironUser) {
		assert 	ironUser != null : new PreconditionException("ironUser != null");

		boolean ret = true;
		ret &= InvariantChecking.checkGlassBoxInvariant(
					REFLECTION_INBOUND_PORT_URI != null &&
									!REFLECTION_INBOUND_PORT_URI.isEmpty(),
					IronUser.class, ironUser,
					"REFLECTION_INBOUND_PORT_URI != null && "
								+ "!REFLECTION_INBOUND_PORT_URI.isEmpty()");
		ret &= InvariantChecking.checkGlassBoxInvariant(
					X_RELATIVE_POSITION >= 0,
							IronUser.class, ironUser,
					"X_RELATIVE_POSITION >= 0");
		ret &= InvariantChecking.checkGlassBoxInvariant(
					Y_RELATIVE_POSITION >= 0,
							IronUser.class, ironUser,
					"Y_RELATIVE_POSITION >= 0");		
		ret &= InvariantChecking.checkGlassBoxInvariant(
					ironUser.currentExecutionType != null,
					IronUser.class, ironUser,
					"currentExecutionType != null");
		ret &= InvariantChecking.checkGlassBoxInvariant(
					ironUser.currentSimulationType != null,
							IronUser.class, ironUser,
					"hcurrentSimulationType != null");
		ret &= InvariantChecking.checkGlassBoxInvariant(
					!ironUser.currentExecutionType.isStandard() ||
								ironUser.currentSimulationType.isNoSimulation(),
					IronUser.class, ironUser,
					"!currentExecutionType.isStandard() || "
					+ "currentSimulationType.isNoSimulation()");
		ret &= InvariantChecking.checkGlassBoxInvariant(
					ironUser.currentSimulationType.isNoSimulation() ||
							(ironUser.globalArchitectureURI != null &&
										!ironUser.globalArchitectureURI.isEmpty()),
					IronUser.class, ironUser,
					"currentSimulationType.isNoSimulation() || "
					+ "(globalArchitectureURI != null && "
					+ "!globalArchitectureURI.isEmpty())");
		ret &= InvariantChecking.checkGlassBoxInvariant(
					ironUser.currentSimulationType.isNoSimulation() ||
							(ironUser.localArchitectureURI != null &&
										!ironUser.localArchitectureURI.isEmpty()),
					IronUser.class, ironUser,
					"currentSimulationType.isNoSimulation() || "
					+ "(localArchitectureURI != null && "
					+ "!localArchitectureURI.isEmpty())");
		ret &= InvariantChecking.checkGlassBoxInvariant(
					!ironUser.currentSimulationType.isSILSimulation() ||
														ironUser.accFactor > 0.0,
					IronUser.class, ironUser,
					"!ironUser.currentSimulationType.isSILSimulation() || "
					+ "ironUser.accFactor > 0.0");
		return ret;
	}
	
	protected static boolean blackBoxInvariants(IronUser ironUser) {
		assert 	ironUser != null : new PreconditionException("ironUser != null");

		boolean ret = true;
		return ret;
	}
		
	
	// -------------------------------------------------------------------------
	// Constructors
	// -------------------------------------------------------------------------

	protected IronUser() throws Exception {
		this(Iron.INBOUND_PORT_URI);
	}

	protected IronUser(String ironInboundPortURI)  throws Exception {
		this(ironInboundPortURI, ExecutionType.STANDARD);
	}

	protected IronUser(String ironInboundPortURI, ExecutionType currentExecutionType) throws Exception {
		this(REFLECTION_INBOUND_PORT_URI, ironInboundPortURI,
			 currentExecutionType, SimulationType.NO_SIMULATION,
			 null, null, null, 0.0, null);

		assert	currentExecutionType.isTest() :
				new PreconditionException("currentExecutionType.isTest()");
	}
	
	protected IronUser(String reflectionInboundPortURI, String ironInboundPortURI, ExecutionType currentExecutionType,
						SimulationType currentSimulationType, String globalArchitectureURI,
						String localArchitectureURI, TimeUnit simulationTimeUnit, double accFactor,
						String clockURI) throws Exception
	{
		super(reflectionInboundPortURI, 1, 1);

		assert ironInboundPortURI != null &&
										!ironInboundPortURI.isEmpty() :
				new PreconditionException(
						"ironInboundPortURI != null && "
						+ "!ironInboundPortURI.isEmpty()");
		assert	currentExecutionType != null :
				new PreconditionException("currentExecutionType != null");
		assert	currentExecutionType.isStandard() ||
								clockURI != null && !clockURI.isEmpty() :
				new PreconditionException(
						"currentExecutionType.isStandard() || "
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
		assert	!currentSimulationType.isSILSimulation() || accFactor > 0.0 :
				new PreconditionException(
						"!currentSimulationType.isSILSimulation() || "
						+ "accFactor > 0.0");

		if (currentExecutionType.isStandard()) {
			throw new RuntimeException(
					"IronUser: standard execution is not implemented.");
		}

		this.currentExecutionType = currentExecutionType;
		this.currentSimulationType = currentSimulationType;
		this.globalArchitectureURI = globalArchitectureURI;
		this.localArchitectureURI = localArchitectureURI;
		this.simulationTimeUnit = simulationTimeUnit;
		this.accFactor = accFactor;
		this.clockURI = clockURI;

		this.initialise(ironInboundPortURI);

		assert	IronUser.glassBoxInvariants(this) :
				new ImplementationInvariantException(
						"IronUser.glassBoxInvariants(this)");
		assert	IronUser.blackBoxInvariants(this) :
				new InvariantException("IronUser.blackBoxInvariants(this)");
	}
	
	protected void initialise(String ironInboundPortURI) throws Exception {
		this.inboundPortURI = ironInboundPortURI;
		
		this.outboundPort = new IronOutboundPort(this);
		this.outboundPort.publishPort();

		switch (this.currentSimulationType) {
			case MIL_SIMULATION:
				Architecture architecture =
						LocalSimulationArchitectures.
							createIronUserMIL_Architecture(
												this.localArchitectureURI,
												this.simulationTimeUnit);
				assert architecture.getRootModelURI().
											equals(this.localArchitectureURI) :
						new AssertionError(
								"local simulation architecture "
								+ this.localArchitectureURI
								+ " does not exist!");
				this.addLocalSimulatorArchitecture(architecture);
				this.global2localSimulationArchitectureURIS.
						put(this.globalArchitectureURI, this.localArchitectureURI);
				break;
				
			case MIL_RT_SIMULATION:
				architecture =
						LocalSimulationArchitectures.
							createIronUserMIL_RT_Architecture(
												this.localArchitectureURI,
												this.simulationTimeUnit,
												this.accFactor);
				assert architecture.getRootModelURI().equals(
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

		if (VERBOSE) {
			this.tracer.get().setTitle("Iron user component");
			this.tracer.get().setRelativePosition(X_RELATIVE_POSITION,
												  Y_RELATIVE_POSITION);
			this.toggleTracing();
		}
	}
	
	// -------------------------------------------------------------------------
	// Component internal testing method triggered by the SIL simulator
	// -------------------------------------------------------------------------
	
	@Override
	public void turnOn() throws Exception {
		if (VERBOSE) 
			this.logMessage("IronUser#turnOn().");
		try {
			this.outboundPort.turnOn();
		} catch (Exception e) {
			throw new RuntimeException(e) ;
		}
	}
	
	@Override
	public void turnOff() throws Exception {
		if (VERBOSE) 
			this.logMessage("IronUser#turnOff().");
		try {
			this.outboundPort.turnOff();
		} catch (Exception e) {
			throw new RuntimeException(e) ;
		}
	}
	
	@Override
	public void setTemperature(IronTemperature t) throws Exception {
		if (VERBOSE) 
			this.logMessage("IronUser#setTemperature(" + t.toString() +").");
		try {
			this.outboundPort.setTemperature(t);
		} catch (Exception e) {
			throw new RuntimeException(e) ;
		}
	}

	@Override
	public void enableSteamMode() throws Exception{
		if (VERBOSE) 
			this.logMessage("IronUser#enableSteamMode().");
		try {
			this.outboundPort.setSteam(IronSteam.ACTIVE);
		} catch (Exception e) {
			throw new RuntimeException(e) ;
		}
	}

	@Override
	public void disableSteamMode() throws Exception {
		if (VERBOSE) 
			this.logMessage("IronUser#disableSteamMode().");
		try {
			this.outboundPort.setSteam(IronSteam.INACTIVE);
		} catch (Exception e) {
			throw new RuntimeException(e) ;
		}
	}

	@Override
	public void enableEnergySavingMode() throws Exception {
		if (VERBOSE) 
			this.logMessage("IronUser#enableEnergySavingMode().");
		try {
			this.outboundPort.setEnergySavingMode(IronEnergySavingMode.ACTIVE);
		} catch (Exception e) {
			throw new RuntimeException(e) ;
		}
	}

	@Override
	public void disableEnergySavingMode() throws Exception {
		if (VERBOSE) 
			this.logMessage("IronUser#disableEnergySavingMode().");
		try {
			this.outboundPort.setEnergySavingMode(IronEnergySavingMode.INACTIVE);
		} catch (Exception e) {
			throw new RuntimeException(e) ;
		}
	}
	
	
	// -------------------------------------------------------------------------
	// Component internal tests
	// -------------------------------------------------------------------------
	
	public void	testGetState(){
		this.logMessage("testGetState()... ");
		try {
			assertEquals(IronState.OFF, this.outboundPort.getState());
		} catch (Exception e) {
			this.logMessage("...KO.");
			assertTrue(false);
		}
		this.logMessage("...done.");
	}
	
	public void	testTurnOn() {
		this.logMessage("testTurnOn()... ");
		try {
			this.outboundPort.turnOn();
			assertEquals(IronState.ON, this.outboundPort.getState());
		} catch (Exception e) {
			assertTrue(false);
		}
		this.logMessage("...done.");
	}
	
	public void	testTurnOff() {
		this.logMessage("testTurnOff()... ");
		try {
			this.outboundPort.turnOff();
			assertEquals(IronState.OFF, this.outboundPort.getState());
		} catch (Exception e) {
			assertTrue(false);
		}
		this.logMessage("...done.");
	}
	
	public void testGetTemperature() {
		this.logMessage("testGetTemperature()... ");
		try {
			assertEquals(IronTemperature.DELICATE, this.outboundPort.getTemperature());
		} catch (Exception e) {
			assertTrue(false);
		}
		this.logMessage("...done.");
	}
	
	public void testSetTemperature() {
		this.logMessage("testSetTemperature()... ");
		try {
			this.outboundPort.setTemperature(IronTemperature.COTTON);
			assertEquals(IronTemperature.COTTON, this.outboundPort.getTemperature());
		} catch (Exception e) {
			assertTrue(false);
		}
		this.logMessage("...done.");
	}
	
	public void testGetSteam() {
		this.logMessage("testGetSteam()... ");
		try {
			assertEquals(IronSteam.INACTIVE, this.outboundPort.getSteam());
		} catch (Exception e) {
			assertTrue(false);
		}
		this.logMessage("...done.");
	} 
	
	public void testSetSteam() {
		this.logMessage("testSetSteam()... ");
		try {
			this.outboundPort.setSteam(IronSteam.ACTIVE);
			assertEquals(IronSteam.ACTIVE, this.outboundPort.getSteam());
		} catch (Exception e) {
			assertTrue(false);
		}
		this.logMessage("...done.");
	}
	
	public void testGetEnergySavingMode() {
		this.logMessage("testGetEnergySavingMode()... ");
		try {
			assertEquals(IronEnergySavingMode.INACTIVE, this.outboundPort.getEnergySavingMode());
		} catch (Exception e) {
			assertTrue(false);
		}
		this.logMessage("...done.");
	} 
	
	public void testSetEnergySavingMode() {
		this.logMessage("testSetEnergySavingMode()... ");
		try {
			this.outboundPort.setEnergySavingMode(IronEnergySavingMode.ACTIVE);
			assertEquals(IronEnergySavingMode.ACTIVE, this.outboundPort.getEnergySavingMode());
		} catch (Exception e) {
			assertTrue(false);
		}
		this.logMessage("...done.");
	}
	
	protected void runAllTests() {
		this.testGetState();
		this.testTurnOn();
		this.testTurnOff();
		this.testGetTemperature();
		this.testSetTemperature();
		this.testGetSteam();
		this.testSetSteam();
		this.testGetEnergySavingMode();
		this.testSetEnergySavingMode();
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
					IronConnector.class.getCanonicalName());

			switch (this.currentSimulationType) {
				case MIL_SIMULATION:
					AtomicSimulatorPlugin asp = new AtomicSimulatorPlugin();
					String uri = this.global2localSimulationArchitectureURIS.
													get(this.globalArchitectureURI);
					Architecture architecture =
						(Architecture) this.localSimulationArchitectures.get(uri);
					asp.setPluginURI(uri);
					asp.setSimulationArchitecture(architecture);
					this.installPlugin(asp);
					break;
				case MIL_RT_SIMULATION:
					RTAtomicSimulatorPlugin rtasp = new RTAtomicSimulatorPlugin();
					uri = this.global2localSimulationArchitectureURIS.
												get(this.globalArchitectureURI);
					architecture =
						(Architecture) this.localSimulationArchitectures.get(uri);
					rtasp.setPluginURI(uri);
					rtasp.setSimulationArchitecture(architecture);
					this.installPlugin(rtasp);
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
	public synchronized void execute() throws Exception
	{
		this.logMessage("IronUser executes.");
		if (this.currentExecutionType.isTest() &&
				(this.currentSimulationType.isNoSimulation() ||
							this.currentSimulationType.isSILSimulation())) 
		{
			ClocksServerWithSimulationOutboundPort clocksServerOutboundPort =
							new ClocksServerWithSimulationOutboundPort(this);
			clocksServerOutboundPort.publishPort();
			this.doPortConnection(
					clocksServerOutboundPort.getPortURI(),
					ClocksServer.STANDARD_INBOUNDPORT_URI,
					ClocksServerWithSimulationConnector.class.getCanonicalName());
			this.logMessage("IronUser gets the clock.");
			AcceleratedAndSimulationClock acceleratedClock =
				clocksServerOutboundPort.getClockWithSimulation(this.clockURI);
			this.doPortDisconnection(clocksServerOutboundPort.getPortURI());
			clocksServerOutboundPort.unpublishPort();

			this.logMessage("IronUser waits until start time.");
			acceleratedClock.waitUntilStart();
			this.logMessage("IronUser starts.");

			if (this.currentSimulationType.isNoSimulation()) {
				this.logMessage("IronUser tests begin without simulation.");
				this.runAllTests();
				this.logMessage("IronUser tests end.");
			} else {
				acceleratedClock.waitUntilSimulationStart();
				silTestScenario(acceleratedClock);
			}
		}
	}
	
	@Override
	public synchronized void finalise() throws Exception {
		this.doPortDisconnection(this.outboundPort.getPortURI());
		super.finalise();
	}

	@Override
	public synchronized void shutdown() throws ComponentShutdownException
	{
		try {
			this.outboundPort.unpublishPort();
		} catch (Exception e) {
			throw new ComponentShutdownException(e) ;
		}
		super.shutdown();
	}
	
	protected void silTestScenario(AcceleratedAndSimulationClock acceleratedClock) {
			assert	!acceleratedClock.simulationStartTimeNotReached() :
					new BCMException("!acceleratedClock.startTimeNotReached()");

			Instant simulationStartInstant =
									acceleratedClock.getSimulationStartInstant();
			Instant turnOn = simulationStartInstant.plusSeconds(3600L);
			Instant delicate = simulationStartInstant.plusSeconds(4500L);
			Instant cotton = simulationStartInstant.plusSeconds(6300L);
			Instant linen = simulationStartInstant.plusSeconds(7200L);
			Instant enableEnergySavingMode = simulationStartInstant.plusSeconds(8000L);
			Instant disableEnergySavingMode = simulationStartInstant.plusSeconds(9200L);
			Instant enableSteamMode = simulationStartInstant.plusSeconds(10200L);
			Instant disableSteamMode = simulationStartInstant.plusSeconds(11200L);
			Instant turnOff = simulationStartInstant.plusSeconds(12000L);

			long delayInNanos = acceleratedClock.nanoDelayUntilInstant(turnOn);
			this.logMessage(
					"Iron#silTestScenario waits for " + delayInNanos
					+ " " + TimeUnit.NANOSECONDS + " i.e., "
					+ TimeUnit.NANOSECONDS.toMillis(delayInNanos)
													+ " " + TimeUnit.MILLISECONDS
					+ " to reach " + turnOn);
			this.scheduleTask(
					o -> { logMessage("IronUser SIL test scenario begins.");
						   try {
							((IronUser)o).turnOn();
						} catch (Exception e) {
							e.printStackTrace();
						}
						 },
					delayInNanos, TimeUnit.NANOSECONDS);
			
			
			delayInNanos = acceleratedClock.nanoDelayUntilInstant(delicate);
			this.logMessage(
					"Iron#silTestScenario waits for " + delayInNanos
					+ " " + TimeUnit.NANOSECONDS + " i.e., "
					+ TimeUnit.NANOSECONDS.toMillis(delayInNanos)
													+ " " + TimeUnit.MILLISECONDS
					+ " to reach " + delicate);
			this.scheduleTask(
					o -> {
						try {
							((IronUser)o).setTemperature(IronTemperature.DELICATE);
						} catch (Exception e) {
							e.printStackTrace();
						}
					},
					delayInNanos, TimeUnit.NANOSECONDS);
			
			
			delayInNanos = acceleratedClock.nanoDelayUntilInstant(cotton);
			this.logMessage(
					"Iron#silTestScenario waits for " + delayInNanos
					+ " " + TimeUnit.NANOSECONDS + " i.e., "
					+ TimeUnit.NANOSECONDS.toMillis(delayInNanos)
													+ " " + TimeUnit.MILLISECONDS
					+ " to reach " + cotton);
			this.scheduleTask(
					o -> {
						try {
							((IronUser)o).setTemperature(IronTemperature.COTTON);
						} catch (Exception e) {
							e.printStackTrace();
						}
					},
					delayInNanos, TimeUnit.NANOSECONDS);
			
			
			delayInNanos = acceleratedClock.nanoDelayUntilInstant(linen);
			this.logMessage(
					"Iron#silTestScenario waits for " + delayInNanos
					+ " " + TimeUnit.NANOSECONDS + " i.e., "
					+ TimeUnit.NANOSECONDS.toMillis(delayInNanos)
													+ " " + TimeUnit.MILLISECONDS
					+ " to reach " + linen);
			this.scheduleTask(
					o -> {
						try {
							((IronUser)o).setTemperature(IronTemperature.LINEN);
						} catch (Exception e) {
							e.printStackTrace();
						}
					},
					delayInNanos, TimeUnit.NANOSECONDS);
			
			delayInNanos = acceleratedClock.nanoDelayUntilInstant(enableEnergySavingMode);
			this.logMessage(
					"Iron#silTestScenario waits for " + delayInNanos
					+ " " + TimeUnit.NANOSECONDS + " i.e., "
					+ TimeUnit.NANOSECONDS.toMillis(delayInNanos)
													+ " " + TimeUnit.MILLISECONDS
					+ " to reach " + enableEnergySavingMode);
			this.scheduleTask(
					o -> {
						try {
							((IronUser)o).enableEnergySavingMode();
						} catch (Exception e) {
							e.printStackTrace();
						}
					},
					delayInNanos, TimeUnit.NANOSECONDS);
			
			delayInNanos = acceleratedClock.nanoDelayUntilInstant(disableEnergySavingMode);
			this.logMessage(
					"Iron#silTestScenario waits for " + delayInNanos
					+ " " + TimeUnit.NANOSECONDS + " i.e., "
					+ TimeUnit.NANOSECONDS.toMillis(delayInNanos)
													+ " " + TimeUnit.MILLISECONDS
					+ " to reach " + disableEnergySavingMode);
			this.scheduleTask(
					o -> {
						try {
							((IronUser)o).disableEnergySavingMode();
						} catch (Exception e) {
							e.printStackTrace();
						}
					},
					delayInNanos, TimeUnit.NANOSECONDS);
		
			delayInNanos = acceleratedClock.nanoDelayUntilInstant(enableSteamMode);
			this.logMessage(
					"Iron#silTestScenario waits for " + delayInNanos
					+ " " + TimeUnit.NANOSECONDS + " i.e., "
					+ TimeUnit.NANOSECONDS.toMillis(delayInNanos)
													+ " " + TimeUnit.MILLISECONDS
					+ " to reach " + enableSteamMode);
			this.scheduleTask(
					o -> {
						try {
							((IronUser)o).enableSteamMode();
						} catch (Exception e) {
							e.printStackTrace();
						}
					},
					delayInNanos, TimeUnit.NANOSECONDS);
			
			
			
			delayInNanos = acceleratedClock.nanoDelayUntilInstant(disableSteamMode);
			this.logMessage(
					"Iron#silTestScenario waits for " + delayInNanos
					+ " " + TimeUnit.NANOSECONDS + " i.e., "
					+ TimeUnit.NANOSECONDS.toMillis(delayInNanos)
													+ " " + TimeUnit.MILLISECONDS
					+ " to reach " + disableSteamMode);
			this.scheduleTask(
					o -> {
						try {
							((IronUser)o).disableSteamMode();
						} catch (Exception e) {
							e.printStackTrace();
						}
					},
					delayInNanos, TimeUnit.NANOSECONDS);
			
			delayInNanos = acceleratedClock.nanoDelayUntilInstant(turnOff);
			this.logMessage(
					"Iron#silTestScenario waits for " + delayInNanos
					+ " " + TimeUnit.NANOSECONDS + " i.e., "
					+ TimeUnit.NANOSECONDS.toMillis(delayInNanos)
													+ " " + TimeUnit.MILLISECONDS
					+ " to reach " + turnOff);
			this.scheduleTask(
					o -> {
						try {
							((IronUser)o).turnOff();
						} catch (Exception e) {
							e.printStackTrace();
						}
					},
					delayInNanos, TimeUnit.NANOSECONDS);
		}
}
