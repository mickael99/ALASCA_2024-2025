package fr.sorbonne_u.components.equipments.battery;

import java.util.HashMap;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import fr.sorbonne_u.components.annotations.OfferedInterfaces;
import fr.sorbonne_u.components.annotations.RequiredInterfaces;
import fr.sorbonne_u.components.cyphy.AbstractCyPhyComponent;
import fr.sorbonne_u.components.cyphy.plugins.devs.AtomicSimulatorPlugin;
import fr.sorbonne_u.components.cyphy.plugins.devs.RTAtomicSimulatorPlugin;
import fr.sorbonne_u.components.cyphy.utils.aclocks.AcceleratedAndSimulationClock;
import fr.sorbonne_u.components.cyphy.utils.aclocks.ClocksServerWithSimulationCI;
import fr.sorbonne_u.components.cyphy.utils.aclocks.ClocksServerWithSimulationConnector;
import fr.sorbonne_u.components.cyphy.utils.aclocks.ClocksServerWithSimulationOutboundPort;
import fr.sorbonne_u.components.equipments.battery.mil.BatteryChargeLevelModel;
import fr.sorbonne_u.components.equipments.battery.mil.BatteryStateModel;
import fr.sorbonne_u.components.equipments.battery.mil.LocalSimulationArchitectures;
import fr.sorbonne_u.components.equipments.battery.mil.events.SetConsumeBatteryEvent;
import fr.sorbonne_u.components.equipments.battery.mil.events.SetProductBatteryEvent;
import fr.sorbonne_u.components.equipments.battery.mil.events.SetStandByBatteryEvent;
import fr.sorbonne_u.components.exceptions.BCMException;
import fr.sorbonne_u.components.exceptions.ComponentShutdownException;
import fr.sorbonne_u.components.exceptions.ComponentStartException;
import fr.sorbonne_u.components.utils.ExecutionType;
import fr.sorbonne_u.components.utils.SimulationType;
import fr.sorbonne_u.devs_simulation.architectures.Architecture;
import fr.sorbonne_u.devs_simulation.models.time.Duration;
import fr.sorbonne_u.devs_simulation.models.time.Time;
import fr.sorbonne_u.devs_simulation.utils.InvariantChecking;
import fr.sorbonne_u.exceptions.ImplementationInvariantException;
import fr.sorbonne_u.exceptions.InvariantException;
import fr.sorbonne_u.exceptions.PreconditionException;
import fr.sorbonne_u.utils.aclocks.AcceleratedClock;
import fr.sorbonne_u.utils.aclocks.ClocksServer;

@OfferedInterfaces(offered = {BatteryCI.class})
@RequiredInterfaces(required = {ClocksServerWithSimulationCI.class})
public class Battery extends AbstractCyPhyComponent implements BatteryI {

	// -------------------------------------------------------------------------
	// Constants and variables
	// -------------------------------------------------------------------------
	
	public static final String REFLECTION_INBOUND_PORT_URI = "BATTERY_RIP_URI";
	
	public static boolean VERBOSE = true;
	public static int X_RELATIVE_POSITION = 2;
	public static int Y_RELATIVE_POSITION = 1;
	
	protected static final BATTERY_STATE INIT_STATE = BATTERY_STATE.STANDBY;
	protected BATTERY_STATE currentState;
	
	protected static final double FAKE_BATTERY_LEVEL = 50.0;
	protected double batteryLevel;
	
	public static final String INBOUND_PORT_URI = "BATTERY_INBOUND_PORT";
	protected BatteryInboundPort inboundPort;
	
	// Execution/Simulation
	protected final ExecutionType currentExecutionType;
	protected final SimulationType currentSimulationType;
					
	protected final String clockURI;
	protected AtomicSimulatorPlugin asp;
	
	protected final String globalArchitectureURI;
	protected final String localArchitectureURI;
	
	protected final TimeUnit simulationTimeUnit;
	protected double accFactor;
	
	protected final CompletableFuture<AcceleratedClock>	clock;
	
	protected static final String CURRENT_CHARGE_LEVEL = "currentChargeLevel";
	
	
	// -------------------------------------------------------------------------
	// Invariants
	// -------------------------------------------------------------------------

