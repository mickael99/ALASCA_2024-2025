package fr.sorbonne_u.components.equipments.toaster.mil;

import fr.sorbonne_u.components.equipments.toaster.mil.events.SetToasterBrowningLevel;
import fr.sorbonne_u.components.equipments.toaster.mil.events.TurnOffToaster;
import fr.sorbonne_u.components.equipments.toaster.mil.events.TurnOnToaster;
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

@ModelExternalEvents(imported = {
        TurnOnToaster.class,
        TurnOffToaster.class,
        SetToasterBrowningLevel.class
})
public class ToasterUnitTesterModel extends AtomicModel {

    // -------------------------------------------------------------------------
    // Constants and variables
    // -------------------------------------------------------------------------
    private static final long serialVersionUID = 1L;
    public static final String URI = ToasterUnitTesterModel.class.getSimpleName();
    public int step;

    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------
    public ToasterUnitTesterModel(String uri, TimeUnit simulatedTimeUnit, AtomicSimulatorI simulatorEngine) throws Exception {
        super(uri, simulatedTimeUnit, simulatorEngine);
        this.getSimulationEngine().setLogger(new StandardLogger());
    }

    // -------------------------------------------------------------------------
    // DEVS simulation protocol
    // -------------------------------------------------------------------------

    @Override
    public void			initialiseState(Time initialTime)
    {
        super.initialiseState(initialTime);
        this.step = 1;
        this.getSimulationEngine().toggleDebugMode();
        this.logMessage("simulation begins.\n");
    }

    @Override
    public ArrayList<EventI> output() {
        if (this.step > 0 && this.step < 10) {
            ArrayList<EventI> ret = new ArrayList<EventI>();
            switch (this.step) {
                case 1:
                    ret.add(new TurnOnToaster(this.getCurrentStateTime()));
                    break;
                case 2:
                    ret.add(new SetToasterBrowningLevel(this.getCurrentStateTime(), new SetToasterBrowningLevel.BrowningLevelValue(ToasterElectricityModel.ToasterBrowningLevel.DEFROST)));
                    break;
                case 3:
                    ret.add(new SetToasterBrowningLevel(this.getCurrentStateTime(), new SetToasterBrowningLevel.BrowningLevelValue(ToasterElectricityModel.ToasterBrowningLevel.LOW)));
                    break;
                case 4:
                    ret.add(new SetToasterBrowningLevel(this.getCurrentStateTime(), new SetToasterBrowningLevel.BrowningLevelValue(ToasterElectricityModel.ToasterBrowningLevel.MEDIUM)));
                    break;
                case 5:
                    ret.add(new SetToasterBrowningLevel(this.getCurrentStateTime(), new SetToasterBrowningLevel.BrowningLevelValue(ToasterElectricityModel.ToasterBrowningLevel.HIGH)));
                    break;
                case 6:
                    ret.add(new TurnOffToaster(this.getCurrentStateTime()));
                    break;
            }
            return ret;
        } else {
            return null;
        }
    }

    @Override
    public Duration timeAdvance()
    {
        // As long as events have to be created and sent, the next internal
        // transition is set at one second later, otherwise, no more internal
        // transitions are triggered (delay = infinity).
        if (this.step < 7) {
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

    // -------------------------------------------------------------------------
    // Optional DEVS simulation protocol: simulation report
    // -------------------------------------------------------------------------

    /**
     * @see fr.sorbonne_u.devs_simulation.models.interfaces.ModelI#getFinalReport()
     */
    @Override
    public SimulationReportI getFinalReport()
    {
        return null;
    }
}
