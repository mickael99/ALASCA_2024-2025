package fr.sorbonne_u.components.equipments.smartLightingE3.connections;

import fr.sorbonne_u.components.ComponentI;
import fr.sorbonne_u.components.equipments.smartLightingE3.SmartLighting;
import fr.sorbonne_u.components.equipments.smartLightingE3.SmartLightingSensorDataCI;
import fr.sorbonne_u.components.equipments.smartLightingE3.interfaces.SmartLightingInternalControlI;
import fr.sorbonne_u.components.equipments.smartLightingE3.measures.SmartLightingCompoundMeasure;
import fr.sorbonne_u.components.equipments.smartLightingE3.measures.SmartLightingSensorData;
import fr.sorbonne_u.components.interfaces.DataOfferedCI;
import fr.sorbonne_u.components.ports.AbstractDataInboundPort;
import fr.sorbonne_u.components.utils.Measure;
import fr.sorbonne_u.exceptions.PreconditionException;

public class SmartLightingSensorDataInboundPort extends AbstractDataInboundPort implements SmartLightingSensorDataCI.HeaterSensorOfferedPullCI {
    // ------------------------------------------------------------------------
    // Constants
    // ------------------------------------------------------------------------

    private static final long serialVersionUID = 1L;

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------

    public SmartLightingSensorDataInboundPort(ComponentI owner) throws Exception {
        super(SmartLightingSensorDataCI.HeaterSensorOfferedPullCI.class, DataOfferedCI.PushCI.class, owner);
        assert owner instanceof SmartLightingInternalControlI :
                new PreconditionException("owner instanceof SmartLightingInternalControlI");
    }

    public SmartLightingSensorDataInboundPort(String uri, ComponentI owner) throws Exception {
        super(uri, SmartLightingSensorDataCI.HeaterSensorOfferedPullCI.class, DataOfferedCI.PushCI.class, owner);
        assert owner instanceof SmartLightingInternalControlI :
                new PreconditionException("owner instanceof SmartLightingInternalControlI");
    }

    // ------------------------------------------------------------------------
    // Methods
    // ------------------------------------------------------------------------

    @Override
    public SmartLightingSensorData<Measure<Boolean>> increaseLightIntensityPullSensor() throws Exception {
        return this.getOwner().handleRequest(
                owner -> {
                    return ((SmartLighting) owner).increaseLightIntensityPullSensor();
                });
    }

    @Override
    public SmartLightingSensorData<Measure<Boolean>> decreaseLightIntensityPullSensor() throws Exception {
        return this.getOwner().handleRequest(
                owner -> {
                    return ((SmartLighting) owner).decreaseLightIntensityPullSensor();
                });
    }

    @Override
    public SmartLightingSensorData<Measure<Double>> targetLightIntensityPullSensor() throws Exception {
        return this.getOwner().handleRequest(
                owner -> {
                    return ((SmartLighting) owner).targetLightIntensityPullSensor();
                });
    }

    @Override
    public SmartLightingSensorData<Measure<Double>> currentLightIntensityPullSensor() throws Exception {
        return this.getOwner().handleRequest(
                owner -> {
                    return ((SmartLighting) owner).currentLightIntensityPullSensor();
                });
    }

    @Override
    public void startLightIntensityPushSensor(long controlPeriod, java.util.concurrent.TimeUnit tu) throws Exception {
        this.getOwner().handleRequest(
                owner -> {
                    ((SmartLighting) owner).startLightIntensityPushSensor(controlPeriod, tu);
                    return null;
                });
    }

    @Override
    public DataOfferedCI.DataI get() throws Exception {
        return new SmartLightingSensorData<SmartLightingCompoundMeasure>(
                new SmartLightingCompoundMeasure(
                        this.targetLightIntensityPullSensor().getMeasure(),
                        this.currentLightIntensityPullSensor().getMeasure()
                )
        );
    }
}
