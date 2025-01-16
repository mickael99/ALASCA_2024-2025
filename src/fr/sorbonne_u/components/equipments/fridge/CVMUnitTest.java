package fr.sorbonne_u.components.equipments.fridge;

import fr.sorbonne_u.components.AbstractComponent;
import fr.sorbonne_u.components.cvm.AbstractCVM;

public class CVMUnitTest extends AbstractCVM {

	public CVMUnitTest() throws Exception {
		Fridge.TEST_REGISTRATION = false;
	}
	
	@Override
	public void			deploy() throws Exception
	{
		AbstractComponent.createComponent(
				Fridge.class.getCanonicalName(),
				new Object[]{false});

		AbstractComponent.createComponent(
				FridgeUser.class.getCanonicalName(),
				new Object[]{true});

		super.deploy();
	}

	public static void	main(String[] args)
	{
		try {
			CVMUnitTest cvm = new CVMUnitTest();
			cvm.startStandardLifeCycle(1000L);
			Thread.sleep(100000L);
			System.exit(0);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
