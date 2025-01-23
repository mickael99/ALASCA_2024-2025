package fr.sorbonne_u.components.equipments.fridge.mil;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import fr.sorbonne_u.components.equipments.fridge.mil.events.*;
import fr.sorbonne_u.components.utils.SimulationType;
import fr.sorbonne_u.devs_simulation.architectures.Architecture;
import fr.sorbonne_u.devs_simulation.architectures.RTArchitecture;
import fr.sorbonne_u.devs_simulation.hioa.architectures.AtomicHIOA_Descriptor;
import fr.sorbonne_u.devs_simulation.hioa.architectures.CoupledHIOA_Descriptor;
import fr.sorbonne_u.devs_simulation.hioa.architectures.RTAtomicHIOA_Descriptor;
import fr.sorbonne_u.devs_simulation.hioa.architectures.RTCoupledHIOA_Descriptor;
import fr.sorbonne_u.devs_simulation.hioa.models.vars.VariableSink;
import fr.sorbonne_u.devs_simulation.hioa.models.vars.VariableSource;
import fr.sorbonne_u.devs_simulation.models.architectures.AbstractAtomicModelDescriptor;
import fr.sorbonne_u.devs_simulation.models.architectures.AtomicModelDescriptor;
import fr.sorbonne_u.devs_simulation.models.architectures.CoupledModelDescriptor;
import fr.sorbonne_u.devs_simulation.models.architectures.RTAtomicModelDescriptor;
import fr.sorbonne_u.devs_simulation.models.events.EventI;
import fr.sorbonne_u.devs_simulation.models.events.EventSink;
import fr.sorbonne_u.devs_simulation.models.events.EventSource;
import fr.sorbonne_u.devs_simulation.models.events.ReexportedEvent;
import fr.sorbonne_u.exceptions.PreconditionException;

public class LocalSimulationArchitectures {
	
