package fr.sorbonne_u.components.equipments.fridge.sil;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import fr.sorbonne_u.components.CoordinatorComponent;
import fr.sorbonne_u.components.GlobalCoupledModel;
import fr.sorbonne_u.components.annotations.RequiredInterfaces;
import fr.sorbonne_u.components.cyphy.AbstractCyPhyComponent;
import fr.sorbonne_u.components.cyphy.plugins.devs.CoordinatorPlugin;
import fr.sorbonne_u.components.cyphy.plugins.devs.SupervisorPlugin;
import fr.sorbonne_u.components.cyphy.plugins.devs.architectures.ComponentAtomicModelDescriptor;
import fr.sorbonne_u.components.cyphy.plugins.devs.architectures.ComponentCoupledModelDescriptor;
import fr.sorbonne_u.components.cyphy.plugins.devs.architectures.ComponentModelArchitecture;
import fr.sorbonne_u.components.cyphy.plugins.devs.architectures.RTComponentAtomicModelDescriptor;
import fr.sorbonne_u.components.cyphy.plugins.devs.architectures.RTComponentCoupledModelDescriptor;
import fr.sorbonne_u.components.cyphy.plugins.devs.architectures.RTComponentModelArchitecture;
import fr.sorbonne_u.components.cyphy.utils.aclocks.AcceleratedAndSimulationClock;
import fr.sorbonne_u.components.cyphy.utils.aclocks.ClocksServerWithSimulationCI;
import fr.sorbonne_u.components.cyphy.utils.aclocks.ClocksServerWithSimulationConnector;
import fr.sorbonne_u.components.cyphy.utils.aclocks.ClocksServerWithSimulationOutboundPort;
import fr.sorbonne_u.components.equipments.fridge.CVM_FridgeUnitTest;
import fr.sorbonne_u.components.equipments.fridge.Fridge;
import fr.sorbonne_u.components.equipments.fridge.FridgeUser;
import fr.sorbonne_u.components.equipments.fridge.mil.FridgeCoupledModel;
import fr.sorbonne_u.components.equipments.fridge.mil.FridgeUnitTestModel;
import fr.sorbonne_u.components.equipments.fridge.mil.events.CloseDoorFridge;
import fr.sorbonne_u.components.equipments.fridge.mil.events.CoolFridge;
import fr.sorbonne_u.components.equipments.fridge.mil.events.DoNotCoolFridge;
import fr.sorbonne_u.components.equipments.fridge.mil.events.OpenDoorFridge;
import fr.sorbonne_u.components.equipments.fridge.mil.events.SetPowerFridge;
import fr.sorbonne_u.components.equipments.fridge.mil.events.SwitchOffFridge;
import fr.sorbonne_u.components.equipments.fridge.mil.events.SwitchOnFridge;
import fr.sorbonne_u.components.exceptions.BCMException;
import fr.sorbonne_u.components.utils.SimulationType;
import fr.sorbonne_u.devs_simulation.models.architectures.AbstractAtomicModelDescriptor;
import fr.sorbonne_u.devs_simulation.models.architectures.CoupledModelDescriptor;
import fr.sorbonne_u.devs_simulation.models.events.EventI;
import fr.sorbonne_u.devs_simulation.models.events.EventSink;
import fr.sorbonne_u.devs_simulation.models.events.EventSource;
import fr.sorbonne_u.exceptions.ImplementationInvariantException;
import fr.sorbonne_u.exceptions.InvariantChecking;
import fr.sorbonne_u.exceptions.InvariantException;
import fr.sorbonne_u.exceptions.PreconditionException;
import fr.sorbonne_u.utils.aclocks.ClocksServer;

@RequiredInterfaces(required = {ClocksServerWithSimulationCI.class})
public class FridgeUnitTestsSupervisor extends AbstractCyPhyComponent {

	// -------------------------------------------------------------------------
	// Constants and variables
	// -------------------------------------------------------------------------
													
	public static final String MIL_ARCHITECTURE_URI = "fridge-mil-simulator";
	public static final String MIL_RT_ARCHITECTURE_URI = "fridge-mil-rt-simulator";
	public static final String SIL_ARCHITECTURE_URI = "fridge-sil-simulator";

