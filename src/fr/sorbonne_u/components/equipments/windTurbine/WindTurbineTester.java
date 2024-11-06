package fr.sorbonne_u.components.equipments.windTurbine;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;

import fr.sorbonne_u.components.AbstractComponent;
import fr.sorbonne_u.components.annotations.RequiredInterfaces;
import fr.sorbonne_u.components.exceptions.ComponentShutdownException;
import fr.sorbonne_u.components.exceptions.ComponentStartException;

@RequiredInterfaces(required = {WindTurbineCI.class})
public class WindTurbineTester extends AbstractComponent {
	public static boolean VERBOSE = true;
	public static int X_RELATIVE_POSITION = 1;
	public static int Y_RELATIVE_POSITION = 1;
	
	protected static final String OUTBOUND_PORT_URI = "OUTBOUND_PORT_URI"; 
	protected WindTurbineOutboundPort outboundPort;
	
	protected WindTurbineTester() throws Exception {
		super(1, 0);
		
		this.outboundPort = new WindTurbineOutboundPort(OUTBOUND_PORT_URI, this);
		this.outboundPort.publishPort();
		
		if(VERBOSE) {
			this.tracer.get().setTitle("Wind Turbine tester component");
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
							WindTurbine.INBOUND_PORT_URI,
							WindTurbineConnector.class.getCanonicalName());
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
	
	
	protected void testIsActivate() {
		if(VERBOSE)
			this.traceMessage("testIsActivate() \n");
		
		try {
			assertFalse(this.outboundPort.isActivate());
		} catch(Exception e) {
			assertTrue(false);
		}
		
		if(VERBOSE)
			this.traceMessage("done... \n");
	}
	
	protected void testActivate() {
		if(VERBOSE)
			this.traceMessage("testActivate() \n");
		
		try {
			this.outboundPort.activate();
			assertTrue(this.outboundPort.isActivate());
		} catch(Exception e) {
			assertTrue(false);
		}

		
		if(VERBOSE)
			this.traceMessage("done... \n");
	}
	
	protected void testStop() {
		if(VERBOSE)
			this.traceMessage("testStop() \n");
		
		try {
			this.outboundPort.stop();
			assertFalse(this.outboundPort.isActivate());
		} catch(Exception e) {
			assertTrue(false);
		}
		
		if(VERBOSE)
			this.traceMessage("done... \n");
	}
	
	protected void testGetCurrentProduction() {
		if(VERBOSE)
			this.traceMessage("testGetCurrentProduction() \n");
		
		try {
			assertEquals(0.0, this.outboundPort.getCurrentProduction());
		} catch(Exception e) {
			assertTrue(false);
		}

		if(VERBOSE)
			this.traceMessage("done... \n");
	}
	
	protected void runAllTests() {
		if(VERBOSE)
			this.traceMessage("Start tests \n");
		
		this.testIsActivate();
		this.testActivate();
		this.testStop();
		this.testGetCurrentProduction();
		
		if(VERBOSE)
			this.traceMessage("All tests are done \n");
	}
}
