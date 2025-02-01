package fr.sorbonne_u.components.equipments.generator;

import fr.sorbonne_u.components.annotations.OfferedInterfaces;
import fr.sorbonne_u.components.annotations.RequiredInterfaces;
import fr.sorbonne_u.components.cyphy.AbstractCyPhyComponent;
import fr.sorbonne_u.components.cyphy.plugins.devs.AtomicSimulatorPlugin;
import fr.sorbonne_u.components.cyphy.plugins.devs.RTAtomicSimulatorPlugin;
import fr.sorbonne_u.components.cyphy.utils.aclocks.AcceleratedAndSimulationClock;
import fr.sorbonne_u.components.cyphy.utils.aclocks.ClocksServerWithSimulationCI;
import fr.sorbonne_u.components.cyphy.utils.aclocks.ClocksServerWithSimulationConnector;
import fr.sorbonne_u.components.cyphy.utils.aclocks.ClocksServerWithSimulationOutboundPort;
import fr.sorbonne_u.components.equipments.generator.interfaces.GeneratorCI;
import fr.sorbonne_u.components.equipments.generator.interfaces.GeneratorImplementationI;
import fr.sorbonne_u.components.equipments.generator.mil.GeneratorStateModel;
import fr.sorbonne_u.components.equipments.generator.mil.events.ActivateGeneratorEvent;
import fr.sorbonne_u.components.equipments.generator.mil.events.StopGeneratorEvent;
import fr.sorbonne_u.components.equipments.generator.ports.GeneratorInboundPort;
import fr.sorbonne_u.components.equipments.generator.mil.LocalSimulationArchitectures;
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
import fr.sorbonne_u.utils.aclocks.ClocksServer;


import java.util.HashMap;
import java.util.concurrent.TimeUnit;

@OfferedInterfaces(offered = {GeneratorCI.class})
@RequiredInterfaces(required = {ClocksServerWithSimulationCI.class})
public class Generator extends AbstractCyPhyComponent implements GeneratorImplementationI {

	public static boolean VERBOSE = true;
	public static int X_RELATIVE_POSITION = 0;
	public static int Y_RELATIVE_POSITION = 4;

	public static final String REFLECTION_INBOUND_PORT_URI = "GENERATOR-REFLECTION-INBOUND-PORT-URI";
	public static final String INBOUND_PORT_URI = "GENERATOR-INBOUND-PORT-URI";
	protected GeneratorInboundPort inboundPort;
	
	protected boolean isStart;
	
	protected static final double MAX_CAPACITY = 200.0;
	protected double fuelLevel = 0.0;
	
	protected static final double DEFAULT_PRODUCTION = 100.0;
	protected double currentProduction = 0.0;

	// Execution/Simulation

	/** current type of execution.											*/
	protected final ExecutionType currentExecutionType;
	/** current type of simulation.											*/
	protected final SimulationType currentSimulationType;

	/** URI of the clock to be used to synchronise the test scenarios and
	 *  the simulation.														*/
	protected final String				clockURI;
	/** plug-in holding the local simulation architecture and simulators.	*/
	protected AtomicSimulatorPlugin asp;
	/** URI of the global simulation architecture to be created or the
	 *  empty string if the component does not execute as a simulation.		*/
	protected final String				globalArchitectureURI;
	/** URI of the local simulation architecture used to compose the global
	 *  simulation architecture or the empty string if the component does
	 *  not execute as a simulation.										*/
	protected final String				localArchitectureURI;
	/** time unit in which {@code simulatedStartTime} and
	 *  {@code simulationDuration} are expressed.							*/
	protected final TimeUnit simulationTimeUnit;
	/** acceleration factor to be used when running the real time
	 *  simulation.															*/
	protected double					accFactor;

	// -------------------------------------------------------------------------
	//Invariant
	// -------------------------------------------------------------------------

