package fr.sorbonne_u.components.equipments.generator.mil;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import fr.sorbonne_u.components.equipments.generator.mil.events.ActivateGeneratorEvent;
import fr.sorbonne_u.components.equipments.generator.mil.events.StopGeneratorEvent;
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

public class RunGeneratorUnitaryMILSimulation {

	public static void main(String[] args) {
		try {
			Map<String, AbstractAtomicModelDescriptor> atomicModelDescriptors = new HashMap<>();

	        atomicModelDescriptors.put(
	                GeneratorElectricityModel.URI,
	                AtomicHIOA_Descriptor.create(
	                        GeneratorElectricityModel.class,
	                        GeneratorElectricityModel.URI,
	                        TimeUnit.MINUTES,
	                        null
	                )
	        );

	        atomicModelDescriptors.put(
	                GeneratorFuelModel.URI,
	                AtomicHIOA_Descriptor.create(
	                        GeneratorFuelModel.class,
	                        GeneratorFuelModel.URI,
	                        TimeUnit.MINUTES,
	                        null
	                )
	        );

	        atomicModelDescriptors.put(
	                GeneratorUserModel.URI,
	                AtomicModelDescriptor.create(
	                        GeneratorUserModel.class,
	                        GeneratorUserModel.URI,
	                        TimeUnit.MINUTES,
	                        null
	                )
	        );

	        
	        Map<String, CoupledModelDescriptor> coupledModelDescriptors = new HashMap<>();

	        Set<String> subModel = new HashSet<>();
	        subModel.add(GeneratorElectricityModel.URI);
	        subModel.add(GeneratorFuelModel.URI);
	        subModel.add(GeneratorUserModel.URI);
	        
	        
	        Map<EventSource, EventSink[]> connections = new HashMap<>();

	        connections.put(
	                new EventSource(GeneratorUserModel.URI, ActivateGeneratorEvent.class),
	                new EventSink[] {
	                        new EventSink(GeneratorElectricityModel.URI, ActivateGeneratorEvent.class),
	                        new EventSink(GeneratorFuelModel.URI, ActivateGeneratorEvent.class)
	                }
	        );

	        connections.put(
	                new EventSource(GeneratorUserModel.URI, StopGeneratorEvent.class),
	                new EventSink[] {
	                        new EventSink(GeneratorElectricityModel.URI, StopGeneratorEvent.class),
	                        new EventSink(GeneratorFuelModel.URI, StopGeneratorEvent.class),
	                }
	        );

	        connections.put(
	                new EventSource(GeneratorFuelModel.URI, StopGeneratorEvent.class),
	                new EventSink[] {
	                        new EventSink(GeneratorElectricityModel.URI, StopGeneratorEvent.class),
	                }
	        );
	        
	        
	        Map<VariableSource, VariableSink[]> bindings = new HashMap<>();

	        bindings.put(
	                new VariableSource("currentFuelLevel", Double.class, GeneratorFuelModel.URI),
	                new VariableSink[] {
	                        new VariableSink("currentFuelLevel", Double.class, GeneratorElectricityModel.URI)
	                }
	        );
	        
	        coupledModelDescriptors.put(
	                GeneratorCoupledModel.URI,
	                new CoupledHIOA_Descriptor(
	                        GeneratorCoupledModel.class,
	                        GeneratorCoupledModel.URI,
	                        subModel,
	                        null,
	                        null,
	                        connections,
	                        null,
	                        null,
	                        null,
	                        bindings
	                )
	        );
	        
	        ArchitectureI architecture = new Architecture(
	                GeneratorCoupledModel.URI,
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
