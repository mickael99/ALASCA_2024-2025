package fr.sorbonne_u.components.equipments.windTurbine;

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
import fr.sorbonne_u.components.equipments.windTurbine.mil.LocalSimulationArchitectures;
import fr.sorbonne_u.components.equipments.windTurbine.mil.WindTurbineStateModel;
import fr.sorbonne_u.components.equipments.windTurbine.mil.events.StartWindTurbineEvent;
import fr.sorbonne_u.components.equipments.windTurbine.mil.events.StopWindTurbineEvent;
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

@OfferedInterfaces(offered = {WindTurbineCI.class})
@RequiredInterfaces(required = {ClocksServerWithSimulationCI.class})
public class WindTurbine extends AbstractCyPhyComponent implements WindTurbineI {
	
	// -------------------------------------------------------------------------
	// Constants and variables
	// -------------------------------------------------------------------------
	
	public static final String REFLECTION_INBOUND_PORT_URI = "WIND_TURBINE_RIP_URI";
	
	public static boolean VERBOSE = true;
	public static int X_RELATIVE_POSITION = 2;
	public static int Y_RELATIVE_POSITION = 1;
	
	public static final WindTurbineState INIT_STATE = WindTurbineState.STANDBY;
	protected WindTurbineState currentState;
	
	public static final String INBOUND_PORT_URI = "WIND_TURBINE_INBOUND_PORT_URI";
	protected WindTurbineInboundPort inboundPort;
	
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
	
		
	// -------------------------------------------------------------------------
	// Invariants
	// -------------------------------------------------------------------------

	protected static boolean glassBoxInvariants(WindTurbine windTurbine) {
		assert 	windTurbine != null : new PreconditionException("WindTurbine != null");

		boolean ret = true;
		ret &= InvariantChecking.checkGlassBoxInvariant(
					windTurbine.currentState != null,
					WindTurbine.class, windTurbine,
					"currentState != null");
		ret &= InvariantChecking.checkGlassBoxInvariant(
					windTurbine.currentExecutionType != null,
					WindTurbine.class, windTurbine,
					"currentExecutionType != null");
		ret &= InvariantChecking.checkGlassBoxInvariant(
					windTurbine.currentSimulationType != null,
					WindTurbine.class, windTurbine,
					"currentSimulationType != null");
		ret &= InvariantChecking.checkGlassBoxInvariant(
					!windTurbine.currentExecutionType.isStandard() ||
						windTurbine.currentSimulationType.isNoSimulation(),
					WindTurbine.class, windTurbine,
					"!currentExecutionType.isStandard() || "
					+ "currentSimulationType.isNoSimulation()");
		ret &= InvariantChecking.checkGlassBoxInvariant(
					windTurbine.currentSimulationType.isNoSimulation() ||
						(windTurbine.globalArchitectureURI != null &&
							!windTurbine.globalArchitectureURI.isEmpty()),
					WindTurbine.class, windTurbine,
					"currentSimulationType.isNoSimulation() || "
					+ "(globalArchitectureURI != null && "
					+ "!globalArchitectureURI.isEmpty())");
		ret &= InvariantChecking.checkGlassBoxInvariant(
					windTurbine.currentSimulationType.isNoSimulation() ||
						(windTurbine.localArchitectureURI != null &&
							!windTurbine.localArchitectureURI.isEmpty()),
					WindTurbine.class, windTurbine,
					"currentSimulationType.isNoSimulation() || "
					+ "(localArchitectureURI != null && "
					+ "!localArchitectureURI.isEmpty())");
		ret &= InvariantChecking.checkGlassBoxInvariant(
					!windTurbine.currentSimulationType.isSimulated() ||
							windTurbine.simulationTimeUnit != null,
					WindTurbine.class, windTurbine,
					"!currentSimulationType.isSimulated() || "
					+ "simulationTimeUnit != null");
		ret &= InvariantChecking.checkGlassBoxInvariant(
					!windTurbine.currentSimulationType.isRealTimeSimulation() ||
													windTurbine.accFactor > 0.0,
					WindTurbine.class, windTurbine,
					"!currentSimulationType.isRealTimeSimulation() || "
					+ "accFactor > 0.0");
		return ret;
	}

	protected static boolean blackBoxInvariants(WindTurbine windTurbine) throws Exception {
		assert 	windTurbine != null : new PreconditionException("WindTurbine != null");

		boolean ret = true;
		ret &= InvariantChecking.checkBlackBoxInvariant(
					X_RELATIVE_POSITION >= 0,
					WindTurbine.class, windTurbine,
					"X_RELATIVE_POSITION >= 0");
		ret &= InvariantChecking.checkBlackBoxInvariant(
					Y_RELATIVE_POSITION >= 0,
					WindTurbine.class, windTurbine,
					"Y_RELATIVE_POSITION >= 0");
		ret &= InvariantChecking.checkBlackBoxInvariant(
					REFLECTION_INBOUND_PORT_URI != null &&
								!REFLECTION_INBOUND_PORT_URI.isEmpty(),
					WindTurbine.class, windTurbine,
					"REFLECTION_INBOUND_PORT_URI != null && "
							+ "!REFLECTION_INBOUND_PORT_URI.isEmpty()");
		ret &= InvariantChecking.checkBlackBoxInvariant(
					INBOUND_PORT_URI != null && !INBOUND_PORT_URI.isEmpty(),
					WindTurbine.class, windTurbine,
					"INBOUND_PORT_URI != null && !INBOUND_PORT_URI.isEmpty()");
		ret &= InvariantChecking.checkBlackBoxInvariant(
					INIT_STATE != null,
					WindTurbine.class, windTurbine,
					"INIT_STATE != null");
		return ret;
	}
	
	
	// -------------------------------------------------------------------------
	// Constructors
	// -------------------------------------------------------------------------
	