	protected static boolean glassBoxInvariants(Battery battery) {
		assert 	battery != null : new PreconditionException("battery != null");

		boolean ret = true;
		ret &= InvariantChecking.checkGlassBoxInvariant(
					battery.currentState != null,
					Battery.class, battery,
					"currentState != null");
		ret &= InvariantChecking.checkGlassBoxInvariant(
					battery.currentExecutionType != null,
					Battery.class, battery,
					"currentExecutionType != null");
		ret &= InvariantChecking.checkGlassBoxInvariant(
					battery.currentSimulationType != null,
					Battery.class, battery,
					"currentSimulationType != null");
		ret &= InvariantChecking.checkGlassBoxInvariant(
					!battery.currentExecutionType.isStandard() ||
								battery.currentSimulationType.isNoSimulation(),
					Battery.class, battery,
					"!currentExecutionType.isStandard() || "
					+ "currentSimulationType.isNoSimulation()");
		ret &= InvariantChecking.checkGlassBoxInvariant(
					battery.currentSimulationType.isNoSimulation() ||
						(battery.globalArchitectureURI != null &&
							!battery.globalArchitectureURI.isEmpty()),
					Battery.class, battery,
					"currentSimulationType.isNoSimulation() || "
					+ "(globalArchitectureURI != null && "
					+ "!globalArchitectureURI.isEmpty())");
		ret &= InvariantChecking.checkGlassBoxInvariant(
					battery.currentSimulationType.isNoSimulation() ||
						(battery.localArchitectureURI != null &&
							!battery.localArchitectureURI.isEmpty()),
					Battery.class, battery,
					"currentSimulationType.isNoSimulation() || "
					+ "(localArchitectureURI != null && "
					+ "!localArchitectureURI.isEmpty())");
		ret &= InvariantChecking.checkGlassBoxInvariant(
					!battery.currentSimulationType.isSimulated() ||
												battery.simulationTimeUnit != null,
					Battery.class, battery,
					"!currentSimulationType.isSimulated() || "
					+ "simulationTimeUnit != null");
		ret &= InvariantChecking.checkGlassBoxInvariant(
					!battery.currentSimulationType.isRealTimeSimulation() ||
														battery.accFactor > 0.0,
					Battery.class, battery,
					"!currentSimulationType.isRealTimeSimulation() || "
					+ "accFactor > 0.0");
		return ret;
	}

	protected static boolean blackBoxInvariants(Battery battery) throws Exception {
		assert 	battery != null : new PreconditionException("battery != null");

		boolean ret = true;
		ret &= InvariantChecking.checkBlackBoxInvariant(
					X_RELATIVE_POSITION >= 0,
					Battery.class, battery,
					"X_RELATIVE_POSITION >= 0");
		ret &= InvariantChecking.checkBlackBoxInvariant(
					Y_RELATIVE_POSITION >= 0,
					Battery.class, battery,
					"Y_RELATIVE_POSITION >= 0");
		ret &= InvariantChecking.checkBlackBoxInvariant(
					REFLECTION_INBOUND_PORT_URI != null &&
								!REFLECTION_INBOUND_PORT_URI.isEmpty(),
					Battery.class, battery,
					"REFLECTION_INBOUND_PORT_URI != null && "
							+ "!REFLECTION_INBOUND_PORT_URI.isEmpty()");
		ret &= InvariantChecking.checkBlackBoxInvariant(
					INBOUND_PORT_URI != null && !INBOUND_PORT_URI.isEmpty(),
					Battery.class, battery,
					"INBOUND_PORT_URI != null && !INBOUND_PORT_URI.isEmpty()");
		ret &= InvariantChecking.checkBlackBoxInvariant(
					INIT_STATE != null,
					Battery.class, battery,
					"INIT_STATE != null");
		return ret;
	}
	
	
	// -------------------------------------------------------------------------
	// Constructors
	// -------------------------------------------------------------------------
	
	protected Battery() throws Exception {
		this(INBOUND_PORT_URI);
	}
			
	protected Battery(String batteryInboundPortURI) throws Exception {		
		this(batteryInboundPortURI, ExecutionType.STANDARD);
	}
	
	protected Battery(String batteryInboundPortURI, ExecutionType currentExecutionType) throws Exception {
		this(REFLECTION_INBOUND_PORT_URI, batteryInboundPortURI,
				 currentExecutionType, SimulationType.NO_SIMULATION,
				 null, null, null, 0.0, null);
	}
	
