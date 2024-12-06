package fr.sorbonne_u.components.equipments.generator;

import fr.sorbonne_u.components.AbstractComponent;
import fr.sorbonne_u.components.annotations.RequiredInterfaces;
import fr.sorbonne_u.components.equipments.generator.connectors.GeneratorConnector;
import fr.sorbonne_u.components.equipments.generator.interfaces.GeneratorCI;
import fr.sorbonne_u.components.equipments.generator.ports.GeneratorOutboundPort;
import fr.sorbonne_u.components.exceptions.ComponentShutdownException;
import fr.sorbonne_u.components.exceptions.ComponentStartException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@RequiredInterfaces(required = {GeneratorCI.class})
public class GeneratorUnitTest extends AbstractComponent {

	public static boolean VERBOSE = true;
	public static int X_RELATIVE_POSITION = 1;
	public static int Y_RELATIVE_POSITION = 1;
	
	protected static final String OUTBOUND_PORT_URI = "OUTBOUND_PORT_URI"; 
	protected GeneratorOutboundPort outboundPort;
	
	protected static final double DEFAULT_PRODUCTION = 100.0;
	
	protected GeneratorUnitTest() throws Exception {
		super(1, 0);
		
		this.outboundPort = new GeneratorOutboundPort(OUTBOUND_PORT_URI, this);
		this.outboundPort.publishPort();
		
		if(VERBOSE) {
			this.tracer.get().setTitle("Generator tester component");
			this.tracer.get().setRelativePosition(X_RELATIVE_POSITION, Y_RELATIVE_POSITION);
			this.toggleTracing();
		}
	}
	
	
	@Override
	public synchronized void start() throws ComponentStartException {
		super.start();

		try {
			this.doPortConnection(
							this.outboundPort.getPortURI(),
							Generator.INBOUND_PORT_URI,
							GeneratorConnector.class.getCanonicalName());
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
	
	protected void testIsRunning() {
		if(VERBOSE) 
            logMessage("testIsRunning()");
		try {
			assertEquals(false, this.outboundPort.isRunning());
		} catch(Exception e) {
			if(VERBOSE) 
	            logMessage("Failed... -> " + e.getMessage());
			assertTrue(false);
		}
		
		if(VERBOSE) 
            logMessage("done...\n");
	}
	
	protected void testActivate() {
		if(VERBOSE) 
            logMessage("testActivate()");
		
		try {
			this.outboundPort.activate();
			assertEquals(true, this.outboundPort.isRunning());
		} catch(Exception e) {
			if(VERBOSE) 
	            logMessage("Failed... -> " + e.getMessage());
			assertTrue(false);
		}

		if(VERBOSE) 
			logMessage("done...\n");
	}
	
	protected void testGetEnergyProduction() {
		if(VERBOSE) 
            logMessage("testGetEnergyProduction()");
		
		try {
			assertEquals(DEFAULT_PRODUCTION, this.outboundPort.getEnergyProduction());
		} catch(Exception e) {
			if(VERBOSE) 
	            logMessage("Failed... -> " + e.getMessage());
			assertTrue(false);
		}

		if(VERBOSE) 
			logMessage("done...\n");
	}
	
	protected void testGetFuelLevel() {
		if(VERBOSE) 
            logMessage("testGetFuelLevel()");
		
		try {
			assertEquals(0.0, this.outboundPort.getFuelLevel());
		} catch(Exception e) {
			if(VERBOSE) 
	            logMessage("Failed... -> " + e.getMessage());
			assertTrue(false);
		}

		if(VERBOSE) 
			logMessage("done...\n");
	}
	
	protected void testFill() {
		if(VERBOSE) 
            logMessage("testFill()");
		
		try {
			this.outboundPort.fill(100.0);
			assertEquals(100.0, this.outboundPort.getFuelLevel());
		} catch(Exception e) {
			if(VERBOSE) 
	            logMessage("Failed... -> " + e.getMessage());
			assertTrue(false);
		}

		if(VERBOSE) 
			logMessage("done...\n");
	}
	
	protected void testStop() {
		if(VERBOSE) 
            logMessage("testStop()");
		
		try {
			this.outboundPort.stop();
			assertEquals(false, this.outboundPort.isRunning());
		} catch(Exception e) {
			if(VERBOSE) 
	            logMessage("Failed... -> " + e.getMessage());
			assertTrue(false);
		}

		if(VERBOSE) 
			logMessage("done...\n");
	}
	
	
	protected void runAllTests() {
		this.testIsRunning();
		this.testActivate();
		this.testGetEnergyProduction();
		this.testGetFuelLevel();
		this.testStop();
		this.testFill();
		
		if(VERBOSE) 
			logMessage("All tests done...");
	}
}
