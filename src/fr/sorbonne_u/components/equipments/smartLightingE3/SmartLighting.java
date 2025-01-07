package fr.sorbonne_u.components.equipments.smartLightingE3;

import fr.sorbonne_u.components.AbstractComponent;
import fr.sorbonne_u.components.annotations.OfferedInterfaces;
import fr.sorbonne_u.components.annotations.RequiredInterfaces;
import fr.sorbonne_u.components.cyphy.AbstractCyPhyComponent;
import fr.sorbonne_u.components.cyphy.plugins.devs.AtomicSimulatorPlugin;
import fr.sorbonne_u.components.cyphy.plugins.devs.RTAtomicSimulatorPlugin;
import fr.sorbonne_u.components.cyphy.utils.aclocks.AcceleratedAndSimulationClock;
import fr.sorbonne_u.components.cyphy.utils.aclocks.ClocksServerWithSimulationConnector;
import fr.sorbonne_u.components.cyphy.utils.aclocks.ClocksServerWithSimulationOutboundPort;
import fr.sorbonne_u.components.equipments.hem.HEM;
import fr.sorbonne_u.components.equipments.hem.registration.RegistrationCI;
import fr.sorbonne_u.components.equipments.hem.registration.RegistrationConnector;
import fr.sorbonne_u.components.equipments.hem.registration.RegistrationOutboundPort;
import fr.sorbonne_u.components.equipments.smartLighting.connections.SmartLightingExternalControlInboundPort;
import fr.sorbonne_u.components.equipments.smartLighting.connections.SmartLightingInternalControlInboundPort;
import fr.sorbonne_u.components.equipments.smartLighting.connections.SmartLightingUserInboundPort;
import fr.sorbonne_u.components.equipments.smartLighting.interfaces.*;
import fr.sorbonne_u.components.equipments.smartLightingE3.connections.SmartLightingActuatorInboundPort;
import fr.sorbonne_u.components.equipments.smartLightingE3.connections.SmartLightingSensorDataInboundPort;
import fr.sorbonne_u.components.equipments.smartLightingE3.measures.SmartLightingCompoundMeasure;
import fr.sorbonne_u.components.equipments.smartLightingE3.measures.SmartLightingSensorData;
import fr.sorbonne_u.components.equipments.smartLightingE3.mil.SmartLightingIlluminanceModel;
import fr.sorbonne_u.components.exceptions.BCMException;
import fr.sorbonne_u.components.exceptions.ComponentShutdownException;
import fr.sorbonne_u.components.exceptions.ComponentStartException;
import fr.sorbonne_u.components.utils.ExecutionType;
import fr.sorbonne_u.components.utils.Measure;
import fr.sorbonne_u.components.utils.MeasurementUnit;
import fr.sorbonne_u.components.utils.SimulationType;
import fr.sorbonne_u.devs_simulation.architectures.Architecture;
import fr.sorbonne_u.devs_simulation.models.time.Duration;
import fr.sorbonne_u.devs_simulation.models.time.Time;
import fr.sorbonne_u.devs_simulation.utils.InvariantChecking;
import fr.sorbonne_u.exceptions.InvariantException;
import fr.sorbonne_u.exceptions.PostconditionException;
import fr.sorbonne_u.exceptions.PreconditionException;
import fr.sorbonne_u.utils.aclocks.AcceleratedClock;
import fr.sorbonne_u.utils.aclocks.ClocksServer;

import java.util.HashMap;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

@OfferedInterfaces(offered = {
        SmartLightingUserCI.class,
        SmartLightingInternalControlCI.class,
        SmartLightingExternalControlCI.class
})
@RequiredInterfaces(required = {RegistrationCI.class})
public class SmartLighting extends AbstractCyPhyComponent implements SmartLightingUserI, SmartLightingInternalControlI {

    // ------------------------------------------------------------------------
    // Inner interfaces and types
    // ------------------------------------------------------------------------
    public static enum SmartLightingState {
        ON,
        OFF,
        INCREASE,
        DECREASE
    }

    protected static enum SmartLightingSensorMeasures{
        LIGHTING_STATUS,
        TARGET_ILLUMINATION,
        CURRENT_ILLUMINATION,
        COMPOUND_ILLUMINATION
    }

    // ------------------------------------------------------------------------
    // Constants and variables
    // ------------------------------------------------------------------------
    public static final String REFLECTION_INBOUND_PORT_URI = "Smart-Lighting-RIP-URI";
    public static final String URI = "SMART-LIGHTING-URI";

