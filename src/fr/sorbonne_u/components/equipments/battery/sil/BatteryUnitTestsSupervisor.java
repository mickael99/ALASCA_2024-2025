package fr.sorbonne_u.components.equipments.battery.sil;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import fr.sorbonne_u.components.CVMIntegrationTest;
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
import fr.sorbonne_u.components.equipments.battery.Battery;
import fr.sorbonne_u.components.equipments.battery.BatteryTester;
import fr.sorbonne_u.components.equipments.battery.CVM_BatteryUnitTest;
import fr.sorbonne_u.components.equipments.battery.mil.BatteryCoupledModel;
import fr.sorbonne_u.components.equipments.battery.mil.BatteryUserModel;
import fr.sorbonne_u.components.equipments.battery.mil.events.SetConsumeBatteryEvent;
import fr.sorbonne_u.components.equipments.battery.mil.events.SetProductBatteryEvent;
import fr.sorbonne_u.components.equipments.battery.mil.events.SetStandByBatteryEvent;
import fr.sorbonne_u.components.exceptions.BCMException;
import fr.sorbonne_u.components.utils.SimulationType;
import fr.sorbonne_u.devs_simulation.models.architectures.AbstractAtomicModelDescriptor;
import fr.sorbonne_u.devs_simulation.models.architectures.CoupledModelDescriptor;
import fr.sorbonne_u.devs_simulation.models.events.EventI;
import fr.sorbonne_u.devs_simulation.models.events.EventSink;
import fr.sorbonne_u.devs_simulation.models.events.EventSource;
import fr.sorbonne_u.devs_simulation.utils.InvariantChecking;
import fr.sorbonne_u.exceptions.ImplementationInvariantException;
import fr.sorbonne_u.exceptions.InvariantException;
import fr.sorbonne_u.exceptions.PreconditionException;
import fr.sorbonne_u.utils.aclocks.ClocksServer;

@RequiredInterfaces(required = {ClocksServerWithSimulationCI.class})
public class BatteryUnitTestsSupervisor extends AbstractCyPhyComponent {
	
	// -------------------------------------------------------------------------
	// Constants and variables
	// -------------------------------------------------------------------------
	
	public static final long DELAY_TO_GET_REPORT = 1000L;
	
	public static final String MIL_ARCHITECTURE_URI ="battery-mil-simulator";
	public static final String MIL_RT_ARCHITECTURE_URI = "battery-mil-rt-simulator";
	public static final String SIL_ARCHITECTURE_URI = "battery-sil-simulator";

	public static boolean VERBOSE = true;
	public static int X_RELATIVE_POSITION = 0;
	public static int Y_RELATIVE_POSITION = 0;

	protected final SimulationType	currentSimulationType;
	protected final String simArchitectureURI;
	
	
	// -------------------------------------------------------------------------
	// Invariants
	// -------------------------------------------------------------------------
	
	protected static boolean glassBoxInvariants(BatteryUnitTestsSupervisor instance) {
		assert instance != null : new PreconditionException("instance != null");

		boolean ret = true;
		return ret;
	}

	protected static boolean blackBoxInvariants(BatteryUnitTestsSupervisor instance) {
		assert instance != null : new PreconditionException("instance != null");

		boolean ret = true;
		ret &= InvariantChecking.checkBlackBoxInvariant(
					DELAY_TO_GET_REPORT > 0 &&
						DELAY_TO_GET_REPORT <
					CVM_BatteryUnitTest.DELAY_TO_STOP,
					BatteryUnitTestsSupervisor.class, instance,
					"DELAY_TO_GET_REPORT > 0 && DELAY_TO_GET_REPORT < "
					+ "CVM_BatteryUnitTest.DELAY_TO_STOP");
		ret &= InvariantChecking.checkBlackBoxInvariant(
					MIL_ARCHITECTURE_URI != null && 
										!MIL_ARCHITECTURE_URI.isEmpty(),
					BatteryUnitTestsSupervisor.class, instance,
					"MIL_ARCHITECTURE_URI != null && "
					+ "!MIL_ARCHITECTURE_URI.isEmpty()");
		ret &= InvariantChecking.checkBlackBoxInvariant(
					MIL_RT_ARCHITECTURE_URI != null && 
										!MIL_RT_ARCHITECTURE_URI.isEmpty(),
					BatteryUnitTestsSupervisor.class, instance,
					"MIL_RT_ARCHITECTURE_URI != null && "
					+ "!MIL_RT_ARCHITECTURE_URI.isEmpty()");
		ret &= InvariantChecking.checkBlackBoxInvariant(
					SIL_ARCHITECTURE_URI != null && 
										!SIL_ARCHITECTURE_URI.isEmpty(),
					BatteryUnitTestsSupervisor.class, instance,
					"SIL_ARCHITECTURE_URI != null && "
					+ "!SIL_ARCHITECTURE_URI.isEmpty()");
		ret &= InvariantChecking.checkBlackBoxInvariant(
					X_RELATIVE_POSITION >= 0,
					BatteryUnitTestsSupervisor.class, instance,
					"X_RELATIVE_POSITION >= 0");
		ret &= InvariantChecking.checkBlackBoxInvariant(
					Y_RELATIVE_POSITION >= 0,
					BatteryUnitTestsSupervisor.class, instance,
					"Y_RELATIVE_POSITION >= 0");
		return ret;
	}
	
	
	// -------------------------------------------------------------------------
	// Constructors
	// -------------------------------------------------------------------------
	
