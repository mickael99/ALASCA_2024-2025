package fr.sorbonne_u.components.equipments.smartLightingE3.interfaces;

public interface SmartLightingUserI extends SmartLightingExternalControlI {

  public boolean isOn() throws Exception;

  public void switchOn() throws Exception;

  public void switchOff() throws Exception;

  public void setTargetIllumination(double targetIllumination) throws Exception;
}
