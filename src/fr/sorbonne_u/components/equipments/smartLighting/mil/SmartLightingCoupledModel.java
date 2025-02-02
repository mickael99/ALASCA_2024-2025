package fr.sorbonne_u.components.equipments.smartLighting.mil;

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

import java.util.Map;
import java.util.concurrent.TimeUnit;

public class SmartLightingCoupledModel extends CoupledModel {

  // ------------------------------------------------------------------------
  // Constants
  // ------------------------------------------------------------------------

  private static final long serialVersionUID = 1L;
  public static final String URI = SmartLightingCoupledModel.class.getSimpleName();

  public static final String MIL_URI = SmartLightingCoupledModel.class.getSimpleName() + "-MIL";
  public static final String MIL_RT_URI =
      SmartLightingCoupledModel.class.getSimpleName() + "-MIL-RT";
  public static final String SIL_URI = SmartLightingCoupledModel.class.getSimpleName() + "-SIL";

  // ------------------------------------------------------------------------
  // Invariants
  // ------------------------------------------------------------------------
  protected static boolean glassBoxInvariants(SmartLightingCoupledModel instance) {
    assert instance != null
        : new NeoSim4JavaException("Precondition violation: " + "instance != null");

    boolean ret = true;
    return ret;
  }

  protected static boolean blackBoxInvariants(SmartLightingCoupledModel instance) {
    // TODO Auto-generated method stub
    assert instance != null
        : new NeoSim4JavaException("Precondition violation: " + "instance != null");

    boolean ret = true;
    ret &=
        InvariantChecking.checkBlackBoxInvariant(
            MIL_URI != null && !MIL_URI.isEmpty(),
            SmartLightingCoupledModel.class,
            instance,
            "MIL_URI != null && !MIL_URI.isEmpty()");
    ret &=
        InvariantChecking.checkBlackBoxInvariant(
            MIL_RT_URI != null && !MIL_RT_URI.isEmpty(),
            SmartLightingCoupledModel.class,
            instance,
            "MIL_RT_URI != null && !MIL_RT_URI.isEmpty()");
    ret &=
        InvariantChecking.checkBlackBoxInvariant(
            SIL_URI != null && !SIL_URI.isEmpty(),
            SmartLightingCoupledModel.class,
            instance,
            "URI != null && !URI.isEmpty()");
    return ret;
  }

  // ------------------------------------------------------------------------
  // Constructors
  // ------------------------------------------------------------------------

  public SmartLightingCoupledModel(
      String uri,
      TimeUnit simulatedTimeUnit,
      CoordinatorI simulationEngine,
      ModelI[] submodels,
      Map<Class<? extends EventI>, EventSink[]> imported,
      Map<Class<? extends EventI>, ReexportedEvent> reexported,
      Map<EventSource, EventSink[]> connections)
      throws Exception {
    super(uri, simulatedTimeUnit, simulationEngine, submodels, imported, reexported, connections);

    assert glassBoxInvariants(this)
        : new NeoSim4JavaException("SmartLightingCoupledModel: White-box invariants violation!");
    assert blackBoxInvariants(this)
        : new NeoSim4JavaException("SmartLightingCoupledModel: Black-box invariants violation!");
  }

  public SmartLightingCoupledModel(
      String uri,
      TimeUnit simulatedTimeUnit,
      CoordinatorI simulationEngine,
      ModelI[] submodels,
      Map<Class<? extends EventI>, EventSink[]> imported,
      Map<Class<? extends EventI>, ReexportedEvent> reexported,
      Map<EventSource, EventSink[]> connections,
      Map<StaticVariableDescriptor, VariableSink[]> importedVars,
      Map<VariableSource, StaticVariableDescriptor> reexportedVars,
      Map<VariableSource, VariableSink[]> bindings)
      throws Exception {
    super(
        uri,
        simulatedTimeUnit,
        simulationEngine,
        submodels,
        imported,
        reexported,
        connections,
        importedVars,
        reexportedVars,
        bindings);

    assert glassBoxInvariants(this)
        : new NeoSim4JavaException("SmartLightingCoupledModel: White-box invariants violation!");
    assert blackBoxInvariants(this)
        : new NeoSim4JavaException("SmartLightingCoupledModel: Black-box invariants violation!");
  }
}
// ------------------------------------------------------------------------
