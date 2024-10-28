package fr.sorbonne_u.components.equipments.smartLighting;

import fr.sorbonne_u.components.AbstractComponent;
import fr.sorbonne_u.components.cvm.AbstractCVM;

public class CVMUnitTest extends AbstractCVM {

    //----------------------------------------------------------------------------
    // Constructors
    //----------------------------------------------------------------------------

    public CVMUnitTest() throws Exception {
        SmartLightingTester.VERBOSE = true;
        SmartLightingTester.X_RELATIVE_POSITION = 0;
        SmartLightingTester.Y_RELATIVE_POSITION = 0;
        SmartLighting.VERBOSE = true;
        SmartLighting.X_RELATIVE_POSITION = 1;
        SmartLighting.Y_RELATIVE_POSITION = 0;
    }

    // -------------------------------------------------------------------------
    // CVM life-cycle
    // -------------------------------------------------------------------------

    @Override
    public void deploy() throws Exception {
        AbstractComponent.createComponent(SmartLighting.class.getCanonicalName(), new Object[] {});
        AbstractComponent.createComponent(SmartLightingTester.class.getCanonicalName(), new Object[] {});
        super.deploy();
    }

    public static void main(String[] args) {
        try {
            CVMUnitTest cvm = new CVMUnitTest();
            cvm.startStandardLifeCycle(1000L);
            Thread.sleep(10000L);
            System.exit(0);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
//------------------------------------------------------------------------------