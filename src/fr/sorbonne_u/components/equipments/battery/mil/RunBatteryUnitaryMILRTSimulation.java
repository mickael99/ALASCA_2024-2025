package fr.sorbonne_u.components.equipments.battery.mil;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import fr.sorbonne_u.components.equipments.battery.mil.events.SetConsumeBatteryEvent;
import fr.sorbonne_u.components.equipments.battery.mil.events.SetProductBatteryEvent;
import fr.sorbonne_u.components.equipments.battery.mil.events.SetStandByBatteryEvent;
import fr.sorbonne_u.devs_simulation.architectures.ArchitectureI;
import fr.sorbonne_u.devs_simulation.architectures.RTArchitecture;
import fr.sorbonne_u.devs_simulation.hioa.architectures.RTAtomicHIOA_Descriptor;
import fr.sorbonne_u.devs_simulation.hioa.architectures.RTCoupledHIOA_Descriptor;
import fr.sorbonne_u.devs_simulation.hioa.models.vars.VariableSink;
import fr.sorbonne_u.devs_simulation.hioa.models.vars.VariableSource;
import fr.sorbonne_u.devs_simulation.models.architectures.AbstractAtomicModelDescriptor;
import fr.sorbonne_u.devs_simulation.models.architectures.CoupledModelDescriptor;
import fr.sorbonne_u.devs_simulation.models.architectures.RTAtomicModelDescriptor;
import fr.sorbonne_u.devs_simulation.models.events.EventSink;
import fr.sorbonne_u.devs_simulation.models.events.EventSource;
import fr.sorbonne_u.devs_simulation.simulators.interfaces.SimulatorI;

public class RunBatteryUnitaryMILRTSimulation {
	
	public static final double ACCELERATION_FACTOR = 1800.0;
	
	public static void main(String[] args) {
        try {

            Map<String, AbstractAtomicModelDescriptor> atomicModelDescriptors = new HashMap<>();

            atomicModelDescriptors.put(
                    BatteryElectricityModel.MIL_RT_URI,
                    RTAtomicHIOA_Descriptor.create(
                    		BatteryElectricityModel.class,
                    		BatteryElectricityModel.MIL_RT_URI,
                            TimeUnit.HOURS,
                            null,
                            ACCELERATION_FACTOR
                    )
            );
            atomicModelDescriptors.put(
                    BatteryChargeLevelModel.MIL_RT_URI,
                    RTAtomicHIOA_Descriptor.create(
                    		BatteryChargeLevelModel.class,
                    		BatteryChargeLevelModel.MIL_RT_URI,
                            TimeUnit.HOURS,
                            null,
                            ACCELERATION_FACTOR
                    )
            );
            atomicModelDescriptors.put(
                    BatteryUserModel.MIL_RT_URI,
                    RTAtomicModelDescriptor.create(
                            BatteryUserModel.class,
                            BatteryUserModel.MIL_RT_URI,
                            TimeUnit.HOURS,
                            null,
                            ACCELERATION_FACTOR
                    )
            );


            Map<String, CoupledModelDescriptor> coupledModelDescriptors = new HashMap<>();

            Set<String> subModel = new HashSet<>();
            subModel.add(BatteryElectricityModel.MIL_RT_URI);
            subModel.add(BatteryChargeLevelModel.MIL_RT_URI);
            subModel.add(BatteryUserModel.MIL_RT_URI);

            Map<EventSource, EventSink[]> connections = new HashMap<>();

            connections.put(
                    new EventSource(BatteryUserModel.MIL_RT_URI, SetProductBatteryEvent.class),
                    new EventSink[] {
                            new EventSink(BatteryElectricityModel.MIL_RT_URI, SetProductBatteryEvent.class),
                            new EventSink(BatteryChargeLevelModel.MIL_RT_URI, SetProductBatteryEvent.class)
                    }
            );

            connections.put(
                    new EventSource(BatteryUserModel.MIL_RT_URI, SetConsumeBatteryEvent.class),
                    new EventSink[] {
                            new EventSink(BatteryElectricityModel.MIL_RT_URI, SetConsumeBatteryEvent.class),
                            new EventSink(BatteryChargeLevelModel.MIL_RT_URI, SetConsumeBatteryEvent.class)
                    }
            );
            
            connections.put(
                    new EventSource(BatteryUserModel.MIL_RT_URI, SetStandByBatteryEvent.class),
                    new EventSink[] {
                            new EventSink(BatteryElectricityModel.MIL_RT_URI, SetStandByBatteryEvent.class),
                            new EventSink(BatteryChargeLevelModel.MIL_RT_URI, SetStandByBatteryEvent.class)
                    }
            );

            Map<VariableSource, VariableSink[]> bindings = new HashMap<>();

            bindings.put(
                    new VariableSource("currentChargeLevel", Double.class, BatteryChargeLevelModel.MIL_RT_URI),
                    new VariableSink[] {
                            new VariableSink("currentChargeLevel", Double.class, BatteryElectricityModel.MIL_RT_URI)
                    }
            );

            coupledModelDescriptors.put(
            		BatteryCoupledModel.MIL_RT_URI,
                    new RTCoupledHIOA_Descriptor(
                            BatteryCoupledModel.class,
                            BatteryCoupledModel.MIL_RT_URI,
                            subModel,
                            null,
                            null,
                            connections,
                            null,
                            null,
                            null,
                            bindings,
                            ACCELERATION_FACTOR
                    )
            );

            ArchitectureI architecture = new RTArchitecture(
            		BatteryCoupledModel.MIL_RT_URI,
                    atomicModelDescriptors,
                    coupledModelDescriptors,
                    TimeUnit.HOURS
            );

            SimulatorI se = architecture.constructSimulator();
			long start = System.currentTimeMillis() + 100;
			double simulationDuration = 24.1;
			se.startRTSimulation(start, 0.0, simulationDuration);
			long sleepTime =
				(long)(TimeUnit.HOURS.toMillis(1) *
								(simulationDuration/ACCELERATION_FACTOR));
			Thread.sleep(sleepTime + 10000L);
			System.exit(0);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
