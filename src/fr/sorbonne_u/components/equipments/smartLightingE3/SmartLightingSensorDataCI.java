package fr.sorbonne_u.components.equipments.smartLightingE3;

import fr.sorbonne_u.components.equipments.smartLightingE3.measures.SmartLightingSensorData;
import fr.sorbonne_u.components.interfaces.DataOfferedCI;
import fr.sorbonne_u.components.interfaces.DataRequiredCI;
import fr.sorbonne_u.components.interfaces.OfferedCI;
import fr.sorbonne_u.components.interfaces.RequiredCI;
import fr.sorbonne_u.components.utils.Measure;

import java.util.concurrent.TimeUnit;

public interface SmartLightingSensorDataCI extends DataOfferedCI, DataRequiredCI {
    //------------------------------------------------------------------------
    // Inner interfaces and Types
    //------------------------------------------------------------------------

    public static interface SmartLightingSensorCI extends OfferedCI, RequiredCI {
        //------------------------------------------------------------------------
        // Methods
        //------------------------------------------------------------------------

        public SmartLightingSensorData<Measure<Boolean>> automaticModePullSensor() throws Exception;

        public SmartLightingSensorData<Measure<Double>> targetLightIntensityPullSensor() throws Exception;

        public SmartLightingSensorData<Measure<Double>> currentLightIntensityPullSensor() throws Exception;

        public void startLightIntensityPushSensor(long controlPeriod, TimeUnit tu) throws Exception;
    }

    public static interface SmartLightingSensorRequiredPullCI extends SmartLightingSensorCI, DataRequiredCI.PullCI {
    }

    public static interface SmartLightingSensorOfferedPullCI extends SmartLightingSensorCI, DataOfferedCI.PullCI {
    }
}
