package fr.sorbonne_u.components.equipments.generator;

import fr.sorbonne_u.components.AbstractComponent;
import fr.sorbonne_u.components.cvm.AbstractCVM;

public class CVMGeneratorTest extends AbstractCVM {
	
	public CVMGeneratorTest() throws Exception {
		super();
	}
	
	@Override
	public void deploy() throws Exception {
		AbstractComponent.createComponent(
				Generator.class.getCanonicalName(),
				new Object[]{});

		AbstractComponent.createComponent(
				GeneratorUnitTest.class.getCanonicalName(),
				new Object[]{});

		super.deploy();
	}
	
	public static void main(String[] args) {
		try {
			CVMGeneratorTest cvm = new CVMGeneratorTest();
			cvm.startStandardLifeCycle(1000L);
			Thread.sleep(100000L);
			System.exit(0);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
