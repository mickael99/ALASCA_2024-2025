package fr.sorbonne_u.components.equipments.smartLightingE3.measures;

import fr.sorbonne_u.components.utils.MeasureI;

public interface SmartLightingMeasureI extends MeasureI {
  default boolean isStateMeasure() {
    return false;
  }

  default boolean isLightIntensityMeasure() {
    return false;
  }
}
