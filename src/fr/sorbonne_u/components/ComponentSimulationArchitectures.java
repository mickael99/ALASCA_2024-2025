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
import fr.sorbonne_u.components.equipments.battery.Battery;
import fr.sorbonne_u.components.equipments.battery.BatteryTester;
import fr.sorbonne_u.components.equipments.battery.mil.BatteryCoupledModel;
import fr.sorbonne_u.components.equipments.battery.mil.BatteryUserModel;
import fr.sorbonne_u.components.equipments.battery.mil.events.SetConsumeBatteryEvent;
import fr.sorbonne_u.components.equipments.battery.mil.events.SetProductBatteryEvent;
import fr.sorbonne_u.components.equipments.battery.mil.events.SetStandByBatteryEvent;
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
import fr.sorbonne_u.components.equipments.windTurbine.WindTurbine;
import fr.sorbonne_u.components.equipments.windTurbine.WindTurbineTester;
import fr.sorbonne_u.components.equipments.windTurbine.mil.WindTurbineCoupledModel;
import fr.sorbonne_u.components.equipments.windTurbine.mil.WindTurbineUserModel;
import fr.sorbonne_u.components.equipments.windTurbine.mil.events.StartWindTurbineEvent;
import fr.sorbonne_u.components.equipments.windTurbine.mil.events.StopWindTurbineEvent;
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
		
//		// WindTurbine
//		atomicModelDescriptors.put(
//				WindTurbineCoupledModel.MIL_URI,
//				ComponentAtomicModelDescriptor.create(
//						WindTurbineCoupledModel.MIL_URI,
//						(Class<? extends EventI>[]) new Class<?>[]{
//							StartWindTurbineEvent.class,
//							StopWindTurbineEvent.class},
//						(Class<? extends EventI>[]) new Class<?>[]{
//							StartWindTurbineEvent.class,
//							StopWindTurbineEvent.class},
//						simulatedTimeUnit,
//						WindTurbine.REFLECTION_INBOUND_PORT_URI));
//		atomicModelDescriptors.put(
//				WindTurbineUserModel.MIL_URI,
//				ComponentAtomicModelDescriptor.create(
//						WindTurbineUserModel.MIL_URI,
//						(Class<? extends EventI>[]) new Class<?>[]{},
//						(Class<? extends EventI>[]) new Class<?>[]{
//							StartWindTurbineEvent.class,
//							StopWindTurbineEvent.class},
//						simulatedTimeUnit,
//						WindTurbineTester.REFLECTION_INBOUND_PORT_URI));
//		
//		// Battery
//		atomicModelDescriptors.put(
//				BatteryCoupledModel.MIL_URI,
//				ComponentAtomicModelDescriptor.create(
//						BatteryCoupledModel.MIL_URI,
//						(Class<? extends EventI>[]) new Class<?>[]{
//							SetProductBatteryEvent.class,
//							SetConsumeBatteryEvent.class,
//							SetStandByBatteryEvent.class},
//						(Class<? extends EventI>[]) new Class<?>[]{
//							SetProductBatteryEvent.class,
//							SetConsumeBatteryEvent.class,
//							SetStandByBatteryEvent.class},
//						simulatedTimeUnit,
//						Battery.REFLECTION_INBOUND_PORT_URI));
//		atomicModelDescriptors.put(
//				BatteryUserModel.MIL_URI,
//				ComponentAtomicModelDescriptor.create(
//						BatteryUserModel.MIL_URI,
//						(Class<? extends EventI>[]) new Class<?>[]{},
//						(Class<? extends EventI>[]) new Class<?>[]{
//							SetProductBatteryEvent.class,
//							SetConsumeBatteryEvent.class,
//							SetStandByBatteryEvent.class},
//						simulatedTimeUnit,
//						BatteryTester.REFLECTION_INBOUND_PORT_URI));
		
		// Electric Metter
		atomicModelDescriptors.put(
				ElectricMeterCoupledModel.MIL_URI,
				ComponentAtomicModelDescriptor.create(
						ElectricMeterCoupledModel.MIL_URI,
						(Class<? extends EventI>[]) new Class<?>[]{
							// Fridge
							CloseDoorFridge.class,
							CoolFridge.class,
							DoNotCoolFridge.class,
							OpenDoorFridge.class,
							SetPowerFridge.class,
							SwitchOffFridge.class,
							SwitchOnFridge.class,
							
							// Iron
							DisableEnergySavingModeIron.class,
							DisableSteamModeIron.class,
							EnableCottonModeIron.class,
							EnableDelicateModeIron.class,
							EnableEnergySavingModeIron.class,
							EnableLinenModeIron.class,
							EnableSteamModeIron.class,
							TurnOffIron.class,
							TurnOnIron.class,
//							
//							// Wind Turbine
//							StartWindTurbineEvent.class,
//							StopWindTurbineEvent.class,
//							
//							// Battery
//							SetProductBatteryEvent.class,
//							SetConsumeBatteryEvent.class,
//							SetStandByBatteryEvent.class
							},
						(Class<? extends EventI>[]) new Class<?>[]{},
						simulatedTimeUnit,
						ElectricMeter.REFLECTION_INBOUND_PORT_URI));

		Map<String,CoupledModelDescriptor> coupledModelDescriptors = new HashMap<>();

		/* 				SUBMODELS			*/
		Set<String> submodels = new HashSet<String>();
		// Iron
		submodels.add(IronStateModel.MIL_URI);
		submodels.add(IronUserModel.MIL_URI);
		
		// Fridge
		submodels.add(FridgeCoupledModel.MIL_URI);
		submodels.add(FridgeUnitTestModel.MIL_URI);
		
