package fr.sorbonne_u.components.equipments.iron;

import fr.sorbonne_u.components.AbstractComponent;
import fr.sorbonne_u.components.cvm.AbstractCVM;

public class CVMUnitTest extends AbstractCVM {
	
	public CVMUnitTest() throws Exception {
		
	}


	// -------------------------------------------------------------------------
	// CVM life-cycle
	// -------------------------------------------------------------------------

	@Override
	public void deploy() throws Exception {
		AbstractComponent.createComponent(
				Iron.class.getCanonicalName(),
				new Object[]{});

		AbstractComponent.createComponent(
					IronTester.class.getCanonicalName(),
					new Object[]{});

		super.deploy();
	}

	public static void main(String[] args) {
		try {
			CVMUnitTest cvm = new CVMUnitTest();
			cvm.startStandardLifeCycle(1000L);
			Thread.sleep(1000000L);
			System.exit(0);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
