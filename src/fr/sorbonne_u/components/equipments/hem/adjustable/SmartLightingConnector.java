package fr.sorbonne_u.components.equipments.hem.adjustable;

import fr.sorbonne_u.components.connectors.AbstractConnector;
import fr.sorbonne_u.components.equipments.smartLighting.interfaces.SmartLightingExternalControlCI;
import fr.sorbonne_u.exceptions.PostconditionException;
import fr.sorbonne_u.exceptions.PreconditionException;

public class SmartLightingConnector extends AbstractConnector implements AdjustableCI {

    // -------------------------------------------------------------------------
    // Constants and variables
    // -------------------------------------------------------------------------

    protected static final int MAX_MODE = 3;
    protected static final double MIN_ADMISSIBLE_ILLU = 0;
    protected static final double MAX_ADMISSIBLE_DELTA = 5.0;

    protected int currentMode;
    protected boolean isSuspended;

    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------

    public SmartLightingConnector() {
        super();

        this.currentMode = MAX_MODE;
        this.isSuspended = false;
    }

    // -------------------------------------------------------------------------
    // Methods
    // -------------------------------------------------------------------------

    @Override
    public int maxMode() throws Exception {
        return MAX_MODE;
    }

    protected void computeAndSetNewPowerLevel(int newMode) throws Exception {
        assert	newMode >= 0 && newMode <= MAX_MODE :
                new PreconditionException("newMode >= 0 && newMode <= MAX_MODE");

        double maxPowerLevel =
                ((SmartLightingExternalControlCI)this.offering).getCurrentPowerLevel();
        double newPowerLevel =
                (newMode - 1) * maxPowerLevel/(MAX_MODE - 1);
        ((SmartLightingExternalControlCI)this.offering).setCurrentPowerLevel(newPowerLevel);

    }

    @Override
    public boolean upMode() throws Exception {
        assert	!this.suspended() : new PreconditionException("!suspended()");
        assert	this.currentMode() <= MAX_MODE :
                new PreconditionException("currentMode() < MAX_MODE");

        try {
            this.computeAndSetNewPowerLevel(this.currentMode + 1);
            this.currentMode++;
        } catch(Exception e) {
            return false;
        }
        return true;
    }

    @Override
    public boolean downMode() throws Exception {
        assert	!this.suspended() : new PreconditionException("!suspended()");
        assert	this.currentMode() > 0 :
                new PreconditionException("currentMode() > 0");

        try {
            this.computeAndSetNewPowerLevel(this.currentMode - 1);
            this.currentMode--;
        } catch(Exception e) {
            return false;
        }
        return true;
    }

    @Override
    public boolean setMode(int modeIndex) throws Exception {
        assert	!this.suspended() : new PreconditionException("!suspended()");
        assert	modeIndex > 0 && modeIndex <= this.maxMode() :
                new PreconditionException(
                        "modeIndex > 0 && modeIndex <= maxMode()");

        try {
            this.computeAndSetNewPowerLevel(modeIndex);
            this.currentMode = modeIndex;
        } catch(Exception e) {
            return false;
        }
        return true;
    }

    @Override
    public int currentMode() throws Exception {
        if (this.suspended())
            return 0;
        else
            return this.currentMode;
    }

    @Override
    public boolean suspended() throws Exception {
        return this.isSuspended;
    }

    @Override
    public boolean suspend() throws Exception {
        assert	!this.suspended() : new PreconditionException("!suspended()");

        try {
            ((SmartLightingExternalControlCI)this.offering).setCurrentPowerLevel(0.0);
            this.isSuspended = true;
        } catch(Exception e) {
            return false;
        }
        return true;
    }

    @Override
    public boolean resume() throws Exception {
        assert	this.suspended() : new PreconditionException("suspended()");

        try {
            this.computeAndSetNewPowerLevel(this.currentMode);
            this.isSuspended = false;
        } catch(Exception e) {
            return false;
        }
        return true;
    }

    @Override
    public double emergency() throws Exception {
        assert	this.suspended() : new PreconditionException("suspended()");

        double currentIllumination =
                ((SmartLightingExternalControlCI)this.offering).getCurrentIllumination();
        double targetIllumination =
                ((SmartLightingExternalControlCI)this.offering).getTargetIllumination();
        double delta = Math.abs(targetIllumination - currentIllumination);
        double ret = -1.0;
        if (currentIllumination < SmartLightingConnector.MIN_ADMISSIBLE_ILLU ||
            delta >= SmartLightingConnector.MAX_ADMISSIBLE_DELTA) {
            ret = 1.0;
        } else {
            ret = delta/SmartLightingConnector.MAX_ADMISSIBLE_DELTA;
        }

        assert	ret >= 0.0 && ret <= 1.0 :
                new PostconditionException("return >= 0.0 && return <= 1.0");

        return ret;
    }
}