package fr.sorbonne_u.components.equipments.smartLightingE3.interfaces;

public interface SmartLightingInternalControlI extends SmartLightingIlluminationI {

  public void IncreaseLightIntensity() throws Exception;

  public void DecreaseLightIntensity() throws Exception;

  public boolean isSwitchingAutomatically() throws Exception;
}
