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
import fr.sorbonne_u.components.equipments.battery.BatteryI;
import fr.sorbonne_u.components.equipments.battery.BatteryOutboundPort;
import fr.sorbonne_u.components.equipments.fridge.Fridge;
import fr.sorbonne_u.components.equipments.generator.Generator;
import fr.sorbonne_u.components.equipments.generator.connectors.GeneratorHEMConnector;
import fr.sorbonne_u.components.equipments.generator.ports.GeneratorHEMOutboundPort;
import fr.sorbonne_u.components.equipments.hem.adjustable.AdjustableCI;
import fr.sorbonne_u.components.equipments.hem.adjustable.AdjustableOutboundPort;
import fr.sorbonne_u.components.equipments.hem.registration.RegistrationCI;
import fr.sorbonne_u.components.equipments.hem.registration.RegistrationI;
import fr.sorbonne_u.components.equipments.hem.registration.RegistrationInboundPort;
import fr.sorbonne_u.components.equipments.meter.ElectricMeter;
import fr.sorbonne_u.components.equipments.meter.connections.ElectricMeterConnector;
import fr.sorbonne_u.components.equipments.meter.connections.ElectricMeterOutboundPort;
import fr.sorbonne_u.components.equipments.windTurbine.WindTurbine;
import fr.sorbonne_u.components.equipments.windTurbine.WindTurbineCI;
import fr.sorbonne_u.components.equipments.windTurbine.WindTurbineConnector;
import fr.sorbonne_u.components.equipments.windTurbine.WindTurbineOutboundPort;
import fr.sorbonne_u.components.exceptions.ComponentShutdownException;
import fr.sorbonne_u.components.exceptions.ComponentStartException;
import fr.sorbonne_u.components.xmlReader.ClassCreator;
import fr.sorbonne_u.exceptions.PreconditionException;
import fr.sorbonne_u.components.equipments.meter.interfaces.ElectricMeterCI;
import fr.sorbonne_u.components.equipments.smartLighting.SmartLighting;
import fr.sorbonne_u.components.equipments.battery.BatteryCI;
import fr.sorbonne_u.components.equipments.generator.interfaces.GeneratorHEMCI;

@OfferedInterfaces(offered = {RegistrationCI.class})
@RequiredInterfaces(required = {AdjustableCI.class, ElectricMeterCI.class, BatteryCI.class, WindTurbineCI.class, GeneratorHEMCI.class})
public class HEM extends AbstractComponent implements RegistrationI {

	// -------------------------------------------------------------------------
	// Constants and variables
	// -------------------------------------------------------------------------
	
	public static enum TestType {
		INTEGRATION,
		FRIDGE, // Done
		SMART_LIGHTING,
		GENERATOR, // Done
		WIND_TURBINE, // Done
		METER, // Done
		BATTERY // Done
	};

	public static boolean VERBOSE = true;
	public static int X_RELATIVE_POSITION = 0;
	public static int Y_RELATIVE_POSITION = 0;

	protected TestType testType;

	// Registration ports
	public static final String URI_REGISTRATION_INBOUND_PORT = "URI_REGISTRATION_INBOUND_PORT";
	protected RegistrationInboundPort registrationPort;
	protected HashMap<String, AdjustableOutboundPort> registeredUriModularEquipement;
	
	// Components ports
	protected ElectricMeterOutboundPort electricMeterPort;
	protected AdjustableOutboundPort controlFridgePort;
	protected AdjustableOutboundPort controlSmartLightingPort;
	protected BatteryOutboundPort batteryOutboundPort;
	protected WindTurbineOutboundPort windTurbineOutboundPort;
	protected GeneratorHEMOutboundPort generatorHEMOutboundPort;

	
	// -------------------------------------------------------------------------
	// Constructors
	// -------------------------------------------------------------------------
	
