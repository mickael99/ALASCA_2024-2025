package fr.sorbonne_u.components.equipments.smartLighting.interfaces;

import fr.sorbonne_u.components.interfaces.OfferedCI;
import fr.sorbonne_u.components.interfaces.RequiredCI;

public interface SmartLightingExternalControlCI extends SmartLightingExternalControlI, RequiredCI, OfferedCI {

    /**
     * @see fr.sorbonne_u.components.equipments.smartLighting.interfaces.SmartLightingExternalControlI#getMaxPowerLevel()
     */
    @Override
    public double getMaxPowerLevel() throws Exception;

    /**
     * @see fr.sorbonne_u.components.equipments.smartLighting.interfaces.SmartLightingExternalControlI#getCurrentPowerLevel()
     */
    @Override
    public double getCurrentPowerLevel() throws Exception;

    /**
     * @see fr.sorbonne_u.components.equipments.smartLighting.interfaces.SmartLightingExternalControlI#setCurrentPowerLevel(double)
     */
    @Override
    public void setCurrentPowerLevel(double powerLevel) throws Exception;

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
