package fr.sorbonne_u.components.equipments.generator;

import fr.sorbonne_u.components.AbstractComponent;
import fr.sorbonne_u.components.annotations.OfferedInterfaces;
import fr.sorbonne_u.components.exceptions.ComponentShutdownException;
import fr.sorbonne_u.exceptions.PreconditionException;

@OfferedInterfaces(offered = {GeneratorCI.class})
public class Generator extends AbstractComponent implements GeneratorImplementationI {

	public static boolean VERBOSE = true;
	public static int X_RELATIVE_POSITION = 0;
	public static int Y_RELATIVE_POSITION = 4;
	
	public static final String INBOUND_PORT_URI = "GENERATOR-INBOUND-PORT-URI";
	protected GeneratorInboundPort inboundPort;
	
	protected boolean isStart;
	
	protected static final double MAX_CAPACITY = 200.0;
	protected double fuelLevel = 0.0;
	
	protected static final double DEFAULT_PRODUCTION = 100.0;
	protected double currentProduction = 0.0;
	
	
	protected Generator() throws Exception {
		super(1, 0);
		
		this.isStart = false;
		
		this.inboundPort = new GeneratorInboundPort(INBOUND_PORT_URI, this);
		this.inboundPort.publishPort();
		
		if(VERBOSE) {
			this.tracer.get().setTitle("Generator component");
			this.tracer.get().setRelativePosition(X_RELATIVE_POSITION,
					Y_RELATIVE_POSITION);
			this.toggleTracing();
		}
	}
	
	
	@Override
	public synchronized void shutdown() throws ComponentShutdownException {
		try {
			this.inboundPort.unpublishPort();
		} catch (Exception e) {
			throw new ComponentShutdownException(e) ;
		}
		super.shutdown();
	}
	
	
	@Override
	public boolean isRunning() throws Exception {
		if(VERBOSE) 
            logMessage("Generator get running : " + this.isStart + "\n");

        return this.isStart;
	}

	@Override
	public void stop() throws Exception {
		if(VERBOSE) 
            logMessage("Generator stop running\n");

        this.isStart = false;
	}
	
	@Override
	public void activate() throws Exception {
		if(VERBOSE) 
            logMessage("Generator start running\n");

        this.isStart = true;
	}

	@Override
	public double getEnergyProduction() throws Exception {
		if(VERBOSE) 
            logMessage("Generator get production : " + DEFAULT_PRODUCTION + "\n");
        
        assert this.isStart : new PreconditionException("getEnergyProduction() -> isStart");

        return DEFAULT_PRODUCTION;
	}

	@Override
	public double getFuelLevel() throws Exception {
		if(VERBOSE) 
            logMessage("Generator get fuel level : " + this.fuelLevel + "\n");
        
        return this.fuelLevel;
	}

	@Override
	public void fill(double quantity) throws Exception {
		if(VERBOSE) 
            logMessage("Generator refill the tank " + quantity);

        assert !this.isStart : new PreconditionException("refill() -> !this.isStart");
        assert quantity > 0 : new PreconditionException("quantity > 0");
        assert this.fuelLevel + quantity <= MAX_CAPACITY : 
        	new PreconditionException("this.fuelLevel + quantity <= MAX_CAPACITY");

        fuelLevel += quantity;
        
        if(VERBOSE) 
            logMessage("New level fuel -> " + this.fuelLevel + "\n");
	}

}
