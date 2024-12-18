package fr.sorbonne_u.components.equipments.iron;

import java.util.HashMap;
import java.util.concurrent.TimeUnit;

import fr.sorbonne_u.components.annotations.OfferedInterfaces;
import fr.sorbonne_u.components.annotations.RequiredInterfaces;
import fr.sorbonne_u.components.cyphy.AbstractCyPhyComponent;
import fr.sorbonne_u.components.cyphy.plugins.devs.AtomicSimulatorPlugin;
import fr.sorbonne_u.components.cyphy.plugins.devs.RTAtomicSimulatorPlugin;
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
import fr.sorbonne_u.exceptions.PostconditionException;
import fr.sorbonne_u.exceptions.PreconditionException;
import fr.sorbonne_u.utils.aclocks.ClocksServer;
import fr.sorbonne_u.components.cyphy.utils.aclocks.AcceleratedAndSimulationClock;
import fr.sorbonne_u.components.cyphy.utils.aclocks.ClocksServerWithSimulationCI;
import fr.sorbonne_u.components.cyphy.utils.aclocks.ClocksServerWithSimulationConnector;
import fr.sorbonne_u.components.cyphy.utils.aclocks.ClocksServerWithSimulationOutboundPort;
import fr.sorbonne_u.components.equipments.iron.mil.IronStateModel;
import fr.sorbonne_u.components.equipments.iron.mil.LocalSimulationArchitectures;
import fr.sorbonne_u.components.equipments.iron.mil.events.DisableEnergySavingModeIron;
import fr.sorbonne_u.components.equipments.iron.mil.events.DisableSteamModeIron;
import fr.sorbonne_u.components.equipments.iron.mil.events.EnableCottonModeIron;
import fr.sorbonne_u.components.equipments.iron.mil.events.EnableDelicateModeIron;
import fr.sorbonne_u.components.equipments.iron.mil.events.EnableEnergySavingModeIron;
import fr.sorbonne_u.components.equipments.iron.mil.events.EnableLinenModeIron;
import fr.sorbonne_u.components.equipments.iron.mil.events.EnableSteamModeIron;
import fr.sorbonne_u.components.equipments.iron.mil.events.TurnOffIron;
import fr.sorbonne_u.components.equipments.iron.mil.events.TurnOnIron;


@OfferedInterfaces(offered={IronUserCI.class})
@RequiredInterfaces(required = {ClocksServerWithSimulationCI.class})
public class Iron extends AbstractCyPhyComponent implements IronImplementationI {
	
	// -------------------------------------------------------------------------
	// Constants and variables
	// -------------------------------------------------------------------------
	
	public static boolean VERBOSE = true;
	public static int X_RELATIVE_POSITION = 0;
	public static int Y_RELATIVE_POSITION = 1;
	
	protected IronState currentState;
	protected final static IronState INITIAL_STATE = IronState.OFF;
		
	protected boolean isSteamEnable;
	
	protected boolean isEnergySavingModeEnable;
	
	public static final String REFLECTION_INBOUND_PORT_URI = "IRON_RIP_URI";
	public static final String INBOUND_PORT_URI = "IRON-INBOUND-PORT";
	protected IronInboundPort inboundPort;
	
	// Execution/Simulation

	protected final ExecutionType currentExecutionType;
	protected final SimulationType currentSimulationType;

													
	protected final String clockURI;
	protected AtomicSimulatorPlugin asp;
	
	protected final String globalArchitectureURI;
	protected final String localArchitectureURI;
	
	protected final TimeUnit simulationTimeUnit;
	protected double accFactor;
	
	
	// -------------------------------------------------------------------------
	// Constructors
	// -------------------------------------------------------------------------
	
	protected Iron() throws Exception {
		this(INBOUND_PORT_URI);
	}
			
	protected Iron(String ironInboundPortURI) throws Exception {		
		this(ironInboundPortURI, ExecutionType.STANDARD);
	}
	