	public static Architecture createFridgeMILLocalArchitecture4UnitTest(String architectureURI, TimeUnit simulatedTimeUnit) throws Exception {
		Map<String,AbstractAtomicModelDescriptor> atomicModelDescriptors = new HashMap<>();

		System.out.println("qujcndsqdné");
		// Atomic descriptors
		atomicModelDescriptors.put(
				FridgeStateModel.MIL_URI,
				AtomicModelDescriptor.create(
						FridgeStateModel.class,
						FridgeStateModel.MIL_URI,
						simulatedTimeUnit,
						null));
		atomicModelDescriptors.put(
				FridgeElectricityModel.MIL_URI,
				AtomicHIOA_Descriptor.create(
						FridgeElectricityModel.class,
						FridgeElectricityModel.MIL_URI,
						simulatedTimeUnit,
						null));
		atomicModelDescriptors.put(
				FridgeTemperatureModel.MIL_URI,
				AtomicHIOA_Descriptor.create(
						FridgeTemperatureModel.class,
						FridgeTemperatureModel.MIL_URI,
						simulatedTimeUnit,
						null));
		atomicModelDescriptors.put(
				ExternalTemperatureModel.MIL_URI,
				AtomicHIOA_Descriptor.create(
						ExternalTemperatureModel.class,
						ExternalTemperatureModel.MIL_URI,
						simulatedTimeUnit,
						null));
		
		// Submodels
		Map<String,CoupledModelDescriptor> coupledModelDescriptors = new HashMap<>();

		Set<String> submodels = new HashSet<String>();
		submodels.add(FridgeElectricityModel.MIL_URI);
		submodels.add(FridgeTemperatureModel.MIL_URI);
		submodels.add(ExternalTemperatureModel.MIL_URI);
		submodels.add(FridgeStateModel.MIL_URI);

		// Imported
		Map<Class<? extends EventI>, EventSink[]> imported = new HashMap<>();

		imported.put(
				CloseDoorFridge.class,
				new EventSink[] {
					new EventSink(FridgeStateModel.MIL_URI, CloseDoorFridge.class)
				});
		imported.put(
				CoolFridge.class,
				new EventSink[] {
					new EventSink(FridgeStateModel.MIL_URI, CoolFridge.class)
				});
		imported.put(
				DoNotCoolFridge.class,
				new EventSink[] {
					new EventSink(FridgeStateModel.MIL_URI, DoNotCoolFridge.class)
				});
		imported.put(
				OpenDoorFridge.class,
				new EventSink[] {
					new EventSink(FridgeStateModel.MIL_URI, OpenDoorFridge.class)
				});
		imported.put(
				SetPowerFridge.class,
				new EventSink[] {
					new EventSink(FridgeStateModel.MIL_URI, SetPowerFridge.class)
				});
		imported.put(
				SwitchOffFridge.class,
				new EventSink[] {
					new EventSink(FridgeStateModel.MIL_URI, SwitchOffFridge.class)
				});
		imported.put(
				SwitchOnFridge.class,
				new EventSink[] {
					new EventSink(FridgeStateModel.MIL_URI, SwitchOnFridge.class)
				});

		
		// Connections
		Map<EventSource,EventSink[]> connections = new HashMap<EventSource,EventSink[]>();

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

		// Bindings
		Map<VariableSource,VariableSink[]> bindings = new HashMap<VariableSource,VariableSink[]>();

		bindings.put(new VariableSource("externalTemperature",
				Double.class,
				ExternalTemperatureModel.MIL_URI),
				new VariableSink[] {
						new VariableSink("externalTemperature",
								 		  Double.class,
								 		  FridgeTemperatureModel.MIL_URI)
				});

		// Coupled model descriptor
		coupledModelDescriptors.put(
				FridgeCoupledModel.MIL_URI,
				new CoupledHIOA_Descriptor(
						FridgeCoupledModel.class,
						FridgeCoupledModel.MIL_URI,
						submodels,
						imported,
						null,
						connections,
						null,
						null,
						null,
						bindings));

		// simulation architecture
		Architecture architecture =
				new Architecture(
						architectureURI,
						FridgeCoupledModel.MIL_URI,
						atomicModelDescriptors,
						coupledModelDescriptors,
						simulatedTimeUnit);

		return architecture;
	}
	
	
	public static Architecture createFridge_RT_LocalArchitecture4UnitTest(
			SimulationType currentSimulationType,
			String architectureURI,
			TimeUnit simulatedTimeUnit,
			double accelerationFactor
			) throws Exception
	{
		assert	currentSimulationType.isMILRTSimulation() ||
									currentSimulationType.isSILSimulation() :
				new PreconditionException(
						"currentSimulationType.isMILRTSimulation() || "
						+ "currentSimulationType.isSILSimulation()");

		String fridgeStateModelURI = null;
		String fridgeTemperatureModelURI = null;
		String externalTemperatureModelURI = null;
		String fridgeElectricityModelURI = null;
		String fridgeCoupledModelURI = null;
		
		switch (currentSimulationType) {
			case MIL_RT_SIMULATION:
				fridgeStateModelURI = FridgeStateModel.MIL_RT_URI;
				fridgeTemperatureModelURI = FridgeTemperatureModel.MIL_RT_URI;
				externalTemperatureModelURI = ExternalTemperatureModel.MIL_RT_URI;
				fridgeElectricityModelURI = FridgeElectricityModel.MIL_RT_URI;
				fridgeCoupledModelURI = FridgeCoupledModel.MIL_RT_URI;
				break;
			case SIL_SIMULATION:
				fridgeStateModelURI = FridgeStateModel.SIL_URI;
				fridgeTemperatureModelURI = FridgeTemperatureModel.SIL_URI;
				externalTemperatureModelURI = ExternalTemperatureModel.SIL_URI;
				fridgeElectricityModelURI = FridgeElectricityModel.SIL_URI;
				fridgeCoupledModelURI = FridgeCoupledModel.SIL_URI;
				break;
			default:
		}

		Map<String,AbstractAtomicModelDescriptor> atomicModelDescriptors =
															new HashMap<>();
		
		atomicModelDescriptors.put(
				fridgeStateModelURI,
				RTAtomicModelDescriptor.create(
						FridgeStateModel.class,
						fridgeStateModelURI,
						simulatedTimeUnit,
						null,
						accelerationFactor));
		atomicModelDescriptors.put(
				fridgeElectricityModelURI,
				RTAtomicHIOA_Descriptor.create(
						FridgeElectricityModel.class,
						fridgeElectricityModelURI,
						simulatedTimeUnit,
						null,
						accelerationFactor));
		atomicModelDescriptors.put(
				fridgeTemperatureModelURI,
				RTAtomicHIOA_Descriptor.create(
						FridgeTemperatureModel.class,
						fridgeTemperatureModelURI,
						simulatedTimeUnit,
						null,
						accelerationFactor));
		atomicModelDescriptors.put(
				externalTemperatureModelURI,
				RTAtomicHIOA_Descriptor.create(
						ExternalTemperatureModel.class,
						externalTemperatureModelURI,
						simulatedTimeUnit,
						null,
						accelerationFactor));

		Map<String,CoupledModelDescriptor> coupledModelDescriptors = new HashMap<>();

		Set<String> submodels = new HashSet<String>();
		submodels.add(fridgeStateModelURI);
		submodels.add(externalTemperatureModelURI);
		submodels.add(fridgeTemperatureModelURI);
		submodels.add(fridgeElectricityModelURI);

		Map<Class<? extends EventI>, EventSink[]> imported = new HashMap<>();

		imported.put(
				CloseDoorFridge.class,
				new EventSink[] {
					new EventSink(fridgeStateModelURI, CloseDoorFridge.class)
				});
		imported.put(
				CoolFridge.class,
				new EventSink[] {
					new EventSink(fridgeStateModelURI, CoolFridge.class)
				});
		imported.put(
				DoNotCoolFridge.class,
				new EventSink[] {
					new EventSink(fridgeStateModelURI, DoNotCoolFridge.class)
				});
		imported.put(
				OpenDoorFridge.class,
				new EventSink[] {
					new EventSink(fridgeStateModelURI, OpenDoorFridge.class)
				});
		imported.put(
				SetPowerFridge.class,
				new EventSink[] {
					new EventSink(fridgeStateModelURI, SetPowerFridge.class)
				});
		imported.put(
				SwitchOffFridge.class,
				new EventSink[] {
					new EventSink(fridgeStateModelURI, SwitchOffFridge.class)
				});
		imported.put(
				SwitchOnFridge.class,
				new EventSink[] {
					new EventSink(fridgeStateModelURI, SwitchOnFridge.class)
				});

		
		Map<EventSource,EventSink[]> connections = new HashMap<EventSource,EventSink[]>();

		connections.put(
				new EventSource(fridgeStateModelURI, SwitchOffFridge.class),
				new EventSink[] {
						new EventSink(fridgeElectricityModelURI,
									  SwitchOffFridge.class),
						new EventSink(fridgeTemperatureModelURI,
									  SwitchOffFridge.class)
				});
		connections.put(
				new EventSource(fridgeStateModelURI, CoolFridge.class),
				new EventSink[] {
						new EventSink(fridgeElectricityModelURI,
									  CoolFridge.class),
						new EventSink(fridgeTemperatureModelURI,
									  CoolFridge.class)
				});
		connections.put(
				new EventSource(fridgeStateModelURI, DoNotCoolFridge.class),
				new EventSink[] {
						new EventSink(fridgeElectricityModelURI,
									  DoNotCoolFridge.class),
						new EventSink(fridgeTemperatureModelURI,
									  DoNotCoolFridge.class)
				});
		connections.put(
				new EventSource(fridgeStateModelURI, SwitchOnFridge.class),
				new EventSink[] {
						new EventSink(fridgeElectricityModelURI,
									  SwitchOnFridge.class),
						new EventSink(fridgeTemperatureModelURI,
									  SwitchOnFridge.class)
				});
		connections.put(
				new EventSource(fridgeStateModelURI, OpenDoorFridge.class),
				new EventSink[] {
						new EventSink(fridgeElectricityModelURI,
									  OpenDoorFridge.class),
						new EventSink(fridgeTemperatureModelURI,
									  OpenDoorFridge.class)
				});
		connections.put(
				new EventSource(fridgeStateModelURI, CloseDoorFridge.class),
				new EventSink[] {
						new EventSink(fridgeElectricityModelURI,
									  CloseDoorFridge.class),
						new EventSink(fridgeTemperatureModelURI,
									  CloseDoorFridge.class)
				});
		connections.put(
				new EventSource(fridgeStateModelURI, SetPowerFridge.class),
				new EventSink[] {
						new EventSink(fridgeElectricityModelURI,
									  SetPowerFridge.class),
						new EventSink(fridgeTemperatureModelURI,
									  SetPowerFridge.class)
				});

		Map<VariableSource,VariableSink[]> bindings =
							new HashMap<VariableSource,VariableSink[]>();

		bindings.put(new VariableSource("externalTemperature",
				Double.class,
				externalTemperatureModelURI),
				new VariableSink[] {
						new VariableSink("externalTemperature",
								 		  Double.class,
								 		  fridgeTemperatureModelURI)
				});

		coupledModelDescriptors.put(
				fridgeCoupledModelURI,
				new RTCoupledHIOA_Descriptor(
						FridgeCoupledModel.class,
						fridgeCoupledModelURI,
						submodels,
						imported,
						null,
						connections,
						null,
						null,
						null,
						bindings,
						accelerationFactor));

		Architecture architecture =
				new RTArchitecture(
						architectureURI,
						fridgeCoupledModelURI,
						atomicModelDescriptors,
						coupledModelDescriptors,
						simulatedTimeUnit,
						accelerationFactor);

		return architecture;
	}
	
