package fr.sorbonne_u.components.equipments.generator.mil;

import fr.sorbonne_u.components.equipments.generator.mil.events.AbstractGeneratorEvents;
import fr.sorbonne_u.devs_simulation.exceptions.NeoSim4JavaException;
import fr.sorbonne_u.devs_simulation.models.AtomicModel;
import fr.sorbonne_u.devs_simulation.models.time.Duration;
import fr.sorbonne_u.devs_simulation.simulators.interfaces.AtomicSimulatorI;
import fr.sorbonne_u.devs_simulation.utils.InvariantChecking;
import fr.sorbonne_u.devs_simulation.utils.StandardLogger;

import java.util.concurrent.TimeUnit;

public class GeneratorStateModel extends AtomicModel implements GeneratorOperationI {

    @Override
    public void activate() {

    }

    @Override
    public void stop() {

    }

    @Override
    public Duration timeAdvance() {
        return null;
    }

    // -------------------------------------------------------------------------
    // Constants and variables
    // -------------------------------------------------------------------------
    public static enum State {
        OFF,
        ON,
    }

    private static final long serialVersionUID = 1L;
    public static final String URI = GeneratorStateModel.class.getSimpleName();
    public static final String MIL_URI = GeneratorStateModel.class.getSimpleName() + "-MIL";
    public static final String MIL_RT_URI = GeneratorStateModel.class.getSimpleName() + "-MIL/SIL_RT";
    public static final String SIL_URI = GeneratorStateModel.class.getSimpleName() + "-MIL/SIL_RT";

    protected State currentState;
    protected AbstractGeneratorEvents lastRecieved;

    // -------------------------------------------------------------------------
    // Invairants
    // -------------------------------------------------------------------------

    protected static boolean glassBoxInvariants(GeneratorStateModel model) {
        assert model != null:
                new NeoSim4JavaException("The model is null!");

        boolean ret = true;
        return ret;
    }

    protected static boolean blackBoxInvariants(GeneratorStateModel instance) {
        assert instance != null:
                new NeoSim4JavaException("The instance is null!");

        boolean ret = true;
        ret &= InvariantChecking.checkBlackBoxInvariant(
                MIL_URI != null && !MIL_URI.isEmpty(),
                GeneratorStateModel.class,
                instance,
                "MIL_URI != null && !MIL_URI.isEmpty()");
        ret &= InvariantChecking.checkBlackBoxInvariant(
                MIL_RT_URI != null && !MIL_RT_URI.isEmpty(),
                GeneratorStateModel.class,
                instance,
                "MIL_RT_URI != null && !MIL_RT_URI.isEmpty()");
        ret &= InvariantChecking.checkBlackBoxInvariant(
                SIL_URI != null && !SIL_URI.isEmpty(),
                GeneratorStateModel.class,
                instance,
                "SIL_URI != null && !SIL_URI.isEmpty()");
        return ret;
    }

    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------

    public GeneratorStateModel(String uri,
                               TimeUnit simulatedTimeUnit,
                               AtomicSimulatorI simulationEngine
                               ) {
        super(uri, simulatedTimeUnit, simulationEngine);
        this.getSimulationEngine().setLogger(new StandardLogger());

        assert glassBoxInvariants(this) :
                new NeoSim4JavaException("GeneratorStateModel.glassBoxInvariants(this)");
        assert blackBoxInvariants(this) :
                new NeoSim4JavaException("GeneratorStateModel.blackBoxInvariants(this)");
    }

    // -------------------------------------------------------------------------
    // Methods
    // -------------------------------------------------------------------------



}
