package fr.sorbonne_u.components.equipments.toaster;

import fr.sorbonne_u.components.cyphy.AbstractCyPhyComponent;
import fr.sorbonne_u.components.cyphy.plugins.devs.AtomicSimulatorPlugin;
import fr.sorbonne_u.components.cyphy.plugins.devs.RTAtomicSimulatorPlugin;
import fr.sorbonne_u.components.cyphy.utils.aclocks.AcceleratedAndSimulationClock;
import fr.sorbonne_u.components.cyphy.utils.aclocks.ClocksServerWithSimulationConnector;
import fr.sorbonne_u.components.cyphy.utils.aclocks.ClocksServerWithSimulationOutboundPort;
import fr.sorbonne_u.components.equipments.toaster.mil.LocalSimulationArchitecture;
import fr.sorbonne_u.components.equipments.toaster.mil.ToasterOperationI;
import fr.sorbonne_u.components.equipments.toaster.mil.ToasterStateModel;
import fr.sorbonne_u.components.exceptions.BCMException;
import fr.sorbonne_u.components.exceptions.ComponentShutdownException;
import fr.sorbonne_u.components.exceptions.ComponentStartException;
import fr.sorbonne_u.components.utils.ExecutionType;
import fr.sorbonne_u.components.utils.SimulationType;
import fr.sorbonne_u.devs_simulation.architectures.Architecture;
import fr.sorbonne_u.devs_simulation.models.time.Time;
import fr.sorbonne_u.exceptions.ImplementationInvariantException;
import fr.sorbonne_u.exceptions.InvariantChecking;
import fr.sorbonne_u.exceptions.InvariantException;
import fr.sorbonne_u.exceptions.PreconditionException;
import fr.sorbonne_u.utils.aclocks.ClocksServer;

import java.time.Instant;
import java.util.concurrent.TimeUnit;

public class ToasterUser extends AbstractCyPhyComponent implements ToasterOperationI {
    // -------------------------------------------------------------------------
    // Constants and variables
    // -------------------------------------------------------------------------

    public static boolean VERBOSE = false;
    public static int X_RELATIVE_POSITION = 0;
    public static int Y_RELATIVE_POSITION = 0;

    public static final String REFLECTION_INBOUND_PORT_URI = "TOASTER-USER-RIP-URI";
    protected ToasterOutboundPort top;
    protected String toasterInboundPortURI;

    //Execution / Simulation
    protected final ExecutionType currentExecutionType;
    protected final SimulationType currentSimulationType;

    protected final String clockURI;
    protected final String globalArchitectureURI;
    protected final String localArchitectureURI;
    protected final TimeUnit simulationTimeUnit;
    protected double accFactor;