    //power level and illumination
    protected static final double MAX_POWER_LEVEL = 100.0;
    protected static final double STANDARD_TARGET_ILLUMINATION = 100.0;
    protected static final MeasurementUnit ILLUMINATION_UNIT = MeasurementUnit.LUX;
    protected SmartLightingState currentState;
    protected double currentPowerLevel;
    protected double targetIllumination;

    // Inbound port URIs
    public static final String USER_INBOUND_PORT_URI = "SMART-LIGHTING-USER-INBOUND-PORT-URI";
    public static final String INTERNAL_CONTROL_INBOUND_PORT_URI = "SMART-LIGHTING-INTERNAL-CONTROL-INBOUND-PORT-URI";
    public static final String EXTERNAL_CONTROL_INBOUND_PORT_URI = "SMART-LIGHTING-EXTERNAL-CONTROL-INBOUND-PORT-URI";
    public static final String SENSOR_INBOUND_PORT_URI = "SMART-LIGHTING-SENSOR-INBOUND-PORT-URI";
    public static final String ACTUATOR_INBOUND_PORT_URI = "SMART-LIGHTING-ACTUATOR-INBOUND-PORT-URI";

    protected static boolean VERBOSE = true;

    // Position of the trace window
    public static int X_RELATIVE_POSITION = 0;
    public static int Y_RELATIVE_POSITION = 0;

    // Outbound ports
    protected SmartLightingUserInboundPort slip;
    protected SmartLightingInternalControlInboundPort slcip;
    protected SmartLightingExternalControlInboundPort sleip;

    //Sensors/Actuators
    protected SmartLightingSensorDataInboundPort sensorDataInboundPort;
    protected SmartLightingActuatorInboundPort actuatorInboundPort;

    //Exwcution/Simulation
    protected final String clockURI;
    protected final CompletableFuture<AcceleratedClock> clock;
    protected final ExecutionType currentExecutionType;
    protected final SimulationType currentSimulationType;
    protected AtomicSimulatorPlugin asp;
    protected final String				globalArchitectureURI;
    protected final String				localArchitectureURI;
    protected final TimeUnit simulationTimeUnit;
    protected double					accFactor;
    protected static final String CURRENT_LIGHT_INTENDITY_NAME = "currentLightIntensity";


    // Registration
    public static boolean TEST_REGISTRATION = false;
    protected RegistrationOutboundPort registrationPort;
    protected boolean isHEMConnectionRequired;
    protected static final String XML_PATH = "smart-lighting-descriptor.xml";

    // ------------------------------------------------------------------------
    // Invairants
    // ------------------------------------------------------------------------
    protected static boolean glassBoxInvariants(SmartLighting sm){
        assert sm != null : new PreconditionException("sm != null");

        boolean ret = true;
        ret &= InvariantChecking.checkGlassBoxInvariant(
                MAX_POWER_LEVEL >= 0,
                SmartLighting.class,
                sm,
                "MAX_POWER_LEVEL >= 0"
                                                       );
        ret &= InvariantChecking.checkGlassBoxInvariant(
                sm.currentState != null,
                SmartLighting.class,
                sm,
                "sm.currentState != null"
                                                       );
        ret &= InvariantChecking.checkGlassBoxInvariant(
                sm.targetIllumination >= 0,
                SmartLighting.class,
                sm,
                "sm.targetIllumination >= 0"
                                                       );
        ret &= InvariantChecking.checkGlassBoxInvariant(
                sm.currentPowerLevel >= 0,
                SmartLighting.class,
                sm,
                "sm.currentPowerLevel >= 0"
                                                       );
        ret &= InvariantChecking.checkGlassBoxInvariant(
                sm.currentExecutionType != null,
                SmartLighting.class,
                sm,
                "sm.currentExecutionType != null"
                                                       );
        ret &= InvariantChecking.checkGlassBoxInvariant(
                sm.currentSimulationType != null,
                SmartLighting.class,
                sm,
                "sm.currentSimulationType != null"
                                                       );
        ret &= InvariantChecking.checkGlassBoxInvariant(
                !sm.currentExecutionType.isStandard() || sm.currentSimulationType.isNoSimulation(),
                SmartLighting.class,
                sm,
                "!sm.currentExecutionType.isStandard() || sm.currentSimulationType.isNoSimulation()"
                                                       );
        ret &= InvariantChecking.checkGlassBoxInvariant(
                sm.currentSimulationType.isNoSimulation() || (sm.globalArchitectureURI != null && sm.globalArchitectureURI.isEmpty()),
                SmartLighting.class,
                sm,
                "sm.currentSimulationType.isNoSimulation() || (sm.globalArchitectureURI != null && sm.globalArchitectureURI.isEmpty())"
                                                       );
        ret &= InvariantChecking.checkGlassBoxInvariant(
                sm.currentSimulationType.isSimulated() || sm.simulationTimeUnit != null,
                SmartLighting.class,
                sm,
                "sm.currentSimulationType.isSimulated() || sm.simulationTimeUnit != null"
                                                       );
        ret &= InvariantChecking.checkGlassBoxInvariant(
                !sm.currentSimulationType.isRealTimeSimulation() || sm.accFactor > 0.0,
                SmartLighting.class,
                sm,
                "!sm.currentSimulationType.isRealTimeSimulation() || sm.accFactor > 0.0"
                                                       );
        return ret;
    }