//		// WindTurbine
//		submodels.add(WindTurbineCoupledModel.MIL_URI);
//		submodels.add(WindTurbineUserModel.MIL_URI);
//		
//		// Battery
//		submodels.add(BatteryUserModel.MIL_URI);
//		submodels.add(BatteryCoupledModel.MIL_URI);
		
		// Electric Metter
		submodels.add(ElectricMeterCoupledModel.MIL_URI);

		
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
		connections.put(
			new EventSource(IronStateModel.MIL_URI,
					TurnOnIron.class),
			new EventSink[] {
				new EventSink(ElectricMeterCoupledModel.MIL_URI,
						TurnOnIron.class)
			});
		connections.put(
			new EventSource(IronStateModel.MIL_URI,
					TurnOffIron.class),
			new EventSink[] {
				new EventSink(ElectricMeterCoupledModel.MIL_URI,
						TurnOffIron.class)
			});
		connections.put(
			new EventSource(IronStateModel.MIL_URI,
					DisableEnergySavingModeIron.class),
			new EventSink[] {
				new EventSink(ElectricMeterCoupledModel.MIL_URI,
							DisableEnergySavingModeIron.class)
			});
		connections.put(
			new EventSource(IronStateModel.MIL_URI,
					DisableSteamModeIron.class),
			new EventSink[] {
				new EventSink(ElectricMeterCoupledModel.MIL_URI,
						DisableSteamModeIron.class)
			});
		connections.put(
				new EventSource(IronStateModel.MIL_URI,
						EnableCottonModeIron.class),
				new EventSink[] {
					new EventSink(ElectricMeterCoupledModel.MIL_URI,
							EnableCottonModeIron.class)
				});
		connections.put(
				new EventSource(IronStateModel.MIL_URI,
						EnableDelicateModeIron.class),
				new EventSink[] {
					new EventSink(ElectricMeterCoupledModel.MIL_URI,
							EnableDelicateModeIron.class)
				});
		connections.put(
				new EventSource(IronStateModel.MIL_URI,
						EnableLinenModeIron.class),
				new EventSink[] {
					new EventSink(ElectricMeterCoupledModel.MIL_URI,
							EnableLinenModeIron.class)
				});
		connections.put(
				new EventSource(IronStateModel.MIL_URI,
						EnableEnergySavingModeIron.class),
				new EventSink[] {
					new EventSink(ElectricMeterCoupledModel.MIL_URI,
							EnableEnergySavingModeIron.class)
				});
		connections.put(
				new EventSource(IronStateModel.MIL_URI,
						EnableSteamModeIron.class),
				new EventSink[] {
					new EventSink(ElectricMeterCoupledModel.MIL_URI,
							EnableSteamModeIron.class)
				});
			
			
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
		connections.put(
				new EventSource(FridgeCoupledModel.MIL_URI, SwitchOffFridge.class),
				new EventSink[] {
						new EventSink(ElectricMeterCoupledModel.MIL_URI,
									  SwitchOffFridge.class),
				});
		connections.put(
				new EventSource(FridgeCoupledModel.MIL_URI, CoolFridge.class),
				new EventSink[] {
						new EventSink(ElectricMeterCoupledModel.MIL_URI,
									  CoolFridge.class),
				});
		connections.put(
				new EventSource(FridgeCoupledModel.MIL_URI, DoNotCoolFridge.class),
				new EventSink[] {
						new EventSink(ElectricMeterCoupledModel.MIL_URI,
									  DoNotCoolFridge.class),
				});
		connections.put(
				new EventSource(FridgeCoupledModel.MIL_URI, SwitchOnFridge.class),
				new EventSink[] {
						new EventSink(ElectricMeterCoupledModel.MIL_URI,
									  SwitchOnFridge.class),
				});
		connections.put(
				new EventSource(FridgeCoupledModel.MIL_URI, OpenDoorFridge.class),
				new EventSink[] {
						new EventSink(ElectricMeterCoupledModel.MIL_URI,
									  OpenDoorFridge.class),
				});
		connections.put(
				new EventSource(FridgeCoupledModel.MIL_URI, CloseDoorFridge.class),
				new EventSink[] {
						new EventSink(ElectricMeterCoupledModel.MIL_URI,
									  CloseDoorFridge.class),
				});
		connections.put(
				new EventSource(FridgeCoupledModel.MIL_URI, SetPowerFridge.class),
				new EventSink[] {
						new EventSink(ElectricMeterCoupledModel.MIL_URI,
									  SetPowerFridge.class),
				});
		