	public static Architecture	createFridgeUserMILLocalArchitecture(String architectureURI, TimeUnit simulatedTimeUnit) throws Exception {
		Map<String,AbstractAtomicModelDescriptor> atomicModelDescriptors = new HashMap<>();
		
		atomicModelDescriptors.put(
				FridgeUnitTestModel.MIL_URI,
				AtomicModelDescriptor.create(
						FridgeUnitTestModel.class,
						FridgeUnitTestModel.MIL_URI,
						simulatedTimeUnit,
						null));
		
		Map<String,CoupledModelDescriptor> coupledModelDescriptors = new HashMap<>();


		Architecture architecture =
				new Architecture(
						architectureURI,
						FridgeUnitTestModel.MIL_URI,
						atomicModelDescriptors,
						coupledModelDescriptors,
						simulatedTimeUnit);

		return architecture;
	}
	
	public static Architecture	createFridgeUserMILRT_LocalArchitecture(String architectureURI, TimeUnit simulatedTimeUnit,
																			double accelerationFactor) throws Exception
	{
		Map<String,AbstractAtomicModelDescriptor> atomicModelDescriptors = new HashMap<>();

		atomicModelDescriptors.put(
				FridgeUnitTestModel.MIL_RT_URI,
				RTAtomicModelDescriptor.create(
						FridgeUnitTestModel.class,
						FridgeUnitTestModel.MIL_RT_URI,
						simulatedTimeUnit,
						null,
						accelerationFactor));

		Map<String,CoupledModelDescriptor> coupledModelDescriptors = new HashMap<>();
		
		Architecture architecture =
				new RTArchitecture(
						architectureURI,
						FridgeUnitTestModel.MIL_RT_URI,
						atomicModelDescriptors,
						coupledModelDescriptors,
						simulatedTimeUnit,
						accelerationFactor);

		return architecture;
	}
	
