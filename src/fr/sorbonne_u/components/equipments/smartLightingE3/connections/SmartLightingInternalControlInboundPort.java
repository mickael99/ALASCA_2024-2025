package fr.sorbonne_u.components.equipments.smartLightingE3.connections;

import fr.sorbonne_u.components.ComponentI;
import fr.sorbonne_u.components.equipments.smartLighting.interfaces.SmartLightingInternalControlCI;
import fr.sorbonne_u.components.equipments.smartLighting.interfaces.SmartLightingInternalControlI;
import fr.sorbonne_u.components.ports.AbstractInboundPort;
import fr.sorbonne_u.exceptions.PreconditionException;

public class SmartLightingInternalControlInboundPort extends AbstractInboundPort implements SmartLightingInternalControlCI {

    // ------------------------------------------------------------------------
    // Constants and variables
    // ------------------------------------------------------------------------
    private static final long serialVersionUID = 1L;

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------

    public SmartLightingInternalControlInboundPort(ComponentI owner) throws Exception {
        super(SmartLightingInternalControlCI.class, owner);
        assert owner instanceof SmartLightingInternalControlI:
            new PreconditionException("Owner is not an instance of SmartLightingInternalControlI");
    }

    public SmartLightingInternalControlInboundPort(String uri, ComponentI owner) throws Exception {
        super(uri, SmartLightingInternalControlCI.class, owner);
        assert owner instanceof SmartLightingInternalControlI:
            new PreconditionException("Owner is not an instance of SmartLightingInternalControlI");
    }

    // ------------------------------------------------------------------------
    // Methods
    // ------------------------------------------------------------------------

    /**
     * @see SmartLightingInternalControlI#isSwitchingAutomatically()
     */
    @Override
    public boolean isSwitchingAutomatically() throws Exception {
        return this.getOwner().handleRequest(owner -> ((SmartLightingInternalControlI) owner).isSwitchingAutomatically());
    }

    /**
     * @see fr.sorbonne_u.components.equipments.smartLighting.interfaces.SmartLightingIlluminationI#getTargetIllumination()
     */
    @Override
    public double getTargetIllumination() throws Exception {
        return this.getOwner().handleRequest(owner -> ((SmartLightingInternalControlI) owner).getTargetIllumination());
    }

    /**
     * @see fr.sorbonne_u.components.equipments.smartLighting.interfaces.SmartLightingIlluminationI#getCurrentIllumination()
     */
    @Override
    public double getCurrentIllumination() throws Exception {
        return this.getOwner().handleRequest(owner -> ((SmartLightingInternalControlI) owner).getCurrentIllumination());
    }

    /**
     * @see SmartLightingInternalControlI#IncreaseLightIntensity()
     */
    @Override
    public void IncreaseLightIntensity() throws Exception {
        this.getOwner().handleRequest(owner -> {
            ((SmartLightingInternalControlI) owner).IncreaseLightIntensity();
            return null;
        });
    }

    /**
     * @see SmartLightingInternalControlI#DecreaseLightIntensity()
     */
    @Override
    public void DecreaseLightIntensity() throws Exception {
        this.getOwner().handleRequest(owner -> {
            ((SmartLightingInternalControlI) owner).DecreaseLightIntensity();
            return null;
        });
    }
}
