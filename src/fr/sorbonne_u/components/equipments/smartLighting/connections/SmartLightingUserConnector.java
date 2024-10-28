package fr.sorbonne_u.components.equipments.smartLighting.connections;

import fr.sorbonne_u.components.connectors.AbstractConnector;
import fr.sorbonne_u.components.equipments.smartLighting.interfaces.SmartLightingUserCI;

public class SmartLightingUserConnector extends AbstractConnector implements SmartLightingUserCI {
    /**
     * @see fr.sorbonne_u.components.equipments.smartLighting.interfaces.SmartLightingUserCI#switchOn()
     */
    @Override
    public void switchOn() throws Exception {
        ((SmartLightingUserCI)this.offering).switchOn();
    }

    /**
     * @see fr.sorbonne_u.components.equipments.smartLighting.interfaces.SmartLightingUserCI#switchOff()
     */
    @Override
    public void switchOff() throws Exception {
        ((SmartLightingUserCI)this.offering).switchOff();
    }

    /**
     * @see fr.sorbonne_u.components.equipments.smartLighting.interfaces.SmartLightingUserCI#isOn()
     */
    @Override
    public boolean isOn() throws Exception {
        return ((SmartLightingUserCI)this.offering).isOn();
    }

    /**
     * @see fr.sorbonne_u.components.equipments.smartLighting.interfaces.SmartLightingUserCI#setTargetIllumination(double)
     */
    @Override
    public void setTargetIllumination(double targetIllumination) throws Exception {
        ((SmartLightingUserCI)this.offering).setTargetIllumination(targetIllumination);
    }

    /**
     * @see fr.sorbonne_u.components.equipments.smartLighting.interfaces.SmartLightingUserCI#getTargetIllumination()
     */
    @Override
    public double getTargetIllumination() throws Exception {
        return ((SmartLightingUserCI)this.offering).getTargetIllumination();
    }

    /**
     * @see fr.sorbonne_u.components.equipments.smartLighting.interfaces.SmartLightingUserCI#getCurrentIllumination()
     */
    @Override
    public double getCurrentIllumination() throws Exception {
        return ((SmartLightingUserCI)this.offering).getCurrentIllumination();
    }

    /**
     * @see fr.sorbonne_u.components.equipments.smartLighting.interfaces.SmartLightingUserCI#getCurrentPowerLevel()
     */
    @Override
    public double getCurrentPowerLevel() throws Exception {
        return ((SmartLightingUserCI)this.offering).getCurrentPowerLevel();
    }

    /**
     * @see fr.sorbonne_u.components.equipments.smartLighting.interfaces.SmartLightingUserCI#getMaxPowerLevel()
     */
    @Override
    public double getMaxPowerLevel() throws Exception {
        return ((SmartLightingUserCI)this.offering).getMaxPowerLevel();
    }

    /**
     * @see fr.sorbonne_u.components.equipments.smartLighting.interfaces.SmartLightingUserCI#setCurrentPowerLevel(double)
     */
    @Override
    public void setCurrentPowerLevel(double powerLevel) throws Exception {
        ((SmartLightingUserCI)this.offering).setCurrentPowerLevel(powerLevel);
    }
}