	protected HEM(TestType testType) throws Exception {
		super(1, 0);
		
		this.testType = testType;
		this.initialisePorts();
		
		this.registeredUriModularEquipement = new HashMap<String, AdjustableOutboundPort>();
		
		if (VERBOSE) {
			this.tracer.get().setTitle("Home Energy Manager component");
			this.tracer.get().setRelativePosition(X_RELATIVE_POSITION,
												  Y_RELATIVE_POSITION);
			this.toggleTracing();
		}
	}
	
	
	protected void initialisePorts() throws Exception {
		if(testType == TestType.INTEGRATION || testType == TestType.METER) {
			this.electricMeterPort = new ElectricMeterOutboundPort(this);
			this.electricMeterPort.publishPort();
		}
		
		if(testType == TestType.INTEGRATION || testType == TestType.BATTERY) {
			this.batteryOutboundPort = new BatteryOutboundPort(this);
			this.batteryOutboundPort.publishPort();
		}
		
		if(testType == TestType.INTEGRATION || testType == TestType.WIND_TURBINE) {
			this.windTurbineOutboundPort = new WindTurbineOutboundPort(this);
			this.windTurbineOutboundPort.publishPort();
		}
		
		if(testType == TestType.INTEGRATION || testType == TestType.GENERATOR) {
			this.generatorHEMOutboundPort = new GeneratorHEMOutboundPort(this);
			this.generatorHEMOutboundPort.publishPort();
		}
		
		if(testType == TestType.INTEGRATION || testType == TestType.FRIDGE || testType == TestType.SMART_LIGHTING) {
			this.registrationPort = new RegistrationInboundPort(URI_REGISTRATION_INBOUND_PORT, this);
			this.registrationPort.publishPort();
		}
	}
	
	// -------------------------------------------------------------------------
	// Component life-cycle
	// -------------------------------------------------------------------------
	
	@Override
	public synchronized void start() throws ComponentStartException {
		super.start();

		try {
			
			if(testType == TestType.INTEGRATION || testType == TestType.METER) {
				this.doPortConnection(
						this.electricMeterPort.getPortURI(), 
						ElectricMeter.ELECTRIC_METER_INBOUND_PORT_URI, 
						ElectricMeterConnector.class.getCanonicalName());
			}
			
			if(testType == TestType.INTEGRATION || testType == TestType.BATTERY) {
				this.doPortConnection(
						this.batteryOutboundPort.getPortURI(), 
						Battery.INTERNAL_INBOUND_PORT, 
						BatteryConnector.class.getCanonicalName());
			}
			
			if(testType == TestType.INTEGRATION || testType == TestType.WIND_TURBINE) {
				this.doPortConnection(
						this.windTurbineOutboundPort.getPortURI(), 
						WindTurbine.INBOUND_PORT_URI, 
						WindTurbineConnector.class.getCanonicalName());
			}
			
			if(testType == TestType.INTEGRATION || testType == TestType.GENERATOR) {
				this.doPortConnection(
						this.generatorHEMOutboundPort.getPortURI(), 
						Generator.INBOUND_PORT_URI, 
						GeneratorHEMConnector.class.getCanonicalName());
			}
			
		} catch (Exception e) {
			throw new ComponentStartException(e) ;
		}
	}
	
	@Override
	public synchronized void execute() throws Exception {
		if(testType == TestType.INTEGRATION || testType == TestType.METER) 
			this.testMeter();
		
		if(testType == TestType.INTEGRATION || testType == TestType.BATTERY) 
			this.testBattery();
		
		if(testType == TestType.INTEGRATION || testType == TestType.WIND_TURBINE) 
			this.testWindTurbine();
		
		if(testType == TestType.INTEGRATION || testType == TestType.GENERATOR) 
			this.testGenerator();
	}
	
	@Override
	public synchronized void finalise() throws Exception
	{	
		if(testType == TestType.INTEGRATION || testType == TestType.METER) 
			this.doPortDisconnection(this.electricMeterPort.getPortURI());
		
		if(testType == TestType.INTEGRATION || testType == TestType.BATTERY) 
			this.doPortDisconnection(this.batteryOutboundPort.getPortURI());
		
		if(testType == TestType.INTEGRATION || testType == TestType.WIND_TURBINE) 
			this.doPortDisconnection(this.windTurbineOutboundPort.getPortURI());
		
		if(testType == TestType.INTEGRATION || testType == TestType.GENERATOR) 
			this.doPortDisconnection(this.generatorHEMOutboundPort.getPortURI());
		
		
		super.finalise();
	}

