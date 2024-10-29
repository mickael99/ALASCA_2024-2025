package fr.sorbonne_u.components.equipments.windTurbine;

import fr.sorbonne_u.components.AbstractComponent;
import fr.sorbonne_u.components.cvm.AbstractCVM;

public class CVMWindTurbineTest extends AbstractCVM {

	public CVMWindTurbineTest() throws Exception {
		super();
	}
	
	@Override
	public void deploy() throws Exception {
		AbstractComponent.createComponent(
				WindTurbine.class.getCanonicalName(),
				new Object[]{});

		AbstractComponent.createComponent(
				WindTurbineTester.class.getCanonicalName(),
				new Object[]{});

		super.deploy();
	}

	public static void main(String[] args) {
		try {
			CVMWindTurbineTest cvm = new CVMWindTurbineTest();
			cvm.startStandardLifeCycle(1000L);
			Thread.sleep(100000L);
			System.exit(0);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
