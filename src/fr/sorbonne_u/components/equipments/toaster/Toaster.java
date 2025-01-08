package fr.sorbonne_u.components.equipments.toaster;

import fr.sorbonne_u.components.AbstractComponent;
import fr.sorbonne_u.components.annotations.OfferedInterfaces;
import fr.sorbonne_u.components.annotations.RequiredInterfaces;
import fr.sorbonne_u.components.cyphy.AbstractCyPhyComponent;
import fr.sorbonne_u.components.cyphy.plugins.devs.AtomicSimulatorPlugin;
import fr.sorbonne_u.components.cyphy.plugins.devs.RTAtomicSimulatorPlugin;
import fr.sorbonne_u.components.cyphy.utils.aclocks.AcceleratedAndSimulationClock;
import fr.sorbonne_u.components.cyphy.utils.aclocks.ClocksServerWithSimulationCI;
import fr.sorbonne_u.components.cyphy.utils.aclocks.ClocksServerWithSimulationConnector;
import fr.sorbonne_u.components.cyphy.utils.aclocks.ClocksServerWithSimulationOutboundPort;
import fr.sorbonne_u.components.equipments.iron.mil.LocalSimulationArchitectures;
import fr.sorbonne_u.components.equipments.toaster.mil.LocalSimulationArchitecture;
import fr.sorbonne_u.components.equipments.toaster.mil.ToasterStateModel;
import fr.sorbonne_u.components.equipments.toaster.mil.events.SetToasterBrowningLevel;
import fr.sorbonne_u.components.equipments.toaster.mil.events.TurnOffToaster;
import fr.sorbonne_u.components.equipments.toaster.mil.events.TurnOnToaster;
import fr.sorbonne_u.components.exceptions.BCMException;
import fr.sorbonne_u.components.exceptions.ComponentShutdownException;
import fr.sorbonne_u.components.exceptions.ComponentStartException;
import fr.sorbonne_u.components.utils.ExecutionType;
import fr.sorbonne_u.components.utils.SimulationType;
import fr.sorbonne_u.devs_simulation.architectures.Architecture;
import fr.sorbonne_u.devs_simulation.models.events.EventInformationI;
import fr.sorbonne_u.devs_simulation.models.time.Duration;
import fr.sorbonne_u.devs_simulation.models.time.Time;
import fr.sorbonne_u.exceptions.*;
import fr.sorbonne_u.utils.aclocks.AcceleratedClock;
import fr.sorbonne_u.utils.aclocks.ClocksServer;

import java.util.HashMap;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@OfferedInterfaces(offered = {ToasterUserCI.class})
@RequiredInterfaces(required = {ClocksServerWithSimulationCI.class})
public class Toaster extends AbstractCyPhyComponent implements ToasterImplementationI {

	// -------------------------------------------------------------------------
	// Constants and variables
	// -------------------------------------------------------------------------
	
	public static boolean VERBOSE = true;
	public static int X_RELATIVE_POSITION = 0;
	public static int Y_RELATIVE_POSITION = 2;

	public static final String REFLECTION_INBOUND_PORT_URI = "TOASTER-RIP-URI";
	public static final String INBOUND_PORT_URI = "TOASTER-INBOUND-PORT";

	public static final ToasterStateModel.ToasterState INITIAL_STATE = ToasterStateModel.ToasterState.OFF;
	public static final ToasterStateModel.ToasterBrowningLevel INITIAL_BROWNING_LEVEL = ToasterStateModel.ToasterBrowningLevel.DEFROST;

	protected ToasterStateModel.ToasterState currentState;
	protected ToasterStateModel.ToasterBrowningLevel currentBrowningLevel;
	protected int sliceCount = 0;

	protected final int MAX_SLICE_COUNT = 3;
	
	protected ToasterInboundPort inboundPort;

	// Execution/Simulation
	protected final ExecutionType currentExecutionType;
	protected final SimulationType currentSimulationType;

	protected final String				clockURI;
	protected AtomicSimulatorPlugin asp;
	protected final String				globalArchitectureURI;
	protected final String				localArchitectureURI;
	protected final TimeUnit simulationTimeUnit;
	protected double					accFactor;


