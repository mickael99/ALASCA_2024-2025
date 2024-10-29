package fr.sorbonne_u.components.equipments.battery;

import fr.sorbonne_u.components.AbstractComponent;
import fr.sorbonne_u.components.exceptions.ComponentShutdownException;

public class Battery extends AbstractComponent implements BatteryI {

	public static final boolean VERBOSE = true;
	public static int X_RELATIVE_POSITION = 2;
	public static int Y_RELATIVE_POSITION = 1;
	
	protected static final STATE INIT_STATE = STATE.CONSUME;
	protected STATE currentState;
	
	public static final String INTERNAL_INBOUND_PORT = "INTERNAL_INBOUND_PORT";
	protected BatteryInboundPort inboundPort;
	
	
	public Battery() {
		super(1, 0);
		try {
			this.currentState = INIT_STATE;
			
			this.inboundPort = new BatteryInboundPort(INTERNAL_INBOUND_PORT, this);
			this.inboundPort.publishPort();
			
			if(VERBOSE) {
				this.tracer.get().setTitle("Battery component");
				this.tracer.get().setRelativePosition(X_RELATIVE_POSITION,
						Y_RELATIVE_POSITION);
				this.toggleTracing();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
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

	@Override
	public STATE getState() throws Exception {
		if(VERBOSE) 
			this.logMessage("Battery gets its current state -> " + this.currentState.toString() + "\n");
		
		return this.currentState;
	}

	@Override
	public void setState(STATE state) throws Exception {
		if(VERBOSE) 
			this.logMessage("Battery new state -> " + state.toString() + "\n");
		
		this.currentState = state;
	}
}