//		// WindTurbineUser -> WindTurbineCoupled
//		connections.put(
//                new EventSource(WindTurbineUserModel.MIL_URI, StartWindTurbineEvent.class),
//                new EventSink[] {
//                        new EventSink(WindTurbineCoupledModel.MIL_URI, StartWindTurbineEvent.class)
//                }
//        );
//
//        connections.put(
//                new EventSource(WindTurbineUserModel.MIL_URI, StopWindTurbineEvent.class),
//                new EventSink[] {
//                        new EventSink(WindTurbineCoupledModel.MIL_URI, StopWindTurbineEvent.class)
//                }
//        );
//        
//        // WindTurbineCoupled -> ElectricMeterCoupled
//      connections.put(
//              new EventSource(WindTurbineCoupledModel.MIL_URI, StartWindTurbineEvent.class),
//              new EventSink[] {
//                      new EventSink(ElectricMeterCoupledModel.MIL_URI, StartWindTurbineEvent.class)
//              }
//      );
//      
//      connections.put(
//              new EventSource(WindTurbineCoupledModel.MIL_URI, StopWindTurbineEvent.class),
//              new EventSink[] {
//                      new EventSink(ElectricMeterCoupledModel.MIL_URI, StopWindTurbineEvent.class)
//              }
//      );
//        
//        // BatteryUser -> BatteryCoupledModel
//        connections.put(
//                new EventSource(BatteryUserModel.MIL_URI, SetProductBatteryEvent.class),
//                new EventSink[] {
//                        new EventSink(BatteryCoupledModel.MIL_URI, SetProductBatteryEvent.class)
//                }
//        );
//
//        connections.put(
//                new EventSource(BatteryUserModel.MIL_URI, SetConsumeBatteryEvent.class),
//                new EventSink[] {
//                        new EventSink(BatteryCoupledModel.MIL_URI, SetConsumeBatteryEvent.class)
//                }
//        );
//        
//        connections.put(
//                new EventSource(BatteryUserModel.MIL_URI, SetStandByBatteryEvent.class),
//                new EventSink[] {
//                        new EventSink(BatteryCoupledModel.MIL_URI, SetStandByBatteryEvent.class)
//                }
//        );
//        
//        // BatteryCoupledModel -> ElectricMeterCoupledModel
//        connections.put(
//                new EventSource(BatteryCoupledModel.MIL_URI, SetProductBatteryEvent.class),
//                new EventSink[] {
//                        new EventSink(ElectricMeterCoupledModel.MIL_URI, SetProductBatteryEvent.class)
//                }
//        );
//
//        connections.put(
//                new EventSource(BatteryCoupledModel.MIL_URI, SetConsumeBatteryEvent.class),
//                new EventSink[] {
//                        new EventSink(ElectricMeterCoupledModel.MIL_URI, SetConsumeBatteryEvent.class)
//                }
//        );
//        
//        connections.put(
//                new EventSource(BatteryCoupledModel.MIL_URI, SetStandByBatteryEvent.class),
//                new EventSink[] {
//                        new EventSink(ElectricMeterCoupledModel.MIL_URI, SetStandByBatteryEvent.class)
//                }
//        );

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
		