	// -------------------------------------------------------------------------
	// Invariant
	// -------------------------------------------------------------------------
	protected static boolean glassBoxInvariants(Toaster t) {
		assert t != null :
			new PreconditionException("Toaster: The toaster is null!");

		boolean ret = true;
		ret &= InvariantChecking.checkGlassBoxInvariant(
				t.currentState != null,
				Toaster.class,
				t,
				"currentState != null");
		ret &= InvariantChecking.checkGlassBoxInvariant(
				t.currentBrowningLevel != null,
				Toaster.class,
				t,
				"currentBrowningLevel != null");
		ret &= InvariantChecking.checkGlassBoxInvariant(
				t.sliceCount >= 0 && t.sliceCount <= t.MAX_SLICE_COUNT,
				Toaster.class,
				t,
				"sliceCount >= 0 && sliceCount <= MAX_SLICE_COUNT");
		ret &= InvariantChecking.checkGlassBoxInvariant(
				t.currentExecutionType != null,
				Toaster.class,
				t,
				"currentExecutionType != null");
		ret &= InvariantChecking.checkGlassBoxInvariant(
				t.currentSimulationType != null,
				Toaster.class,
				t,
				"currentSimulationType != null");
		ret &= InvariantChecking.checkGlassBoxInvariant(
				!t.currentExecutionType.isStandard() || t.currentSimulationType.isNoSimulation(),
				Toaster.class,
				t,
				"!currentExecutionType.isStandard() || currentSimulationType.isNoSimulation()");
		ret &= InvariantChecking.checkGlassBoxInvariant(
				t.currentSimulationType.isNoSimulation() || (t.globalArchitectureURI != null && t.globalArchitectureURI.isEmpty()),
				Toaster.class,
				t,
				"currentSimulationType.isNoSimulation() || (globalArchitectureURI != null && globalArchitectureURI.isEmpty())");
		ret &= InvariantChecking.checkGlassBoxInvariant(
				t.currentSimulationType.isNoSimulation() || (t.localArchitectureURI != null && t.localArchitectureURI.isEmpty()),
				Toaster.class,
				t,
				"currentSimulationType.isNoSimulation() || (localArchitectureURI != null && localArchitectureURI.isEmpty())");
		ret &= InvariantChecking.checkGlassBoxInvariant(
				!t.currentSimulationType.isSimulated() || t.simulationTimeUnit != null,
				Toaster.class,
				t,
				"currentSimulationType.isSimulated() || simulationTimeUnit != null");
		ret &= InvariantChecking.checkGlassBoxInvariant(
				!t.currentSimulationType.isRealTimeSimulation() || t.accFactor > 0.0,
				Toaster.class,
				t,
				"!currentSimulationType.isRealTimeSimulation() || accFactor > 0.0");
		return ret;
	}

	protected static boolean	blackBoxInvariants(Toaster hd)
	{
		assert 	hd != null : new PreconditionException("hd != null");

		boolean ret = true;
		ret &= InvariantChecking.checkBlackBoxInvariant(
				X_RELATIVE_POSITION >= 0,
				Toaster.class, hd,
				"X_RELATIVE_POSITION >= 0");
		ret &= InvariantChecking.checkBlackBoxInvariant(
				Y_RELATIVE_POSITION >= 0,
				Toaster.class, hd,
				"Y_RELATIVE_POSITION >= 0");
		ret &= InvariantChecking.checkBlackBoxInvariant(
				REFLECTION_INBOUND_PORT_URI != null &&
				!REFLECTION_INBOUND_PORT_URI.isEmpty(),
				Toaster.class, hd,
				"REFLECTION_INBOUND_PORT_URI != null && "
				+ "!REFLECTION_INBOUND_PORT_URI.isEmpty()");
		ret &= InvariantChecking.checkBlackBoxInvariant(
				INBOUND_PORT_URI != null && !INBOUND_PORT_URI.isEmpty(),
				Toaster.class, hd,
				"INBOUND_PORT_URI != null && !INBOUND_PORT_URI.isEmpty()");
		ret &= InvariantChecking.checkBlackBoxInvariant(
				INITIAL_STATE != null,
				Toaster.class, hd,
				"INITIAL_STATE != null");
		ret &= InvariantChecking.checkBlackBoxInvariant(
				INITIAL_BROWNING_LEVEL != null,
				Toaster.class, hd,
				"INITIAL_BROWNING_LEVEL != null");
		return ret;
	}

	// -------------------------------------------------------------------------
	// Constructors
	// -------------------------------------------------------------------------
	
	protected Toaster() throws Exception {
		this(INBOUND_PORT_URI);
	}
			
	protected Toaster(String toasterInboundPortURI) throws Exception {
		this(toasterInboundPortURI, ExecutionType.STANDARD);
	}

	protected Toaster(
			String toasterInboundPortURI,
			ExecutionType et
		) throws Exception {
		this(
			REFLECTION_INBOUND_PORT_URI,
			toasterInboundPortURI,
			et,
			SimulationType.NO_SIMULATION,
			null,
			null,
			null,
			0.0,
			null
		);
	}

