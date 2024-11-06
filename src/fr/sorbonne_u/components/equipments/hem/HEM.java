package fr.sorbonne_u.components.equipments.hem;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashMap;

import fr.sorbonne_u.components.AbstractComponent;
import fr.sorbonne_u.components.annotations.OfferedInterfaces;
import fr.sorbonne_u.components.annotations.RequiredInterfaces;
import fr.sorbonne_u.components.equipments.battery.Battery;
import fr.sorbonne_u.components.equipments.battery.BatteryConnector;
import fr.sorbonne_u.components.equipments.battery.BatteryOutboundPort;
import fr.sorbonne_u.components.equipments.fridge.Fridge;
import fr.sorbonne_u.components.equipments.hem.adjustable.AdjustableCI;
import fr.sorbonne_u.components.equipments.hem.adjustable.AdjustableOutboundPort;
import fr.sorbonne_u.components.equipments.hem.adjustable.FridgeConnector;
import fr.sorbonne_u.components.equipments.hem.registration.RegistrationCI;
import fr.sorbonne_u.components.equipments.hem.registration.RegistrationI;
import fr.sorbonne_u.components.equipments.hem.registration.RegistrationInboundPort;
import fr.sorbonne_u.components.equipments.meter.ElectricMeter;
import fr.sorbonne_u.components.equipments.meter.connections.ElectricMeterConnector;
import fr.sorbonne_u.components.equipments.meter.connections.ElectricMeterOutboundPort;
import fr.sorbonne_u.components.equipments.windTurbine.WindTurbine;
import fr.sorbonne_u.components.equipments.windTurbine.WindTurbineConnector;
import fr.sorbonne_u.components.equipments.windTurbine.WindTurbineOutboundPort;
import fr.sorbonne_u.components.exceptions.ComponentShutdownException;
import fr.sorbonne_u.components.exceptions.ComponentStartException;
import fr.sorbonne_u.components.xmlReader.ClassCreator;
import fr.sorbonne_u.exceptions.PreconditionException;
import fr.sorbonne_u.utils.aclocks.AcceleratedClock;
import fr.sorbonne_u.components.equipments.meter.interfaces.ElectricMeterCI;
import fr.sorbonne_u.components.equipments.battery.BatteryCI;

@OfferedInterfaces(offered = {RegistrationCI.class})
@RequiredInterfaces(required = {AdjustableCI.class, ElectricMeterCI.class, BatteryCI.class})
public class HEM extends AbstractComponent implements RegistrationI {

	// -------------------------------------------------------------------------
	// Constants and variables
	// -------------------------------------------------------------------------

	public static boolean VERBOSE = true;
	public static int X_RELATIVE_POSITION = 0;
	public static int Y_RELATIVE_POSITION = 0;

	protected ElectricMeterOutboundPort electricMeterPort;

	protected AdjustableOutboundPort controlFridgePort;

	protected AcceleratedClock ac;
	
	public static final String URI_REGISTRATION_INBOUND_PORT = "URI_REGISTRATION_INBOUND_PORT";
	protected RegistrationInboundPort registrationPort;
	protected HashMap<String, AdjustableOutboundPort> registeredUriModularEquipement;
	
	// Test
	public static boolean TEST_COMMUNICATION_WITH_FRIDGE; 
	protected boolean isTestElectricMetter;
	protected boolean isTestFridge;
	public static final boolean IS_INTEGRATION_TEST = false;
	
	// Battery (If it's an integration test)
	public final static String URI_BATTERY_OUTBOUND_PORT = "URI_BATTERY_OUTBOUND_PORT";
	protected BatteryOutboundPort batteryOutboundPort;
	
	// Wind turbine (If it's an integration test)
	public final static String URI_WIND_TURBINE_OUTBOUND_PORT = "URI_WIND_TURBINE_OUTBOUND_PORT";
	protected WindTurbineOutboundPort windTurbineOutboundPort;

	
	// -------------------------------------------------------------------------
	// Constructors
	// -------------------------------------------------------------------------

