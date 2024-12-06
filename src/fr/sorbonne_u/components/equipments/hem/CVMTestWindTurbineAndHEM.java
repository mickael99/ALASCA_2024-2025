package fr.sorbonne_u.components.equipments.hem;

import fr.sorbonne_u.components.AbstractComponent;
import fr.sorbonne_u.components.cvm.AbstractCVM;
import fr.sorbonne_u.components.equipments.windTurbine.WindTurbine;

public class CVMTestWindTurbineAndHEM extends AbstractCVM {
	
	public CVMTestWindTurbineAndHEM() throws Exception {
		HEM.X_RELATIVE_POSITION = 0;
		HEM.Y_RELATIVE_POSITION = 0;
		
		WindTurbine.X_RELATIVE_POSITION = 1;
		WindTurbine.Y_RELATIVE_POSITION = 0;
	}
	
	@Override
	public void deploy() throws Exception {
		AbstractComponent.createComponent(
				HEM.class.getCanonicalName(),
				new Object[]{HEM.TestType.WIND_TURBINE});

		AbstractComponent.createComponent(
				WindTurbine.class.getCanonicalName(),
				new Object[]{});

		super.deploy();
	}

	public static void	main(String[] args) {
		try {
			CVMTestWindTurbineAndHEM cvm = new CVMTestWindTurbineAndHEM();
			cvm.startStandardLifeCycle(1000L);
			Thread.sleep(100000L);
			System.exit(0);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