	protected static boolean glassBoxInvariants(Generator instance) {
		assert instance != null : new PreconditionException("instance != null");

		boolean ret = true;
		ret &= InvariantChecking.checkGlassBoxInvariant(
				instance.currentExecutionType != null,
				Generator.class,
				instance,
				"instance.currentExecutionType != null");
		ret &= InvariantChecking.checkGlassBoxInvariant(
				instance.currentSimulationType != null,
				Generator.class,
				instance,
				"instance.currentSimulationType != null");
		ret &= InvariantChecking.checkGlassBoxInvariant(
				!instance.currentExecutionType.isStandard() ||
				instance.currentSimulationType.isNoSimulation(),
				Generator.class, instance,
				"!currentExecutionType.isStandard() || "
				+ "currentSimulationType.isNoSimulation()");
		ret &= InvariantChecking.checkGlassBoxInvariant(
				instance.currentSimulationType.isNoSimulation() ||
				(instance.globalArchitectureURI != null &&
				 !instance.globalArchitectureURI.isEmpty()),
				Generator.class, instance,
				"currentSimulationType.isNoSimulation() || "
				+ "(globalArchitectureURI != null && "
				+ "!globalArchitectureURI.isEmpty())");
		ret &= InvariantChecking.checkGlassBoxInvariant(
				instance.currentSimulationType.isNoSimulation() ||
				(instance.localArchitectureURI != null &&
				 !instance.localArchitectureURI.isEmpty()),
				Generator.class, instance,
				"currentSimulationType.isNoSimulation() || "
				+ "(localArchitectureURI != null && "
				+ "!localArchitectureURI.isEmpty())");
		ret &= InvariantChecking.checkGlassBoxInvariant(
				!instance.currentSimulationType.isSimulated() ||
				instance.simulationTimeUnit != null,
				Generator.class, instance,
				"!currentSimulationType.isSimulated() || "
				+ "simulationTimeUnit != null");
		ret &= InvariantChecking.checkGlassBoxInvariant(
				!instance.currentSimulationType.isRealTimeSimulation() ||
				instance.accFactor > 0.0,
				Generator.class, instance,
				"!instance.currentSimulationType.isRealTimeSimulation() || "
				+ "instance.accFactor > 0.0");				
		return ret;
	}

	protected static boolean blackBoxInvariants(Generator instance) {
		assert instance != null : new PreconditionException("instance != null");

		boolean ret = true;
		ret &= InvariantChecking.checkBlackBoxInvariant(
				X_RELATIVE_POSITION >= 0,
				Generator.class, instance,
				"X_RELATIVE_POSITION >= 0");
		ret &= InvariantChecking.checkBlackBoxInvariant(
				Y_RELATIVE_POSITION >= 0,
				Generator.class, instance,
				"Y_RELATIVE_POSITION >= 0");
		ret &= InvariantChecking.checkBlackBoxInvariant(
				REFLECTION_INBOUND_PORT_URI != null &&
				!REFLECTION_INBOUND_PORT_URI.isEmpty(),
				Generator.class, instance,
				"REFLECTION_INBOUND_PORT_URI != null && "
				+ "!REFLECTION_INBOUND_PORT_URI.isEmpty()");
		ret &= InvariantChecking.checkBlackBoxInvariant(
				INBOUND_PORT_URI != null && !INBOUND_PORT_URI.isEmpty(),
				Generator.class, instance,
				"INBOUND_PORT_URI != null && !INBOUND_PORT_URI.isEmpty()");
		return ret;
	}

	// -------------------------------------------------------------------------
	// Constructors
	// -------------------------------------------------------------------------

	protected Generator () throws Exception {
		this(INBOUND_PORT_URI);
	}

	protected Generator(String inboundPortURI) throws Exception {
		this(inboundPortURI, ExecutionType.STANDARD);
	}

	protected Generator(
			String inboundPortURI,
			ExecutionType executionType
					   ) throws Exception {
		this(REFLECTION_INBOUND_PORT_URI, inboundPortURI,
			 executionType, SimulationType.NO_SIMULATION,
			 null, null, null, 0.0, null);

		assert	executionType.isTest() :
				new PreconditionException("executionType.isTest()");
	}