	public static Architecture	createFridgeMILLocalArchitecture4IntegrationTest(
		String architectureURI,
		TimeUnit simulatedTimeUnit
		) throws Exception
	{

		Map<String,AbstractAtomicModelDescriptor> atomicModelDescriptors =
															new HashMap<>();

		atomicModelDescriptors.put(
				FridgeStateModel.MIL_URI,
				AtomicModelDescriptor.create(
						FridgeStateModel.class,
						FridgeStateModel.MIL_URI,
						simulatedTimeUnit,
						null));
		atomicModelDescriptors.put(
				FridgeTemperatureModel.MIL_URI,
				AtomicHIOA_Descriptor.create(
						FridgeTemperatureModel.class,
						FridgeTemperatureModel.MIL_URI,
						simulatedTimeUnit,
						null));
		atomicModelDescriptors.put(
				ExternalTemperatureModel.MIL_URI,
				AtomicHIOA_Descriptor.create(
						ExternalTemperatureModel.class,
						ExternalTemperatureModel.MIL_URI,
						simulatedTimeUnit,
						null));

		Map<String,CoupledModelDescriptor> coupledModelDescriptors = new HashMap<>();

		Set<String> submodels = new HashSet<String>();
		submodels.add(FridgeStateModel.MIL_URI);
		submodels.add(ExternalTemperatureModel.MIL_URI);
		submodels.add(FridgeTemperatureModel.MIL_URI);

		Map<Class<? extends EventI>, EventSink[]> imported = new HashMap<>();

		imported.put(
				CloseDoorFridge.class,
				new EventSink[] {
					new EventSink(FridgeStateModel.MIL_URI, CloseDoorFridge.class)
				});
		imported.put(
				CoolFridge.class,
				new EventSink[] {
					new EventSink(FridgeStateModel.MIL_URI, CoolFridge.class)
				});
		imported.put(
				DoNotCoolFridge.class,
				new EventSink[] {
					new EventSink(FridgeStateModel.MIL_URI, DoNotCoolFridge.class)
				});
		imported.put(
				OpenDoorFridge.class,
				new EventSink[] {
					new EventSink(FridgeStateModel.MIL_URI, OpenDoorFridge.class)
				});
		imported.put(
				SetPowerFridge.class,
				new EventSink[] {
					new EventSink(FridgeStateModel.MIL_URI, SetPowerFridge.class)
				});
		imported.put(
				SwitchOffFridge.class,
				new EventSink[] {
					new EventSink(FridgeStateModel.MIL_URI, SwitchOffFridge.class)
				});
		imported.put(
				SwitchOnFridge.class,
				new EventSink[] {
					new EventSink(FridgeStateModel.MIL_URI, SwitchOnFridge.class)
				});


		Map<Class<? extends EventI>,ReexportedEvent> reexported =
				new HashMap<Class<? extends EventI>,ReexportedEvent>();

		reexported.put(
				CloseDoorFridge.class,
				new ReexportedEvent(FridgeStateModel.MIL_URI,
									CloseDoorFridge.class));
		reexported.put(
				CoolFridge.class,
				new ReexportedEvent(FridgeStateModel.MIL_URI,
									CoolFridge.class));
		reexported.put(
				DoNotCoolFridge.class,
				new ReexportedEvent(FridgeStateModel.MIL_URI,
									DoNotCoolFridge.class));
		reexported.put(
				OpenDoorFridge.class,
				new ReexportedEvent(FridgeStateModel.MIL_URI,
									OpenDoorFridge.class));
		reexported.put(
				SetPowerFridge.class,
				new ReexportedEvent(FridgeStateModel.MIL_URI,
									SetPowerFridge.class));
		reexported.put(
				SwitchOffFridge.class,
				new ReexportedEvent(FridgeStateModel.MIL_URI,
									SwitchOffFridge.class));
		reexported.put(
				SwitchOnFridge.class,
				new ReexportedEvent(FridgeStateModel.MIL_URI,
									SwitchOnFridge.class));

		Map<EventSource,EventSink[]> connections =
									new HashMap<EventSource,EventSink[]>();

		connections.put(
				new EventSource(FridgeStateModel.MIL_URI, SwitchOffFridge.class),
				new EventSink[] {
						new EventSink(FridgeTemperatureModel.MIL_URI,
									  SwitchOffFridge.class)
				});
		connections.put(
				new EventSource(FridgeStateModel.MIL_URI, CoolFridge.class),
				new EventSink[] {
						new EventSink(FridgeTemperatureModel.MIL_URI,
									  CoolFridge.class)
				});
		connections.put(
				new EventSource(FridgeStateModel.MIL_URI, DoNotCoolFridge.class),
				new EventSink[] {
						new EventSink(FridgeTemperatureModel.MIL_URI,
									  DoNotCoolFridge.class)
				});
		connections.put(
				new EventSource(FridgeStateModel.MIL_URI, SwitchOnFridge.class),
				new EventSink[] {
						new EventSink(FridgeTemperatureModel.MIL_URI,
									  SwitchOnFridge.class)
				});
		connections.put(
				new EventSource(FridgeStateModel.MIL_URI, OpenDoorFridge.class),
				new EventSink[] {
						new EventSink(FridgeTemperatureModel.MIL_URI,
									  OpenDoorFridge.class)
				});
		connections.put(
				new EventSource(FridgeStateModel.MIL_URI, CloseDoorFridge.class),
				new EventSink[] {
						new EventSink(FridgeTemperatureModel.MIL_URI,
									  CloseDoorFridge.class)
				});
		connections.put(
				new EventSource(FridgeStateModel.MIL_URI, SetPowerFridge.class),
				new EventSink[] {
						new EventSink(FridgeTemperatureModel.MIL_URI,
									  SetPowerFridge.class)
				});

		Map<VariableSource,VariableSink[]> bindings = new HashMap<VariableSource,VariableSink[]>();

		bindings.put(new VariableSource("externalTemperature",
				Double.class,
				ExternalTemperatureModel.MIL_URI),
				new VariableSink[] {
						new VariableSink("externalTemperature",
								 		  Double.class,
								 		  FridgeTemperatureModel.MIL_URI)
				});

		coupledModelDescriptors.put(
				FridgeCoupledModel.MIL_URI,
				new CoupledHIOA_Descriptor(
						FridgeCoupledModel.class,
						FridgeCoupledModel.MIL_URI,
						submodels,
						imported,
						reexported,
						connections,
						null,
						null,
						null,
						bindings));

		Architecture architecture =
				new Architecture(
						architectureURI,
						FridgeCoupledModel.MIL_URI,
						atomicModelDescriptors,
						coupledModelDescriptors,
						simulatedTimeUnit);

		return architecture;
	}
	
