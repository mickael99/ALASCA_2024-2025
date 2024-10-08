package fr.sorbonne_u.components.equipments.fridge;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import fr.sorbonne_u.components.AbstractComponent;
import fr.sorbonne_u.components.equipments.fridge.connections.FridgeExternalControlInboundPort;
import fr.sorbonne_u.components.equipments.fridge.connections.FridgeInternalControlInboundPort;
import fr.sorbonne_u.components.equipments.fridge.connections.FridgeUserInboundPort;
import fr.sorbonne_u.components.equipments.fridge.interfaces.FridgeInternalControlI;
import fr.sorbonne_u.components.equipments.fridge.interfaces.FridgeUserI;
import fr.sorbonne_u.components.equipments.hem.HEM;
import fr.sorbonne_u.components.equipments.hem.registration.RegistrationConnector;
import fr.sorbonne_u.components.equipments.hem.registration.RegistrationOutboundPort;
import fr.sorbonne_u.components.exceptions.ComponentShutdownException;
import fr.sorbonne_u.components.exceptions.ComponentStartException;
import fr.sorbonne_u.exceptions.PreconditionException;

public class Fridge extends AbstractComponent implements FridgeInternalControlI, FridgeUserI {

	// -------------------------------------------------------------------------
	// Inner interfaces and types
	// -------------------------------------------------------------------------
	
	public static enum FridgeState {
		ON,
		COOLING,
		OFF
	}
	
	
	// -------------------------------------------------------------------------
	// Constants and variables
	// -------------------------------------------------------------------------
	
	protected static final String URI = "FRIDGE-URI"; 
	
	// Tracing
	public static final boolean VERBOSE = true;
	public static int X_RELATIVE_POSITION = 0;			
	public static int Y_RELATIVE_POSITION = 0;
	
	// State
	protected FridgeState currentState;
	
	// Power level
	protected static final double MAX_COOLING_POWER = 500.0;
	protected double currentCoolingPower;
	
	// Temperature
	protected static final double MIN_TEMPERATURE = 0.0;
	protected static final double MAX_TEMPERATURE = 8.0;
	protected static final double FAKE_CURRENT_TEMPERATURE = 6.0;
	protected static final double STANDARD_TARGET_TEMPERATURE = 4.0;
	protected double targetTemperature;
	
	// Alarm
	protected boolean isDoorOpen;
	protected boolean alarmTriggered;
	
	// Connections
	public static final String USER_INBOUND_PORT_URI = "FRIDGE-USER-INBOUND-PORT-URI";
	public static final String INTERNAL_CONTROL_INBOUND_PORT_URI = "FRIDGE-INTERNAL-CONTROL-INBOUND-PORT-URI";
	public static final String EXTERNAL_CONTROL_INBOUND_PORT_URI = "FRIDGE-EXTERNAL-CONTROL-INBOUND-PORT-URI";

	protected FridgeUserInboundPort userInbound;
	protected FridgeInternalControlInboundPort internalInbound;
	protected FridgeExternalControlInboundPort externalInbound;
	
	// Registration
	public static boolean TEST_REGISTRATION = false;
	protected RegistrationOutboundPort registrationPort;
	protected boolean isHEMConnectionRequired;
	protected static final String XML_PATH = "fridge-descriptor.xml";
	
	
	// -------------------------------------------------------------------------
	// Constructors
	// -------------------------------------------------------------------------
	
	protected Fridge(boolean isHEMConnectionRequired) throws Exception {
		this(USER_INBOUND_PORT_URI, INTERNAL_CONTROL_INBOUND_PORT_URI, EXTERNAL_CONTROL_INBOUND_PORT_URI, isHEMConnectionRequired);
	}
	
	protected Fridge(String userInboundURI, String internalInboundURI, String externalInboundURI, boolean isHEMConnectionRequired) throws Exception {
		super(2, 0);
		this.initialise(userInboundURI, internalInboundURI, externalInboundURI, isHEMConnectionRequired);
	}
	
	protected Fridge(String reflectionInboundPortURI, String userInboundURI, String internalInboundURI, String externalInboundURI, boolean isHEMConnectionRequired) throws Exception {
		super(reflectionInboundPortURI, 1, 0);
		this.initialise(userInboundURI, internalInboundURI, externalInboundURI, isHEMConnectionRequired);
	}
	
