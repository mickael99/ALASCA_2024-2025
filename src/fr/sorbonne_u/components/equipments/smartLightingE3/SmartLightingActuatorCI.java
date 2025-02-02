package fr.sorbonne_u.components.equipments.smartLightingE3;

import fr.sorbonne_u.components.interfaces.OfferedCI;
import fr.sorbonne_u.components.interfaces.RequiredCI;

public interface SmartLightingActuatorCI extends OfferedCI, RequiredCI {
  public void IncreaseLightIntensity() throws Exception;

  public void DescreseLightIntensity() throws Exception;

  public boolean isSwitchingAutomatically() throws Exception;
}
