package fr.sorbonne_u.components.equipments.smartLighting.interfaces;

import fr.sorbonne_u.components.interfaces.OfferedCI;
import fr.sorbonne_u.components.interfaces.RequiredCI;

public interface SmartLightingInternalControlCI extends SmartLightingInternalControlI, OfferedCI, RequiredCI {

    /**
     * @see fr.sorbonne_u.components.equipments.smartLighting.interfaces.SmartLightingInternalControlI#IncreaseLightIntensity()
     */
    @Override
    public void IncreaseLightIntensity() throws Exception;

    /**
     * @see fr.sorbonne_u.components.equipments.smartLighting.interfaces.SmartLightingInternalControlI#DecreaseLightIntensity()
     */
    @Override
    public void DecreaseLightIntensity() throws Exception;

    /**
     * @see fr.sorbonne_u.components.equipments.smartLighting.interfaces.SmartLightingInternalControlI#isSwitchingAutomatically()
     */
    @Override
    public boolean isSwitchingAutomatically() throws Exception;

    /**
     * @see fr.sorbonne_u.components.equipments.smartLighting.interfaces.SmartLightingIlluminationI#getTargetIllumination()
     */
    @Override
    public double getTargetIllumination() throws Exception;

    /**
     * @see fr.sorbonne_u.components.equipments.smartLighting.interfaces.SmartLightingIlluminationI#getCurrentIllumination()
     */
    @Override
    public double getCurrentIllumination() throws Exception;
}
