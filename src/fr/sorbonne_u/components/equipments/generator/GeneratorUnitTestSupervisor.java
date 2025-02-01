package fr.sorbonne_u.components.equipments.generator;

import fr.sorbonne_u.components.CoordinatorComponent;
import fr.sorbonne_u.components.GlobalCoupledModel;
import fr.sorbonne_u.components.cyphy.AbstractCyPhyComponent;
import fr.sorbonne_u.components.cyphy.plugins.devs.CoordinatorPlugin;
import fr.sorbonne_u.components.cyphy.plugins.devs.SupervisorPlugin;
import fr.sorbonne_u.components.cyphy.plugins.devs.architectures.*;
import fr.sorbonne_u.components.cyphy.utils.aclocks.AcceleratedAndSimulationClock;
import fr.sorbonne_u.components.cyphy.utils.aclocks.ClocksServerWithSimulationCI;
import fr.sorbonne_u.components.cyphy.utils.aclocks.ClocksServerWithSimulationConnector;
import fr.sorbonne_u.components.cyphy.utils.aclocks.ClocksServerWithSimulationOutboundPort;
import fr.sorbonne_u.components.equipments.generator.mil.GeneratorCoupledModel;
import fr.sorbonne_u.components.equipments.generator.mil.GeneratorUserModel;
import fr.sorbonne_u.components.equipments.generator.mil.events.ActivateGeneratorEvent;
import fr.sorbonne_u.components.equipments.generator.mil.events.StopGeneratorEvent;
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
import fr.sorbonne_u.exceptions.InvariantException;
import fr.sorbonne_u.exceptions.PreconditionException;
import fr.sorbonne_u.utils.aclocks.ClocksServer;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import fr.sorbonne_u.components.annotations.RequiredInterfaces;


@RequiredInterfaces(required = {ClocksServerWithSimulationCI.class})
public class GeneratorUnitTestSupervisor extends AbstractCyPhyComponent {

    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------

    public static final long		DELAY_TO_GET_REPORT = 1000L;

    /** URI of the simulation architecture when a MIL simulation is
     *  executed.															*/
    public static final String		MIL_ARCHITECTURE_URI =
            "generator-mil-simulator";
    /** URI of the simulation architecture when a MIL real time
     *  simulation is executed.												*/
    public static final String		MIL_RT_ARCHITECTURE_URI =
            "generator-mil-rt-simulator";
    /** URI of the simulation architecture when a SIL simulation is
     *  executed.															*/
    public static final String		SIL_ARCHITECTURE_URI =
            "generator-sil-simulator";

    /** when true, methods trace their actions.								*/
    public static boolean			VERBOSE = false;
    /** when tracing, x coordinate of the window relative position.			*/
    public static int				X_RELATIVE_POSITION = 0;
    /** when tracing, y coordinate of the window relative position.			*/
    public static int				Y_RELATIVE_POSITION = 0;

    // Execution/Simulation

    /** current type of simulation.											*/
    protected final SimulationType currentSimulationType;
    /** URI of the simulation architecture to be created or the empty string
     *  if the component does not execute as a SIL simulation.				*/
    protected final String			simArchitectureURI;

    protected static boolean	glassBoxInvariants(
            GeneratorUnitTestSupervisor instance
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
            GeneratorUnitTestSupervisor instance
                                                  )
    {
        assert instance != null : new PreconditionException("instance != null");

        boolean ret = true;
        ret &= InvariantChecking.checkBlackBoxInvariant(
                DELAY_TO_GET_REPORT > 0 &&
                DELAY_TO_GET_REPORT <
                CVM_GeneratorUnitTest.DELAY_TO_STOP,
                GeneratorUnitTestSupervisor.class, instance,
                "DELAY_TO_GET_REPORT > 0 && DELAY_TO_GET_REPORT < "
                + "CVM_GeneratorUnitTest.DELAY_TO_STOP");
        ret &= InvariantChecking.checkBlackBoxInvariant(
                MIL_ARCHITECTURE_URI != null &&
                !MIL_ARCHITECTURE_URI.isEmpty(),
                GeneratorUnitTestSupervisor.class, instance,
                "MIL_ARCHITECTURE_URI != null && "
                + "!MIL_ARCHITECTURE_URI.isEmpty()");
        ret &= InvariantChecking.checkBlackBoxInvariant(
                MIL_RT_ARCHITECTURE_URI != null &&
                !MIL_RT_ARCHITECTURE_URI.isEmpty(),
                GeneratorUnitTestSupervisor.class, instance,
                "MIL_RT_ARCHITECTURE_URI != null && "
                + "!MIL_RT_ARCHITECTURE_URI.isEmpty()");
        ret &= InvariantChecking.checkBlackBoxInvariant(
                SIL_ARCHITECTURE_URI != null &&
                !SIL_ARCHITECTURE_URI.isEmpty(),
                GeneratorUnitTestSupervisor.class, instance,
                "SIL_ARCHITECTURE_URI != null && "
                + "!SIL_ARCHITECTURE_URI.isEmpty()");
        ret &= InvariantChecking.checkBlackBoxInvariant(
                X_RELATIVE_POSITION >= 0,
                GeneratorUnitTestSupervisor.class, instance,
                "X_RELATIVE_POSITION >= 0");
        ret &= InvariantChecking.checkBlackBoxInvariant(
                Y_RELATIVE_POSITION >= 0,
                GeneratorUnitTestSupervisor.class, instance,
                "Y_RELATIVE_POSITION >= 0");
        return ret;
    }


