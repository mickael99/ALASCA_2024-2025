package fr.sorbonne_u.components.equipments.fridge;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;

import fr.sorbonne_u.components.AbstractComponent;
import fr.sorbonne_u.components.annotations.RequiredInterfaces;
import fr.sorbonne_u.components.equipments.fridge.connections.FridgeExternalControlConnector;
import fr.sorbonne_u.components.equipments.fridge.connections.FridgeExternalControlOutboundPort;
import fr.sorbonne_u.components.equipments.fridge.connections.FridgeInternalControlConnector;
import fr.sorbonne_u.components.equipments.fridge.connections.FridgeInternalControlOutboundPort;
import fr.sorbonne_u.components.equipments.fridge.connections.FridgeUserConnector;
import fr.sorbonne_u.components.equipments.fridge.connections.FridgeUserOutboundPort;
import fr.sorbonne_u.components.equipments.fridge.interfaces.FridgeExternalControlCI;
import fr.sorbonne_u.components.equipments.fridge.interfaces.FridgeInternalControlCI;
import fr.sorbonne_u.components.equipments.fridge.interfaces.FridgeUserCI;
import fr.sorbonne_u.components.equipments.fridge.mil.FridgeElectricityModel.FridgeState;
import fr.sorbonne_u.components.exceptions.ComponentShutdownException;
import fr.sorbonne_u.components.exceptions.ComponentStartException;
import fr.sorbonne_u.utils.aclocks.ClocksServerOutboundPort;

@RequiredInterfaces(required = {FridgeExternalControlCI.class, FridgeInternalControlCI.class, FridgeUserCI.class})
public class FridgeTester extends AbstractComponent {
	
	// -------------------------------------------------------------------------
	// Constants and variables
	// -------------------------------------------------------------------------
	
	// Tracing
	public static int X_RELATIVE_POSITION = 1;
	public static int Y_RELATIVE_POSITION = 0;
	
	// Unit test
	protected boolean isUnitTest = true;
	
	// Inbound ports URI
	protected String userInboundPortURI;
	protected String internalInboundPortURI;
	protected String externalInboundPortURI;
	
	// Outbound ports
	protected FridgeUserOutboundPort userOutboundPort;
	protected FridgeInternalControlOutboundPort internalOutboundPort;
	protected FridgeExternalControlOutboundPort externalOutboundPort;
	
	// Clock
	protected ClocksServerOutboundPort	clocksServerOutboundPort;
	
	
	// -------------------------------------------------------------------------
	// Constructors
	// -------------------------------------------------------------------------
	
	protected FridgeTester(boolean isUnitTest) throws Exception {
		this(isUnitTest, Fridge.USER_INBOUND_PORT_URI,
				Fridge.INTERNAL_CONTROL_INBOUND_PORT_URI, Fridge.EXTERNAL_CONTROL_INBOUND_PORT_URI);
	}

	protected FridgeTester(boolean isUnitTest, String fridgeUserInboundPortURI,	String fridgeInternalControlInboundPortURI,
							String fridgeExternalControlInboundPortURI) throws Exception {
		super(1, 1);
		this.isUnitTest = isUnitTest;
		this.initialise(fridgeUserInboundPortURI, fridgeInternalControlInboundPortURI, fridgeExternalControlInboundPortURI);
	}

	protected FridgeTester(boolean isUnitTest, String reflectionInboundPortURI, String fridgeUserInboundPortURI,
							String fridgeInternalControlInboundPortURI, String fridgeExternalControlInboundPortURI) throws Exception {
		super(reflectionInboundPortURI, 1, 1);
		this.isUnitTest = isUnitTest;
		this.initialise(fridgeUserInboundPortURI, fridgeInternalControlInboundPortURI, fridgeExternalControlInboundPortURI);
	}

	protected void initialise(String fridgeUserInboundPortURI, String fridgeInternalControlInboundPortURI, String fridgeExternalControlInboundPortURI) throws Exception {
		// Connections
		this.userInboundPortURI = fridgeUserInboundPortURI;
		this.userOutboundPort = new FridgeUserOutboundPort(this);
		this.userOutboundPort.publishPort();
		
		this.internalInboundPortURI = fridgeInternalControlInboundPortURI;
		this.internalOutboundPort = new FridgeInternalControlOutboundPort(this);
		this.internalOutboundPort.publishPort();
		
		this.externalInboundPortURI = fridgeExternalControlInboundPortURI;
		this.externalOutboundPort = new FridgeExternalControlOutboundPort(this);
		this.externalOutboundPort.publishPort();

		// Tracing
		this.tracer.get().setTitle("Fridge tester component");
		this.tracer.get().setRelativePosition(X_RELATIVE_POSITION, Y_RELATIVE_POSITION);
		this.toggleTracing();
	}
	
	
	// -------------------------------------------------------------------------
	// Component life-cycle
	// -------------------------------------------------------------------------