    protected static boolean blackBoxInvariants(SmartLighting sm){
        assert sm != null : new PreconditionException("sm != null");

        boolean ret = true;
        ret &= InvariantChecking.checkBlackBoxInvariant(
                REFLECTION_INBOUND_PORT_URI != null && !REFLECTION_INBOUND_PORT_URI.isEmpty(),
                SmartLighting.class,
                sm,
                "REFLECTION_INBOUND_PORT_URI != null && !REFLECTION_INBOUND_PORT_URI.isEmpty()"
                                                       );
        ret &= InvariantChecking.checkBlackBoxInvariant(
                ILLUMINATION_UNIT != null,
                SmartLighting.class,
                sm,
                "ILLUMINATION_UNIT != null"
                                                       );
        ret &= InvariantChecking.checkBlackBoxInvariant(
                INTERNAL_CONTROL_INBOUND_PORT_URI != null && !INTERNAL_CONTROL_INBOUND_PORT_URI.isEmpty(),
                SmartLighting.class,
                sm,
                "INTERNAL_CONTROL_INBOUND_PORT_URI != null && !INTERNAL_CONTROL_INBOUND_PORT_URI.isEmpty()"
                                                       );
        ret &= InvariantChecking.checkBlackBoxInvariant(
                EXTERNAL_CONTROL_INBOUND_PORT_URI != null && !EXTERNAL_CONTROL_INBOUND_PORT_URI.isEmpty(),
                SmartLighting.class,
                sm,
                "EXTERNAL_CONTROL_INBOUND_PORT_URI != null && !EXTERNAL_CONTROL_INBOUND_PORT_URI.isEmpty()"
                                                       );
        ret &= InvariantChecking.checkBlackBoxInvariant(
                SENSOR_INBOUND_PORT_URI != null && !SENSOR_INBOUND_PORT_URI.isEmpty(),
                SmartLighting.class,
                sm,
                "SENSOR_INBOUND_PORT_URI != null && !SENSOR_INBOUND_PORT_URI.isEmpty()"
                                                       );
        ret &= InvariantChecking.checkBlackBoxInvariant(
                ACTUATOR_INBOUND_PORT_URI != null && !ACTUATOR_INBOUND_PORT_URI.isEmpty(),
                SmartLighting.class,
                sm,
                "ACTUATOR_INBOUND_PORT_URI != null && !ACTUATOR_INBOUND_PORT_URI.isEmpty()"
                                                       );
        ret &= InvariantChecking.checkBlackBoxInvariant(
                X_RELATIVE_POSITION >= 0,
                SmartLighting.class,
                sm,
                "X_RELATIVE_POSITION >= 0"
                                                       );
        ret &= InvariantChecking.checkBlackBoxInvariant(
                Y_RELATIVE_POSITION >= 0,
                SmartLighting.class,
                sm,
                "Y_RELATIVE_POSITION >= 0"
                                                       );


        return ret;
    }

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------
    protected SmartLighting(boolean isHEMConnectionRequired) throws Exception {
        this(
            USER_INBOUND_PORT_URI,
            INTERNAL_CONTROL_INBOUND_PORT_URI,
            EXTERNAL_CONTROL_INBOUND_PORT_URI,
            SENSOR_INBOUND_PORT_URI,
            ACTUATOR_INBOUND_PORT_URI,
            isHEMConnectionRequired
            );
    }