	/**
	 * @see fr.sorbonne_u.components.AbstractComponent#shutdown()
	 */
	@Override
	public synchronized void	shutdown() throws ComponentShutdownException
	{
		try {
			if(testType == TestType.INTEGRATION || testType == TestType.METER) 
				this.electricMeterPort.unpublishPort();
			
			if(testType == TestType.INTEGRATION || testType == TestType.BATTERY) 
				this.batteryOutboundPort.unpublishPort();
			
			if(testType == TestType.INTEGRATION || testType == TestType.WIND_TURBINE) 
				this.windTurbineOutboundPort.unpublishPort();
			
			if(testType == TestType.INTEGRATION || testType == TestType.GENERATOR) 
				this.generatorHEMOutboundPort.unpublishPort();
			
			if(testType == TestType.INTEGRATION || testType == TestType.FRIDGE || testType == TestType.SMART_LIGHTING) 
				this.registrationPort.unpublishPort();
				
		} catch (Exception e) {
			throw new ComponentShutdownException(e) ;
		}
		super.shutdown();
	}
	
	
	// -------------------------------------------------------------------------
	// Tests methods
	// -------------------------------------------------------------------------

	protected void	testMeter() throws Exception {
		this.traceMessage("testMeter()\n");
		
		this.traceMessage("Electric meter current consumption? " +
				this.electricMeterPort.getCurrentConsumption() + "\n");
		this.traceMessage("Electric meter current production? " +
				this.electricMeterPort.getCurrentProduction() + "\n");
		
		this.traceMessage("testMeter done...\n");
	}
	
	protected void testBattery() throws Exception {
		this.traceMessage("testBattery()\n");
		
		this.traceMessage("Battery mode? " +
							this.batteryOutboundPort.getState().toString() + "\n");
		
		this.batteryOutboundPort.setState(BatteryI.STATE.CONSUME);
		this.traceMessage("Battery mode? " +
				this.batteryOutboundPort.getState().toString() + "\n");
		
		this.batteryOutboundPort.setState(BatteryI.STATE.PRODUCT);
		this.traceMessage("Battery mode? " +
				this.batteryOutboundPort.getState().toString() + "\n");
		
		this.traceMessage("Battery charge level? " +
				this.batteryOutboundPort.getBatteryLevel() + "\n");
		this.traceMessage("testBattery() done...\n");
	}
	
	protected void testWindTurbine() throws Exception {
		this.traceMessage("testWindTurbine()\n");
		
		this.traceMessage("Wind turbine is activated? " +
							this.windTurbineOutboundPort.isActivate() + "\n");
		
		this.windTurbineOutboundPort.activate();
		this.traceMessage("Wind turbine starts turning? " +
							this.windTurbineOutboundPort.isActivate() + "\n");
		
		this.windTurbineOutboundPort.stop();
		this.traceMessage("Wind turbine stops turning? " +
							this.windTurbineOutboundPort.isActivate() + "\n");
		
		this.traceMessage("testWindTurbine() done...\n");
	}
	
	protected void testGenerator() throws Exception {
		this.traceMessage("testGenerator()\n");
		
		this.traceMessage("Generator is not running? " +
							this.generatorHEMOutboundPort.isRunning() + "\n");
		
		this.generatorHEMOutboundPort.activate();
		this.traceMessage("Generator is running? " +
							this.generatorHEMOutboundPort.isRunning() + "\n");
		
		this.generatorHEMOutboundPort.stop();
		this.traceMessage("Generator is not running? " +
							this.generatorHEMOutboundPort.isRunning() + "\n");
		
		this.traceMessage("testGenerator() done...\n");
	}
	
	public void testFridge(String fridgeURI) throws Exception {
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

	public void testSmartLighting(String SmartLightingURI) throws Exception {
		if(VERBOSE)
			this.traceMessage("\n\nStart scenario between the HEM and the smart lighting\n");

		assert this.registeredUriModularEquipement.containsKey(SmartLightingURI) :
				new PreconditionException("Impossible test the commmunication with the fridge because it's not connect to the HEM");

		AdjustableOutboundPort ao = this.registeredUriModularEquipement.get(SmartLightingURI);		
	
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
	

	// -------------------------------------------------------------------------
	// Registration methods
	// -------------------------------------------------------------------------
	
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
		
		if(uid == Fridge.URI) 
			this.testFridge(uid);

		if(uid == SmartLighting.URI)
			this.testSmartLighting(uid);

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
}