	protected Iron(String ironInboundPortURI, ExecutionType currentExecutionType) throws Exception {
		this(REFLECTION_INBOUND_PORT_URI, ironInboundPortURI,
				 currentExecutionType, SimulationType.NO_SIMULATION,
				 null, null, null, 0.0, null);

		assert	currentExecutionType.isTest() :
				new PreconditionException("currentExecutionType.isTest()");
	}
	
	protected Iron (String reflectionInboundPortURI, String ironInboundPortURI, ExecutionType currentExecutionType,
							SimulationType currentSimulationType, String globalArchitectureURI, String localArchitectureURI,
							TimeUnit simulationTimeUnit, double accFactor, String clockURI ) throws Exception
	{
		super(reflectionInboundPortURI, 2, 0);
		
		assert	ironInboundPortURI != null && !ironInboundPortURI.isEmpty() :
			new PreconditionException("ironInboundPortURI != null && " + "!ironInboundPortURI.isEmpty()");
		
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
		
		this.initialise(ironInboundPortURI);
		
		assert	Iron.glassBoxInvariants(this) :
			new ImplementationInvariantException(
					"Iron.glassBoxInvariants(this)");
		assert	Iron.blackBoxInvariants(this) :
			new InvariantException("Iron.blackBoxInvariants(this)");
	}
	
