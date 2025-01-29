package fr.sorbonne_u.components;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import fr.sorbonne_u.components.cyphy.plugins.devs.CoordinatorPlugin;
import fr.sorbonne_u.components.cyphy.plugins.devs.architectures.ComponentAtomicModelDescriptor;
import fr.sorbonne_u.components.cyphy.plugins.devs.architectures.ComponentCoupledModelDescriptor;
import fr.sorbonne_u.components.cyphy.plugins.devs.architectures.ComponentModelArchitecture;
import fr.sorbonne_u.components.cyphy.plugins.devs.architectures.RTComponentAtomicModelDescriptor;
import fr.sorbonne_u.components.cyphy.plugins.devs.architectures.RTComponentCoupledModelDescriptor;
import fr.sorbonne_u.components.equipments.fridge.Fridge;
import fr.sorbonne_u.components.equipments.fridge.FridgeUser;
import fr.sorbonne_u.components.equipments.fridge.mil.FridgeCoupledModel;
import fr.sorbonne_u.components.equipments.fridge.mil.FridgeUnitTestModel;
import fr.sorbonne_u.components.equipments.fridge.mil.events.*;
import fr.sorbonne_u.components.equipments.iron.Iron;
import fr.sorbonne_u.components.equipments.iron.IronUser;
import fr.sorbonne_u.components.equipments.iron.mil.IronStateModel;
import fr.sorbonne_u.components.equipments.iron.mil.IronUserModel;
import fr.sorbonne_u.components.equipments.iron.mil.events.*;
import fr.sorbonne_u.components.equipments.meter.ElectricMeter;
import fr.sorbonne_u.components.equipments.meter.mil.ElectricMeterCoupledModel;
import fr.sorbonne_u.devs_simulation.models.architectures.AbstractAtomicModelDescriptor;
import fr.sorbonne_u.devs_simulation.models.architectures.CoupledModelDescriptor;
import fr.sorbonne_u.devs_simulation.models.events.EventI;
import fr.sorbonne_u.devs_simulation.models.events.EventSink;
import fr.sorbonne_u.devs_simulation.models.events.EventSource;

public class ComponentSimulationArchitectures {

