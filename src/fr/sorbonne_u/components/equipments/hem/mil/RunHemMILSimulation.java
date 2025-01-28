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
import fr.sorbonne_u.components.equipments.fridge.mil.FridgeStateModel;
import fr.sorbonne_u.components.equipments.fridge.mil.FridgeTemperatureModel;
import fr.sorbonne_u.components.equipments.fridge.mil.FridgeUnitTestModel;
import fr.sorbonne_u.components.equipments.fridge.mil.events.CloseDoorFridge;
import fr.sorbonne_u.components.equipments.fridge.mil.events.CoolFridge;
import fr.sorbonne_u.components.equipments.fridge.mil.events.DoNotCoolFridge;
import fr.sorbonne_u.components.equipments.fridge.mil.events.OpenDoorFridge;
import fr.sorbonne_u.components.equipments.fridge.mil.events.SetPowerFridge;
import fr.sorbonne_u.components.equipments.fridge.mil.events.SwitchOffFridge;
import fr.sorbonne_u.components.equipments.fridge.mil.events.SwitchOnFridge;
import fr.sorbonne_u.components.equipments.generator.mil.GeneratorElectricityModel;
import fr.sorbonne_u.components.equipments.generator.mil.GeneratorFuelModel;
import fr.sorbonne_u.components.equipments.generator.mil.GeneratorUserModel;
import fr.sorbonne_u.components.equipments.generator.mil.events.ActivateGeneratorEvent;
import fr.sorbonne_u.components.equipments.generator.mil.events.StopGeneratorEvent;
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
import fr.sorbonne_u.components.equipments.meter.mil.ElectricMeterElectricityModel;
import fr.sorbonne_u.components.equipments.smartLighting.mil.ExternalIlluminanceModel;
import fr.sorbonne_u.components.equipments.smartLighting.mil.SmartLightingElectricityModel;
import fr.sorbonne_u.components.equipments.smartLighting.mil.SmartLightingIlluminanceModel;
import fr.sorbonne_u.components.equipments.smartLighting.mil.SmartLightingUnitTesterModel;
import fr.sorbonne_u.components.equipments.smartLighting.mil.events.DecreaseLighting;
import fr.sorbonne_u.components.equipments.smartLighting.mil.events.IncreaseLighting;
import fr.sorbonne_u.components.equipments.smartLighting.mil.events.SetPowerSmartLighting;
import fr.sorbonne_u.components.equipments.smartLighting.mil.events.StopAdjustingLighting;
import fr.sorbonne_u.components.equipments.smartLighting.mil.events.TurnOffSmartLighting;
import fr.sorbonne_u.components.equipments.smartLighting.mil.events.TurnOnSmartLighting;
import fr.sorbonne_u.components.equipments.windTurbine.mil.ExternalWindModel;
import fr.sorbonne_u.components.equipments.windTurbine.mil.WindTurbineElectricityModel;
import fr.sorbonne_u.components.equipments.windTurbine.mil.WindTurbineUserModel;
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
					FridgeStateModel.MIL_URI,
					AtomicModelDescriptor.create(
							FridgeStateModel.class,
							FridgeStateModel.MIL_URI,
							TimeUnit.SECONDS,
							null));
			atomicModelDescriptors.put(
					FridgeElectricityModel.MIL_URI,
					AtomicHIOA_Descriptor.create(
							FridgeElectricityModel.class,
							FridgeElectricityModel.MIL_URI,
							TimeUnit.SECONDS,
							null));
			atomicModelDescriptors.put(
					FridgeTemperatureModel.MIL_URI,
					AtomicHIOA_Descriptor.create(
							FridgeTemperatureModel.class,
							FridgeTemperatureModel.MIL_URI,
							TimeUnit.SECONDS,
							null));
			atomicModelDescriptors.put(
					ExternalTemperatureModel.MIL_URI,
					AtomicHIOA_Descriptor.create(
							ExternalTemperatureModel.class,
							ExternalTemperatureModel.MIL_URI,
							TimeUnit.SECONDS,
							null));
			atomicModelDescriptors.put(
					FridgeUnitTestModel.MIL_URI,
					AtomicModelDescriptor.create(
							FridgeUnitTestModel.class,
							FridgeUnitTestModel.MIL_URI,
							TimeUnit.SECONDS,
							null));

			//Add smart lighting
			atomicModelDescriptors.put(
                    SmartLightingElectricityModel.URI,
                    AtomicHIOA_Descriptor.create(
                            SmartLightingElectricityModel.class,
                            SmartLightingElectricityModel.URI,
                            TimeUnit.SECONDS,
                            null
                            ));
            atomicModelDescriptors.put(
                    SmartLightingIlluminanceModel.URI,
                    AtomicHIOA_Descriptor.create(
                            SmartLightingIlluminanceModel.class,
                            SmartLightingIlluminanceModel.URI,
                            TimeUnit.SECONDS,
                            null
                            ));
            atomicModelDescriptors.put(
                    ExternalIlluminanceModel.URI,
                    AtomicHIOA_Descriptor.create(
                            ExternalIlluminanceModel.class,
                            ExternalIlluminanceModel.URI,
                            TimeUnit.SECONDS,
                            null
                            ));
            atomicModelDescriptors.put(
                    SmartLightingUnitTesterModel.URI,
                    AtomicModelDescriptor.create(
                            SmartLightingUnitTesterModel.class,
                            SmartLightingUnitTesterModel.URI,
                            TimeUnit.SECONDS,
                            null
                            ));
			
            // Add iron
			atomicModelDescriptors.put(
					IronElectricityModel.MIL_URI,
					AtomicHIOA_Descriptor.create(
							IronElectricityModel.class,
							IronElectricityModel.MIL_URI,
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
                    BatteryElectricityModel.MIL_URI,
                    AtomicHIOA_Descriptor.create(
                    		BatteryElectricityModel.class,
                    		BatteryElectricityModel.MIL_URI,
                            TimeUnit.SECONDS,
                            null
                    )
            );

            atomicModelDescriptors.put(
                    BatteryChargeLevelModel.MIL_URI,
                    AtomicHIOA_Descriptor.create(
                    		BatteryChargeLevelModel.class,
                    		BatteryChargeLevelModel.MIL_URI,
                            TimeUnit.SECONDS,
                            null
                    )
            );

            atomicModelDescriptors.put(
                    BatteryUserModel.MIL_URI,
                    AtomicModelDescriptor.create(
                            BatteryUserModel.class,
                            BatteryUserModel.MIL_URI,
                            TimeUnit.SECONDS,
                            null
                    )
            );
            
            // Add Wind turbine
            atomicModelDescriptors.put(
                    WindTurbineElectricityModel.MIL_URI,
                    AtomicHIOA_Descriptor.create(
                            WindTurbineElectricityModel.class,
                            WindTurbineElectricityModel.MIL_URI,
                            TimeUnit.SECONDS,
                            null
                    )
            );

            atomicModelDescriptors.put(
                    ExternalWindModel.MIL_URI,
                    AtomicHIOA_Descriptor.create(
                            ExternalWindModel.class,
                            ExternalWindModel.MIL_URI,
                            TimeUnit.SECONDS,
                            null
                    )
            );

            atomicModelDescriptors.put(
                    WindTurbineUserModel.MIL_URI,
                    AtomicModelDescriptor.create(
                            WindTurbineUserModel.class,
                            WindTurbineUserModel.MIL_URI,
                            TimeUnit.SECONDS,
                            null
                    )
            );
            
            // Add Generator
            atomicModelDescriptors.put(
	                GeneratorElectricityModel.URI,
	                AtomicHIOA_Descriptor.create(
	                        GeneratorElectricityModel.class,
	                        GeneratorElectricityModel.URI,
	                        TimeUnit.SECONDS,
	                        null
	                )
	        );

	        atomicModelDescriptors.put(
	                GeneratorFuelModel.URI,
	                AtomicHIOA_Descriptor.create(
	                        GeneratorFuelModel.class,
	                        GeneratorFuelModel.URI,
	                        TimeUnit.SECONDS,
	                        null
	                )
	        );

	        atomicModelDescriptors.put(
	                GeneratorUserModel.URI,
	                AtomicModelDescriptor.create(
	                        GeneratorUserModel.class,
	                        GeneratorUserModel.URI,
	                        TimeUnit.SECONDS,
	                        null
	                )
	        );
            
            // Add Metter
            atomicModelDescriptors.put(
                    ElectricMeterElectricityModel.URI,
                    AtomicHIOA_Descriptor.create(
                            ElectricMeterElectricityModel.class,
                            ElectricMeterElectricityModel.URI,
                            TimeUnit.SECONDS,
                            null
                    )
            );

            // Submodels
            Set<String> submodels = new HashSet<>();
            submodels.add(FridgeElectricityModel.MIL_URI);
			submodels.add(FridgeTemperatureModel.MIL_URI);
			submodels.add(ExternalTemperatureModel.MIL_URI);
			submodels.add(FridgeUnitTestModel.MIL_URI);
			submodels.add(FridgeStateModel.MIL_URI);
            
            submodels.add(SmartLightingElectricityModel.URI);
            submodels.add(SmartLightingIlluminanceModel.URI);
            submodels.add(ExternalIlluminanceModel.URI);
            submodels.add(SmartLightingUnitTesterModel.URI);
            
            submodels.add(IronElectricityModel.MIL_URI);
			submodels.add(IronUserModel.URI);
			
			submodels.add(BatteryElectricityModel.MIL_URI);
			submodels.add(BatteryChargeLevelModel.MIL_URI);
			submodels.add(BatteryUserModel.MIL_URI);
			
			submodels.add(WindTurbineElectricityModel.MIL_URI);
			submodels.add(WindTurbineUserModel.MIL_URI);
			submodels.add(ExternalWindModel.MIL_URI);
			
			submodels.add(GeneratorElectricityModel.URI);
	        submodels.add(GeneratorFuelModel.URI);
	        submodels.add(GeneratorUserModel.URI);
	        
			submodels.add(ElectricMeterElectricityModel.URI);

            Map<EventSource, EventSink[]> connections = new HashMap<>();

            // Add the fridge events
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
			
			// Add smart lighting events
			connections.put(
                    new EventSource(
                            SmartLightingUnitTesterModel.URI,
                            TurnOnSmartLighting.class
                    ),
                    new EventSink[]{
                            new EventSink(
                                    SmartLightingElectricityModel.URI,
                                    TurnOnSmartLighting.class
                            )
                    });
            connections.put(
                    new EventSource(
                            SmartLightingUnitTesterModel.URI,
                            SetPowerSmartLighting.class
                    ),
                    new EventSink[]{
                            new EventSink(
                                    SmartLightingElectricityModel.URI,
                                    SetPowerSmartLighting.class
                            )
                    });
            connections.put(
                    new EventSource(
                            SmartLightingUnitTesterModel.URI,
                            TurnOffSmartLighting.class
                    ),
                    new EventSink[]{
                            new EventSink(
                                    SmartLightingElectricityModel.URI,
                                    TurnOffSmartLighting.class
                            ),
                    });
            connections.put(
                    new EventSource(
                            SmartLightingUnitTesterModel.URI,
                            IncreaseLighting.class
                    ),
                    new EventSink[]{
                            new EventSink(
                                    SmartLightingElectricityModel.URI,
                                    IncreaseLighting.class
                            ),
                            new EventSink(
                                    SmartLightingIlluminanceModel.URI,
                                    IncreaseLighting.class
                            )
                    });
            connections.put(
                    new EventSource(
                            SmartLightingUnitTesterModel.URI,
                            DecreaseLighting.class
                    ),
                    new EventSink[]{
                            new EventSink(
                                    SmartLightingElectricityModel.URI,
                                    DecreaseLighting.class
                            ),
                            new EventSink(
                                    SmartLightingIlluminanceModel.URI,
                                    DecreaseLighting.class
                            )
                    });
            connections.put(
                    new EventSource(
                            SmartLightingUnitTesterModel.URI,
                            StopAdjustingLighting.class
                    ),
                    new EventSink[]{
                            new EventSink(
                                    SmartLightingElectricityModel.URI,
                                    StopAdjustingLighting.class
                            ),
                            new EventSink(
                                    SmartLightingIlluminanceModel.URI,
                                    StopAdjustingLighting.class
                            )
                    });
            
			// Add iron
			connections.put(
					new EventSource(IronUserModel.URI, TurnOnIron.class),
					new EventSink[] {
							new EventSink(IronElectricityModel.MIL_URI,
										  TurnOnIron.class)
					});
			connections.put(
					new EventSource(IronUserModel.URI, TurnOffIron.class),
					new EventSink[] {
							new EventSink(IronElectricityModel.MIL_URI,
									TurnOffIron.class)
					});
			connections.put(
					new EventSource(IronUserModel.URI, EnableDelicateModeIron.class),
					new EventSink[] {
							new EventSink(IronElectricityModel.MIL_URI,
									EnableDelicateModeIron.class)
					});
			connections.put(
					new EventSource(IronUserModel.URI, EnableCottonModeIron.class),
					new EventSink[] {
							new EventSink(IronElectricityModel.MIL_URI,
										EnableCottonModeIron.class)
					});
			connections.put(
					new EventSource(IronUserModel.URI, EnableLinenModeIron.class),
					new EventSink[] {
							new EventSink(IronElectricityModel.MIL_URI,
									EnableLinenModeIron.class)
					});
			connections.put(
					new EventSource(IronUserModel.URI, EnableEnergySavingModeIron.class),
					new EventSink[] {
							new EventSink(IronElectricityModel.MIL_URI,
									EnableEnergySavingModeIron.class)
					});
			connections.put(
					new EventSource(IronUserModel.URI, EnableSteamModeIron.class),
					new EventSink[] {
							new EventSink(IronElectricityModel.MIL_URI,
									EnableSteamModeIron.class)
					});
			connections.put(
					new EventSource(IronUserModel.URI, DisableEnergySavingModeIron.class),
					new EventSink[] {
							new EventSink(IronElectricityModel.MIL_URI,
									DisableEnergySavingModeIron.class)
					});
			connections.put(
					new EventSource(IronUserModel.URI, DisableSteamModeIron.class),
					new EventSink[] {
							new EventSink(IronElectricityModel.MIL_URI,
									DisableSteamModeIron.class)
					});
			
			// Add battery
			connections.put(
                    new EventSource(BatteryUserModel.MIL_URI, SetProductBatteryEvent.class),
                    new EventSink[] {
                            new EventSink(BatteryElectricityModel.MIL_URI, SetProductBatteryEvent.class),
                            new EventSink(BatteryChargeLevelModel.MIL_URI, SetProductBatteryEvent.class)
                    }
            );

            connections.put(
                    new EventSource(BatteryUserModel.MIL_URI, SetConsumeBatteryEvent.class),
                    new EventSink[] {
                            new EventSink(BatteryElectricityModel.MIL_URI, SetConsumeBatteryEvent.class),
                            new EventSink(BatteryChargeLevelModel.MIL_URI, SetConsumeBatteryEvent.class)
                    }
            );
            
            // Add wind turbine events
            connections.put(
                    new EventSource(WindTurbineUserModel.MIL_URI, StartWindTurbineEvent.class),
                    new EventSink[] {
                            new EventSink(WindTurbineElectricityModel.MIL_URI, StartWindTurbineEvent.class)
                    }
            );

            connections.put(
                    new EventSource(WindTurbineUserModel.MIL_URI, StopWindTurbineEvent.class),
                    new EventSink[] {
                            new EventSink(WindTurbineElectricityModel.MIL_URI, StopWindTurbineEvent.class)
                    }
            );
 
            
            // Add generator
            connections.put(
	                new EventSource(GeneratorUserModel.URI, ActivateGeneratorEvent.class),
	                new EventSink[] {
	                        new EventSink(GeneratorElectricityModel.URI, ActivateGeneratorEvent.class),
	                        new EventSink(GeneratorFuelModel.URI, ActivateGeneratorEvent.class)
	                }
	        );

	        connections.put(
	                new EventSource(GeneratorUserModel.URI, StopGeneratorEvent.class),
	                new EventSink[] {
	                        new EventSink(GeneratorElectricityModel.URI, StopGeneratorEvent.class),
	                        new EventSink(GeneratorFuelModel.URI, StopGeneratorEvent.class),
	                }
	        );

	        connections.put(
	                new EventSource(GeneratorFuelModel.URI, StopGeneratorEvent.class),
	                new EventSink[] {
	                        new EventSink(GeneratorElectricityModel.URI, StopGeneratorEvent.class),
	                }
	        );
            
            Map<VariableSource, VariableSink[]> bindings = new HashMap<>();
            
            // Add iron bindings
           
            bindings.put(new VariableSource("currentIntensity",
					Double.class,
					IronElectricityModel.MIL_URI),
			 new VariableSink[] {
					 new VariableSink("currentIronConsumption",
							 		  Double.class,
							 		  ElectricMeterElectricityModel.URI)
			 }); 
            
            // Add fridge bindings
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
			
			bindings.put(new VariableSource("currentIntensity",
							Double.class,
							FridgeElectricityModel.MIL_URI),
			new VariableSink[] {
					new VariableSink("currentFridgeConsumption",
							 		  Double.class,
							 		  ElectricMeterElectricityModel.URI)
			}); 
			
			// Add smart lighting bindings
			bindings.put(
                    new VariableSource(
                            "externalIlluminance",
                            Double.class,
                            ExternalIlluminanceModel.URI
                    ),
                    new VariableSink[]{
                            new VariableSink(
                                    "externalIlluminance",
                                    Double.class,
                                    SmartLightingIlluminanceModel.URI
                            )
                    });
            bindings.put(
                    new VariableSource(
                            "currentPowerLevel",
                            Double.class,
                            SmartLightingElectricityModel.URI
                    ),
                    new VariableSink[]{
                            new VariableSink(
                                    "currentPowerLevel",
                                    Double.class,
                                    SmartLightingIlluminanceModel.URI
                            )
                    });
            bindings.put(
                    new VariableSource(
                            "currentIntensity",
                            Double.class,
                            SmartLightingElectricityModel.URI
                    ),
                    new VariableSink[]{
                            new VariableSink(
                                    "currentSmartLightingConsumption",
                                    Double.class,
                                    ElectricMeterElectricityModel.URI
                            )
                    });
			
			// Add battery bindings
			bindings.put(
                    new VariableSource("currentChargeLevel", Double.class, BatteryChargeLevelModel.MIL_URI),
                    new VariableSink[] {
                            new VariableSink("currentChargeLevel", Double.class, BatteryElectricityModel.MIL_URI)
                    }
            );
			
			bindings.put(
                    new VariableSource("currentProduction", Double.class, BatteryElectricityModel.MIL_URI),
                    new VariableSink[] {
                            new VariableSink("currentBatteryProduction", Double.class, ElectricMeterElectricityModel.URI)
                    }
            );
			
			bindings.put(
                    new VariableSource("currentConsumption", Double.class, BatteryElectricityModel.MIL_URI),
                    new VariableSink[] {
                            new VariableSink("currentBatteryConsumption", Double.class, ElectricMeterElectricityModel.URI)
                    }
            ); 

			// Add wind turbine bindings
			bindings.put(
                    new VariableSource("externalWindSpeed",
                            Double.class,
                            ExternalWindModel.MIL_URI),
                    new VariableSink[] {
                            new VariableSink("externalWindSpeed",
                                    Double.class,
                                    WindTurbineElectricityModel.MIL_URI)
                    });
			bindings.put(
                    new VariableSource("currentProduction",
                            Double.class,
                            WindTurbineElectricityModel.MIL_URI),
                    new VariableSink[] {
                            new VariableSink("currentWindTurbineProduction",
                                    Double.class,
                                    ElectricMeterElectricityModel.URI)
                    });
			
			// Add generator bindings
			bindings.put(
	                new VariableSource("currentFuelLevel", Double.class, GeneratorFuelModel.URI),
	                new VariableSink[] {
	                        new VariableSink("currentFuelLevel", Double.class, GeneratorElectricityModel.URI)
	                }
	        );
			bindings.put(
	                new VariableSource("currentProduction", Double.class, GeneratorElectricityModel.URI),
	                new VariableSink[] {
	                        new VariableSink("currentGeneratorProduction", Double.class, ElectricMeterElectricityModel.URI)
	                }
	        );
			
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
