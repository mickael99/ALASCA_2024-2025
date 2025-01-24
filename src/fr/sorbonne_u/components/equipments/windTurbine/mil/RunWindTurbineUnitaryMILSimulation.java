package fr.sorbonne_u.components.equipments.windTurbine.mil;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import fr.sorbonne_u.components.equipments.windTurbine.mil.events.*;
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

public class RunWindTurbineUnitaryMILSimulation {

	public static void main(String[] args) {
        try {

            Map<String, AbstractAtomicModelDescriptor> atomicModelDescriptors = new HashMap<>();

            atomicModelDescriptors.put(
                    WindTurbineElectricityModel.MIL_URI,
                    AtomicHIOA_Descriptor.create(
                            WindTurbineElectricityModel.class,
                            WindTurbineElectricityModel.MIL_URI,
                            TimeUnit.HOURS,
                            null
                    )
            );
            atomicModelDescriptors.put(
                    ExternalWindModel.MIL_URI,
                    AtomicHIOA_Descriptor.create(
                            ExternalWindModel.class,
                            ExternalWindModel.MIL_URI,
                            TimeUnit.HOURS,
                            null
                    )
            );
            atomicModelDescriptors.put(
                    WindTurbineUserModel.MIL_URI,
                    AtomicModelDescriptor.create(
                            WindTurbineUserModel.class,
                            WindTurbineUserModel.MIL_URI,
                            TimeUnit.HOURS,
                            null
                    )
            );
            atomicModelDescriptors.put(
                    WindTurbineStateModel.MIL_URI,
                    AtomicModelDescriptor.create(
                    		WindTurbineStateModel.class,
                    		WindTurbineStateModel.MIL_URI,
                            TimeUnit.HOURS,
                            null
                    )
            );

            
            Map<String, CoupledModelDescriptor> coupledModelDescriptors = new HashMap<>();

            Set<String> subModels = new HashSet<>();
            subModels.add(WindTurbineElectricityModel.MIL_URI);
            subModels.add(WindTurbineUserModel.MIL_URI);
            subModels.add(ExternalWindModel.MIL_URI);
            subModels.add(WindTurbineStateModel.MIL_URI);


            Map<EventSource, EventSink[]> connections = new HashMap<EventSource,EventSink[]>();

            connections.put(
                    new EventSource(WindTurbineUserModel.MIL_URI, StartWindTurbineEvent.class),
                    new EventSink[] {
                            new EventSink(WindTurbineStateModel.MIL_URI, StartWindTurbineEvent.class)
                    }
            );

            connections.put(
                    new EventSource(WindTurbineUserModel.MIL_URI, StopWindTurbineEvent.class),
                    new EventSink[] {
                            new EventSink(WindTurbineStateModel.MIL_URI, StopWindTurbineEvent.class)
                    }
            );
            connections.put(
                    new EventSource(WindTurbineStateModel.MIL_URI, StartWindTurbineEvent.class),
                    new EventSink[] {
                            new EventSink(WindTurbineElectricityModel.MIL_URI, StartWindTurbineEvent.class)
                    }
            );

            connections.put(
                    new EventSource(WindTurbineStateModel.MIL_URI, StopWindTurbineEvent.class),
                    new EventSink[] {
                            new EventSink(WindTurbineElectricityModel.MIL_URI, StopWindTurbineEvent.class)
                    }
            );
            

            Map<VariableSource, VariableSink[]> bindings = new HashMap<>();

            bindings.put(
                    new VariableSource("externalWindSpeed",
                            Double.class,
                            ExternalWindModel.MIL_URI),
                    new VariableSink[] {
                            new VariableSink("externalWindSpeed",
                                    Double.class,
                                    WindTurbineElectricityModel.MIL_URI)
                    });

            coupledModelDescriptors.put(
                    WindTurbineCoupledModel.MIL_URI,
                    new CoupledHIOA_Descriptor(
                            WindTurbineCoupledModel.class,
                            WindTurbineCoupledModel.MIL_URI,
                            subModels,
                            null,
                            null,
                            connections,
                            null,
                            null,
                            null,
                            bindings));

            ArchitectureI architecture = new Architecture(
                            WindTurbineCoupledModel.MIL_URI,
                            atomicModelDescriptors,
                            coupledModelDescriptors,
                            TimeUnit.SECONDS
            );

            // Start the simulation
            SimulatorI engine = architecture.constructSimulator();
            SimulationEngine.SIMULATION_STEP_SLEEP_TIME = 0L;
            engine.doStandAloneSimulation(0.0, 40.0);
            System.exit(0);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
