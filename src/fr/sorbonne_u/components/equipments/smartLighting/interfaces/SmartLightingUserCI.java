package fr.sorbonne_u.components.equipments.smartLighting.interfaces;

import fr.sorbonne_u.components.interfaces.OfferedCI;
import fr.sorbonne_u.components.interfaces.RequiredCI;

public interface SmartLightingUserCI extends SmartLightingUserI, OfferedCI, RequiredCI {

    /**
     * @see fr.sorbonne_u.components.equipments.smartLighting.interfaces.SmartLightingUserI#isOn()
     */
    @Override
    public boolean isOn() throws Exception;

    /**
     * @see fr.sorbonne_u.components.equipments.smartLighting.interfaces.SmartLightingUserI#switchOn()
     */
    @Override
    public void switchOn() throws Exception;

    /**
     * @see fr.sorbonne_u.components.equipments.smartLighting.interfaces.SmartLightingUserI#switchOff()
     */
    @Override
    public void switchOff() throws Exception;

    /**
     * @see fr.sorbonne_u.components.equipments.smartLighting.interfaces.SmartLightingUserI#setTargetIllumination(double)
     */
    @Override
    public void setTargetIllumination(double targetIllumination) throws Exception;

    /**
     * @see fr.sorbonne_u.components.equipments.smartLighting.interfaces.SmartLightingIlluminationI#getTargetIllumination()
     */
    public double getTargetIllumination() throws Exception;

    /**
     * @see fr.sorbonne_u.components.equipments.smartLighting.interfaces.SmartLightingIlluminationI#getCurrentIllumination()
     */
    public double getCurrentIllumination() throws Exception;

    /**
     * @see fr.sorbonne_u.components.equipments.smartLighting.interfaces.SmartLightingExternalControlI#getMaxPowerLevel()
     */
    public double getMaxPowerLevel() throws Exception;

    /**
     * @see fr.sorbonne_u.components.equipments.smartLighting.interfaces.SmartLightingExternalControlI#getCurrentPowerLevel()
     */
    public double getCurrentPowerLevel() throws Exception;

    /**
     * @see fr.sorbonne_u.components.equipments.smartLighting.interfaces.SmartLightingExternalControlI#setCurrentPowerLevel(double)
     */
    public void setCurrentPowerLevel(double powerLevel) throws Exception;
}
