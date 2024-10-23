package fr.sorbonne_u.components.equipments.smartLighting;

import fr.sorbonne_u.components.AbstractComponent;
import fr.sorbonne_u.components.CVMIntegrationTest;
import fr.sorbonne_u.components.annotations.RequiredInterfaces;
import fr.sorbonne_u.components.equipments.smartLighting.connections.*;
import fr.sorbonne_u.components.equipments.smartLighting.interfaces.SmartLightingExternalControlCI;
import fr.sorbonne_u.components.equipments.smartLighting.interfaces.SmartLightingInternalControlCI;
import fr.sorbonne_u.components.equipments.smartLighting.interfaces.SmartLightingUserCI;
import fr.sorbonne_u.components.exceptions.ComponentShutdownException;
import fr.sorbonne_u.components.exceptions.ComponentStartException;
import fr.sorbonne_u.utils.aclocks.AcceleratedClock;
import fr.sorbonne_u.utils.aclocks.ClocksServer;
import fr.sorbonne_u.utils.aclocks.ClocksServerCI;
import fr.sorbonne_u.utils.aclocks.ClocksServerOutboundPort;

import java.time.Instant;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

@RequiredInterfaces(required = {SmartLightingUserCI.class, SmartLightingInternalControlCI.class, SmartLightingExternalControlCI.class, ClocksServerCI.class})
public class SmartLightingTester extends AbstractComponent {

    // ------------------------------------------------------------------------
    // Constants and variables
    // ------------------------------------------------------------------------

    public static final int		SWITCH_ON_DELAY = 2;

    public static final int		SWITCH_OFF_DELAY = 8 ;

    public static boolean       VERBOSE = false;

    public static int			X_RELATIVE_POSITION = 0;

    public static int			Y_RELATIVE_POSITION = 0;

    /** true if the component must perform unit tests, otherwise it
     *  executes integration tests actions.									*/
    protected  boolean		isUnitTest;
    /** URI of the user component interface inbound port.					*/
    protected String			smartLightingUserInboundPortURI;
    /** URI of the internal control component interface inbound port.		*/
    protected String			smartLignthingInternalControlInboundPortURI;
    /** URI of the external control component interface inbound port.		*/
    protected String			smartLightingExternalControlInboundPortURI;

    protected SmartLightingUserOutboundPort slop;

    protected SmartLightingExternalControlOutboundPort slecop;

    protected SmartLightingInternalControlOutboundPort slicop;

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------

    protected SmartLightingTester(boolean isUnitTest) throws Exception {
        this(isUnitTest, SmartLighting.USER_INBOUND_PORT_URI, SmartLighting.INTERNAL_CONTROL_INBOUND_PORT_URI, SmartLighting.EXTERNAL_CONTROL_INBOUND_PORT_URI);
    }

    protected SmartLightingTester(boolean isUnitTest, String smartLightingUserInboundPortURI, String smartLightingInternalControlInboundPortURI, String smartLightingExternalControlInboundPortURI) throws Exception {
        super(1, 1);
        this.isUnitTest = isUnitTest;
        this.initialise(smartLightingUserInboundPortURI, smartLightingInternalControlInboundPortURI, smartLightingExternalControlInboundPortURI);
    }

    protected SmartLightingTester(boolean isUnitTest, String reflectionInboundPortURI, String smartLightingUserInboundPortURI, String smartLightingInternalControlInboundPortURI, String smartLightingExternalControlInboundPortURI) throws Exception {
        super(reflectionInboundPortURI, 1, 1);
        this.isUnitTest = false;
        this.initialise(smartLightingUserInboundPortURI, smartLightingInternalControlInboundPortURI, smartLightingExternalControlInboundPortURI);
    }

    protected void initialise(String smartLightingUserInboundPortURI, String smartLightingInternalControlInboundPortURI, String smartLightingExternalControlInboundPortURI) throws Exception {

        this.smartLightingUserInboundPortURI = smartLightingUserInboundPortURI;
        this.slop = new SmartLightingUserOutboundPort(this);
        this.slop.publishPort();

        this.smartLignthingInternalControlInboundPortURI = smartLightingInternalControlInboundPortURI;
        this.slicop = new SmartLightingInternalControlOutboundPort(this);
        this.slicop.publishPort();

        this.smartLightingExternalControlInboundPortURI = smartLightingExternalControlInboundPortURI;
        this.slecop = new SmartLightingExternalControlOutboundPort(this);
        this.slecop.publishPort();

        if(VERBOSE){
            this.tracer.get().setTitle("SmartLightingTester component");
            this.tracer.get().setRelativePosition(X_RELATIVE_POSITION, Y_RELATIVE_POSITION);
            this.toggleTracing();
        }

    }

