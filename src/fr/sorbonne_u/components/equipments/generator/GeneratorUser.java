package fr.sorbonne_u.components.equipments.generator;

import fr.sorbonne_u.components.annotations.RequiredInterfaces;
import fr.sorbonne_u.components.cyphy.AbstractCyPhyComponent;
import fr.sorbonne_u.components.cyphy.plugins.devs.AtomicSimulatorPlugin;
import fr.sorbonne_u.components.cyphy.plugins.devs.RTAtomicSimulatorPlugin;
import fr.sorbonne_u.components.cyphy.utils.aclocks.AcceleratedAndSimulationClock;
import fr.sorbonne_u.components.cyphy.utils.aclocks.ClocksServerWithSimulationCI;
import fr.sorbonne_u.components.cyphy.utils.aclocks.ClocksServerWithSimulationConnector;
import fr.sorbonne_u.components.cyphy.utils.aclocks.ClocksServerWithSimulationOutboundPort;
import fr.sorbonne_u.components.equipments.generator.connectors.GeneratorConnector;
import fr.sorbonne_u.components.equipments.generator.interfaces.GeneratorCI;
import fr.sorbonne_u.components.equipments.generator.mil.GeneratorOperationI;
import fr.sorbonne_u.components.equipments.generator.mil.LocalSimulationArchitectures;
import fr.sorbonne_u.components.equipments.generator.ports.GeneratorOutboundPort;
import fr.sorbonne_u.components.exceptions.BCMException;
import fr.sorbonne_u.components.exceptions.ComponentShutdownException;
import fr.sorbonne_u.components.exceptions.ComponentStartException;
import fr.sorbonne_u.components.utils.ExecutionType;
import fr.sorbonne_u.components.utils.SimulationType;
import fr.sorbonne_u.devs_simulation.architectures.Architecture;
import fr.sorbonne_u.devs_simulation.utils.InvariantChecking;
import fr.sorbonne_u.exceptions.ImplementationInvariantException;
import fr.sorbonne_u.exceptions.InvariantException;
import fr.sorbonne_u.exceptions.PreconditionException;
import fr.sorbonne_u.utils.aclocks.ClocksServer;

import java.time.Instant;
import java.util.concurrent.TimeUnit;

@RequiredInterfaces(required = {
        GeneratorCI.class,
        ClocksServerWithSimulationCI.class})
public class GeneratorUser extends AbstractCyPhyComponent implements GeneratorOperationI {

    /** when true, operations are traced.									*/
    public static boolean				VERBOSE = false ;
    /** when tracing, x coordinate of the window relative position.			*/
    public static int					X_RELATIVE_POSITION = 0;
    /** when tracing, y coordinate of the window relative position.			*/
    public static int					Y_RELATIVE_POSITION = 0;

    /** standard reflection, inbound port URI for the {@code GeneratorUser}
     *  component.															*/
    public static final String			REFLECTION_INBOUND_PORT_URI  = "GENERATOR-USER-REFLECTION-INBOUND-PORT-URI" ;

    protected GeneratorOutboundPort gop;
    protected String generatorInboundPortURI;

    // Execution/Simulation

    /** current type of execution.											*/
    protected final ExecutionType currentExecutionType;
    /** current type of simulation.											*/
    protected final SimulationType currentSimulationType;

    /** URI of the clock to be used to synchronise the test scenarios and
     *  the simulation.														*/
    protected final String				clockURI;
    /** URI of the global simulation architecture to be created or the
     *  empty string if the component does not execute as a simulation.		*/
    protected final String				globalArchitectureURI;
    /** URI of the local simulation architecture used to compose the global
     *  simulation architecture or the empty string if the component does
     *  not execute as a simulation.										*/
    protected final String				localArchitectureURI;
    /** time unit in which simulated times and durations are expressed.		*/
    protected final TimeUnit simulationTimeUnit;
    /** acceleration factor to be used when running the real time
     *  simulation.															*/
    protected double					accFactor;

