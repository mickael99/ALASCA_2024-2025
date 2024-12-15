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
import fr.sorbonne_u.exceptions.PreconditionException;
import fr.sorbonne_u.utils.aclocks.ClocksServer;
import fr.sorbonne_u.components.cyphy.utils.aclocks.AcceleratedAndSimulationClock;
import fr.sorbonne_u.components.cyphy.utils.aclocks.ClocksServerWithSimulationCI;
import fr.sorbonne_u.components.cyphy.utils.aclocks.ClocksServerWithSimulationConnector;
import fr.sorbonne_u.components.cyphy.utils.aclocks.ClocksServerWithSimulationOutboundPort;
import fr.sorbonne_u.components.equipments.iron.sil.LocalSimulationArchitectures;


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
	
	protected IronTemperature currentTemperature;
	protected static final IronTemperature INITIAL_TEMPERATURE = IronTemperature.DELICATE;
	
	protected IronSteam currentSteam;
	protected static final IronSteam INITIAL_STEAM = IronSteam.INACTIVE;
	
	protected IronEnergySavingMode currentEnergySavingMode;
	protected static final IronEnergySavingMode INITIAL_ENERGY_SAVING_MODE = IronEnergySavingMode.INACTIVE;
	
	public static final String REFLECTION_INBOUND_PORT_URI =
										"IRON_RIP_URI";
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
		
//		assert	HairDryer.glassBoxInvariants(this) :
//			new ImplementationInvariantException(
//					"HairDryer.glassBoxInvariants(this)");
//		assert	HairDryer.blackBoxInvariants(this) :
//			new InvariantException("HairDryer.blackBoxInvariants(this)");
	}
	
	protected void initialise(String inboundPortURI) throws Exception {
		assert inboundPortURI != null :
			new PreconditionException(
								"the iron inbound port uri is null, impossible to initialise");
		assert	!inboundPortURI.isEmpty() :
					new PreconditionException(
								"the iron inbound port uri is empty, impossible to initialise");
		
		this.currentState = INITIAL_STATE;
		this.currentTemperature = INITIAL_TEMPERATURE;
		this.currentSteam = INITIAL_STEAM;
		this.currentEnergySavingMode = INITIAL_ENERGY_SAVING_MODE;
		
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
					iron.currentTemperature != null,
					Iron.class, iron,
					"currentTemperature != null");
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
					INITIAL_TEMPERATURE != null,
					Iron.class, iron,
					"INITIAL_TEMPERATURE != null");
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
	public IronState getState() throws Exception {
		if(VERBOSE)
			this.traceMessage("Iron returns its state : " + this.currentState.toString() + ".\n");
		
		return this.currentState;
	}

	@Override
	public void turnOn() throws Exception {
		if(VERBOSE)
			this.traceMessage("Iron is turning on.\n");
		
		this.currentState = IronState.ON;
	}

	@Override
	public void turnOff() throws Exception {
		if(VERBOSE)
			this.traceMessage("Iron is turning off.\n");
		
		this.currentState = IronState.OFF;
	}

	@Override
	public IronTemperature getTemperature() throws Exception {
		if(VERBOSE)
			this.traceMessage("Iron returns its temperature : " + this.currentTemperature.toString() + ".\n");
		
		return this.currentTemperature;
	}

	@Override
	public void setTemperature(IronTemperature t) throws Exception {
		if(VERBOSE)
			this.traceMessage("Trying setting the temperature.\n");
		
		assert this.currentState == IronState.OFF : 
			new PreconditionException("Impossible to set the iron temperature because it's turning off\n.");
		
		this.currentTemperature = t;
		
		if(VERBOSE)
			this.traceMessage("Iron gets a new temperature : " + this.currentTemperature.toString() + ".\n");
	}

	@Override
	public IronSteam getSteam() throws Exception {
		if(VERBOSE)
			this.traceMessage("Iron returns its steam mode : " + this.currentSteam.toString() + ".\n");
		
		return this.currentSteam;
	}

	@Override
	public void setSteam(IronSteam s) throws Exception {
		if(VERBOSE)
			this.traceMessage("Trying setting the steam mode.\n");
		
		assert this.currentState == IronState.OFF : 
			new PreconditionException("Impossible to set the iron steam mode because it's turning off\n.");
		
		this.currentSteam = s;
		
		if(VERBOSE)
			this.traceMessage("Iron gets a new steam mode : " + this.currentSteam.toString() + ".\n");
	}

	@Override
	public IronEnergySavingMode getEnergySavingMode() throws Exception {
		if(VERBOSE)
			this.traceMessage("Iron returns its energy saving mode : " + this.currentEnergySavingMode.toString() + ".\n");
		
		return this.currentEnergySavingMode;
	}

	@Override
	public void setEnergySavingMode(IronEnergySavingMode e) throws Exception {
		if(VERBOSE)
			this.traceMessage("Trying setting the energy saving mode.\n");
		
		assert this.currentState == IronState.OFF : 
			new PreconditionException("Impossible to set the iron energy saving mode because it's turning off\n.");
		
		this.currentEnergySavingMode = e;
		
		if(VERBOSE)
			this.traceMessage("Iron gets a new energu : " + this.currentEnergySavingMode.toString() + ".\n");
	}
}
