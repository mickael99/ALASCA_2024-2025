package fr.sorbonne_u.components.equipments.smartLighting.interfaces;

public interface SmartLightingExternalControlI extends SmartLightingIlluminationI {

    public double getMaxPowerLevel() throws Exception;

    public double getCurrentPowerLevel() throws Exception;

    public void setCurrentPowerLevel(double powerLevel) throws Exception;


}
