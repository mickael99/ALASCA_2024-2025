package fr.sorbonne_u.components.equipments.iron.mil;

import fr.sorbonne_u.devs_simulation.models.time.Duration;
import fr.sorbonne_u.devs_simulation.models.time.Time;
import fr.sorbonne_u.devs_simulation.simulators.SimulationEngine;
import fr.sorbonne_u.devs_simulation.simulators.interfaces.SimulationReportI;
import fr.sorbonne_u.devs_simulation.simulators.interfaces.SimulatorI;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import fr.sorbonne_u.components.equipments.iron.mil.events.*;
import fr.sorbonne_u.devs_simulation.architectures.Architecture;
import fr.sorbonne_u.devs_simulation.architectures.ArchitectureI;
import fr.sorbonne_u.devs_simulation.hioa.architectures.AtomicHIOA_Descriptor;
import fr.sorbonne_u.devs_simulation.models.architectures.AbstractAtomicModelDescriptor;
import fr.sorbonne_u.devs_simulation.models.architectures.AtomicModelDescriptor;
import fr.sorbonne_u.devs_simulation.models.architectures.CoupledModelDescriptor;
import fr.sorbonne_u.devs_simulation.models.events.EventSink;
import fr.sorbonne_u.devs_simulation.models.events.EventSource;


public class RunIronUnitaryMILSimulation {
	