	protected HEM() {
		super(2, 1);
		
		this.isTestElectricMetter = true;
		this.isTestFridge = true;
		
		registeredUriModularEquipement = new HashMap<String, AdjustableOutboundPort>();

		if (VERBOSE) {
			this.tracer.get().setTitle("Home Energy Manager component");
			this.tracer.get().setRelativePosition(X_RELATIVE_POSITION,
												  Y_RELATIVE_POSITION);
			this.toggleTracing();
		}
	}
	
	protected HEM(boolean isTestElectricMetter) throws Exception {
		super(1, 1);
		
		this.isTestElectricMetter = isTestElectricMetter;
		this.isTestFridge = false;
		
		this.initialisePorts();

		if (VERBOSE) {
			this.tracer.get().setTitle("Home Energy Manager component");
			this.tracer.get().setRelativePosition(X_RELATIVE_POSITION,
												  Y_RELATIVE_POSITION);
			this.toggleTracing();
		}
	}
	
	protected HEM(boolean isTestElectricMetter, boolean isTestFridge) throws Exception {
		super(1, 1);
		
		this.isTestElectricMetter = isTestElectricMetter;
		this.isTestFridge = isTestFridge;
		
		this.initialisePorts();

		if (VERBOSE) {
			this.tracer.get().setTitle("Home Energy Manager component");
			this.tracer.get().setRelativePosition(X_RELATIVE_POSITION,
												  Y_RELATIVE_POSITION);
			this.toggleTracing();
		}
	}
	
	protected void initialisePorts() throws Exception {
		registeredUriModularEquipement = new HashMap<String, AdjustableOutboundPort>();
		
		this.registrationPort = new RegistrationInboundPort(URI_REGISTRATION_INBOUND_PORT, this);
		this.registrationPort.publishPort();
		
		if(IS_INTEGRATION_TEST) {
			this.windTurbineOutboundPort = new WindTurbineOutboundPort(URI_WIND_TURBINE_OUTBOUND_PORT, this);
			this.windTurbineOutboundPort.publishPort();
			
			this.batteryOutboundPort = new BatteryOutboundPort(URI_BATTERY_OUTBOUND_PORT, this);
			this.batteryOutboundPort.publishPort();
		}
	}

	
	// -------------------------------------------------------------------------
	// Component life-cycle
	// -------------------------------------------------------------------------
	
	@Override
	public synchronized void start() throws ComponentStartException {
		super.start();

		try {
			if(this.isTestElectricMetter) {
				this.electricMeterPort = new ElectricMeterOutboundPort(this);
				this.electricMeterPort.publishPort();
				this.doPortConnection(
						this.electricMeterPort.getPortURI(),
						ElectricMeter.ELECTRIC_METER_INBOUND_PORT_URI,
						ElectricMeterConnector.class.getCanonicalName());	
			}

			if(this.isTestFridge) {
				this.controlFridgePort = new AdjustableOutboundPort(this);
				this.controlFridgePort.publishPort();
				this.doPortConnection(
						this.controlFridgePort.getPortURI(),
						Fridge.EXTERNAL_CONTROL_INBOUND_PORT_URI,
						FridgeConnector.class.getCanonicalName());	
			}
			
			if(IS_INTEGRATION_TEST) {
				this.doPortConnection(
						this.windTurbineOutboundPort.getPortURI(),
						WindTurbine.INBOUND_PORT_URI,
						WindTurbineConnector.class.getCanonicalName());
				
				this.doPortConnection(
						this.batteryOutboundPort.getPortURI(),
						Battery.INTERNAL_INBOUND_PORT,
						BatteryConnector.class.getCanonicalName());
			}
			
		} catch (Exception e) {
			throw new ComponentStartException(e) ;
		}
	}
	
	@Override
	public synchronized void execute() throws Exception {
		/*
		this.ac = null;
		ClocksServerOutboundPort clocksServerOutboundPort =
											new ClocksServerOutboundPort(this);
		clocksServerOutboundPort.publishPort();
		this.doPortConnection(
					clocksServerOutboundPort.getPortURI(),
					ClocksServer.STANDARD_INBOUNDPORT_URI,
					ClocksServerConnector.class.getCanonicalName());
		this.traceMessage("HEM gets the clock.\n");
		this.ac = clocksServerOutboundPort.getClock(CVMIntegrationTest.CLOCK_URI);
		this.doPortDisconnection(clocksServerOutboundPort.getPortURI());
		clocksServerOutboundPort.unpublishPort();
		this.traceMessage("HEM waits until start time.\n");
		this.ac.waitUntilStart();
		this.traceMessage("HEM starts.\n");*/
		
		if(this.isTestElectricMetter)
			this.testMeter();
		
		if(this.isTestFridge)
			this.testFridge();
	}
	
