package fr.sorbonne_u.components.equipments.smartLightingE3.interfaces;

import fr.sorbonne_u.components.interfaces.OfferedCI;
import fr.sorbonne_u.components.interfaces.RequiredCI;

public interface SmartLightingUserCI extends SmartLightingUserI, OfferedCI, RequiredCI {

    /**
     * @see SmartLightingUserI#isOn()
     */
    @Override
    public boolean isOn() throws Exception;

    /**
     * @see SmartLightingUserI#switchOn()
     */
    @Override
    public void switchOn() throws Exception;

    /**
     * @see SmartLightingUserI#switchOff()
     */
    @Override
    public void switchOff() throws Exception;

    /**
     * @see SmartLightingUserI#setTargetIllumination(double)
     */
    @Override
    public void setTargetIllumination(double targetIllumination) throws Exception;

    /**
     * @see SmartLightingIlluminationI#getTargetIllumination()
     */
    public double getTargetIllumination() throws Exception;

    /**
     * @see SmartLightingIlluminationI#getCurrentIllumination()
     */
    public double getCurrentIllumination() throws Exception;

    /**
     * @see SmartLightingExternalControlI#getMaxPowerLevel()
     */
    public double getMaxPowerLevel() throws Exception;

    /**
     * @see SmartLightingExternalControlI#getCurrentPowerLevel()
     */
    public double getCurrentPowerLevel() throws Exception;

    /**
     * @see SmartLightingExternalControlI#setCurrentPowerLevel(double)
     */
    public void setCurrentPowerLevel(double powerLevel) throws Exception;
}