    //  -------------------------------------------------------------------------
    // Invariants
    //  -------------------------------------------------------------------------

    protected static boolean	glassBoxInvariants(GeneratorUser instance)
    {
        assert 	instance != null : new PreconditionException("instance != null");

        boolean ret = true;
        ret &= InvariantChecking.checkGlassBoxInvariant(
                REFLECTION_INBOUND_PORT_URI != null &&
                !REFLECTION_INBOUND_PORT_URI.isEmpty(),
                GeneratorUser.class, instance,
                "REFLECTION_INBOUND_PORT_URI != null && "
                + "!REFLECTION_INBOUND_PORT_URI.isEmpty()");
        ret &= InvariantChecking.checkGlassBoxInvariant(
                X_RELATIVE_POSITION >= 0,
                GeneratorUser.class, instance,
                "X_RELATIVE_POSITION >= 0");
        ret &= InvariantChecking.checkGlassBoxInvariant(
                Y_RELATIVE_POSITION >= 0,
                GeneratorUser.class, instance,
                "Y_RELATIVE_POSITION >= 0");
        ret &= InvariantChecking.checkGlassBoxInvariant(
                instance.currentExecutionType != null,
                GeneratorUser.class, instance,
                "currentExecutionType != null");
        ret &= InvariantChecking.checkGlassBoxInvariant(
                instance.currentSimulationType != null,
                GeneratorUser.class, instance,
                "hcurrentSimulationType != null");
        ret &= InvariantChecking.checkGlassBoxInvariant(
                !instance.currentExecutionType.isStandard() ||
                instance.currentSimulationType.isNoSimulation(),
                GeneratorUser.class, instance,
                "!currentExecutionType.isStandard() || "
                + "currentSimulationType.isNoSimulation()");
        ret &= InvariantChecking.checkGlassBoxInvariant(
                instance.currentSimulationType.isNoSimulation() ||
                (instance.globalArchitectureURI != null &&
                 !instance.globalArchitectureURI.isEmpty()),
                GeneratorUser.class, instance,
                "currentSimulationType.isNoSimulation() || "
                + "(globalArchitectureURI != null && "
                + "!globalArchitectureURI.isEmpty())");
        ret &= InvariantChecking.checkGlassBoxInvariant(
                instance.currentSimulationType.isNoSimulation() ||
                (instance.localArchitectureURI != null &&
                 !instance.localArchitectureURI.isEmpty()),
                GeneratorUser.class, instance,
                "currentSimulationType.isNoSimulation() || "
                + "(localArchitectureURI != null && "
                + "!localArchitectureURI.isEmpty())");
        ret &= InvariantChecking.checkGlassBoxInvariant(
                !instance.currentSimulationType.isSILSimulation() ||
                instance.accFactor > 0.0,
                GeneratorUser.class, instance,
                "!instance.currentSimulationType.isSILSimulation() || "
                + "instance.accFactor > 0.0");
        return ret;
    }

    protected static boolean	blackBoxInvariants(GeneratorUser hd)
    {
        assert 	hd != null : new PreconditionException("hd != null");

        boolean ret = true;
        return ret;
    }

    //  -------------------------------------------------------------------------
    // Constructors
    //  -------------------------------------------------------------------------

    protected GeneratorUser() throws Exception
    {
        this(Generator.INBOUND_PORT_URI);
    }

    protected GeneratorUser(String inboundPortURI)
            throws Exception
    {
        this(inboundPortURI, ExecutionType.STANDARD);
    }

    protected GeneratorUser(
            String inboundPortURI,
            ExecutionType currentExecutionType
                                      ) throws Exception
    {
        this(REFLECTION_INBOUND_PORT_URI, inboundPortURI,
             currentExecutionType, SimulationType.NO_SIMULATION,
             null, null, null, 0.0, null);

        assert	currentExecutionType.isTest() :
                new PreconditionException("currentExecutionType.isTest()");
    }