	protected Toaster(
			String reflectionInboundPortURI,
			String toasterInboundPortURI,
			ExecutionType et,
			SimulationType st,
			String globalArchitectureURI,
			String localArchitectureURI,
			TimeUnit simulationTimeUnit,
			double simulationAccelerationFactor,
			String clockURI
		) throws Exception {
		super(reflectionInboundPortURI,2, 0);
		assert toasterInboundPortURI != null && !toasterInboundPortURI.isEmpty():
			new PreconditionException("Toaster: toasterInboundPortURI != null && !toasterInboundPortURI.isEmpty()");
		assert et != null:
			new PreconditionException("Toaster: et != null");
		assert !et.isStandard() || clockURI != null && !clockURI.isEmpty():
			new PreconditionException("Toaster: !et.isStandard() || clockURI != null && !clockURI.isEmpty()");
		assert !et.isStandard():
			new PreconditionException("Toaster: !et.isStandard()");
		assert st.isNoSimulation() || (globalArchitectureURI != null && !globalArchitectureURI.isEmpty()):
			new PreconditionException("Toaster: st.isNoSimulation() || (globalArchitectureURI != null && !globalArchitectureURI.isEmpty())");
		assert st.isNoSimulation() || (localArchitectureURI != null && !localArchitectureURI.isEmpty()):
			new PreconditionException("Toaster: st.isNoSimulation() || (localArchitectureURI != null && !localArchitectureURI.isEmpty())");
		assert !st.isSimulated() || simulationTimeUnit != null:
			new PreconditionException("Toaster: !st.isSimulated() || simulationTimeUnit != null");
		assert !st.isRealTimeSimulation() || simulationAccelerationFactor > 0.0:
			new PreconditionException("Toaster: !st.isRealTimeSimulation() || simulationAccelerationFactor > 0.0");

		this.currentExecutionType = et;
		this.currentSimulationType = st;
		this.globalArchitectureURI = globalArchitectureURI;
		this.localArchitectureURI = localArchitectureURI;
		this.simulationTimeUnit = simulationTimeUnit;
		this.accFactor = simulationAccelerationFactor;
		this.clockURI = clockURI;

		this.initialise(toasterInboundPortURI);

		assert Toaster.glassBoxInvariants(this):
			new ImplementationInvariantException("Toaster: glassBoxInvariants(this)");
		assert Toaster.blackBoxInvariants(this):
			new InvariantException("Toaster: blackBoxInvariants(this)");
	}
	