    // -------------------------------------------------------------------------
    // Invariants
    // -------------------------------------------------------------------------
    protected static boolean glassBoxInvariant(ToasterUser model) {
        assert model != null :
                new PreconditionException("Invariant violation: model is null");

        boolean ret = true;
        ret &= InvariantChecking.checkGlassBoxInvariant(
                REFLECTION_INBOUND_PORT_URI != null && !REFLECTION_INBOUND_PORT_URI.isEmpty(),
                ToasterUser.class,
                model,
                "REFLECTION_INBOUND_PORT_URI != null && !REFLECTION_INBOUND_PORT_URI.isEmpty()"
                                                       );
        ret &= InvariantChecking.checkGlassBoxInvariant(
                X_RELATIVE_POSITION >= 0,
                ToasterUser.class,
                model,
                "X_RELATIVE_POSITION >= 0"
                                                       );
        ret &= InvariantChecking.checkGlassBoxInvariant(
                Y_RELATIVE_POSITION >= 0,
                ToasterUser.class,
                model,
                "Y_RELATIVE_POSITION >= 0"
                                                       );
        ret &= InvariantChecking.checkGlassBoxInvariant(
                model.currentExecutionType != null,
                ToasterUser.class,
                model,
                "currentExecutionType != null"
                                                       );
        ret &= InvariantChecking.checkGlassBoxInvariant(
                model.currentSimulationType != null,
                ToasterUser.class,
                model,
                "currentSimulationType != null"
                                                       );
        ret &= InvariantChecking.checkGlassBoxInvariant(
                !model.currentExecutionType.isStandard() || model.currentSimulationType.isNoSimulation(),
                ToasterUser.class,
                model,
                "!model.currentExecutionType.isStandard() || model.currentSimulationType.isNoSimulation()"
                                                       );
        ret &= InvariantChecking.checkGlassBoxInvariant(
                model.currentSimulationType.isNoSimulation() || (model.globalArchitectureURI != null && !model.globalArchitectureURI.isEmpty()),
                ToasterUser.class,
                model,
                "model.currentSimulationType.isNoSimulation() || (model.globalArchitectureURI != null && !model.globalArchitectureURI.isEmpty())"
                                                       );
        ret &= InvariantChecking.checkGlassBoxInvariant(
                model.currentSimulationType.isNoSimulation() || (model.localArchitectureURI != null && !model.localArchitectureURI.isEmpty()),
                ToasterUser.class,
                model,
                "model.currentSimulationType.isNoSimulation() || (model.localArchitectureURI != null && !model.localArchitectureURI.isEmpty())"
                                                       );
        ret &= InvariantChecking.checkGlassBoxInvariant(
                model.currentSimulationType.isSILSimulation() || model.accFactor > 0.0,
                ToasterUser.class,
                model,
                "model.currentSimulationType.isSILSimulation() || model.accFactor > 0.0"
                                                       );
        return ret;
    }

    protected static boolean blackBoxInvariant(ToasterUser model) {
        assert model != null :
                new PreconditionException("Invariant violation: model is null");

        boolean ret = true;
        return ret;
    }

    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------
    protected ToasterUser() throws Exception {
        this(Toaster.INBOUND_PORT_URI);
    }

    protected ToasterUser(String toasterInboundPortURI) throws Exception {
        this(
                toasterInboundPortURI,
                ExecutionType.STANDARD
            );
    }

    protected ToasterUser(String toasterInboundPortURI, ExecutionType et) throws Exception {
        this(REFLECTION_INBOUND_PORT_URI, toasterInboundPortURI, et, SimulationType.NO_SIMULATION, null, null, null, 0.0, null);
    }

    protected ToasterUser(
            String reflectionInboundPortURI,
            String toasterInboundPortURI,
            ExecutionType currentExecutionType,
            SimulationType currentSimulationType,
            String globalArchitectureURI,
            String localArchitectureURI,
            TimeUnit simulationTimeUnit,
            double accFactor,
            String clockURI
                         ) throws Exception {
        super(reflectionInboundPortURI, 1, 1);
        assert toasterInboundPortURI != null && !toasterInboundPortURI.isEmpty() :
                new PreconditionException("Invariant violation: toasterInboundPortURI != null && !toasterInboundPortURI.isEmpty()");
        assert currentExecutionType != null :
                new PreconditionException("Invariant violation: currentExecutionType != null");
        assert currentExecutionType.isStandard() || clockURI != null && !clockURI.isEmpty() :
                new PreconditionException("Invariant violation: currentExecutionType.isStandard() || clockURI != null && !clockURI.isEmpty()");
        assert !currentExecutionType.isStandard() || currentSimulationType.isNoSimulation() :
                new PreconditionException("Invariant violation: !currentExecutionType.isStandard() || currentSimulationType.isNoSimulation()");
        assert currentSimulationType.isNoSimulation() || (globalArchitectureURI != null && !globalArchitectureURI.isEmpty()) :
                new PreconditionException("Invariant violation: currentSimulationType.isNoSimulation() || (globalArchitectureURI != null && !globalArchitectureURI.isEmpty())");
        assert currentSimulationType.isNoSimulation() || (localArchitectureURI != null && !localArchitectureURI.isEmpty()) :
                new PreconditionException("Invariant violation: currentSimulationType.isNoSimulation() || (localArchitectureURI != null && !localArchitectureURI.isEmpty())");
        assert !currentSimulationType.isSILSimulation() || accFactor > 0.0 :
                new PreconditionException("Invariant violation: !currentSimulationType.isSILSimulation() || accFactor > 0.0");

        if (currentExecutionType.isStandard()) {
            throw new RuntimeException(
                    "ToasterUser: standard execution is not implemented.");
        }

        this.currentExecutionType = currentExecutionType;
        this.currentSimulationType = currentSimulationType;
        this.globalArchitectureURI = globalArchitectureURI;
        this.localArchitectureURI = localArchitectureURI;
        this.simulationTimeUnit = simulationTimeUnit;
        this.accFactor = accFactor;
        this.clockURI = clockURI;

        this.initialise(toasterInboundPortURI);

        assert ToasterUser.glassBoxInvariant(this) :
                new ImplementationInvariantException("Invariant violation: ToasterUser.glassBoxInvariant(this)");
        assert ToasterUser.blackBoxInvariant(this) :
                new ImplementationInvariantException("Invariant violation: ToasterUser.blackBoxInvariant(this)");


    }

