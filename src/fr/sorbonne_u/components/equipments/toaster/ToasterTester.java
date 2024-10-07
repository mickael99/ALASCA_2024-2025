package fr.sorbonne_u.components.equipments.toaster;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import fr.sorbonne_u.components.AbstractComponent;
import fr.sorbonne_u.components.CVMIntegrationTest;
import fr.sorbonne_u.components.equipments.toaster.ToasterImplementationI.ToasterBrowningLevel;
import fr.sorbonne_u.components.equipments.toaster.ToasterImplementationI.ToasterState;
import fr.sorbonne_u.components.exceptions.ComponentShutdownException;
import fr.sorbonne_u.components.exceptions.ComponentStartException;
import fr.sorbonne_u.utils.aclocks.AcceleratedClock;
import fr.sorbonne_u.utils.aclocks.ClocksServer;
import fr.sorbonne_u.utils.aclocks.ClocksServerConnector;
import fr.sorbonne_u.utils.aclocks.ClocksServerOutboundPort;

public class ToasterTester extends AbstractComponent {
	
	// -------------------------------------------------------------------------
	// Constants and variables
	// -------------------------------------------------------------------------
	
	protected boolean isUnitTest;
	
	public static boolean VERBOSE = true;
	public static int X_RELATIVE_POSITION = 1;
	public static int Y_RELATIVE_POSITION = 2;
	
	protected ToasterOutboundPort outboundPort;
	
	protected String inboudPortURI;
	
	protected ClocksServerOutboundPort	clocksServerOutboundPort;
	
	// -------------------------------------------------------------------------
	// Constructors
	// -------------------------------------------------------------------------

	protected ToasterTester(boolean isUnitTest) throws Exception {
		this(isUnitTest, Toaster.INBOUND_PORT_URI);
	}

	protected ToasterTester(boolean isUnitTest, String ToasterInboundPortURI) throws Exception {
		super(1, 0);

		this.isUnitTest = isUnitTest;
		this.initialise(ToasterInboundPortURI);
	}
	
	protected ToasterTester(boolean isUnitTest, String ToasterInboundPortURI, String reflectionInboundPortURI ) throws Exception {
		super(reflectionInboundPortURI, 1, 0);

		this.isUnitTest = isUnitTest;
		this.initialise(ToasterInboundPortURI);
	}
	
	protected void initialise(String ToasterInboundPortURI) throws Exception {
		this.inboudPortURI = ToasterInboundPortURI;
		this.outboundPort = new ToasterOutboundPort(this);
		this.outboundPort.publishPort();

		if (VERBOSE) {
			this.tracer.get().setTitle("Toaster tester component");
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
			this.doPortConnection(
							this.outboundPort.getPortURI(),
							this.inboudPortURI,
							ToasterConnector.class.getCanonicalName());
		} catch (Exception e) {
			throw new ComponentStartException(e) ;
		}
	}

	@Override
	public synchronized void execute() throws Exception {
		
		if (!this.isUnitTest) {
			
			this.clocksServerOutboundPort = new ClocksServerOutboundPort(this);
			this.clocksServerOutboundPort.publishPort();
			this.doPortConnection(
					this.clocksServerOutboundPort.getPortURI(),
					ClocksServer.STANDARD_INBOUNDPORT_URI,
					ClocksServerConnector.class.getCanonicalName());
			System.out.println("Toaster Tester gets the clock");
			AcceleratedClock ac =
					this.clocksServerOutboundPort.getClock(
										CVMIntegrationTest.TEST_CLOCK_URI);

			System.out.println("Toaster Tester waits until start");
			ac.waitUntilStart();
			System.out.println("Toaster Tester waits to perform tests");
			this.doPortDisconnection(
						this.clocksServerOutboundPort.getPortURI());
			this.clocksServerOutboundPort.unpublishPort();
			Thread.sleep(3000);
		}
		this.runAllTests();
		System.out.println("Toaster Tester ends");
	}

	@Override
	public synchronized void finalise() throws Exception {
		this.doPortDisconnection(this.outboundPort.getPortURI());
		super.finalise();
	}

	@Override
	public synchronized void shutdown() throws ComponentShutdownException {
		try {
			this.outboundPort.unpublishPort();
		} catch (Exception e) {
			throw new ComponentShutdownException(e) ;
		}
		super.shutdown();
	}
	
	
	// -------------------------------------------------------------------------
	// Component internal methods
	// -------------------------------------------------------------------------

	public void	testGetState(){
		this.logMessage("testGetState()... ");
		try {
			assertEquals(ToasterState.OFF, this.outboundPort.getState());
		} catch (Exception e) {
			this.logMessage("...KO.");
			assertTrue(false);
		}
		this.logMessage("...done.");
	}

	public void	testgetBrowningLevel() {
		this.logMessage("TestgetBrowningLevel()... ");
		try {
			assertEquals(ToasterBrowningLevel.LOW, this.outboundPort.getBrowningLevel());
		} catch (Exception e) {
			assertTrue(false);
		}
		this.logMessage("...done.");
	}
	
	public void testGetSliceCount() {
		this.logMessage("TestGetSliceCount()... ");
		try {
			assertEquals(0, this.outboundPort.getSliceCount());
		} catch (Exception e) {
			assertTrue(false);
		}
		this.logMessage("...done.");
	}
	
	public void testSetSliceCount() throws Exception {
		this.logMessage("TestSetSliceCount()... ");
		this.outboundPort.setSliceCount(2);
		try {
			assertEquals(2, this.outboundPort.getSliceCount());
		} catch (Exception e) {
			assertTrue(false);
		}
		this.logMessage("...done.");
	}

	public void	testTurnOn() {
		this.logMessage("testTurnOn()... ");
		try {
			this.outboundPort.turnOn();
			assertEquals(ToasterState.ON, this.outboundPort.getState());
		} catch (Exception e) {
			assertTrue(false);
		}
		this.logMessage("...done.");
	}
	
	public void	testTurnOff() {
		this.logMessage("testTurnOff()... ");
		try {
			this.outboundPort.turnOff();
			assertEquals(ToasterState.OFF, this.outboundPort.getState());
		} catch (Exception e) {
			assertTrue(false);
		}
		this.logMessage("...done.");
	}

	public void	testSetBrowningLevel()
	{
		this.logMessage("testSetBrowningLevel()... ");
		try {
			this.outboundPort.turnOn();
			this.outboundPort.setBrowningLevel(ToasterBrowningLevel.HIGH);
			assertEquals(ToasterBrowningLevel.HIGH, this.outboundPort.getBrowningLevel());
		} catch (Exception e) {
			assertTrue(false);
		}
		this.logMessage("...done.");
	}

	protected void runAllTests() {
		this.testGetState();
		this.testgetBrowningLevel();
		this.testGetSliceCount();
		this.testTurnOn();
		this.testTurnOff();
		this.testSetBrowningLevel();
	}
}
