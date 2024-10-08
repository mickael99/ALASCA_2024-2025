package fr.sorbonne_u.components.equipments.hem;

import fr.sorbonne_u.components.AbstractComponent;
import fr.sorbonne_u.components.cvm.AbstractCVM;
import fr.sorbonne_u.components.equipments.meter.ElectricMeter;

public class CVMTestMetterAndHEM extends AbstractCVM {
	
	public CVMTestMetterAndHEM() throws Exception {
		
	}
	
	@Override
	public void deploy() throws Exception {
		AbstractComponent.createComponent(
				HEM.class.getCanonicalName(),
				new Object[]{true});

		AbstractComponent.createComponent(
				ElectricMeter.class.getCanonicalName(),
				new Object[]{});

		super.deploy();
	}

	public static void	main(String[] args) {
		try {
			CVMTestMetterAndHEM cvm = new CVMTestMetterAndHEM();
			cvm.startStandardLifeCycle(1000L);
			Thread.sleep(100000L);
			System.exit(0);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
