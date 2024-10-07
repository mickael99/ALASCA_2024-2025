package fr.sorbonne_u.components;

import fr.sorbonne_u.components.cvm.AbstractCVM;
import fr.sorbonne_u.components.equipments.fridge.Fridge;
import fr.sorbonne_u.components.equipments.fridge.FridgeTester;
import fr.sorbonne_u.components.equipments.iron.Iron;
import fr.sorbonne_u.components.equipments.iron.IronTester;
import fr.sorbonne_u.components.equipments.toaster.Toaster;
import fr.sorbonne_u.components.equipments.toaster.ToasterTester;
import fr.sorbonne_u.exceptions.ContractException;
import fr.sorbonne_u.utils.aclocks.ClocksServer;

public class CVMIntegrationTest extends AbstractCVM {
	
	public static final String	TEST_CLOCK_URI = "test-clock";
	public static final long	DELAY_TO_START_IN_MILLIS = 3000;
	
	public CVMIntegrationTest() throws Exception {
		ContractException.VERBOSE = true;
		ClocksServer.VERBOSE = true;
		ClocksServer.X_RELATIVE_POSITION = 2;
		ClocksServer.Y_RELATIVE_POSITION = 0;
	}
	
	@Override
	public void	deploy() throws Exception {
		AbstractComponent.createComponent(
				ClocksServer.class.getCanonicalName(),
				new Object[]{});

		AbstractComponent.createComponent(
				Iron.class.getCanonicalName(),
				new Object[]{});

		AbstractComponent.createComponent(
					IronTester.class.getCanonicalName(),
					new Object[]{true});
		
		AbstractComponent.createComponent(
				Toaster.class.getCanonicalName(),
				new Object[]{});
	
		AbstractComponent.createComponent(
				ToasterTester.class.getCanonicalName(),
				new Object[]{true});
		
		AbstractComponent.createComponent(
				Fridge.class.getCanonicalName(),
				new Object[]{});
	
		AbstractComponent.createComponent(
				FridgeTester.class.getCanonicalName(),
				new Object[]{true});

		super.deploy();
	}

	public static void main(String[] args) {
		try {
			CVMIntegrationTest cvm = new CVMIntegrationTest();
			cvm.startStandardLifeCycle(12000L);
			Thread.sleep(30000L);
			System.exit(0);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
}
