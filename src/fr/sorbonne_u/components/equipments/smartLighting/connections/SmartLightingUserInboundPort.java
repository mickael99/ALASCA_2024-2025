package fr.sorbonne_u.components.equipments.smartLighting.connections;

import fr.sorbonne_u.components.ComponentI;
import fr.sorbonne_u.components.equipments.smartLighting.interfaces.SmartLightingUserCI;
import fr.sorbonne_u.components.ports.AbstractInboundPort;
import fr.sorbonne_u.exceptions.PreconditionException;

public class SmartLightingUserInboundPort extends AbstractInboundPort implements SmartLightingUserCI {

    // -------------------------------------------------------------------------
    // Constants and variables
    // -------------------------------------------------------------------------
    private static final long serialVersionUID = 1L;

    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------

    public SmartLightingUserInboundPort(ComponentI owner) throws Exception {
        super (SmartLightingUserCI.class, owner);
        assert owner instanceof SmartLightingUserCI:
            new PreconditionException("Owner is not an instance of SmartLightingUserCI");
    }


    public SmartLightingUserInboundPort(String uri, ComponentI owner) throws Exception {
        super(uri, SmartLightingUserCI.class, owner);
        assert owner instanceof SmartLightingUserCI:
            new PreconditionException("Owner is not an instance of SmartLightingUserCI");
    }

    // -------------------------------------------------------------------------
    // Methods
    // -------------------------------------------------------------------------

    @Override
    public void switchOn() throws Exception {
        this.getOwner().handleRequest(owner -> {
            ((SmartLightingUserCI) owner).switchOn();
            return null;
        });
    }

    @Override
    public void switchOff() throws Exception {
        this.getOwner().handleRequest(owner -> {
            ((SmartLightingUserCI) owner).switchOff();
            return null;
        });
    }

    @Override
    public boolean isOn() throws Exception {
        return this.getOwner().handleRequest(owner -> ((SmartLightingUserCI) owner).isOn());
    }


    @Override
    public void setTargetIllumination(double targetIllumination) throws Exception {
        this.getOwner().handleRequest(owner -> {
            ((SmartLightingUserCI) owner).setTargetIllumination(targetIllumination);
            return null;
        });
    }

    @Override
    public double getTargetIllumination() throws Exception {
        return this.getOwner().handleRequest(owner -> ((SmartLightingUserCI) owner).getTargetIllumination());
    }

    @Override
    public double getCurrentIllumination() throws Exception {
        return this.getOwner().handleRequest(owner -> ((SmartLightingUserCI) owner).getCurrentIllumination());
    }

    @Override
    public double getMaxPowerLevel() throws Exception {
        return this.getOwner().handleRequest(owner -> ((SmartLightingUserCI) owner).getMaxPowerLevel());
    }

    @Override
    public double getCurrentPowerLevel() throws Exception {
        return this.getOwner().handleRequest(owner -> ((SmartLightingUserCI) owner).getCurrentPowerLevel());
    }

    @Override
    public void setCurrentPowerLevel(double powerLevel) throws Exception {
        this.getOwner().handleRequest(owner -> {
            ((SmartLightingUserCI) owner).setCurrentPowerLevel(powerLevel);
            return null;
        });
    }
}
