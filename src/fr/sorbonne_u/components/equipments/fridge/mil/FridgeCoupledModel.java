package fr.sorbonne_u.components.equipments.fridge.mil;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import fr.sorbonne_u.devs_simulation.exceptions.NeoSim4JavaException;
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
import fr.sorbonne_u.devs_simulation.utils.InvariantChecking;

public class FridgeCoupledModel extends CoupledModel {

	private static final long serialVersionUID = 1L;
	
	public static final String MIL_URI = FridgeCoupledModel.class.getSimpleName() + "-MIL";
	public static final String MIL_RT_URI = FridgeCoupledModel.class.getSimpleName() + "-MIL-RT";
	public static final String SIL_URI = FridgeCoupledModel.class.getSimpleName() + "-SIL";

	
	protected static boolean glassBoxInvariants(FridgeCoupledModel instance) {
			assert	instance != null :
					new NeoSim4JavaException("Precondition violation: "
							+ "instance != null");

			boolean ret = true;
			return ret;
		}

	protected static boolean blackBoxInvariants(FridgeCoupledModel instance) {
		assert	instance != null :
				new NeoSim4JavaException("Precondition violation: "
						+ "instance != null");

		boolean ret = true;
		ret &= InvariantChecking.checkBlackBoxInvariant(
					MIL_URI != null && !MIL_URI.isEmpty(),
					FridgeCoupledModel.class,
					instance,
					"MIL_URI != null && !MIL_URI.isEmpty()");
		ret &= InvariantChecking.checkBlackBoxInvariant(
					MIL_RT_URI != null && !MIL_RT_URI.isEmpty(),
					FridgeCoupledModel.class,
					instance,
					"MIL_RT_URI != null && !MIL_RT_URI.isEmpty()");
		ret &= InvariantChecking.checkBlackBoxInvariant(
					SIL_URI != null && !SIL_URI.isEmpty(),
					FridgeCoupledModel.class,
					instance,
					"URI != null && !URI.isEmpty()");
		return ret;
	}
		
	public FridgeCoupledModel(
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
		
		assert	glassBoxInvariants(this) :
				new NeoSim4JavaException("White-box invariants violation!");
		assert	blackBoxInvariants(this) :
				new NeoSim4JavaException("Black-box invariants violation!");
	}
	
	public FridgeCoupledModel(
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
		
		assert	glassBoxInvariants(this) :
				new NeoSim4JavaException("White-box invariants violation!");
		assert	blackBoxInvariants(this) :
				new NeoSim4JavaException("Black-box invariants violation!");
	}
}
