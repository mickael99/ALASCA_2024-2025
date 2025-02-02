package fr.sorbonne_u.components.equipments.smartLighting.connections;

import fr.sorbonne_u.components.ComponentI;
import fr.sorbonne_u.components.equipments.smartLighting.SmartLightingSensorDataCI;
import fr.sorbonne_u.components.equipments.smartLighting.measures.SmartLightingSensorData;
import fr.sorbonne_u.components.interfaces.DataRequiredCI;
import fr.sorbonne_u.components.ports.AbstractDataOutboundPort;
import fr.sorbonne_u.components.utils.Measure;

public class SmartLightingSensorDataOutboundPort extends AbstractDataOutboundPort
    implements SmartLightingSensorDataCI.SmartLightingSensorRequiredPullCI {
  // ------------------------------------------------------------------------
  // Constants
  // ------------------------------------------------------------------------

  private static final long serialVersionUID = 1L;

  // ------------------------------------------------------------------------
  // Constructors
  // ------------------------------------------------------------------------

  public SmartLightingSensorDataOutboundPort(ComponentI owner) throws Exception {
    super(DataRequiredCI.PullCI.class, DataRequiredCI.PushCI.class, owner);
  }

  public SmartLightingSensorDataOutboundPort(String uri, ComponentI owner) throws Exception {
    super(
        uri,
        SmartLightingSensorDataCI.SmartLightingSensorRequiredPullCI.class,
        DataRequiredCI.PushCI.class,
        owner);
  }

  // ------------------------------------------------------------------------
  // Methods
  // ------------------------------------------------------------------------

  @Override
  public SmartLightingSensorData<Measure<Boolean>> automaticModePullSensor() throws Exception {
    return ((SmartLightingSensorDataCI.SmartLightingSensorRequiredPullCI) this.getConnector())
        .automaticModePullSensor();
  }

  @Override
  public SmartLightingSensorData<Measure<Double>> targetIlluminationPullSensor() throws Exception {
    return ((SmartLightingSensorDataCI.SmartLightingSensorRequiredPullCI) this.getConnector())
        .targetIlluminationPullSensor();
  }

  @Override
  public SmartLightingSensorData<Measure<Double>> currentIlluminationPullSensor() throws Exception {
    return ((SmartLightingSensorDataCI.SmartLightingSensorRequiredPullCI) this.getConnector())
        .currentIlluminationPullSensor();
  }

  @Override
  public void startIlluminationPushSensor(long controlPeriod, java.util.concurrent.TimeUnit tu)
      throws Exception {
    ((SmartLightingSensorDataCI.SmartLightingSensorRequiredPullCI) this.getConnector())
        .startIlluminationPushSensor(controlPeriod, tu);
  }

  @Override
  public void receive(DataRequiredCI.DataI d) throws Exception {
    // TODO: add method receive() after Controller implementation

  }
}
