package fr.sorbonne_u.components.equipments.smartLightingE3.connections;

import fr.sorbonne_u.components.ComponentI;
import fr.sorbonne_u.components.equipments.smartLightingE3.SmartLightingActuatorCI;
import fr.sorbonne_u.components.equipments.smartLightingE3.interfaces.SmartLightingInternalControlI;
import fr.sorbonne_u.components.ports.AbstractInboundPort;
import fr.sorbonne_u.exceptions.PreconditionException;

public class SmartLightingActuatorInboundPort extends AbstractInboundPort
    implements SmartLightingActuatorCI {

  private static final long serialVersionUID = 1L;

  public SmartLightingActuatorInboundPort(ComponentI owner) throws Exception {
    super(SmartLightingActuatorCI.class, owner);
    assert owner instanceof SmartLightingInternalControlI
        : new PreconditionException("owner instanceof SmartLightingInternalControlI");
  }

  public SmartLightingActuatorInboundPort(String uri, ComponentI owner) throws Exception {
    super(uri, SmartLightingActuatorCI.class, owner);
    assert owner instanceof SmartLightingInternalControlI
        : new PreconditionException("owner instanceof SmartLightingInternalControlI");
  }

  @Override
  public void DescreseLightIntensity() throws Exception {
    this.getOwner()
        .handleRequest(
            owner -> {
              ((SmartLightingInternalControlI) owner).DecreaseLightIntensity();
              return null;
            });
  }

  @Override
  public void IncreaseLightIntensity() throws Exception {
    this.getOwner()
        .handleRequest(
            owner -> {
              ((SmartLightingInternalControlI) owner).IncreaseLightIntensity();
              return null;
            });
  }

  @Override
  public boolean isSwitchingAutomatically() throws Exception {
    return this.getOwner()
        .handleRequest(
            owner -> {
              return ((SmartLightingInternalControlI) owner).isSwitchingAutomatically();
            });
  }
}