	@SuppressWarnings("unchecked")
	public static ComponentModelArchitecture createMILComponentSimulationArchitectures(
		String architectureURI, 
		TimeUnit simulatedTimeUnit
		) throws Exception
	{

		/* 				ATOMIC DESCRIPTORS			*/
		
		Map<String,AbstractAtomicModelDescriptor> atomicModelDescriptors = new HashMap<>();

		// Iron
		atomicModelDescriptors.put(
				IronStateModel.MIL_URI,
				ComponentAtomicModelDescriptor.create(
						IronStateModel.MIL_URI,
						(Class<? extends EventI>[]) new Class<?>[]{
							DisableEnergySavingModeIron.class,
							DisableSteamModeIron.class,
							EnableCottonModeIron.class,
							EnableDelicateModeIron.class,
							EnableEnergySavingModeIron.class,
							EnableLinenModeIron.class,
							EnableSteamModeIron.class,
							TurnOffIron.class,
							TurnOnIron.class},
						(Class<? extends EventI>[]) new Class<?>[]{
							DisableEnergySavingModeIron.class,
							DisableSteamModeIron.class,
							EnableCottonModeIron.class,
							EnableDelicateModeIron.class,
							EnableEnergySavingModeIron.class,
							EnableLinenModeIron.class,
							EnableSteamModeIron.class,
							TurnOffIron.class,
							TurnOnIron.class},
						simulatedTimeUnit,
						Iron.REFLECTION_INBOUND_PORT_URI
						));
		atomicModelDescriptors.put(
				IronUserModel.MIL_URI,
				ComponentAtomicModelDescriptor.create(
						IronUserModel.MIL_URI,
						(Class<? extends EventI>[]) new Class<?>[]{},
						(Class<? extends EventI>[]) new Class<?>[]{
							DisableEnergySavingModeIron.class,
							DisableSteamModeIron.class,
							EnableCottonModeIron.class,
							EnableDelicateModeIron.class,
							EnableEnergySavingModeIron.class,
							EnableLinenModeIron.class,
							EnableSteamModeIron.class,
							TurnOffIron.class,
							TurnOnIron.class},
						simulatedTimeUnit,
						IronUser.REFLECTION_INBOUND_PORT_URI));

		// Fridge
		atomicModelDescriptors.put(
				FridgeCoupledModel.MIL_URI,
				ComponentAtomicModelDescriptor.create(
						FridgeCoupledModel.MIL_URI,
						(Class<? extends EventI>[]) new Class<?>[]{
							CloseDoorFridge.class,
							CoolFridge.class,
							DoNotCoolFridge.class,
							OpenDoorFridge.class,
							SetPowerFridge.class,
							SwitchOffFridge.class,
							SwitchOnFridge.class},
						(Class<? extends EventI>[]) new Class<?>[]{
							CloseDoorFridge.class,
							CoolFridge.class,
							DoNotCoolFridge.class,
							OpenDoorFridge.class,
							SetPowerFridge.class,
							SwitchOffFridge.class,
							SwitchOnFridge.class},
						simulatedTimeUnit,
						Fridge.REFLECTION_INBOUND_PORT_URI));
		atomicModelDescriptors.put(
				FridgeUnitTestModel.MIL_URI,
				ComponentAtomicModelDescriptor.create(
						FridgeUnitTestModel.MIL_URI,
						(Class<? extends EventI>[]) new Class<?>[]{},
						(Class<? extends EventI>[]) new Class<?>[]{
							CloseDoorFridge.class,
							CoolFridge.class,
							DoNotCoolFridge.class,
							OpenDoorFridge.class,
							SetPowerFridge.class,
							SwitchOffFridge.class,
							SwitchOnFridge.class},
						simulatedTimeUnit,
						FridgeUser.REFLECTION_INBOUND_PORT_URI));

		// Electric Metter
//		atomicModelDescriptors.put(
//				ElectricMeterCoupledModel.MIL_URI,
//				ComponentAtomicModelDescriptor.create(
//						ElectricMeterCoupledModel.MIL_URI,
//						(Class<? extends EventI>[]) new Class<?>[]{
//							CloseDoorFridge.class,
//							CoolFridge.class,
//							DoNotCoolFridge.class,
//							OpenDoorFridge.class,
//							SetPowerFridge.class,
//							SwitchOffFridge.class,
//							SwitchOnFridge.class
							
//							DisableEnergySavingModeIron.class,
//							DisableSteamModeIron.class,
//							EnableCottonModeIron.class,
//							EnableDelicateModeIron.class,
//							EnableEnergySavingModeIron.class,
//							EnableLinenModeIron.class,
//							EnableSteamModeIron.class,
//							TurnOffIron.class,
//							TurnOnIron.class
//							},
//						(Class<? extends EventI>[]) new Class<?>[]{},
//						simulatedTimeUnit,
//						ElectricMeter.REFLECTION_INBOUND_PORT_URI));

		Map<String,CoupledModelDescriptor> coupledModelDescriptors = new HashMap<>();

		/* 				SUBMODELS			*/
		Set<String> submodels = new HashSet<String>();
		// Iron
		submodels.add(IronStateModel.MIL_URI);
		submodels.add(IronUserModel.MIL_URI);
		
		// Fridge
		submodels.add(FridgeCoupledModel.MIL_URI);
		submodels.add(FridgeUnitTestModel.MIL_URI);
		
		// Electric Metter
//		submodels.add(ElectricMeterCoupledModel.MIL_URI);

		
		/* 				CONNECTIONS			*/
		
		Map<EventSource,EventSink[]> connections = new HashMap<EventSource,EventSink[]>();
		
		// IronUser -> IronState
		connections.put(
			new EventSource(IronUserModel.MIL_URI,
					TurnOnIron.class),
			new EventSink[] {
				new EventSink(IronStateModel.MIL_URI,
						TurnOnIron.class)
			});
		connections.put(
			new EventSource(IronUserModel.MIL_URI,
					TurnOffIron.class),
			new EventSink[] {
				new EventSink(IronStateModel.MIL_URI,
						TurnOffIron.class)
			});
		connections.put(
			new EventSource(IronUserModel.MIL_URI,
					DisableEnergySavingModeIron.class),
			new EventSink[] {
				new EventSink(IronStateModel.MIL_URI,
							DisableEnergySavingModeIron.class)
			});
		connections.put(
			new EventSource(IronUserModel.MIL_URI,
					DisableSteamModeIron.class),
			new EventSink[] {
				new EventSink(IronStateModel.MIL_URI,
						DisableSteamModeIron.class)
			});
		connections.put(
				new EventSource(IronUserModel.MIL_URI,
						EnableCottonModeIron.class),
				new EventSink[] {
					new EventSink(IronStateModel.MIL_URI,
							EnableCottonModeIron.class)
				});
		connections.put(
				new EventSource(IronUserModel.MIL_URI,
						EnableDelicateModeIron.class),
				new EventSink[] {
					new EventSink(IronStateModel.MIL_URI,
							EnableDelicateModeIron.class)
				});
		connections.put(
				new EventSource(IronUserModel.MIL_URI,
						EnableLinenModeIron.class),
				new EventSink[] {
					new EventSink(IronStateModel.MIL_URI,
							EnableLinenModeIron.class)
				});
		connections.put(
				new EventSource(IronUserModel.MIL_URI,
						EnableEnergySavingModeIron.class),
				new EventSink[] {
					new EventSink(IronStateModel.MIL_URI,
							EnableEnergySavingModeIron.class)
				});
		connections.put(
				new EventSource(IronUserModel.MIL_URI,
						EnableSteamModeIron.class),
				new EventSink[] {
					new EventSink(IronStateModel.MIL_URI,
							EnableSteamModeIron.class)
				});
		
		
		// IronState -> ElectricMeterCoupledModel
//		connections.put(
//			new EventSource(IronStateModel.MIL_URI,
//					TurnOnIron.class),
//			new EventSink[] {
//				new EventSink(ElectricMeterCoupledModel.MIL_URI,
//						TurnOnIron.class)
//			});
//		connections.put(
//			new EventSource(IronStateModel.MIL_URI,
//					TurnOffIron.class),
//			new EventSink[] {
//				new EventSink(ElectricMeterCoupledModel.MIL_URI,
//						TurnOffIron.class)
//			});
//		connections.put(
//			new EventSource(IronStateModel.MIL_URI,
//					DisableEnergySavingModeIron.class),
//			new EventSink[] {
//				new EventSink(ElectricMeterCoupledModel.MIL_URI,
//							DisableEnergySavingModeIron.class)
//			});
//		connections.put(
//			new EventSource(IronStateModel.MIL_URI,
//					DisableSteamModeIron.class),
//			new EventSink[] {
//				new EventSink(ElectricMeterCoupledModel.MIL_URI,
//						DisableSteamModeIron.class)
//			});
//		connections.put(
//				new EventSource(IronStateModel.MIL_URI,
//						EnableCottonModeIron.class),
//				new EventSink[] {
//					new EventSink(ElectricMeterCoupledModel.MIL_URI,
//							EnableCottonModeIron.class)
//				});
//		connections.put(
//				new EventSource(IronStateModel.MIL_URI,
//						EnableDelicateModeIron.class),
//				new EventSink[] {
//					new EventSink(ElectricMeterCoupledModel.MIL_URI,
//							EnableDelicateModeIron.class)
//				});
//		connections.put(
//				new EventSource(IronStateModel.MIL_URI,
//						EnableLinenModeIron.class),
//				new EventSink[] {
//					new EventSink(ElectricMeterCoupledModel.MIL_URI,
//							EnableLinenModeIron.class)
//				});
//		connections.put(
//				new EventSource(IronStateModel.MIL_URI,
//						EnableEnergySavingModeIron.class),
//				new EventSink[] {
//					new EventSink(ElectricMeterCoupledModel.MIL_URI,
//							EnableEnergySavingModeIron.class)
//				});
//		connections.put(
//				new EventSource(IronStateModel.MIL_URI,
//						EnableSteamModeIron.class),
//				new EventSink[] {
//					new EventSink(ElectricMeterCoupledModel.MIL_URI,
//							EnableSteamModeIron.class)
//				});
			
			
		// FridgeUnitTestModel -> FridgeCoupledModel
		connections.put(
				new EventSource(FridgeUnitTestModel.MIL_URI, SwitchOffFridge.class),
				new EventSink[] {
						new EventSink(FridgeCoupledModel.MIL_URI,
									  SwitchOffFridge.class),
				});
		connections.put(
				new EventSource(FridgeUnitTestModel.MIL_URI, CoolFridge.class),
				new EventSink[] {
						new EventSink(FridgeCoupledModel.MIL_URI,
									  CoolFridge.class),
				});
		connections.put(
				new EventSource(FridgeUnitTestModel.MIL_URI, DoNotCoolFridge.class),
				new EventSink[] {
						new EventSink(FridgeCoupledModel.MIL_URI,
									  DoNotCoolFridge.class),
				});
		connections.put(
				new EventSource(FridgeUnitTestModel.MIL_URI, SwitchOnFridge.class),
				new EventSink[] {
						new EventSink(FridgeCoupledModel.MIL_URI,
									  SwitchOnFridge.class),
				});
		connections.put(
				new EventSource(FridgeUnitTestModel.MIL_URI, OpenDoorFridge.class),
				new EventSink[] {
						new EventSink(FridgeCoupledModel.MIL_URI,
									  OpenDoorFridge.class),
				});
		connections.put(
				new EventSource(FridgeUnitTestModel.MIL_URI, CloseDoorFridge.class),
				new EventSink[] {
						new EventSink(FridgeCoupledModel.MIL_URI,
									  CloseDoorFridge.class),
				});
		connections.put(
				new EventSource(FridgeUnitTestModel.MIL_URI, SetPowerFridge.class),
				new EventSink[] {
						new EventSink(FridgeCoupledModel.MIL_URI,
									  SetPowerFridge.class),
				});
		
		
		// FridgeCoupledModel -> ElectricMeterCoupledModel
//		connections.put(
//				new EventSource(FridgeCoupledModel.MIL_URI, SwitchOffFridge.class),
//				new EventSink[] {
//						new EventSink(ElectricMeterCoupledModel.MIL_URI,
//									  SwitchOffFridge.class),
//				});
//		connections.put(
//				new EventSource(FridgeCoupledModel.MIL_URI, CoolFridge.class),
//				new EventSink[] {
//						new EventSink(ElectricMeterCoupledModel.MIL_URI,
//									  CoolFridge.class),
//				});
//		connections.put(
//				new EventSource(FridgeCoupledModel.MIL_URI, DoNotCoolFridge.class),
//				new EventSink[] {
//						new EventSink(ElectricMeterCoupledModel.MIL_URI,
//									  DoNotCoolFridge.class),
//				});
//		connections.put(
//				new EventSource(FridgeCoupledModel.MIL_URI, SwitchOnFridge.class),
//				new EventSink[] {
//						new EventSink(ElectricMeterCoupledModel.MIL_URI,
//									  SwitchOnFridge.class),
//				});
//		connections.put(
//				new EventSource(FridgeCoupledModel.MIL_URI, OpenDoorFridge.class),
//				new EventSink[] {
//						new EventSink(ElectricMeterCoupledModel.MIL_URI,
//									  OpenDoorFridge.class),
//				});
//		connections.put(
//				new EventSource(FridgeCoupledModel.MIL_URI, CloseDoorFridge.class),
//				new EventSink[] {
//						new EventSink(ElectricMeterCoupledModel.MIL_URI,
//									  CloseDoorFridge.class),
//				});
//		connections.put(
//				new EventSource(FridgeCoupledModel.MIL_URI, SetPowerFridge.class),
//				new EventSink[] {
//						new EventSink(ElectricMeterCoupledModel.MIL_URI,
//									  SetPowerFridge.class),
//				});

		// coupled model descriptor
		coupledModelDescriptors.put(
				GlobalCoupledModel.MIL_URI,
				ComponentCoupledModelDescriptor.create(
						GlobalCoupledModel.class,
						GlobalCoupledModel.MIL_URI,
						submodels,
						null,
						null,
						connections,
						null,
						CoordinatorComponent.REFLECTION_INBOUND_PORT_URI,
						CoordinatorPlugin.class,
						null));

		ComponentModelArchitecture architecture =
				new ComponentModelArchitecture(
						GlobalSupervisor.MIL_SIM_ARCHITECTURE_URI,
						GlobalCoupledModel.MIL_URI,
						atomicModelDescriptors,
						coupledModelDescriptors,
						simulatedTimeUnit);

		return architecture;
	}
	
	
	