    protected SmartLighting(
                    String userInboundPortURI,
                    String internalControlInboundPortURI,
                    String externalControlInboundPortURI,
                    String sensorInboundPortURI,
                    String actuatorInboundPortURI,
                    boolean isHEMConnectionRequired
                           ) throws Exception {
        super(2, 1);
        this.initialise(
                userInboundPortURI,
                internalControlInboundPortURI,
                externalControlInboundPortURI,
                sensorInboundPortURI,
                actuatorInboundPortURI,
                isHEMConnectionRequired
               );
    }

    protected SmartLighting(
                    String reflectionInboundPortURI,
                    String userInboundPortURI,
                    String internalControlInboundPortURI,
                    String externalControlInboundPortURI,
                    String sensorInboundPortURI,
                    String actuatorInboundPortURI,
                    ExecutionType executionType,
                    SimulationType simulationType,
                    String globalArchitectureURI,
                    String localArchitectureURI,
                    TimeUnit simulationTimeUnit,
                    double accFactor,
                    String clockURI,
                    boolean isHEMConnectionRequired
                           ) throws Exception {
        super(reflectionInboundPortURI, 2, 1);
        assert executionType != null: new PreconditionException("executionType != null");
        assert !executionType.isStandard() || clockURI != null || !clockURI.isEmpty():
                new PreconditionException("!executionType.isStandard() || clockURI != null || !clockURI.isEmpty()");
        assert !executionType.isStandard() || simulationType.isNoSimulation():
                new PreconditionException("!executionType.isStandard() || simulationType.isNoSimulation()");
        assert simulationType.isNoSimulation() || (globalArchitectureURI != null && !globalArchitectureURI.isEmpty()):
                new PreconditionException("simulationType.isNoSimulation() || (globalArchitectureURI != null && !globalArchitectureURI.isEmpty())");
        assert simulationType.isNoSimulation() || (localArchitectureURI != null && !localArchitectureURI.isEmpty()):
                new PreconditionException("simulationType.isNoSimulation() || (localArchitectureURI != null && !localArchitectureURI.isEmpty())");
        assert !simulationType.isSimulated() || simulationTimeUnit != null:
                new PreconditionException("!simulationType.isSimulated() || simulationTimeUnit != null");
        assert !simulationType.isRealTimeSimulation() || accFactor > 0.0:
                new PreconditionException("!simulationType.isRealTimeSimulation() || accFactor > 0.0");

        this.clockURI = clockURI;
        this.currentExecutionType = executionType;
        this.currentSimulationType = simulationType;
        this.globalArchitectureURI = globalArchitectureURI;
        this.localArchitectureURI = localArchitectureURI;
        this.simulationTimeUnit = simulationTimeUnit;
        this.accFactor = accFactor;
        this.clock = new CompletableFuture<AcceleratedClock>();

        this.initialise(
                userInboundPortURI,
                internalControlInboundPortURI,
                externalControlInboundPortURI,
                sensorInboundPortURI,
                actuatorInboundPortURI,
                isHEMConnectionRequired
               );

        assert SmartLighting.glassBoxInvariants(this) : new InvariantException("glassBoxInvariants(this)");
        assert SmartLighting.blackBoxInvariants(this) : new InvariantException("blackBoxInvariants(this)");
    }

