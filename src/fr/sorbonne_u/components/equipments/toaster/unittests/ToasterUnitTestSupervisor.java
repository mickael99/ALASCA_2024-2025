package fr.sorbonne_u.components.equipments.toaster.unittests;


import fr.sorbonne_u.components.CoordinatorComponent;
import fr.sorbonne_u.components.GlobalCoupledModel;
import fr.sorbonne_u.components.annotations.RequiredInterfaces;
import fr.sorbonne_u.components.cyphy.AbstractCyPhyComponent;
import fr.sorbonne_u.components.cyphy.plugins.devs.CoordinatorPlugin;
import fr.sorbonne_u.components.cyphy.plugins.devs.SupervisorPlugin;
import fr.sorbonne_u.components.cyphy.plugins.devs.architectures.*;
import fr.sorbonne_u.components.cyphy.utils.aclocks.AcceleratedAndSimulationClock;
import fr.sorbonne_u.components.cyphy.utils.aclocks.ClocksServerWithSimulationCI;
import fr.sorbonne_u.components.cyphy.utils.aclocks.ClocksServerWithSimulationConnector;
import fr.sorbonne_u.components.cyphy.utils.aclocks.ClocksServerWithSimulationOutboundPort;
import fr.sorbonne_u.components.equipments.toaster.Toaster;
import fr.sorbonne_u.components.equipments.toaster.ToasterUser;
import fr.sorbonne_u.components.equipments.toaster.mil.ToasterCoupledModel;
import fr.sorbonne_u.components.equipments.toaster.mil.ToasterUnitTesterModel;
import fr.sorbonne_u.components.equipments.toaster.mil.events.SetToasterBrowningLevel;
import fr.sorbonne_u.components.equipments.toaster.mil.events.TurnOffToaster;
import fr.sorbonne_u.components.equipments.toaster.mil.events.TurnOnToaster;
import fr.sorbonne_u.components.exceptions.BCMException;
import fr.sorbonne_u.components.hem2024e1.CVMIntegrationTest;
import fr.sorbonne_u.components.utils.SimulationType;
import fr.sorbonne_u.devs_simulation.models.architectures.AbstractAtomicModelDescriptor;
import fr.sorbonne_u.devs_simulation.models.architectures.CoupledModelDescriptor;
import fr.sorbonne_u.devs_simulation.models.events.EventI;
import fr.sorbonne_u.devs_simulation.models.events.EventSink;
import fr.sorbonne_u.devs_simulation.models.events.EventSource;
import fr.sorbonne_u.devs_simulation.utils.InvariantChecking;
import fr.sorbonne_u.exceptions.ImplementationInvariantException;
import fr.sorbonne_u.exceptions.PreconditionException;
import fr.sorbonne_u.utils.aclocks.ClocksServer;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@RequiredInterfaces(required = {ClocksServerWithSimulationCI.class})
public class ToasterUnitTestSupervisor extends AbstractCyPhyComponent {

    // -------------------------------------------------------------------------
    // Constants and variables
    // -------------------------------------------------------------------------
    public static final long		DELAY_TO_GET_REPORT = 1000L;

    /** URI of the simulation architecture when a MIL simulation is
     *  executed.															*/
    public static final String		MIL_ARCHITECTURE_URI =
            "toaster-mil-simulator";
    /** URI of the simulation architecture when a MIL real time
     *  simulation is executed.												*/
    public static final String		MIL_RT_ARCHITECTURE_URI =
            "toaster-mil-rt-simulator";
    /** URI of the simulation architecture when a SIL simulation is
     *  executed.															*/
    public static final String		SIL_ARCHITECTURE_URI =
            "toaster-sil-simulator";

    public static boolean			VERBOSE = false;
    public static int				X_RELATIVE_POSITION = 0;
    public static int				Y_RELATIVE_POSITION = 0;

    protected final SimulationType currentSimulationType;
    protected final String			simArchitectureURI;

    // -------------------------------------------------------------------------
    // Invariants
    // -------------------------------------------------------------------------