    protected void initialise(String toasterInboundPortURI) throws Exception {
        this.toasterInboundPortURI = toasterInboundPortURI;
        this.top = new ToasterOutboundPort(this);
        this.top.publishPort();

        switch (this.currentSimulationType) {
            case MIL_SIMULATION:
                Architecture architecture = LocalSimulationArchitecture.createToasterUserMILLocalArchitecture(
                        this.localArchitectureURI,
                        this.simulationTimeUnit
                                                                                                             );

                assert architecture.getRootModelURI().equals(this.localArchitectureURI) :
                        new BCMException(
                                "local simulator " + this.localArchitectureURI
                                + " does not exist!");
                this.addLocalSimulatorArchitecture(architecture);
                this.global2localSimulationArchitectureURIS.
                        put(this.globalArchitectureURI, this.localArchitectureURI);
                break;

            case MIL_RT_SIMULATION:
                architecture = LocalSimulationArchitecture.createToasterUserMILRT_LocalArchitecture(
                        this.localArchitectureURI,
                        this.simulationTimeUnit,
                        this.accFactor
                                                                                                   );
                assert architecture.getRootModelURI().equals(this.localArchitectureURI) :
                        new BCMException(
                                "local simulator " + this.localArchitectureURI
                                + " does not exist!");
                this.addLocalSimulatorArchitecture(architecture);
                this.global2localSimulationArchitectureURIS.
                        put(this.globalArchitectureURI, this.localArchitectureURI);
            case SIL_SIMULATION:
            case NO_SIMULATION:
            default:
        }

        if (VERBOSE) {
            this.tracer.get().setTitle("Heater user component");
            this.tracer.get().setRelativePosition(
                    X_RELATIVE_POSITION,
                    Y_RELATIVE_POSITION
                                                 );
            this.toggleTracing();
        }

        // Invariant checking
        assert glassBoxInvariant(this) :
                new ImplementationInvariantException("glassBoxInvariants(this)");
        assert blackBoxInvariant(this) :
                new InvariantException("blackBoxInvariants(this)");
    }


    // -------------------------------------------------------------------------
    // Component internal testing method triggered by the SIL simulator
    // -------------------------------------------------------------------------