	@SuppressWarnings("unchecked")
	public static ComponentModelArchitecture
									createMILRTComponentSimulationArchitectures(
		String architectureURI, 
		TimeUnit simulatedTimeUnit,
		double accelerationFactor
		) throws Exception
	{
		// map that will contain the atomic model descriptors to construct
		// the simulation architecture
		Map<String,AbstractAtomicModelDescriptor> atomicModelDescriptors =
															new HashMap<>();

		atomicModelDescriptors.put(
				IronStateModel.MIL_RT_URI,
				RTComponentAtomicModelDescriptor.create(
						IronStateModel.MIL_RT_URI,
						(Class<? extends EventI>[]) new Class<?>[]{
							DisableEnergySavingModeIron.class,
							DisableSteamModeIron.class,
							EnableCottonModeIron.class,
							EnableDelicateModeIron.class,
							EnableEnergySavingModeIron.class,
							EnableLinenModeIron.class,
							EnableSteamModeIron.class,
							TurnOffIron.class,
							TurnOnIron.class},
						(Class<? extends EventI>[]) new Class<?>[]{
							DisableEnergySavingModeIron.class,
							DisableSteamModeIron.class,
							EnableCottonModeIron.class,
							EnableDelicateModeIron.class,
							EnableEnergySavingModeIron.class,
							EnableLinenModeIron.class,
							EnableSteamModeIron.class,
							TurnOffIron.class,
							TurnOnIron.class},	
						simulatedTimeUnit,
						Iron.REFLECTION_INBOUND_PORT_URI
						));
		atomicModelDescriptors.put(
				IronUserModel.MIL_RT_URI,
				RTComponentAtomicModelDescriptor.create(
						IronUserModel.MIL_RT_URI,
						(Class<? extends EventI>[]) new Class<?>[]{},
						(Class<? extends EventI>[]) new Class<?>[]{
							DisableEnergySavingModeIron.class,
							DisableSteamModeIron.class,
							EnableCottonModeIron.class,
							EnableDelicateModeIron.class,
							EnableEnergySavingModeIron.class,
							EnableLinenModeIron.class,
							EnableSteamModeIron.class,
							TurnOffIron.class,
							TurnOnIron.class},	
						simulatedTimeUnit,
						IronUser.REFLECTION_INBOUND_PORT_URI));

		atomicModelDescriptors.put(
				FridgeCoupledModel.MIL_RT_URI,
				RTComponentAtomicModelDescriptor.create(
						FridgeCoupledModel.MIL_RT_URI,
						(Class<? extends EventI>[]) new Class<?>[]{
							CloseDoorFridge.class,
							CoolFridge.class,
							DoNotCoolFridge.class,
							OpenDoorFridge.class,
							SetPowerFridge.class,
							SwitchOffFridge.class,
							SwitchOnFridge.class},
						(Class<? extends EventI>[]) new Class<?>[]{
							CloseDoorFridge.class,
							CoolFridge.class,
							DoNotCoolFridge.class,
							OpenDoorFridge.class,
							SetPowerFridge.class,
							SwitchOffFridge.class,
							SwitchOnFridge.class},
						simulatedTimeUnit,
						Fridge.REFLECTION_INBOUND_PORT_URI));
		atomicModelDescriptors.put(
				FridgeUnitTestModel.MIL_RT_URI,
				RTComponentAtomicModelDescriptor.create(
						FridgeUnitTestModel.MIL_RT_URI,
						(Class<? extends EventI>[]) new Class<?>[]{},
						(Class<? extends EventI>[]) new Class<?>[]{
							CloseDoorFridge.class,
							CoolFridge.class,
							DoNotCoolFridge.class,
							OpenDoorFridge.class,
							SetPowerFridge.class,
							SwitchOffFridge.class,
							SwitchOnFridge.class},
						simulatedTimeUnit,
						FridgeUser.REFLECTION_INBOUND_PORT_URI));

//		atomicModelDescriptors.put(
//				ElectricMeterCoupledModel.MIL_RT_URI,
//				RTComponentAtomicModelDescriptor.create(
//						ElectricMeterCoupledModel.MIL_RT_URI,
//						(Class<? extends EventI>[]) new Class<?>[]{
//							CloseDoorFridge.class,
//							CoolFridge.class,
//							DoNotCoolFridge.class,
//							OpenDoorFridge.class,
//							SetPowerFridge.class,
//							SwitchOffFridge.class,
//							SwitchOnFridge.class,
//							DisableEnergySavingModeIron.class,
//							DisableSteamModeIron.class,
//							EnableCottonModeIron.class,
//							EnableDelicateModeIron.class,
//							EnableEnergySavingModeIron.class,
//							EnableLinenModeIron.class,
//							EnableSteamModeIron.class,
//							TurnOffIron.class,
//							TurnOnIron.class
//							},
//						(Class<? extends EventI>[]) new Class<?>[]{},
//						simulatedTimeUnit,
//						ElectricMeter.REFLECTION_INBOUND_PORT_URI));

		
		Map<String,CoupledModelDescriptor> coupledModelDescriptors =
															new HashMap<>();


		Set<String> submodels = new HashSet<String>();
		submodels.add(IronStateModel.MIL_RT_URI);
		submodels.add(IronUserModel.MIL_RT_URI);
		submodels.add(FridgeCoupledModel.MIL_RT_URI);
		submodels.add(FridgeUnitTestModel.MIL_RT_URI);
//		submodels.add(ElectricMeterCoupledModel.MIL_RT_URI);

		Map<EventSource,EventSink[]> connections =
									new HashMap<EventSource,EventSink[]>();
									
		// IronUser -> IronState
		connections.put(
			new EventSource(IronUserModel.MIL_RT_URI,
					TurnOnIron.class),
			new EventSink[] {
				new EventSink(IronStateModel.MIL_RT_URI,
						TurnOnIron.class)
			});
		connections.put(
			new EventSource(IronUserModel.MIL_RT_URI,
					TurnOffIron.class),
			new EventSink[] {
				new EventSink(IronStateModel.MIL_RT_URI,
						TurnOffIron.class)
			});
		connections.put(
			new EventSource(IronUserModel.MIL_RT_URI,
					DisableEnergySavingModeIron.class),
			new EventSink[] {
				new EventSink(IronStateModel.MIL_RT_URI,
							DisableEnergySavingModeIron.class)
			});
		connections.put(
			new EventSource(IronUserModel.MIL_RT_URI,
					DisableSteamModeIron.class),
			new EventSink[] {
				new EventSink(IronStateModel.MIL_RT_URI,
						DisableSteamModeIron.class)
			});
		connections.put(
				new EventSource(IronUserModel.MIL_RT_URI,
						EnableCottonModeIron.class),
				new EventSink[] {
					new EventSink(IronStateModel.MIL_RT_URI,
							EnableCottonModeIron.class)
				});
		connections.put(
				new EventSource(IronUserModel.MIL_RT_URI,
						EnableDelicateModeIron.class),
				new EventSink[] {
					new EventSink(IronStateModel.MIL_RT_URI,
							EnableDelicateModeIron.class)
				});
		connections.put(
				new EventSource(IronUserModel.MIL_RT_URI,
						EnableLinenModeIron.class),
				new EventSink[] {
					new EventSink(IronStateModel.MIL_RT_URI,
							EnableLinenModeIron.class)
				});
		connections.put(
				new EventSource(IronUserModel.MIL_RT_URI,
						EnableEnergySavingModeIron.class),
				new EventSink[] {
					new EventSink(IronStateModel.MIL_RT_URI,
							EnableEnergySavingModeIron.class)
				});
		connections.put(
				new EventSource(IronUserModel.MIL_RT_URI,
						EnableSteamModeIron.class),
				new EventSink[] {
					new EventSink(IronStateModel.MIL_RT_URI,
							EnableSteamModeIron.class)
				});
		
		
		// IronState -> ElectricMeterCoupledModel
//		connections.put(
//			new EventSource(IronStateModel.MIL_RT_URI,
//					TurnOnIron.class),
//			new EventSink[] {
//				new EventSink(ElectricMeterCoupledModel.MIL_RT_URI,
//						TurnOnIron.class)
//			});
//		connections.put(
//			new EventSource(IronStateModel.MIL_RT_URI,
//					TurnOffIron.class),
//			new EventSink[] {
//				new EventSink(ElectricMeterCoupledModel.MIL_RT_URI,
//						TurnOffIron.class)
//			});
//		connections.put(
//			new EventSource(IronStateModel.MIL_RT_URI,
//					DisableEnergySavingModeIron.class),
//			new EventSink[] {
//				new EventSink(ElectricMeterCoupledModel.MIL_RT_URI,
//							DisableEnergySavingModeIron.class)
//			});
//		connections.put(
//			new EventSource(IronStateModel.MIL_RT_URI,
//					DisableSteamModeIron.class),
//			new EventSink[] {
//				new EventSink(ElectricMeterCoupledModel.MIL_RT_URI,
//						DisableSteamModeIron.class)
//			});
//		connections.put(
//				new EventSource(IronStateModel.MIL_RT_URI,
//						EnableCottonModeIron.class),
//				new EventSink[] {
//					new EventSink(ElectricMeterCoupledModel.MIL_RT_URI,
//							EnableCottonModeIron.class)
//				});
//		connections.put(
//				new EventSource(IronStateModel.MIL_RT_URI,
//						EnableDelicateModeIron.class),
//				new EventSink[] {
//					new EventSink(ElectricMeterCoupledModel.MIL_RT_URI,
//							EnableDelicateModeIron.class)
//				});
//		connections.put(
//				new EventSource(IronStateModel.MIL_RT_URI,
//						EnableLinenModeIron.class),
//				new EventSink[] {
//					new EventSink(ElectricMeterCoupledModel.MIL_RT_URI,
//							EnableLinenModeIron.class)
//				});
//		connections.put(
//				new EventSource(IronStateModel.MIL_RT_URI,
//						EnableEnergySavingModeIron.class),
//				new EventSink[] {
//					new EventSink(ElectricMeterCoupledModel.MIL_RT_URI,
//							EnableEnergySavingModeIron.class)
//				});
//		connections.put(
//				new EventSource(IronStateModel.MIL_RT_URI,
//						EnableSteamModeIron.class),
//				new EventSink[] {
//					new EventSink(ElectricMeterCoupledModel.MIL_RT_URI,
//							EnableSteamModeIron.class)
//				});
			
			
		// FridgeUnitTestModel -> FridgeCoupledModel
		connections.put(
				new EventSource(FridgeUnitTestModel.MIL_RT_URI, SwitchOffFridge.class),
				new EventSink[] {
						new EventSink(FridgeCoupledModel.MIL_RT_URI,
									  SwitchOffFridge.class),
				});
		connections.put(
				new EventSource(FridgeUnitTestModel.MIL_RT_URI, CoolFridge.class),
				new EventSink[] {
						new EventSink(FridgeCoupledModel.MIL_RT_URI,
									  CoolFridge.class),
				});
		connections.put(
				new EventSource(FridgeUnitTestModel.MIL_RT_URI, DoNotCoolFridge.class),
				new EventSink[] {
						new EventSink(FridgeCoupledModel.MIL_RT_URI,
									  DoNotCoolFridge.class),
				});
		connections.put(
				new EventSource(FridgeUnitTestModel.MIL_RT_URI, SwitchOnFridge.class),
				new EventSink[] {
						new EventSink(FridgeCoupledModel.MIL_RT_URI,
									  SwitchOnFridge.class),
				});
		connections.put(
				new EventSource(FridgeUnitTestModel.MIL_RT_URI, OpenDoorFridge.class),
				new EventSink[] {
						new EventSink(FridgeCoupledModel.MIL_RT_URI,
									  OpenDoorFridge.class),
				});
		connections.put(
				new EventSource(FridgeUnitTestModel.MIL_RT_URI, CloseDoorFridge.class),
				new EventSink[] {
						new EventSink(FridgeCoupledModel.MIL_RT_URI,
									  CloseDoorFridge.class),
				});
		connections.put(
				new EventSource(FridgeUnitTestModel.MIL_RT_URI, SetPowerFridge.class),
				new EventSink[] {
						new EventSink(FridgeCoupledModel.MIL_RT_URI,
									  SetPowerFridge.class),
				});
		
		
		// FridgeCoupledModel -> ElectricMeterCoupledModel
//		connections.put(
//				new EventSource(FridgeCoupledModel.MIL_RT_URI, SwitchOffFridge.class),
//				new EventSink[] {
//						new EventSink(ElectricMeterCoupledModel.MIL_RT_URI,
//									  SwitchOffFridge.class),
//				});
//		connections.put(
//				new EventSource(FridgeCoupledModel.MIL_RT_URI, CoolFridge.class),
//				new EventSink[] {
//						new EventSink(ElectricMeterCoupledModel.MIL_RT_URI,
//									  CoolFridge.class),
//				});
//		connections.put(
//				new EventSource(FridgeCoupledModel.MIL_RT_URI, DoNotCoolFridge.class),
//				new EventSink[] {
//						new EventSink(ElectricMeterCoupledModel.MIL_RT_URI,
//									  DoNotCoolFridge.class),
//				});
//		connections.put(
//				new EventSource(FridgeCoupledModel.MIL_RT_URI, SwitchOnFridge.class),
//				new EventSink[] {
//						new EventSink(ElectricMeterCoupledModel.MIL_RT_URI,
//									  SwitchOnFridge.class),
//				});
//		connections.put(
//				new EventSource(FridgeCoupledModel.MIL_RT_URI, OpenDoorFridge.class),
//				new EventSink[] {
//						new EventSink(ElectricMeterCoupledModel.MIL_RT_URI,
//									  OpenDoorFridge.class),
//				});
//		connections.put(
//				new EventSource(FridgeCoupledModel.MIL_RT_URI, CloseDoorFridge.class),
//				new EventSink[] {
//						new EventSink(ElectricMeterCoupledModel.MIL_RT_URI,
//									  CloseDoorFridge.class),
//				});
//		connections.put(
//				new EventSource(FridgeCoupledModel.MIL_RT_URI, SetPowerFridge.class),
//				new EventSink[] {
//						new EventSink(ElectricMeterCoupledModel.MIL_RT_URI,
//									  SetPowerFridge.class),
//				});

		// coupled model descriptor
		coupledModelDescriptors.put(
				GlobalCoupledModel.MIL_RT_URI,
				RTComponentCoupledModelDescriptor.create(
						GlobalCoupledModel.class,
						GlobalCoupledModel.MIL_RT_URI,
						submodels,
						null,
						null,
						connections,
						null,
						CoordinatorComponent.REFLECTION_INBOUND_PORT_URI,
						CoordinatorPlugin.class,
						null,
						accelerationFactor));

		ComponentModelArchitecture architecture =
				new ComponentModelArchitecture(
						GlobalSupervisor.MIL_RT_SIM_ARCHITECTURE_URI,
						GlobalCoupledModel.MIL_RT_URI,
						atomicModelDescriptors,
						coupledModelDescriptors,
						TimeUnit.HOURS);

		return architecture;
	}
	
