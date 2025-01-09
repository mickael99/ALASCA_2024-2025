package fr.sorbonne_u.components.equipments.generator.mil;

import fr.sorbonne_u.components.equipments.generator.mil.events.ActivateGeneratorEvent;
import fr.sorbonne_u.components.equipments.generator.mil.events.StopGeneratorEvent;
import fr.sorbonne_u.devs_simulation.architectures.Architecture;
import fr.sorbonne_u.devs_simulation.architectures.ArchitectureI;
import fr.sorbonne_u.devs_simulation.hioa.architectures.AtomicHIOA_Descriptor;
import fr.sorbonne_u.devs_simulation.hioa.architectures.CoupledHIOA_Descriptor;
import fr.sorbonne_u.devs_simulation.hioa.architectures.RTCoupledHIOA_Descriptor;
import fr.sorbonne_u.devs_simulation.hioa.models.vars.VariableSink;
import fr.sorbonne_u.devs_simulation.hioa.models.vars.VariableSource;
import fr.sorbonne_u.devs_simulation.models.architectures.AbstractAtomicModelDescriptor;
import fr.sorbonne_u.devs_simulation.models.architectures.AtomicModelDescriptor;
import fr.sorbonne_u.devs_simulation.models.architectures.CoupledModelDescriptor;
import fr.sorbonne_u.devs_simulation.models.architectures.RTCoupledModelDescriptor;
import fr.sorbonne_u.devs_simulation.models.events.EventSink;
import fr.sorbonne_u.devs_simulation.models.events.EventSource;
import fr.sorbonne_u.devs_simulation.simulators.SimulationEngine;
import fr.sorbonne_u.devs_simulation.simulators.interfaces.SimulatorI;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class RunGeneratorUnitaryMILRTSimulation {

    //TODO: Discuss TIME_UNIT
    public static final TimeUnit	TIME_UNIT = TimeUnit.MINUTES;
    /** the acceleration factor used in the real time MIL simulations.	 	*/
    public static final double		ACCELERATION_FACTOR = 3600.0;

    public static void main(String[] args) {
        try{
            Map<String, AbstractAtomicModelDescriptor> atomicModelDescriptors = new HashMap<>();
    
            atomicModelDescriptors.put(
                    GeneratorElectricityModel.MIL_RT_URI,
                    AtomicHIOA_Descriptor.create(
                            GeneratorElectricityModel.class,
                            GeneratorElectricityModel.MIL_RT_URI,
                            TimeUnit.MINUTES,
                            null
                                                )
                                      );
    
            atomicModelDescriptors.put(
                    GeneratorFuelModel.MIL_RT_URI,
                    AtomicHIOA_Descriptor.create(
                            GeneratorFuelModel.class,
                            GeneratorFuelModel.MIL_RT_URI,
                            TimeUnit.MINUTES,
                            null
                                                )
                                      );
    
            atomicModelDescriptors.put(
                    GeneratorUserModel.MIL_RT_URI,
                    AtomicModelDescriptor.create(
                            GeneratorUserModel.class,
                            GeneratorUserModel.MIL_RT_URI,
                            TimeUnit.MINUTES,
                            null
                                                )
                                      );
    
    
            Map<String, CoupledModelDescriptor> coupledModelDescriptors = new HashMap<>();
    
            Set<String> subModel = new HashSet<>();
            subModel.add(GeneratorElectricityModel.MIL_RT_URI);
            subModel.add(GeneratorFuelModel.MIL_RT_URI);
            subModel.add(GeneratorUserModel.MIL_RT_URI);
    
    
            Map<EventSource, EventSink[]> connections = new HashMap<>();
    
            connections.put(
                    new EventSource(GeneratorUserModel.MIL_RT_URI, ActivateGeneratorEvent.class),
                    new EventSink[] {
                            new EventSink(GeneratorElectricityModel.MIL_RT_URI, ActivateGeneratorEvent.class),
                            new EventSink(GeneratorFuelModel.MIL_RT_URI, ActivateGeneratorEvent.class)
                    }
                           );
    
            connections.put(
                    new EventSource(GeneratorUserModel.MIL_RT_URI, StopGeneratorEvent.class),
                    new EventSink[] {
                            new EventSink(GeneratorElectricityModel.MIL_RT_URI, StopGeneratorEvent.class),
                            new EventSink(GeneratorFuelModel.MIL_RT_URI, StopGeneratorEvent.class),
                            }
                           );
    
            connections.put(
                    new EventSource(GeneratorFuelModel.MIL_RT_URI, StopGeneratorEvent.class),
                    new EventSink[] {
                            new EventSink(GeneratorElectricityModel.MIL_RT_URI, StopGeneratorEvent.class),
                            }
                           );
    
    
            Map<VariableSource, VariableSink[]> bindings = new HashMap<>();
    
            bindings.put(
                    new VariableSource("currentFuelLevel", Double.class, GeneratorFuelModel.MIL_RT_URI),
                    new VariableSink[] {
                            new VariableSink("currentFuelLevel", Double.class, GeneratorElectricityModel.MIL_RT_URI)
                    }
                        );
    
            coupledModelDescriptors.put(
                    GeneratorCoupledModel.MIL_RT_URI,
                    new RTCoupledHIOA_Descriptor(
                            GeneratorCoupledModel.class,
                            GeneratorCoupledModel.MIL_RT_URI,
                            subModel,
                            null,
                            null,
                            connections,
                            null,
                            null,
                            null,
                            bindings,
                            ACCELERATION_FACTOR
                    ));
    
            ArchitectureI architecture = new Architecture(
                    GeneratorCoupledModel.MIL_RT_URI,
                    atomicModelDescriptors,
                    coupledModelDescriptors,
                    TimeUnit.MINUTES
            );
    
            SimulatorI engine = architecture.constructSimulator();
            SimulationEngine.SIMULATION_STEP_SLEEP_TIME = 0L;
            engine.doStandAloneSimulation(0.0, 120.0);
            System.exit(0);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