    // ------------------------------------------------------------------------
    // Component services implementation
    // ------------------------------------------------------------------------

    protected void testSwitchStates() {
        this.traceMessage("Testing switch states...\n");
        try{
            this.slop.switchOn();
        } catch (Exception e) {
            this.traceMessage("...KO. " + e + "\n");
            fail();
        }
        try{
            this.slop.switchOff();
        } catch (Exception e) {
            this.traceMessage("...KO. " + e + "\n");
            fail();
        }
        this.traceMessage("...testSwitchStatesDone.\n");
    }

    protected void testOn(){
        this.traceMessage("Testing isOn...\n");
        try{
            assertFalse(this.slop.isOn());
        } catch (Exception e) {
            this.traceMessage("...KO. " + e + "\n");
            fail();
        }
        try {
            this.slop.switchOn();
            assertTrue(this.slop.isOn());
            this.slop.switchOff();
        } catch (Exception e) {
            this.traceMessage("...KO. " + e + "\n");
            fail();
        }
        this.traceMessage("...testOn() Done.\n");
    }

    protected void testTargetIllumination(){
        this.traceMessage("Testing setTargetIllumination...\n");
        try{
            this.slop.switchOn();
            this.slop.setTargetIllumination(200.0);
            assertEquals(200.0, this.slop.getTargetIllumination());
            this.slop.setTargetIllumination(SmartLighting.STANDARD_TARGET_ILLUMINATION);
            this.slop.switchOff();
        } catch (Exception e) {
            this.traceMessage("...KO. " + e + "\n");
            fail();
        }
        this.traceMessage("...testTargetIllumination() Done.\n");
    }

    protected void testCurrentIllumination(){
        this.traceMessage("Testing currentIllumination...\n");
        try{
            this.slop.switchOn();
            assertEquals(0, this.slop.getCurrentIllumination());
            this.slop.switchOff();
        } catch (Exception e) {
            this.traceMessage("...KO. " + e + "\n");
            fail();
        }
        this.traceMessage("...testCurrentIllumination() Done.\n");
    }

    public void testPowerLevel(){
        this.traceMessage("Testing powerLevel...\n");
        try{
            assertEquals(SmartLighting.MAX_POWER_LEVEL, this.slop.getMaxPowerLevel());
            this.slop.switchOn();
            this.slop.setCurrentPowerLevel(SmartLighting.MAX_POWER_LEVEL/2.0);
            assertEquals(SmartLighting.MAX_POWER_LEVEL/2.0, this.slop.getCurrentPowerLevel());
            this.slop.switchOff();
        } catch (Exception e) {
            this.traceMessage("...KO. " + e + "\n");
            fail();
        }
        this.traceMessage("...testPowerLevel() Done.\n");
    }

    protected void testInternalControl(){
        this.traceMessage("Testing internal control...\n");
        try{
            this.slop.switchOn();
            assertEquals(SmartLighting.STANDARD_TARGET_ILLUMINATION, this.slop.getTargetIllumination());
            assertTrue(this.slop.isOn());
            assertEquals(0, this.slop.getCurrentIllumination());
            this.slicop.IncreaseLightIntensity();
            assertTrue(this.slicop.isSwitchingAutomatically());
            this.slop.switchOff();
        } catch (Exception e) {
            this.traceMessage("...KO. " + e + "\n");
            fail();
        }
        this.traceMessage("...testInternalControl() Done.\n");
    }

    protected void testExternalControl(){
        this.traceMessage("Testing external control...\n");
        try{
            assertEquals(SmartLighting.MAX_POWER_LEVEL, this.slop.getMaxPowerLevel());
            this.slop.switchOn();
            this.slecop.setCurrentPowerLevel(SmartLighting.MAX_POWER_LEVEL/2.0);
            assertEquals(SmartLighting.MAX_POWER_LEVEL/2.0, this.slop.getCurrentPowerLevel());
            this.slop.switchOff();
        } catch (Exception e) {
            this.traceMessage("...KO. " + e + "\n");
            fail();
        }
        this.traceMessage("...testExternalControl() Done.\n");
    }