//		// WindTurbine
//		atomicModelDescriptors.put(
//				WindTurbineCoupledModel.MIL_RT_URI,
//				RTComponentAtomicModelDescriptor.create(
//						WindTurbineCoupledModel.MIL_RT_URI,
//						(Class<? extends EventI>[]) new Class<?>[]{
//							StartWindTurbineEvent.class,
//							StopWindTurbineEvent.class},
//						(Class<? extends EventI>[]) new Class<?>[]{
//							StartWindTurbineEvent.class,
//							StopWindTurbineEvent.class},
//						simulatedTimeUnit,
//						WindTurbine.REFLECTION_INBOUND_PORT_URI));
//		atomicModelDescriptors.put(
//				WindTurbineUserModel.MIL_RT_URI,
//				RTComponentAtomicModelDescriptor.create(
//						WindTurbineUserModel.MIL_RT_URI,
//						(Class<? extends EventI>[]) new Class<?>[]{},
//						(Class<? extends EventI>[]) new Class<?>[]{
//							StartWindTurbineEvent.class,
//							StopWindTurbineEvent.class},
//						simulatedTimeUnit,
//						WindTurbineTester.REFLECTION_INBOUND_PORT_URI));
//		
//		// Battery
//		atomicModelDescriptors.put(
//				BatteryCoupledModel.MIL_RT_URI,
//				RTComponentAtomicModelDescriptor.create(
//						BatteryCoupledModel.MIL_RT_URI,
//						(Class<? extends EventI>[]) new Class<?>[]{
//							SetProductBatteryEvent.class,
//							SetConsumeBatteryEvent.class,
//							SetStandByBatteryEvent.class},
//						(Class<? extends EventI>[]) new Class<?>[]{
//							SetProductBatteryEvent.class,
//							SetConsumeBatteryEvent.class,
//							SetStandByBatteryEvent.class},
//						simulatedTimeUnit,
//						Battery.REFLECTION_INBOUND_PORT_URI));
//		atomicModelDescriptors.put(
//				BatteryUserModel.MIL_RT_URI,
//				RTComponentAtomicModelDescriptor.create(
//						BatteryUserModel.MIL_RT_URI,
//						(Class<? extends EventI>[]) new Class<?>[]{},
//						(Class<? extends EventI>[]) new Class<?>[]{
//							SetProductBatteryEvent.class,
//							SetConsumeBatteryEvent.class,
//							SetStandByBatteryEvent.class},
//						simulatedTimeUnit,
//						BatteryTester.REFLECTION_INBOUND_PORT_URI));

		atomicModelDescriptors.put(
				ElectricMeterCoupledModel.MIL_RT_URI,
				RTComponentAtomicModelDescriptor.create(
						ElectricMeterCoupledModel.MIL_RT_URI,
						(Class<? extends EventI>[]) new Class<?>[]{
							// Fridge
							CloseDoorFridge.class,
							CoolFridge.class,
							DoNotCoolFridge.class,
							OpenDoorFridge.class,
							SetPowerFridge.class,
							SwitchOffFridge.class,
							SwitchOnFridge.class,
							
							// Iron
							DisableEnergySavingModeIron.class,
							DisableSteamModeIron.class,
							EnableCottonModeIron.class,
							EnableDelicateModeIron.class,
							EnableEnergySavingModeIron.class,
							EnableLinenModeIron.class,
							EnableSteamModeIron.class,
							TurnOffIron.class,
							TurnOnIron.class
							
//							// Wind turbine
//							StartWindTurbineEvent.class,
//							StopWindTurbineEvent.class,
//							
//							// Battery
//							SetProductBatteryEvent.class,
//							SetConsumeBatteryEvent.class,
//							SetStandByBatteryEvent.class
							},
						(Class<? extends EventI>[]) new Class<?>[]{},
						simulatedTimeUnit,
						ElectricMeter.REFLECTION_INBOUND_PORT_URI));

		
		Map<String,CoupledModelDescriptor> coupledModelDescriptors =
															new HashMap<>();


		Set<String> submodels = new HashSet<String>();
		submodels.add(IronStateModel.MIL_RT_URI);
		submodels.add(IronUserModel.MIL_RT_URI);
		
		submodels.add(FridgeCoupledModel.MIL_RT_URI);
		submodels.add(FridgeUnitTestModel.MIL_RT_URI);
		
