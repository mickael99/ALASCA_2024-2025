package fr.sorbonne_u.components.equipments.smartLighting;

import fr.sorbonne_u.components.AbstractComponent;
import fr.sorbonne_u.components.cvm.AbstractCVM;
import fr.sorbonne_u.components.equipments.hem.HEM;

public class CVMTestRegister extends AbstractCVM {

    public CVMTestRegister() throws Exception {
        SmartLighting.TEST_REGISTRATION = true;
        HEM.TEST_COMMUNICATION_WITH_SMART_LIGHTING = true;
    }

    @Override
    public void			deploy() throws Exception
    {
        AbstractComponent.createComponent(
                SmartLighting.class.getCanonicalName(),
                new Object[]{true});

        AbstractComponent.createComponent(
                HEM.class.getCanonicalName(),
                new Object[]{false, false, false});

        super.deploy();
    }

    public static void	main(String[] args)
    {
        try {
            fr.sorbonne_u.components.equipments.smartLighting.CVMTestRegister cvm = new fr.sorbonne_u.components.equipments.smartLighting.CVMTestRegister();
            cvm.startStandardLifeCycle(1000L);
            Thread.sleep(100000L);
            System.exit(0);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
