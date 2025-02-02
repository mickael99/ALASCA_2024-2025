package fr.sorbonne_u.components.equipments.smartLighting.connections;

import fr.sorbonne_u.components.ComponentI;
import fr.sorbonne_u.components.equipments.smartLighting.interfaces.SmartLightingInternalControlCI;
import fr.sorbonne_u.components.ports.AbstractOutboundPort;

public class SmartLightingInternalControlOutboundPort extends AbstractOutboundPort
    implements SmartLightingInternalControlCI {

  // ----------------------------------------------------------------------------
  // Constants and variables
  // ----------------------------------------------------------------------------

  private static final long serialVersionUID = 1L;

  // ----------------------------------------------------------------------------
  // Constructors
  // ----------------------------------------------------------------------------

  public SmartLightingInternalControlOutboundPort(String uri, ComponentI owner) throws Exception {
    super(uri, SmartLightingInternalControlCI.class, owner);
  }

  public SmartLightingInternalControlOutboundPort(ComponentI owner) throws Exception {
    super(SmartLightingInternalControlCI.class, owner);
  }

  // ----------------------------------------------------------------------------
  // Methods
  // ----------------------------------------------------------------------------

  /**
   * @see SmartLightingInternalControlCI#IncreaseLightIntensity()
   */
  @Override
  public void IncreaseLightIntensity() throws Exception {
    ((SmartLightingInternalControlCI) this.getConnector()).IncreaseLightIntensity();
  }

  /**
   * @see SmartLightingInternalControlCI#DecreaseLightIntensity()
   */
  @Override
  public void DecreaseLightIntensity() throws Exception {
    ((SmartLightingInternalControlCI) this.getConnector()).DecreaseLightIntensity();
  }

  /**
   * @see SmartLightingInternalControlCI#isSwitchingAutomatically()
   */
  @Override
  public boolean isSwitchingAutomatically() throws Exception {
    return ((SmartLightingInternalControlCI) this.getConnector()).isSwitchingAutomatically();
  }

  /**
   * @see
   *     fr.sorbonne_u.components.equipments.smartLighting.interfaces.SmartLightingIlluminationI#getTargetIllumination()
   */
  @Override
  public double getTargetIllumination() throws Exception {
    return ((SmartLightingInternalControlCI) this.getConnector()).getTargetIllumination();
  }

  /**
   * @see
   *     fr.sorbonne_u.components.equipments.smartLighting.interfaces.SmartLightingIlluminationI#getCurrentIllumination()
   */
  @Override
  public double getCurrentIllumination() throws Exception {
    return ((SmartLightingInternalControlCI) this.getConnector()).getCurrentIllumination();
  }
}
