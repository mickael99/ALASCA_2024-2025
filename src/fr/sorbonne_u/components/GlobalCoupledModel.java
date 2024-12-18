package fr.sorbonne_u.components;

import java.util.Map;
import java.util.concurrent.TimeUnit;
import fr.sorbonne_u.devs_simulation.models.CoupledModel;
import fr.sorbonne_u.devs_simulation.models.events.EventI;
import fr.sorbonne_u.devs_simulation.models.events.EventSink;
import fr.sorbonne_u.devs_simulation.models.events.EventSource;
import fr.sorbonne_u.devs_simulation.models.events.ReexportedEvent;
import fr.sorbonne_u.devs_simulation.models.interfaces.ModelI;
import fr.sorbonne_u.devs_simulation.simulators.interfaces.CoordinatorI;


public class GlobalCoupledModel extends CoupledModel {

	private static final long serialVersionUID = 1L;							
	public static final String	MIL_URI = GlobalCoupledModel.class.getSimpleName() + "-MIL";
	public static final String	MIL_RT_URI = GlobalCoupledModel.class.getSimpleName() + "-MIL_RT";
	public static final String	SIL_URI = GlobalCoupledModel.class.getSimpleName() + "-SIL";

	// -------------------------------------------------------------------------
	// Constructors
	// -------------------------------------------------------------------------

	public GlobalCoupledModel(String uri, TimeUnit simulatedTimeUnit, CoordinatorI simulationEngine,
								ModelI[] submodels, Map<Class<? extends EventI>,EventSink[]> imported,
								Map<Class<? extends EventI>,ReexportedEvent> reexported,
								Map<EventSource, EventSink[]> connections) throws Exception
	{
		super(uri, simulatedTimeUnit, simulationEngine, submodels,
			  imported, reexported, connections);
	}
}
