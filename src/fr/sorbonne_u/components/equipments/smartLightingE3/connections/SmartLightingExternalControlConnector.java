package fr.sorbonne_u.components.equipments.smartLightingE3.connections;

import fr.sorbonne_u.components.connectors.AbstractConnector;
import fr.sorbonne_u.components.equipments.smartLighting.interfaces.SmartLightingExternalControlCI;

public class SmartLightingExternalControlConnector extends AbstractConnector
    implements SmartLightingExternalControlCI {

  /**
   * @see SmartLightingExternalControlCI#getMaxPowerLevel()
   */
  @Override
  public double getMaxPowerLevel() throws Exception {
    return ((SmartLightingExternalControlCI) this.offering).getMaxPowerLevel();
  }

  /**
   * @see SmartLightingExternalControlCI#getCurrentPowerLevel()
   */
  @Override
  public double getCurrentPowerLevel() throws Exception {
    return ((SmartLightingExternalControlCI) this.offering).getCurrentPowerLevel();
  }

  /**
   * @see SmartLightingExternalControlCI#setCurrentPowerLevel(double)
   */
  @Override
  public void setCurrentPowerLevel(double powerLevel) throws Exception {
    ((SmartLightingExternalControlCI) this.offering).setCurrentPowerLevel(powerLevel);
  }

  /**
   * @see
   *     fr.sorbonne_u.components.equipments.smartLighting.interfaces.SmartLightingIlluminationI#getTargetIllumination()
   */
  @Override
  public double getTargetIllumination() throws Exception {
    return ((SmartLightingExternalControlCI) this.offering).getTargetIllumination();
  }

  /**
   * @see
   *     fr.sorbonne_u.components.equipments.smartLighting.interfaces.SmartLightingIlluminationI#getCurrentIllumination()
   */
  @Override
  public double getCurrentIllumination() throws Exception {
    return ((SmartLightingExternalControlCI) this.offering).getCurrentIllumination();
  }
}
