package fr.sorbonne_u.components.equipments.smartLighting.interfaces;

public interface SmartLightingInternalControlI extends SmartLightingIlluminationI {

  public void IncreaseLightIntensity() throws Exception;

  public void DecreaseLightIntensity() throws Exception;

  public boolean isSwitchingAutomatically() throws Exception;
}