	protected WindTurbine() throws Exception {
		this(INBOUND_PORT_URI);
	}
			
	protected WindTurbine(String batteryInboundPortURI) throws Exception {		
		this(batteryInboundPortURI, ExecutionType.STANDARD);
	}
	
	protected WindTurbine(String batteryInboundPortURI, ExecutionType currentExecutionType) throws Exception {
		this(REFLECTION_INBOUND_PORT_URI, batteryInboundPortURI,
				 currentExecutionType, SimulationType.NO_SIMULATION,
				 null, null, null, 0.0, null);
	}
	
	protected WindTurbine(String reflectionInboundPortURI, 
			String windTurbineInboundPortURI, 
			ExecutionType currentExecutionType,
			SimulationType currentSimulationType, 
			String globalArchitectureURI, 
			String localArchitectureURI,
			TimeUnit simulationTimeUnit, 
			double accFactor, 
			String clockURI ) throws Exception 
	{
super(reflectionInboundPortURI, 2, 1);
		
		assert	windTurbineInboundPortURI != null && !windTurbineInboundPortURI.isEmpty() :
			new PreconditionException("windTurbineInboundPortURI != null && " + "!windTurbineInboundPortURI.isEmpty()");
		
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
		this.initialise(windTurbineInboundPortURI);
		
		assert	WindTurbine.glassBoxInvariants(this) :
			new ImplementationInvariantException(
					"WindTurbine.glassBoxInvariants(this)");
		assert	WindTurbine.blackBoxInvariants(this) :
			new InvariantException("WindTurbine.blackBoxInvariants(this)");
	}
	
	
	protected void initialise(String inboundPortURI) throws Exception {
		assert inboundPortURI != null :
			new PreconditionException(
								"the wind turbine inbound port uri is null, impossible to initialise");
		assert	!inboundPortURI.isEmpty() :
					new PreconditionException(
								"the wind turbine inbound port uri is empty, impossible to initialise");
		
		this.currentState = INIT_STATE;
		
		this.inboundPort = new WindTurbineInboundPort(inboundPortURI, this);
		this.inboundPort.publishPort();
		
		switch (this.currentSimulationType) {
			case MIL_SIMULATION:
				Architecture architecture = null;
				if (this.currentExecutionType.isUnitTest()) {
					architecture = LocalSimulationArchitectures.createWindTurbineMILLocalArchitecture4UnitTest(
														this.localArchitectureURI, this.simulationTimeUnit);
				} 
				else {
					assert this.currentExecutionType.isIntegrationTest();
					
					architecture = LocalSimulationArchitectures.createWindTurbineMILLocalArchitecture4IntegrationTest(
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
					architecture = LocalSimulationArchitectures.createWindTurbineMIL_RT_Architecture4UnitTest(
																	this.currentSimulationType,
																	this.localArchitectureURI,
																	this.simulationTimeUnit,
																	this.accFactor);
				} 
				else {
					assert	this.currentExecutionType.isIntegrationTest();
					architecture = LocalSimulationArchitectures.createWindTurbine_RT_LocalArchitecture4IntegrationTest(
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
			this.tracer.get().setTitle("Wind turbine component");
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
					this.asp = new RTAtomicSimulatorPlugin();
					uri = this.global2localSimulationArchitectureURIS.get(this.globalArchitectureURI);
					architecture = (Architecture) this.localSimulationArchitectures.get(uri);
					((RTAtomicSimulatorPlugin)this.asp).setPluginURI(uri);
					((RTAtomicSimulatorPlugin)this.asp).setSimulationArchitecture(architecture);
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
	public boolean isActivate() throws Exception {
		if(VERBOSE)
			this.traceMessage("Wind turbine says if it's activate or not -> " + this.currentState.toString() + "\n");
		
		return this.currentState == WindTurbineState.ACTIVE;
	}


	@Override
	public void stop() throws Exception {
		if(VERBOSE)
			this.traceMessage("Stop the wind turbine \n");
		
		this.currentState = WindTurbineState.STANDBY;
		
		if(this.currentSimulationType.isSILSimulation()) 
		{
			((RTAtomicSimulatorPlugin)this.asp).triggerExternalEvent(
					WindTurbineStateModel.SIL_URI, 
					t -> new StopWindTurbineEvent(t));
		}
	}
	
	@Override
	public void activate() throws Exception {
		if(VERBOSE)
			this.traceMessage("start the wind turbine \n");
		
		this.currentState = WindTurbineState.ACTIVE;
		
		if(this.currentSimulationType.isSILSimulation()) 
		{
			((RTAtomicSimulatorPlugin)this.asp).triggerExternalEvent(
					WindTurbineStateModel.SIL_URI, 
					t -> new StartWindTurbineEvent(t));
		}
	}
}
