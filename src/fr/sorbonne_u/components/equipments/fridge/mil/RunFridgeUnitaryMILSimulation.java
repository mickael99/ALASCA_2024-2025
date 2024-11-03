package fr.sorbonne_u.components.equipments.fridge.mil;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import fr.sorbonne_u.components.equipments.fridge.mil.events.*;
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

public class RunFridgeUnitaryMILSimulation {
	
	public static void main(String[] args)
	{
		try {
			Map<String,AbstractAtomicModelDescriptor> atomicModelDescriptors =
															new HashMap<>();

			atomicModelDescriptors.put(
					FridgeElectricityModel.URI,
					AtomicHIOA_Descriptor.create(
							FridgeElectricityModel.class,
							FridgeElectricityModel.URI,
							TimeUnit.HOURS,
							null));
			atomicModelDescriptors.put(
					FridgeTemperatureModel.URI,
					AtomicHIOA_Descriptor.create(
							FridgeTemperatureModel.class,
							FridgeTemperatureModel.URI,
							TimeUnit.HOURS,
							null));
			atomicModelDescriptors.put(
					ExternalTemperatureModel.URI,
					AtomicHIOA_Descriptor.create(
							ExternalTemperatureModel.class,
							ExternalTemperatureModel.URI,
							TimeUnit.HOURS,
							null));
			atomicModelDescriptors.put(
					FridgeUnitTestModel.URI,
					AtomicModelDescriptor.create(
							FridgeUnitTestModel.class,
							FridgeUnitTestModel.URI,
							TimeUnit.HOURS,
							null));

			Map<String,CoupledModelDescriptor> coupledModelDescriptors = new HashMap<>();

			Set<String> submodels = new HashSet<String>();
			submodels.add(FridgeElectricityModel.URI);
			submodels.add(FridgeTemperatureModel.URI);
			submodels.add(ExternalTemperatureModel.URI);
			submodels.add(FridgeUnitTestModel.URI);
			
			Map<EventSource,EventSink[]> connections = new HashMap<EventSource,EventSink[]>();

			connections.put(
					new EventSource(FridgeUnitTestModel.URI,
									SetPowerFridge.class),
					new EventSink[] {
							new EventSink(FridgeElectricityModel.URI,
										  SetPowerFridge.class)
					});
			connections.put(
					new EventSource(FridgeUnitTestModel.URI,
									SwitchOnFridge.class),
					new EventSink[] {
							new EventSink(FridgeElectricityModel.URI,
										  SwitchOnFridge.class)
					});
			connections.put(
					new EventSource(FridgeUnitTestModel.URI,
									SwitchOffFridge.class),
					new EventSink[] {
							new EventSink(FridgeElectricityModel.URI,
										  SwitchOffFridge.class),
							new EventSink(FridgeTemperatureModel.URI,
										  SwitchOffFridge.class)
					});
			connections.put(
					new EventSource(FridgeUnitTestModel.URI, Cool.class),
					new EventSink[] {
							new EventSink(FridgeElectricityModel.URI,
										  Cool.class),
							new EventSink(FridgeTemperatureModel.URI,
										  Cool.class)
					});
			connections.put(
					new EventSource(FridgeUnitTestModel.URI, DoNotCool.class),
					new EventSink[] {
							new EventSink(FridgeElectricityModel.URI,
										  DoNotCool.class),
							new EventSink(FridgeTemperatureModel.URI,
										  DoNotCool.class)
					});

			// variable bindings between exporting and importing models
			Map<VariableSource,VariableSink[]> bindings =
								new HashMap<VariableSource,VariableSink[]>();

			bindings.put(new VariableSource("externalTemperature",
											Double.class,
											ExternalTemperatureModel.URI),
						 new VariableSink[] {
								 new VariableSink("externalTemperature",
										 		  Double.class,
										 		  FridgeTemperatureModel.URI)
						 });
			bindings.put(new VariableSource("currentCoolingPower",
											Double.class,
											FridgeElectricityModel.URI),
						 new VariableSink[] {
								 new VariableSink("currentCoolingPower",
										 		  Double.class,
										 		  FridgeTemperatureModel.URI)
						 });

			coupledModelDescriptors.put(
					FridgeCoupledModel.URI,
					new CoupledHIOA_Descriptor(
							FridgeCoupledModel.class,
							FridgeCoupledModel.URI,
							submodels,
							null,
							null,
							connections,
							null,
							null,
							null,
							bindings));

			ArchitectureI architecture =
					new Architecture(
							FridgeCoupledModel.URI,
							atomicModelDescriptors,
							coupledModelDescriptors,
							TimeUnit.HOURS);

			SimulatorI se = architecture.constructSimulator();
			SimulationEngine.SIMULATION_STEP_SLEEP_TIME = 0L;
			se.doStandAloneSimulation(0.0, 24.0);
			System.exit(0);
		} catch (Exception e) {
			throw new RuntimeException(e) ;
		}
	}
}
