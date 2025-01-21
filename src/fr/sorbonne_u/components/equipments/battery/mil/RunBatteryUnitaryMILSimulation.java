package fr.sorbonne_u.components.equipments.battery.mil;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import fr.sorbonne_u.components.equipments.battery.mil.events.SetConsumeBatteryEvent;
import fr.sorbonne_u.components.equipments.battery.mil.events.SetProductBatteryEvent;
import fr.sorbonne_u.components.equipments.battery.mil.events.SetStandByBatteryEvent;
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
	                    BatteryElectricityModel.MIL_URI,
	                    AtomicHIOA_Descriptor.create(
	                    		BatteryElectricityModel.class,
	                    		BatteryElectricityModel.MIL_URI,
	                            TimeUnit.HOURS,
	                            null
	                    )
	            );
	            atomicModelDescriptors.put(
	                    BatteryChargeLevelModel.MIL_URI,
	                    AtomicHIOA_Descriptor.create(
	                    		BatteryChargeLevelModel.class,
	                    		BatteryChargeLevelModel.MIL_URI,
	                            TimeUnit.HOURS,
	                            null
	                    )
	            );
	            atomicModelDescriptors.put(
	                    BatteryUserModel.MIL_URI,
	                    AtomicModelDescriptor.create(
	                            BatteryUserModel.class,
	                            BatteryUserModel.MIL_URI,
	                            TimeUnit.HOURS,
	                            null
	                    )
	            );
	            atomicModelDescriptors.put(
	                    BatteryStateModel.MIL_URI,
	                    AtomicModelDescriptor.create(
	                    		BatteryStateModel.class,
	                    		BatteryStateModel.MIL_URI,
	                            TimeUnit.HOURS,
	                            null
	                    )
	            );


	            Map<String, CoupledModelDescriptor> coupledModelDescriptors = new HashMap<>();

	            Set<String> subModel = new HashSet<>();
	            subModel.add(BatteryElectricityModel.MIL_URI);
	            subModel.add(BatteryChargeLevelModel.MIL_URI);
	            subModel.add(BatteryUserModel.MIL_URI);
	            subModel.add(BatteryStateModel.MIL_URI);

	            Map<EventSource, EventSink[]> connections = new HashMap<>();
	            
	            // User -> State
	            connections.put(
	                    new EventSource(BatteryUserModel.MIL_URI, SetProductBatteryEvent.class),
	                    new EventSink[] {
	                            new EventSink(BatteryStateModel.MIL_URI, SetProductBatteryEvent.class),
	                    }
	            );
	            connections.put(
	                    new EventSource(BatteryUserModel.MIL_URI, SetConsumeBatteryEvent.class),
	                    new EventSink[] {
	                            new EventSink(BatteryStateModel.MIL_URI, SetConsumeBatteryEvent.class),
	                    }
	            );
	            connections.put(
	                    new EventSource(BatteryUserModel.MIL_URI, SetStandByBatteryEvent.class),
	                    new EventSink[] {
	                            new EventSink(BatteryStateModel.MIL_URI, SetStandByBatteryEvent.class),
	                    }
	            );
	            
	            
	            // State -> Electricity
	            // State -> Charge level
	            
	            connections.put(
	                    new EventSource(BatteryStateModel.MIL_URI, SetProductBatteryEvent.class),
	                    new EventSink[] {
	                            new EventSink(BatteryElectricityModel.MIL_URI, SetProductBatteryEvent.class),
	                            new EventSink(BatteryChargeLevelModel.MIL_URI, SetProductBatteryEvent.class)
	                    }
	            );

	            connections.put(
	                    new EventSource(BatteryStateModel.MIL_URI, SetConsumeBatteryEvent.class),
	                    new EventSink[] {
	                            new EventSink(BatteryElectricityModel.MIL_URI, SetConsumeBatteryEvent.class),
	                            new EventSink(BatteryChargeLevelModel.MIL_URI, SetConsumeBatteryEvent.class)
	                    }
	            );
	            
	            connections.put(
	                    new EventSource(BatteryStateModel.MIL_URI, SetStandByBatteryEvent.class),
	                    new EventSink[] {
	                            new EventSink(BatteryElectricityModel.MIL_URI, SetStandByBatteryEvent.class),
	                            new EventSink(BatteryChargeLevelModel.MIL_URI, SetStandByBatteryEvent.class)
	                    }
	            );

	            Map<VariableSource, VariableSink[]> bindings = new HashMap<>();

	            bindings.put(
	                    new VariableSource("currentChargeLevel", Double.class, BatteryChargeLevelModel.MIL_URI),
	                    new VariableSink[] {
	                            new VariableSink("currentChargeLevel", Double.class, BatteryElectricityModel.MIL_URI)
	                    }
	            );

	            coupledModelDescriptors.put(
	            		BatteryCoupledModel.MIL_URI,
	                    new CoupledHIOA_Descriptor(
	                            BatteryCoupledModel.class,
	                            BatteryCoupledModel.MIL_URI,
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
	            		BatteryCoupledModel.MIL_URI,
	                    atomicModelDescriptors,
	                    coupledModelDescriptors,
	                    TimeUnit.HOURS
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