	protected void initialise(String userInboundURI, String internalInboundURI, String externalInboundURI, boolean isHEMConnectionRequired) throws Exception {
		assert userInboundURI != null && !userInboundURI.isEmpty();
		assert internalInboundURI != null && !internalInboundURI.isEmpty();
		assert externalInboundURI != null && !externalInboundURI.isEmpty();
		
		// Variables
		this.currentState = FridgeState.OFF;
		this.currentCoolingPower = MAX_COOLING_POWER;
		this.targetTemperature = STANDARD_TARGET_TEMPERATURE;
		
		this.isDoorOpen = false;
		this.alarmTriggered = false;
		
		// Connections
		this.userInbound = new FridgeUserInboundPort(userInboundURI, this);
		this.userInbound.publishPort();
		
		this.internalInbound = new FridgeInternalControlInboundPort(internalInboundURI, this);
		this.internalInbound.publishPort();
		
		this.externalInbound = new FridgeExternalControlInboundPort(externalInboundURI, this);
		this.externalInbound.publishPort();
		
		// Registration
		this.isHEMConnectionRequired = isHEMConnectionRequired;
		
		if(this.isHEMConnectionRequired) {
			this.registrationPort = new RegistrationOutboundPort(this);
			this.registrationPort.publishPort();
		}
		
		// Tracing
		if (VERBOSE) {
			this.tracer.get().setTitle("Fridge component");
			this.tracer.get().setRelativePosition(X_RELATIVE_POSITION,
												  Y_RELATIVE_POSITION);
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
			// Registration
			if(this.isHEMConnectionRequired) {
				this.doPortConnection(
						this.registrationPort.getPortURI(),
						HEM.URI_REGISTRATION_INBOUND_PORT,
						RegistrationConnector.class.getCanonicalName());
			}
		} catch (Exception e) {
			throw new ComponentStartException(e) ;
		}
	}
	
	@Override
	public synchronized void execute() throws Exception {
		if (this.isHEMConnectionRequired && TEST_REGISTRATION) 
			this.runAllRegistrationTest();
	}
	
	@Override
	public synchronized void shutdown() throws ComponentShutdownException
	{
		try {
			this.userInbound.unpublishPort();
			this.internalInbound.unpublishPort();
			this.externalInbound.unpublishPort();
			
			// Registration
			if(this.isHEMConnectionRequired)
				this.registrationPort.unpublishPort();
			
		} catch (Exception e) {
			throw new ComponentShutdownException(e) ;
		}
		super.shutdown();
	}
	
	@Override
	public synchronized void finalise() throws Exception {
		// Registration
		if(this.isHEMConnectionRequired)
			this.doPortDisconnection(this.registrationPort.getPortURI());
		
		super.finalise();
	}
	
	
	// -------------------------------------------------------------------------
	// Component services implementation
	// -------------------------------------------------------------------------
	
	@Override
	public double getTargetTemperature() throws Exception { 
		if (VERBOSE)
			this.traceMessage("Fridge returns its target temperature -> " + this.targetTemperature + "\n.");
		
		return this.targetTemperature;
	}

	@Override
	public double getCurrentTemperature() throws Exception { 
		if (VERBOSE)
			this.traceMessage("Fridge returns its current temperature -> " + FAKE_CURRENT_TEMPERATURE + "\n.");
		
		return FAKE_CURRENT_TEMPERATURE;
	}

	@Override
	public void close() throws Exception { 
		this.isDoorOpen = false;
		
		if (VERBOSE)
			this.traceMessage("The door of the fridge is close \n.");
	}

	@Override
	public double getMaxCoolingPower() throws Exception { 
		if (VERBOSE)
			this.traceMessage("Fridge returns its max cooling power -> " + MAX_COOLING_POWER + "\n.");
		
		return MAX_COOLING_POWER;
	}

	@Override
	public double getCurrentCoolingPower() throws Exception { 
		if (VERBOSE)
			this.traceMessage("Fridge returns its current cooling power -> " + this.currentCoolingPower + "\n.");
		
		return this.currentCoolingPower;
	}

	@Override
	public void setCurrentCoolingPower(double power) throws Exception { 
		if (VERBOSE)
			this.traceMessage("Trying to set current cooling power -> " + power + "\n.");
				
		assert this.currentState != FridgeState.OFF :
			new PreconditionException("Impossible to set current cooling power because the fridge is off");
		assert power > 0 && power <= MAX_COOLING_POWER :
			new PreconditionException("The cooling power is not between 0 and " + MAX_COOLING_POWER + " -> " + power);

		this.currentCoolingPower = power;
		
		if (VERBOSE)
			this.traceMessage("Current cooling power is changing -> " + this.currentCoolingPower + "\n.");
	}

	@Override
	public boolean isAlarmTriggered() throws Exception { 
		if (VERBOSE) {
			if (this.alarmTriggered)
				this.traceMessage("Alarm is triggered \n.");
			else
				this.traceMessage("Alarm is not triggered \n.");
		}
		
		return this.alarmTriggered;
	}

	@Override
	public void setTargetTemperature(double temperature) throws Exception { 
		if (VERBOSE)
			this.traceMessage("Trying to set target temperature -> " + temperature + "\n.");
		
		assert this.currentState != FridgeState.OFF :
			new PreconditionException("Impossible to set target temperature because the fridge is off");
		assert temperature >= MIN_TEMPERATURE && temperature < FAKE_CURRENT_TEMPERATURE :
			new PreconditionException("The target temperature is not between " + MIN_TEMPERATURE + " and " + FAKE_CURRENT_TEMPERATURE + " -> " + temperature);
		
		this.targetTemperature = temperature;
		
		if (VERBOSE)
			this.traceMessage("Target temperature is changing -> " + this.targetTemperature + "\n.");
	}

