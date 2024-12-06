package fr.sorbonne_u.components.equipments.battery;

import fr.sorbonne_u.components.AbstractComponent;
import fr.sorbonne_u.components.annotations.RequiredInterfaces;
import fr.sorbonne_u.components.equipments.battery.BatteryI.STATE;
import fr.sorbonne_u.components.exceptions.ComponentShutdownException;
import fr.sorbonne_u.components.exceptions.ComponentStartException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@RequiredInterfaces(required = {BatteryCI.class})
public class BatteryTester extends AbstractComponent {
	
	public static boolean VERBOSE = true;
	public static int X_RELATIVE_POSITION = 1;
	public static int Y_RELATIVE_POSITION = 1;
	
	protected static final String OUTBOUND_PORT_URI = "OUTBOUND_PORT_URI"; 
	protected BatteryOutboundPort outboundPort;
	
	protected BatteryTester() throws Exception {
		super(1, 0);
		
		this.outboundPort = new BatteryOutboundPort(OUTBOUND_PORT_URI, this);
		this.outboundPort.publishPort();
		
		if(VERBOSE) {
			this.tracer.get().setTitle("Battery tester component");
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
							Battery.INTERNAL_INBOUND_PORT,
							BatteryConnector.class.getCanonicalName());
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
	
	protected void testGetState() throws Exception {
		if(VERBOSE)
			this.traceMessage("testGetState() \n");
		try {
			assertEquals(BatteryI.STATE.STANDBY, this.outboundPort.getState());
		} catch(Exception e) {
			assertTrue(false);
		}
		if(VERBOSE)
			this.traceMessage("done... \n");
	}
	
	protected void testSetState() throws Exception {
		if(VERBOSE)
			this.traceMessage("testSetState() \n");
		
		try {
			this.outboundPort.setState(STATE.PRODUCT);
			
			assertEquals(BatteryI.STATE.PRODUCT, this.outboundPort.getState());
		} catch(Exception e) {
			assertTrue(false);
		}
		
		if(VERBOSE)
			this.traceMessage("done... \n");
	}
	
	protected void testGetBatteryLevel() throws Exception {
		if(VERBOSE)
			this.traceMessage("testGetPowerLevel() \n");
		
		try {
			assertEquals(this.outboundPort.getBatteryLevel(), 0.0);
		} catch(Exception e) {
			assertTrue(false);
		}
		
		if(VERBOSE)
			this.traceMessage("done... \n");
	}
	
	protected void runAllTests() throws Exception {
		if(VERBOSE)
			this.traceMessage("Tests start... \n");
		
		this.testGetState();
		this.testSetState();
		this.testGetBatteryLevel();
		
		if(VERBOSE)
			this.traceMessage("Battery tests end... \n");
	}
}
