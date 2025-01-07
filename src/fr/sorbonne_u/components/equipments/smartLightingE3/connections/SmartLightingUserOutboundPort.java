package fr.sorbonne_u.components.equipments.smartLightingE3.connections;

import fr.sorbonne_u.components.ComponentI;
import fr.sorbonne_u.components.equipments.smartLighting.interfaces.SmartLightingUserCI;
import fr.sorbonne_u.components.ports.AbstractOutboundPort;

public class SmartLightingUserOutboundPort extends AbstractOutboundPort implements SmartLightingUserCI {

    //----------------------------------------------------------------------------
    // Constants and variables
    //----------------------------------------------------------------------------

    private static final long serialVersionUID = 1L;

    //----------------------------------------------------------------------------
    // Constructors
    //----------------------------------------------------------------------------

    public SmartLightingUserOutboundPort(String uri, ComponentI owner) throws Exception {
        super(uri, SmartLightingUserCI.class, owner);
    }

    public SmartLightingUserOutboundPort(ComponentI owner) throws Exception {
        super(SmartLightingUserCI.class, owner);
    }

    @Override
    public void switchOn() throws Exception {
        ((SmartLightingUserCI)this.getConnector()).switchOn();
    }

    @Override
    public void switchOff() throws Exception {
        ((SmartLightingUserCI)this.getConnector()).switchOff();
    }

    @Override
    public boolean isOn() throws Exception {
        return ((SmartLightingUserCI)this.getConnector()).isOn();
    }

    @Override
    public void setTargetIllumination(double targetIllumination) throws Exception {
        ((SmartLightingUserCI)this.getConnector()).setTargetIllumination(targetIllumination);
    }

    @Override
    public double getTargetIllumination() throws Exception {
        return ((SmartLightingUserCI)this.getConnector()).getTargetIllumination();
    }

    @Override
    public double getCurrentIllumination() throws Exception {
        return ((SmartLightingUserCI)this.getConnector()).getCurrentIllumination();
    }

    @Override
    public double getCurrentPowerLevel() throws Exception {
        return ((SmartLightingUserCI)this.getConnector()).getCurrentPowerLevel();
    }

    @Override
    public void setCurrentPowerLevel(double powerLevel) throws Exception {
        ((SmartLightingUserCI)this.getConnector()).setCurrentPowerLevel(powerLevel);
    }

    @Override
    public double getMaxPowerLevel() throws Exception {
        return ((SmartLightingUserCI)this.getConnector()).getMaxPowerLevel();
    }
}
