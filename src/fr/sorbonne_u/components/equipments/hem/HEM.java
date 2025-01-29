package fr.sorbonne_u.components.equipments.hem;

import fr.sorbonne_u.components.cyphy.utils.aclocks.ClocksServerWithSimulationCI;
import java.time.Instant;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;
import fr.sorbonne_u.components.AbstractComponent;
import fr.sorbonne_u.components.CVMIntegrationTest;
import fr.sorbonne_u.components.annotations.RequiredInterfaces;
import fr.sorbonne_u.components.cyphy.utils.aclocks.AcceleratedAndSimulationClock;
import fr.sorbonne_u.components.cyphy.utils.aclocks.ClocksServerWithSimulationConnector;
import fr.sorbonne_u.components.cyphy.utils.aclocks.ClocksServerWithSimulationOutboundPort;
import fr.sorbonne_u.components.equipments.fridge.Fridge;
import fr.sorbonne_u.components.equipments.hem.adjustable.AdjustableCI;
import fr.sorbonne_u.components.equipments.hem.adjustable.AdjustableOutboundPort;
import fr.sorbonne_u.components.equipments.hem.adjustable.FridgeConnector;
import fr.sorbonne_u.components.equipments.hem.registration.RegistrationI;
import fr.sorbonne_u.components.equipments.hem.registration.RegistrationInboundPort;
import fr.sorbonne_u.components.equipments.meter.ElectricMeter;
import fr.sorbonne_u.components.equipments.meter.connections.ElectricMeterConnector;
import fr.sorbonne_u.components.equipments.meter.connections.ElectricMeterOutboundPort;
import fr.sorbonne_u.components.equipments.windTurbine.WindTurbineCI;
import fr.sorbonne_u.components.exceptions.ComponentShutdownException;
import fr.sorbonne_u.components.exceptions.ComponentStartException;
import fr.sorbonne_u.components.utils.SimulationType;
import fr.sorbonne_u.components.xmlReader.ClassCreator;
import fr.sorbonne_u.utils.aclocks.AcceleratedClock;
import fr.sorbonne_u.utils.aclocks.ClocksServer;
import fr.sorbonne_u.components.equipments.meter.interfaces.ElectricMeterCI;
import fr.sorbonne_u.components.equipments.battery.BatteryCI;
import fr.sorbonne_u.components.equipments.generator.interfaces.GeneratorHEMCI;

//@OfferedInterfaces(offered = {RegistrationCI.class})
@RequiredInterfaces(required = {AdjustableCI.class, 
								ElectricMeterCI.class, 
								BatteryCI.class, 
								WindTurbineCI.class, 
								GeneratorHEMCI.class,
								ClocksServerWithSimulationCI.class})
public class HEM extends AbstractComponent implements RegistrationI {

	// -------------------------------------------------------------------------
	// Constants and variables
	// -------------------------------------------------------------------------

	public static boolean VERBOSE = true;
	public static int X_RELATIVE_POSITION = 0;
	public static int Y_RELATIVE_POSITION = 0;

	// Registration ports
	public static final String URI_REGISTRATION_INBOUND_PORT = "URI_REGISTRATION_INBOUND_PORT";
	protected RegistrationInboundPort registrationPort;
	protected HashMap<String, AdjustableOutboundPort> registeredUriModularEquipement;
	
	// Components ports
	protected ElectricMeterOutboundPort electricMeterOutboundPort;
	protected AdjustableOutboundPort controlFridgeOutboundPort;
	//protected AdjustableOutboundPort controlSmartLightingPort;
	//protected BatteryOutboundPort batteryOutboundPort;
	//protected WindTurbineOutboundPort windTurbineOutboundPort;
	//protected GeneratorHEMOutboundPort generatorHEMOutboundPort;
	
	
	// Execution/Simulation
	protected final SimulationType currentSimulationType;
	protected final long PERIOD_IN_SECONDS = 60L;
	
	
	// -------------------------------------------------------------------------
	// Constructors
	// -------------------------------------------------------------------------
	
	protected HEM(SimulationType currentSimulationType) {
		super(1, 1);

		this.currentSimulationType = currentSimulationType;

		if (VERBOSE) {
			this.tracer.get().setTitle("Home Energy Manager component");
			this.tracer.get().setRelativePosition(X_RELATIVE_POSITION,
												  Y_RELATIVE_POSITION);
			this.toggleTracing();
		}
	}
	
	
	// -------------------------------------------------------------------------
	// Component internal methods
	// -------------------------------------------------------------------------