    public void runAllTests() {
        this.testSwitchStates();
        this.testOn();
        this.testTargetIllumination();
        this.testCurrentIllumination();
        this.testPowerLevel();
        this.testInternalControl();
        this.testExternalControl();
    }

    // -------------------------------------------------------------------------
    // Component life-cycle
    // -------------------------------------------------------------------------
    /**
     * @see fr.sorbonne_u.components.AbstractComponent#start()
     */
    @Override
    public synchronized void start() throws ComponentStartException {
        super.start();
        try{
            this.doPortConnection(
                this.slop.getPortURI(),
                this.smartLightingUserInboundPortURI,
                SmartLightingUserConnector.class.getCanonicalName()
            );
            this.doPortConnection(
                this.slicop.getPortURI(),
                this.smartLignthingInternalControlInboundPortURI,
                SmartLightingInternalControlConnector.class.getCanonicalName()
            );
            this.doPortConnection(
                this.slecop.getPortURI(),
                this.smartLightingExternalControlInboundPortURI,
                SmartLightingExternalControlConnector.class.getCanonicalName()
            );
        } catch (Exception e) {
            throw new ComponentStartException(e);
        }
    }

    /**
     * @see fr.sorbonne_u.components.AbstractComponent#execute()
     */
    @Override
    public synchronized void execute() throws Exception {
        if (this.isUnitTest) {
            this.runAllTests();
        } else {
            ClocksServerOutboundPort csop = new ClocksServerOutboundPort(this);
            csop.publishPort();
            this.doPortConnection(
                csop.getPortURI(),
                ClocksServer.STANDARD_INBOUNDPORT_URI,
                ClocksServerOutboundPort.class.getCanonicalName()
            );
            this.traceMessage("Smart Lighting test gets the clocks\n");
            AcceleratedClock ac = csop.getClock(CVMIntegrationTest.TEST_CLOCK_URI);
            this.doPortDisconnection(csop.getPortURI());
            csop.unpublishPort();
            csop = null;

            Instant startInstance =ac.getStartInstant();
            Instant smartLightingSwitchOn = startInstance.plusSeconds(SWITCH_ON_DELAY);
            Instant smartLightingSwitchOff = startInstance.plusSeconds(SWITCH_OFF_DELAY);

            this.traceMessage("Smart Lighting tester waits until start.\n");
            ac.waitUntilStart();
            this.traceMessage("Smart Lighting tester schedules switch on and off.\n");
            long delayToSwitchOn = ac.nanoDelayUntilInstant(smartLightingSwitchOn);
            long delayToSwitchOff = ac.nanoDelayUntilInstant(smartLightingSwitchOff);

            // This is to avoid mixing the 'this' of the task object with the 'this'
            // representing the component object in the code of the next methods run
            AbstractComponent o = this;

            //schedule switch on smart lighting
            this.scheduleTaskOnComponent(
                    new AbstractComponent.AbstractTask() {
                        @Override
                        public void run() {
                            try {
                                o.traceMessage("Smart Lighting switches on.\n");
                                slop.switchOn();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    },delayToSwitchOn, TimeUnit.NANOSECONDS
            );


            //schedule switch off smart lighting
            this.scheduleTaskOnComponent(
                    new AbstractComponent.AbstractTask() {
                        @Override
                        public void run() {
                            try {
                                o.traceMessage("Smart Lighting switches off.\n");
                                slop.switchOff();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    },delayToSwitchOff, TimeUnit.NANOSECONDS
            );
        }
    }

    /**
     * @see fr.sorbonne_u.components.AbstractComponent#finalise()
     */
    @Override
    public void finalise() throws Exception {
        this.doPortDisconnection(this.slop.getPortURI());
        this.doPortDisconnection(this.slicop.getPortURI());
        this.doPortDisconnection(this.slecop.getPortURI());
        super.finalise();
    }

    /**
     * @see fr.sorbonne_u.components.AbstractComponent#shutdown()
     */
    @Override
    public void shutdown() throws ComponentShutdownException {
        try{
            this.slop.unpublishPort();
            this.slicop.unpublishPort();
            this.slecop.unpublishPort();
        } catch (Exception e) {
            throw new ComponentShutdownException(e);
        }
        super.shutdown();
    }

}
