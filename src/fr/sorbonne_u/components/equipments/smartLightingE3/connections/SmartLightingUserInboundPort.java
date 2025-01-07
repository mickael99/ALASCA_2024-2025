package fr.sorbonne_u.components.equipments.smartLightingE3.connections;

import fr.sorbonne_u.components.ComponentI;
import fr.sorbonne_u.components.equipments.smartLighting.SmartLighting;
import fr.sorbonne_u.components.equipments.smartLighting.interfaces.SmartLightingUserCI;
import fr.sorbonne_u.components.equipments.smartLighting.interfaces.SmartLightingUserI;
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
        assert owner instanceof SmartLightingUserI :
            new PreconditionException("Owner is not an instance of SmartLightingUserI");
    }


    public SmartLightingUserInboundPort(String uri, ComponentI owner) throws Exception {
        super(uri, SmartLightingUserCI.class, owner);
        assert owner instanceof SmartLightingUserI:
            new PreconditionException("Owner is not an instance of SmartLightingUserI");
    }

    // -------------------------------------------------------------------------
    // Methods
    // -------------------------------------------------------------------------

    @Override
    public void switchOn() throws Exception {
        this.getOwner().handleRequest(owner -> {
            ((SmartLighting) owner).switchOn();
            return null;
        });
    }

    @Override
    public void switchOff() throws Exception {
        this.getOwner().handleRequest(owner -> {
            ((SmartLightingUserI) owner).switchOff();
            return null;
        });
    }

    @Override
    public boolean isOn() throws Exception {
        return this.getOwner().handleRequest(owner -> ((SmartLightingUserI) owner).isOn());
    }


    @Override
    public void setTargetIllumination(double targetIllumination) throws Exception {
        this.getOwner().handleRequest(owner -> {
            ((SmartLightingUserI) owner).setTargetIllumination(targetIllumination);
            return null;
        });
    }

    @Override
    public double getTargetIllumination() throws Exception {
        return this.getOwner().handleRequest(owner -> ((SmartLightingUserI) owner).getTargetIllumination());
    }

    @Override
    public double getCurrentIllumination() throws Exception {
        return this.getOwner().handleRequest(owner -> ((SmartLightingUserI) owner).getCurrentIllumination());
    }

    @Override
    public double getMaxPowerLevel() throws Exception {
        return this.getOwner().handleRequest(owner -> ((SmartLightingUserI) owner).getMaxPowerLevel());
    }

    @Override
    public double getCurrentPowerLevel() throws Exception {
        return this.getOwner().handleRequest(owner -> ((SmartLightingUserI) owner).getCurrentPowerLevel());
    }

    @Override
    public void setCurrentPowerLevel(double powerLevel) throws Exception {
        this.getOwner().handleRequest(owner -> {
            ((SmartLightingUserI) owner).setCurrentPowerLevel(powerLevel);
            return null;
        });
    }
}
