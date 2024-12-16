package fr.sorbonne_u.components.equipments.iron.mil;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import fr.sorbonne_u.components.equipments.iron.sil.IronStateModel;
import fr.sorbonne_u.components.equipments.iron.mil.events.*;
import fr.sorbonne_u.devs_simulation.architectures.ArchitectureI;
import fr.sorbonne_u.devs_simulation.architectures.RTArchitecture;
import fr.sorbonne_u.devs_simulation.hioa.architectures.RTAtomicHIOA_Descriptor;
import fr.sorbonne_u.devs_simulation.models.architectures.AbstractAtomicModelDescriptor;
import fr.sorbonne_u.devs_simulation.models.architectures.CoupledModelDescriptor;
import fr.sorbonne_u.devs_simulation.models.architectures.RTAtomicModelDescriptor;
import fr.sorbonne_u.devs_simulation.models.architectures.RTCoupledModelDescriptor;
import fr.sorbonne_u.devs_simulation.models.events.EventSink;
import fr.sorbonne_u.devs_simulation.models.events.EventSource;
import fr.sorbonne_u.devs_simulation.models.time.Duration;
import fr.sorbonne_u.devs_simulation.models.time.Time;
import fr.sorbonne_u.devs_simulation.simulators.SimulationEngine;
import fr.sorbonne_u.devs_simulation.simulators.interfaces.SimulationReportI;
import fr.sorbonne_u.devs_simulation.simulators.interfaces.SimulatorI;

public class RunIronUnitaryMILRTSimulation {

	public static final TimeUnit TIME_UNIT = TimeUnit.HOURS;
    public static final double ACCELERATION_FACTOR = 3600.0;
    
