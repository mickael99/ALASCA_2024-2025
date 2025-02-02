package fr.sorbonne_u.components.equipments.smartLighting.connections;

import fr.sorbonne_u.components.connectors.DataConnector;
import fr.sorbonne_u.components.equipments.smartLighting.SmartLightingSensorDataCI;
import fr.sorbonne_u.components.equipments.smartLighting.measures.SmartLightingSensorData;
import fr.sorbonne_u.components.utils.Measure;

public class SmartLightingSensorDataConnector extends DataConnector
    implements SmartLightingSensorDataCI.SmartLightingSensorRequiredPullCI {

  // ------------------------------------------------------------------------
  // Methods
  // ------------------------------------------------------------------------
  @Override
  public SmartLightingSensorData<Measure<Boolean>> automaticModePullSensor() throws Exception {
    return ((SmartLightingSensorDataCI.SmartLightingSensorRequiredPullCI) this.offering)
        .automaticModePullSensor();
  }

  @Override
  public SmartLightingSensorData<Measure<Double>> targetIlluminationPullSensor() throws Exception {
    return ((SmartLightingSensorDataCI.SmartLightingSensorRequiredPullCI) this.offering)
        .targetIlluminationPullSensor();
  }

  @Override
  public SmartLightingSensorData<Measure<Double>> currentIlluminationPullSensor() throws Exception {
    return ((SmartLightingSensorDataCI.SmartLightingSensorRequiredPullCI) this.offering)
        .currentIlluminationPullSensor();
  }

  @Override
  public void startIlluminationPushSensor(long controlPeriod, java.util.concurrent.TimeUnit tu)
      throws Exception {
    ((SmartLightingSensorDataCI.SmartLightingSensorRequiredPullCI) this.offering)
        .startIlluminationPushSensor(controlPeriod, tu);
  }
}
