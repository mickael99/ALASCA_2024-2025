package fr.sorbonne_u.components.equipments.smartLightingE3.connections;

import fr.sorbonne_u.components.ComponentI;
import fr.sorbonne_u.components.equipments.smartLighting.interfaces.SmartLightingExternalControlCI;
import fr.sorbonne_u.components.ports.AbstractOutboundPort;

public class SmartLightingExternalControlOutboundPort extends AbstractOutboundPort
    implements SmartLightingExternalControlCI {

  // ------------------------------------------------------------------------
  // Constants and variables
  // ------------------------------------------------------------------------

  private static final long serialVersionUID = 1L;

  // ------------------------------------------------------------------------
  // Constructors
  // ------------------------------------------------------------------------

  public SmartLightingExternalControlOutboundPort(String uri, ComponentI owner) throws Exception {
    super(uri, SmartLightingExternalControlCI.class, owner);
  }

  public SmartLightingExternalControlOutboundPort(ComponentI owner) throws Exception {
    super(SmartLightingExternalControlCI.class, owner);
  }

  // ------------------------------------------------------------------------
  // Methods
  // ------------------------------------------------------------------------

  /**
   * @see SmartLightingExternalControlCI#getMaxPowerLevel()
   */
  @Override
  public double getMaxPowerLevel() throws Exception {
    return ((SmartLightingExternalControlCI) this.getConnector()).getMaxPowerLevel();
  }

  /**
   * @see SmartLightingExternalControlCI#getCurrentPowerLevel()
   */
  @Override
  public double getCurrentPowerLevel() throws Exception {
    return ((SmartLightingExternalControlCI) this.getConnector()).getCurrentPowerLevel();
  }

  /**
   * @see SmartLightingExternalControlCI#setCurrentPowerLevel(double)
   */
  @Override
  public void setCurrentPowerLevel(double powerLevel) throws Exception {
    ((SmartLightingExternalControlCI) this.getConnector()).setCurrentPowerLevel(powerLevel);
  }

  /**
   * @see
   *     fr.sorbonne_u.components.equipments.smartLighting.interfaces.SmartLightingIlluminationI#getTargetIllumination()
   */
  @Override
  public double getTargetIllumination() throws Exception {
    return ((SmartLightingExternalControlCI) this.getConnector()).getTargetIllumination();
  }

  /**
   * @see
   *     fr.sorbonne_u.components.equipments.smartLighting.interfaces.SmartLightingIlluminationI#getCurrentIllumination()
   */
  @Override
  public double getCurrentIllumination() throws Exception {
    return ((SmartLightingExternalControlCI) this.getConnector()).getCurrentIllumination();
  }
}