	public static void main(String[] args) {
		Time.setPrintPrecision(4);
        Duration.setPrintPrecision(4);

        try {
        	Map<String, AbstractAtomicModelDescriptor> atomicModelDescriptors = new HashMap<>();

            atomicModelDescriptors.put(
                    IronElectricityModel.MIL_RT_URI,
                    RTAtomicHIOA_Descriptor.create(
                            IronElectricityModel.class,
                            IronElectricityModel.MIL_RT_URI,
                            TIME_UNIT,
                            null,
                            ACCELERATION_FACTOR));

            atomicModelDescriptors.put(
                    IronStateModel.MIL_RT_URI,
                    RTAtomicModelDescriptor.create(
                            IronStateModel.class,
                            IronStateModel.MIL_RT_URI,
                            TIME_UNIT,
                            null,
                            ACCELERATION_FACTOR));

            atomicModelDescriptors.put(
                    IronUserModel.MIL_RT_URI,
                    RTAtomicModelDescriptor.create(
                            IronUserModel.class,
                            IronUserModel.MIL_RT_URI,
                            TIME_UNIT,
                            null,
                            ACCELERATION_FACTOR));

            Map<String, CoupledModelDescriptor> coupledModelDescriptors = new HashMap<>();

            Set<String> submodels = new HashSet<>();
            submodels.add(IronElectricityModel.MIL_RT_URI);
            submodels.add(IronStateModel.MIL_RT_URI);
            submodels.add(IronUserModel.MIL_RT_URI);

            Map<EventSource, EventSink[]> connections = new HashMap<>();

            // User model -> State model
            connections.put(
                new EventSource(IronUserModel.MIL_RT_URI, TurnOnIron.class),
                new EventSink[] {
                    new EventSink(IronStateModel.MIL_RT_URI, TurnOnIron.class)
                });
            connections.put(
                new EventSource(IronUserModel.MIL_RT_URI, TurnOffIron.class),
                new EventSink[] {
                    new EventSink(IronStateModel.MIL_RT_URI, TurnOffIron.class)
                });
            connections.put(
                new EventSource(IronUserModel.MIL_RT_URI, EnableDelicateModeIron.class),
                new EventSink[] {
                    new EventSink(IronStateModel.MIL_RT_URI, EnableDelicateModeIron.class)
                });
            connections.put(
                new EventSource(IronUserModel.MIL_RT_URI, EnableCottonModeIron.class),
                new EventSink[] {
                    new EventSink(IronStateModel.MIL_RT_URI, EnableCottonModeIron.class)
                });
            connections.put(
                new EventSource(IronUserModel.MIL_RT_URI, EnableLinenModeIron.class),
                new EventSink[] {
                    new EventSink(IronStateModel.MIL_RT_URI, EnableLinenModeIron.class)
                });
            connections.put(
                    new EventSource(IronUserModel.MIL_RT_URI, EnableEnergySavingModeIron.class),
                    new EventSink[] {
                        new EventSink(IronStateModel.MIL_RT_URI, EnableEnergySavingModeIron.class)
                    });
            connections.put(
                new EventSource(IronUserModel.MIL_RT_URI, DisableEnergySavingModeIron.class),
                new EventSink[] {
                    new EventSink(IronStateModel.MIL_RT_URI, DisableEnergySavingModeIron.class)
                });
            connections.put(
                new EventSource(IronUserModel.MIL_RT_URI, EnableSteamModeIron.class),
                new EventSink[] {
                    new EventSink(IronStateModel.MIL_RT_URI, EnableSteamModeIron.class)
                });
            connections.put(
                new EventSource(IronUserModel.MIL_RT_URI, DisableSteamModeIron.class),
                new EventSink[] {
                    new EventSink(IronStateModel.MIL_RT_URI, DisableSteamModeIron.class)
                });

            // State model -> Electricity model
            connections.put(
                new EventSource(IronStateModel.MIL_RT_URI, TurnOnIron.class),
                new EventSink[] {
                    new EventSink(IronElectricityModel.MIL_RT_URI, TurnOnIron.class)
                });
            connections.put(
                new EventSource(IronStateModel.MIL_RT_URI, TurnOffIron.class),
                new EventSink[] {
                    new EventSink(IronElectricityModel.MIL_RT_URI, TurnOffIron.class)
                });
            connections.put(
                new EventSource(IronStateModel.MIL_RT_URI, EnableDelicateModeIron.class),
                new EventSink[] {
                    new EventSink(IronElectricityModel.MIL_RT_URI, EnableDelicateModeIron.class)
                });
            connections.put(
                new EventSource(IronStateModel.MIL_RT_URI, EnableCottonModeIron.class),
                new EventSink[] {
                    new EventSink(IronElectricityModel.MIL_RT_URI, EnableCottonModeIron.class)
                });
            connections.put(
                new EventSource(IronStateModel.MIL_RT_URI, EnableLinenModeIron.class),
                new EventSink[] {
                    new EventSink(IronElectricityModel.MIL_RT_URI, EnableLinenModeIron.class)
                });
            connections.put(
                    new EventSource(IronStateModel.MIL_RT_URI, EnableEnergySavingModeIron.class),
                    new EventSink[] {
                        new EventSink(IronElectricityModel.MIL_RT_URI, EnableEnergySavingModeIron.class)
                    });
            connections.put(
                new EventSource(IronStateModel.MIL_RT_URI, DisableEnergySavingModeIron.class),
                new EventSink[] {
                    new EventSink(IronElectricityModel.MIL_RT_URI, DisableEnergySavingModeIron.class)
                });
            connections.put(
                new EventSource(IronStateModel.MIL_RT_URI, EnableSteamModeIron.class),
                new EventSink[] {
                    new EventSink(IronElectricityModel.MIL_RT_URI, EnableSteamModeIron.class)
                });
            connections.put(
                new EventSource(IronStateModel.MIL_RT_URI, DisableSteamModeIron.class),
                new EventSink[] {
                    new EventSink(IronElectricityModel.MIL_RT_URI, DisableSteamModeIron.class)
                });

            coupledModelDescriptors.put(
                    IronCoupledModel.MIL_RT_URI,
                    new RTCoupledModelDescriptor(
                            IronCoupledModel.class,
                            IronCoupledModel.MIL_RT_URI,
                            submodels,
                            null,
                            null,
                            connections,
                            null,
                            ACCELERATION_FACTOR));

            ArchitectureI architecture = new RTArchitecture(
                    IronCoupledModel.MIL_RT_URI,
                    atomicModelDescriptors,
                    coupledModelDescriptors,
                    TIME_UNIT);

            SimulatorI se = architecture.constructSimulator();
            SimulationEngine.SIMULATION_STEP_SLEEP_TIME = 0L;

            long start = System.currentTimeMillis() + 100;
            double simulationDuration = 24.0; // 24 hours of simulation
            se.startRTSimulation(start, 0.0, simulationDuration);
            long executionDuration = new Double(TIME_UNIT.toMillis(1) * (simulationDuration / ACCELERATION_FACTOR)).longValue();
            Thread.sleep(executionDuration + 2000L);

            // Final report
            SimulationReportI sr = se.getSimulatedModel().getFinalReport();
            System.out.println("Final report:\n" + sr);

            Thread.sleep(10000L);
            
            System.exit(0);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}