//		submodels.add(WindTurbineCoupledModel.MIL_RT_URI);
//		submodels.add(WindTurbineUserModel.MIL_RT_URI);
//		
//		submodels.add(BatteryUserModel.MIL_RT_URI);
//		submodels.add(BatteryCoupledModel.MIL_RT_URI);
		
		submodels.add(ElectricMeterCoupledModel.MIL_RT_URI);

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
		connections.put(
			new EventSource(IronStateModel.MIL_RT_URI,
					TurnOnIron.class),
			new EventSink[] {
				new EventSink(ElectricMeterCoupledModel.MIL_RT_URI,
						TurnOnIron.class)
			});
		connections.put(
			new EventSource(IronStateModel.MIL_RT_URI,
					TurnOffIron.class),
			new EventSink[] {
				new EventSink(ElectricMeterCoupledModel.MIL_RT_URI,
						TurnOffIron.class)
			});
		connections.put(
			new EventSource(IronStateModel.MIL_RT_URI,
					DisableEnergySavingModeIron.class),
			new EventSink[] {
				new EventSink(ElectricMeterCoupledModel.MIL_RT_URI,
							DisableEnergySavingModeIron.class)
			});
		connections.put(
			new EventSource(IronStateModel.MIL_RT_URI,
					DisableSteamModeIron.class),
			new EventSink[] {
				new EventSink(ElectricMeterCoupledModel.MIL_RT_URI,
						DisableSteamModeIron.class)
			});
		connections.put(
				new EventSource(IronStateModel.MIL_RT_URI,
						EnableCottonModeIron.class),
				new EventSink[] {
					new EventSink(ElectricMeterCoupledModel.MIL_RT_URI,
							EnableCottonModeIron.class)
				});
		connections.put(
				new EventSource(IronStateModel.MIL_RT_URI,
						EnableDelicateModeIron.class),
				new EventSink[] {
					new EventSink(ElectricMeterCoupledModel.MIL_RT_URI,
							EnableDelicateModeIron.class)
				});
		connections.put(
				new EventSource(IronStateModel.MIL_RT_URI,
						EnableLinenModeIron.class),
				new EventSink[] {
					new EventSink(ElectricMeterCoupledModel.MIL_RT_URI,
							EnableLinenModeIron.class)
				});
		connections.put(
				new EventSource(IronStateModel.MIL_RT_URI,
						EnableEnergySavingModeIron.class),
				new EventSink[] {
					new EventSink(ElectricMeterCoupledModel.MIL_RT_URI,
							EnableEnergySavingModeIron.class)
				});
		connections.put(
				new EventSource(IronStateModel.MIL_RT_URI,
						EnableSteamModeIron.class),
				new EventSink[] {
					new EventSink(ElectricMeterCoupledModel.MIL_RT_URI,
							EnableSteamModeIron.class)
				});
			
			
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
		connections.put(
				new EventSource(FridgeCoupledModel.MIL_RT_URI, SwitchOffFridge.class),
				new EventSink[] {
						new EventSink(ElectricMeterCoupledModel.MIL_RT_URI,
									  SwitchOffFridge.class),
				});
		connections.put(
				new EventSource(FridgeCoupledModel.MIL_RT_URI, CoolFridge.class),
				new EventSink[] {
						new EventSink(ElectricMeterCoupledModel.MIL_RT_URI,
									  CoolFridge.class),
				});
		connections.put(
				new EventSource(FridgeCoupledModel.MIL_RT_URI, DoNotCoolFridge.class),
				new EventSink[] {
						new EventSink(ElectricMeterCoupledModel.MIL_RT_URI,
									  DoNotCoolFridge.class),
				});
		connections.put(
				new EventSource(FridgeCoupledModel.MIL_RT_URI, SwitchOnFridge.class),
				new EventSink[] {
						new EventSink(ElectricMeterCoupledModel.MIL_RT_URI,
									  SwitchOnFridge.class),
				});
		connections.put(
				new EventSource(FridgeCoupledModel.MIL_RT_URI, OpenDoorFridge.class),
				new EventSink[] {
						new EventSink(ElectricMeterCoupledModel.MIL_RT_URI,
									  OpenDoorFridge.class),
				});
		connections.put(
				new EventSource(FridgeCoupledModel.MIL_RT_URI, CloseDoorFridge.class),
				new EventSink[] {
						new EventSink(ElectricMeterCoupledModel.MIL_RT_URI,
									  CloseDoorFridge.class),
				});
		connections.put(
				new EventSource(FridgeCoupledModel.MIL_RT_URI, SetPowerFridge.class),
				new EventSink[] {
						new EventSink(ElectricMeterCoupledModel.MIL_RT_URI,
									  SetPowerFridge.class),
				});