	public static boolean VERBOSE = true;
	public static int X_RELATIVE_POSITION = 0;
	public static int Y_RELATIVE_POSITION = 0;

	// Execution/Simulation
	protected final SimulationType	currentSimulationType;
	protected final String			simArchitectureURI;
	
	
	// -------------------------------------------------------------------------
	// Invariants
	// -------------------------------------------------------------------------

	protected static boolean glassBoxInvariants(FridgeUnitTestsSupervisor instance) {
		assert instance != null : new PreconditionException("instance != null");

		boolean ret = true;
		ret &= InvariantChecking.checkGlassBoxInvariant(
					MIL_ARCHITECTURE_URI != null && 
											!MIL_ARCHITECTURE_URI.isEmpty(),
					FridgeUnitTestsSupervisor.class, instance,
					"MIL_ARCHITECTURE_URI != null && "
					+ "!MIL_ARCHITECTURE_URI.isEmpty()");
		ret &= InvariantChecking.checkGlassBoxInvariant(
					MIL_RT_ARCHITECTURE_URI != null && 
											!MIL_RT_ARCHITECTURE_URI.isEmpty(),
					FridgeUnitTestsSupervisor.class, instance,
					"MIL_RT_ARCHITECTURE_URI != null && "
					+ "!MIL_RT_ARCHITECTURE_URI.isEmpty()");
		ret &= InvariantChecking.checkGlassBoxInvariant(
					SIL_ARCHITECTURE_URI != null && 
											!SIL_ARCHITECTURE_URI.isEmpty(),
					FridgeUnitTestsSupervisor.class, instance,
					"SIL_ARCHITECTURE_URI != null && "
					+ "!SIL_ARCHITECTURE_URI.isEmpty()");
		ret &= InvariantChecking.checkGlassBoxInvariant(
					X_RELATIVE_POSITION >= 0,
					FridgeUnitTestsSupervisor.class, instance,
					"X_RELATIVE_POSITION >= 0");
		ret &= InvariantChecking.checkGlassBoxInvariant(
					Y_RELATIVE_POSITION >= 0,
					FridgeUnitTestsSupervisor.class, instance,
					"Y_RELATIVE_POSITION >= 0");
		
		return ret;
	}

	protected static boolean blackBoxInvariants(FridgeUnitTestsSupervisor instance) {
		assert instance != null : new PreconditionException("instance != null");

		boolean ret = true;
		return ret;
	}
	
	
	// -------------------------------------------------------------------------
	// Constructors
	// -------------------------------------------------------------------------

	protected FridgeUnitTestsSupervisor(SimulationType currentSimulationType, String simArchitectureURI) {
		super(2, 1);
		
		assert	currentSimulationType != null :
				new PreconditionException("currentExecutionType != null");
		assert	!currentSimulationType.isSimulated() || 
										(simArchitectureURI != null &&
												!simArchitectureURI.isEmpty()) :
				new PreconditionException(
					"!currentExecutionType.isSimulated() ||  "
					+ "(simArchitectureURI != null && "
					+ "!simArchitectureURI.isEmpty())");

		this.currentSimulationType = currentSimulationType;
		this.simArchitectureURI = simArchitectureURI;

		this.tracer.get().setTitle("Fridge unit test supervisor");
		this.tracer.get().setRelativePosition(X_RELATIVE_POSITION,
											  Y_RELATIVE_POSITION);
		this.toggleTracing();

		assert	FridgeUnitTestsSupervisor.glassBoxInvariants(this) :
				new ImplementationInvariantException(
						"FridgeUnitTestsSupervisor.glassBoxInvariants(this)");
		assert	FridgeUnitTestsSupervisor.blackBoxInvariants(this) :
				new InvariantException(
						"FridgeUnitTestsSupervisor.blackBoxInvariants(this)");
	}
	
	
	// -------------------------------------------------------------------------
	// Component life-cycle
	// -------------------------------------------------------------------------

