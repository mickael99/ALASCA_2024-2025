package fr.sorbonne_u.components.equipments.smartLighting.connections;

import fr.sorbonne_u.components.ComponentI;
import fr.sorbonne_u.components.equipments.smartLighting.SmartLighting;
import fr.sorbonne_u.components.equipments.smartLighting.SmartLightingSensorDataCI;
import fr.sorbonne_u.components.equipments.smartLighting.interfaces.SmartLightingInternalControlI;
import fr.sorbonne_u.components.equipments.smartLighting.measures.SmartLightingCompoundMeasure;
import fr.sorbonne_u.components.equipments.smartLighting.measures.SmartLightingSensorData;
import fr.sorbonne_u.components.interfaces.DataOfferedCI;
import fr.sorbonne_u.components.ports.AbstractDataInboundPort;
import fr.sorbonne_u.components.utils.Measure;
import fr.sorbonne_u.exceptions.PreconditionException;

public class SmartLightingSensorDataInboundPort extends AbstractDataInboundPort
    implements SmartLightingSensorDataCI.SmartLightingSensorOfferedPullCI {
  // ------------------------------------------------------------------------
  // Constants
  // ------------------------------------------------------------------------

  private static final long serialVersionUID = 1L;

  // ------------------------------------------------------------------------
  // Constructors
  // ------------------------------------------------------------------------

  public SmartLightingSensorDataInboundPort(ComponentI owner) throws Exception {
    super(
        SmartLightingSensorDataCI.SmartLightingSensorOfferedPullCI.class,
        DataOfferedCI.PushCI.class,
        owner);
    assert owner instanceof SmartLightingInternalControlI
        : new PreconditionException("owner instanceof SmartLightingInternalControlI");
  }

  public SmartLightingSensorDataInboundPort(String uri, ComponentI owner) throws Exception {
    super(
        uri,
        SmartLightingSensorDataCI.SmartLightingSensorOfferedPullCI.class,
        DataOfferedCI.PushCI.class,
        owner);
    assert owner instanceof SmartLightingInternalControlI
        : new PreconditionException("owner instanceof SmartLightingInternalControlI");
  }

  // ------------------------------------------------------------------------
  // Methods
  // ------------------------------------------------------------------------

  @Override
  public SmartLightingSensorData<Measure<Boolean>> automaticModePullSensor() throws Exception {
    return this.getOwner()
        .handleRequest(
            owner -> {
              return ((SmartLighting) owner).automaticModePullSensor();
            });
  }

  public SmartLightingSensorData<Measure<Double>> targetIlluminationPullSensor() throws Exception {
    return this.getOwner()
        .handleRequest(
            owner -> {
              return ((SmartLighting) owner).targetIlluminationPullSensor();
            });
  }

  public SmartLightingSensorData<Measure<Double>> currentIlluminationPullSensor() throws Exception {
    return this.getOwner()
        .handleRequest(
            owner -> {
              return ((SmartLighting) owner).currentIlluminationPullSensor();
            });
  }

  public void startIlluminationPushSensor(long controlPeriod, java.util.concurrent.TimeUnit tu)
      throws Exception {
    this.getOwner()
        .handleRequest(
            owner -> {
              ((SmartLighting) owner).startIlluminationPushSensor(controlPeriod, tu);
              return null;
            });
  }

  @Override
  public DataOfferedCI.DataI get() throws Exception {
    return new SmartLightingSensorData<SmartLightingCompoundMeasure>(
        new SmartLightingCompoundMeasure(
            this.targetIlluminationPullSensor().getMeasure(),
            this.currentIlluminationPullSensor().getMeasure()));
  }
}