//		// WindTurbineUser -> WindTurbineCoupled
//		connections.put(
//                new EventSource(WindTurbineUserModel.MIL_RT_URI, StartWindTurbineEvent.class),
//                new EventSink[] {
//                        new EventSink(WindTurbineCoupledModel.MIL_RT_URI, StartWindTurbineEvent.class)
//                }
//        );
//
//        connections.put(
//                new EventSource(WindTurbineUserModel.MIL_RT_URI, StopWindTurbineEvent.class),
//                new EventSink[] {
//                        new EventSink(WindTurbineCoupledModel.MIL_RT_URI, StopWindTurbineEvent.class)
//                }
//        );
//        
//        
//        
//        
//        // WindTurbineCoupled -> ElectricMeterCoupled
//        connections.put(
//                new EventSource(WindTurbineCoupledModel.MIL_RT_URI, StartWindTurbineEvent.class),
//                new EventSink[] {
//                        new EventSink(ElectricMeterCoupledModel.MIL_RT_URI, StartWindTurbineEvent.class)
//                }
//        );
//        
//        connections.put(
//                new EventSource(WindTurbineCoupledModel.MIL_RT_URI, StopWindTurbineEvent.class),
//                new EventSink[] {
//                        new EventSink(ElectricMeterCoupledModel.MIL_RT_URI, StopWindTurbineEvent.class)
//                }
//        );
//		        
//        // BatteryUser -> BatteryCoupledModel
//        connections.put(
//                new EventSource(BatteryUserModel.MIL_RT_URI, SetProductBatteryEvent.class),
//                new EventSink[] {
//                        new EventSink(BatteryCoupledModel.MIL_RT_URI, SetProductBatteryEvent.class)
//                }
//        );
//
//        connections.put(
//                new EventSource(BatteryUserModel.MIL_RT_URI, SetConsumeBatteryEvent.class),
//                new EventSink[] {
//                        new EventSink(BatteryCoupledModel.MIL_RT_URI, SetConsumeBatteryEvent.class)
//                }
//        );
//        
//        connections.put(
//                new EventSource(BatteryUserModel.MIL_RT_URI, SetStandByBatteryEvent.class),
//                new EventSink[] {
//                        new EventSink(BatteryCoupledModel.MIL_RT_URI, SetStandByBatteryEvent.class)
//                }
//        );
//        
//        // BatteryCoupledModel -> ElectricMeterCoupledModel
//        connections.put(
//                new EventSource(BatteryCoupledModel.MIL_RT_URI, SetProductBatteryEvent.class),
//                new EventSink[] {
//                        new EventSink(ElectricMeterCoupledModel.MIL_RT_URI, SetProductBatteryEvent.class)
//                }
//        );
//
//        connections.put(
//                new EventSource(BatteryCoupledModel.MIL_RT_URI, SetConsumeBatteryEvent.class),
//                new EventSink[] {
//                        new EventSink(ElectricMeterCoupledModel.MIL_RT_URI, SetConsumeBatteryEvent.class)
//                }
//        );
//        
//        connections.put(
//                new EventSource(BatteryCoupledModel.MIL_RT_URI, SetStandByBatteryEvent.class),
//                new EventSink[] {
//                        new EventSink(ElectricMeterCoupledModel.MIL_RT_URI, SetStandByBatteryEvent.class)
//                }
//        );
        
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
		
