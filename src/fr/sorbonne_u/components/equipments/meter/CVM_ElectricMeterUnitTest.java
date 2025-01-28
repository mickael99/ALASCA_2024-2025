package fr.sorbonne_u.components.equipments.meter;

import fr.sorbonne_u.components.AbstractComponent;
import fr.sorbonne_u.components.cvm.AbstractCVM;
import fr.sorbonne_u.components.utils.ExecutionType;
import fr.sorbonne_u.components.utils.SimulationType;
import fr.sorbonne_u.utils.aclocks.ClocksServer;

import java.time.Instant;
import java.util.concurrent.TimeUnit;

public class CVM_ElectricMeterUnitTest extends AbstractCVM
{
	// -------------------------------------------------------------------------
	// Constants and variables
	// -------------------------------------------------------------------------
	
	public static final long DELAY_TO_START = 3000L;
	public static final String CLOCK_URI = "hem-clock";
	public static final String START_INSTANT = "2023-11-22T00:00:00.00Z";

	
	// -------------------------------------------------------------------------
	// Constructors
	// -------------------------------------------------------------------------

	public CVM_ElectricMeterUnitTest() throws Exception {
		ElectricMeterUnitTester.VERBOSE = true;
		ElectricMeterUnitTester.X_RELATIVE_POSITION = 0;
		ElectricMeterUnitTester.Y_RELATIVE_POSITION = 0;
		
		ElectricMeter.VERBOSE = true;
		ElectricMeter.X_RELATIVE_POSITION = 1;
		ElectricMeter.Y_RELATIVE_POSITION = 0;
		
		ClocksServer.VERBOSE = true;
		ClocksServer.X_RELATIVE_POSITION = 0;
		ClocksServer.Y_RELATIVE_POSITION = 1;
	}

	
	// -------------------------------------------------------------------------
	// CVM life-cycle
	// -------------------------------------------------------------------------

	@Override
	public void			deploy() throws Exception
	{
		AbstractComponent.createComponent(
				ElectricMeter.class.getCanonicalName(),
				new Object[]{ElectricMeter.REFLECTION_INBOUND_PORT_URI,
							 ElectricMeter.ELECTRIC_METER_INBOUND_PORT_URI,
							 ExecutionType.UNIT_TEST,
							 SimulationType.NO_SIMULATION,
							 "",
							 "",
							 TimeUnit.DAYS,
							 0.0,
							 CLOCK_URI});

		AbstractComponent.createComponent(
				ElectricMeterUnitTester.class.getCanonicalName(),
				new Object[]{CLOCK_URI});

		long unixEpochStartTimeInMillis =
				System.currentTimeMillis() + DELAY_TO_START;

		AbstractComponent.createComponent(
				ClocksServer.class.getCanonicalName(),
				new Object[]{
						CLOCK_URI,
						TimeUnit.MILLISECONDS.toNanos(
								unixEpochStartTimeInMillis),
						Instant.parse(START_INSTANT),
						1.0});

		super.deploy();
	}

	public static void	main(String[] args)
	{
		try {
			CVM_ElectricMeterUnitTest cvm = new CVM_ElectricMeterUnitTest();
			cvm.startStandardLifeCycle(1000L);
			Thread.sleep(1000000L);
			System.exit(0);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
// -----------------------------------------------------------------------------
