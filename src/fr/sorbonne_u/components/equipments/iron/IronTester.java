package fr.sorbonne_u.components.equipments.iron;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import fr.sorbonne_u.components.AbstractComponent;
import fr.sorbonne_u.components.annotations.RequiredInterfaces;
import fr.sorbonne_u.components.equipments.iron.IronImplementationI.IronEnergySavingMode;
import fr.sorbonne_u.components.equipments.iron.IronImplementationI.IronState;
import fr.sorbonne_u.components.equipments.iron.IronImplementationI.IronSteam;
import fr.sorbonne_u.components.equipments.iron.IronImplementationI.IronTemperature;
import fr.sorbonne_u.components.exceptions.ComponentShutdownException;
import fr.sorbonne_u.components.exceptions.ComponentStartException;

@RequiredInterfaces(required={IronUserCI.class})
public class IronTester extends AbstractComponent {
	
	// -------------------------------------------------------------------------
	// Constants and variables
	// -------------------------------------------------------------------------
		
	public static boolean VERBOSE = true;
	public static int X_RELATIVE_POSITION = 1;
	public static int Y_RELATIVE_POSITION = 1;
	
	protected IronOutboundPort outboundPort;
	protected String inboudPortURI;
	
	// -------------------------------------------------------------------------
	// Constructors
	// -------------------------------------------------------------------------

	protected IronTester() throws Exception {
		this(Iron.INBOUND_PORT_URI);
	}

	protected IronTester(String ironInboundPortURI) throws Exception {
		super(1, 0);

		this.initialise(ironInboundPortURI);
	}
	
	protected IronTester(String ironInboundPortURI, String reflectionInboundPortURI ) throws Exception {
		super(reflectionInboundPortURI, 1, 0);

		this.initialise(ironInboundPortURI);
	}
	
	protected void initialise(String ironInboundPortURI) throws Exception {
		this.inboudPortURI = ironInboundPortURI;
		this.outboundPort = new IronOutboundPort(this);
		this.outboundPort.publishPort();

		if (VERBOSE) {
			this.tracer.get().setTitle("Iron tester component");
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
							IronConnector.class.getCanonicalName());
		} catch (Exception e) {
			throw new ComponentStartException(e) ;
		}
	}

	@Override
	public synchronized void execute() throws Exception {
		this.runAllTests();
	}

	@Override
	public synchronized void finalise() throws Exception {
		this.doPortDisconnection(this.outboundPort.getPortURI());
		super.finalise();
	}

	@Override
	public synchronized void	shutdown() throws ComponentShutdownException {
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
			assertEquals(IronState.OFF, this.outboundPort.getState());
		} catch (Exception e) {
			this.logMessage("...KO.");
			assertTrue(false);
		}
		this.logMessage("...done.");
	}
	
	public void	testTurnOn() {
		this.logMessage("testTurnOn()... ");
		try {
			this.outboundPort.turnOn();
			assertEquals(IronState.ON, this.outboundPort.getState());
		} catch (Exception e) {
			assertTrue(false);
		}
		this.logMessage("...done.");
	}
	
	public void	testTurnOff() {
		this.logMessage("testTurnOff()... ");
		try {
			this.outboundPort.turnOff();
			assertEquals(IronState.OFF, this.outboundPort.getState());
		} catch (Exception e) {
			assertTrue(false);
		}
		this.logMessage("...done.");
	}
	
	public void testGetTemperature() {
		this.logMessage("testGetTemperature()... ");
		try {
			assertEquals(IronTemperature.DELICATE, this.outboundPort.getTemperature());
		} catch (Exception e) {
			assertTrue(false);
		}
		this.logMessage("...done.");
	}
	
	public void testSetTemperature() {
		this.logMessage("testSetTemperature()... ");
		try {
			this.outboundPort.setTemperature(IronTemperature.COTTON);
			assertEquals(IronTemperature.COTTON, this.outboundPort.getTemperature());
		} catch (Exception e) {
			assertTrue(false);
		}
		this.logMessage("...done.");
	}
	
	public void testGetSteam() {
		this.logMessage("testGetSteam()... ");
		try {
			assertEquals(IronSteam.INACTIVE, this.outboundPort.getSteam());
		} catch (Exception e) {
			assertTrue(false);
		}
		this.logMessage("...done.");
	} 
	
	public void testSetSteam() {
		this.logMessage("testSetSteam()... ");
		try {
			this.outboundPort.setSteam(IronSteam.ACTIVE);
			assertEquals(IronSteam.ACTIVE, this.outboundPort.getSteam());
		} catch (Exception e) {
			assertTrue(false);
		}
		this.logMessage("...done.");
	}
	
	public void testGetEnergySavingMode() {
		this.logMessage("testGetEnergySavingMode()... ");
		try {
			assertEquals(IronEnergySavingMode.INACTIVE, this.outboundPort.getEnergySavingMode());
		} catch (Exception e) {
			assertTrue(false);
		}
		this.logMessage("...done.");
	} 
	
	public void testSetEnergySavingMode() {
		this.logMessage("testSetEnergySavingMode()... ");
		try {
			this.outboundPort.setEnergySavingMode(IronEnergySavingMode.ACTIVE);
			assertEquals(IronEnergySavingMode.ACTIVE, this.outboundPort.getEnergySavingMode());
		} catch (Exception e) {
			assertTrue(false);
		}
		this.logMessage("...done.");
	}
	
	protected void runAllTests() {
		this.testGetState();
		this.testTurnOn();
		this.testTurnOff();
		this.testGetTemperature();
		this.testSetTemperature();
		this.testGetSteam();
		this.testSetSteam();
		this.testGetEnergySavingMode();
		this.testSetEnergySavingMode();
	}
}
