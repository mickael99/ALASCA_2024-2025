package fr.sorbonne_u.components.equipments.toaster;

import fr.sorbonne_u.components.AbstractComponent;
import fr.sorbonne_u.components.annotations.OfferedInterfaces;
import fr.sorbonne_u.components.exceptions.ComponentShutdownException;
import fr.sorbonne_u.exceptions.PreconditionException;

@OfferedInterfaces(offered = {ToasterUserCI.class})
public class Toaster extends AbstractComponent implements ToasterImplementationI {

	// -------------------------------------------------------------------------
	// Constants and variables
	// -------------------------------------------------------------------------
	
	public static boolean VERBOSE = true;
	public static int X_RELATIVE_POSITION = 0;
	public static int Y_RELATIVE_POSITION = 2;
	
	protected ToasterState currentState;
	protected ToasterBrowningLevel currentBrowningLevel;
	protected int sliceCount = 0;
	
	protected final ToasterState INITIAL_STATE = ToasterState.OFF;
	protected final ToasterBrowningLevel  INITIAL_BROWNING_LEVEL = ToasterBrowningLevel.LOW;
	protected final int MAX_SLICE_COUNT = 3;
	
	public static final String INBOUND_PORT_URI = "TOASTER-INBOUND-PORT";
	protected ToasterInboundPort inboundPort;
	
	
	// -------------------------------------------------------------------------
	// Constructors
	// -------------------------------------------------------------------------
	
	protected Toaster() throws Exception {
		super(1, 0);
		this.initialise(INBOUND_PORT_URI);
	}
			
	protected Toaster(String ironInboundPortURI) throws Exception {
		super(1, 0);
		this.initialise(ironInboundPortURI);
	}

	protected Toaster(String ironInboundPortURI,String reflectionInboundPortURI) throws Exception {
		super(reflectionInboundPortURI, 1, 0);
		this.initialise(ironInboundPortURI);
	}
	
	protected void initialise(String inboundPortURI) throws Exception {
		assert inboundPortURI != null :
			new PreconditionException(
								"the toaster inbound port uri is null, impossible to initialise");
		assert	!inboundPortURI.isEmpty() :
					new PreconditionException(
								"the toaster inbound port uri is empty, impossible to initialise");
		
		this.currentState = INITIAL_STATE;
		this.currentBrowningLevel = INITIAL_BROWNING_LEVEL;
		
		this.inboundPort = new ToasterInboundPort(inboundPortURI, this);
		this.inboundPort.publishPort();
		
		this.sliceCount = 0;
		
		if (VERBOSE) {
			this.tracer.get().setTitle("Toaster component");
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
	public ToasterState getState() throws Exception {
		if (VERBOSE) {
			this.traceMessage("Toaster returns its state : " +
													this.currentState + ".\n");
		}
		
		return this.currentState;
	}

	@Override
	public ToasterBrowningLevel getBrowningLevel() throws Exception {
		if (VERBOSE) 
			this.traceMessage("Toaster returns its browning level : " + this.currentBrowningLevel + ".\n");
		
		return currentBrowningLevel;
	}
	
	@Override
	public int getSliceCount() throws Exception {
		if (VERBOSE)
		 this.traceMessage("Toaster get " + this.sliceCount + "slice of bread(s).\n");

		return this.sliceCount;
	}

	@Override
	public void turnOn() throws Exception {
		if (VERBOSE) 
			this.traceMessage("Trying to turn on the toaster");

		assert this.sliceCount > 0 && sliceCount <= this.MAX_SLICE_COUNT:
			new PreconditionException("Impossible to turn on the toaster because the slice count must be 1, 2 or 3: " + this.sliceCount + "\n.");
		
		this.currentState = ToasterState.ON;
		
		if (VERBOSE) 
			this.traceMessage("Toaster is turning on \n.");
	}

	@Override
	public void turnOff() throws Exception {
		this.currentState = ToasterState.OFF;
		if (VERBOSE) 
			this.traceMessage("Toaster is turning off \n.");
	}

	@Override
	public void setSliceCount(int sliceCount) throws Exception {
		if (VERBOSE)
			this.traceMessage("Trying to set slice count in Toaster \n.");
		
		assert this.currentState == ToasterState.OFF :
			new PreconditionException("Toaster must be turn off for setting slice count \n.");
		assert sliceCount <= 3 && sliceCount >= 0 :
			new PreconditionException("Toaster can contain between 0 et 3 slice of bread \n.");
		
		this.sliceCount = sliceCount;
		
		if (VERBOSE)
			this.traceMessage("The toaster get " + this.sliceCount + " slice of bread \n.");
	}

	@Override
	public void setBrowningLevel(ToasterBrowningLevel bl) throws Exception {
		if (VERBOSE) 
			this.traceMessage("Trying to set toaster browning level \n.");
		
		assert this.currentState == ToasterState.OFF :
			new PreconditionException("Toaster must be turn off for setting browning level \n."); 
		
		this.currentBrowningLevel = bl;
		
		if (VERBOSE)
			this.traceMessage("The toaster is setting on " + this.currentBrowningLevel + "\n.");
	}

}