	protected			Generator(
			String reflectionInboundPortURI,
			String inboundPortURI,
			ExecutionType currentExecutionType,
			SimulationType currentSimulationType,
			String globalArchitectureURI,
			String localArchitectureURI,
			TimeUnit simulationTimeUnit,
			double accFactor,
			String clockURI
								  ) throws Exception{
		super(reflectionInboundPortURI, 2, 0);

		assert	inboundPortURI != null &&
				  !inboundPortURI.isEmpty() :
				new PreconditionException(
						"inboundPortURI != null && "
						+ "!inboundPortURI.isEmpty()");
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
						"currentSimulationType.isNoSimulation() || "
						+ "(globalArchitectureURI != null && "
						+ "!globalArchitectureURI.isEmpty())");
		assert	currentSimulationType.isNoSimulation() ||
				  (localArchitectureURI != null &&
				   !localArchitectureURI.isEmpty()) :
				new PreconditionException(
						"currentSimulationType.isNoSimulation() || "
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
						"!currentSimulationType.isRealTimeSimulation() || "
						+ "accFactor > 0.0");

		this.currentExecutionType = currentExecutionType;
		this.currentSimulationType = currentSimulationType;
		this.globalArchitectureURI = globalArchitectureURI;
		this.localArchitectureURI = localArchitectureURI;
		this.simulationTimeUnit = simulationTimeUnit;
		this.accFactor = accFactor;
		this.clockURI = clockURI;

		this.initialise(inboundPortURI);

