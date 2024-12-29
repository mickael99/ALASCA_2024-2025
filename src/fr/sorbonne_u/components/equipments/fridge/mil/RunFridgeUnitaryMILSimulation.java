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
import fr.sorbonne_u.devs_simulation.simulators.interfaces.SimulationReportI;
import fr.sorbonne_u.devs_simulation.simulators.interfaces.SimulatorI;

public class RunFridgeUnitaryMILSimulation {
	
	public static void main(String[] args)
	{
		try {
			Map<String,AbstractAtomicModelDescriptor> atomicModelDescriptors =
															new HashMap<>();

			atomicModelDescriptors.put(
					FridgeStateModel.MIL_URI,
					AtomicModelDescriptor.create(
							FridgeStateModel.class,
							FridgeStateModel.MIL_URI,
							TimeUnit.HOURS,
							null));
			atomicModelDescriptors.put(
					FridgeElectricityModel.MIL_URI,
					AtomicHIOA_Descriptor.create(
							FridgeElectricityModel.class,
							FridgeElectricityModel.MIL_URI,
							TimeUnit.HOURS,
							null));
			atomicModelDescriptors.put(
					FridgeTemperatureModel.MIL_URI,
					AtomicHIOA_Descriptor.create(
							FridgeTemperatureModel.class,
							FridgeTemperatureModel.MIL_URI,
							TimeUnit.HOURS,
							null));
			atomicModelDescriptors.put(
					ExternalTemperatureModel.MIL_URI,
					AtomicHIOA_Descriptor.create(
							ExternalTemperatureModel.class,
							ExternalTemperatureModel.MIL_URI,
							TimeUnit.HOURS,
							null));
			atomicModelDescriptors.put(
					FridgeUnitTestModel.MIL_URI,
					AtomicModelDescriptor.create(
							FridgeUnitTestModel.class,
							FridgeUnitTestModel.MIL_URI,
							TimeUnit.HOURS,
							null));

			Map<String,CoupledModelDescriptor> coupledModelDescriptors = new HashMap<>();

			Set<String> submodels = new HashSet<String>();
			submodels.add(FridgeElectricityModel.MIL_URI);
			submodels.add(FridgeTemperatureModel.MIL_URI);
			submodels.add(ExternalTemperatureModel.MIL_URI);
			submodels.add(FridgeUnitTestModel.MIL_URI);
			submodels.add(FridgeStateModel.MIL_URI);
			
			Map<EventSource,EventSink[]> connections = new HashMap<EventSource,EventSink[]>();

			// UnitTestModel => StateModel
			connections.put(
					new EventSource(FridgeUnitTestModel.MIL_URI,
									SwitchOnFridge.class),
					new EventSink[] {
							new EventSink(FridgeStateModel.MIL_URI,
										  SwitchOnFridge.class)
					});
			connections.put(
					new EventSource(FridgeUnitTestModel.MIL_URI,
									SwitchOffFridge.class),
					new EventSink[] {
							new EventSink(FridgeStateModel.MIL_URI,
										  SwitchOffFridge.class)
					});
			connections.put(
					new EventSource(FridgeUnitTestModel.MIL_URI,
									OpenDoorFridge.class),
					new EventSink[] {
							new EventSink(FridgeStateModel.MIL_URI,
										  OpenDoorFridge.class)
					});
			connections.put(
					new EventSource(FridgeUnitTestModel.MIL_URI,
									CloseDoorFridge.class),
					new EventSink[] {
							new EventSink(FridgeStateModel.MIL_URI,
										  CloseDoorFridge.class)
					});
			connections.put(
					new EventSource(FridgeUnitTestModel.MIL_URI,
									CoolFridge.class),
					new EventSink[] {
							new EventSink(FridgeStateModel.MIL_URI,
										  CoolFridge.class)
					});
			connections.put(
					new EventSource(FridgeUnitTestModel.MIL_URI,
									DoNotCoolFridge.class),
					new EventSink[] {
							new EventSink(FridgeStateModel.MIL_URI,
										  DoNotCoolFridge.class)
					});
			connections.put(
					new EventSource(FridgeUnitTestModel.MIL_URI,
									SetPowerFridge.class),
					new EventSink[] {
							new EventSink(FridgeStateModel.MIL_URI,
										  SetPowerFridge.class)
					});
			
			
			// StateModel => ElectricityModel
			// StateModel => TemperatureModel
			connections.put(
					new EventSource(FridgeStateModel.MIL_URI, SwitchOffFridge.class),
					new EventSink[] {
							new EventSink(FridgeElectricityModel.MIL_URI,
										  SwitchOffFridge.class),
							new EventSink(FridgeTemperatureModel.MIL_URI,
										  SwitchOffFridge.class)
					});
			connections.put(
					new EventSource(FridgeStateModel.MIL_URI, CoolFridge.class),
					new EventSink[] {
							new EventSink(FridgeElectricityModel.MIL_URI,
										  CoolFridge.class),
							new EventSink(FridgeTemperatureModel.MIL_URI,
										  CoolFridge.class)
					});
			connections.put(
					new EventSource(FridgeStateModel.MIL_URI, DoNotCoolFridge.class),
					new EventSink[] {
							new EventSink(FridgeElectricityModel.MIL_URI,
										  DoNotCoolFridge.class),
							new EventSink(FridgeTemperatureModel.MIL_URI,
										  DoNotCoolFridge.class)
					});
			connections.put(
					new EventSource(FridgeStateModel.MIL_URI, SwitchOnFridge.class),
					new EventSink[] {
							new EventSink(FridgeElectricityModel.MIL_URI,
										  SwitchOnFridge.class),
							new EventSink(FridgeTemperatureModel.MIL_URI,
										  SwitchOnFridge.class)
					});
			connections.put(
					new EventSource(FridgeStateModel.MIL_URI, OpenDoorFridge.class),
					new EventSink[] {
							new EventSink(FridgeElectricityModel.MIL_URI,
										  OpenDoorFridge.class),
							new EventSink(FridgeTemperatureModel.MIL_URI,
										  OpenDoorFridge.class)
					});
			connections.put(
					new EventSource(FridgeStateModel.MIL_URI, CloseDoorFridge.class),
					new EventSink[] {
							new EventSink(FridgeElectricityModel.MIL_URI,
										  CloseDoorFridge.class),
							new EventSink(FridgeTemperatureModel.MIL_URI,
										  CloseDoorFridge.class)
					});
			connections.put(
					new EventSource(FridgeStateModel.MIL_URI, SetPowerFridge.class),
					new EventSink[] {
							new EventSink(FridgeElectricityModel.MIL_URI,
										  SetPowerFridge.class),
							new EventSink(FridgeTemperatureModel.MIL_URI,
										  SetPowerFridge.class)
					});

			// Variable bindings between exporting and importing models
			Map<VariableSource,VariableSink[]> bindings =
								new HashMap<VariableSource,VariableSink[]>();

			bindings.put(new VariableSource("externalTemperature",
											Double.class,
											ExternalTemperatureModel.MIL_URI),
						 new VariableSink[] {
								 new VariableSink("externalTemperature",
										 		  Double.class,
										 		  FridgeTemperatureModel.MIL_URI)
						 });
			bindings.put(new VariableSource("currentCoolingPower",
					Double.class,
					FridgeElectricityModel.MIL_URI),
			 new VariableSink[] {
					 new VariableSink("currentCoolingPower",
							 		  Double.class,
							 		  FridgeTemperatureModel.MIL_URI)
			 });

			coupledModelDescriptors.put(
					FridgeCoupledModel.MIL_URI,
					new CoupledHIOA_Descriptor(
							FridgeCoupledModel.class,
							FridgeCoupledModel.MIL_URI,
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
							FridgeCoupledModel.MIL_URI,
							atomicModelDescriptors,
							coupledModelDescriptors,
							TimeUnit.HOURS);

			SimulatorI se = architecture.constructSimulator();
			SimulationEngine.SIMULATION_STEP_SLEEP_TIME = 0L;
			
			se.doStandAloneSimulation(0.0, 24.0);
			
			SimulationReportI sr = se.getSimulatedModel().getFinalReport();
			System.out.println(sr);
			
			System.exit(0);
		} catch (Exception e) {
			throw new RuntimeException(e) ;
		}
	}
}
