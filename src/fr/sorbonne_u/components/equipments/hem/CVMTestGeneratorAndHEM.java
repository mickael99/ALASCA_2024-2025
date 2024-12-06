package fr.sorbonne_u.components.equipments.hem;

import fr.sorbonne_u.components.AbstractComponent;
import fr.sorbonne_u.components.cvm.AbstractCVM;
import fr.sorbonne_u.components.equipments.generator.Generator;

public class CVMTestGeneratorAndHEM extends AbstractCVM {

	public CVMTestGeneratorAndHEM() throws Exception {
		HEM.X_RELATIVE_POSITION = 0;
		HEM.Y_RELATIVE_POSITION = 0;
		
		Generator.X_RELATIVE_POSITION = 1;
		Generator.Y_RELATIVE_POSITION = 0;
	}
	
	@Override
	public void deploy() throws Exception {
		AbstractComponent.createComponent(
				HEM.class.getCanonicalName(),
				new Object[]{HEM.TestType.GENERATOR});

		AbstractComponent.createComponent(
				Generator.class.getCanonicalName(),
				new Object[]{});

		super.deploy();
	}

	public static void	main(String[] args) {
		try {
			CVMTestGeneratorAndHEM cvm = new CVMTestGeneratorAndHEM();
			cvm.startStandardLifeCycle(1000L);
			Thread.sleep(100000L);
			System.exit(0);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
