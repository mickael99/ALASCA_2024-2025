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
import fr.sorbonne_u.devs_simulation.models.time.Duration;
import fr.sorbonne_u.devs_simulation.models.time.Time;
import fr.sorbonne_u.devs_simulation.simulators.SimulationEngine;
import fr.sorbonne_u.devs_simulation.simulators.interfaces.SimulatorI;

public class RunGeneratorUnitaryMILSimulation {

	public static final TimeUnit	TIME_UNIT = TimeUnit.HOURS;


	public static void main(String[] args) {
		Time.setPrintPrecision(4);
		Duration.setPrintPrecision(4);

		//TODO: Discuss TIME_UNIT
		try {
			Map<String, AbstractAtomicModelDescriptor> atomicModelDescriptors = new HashMap<>();

	        atomicModelDescriptors.put(
	                GeneratorElectricityModel.MIL_URI,
	                AtomicHIOA_Descriptor.create(
	                        GeneratorElectricityModel.class,
	                        GeneratorElectricityModel.MIL_URI,
	                        TimeUnit.MINUTES,
	                        null
	                )
	        );

	        atomicModelDescriptors.put(
	                GeneratorFuelModel.MIL_URI,
	                AtomicHIOA_Descriptor.create(
	                        GeneratorFuelModel.class,
	                        GeneratorFuelModel.MIL_URI,
	                        TimeUnit.MINUTES,
	                        null
	                )
	        );

	        atomicModelDescriptors.put(
	                GeneratorUserModel.MIL_URI,
	                AtomicModelDescriptor.create(
	                        GeneratorUserModel.class,
	                        GeneratorUserModel.MIL_URI,
	                        TimeUnit.MINUTES,
	                        null
	                )
	        );

	        
	        Map<String, CoupledModelDescriptor> coupledModelDescriptors = new HashMap<>();

	        Set<String> subModel = new HashSet<>();
	        subModel.add(GeneratorElectricityModel.MIL_URI);
	        subModel.add(GeneratorFuelModel.MIL_URI);
	        subModel.add(GeneratorUserModel.MIL_URI);
	        
	        
	        Map<EventSource, EventSink[]> connections = new HashMap<>();

	        connections.put(
	                new EventSource(GeneratorUserModel.MIL_URI, ActivateGeneratorEvent.class),
	                new EventSink[] {
	                        new EventSink(GeneratorElectricityModel.MIL_URI, ActivateGeneratorEvent.class),
	                        new EventSink(GeneratorFuelModel.MIL_URI, ActivateGeneratorEvent.class)
	                }
	        );

	        connections.put(
	                new EventSource(GeneratorUserModel.MIL_URI, StopGeneratorEvent.class),
	                new EventSink[] {
	                        new EventSink(GeneratorElectricityModel.MIL_URI, StopGeneratorEvent.class),
	                        new EventSink(GeneratorFuelModel.MIL_URI, StopGeneratorEvent.class),
	                }
	        );

	        connections.put(
	                new EventSource(GeneratorFuelModel.MIL_URI, StopGeneratorEvent.class),
	                new EventSink[] {
	                        new EventSink(GeneratorElectricityModel.MIL_URI, StopGeneratorEvent.class),
	                }
	        );
	        
	        
	        Map<VariableSource, VariableSink[]> bindings = new HashMap<>();

	        bindings.put(
	                new VariableSource("currentFuelLevel", Double.class, GeneratorFuelModel.MIL_URI),
	                new VariableSink[] {
	                        new VariableSink("currentFuelLevel", Double.class, GeneratorElectricityModel.MIL_URI)
	                }
	        );
	        
	        coupledModelDescriptors.put(
	                GeneratorCoupledModel.MIL_URI,
	                new CoupledHIOA_Descriptor(
	                        GeneratorCoupledModel.class,
	                        GeneratorCoupledModel.MIL_URI,
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
	                GeneratorCoupledModel.MIL_URI,
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
