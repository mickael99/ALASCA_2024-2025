package fr.sorbonne_u.components.equipments.smartLighting.connections;

import fr.sorbonne_u.components.ComponentI;
import fr.sorbonne_u.components.equipments.smartLighting.interfaces.SmartLightingExternalControlCI;
import fr.sorbonne_u.components.equipments.smartLighting.interfaces.SmartLightingExternalControlI;
import fr.sorbonne_u.components.ports.AbstractInboundPort;
import fr.sorbonne_u.exceptions.PreconditionException;

public class SmartLightingExternalControlInboundPort extends AbstractInboundPort implements SmartLightingExternalControlCI {

    // -------------------------------------------------------------------------
    // Constants and variables
    // -------------------------------------------------------------------------
    private static final long serialVersionUID = 1L;

    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------

    public SmartLightingExternalControlInboundPort(ComponentI owner) throws Exception {
        super(SmartLightingExternalControlCI.class, owner);
        assert owner instanceof SmartLightingExternalControlI:
            new PreconditionException("Owner is not an instance of SmartLightingExternalControlI");
    }

    public SmartLightingExternalControlInboundPort(String uri, ComponentI owner) throws Exception {
        super(uri, SmartLightingExternalControlCI.class, owner);
        assert owner instanceof SmartLightingExternalControlI:
            new PreconditionException("Owner is not an instance of SmartLightingExternalControlI");
    }

    // -------------------------------------------------------------------------
    // Methods
    // -------------------------------------------------------------------------

    @Override
    public double getMaxPowerLevel() throws Exception {
        return this.getOwner().handleRequest(owner -> ((SmartLightingExternalControlI) owner).getMaxPowerLevel());
    }

    @Override
    public double getCurrentPowerLevel() throws Exception {
        return this.getOwner().handleRequest(owner -> ((SmartLightingExternalControlI) owner).getCurrentPowerLevel());
    }

    @Override
    public void setCurrentPowerLevel(double powerLevel) throws Exception {
        this.getOwner().handleRequest(owner -> {
            ((SmartLightingExternalControlCI) owner).setCurrentPowerLevel(powerLevel);
            return null;
        });
    }

    @Override
    public double getTargetIllumination() throws Exception {
        return this.getOwner().handleRequest(owner -> ((SmartLightingExternalControlCI) owner).getTargetIllumination());
    }

    @Override
    public double getCurrentIllumination() throws Exception {
        return this.getOwner().handleRequest(owner -> ((SmartLightingExternalControlCI) owner).getCurrentIllumination());
    }
}