	@Override
	public void execute() throws Exception {
		ClocksServerWithSimulationOutboundPort clocksServerOutboundPort =
				new ClocksServerWithSimulationOutboundPort(this);
		clocksServerOutboundPort.publishPort();
		this.doPortConnection(
				clocksServerOutboundPort.getPortURI(),
				ClocksServer.STANDARD_INBOUNDPORT_URI,
				ClocksServerWithSimulationConnector.class.getCanonicalName());
		this.logMessage("FridgeUnitTestsSupervisor gets the clock.");
		AcceleratedAndSimulationClock acceleratedClock =
				clocksServerOutboundPort.getClockWithSimulation(
												CVM_FridgeUnitTest.CLOCK_URI);
		this.doPortDisconnection(clocksServerOutboundPort.getPortURI());
		clocksServerOutboundPort.unpublishPort();
		clocksServerOutboundPort.destroyPort();

		long simulationStartTimeInMillis = 
				TimeUnit.NANOSECONDS.toMillis(
							acceleratedClock.getSimulationStartEpochNanos());
		
		this.logMessage("FridgeUnitTestsSupervisor waits until start time.");
		acceleratedClock.waitUntilStart();
		this.logMessage("FridgeUnitTestsSupervisor starts.");

		switch (this.currentSimulationType) {
		case MIL_SIMULATION:
			ComponentModelArchitecture cma =
					createMILComponentSimulationArchitectures(
										this.simArchitectureURI,
										acceleratedClock.getSimulatedTimeUnit());
			SupervisorPlugin sp = new SupervisorPlugin(cma);
			sp.setPluginURI(FridgeUnitTestsSupervisor.MIL_ARCHITECTURE_URI);
			this.installPlugin(sp);
			this.logMessage("plug-in installed.");
			sp.constructSimulator();
			this.logMessage("simulator constructed.");
			sp.setSimulationRunParameters(new HashMap<>());
			acceleratedClock.waitUntilSimulationStart();
			sp.doStandAloneSimulation(0.0,
									  acceleratedClock.getSimulatedDuration());
			this.logMessage(sp.getFinalReport().toString());
			this.logMessage("simulation ends.");				
			break;
		case MIL_RT_SIMULATION:
			cma = createMILRTComponentSimulationArchitectures(
									this.simArchitectureURI,
									acceleratedClock.getSimulatedTimeUnit(),
									acceleratedClock.getAccelerationFactor());
			sp = new SupervisorPlugin(cma);
			sp.setPluginURI(FridgeUnitTestsSupervisor.MIL_ARCHITECTURE_URI);
			this.installPlugin(sp);
			this.logMessage("plug-in installed.");
			sp.constructSimulator();
			this.logMessage("simulator constructed, simulation begins.");
			sp.setSimulationRunParameters(new HashMap<>());
			assert	simulationStartTimeInMillis > System.currentTimeMillis() :
					new BCMException(
							"simulationStartTimeInMillis > "
							+ "System.currentTimeMillis()");
			sp.startRTSimulation(simulationStartTimeInMillis,
								 acceleratedClock.getSimulatedStartTime(),
								 acceleratedClock.getSimulatedDuration());
			acceleratedClock.waitUntilSimulationEnd();
			Thread.sleep(250L);
			this.logMessage(sp.getFinalReport().toString());
			this.logMessage("simulation ends.");				
			break;
		case SIL_SIMULATION:
		default:
		}		
	}
	
	
	@SuppressWarnings("unchecked")
	public static ComponentModelArchitecture 
							createMILComponentSimulationArchitectures(
									String architectureURI, 
									TimeUnit simulatedTimeUnit
									) throws Exception
	{
		Map<String,AbstractAtomicModelDescriptor> atomicModelDescriptors =
															new HashMap<>();

		atomicModelDescriptors.put(
				FridgeCoupledModel.MIL_URI,
				ComponentAtomicModelDescriptor.create(
						FridgeCoupledModel.MIL_URI,
						(Class<? extends EventI>[]) new Class<?>[]{
							SwitchOnFridge.class,
							SwitchOffFridge.class,
							CloseDoorFridge.class,
							OpenDoorFridge.class,
							DoNotCoolFridge.class,
							SetPowerFridge.class,
							CoolFridge.class},
						(Class<? extends EventI>[]) new Class<?>[]{},
						simulatedTimeUnit,
						Fridge.REFLECTION_INBOUND_PORT_URI
						));
		atomicModelDescriptors.put(
				FridgeUnitTestModel.MIL_URI,
				ComponentAtomicModelDescriptor.create(
						FridgeUnitTestModel.MIL_URI,
						(Class<? extends EventI>[]) new Class<?>[]{},
						(Class<? extends EventI>[]) new Class<?>[]{
							SwitchOnFridge.class,
							SwitchOffFridge.class,
							CloseDoorFridge.class,
							OpenDoorFridge.class,
							DoNotCoolFridge.class,
							SetPowerFridge.class,
							CoolFridge.class},
						simulatedTimeUnit,
						FridgeUser.REFLECTION_INBOUND_PORT_URI));

		Map<String, CoupledModelDescriptor> coupledModelDescriptors = new HashMap<>();

		Set<String> submodels = new HashSet<String>();
		submodels.add(FridgeCoupledModel.MIL_URI);
		submodels.add(FridgeUnitTestModel.MIL_URI);

		Map<EventSource,EventSink[]> connections =
									new HashMap<EventSource,EventSink[]>();
		connections.put(
			new EventSource(FridgeUnitTestModel.MIL_URI,
							SwitchOnFridge.class),
			new EventSink[] {
				new EventSink(FridgeCoupledModel.MIL_URI,
							  SwitchOnFridge.class)
			});
		connections.put(
			new EventSource(FridgeUnitTestModel.MIL_URI,
							SwitchOffFridge.class),
			new EventSink[] {
				new EventSink(FridgeCoupledModel.MIL_URI,
							  SwitchOffFridge.class)
			});
		connections.put(
			new EventSource(FridgeUnitTestModel.MIL_URI,
							SetPowerFridge.class),
				new EventSink[] {
				new EventSink(FridgeCoupledModel.MIL_URI,
							  SetPowerFridge.class)
			});
		connections.put(
			new EventSource(FridgeUnitTestModel.MIL_URI, CoolFridge.class),
			new EventSink[] {
				new EventSink(FridgeCoupledModel.MIL_URI, CoolFridge.class)
			});
		connections.put(
			new EventSource(FridgeUnitTestModel.MIL_URI, DoNotCoolFridge.class),
			new EventSink[] {
				new EventSink(FridgeCoupledModel.MIL_URI, DoNotCoolFridge.class)
			});
		connections.put(
			new EventSource(FridgeUnitTestModel.MIL_URI, OpenDoorFridge.class),
			new EventSink[] {
				new EventSink(FridgeCoupledModel.MIL_URI, OpenDoorFridge.class)
			});
		connections.put(
			new EventSource(FridgeUnitTestModel.MIL_URI, CloseDoorFridge.class),
			new EventSink[] {
				new EventSink(FridgeCoupledModel.MIL_URI, CloseDoorFridge.class)
			});

		// coupled model descriptor
		coupledModelDescriptors.put(
				GlobalCoupledModel.MIL_URI,
				ComponentCoupledModelDescriptor.create(
						GlobalCoupledModel.class,
						GlobalCoupledModel.MIL_URI,
						submodels,
						null,
						null,
						connections,
						null,
						CoordinatorComponent.REFLECTION_INBOUND_PORT_URI,
						CoordinatorPlugin.class,
						null));

		ComponentModelArchitecture architecture =
				new ComponentModelArchitecture(
						architectureURI,
						GlobalCoupledModel.MIL_URI,
						atomicModelDescriptors,
						coupledModelDescriptors,
						simulatedTimeUnit);

		return architecture;
	}
	
