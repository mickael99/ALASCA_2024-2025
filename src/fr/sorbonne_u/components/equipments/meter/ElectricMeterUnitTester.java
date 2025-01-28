package fr.sorbonne_u.components.equipments.meter;


import fr.sorbonne_u.components.AbstractComponent;
import fr.sorbonne_u.components.annotations.RequiredInterfaces;
import fr.sorbonne_u.components.equipments.meter.connections.ElectricMeterConnector;
import fr.sorbonne_u.components.equipments.meter.connections.ElectricMeterOutboundPort;
import fr.sorbonne_u.components.equipments.meter.interfaces.ElectricMeterCI;
import fr.sorbonne_u.components.exceptions.ComponentShutdownException;
import fr.sorbonne_u.components.exceptions.ComponentStartException;
import fr.sorbonne_u.utils.aclocks.*;

import static org.junit.jupiter.api.Assertions.assertTrue;

@RequiredInterfaces(required={ElectricMeterCI.class, ClocksServerCI.class})
public class ElectricMeterUnitTester extends AbstractComponent {
	// -------------------------------------------------------------------------
	// Constants and variables
	// -------------------------------------------------------------------------

	public static boolean		VERBOSE = false;
	public static int			X_RELATIVE_POSITION = 0;
	public static int			Y_RELATIVE_POSITION = 0;

	protected ElectricMeterOutboundPort outboundPort;
	protected String clockURI;

	// -------------------------------------------------------------------------
	// Constructors
	// -------------------------------------------------------------------------

	protected ElectricMeterUnitTester(String clockURI) throws Exception {
		super(1, 0);

		this.clockURI = clockURI;

		this.outboundPort = new ElectricMeterOutboundPort(this);
		this.outboundPort.publishPort();

		if(VERBOSE) {
			this.tracer.get().setTitle("Electric meter tester component");
			this.tracer.get().setRelativePosition(X_RELATIVE_POSITION,
												  Y_RELATIVE_POSITION);
			this.toggleTracing();
		}
	}

	// -------------------------------------------------------------------------
	// Component internal methods
	// -------------------------------------------------------------------------

	protected void testGetCurrentConsumption() {
		this.traceMessage("testGetCurrentConsumption()...\n");
		try {
			this.traceMessage("Electric meter current consumption? " +
									this.outboundPort.getCurrentConsumption() + "\n");
		} catch (Exception e) {
			this.traceMessage("...KO.\n");
			assertTrue(false);
		}
		this.traceMessage("...done.\n");
	}

	protected void testGetCurrentProduction() {
		this.traceMessage("testGetCurrentProduction()...\n");
		try {
			this.traceMessage("Electric meter current production? " +
									this.outboundPort.getCurrentProduction() + "\n");
		} catch (Exception e) {
			this.traceMessage("...KO.\n");
			assertTrue(false);
		}
		this.traceMessage("...done.\n");
	}

	protected void runAllTests() {
		this.testGetCurrentConsumption();
		this.testGetCurrentProduction();
	}

	// -------------------------------------------------------------------------
	// Component life-cycle
	// -------------------------------------------------------------------------

	@Override
	public synchronized void start() throws ComponentStartException {
		super.start();

		try {
			this.doPortConnection(
					this.outboundPort.getPortURI(),
					ElectricMeter.ELECTRIC_METER_INBOUND_PORT_URI,
					ElectricMeterConnector.class.getCanonicalName());
		} catch (Exception e) {
			throw new ComponentStartException(e) ;
		}
	}

	@Override
	public synchronized void execute() throws Exception {
		ClocksServerOutboundPort clocksServerOutboundPort =
				new ClocksServerOutboundPort(this);
		clocksServerOutboundPort.publishPort();
		this.doPortConnection(
				clocksServerOutboundPort.getPortURI(),
				ClocksServer.STANDARD_INBOUNDPORT_URI,
				ClocksServerConnector.class.getCanonicalName());
		this.logMessage("ElectricMeterUnitTester gets the clock.");
		AcceleratedClock ac =
				clocksServerOutboundPort.getClock(this.clockURI);
		this.doPortDisconnection(clocksServerOutboundPort.getPortURI());
		clocksServerOutboundPort.unpublishPort();

		this.logMessage("ElectricMeterUnitTester waits until start time.");
		ac.waitUntilStart();
		this.logMessage("ElectricMeterUnitTester starts.");
		this.logMessage("ElectricMeterUnitTester begins to perform tests.");
		this.runAllTests();
		this.logMessage("ElectricMeterUnitTester tests end.");
	}

	@Override
	public synchronized void finalise() throws Exception {
		this.doPortDisconnection(this.outboundPort.getPortURI());
		super.finalise();
	}

	@Override
	public synchronized void shutdown() throws ComponentShutdownException {
		try {
			this.outboundPort.unpublishPort();
		} catch (Exception e) {
			throw new ComponentShutdownException(e) ;
		}
		super.shutdown();
	}
}