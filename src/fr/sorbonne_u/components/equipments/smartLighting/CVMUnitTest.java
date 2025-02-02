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
        fr.sorbonne_u.components.equipments.smartLighting.SmartLighting.X_RELATIVE_POSITION = 1;
        fr.sorbonne_u.components.equipments.smartLighting.SmartLighting.Y_RELATIVE_POSITION = 0;
    }

    // -------------------------------------------------------------------------
    // CVM life-cycle
    // -------------------------------------------------------------------------

    /**
     * @see AbstractCVM#deploy()
     */
    @Override
    public void deploy() throws Exception {
        AbstractComponent.createComponent(SmartLighting.class.getCanonicalName(), new Object[] {false});
        AbstractComponent.createComponent(SmartLightingTester.class.getCanonicalName(), new Object[] {true});
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