    protected static boolean	glassBoxInvariants(
            ToasterUnitTestSupervisor instance
                                                  )
    {
        assert instance != null : new PreconditionException("instance != null");

        boolean ret = true;
        // The glass-box invariants are ensured by the fact that the fields are
        // final and that the constructor verifies the same  assertions as
        // preconditions
        return ret;
    }

    protected static boolean	blackBoxInvariants(
            ToasterUnitTestSupervisor instance
                                                  )
    {
        assert instance != null : new PreconditionException("instance != null");

        boolean ret = true;
        ret &= InvariantChecking.checkBlackBoxInvariant(
                DELAY_TO_GET_REPORT > 0 &&
                DELAY_TO_GET_REPORT <
                CVM_ToasterUnitTest.DELAY_TO_STOP,
                ToasterUnitTestSupervisor.class, instance,
                "DELAY_TO_GET_REPORT > 0 && DELAY_TO_GET_REPORT < "
                + "CVM_HairDryerUnitTest.DELAY_TO_STOP");
        ret &= InvariantChecking.checkBlackBoxInvariant(
                MIL_ARCHITECTURE_URI != null &&
                !MIL_ARCHITECTURE_URI.isEmpty(),
                ToasterUnitTestSupervisor.class, instance,
                "MIL_ARCHITECTURE_URI != null && "
                + "!MIL_ARCHITECTURE_URI.isEmpty()");
        ret &= InvariantChecking.checkBlackBoxInvariant(
                MIL_RT_ARCHITECTURE_URI != null &&
                !MIL_RT_ARCHITECTURE_URI.isEmpty(),
                ToasterUnitTestSupervisor.class, instance,
                "MIL_RT_ARCHITECTURE_URI != null && "
                + "!MIL_RT_ARCHITECTURE_URI.isEmpty()");
        ret &= InvariantChecking.checkBlackBoxInvariant(
                SIL_ARCHITECTURE_URI != null &&
                !SIL_ARCHITECTURE_URI.isEmpty(),
                ToasterUnitTestSupervisor.class, instance,
                "SIL_ARCHITECTURE_URI != null && "
                + "!SIL_ARCHITECTURE_URI.isEmpty()");
        ret &= InvariantChecking.checkBlackBoxInvariant(
                X_RELATIVE_POSITION >= 0,
                ToasterUnitTestSupervisor.class, instance,
                "X_RELATIVE_POSITION >= 0");
        ret &= InvariantChecking.checkBlackBoxInvariant(
                Y_RELATIVE_POSITION >= 0,
                ToasterUnitTestSupervisor.class, instance,
                "Y_RELATIVE_POSITION >= 0");
        return ret;
    }

    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------
    protected ToasterUnitTestSupervisor(
            SimulationType simulationType,
            String simArchitectureURI
                                       )
    {
        super(2, 1);

        assert	simulationType != null :
                new PreconditionException("currentExecutionType != null");
        assert	!simulationType.isSimulated() ||
                  (simArchitectureURI != null &&
                   !simArchitectureURI.isEmpty()) :
                new PreconditionException(
                        "!currentExecutionType.isSimulated() ||  "
                        + "(simArchitectureURI != null && "
                        + "!simArchitectureURI.isEmpty())");


        this.currentSimulationType = simulationType;
        this.simArchitectureURI = simArchitectureURI;

        this.tracer.get().setTitle("HairDryer unit test supervisor");
        this.tracer.get().setRelativePosition(X_RELATIVE_POSITION,
                                              Y_RELATIVE_POSITION);
        this.toggleTracing();

        assert glassBoxInvariants(this) :
            new ImplementationInvariantException("glassBoxInvariants(this)");
        assert blackBoxInvariants(this) :
            new ImplementationInvariantException("blackBoxInvariants(this)");
    }

    // -------------------------------------------------------------------------
    // Component life-cycle
    // -------------------------------------------------------------------------

