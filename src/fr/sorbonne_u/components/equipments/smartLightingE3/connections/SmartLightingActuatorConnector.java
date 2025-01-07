package fr.sorbonne_u.components.equipments.smartLightingE3.connections;

import fr.sorbonne_u.components.connectors.AbstractConnector;
import fr.sorbonne_u.components.equipments.smartLightingE3.SmartLightingActuatorCI;

public class SmartLightingActuatorConnector extends AbstractConnector implements SmartLightingActuatorCI {
    @Override
    public void IncreaseLightIntensity() throws Exception {
        ((SmartLightingActuatorCI)this.offering).IncreaseLightIntensity();
    }

    @Override
    public void DescreseLightIntensity() throws Exception {
        ((SmartLightingActuatorCI)this.offering).DescreseLightIntensity();
    }

    @Override
    public boolean isSwitchingAutomatically() throws Exception {
        return ((SmartLightingActuatorCI)this.offering).isSwitchingAutomatically();
    }
}
//------------------------------------------------------------------------