	public static void main(String[] args) {
		Time.setPrintPrecision(4);
		Duration.setPrintPrecision(4);
		
		try {
			Map<String, AbstractAtomicModelDescriptor> atomicModelDescriptors = new HashMap<>();
			
			atomicModelDescriptors.put(
					IronStateModel.MIL_URI,
					AtomicModelDescriptor.create(
							IronStateModel.class,
							IronStateModel.MIL_URI,
							TimeUnit.HOURS,
							null));
			atomicModelDescriptors.put(
					IronElectricityModel.MIL_URI,
					AtomicHIOA_Descriptor.create(
							IronElectricityModel.class,
							IronElectricityModel.MIL_URI,
							TimeUnit.HOURS,
							null));
			atomicModelDescriptors.put(
					IronUserModel.MIL_URI,
					AtomicModelDescriptor.create(
							IronUserModel.class,
							IronUserModel.MIL_URI,
							TimeUnit.HOURS,
							null));
			
			Map<String,CoupledModelDescriptor> coupledModelDescriptors = new HashMap<>();
			Set<String> submodels = new HashSet<String>();
			submodels.add(IronStateModel.MIL_URI);
			submodels.add(IronElectricityModel.MIL_URI);
			submodels.add(IronUserModel.MIL_URI);
			
			Map<EventSource,EventSink[]> connections = new HashMap<EventSource,EventSink[]>();
			
			// User model -> State model
			connections.put(
	                new EventSource(IronUserModel.MIL_URI, TurnOnIron.class),
	                new EventSink[] {
	                    new EventSink(IronStateModel.MIL_URI, TurnOnIron.class)
	                });
            connections.put(
                new EventSource(IronUserModel.MIL_URI, TurnOffIron.class),
                new EventSink[] {
                    new EventSink(IronStateModel.MIL_URI, TurnOffIron.class)
                });
            connections.put(
                new EventSource(IronUserModel.MIL_URI, EnableDelicateModeIron.class),
                new EventSink[] {
                    new EventSink(IronStateModel.MIL_URI, EnableDelicateModeIron.class)
                });
            connections.put(
                new EventSource(IronUserModel.MIL_URI, EnableCottonModeIron.class),
                new EventSink[] {
                    new EventSink(IronStateModel.MIL_URI, EnableCottonModeIron.class)
                });
            connections.put(
                new EventSource(IronUserModel.MIL_URI, EnableLinenModeIron.class),
                new EventSink[] {
                    new EventSink(IronStateModel.MIL_URI, EnableLinenModeIron.class)
                });
            connections.put(
                    new EventSource(IronUserModel.MIL_URI, EnableEnergySavingModeIron.class),
                    new EventSink[] {
                        new EventSink(IronStateModel.MIL_URI, EnableEnergySavingModeIron.class)
                    });
            connections.put(
                new EventSource(IronUserModel.MIL_URI, DisableEnergySavingModeIron.class),
                new EventSink[] {
                    new EventSink(IronStateModel.MIL_URI, DisableEnergySavingModeIron.class)
                });
            connections.put(
                new EventSource(IronUserModel.MIL_URI, EnableSteamModeIron.class),
                new EventSink[] {
                    new EventSink(IronStateModel.MIL_URI, EnableSteamModeIron.class)
                });
            connections.put(
                new EventSource(IronUserModel.MIL_URI, DisableSteamModeIron.class),
                new EventSink[] {
                    new EventSink(IronStateModel.MIL_URI, DisableSteamModeIron.class)
                });
			
			// State model -> Electricity model
	            connections.put(
                    new EventSource(IronStateModel.MIL_URI, TurnOnIron.class),
                    new EventSink[] {
                        new EventSink(IronElectricityModel.MIL_URI, TurnOnIron.class)
                    });
                connections.put(
                    new EventSource(IronStateModel.MIL_URI, TurnOffIron.class),
                    new EventSink[] {
                        new EventSink(IronElectricityModel.MIL_URI, TurnOffIron.class)
                    });
                connections.put(
                    new EventSource(IronStateModel.MIL_URI, EnableDelicateModeIron.class),
                    new EventSink[] {
                        new EventSink(IronElectricityModel.MIL_URI, EnableDelicateModeIron.class)
                    });
                connections.put(
                    new EventSource(IronStateModel.MIL_URI, EnableCottonModeIron.class),
                    new EventSink[] {
                        new EventSink(IronElectricityModel.MIL_URI, EnableCottonModeIron.class)
                    });
                connections.put(
                    new EventSource(IronStateModel.MIL_URI, EnableLinenModeIron.class),
                    new EventSink[] {
                        new EventSink(IronElectricityModel.MIL_URI, EnableLinenModeIron.class)
                    });
                connections.put(
                        new EventSource(IronStateModel.MIL_URI, EnableEnergySavingModeIron.class),
                        new EventSink[] {
                            new EventSink(IronElectricityModel.MIL_URI, EnableEnergySavingModeIron.class)
                        });
                connections.put(
                    new EventSource(IronStateModel.MIL_URI, DisableEnergySavingModeIron.class),
                    new EventSink[] {
                        new EventSink(IronElectricityModel.MIL_URI, DisableEnergySavingModeIron.class)
                    });
                connections.put(
                    new EventSource(IronStateModel.MIL_URI, EnableSteamModeIron.class),
                    new EventSink[] {
                        new EventSink(IronElectricityModel.MIL_URI, EnableSteamModeIron.class)
                    });
                connections.put(
                    new EventSource(IronStateModel.MIL_URI, DisableSteamModeIron.class),
                    new EventSink[] {
                        new EventSink(IronElectricityModel.MIL_URI, DisableSteamModeIron.class)
                    });
			
			coupledModelDescriptors.put(
					IronCoupledModel.MIL_URI,
					new CoupledModelDescriptor(
							IronCoupledModel.class,
							IronCoupledModel.MIL_URI,
							submodels,
							null,
							null,
							connections,
							null));
			
			ArchitectureI architecture =
					new Architecture(
							IronCoupledModel.MIL_URI,
							atomicModelDescriptors,
							coupledModelDescriptors,
							TimeUnit.HOURS);
			
			SimulatorI se = architecture.constructSimulator();
			SimulationEngine.SIMULATION_STEP_SLEEP_TIME = 0L;
			se.doStandAloneSimulation(0.0, 24.0); 
			SimulationReportI sr = se.getSimulatedModel().getFinalReport();
			System.out.println(sr);
			
			System.exit(0);
			
		}
		catch(Exception e) {
			throw new RuntimeException(e);
		}
	}
}
