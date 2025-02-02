package fr.sorbonne_u.components.equipments.smartLighting.measures;

import fr.sorbonne_u.components.utils.CompoundMeasure;
import fr.sorbonne_u.components.utils.Measure;
import fr.sorbonne_u.components.utils.MeasureI;
import fr.sorbonne_u.components.utils.MeasurementUnit;
import fr.sorbonne_u.exceptions.ImplementationInvariantException;
import fr.sorbonne_u.exceptions.InvariantChecking;
import fr.sorbonne_u.exceptions.InvariantException;
import fr.sorbonne_u.exceptions.PreconditionException;

public class SmartLightingCompoundMeasure extends CompoundMeasure implements SmartLightingMeasureI {

  // ------------------------------------------------------------------------
  // Constants and variables
  // ------------------------------------------------------------------------

  private static final long serialVersionUID = 1L;
  protected static final int TARGET_LIGHT_INTENSITY_INDEX = 0;
  protected static final int CURRENT_LIGHT_INTENSITY_INDEX = 1;

  // ------------------------------------------------------------------------
  // Invariant
  // ------------------------------------------------------------------------
  protected static boolean glassBoxInvariants(SmartLightingCompoundMeasure instance) {
    assert instance != null : new PreconditionException("instance != null");

    boolean ret = true;
    ret &=
        InvariantChecking.checkGlassBoxInvariant(
            TARGET_LIGHT_INTENSITY_INDEX >= 0,
            SmartLightingCompoundMeasure.class,
            instance,
            "TARGET_LIGHT_INTENSITY_INDEX >= 0");
    ret &=
        InvariantChecking.checkGlassBoxInvariant(
            CURRENT_LIGHT_INTENSITY_INDEX >= 0,
            SmartLightingCompoundMeasure.class,
            instance,
            "CURRENT_LIGHT_INTENSITY_INDEX >= 0");
    ret &=
        InvariantChecking.checkGlassBoxInvariant(
            TARGET_LIGHT_INTENSITY_INDEX != CURRENT_LIGHT_INTENSITY_INDEX,
            SmartLightingCompoundMeasure.class,
            instance,
            "TARGET_LIGHT_INTENSITY_INDEX != CURRENT_LIGHT_INTENSITY_INDEX");
    return ret;
  }

  protected static boolean blacBoxInvariants(SmartLightingCompoundMeasure instance) {
    assert instance != null : new PreconditionException("instance != null");

    return true;
  }

  public SmartLightingCompoundMeasure(
      Measure<Double> targetLightIntensity, Measure<Double> currentLightIntensity) {
    super(new MeasureI[] {targetLightIntensity, currentLightIntensity});

    assert targetLightIntensity.getData() == this.getTargetLightIntensity()
        : new PreconditionException(
            "targetLightIntensity.getData() == this.getTargetLightIntensity()");
    assert targetLightIntensity.getMeasurementUnit()
            == this.getCurrentLightIntensityMeasurementUnit()
        : new PreconditionException(
            "targetLightIntensity.getMeasurementUnit() == this.getCurrentLightIntensityMeasurementUnit()");
    assert currentLightIntensity.getData() == this.getCurrentLightIntensity()
        : new PreconditionException(
            "currentLightIntensity.getData() == this.getCurrentLightIntensity()");
    assert currentLightIntensity.getMeasurementUnit()
            == this.getCurrentLightIntensityMeasurementUnit()
        : new PreconditionException(
            "currentLightIntensity.getMeasurementUnit() == this.getCurrentLightIntensityMeasurementUnit()");

    assert glassBoxInvariants(this)
        : new ImplementationInvariantException("glassBoxInvariants(HeatPumpCompoundMeasure)");
    assert blacBoxInvariants(this)
        : new InvariantException("blacBoxInvariants(HeatPumpCompoundMeasure)");
  }

  @Override
  public boolean isLightIntensityMeasure() {
    return true;
  }

  @SuppressWarnings("unchecked")
  public double getTargetLightIntensity() {
    return ((Measure<Double>) this.getMeasure(TARGET_LIGHT_INTENSITY_INDEX)).getData();
  }

  @SuppressWarnings("unchecked")
  public double getCurrentLightIntensity() {
    return ((Measure<Double>) this.getMeasure(CURRENT_LIGHT_INTENSITY_INDEX)).getData();
  }

  @SuppressWarnings("unchecked")
  public MeasurementUnit getCurrentLightIntensityMeasurementUnit() {
    return ((Measure<Double>) this.getMeasure(TARGET_LIGHT_INTENSITY_INDEX)).getMeasurementUnit();
  }
}
