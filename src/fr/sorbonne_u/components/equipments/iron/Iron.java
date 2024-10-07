package fr.sorbonne_u.components.equipments.iron;

import fr.sorbonne_u.components.AbstractComponent;
import fr.sorbonne_u.components.exceptions.ComponentShutdownException;
import fr.sorbonne_u.exceptions.PreconditionException;

public class Iron extends AbstractComponent implements IronImplementationI {
	
	// -------------------------------------------------------------------------
	// Constants and variables
	// -------------------------------------------------------------------------
	
	public static boolean VERBOSE = true;
	public static int X_RELATIVE_POSITION = 0;
	public static int Y_RELATIVE_POSITION = 1;
	
	protected IronState currentState;
	protected final IronState INITIAL_STATE = IronState.OFF;
	
	protected IronTemperature currentTemperature;
	protected final IronTemperature INITIAL_TEMPERATURE = IronTemperature.DELICATE;
	
	protected IronSteam currentSteam;
	protected final IronSteam INITIAL_STEAM = IronSteam.INACTIVE;
	
	protected IronEnergySavingMode currentEnergySavingMode;
	protected final IronEnergySavingMode INITIAL_ENERGY_SAVING_MODE = IronEnergySavingMode.INACTIVE;
	
	public static final String INBOUND_PORT_URI = "IRON-INBOUND-PORT";
	protected IronInboundPort inboundPort;
	
	
	// -------------------------------------------------------------------------
	// Constructors
	// -------------------------------------------------------------------------
	
	protected Iron() throws Exception {
		super(1, 0);
		this.initialise(INBOUND_PORT_URI);
	}
			
	protected Iron(String ironInboundPortURI) throws Exception {
		super(1, 0);
		this.initialise(ironInboundPortURI);
	}

	protected Iron(String ironInboundPortURI,String reflectionInboundPortURI) throws Exception {
		super(reflectionInboundPortURI, 1, 0);
		this.initialise(ironInboundPortURI);
	}
	
	protected void initialise(String inboundPortURI) throws Exception {
		assert inboundPortURI != null :
			new PreconditionException(
								"the iron inbound port uri is null, impossible to initialise");
		assert	!inboundPortURI.isEmpty() :
					new PreconditionException(
								"the iron inbound port uri is empty, impossible to initialise");
		
		this.currentState = INITIAL_STATE;
		this.currentTemperature = INITIAL_TEMPERATURE;
		this.currentSteam = INITIAL_STEAM;
		this.currentEnergySavingMode = INITIAL_ENERGY_SAVING_MODE;
		
		this.inboundPort = new IronInboundPort(inboundPortURI, this);
		this.inboundPort.publishPort();
				
		if (VERBOSE) {
			this.tracer.get().setTitle("Iron component");
			this.tracer.get().setRelativePosition(X_RELATIVE_POSITION,
												  Y_RELATIVE_POSITION);
			this.toggleTracing();
		}
	}
	
	
	// -------------------------------------------------------------------------
	// Component life-cycle
	// -------------------------------------------------------------------------
	
	@Override
	public synchronized void shutdown() throws ComponentShutdownException
	{
		try {
			this.inboundPort.unpublishPort();
		} catch (Exception e) {
			throw new ComponentShutdownException(e) ;
		}
		super.shutdown();
	}

	
	// -------------------------------------------------------------------------
	// Component services implementation
	// -------------------------------------------------------------------------
	
	@Override
	public IronState getState() throws Exception {
		if(VERBOSE)
			this.traceMessage("Iron returns its state : " + this.currentState.toString() + ".\n");
		
		return this.currentState;
	}

	@Override
	public void turnOn() throws Exception {
		if(VERBOSE)
			this.traceMessage("Iron is turning on.\n");
		
		this.currentState = IronState.ON;
	}

	@Override
	public void turnOff() throws Exception {
		if(VERBOSE)
			this.traceMessage("Iron is turning off.\n");
		
		this.currentState = IronState.OFF;
	}

	@Override
	public IronTemperature getTemperature() throws Exception {
		if(VERBOSE)
			this.traceMessage("Iron returns its temperature : " + this.currentTemperature.toString() + ".\n");
		
		return this.currentTemperature;
	}

	@Override
	public void setTemperature(IronTemperature t) throws Exception {
		if(VERBOSE)
			this.traceMessage("Trying setting the temperature.\n");
		
		assert this.currentState == IronState.OFF : 
			new PreconditionException("Impossible to set the iron temperature because it's turning off\n.");
		
		this.currentTemperature = t;
		
		if(VERBOSE)
			this.traceMessage("Iron gets a new temperature : " + this.currentTemperature.toString() + ".\n");
	}

	@Override
	public IronSteam getSteam() throws Exception {
		if(VERBOSE)
			this.traceMessage("Iron returns its steam mode : " + this.currentSteam.toString() + ".\n");
		
		return this.currentSteam;
	}

	@Override
	public void setSteam(IronSteam s) throws Exception {
		if(VERBOSE)
			this.traceMessage("Trying setting the steam mode.\n");
		
		assert this.currentState == IronState.OFF : 
			new PreconditionException("Impossible to set the iron steam mode because it's turning off\n.");
		
		this.currentSteam = s;
		
		if(VERBOSE)
			this.traceMessage("Iron gets a new steam mode : " + this.currentSteam.toString() + ".\n");
	}

	@Override
	public IronEnergySavingMode getEnergySavingMode() throws Exception {
		if(VERBOSE)
			this.traceMessage("Iron returns its energy saving mode : " + this.currentEnergySavingMode.toString() + ".\n");
		
		return this.currentEnergySavingMode;
	}

	@Override
	public void setEnergySavingMode(IronEnergySavingMode e) throws Exception {
		if(VERBOSE)
			this.traceMessage("Trying setting the energy saving mode.\n");
		
		assert this.currentState == IronState.OFF : 
			new PreconditionException("Impossible to set the iron energy saving mode because it's turning off\n.");
		
		this.currentEnergySavingMode = e;
		
		if(VERBOSE)
			this.traceMessage("Iron gets a new energu : " + this.currentEnergySavingMode.toString() + ".\n");
	}
}