	@SuppressWarnings("unchecked")
	public static ComponentModelArchitecture
									createSILComponentSimulationArchitectures(
		String architectureURI, 
		TimeUnit simulatedTimeUnit,
		double accelerationFactor
		) throws Exception
	{
		Map<String,AbstractAtomicModelDescriptor> atomicModelDescriptors =
															new HashMap<>();

		atomicModelDescriptors.put(
				IronStateModel.SIL_URI,
				RTComponentAtomicModelDescriptor.create(
						IronStateModel.SIL_URI,
						(Class<? extends EventI>[]) new Class<?>[]{
							DisableEnergySavingModeIron.class,
							DisableSteamModeIron.class,
							EnableCottonModeIron.class,
							EnableDelicateModeIron.class,
							EnableEnergySavingModeIron.class,
							EnableLinenModeIron.class,
							EnableSteamModeIron.class,
							TurnOffIron.class,
							TurnOnIron.class},	
						(Class<? extends EventI>[]) new Class<?>[]{
							DisableEnergySavingModeIron.class,
							DisableSteamModeIron.class,
							EnableCottonModeIron.class,
							EnableDelicateModeIron.class,
							EnableEnergySavingModeIron.class,
							EnableLinenModeIron.class,
							EnableSteamModeIron.class,
							TurnOffIron.class,
							TurnOnIron.class},	
						simulatedTimeUnit,
						Iron.REFLECTION_INBOUND_PORT_URI
						));

		atomicModelDescriptors.put(
				FridgeCoupledModel.SIL_URI,
				RTComponentAtomicModelDescriptor.create(
						FridgeCoupledModel.SIL_URI,
						(Class<? extends EventI>[]) new Class<?>[]{
							CloseDoorFridge.class,
							CoolFridge.class,
							DoNotCoolFridge.class,
							OpenDoorFridge.class,
							SetPowerFridge.class,
							SwitchOffFridge.class,
							SwitchOnFridge.class},
						(Class<? extends EventI>[]) new Class<?>[]{
							CloseDoorFridge.class,
							CoolFridge.class,
							DoNotCoolFridge.class,
							OpenDoorFridge.class,
							SetPowerFridge.class,
							SwitchOffFridge.class,
							SwitchOnFridge.class},
						simulatedTimeUnit,
						Fridge.REFLECTION_INBOUND_PORT_URI));

//		atomicModelDescriptors.put(
//				ElectricMeterCoupledModel.SIL_URI,
//				RTComponentAtomicModelDescriptor.create(
//						ElectricMeterCoupledModel.SIL_URI,
//						(Class<? extends EventI>[]) new Class<?>[]{
//							CloseDoorFridge.class,
//							CoolFridge.class,
//							DoNotCoolFridge.class,
//							OpenDoorFridge.class,
//							SetPowerFridge.class,
//							SwitchOffFridge.class,
//							SwitchOnFridge.class,
//							DisableEnergySavingModeIron.class,
//							DisableSteamModeIron.class,
//							EnableCottonModeIron.class,
//							EnableDelicateModeIron.class,
//							EnableEnergySavingModeIron.class,
//							EnableLinenModeIron.class,
//							EnableSteamModeIron.class,
//							TurnOffIron.class,
//							TurnOnIron.class
//							},
//						(Class<? extends EventI>[]) new Class<?>[]{},
//						simulatedTimeUnit,
//						ElectricMeter.REFLECTION_INBOUND_PORT_URI));

		Map<String,CoupledModelDescriptor> coupledModelDescriptors =
															new HashMap<>();

		Set<String> submodels = new HashSet<String>();
		submodels.add(IronStateModel.SIL_URI);
		submodels.add(FridgeCoupledModel.SIL_URI);
//		submodels.add(ElectricMeterCoupledModel.SIL_URI);

		Map<EventSource,EventSink[]> connections = 	new HashMap<EventSource,EventSink[]>();

		
		// IronStateModel -> ElectricMeterCoupledModel
//		connections.put(
//			new EventSource(IronStateModel.SIL_URI,
//					TurnOnIron.class),
//			new EventSink[] {
//				new EventSink(ElectricMeterCoupledModel.SIL_URI,
//						TurnOnIron.class)
//			});
//		connections.put(
//			new EventSource(IronStateModel.SIL_URI,
//					TurnOffIron.class),
//			new EventSink[] {
//				new EventSink(ElectricMeterCoupledModel.SIL_URI,
//						TurnOffIron.class)
//			});
//		connections.put(
//			new EventSource(IronStateModel.SIL_URI,
//					DisableEnergySavingModeIron.class),
//			new EventSink[] {
//				new EventSink(ElectricMeterCoupledModel.SIL_URI,
//							DisableEnergySavingModeIron.class)
//			});
//		connections.put(
//			new EventSource(IronStateModel.SIL_URI,
//					DisableSteamModeIron.class),
//			new EventSink[] {
//				new EventSink(ElectricMeterCoupledModel.SIL_URI,
//						DisableSteamModeIron.class)
//			});
//		connections.put(
//				new EventSource(IronStateModel.SIL_URI,
//						EnableCottonModeIron.class),
//				new EventSink[] {
//					new EventSink(ElectricMeterCoupledModel.SIL_URI,
//							EnableCottonModeIron.class)
//				});
//		connections.put(
//				new EventSource(IronStateModel.SIL_URI,
//						EnableDelicateModeIron.class),
//				new EventSink[] {
//					new EventSink(ElectricMeterCoupledModel.SIL_URI,
//							EnableDelicateModeIron.class)
//				});
//		connections.put(
//				new EventSource(IronStateModel.SIL_URI,
//						EnableLinenModeIron.class),
//				new EventSink[] {
//					new EventSink(ElectricMeterCoupledModel.SIL_URI,
//							EnableLinenModeIron.class)
//				});
//		connections.put(
//				new EventSource(IronStateModel.SIL_URI,
//						EnableEnergySavingModeIron.class),
//				new EventSink[] {
//					new EventSink(ElectricMeterCoupledModel.SIL_URI,
//							EnableEnergySavingModeIron.class)
//				});
//		connections.put(
//				new EventSource(IronStateModel.SIL_URI,
//						EnableSteamModeIron.class),
//				new EventSink[] {
//					new EventSink(ElectricMeterCoupledModel.SIL_URI,
//							EnableSteamModeIron.class)
//				});

		// FridgeCoupledModel -> ElectricMeterCoupledModel
//		connections.put(
//				new EventSource(FridgeCoupledModel.SIL_URI, SwitchOffFridge.class),
//				new EventSink[] {
//						new EventSink(ElectricMeterCoupledModel.SIL_URI,
//									  SwitchOffFridge.class),
//				});
//		connections.put(
//				new EventSource(FridgeCoupledModel.SIL_URI, CoolFridge.class),
//				new EventSink[] {
//						new EventSink(ElectricMeterCoupledModel.SIL_URI,
//									  CoolFridge.class),
//				});
//		connections.put(
//				new EventSource(FridgeCoupledModel.SIL_URI, DoNotCoolFridge.class),
//				new EventSink[] {
//						new EventSink(ElectricMeterCoupledModel.SIL_URI,
//									  DoNotCoolFridge.class),
//				});
//		connections.put(
//				new EventSource(FridgeCoupledModel.SIL_URI, SwitchOnFridge.class),
//				new EventSink[] {
//						new EventSink(ElectricMeterCoupledModel.SIL_URI,
//									  SwitchOnFridge.class),
//				});
//		connections.put(
//				new EventSource(FridgeCoupledModel.SIL_URI, OpenDoorFridge.class),
//				new EventSink[] {
//						new EventSink(ElectricMeterCoupledModel.SIL_URI,
//									  OpenDoorFridge.class),
//				});
//		connections.put(
//				new EventSource(FridgeCoupledModel.SIL_URI, CloseDoorFridge.class),
//				new EventSink[] {
//						new EventSink(ElectricMeterCoupledModel.SIL_URI,
//									  CloseDoorFridge.class),
//				});
//		connections.put(
//				new EventSource(FridgeCoupledModel.SIL_URI, SetPowerFridge.class),
//				new EventSink[] {
//						new EventSink(ElectricMeterCoupledModel.SIL_URI,
//									  SetPowerFridge.class),
//				});

		// coupled model descriptor
		coupledModelDescriptors.put(
				GlobalCoupledModel.SIL_URI,
				RTComponentCoupledModelDescriptor.create(
						GlobalCoupledModel.class,
						GlobalCoupledModel.SIL_URI,
						submodels,
						null,
						null,
						connections,
						null,
						CoordinatorComponent.REFLECTION_INBOUND_PORT_URI,
						CoordinatorPlugin.class,
						null,
						accelerationFactor));

		ComponentModelArchitecture architecture =
				new ComponentModelArchitecture(
						GlobalSupervisor.SIL_SIM_ARCHITECTURE_URI,
						GlobalCoupledModel.SIL_URI,
						atomicModelDescriptors,
						coupledModelDescriptors,
						simulatedTimeUnit);

		return architecture;
	}
}
