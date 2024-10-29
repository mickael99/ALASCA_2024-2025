package fr.sorbonne_u.components.equipments.battery;

import fr.sorbonne_u.components.AbstractComponent;
import fr.sorbonne_u.components.cvm.AbstractCVM;


public class CVMBatteryTest extends AbstractCVM {
	
	public CVMBatteryTest() throws Exception {

	}
	
	@Override
	public void deploy() throws Exception {
		AbstractComponent.createComponent(
				Battery.class.getCanonicalName(),
				new Object[]{});

		AbstractComponent.createComponent(
				BatteryTester.class.getCanonicalName(),
				new Object[]{});

		super.deploy();
	}

	public static void main(String[] args) {
		try {
			CVMBatteryTest cvm = new CVMBatteryTest();
			cvm.startStandardLifeCycle(1000L);
			Thread.sleep(100000L);
			System.exit(0);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
