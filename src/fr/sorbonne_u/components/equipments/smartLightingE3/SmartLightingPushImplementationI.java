package fr.sorbonne_u.components.equipments.smartLightingE3;

import fr.sorbonne_u.components.interfaces.DataRequiredCI;

public interface SmartLightingPushImplementationI {
  public void receiveDataFromSmartLighting(DataRequiredCI.DataI sd);
}