    protected void initialise(
            String smartLightingUserInboundPortURI,
            String smartLightingInternalControlInboundPortURI,
            String smartLightingExternalControlInboundPortURI,
            String smartLightingSensorInboundPortURI,
            String smartLightingActuatorInboundPortURI,
            boolean isHEMConnectionRequired
            ) throws Exception {

        assert smartLightingUserInboundPortURI != null && !smartLightingUserInboundPortURI.isEmpty():
                new PreconditionException("smartLightingUserInboundPortURI != null && !smartLightingUserInboundPortURI.isEmpty()");
        assert smartLightingInternalControlInboundPortURI != null && !smartLightingInternalControlInboundPortURI.isEmpty():
                new PreconditionException("smartLightingInternalControlInboundPortURI != null && !smartLightingInternalControlInboundPortURI.isEmpty()");
        assert smartLightingExternalControlInboundPortURI != null && !smartLightingExternalControlInboundPortURI.isEmpty():
                new PreconditionException("smartLightingExternalControlInboundPortURI != null && !smartLightingExternalControlInboundPortURI.isEmpty()");
        assert smartLightingSensorInboundPortURI != null && !smartLightingSensorInboundPortURI.isEmpty():
                new PreconditionException("smartLightingSensorInboundPortURI != null && !smartLightingSensorInboundPortURI.isEmpty()");
        assert smartLightingActuatorInboundPortURI != null && !smartLightingActuatorInboundPortURI.isEmpty():
                new PreconditionException("smartLightingActuatorInboundPortURI != null && !smartLightingActuatorInboundPortURI.isEmpty()");

        //variables
        this.currentState = SmartLightingState.OFF;
        this.currentPowerLevel = MAX_POWER_LEVEL;
        this.targetIllumination = STANDARD_TARGET_ILLUMINATION;

        // connections
        this.slip = new SmartLightingUserInboundPort(smartLightingUserInboundPortURI, this);
        this.slip.publishPort();
        this.slcip = new SmartLightingInternalControlInboundPort(smartLightingInternalControlInboundPortURI, this);
        this.slcip.publishPort();
        this.sleip = new SmartLightingExternalControlInboundPort(smartLightingExternalControlInboundPortURI, this);
        this.sleip.publishPort();

        this.sensorDataInboundPort = new SmartLightingSensorDataInboundPort(smartLightingSensorInboundPortURI,this);
        this.sensorDataInboundPort.publishPort();
        this.actuatorInboundPort = new SmartLightingActuatorInboundPort(smartLightingActuatorInboundPortURI,this);
        this.actuatorInboundPort.publishPort();

        // Registration
        this.isHEMConnectionRequired = isHEMConnectionRequired;

        if(this.isHEMConnectionRequired) {
            this.registrationPort = new RegistrationOutboundPort(this);
            this.registrationPort.publishPort();
        }

        switch (this.currentSimulationType){
            case MIL_SIMULATION:
                Architecture architecture = null;
                if (this.currentExecutionType.isUnitTest()){
                    //TODO: add the architecture after mil
                }
        }

        if (VERBOSE) {
            this.tracer.get().setTitle("SmartLighting component");
            this.tracer.get().setRelativePosition(X_RELATIVE_POSITION, Y_RELATIVE_POSITION);
            this.toggleTracing();
        }
    }

