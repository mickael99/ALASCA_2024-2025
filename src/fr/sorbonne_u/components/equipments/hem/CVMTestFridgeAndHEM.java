package fr.sorbonne_u.components.equipments.hem;

import fr.sorbonne_u.components.AbstractComponent;
import fr.sorbonne_u.components.cvm.AbstractCVM;
import fr.sorbonne_u.components.equipments.fridge.Fridge;

public class CVMTestFridgeAndHEM extends AbstractCVM {
	
	public CVMTestFridgeAndHEM() throws Exception {
		
	}
	
	@Override
	public void deploy() throws Exception {
		AbstractComponent.createComponent(
				HEM.class.getCanonicalName(),
				new Object[]{false, true});

		AbstractComponent.createComponent(
				Fridge.class.getCanonicalName(),
				new Object[]{false});

		super.deploy();
	}

	public static void	main(String[] args) {
		try {
			CVMTestFridgeAndHEM cvm = new CVMTestFridgeAndHEM();
			cvm.startStandardLifeCycle(1000L);
			Thread.sleep(100000L);
			System.exit(0);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
