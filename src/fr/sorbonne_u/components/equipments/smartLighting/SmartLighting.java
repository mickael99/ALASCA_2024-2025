package fr.sorbonne_u.components.equipments.smartLighting;

import fr.sorbonne_u.components.AbstractComponent;
import fr.sorbonne_u.components.annotations.OfferedInterfaces;
import fr.sorbonne_u.components.annotations.RequiredInterfaces;
import fr.sorbonne_u.components.equipments.hem.HEM;
import fr.sorbonne_u.components.equipments.hem.registration.RegistrationCI;
import fr.sorbonne_u.components.equipments.hem.registration.RegistrationConnector;
import fr.sorbonne_u.components.equipments.hem.registration.RegistrationOutboundPort;
import fr.sorbonne_u.components.equipments.smartLighting.connections.SmartLightingExternalControlInboundPort;
import fr.sorbonne_u.components.equipments.smartLighting.connections.SmartLightingInternalControlInboundPort;
import fr.sorbonne_u.components.equipments.smartLighting.connections.SmartLightingUserInboundPort;
import fr.sorbonne_u.components.equipments.smartLighting.interfaces.*;
import fr.sorbonne_u.components.exceptions.ComponentShutdownException;
import fr.sorbonne_u.components.exceptions.ComponentStartException;
import fr.sorbonne_u.exceptions.PostconditionException;
import fr.sorbonne_u.exceptions.PreconditionException;

import static org.junit.jupiter.api.Assertions.*;

@OfferedInterfaces(offered = {
        SmartLightingUserCI.class,
        SmartLightingInternalControlCI.class,
        SmartLightingExternalControlCI.class
})
@RequiredInterfaces(required = {RegistrationCI.class})
public class SmartLighting extends AbstractComponent implements SmartLightingUserI, SmartLightingInternalControlI {

    // ------------------------------------------------------------------------
    // Inner interfaces and types
    // ------------------------------------------------------------------------
    protected static enum SmartLightingState {
        ON,
        OFF,
        INCREASE,
        DECREASE
    }

    // ------------------------------------------------------------------------
    // Constants and variables
    // ------------------------------------------------------------------------
    public static final String REFLECTION_INBOUND_PORT_URI = "Smart-Lighting-RIP-URI";
    public static final String URI = "SMART-LIGHTING-URI";

    //power level and illumination
    protected static final double MAX_POWER_LEVEL = 100.0;
    protected static final double STANDARD_TARGET_ILLUMINATION = 100.0;
    protected SmartLightingState currentState;
    protected double currentPowerLevel;
    protected double targetIllumination;

    // Inbound port URIs
    public static final String USER_INBOUND_PORT_URI = "SMART-LIGHTING-USER-INBOUND-PORT-URI";
    public static final String INTERNAL_CONTROL_INBOUND_PORT_URI = "SMART-LIGHTING-INTERNAL-CONTROL-INBOUND-PORT-URI";
    public static final String EXTERNAL_CONTROL_INBOUND_PORT_URI = "SMART-LIGHTING-EXTERNAL-CONTROL-INBOUND-PORT-URI";

    protected static boolean VERBOSE = true;

    // Position of the trace window
    public static int X_RELATIVE_POSITION = 0;
    public static int Y_RELATIVE_POSITION = 0;

    // Outbound ports
    protected SmartLightingUserInboundPort slip;
    protected SmartLightingInternalControlInboundPort slcip;
    protected SmartLightingExternalControlInboundPort sleip;

    // Registration
    public static boolean TEST_REGISTRATION = false;
    protected RegistrationOutboundPort registrationPort;
    protected boolean isHEMConnectionRequired;
    protected static final String XML_PATH = "smart-lighting-descriptor.xml";



    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------
    protected SmartLighting(boolean isHEMConnectionRequired) throws Exception {
        this(
            USER_INBOUND_PORT_URI,
            INTERNAL_CONTROL_INBOUND_PORT_URI,
            EXTERNAL_CONTROL_INBOUND_PORT_URI,
            isHEMConnectionRequired
            );
    }

    protected SmartLighting(
                    String userInboundPortURI,
                    String internalControlInboundPortURI,
                    String externalControlInboundPortURI,
                    boolean isHEMConnectionRequired
                   ) throws Exception {
        super(2, 0);
        this.initialise(
                userInboundPortURI,
                internalControlInboundPortURI,
                externalControlInboundPortURI,
                isHEMConnectionRequired
               );
    }

    protected SmartLighting(
                    String reflectionInboundPortURI,
                    String userInboundPortURI,
                    String internalControlInboundPortURI,
                    String externalControlInboundPortURI,
                    boolean isHEMConnectionRequired
                   ) throws Exception {
        super(reflectionInboundPortURI, 2, 0);
        this.initialise(
                userInboundPortURI,
                internalControlInboundPortURI,
                externalControlInboundPortURI,
                isHEMConnectionRequired
               );
    }

    protected void initialise(
            String smartLightingUserInboundPortURI,
            String smartLightingInternalControlInboundPortURI,
            String smartLightingExternalControlInboundPortURI,
            boolean isHEMConnectionRequired
            ) throws Exception {

        assert smartLightingUserInboundPortURI != null && !smartLightingUserInboundPortURI.isEmpty();
        assert smartLightingInternalControlInboundPortURI != null && !smartLightingInternalControlInboundPortURI.isEmpty();
        assert smartLightingExternalControlInboundPortURI != null && !smartLightingExternalControlInboundPortURI.isEmpty();

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

        // Registration
        this.isHEMConnectionRequired = isHEMConnectionRequired;

        if(this.isHEMConnectionRequired) {
            this.registrationPort = new RegistrationOutboundPort(this);
            this.registrationPort.publishPort();
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
        } catch (Exception e) {
            throw new ComponentStartException(e) ;
        }
    }

    @Override
    public synchronized void execute() throws Exception {
        if (this.isHEMConnectionRequired && TEST_REGISTRATION)
            this.runAllRegistrationTest();
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
