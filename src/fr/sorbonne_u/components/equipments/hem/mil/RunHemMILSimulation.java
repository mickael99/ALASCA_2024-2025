package fr.sorbonne_u.components.equipments.hem.mil;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import fr.sorbonne_u.components.equipments.battery.mil.BatteryChargeLevelModel;
import fr.sorbonne_u.components.equipments.battery.mil.BatteryElectricityModel;
import fr.sorbonne_u.components.equipments.battery.mil.BatteryUserModel;
import fr.sorbonne_u.components.equipments.battery.mil.events.SetConsumeBatteryEvent;
import fr.sorbonne_u.components.equipments.battery.mil.events.SetProductBatteryEvent;
import fr.sorbonne_u.components.equipments.fridge.mil.ExternalTemperatureModel;
import fr.sorbonne_u.components.equipments.fridge.mil.FridgeElectricityModel;
import fr.sorbonne_u.components.equipments.fridge.mil.FridgeTemperatureModel;
import fr.sorbonne_u.components.equipments.fridge.mil.FridgeUnitTestModel;
import fr.sorbonne_u.components.equipments.fridge.mil.events.Cool;
import fr.sorbonne_u.components.equipments.fridge.mil.events.DoNotCool;
import fr.sorbonne_u.components.equipments.fridge.mil.events.SetPowerFridge;
import fr.sorbonne_u.components.equipments.fridge.mil.events.SwitchOffFridge;
import fr.sorbonne_u.components.equipments.fridge.mil.events.SwitchOnFridge;
import fr.sorbonne_u.components.equipments.iron.mil.IronElectricityModel;
import fr.sorbonne_u.components.equipments.iron.mil.IronUserModel;
import fr.sorbonne_u.components.equipments.iron.mil.events.DisableEnergySavingModeIron;
import fr.sorbonne_u.components.equipments.iron.mil.events.DisableSteamModeIron;
import fr.sorbonne_u.components.equipments.iron.mil.events.EnableCottonModeIron;
import fr.sorbonne_u.components.equipments.iron.mil.events.EnableDelicateModeIron;
import fr.sorbonne_u.components.equipments.iron.mil.events.EnableEnergySavingModeIron;
import fr.sorbonne_u.components.equipments.iron.mil.events.EnableLinenModeIron;
import fr.sorbonne_u.components.equipments.iron.mil.events.EnableSteamModeIron;
import fr.sorbonne_u.components.equipments.iron.mil.events.TurnOffIron;
import fr.sorbonne_u.components.equipments.iron.mil.events.TurnOnIron;
import fr.sorbonne_u.components.equipments.meter.mil.MeterElectricityModel;
import fr.sorbonne_u.components.equipments.windTurbine.mil.ExternalWindModel;
import fr.sorbonne_u.components.equipments.windTurbine.mil.WindTurbineElectricityModel;
import fr.sorbonne_u.components.equipments.windTurbine.mil.WindTurbineUserModel;
import fr.sorbonne_u.components.equipments.windTurbine.mil.events.SetWindSpeedEvent;
import fr.sorbonne_u.components.equipments.windTurbine.mil.events.StartWindTurbineEvent;
import fr.sorbonne_u.components.equipments.windTurbine.mil.events.StopWindTurbineEvent;
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

public class RunHemMILSimulation {

