package fr.sorbonne_u.components.equipments.smartLightingE3.interfaces;

import fr.sorbonne_u.components.interfaces.OfferedCI;
import fr.sorbonne_u.components.interfaces.RequiredCI;

public interface SmartLightingExternalControlCI extends SmartLightingExternalControlI, RequiredCI, OfferedCI {

    /**
     * @see SmartLightingExternalControlI#getMaxPowerLevel()
     */
    @Override
    public double getMaxPowerLevel() throws Exception;

    /**
     * @see SmartLightingExternalControlI#getCurrentPowerLevel()
     */
    @Override
    public double getCurrentPowerLevel() throws Exception;

    /**
     * @see SmartLightingExternalControlI#setCurrentPowerLevel(double)
     */
    @Override
    public void setCurrentPowerLevel(double powerLevel) throws Exception;

    /**
     * @see SmartLightingIlluminationI#getTargetIllumination()
     */
    @Override
    public double getTargetIllumination() throws Exception;

    /**
     * @see SmartLightingIlluminationI#getCurrentIllumination()
     */
    @Override
    public double getCurrentIllumination() throws Exception;

}
