package fr.sorbonne_u.components.equipments.iron.mil;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import fr.sorbonne_u.devs_simulation.models.CoupledModel;
import fr.sorbonne_u.devs_simulation.models.events.EventI;
import fr.sorbonne_u.devs_simulation.models.events.EventSink;
import fr.sorbonne_u.devs_simulation.models.events.EventSource;
import fr.sorbonne_u.devs_simulation.models.events.ReexportedEvent;
import fr.sorbonne_u.devs_simulation.models.interfaces.ModelI;
import fr.sorbonne_u.devs_simulation.simulators.interfaces.CoordinatorI;

public class IronCoupledModel extends CoupledModel {

	private static final long serialVersionUID = 1L;														
	public static final String MIL_URI = IronCoupledModel.class.getSimpleName() + "-MIL";
	public static final String MIL_RT_URI = IronCoupledModel.class.getSimpleName() + "-MIL-RT";
	public static final String SIL_URI = IronCoupledModel.class.getSimpleName() + "-SIL";

	public IronCoupledModel(String uri, TimeUnit simulatedTimeUnit, CoordinatorI simulationEngine, ModelI[] submodels,
							Map<Class<? extends EventI>,EventSink[]> imported, 
							Map<Class<? extends EventI>, ReexportedEvent> reexported, Map<EventSource, EventSink[]> connections) throws Exception 
	{
		super(uri, simulatedTimeUnit, simulationEngine, submodels,
			  imported, reexported, connections);
	}
}
