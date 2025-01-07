package fr.sorbonne_u.components.equipments.smartLightingE3.connections;

import fr.sorbonne_u.components.connectors.DataConnector;
import fr.sorbonne_u.components.equipments.smartLightingE3.SmartLightingSensorDataCI;
import fr.sorbonne_u.components.equipments.smartLightingE3.measures.SmartLightingSensorData;
import fr.sorbonne_u.components.utils.Measure;

public class SmartLightingSensorDataConnector extends DataConnector implements SmartLightingSensorDataCI.SmartLightingSensorRequiredPullCI {

    //------------------------------------------------------------------------
    // Methods
    //------------------------------------------------------------------------
    @Override
    public SmartLightingSensorData<Measure<Boolean>> automaticModePullSensor() throws Exception {
        return ((SmartLightingSensorDataCI.SmartLightingSensorRequiredPullCI)this.offering).automaticModePullSensor();
    }

    @Override
    public SmartLightingSensorData<Measure<Boolean>> decreaseLightIntensityPullSensor() throws Exception {
        return ((SmartLightingSensorDataCI.SmartLightingSensorRequiredPullCI)this.offering).decreaseLightIntensityPullSensor();
    }

    @Override
    public SmartLightingSensorData<Measure<Double>> targetLightIntensityPullSensor() throws Exception {
        return ((SmartLightingSensorDataCI.SmartLightingSensorRequiredPullCI)this.offering).targetLightIntensityPullSensor();
    }

    @Override
    public SmartLightingSensorData<Measure<Double>> currentLightIntensityPullSensor() throws Exception {
        return ((SmartLightingSensorDataCI.SmartLightingSensorRequiredPullCI)this.offering).currentLightIntensityPullSensor();
    }

    @Override
    public void startLightIntensityPushSensor(long controlPeriod, java.util.concurrent.TimeUnit tu) throws Exception {
        ((SmartLightingSensorDataCI.SmartLightingSensorRequiredPullCI)this.offering).startLightIntensityPushSensor(controlPeriod, tu);
    }
}