	protected void loop(Instant current, Instant end, AcceleratedClock ac) {
		long delayInNanos = ac.nanoDelayUntilInstant(current);
		Instant next = current.plusSeconds(PERIOD_IN_SECONDS);
		if (next.compareTo(end) < 0) {
			this.scheduleTask(
				o -> {
					try	{
						o.traceMessage(
								"Electric meter current consumption: " +
								/*electricMeterOutboundPort.getCurrentConsumption()*/ "Not implemented yet\n");
						o.traceMessage(
								"Electric meter current production: " +
								electricMeterOutboundPort.getCurrentProduction() + "Not implemented yet\n");
						loop(next, end, ac);
					} catch(Exception e) {
						e.printStackTrace();
					}
				}, delayInNanos, TimeUnit.NANOSECONDS);
		}
	}
	
//	protected HEM(TestType testType) throws Exception {
//		super(1, 0);
//		
//		this.testType = testType;
//		this.initialisePorts();
//		
//		this.registeredUriModularEquipement = new HashMap<String, AdjustableOutboundPort>();
//		
//		if (VERBOSE) {
//			this.tracer.get().setTitle("Home Energy Manager component");
//			this.tracer.get().setRelativePosition(X_RELATIVE_POSITION,
//												  Y_RELATIVE_POSITION);
//			this.toggleTracing();
//		}
//	}
	
	
//	protected void initialisePorts() throws Exception {
//		if(testType == TestType.INTEGRATION || testType == TestType.METER) {
//			this.electricMeterPort = new ElectricMeterOutboundPort(this);
//			this.electricMeterPort.publishPort();
//		}
//		
//		if(testType == TestType.INTEGRATION || testType == TestType.BATTERY) {
//			this.batteryOutboundPort = new BatteryOutboundPort(this);
//			this.batteryOutboundPort.publishPort();
//		}
//		
//		if(testType == TestType.INTEGRATION || testType == TestType.WIND_TURBINE) {
//			this.windTurbineOutboundPort = new WindTurbineOutboundPort(this);
//			this.windTurbineOutboundPort.publishPort();
//		}
//		
//		if(testType == TestType.INTEGRATION || testType == TestType.GENERATOR) {
//			this.generatorHEMOutboundPort = new GeneratorHEMOutboundPort(this);
//			this.generatorHEMOutboundPort.publishPort();
//		}
//		
//		if(testType == TestType.INTEGRATION || testType == TestType.FRIDGE || testType == TestType.SMART_LIGHTING) {
//			this.registrationPort = new RegistrationInboundPort(URI_REGISTRATION_INBOUND_PORT, this);
//			this.registrationPort.publishPort();
//		}
//	}
	
	// -------------------------------------------------------------------------
	// Component life-cycle
	// -------------------------------------------------------------------------
	
	@Override
	public synchronized void start() throws ComponentStartException {
		super.start();

		try {
//			this.electricMeterOutboundPort = new ElectricMeterOutboundPort(this);
//			this.electricMeterOutboundPort.publishPort();
//			this.doPortConnection(
//					this.electricMeterOutboundPort.getPortURI(),
//					ElectricMeter.ELECTRIC_METER_INBOUND_PORT_URI,
//					ElectricMeterConnector.class.getCanonicalName());

			this.controlFridgeOutboundPort = new AdjustableOutboundPort(this);
			this.controlFridgeOutboundPort.publishPort();
			this.doPortConnection(
					this.controlFridgeOutboundPort.getPortURI(),
					Fridge.EXTERNAL_CONTROL_INBOUND_PORT_URI,
					FridgeConnector.class.getCanonicalName());
		} catch (Exception e) {
			throw new ComponentStartException(e) ;
		}
	}
	
