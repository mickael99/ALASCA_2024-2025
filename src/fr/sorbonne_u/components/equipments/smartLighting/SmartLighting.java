package fr.sorbonne_u.components.equipments.smartLighting;

import fr.sorbonne_u.components.AbstractComponent;
import fr.sorbonne_u.components.equipments.smartLighting.interfaces.SmartLightingInternalControlI;
import fr.sorbonne_u.components.equipments.smartLighting.interfaces.SmartLightingUserI;
import javassist.util.proxy.ProxyObject;

public class SmartLighting extends AbstractComponent implements SmartLightingUserI, SmartLightingInternalControlI {

    // ------------------------------------------------------------------------
    // Inner interfaces and types
    // ------------------------------------------------------------------------
    protected static enum SmartLightingState {
        ON, OFF, INCREASE, DECREASE
    }

    // ------------------------------------------------------------------------
    // Constants and variables
    // ------------------------------------------------------------------------
    public static final String REFLECTION_INBOUND_PORT_URI = "Smart-Lighting-RIP-URI";

    protected static final double MAX_POWER_LEVEL = 100.0;

    protected static final double STANDARD_TARGET_ILLUMINATION = 100.0;

    protected static final String USER_INBOUND_PORT_URI = "SMART-LIGHTING-USER-INBOUND-PORT-URI";

    protected static final String INTERNAL_CONTROL_INBOUND_PORT_URI = "SMART-LIGHTING-INTERNAL-CONTROL-INBOUND-PORT-URI";

    protected static final String EXTERNAL_CONTROL_INBOUND_PORT_URI = "SMART-LIGHTING-EXTERNAL-CONTROL-INBOUND-PORT-URI";

    protected static boolean VERBOSE = false;

    public static int X_RELATIVE_POSITION = 0;

    public static int Y_RELATIVE_POSITION = 0;

    public static final double		FAKE_CURRENT_TEMPERATURE = 10.0;

    protected SmartLightingState currentState;

    protected double currentPowerLevel;

    protected double targetIllumination;

    //TODO: Add the port here

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------
    protected SmartLighting() throws Exception {
        this(USER_INBOUND_PORT_URI, INTERNAL_CONTROL_INBOUND_PORT_URI, EXTERNAL_CONTROL_INBOUND_PORT_URI);
    }

    protected SmartLighting(String userInboundPortURI, String internalControlInboundPortURI, String externalControlInboundPortURI) throws Exception {
        super(1, 0);
    }

    protected void initialise(String smartLightingUserInboundPortURI, String smartLightingInternalControlInboundPortURI, String smartLightingExternalControlInboundPortURI) throws Exception {

        this.currentState = SmartLightingState.OFF;
        this.currentPowerLevel = MAX_POWER_LEVEL;
        this.targetIllumination = STANDARD_TARGET_ILLUMINATION;

    }

    @Override
    public boolean isOn() throws Exception {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void switchOn() throws Exception {
        // TODO Auto-generated method stub

    }

    @Override
    public void switchOff() throws Exception {
        // TODO Auto-generated method stub

    }

    @Override
    public void setTargetIllumination(double targetIllumination) throws Exception {
        // TODO Auto-generated method stub

    }

    @Override
    public double getMaxPowerLevel() throws Exception {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public double getCurrentPowerLevel() throws Exception {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public void setCurrentPowerLevel(double powerLevel) throws Exception {
        // TODO Auto-generated method stub

    }

    @Override
    public void IncreaseLightIntensity() throws Exception {
        // TODO Auto-generated method stub

    }

    @Override
    public void DecreaseLightIntensity() throws Exception {
        // TODO Auto-generated method stub

    }

    @Override
    public boolean isSwitchingAutomatically() throws Exception {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public double getTargetIllumination() throws Exception {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public double getCurrentIllumination() throws Exception {
        // TODO Auto-generated method stub
        return 0;
    }
}
