package fr.sorbonne_u.components.equipments.fridge.mil;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import fr.sorbonne_u.components.equipments.fridge.mil.events.CloseDoorFridge;
import fr.sorbonne_u.components.equipments.fridge.mil.events.CoolFridge;
import fr.sorbonne_u.components.equipments.fridge.mil.events.DoNotCoolFridge;
import fr.sorbonne_u.components.equipments.fridge.mil.events.OpenDoorFridge;
import fr.sorbonne_u.components.equipments.fridge.mil.events.SetPowerFridge;
import fr.sorbonne_u.components.equipments.fridge.mil.events.SwitchOffFridge;
import fr.sorbonne_u.components.equipments.fridge.mil.events.SwitchOnFridge;
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
import fr.sorbonne_u.devs_simulation.simulators.interfaces.SimulationReportI;
import fr.sorbonne_u.devs_simulation.simulators.interfaces.SimulatorI;

public class RunFridgeUnitaryMILRTSimulation {
	
	public static final double ACCELERATION_FACTOR = 1800.0;

	public static void main(String[] args){		
		try {
			Map<String,AbstractAtomicModelDescriptor> atomicModelDescriptors =
															new HashMap<>();

			atomicModelDescriptors.put(
					FridgeStateModel.MIL_RT_URI,
					RTAtomicModelDescriptor.create(
							FridgeStateModel.class,
							FridgeStateModel.MIL_RT_URI,
							TimeUnit.HOURS,
							null,
							ACCELERATION_FACTOR));
			atomicModelDescriptors.put(
					FridgeElectricityModel.MIL_RT_URI,
					RTAtomicHIOA_Descriptor.create(
							FridgeElectricityModel.class,
							FridgeElectricityModel.MIL_RT_URI,
							TimeUnit.HOURS,
							null,
							ACCELERATION_FACTOR));
			atomicModelDescriptors.put(
					FridgeTemperatureModel.MIL_RT_URI,
					RTAtomicHIOA_Descriptor.create(
							FridgeTemperatureModel.class,
							FridgeTemperatureModel.MIL_RT_URI,
							TimeUnit.HOURS,
							null,
							ACCELERATION_FACTOR));
			atomicModelDescriptors.put(
					ExternalTemperatureModel.MIL_RT_URI,
					RTAtomicHIOA_Descriptor.create(
							ExternalTemperatureModel.class,
							ExternalTemperatureModel.MIL_RT_URI,
							TimeUnit.HOURS,
							null,
							ACCELERATION_FACTOR));
			atomicModelDescriptors.put(
					FridgeUnitTestModel.MIL_RT_URI,
					RTAtomicModelDescriptor.create(
							FridgeUnitTestModel.class,
							FridgeUnitTestModel.MIL_RT_URI,
							TimeUnit.HOURS,
							null,
							ACCELERATION_FACTOR));

			Map<String,CoupledModelDescriptor> coupledModelDescriptors = new HashMap<>();

			Set<String> submodels = new HashSet<String>();
			submodels.add(FridgeElectricityModel.MIL_RT_URI);
			submodels.add(FridgeTemperatureModel.MIL_RT_URI);
			submodels.add(ExternalTemperatureModel.MIL_RT_URI);
			submodels.add(FridgeUnitTestModel.MIL_RT_URI);
			submodels.add(FridgeStateModel.MIL_RT_URI);
			
			Map<EventSource,EventSink[]> connections = new HashMap<EventSource,EventSink[]>();

			// UnitTestModel => StateModel
			connections.put(
					new EventSource(FridgeUnitTestModel.MIL_RT_URI,
									SwitchOnFridge.class),
					new EventSink[] {
							new EventSink(FridgeStateModel.MIL_RT_URI,
										  SwitchOnFridge.class)
					});
			connections.put(
					new EventSource(FridgeUnitTestModel.MIL_RT_URI,
									SwitchOffFridge.class),
					new EventSink[] {
							new EventSink(FridgeStateModel.MIL_RT_URI,
										  SwitchOffFridge.class)
					});
			connections.put(
					new EventSource(FridgeUnitTestModel.MIL_RT_URI,
									OpenDoorFridge.class),
					new EventSink[] {
							new EventSink(FridgeStateModel.MIL_RT_URI,
										  OpenDoorFridge.class)
					});
			connections.put(
					new EventSource(FridgeUnitTestModel.MIL_RT_URI,
									CloseDoorFridge.class),
					new EventSink[] {
							new EventSink(FridgeStateModel.MIL_RT_URI,
										  CloseDoorFridge.class)
					});
			connections.put(
					new EventSource(FridgeUnitTestModel.MIL_RT_URI,
									CoolFridge.class),
					new EventSink[] {
							new EventSink(FridgeStateModel.MIL_RT_URI,
										  CoolFridge.class)
					});
			connections.put(
					new EventSource(FridgeUnitTestModel.MIL_RT_URI,
									DoNotCoolFridge.class),
					new EventSink[] {
							new EventSink(FridgeStateModel.MIL_RT_URI,
										  DoNotCoolFridge.class)
					});
			connections.put(
					new EventSource(FridgeUnitTestModel.MIL_RT_URI,
									SetPowerFridge.class),
					new EventSink[] {
							new EventSink(FridgeStateModel.MIL_RT_URI,
										  SetPowerFridge.class)
					});
			
			
			// StateModel => ElectricityModel
			// StateModel => TemperatureModel
			connections.put(
					new EventSource(FridgeStateModel.MIL_RT_URI, SwitchOffFridge.class),
					new EventSink[] {
							new EventSink(FridgeElectricityModel.MIL_RT_URI,
										  SwitchOffFridge.class),
							new EventSink(FridgeTemperatureModel.MIL_RT_URI,
										  SwitchOffFridge.class)
					});
			connections.put(
					new EventSource(FridgeStateModel.MIL_RT_URI, CoolFridge.class),
					new EventSink[] {
							new EventSink(FridgeElectricityModel.MIL_RT_URI,
										  CoolFridge.class),
							new EventSink(FridgeTemperatureModel.MIL_RT_URI,
										  CoolFridge.class)
					});
			connections.put(
					new EventSource(FridgeStateModel.MIL_RT_URI, DoNotCoolFridge.class),
					new EventSink[] {
							new EventSink(FridgeElectricityModel.MIL_RT_URI,
										  DoNotCoolFridge.class),
							new EventSink(FridgeTemperatureModel.MIL_RT_URI,
										  DoNotCoolFridge.class)
					});
			connections.put(
					new EventSource(FridgeStateModel.MIL_RT_URI, SwitchOnFridge.class),
					new EventSink[] {
							new EventSink(FridgeElectricityModel.MIL_RT_URI,
										  SwitchOnFridge.class),
							new EventSink(FridgeTemperatureModel.MIL_RT_URI,
										  SwitchOnFridge.class)
					});
			connections.put(
					new EventSource(FridgeStateModel.MIL_RT_URI, OpenDoorFridge.class),
					new EventSink[] {
							new EventSink(FridgeElectricityModel.MIL_RT_URI,
										  OpenDoorFridge.class),
							new EventSink(FridgeTemperatureModel.MIL_RT_URI,
										  OpenDoorFridge.class)
					});
			connections.put(
					new EventSource(FridgeStateModel.MIL_RT_URI, CloseDoorFridge.class),
					new EventSink[] {
							new EventSink(FridgeElectricityModel.MIL_RT_URI,
										  CloseDoorFridge.class),
							new EventSink(FridgeTemperatureModel.MIL_RT_URI,
										  CloseDoorFridge.class)
					});
			connections.put(
					new EventSource(FridgeStateModel.MIL_RT_URI, SetPowerFridge.class),
					new EventSink[] {
							new EventSink(FridgeElectricityModel.MIL_RT_URI,
										  SetPowerFridge.class),
							new EventSink(FridgeTemperatureModel.MIL_RT_URI,
										  SetPowerFridge.class)
					});

			// variable bindings between exporting and importing models
			Map<VariableSource,VariableSink[]> bindings =
								new HashMap<VariableSource,VariableSink[]>();

			bindings.put(new VariableSource("externalTemperature",
											Double.class,
											ExternalTemperatureModel.MIL_RT_URI),
						 new VariableSink[] {
								 new VariableSink("externalTemperature",
										 		  Double.class,
										 		  FridgeTemperatureModel.MIL_RT_URI)
						 });
			bindings.put(new VariableSource("currentCoolingPower",
											Double.class,
											FridgeElectricityModel.MIL_RT_URI),
						 new VariableSink[] {
								 new VariableSink("currentCoolingPower",
										 		  Double.class,
										 		  FridgeTemperatureModel.MIL_RT_URI)
						 });

			coupledModelDescriptors.put(
					FridgeCoupledModel.MIL_RT_URI,
					new RTCoupledHIOA_Descriptor(
							FridgeCoupledModel.class,
							FridgeCoupledModel.MIL_RT_URI,
							submodels,
							null,
							null,
							connections,
							null,
							null,
							null,
							bindings,
							ACCELERATION_FACTOR));

			ArchitectureI architecture =
					new RTArchitecture(
							FridgeCoupledModel.MIL_RT_URI,
							atomicModelDescriptors,
							coupledModelDescriptors,
							TimeUnit.HOURS);

			SimulatorI se = architecture.constructSimulator();
			
			long start = System.currentTimeMillis() + 100;
			double simulationDuration = 24.1;
			
			se.startRTSimulation(start, 0.0, simulationDuration);
			long sleepTime = (long)(TimeUnit.HOURS.toMillis(1) *
									(simulationDuration/ACCELERATION_FACTOR));
						
			Thread.sleep(sleepTime + 10000L);
			
			SimulationReportI sr = se.getFinalReport();
			System.out.println("Report -> " + sr);
			
			System.exit(0);
		} catch (Exception e) {
			throw new RuntimeException(e) ;
		}
	}
}
