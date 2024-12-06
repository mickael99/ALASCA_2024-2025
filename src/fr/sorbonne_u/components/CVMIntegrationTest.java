package fr.sorbonne_u.components;

import fr.sorbonne_u.components.cvm.AbstractCVM;
import fr.sorbonne_u.components.equipments.battery.Battery;
import fr.sorbonne_u.components.equipments.fridge.Fridge;
import fr.sorbonne_u.components.equipments.generator.Generator;
import fr.sorbonne_u.components.equipments.hem.HEM;
import fr.sorbonne_u.components.equipments.iron.Iron;
import fr.sorbonne_u.components.equipments.iron.IronTester;
import fr.sorbonne_u.components.equipments.meter.ElectricMeter;
import fr.sorbonne_u.components.equipments.smartLighting.SmartLighting;
import fr.sorbonne_u.components.equipments.windTurbine.WindTurbine;

public class CVMIntegrationTest extends AbstractCVM {
	
	public CVMIntegrationTest() throws Exception {
		Fridge.TEST_REGISTRATION = true;
		SmartLighting.TEST_REGISTRATION = true;
		
		HEM.X_RELATIVE_POSITION = 0;
		HEM.Y_RELATIVE_POSITION = 0;
		
		Fridge.X_RELATIVE_POSITION = 1;
		Fridge.Y_RELATIVE_POSITION = 0;
		
		SmartLighting.X_RELATIVE_POSITION = 2;
		SmartLighting.Y_RELATIVE_POSITION = 0;
		
		WindTurbine.X_RELATIVE_POSITION = 1;
		WindTurbine.Y_RELATIVE_POSITION = 1;
		
		Generator.X_RELATIVE_POSITION = 2;
		Generator.Y_RELATIVE_POSITION = 1;
		
		Battery.X_RELATIVE_POSITION = 0;
		Battery.Y_RELATIVE_POSITION = 2;
		
		ElectricMeter.X_RELATIVE_POSITION = 0;
		ElectricMeter.Y_RELATIVE_POSITION = 3;
		
		Iron.X_RELATIVE_POSITION = 2;
		Iron.Y_RELATIVE_POSITION = 3;
		
		IronTester.X_RELATIVE_POSITION = 3;
		IronTester.Y_RELATIVE_POSITION = 3;
	}
	
	@Override
	public void	deploy() throws Exception {

		AbstractComponent.createComponent(
				Iron.class.getCanonicalName(),
				new Object[]{});

		AbstractComponent.createComponent(
					IronTester.class.getCanonicalName(),
					new Object[]{});
		
//		AbstractComponent.createComponent(
//				Toaster.class.getCanonicalName(),
//				new Object[]{});
//	
//		AbstractComponent.createComponent(
//				ToasterTester.class.getCanonicalName(),
//				new Object[]{true});
		
		AbstractComponent.createComponent(
				HEM.class.getCanonicalName(),
				new Object[]{HEM.TestType.INTEGRATION});
		
		AbstractComponent.createComponent(
				Fridge.class.getCanonicalName(),
				new Object[]{true});
	
		AbstractComponent.createComponent(
                SmartLighting.class.getCanonicalName(),
                new Object[]{true});
		
		AbstractComponent.createComponent(
				WindTurbine.class.getCanonicalName(),
				new Object[]{});
		
		AbstractComponent.createComponent(
				Generator.class.getCanonicalName(),
				new Object[]{});
		
		AbstractComponent.createComponent(
				ElectricMeter.class.getCanonicalName(),
				new Object[]{});
		
		AbstractComponent.createComponent(
				Battery.class.getCanonicalName(),
				new Object[]{});

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
