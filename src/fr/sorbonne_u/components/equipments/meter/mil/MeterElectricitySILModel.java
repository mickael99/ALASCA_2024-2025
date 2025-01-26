package fr.sorbonne_u.components.equipments.meter.mil;

import fr.sorbonne_u.devs_simulation.models.time.Duration;
import fr.sorbonne_u.devs_simulation.simulators.interfaces.AtomicSimulatorI;

import java.util.concurrent.TimeUnit;

public class MeterElectricitySILModel extends MeterElectricityModel{

    //------------------------------------------------------------
    // Constants and variables
    //------------------------------------------------------------

    private static final long serialVersionUID = 1L;
    public static final String SIL_URI = MeterElectricitySILModel.class.getSimpleName() + "-SIL";

    //------------------------------------------------------------
    // Constructor
    //------------------------------------------------------------

    public	MeterElectricitySILModel(
            String uri,
            TimeUnit simulatedTimeUnit,
            AtomicSimulatorI simulationEngine
                                                          ) throws Exception
    {
        super(uri, simulatedTimeUnit, simulationEngine);
    }

    //------------------------------------------------------------
    // Methods
    //------------------------------------------------------------

    @Override
    public void			userDefinedInternalTransition(Duration elapsedTime)
    {
        super.userDefinedInternalTransition(elapsedTime);

    }
}