	protected void initialise(String inboundPortURI) throws Exception {
		assert inboundPortURI != null :
			new PreconditionException(
								"Toaster: the toaster inbound port uri is null, impossible to initialise");
		assert	!inboundPortURI.isEmpty() :
					new PreconditionException(
								"Toaster: the toaster inbound port uri is empty, impossible to initialise");
		
		this.currentState = INITIAL_STATE;
		this.currentBrowningLevel = INITIAL_BROWNING_LEVEL;
		
		this.inboundPort = new ToasterInboundPort(inboundPortURI, this);
		this.inboundPort.publishPort();
		
		this.sliceCount = 0;

		switch (this.currentSimulationType){
			case MIL_SIMULATION:
				Architecture architecture = null;
				if(this.currentExecutionType.isUnitTest()) {
					architecture = LocalSimulationArchitecture.createToasterMILLocalArchitecture4UnitTest(
						this.localArchitectureURI,
						this.simulationTimeUnit);

				}else {
					assert this.currentExecutionType.isIntegrationTest();
					architecture = LocalSimulationArchitecture.createToasterMILLocalArchitecture4IntegrationTest(
						this.localArchitectureURI,
						this.simulationTimeUnit);
				}

				assert architecture.getRootModelURI().equals(this.localArchitectureURI):
						new BCMException("local simulation architecture" + this.localArchitectureURI + "does not match exist");

				this.addLocalSimulatorArchitecture(architecture);
				this.global2localSimulationArchitectureURIS.put(this.globalArchitectureURI, this.localArchitectureURI);
				break;

			case MIL_RT_SIMULATION:
			case SIL_SIMULATION:
				architecture = null;
				if(this.currentExecutionType.isUnitTest()) {
					architecture = LocalSimulationArchitecture.createToaster_RT_LocalArchitecture4UnitTest(
							this.currentSimulationType,
							this.localArchitectureURI,
							this.simulationTimeUnit,
							this.accFactor);
				} else {
					architecture = LocalSimulationArchitecture.createToaster_RT_LocalArchitecture4IntegrationTest(
							this.currentSimulationType,
							this.localArchitectureURI,
							this.simulationTimeUnit,
							this.accFactor);
				}
				assert architecture.getRootModelURI().equals(this.localArchitectureURI):
						new BCMException("local simulation simulator" + this.localArchitectureURI + "does not match exist");
				this.addLocalSimulatorArchitecture(architecture);
				this.global2localSimulationArchitectureURIS.put(this.globalArchitectureURI, this.localArchitectureURI);
				break;

			case NO_SIMULATION:

			default:
		}
		
		if (VERBOSE) {
			this.tracer.get().setTitle("Toaster component");
			this.tracer.get().setRelativePosition(X_RELATIVE_POSITION,
												  Y_RELATIVE_POSITION);
			this.toggleTracing();
		}
	}
	
	
	// -------------------------------------------------------------------------
	// Component life-cycle
	// -------------------------------------------------------------------------
	@Override
	public synchronized void start() throws ComponentStartException{
		super.start();

		try{
			switch (this.currentSimulationType){
				case MIL_SIMULATION:
					this.asp = new AtomicSimulatorPlugin();
					String uri = this.global2localSimulationArchitectureURIS.get(this.globalArchitectureURI);
					Architecture architecture = (Architecture) this.localSimulationArchitectures.get(uri);
					this.asp.setPluginURI(uri);
					this.asp.setSimulationArchitecture(architecture);
					this.installPlugin(this.asp);
					break;

				case MIL_RT_SIMULATION:
					this.asp = new AtomicSimulatorPlugin();
					uri = this.global2localSimulationArchitectureURIS.get(this.globalArchitectureURI);
					architecture = (Architecture) this.localSimulationArchitectures.get(uri);
					((RTAtomicSimulatorPlugin)this.asp).setPluginURI(uri);
					((RTAtomicSimulatorPlugin)this.asp).setSimulationArchitecture(architecture);
					this.installPlugin(this.asp);
					break;

				case SIL_SIMULATION:
					uri = this.global2localSimulationArchitectureURIS.get(this.globalArchitectureURI);
					architecture = (Architecture) this.localSimulationArchitectures.get(uri);
					((RTAtomicSimulatorPlugin)this.asp).setPluginURI(uri);
					((RTAtomicSimulatorPlugin)this.asp).setSimulationArchitecture(architecture);
					this.installPlugin(this.asp);
					break;

				case NO_SIMULATION:

				default:
			}
		} catch (Exception e) {
			throw new ComponentStartException(e);
		}
	}