	protected void initialise(String inboundPortURI) throws Exception {
		assert inboundPortURI != null :
			new PreconditionException(
								"the iron inbound port uri is null, impossible to initialise");
		assert	!inboundPortURI.isEmpty() :
					new PreconditionException(
								"the iron inbound port uri is empty, impossible to initialise");
		
		this.currentState = INITIAL_STATE;
		this.isSteamEnable = false;
		this.isEnergySavingModeEnable = false;
		
		this.inboundPort = new IronInboundPort(inboundPortURI, this);
		this.inboundPort.publishPort();
		
		switch (this.currentSimulationType) {
			case MIL_SIMULATION:
				Architecture architecture = null;
				if (this.currentExecutionType.isUnitTest()) {
					architecture = LocalSimulationArchitectures.createIronMILLocalArchitecture4UnitTest(
														this.localArchitectureURI, this.simulationTimeUnit);
				} 
				else {
					assert this.currentExecutionType.isIntegrationTest();
					
					architecture = LocalSimulationArchitectures.createIronMILArchitecture4IntegrationTest(
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
					architecture = LocalSimulationArchitectures.createIronMIL_RT_Architecture4UnitTest(
																	this.localArchitectureURI,
																	this.simulationTimeUnit,
																	this.accFactor);
				} 
				else {
					assert	this.currentExecutionType.isIntegrationTest();
					architecture = LocalSimulationArchitectures.createIronMIL_RT_Architecture4IntegrationTest(
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
			this.tracer.get().setTitle("Iron component");
			this.tracer.get().setRelativePosition(X_RELATIVE_POSITION,
												  Y_RELATIVE_POSITION);
			this.toggleTracing();
		}
	}
	
	
	// -------------------------------------------------------------------------
	// Invariants
	// -------------------------------------------------------------------------

	protected static boolean glassBoxInvariants(Iron iron) {
		assert 	iron != null : new PreconditionException("iron != null");

		boolean ret = true;
		ret &= InvariantChecking.checkGlassBoxInvariant(
					iron.currentState != null,
					Iron.class, iron,
					"currentState != null");
		ret &= InvariantChecking.checkGlassBoxInvariant(
					iron.currentExecutionType != null,
					Iron.class, iron,
					"currentExecutionType != null");
		ret &= InvariantChecking.checkGlassBoxInvariant(
					iron.currentSimulationType != null,
					Iron.class, iron,
					"currentSimulationType != null");
		ret &= InvariantChecking.checkGlassBoxInvariant(
					!iron.currentExecutionType.isStandard() ||
								iron.currentSimulationType.isNoSimulation(),
					Iron.class, iron,
					"!currentExecutionType.isStandard() || "
					+ "currentSimulationType.isNoSimulation()");
		ret &= InvariantChecking.checkGlassBoxInvariant(
					iron.currentSimulationType.isNoSimulation() ||
						(iron.globalArchitectureURI != null &&
							!iron.globalArchitectureURI.isEmpty()),
					Iron.class, iron,
					"currentSimulationType.isNoSimulation() || "
					+ "(globalArchitectureURI != null && "
					+ "!globalArchitectureURI.isEmpty())");
		ret &= InvariantChecking.checkGlassBoxInvariant(
					iron.currentSimulationType.isNoSimulation() ||
						(iron.localArchitectureURI != null &&
							!iron.localArchitectureURI.isEmpty()),
					Iron.class, iron,
					"currentSimulationType.isNoSimulation() || "
					+ "(localArchitectureURI != null && "
					+ "!localArchitectureURI.isEmpty())");
		ret &= InvariantChecking.checkGlassBoxInvariant(
					!iron.currentSimulationType.isSimulated() ||
												iron.simulationTimeUnit != null,
					Iron.class, iron,
					"!currentSimulationType.isSimulated() || "
					+ "simulationTimeUnit != null");
		ret &= InvariantChecking.checkGlassBoxInvariant(
					!iron.currentSimulationType.isRealTimeSimulation() ||
														iron.accFactor > 0.0,
					Iron.class, iron,
					"!currentSimulationType.isRealTimeSimulation() || "
					+ "accFactor > 0.0");
		return ret;
	}

	protected static boolean blackBoxInvariants(Iron iron) throws Exception {
		assert 	iron != null : new PreconditionException("iron != null");

		boolean ret = true;
		ret &= InvariantChecking.checkBlackBoxInvariant(
					X_RELATIVE_POSITION >= 0,
					Iron.class, iron,
					"X_RELATIVE_POSITION >= 0");
		ret &= InvariantChecking.checkBlackBoxInvariant(
					Y_RELATIVE_POSITION >= 0,
					Iron.class, iron,
					"Y_RELATIVE_POSITION >= 0");
		ret &= InvariantChecking.checkBlackBoxInvariant(
					REFLECTION_INBOUND_PORT_URI != null &&
								!REFLECTION_INBOUND_PORT_URI.isEmpty(),
					Iron.class, iron,
					"REFLECTION_INBOUND_PORT_URI != null && "
							+ "!REFLECTION_INBOUND_PORT_URI.isEmpty()");
		ret &= InvariantChecking.checkBlackBoxInvariant(
					INBOUND_PORT_URI != null && !INBOUND_PORT_URI.isEmpty(),
					Iron.class, iron,
					"INBOUND_PORT_URI != null && !INBOUND_PORT_URI.isEmpty()");
		ret &= InvariantChecking.checkBlackBoxInvariant(
					INITIAL_STATE != null,
					Iron.class, iron,
					"INITIAL_STATE != null");
		return ret;
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
	public void execute() throws Exception {
		if(this.currentExecutionType.isUnitTest() && this.currentSimulationType.isSILSimulation()) {
			ClocksServerWithSimulationOutboundPort clocksServerOutboundPort =
					new ClocksServerWithSimulationOutboundPort(this);
			clocksServerOutboundPort.publishPort();
			this.doPortConnection(
					clocksServerOutboundPort.getPortURI(),
					ClocksServer.STANDARD_INBOUNDPORT_URI,
					ClocksServerWithSimulationConnector.class.getCanonicalName());
			this.logMessage("Iron gets the clock.");
			AcceleratedAndSimulationClock acceleratedClock = 
					clocksServerOutboundPort.getClockWithSimulation(this.clockURI);
			this.doPortDisconnection(clocksServerOutboundPort.getPortURI());
			clocksServerOutboundPort.unpublishPort();

			this.asp.createSimulator();
			this.asp.setSimulationRunParameters(new HashMap<>());
			this.asp.initialiseSimulation(
						new Time(acceleratedClock.getSimulatedStartTime(),
								 this.simulationTimeUnit),
						new Duration(acceleratedClock.getSimulatedDuration(),
									 this.simulationTimeUnit));
			this.asp.startRTSimulation(
					TimeUnit.NANOSECONDS.toMillis(
							acceleratedClock.getSimulationStartEpochNanos()),
					acceleratedClock.getSimulatedStartTime(),
					acceleratedClock.getSimulatedDuration());

			acceleratedClock.waitUntilSimulationEnd();

			Thread.sleep(200L);
			this.logMessage(this.asp.getFinalReport().toString());
		}
	}
	 
	@Override
	public synchronized void shutdown() throws ComponentShutdownException {
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
	public IronState getState() throws Exception {
		if(VERBOSE)
			this.traceMessage("Iron returns its state : " + this.currentState.toString() + ".\n");
		
		return this.currentState;
	}

	@Override
	public void turnOn() throws Exception {
		assert	this.getState() == IronState.OFF :
			new PreconditionException("getState() == IronState.OFF");
		
		if(VERBOSE)
			this.traceMessage("Iron is turning on.\n");
		
		this.currentState = IronState.DELICATE;
		
		assert	this.getState() == IronState.DELICATE :
			new PostconditionException("getState() == IronState.DELICATE");
		
		if(this.currentSimulationType.isSILSimulation()) 
		{
			if(VERBOSE)
				this.traceMessage("Trigger new event for IronStateModel -> TurnOn().\n");
			
			((RTAtomicSimulatorPlugin)this.asp).triggerExternalEvent(
					IronStateModel.SIL_URI,
					t -> new TurnOnIron(t));
		}
	}

	@Override
	public void turnOff() throws Exception {
		assert	this.getState() != IronState.OFF :
			new PreconditionException("getState() != IronState.OFF");
		
		if(VERBOSE)
			this.traceMessage("Iron is turning off.\n");
		
		this.currentState = IronState.OFF;
		
		assert	this.getState() == IronState.OFF :
			new PostconditionException("getState() == IronState.OFF");
		
		if(this.currentSimulationType.isSILSimulation()) 
		{
			if(VERBOSE)
				this.traceMessage("Trigger new event for IronStateModel -> TurnOff().\n");
			
			((RTAtomicSimulatorPlugin)this.asp).triggerExternalEvent(
					IronStateModel.SIL_URI,
					t -> new TurnOffIron(t));
		}
	}

	@Override
	public boolean isTurnOn() throws Exception {
		if(VERBOSE)
			this.traceMessage("Check if the iron is turn on .\n");
		
		boolean ret;
		if(this.getState() != IronState.OFF)
			ret = true;
		ret = false;
		
		if(VERBOSE)
			this.traceMessage("Is iron turn on ? ->  " + ret + ".\n");
		
		return ret;
	}

	@Override
	public void setState(IronState s) throws Exception {
		if(VERBOSE)
			this.traceMessage("Trying setting the stat  -> " + s.toString() + ".\n");
		
		assert this.currentState != s : 
			new PreconditionException("this.currentState != " + s.toString());
		
		if(VERBOSE)
			this.traceMessage("Iron gets a new state : " + s.toString() + ".\n");
		
		if(this.currentSimulationType.isSILSimulation()) 
		{
			switch(s) {
				case DELICATE: 
					if(VERBOSE)
						this.traceMessage("Trigger new event for IronStateModel -> EnableDelicateModeIron().\n");
					
					((RTAtomicSimulatorPlugin)this.asp).triggerExternalEvent(
							IronStateModel.SIL_URI,
							time -> new EnableDelicateModeIron(time));
					break;
					
				case COTTON:
					if(VERBOSE)
						this.traceMessage("Trigger new event for IronStateModel -> EnableCottonModeIron().\n");
					
					((RTAtomicSimulatorPlugin)this.asp).triggerExternalEvent(
							IronStateModel.SIL_URI,
							time -> new EnableCottonModeIron(time));
					break;
					
				case LINEN:
					if(VERBOSE)
						this.traceMessage("Trigger new event for IronStateModel -> EnableLinenModeIron().\n");
					
					((RTAtomicSimulatorPlugin)this.asp).triggerExternalEvent(
							IronStateModel.SIL_URI,
							time -> new EnableLinenModeIron(time));
					break;
					
				default: 
					this.turnOff();
					break;
			}
		}
		
	}

	@Override
	public boolean isSteamModeEnable() throws Exception {
		if(VERBOSE)
			this.traceMessage("Check if the steam mode is enable .\n");
		
		boolean ret;
		
		if(this.isSteamEnable)
			ret = true;
		ret = false;
		
		if(VERBOSE)
			this.traceMessage("Is steam mode enable ?  -> " + ret + ".\n");
		
		return ret;
	}

	@Override
	public void EnableSteamMode() throws Exception {
		if(VERBOSE)
			this.traceMessage("Trying to enable steam mode.\n");
		
		assert this.isSteamEnable: 
			new PreconditionException("this.isSteamEnable");
		
		this.isSteamEnable = true;
		
		if(VERBOSE)
			this.traceMessage("Steam mode enable success.\n");
		
		if(this.currentSimulationType.isSILSimulation()) 
		{
			if(VERBOSE)
				this.traceMessage("Trigger new event for IronStateModel -> EnableSteamMode().\n");
			
			((RTAtomicSimulatorPlugin)this.asp).triggerExternalEvent(
					IronStateModel.SIL_URI,
					t -> new EnableSteamModeIron(t));
		}
	}

	@Override
	public void DisableSteamMode() throws Exception {
		if(VERBOSE)
			this.traceMessage("Trying to disable steam mode.\n");
		
		assert !this.isSteamEnable: 
			new PreconditionException("!this.isSteamEnable");
		
		this.isSteamEnable = false;
		
		if(VERBOSE)
			this.traceMessage("Steam mode disable success.\n");
		
		if(this.currentSimulationType.isSILSimulation()) 
		{
			if(VERBOSE)
				this.traceMessage("Trigger new event for IronStateModel -> DisableSteamMode().\n");
			
			((RTAtomicSimulatorPlugin)this.asp).triggerExternalEvent(
					IronStateModel.SIL_URI,
					t -> new DisableSteamModeIron(t));
		}
	}

	@Override
	public boolean isEnergySavingModeEnable() throws Exception {
		if(VERBOSE)
			this.traceMessage("Check if the energy saving mode is enable .\n");
		
		boolean ret;
		
		if(this.isEnergySavingModeEnable)
			ret = true;
		ret = false;
		
		if(VERBOSE)
			this.traceMessage("Is energy saving mode enable ?  -> " + ret + ".\n");
		
		return ret;
	}

	@Override
	public void EnableEnergySavingMode() throws Exception {
		if(VERBOSE)
			this.traceMessage("Trying to enable energy saving mode.\n");
		
		assert this.isEnergySavingModeEnable: 
			new PreconditionException("this.isEnergySavingModeEnable");
		
		this.isEnergySavingModeEnable = true;
		
		if(VERBOSE)
			this.traceMessage("Energy saving mode enable success.\n");
		
		if(this.currentSimulationType.isSILSimulation()) 
		{
			if(VERBOSE)
				this.traceMessage("Trigger new event for IronStateModel -> EnableEnergySavingMode().\n");
			
			((RTAtomicSimulatorPlugin)this.asp).triggerExternalEvent(
					IronStateModel.SIL_URI,
					t -> new EnableEnergySavingModeIron(t));
		}
	}

	@Override
	public void DisableEnergySavingMode() throws Exception {
		if(VERBOSE)
			this.traceMessage("Trying to disable energy saving mode.\n");
		
		assert !this.isEnergySavingModeEnable: 
			new PreconditionException("!this.isEnergySavingModeEnable");
		
		this.isEnergySavingModeEnable = false;
		
		if(VERBOSE)
			this.traceMessage("Energy saving mode disable success.\n");
		
		if(this.currentSimulationType.isSILSimulation()) 
		{
			if(VERBOSE)
				this.traceMessage("Trigger new event for IronStateModel -> DisableEnergySavingMode().\n");
			
			((RTAtomicSimulatorPlugin)this.asp).triggerExternalEvent(
					IronStateModel.SIL_URI,
					t -> new DisableEnergySavingModeIron(t));
		}
	}
}
