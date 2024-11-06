package fr.sorbonne_u.components.equipments.toaster.mil;

import fr.sorbonne_u.components.equipments.toaster.mil.events.SetToasterBrowningLevel;
import fr.sorbonne_u.components.equipments.toaster.mil.events.TurnOffToaster;
import fr.sorbonne_u.components.equipments.toaster.mil.events.TurnOnToaster;
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

public class RunToasterUnaryMILSimulation {

    public static void main(String[] args) {
        try {
            Map<String, AbstractAtomicModelDescriptor> atomicModelDescriptors =
                    new HashMap<>();

            atomicModelDescriptors.put(
                    ToasterElectricityModel.URI,
                    AtomicHIOA_Descriptor.create(
                            ToasterElectricityModel.class,
                            ToasterElectricityModel.URI,
                            TimeUnit.HOURS,
                            null));

            atomicModelDescriptors.put(
                    ToasterUnitTesterModel.URI,
                    AtomicModelDescriptor.create(
                            ToasterUnitTesterModel.class,
                            ToasterUnitTesterModel.URI,
                            TimeUnit.HOURS,
                            null));

            Map<String, CoupledModelDescriptor> coupledModelDescriptors =
                    new HashMap<>();

            Set<String> submodels = new HashSet<String>();
            submodels.add(ToasterElectricityModel.URI);
            submodels.add(ToasterUnitTesterModel.URI);

            Map<EventSource, EventSink[]> connections =
                    new HashMap<EventSource,EventSink[]>();

            connections.put(
                    new EventSource(ToasterUnitTesterModel.URI, TurnOnToaster.class),
                    new EventSink[] {
                            new EventSink(ToasterElectricityModel.URI, TurnOnToaster.class)
                    });
            connections.put(
                    new EventSource(ToasterUnitTesterModel.URI, TurnOffToaster.class),
                    new EventSink[] {
                            new EventSink(ToasterElectricityModel.URI, TurnOffToaster.class)
                    });
            connections.put(
                    new EventSource(ToasterUnitTesterModel.URI, SetToasterBrowningLevel.class),
                    new EventSink[] {
                            new EventSink(ToasterElectricityModel.URI, SetToasterBrowningLevel.class)
                    });

            // coupled model descriptor
            coupledModelDescriptors.put(
                    ToasterCoupledModel.URI,
                    new CoupledHIOA_Descriptor(
                            ToasterCoupledModel.class,
                            ToasterCoupledModel.URI,
                            submodels,
                            null,
                            null,
                            connections,
                            null,
                            null,
                            null,
                            null));

            // simulation architecture
            ArchitectureI architecture =
                    new Architecture(
                            ToasterCoupledModel.URI,
                            atomicModelDescriptors,
                            coupledModelDescriptors,
                            TimeUnit.HOURS);
            // create the simulator from the simulation architecture
            SimulatorI se = architecture.constructSimulator();
            // this add additional time at each simulation step in
            // standard simulations (useful when debugging)
            SimulationEngine.SIMULATION_STEP_SLEEP_TIME = 0L;
            // run a simulation with the simulation beginning at 0.0 and
            // ending at 24.0
            se.doStandAloneSimulation(0.0, 24.0);
            System.exit(0);
        } catch (Exception e) {
            throw new RuntimeException(e) ;
        }
    }
}

// -----------------------------------------------------------------------------