	@Override
	public synchronized void start() throws ComponentStartException {
		super.start();

		try {
			this.doPortConnection(
					this.internalOutboundPort.getPortURI(),
					this.internalInboundPortURI,
					FridgeInternalControlConnector.class.getCanonicalName());
			this.doPortConnection(
					this.externalOutboundPort.getPortURI(),
					this.externalInboundPortURI,
					FridgeExternalControlConnector.class.getCanonicalName());
			this.doPortConnection(
					this.userOutboundPort.getPortURI(),
					this.userInboundPortURI,
					FridgeUserConnector.class.getCanonicalName());
		} catch (Exception e) {
			throw new ComponentStartException(e) ;
		}
	}

	@Override
	public synchronized void execute() throws Exception {
		/*
		if (this.isUnitTest) {
			this.runAllTests();
		} else {
			this.clocksServerOutboundPort = new ClocksServerOutboundPort(this);
			this.clocksServerOutboundPort.publishPort();
			this.doPortConnection(
					this.clocksServerOutboundPort.getPortURI(),
					ClocksServer.STANDARD_INBOUNDPORT_URI,
					ClocksServerConnector.class.getCanonicalName());
			System.out.println("Heater tester gets the clock");
			AcceleratedClock ac =
					this.clocksServerOutboundPort.getClock(
										CVMIntegrationTest.TEST_CLOCK_URI);
			System.out.println("Heater tester waits until start");
			this.doPortDisconnection(
						this.clocksServerOutboundPort.getPortURI());
			this.clocksServerOutboundPort.unpublishPort();

			Instant heaterSwitchOn = Instant.parse("2023-09-20T15:00:02.00Z");
			Instant heaterSwitchOff = Instant.parse("2023-09-20T15:00:08.00Z");
			ac.waitUntilStart();
			System.out.println("Heater tester schedules switch on and off");
			long delayToSwitchOn = ac.nanoDelayUntilInstant(heaterSwitchOn);
			long delayToSwitchOff = ac.nanoDelayUntilInstant(heaterSwitchOff);

			// This is to avoid mixing the 'this' of the task object with the 'this'
			// representing the component object in the code of the next methods run
			AbstractComponent o = this;

			// schedule the switch on heater
			this.scheduleTaskOnComponent(
					new AbstractComponent.AbstractTask() {
						@Override
						public void run() {
							try {
								System.out.println("Heater switches on.");
								o.traceMessage("Heater switches on.\n");
								hop.switchOn();
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
					}, delayToSwitchOn, TimeUnit.NANOSECONDS);
			// schedule the switch off heater
			this.scheduleTaskOnComponent(
					new AbstractComponent.AbstractTask() {
						@Override
						public void run() {
							try {
								o.traceMessage("Heater switches off.\n");
								hop.switchOff();
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
					}, delayToSwitchOff, TimeUnit.NANOSECONDS);
		}*/
		
		this.runAllTest();
		System.out.println("Fridge Tester ends");
	}

	@Override
	public synchronized void finalise() throws Exception {
		this.doPortDisconnection(this.userOutboundPort.getPortURI());
		this.doPortDisconnection(this.externalOutboundPort.getPortURI());
		this.doPortDisconnection(this.internalOutboundPort.getPortURI());
		
		super.finalise();
	}

	@Override
	public synchronized void shutdown() throws ComponentShutdownException {
		try {
			this.userOutboundPort.unpublishPort();
			this.externalOutboundPort.unpublishPort();
			this.internalOutboundPort.unpublishPort();
		} catch (Exception e) {
			throw new ComponentShutdownException(e) ;
		}
		super.shutdown();
	}
	
	
	// -------------------------------------------------------------------------
	// Component services implementation
	// -------------------------------------------------------------------------
	
	
	protected void testGetState() { 
		this.traceMessage("testGetState...\n");
		
		try {
			assertEquals(this.userOutboundPort.getState(), FridgeState.OFF);
		} catch (Exception e) {
			this.traceMessage("...KO.\n" + e);
			assertTrue(false);
		}
		
		this.traceMessage("testGetState done \n");
	}
	
	protected void testGetTargetTemperature() { 
		this.traceMessage("testGetTargetTemperature...\n");
		
		try {
			assertEquals(4.0, this.userOutboundPort.getTargetTemperature());
			assertEquals(4.0, this.internalOutboundPort.getTargetTemperature());
		} catch (Exception e) {
			this.traceMessage("...KO.\n" + e);
			assertTrue(false);
		}
		
		this.traceMessage("testGetTargetTemperature done \n");
	}
	
