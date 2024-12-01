package fr.sorbonne_u.components.equipments.battery.mil;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import fr.sorbonne_u.components.equipments.battery.mil.events.SetConsumeBatteryEvent;
import fr.sorbonne_u.components.equipments.battery.mil.events.SetProductBatteryEvent;
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

public class RunBatteryUnitaryMILSimulation {
	 public static void main(String[] args) {
	        try {

	            Map<String, AbstractAtomicModelDescriptor> atomicModelDescriptors = new HashMap<>();

	            atomicModelDescriptors.put(
	                    BatteryElectricityModel.URI,
	                    AtomicHIOA_Descriptor.create(
	                    		BatteryElectricityModel.class,
	                    		BatteryElectricityModel.URI,
	                            TimeUnit.SECONDS,
	                            null
	                    )
	            );

	            atomicModelDescriptors.put(
	                    BatteryChargeLevelModel.URI,
	                    AtomicHIOA_Descriptor.create(
	                    		BatteryChargeLevelModel.class,
	                    		BatteryChargeLevelModel.URI,
	                            TimeUnit.SECONDS,
	                            null
	                    )
	            );

	            atomicModelDescriptors.put(
	                    BatteryUserModel.URI,
	                    AtomicModelDescriptor.create(
	                            BatteryUserModel.class,
	                            BatteryUserModel.URI,
	                            TimeUnit.SECONDS,
	                            null
	                    )
	            );


	            Map<String, CoupledModelDescriptor> coupledModelDescriptors = new HashMap<>();

	            Set<String> subModel = new HashSet<>();
	            subModel.add(BatteryElectricityModel.URI);
	            subModel.add(BatteryChargeLevelModel.URI);
	            subModel.add(BatteryUserModel.URI);

	            Map<EventSource, EventSink[]> connections = new HashMap<>();

	            connections.put(
	                    new EventSource(BatteryUserModel.URI, SetProductBatteryEvent.class),
	                    new EventSink[] {
	                            new EventSink(BatteryElectricityModel.URI, SetProductBatteryEvent.class),
	                            new EventSink(BatteryChargeLevelModel.URI, SetProductBatteryEvent.class)
	                    }
	            );

	            connections.put(
	                    new EventSource(BatteryUserModel.URI, SetConsumeBatteryEvent.class),
	                    new EventSink[] {
	                            new EventSink(BatteryElectricityModel.URI, SetConsumeBatteryEvent.class),
	                            new EventSink(BatteryChargeLevelModel.URI, SetConsumeBatteryEvent.class)
	                    }
	            );

	            Map<VariableSource, VariableSink[]> bindings = new HashMap<>();

	            bindings.put(
	                    new VariableSource("currentChargeLevel", Double.class, BatteryChargeLevelModel.URI),
	                    new VariableSink[] {
	                            new VariableSink("currentChargeLevel", Double.class, BatteryElectricityModel.URI)
	                    }
	            );

	            coupledModelDescriptors.put(
	            		BatteryCoupledModel.URI,
	                    new CoupledHIOA_Descriptor(
	                            BatteryCoupledModel.class,
	                            BatteryCoupledModel.URI,
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
	            		BatteryCoupledModel.URI,
	                    atomicModelDescriptors,
	                    coupledModelDescriptors,
	                    TimeUnit.SECONDS
	            );

	            SimulatorI engine = architecture.constructSimulator();
	            SimulationEngine.SIMULATION_STEP_SLEEP_TIME = 0L;
	            engine.doStandAloneSimulation(0.0, 100.0);
	            System.exit(0);

	        } catch (Exception e) {
	            throw new RuntimeException(e);
	        }
	    }
}