	public static void main(String[] args) {
        try {
        	Map<String, AbstractAtomicModelDescriptor> atomicModelDescriptors = new HashMap<>();

            // Add the fridge
        	atomicModelDescriptors.put(
					FridgeElectricityModel.URI,
					AtomicHIOA_Descriptor.create(
							FridgeElectricityModel.class,
							FridgeElectricityModel.URI,
							TimeUnit.SECONDS,
							null));
			atomicModelDescriptors.put(
					FridgeTemperatureModel.URI,
					AtomicHIOA_Descriptor.create(
							FridgeTemperatureModel.class,
							FridgeTemperatureModel.URI,
							TimeUnit.SECONDS,
							null));
			atomicModelDescriptors.put(
					ExternalTemperatureModel.URI,
					AtomicHIOA_Descriptor.create(
							ExternalTemperatureModel.class,
							ExternalTemperatureModel.URI,
							TimeUnit.SECONDS,
							null));
			atomicModelDescriptors.put(
					FridgeUnitTestModel.URI,
					AtomicModelDescriptor.create(
							FridgeUnitTestModel.class,
							FridgeUnitTestModel.URI,
							TimeUnit.SECONDS,
							null));

            // Add iron
			atomicModelDescriptors.put(
					IronElectricityModel.URI,
					AtomicHIOA_Descriptor.create(
							IronElectricityModel.class,
							IronElectricityModel.URI,
							TimeUnit.SECONDS,
							null));
			atomicModelDescriptors.put(
					IronUserModel.URI,
					AtomicModelDescriptor.create(
							IronUserModel.class,
							IronUserModel.URI,
							TimeUnit.SECONDS,
							null));
			
			// Add battery
			atomicModelDescriptors.put(
                    BatteryElectricityModel.URI,
                    AtomicHIOA_Descriptor.create(
                    		BatteryElectricityModel.class,
                    		BatteryElectricityModel.URI,
                            TimeUnit.SECONDS,
                            null
                    )
            );

            atomicModelDescriptors.put(
                    BatteryChargeLevelModel.URI,
                    AtomicHIOA_Descriptor.create(
                    		BatteryChargeLevelModel.class,
                    		BatteryChargeLevelModel.URI,
                            TimeUnit.SECONDS,
                            null
                    )
            );

            atomicModelDescriptors.put(
                    BatteryUserModel.URI,
                    AtomicModelDescriptor.create(
                            BatteryUserModel.class,
                            BatteryUserModel.URI,
                            TimeUnit.SECONDS,
                            null
                    )
            );
            
            // Add Wind turbine
            atomicModelDescriptors.put(
                    WindTurbineElectricityModel.URI,
                    AtomicHIOA_Descriptor.create(
                            WindTurbineElectricityModel.class,
                            WindTurbineElectricityModel.URI,
                            TimeUnit.HOURS,
                            null
                    )
            );

            atomicModelDescriptors.put(
                    ExternalWindModel.URI,
                    AtomicHIOA_Descriptor.create(
                            ExternalWindModel.class,
                            ExternalWindModel.URI,
                            TimeUnit.HOURS,
                            null
                    )
            );

            atomicModelDescriptors.put(
                    WindTurbineUserModel.URI,
                    AtomicModelDescriptor.create(
                            WindTurbineUserModel.class,
                            WindTurbineUserModel.URI,
                            TimeUnit.HOURS,
                            null
                    )
            );
            
            // Add Metter
            atomicModelDescriptors.put(
                    MeterElectricityModel.URI,
                    AtomicHIOA_Descriptor.create(
                            MeterElectricityModel.class,
                            MeterElectricityModel.URI,
                            TimeUnit.SECONDS,
                            null
                    )
            );

            // Submodels
            Set<String> submodels = new HashSet<>();
            submodels.add(FridgeElectricityModel.URI);
            submodels.add(FridgeTemperatureModel.URI);
            submodels.add(ExternalTemperatureModel.URI);
            submodels.add(FridgeUnitTestModel.URI);
            
            submodels.add(IronElectricityModel.URI);
			submodels.add(IronUserModel.URI);
			
			submodels.add(BatteryElectricityModel.URI);
			submodels.add(BatteryChargeLevelModel.URI);
			submodels.add(BatteryUserModel.URI);
			
			submodels.add(WindTurbineElectricityModel.URI);
			submodels.add(WindTurbineUserModel.URI);
			submodels.add(ExternalWindModel.URI);
			
			//submodels.add(MeterElectricityModel.URI);

            Map<EventSource, EventSink[]> connections = new HashMap<>();

            // Add the fridge events
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
			
			// Add iron
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
			
			// Add battery
			connections.put(
                    new EventSource(BatteryUserModel.URI, SetProductBatteryEvent.class),
                    new EventSink[] {
                            new EventSink(BatteryElectricityModel.URI, SetProductBatteryEvent.class),
                            new EventSink(BatteryChargeLevelModel.URI, SetProductBatteryEvent.class)
                    }
            );

            connections.put(
                    new EventSource(BatteryUserModel.URI, SetConsumeBatteryEvent.class),
                    new EventSink[] {
                            new EventSink(BatteryElectricityModel.URI, SetConsumeBatteryEvent.class),
                            new EventSink(BatteryChargeLevelModel.URI, SetConsumeBatteryEvent.class)
                    }
            );
            
            // Add wind turbine evnts
            connections.put(
                    new EventSource(WindTurbineUserModel.URI, StartWindTurbineEvent.class),
                    new EventSink[] {
                            new EventSink(WindTurbineElectricityModel.URI, StartWindTurbineEvent.class)
                    }
            );

            connections.put(
                    new EventSource(WindTurbineUserModel.URI, StopWindTurbineEvent.class),
                    new EventSink[] {
                            new EventSink(WindTurbineElectricityModel.URI, StopWindTurbineEvent.class)
                    }
            );

            connections.put(
                    new EventSource(ExternalWindModel.URI, SetWindSpeedEvent.class),
                    new EventSink[] {
                            new EventSink(WindTurbineElectricityModel.URI, SetWindSpeedEvent.class)
                    }
            );

            
            Map<VariableSource, VariableSink[]> bindings = new HashMap<>();
            
            // Add fridge bindings
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
			
			// Add battery bindings
			bindings.put(
                    new VariableSource("currentChargeLevel", Double.class, BatteryChargeLevelModel.URI),
                    new VariableSink[] {
                            new VariableSink("currentChargeLevel", Double.class, BatteryElectricityModel.URI)
                    }
            );

			// Add wind turbine bindings
			bindings.put(
                    new VariableSource("externalWindSpeed",
                            Double.class,
                            ExternalWindModel.URI),
                    new VariableSink[] {
                            new VariableSink("externalWindSpeed",
                                    Double.class,
                                    WindTurbineElectricityModel.URI)
                    });
			
			
			
			 Map<String, CoupledModelDescriptor> coupledModelDescriptors = new HashMap<>();

            coupledModelDescriptors.put(
                    HEM_CoupledModel.URI,
                    new CoupledHIOA_Descriptor(
                    		HEM_CoupledModel.class,
                    		HEM_CoupledModel.URI,
                            submodels,
                            null,
                            null,
                            connections,
                            null,
                            null,
                            null,
                            bindings
                    ));


            ArchitectureI architecture = new Architecture(
            		HEM_CoupledModel.URI,
                    atomicModelDescriptors,
                    coupledModelDescriptors,
                    TimeUnit.SECONDS
            );

            SimulatorI engine = architecture.constructSimulator();
            SimulationEngine.SIMULATION_STEP_SLEEP_TIME = 0L;
            engine.doStandAloneSimulation(0.0, 100.0);

            System.exit(0);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
