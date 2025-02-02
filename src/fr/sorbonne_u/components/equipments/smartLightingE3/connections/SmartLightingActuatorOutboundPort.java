package fr.sorbonne_u.components.equipments.smartLightingE3.connections;

import fr.sorbonne_u.components.ComponentI;
import fr.sorbonne_u.components.equipments.smartLightingE3.SmartLightingActuatorCI;
import fr.sorbonne_u.components.ports.AbstractOutboundPort;

public class SmartLightingActuatorOutboundPort extends AbstractOutboundPort
    implements SmartLightingActuatorCI {

  private static final long serialVersionUID = 1L;

  public SmartLightingActuatorOutboundPort(ComponentI owner) throws Exception {
    super(SmartLightingActuatorCI.class, owner);
  }

  public SmartLightingActuatorOutboundPort(String uri, ComponentI owner) throws Exception {
    super(uri, SmartLightingActuatorCI.class, owner);
  }

  @Override
  public void IncreaseLightIntensity() throws Exception {
    ((SmartLightingActuatorCI) this.connector).IncreaseLightIntensity();
  }

  @Override
  public void DescreseLightIntensity() throws Exception {
    ((SmartLightingActuatorCI) this.connector).DescreseLightIntensity();
  }

  @Override
  public boolean isSwitchingAutomatically() throws Exception {
    return ((SmartLightingActuatorCI) this.connector).isSwitchingAutomatically();
  }
}