	@Override
	public synchronized void finalise() throws Exception
	{
		if(this.isTestFridge)
			this.doPortDisconnection(this.controlFridgePort.getPortURI());
		
		if(this.isTestElectricMetter)
			this.doPortDisconnection(this.electricMeterPort.getPortURI());
		
		if(IS_INTEGRATION_TEST) {
			this.doPortDisconnection(this.windTurbineOutboundPort.getPortURI());
			this.doPortDisconnection(this.batteryOutboundPort.getPortURI());
		}
		
		super.finalise();
	}

	/**
	 * @see fr.sorbonne_u.components.AbstractComponent#shutdown()
	 */
	@Override
	public synchronized void	shutdown() throws ComponentShutdownException
	{
		try {
			if(this.isTestFridge) 
				this.controlFridgePort.unpublishPort();
			
			this.registrationPort.unpublishPort();
			if(this.isTestElectricMetter)
				this.electricMeterPort.unpublishPort();
			if(IS_INTEGRATION_TEST) {
				this.windTurbineOutboundPort.unpublishPort();
				this.batteryOutboundPort.unpublishPort();
			}
			
		} catch (Exception e) {
			throw new ComponentShutdownException(e) ;
		}
		super.shutdown();
	}
	
	
	// -------------------------------------------------------------------------
	// Internal methods
	// -------------------------------------------------------------------------

	protected void	testMeter() throws Exception {
		this.traceMessage("testMeter()\n");
		
		this.traceMessage("Electric meter current consumption? " +
				this.electricMeterPort.getCurrentConsumption() + "\n");
		this.traceMessage("Electric meter current production? " +
				this.electricMeterPort.getCurrentProduction() + "\n");
		
		this.traceMessage("testMeter done...\n");
	}
	
	protected void testFridge() throws Exception {
		this.traceMessage("testFridge()\n");
		
		traceMessage("Fridge maxMode index? " +
						this.controlFridgePort.maxMode() + "\n");
		traceMessage("Fridge current mode index? " +
						this.controlFridgePort.currentMode() + "\n");
		traceMessage("Fridge going down one mode? " +
						this.controlFridgePort.downMode() + "\n");
		traceMessage("Fridge current mode is? " +
						this.controlFridgePort.currentMode() + "\n");
		traceMessage("Fridge going up one mode? " +
						this.controlFridgePort.upMode() + "\n");
		traceMessage("Fridge current mode is? " +
						this.controlFridgePort.currentMode() + "\n");
		traceMessage("Fridge setting current mode? " +
						this.controlFridgePort.setMode(1) + "\n");
		traceMessage("Fridge current mode is? " +
						this.controlFridgePort.currentMode() + "\n");
		traceMessage("Fridge is suspended? " +
						this.controlFridgePort.suspended() + "\n");
		traceMessage("Fridge suspends? " +
						this.controlFridgePort.suspend() + "\n");
		traceMessage("Fridge is suspended? " +
						this.controlFridgePort.suspended() + "\n");
		traceMessage("Fridge emergency? " +
						this.controlFridgePort.emergency() + "\n");
		
		Thread.sleep(1000);
		
		traceMessage("Fridge emergency? " +
						this.controlFridgePort.emergency() + "\n");
		traceMessage("Fridge resumes? " +
						this.controlFridgePort.resume() + "\n");
		traceMessage("Fridge is suspended? " +
						this.controlFridgePort.suspended() + "\n");
		traceMessage("Fridge current mode is? " +
						this.controlFridgePort.currentMode() + "\n");
		
		this.traceMessage("testFridge done...\n");
	}