	@Override
	public synchronized void	execute() throws Exception
	{
		AcceleratedAndSimulationClock ac = null;
		ClocksServerWithSimulationOutboundPort clocksServerOutboundPort =
							new ClocksServerWithSimulationOutboundPort(this);
		clocksServerOutboundPort.publishPort();
		this.doPortConnection(
					clocksServerOutboundPort.getPortURI(),
					ClocksServer.STANDARD_INBOUNDPORT_URI,
					ClocksServerWithSimulationConnector.class.getCanonicalName());
		this.logMessage("HEM gets the clock.");
		ac = clocksServerOutboundPort.getClockWithSimulation(
												CVMIntegrationTest.CLOCK_URI);
		this.doPortDisconnection(clocksServerOutboundPort.getPortURI());
		clocksServerOutboundPort.unpublishPort();
		clocksServerOutboundPort.destroyPort();
		this.logMessage("HEM waits until start time.");
		ac.waitUntilStart();
		this.logMessage("HEM starts.");

		if (this.currentSimulationType.isMilSimulation() ||
							this.currentSimulationType.isMILRTSimulation()) {
			this.logMessage("HEM has no MIL or MIL RT simulator yet.");
		} else if (this.currentSimulationType.isSILSimulation()) {
			Instant first = ac.getSimulationStartInstant().plusSeconds(600L);
			Instant end = ac.getSimulationEndInstant().minusSeconds(600L);
			this.logMessage("HEM schedules the SIL integration test.");
			this.loop(first, end, ac);
		} else {
			Instant meterTest = ac.getStartInstant().plusSeconds(60L);
			long delay = ac.nanoDelayUntilInstant(meterTest);
			this.logMessage("HEM schedules the meter integration test in "
										+ delay + " " + TimeUnit.NANOSECONDS);
			this.scheduleTaskOnComponent(
				new AbstractComponent.AbstractTask() {
					@Override
					public void run() {
						try {
							traceMessage(
									"Electric meter current consumption: " +
									/*electricMeterOutboundPort.getCurrentConsumption()*/ "Not implemented yet\n");
							traceMessage(
									"Electric meter current production: " +
									/*electricMeterOutboundPort.getCurrentProduction()*/ "Not implemented yet\n");
							traceMessage("HEM meter test ends.\n");
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}, delay, TimeUnit.NANOSECONDS);

			Instant fridge1 = ac.getStartInstant().plusSeconds(30L);
			delay = ac.nanoDelayUntilInstant(fridge1);
			this.logMessage("HEM schedules the heater first call in "
										+ delay + " " + TimeUnit.NANOSECONDS);
			this.scheduleTaskOnComponent(
					new AbstractComponent.AbstractTask() {
						@Override
						public void run() {
							try {
								traceMessage("HEM fridge first call begins.\n");
								traceMessage("Fridge maxMode index? " +
												controlFridgeOutboundPort.maxMode() + "\n");
								traceMessage("Fridge current mode index? " +
												controlFridgeOutboundPort.currentMode() + "\n");
								traceMessage("Fridge going down one mode? " +
												controlFridgeOutboundPort.downMode() + "\n");
								traceMessage("Fridge current mode is? " +
												controlFridgeOutboundPort.currentMode() + "\n");
								traceMessage("Fridge going up one mode? " +
												controlFridgeOutboundPort.upMode() + "\n");
								traceMessage("Fridge current mode is? " +
												controlFridgeOutboundPort.currentMode() + "\n");
								traceMessage("Fridge setting current mode? " +
												controlFridgeOutboundPort.setMode(2) + "\n");
								traceMessage("Fridge current mode is? " +
												controlFridgeOutboundPort.currentMode() + "\n");
								traceMessage("Fridge is suspended? " +
												controlFridgeOutboundPort.suspended() + "\n");
								traceMessage("HEM fridge first call ends.\n");
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
					}, delay, TimeUnit.NANOSECONDS);

			Instant fridge2 = ac.getStartInstant().plusSeconds(120L);
			delay = ac.nanoDelayUntilInstant(fridge2);
			this.logMessage("HEM schedules the fridge second call in "
										+ delay + " " + TimeUnit.NANOSECONDS);
			this.scheduleTaskOnComponent(
					new AbstractComponent.AbstractTask() {
						@Override
						public void run() {
							try {
								traceMessage("HEM fridge second call begins.\n");
								traceMessage("Fridge suspends? " +
												controlFridgeOutboundPort.suspend() + "\n");
								traceMessage("Fridge is suspended? " +
												controlFridgeOutboundPort.suspended() + "\n");
								traceMessage("Fridge emergency? " +
												controlFridgeOutboundPort.emergency() + "\n");
								traceMessage("HEM fridge second call ends.\n");
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
					}, delay, TimeUnit.NANOSECONDS);

			Instant fridge3 = ac.getStartInstant().plusSeconds(240L);
			delay = ac.nanoDelayUntilInstant(fridge3);
			this.logMessage("HEM schedules the fridge third call in "
										+ delay + " " + TimeUnit.NANOSECONDS);
			this.scheduleTaskOnComponent(
					new AbstractComponent.AbstractTask() {
						@Override
						public void run() {
							try {
								traceMessage("HEM fridge third call begins.\n");
								traceMessage("Fridge emergency? " +
												controlFridgeOutboundPort.emergency() + "\n");
								traceMessage("Fridge resumes? " +
												controlFridgeOutboundPort.resume() + "\n");
								traceMessage("Fridge is suspended? " +
												controlFridgeOutboundPort.suspended() + "\n");
								traceMessage("Fridge current mode is? " +
												controlFridgeOutboundPort.currentMode() + "\n");
								traceMessage("HEM fridge third call ends.\n");
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
					}, delay, TimeUnit.NANOSECONDS);
		}
	}
	
	@Override
	public synchronized void finalise() throws Exception {
		this.logMessage("HEM ends.");
//		this.doPortDisconnection(this.electricMeterOutboundPort.getPortURI());
		this.doPortDisconnection(this.controlFridgeOutboundPort.getPortURI());
		
		super.finalise();
	}

	@Override
	public synchronized void shutdown() throws ComponentShutdownException {
		try {
//			this.electricMeterOutboundPort.unpublishPort();
			this.controlFridgeOutboundPort.unpublishPort();
		} catch (Exception e) {
			throw new ComponentShutdownException(e) ;
		}
		super.shutdown();
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
