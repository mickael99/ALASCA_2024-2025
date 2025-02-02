package fr.sorbonne_u.components.equipments.smartLighting.mil;

public interface SmartLightingOperationI {
  void setCurrentPower(double power, fr.sorbonne_u.devs_simulation.models.time.Time t);

  public SmartLightingStateModel.State getState();

  public void setState(SmartLightingStateModel.State state);
}