    protected GeneratorUser(
            String reflectionInboundPortURI,
            String inboundPortURI,
            ExecutionType currentExecutionType,
            SimulationType currentSimulationType,
            String globalArchitectureURI,
            String localArchitectureURI,
            TimeUnit simulationTimeUnit,
            double accFactor,
            String clockURI
                           ) throws Exception
    {
        // one thread for execute and one schedulable for SIL scenarios
        super(reflectionInboundPortURI, 1, 1);

        assert	inboundPortURI != null &&
                  !inboundPortURI.isEmpty() :
                new PreconditionException(
                        "hairDryerInboundPortURI != null && "
                        + "!hairDryerInboundPortURI.isEmpty()");
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
                        "currentSimulationType.isNoSimulation() ||  "
                        + "(globalArchitectureURI != null && "
                        + "!globalArchitectureURI.isEmpty())");
        assert	currentSimulationType.isNoSimulation() ||
                  (localArchitectureURI != null &&
                   !localArchitectureURI.isEmpty()) :
                new PreconditionException(
                        "currentSimulationType.isNoSimulation() ||  "
                        + "(localArchitectureURI != null && "
                        + "!localArchitectureURI.isEmpty())");
        assert	!currentSimulationType.isSILSimulation() || accFactor > 0.0 :
                new PreconditionException(
                        "!currentSimulationType.isSILSimulation() || "
                        + "accFactor > 0.0");

        if (currentExecutionType.isStandard()) {
            throw new RuntimeException(
                    "GeneratorUser: standard execution is not implemented.");
        }

        this.currentExecutionType = currentExecutionType;
        this.currentSimulationType = currentSimulationType;
        this.globalArchitectureURI = globalArchitectureURI;
        this.localArchitectureURI = localArchitectureURI;
        this.simulationTimeUnit = simulationTimeUnit;
        this.accFactor = accFactor;
        this.clockURI = clockURI;

        this.initialise(inboundPortURI);