	protected Battery(
			String reflectionInboundPortURI, 
			String batteryInboundPortURI, 
			ExecutionType currentExecutionType,
			SimulationType currentSimulationType, 
			String globalArchitectureURI, 
			String localArchitectureURI,
			TimeUnit simulationTimeUnit, 
			double accFactor, 
			String clockURI ) throws Exception
	{		
		super(reflectionInboundPortURI, 2, 1);
		
		assert	batteryInboundPortURI != null && !batteryInboundPortURI.isEmpty() :
			new PreconditionException("batteryInboundPortURI != null && " + "!batteryInboundPortURI.isEmpty()");
		
		assert	currentExecutionType != null :
			new PreconditionException("currentExecutionType != null");
		
		assert	currentExecutionType.isStandard() || clockURI != null && !clockURI.isEmpty() :
			new PreconditionException("currentExecutionType.isStandard() || " + "clockURI != null && !clockURI.isEmpty()");
		
		assert	!currentExecutionType.isStandard() || currentSimulationType.isNoSimulation() :
			new PreconditionException("!currentExecutionType.isStandard() || " + "currentSimulationType.isNoSimulation()");
		
		assert	currentSimulationType.isNoSimulation() || (globalArchitectureURI != null &&!globalArchitectureURI.isEmpty()) :
			new PreconditionException("currentSimulationType.isNoSimulation() || "+ "(globalArchitectureURI != null && "
										+ "!globalArchitectureURI.isEmpty())");
		
		assert	currentSimulationType.isNoSimulation() || (localArchitectureURI != null && !localArchitectureURI.isEmpty()) :
			new PreconditionException("currentSimulationType.isNoSimulation() || " + "(localArchitectureURI != null && "
										+ "!localArchitectureURI.isEmpty())");
		
		assert	!currentSimulationType.isSimulated() || simulationTimeUnit != null :
			new PreconditionException("!currentSimulationType.isSimulated() || " + "simulationTimeUnit != null");
		
		assert	!currentSimulationType.isRealTimeSimulation() || accFactor > 0.0 :
			new PreconditionException("!currentSimulationType.isRealTimeSimulation() || "+ "accFactor > 0.0");
	
		
		this.currentExecutionType = currentExecutionType;
		this.currentSimulationType = currentSimulationType;
		this.globalArchitectureURI = globalArchitectureURI;
		this.localArchitectureURI = localArchitectureURI;
		this.simulationTimeUnit = simulationTimeUnit;
		this.accFactor = accFactor;
		this.clockURI = clockURI;
		
		this.clock = new CompletableFuture<AcceleratedClock>();
		this.initialise(batteryInboundPortURI);
		
		assert	Battery.glassBoxInvariants(this) :
			new ImplementationInvariantException(
					"Battery.glassBoxInvariants(this)");
		assert	Battery.blackBoxInvariants(this) :
			new InvariantException("Battery.blackBoxInvariants(this)");
	}
	
	protected void initialise(String inboundPortURI) throws Exception {
		assert inboundPortURI != null :
			new PreconditionException(
								"the battery inbound port uri is null, impossible to initialise");
		assert	!inboundPortURI.isEmpty() :
					new PreconditionException(
								"the battery inbound port uri is empty, impossible to initialise");
		
		this.currentState = INIT_STATE;
		
		this.inboundPort = new BatteryInboundPort(inboundPortURI, this);
		this.inboundPort.publishPort();
		
		switch (this.currentSimulationType) {
			case MIL_SIMULATION:
				Architecture architecture = null;
				if (this.currentExecutionType.isUnitTest()) {
					architecture = LocalSimulationArchitectures.createBatteryMILLocalArchitecture4UnitTest(
														this.localArchitectureURI, this.simulationTimeUnit);
				} 
				else {
					assert this.currentExecutionType.isIntegrationTest();
					
					architecture = LocalSimulationArchitectures.createBatteryMILLocalArchitecture4IntegrationTest(
														this.localArchitectureURI, this.simulationTimeUnit);
				}
				
				assert architecture.getRootModelURI().equals(this.localArchitectureURI) :
					new BCMException("local simulation architecture " + this.localArchitectureURI
										+ " does not exist!");
				
				this.addLocalSimulatorArchitecture(architecture);
				this.global2localSimulationArchitectureURIS.put(this.globalArchitectureURI, this.localArchitectureURI);
				break;
				
			case MIL_RT_SIMULATION:
				
			case SIL_SIMULATION:
				architecture = null;
				if (this.currentExecutionType.isUnitTest()) {
					architecture = LocalSimulationArchitectures.createBatteryMIL_RT_Architecture4UnitTest(
																	this.currentSimulationType,
																	this.localArchitectureURI,
																	this.simulationTimeUnit,
																	this.accFactor);
				} 
				else {
					assert	this.currentExecutionType.isIntegrationTest();
					architecture = LocalSimulationArchitectures.createBattery_RT_LocalArchitecture4IntegrationTest(
																	this.currentSimulationType,
																	this.localArchitectureURI,
																	this.simulationTimeUnit,
																	this.accFactor);
				}
				
				assert	architecture.getRootModelURI().equals(this.localArchitectureURI) :
					new BCMException("local simulation architecture " + this.localArchitectureURI
										+ " does not exist!");
				
				this.addLocalSimulatorArchitecture(architecture);
				this.global2localSimulationArchitectureURIS.put(this.globalArchitectureURI, this.localArchitectureURI);
				break;
				
			default:
		}
				
		if (VERBOSE) {
			this.tracer.get().setTitle("Battery component");
			this.tracer.get().setRelativePosition(X_RELATIVE_POSITION,
												  Y_RELATIVE_POSITION);
			this.toggleTracing();
		}
	}
	
	
	// -------------------------------------------------------------------------
	// Component life-cycle
	// -------------------------------------------------------------------------
	
