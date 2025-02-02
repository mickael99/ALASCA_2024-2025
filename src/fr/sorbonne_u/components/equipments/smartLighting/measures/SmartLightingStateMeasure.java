package fr.sorbonne_u.components.equipments.smartLighting.measures;

import fr.sorbonne_u.components.equipments.smartLighting.SmartLighting;
import fr.sorbonne_u.components.utils.Measure;
import fr.sorbonne_u.utils.aclocks.AcceleratedClock;

public class SmartLightingStateMeasure extends Measure<SmartLighting.SmartLightingState>
    implements SmartLightingMeasureI {
  // ------------------------------------------------------------------------
  // Constants
  // ------------------------------------------------------------------------

  private static final long serialVersionUID = 1L;

  // ------------------------------------------------------------------------
  // Constructors
  // ------------------------------------------------------------------------

  public SmartLightingStateMeasure(AcceleratedClock ac, SmartLighting.SmartLightingState data) {
    super(ac, data);
  }

  public SmartLightingStateMeasure(SmartLighting.SmartLightingState data) {
    super(data);
  }

  // ------------------------------------------------------------------------
  // Methods
  // ------------------------------------------------------------------------

  @Override
  public boolean isStateMeasure() {
    return true;
  }
}