	public static Architecture	createFridge_RT_LocalArchitecture4IntegrationTest(
		SimulationType currentSimulationType,
		String architectureURI,
		TimeUnit simulatedTimeUnit,
		double accelerationFactor
		) throws Exception
	{
		assert	currentSimulationType.isMILRTSimulation() ||
									currentSimulationType.isSILSimulation() :
				new PreconditionException(
						"currentSimulationType.isMILRTSimulation() || "
						+ "currentSimulationType.isSILSimulation()");

		String fridgeStateModelURI = null;
		String fridgeTemperatureModelURI = null;
		String externalTemperatureModelURI = null;
		String fridgeCoupledModelURI = null;
		switch (currentSimulationType) {
		case MIL_RT_SIMULATION:
			fridgeStateModelURI = FridgeStateModel.MIL_RT_URI;
			fridgeTemperatureModelURI = FridgeTemperatureModel.MIL_RT_URI;
			externalTemperatureModelURI = ExternalTemperatureModel.MIL_RT_URI;
			fridgeCoupledModelURI = FridgeCoupledModel.MIL_RT_URI;
			break;
		case SIL_SIMULATION:
			fridgeStateModelURI = FridgeStateModel.SIL_URI;
			fridgeTemperatureModelURI = FridgeTemperatureModel.SIL_URI;
			externalTemperatureModelURI = ExternalTemperatureModel.SIL_URI;
			fridgeCoupledModelURI = FridgeCoupledModel.SIL_URI;
			break;
		default:
		}

		Map<String,AbstractAtomicModelDescriptor> atomicModelDescriptors = new HashMap<>();

		atomicModelDescriptors.put(
				fridgeStateModelURI,
				RTAtomicModelDescriptor.create(
						FridgeStateModel.class,
						fridgeStateModelURI,
						simulatedTimeUnit,
						null,
						accelerationFactor));
		atomicModelDescriptors.put(
				externalTemperatureModelURI,
				RTAtomicHIOA_Descriptor.create(
						ExternalTemperatureModel.class,
						externalTemperatureModelURI,
						simulatedTimeUnit,
						null,
						accelerationFactor));
		atomicModelDescriptors.put(
				fridgeTemperatureModelURI,
				RTAtomicHIOA_Descriptor.create(
						FridgeTemperatureModel.class,
						fridgeTemperatureModelURI,
						simulatedTimeUnit,
						null,
						accelerationFactor));

		Map<String,CoupledModelDescriptor> coupledModelDescriptors = new HashMap<>();

		Set<String> submodels = new HashSet<String>();
		submodels.add(fridgeStateModelURI);
		submodels.add(externalTemperatureModelURI);
		submodels.add(fridgeTemperatureModelURI);

		Map<Class<? extends EventI>, EventSink[]> imported = new HashMap<>();

		imported.put(
				CloseDoorFridge.class,
				new EventSink[] {
					new EventSink(fridgeStateModelURI, CloseDoorFridge.class)
				});
		imported.put(
				CoolFridge.class,
				new EventSink[] {
					new EventSink(fridgeStateModelURI, CoolFridge.class)
				});
		imported.put(
				DoNotCoolFridge.class,
				new EventSink[] {
					new EventSink(fridgeStateModelURI, DoNotCoolFridge.class)
				});
		imported.put(
				OpenDoorFridge.class,
				new EventSink[] {
					new EventSink(fridgeStateModelURI, OpenDoorFridge.class)
				});
		imported.put(
				SetPowerFridge.class,
				new EventSink[] {
					new EventSink(fridgeStateModelURI, SetPowerFridge.class)
				});
		imported.put(
				SwitchOffFridge.class,
				new EventSink[] {
					new EventSink(fridgeStateModelURI, SwitchOffFridge.class)
				});
		imported.put(
				SwitchOnFridge.class,
				new EventSink[] {
					new EventSink(fridgeStateModelURI, SwitchOnFridge.class)
				});

		Map<Class<? extends EventI>,ReexportedEvent> reexported =
				new HashMap<Class<? extends EventI>,ReexportedEvent>();

		reexported.put(
				CloseDoorFridge.class,
				new ReexportedEvent(fridgeStateModelURI, CloseDoorFridge.class));
		reexported.put(
				CoolFridge.class,
				new ReexportedEvent(fridgeStateModelURI, CoolFridge.class));
		reexported.put(
				DoNotCoolFridge.class,
				new ReexportedEvent(fridgeStateModelURI, DoNotCoolFridge.class));
		reexported.put(
				OpenDoorFridge.class,
				new ReexportedEvent(fridgeStateModelURI, OpenDoorFridge.class));
		reexported.put(
				SetPowerFridge.class,
				new ReexportedEvent(fridgeStateModelURI, SetPowerFridge.class));
		reexported.put(
				SwitchOffFridge.class,
				new ReexportedEvent(fridgeStateModelURI, SwitchOffFridge.class));
		reexported.put(
				SwitchOnFridge.class,
				new ReexportedEvent(fridgeStateModelURI, SwitchOnFridge.class));

		Map<EventSource,EventSink[]> connections =
									new HashMap<EventSource,EventSink[]>();

		connections.put(
				new EventSource(fridgeStateModelURI, SwitchOffFridge.class),
				new EventSink[] {
						new EventSink(fridgeTemperatureModelURI,  SwitchOffFridge.class)
				});
		connections.put(
				new EventSource(fridgeStateModelURI, CoolFridge.class),
				new EventSink[] {
						new EventSink(fridgeTemperatureModelURI, CoolFridge.class)
				});
		connections.put(
				new EventSource(fridgeStateModelURI, DoNotCoolFridge.class),
				new EventSink[] {
						new EventSink(fridgeTemperatureModelURI, DoNotCoolFridge.class)
				});
		connections.put(
				new EventSource(fridgeStateModelURI, SwitchOnFridge.class),
				new EventSink[] {
						new EventSink(fridgeTemperatureModelURI, SwitchOnFridge.class)
				});
		connections.put(
				new EventSource(fridgeStateModelURI, OpenDoorFridge.class),
				new EventSink[] {
						new EventSink(fridgeTemperatureModelURI, OpenDoorFridge.class)
				});
		connections.put(
				new EventSource(fridgeStateModelURI, CloseDoorFridge.class),
				new EventSink[] {
						new EventSink(fridgeTemperatureModelURI, CloseDoorFridge.class)
				});
		connections.put(
				new EventSource(fridgeStateModelURI, SetPowerFridge.class),
				new EventSink[] {
						new EventSink(fridgeTemperatureModelURI,  SetPowerFridge.class)
				});

		Map<VariableSource,VariableSink[]> bindings = new HashMap<VariableSource,VariableSink[]>();

		bindings.put(new VariableSource("externalTemperature",
				Double.class,
				externalTemperatureModelURI),
				new VariableSink[] {
						new VariableSink("externalTemperature",
								 		  Double.class,
								 		 fridgeTemperatureModelURI)
				});

		coupledModelDescriptors.put(
				fridgeCoupledModelURI,
				new RTCoupledHIOA_Descriptor(
						FridgeCoupledModel.class,
						fridgeCoupledModelURI,
						submodels,
						imported,
						reexported,
						connections,
						null,
						null,
						null,
						bindings,
						accelerationFactor));

		Architecture architecture =
				new RTArchitecture(
						architectureURI,
						fridgeCoupledModelURI,
						atomicModelDescriptors,
						coupledModelDescriptors,
						simulatedTimeUnit,
						accelerationFactor);

		return architecture;
	}
}