	@Override
	public synchronized void start() throws ComponentStartException {
		super.start();
		
		try {
			switch(this.currentSimulationType) {
				case MIL_SIMULATION:
					this.asp = new AtomicSimulatorPlugin();
					String uri = this.global2localSimulationArchitectureURIS.get(this.globalArchitectureURI);
					Architecture architecture = (Architecture)this.localSimulationArchitectures.get(uri);
					this.asp.setPluginURI(uri);
					this.asp.setSimulationArchitecture(architecture);
					this.installPlugin(this.asp);
					break;
					
				case MIL_RT_SIMULATION:
					this.asp = new RTAtomicSimulatorPlugin();
					uri = this.global2localSimulationArchitectureURIS.get(this.globalArchitectureURI);
					architecture = (Architecture)this.localSimulationArchitectures.get(uri);
					((RTAtomicSimulatorPlugin)this.asp).setPluginURI(uri);
					((RTAtomicSimulatorPlugin)this.asp).setSimulationArchitecture(architecture);
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
							assert	modelURI.equals(BatteryChargeLevelModel.SIL_URI);
							return ((BatteryChargeLevelModel)
											this.atomicSimulators.get(modelURI).
													getSimulatedModel()).getCurrentChargeLevel();
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
		}
		catch(Exception e) {
			throw new ComponentStartException(e);
		}
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
	}
	
	@Override
	public synchronized void finalise() throws Exception {
		super.finalise();
	}
	
	@Override
	public synchronized void shutdown() throws ComponentShutdownException
	{
		try {
			this.inboundPort.unpublishPort();
		} catch (Exception e) {
			throw new ComponentShutdownException(e) ;
		}
		super.shutdown();
	}

	
	// -------------------------------------------------------------------------
	// Component services implementation
	// -------------------------------------------------------------------------
	
	@Override
	public BATTERY_STATE getState() throws Exception {
		if(VERBOSE) 
			this.logMessage("Battery gets its current state -> " + this.currentState.toString() + "\n");
		
		return this.currentState;
	}

	@Override
	public void setState(BATTERY_STATE state) throws Exception {
		this.currentState = state;
		
		if(this.currentSimulationType.isSILSimulation()) {
			switch(state) {
			case STANDBY:
				((RTAtomicSimulatorPlugin)this.asp).triggerExternalEvent(
						BatteryStateModel.SIL_URI, 
						t -> new SetStandByBatteryEvent(t));
				break;
				
			case CONSUME:
				((RTAtomicSimulatorPlugin)this.asp).triggerExternalEvent(
						BatteryStateModel.SIL_URI, 
						t -> new SetConsumeBatteryEvent(t));
				break;
				
			case PRODUCT:
				((RTAtomicSimulatorPlugin)this.asp).triggerExternalEvent(
						BatteryStateModel.SIL_URI, 
						t -> new SetProductBatteryEvent(t));
				break;
			}
		}
		
		if(VERBOSE) 
			this.logMessage("Battery new state -> " + state.toString() + "\n");
	}
	
	@Override
	public double getBatteryLevel() throws Exception {
		if(VERBOSE) 
			this.logMessage("Battery returns its level \n");
		
		double batteryLevel = FAKE_BATTERY_LEVEL;
		
		if (this.currentSimulationType.isSILSimulation())
		{
			batteryLevel = (double)((RTAtomicSimulatorPlugin)this.asp).
												getModelStateValue(
														BatteryChargeLevelModel.SIL_URI,
														CURRENT_CHARGE_LEVEL);
		}
		
		if (VERBOSE)
			this.traceMessage("Battery returns its current charge level -> " + batteryLevel + "\n.");
		
		return batteryLevel;
	}
}