	@SuppressWarnings("unchecked")
	public static ComponentModelArchitecture
									createMILRTComponentSimulationArchitectures(
		String architectureURI, 
		TimeUnit simulatedTimeUnit,
		double accelerationFactor
		) throws Exception
	{
		Map<String,AbstractAtomicModelDescriptor> atomicModelDescriptors =
															new HashMap<>();

		atomicModelDescriptors.put(
				FridgeCoupledModel.MIL_RT_URI,
				RTComponentAtomicModelDescriptor.create(
						FridgeCoupledModel.MIL_RT_URI,
						(Class<? extends EventI>[]) new Class<?>[]{
							SwitchOnFridge.class,
							SwitchOffFridge.class,
							CloseDoorFridge.class,
							OpenDoorFridge.class,
							DoNotCoolFridge.class,
							SetPowerFridge.class,
							CoolFridge.class},
						(Class<? extends EventI>[]) new Class<?>[]{},
						simulatedTimeUnit,
						Fridge.REFLECTION_INBOUND_PORT_URI
						));
		atomicModelDescriptors.put(
				FridgeUnitTestModel.MIL_RT_URI,
				RTComponentAtomicModelDescriptor.create(
						FridgeUnitTestModel.MIL_RT_URI,
						(Class<? extends EventI>[]) new Class<?>[]{},
						(Class<? extends EventI>[]) new Class<?>[]{
							SwitchOnFridge.class,
							SwitchOffFridge.class,
							CloseDoorFridge.class,
							OpenDoorFridge.class,
							DoNotCoolFridge.class,
							SetPowerFridge.class,
							CoolFridge.class},
						simulatedTimeUnit,
						FridgeUser.REFLECTION_INBOUND_PORT_URI));

		Map<String,CoupledModelDescriptor> coupledModelDescriptors =
															new HashMap<>();

		Set<String> submodels = new HashSet<String>();
		submodels.add(FridgeCoupledModel.MIL_RT_URI);
		submodels.add(FridgeUnitTestModel.MIL_RT_URI);

		Map<EventSource,EventSink[]> connections =
									new HashMap<EventSource,EventSink[]>();
	connections.put(
			new EventSource(FridgeUnitTestModel.MIL_RT_URI,
							SwitchOnFridge.class),
			new EventSink[] {
				new EventSink(FridgeCoupledModel.MIL_RT_URI,
							  SwitchOnFridge.class)
			});
		connections.put(
			new EventSource(FridgeUnitTestModel.MIL_RT_URI,
							SwitchOffFridge.class),
			new EventSink[] {
				new EventSink(FridgeCoupledModel.MIL_RT_URI,
							  SwitchOffFridge.class)
			});
		connections.put(
			new EventSource(FridgeUnitTestModel.MIL_RT_URI,
							SetPowerFridge.class),
				new EventSink[] {
				new EventSink(FridgeCoupledModel.MIL_RT_URI,
							  SetPowerFridge.class)
			});
		connections.put(
			new EventSource(FridgeUnitTestModel.MIL_RT_URI, CoolFridge.class),
			new EventSink[] {
				new EventSink(FridgeCoupledModel.MIL_RT_URI, CoolFridge.class)
			});
		connections.put(
			new EventSource(FridgeUnitTestModel.MIL_RT_URI, DoNotCoolFridge.class),
			new EventSink[] {
				new EventSink(FridgeCoupledModel.MIL_RT_URI, DoNotCoolFridge.class)
			});
		connections.put(
			new EventSource(FridgeUnitTestModel.MIL_RT_URI, OpenDoorFridge.class),
			new EventSink[] {
				new EventSink(FridgeCoupledModel.MIL_RT_URI, OpenDoorFridge.class)
			});
		connections.put(
			new EventSource(FridgeUnitTestModel.MIL_RT_URI, CloseDoorFridge.class),
			new EventSink[] {
				new EventSink(FridgeCoupledModel.MIL_RT_URI, CloseDoorFridge.class)
			});

		// coupled model descriptor
		coupledModelDescriptors.put(
				GlobalCoupledModel.MIL_RT_URI,
				RTComponentCoupledModelDescriptor.create(
						GlobalCoupledModel.class,
						GlobalCoupledModel.MIL_RT_URI,
						submodels,
						null,
						null,
						connections,
						null,
						CoordinatorComponent.REFLECTION_INBOUND_PORT_URI,
						CoordinatorPlugin.class,
						null,
						accelerationFactor));

		ComponentModelArchitecture architecture =
				new RTComponentModelArchitecture(
						architectureURI,
						GlobalCoupledModel.MIL_RT_URI,
						atomicModelDescriptors,
						coupledModelDescriptors,
						simulatedTimeUnit);

		return architecture;
	}
}