	protected BatteryUnitTestsSupervisor(SimulationType currentSimulationType,String simArchitectureURI) {
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

		this.tracer.get().setTitle("Battery unit test supervisor");
		this.tracer.get().setRelativePosition(X_RELATIVE_POSITION, Y_RELATIVE_POSITION);
		this.toggleTracing();

		assert	BatteryUnitTestsSupervisor.glassBoxInvariants(this) :
				new ImplementationInvariantException(
						"BatteryUnitTestsSupervisor.glassBoxInvariants(this)");
		assert	BatteryUnitTestsSupervisor.blackBoxInvariants(this) :
				new InvariantException(
						"BatteryUnitTestsSupervisor.blackBoxInvariants(this)");
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
		this.logMessage("BatteryUnitTestsSupervisor gets the clock.");
		AcceleratedAndSimulationClock acceleratedClock =
			clocksServerOutboundPort.getClockWithSimulation(CVMIntegrationTest.CLOCK_URI);
		this.doPortDisconnection(clocksServerOutboundPort.getPortURI());
		clocksServerOutboundPort.unpublishPort();
		clocksServerOutboundPort.destroyPort();

		long simulationStartTimeInMillis = 
				TimeUnit.NANOSECONDS.toMillis(
						acceleratedClock.getSimulationStartEpochNanos());
		
		this.logMessage("BatteryUnitTestsSupervisor waits until start time.");
		acceleratedClock.waitUntilStart();
		this.logMessage("BatteryUnitTestsSupervisor starts.");

		switch (this.currentSimulationType) {
			case MIL_SIMULATION:
				ComponentModelArchitecture cma =
						createMILComponentSimulationArchitectures(
											this.simArchitectureURI,
											acceleratedClock.getSimulatedTimeUnit());
	
				SupervisorPlugin sp = new SupervisorPlugin(cma);
				
				sp.setPluginURI(BatteryUnitTestsSupervisor.MIL_ARCHITECTURE_URI);
				this.installPlugin(sp);
				this.logMessage("plug-in installed.");
				sp.constructSimulator();
				this.logMessage("simulator constructed.");
				sp.setSimulationRunParameters(new HashMap<>());
				acceleratedClock.waitUntilSimulationStart();
				logMessage("simulation begins.");
				sp.doStandAloneSimulation(
									0.0, acceleratedClock.getSimulatedDuration());
				logMessage("simulation ends.");				
				break;
				
			case MIL_RT_SIMULATION:
				cma = createMILRTComponentSimulationArchitectures(
										this.simArchitectureURI,
										acceleratedClock.getSimulatedTimeUnit(),
										acceleratedClock.getAccelerationFactor());
				sp = new SupervisorPlugin(cma);
				sp.setPluginURI(BatteryUnitTestsSupervisor.MIL_ARCHITECTURE_URI);
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
				Thread.sleep(200L);
				this.logMessage(sp.getFinalReport().toString());
				this.logMessage("simulation ends.");
				break;
				
			case SIL_SIMULATION:
				
			default:
		}		
	}
	
	
	// -------------------------------------------------------------------------
	// Methods
	// -------------------------------------------------------------------------

