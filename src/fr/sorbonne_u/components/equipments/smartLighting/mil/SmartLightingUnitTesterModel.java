package fr.sorbonne_u.components.equipments.smartLighting.mil;

import fr.sorbonne_u.components.equipments.smartLighting.mil.events.*;
import fr.sorbonne_u.devs_simulation.models.AtomicModel;
import fr.sorbonne_u.devs_simulation.models.annotations.ModelExternalEvents;
import fr.sorbonne_u.devs_simulation.models.events.EventI;
import fr.sorbonne_u.devs_simulation.models.time.Duration;
import fr.sorbonne_u.devs_simulation.models.time.Time;
import fr.sorbonne_u.devs_simulation.simulators.interfaces.AtomicSimulatorI;
import fr.sorbonne_u.devs_simulation.simulators.interfaces.SimulationReportI;
import fr.sorbonne_u.devs_simulation.utils.StandardLogger;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

@ModelExternalEvents(exported = {
        TurnOffSmartLighting.class,
        IncreaseLighting.class,
        DecreaseLighting.class,
        StopAdjustingLighting.class,
        TurnOnSmartLighting.class,
        SetPowerSmartLighting.class
})
//-------------------------------------------------------------------------
public class SmartLightingUnitTesterModel extends AtomicModel {

    // ------------------------------------------------------------------------
    // Constants
    // ------------------------------------------------------------------------

    private static final long serialVersionUID = 1L;
    public static final String URI = SmartLightingUnitTesterModel.class.getSimpleName();
    protected int step;

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------
    public SmartLightingUnitTesterModel(String uri,
                                        TimeUnit simulatedTimeUnit,
                                        AtomicSimulatorI modelDescription
                                       ) throws Exception {
        super(uri, simulatedTimeUnit, modelDescription);
        this.getSimulationEngine().setLogger(new StandardLogger());
    }

    // -------------------------------------------------------------------------
    // DEVS simulation protocol
    // -------------------------------------------------------------------------

    /**
     * @see fr.sorbonne_u.devs_simulation.models.AtomicModel#initialiseState(fr.sorbonne_u.devs_simulation.models.time.Time)
     */
    @Override
    public void initialiseState(Time initialTime) {
        super.initialiseState(initialTime);
        this.step = 1;
        this.getSimulationEngine().toggleDebugMode();
        this.logMessage("simulation begins.\n");
    }

    /**
     * @see fr.sorbonne_u.devs_simulation.models.interfaces.AtomicModelI#output()
     */
    @Override
    public ArrayList<EventI> output()
    {
        // Simple way to implement a test scenario. Here each step generates
        // an event sent to the other models in the standard order.
        if (this.step > 0 && this.step < 10) {
            ArrayList<EventI> ret = new ArrayList<EventI>();
            switch (this.step) {
                case 1:
                    ret.add(new TurnOnSmartLighting(this.getTimeOfNextEvent()));
                    break;
                case 2:
                    ret.add(new IncreaseLighting(this.getTimeOfNextEvent()));
                    break;
                case 3:
                    ret.add(new DecreaseLighting(this.getTimeOfNextEvent()));
                    break;
                case 4:
                    ret.add(new StopAdjustingLighting(this.getTimeOfNextEvent()));
                    break;
                case 5:
                    ret.add(new SetPowerSmartLighting(this.getTimeOfNextEvent(),
                                                      new SetPowerSmartLighting.PowerValue(500.0)));
                    break;
                case 6:
                    ret.add(new SetPowerSmartLighting(this.getTimeOfNextEvent(),
                                               new SetPowerSmartLighting.PowerValue(300.0)));
                    break;
                case 7:
                    ret.add(new SetPowerSmartLighting(this.getTimeOfNextEvent(),
                                               new SetPowerSmartLighting.PowerValue(100.0)));
                    break;
                case 8:
                    ret.add(new SetPowerSmartLighting(this.getTimeOfNextEvent(),
                                               new SetPowerSmartLighting.PowerValue(50.0)));
                    break;
                case 9:
                    ret.add(new TurnOffSmartLighting(this.getTimeOfNextEvent()));
                    break;
            }
            return ret;
        } else {
            return null;
        }
    }

    /**
     * @see fr.sorbonne_u.devs_simulation.models.interfaces.ModelI#timeAdvance()
     */
    @Override
    public Duration timeAdvance()
    {
        if (this.step < 10) {
            return new Duration(1.0, this.getSimulatedTimeUnit());
        } else {
            return Duration.INFINITY;
        }
    }

    /**
     * @see fr.sorbonne_u.devs_simulation.models.AtomicModel#userDefinedInternalTransition(fr.sorbonne_u.devs_simulation.models.time.Duration)
     */
    @Override
    public void			userDefinedInternalTransition(Duration elapsedTime)
    {
        super.userDefinedInternalTransition(elapsedTime);

        // advance to the next step in the scenario
        this.step++;
    }

    /**
     * @see fr.sorbonne_u.devs_simulation.models.interfaces.ModelI#endSimulation(fr.sorbonne_u.devs_simulation.models.time.Time)
     */
    @Override
    public void			endSimulation(Time endTime)
    {
        this.logMessage("simulation ends.\n");
        super.endSimulation(endTime);
    }

    /**
     * @see fr.sorbonne_u.devs_simulation.models.interfaces.ModelI#getFinalReport()
     */
    @Override
    public SimulationReportI getFinalReport()
    {
        return null;
    }
}
// ------------------------------------------------------------------------
