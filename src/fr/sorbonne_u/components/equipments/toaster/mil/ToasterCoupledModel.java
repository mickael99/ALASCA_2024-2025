package fr.sorbonne_u.components.equipments.toaster.mil;

import fr.sorbonne_u.devs_simulation.models.CoupledModel;
import fr.sorbonne_u.devs_simulation.models.events.EventI;
import fr.sorbonne_u.devs_simulation.models.events.EventSink;
import fr.sorbonne_u.devs_simulation.models.events.EventSource;
import fr.sorbonne_u.devs_simulation.models.events.ReexportedEvent;
import fr.sorbonne_u.devs_simulation.models.interfaces.ModelI;
import fr.sorbonne_u.devs_simulation.simulators.interfaces.CoordinatorI;
import fr.sorbonne_u.devs_simulation.hioa.models.vars.StaticVariableDescriptor;
import fr.sorbonne_u.devs_simulation.hioa.models.vars.VariableSink;
import fr.sorbonne_u.devs_simulation.hioa.models.vars.VariableSource;

import java.util.Map;
import java.util.concurrent.TimeUnit;

public class ToasterCoupledModel extends CoupledModel {
    // -------------------------------------------------------------------------
    // Constants and variables
    // -------------------------------------------------------------------------

    private static final long serialVersionUID = 1L;
    public static final String URI = ToasterCoupledModel.class.getSimpleName();

    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------

    public	ToasterCoupledModel(
            String uri,
            TimeUnit simulatedTimeUnit,
            CoordinatorI simulationEngine,
            ModelI[] submodels,
            Map<Class<? extends EventI>,
                    EventSink[]> imported,
            Map<Class<? extends EventI>, ReexportedEvent> reexported,
            Map<EventSource, EventSink[]> connections
    ) throws Exception
    {
        super(uri, simulatedTimeUnit, simulationEngine, submodels,
                imported, reexported, connections);
    }

    public	ToasterCoupledModel(
            String uri,
            TimeUnit simulatedTimeUnit,
            CoordinatorI simulationEngine,
            ModelI[] submodels,
            Map<Class<? extends EventI>, EventSink[]> imported,
            Map<Class<? extends EventI>, ReexportedEvent> reexported,
            Map<EventSource, EventSink[]> connections,
            Map<StaticVariableDescriptor, VariableSink[]> importedVars,
            Map<VariableSource, StaticVariableDescriptor> reexportedVars,
            Map<VariableSource, VariableSink[]> bindings
    ) throws Exception
    {
        super(uri, simulatedTimeUnit, simulationEngine, submodels,
                imported, reexported, connections,
                importedVars, reexportedVars, bindings);
    }
}
