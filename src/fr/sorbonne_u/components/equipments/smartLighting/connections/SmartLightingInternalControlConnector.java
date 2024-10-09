package fr.sorbonne_u.components.equipments.smartLighting.connections;

import fr.sorbonne_u.components.connectors.AbstractConnector;
import fr.sorbonne_u.components.equipments.smartLighting.interfaces.SmartLightingInternalControlCI;

public class SmartLightingInternalControlConnector extends AbstractConnector implements SmartLightingInternalControlCI {

    /**
     * @see fr.sorbonne_u.components.equipments.smartLighting.interfaces.SmartLightingInternalControlCI#IncreaseLightIntensity()
     */
    @Override
    public void IncreaseLightIntensity() throws Exception {
        ((SmartLightingInternalControlCI)this.offering).IncreaseLightIntensity();
    }

    /**
     * @see fr.sorbonne_u.components.equipments.smartLighting.interfaces.SmartLightingInternalControlCI#DecreaseLightIntensity()
     */
    @Override
    public void DecreaseLightIntensity() throws Exception {
        ((SmartLightingInternalControlCI)this.offering).DecreaseLightIntensity();
    }

    /**
     * @see fr.sorbonne_u.components.equipments.smartLighting.interfaces.SmartLightingInternalControlCI#isSwitchingAutomatically()
     */
    @Override
    public boolean isSwitchingAutomatically() throws Exception {
        return ((SmartLightingInternalControlCI)this.offering).isSwitchingAutomatically();
    }

    /**
     * @see fr.sorbonne_u.components.equipments.smartLighting.interfaces.SmartLightingIlluminationI#getTargetIllumination()
     */
    @Override
    public double getTargetIllumination() throws Exception {
        return ((SmartLightingInternalControlCI)this.offering).getTargetIllumination();
    }

    /**
     * @see fr.sorbonne_u.components.equipments.smartLighting.interfaces.SmartLightingIlluminationI#getCurrentIllumination()
     */
    @Override
    public double getCurrentIllumination() throws Exception {
        return ((SmartLightingInternalControlCI)this.offering).getCurrentIllumination();
    }
}