    @Override
    public void			execute() throws Exception
    {
        ClocksServerWithSimulationOutboundPort clocksServerOutboundPort =
                new ClocksServerWithSimulationOutboundPort(this);
        clocksServerOutboundPort.publishPort();
        this.doPortConnection(
                clocksServerOutboundPort.getPortURI(),
                ClocksServer.STANDARD_INBOUNDPORT_URI,
                ClocksServerWithSimulationConnector.class.getCanonicalName());
        this.logMessage("ToasterUnitTestSupervisor gets the clock.");
        AcceleratedAndSimulationClock acceleratedClock =
                clocksServerOutboundPort.getClockWithSimulation(
                        CVMIntegrationTest.CLOCK_URI);
        this.doPortDisconnection(clocksServerOutboundPort.getPortURI());
        clocksServerOutboundPort.unpublishPort();
        clocksServerOutboundPort.destroyPort();

        long simulationStartTimeInMillis =
                TimeUnit.NANOSECONDS.toMillis(
                        acceleratedClock.getSimulationStartEpochNanos());

        this.logMessage("ToasterUnitTestSupervisor waits until start time.");
        acceleratedClock.waitUntilStart();
        this.logMessage("ToasterUnitTestSupervisor starts.");

        switch (this.currentSimulationType) {
            case MIL_SIMULATION:
                // create the global simulation architecture given the type of
                // simulation for the current run
                ComponentModelArchitecture cma =
                        createMILComponentSimulationArchitectures(
                                this.simArchitectureURI,
                                acceleratedClock.getSimulatedTimeUnit());
                // create and install the supervisor plug-in
                SupervisorPlugin sp = new SupervisorPlugin(cma);
                sp.setPluginURI(ToasterUnitTestSupervisor.MIL_ARCHITECTURE_URI);
                this.installPlugin(sp);
                this.logMessage("plug-in installed.");
                // construct the global simulator
                sp.constructSimulator();
                this.logMessage("simulator constructed.");
                // initialise the simulation run parameters (it will set the model
                // logger to the component logger)
                sp.setSimulationRunParameters(new HashMap<>());
                acceleratedClock.waitUntilSimulationStart();
                // execute the MIL simulation
                sp.doStandAloneSimulation(0.0,
                                          acceleratedClock.getSimulatedDuration());
                this.logMessage(sp.getFinalReport().toString());
                this.logMessage("simulation ends.");
                break;
            case MIL_RT_SIMULATION:
                // create the global simulation architecture given the type of
                // simulation for the current run
                cma = createMILRTComponentSimulationArchitectures(
                        this.simArchitectureURI,
                        acceleratedClock.getSimulatedTimeUnit(),
                        acceleratedClock.getAccelerationFactor());
                // create and install the supervisor plug-in
                sp = new SupervisorPlugin(cma);
                sp.setPluginURI(ToasterUnitTestSupervisor.MIL_ARCHITECTURE_URI);
                this.installPlugin(sp);
                this.logMessage("plug-in installed.");
                // construct the global simulator
                sp.constructSimulator();
                this.logMessage("simulator constructed, simulation begins.");
                // initialise the simulation run parameters (it will set the model
                // logger to the component logger)
                sp.setSimulationRunParameters(new HashMap<>());
                // start the MIL real time simulation
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
    public static ComponentModelArchitecture createMILComponentSimulationArchitectures(
            String architectureURI,
            TimeUnit simulatedTimeUnit
                                                                                      ) throws Exception{
        Map<String, AbstractAtomicModelDescriptor> atomicModelDescriptors =
                new HashMap<>();

        atomicModelDescriptors.put(
                ToasterCoupledModel.MIL_URI,
                ComponentAtomicModelDescriptor.create(
                        ToasterCoupledModel.MIL_URI,
                        (Class<? extends EventI>[]) new Class<?>[]{
                                TurnOnToaster.class,
                                TurnOffToaster.class,
                                SetToasterBrowningLevel.class
                        },
                        (Class<? extends EventI>[]) new Class<?>[]{},
                        simulatedTimeUnit,
                        Toaster.REFLECTION_INBOUND_PORT_URI
                                                     ));
        atomicModelDescriptors.put(
                ToasterUnitTesterModel.MIL_URI,
                ComponentAtomicModelDescriptor.create(
                        ToasterUnitTesterModel.MIL_URI,
                        (Class<? extends EventI>[]) new Class<?>[]{},
                        (Class<? extends EventI>[]) new Class<?>[]{
                                TurnOnToaster.class,
                                TurnOffToaster.class,
                                SetToasterBrowningLevel.class
                        },
                        simulatedTimeUnit,
                        ToasterUser.REFLECTION_INBOUND_PORT_URI
                                                     ));

        Map<String, CoupledModelDescriptor> coupledModelDescriptors =
                new HashMap<>();
        Set<String> submodels = new HashSet<String>();
        submodels.add(ToasterCoupledModel.MIL_URI);
        submodels.add(ToasterUnitTesterModel.MIL_URI);

        Map<EventSource,EventSink[]> connections =
                new HashMap<EventSource, EventSink[]>();
        connections.put(
                new EventSource(ToasterUnitTesterModel.MIL_URI, TurnOnToaster.class),
                new EventSink[]{new EventSink(ToasterCoupledModel.MIL_URI,
                                              TurnOnToaster.class)});
        connections.put(
                new EventSource(ToasterUnitTesterModel.MIL_URI, TurnOffToaster.class),
                new EventSink[]{new EventSink(ToasterCoupledModel.MIL_URI,
                                              TurnOffToaster.class)});
        connections.put(
                new EventSource(ToasterUnitTesterModel.MIL_URI, SetToasterBrowningLevel.class),
                new EventSink[]{new EventSink(ToasterCoupledModel.MIL_URI,
                                              SetToasterBrowningLevel.class)});

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
                        ));

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
    public static ComponentModelArchitecture createMILRTComponentSimulationArchitectures(
            String architectureURI,
            TimeUnit simulatedTimeUnit,
            double accelerationFactor
                                                                                        ) throws Exception{
        Map<String, AbstractAtomicModelDescriptor> atomicModelDescriptors =
                new HashMap<>();

        atomicModelDescriptors.put(
                ToasterCoupledModel.MIL_RT_URI,
                ComponentAtomicModelDescriptor.create(
                        ToasterCoupledModel.MIL_RT_URI,
                        (Class<? extends EventI>[]) new Class<?>[]{
                                TurnOnToaster.class,
                                TurnOffToaster.class,
                                SetToasterBrowningLevel.class
                        },
                        (Class<? extends EventI>[]) new Class<?>[]{},
                        simulatedTimeUnit,
                        Toaster.REFLECTION_INBOUND_PORT_URI
                                                     ));
        atomicModelDescriptors.put(
                ToasterUnitTesterModel.MIL_RT_URI,
                ComponentAtomicModelDescriptor.create(
                        ToasterUnitTesterModel.MIL_RT_URI,
                        (Class<? extends EventI>[]) new Class<?>[]{},
                        (Class<? extends EventI>[]) new Class<?>[]{
                                TurnOnToaster.class,
                                TurnOffToaster.class,
                                SetToasterBrowningLevel.class
                        },
                        simulatedTimeUnit,
                        ToasterUser.REFLECTION_INBOUND_PORT_URI
                                                     ));

        Map<String, CoupledModelDescriptor> coupledModelDescriptors =
                new HashMap<>();
        Set<String> submodels = new HashSet<String>();
        submodels.add(ToasterCoupledModel.MIL_RT_URI);
        submodels.add(ToasterUnitTesterModel.MIL_RT_URI);

        Map<EventSource,EventSink[]> connections =
                new HashMap<EventSource, EventSink[]>();
        connections.put(
                new EventSource(ToasterUnitTesterModel.MIL_RT_URI, TurnOnToaster.class),
                new EventSink[]{new EventSink(ToasterCoupledModel.MIL_RT_URI,
                                              TurnOnToaster.class)});
        connections.put(
                new EventSource(ToasterUnitTesterModel.MIL_RT_URI, TurnOffToaster.class),
                new EventSink[]{new EventSink(ToasterCoupledModel.MIL_RT_URI,
                                              TurnOffToaster.class)});
        connections.put(
                new EventSource(ToasterUnitTesterModel.MIL_RT_URI, SetToasterBrowningLevel.class),
                new EventSink[]{new EventSink(ToasterCoupledModel.MIL_RT_URI,
                                              SetToasterBrowningLevel.class)});

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
                                                        ));
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
