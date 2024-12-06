package fr.sorbonne_u.components.equipments.windTurbine;

import fr.sorbonne_u.components.AbstractComponent;
import fr.sorbonne_u.components.annotations.OfferedInterfaces;
import fr.sorbonne_u.components.exceptions.ComponentShutdownException;

@OfferedInterfaces(offered = {WindTurbineCI.class})
public class WindTurbine extends AbstractComponent implements WindTurbineI {
	
	public static final boolean VERBOSE = true;
	public static int X_RELATIVE_POSITION = 2;
	public static int Y_RELATIVE_POSITION = 1;
	
	public static final STATE INIT_STATE = STATE.STANDBY;
	protected STATE currentState;
	
	public static final String INBOUND_PORT_URI = "INBOUND_PORT_URI";
	protected WindTurbineInboundPort inboundPort;
	
	protected double currentProduction = 0.0;
	
	
	protected WindTurbine() throws Exception {
		super(1, 0);
		
		this.currentState = INIT_STATE;
		
		this.inboundPort = new WindTurbineInboundPort(INBOUND_PORT_URI, this);
		this.inboundPort.publishPort();
		
		if(VERBOSE) {
			this.tracer.get().setTitle("Wind turbine component");
			this.tracer.get().setRelativePosition(X_RELATIVE_POSITION,
					Y_RELATIVE_POSITION);
			this.toggleTracing();
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
	public boolean isActivate() throws Exception {
		if(VERBOSE)
			this.traceMessage("Wind turbine says if it's activate or not -> " + this.currentState.toString() + "\n");
		
		return this.currentState == STATE.ACTIVE;
	}


	@Override
	public void stop() throws Exception {
		if(VERBOSE)
			this.traceMessage("Stop the wind turbine \n");
		
		this.currentState = STATE.STANDBY;
	}
	
	@Override
	public void activate() throws Exception {
		if(VERBOSE)
			this.traceMessage("start the wind turbine \n");
		
		this.currentState = STATE.ACTIVE;
	}


	@Override
	public double getCurrentProduction() throws Exception {
		if(VERBOSE)
			this.traceMessage("The wind turbine returns its current production -> " + this.currentProduction + "\n");
		
		return this.currentProduction;
	}
}