	@Override
	public void execute() throws Exception{
		if(!this.currentExecutionType.isStandard()){
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

			if (this.currentExecutionType.isUnitTest() &&
				this.currentSimulationType.isSILSimulation()) {
				this.asp.createSimulator();
				this.asp.setSimulationRunParameters(new HashMap<>());
				this.asp.initialiseSimulation(
						new Time(
								clock.getSimulatedStartTime(),
								clock.getSimulatedTimeUnit()
						),
						new Duration(
								clock.getSimulatedDuration(),
								clock.getSimulatedTimeUnit()
						)
											 );
				// compute the real time of start of the simulation using the
				// accelerated clock
				long simulationStartTimeInMillis =
						TimeUnit.NANOSECONDS.toMillis(
								clock.getSimulationStartEpochNanos());
				assert simulationStartTimeInMillis > System.currentTimeMillis() :
						new BCMException(
								"simulationStartTimeInMillis > "
								+ "System.currentTimeMillis()");
				this.asp.startRTSimulation(
						simulationStartTimeInMillis,
						clock.getSimulatedStartTime(),
						clock.getSimulatedDuration()
										  );
				// rather than waiting and sleeping, a task could also be
				// scheduled to free the current thread
				clock.waitUntilSimulationEnd();
				// leave some time for the simulators to perform end of
				// simulation catering tasks
				Thread.sleep(250L);
				this.logMessage(this.asp.getFinalReport().toString());
			}
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
	public ToasterStateModel.ToasterState getState() throws Exception {
		if (VERBOSE) {
			this.traceMessage("Toaster returns its state : " +
													this.currentState + ".\n");
		}
		
		return this.currentState;
	}

	@Override
	public ToasterStateModel.ToasterBrowningLevel getBrowningLevel() throws Exception {
		if (VERBOSE) 
			this.traceMessage("Toaster returns its browning level : " + this.currentBrowningLevel + ".\n");
		
		return currentBrowningLevel;
	}
	
	@Override
	public int getSliceCount() throws Exception {
		if (VERBOSE)
		 this.traceMessage("Toaster get " + this.sliceCount + "slice of bread(s).\n");

		return this.sliceCount;
	}

	@Override
	public void turnOn() throws Exception {
		if (VERBOSE) 
			this.traceMessage("Trying to turn on the toaster");

		assert this.sliceCount > 0 && sliceCount <= this.MAX_SLICE_COUNT:
			new PreconditionException("Impossible to turn on the toaster because the slice count must be 1, 2 or 3: " + this.sliceCount + "\n.");
		
		this.currentState = ToasterStateModel.ToasterState.ON;
		
		assert this.currentState == ToasterStateModel.ToasterState.ON:
			new PostconditionException("The toaster must be on after turning on \n.");
		
		if (VERBOSE) 
			this.traceMessage("Toaster is turning on \n.");

		if (this.currentSimulationType.isSILSimulation()) {
			// For SIL simulation, an operation done in the component code
			// must be reflected in the simulation; to do so, the component
			// code triggers an external event sent to the ToasterStateModel
			// to make it change its state to on.
			((RTAtomicSimulatorPlugin)this.asp).triggerExternalEvent(
					ToasterStateModel.SIL_URI,
					t -> new TurnOnToaster(t));
		}
	}

	@Override
	public void turnOff() throws Exception {
		assert this.currentState == ToasterStateModel.ToasterState.ON:
			new PreconditionException("Toaster must be turn on for turning off \n.");
		
		this.currentState = ToasterStateModel.ToasterState.OFF;
		if (VERBOSE) 
			this.traceMessage("Toaster is turning off \n.");
		
		assert this.currentState == ToasterStateModel.ToasterState.OFF:
			new PostconditionException("The toaster must be off after turning off \n.");
		
		if (this.currentSimulationType.isSILSimulation()) {
			// For SIL simulation, an operation done in the component code
			// must be reflected in the simulation; to do so, the component
			// code triggers an external event sent to the ToasterStateModel
			// to make it change its state to off.
			((RTAtomicSimulatorPlugin)this.asp).triggerExternalEvent(
					ToasterStateModel.SIL_URI,
					t -> new TurnOffToaster(t));
		}
	}

	@Override
	public void setSliceCount(int sliceCount) throws Exception {
		if (VERBOSE)
			this.traceMessage("Trying to set slice count in Toaster \n.");
		
		assert this.currentState == ToasterStateModel.ToasterState.OFF :
			new PreconditionException("Toaster must be turn off for setting slice count \n.");
		assert sliceCount <= 3 && sliceCount >= 0 :
			new PreconditionException("Toaster can contain between 0 et 3 slice of bread \n.");
		
		this.sliceCount = sliceCount;
		
		if (VERBOSE)
			this.traceMessage("The toaster get " + this.sliceCount + " slice of bread \n.");
	}

	@Override
	public void setBrowningLevel(ToasterStateModel.ToasterBrowningLevel bl) throws Exception {
		if (VERBOSE) 
			this.traceMessage("Trying to set toaster browning level \n.");
		
		assert this.currentState == ToasterStateModel.ToasterState.OFF :
			new PreconditionException("Toaster must be turn off for setting browning level \n."); 
		
		this.currentBrowningLevel = bl;
		
		if (VERBOSE)
			this.traceMessage("The toaster is setting on " + this.currentBrowningLevel + "\n.");
		
		if (this.currentSimulationType.isSILSimulation()) {
			// For SIL simulation, an operation done in the component code
			// must be reflected in the simulation; to do so, the component
			// code triggers an external event sent to the ToasterStateModel
			// to make it change its state to off.
			SetToasterBrowningLevel.BrowningLevelValue browningLevelToPass = null;
			switch (bl) {
				case DEFROST:
					browningLevelToPass = new SetToasterBrowningLevel.BrowningLevelValue(ToasterStateModel.ToasterBrowningLevel.DEFROST);
					break;
				case LOW:
					browningLevelToPass = new SetToasterBrowningLevel.BrowningLevelValue(ToasterStateModel.ToasterBrowningLevel.LOW);
					break;
				case MEDIUM:
					browningLevelToPass = new SetToasterBrowningLevel.BrowningLevelValue(ToasterStateModel.ToasterBrowningLevel.MEDIUM);
					break;
				case HIGH:
					browningLevelToPass = new SetToasterBrowningLevel.BrowningLevelValue(ToasterStateModel.ToasterBrowningLevel.HIGH);
					break;
			}
			final EventInformationI browningLevel = browningLevelToPass;
			assert browningLevel != null:
					new PreconditionException("browningLevel != null");
			((RTAtomicSimulatorPlugin)this.asp).triggerExternalEvent(
					ToasterStateModel.SIL_URI,
					t -> new SetToasterBrowningLevel(t, browningLevel));
		}
	}

}
