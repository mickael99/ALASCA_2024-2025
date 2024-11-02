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
					IronElectricityModel.URI,
					AtomicHIOA_Descriptor.create(
							IronElectricityModel.class,
							IronElectricityModel.URI,
							TimeUnit.HOURS,
							null));
			atomicModelDescriptors.put(
					IronUserModel.URI,
					AtomicModelDescriptor.create(
							IronUserModel.class,
							IronUserModel.URI,
							TimeUnit.HOURS,
							null));
			
			Map<String,CoupledModelDescriptor> coupledModelDescriptors = new HashMap<>();
			Set<String> submodels = new HashSet<String>();
			submodels.add(IronElectricityModel.URI);
			submodels.add(IronUserModel.URI);
			
			Map<EventSource,EventSink[]> connections = new HashMap<EventSource,EventSink[]>();
			connections.put(
					new EventSource(IronUserModel.URI, TurnOnIron.class),
					new EventSink[] {
							new EventSink(IronElectricityModel.URI,
										  TurnOnIron.class)
					});
			connections.put(
					new EventSource(IronUserModel.URI, TurnOffIron.class),
					new EventSink[] {
							new EventSink(IronElectricityModel.URI,
									TurnOffIron.class)
					});
			connections.put(
					new EventSource(IronUserModel.URI, EnableDelicateModeIron.class),
					new EventSink[] {
							new EventSink(IronElectricityModel.URI,
									EnableDelicateModeIron.class)
					});
			connections.put(
					new EventSource(IronUserModel.URI, EnableCottonModeIron.class),
					new EventSink[] {
							new EventSink(IronElectricityModel.URI,
										EnableCottonModeIron.class)
					});
			connections.put(
					new EventSource(IronUserModel.URI, EnableLinenModeIron.class),
					new EventSink[] {
							new EventSink(IronElectricityModel.URI,
									EnableLinenModeIron.class)
					});
			connections.put(
					new EventSource(IronUserModel.URI, EnableEnergySavingModeIron.class),
					new EventSink[] {
							new EventSink(IronElectricityModel.URI,
									EnableEnergySavingModeIron.class)
					});
			connections.put(
					new EventSource(IronUserModel.URI, EnableSteamModeIron.class),
					new EventSink[] {
							new EventSink(IronElectricityModel.URI,
									EnableSteamModeIron.class)
					});
			connections.put(
					new EventSource(IronUserModel.URI, DisableEnergySavingModeIron.class),
					new EventSink[] {
							new EventSink(IronElectricityModel.URI,
									DisableEnergySavingModeIron.class)
					});
			connections.put(
					new EventSource(IronUserModel.URI, DisableSteamModeIron.class),
					new EventSink[] {
							new EventSink(IronElectricityModel.URI,
									DisableSteamModeIron.class)
					});
			
			coupledModelDescriptors.put(
					IronCoupleModel.URI,
					new CoupledModelDescriptor(
							IronCoupleModel.class,
							IronCoupleModel.URI,
							submodels,
							null,
							null,
							connections,
							null));
			
			ArchitectureI architecture =
					new Architecture(
							IronCoupleModel.URI,
							atomicModelDescriptors,
							coupledModelDescriptors,
							TimeUnit.HOURS);
			
			SimulatorI se = architecture.constructSimulator();
			SimulationEngine.SIMULATION_STEP_SLEEP_TIME = 0L;
			se.doStandAloneSimulation(0.0, 24.0); // Bug ici
			SimulationReportI sr = se.getSimulatedModel().getFinalReport();
			System.out.println(sr);
			
			System.exit(0);
			
		}
		catch(Exception e) {
			throw new RuntimeException(e);
		}
	}
}
