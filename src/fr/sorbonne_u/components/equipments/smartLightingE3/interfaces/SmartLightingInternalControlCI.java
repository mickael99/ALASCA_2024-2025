package fr.sorbonne_u.components.equipments.smartLightingE3.interfaces;

import fr.sorbonne_u.components.interfaces.OfferedCI;
import fr.sorbonne_u.components.interfaces.RequiredCI;

public interface SmartLightingInternalControlCI
    extends SmartLightingInternalControlI, OfferedCI, RequiredCI {

  /**
   * @see SmartLightingInternalControlI#IncreaseLightIntensity()
   */
  @Override
  public void IncreaseLightIntensity() throws Exception;

  /**
   * @see SmartLightingInternalControlI#DecreaseLightIntensity()
   */
  @Override
  public void DecreaseLightIntensity() throws Exception;

  /**
   * @see SmartLightingInternalControlI#isSwitchingAutomatically()
   */
  @Override
  public boolean isSwitchingAutomatically() throws Exception;

  /**
   * @see SmartLightingIlluminationI#getTargetIllumination()
   */
  @Override
  public double getTargetIllumination() throws Exception;

  /**
   * @see SmartLightingIlluminationI#getCurrentIllumination()
   */
  @Override
  public double getCurrentIllumination() throws Exception;
}