//		// WindTurbine
//		atomicModelDescriptors.put(
//				WindTurbineCoupledModel.SIL_URI,
//				RTComponentAtomicModelDescriptor.create(
//						WindTurbineCoupledModel.SIL_URI,
//						(Class<? extends EventI>[]) new Class<?>[]{
//							StartWindTurbineEvent.class,
//							StopWindTurbineEvent.class},
//						(Class<? extends EventI>[]) new Class<?>[]{
//							StartWindTurbineEvent.class,
//							StopWindTurbineEvent.class},
//						simulatedTimeUnit,
//						WindTurbine.REFLECTION_INBOUND_PORT_URI));
//		
//		// Battery
//		atomicModelDescriptors.put(
//				BatteryCoupledModel.SIL_URI,
//				RTComponentAtomicModelDescriptor.create(
//						BatteryCoupledModel.SIL_URI,
//						(Class<? extends EventI>[]) new Class<?>[]{
//							SetProductBatteryEvent.class,
//							SetConsumeBatteryEvent.class,
//							SetStandByBatteryEvent.class},
//						(Class<? extends EventI>[]) new Class<?>[]{
//							SetProductBatteryEvent.class,
//							SetConsumeBatteryEvent.class,
//							SetStandByBatteryEvent.class},
//						simulatedTimeUnit,
//						Battery.REFLECTION_INBOUND_PORT_URI));

		atomicModelDescriptors.put(
				ElectricMeterCoupledModel.SIL_URI,
				RTComponentAtomicModelDescriptor.create(
						ElectricMeterCoupledModel.SIL_URI,
						(Class<? extends EventI>[]) new Class<?>[]{
							// Fridge
							CloseDoorFridge.class,
							CoolFridge.class,
							DoNotCoolFridge.class,
							OpenDoorFridge.class,
							SetPowerFridge.class,
							SwitchOffFridge.class,
							SwitchOnFridge.class,
							
							// Iron
							DisableEnergySavingModeIron.class,
							DisableSteamModeIron.class,
							EnableCottonModeIron.class,
							EnableDelicateModeIron.class,
							EnableEnergySavingModeIron.class,
							EnableLinenModeIron.class,
							EnableSteamModeIron.class,
							TurnOffIron.class,
							TurnOnIron.class
							
//							// Wind turbine
//							StartWindTurbineEvent.class,
//							StopWindTurbineEvent.class,
//							
//							// Battery
//							SetProductBatteryEvent.class,
//							SetConsumeBatteryEvent.class,
//							SetStandByBatteryEvent.class
							},
						(Class<? extends EventI>[]) new Class<?>[]{},
						simulatedTimeUnit,
						ElectricMeter.REFLECTION_INBOUND_PORT_URI));

		Map<String,CoupledModelDescriptor> coupledModelDescriptors =
															new HashMap<>();

		Set<String> submodels = new HashSet<String>();
		submodels.add(IronStateModel.SIL_URI);
		submodels.add(FridgeCoupledModel.SIL_URI);