	@Override
	public boolean isOpen() throws Exception { 
		if (VERBOSE) {
			if (this.isDoorOpen)
				this.traceMessage("The door is open \n.");
			else
				this.traceMessage("The door is close \n.");
		}
		
		return this.isDoorOpen;
	}

	@Override
	public FridgeState getState() throws Exception { 
		if (VERBOSE) 
			this.traceMessage("Fridge state -> " + this.currentState.toString() + "\n.");
		
		return this.currentState;
	}

	@Override
	public void switchOn() throws Exception { 
		this.currentState = FridgeState.ON;
		
		if (VERBOSE) 
			this.traceMessage("Fridge is turning on \n.");
	}

	@Override
	public void switchOff() throws Exception { 
		this.currentState = FridgeState.OFF;
		
		if (VERBOSE) 
			this.traceMessage("Fridge is turning off \n.");
	}

	@Override
	public void open() throws Exception { 
		this.isDoorOpen = true;
				
		if (VERBOSE) 
			this.traceMessage("Fridge door is open \n.");
	}

	@Override
	public boolean isCooling() throws Exception {	
		if (VERBOSE) {
			if(this.currentState == FridgeState.COOLING)
				this.traceMessage("Fridge is cooling \n.");
			else
				this.traceMessage("Fridge is not coolong \n.");
		}
			
		if(this.currentState == FridgeState.COOLING)
			return true;
		return false;
	}

	@Override
	public void startCooling() throws Exception { 
		if(VERBOSE)
			this.traceMessage("Trying to start cooling \n.");
		
		assert this.currentState != FridgeState.OFF :
			new PreconditionException("Impossible to start cooling because the fridge is off");
		
		this.currentState = FridgeState.COOLING;
		
		if(VERBOSE)
			this.traceMessage("The fridge is cooling \n.");
	}

	@Override
	public void stopCooling() throws Exception { 
		if(VERBOSE)
			this.traceMessage("Trying to stop cooling \n.");
		
		assert this.currentState != FridgeState.OFF :
			new PreconditionException("Impossible to stop cooling because the fridge is off");
		
		this.currentState = FridgeState.ON;
		
		if(VERBOSE)
			this.traceMessage("The fridge stop cooling \n.");
	}

	@Override
	public void triggeredAlarm() throws Exception { 
		if(VERBOSE)
			this.traceMessage("Trying to trigger alarm \n.");
		
		assert this.currentState != FridgeState.OFF :
			new PreconditionException("Impossible to trigger alarm because the fridge is off");
		assert this.isDoorOpen :
			new PreconditionException("Impossible to trigger alarm because the door is not open");
		
		this.alarmTriggered = true;
		
		if(VERBOSE)
			this.traceMessage("The alarm is triggering \n.");
	}

	@Override
	public void stopAlarm() throws Exception { 
		if(VERBOSE)
			this.traceMessage("Trying to stop alarm \n.");
	
		assert this.currentState != FridgeState.OFF :
			new PreconditionException("Impossible to stop alarm because the fridge is off");
		assert !this.isDoorOpen :
			new PreconditionException("Impossible to stop alarm because the door is open");
		
		this.alarmTriggered = false;
		
		if(VERBOSE)
			this.traceMessage("The alarm is stop \n.");
	}
	
	
	// -------------------------------------------------------------------------
	// Component Tests Registration
	// -------------------------------------------------------------------------
	
	protected void testRegistered() {
		if(VERBOSE)
			this.traceMessage("Test registered\n");
		try {
			assertFalse(this.registrationPort.registered(URI));
		}
		catch(Exception e) {
			this.traceMessage("...KO.\n" + e);
			assertTrue(false);
		}
		if(VERBOSE)
			this.traceMessage("Done...\n");
	}
	
	protected void testRegister() {
		if(VERBOSE)
			this.traceMessage("Test register\n");
		try {
			assertTrue(this.registrationPort.register(URI, this.externalInbound.getPortURI(), XML_PATH));
			assertTrue(this.registrationPort.registered(URI));
		}
		catch(Exception e) {
			this.traceMessage("...KO.\n" + e);
			assertTrue(false);
		}
		if(VERBOSE)
			this.traceMessage("Done...\n");
	}
	
	protected void testUnregister() {
		if(VERBOSE)
			this.traceMessage("Test unregister\n");
		try {
			this.registrationPort.unregister(URI);
			assertFalse(this.registrationPort.registered(URI));
		}
		catch(Exception e) {
			this.traceMessage("...KO.\n" + e);
			assertTrue(false);
		}
		if(VERBOSE)
			this.traceMessage("Done...\n");
	}
	
	protected void runAllRegistrationTest() throws Exception {
		// Switch on to make the tests
		this.switchOn();
		
		this.testRegistered();
		this.testRegister();
		this.testUnregister();
	}
}
