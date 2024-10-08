package fr.sorbonne_u.components.equipments.fridge;

import fr.sorbonne_u.components.AbstractComponent;
import fr.sorbonne_u.components.cvm.AbstractCVM;
import fr.sorbonne_u.components.equipments.hem.HEM;

public class CVMTestRegister extends AbstractCVM {
	
	public CVMTestRegister() throws Exception {
		Fridge.TEST_REGISTRATION = true;
		HEM.TEST_COMMUNICATION_WITH_FRIDGE = true;
	}
	
	@Override
	public void			deploy() throws Exception
	{
		AbstractComponent.createComponent(
				Fridge.class.getCanonicalName(),
				new Object[]{true});

		AbstractComponent.createComponent(
				HEM.class.getCanonicalName(),
				new Object[]{false, false});

		super.deploy();
	}

	public static void	main(String[] args)
	{
		try {
			CVMTestRegister cvm = new CVMTestRegister();
			cvm.startStandardLifeCycle(1000L);
			Thread.sleep(100000L);
			System.exit(0);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
