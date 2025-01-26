package fr.sorbonne_u.components.equipments.meter.mil;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import fr.sorbonne_u.devs_simulation.hioa.models.vars.StaticVariableDescriptor;
import fr.sorbonne_u.devs_simulation.hioa.models.vars.VariableSink;
import fr.sorbonne_u.devs_simulation.hioa.models.vars.VariableSource;
import fr.sorbonne_u.devs_simulation.models.CoupledModel;
import fr.sorbonne_u.devs_simulation.models.events.EventI;
import fr.sorbonne_u.devs_simulation.models.events.EventSink;
import fr.sorbonne_u.devs_simulation.models.events.EventSource;
import fr.sorbonne_u.devs_simulation.models.events.ReexportedEvent;
import fr.sorbonne_u.devs_simulation.models.interfaces.ModelI;
import fr.sorbonne_u.devs_simulation.simulators.interfaces.CoordinatorI;

public class MeterCoupledModel extends CoupledModel {
	
	private static final long serialVersionUID = 1L;
    public static final String URI = MeterCoupledModel.class.getSimpleName();
	public static final String	MIL_URI = MeterCoupledModel.class.getSimpleName() + "-MIL";
	public static final String	MIL_RT_URI = MeterCoupledModel.class.getSimpleName() + "-MIL_RT";
	public static final String	SIL_URI = MeterCoupledModel.class.getSimpleName() + "-SIL";

    public MeterCoupledModel(String uri, TimeUnit simulatedTimeUnit, CoordinatorI simulationEngine, ModelI[] submodels,
							 Map<Class<? extends EventI>, EventSink[]> imported, Map<Class<? extends EventI>, ReexportedEvent> reexported,
							 Map<EventSource, EventSink[]> connections, Map<StaticVariableDescriptor, VariableSink[]> importedVars,
							 Map<VariableSource, StaticVariableDescriptor> reexportedVars,Map<VariableSource, VariableSink[]> bindings ) throws Exception 
    {
    	super(uri, simulatedTimeUnit, simulationEngine, submodels,
  			  imported, reexported, connections,
  			  importedVars, reexportedVars, bindings);
    }
}