//		submodels.add(WindTurbineCoupledModel.SIL_URI);
//		submodels.add(BatteryCoupledModel.SIL_URI);
		submodels.add(ElectricMeterCoupledModel.SIL_URI);

		Map<EventSource,EventSink[]> connections = 	new HashMap<EventSource,EventSink[]>();

		
		// IronStateModel -> ElectricMeterCoupledModel
		connections.put(
			new EventSource(IronStateModel.SIL_URI,
					TurnOnIron.class),
			new EventSink[] {
				new EventSink(ElectricMeterCoupledModel.SIL_URI,
						TurnOnIron.class)
			});
		connections.put(
			new EventSource(IronStateModel.SIL_URI,
					TurnOffIron.class),
			new EventSink[] {
				new EventSink(ElectricMeterCoupledModel.SIL_URI,
						TurnOffIron.class)
			});
		connections.put(
			new EventSource(IronStateModel.SIL_URI,
					DisableEnergySavingModeIron.class),
			new EventSink[] {
				new EventSink(ElectricMeterCoupledModel.SIL_URI,
							DisableEnergySavingModeIron.class)
			});
		connections.put(
			new EventSource(IronStateModel.SIL_URI,
					DisableSteamModeIron.class),
			new EventSink[] {
				new EventSink(ElectricMeterCoupledModel.SIL_URI,
						DisableSteamModeIron.class)
			});
		connections.put(
				new EventSource(IronStateModel.SIL_URI,
						EnableCottonModeIron.class),
				new EventSink[] {
					new EventSink(ElectricMeterCoupledModel.SIL_URI,
							EnableCottonModeIron.class)
				});
		connections.put(
				new EventSource(IronStateModel.SIL_URI,
						EnableDelicateModeIron.class),
				new EventSink[] {
					new EventSink(ElectricMeterCoupledModel.SIL_URI,
							EnableDelicateModeIron.class)
				});
		connections.put(
				new EventSource(IronStateModel.SIL_URI,
						EnableLinenModeIron.class),
				new EventSink[] {
					new EventSink(ElectricMeterCoupledModel.SIL_URI,
							EnableLinenModeIron.class)
				});
		connections.put(
				new EventSource(IronStateModel.SIL_URI,
						EnableEnergySavingModeIron.class),
				new EventSink[] {
					new EventSink(ElectricMeterCoupledModel.SIL_URI,
							EnableEnergySavingModeIron.class)
				});
		connections.put(
				new EventSource(IronStateModel.SIL_URI,
						EnableSteamModeIron.class),
				new EventSink[] {
					new EventSink(ElectricMeterCoupledModel.SIL_URI,
							EnableSteamModeIron.class)
				});

		// FridgeCoupledModel -> ElectricMeterCoupledModel
		connections.put(
				new EventSource(FridgeCoupledModel.SIL_URI, SwitchOffFridge.class),
				new EventSink[] {
						new EventSink(ElectricMeterCoupledModel.SIL_URI,
									  SwitchOffFridge.class),
				});
		connections.put(
				new EventSource(FridgeCoupledModel.SIL_URI, CoolFridge.class),
				new EventSink[] {
						new EventSink(ElectricMeterCoupledModel.SIL_URI,
									  CoolFridge.class),
				});
		connections.put(
				new EventSource(FridgeCoupledModel.SIL_URI, DoNotCoolFridge.class),
				new EventSink[] {
						new EventSink(ElectricMeterCoupledModel.SIL_URI,
									  DoNotCoolFridge.class),
				});
		connections.put(
				new EventSource(FridgeCoupledModel.SIL_URI, SwitchOnFridge.class),
				new EventSink[] {
						new EventSink(ElectricMeterCoupledModel.SIL_URI,
									  SwitchOnFridge.class),
				});
		connections.put(
				new EventSource(FridgeCoupledModel.SIL_URI, OpenDoorFridge.class),
				new EventSink[] {
						new EventSink(ElectricMeterCoupledModel.SIL_URI,
									  OpenDoorFridge.class),
				});
		connections.put(
				new EventSource(FridgeCoupledModel.SIL_URI, CloseDoorFridge.class),
				new EventSink[] {
						new EventSink(ElectricMeterCoupledModel.SIL_URI,
									  CloseDoorFridge.class),
				});
		connections.put(
				new EventSource(FridgeCoupledModel.SIL_URI, SetPowerFridge.class),
				new EventSink[] {
						new EventSink(ElectricMeterCoupledModel.SIL_URI,
									  SetPowerFridge.class),
				});
        
  
//        // WindTurbineCoupled -> ElectricMeterCoupled
//        connections.put(
//                new EventSource(WindTurbineCoupledModel.SIL_URI, StartWindTurbineEvent.class),
//                new EventSink[] {
//                        new EventSink(ElectricMeterCoupledModel.SIL_URI, StartWindTurbineEvent.class)
//                }
//        );
//        
//        connections.put(
//                new EventSource(WindTurbineCoupledModel.SIL_URI, StopWindTurbineEvent.class),
//                new EventSink[] {
//                        new EventSink(ElectricMeterCoupledModel.SIL_URI, StopWindTurbineEvent.class)
//                }
//        );
//		
//		  // BatteryCoupledModel -> ElectricMeterCoupledModel
//      connections.put(
//              new EventSource(BatteryCoupledModel.SIL_URI, SetProductBatteryEvent.class),
//              new EventSink[] {
//                      new EventSink(ElectricMeterCoupledModel.SIL_URI, SetProductBatteryEvent.class)
//              }
//      );
//
//      connections.put(
//              new EventSource(BatteryCoupledModel.SIL_URI, SetConsumeBatteryEvent.class),
//              new EventSink[] {
//                      new EventSink(ElectricMeterCoupledModel.SIL_URI, SetConsumeBatteryEvent.class)
//              }
//      );
//      
//      connections.put(
//              new EventSource(BatteryCoupledModel.SIL_URI, SetStandByBatteryEvent.class),
//              new EventSink[] {
//                      new EventSink(ElectricMeterCoupledModel.SIL_URI, SetStandByBatteryEvent.class)
//              }
//      );

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
