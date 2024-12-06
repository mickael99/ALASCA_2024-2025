package fr.sorbonne_u.components.equipments.hem;

import fr.sorbonne_u.components.AbstractComponent;
import fr.sorbonne_u.components.cvm.AbstractCVM;
import fr.sorbonne_u.components.equipments.smartLighting.SmartLighting;

public class CVMTestSmartLightingAndHEM extends AbstractCVM {

    public CVMTestSmartLightingAndHEM() throws Exception {
        SmartLighting.TEST_REGISTRATION = true;
        
        HEM.X_RELATIVE_POSITION = 0;
		HEM.Y_RELATIVE_POSITION = 0;
		
		SmartLighting.X_RELATIVE_POSITION = 1;
		SmartLighting.Y_RELATIVE_POSITION = 0;
    }

    @Override
    public void	deploy() throws Exception
    {
        AbstractComponent.createComponent(
                SmartLighting.class.getCanonicalName(),
                new Object[]{true});

        AbstractComponent.createComponent(
                HEM.class.getCanonicalName(),
                new Object[]{HEM.TestType.SMART_LIGHTING});

        super.deploy();
    }

    public static void	main(String[] args)
    {
        try {
            CVMTestSmartLightingAndHEM cvm = new CVMTestSmartLightingAndHEM();
            cvm.startStandardLifeCycle(1000L);
            Thread.sleep(100000L);
            System.exit(0);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
