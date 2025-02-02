package fr.sorbonne_u.components.equipments.smartLighting.measures;

import fr.sorbonne_u.components.interfaces.DataOfferedCI;
import fr.sorbonne_u.components.interfaces.DataRequiredCI;
import fr.sorbonne_u.components.utils.MeasureI;
import fr.sorbonne_u.components.utils.SensorData;

public class SmartLightingSensorData<T extends MeasureI> extends SensorData<T>
    implements DataOfferedCI.DataI, DataRequiredCI.DataI {
  // ------------------------------------------------------------------------
  // Constants
  // ------------------------------------------------------------------------

  private static final long serialVersionUID = 1L;

  // ------------------------------------------------------------------------
  // Constructors
  // ------------------------------------------------------------------------
  public SmartLightingSensorData(T m) {
    super(m);
  }
}