    @Override
    public void setToasterState(ToasterStateModel.ToasterState state, Time t) {
        if (VERBOSE) {
            this.logMessage("ToasterUser setting state to " + state);
        }

        try {
            switch (state) {
                case ON:
                    this.top.turnOn();
                    break;
                case OFF:
                    this.top.turnOff();
                    break;
                default:
                    throw new Exception("Unknown state " + state);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void setToasterBrowningLevel(ToasterStateModel.ToasterBrowningLevel bl, Time t) {
        if (VERBOSE) {
            this.logMessage("ToasterUser setting browning level to " + bl);
        }

        try {
            this.top.setBrowningLevel(bl);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public ToasterStateModel.ToasterState getToasterState() {
        if (VERBOSE) {
            this.logMessage("ToasterUser getting state");
        }
        ToasterStateModel.ToasterState state = null;
        try {
            state = this.top.getState();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return state;
    }

    @Override
    public ToasterStateModel.ToasterBrowningLevel getToasterBrowningLevel() throws Exception {

        if (VERBOSE) {
            this.logMessage("ToasterUser getting browning level");
        }
        ToasterStateModel.ToasterBrowningLevel bl = null;
        try {
            bl = this.top.getBrowningLevel();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return bl;
    }

    // -------------------------------------------------------------------------
    // Component internal tests
    // -------------------------------------------------------------------------

    public void testGetState() throws Exception {
        if (VERBOSE) {
            this.logMessage("ToasterUser testing getState");
        }

        try{
            assert(ToasterStateModel.ToasterState.OFF == this.top.getState());
        } catch (Exception e) {
            this.logMessage("ToasterUser testGetState failed");
            throw new RuntimeException(e);
        }
        this.logMessage("ToasterUser testGetState succeeded");
    }

    public void testGetBrowingLevel(){
        if (VERBOSE) {
            this.logMessage("ToasterUser testing getBrowningLevel");
        }

        try {
            assert(ToasterStateModel.ToasterBrowningLevel.DEFROST == this.top.getBrowningLevel());
        } catch (Exception e) {
            this.logMessage("ToasterUser testGetBrowningLevel failed");
            throw new RuntimeException(e);
        }
        this.logMessage("ToasterUser testGetBrowningLevel succeeded");
    }

    public void testSetState(){
        if (VERBOSE) {
            this.logMessage("ToasterUser testing setState");
        }
        try {
            assert (ToasterStateModel.ToasterState.OFF == this.top.getState());
            this.top.turnOn();
            assert(ToasterStateModel.ToasterState.ON == this.top.getState());
            assert (ToasterStateModel.ToasterBrowningLevel.DEFROST == this.top.getBrowningLevel());
        } catch (Exception e) {
            this.logMessage("ToasterUser testSetState failed");
            throw new RuntimeException(e);
        }

        try{
            this.top.turnOff();
            assert(ToasterStateModel.ToasterState.OFF == this.top.getState());
        } catch (Exception e) {
            this.logMessage("ToasterUser testSetState failed");
            throw new RuntimeException(e);
        }

        this.logMessage("ToasterUser testSetState succeeded");
    }

    public void testSetBrowningLevel(){
        if (VERBOSE) {
            this.logMessage("ToasterUser testing setBrowningLevel");
        }

        try {
            this.top.turnOn();
            assert (ToasterStateModel.ToasterBrowningLevel.DEFROST == this.top.getBrowningLevel());
            this.top.setBrowningLevel(ToasterStateModel.ToasterBrowningLevel.LOW);
            assert(ToasterStateModel.ToasterBrowningLevel.LOW == this.top.getBrowningLevel());
            this.top.setBrowningLevel(ToasterStateModel.ToasterBrowningLevel.MEDIUM);
            assert(ToasterStateModel.ToasterBrowningLevel.MEDIUM == this.top.getBrowningLevel());
            this.top.setBrowningLevel(ToasterStateModel.ToasterBrowningLevel.HIGH);
            assert(ToasterStateModel.ToasterBrowningLevel.HIGH == this.top.getBrowningLevel());
            this.top.setBrowningLevel(ToasterStateModel.ToasterBrowningLevel.DEFROST);
            assert(ToasterStateModel.ToasterBrowningLevel.DEFROST == this.top.getBrowningLevel());
            this.top.turnOff();
            assert (ToasterStateModel.ToasterBrowningLevel.DEFROST == this.top.getBrowningLevel());
        } catch (Exception e) {
            this.logMessage("ToasterUser testSetBrowningLevel failed");
            throw new RuntimeException(e);
        }

        this.logMessage("ToasterUser testSetBrowningLevel succeeded");
    }

    protected void			runAllTests()
    {
        try {
            this.testGetState();
            this.testGetBrowingLevel();
            this.testSetState();
            this.testSetBrowningLevel();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // -------------------------------------------------------------------------
    // Component life-cycle methods
    // -------------------------------------------------------------------------

    @Override
    public synchronized void	start()
            throws ComponentStartException
    {
        super.start();

        try {
            this.doPortConnection(
                    this.top.getPortURI(),
                    this.toasterInboundPortURI,
                    Toaster.INBOUND_PORT_URI
                                 );
            switch (this.currentSimulationType){
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
                    // in SIL simulations, the HairDryerUser component do not use
                    // simulators, only global clock driven actions
                case NO_SIMULATION:
                default:
            }
        } catch (Exception e) {
            throw new ComponentStartException(e);
        }
    }

    public synchronized void execute() {
        this.logMessage("ToasterUser executing");
        try {
            if (this.currentExecutionType.isTest() &&
                (
                        this.currentSimulationType.isNoSimulation() ||
                        this.currentSimulationType.isSILSimulation()
                )) {
                ClocksServerWithSimulationOutboundPort clocksServerOutboundPort =
                        new ClocksServerWithSimulationOutboundPort(this);
                clocksServerOutboundPort.publishPort();
                this.doPortConnection(
                        clocksServerOutboundPort.getPortURI(),
                        ClocksServer.STANDARD_INBOUNDPORT_URI,
                        ClocksServerWithSimulationConnector.class.getCanonicalName()
                                     );
                this.logMessage("HairDryerUser gets the clock.");
                AcceleratedAndSimulationClock acceleratedClock =
                        clocksServerOutboundPort.getClockWithSimulation(this.clockURI);
                this.doPortDisconnection(clocksServerOutboundPort.getPortURI());
                clocksServerOutboundPort.unpublishPort();

                this.logMessage("Toaster wait until the start of the simulation.");
                acceleratedClock.waitUntilStart();
                this.logMessage("ToasterUser starts the tests.");


                if (this.currentSimulationType.isNoSimulation()) {
                    // In test execution types with no simulation, the component
                    // executes a series of calls to the hair dryer to test all of
                    // its methods.
                    this.logMessage("ToasterUser tests begin without simulation.");
                    this.runAllTests();
                    this.logMessage("ToasterUser tests end.");
                } else {
                    // synchronise with the start of the SIL simulation
                    acceleratedClock.waitUntilSimulationStart();
                    // execute the SIL test scenario
                    silTestScenario(acceleratedClock);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public synchronized void	finalise() throws Exception
    {
        this.doPortDisconnection(this.top.getPortURI());

        super.finalise();
    }

    /**
     * @see fr.sorbonne_u.components.AbstractComponent#shutdown()
     */
    @Override
    public synchronized void	shutdown() throws ComponentShutdownException
    {
        try {
            this.top.unpublishPort();
        } catch (Exception e) {
            throw new ComponentShutdownException(e) ;
        }
        super.shutdown();
    }

    // -------------------------------------------------------------------------
    // SIL test scenarios
    // -------------------------------------------------------------------------
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
        Instant setMiddium = simulationStartInstant.plusSeconds(6300L);
        Instant setLow = simulationStartInstant.plusSeconds(7200L);
        Instant setDefrost = simulationStartInstant.plusSeconds(8100L);
        Instant switchOff = simulationStartInstant.plusSeconds(9000L);

        // For each action, compute the waiting time for this action using the
        // above instant and the clock, and then schedule the rask that will
        // perform the action at the appropriate time.
        long delayInNanos = acceleratedClock.nanoDelayUntilInstant(switchOn);
            this.logMessage(
                    "HairDryer#silTestScenario waits for " + delayInNanos
                    + " " + TimeUnit.NANOSECONDS + " i.e., "
                    + TimeUnit.NANOSECONDS.toMillis(delayInNanos)
                    + " " + TimeUnit.MILLISECONDS
                    + " to reach " + switchOn);
            this.scheduleTask(
                    o -> {
                        logMessage("HairDryerUser SIL test scenario begins.");
                        try {
                            ((ToasterUser) o).setToasterState(ToasterStateModel.ToasterState.ON, null);
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    },
                    delayInNanos, TimeUnit.NANOSECONDS
                             );
            delayInNanos = acceleratedClock.nanoDelayUntilInstant(setHigh);
            this.logMessage(
                    "HairDryer#silTestScenario waits for " + delayInNanos
                    + " " + TimeUnit.NANOSECONDS + " i.e., "
                    + TimeUnit.NANOSECONDS.toMillis(delayInNanos)
                    + " " + TimeUnit.MILLISECONDS
                    + " to reach " + setHigh);
            this.scheduleTask(
                    o -> {
                        try {
                            ((ToasterUser) o).setToasterBrowningLevel(ToasterStateModel.ToasterBrowningLevel.HIGH, null);
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    },
                    delayInNanos, TimeUnit.NANOSECONDS
                             );
            delayInNanos = acceleratedClock.nanoDelayUntilInstant(setMiddium);
            this.logMessage(
                    "HairDryer#silTestScenario waits for " + delayInNanos
                    + " " + TimeUnit.NANOSECONDS + " i.e., "
                    + TimeUnit.NANOSECONDS.toMillis(delayInNanos)
                    + " " + TimeUnit.MILLISECONDS
                    + " to reach " + setMiddium);
            this.scheduleTask(
                    o -> {
                        try {
                            ((ToasterUser) o).setToasterBrowningLevel(ToasterStateModel.ToasterBrowningLevel.MEDIUM, null);
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    },
                    delayInNanos, TimeUnit.NANOSECONDS
                             );
            delayInNanos = acceleratedClock.nanoDelayUntilInstant(setLow);
            this.logMessage(
                    "HairDryer#silTestScenario waits for " + delayInNanos
                    + " " + TimeUnit.NANOSECONDS + " i.e., "
                    + TimeUnit.NANOSECONDS.toMillis(delayInNanos)
                    + " " + TimeUnit.MILLISECONDS
                    + " to reach " + setLow);
            this.scheduleTask(
                    o -> {
                        try {
                            ((ToasterUser) o).setToasterBrowningLevel(ToasterStateModel.ToasterBrowningLevel.LOW, null);
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    },
                    delayInNanos, TimeUnit.NANOSECONDS
                             );
            delayInNanos = acceleratedClock.nanoDelayUntilInstant(setDefrost);
            this.logMessage(
                    "HairDryer#silTestScenario waits for " + delayInNanos
                    + " " + TimeUnit.NANOSECONDS + " i.e., "
                    + TimeUnit.NANOSECONDS.toMillis(delayInNanos)
                    + " " + TimeUnit.MILLISECONDS
                    + " to reach " + setDefrost);
            this.scheduleTask(
                    o -> {
                        try {
                            ((ToasterUser) o).setToasterBrowningLevel(ToasterStateModel.ToasterBrowningLevel.DEFROST, null);
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    },
                    delayInNanos, TimeUnit.NANOSECONDS
                             );
            delayInNanos = acceleratedClock.nanoDelayUntilInstant(switchOff);
            this.logMessage(
                    "HairDryer#silTestScenario waits for " + delayInNanos
                    + " " + TimeUnit.NANOSECONDS + " i.e., "
                    + TimeUnit.NANOSECONDS.toMillis(delayInNanos)
                    + " " + TimeUnit.MILLISECONDS
                    + " to reach " + switchOff);
            this.scheduleTask(
                    o -> {
                        try {
                            ((ToasterUser) o).setToasterState(ToasterStateModel.ToasterState.OFF, null);
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                        ;
                        logMessage("HairDryerUser SIL test scenario ends.");
                    },
                    delayInNanos, TimeUnit.NANOSECONDS
                             );
    }

}