        assert	GeneratorUser.glassBoxInvariants(this) :
                new ImplementationInvariantException(
                        "GeneratorUser.glassBoxInvariants(this)");
        assert	GeneratorUser.blackBoxInvariants(this) :
                new InvariantException("GeneratorUser.blackBoxInvariants(this)");
    }

    protected void		initialise(
            String hairDryerInboundPortURI
                                    ) throws Exception
    {
        this.generatorInboundPortURI = hairDryerInboundPortURI;
        this.gop = new GeneratorOutboundPort(this);
        this.gop.publishPort();

        // create the local simulation architecture given the type of
        // simulation for the current run
        switch (this.currentSimulationType) {
            case MIL_SIMULATION:
                Architecture architecture =
                        LocalSimulationArchitectures.
                                createGeneratorUserMIL_Architecture(
                                        this.localArchitectureURI,
                                        this.simulationTimeUnit);
                assert	architecture.getRootModelURI().
                                      equals(this.localArchitectureURI) :
                        new AssertionError(
                                "local simulation architecture "
                                + this.localArchitectureURI
                                + " does not exist!");
                this.addLocalSimulatorArchitecture(architecture);
                this.global2localSimulationArchitectureURIS.
                        put(this.globalArchitectureURI, this.localArchitectureURI);
                break;
            case MIL_RT_SIMULATION:
                architecture =
                        LocalSimulationArchitectures.
                                createGeneratorUserMIL_RT_Architecture(
                                        this.localArchitectureURI,
                                        this.simulationTimeUnit,
                                        this.accFactor);
                assert	architecture.getRootModelURI().equals(
                        this.localArchitectureURI) :
                        new BCMException(
                                "local simulator " + this.localArchitectureURI
                                + " does not exist!");
                this.addLocalSimulatorArchitecture(architecture);
                this.global2localSimulationArchitectureURIS.
                        put(this.globalArchitectureURI, this.localArchitectureURI);
                break;
            case SIL_SIMULATION:
            case NO_SIMULATION:
            default:
                // in SIL simulations, the GeneratorUser component do not use
                // simulators, only global clock driven actions
        }

        if (VERBOSE) {
            this.tracer.get().setTitle("Generator user component");
            this.tracer.get().setRelativePosition(X_RELATIVE_POSITION,
                                                  Y_RELATIVE_POSITION);
            this.toggleTracing();
        }
    }

    //  -------------------------------------------------------------------------
    // Methods
    //  -------------------------------------------------------------------------

    @Override
    public void activate() {
        if (VERBOSE) {
            this.logMessage("GeneratorUser starting...");
        }
        try {
            this.gop.activate();
        } catch (Exception e) {
            throw new RuntimeException(e) ;
        }
    }

    @Override
    public void stop() {
        if (VERBOSE) {
            this.logMessage("GeneratorUser stopping...");
        }
        try {
            this.gop.stop();
        } catch (Exception e) {
            throw new RuntimeException(e) ;
        }

    }

    // -------------------------------------------------------------------------
    // Component internal tests
    // -------------------------------------------------------------------------

    protected void	testIsRunning() {
        if (VERBOSE) {
            this.logMessage("testIsRunning()");
        }
        try {
            assert !this.gop.isRunning();
        } catch (Exception e) {
            if (VERBOSE) {
                this.logMessage("Failed... -> " + e.getMessage());
            }
            assert false;
        }
        if (VERBOSE) {
            this.logMessage("done...\n");
        }
    }

    protected void	testActivate() {
        if (VERBOSE) {
            this.logMessage("testActivate()");
        }
        try {
            this.gop.activate();
            assert this.gop.isRunning();
        } catch (Exception e) {
            if (VERBOSE) {
                this.logMessage("Failed... -> " + e.getMessage());
            }
            assert false;
        }
        try {
            this.gop.stop();
            assert !this.gop.isRunning();
        } catch (Exception e) {
            throw new RuntimeException(e) ;
        }
        if (VERBOSE) {
            this.logMessage("done...\n");
        }
    }

    protected void runAllTests() {
        this.testIsRunning();
        this.testActivate();
    }

    // -------------------------------------------------------------------------
    // Component life-cycle
    // -------------------------------------------------------------------------

    @Override
    public synchronized void	start()
            throws ComponentStartException
    {
        super.start();


        try {
            this.doPortConnection(
                    this.gop.getPortURI(),
                    this.generatorInboundPortURI,
                    GeneratorConnector.class.getCanonicalName());

            switch (this.currentSimulationType) {
                case MIL_SIMULATION:
                    AtomicSimulatorPlugin asp = new AtomicSimulatorPlugin();
                    String uri = this.global2localSimulationArchitectureURIS.
                            get(this.globalArchitectureURI);
                    Architecture architecture =
                            (Architecture) this.localSimulationArchitectures.get(uri);
                    asp.setPluginURI(uri);
                    asp.setSimulationArchitecture(architecture);
                    this.installPlugin(asp);
                    break;
                case MIL_RT_SIMULATION:
                    RTAtomicSimulatorPlugin rtasp = new RTAtomicSimulatorPlugin();
                    uri = this.global2localSimulationArchitectureURIS.
                            get(this.globalArchitectureURI);
                    architecture =
                            (Architecture) this.localSimulationArchitectures.get(uri);
                    rtasp.setPluginURI(uri);
                    rtasp.setSimulationArchitecture(architecture);
                    this.installPlugin(rtasp);
                    break;
                case SIL_SIMULATION:
                    // in SIL simulations, the GeneratorUser component do not use
                    // simulators, only global clock driven actions
                case NO_SIMULATION:
                default:
            }
        } catch (Exception e) {
            throw new ComponentStartException(e) ;
        }
    }

    @Override
    public synchronized void execute() throws Exception
    {
        this.logMessage("GeneratorUser executes.");
        if (this.currentExecutionType.isTest() &&
            (this.currentSimulationType.isNoSimulation() ||
             this.currentSimulationType.isSILSimulation())) {
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
            this.logMessage("GeneratorUser gets the clock.");
            AcceleratedAndSimulationClock acceleratedClock =
                    clocksServerOutboundPort.getClockWithSimulation(this.clockURI);
            this.doPortDisconnection(clocksServerOutboundPort.getPortURI());
            clocksServerOutboundPort.unpublishPort();

            // Now, wait for the other components synchronising on the
            // accelerated clock
            this.logMessage("GeneratorUser waits until start time.");
            acceleratedClock.waitUntilStart();
            this.logMessage("Generator starts.");

            if (this.currentSimulationType.isNoSimulation()) {
                // In test execution types with no simulation, the component
                // executes a series of calls to the hair dryer to test all of
                // its methods.
                this.logMessage("GeneratorUser tests begin without simulation.");
                this.runAllTests();
                this.logMessage("GeneratorUser tests end.");
            } else {
                // synchronise with the start of the SIL simulation
                acceleratedClock.waitUntilSimulationStart();
                // execute the SIL test scenario
                silTestScenario(acceleratedClock);
            }
        }
    }

    @Override
    public synchronized void	finalise() throws Exception
    {
        this.doPortDisconnection(this.gop.getPortURI());

        super.finalise();
    }
    
    @Override
    public synchronized void	shutdown() throws ComponentShutdownException
    {
        try {
            this.gop.unpublishPort();
        } catch (Exception e) {
            throw new ComponentShutdownException(e) ;
        }
        super.shutdown();
    }

    protected void			silTestScenario(
            AcceleratedAndSimulationClock acceleratedClock
                                             )
    {
        assert	!acceleratedClock.simulationStartTimeNotReached() :
                new BCMException("!acceleratedClock.startTimeNotReached()");

        // Define the instants of the different actions in the scenario.
        Instant simulationStartInstant =
                acceleratedClock.getSimulationStartInstant();
        Instant switchOn = simulationStartInstant.plusSeconds(3600L);
        Instant setHigh = simulationStartInstant.plusSeconds(4500L);
        Instant setLow = simulationStartInstant.plusSeconds(6300L);
        Instant switchOff = simulationStartInstant.plusSeconds(7200L);

        // For each action, compute the waiting time for this action using the
        // above instant and the clock, and then schedule the rask that will
        // perform the action at the appropriate time.
        long delayInNanos = acceleratedClock.nanoDelayUntilInstant(switchOn);
        this.logMessage(
                "Generator#silTestScenario waits for " + delayInNanos
                + " " + TimeUnit.NANOSECONDS + " i.e., "
                + TimeUnit.NANOSECONDS.toMillis(delayInNanos)
                + " " + TimeUnit.MILLISECONDS
                + " to reach " + switchOn);
        this.scheduleTask(
                o -> { logMessage("GeneratorUser SIL test scenario begins.");
                    ((GeneratorUser)o).activate();
                },
                delayInNanos, TimeUnit.NANOSECONDS);
        delayInNanos = acceleratedClock.nanoDelayUntilInstant(setHigh);
        this.logMessage(
                "Generator#silTestScenario waits for " + delayInNanos
                + " " + TimeUnit.NANOSECONDS + " i.e., "
                + TimeUnit.NANOSECONDS.toMillis(delayInNanos)
                + " " + TimeUnit.MILLISECONDS
                + " to reach " + setHigh);
        this.scheduleTask(
                o -> ((GeneratorUser)o).stop(),
                delayInNanos, TimeUnit.NANOSECONDS);
        delayInNanos = acceleratedClock.nanoDelayUntilInstant(setLow);
        this.logMessage(
                "Generator#silTestScenario waits for " + delayInNanos
                + " " + TimeUnit.NANOSECONDS + " i.e., "
                + TimeUnit.NANOSECONDS.toMillis(delayInNanos)
                + " " + TimeUnit.MILLISECONDS
                + " to reach " + setLow);

    }
}