	@SuppressWarnings("unchecked")
	public static ComponentModelArchitecture createMILComponentSimulationArchitectures
					(String architectureURI, TimeUnit simulatedTimeUnit) throws Exception
	{
		Map<String, AbstractAtomicModelDescriptor> atomicModelDescriptors = new HashMap<>();
		
		atomicModelDescriptors.put(
		    BatteryCoupledModel.MIL_URI,
		    ComponentAtomicModelDescriptor.create(
		        BatteryCoupledModel.MIL_URI,
		        (Class<? extends EventI>[]) new Class<?>[]{
		            SetStandByBatteryEvent.class,
		            SetProductBatteryEvent.class,
		            SetConsumeBatteryEvent.class},
		        (Class<? extends EventI>[]) new Class<?>[]{},
		        simulatedTimeUnit,
		        Battery.REFLECTION_INBOUND_PORT_URI
		    )
		);

		atomicModelDescriptors.put(
		    BatteryUserModel.MIL_URI,
		    ComponentAtomicModelDescriptor.create(
		        BatteryUserModel.MIL_URI,
		        (Class<? extends EventI>[]) new Class<?>[]{},
		        (Class<? extends EventI>[]) new Class<?>[]{
		        	SetStandByBatteryEvent.class,
		            SetProductBatteryEvent.class,
		            SetConsumeBatteryEvent.class},
		        simulatedTimeUnit,
		        BatteryTester.REFLECTION_INBOUND_PORT_URI
		    )
		);

		Map<String, CoupledModelDescriptor> coupledModelDescriptors = new HashMap<>();

		Set<String> submodels = new HashSet<>();
		submodels.add(BatteryCoupledModel.MIL_URI);
		submodels.add(BatteryUserModel.MIL_URI);

		Map<EventSource, EventSink[]> connections = new HashMap<>();
		connections.put(
		    new EventSource(BatteryUserModel.MIL_URI, SetStandByBatteryEvent.class),
		    new EventSink[]{
		        new EventSink(BatteryCoupledModel.MIL_URI, SetStandByBatteryEvent.class)
		    });
		connections.put(
		    new EventSource(BatteryUserModel.MIL_URI, SetProductBatteryEvent.class),
		    new EventSink[]{
		        new EventSink(BatteryCoupledModel.MIL_URI, SetProductBatteryEvent.class)
		    });
		connections.put(
		    new EventSource(BatteryUserModel.MIL_URI, SetConsumeBatteryEvent.class),
		    new EventSink[]{
		        new EventSink(BatteryCoupledModel.MIL_URI, SetConsumeBatteryEvent.class)
		    });

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
		        null
		    )
		);

		ComponentModelArchitecture architecture = 
				new ComponentModelArchitecture(
				    architectureURI,
				    GlobalCoupledModel.MIL_URI,
				    atomicModelDescriptors,
				    coupledModelDescriptors,
				    simulatedTimeUnit
			    );

		return architecture;
	}
	
	@SuppressWarnings("unchecked")
	public static ComponentModelArchitecture createMILRTComponentSimulationArchitectures(String architectureURI, TimeUnit simulatedTimeUnit,
												double accelerationFactor) throws Exception 
	{
		Map<String, AbstractAtomicModelDescriptor> atomicModelDescriptors = new HashMap<>();

	    atomicModelDescriptors.put(
	        BatteryCoupledModel.MIL_RT_URI,
	        RTComponentAtomicModelDescriptor.create(
	            BatteryCoupledModel.MIL_RT_URI,
	            (Class<? extends EventI>[]) new Class<?>[]{
	            	SetStandByBatteryEvent.class,
		            SetProductBatteryEvent.class,
		            SetConsumeBatteryEvent.class},
	            (Class<? extends EventI>[]) new Class<?>[]{},
	            simulatedTimeUnit,
	            Battery.REFLECTION_INBOUND_PORT_URI
	        )
	    );

	    atomicModelDescriptors.put(
	        BatteryUserModel.MIL_RT_URI,
	        RTComponentAtomicModelDescriptor.create(
	            BatteryUserModel.MIL_RT_URI,
	            (Class<? extends EventI>[]) new Class<?>[]{},
	            (Class<? extends EventI>[]) new Class<?>[]{
	            	SetStandByBatteryEvent.class,
		            SetProductBatteryEvent.class,
		            SetConsumeBatteryEvent.class},
	            simulatedTimeUnit,
	            BatteryTester.REFLECTION_INBOUND_PORT_URI
	        )
	    );

	    Map<String, CoupledModelDescriptor> coupledModelDescriptors = new HashMap<>();

	    Set<String> submodels = new HashSet<>();
	    submodels.add(BatteryCoupledModel.MIL_RT_URI);
	    submodels.add(BatteryUserModel.MIL_RT_URI);

	    Map<EventSource, EventSink[]> connections = new HashMap<>();
	    connections.put(
		    new EventSource(BatteryUserModel.MIL_RT_URI, SetStandByBatteryEvent.class),
		    new EventSink[]{
		        new EventSink(BatteryCoupledModel.MIL_RT_URI, SetStandByBatteryEvent.class)
		    });
		connections.put(
		    new EventSource(BatteryUserModel.MIL_RT_URI, SetProductBatteryEvent.class),
		    new EventSink[]{
		        new EventSink(BatteryCoupledModel.MIL_RT_URI, SetProductBatteryEvent.class)
		    });
		connections.put(
		    new EventSource(BatteryUserModel.MIL_RT_URI, SetConsumeBatteryEvent.class),
		    new EventSink[]{
		        new EventSink(BatteryCoupledModel.MIL_RT_URI, SetConsumeBatteryEvent.class)
		    });

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
	            accelerationFactor
	        )
	    );

	    ComponentModelArchitecture architecture = new RTComponentModelArchitecture(
	        architectureURI,
	        GlobalCoupledModel.MIL_RT_URI,
	        atomicModelDescriptors,
	        coupledModelDescriptors,
	        simulatedTimeUnit
	    );

	    return architecture;
	}
}
