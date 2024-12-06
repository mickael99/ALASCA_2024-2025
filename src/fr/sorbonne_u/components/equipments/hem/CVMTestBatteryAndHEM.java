package fr.sorbonne_u.components.equipments.hem;

import fr.sorbonne_u.components.AbstractComponent;
import fr.sorbonne_u.components.cvm.AbstractCVM;
import fr.sorbonne_u.components.equipments.battery.Battery;

public class CVMTestBatteryAndHEM extends AbstractCVM {
	
	public CVMTestBatteryAndHEM() throws Exception {
		HEM.X_RELATIVE_POSITION = 0;
		HEM.Y_RELATIVE_POSITION = 0;
		
		Battery.X_RELATIVE_POSITION = 1;
		Battery.Y_RELATIVE_POSITION = 0;
	}
	
	@Override
	public void deploy() throws Exception {
		AbstractComponent.createComponent(
				HEM.class.getCanonicalName(),
				new Object[]{HEM.TestType.BATTERY});

		AbstractComponent.createComponent(
				Battery.class.getCanonicalName(),
				new Object[]{});

		super.deploy();
	}

	public static void	main(String[] args) {
		try {
			CVMTestBatteryAndHEM cvm = new CVMTestBatteryAndHEM();
			cvm.startStandardLifeCycle(1000L);
			Thread.sleep(100000L);
			System.exit(0);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