	protected void testGetMaxCoolingPower() { 
		this.traceMessage("testGetMaxCoolingPower...\n");
		
		try {
			assertEquals(500.0, this.userOutboundPort.getMaxCoolingPower());
			assertEquals(500.0, this.externalOutboundPort.getMaxCoolingPower());
		} catch (Exception e) {
			this.traceMessage("...KO.\n" + e);
			assertTrue(false);
		}
		
		this.traceMessage("testGetMaxCoolingPower done \n");
	}
	
	protected void testGetCurrentCoolingPower() { 
		this.traceMessage("testGetCurrentCoolingPower...\n");
		
		try {
			assertEquals(500.0, this.userOutboundPort.getCurrentCoolingPower());
			assertEquals(500.0, this.externalOutboundPort.getCurrentCoolingPower());
		} catch (Exception e) {
			this.traceMessage("...KO.\n" + e);
			assertTrue(false);
		}
		
		this.traceMessage("testGetCurrentCoolingPower done \n");
	}
	
	protected void testIsCooling() { 
		this.traceMessage("testIsCooling...\n");
		
		try {
			assertFalse(this.internalOutboundPort.isCooling());
		} catch (Exception e) {
			this.traceMessage("...KO.\n" + e);
			assertTrue(false);
		}
		
		this.traceMessage("testIsCooling done \n");	
	}
	
	protected void testSwitchOn() { 
		this.traceMessage("testSwitchOn...\n");
		
		try {
			this.userOutboundPort.switchOn();
			assertEquals(this.userOutboundPort.getState(), FridgeState.ON);
		} catch (Exception e) {
			this.traceMessage("...KO.\n" + e);
			assertTrue(false);
		}
		
		this.traceMessage("testSwitchOn done \n");
	}
	
	protected void testSwitchOff() { 
		this.traceMessage("testSwitchOff...\n");
		
		try {
			this.userOutboundPort.switchOff();
			assertEquals(this.userOutboundPort.getState(), FridgeState.OFF);
		} catch (Exception e) {
			this.traceMessage("...KO.\n" + e);
			assertTrue(false);
		}
		
		this.traceMessage("testSwitchOff done \n");
	}
	
	protected void testSetCurrentCoolingPower() { 
		this.traceMessage("testSetCurrentCoolingPower...\n");
		
		try {
			this.userOutboundPort.setCurrentCoolingPower(300.0);
			assertEquals(300.0, this.userOutboundPort.getCurrentCoolingPower());
			
			this.externalOutboundPort.setCurrentCoolingPower(200.0);
			assertEquals(200.0, this.externalOutboundPort.getCurrentCoolingPower());
		} catch (Exception e) {
			this.traceMessage("...KO.\n" + e);
			assertTrue(false);
		}
		
		this.traceMessage("testSetCurrentCoolingPower done \n");
	}
	
	protected void testSetTargetTemperature() { 
		this.traceMessage("testSetTargetTemperature...\n");
		
		try {
			this.userOutboundPort.setTargetTemperature(3.0);
			assertEquals(3.0, this.userOutboundPort.getTargetTemperature());
			
			this.externalOutboundPort.setTargetTemperature(2.0);
			assertEquals(2.0, this.userOutboundPort.getTargetTemperature());
		} catch (Exception e) {
			this.traceMessage("...KO.\n" + e);
			assertTrue(false);
		}
		
		this.traceMessage("testSetTargetTemperature done \n");
	}
	
	protected void testStartCooling() { 
		this.traceMessage("testStartCooling...\n");
		
		try {
			this.internalOutboundPort.startCooling();
			assertEquals(FridgeState.COOLING, this.userOutboundPort.getState());
		} catch (Exception e) {
			this.traceMessage("...KO.\n" + e);
			assertTrue(false);
		}
		
		this.traceMessage("testStartCooling done \n");
	}
	
	protected void testStopCooling() { 
		this.traceMessage("testStopCooling...\n");
		
		try {
			this.internalOutboundPort.stopCooling();
			assertEquals(FridgeState.ON, this.userOutboundPort.getState());
		} catch (Exception e) {
			this.traceMessage("...KO.\n" + e);
			assertTrue(false);
		}
		
		this.traceMessage("testStopCooling done \n");	
	}
	
	protected void testSwitchoff() {
		this.traceMessage("testSwitchoff...\n");
		
		try {
			this.userOutboundPort.switchOff();
			assertEquals(FridgeState.OFF, this.userOutboundPort.getState());
		} catch (Exception e) {
			this.traceMessage("...KO.\n" + e);
			assertTrue(false);
		}
		
		this.traceMessage("testSwitchoff done \n");
	}
	
	protected void runAllTest() {
		testGetState();
		testGetTargetTemperature(); 
		testGetMaxCoolingPower();		
		testGetCurrentCoolingPower();
		testIsCooling();
		testSwitchOn();
		testSetCurrentCoolingPower();
		testSetTargetTemperature();
		testStartCooling();
		testStopCooling();
		testSwitchoff();
	}
}