	@Override
	public boolean registered(String uid) throws Exception {
		if(VERBOSE)
			this.traceMessage("registered \n");
		
		if(this.registeredUriModularEquipement.containsKey(uid)) {
			if(VERBOSE)
				this.traceMessage(uid + " is registered \n");
			return true;
		}
		
		if(VERBOSE)
			this.traceMessage(uid + " is not registered \n");
		return false;
	}

	@Override
	public boolean register(String uid, String controlPortURI, String xmlControlAdapter) throws Exception {
		if(VERBOSE)
			this.traceMessage("register of " + uid + "\n");
		
		if(registered(uid)) {
			if(VERBOSE)
				this.traceMessage("Impossible to register " + uid + " because it's not registered\n");
			
			return false;
		}
	
		AdjustableOutboundPort ao = new AdjustableOutboundPort(this);
		ao.publishPort();
		this.registeredUriModularEquipement.put(uid, ao);
		ClassCreator classCreator = new ClassCreator(xmlControlAdapter);
		Class<?> classConnector = classCreator.createClass();

		this.doPortConnection(ao.getPortURI(), 
				controlPortURI, 
				classConnector.getCanonicalName());
		
		if(TEST_COMMUNICATION_WITH_FRIDGE)
			this.scenarioCommunicationWithFridge(uid);
		
		return true;
	}

	@Override
	public void unregister(String uid) throws Exception {
		if(VERBOSE)
			this.traceMessage("unregister of" + uid + "\n");
		if(registered(uid)) {
			this.doPortDisconnection(this.registeredUriModularEquipement.get(uid).getPortURI());
			this.registeredUriModularEquipement.get(uid).unpublishPort();
			this.registeredUriModularEquipement.remove(uid);
		}
	}
	
	public void scenarioCommunicationWithFridge(String fridgeURI) throws Exception {
		if(VERBOSE)
			this.traceMessage("\n\nStart scenario between the HEM and the fridge\n");
		
		assert this.registeredUriModularEquipement.containsKey(fridgeURI) :
			new PreconditionException("Impossible test the commmunication with the fridge because it's not connect to the HEM");
		
		AdjustableOutboundPort ao = this.registeredUriModularEquipement.get(fridgeURI);
		
		if(VERBOSE)
			this.traceMessage("maxMode()\n");
		assertEquals(ao.maxMode(), 3);
		if(VERBOSE)
			this.traceMessage("done...\n");
		
		if(VERBOSE)
			this.traceMessage("currentMode()\n");
		assertEquals(ao.currentMode(), 3);
		if(VERBOSE)
			this.traceMessage("done...\n");
		
		if(VERBOSE)
			this.traceMessage("downMode()\n");
		assertTrue(ao.downMode());
		assertEquals(ao.currentMode(), 2);
		
		if(VERBOSE)
			this.traceMessage("done...\n");
		
		if(VERBOSE)
			this.traceMessage("upMode()\n");
		assertTrue(ao.upMode());
		assertEquals(ao.currentMode(), 3);
		if(VERBOSE)
			this.traceMessage("done...\n");
		
		if(VERBOSE)
			this.traceMessage("setMode()\n");
		assertTrue(ao.setMode(1));
		assertEquals(ao.currentMode(), 1);
		if(VERBOSE)
			this.traceMessage("done...\n");
		
		if(VERBOSE)
			this.traceMessage("suspended()\n");
		assertFalse(ao.suspended());
		if(VERBOSE)
			this.traceMessage("done...\n");
		
		if(VERBOSE)
			this.traceMessage("suspend()\n");
		assertTrue(ao.suspend());
		assertTrue(ao.suspended());
		if(VERBOSE)
			this.traceMessage("done...\n");
		
		if(VERBOSE)
			this.traceMessage("resume()\n");
		assertTrue(ao.resume());
		assertFalse(ao.suspended());
		if(VERBOSE)
			this.traceMessage("done...\n");
		
		if(VERBOSE)
			this.traceMessage("emergency...\n");
		if(VERBOSE)
			this.traceMessage("done...\n");
		
		if(VERBOSE)
			this.traceMessage("End of scenario between the HEM and the fridge \n\n");
	}
}