    protected			GeneratorUnitTestSupervisor(
            SimulationType currentSimulationType,
            String simArchitectureURI
                                                     )
    {
        // one standard thread for execute, one standard for report reception
        // and one schedulable thread to schedule the start of MIL simulations
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

        this.tracer.get().setTitle("Generator unit test supervisor");
        this.tracer.get().setRelativePosition(X_RELATIVE_POSITION,
                                              Y_RELATIVE_POSITION);
        this.toggleTracing();

        assert	GeneratorUnitTestSupervisor.glassBoxInvariants(this) :
                new ImplementationInvariantException(
                        "GeneratorUnitTestSupervisor.glassBoxInvariants(this)");
        assert	GeneratorUnitTestSupervisor.blackBoxInvariants(this) :
                new InvariantException(
                        "GeneratorUnitTestSupervisor.blackBoxInvariants(this)");
    }

    @Override
    public void			execute() throws Exception
    {
        // First, the component must synchronise with other components
        // to start the execution of the test scenario; we use a
        // time-triggered synchronisation scheme with the accelerated clock
        ClocksServerWithSimulationOutboundPort clocksServerOutboundPort =
                new ClocksServerWithSimulationOutboundPort(this);
        clocksServerOutboundPort.publishPort();
        this.doPortConnection(
                clocksServerOutboundPort.getPortURI(),
                ClocksServer.STANDARD_INBOUNDPORT_URI,
                ClocksServerWithSimulationConnector.class.getCanonicalName());
        this.logMessage("GeneratorUnitTestSupervisor gets the clock.");
        AcceleratedAndSimulationClock acceleratedClock =
                clocksServerOutboundPort.getClockWithSimulation(
                        CVMIntegrationTest.CLOCK_URI);
        this.doPortDisconnection(clocksServerOutboundPort.getPortURI());
        clocksServerOutboundPort.unpublishPort();
        clocksServerOutboundPort.destroyPort();

        // compute the real time of start of the simulation using the
        // accelerated clock; this time is the start time of the
        // execution defined in the accelerated clock plus a delay defined
        // in the CVM class launching this execution 
        long simulationStartTimeInMillis =
                TimeUnit.NANOSECONDS.toMillis(
                        acceleratedClock.getSimulationStartEpochNanos());

        this.logMessage("GeneratorUnitTestSupervisor waits until start time.");
        acceleratedClock.waitUntilStart();
        this.logMessage("GeneratorUnitTestSupervisor starts.");

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
                sp.setPluginURI(GeneratorUnitTestSupervisor.MIL_ARCHITECTURE_URI);
                this.installPlugin(sp);
                this.logMessage("plug-in installed.");
                // construct the global simulator
                sp.constructSimulator();
                this.logMessage("simulator constructed.");
                // initialise the simulation run parameters (it will set the model
                // logger to the component logger)
                sp.setSimulationRunParameters(new HashMap<>());
                // execute the MIL simulation
                acceleratedClock.waitUntilSimulationStart();
                logMessage("simulation begins.");
                sp.doStandAloneSimulation(
                        0.0, acceleratedClock.getSimulatedDuration());
                logMessage("simulation ends.");
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
                sp.setPluginURI(GeneratorUnitTestSupervisor.MIL_ARCHITECTURE_URI);
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
                // wait until the end of the simulation
                acceleratedClock.waitUntilSimulationEnd();
                // give some time for the end of simulation catering tasks
                Thread.sleep(200L);
                this.logMessage(sp.getFinalReport().toString());
                break;
            case SIL_SIMULATION:
                // For SIL simulations in hair dryer unit tests, there is only one
                // component, Generator, that executes a simulation; the component
                // starts it itself in its execute method
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
        // map that will contain the atomic model descriptors to construct
        // the simulation architecture
        Map<String, AbstractAtomicModelDescriptor> atomicModelDescriptors =
                new HashMap<>();

        // The Generator simulator is the composition of two atomic models, the
        // GeneratorStateModel and the GeneratorElectricityModel, to get the
        // GeneratorCoupledModel. In the overall unit test component simulation
        // model, this coupled model is seen as an atomic model, hiding the
        // internal local simulation architecture of the Generator behind the
        // closure of DEVS models composition principle (a coupled model can be
        // seen as an atomic model).
        atomicModelDescriptors.put(
                GeneratorCoupledModel.MIL_URI,
                ComponentAtomicModelDescriptor.create(
                        GeneratorCoupledModel.MIL_URI,
                        (Class<? extends EventI>[]) new Class<?>[]{
                                ActivateGeneratorEvent.class,
                                StopGeneratorEvent.class},
                        (Class<? extends EventI>[]) new Class<?>[]{},
                        simulatedTimeUnit,
                        Generator.REFLECTION_INBOUND_PORT_URI
                                                     ));
        // The GeneratorUser simulator is made of only one atomic model, which
        // is therefore seen as an atomic model also at the component simulation
        // architecture level.
        atomicModelDescriptors.put(
                GeneratorUserModel.MIL_URI,
                ComponentAtomicModelDescriptor.create(
                        GeneratorUserModel.MIL_URI,
                        (Class<? extends EventI>[]) new Class<?>[]{},
                        (Class<? extends EventI>[]) new Class<?>[]{
                                ActivateGeneratorEvent.class,
                                StopGeneratorEvent.class},
                        simulatedTimeUnit,
                        GeneratorUser.REFLECTION_INBOUND_PORT_URI));

        // map that will contain the coupled model descriptors to construct
        // the simulation architecture
        Map<String, CoupledModelDescriptor> coupledModelDescriptors =
                new HashMap<>();

        // the set of submodels of the coupled model, given by their URIs
        Set<String> submodels = new HashSet<String>();
        submodels.add(GeneratorCoupledModel.MIL_URI);
        submodels.add(GeneratorUserModel.MIL_URI);

        // event exchanging connections between exporting and importing
        // models
        Map<EventSource, EventSink[]> connections =
                new HashMap<EventSource,EventSink[]>();
        connections.put(
                new EventSource(GeneratorUserModel.MIL_URI,
                                ActivateGeneratorEvent.class),
                new EventSink[] {
                        new EventSink(GeneratorCoupledModel.MIL_URI,
                                      ActivateGeneratorEvent.class)
                });
        connections.put(
                new EventSource(GeneratorUserModel.MIL_URI,
                                StopGeneratorEvent.class),
                new EventSink[] {
                        new EventSink(GeneratorCoupledModel.MIL_URI,
                                      StopGeneratorEvent.class)
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
        // map that will contain the atomic model descriptors to construct
        // the simulation architecture
        Map<String,AbstractAtomicModelDescriptor> atomicModelDescriptors =
                new HashMap<>();

        // The Generator simulator is the composition of two atomic models, the
        // GeneratorStateModel and the GeneratorElectricityModel, to get the
        // GeneratorCoupledModel. In the overall unit test component simulation
        // model, this coupled model is seen as an atomic model, hiding the
        // internal local simulation architecture of the Generator behind the
        // closure of DEVS models composition principle (a coupled model can be
        // seen as an atomic model).
        atomicModelDescriptors.put(
                GeneratorCoupledModel.MIL_RT_URI,
                RTComponentAtomicModelDescriptor.create(
                        GeneratorCoupledModel.MIL_RT_URI,
                        (Class<? extends EventI>[]) new Class<?>[]{
                                ActivateGeneratorEvent.class,
                                StopGeneratorEvent.class},
                        (Class<? extends EventI>[]) new Class<?>[]{},
                        simulatedTimeUnit,
                        Generator.REFLECTION_INBOUND_PORT_URI
                                                       ));
        // The GeneratorUser simulator is made of only one atomic model, which
        // is therefore seen as an atomic model also at the component simulation
        // architecture level.
        atomicModelDescriptors.put(
                GeneratorUserModel.MIL_RT_URI,
                RTComponentAtomicModelDescriptor.create(
                        GeneratorUserModel.MIL_RT_URI,
                        (Class<? extends EventI>[]) new Class<?>[]{},
                        (Class<? extends EventI>[]) new Class<?>[]{
                                ActivateGeneratorEvent.class,
                                StopGeneratorEvent.class},
                        simulatedTimeUnit,
                        GeneratorUser.REFLECTION_INBOUND_PORT_URI));

        // map that will contain the coupled model descriptors to construct
        // the simulation architecture
        Map<String,CoupledModelDescriptor> coupledModelDescriptors =
                new HashMap<>();

        // the set of submodels of the coupled model, given by their URIs
        Set<String> submodels = new HashSet<String>();
        submodels.add(GeneratorCoupledModel.MIL_RT_URI);
        submodels.add(GeneratorUserModel.MIL_RT_URI);

        // event exchanging connections between exporting and importing
        // models
        Map<EventSource,EventSink[]> connections =
                new HashMap<EventSource,EventSink[]>();
        connections.put(
                new EventSource(GeneratorUserModel.MIL_RT_URI,
                                ActivateGeneratorEvent.class),
                new EventSink[] {
                        new EventSink(GeneratorCoupledModel.MIL_RT_URI,
                                      ActivateGeneratorEvent.class)
                });
        connections.put(
                new EventSource(GeneratorUserModel.MIL_RT_URI,
                                StopGeneratorEvent.class),
                new EventSink[] {
                        new EventSink(GeneratorCoupledModel.MIL_RT_URI,
                                      StopGeneratorEvent.class)
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
// -----------------------------------------------------------------------------
