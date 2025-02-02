package fr.sorbonne_u.components.equipments.smartLightingE3.mil;

import fr.sorbonne_u.components.equipments.smartLightingE3.mil.events.*;
import fr.sorbonne_u.components.utils.SimulationType;
import fr.sorbonne_u.devs_simulation.architectures.Architecture;
import fr.sorbonne_u.devs_simulation.architectures.RTArchitecture;
import fr.sorbonne_u.devs_simulation.hioa.architectures.CoupledHIOA_Descriptor;
import fr.sorbonne_u.devs_simulation.hioa.architectures.RTCoupledHIOA_Descriptor;
import fr.sorbonne_u.devs_simulation.hioa.models.vars.VariableSink;
import fr.sorbonne_u.devs_simulation.hioa.models.vars.VariableSource;
import fr.sorbonne_u.devs_simulation.models.architectures.AbstractAtomicModelDescriptor;
import fr.sorbonne_u.devs_simulation.models.architectures.AtomicModelDescriptor;
import fr.sorbonne_u.devs_simulation.models.architectures.CoupledModelDescriptor;
import fr.sorbonne_u.devs_simulation.models.architectures.RTAtomicModelDescriptor;
import fr.sorbonne_u.devs_simulation.models.events.EventI;
import fr.sorbonne_u.devs_simulation.models.events.EventSink;
import fr.sorbonne_u.devs_simulation.models.events.EventSource;
import fr.sorbonne_u.devs_simulation.models.events.ReexportedEvent;
import fr.sorbonne_u.exceptions.PreconditionException;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public abstract class LocalSimulationArchitectures {

  public static Architecture createSmartLightingMILLocalArchitecture4UnitTest(
      String architectureURI, TimeUnit simulatedTimeUnit) throws Exception {
    Map<String, AbstractAtomicModelDescriptor> atomicModelDescriptors = new HashMap<>();

    atomicModelDescriptors.put(
        SmartLightingStateModel.MIL_URI,
        AtomicModelDescriptor.create(
            SmartLightingStateModel.class,
            SmartLightingStateModel.MIL_URI,
            simulatedTimeUnit,
            null));
    atomicModelDescriptors.put(
        SmartLightingIlluminanceModel.MIL_URI,
        AtomicModelDescriptor.create(
            SmartLightingIlluminanceModel.class,
            SmartLightingIlluminanceModel.MIL_URI,
            simulatedTimeUnit,
            null));
    atomicModelDescriptors.put(
        ExternalIlluminanceModel.MIL_URI,
        AtomicModelDescriptor.create(
            ExternalIlluminanceModel.class,
            ExternalIlluminanceModel.MIL_URI,
            simulatedTimeUnit,
            null));
    atomicModelDescriptors.put(
        SmartLightingElectricityModel.MIL_URI,
        AtomicModelDescriptor.create(
            SmartLightingElectricityModel.class,
            SmartLightingElectricityModel.MIL_URI,
            simulatedTimeUnit,
            null));

    Map<String, CoupledModelDescriptor> coupledModelDescriptors = new HashMap<>();

    Set<String> submodels = new HashSet<String>();
    submodels.add(SmartLightingStateModel.MIL_URI);
    submodels.add(SmartLightingIlluminanceModel.MIL_URI);
    submodels.add(ExternalIlluminanceModel.MIL_URI);
    submodels.add(SmartLightingElectricityModel.MIL_URI);

    Map<Class<? extends EventI>, EventSink[]> imported = new HashMap<>();
    imported.put(
        TurnOnSmartLighting.class,
        new EventSink[] {
          new EventSink(SmartLightingStateModel.MIL_URI, TurnOnSmartLighting.class)
        });
    imported.put(
        TurnOffSmartLighting.class,
        new EventSink[] {
          new EventSink(SmartLightingStateModel.MIL_URI, TurnOffSmartLighting.class)
        });
    imported.put(
        SetPowerSmartLighting.class,
        new EventSink[] {
          new EventSink(SmartLightingStateModel.MIL_URI, SetPowerSmartLighting.class)
        });
    imported.put(
        IncreaseLighting.class,
        new EventSink[] {new EventSink(SmartLightingStateModel.MIL_URI, IncreaseLighting.class)});
    imported.put(
        DecreaseLighting.class,
        new EventSink[] {new EventSink(SmartLightingStateModel.MIL_URI, DecreaseLighting.class)});

    Map<EventSource, EventSink[]> connections = new HashMap<EventSource, EventSink[]>();
    connections.put(
        new EventSource(SmartLightingStateModel.MIL_URI, TurnOnSmartLighting.class),
        new EventSink[] {
          new EventSink(SmartLightingElectricityModel.MIL_URI, TurnOnSmartLighting.class)
        });
    connections.put(
        new EventSource(SmartLightingStateModel.MIL_URI, SetPowerSmartLighting.class),
        new EventSink[] {
          new EventSink(SmartLightingElectricityModel.MIL_URI, SetPowerSmartLighting.class),
          new EventSink(SmartLightingIlluminanceModel.MIL_URI, SetPowerSmartLighting.class)
        });
    connections.put(
        new EventSource(SmartLightingStateModel.MIL_URI, IncreaseLighting.class),
        new EventSink[] {
          new EventSink(SmartLightingElectricityModel.MIL_URI, IncreaseLighting.class),
          new EventSink(SmartLightingIlluminanceModel.MIL_URI, IncreaseLighting.class)
        });
    connections.put(
        new EventSource(SmartLightingStateModel.MIL_URI, DecreaseLighting.class),
        new EventSink[] {
          new EventSink(SmartLightingElectricityModel.MIL_URI, DecreaseLighting.class),
          new EventSink(SmartLightingIlluminanceModel.MIL_URI, DecreaseLighting.class)
        });
    connections.put(
        new EventSource(SmartLightingStateModel.MIL_URI, TurnOffSmartLighting.class),
        new EventSink[] {
          new EventSink(SmartLightingElectricityModel.MIL_URI, TurnOffSmartLighting.class)
        });

    Map<VariableSource, VariableSink[]> bindings = new HashMap<VariableSource, VariableSink[]>();
    bindings.put(
        new VariableSource("externalIlluminance", Double.class, ExternalIlluminanceModel.MIL_URI),
        new VariableSink[] {
          new VariableSink(
              "externalIlluminance", Double.class, SmartLightingIlluminanceModel.MIL_URI)
        });

    coupledModelDescriptors.put(
        SmartLightingCoupledModel.MIL_URI,
        new CoupledHIOA_Descriptor(
            SmartLightingCoupledModel.class,
            SmartLightingCoupledModel.MIL_URI,
            submodels,
            imported,
            null,
            connections,
            null,
            null,
            null,
            bindings));

    Architecture architecture =
        new Architecture(
            architectureURI,
            SmartLightingCoupledModel.MIL_URI,
            atomicModelDescriptors,
            coupledModelDescriptors,
            simulatedTimeUnit);

    return architecture;
  }

  public static Architecture createSmartLighting_RT_LocalArchitecture4UnitTest(
      SimulationType currentSimulationType,
      String architectureURI,
      TimeUnit simulatedTimeUnit,
      double accelerationFactor)
      throws Exception {
    assert currentSimulationType.isMILRTSimulation() || currentSimulationType.isSILSimulation()
        : new PreconditionException(
            "currentSimulationType.isMILRTSimulation() || "
                + "currentSimulationType.isSILSimulation()");

    String smartLightingStateModelURI = null;
    String smartLightingIlluminanceModelURI = null;
    String externalIlluminanceModelURI = null;
    String smartLightingElectricityModelURI = null;
    String smartLightingCoupledModelURI = null;

    switch (currentSimulationType) {
      case MIL_RT_SIMULATION:
        smartLightingCoupledModelURI = SmartLightingCoupledModel.MIL_RT_URI;
        smartLightingStateModelURI = SmartLightingStateModel.MIL_RT_URI;
        smartLightingIlluminanceModelURI = SmartLightingIlluminanceModel.MIL_RT_URI;
        externalIlluminanceModelURI = ExternalIlluminanceModel.MIL_RT_URI;
        smartLightingElectricityModelURI = SmartLightingElectricityModel.MIL_RT_URI;
        break;
      case SIL_SIMULATION:
        smartLightingCoupledModelURI = SmartLightingCoupledModel.SIL_URI;
        smartLightingStateModelURI = SmartLightingStateModel.SIL_URI;
        smartLightingIlluminanceModelURI = SmartLightingIlluminanceModel.SIL_URI;
        externalIlluminanceModelURI = ExternalIlluminanceModel.SIL_URI;
        smartLightingElectricityModelURI = SmartLightingElectricityModel.SIL_URI;
        break;
      default:
        break;
    }
    Map<String, AbstractAtomicModelDescriptor> atomicModelDescriptors = new HashMap<>();
    atomicModelDescriptors.put(
        smartLightingStateModelURI,
        RTAtomicModelDescriptor.create(
            SmartLightingStateModel.class,
            smartLightingStateModelURI,
            simulatedTimeUnit,
            null,
            accelerationFactor));
    atomicModelDescriptors.put(
        externalIlluminanceModelURI,
        RTAtomicModelDescriptor.create(
            ExternalIlluminanceModel.class,
            externalIlluminanceModelURI,
            simulatedTimeUnit,
            null,
            accelerationFactor));
    atomicModelDescriptors.put(
        smartLightingIlluminanceModelURI,
        RTAtomicModelDescriptor.create(
            SmartLightingIlluminanceModel.class,
            smartLightingIlluminanceModelURI,
            simulatedTimeUnit,
            null,
            accelerationFactor));
    atomicModelDescriptors.put(
        smartLightingElectricityModelURI,
        RTAtomicModelDescriptor.create(
            SmartLightingElectricityModel.class,
            smartLightingElectricityModelURI,
            simulatedTimeUnit,
            null,
            accelerationFactor));

    Map<String, CoupledModelDescriptor> coupledModelDescriptors = new HashMap<>();

    Set<String> submodels = new HashSet<String>();
    submodels.add(smartLightingStateModelURI);
    submodels.add(smartLightingIlluminanceModelURI);
    submodels.add(externalIlluminanceModelURI);
    submodels.add(smartLightingElectricityModelURI);

    Map<Class<? extends EventI>, EventSink[]> imported = new HashMap<>();
    imported.put(
        TurnOnSmartLighting.class,
        new EventSink[] {new EventSink(smartLightingStateModelURI, TurnOnSmartLighting.class)});
    imported.put(
        TurnOffSmartLighting.class,
        new EventSink[] {new EventSink(smartLightingStateModelURI, TurnOffSmartLighting.class)});
    imported.put(
        SetPowerSmartLighting.class,
        new EventSink[] {new EventSink(smartLightingStateModelURI, SetPowerSmartLighting.class)});
    imported.put(
        IncreaseLighting.class,
        new EventSink[] {new EventSink(smartLightingStateModelURI, IncreaseLighting.class)});
    imported.put(
        DecreaseLighting.class,
        new EventSink[] {new EventSink(smartLightingStateModelURI, DecreaseLighting.class)});

    Map<EventSource, EventSink[]> connections = new HashMap<EventSource, EventSink[]>();
    connections.put(
        new EventSource(smartLightingStateModelURI, TurnOnSmartLighting.class),
        new EventSink[] {
          new EventSink(smartLightingElectricityModelURI, TurnOnSmartLighting.class)
        });
    connections.put(
        new EventSource(smartLightingStateModelURI, SetPowerSmartLighting.class),
        new EventSink[] {
          new EventSink(smartLightingElectricityModelURI, SetPowerSmartLighting.class),
          new EventSink(smartLightingIlluminanceModelURI, SetPowerSmartLighting.class)
        });
    connections.put(
        new EventSource(smartLightingStateModelURI, IncreaseLighting.class),
        new EventSink[] {
          new EventSink(smartLightingElectricityModelURI, IncreaseLighting.class),
          new EventSink(smartLightingIlluminanceModelURI, IncreaseLighting.class)
        });
    connections.put(
        new EventSource(smartLightingStateModelURI, DecreaseLighting.class),
        new EventSink[] {
          new EventSink(smartLightingElectricityModelURI, DecreaseLighting.class),
          new EventSink(smartLightingIlluminanceModelURI, DecreaseLighting.class)
        });
    connections.put(
        new EventSource(smartLightingStateModelURI, TurnOffSmartLighting.class),
        new EventSink[] {
          new EventSink(smartLightingElectricityModelURI, TurnOffSmartLighting.class)
        });

    Map<VariableSource, VariableSink[]> bindings = new HashMap<VariableSource, VariableSink[]>();
    bindings.put(
        new VariableSource("externalIlluminance", Double.class, externalIlluminanceModelURI),
        new VariableSink[] {
          new VariableSink("externalIlluminance", Double.class, smartLightingIlluminanceModelURI)
        });

    coupledModelDescriptors.put(
        smartLightingCoupledModelURI,
        new RTCoupledHIOA_Descriptor(
            SmartLightingCoupledModel.class,
            smartLightingCoupledModelURI,
            submodels,
            imported,
            null,
            connections,
            null,
            null,
            null,
            bindings,
            accelerationFactor));

    Architecture architecture =
        new RTArchitecture(
            architectureURI,
            smartLightingCoupledModelURI,
            atomicModelDescriptors,
            coupledModelDescriptors,
            simulatedTimeUnit,
            accelerationFactor);
    return architecture;
  }

  public static Architecture createSmartLightingUserMILLocalArchitecture(
      String architectureURI, TimeUnit simulatedTimeUnit) throws Exception {
    Map<String, AbstractAtomicModelDescriptor> atomicModelDescriptors = new HashMap<>();
    atomicModelDescriptors.put(
        SmartLightingUnitTesterModel.MIL_URI,
        AtomicModelDescriptor.create(
            SmartLightingUnitTesterModel.class,
            SmartLightingUnitTesterModel.MIL_URI,
            simulatedTimeUnit,
            null));

    Map<String, CoupledModelDescriptor> coupledModelDescriptors = new HashMap<>();

    Architecture architecture =
        new Architecture(
            architectureURI,
            SmartLightingUnitTesterModel.MIL_URI,
            atomicModelDescriptors,
            coupledModelDescriptors,
            simulatedTimeUnit);
    return architecture;
  }

  public static Architecture createSmartLightingUserMILRT_LocalArchitecture(
      String architectureURI, TimeUnit simulatedTimeUnit, double accelerationFactor)
      throws Exception {
    Map<String, AbstractAtomicModelDescriptor> atomicModelDescriptors = new HashMap<>();
    atomicModelDescriptors.put(
        SmartLightingUnitTesterModel.MIL_RT_URI,
        RTAtomicModelDescriptor.create(
            SmartLightingUnitTesterModel.class,
            SmartLightingUnitTesterModel.MIL_RT_URI,
            simulatedTimeUnit,
            null,
            accelerationFactor));

    Map<String, CoupledModelDescriptor> coupledModelDescriptors = new HashMap<>();

    Architecture architecture =
        new RTArchitecture(
            architectureURI,
            SmartLightingUnitTesterModel.MIL_RT_URI,
            atomicModelDescriptors,
            coupledModelDescriptors,
            simulatedTimeUnit,
            accelerationFactor);
    return architecture;
  }

  public static Architecture createSmartLightingMILLocalArchitecture4IntegrationTest(
      String architectureURI, TimeUnit simulatedTimeUnit) throws Exception {
    Map<String, AbstractAtomicModelDescriptor> atomicModelDescriptors = new HashMap<>();

    atomicModelDescriptors.put(
        SmartLightingStateModel.MIL_URI,
        AtomicModelDescriptor.create(
            SmartLightingStateModel.class,
            SmartLightingStateModel.MIL_URI,
            simulatedTimeUnit,
            null));
    atomicModelDescriptors.put(
        SmartLightingIlluminanceModel.MIL_URI,
        AtomicModelDescriptor.create(
            SmartLightingIlluminanceModel.class,
            SmartLightingIlluminanceModel.MIL_URI,
            simulatedTimeUnit,
            null));
    atomicModelDescriptors.put(
        ExternalIlluminanceModel.MIL_URI,
        AtomicModelDescriptor.create(
            ExternalIlluminanceModel.class,
            ExternalIlluminanceModel.MIL_URI,
            simulatedTimeUnit,
            null));

    Map<String, CoupledModelDescriptor> coupledModelDescriptors = new HashMap<>();
    Set<String> submodels = new HashSet<String>();
    submodels.add(SmartLightingStateModel.MIL_URI);
    submodels.add(SmartLightingIlluminanceModel.MIL_URI);
    submodels.add(ExternalIlluminanceModel.MIL_URI);

    Map<Class<? extends EventI>, EventSink[]> imported = new HashMap<>();
    imported.put(
        TurnOnSmartLighting.class,
        new EventSink[] {
          new EventSink(SmartLightingStateModel.MIL_URI, TurnOnSmartLighting.class)
        });
    imported.put(
        TurnOffSmartLighting.class,
        new EventSink[] {
          new EventSink(SmartLightingStateModel.MIL_URI, TurnOffSmartLighting.class)
        });
    imported.put(
        SetPowerSmartLighting.class,
        new EventSink[] {
          new EventSink(SmartLightingStateModel.MIL_URI, SetPowerSmartLighting.class)
        });
    imported.put(
        IncreaseLighting.class,
        new EventSink[] {new EventSink(SmartLightingStateModel.MIL_URI, IncreaseLighting.class)});
    imported.put(
        DecreaseLighting.class,
        new EventSink[] {new EventSink(SmartLightingStateModel.MIL_URI, DecreaseLighting.class)});

    Map<Class<? extends EventI>, ReexportedEvent> reexported =
        new HashMap<Class<? extends EventI>, ReexportedEvent>();
    reexported.put(
        TurnOnSmartLighting.class,
        new ReexportedEvent(SmartLightingStateModel.MIL_URI, TurnOnSmartLighting.class));
    reexported.put(
        TurnOffSmartLighting.class,
        new ReexportedEvent(SmartLightingStateModel.MIL_URI, TurnOffSmartLighting.class));
    reexported.put(
        SetPowerSmartLighting.class,
        new ReexportedEvent(SmartLightingStateModel.MIL_URI, SetPowerSmartLighting.class));
    reexported.put(
        IncreaseLighting.class,
        new ReexportedEvent(SmartLightingStateModel.MIL_URI, IncreaseLighting.class));
    reexported.put(
        DecreaseLighting.class,
        new ReexportedEvent(SmartLightingStateModel.MIL_URI, DecreaseLighting.class));

    Map<EventSource, EventSink[]> connections = new HashMap<EventSource, EventSink[]>();
    connections.put(
        new EventSource(SmartLightingStateModel.MIL_URI, TurnOnSmartLighting.class),
        new EventSink[] {
          new EventSink(SmartLightingIlluminanceModel.MIL_URI, TurnOnSmartLighting.class)
        });
    connections.put(
        new EventSource(SmartLightingStateModel.MIL_URI, SetPowerSmartLighting.class),
        new EventSink[] {
          new EventSink(SmartLightingIlluminanceModel.MIL_URI, SetPowerSmartLighting.class)
        });
    connections.put(
        new EventSource(SmartLightingStateModel.MIL_URI, IncreaseLighting.class),
        new EventSink[] {
          new EventSink(SmartLightingIlluminanceModel.MIL_URI, IncreaseLighting.class)
        });
    connections.put(
        new EventSource(SmartLightingStateModel.MIL_URI, DecreaseLighting.class),
        new EventSink[] {
          new EventSink(SmartLightingIlluminanceModel.MIL_URI, DecreaseLighting.class)
        });
    connections.put(
        new EventSource(SmartLightingStateModel.MIL_URI, TurnOffSmartLighting.class),
        new EventSink[] {
          new EventSink(SmartLightingIlluminanceModel.MIL_URI, TurnOffSmartLighting.class)
        });

    Map<VariableSource, VariableSink[]> bindings = new HashMap<VariableSource, VariableSink[]>();
    bindings.put(
        new VariableSource("externalIlluminance", Double.class, ExternalIlluminanceModel.MIL_URI),
        new VariableSink[] {
          new VariableSink(
              "externalIlluminance", Double.class, SmartLightingIlluminanceModel.MIL_URI)
        });

    coupledModelDescriptors.put(
        SmartLightingCoupledModel.MIL_URI,
        new CoupledHIOA_Descriptor(
            SmartLightingCoupledModel.class,
            SmartLightingCoupledModel.MIL_URI,
            submodels,
            imported,
            null,
            connections,
            null,
            null,
            null,
            bindings));

    Architecture architecture =
        new Architecture(
            architectureURI,
            SmartLightingCoupledModel.MIL_URI,
            atomicModelDescriptors,
            coupledModelDescriptors,
            simulatedTimeUnit);

    return architecture;
  }

  public static Architecture createSmartLighting_RT_LocalArchitecture4IntegrationTest(
      SimulationType currentSimulationType,
      String architectureURI,
      TimeUnit simulatedTimeUnit,
      double accelerationFactor)
      throws Exception {
    assert currentSimulationType.isMILRTSimulation() || currentSimulationType.isSILSimulation()
        : new PreconditionException(
            "currentSimulationType.isMILRTSimulation() || "
                + "currentSimulationType.isSILSimulation()");

    String smartLightingStateModelURI = null;
    String smartLightingIlluminanceModelURI = null;
    String externalIlluminanceModelURI = null;
    String smartLightingCoupledModelURI = null;

    switch (currentSimulationType) {
      case MIL_RT_SIMULATION:
        smartLightingCoupledModelURI = SmartLightingCoupledModel.MIL_RT_URI;
        smartLightingStateModelURI = SmartLightingStateModel.MIL_RT_URI;
        smartLightingIlluminanceModelURI = SmartLightingIlluminanceModel.MIL_RT_URI;
        externalIlluminanceModelURI = ExternalIlluminanceModel.MIL_RT_URI;
        break;
      case SIL_SIMULATION:
        smartLightingCoupledModelURI = SmartLightingCoupledModel.SIL_URI;
        smartLightingStateModelURI = SmartLightingStateModel.SIL_URI;
        smartLightingIlluminanceModelURI = SmartLightingIlluminanceModel.SIL_URI;
        externalIlluminanceModelURI = ExternalIlluminanceModel.SIL_URI;
        break;
      default:
        break;
    }

    Map<String, AbstractAtomicModelDescriptor> atomicModelDescriptors = new HashMap<>();
    atomicModelDescriptors.put(
        smartLightingStateModelURI,
        RTAtomicModelDescriptor.create(
            SmartLightingStateModel.class,
            smartLightingStateModelURI,
            simulatedTimeUnit,
            null,
            accelerationFactor));
    atomicModelDescriptors.put(
        externalIlluminanceModelURI,
        RTAtomicModelDescriptor.create(
            ExternalIlluminanceModel.class,
            externalIlluminanceModelURI,
            simulatedTimeUnit,
            null,
            accelerationFactor));
    atomicModelDescriptors.put(
        smartLightingIlluminanceModelURI,
        RTAtomicModelDescriptor.create(
            SmartLightingIlluminanceModel.class,
            smartLightingIlluminanceModelURI,
            simulatedTimeUnit,
            null,
            accelerationFactor));

    Map<String, CoupledModelDescriptor> coupledModelDescriptors = new HashMap<>();

    Set<String> submodels = new HashSet<String>();
    submodels.add(smartLightingStateModelURI);
    submodels.add(smartLightingIlluminanceModelURI);
    submodels.add(externalIlluminanceModelURI);

    Map<Class<? extends EventI>, EventSink[]> imported = new HashMap<>();
    imported.put(
        TurnOnSmartLighting.class,
        new EventSink[] {new EventSink(smartLightingStateModelURI, TurnOnSmartLighting.class)});
    imported.put(
        TurnOffSmartLighting.class,
        new EventSink[] {new EventSink(smartLightingStateModelURI, TurnOffSmartLighting.class)});
    imported.put(
        SetPowerSmartLighting.class,
        new EventSink[] {new EventSink(smartLightingStateModelURI, SetPowerSmartLighting.class)});
    imported.put(
        IncreaseLighting.class,
        new EventSink[] {new EventSink(smartLightingStateModelURI, IncreaseLighting.class)});
    imported.put(
        DecreaseLighting.class,
        new EventSink[] {new EventSink(smartLightingStateModelURI, DecreaseLighting.class)});

    Map<Class<? extends EventI>, ReexportedEvent> reexported =
        new HashMap<Class<? extends EventI>, ReexportedEvent>();
    reexported.put(
        TurnOnSmartLighting.class,
        new ReexportedEvent(smartLightingStateModelURI, TurnOnSmartLighting.class));
    reexported.put(
        TurnOffSmartLighting.class,
        new ReexportedEvent(smartLightingStateModelURI, TurnOffSmartLighting.class));
    reexported.put(
        SetPowerSmartLighting.class,
        new ReexportedEvent(smartLightingStateModelURI, SetPowerSmartLighting.class));
    reexported.put(
        IncreaseLighting.class,
        new ReexportedEvent(smartLightingStateModelURI, IncreaseLighting.class));
    reexported.put(
        DecreaseLighting.class,
        new ReexportedEvent(smartLightingStateModelURI, DecreaseLighting.class));

    Map<EventSource, EventSink[]> connections = new HashMap<EventSource, EventSink[]>();
    connections.put(
        new EventSource(smartLightingStateModelURI, TurnOnSmartLighting.class),
        new EventSink[] {
          new EventSink(smartLightingIlluminanceModelURI, TurnOnSmartLighting.class)
        });
    connections.put(
        new EventSource(smartLightingStateModelURI, SetPowerSmartLighting.class),
        new EventSink[] {
          new EventSink(smartLightingIlluminanceModelURI, SetPowerSmartLighting.class)
        });
    connections.put(
        new EventSource(smartLightingStateModelURI, IncreaseLighting.class),
        new EventSink[] {new EventSink(smartLightingIlluminanceModelURI, IncreaseLighting.class)});
    connections.put(
        new EventSource(smartLightingStateModelURI, DecreaseLighting.class),
        new EventSink[] {new EventSink(smartLightingIlluminanceModelURI, DecreaseLighting.class)});
    connections.put(
        new EventSource(smartLightingStateModelURI, TurnOffSmartLighting.class),
        new EventSink[] {
          new EventSink(smartLightingIlluminanceModelURI, TurnOffSmartLighting.class)
        });

    Map<VariableSource, VariableSink[]> bindings = new HashMap<VariableSource, VariableSink[]>();
    bindings.put(
        new VariableSource("externalIlluminance", Double.class, externalIlluminanceModelURI),
        new VariableSink[] {
          new VariableSink("externalIlluminance", Double.class, smartLightingIlluminanceModelURI)
        });

    coupledModelDescriptors.put(
        smartLightingCoupledModelURI,
        new RTCoupledHIOA_Descriptor(
            SmartLightingCoupledModel.class,
            smartLightingCoupledModelURI,
            submodels,
            imported,
            reexported,
            connections,
            null,
            null,
            null,
            bindings,
            accelerationFactor));

    Architecture architecture =
        new RTArchitecture(
            architectureURI,
            smartLightingCoupledModelURI,
            atomicModelDescriptors,
            coupledModelDescriptors,
            simulatedTimeUnit,
            accelerationFactor);
    return architecture;
  }
}
