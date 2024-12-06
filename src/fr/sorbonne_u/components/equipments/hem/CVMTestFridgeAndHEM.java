package fr.sorbonne_u.components.equipments.hem;

import fr.sorbonne_u.components.AbstractComponent;
import fr.sorbonne_u.components.cvm.AbstractCVM;
import fr.sorbonne_u.components.equipments.fridge.Fridge;

public class CVMTestFridgeAndHEM extends AbstractCVM {
	
	public CVMTestFridgeAndHEM() throws Exception {
		Fridge.TEST_REGISTRATION = true;
		
		HEM.X_RELATIVE_POSITION = 0;
		HEM.Y_RELATIVE_POSITION = 0;
		
		Fridge.X_RELATIVE_POSITION = 1;
		Fridge.Y_RELATIVE_POSITION = 0;
	}
	
	@Override
	public void	deploy() throws Exception
	{
		AbstractComponent.createComponent(
				Fridge.class.getCanonicalName(),
				new Object[]{true});

		AbstractComponent.createComponent(
				HEM.class.getCanonicalName(),
				new Object[]{HEM.TestType.FRIDGE});

		super.deploy();
	}

	public static void	main(String[] args)
	{
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
