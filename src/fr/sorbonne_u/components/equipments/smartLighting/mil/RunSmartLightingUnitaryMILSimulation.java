package fr.sorbonne_u.components.equipments.smartLighting.mil;

import fr.sorbonne_u.components.equipments.smartLighting.mil.events.*;
import fr.sorbonne_u.devs_simulation.architectures.Architecture;
import fr.sorbonne_u.devs_simulation.architectures.ArchitectureI;
import fr.sorbonne_u.devs_simulation.hioa.architectures.AtomicHIOA_Descriptor;
import fr.sorbonne_u.devs_simulation.hioa.architectures.CoupledHIOA_Descriptor;
import fr.sorbonne_u.devs_simulation.hioa.models.vars.VariableSink;
import fr.sorbonne_u.devs_simulation.hioa.models.vars.VariableSource;
import fr.sorbonne_u.devs_simulation.models.architectures.AbstractAtomicModelDescriptor;
import fr.sorbonne_u.devs_simulation.models.architectures.AtomicModelDescriptor;
import fr.sorbonne_u.devs_simulation.models.architectures.CoupledModelDescriptor;
import fr.sorbonne_u.devs_simulation.models.events.EventSink;
import fr.sorbonne_u.devs_simulation.models.events.EventSource;
import fr.sorbonne_u.devs_simulation.simulators.SimulationEngine;
import fr.sorbonne_u.devs_simulation.simulators.interfaces.SimulatorI;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class RunSmartLightingUnitaryMILSimulation {
  public static void main(String[] args) {
    try {
      // atomic model descriptors
      Map<String, AbstractAtomicModelDescriptor> atomicModelDescriptors = new HashMap<>();

      atomicModelDescriptors.put(
          SmartLightingElectricityModel.URI,
          AtomicHIOA_Descriptor.create(
              SmartLightingElectricityModel.class,
              SmartLightingElectricityModel.URI,
              TimeUnit.HOURS,
              null));
      atomicModelDescriptors.put(
          SmartLightingIlluminanceModel.URI,
          AtomicHIOA_Descriptor.create(
              SmartLightingIlluminanceModel.class,
              SmartLightingIlluminanceModel.URI,
              TimeUnit.HOURS,
              null));
      atomicModelDescriptors.put(
          ExternalIlluminanceModel.URI,
          AtomicHIOA_Descriptor.create(
              ExternalIlluminanceModel.class, ExternalIlluminanceModel.URI, TimeUnit.HOURS, null));
      atomicModelDescriptors.put(
          SmartLightingUnitTesterModel.URI,
          AtomicModelDescriptor.create(
              SmartLightingUnitTesterModel.class,
              SmartLightingUnitTesterModel.URI,
              TimeUnit.HOURS,
              null));

      Map<String, CoupledModelDescriptor> coupledModelDescriptors = new HashMap<>();

      // submodels
      Set<String> submodels = new HashSet<String>();
      submodels.add(SmartLightingElectricityModel.URI);
      submodels.add(SmartLightingIlluminanceModel.URI);
      submodels.add(ExternalIlluminanceModel.URI);
      submodels.add(SmartLightingUnitTesterModel.URI);

      // connections
      Map<EventSource, EventSink[]> connections = new HashMap<EventSource, EventSink[]>();

      connections.put(
          new EventSource(SmartLightingUnitTesterModel.URI, TurnOnSmartLighting.class),
          new EventSink[] {
            new EventSink(SmartLightingElectricityModel.URI, TurnOnSmartLighting.class)
          });
      connections.put(
          new EventSource(SmartLightingUnitTesterModel.URI, SetPowerSmartLighting.class),
          new EventSink[] {
            new EventSink(SmartLightingElectricityModel.URI, SetPowerSmartLighting.class)
          });
      connections.put(
          new EventSource(SmartLightingUnitTesterModel.URI, TurnOffSmartLighting.class),
          new EventSink[] {
            new EventSink(SmartLightingElectricityModel.URI, TurnOffSmartLighting.class),
          });
      connections.put(
          new EventSource(SmartLightingUnitTesterModel.URI, IncreaseLighting.class),
          new EventSink[] {
            new EventSink(SmartLightingElectricityModel.URI, IncreaseLighting.class),
            new EventSink(SmartLightingIlluminanceModel.URI, IncreaseLighting.class)
          });
      connections.put(
          new EventSource(SmartLightingUnitTesterModel.URI, DecreaseLighting.class),
          new EventSink[] {
            new EventSink(SmartLightingElectricityModel.URI, DecreaseLighting.class),
            new EventSink(SmartLightingIlluminanceModel.URI, DecreaseLighting.class)
          });
      connections.put(
          new EventSource(SmartLightingUnitTesterModel.URI, StopAdjustingLighting.class),
          new EventSink[] {
            new EventSink(SmartLightingElectricityModel.URI, StopAdjustingLighting.class),
            new EventSink(SmartLightingIlluminanceModel.URI, StopAdjustingLighting.class)
          });

      // bindings
      Map<VariableSource, VariableSink[]> bindings = new HashMap<VariableSource, VariableSink[]>();

      bindings.put(
          new VariableSource("externalIlluminance", Double.class, ExternalIlluminanceModel.URI),
          new VariableSink[] {
            new VariableSink("externalIlluminance", Double.class, SmartLightingIlluminanceModel.URI)
          });
      bindings.put(
          new VariableSource("currentPowerLevel", Double.class, SmartLightingElectricityModel.URI),
          new VariableSink[] {
            new VariableSink("currentPowerLevel", Double.class, SmartLightingIlluminanceModel.URI)
          });

      coupledModelDescriptors.put(
          SmartLightingCoupledModel.URI,
          new CoupledHIOA_Descriptor(
              SmartLightingCoupledModel.class,
              SmartLightingCoupledModel.URI,
              submodels,
              null,
              null,
              connections,
              null,
              null,
              null,
              bindings));

      ArchitectureI architecture =
          new Architecture(
              SmartLightingCoupledModel.URI,
              atomicModelDescriptors,
              coupledModelDescriptors,
              TimeUnit.HOURS);

      SimulatorI se = architecture.constructSimulator();
      SimulationEngine.SIMULATION_STEP_SLEEP_TIME = 0L;
      se.doStandAloneSimulation(0.0, 24.0);
      System.exit(0);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}
// ------------------------------------------------------------------------