		assert	glassBoxInvariants(this) :
				new ImplementationInvariantException("glassBoxInvariants(this)");
		assert	blackBoxInvariants(this) :
				new InvariantException("blackBoxInvariants(this)");
	}

	protected void initialise(String inboundPortURI) throws Exception {

		assert inboundPortURI != null && !inboundPortURI.isEmpty() :
			new PreconditionException("inboundPortURI != null && !inboundPortURI.isEmpty()");

		this.isStart = false;
		this.fuelLevel = 0.0;
		this.currentProduction = 0.0;

		this.inboundPort = new GeneratorInboundPort(inboundPortURI, this);
		this.inboundPort.publishPort();

		switch (this.currentSimulationType) {
			case MIL_SIMULATION:
				Architecture architecture = null;
				if (this.currentExecutionType.isUnitTest()) {
					architecture =
							LocalSimulationArchitectures.
									createGeneratorMILLocalArchitecture4UnitTest(
											this.localArchitectureURI,
											this.simulationTimeUnit);
				} else {
					assert	this.currentExecutionType.isIntegrationTest();
					architecture =
							LocalSimulationArchitectures.
									createGeneratorMILArchitecture4IntegrationTest(
											this.localArchitectureURI,
											this.simulationTimeUnit);
				}
				assert	architecture.getRootModelURI().equals(
						this.localArchitectureURI) :
						new BCMException(
								"local simulation architecture "
								+ this.localArchitectureURI
								+ " does not exist!");
				this.addLocalSimulatorArchitecture(architecture);
				this.global2localSimulationArchitectureURIS.
						put(this.globalArchitectureURI, this.localArchitectureURI);
				break;
			case MIL_RT_SIMULATION:
				// in MIL RT simulations, the HairDryer component uses the same
				// simulators as in SIL simulations
			case SIL_SIMULATION:
				architecture = null;
				if (this.currentExecutionType.isUnitTest()) {
					architecture =
							LocalSimulationArchitectures.
									createGeneratorMIL_RT_Architecture4UnitTest(
											this.localArchitectureURI,
											this.simulationTimeUnit,
											this.accFactor);
				} else {
					assert	this.currentExecutionType.isIntegrationTest();
					architecture =
							LocalSimulationArchitectures.
									createGeneratorMIL_RT_Architecture4IntegrationTest(
											this.localArchitectureURI,
											this.simulationTimeUnit,
											this.accFactor);

				}
				assert	architecture.getRootModelURI().equals(
						this.localArchitectureURI) :
						new BCMException(
								"local simulation architecture "
								+ this.localArchitectureURI
								+ " does not exist!");
				this.addLocalSimulatorArchitecture(architecture);
				this.global2localSimulationArchitectureURIS.
						put(this.globalArchitectureURI, this.localArchitectureURI);
				break;
			default:
		}

		if(VERBOSE){
			this.tracer.get().setTitle("Generator component");
			this.tracer.get().setRelativePosition(X_RELATIVE_POSITION, Y_RELATIVE_POSITION);
			this.toggleTracing();
		}
	}

	// -------------------------------------------------------------------------
	// Component life-cycle
	// -------------------------------------------------------------------------

	@Override
	public synchronized void	start() throws ComponentStartException
	{
		super.start();

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
					architecture =
							(Architecture) this.localSimulationArchitectures.get(uri);
					((RTAtomicSimulatorPlugin)this.asp).setPluginURI(uri);
					((RTAtomicSimulatorPlugin)this.asp).
							setSimulationArchitecture(architecture);
					this.installPlugin(this.asp);
					break;
				case SIL_SIMULATION:
					// for the HairDryer, real time MIL and SIL use the same
					// simulation models
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
				case NO_SIMULATION:
				default:
			}
		} catch (Exception e) {
			throw new ComponentStartException(e) ;
		}
	}

	@Override
	public void			execute() throws Exception
	{
		if (this.currentExecutionType.isUnitTest() &&
			this.currentSimulationType.isSILSimulation()) {
			ClocksServerWithSimulationOutboundPort clocksServerOutboundPort =
					new ClocksServerWithSimulationOutboundPort(this);
			clocksServerOutboundPort.publishPort();
			this.doPortConnection(
					clocksServerOutboundPort.getPortURI(),
					ClocksServer.STANDARD_INBOUNDPORT_URI,
					ClocksServerWithSimulationConnector.class.getCanonicalName());
			this.logMessage("HairDryer gets the clock.");
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
			// schedule the start of the SIL (real time) simulation
			this.asp.startRTSimulation(
					TimeUnit.NANOSECONDS.toMillis(
							acceleratedClock.getSimulationStartEpochNanos()),
					acceleratedClock.getSimulatedStartTime(),
					acceleratedClock.getSimulatedDuration());
			// wait until the simulation ends
			acceleratedClock.waitUntilSimulationEnd();
			// give some time for the end of simulation catering tasks
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
	public boolean isRunning() throws Exception {
		if(VERBOSE) 
            logMessage("Generator get running : " + this.isStart + "\n");

        return this.isStart;
	}

	@Override
	public void stop() throws Exception {
		if(VERBOSE) 
            logMessage("Generator stop running\n");

        this.isStart = false;

		if (this.currentSimulationType.isSILSimulation()) {
			// For SIL simulation, an operation done in the component code
			// must be reflected in the simulation; to do so, the component
			// code triggers an external event sent to the HairDryerStateModel
			// to make it change its state to on.
			((RTAtomicSimulatorPlugin)this.asp).triggerExternalEvent(
					GeneratorStateModel.SIL_URI,
                    StopGeneratorEvent::new
                                                                    );
		}
	}
	
	@Override
	public void activate() throws Exception {
		if(VERBOSE) 
            logMessage("Generator start running\n");

        this.isStart = true;

		if (this.currentSimulationType.isSILSimulation()) {
			// For SIL simulation, an operation done in the component code
			// must be reflected in the simulation; to do so, the component
			// code triggers an external event sent to the HairDryerStateModel
			// to make it change its state to on.
			((RTAtomicSimulatorPlugin)this.asp).triggerExternalEvent(
					GeneratorStateModel.SIL_URI,
                    ActivateGeneratorEvent::new
                                                                    );
		}
	}

	@Override
	public double getEnergyProduction() throws Exception {
		if(VERBOSE) 
            logMessage("Generator get production : " + DEFAULT_PRODUCTION + "\n");
        
        assert this.isStart : new PreconditionException("getEnergyProduction() -> isStart");

        return DEFAULT_PRODUCTION;
	}

	@Override
	public double getFuelLevel() throws Exception {
		if(VERBOSE) 
            logMessage("Generator get fuel level : " + this.fuelLevel + "\n");
        
        return this.fuelLevel;
	}

	@Override
	public void fill(double quantity) throws Exception {
		if(VERBOSE) 
            logMessage("Generator refill the tank " + quantity);

        assert !this.isStart : new PreconditionException("refill() -> !this.isStart");
        assert quantity > 0 : new PreconditionException("quantity > 0");
        assert this.fuelLevel + quantity <= MAX_CAPACITY : 
        	new PreconditionException("this.fuelLevel + quantity <= MAX_CAPACITY");

        fuelLevel += quantity;
        
        if(VERBOSE) 
            logMessage("New level fuel -> " + this.fuelLevel + "\n");
	}

}