    // -------------------------------------------------------------------------
    // Component life-cycle
    // -------------------------------------------------------------------------
    @Override
    public synchronized void start() throws ComponentStartException {
        super.start();

        try {
            // Registration
            if(this.isHEMConnectionRequired) {
                this.doPortConnection(
                        this.registrationPort.getPortURI(),
                        HEM.URI_REGISTRATION_INBOUND_PORT,
                        RegistrationConnector.class.getCanonicalName());
            }

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
                    this.asp = new RTAtomicSimulatorPlugin();
                    uri = this.global2localSimulationArchitectureURIS.get(this.globalArchitectureURI);
                    architecture = (Architecture) this.localSimulationArchitectures.get(uri);
                    ((RTAtomicSimulatorPlugin)this.asp).setPluginURI(uri);
                    ((RTAtomicSimulatorPlugin)this.asp).setSimulationArchitecture(architecture);
                    this.installPlugin(this.asp);
                    break;
                case SIL_SIMULATION:
                    this.asp = new RTAtomicSimulatorPlugin() {
                            private static final long serialVersionUID = 1L;
                            /**
                             * @see fr.sorbonne_u.components.cyphy.plugins.devs.AtomicSimulatorPlugin#getModelStateValue(java.lang.String, java.lang.String)
                             */
                            @Override
                            public Object	getModelStateValue(
                                    String modelURI,
                                    String name
                        ) throws Exception
                            {
                                assert	modelURI.equals(SmartLightingIlluminanceModel.SIL_URI);
                                assert	name.equals(CURRENT_LIGHT_INTENDITY_NAME_NAME);
                                return ((SmartLightingIlluminanceModel)
                                        this.atomicSimulators.get(modelURI).
                                                             getSimulatedModel()).
                                        getCurrentIllumination();
                                //TODO: fix the errors
                            }
                    };
                    uri = this.global2localSimulationArchitectureURIS.get(this.globalArchitectureURI);
                    architecture = (Architecture) this.localSimulationArchitectures.get(uri);
                    ((RTAtomicSimulatorPlugin)this.asp).setPluginURI(uri);
                    ((RTAtomicSimulatorPlugin)this.asp).setSimulationArchitecture(architecture);
                    this.installPlugin(this.asp);
                    break;
                case NO_SIMULATION:
                    break;
                default:
                    break;
            }
        } catch (Exception e) {
            throw new ComponentStartException(e) ;
        }
    }

    @Override
    public synchronized void execute() throws Exception {
        if (this.isHEMConnectionRequired && TEST_REGISTRATION)
            this.runAllRegistrationTest();
        if (this.isHEMConnectionRequired){
            if(!this.currentExecutionType.isStandard()){
                ClocksServerWithSimulationOutboundPort clockServerOBP = new ClocksServerWithSimulationOutboundPort(this);
                clockServerOBP.publishPort();
                this.doPortConnection(
                        clockServerOBP.getPortURI(),
                        ClocksServer.STANDARD_INBOUNDPORT_URI,
                        ClocksServerWithSimulationConnector.class.getCanonicalName()
                );
                AcceleratedAndSimulationClock clock =
                        clockServerOBP.getClockWithSimulation(this.clockURI);
                this.doPortDisconnection(clockServerOBP.getPortURI());
                clockServerOBP.unpublishPort();
                clockServerOBP.destroyPort();

                this.clock.complete(clock);

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
                }

                long simulationStartTimeInMillis =
                        TimeUnit.NANOSECONDS.toMillis(
                                clock.getSimulationStartEpochNanos());
                assert	simulationStartTimeInMillis > System.currentTimeMillis() :
                        new BCMException(
                                "simulationStartTimeInMillis > "
                                + "System.currentTimeMillis()");
                this.asp.startRTSimulation(simulationStartTimeInMillis,
                                           clock.getSimulatedStartTime(),
                                           clock.getSimulatedDuration());

                clock.waitUntilSimulationEnd();

                Thread.sleep(250L);
                this.logMessage(this.asp.getFinalReport().toString());
            }
        }
    }

    @Override
    public synchronized void shutdown() throws ComponentShutdownException {
        try {

            // Registration
            if(this.isHEMConnectionRequired)
                this.registrationPort.unpublishPort();

            this.slip.unpublishPort();
            this.slcip.unpublishPort();
            this.sleip.unpublishPort();
            this.sensorDataInboundPort.unpublishPort();
            this.actuatorInboundPort.unpublishPort();
        } catch (Exception e) {
            throw new ComponentShutdownException(e);
        }
        super.shutdown();
    }

    @Override
    public synchronized void finalise() throws Exception {
        // Registration
        if(this.isHEMConnectionRequired)
            this.doPortDisconnection(this.registrationPort.getPortURI());

        super.finalise();
    }
    // ------------------------------------------------------------------------
    // Comonent services inplementation
    // ------------------------------------------------------------------------

    @Override
    public boolean isOn() throws Exception {
        if (SmartLighting.VERBOSE) {
            this.traceMessage("SmartLighting returns its state: " + this.currentState + ".\n");
        }
        return this.currentState == SmartLightingState.ON || this.currentState == SmartLightingState.INCREASE || this.currentState == SmartLightingState.DECREASE;
    }


    @Override
    public void switchOn() throws Exception {
        if (SmartLighting.VERBOSE) {
            this.traceMessage("SmartLighting switches on.\n");
        }

        assert !this.isOn() : new PreconditionException("SmartLighting is already on.");

        this.currentState = SmartLightingState.ON;

        assert this.isOn() : new PostconditionException("SmartLighting is not on.");

        //TODO: add the code to switch on the light
    }

    @Override
    public void switchOff() throws Exception {

        if (SmartLighting.VERBOSE) {
            this.traceMessage("SmartLighting switches off.\n");
        }

        assert this.isOn() : new PreconditionException("SmartLighting is already off.");

        this.currentState = SmartLightingState.OFF;

        assert !this.isOn() : new PostconditionException("SmartLighting is not off.");
    }

    @Override
    public void setTargetIllumination(double targetIllumination) throws Exception {
        if (SmartLighting.VERBOSE) {
            this.traceMessage("SmartLighting sets its target illumination to " + targetIllumination + ".\n");
        }

        assert this.isOn() : new PreconditionException("SmartLighting is off.");

        assert targetIllumination >= 0.0 : new PreconditionException("Target illumination must be positive.");

        this.targetIllumination = targetIllumination;

        assert this.getTargetIllumination() == targetIllumination : new PostconditionException("Target illumination has not been set correctly.");
    }

    @Override
    public double getMaxPowerLevel() throws Exception {
        if (SmartLighting.VERBOSE) {
            this.traceMessage("SmartLighting returns its maximum power level: " + MAX_POWER_LEVEL + ".\n");
        }
        return MAX_POWER_LEVEL;
    }

    @Override
    public double getCurrentPowerLevel() throws Exception {
        if (SmartLighting.VERBOSE) {
            this.traceMessage("SmartLighting returns its current power level: " + this.currentPowerLevel + ".\n");
        }

        assert this.isOn() : new PreconditionException("SmartLighting is off.");

        double powerLevel = this.currentPowerLevel;

        assert powerLevel >= 0.0 && powerLevel <= MAX_POWER_LEVEL : new PostconditionException("Current Power level is not in the correct range.");

        return powerLevel;
    }

    @Override
    public void setCurrentPowerLevel(double powerLevel) throws Exception {
        if (SmartLighting.VERBOSE) {
            this.traceMessage("SmartLighting sets its current power level to " + powerLevel + ".\n");
        }

        assert this.isOn() : new PreconditionException("SmartLighting is off.");
        assert powerLevel >= 0.0 && powerLevel <= MAX_POWER_LEVEL : new PreconditionException("Power level must be in the correct range.");

        this.currentPowerLevel = powerLevel;

        assert this.getCurrentPowerLevel() == powerLevel : new PostconditionException("Current power level has not been set correctly.");
    }

    @Override
    public void IncreaseLightIntensity() throws Exception {
        if (SmartLighting.VERBOSE) {
            this.traceMessage("SmartLighting increases its light intensity.\n");
        }

        assert this.isOn() : new PreconditionException("SmartLighting is off.");
        assert !this.isSwitchingAutomatically() : new PreconditionException("SmartLighting is already in mode automatically.");

        this.currentState = SmartLightingState.INCREASE;

        assert this.isSwitchingAutomatically() : new PostconditionException("SmartLighting is not in mode automatically.");
    }

    @Override
    public void DecreaseLightIntensity() throws Exception {
        if (SmartLighting.VERBOSE) {
            this.traceMessage("SmartLighting decreases its light intensity.\n");
        }

        assert this.isOn() : new PreconditionException("SmartLighting is off.");
        assert !this.isSwitchingAutomatically() : new PreconditionException("SmartLighting is already in mode automatically.");

        this.currentState = SmartLightingState.DECREASE;

        assert this.isSwitchingAutomatically() : new PostconditionException("SmartLighting is not in mode automatically.");
    }

    @Override
    public boolean isSwitchingAutomatically() throws Exception {
        if (SmartLighting.VERBOSE) {
            this.traceMessage("SmartLighting returns its state: " + this.currentState + ".\n");
        }

        assert this.isOn() : new PreconditionException("SmartLighting is off.");

        return this.currentState == SmartLightingState.INCREASE || this.currentState == SmartLightingState.DECREASE;
    }

    @Override
    public double getTargetIllumination() throws Exception {
        assert this.isOn() : new PreconditionException("SmartLighting is off.");

        if (SmartLighting.VERBOSE) {
            this.traceMessage("SmartLighting returns its target illumination: " + this.targetIllumination + ".\n");
        }

        double targetIllumination = this.targetIllumination;

        assert targetIllumination >= 0.0 : new PostconditionException("Target illumination is not in the correct range.");

        return targetIllumination;
    }

    @Override
    public double getCurrentIllumination() throws Exception {
        assert this.isOn() : new PreconditionException("SmartLighting is off.");

        if (SmartLighting.VERBOSE) {
            this.traceMessage("SmartLighting returns its current illumination: " + this.targetIllumination + ".\n");
        }

        return 0;
    }

    //-------------------------------------------------------------------------
    // Component sensors
    //-------------------------------------------------------------------------

    public SmartLightingSensorData<Measure<Boolean>> automaticModePullSensor() throws Exception {
        return new SmartLightingSensorData<Measure<Boolean>>(
                new Measure<Boolean>(this.isSwitchingAutomatically()));
    }

    public SmartLightingSensorData<Measure<Double>> targetIlluminationPullSensor() throws Exception {
        return new SmartLightingSensorData<Measure<Double>>(
                new Measure<Double>(this.getTargetIllumination(), MeasurementUnit.LUX));
    }

    public SmartLightingSensorData<Measure<Double>> currentIlluminationPullSensor() throws Exception {
        return new SmartLightingSensorData<Measure<Double>>(
                new Measure<Double>(this.getCurrentIllumination(), MeasurementUnit.LUX));
    }

    public void startIlluminationPushSensor(long controlPeriod, TimeUnit tu) throws Exception {
        long actualControlPeriod = -1L;
        if (this.currentExecutionType.isStandard()) {
            actualControlPeriod = (long)(controlPeriod * tu.toNanos(1));
        } else {
            // this will synchronise the start of the push sensor with the
            // availability of the clock, required to compute the actual push
            // period with the correct acceleration factor
            AcceleratedClock ac = this.clock.get();
            // the accelerated period is in nanoseconds, hence first convert
            // the period to nanoseconds, perform the division and then
            // convert to long (hence providing a better precision than
            // first dividing and then converting to nanoseconds...)
            actualControlPeriod =
                    (long)((controlPeriod * tu.toNanos(1))/
                           ac.getAccelerationFactor());
            // sanity checking, the standard Java scheduler has a
            // precision no less than 10 milliseconds...
            if (actualControlPeriod < TimeUnit.MILLISECONDS.toNanos(10)) {
                System.out.println(
                        "Warning: accelerated control period is "
                        + "too small ("
                        + actualControlPeriod +
                        "), unexpected scheduling problems may"
                        + " occur!");
            }
        }
        this.illuminancePushSensorTask(actualControlPeriod);
    }

    protected void illuminancePushSensorTask(long actualControlPeriod) throws Exception {
        assert	actualControlPeriod > 0 :
                new PreconditionException("actualControlPeriod > 0");

        if (this.currentState != SmartLightingState.OFF) {
            this.traceMessage("Smart Lighting performs a new temperatures push.\n");
            this.IlluminationPushSensor();
            if(this.currentExecutionType.isStandard() || this.currentSimulationType.isSILSimulation()
                 || this.currentSimulationType.isHILSimulation()){
                this.scheduleTaskOnComponent(
                        new AbstractComponent.AbstractTask() {
                            @Override
                            public void run() {
                                try {
                                    illuminancePushSensorTask(actualControlPeriod);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        },
                        actualControlPeriod,
                        TimeUnit.NANOSECONDS);
            }
        }
    }

    protected void IlluminationPushSensor() throws Exception {
        this.sensorDataInboundPort.send(new SmartLightingSensorData<SmartLightingCompoundMeasure>(
                new SmartLightingCompoundMeasure(
                        this.targetIlluminationPullSensor().getMeasure(),
                        this.currentIlluminationPullSensor().getMeasure()
                )
        ));
    }

    // -------------------------------------------------------------------------
    // Component Tests Registration
    // -------------------------------------------------------------------------

    protected void testRegistered() {
        if(VERBOSE)
            this.traceMessage("Test registered\n");
        try {
            assertFalse(this.registrationPort.registered(URI));
        }
        catch(Exception e) {
            this.traceMessage("...KO.\n" + e);
            assertTrue(false);
        }
        if(VERBOSE)
            this.traceMessage("Done...\n");
    }

    protected void testRegister() {
        if(VERBOSE)
            this.traceMessage("Test register\n");
        try {
            assertTrue(this.registrationPort.register(URI, this.sleip.getPortURI(), XML_PATH));
            assertTrue(this.registrationPort.registered(URI));
        }
        catch(Exception e) {
            this.traceMessage("...KO.\n" + e);
            fail();
        }
        if(VERBOSE)
            this.traceMessage("Done...\n");
    }

    protected void testUnregister() {
        if(VERBOSE)
            this.traceMessage("Test unregister\n");
        try {
            this.registrationPort.unregister(URI);
            assertFalse(this.registrationPort.registered(URI));
        }
        catch(Exception e) {
            this.traceMessage("...KO.\n" + e);
            assertTrue(false);
        }
        if(VERBOSE)
            this.traceMessage("Done...\n");
    }

    protected void runAllRegistrationTest() throws Exception {
        // Switch on to make the tests
        this.switchOn();

        this.testRegistered();
        this.testRegister();
        this.testUnregister();
    }
}
