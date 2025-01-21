package fr.sorbonne_u.components.equipments.battery.mil;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

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
import fr.sorbonne_u.components.equipments.battery.mil.events.*;

public class LocalSimulationArchitectures {
	public static Architecture createBatteryMILLocalArchitecture4UnitTest(String architectureURI, TimeUnit simulatedTimeUnit) throws Exception {
		Map<String,AbstractAtomicModelDescriptor> atomicModelDescriptors =
															new HashMap<>();

		// atom model descriptors
		atomicModelDescriptors.put(
                BatteryElectricityModel.MIL_URI,
                AtomicHIOA_Descriptor.create(
                		BatteryElectricityModel.class,
                		BatteryElectricityModel.MIL_URI,
                		simulatedTimeUnit,
                        null
                )
        );
        atomicModelDescriptors.put(
                BatteryChargeLevelModel.MIL_URI,
                AtomicHIOA_Descriptor.create(
                		BatteryChargeLevelModel.class,
                		BatteryChargeLevelModel.MIL_URI,
                		simulatedTimeUnit,
                        null
                )
        );
        atomicModelDescriptors.put(
                BatteryStateModel.MIL_URI,
                AtomicModelDescriptor.create(
                		BatteryStateModel.class,
                		BatteryStateModel.MIL_URI,
                		simulatedTimeUnit,
                        null
                )
        );
        
        // Submodels
		Map<String,CoupledModelDescriptor> coupledModelDescriptors =new HashMap<>();

		Set<String> submodels = new HashSet<String>();
		submodels.add(BatteryElectricityModel.MIL_URI);
		submodels.add(BatteryChargeLevelModel.MIL_URI);
        submodels.add(BatteryStateModel.MIL_URI);

        // Imported
		Map<Class<? extends EventI>, EventSink[]> imported = new HashMap<>();

		imported.put(
				SetProductBatteryEvent.class,
				new EventSink[] {
					new EventSink(BatteryStateModel.MIL_URI,
								  SetProductBatteryEvent.class)
				});
		imported.put(
				SetConsumeBatteryEvent.class,
				new EventSink[] {
					new EventSink(BatteryStateModel.MIL_URI,
							      SetConsumeBatteryEvent.class)
				});
		imported.put(
				SetConsumeBatteryEvent.class,
				new EventSink[] {
					new EventSink(BatteryStateModel.MIL_URI,
								  SetConsumeBatteryEvent.class)
				});

		
		// Connections
		Map<EventSource,EventSink[]> connections = new HashMap<EventSource,EventSink[]>();

		connections.put(
                new EventSource(BatteryStateModel.MIL_URI, SetProductBatteryEvent.class),
                new EventSink[] {
                        new EventSink(BatteryElectricityModel.MIL_URI, SetProductBatteryEvent.class),
                        new EventSink(BatteryChargeLevelModel.MIL_URI, SetProductBatteryEvent.class)
                }
        );

        connections.put(
                new EventSource(BatteryStateModel.MIL_URI, SetConsumeBatteryEvent.class),
                new EventSink[] {
                        new EventSink(BatteryElectricityModel.MIL_URI, SetConsumeBatteryEvent.class),
                        new EventSink(BatteryChargeLevelModel.MIL_URI, SetConsumeBatteryEvent.class)
                }
        );
        
        connections.put(
                new EventSource(BatteryStateModel.MIL_URI, SetStandByBatteryEvent.class),
                new EventSink[] {
                        new EventSink(BatteryElectricityModel.MIL_URI, SetStandByBatteryEvent.class),
                        new EventSink(BatteryChargeLevelModel.MIL_URI, SetStandByBatteryEvent.class)
                }
        );

		// Variable bindings
		Map<VariableSource,VariableSink[]> bindings = new HashMap<VariableSource,VariableSink[]>();

		bindings.put(
                new VariableSource("currentChargeLevel", Double.class, BatteryChargeLevelModel.MIL_URI),
                new VariableSink[] {
                        new VariableSink("currentChargeLevel", Double.class, BatteryElectricityModel.MIL_URI)
                }
        );

		// coupled model descriptor
		coupledModelDescriptors.put(
        		BatteryCoupledModel.MIL_URI,
                new CoupledHIOA_Descriptor(
                        BatteryCoupledModel.class,
                        BatteryCoupledModel.MIL_URI,
                        submodels,
                        null,
                        null,
                        connections,
                        null,
                        null,
                        null,
                        bindings
                )
        );

		// simulation architecture
		Architecture architecture =
				new Architecture(
						architectureURI,
						BatteryCoupledModel.MIL_URI,
						atomicModelDescriptors,
						coupledModelDescriptors,
						simulatedTimeUnit);

		return architecture;
	}

	public static Architecture createBattery_RT_LocalArchitecture4UnitTest(
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

		// URI
		String batteryStateModelURI = null;
		String batteryChargeLevelModelURI = null;
		String batteryElectricityModelURI = null;
		String batteryCoupledModelURI = null;
		
		switch (currentSimulationType) {
			case MIL_RT_SIMULATION:
				batteryStateModelURI = BatteryStateModel.MIL_RT_URI;
				batteryChargeLevelModelURI = BatteryChargeLevelModel.MIL_RT_URI;
				batteryElectricityModelURI = BatteryElectricityModel.MIL_RT_URI;
				batteryCoupledModelURI = BatteryCoupledModel.MIL_RT_URI;
				break;
				
			case SIL_SIMULATION:
				batteryStateModelURI = BatteryStateModel.SIL_URI;
				batteryChargeLevelModelURI = BatteryChargeLevelModel.SIL_URI;
				batteryElectricityModelURI = BatteryElectricityModel.SIL_URI;
				batteryCoupledModelURI = BatteryCoupledModel.SIL_URI;
				break;
			
			default:
		}
		
		// Atomic models
		Map<String,AbstractAtomicModelDescriptor> atomicModelDescriptors = new HashMap<>();

		atomicModelDescriptors.put(
				batteryStateModelURI,
				RTAtomicModelDescriptor.create(
						BatteryStateModel.class,
						batteryStateModelURI,
						simulatedTimeUnit,
						null,
						accelerationFactor));
		atomicModelDescriptors.put(
				batteryChargeLevelModelURI,
				RTAtomicHIOA_Descriptor.create(
						BatteryChargeLevelModel.class,
						batteryChargeLevelModelURI,
						simulatedTimeUnit,
						null,
						accelerationFactor));
		atomicModelDescriptors.put(
				batteryElectricityModelURI,
				RTAtomicHIOA_Descriptor.create(
						BatteryElectricityModel.class,
						batteryElectricityModelURI,
						simulatedTimeUnit,
						null,
						accelerationFactor));

		// Submodels
		Map<String,CoupledModelDescriptor> coupledModelDescriptors = new HashMap<>();

		Set<String> submodels = new HashSet<String>();
		submodels.add(batteryStateModelURI);
		submodels.add(batteryChargeLevelModelURI);
		submodels.add(batteryElectricityModelURI);

		// Imported
		Map<Class<? extends EventI>, EventSink[]> imported = new HashMap<>();

		imported.put(
				SetProductBatteryEvent.class,
				new EventSink[] {
					new EventSink(batteryStateModelURI,
							      SetProductBatteryEvent.class)
				});
		imported.put(
				SetConsumeBatteryEvent.class,
				new EventSink[] {
					new EventSink(batteryStateModelURI,
							      SetConsumeBatteryEvent.class)
				});
		imported.put(
				SetStandByBatteryEvent.class,
				new EventSink[] {
					new EventSink(batteryStateModelURI,
							      SetStandByBatteryEvent.class)
				});

		// Connections
		Map<EventSource,EventSink[]> connections = new HashMap<EventSource,EventSink[]>();

		connections.put(
                new EventSource(batteryStateModelURI, SetProductBatteryEvent.class),
                new EventSink[] {
                        new EventSink(batteryElectricityModelURI, SetProductBatteryEvent.class),
                        new EventSink(batteryChargeLevelModelURI, SetProductBatteryEvent.class)
                }
        );

        connections.put(
                new EventSource(batteryStateModelURI, SetConsumeBatteryEvent.class),
                new EventSink[] {
                        new EventSink(batteryElectricityModelURI, SetConsumeBatteryEvent.class),
                        new EventSink(batteryChargeLevelModelURI, SetConsumeBatteryEvent.class)
                }
        );
        
        connections.put(
                new EventSource(batteryStateModelURI, SetStandByBatteryEvent.class),
                new EventSink[] {
                        new EventSink(batteryElectricityModelURI, SetStandByBatteryEvent.class),
                        new EventSink(batteryChargeLevelModelURI, SetStandByBatteryEvent.class)
                }
        );

		// Bindings
		Map<VariableSource,VariableSink[]> bindings = new HashMap<VariableSource,VariableSink[]>();

		bindings.put(
                new VariableSource("currentChargeLevel", Double.class, batteryChargeLevelModelURI),
                new VariableSink[] {
                        new VariableSink("currentChargeLevel", Double.class, batteryElectricityModelURI)
                }
        );

		// coupled model descriptor
		coupledModelDescriptors.put(
				batteryCoupledModelURI,
				new RTCoupledHIOA_Descriptor(
						BatteryCoupledModel.class,
						batteryCoupledModelURI,
						submodels,
						imported,
						null,
						connections,
						null,
						null,
						null,
						bindings,
						accelerationFactor));

		// simulation architecture
		Architecture architecture =
				new RTArchitecture(
						architectureURI,
						batteryCoupledModelURI,
						atomicModelDescriptors,
						coupledModelDescriptors,
						simulatedTimeUnit,
						accelerationFactor);

		return architecture;
	}

	public static Architecture	createBatteryUserMILLocalArchitecture(
		String architectureURI,
		TimeUnit simulatedTimeUnit
		) throws Exception
	{
		Map<String,AbstractAtomicModelDescriptor> atomicModelDescriptors = new HashMap<>();

		atomicModelDescriptors.put(
				BatteryUserModel.MIL_URI,
				AtomicModelDescriptor.create(
						BatteryUserModel.class,
						BatteryUserModel.MIL_URI,
						simulatedTimeUnit,
						null));

		Map<String,CoupledModelDescriptor> coupledModelDescriptors = new HashMap<>();

		Architecture architecture =
				new Architecture(
						architectureURI,
						BatteryUserModel.MIL_URI,
						atomicModelDescriptors,
						coupledModelDescriptors,
						simulatedTimeUnit);

		return architecture;
	}

	public static Architecture	createBatteryUserMILRT_LocalArchitecture(
		String architectureURI,
		TimeUnit simulatedTimeUnit,
		double accelerationFactor
		) throws Exception
	{
		Map<String,AbstractAtomicModelDescriptor> atomicModelDescriptors = new HashMap<>();

		atomicModelDescriptors.put(
				BatteryUserModel.MIL_RT_URI,
				RTAtomicModelDescriptor.create(
						BatteryUserModel.class,
						BatteryUserModel.MIL_RT_URI,
						simulatedTimeUnit,
						null,
						accelerationFactor));

		Map<String,CoupledModelDescriptor> coupledModelDescriptors = new HashMap<>();


		Architecture architecture =
				new RTArchitecture(
						architectureURI,
						BatteryUserModel.MIL_RT_URI,
						atomicModelDescriptors,
						coupledModelDescriptors,
						simulatedTimeUnit,
						accelerationFactor);

		return architecture;
	}

	
	public static Architecture	createBatteryMILLocalArchitecture4IntegrationTest(
		String architectureURI,
		TimeUnit simulatedTimeUnit
		) throws Exception
	{

		Map<String,AbstractAtomicModelDescriptor> atomicModelDescriptors = new HashMap<>();

		atomicModelDescriptors.put(
				BatteryStateModel.MIL_URI,
				AtomicModelDescriptor.create(
						BatteryStateModel.class,
						BatteryStateModel.MIL_URI,
						simulatedTimeUnit,
						null));
		atomicModelDescriptors.put(
				BatteryElectricityModel.MIL_URI,
				AtomicHIOA_Descriptor.create(
						BatteryElectricityModel.class,
						BatteryElectricityModel.MIL_URI,
						simulatedTimeUnit,
						null));
		atomicModelDescriptors.put(
				BatteryChargeLevelModel.MIL_URI,
				AtomicHIOA_Descriptor.create(
						BatteryChargeLevelModel.class,
						BatteryChargeLevelModel.MIL_URI,
						simulatedTimeUnit,
						null));

		Map<String,CoupledModelDescriptor> coupledModelDescriptors = new HashMap<>();

		Set<String> submodels = new HashSet<String>();
		submodels.add(BatteryStateModel.MIL_URI);
		submodels.add(BatteryElectricityModel.MIL_URI);
		submodels.add(BatteryChargeLevelModel.MIL_URI);

		// events received by the coupled model transmitted to its submodels
		Map<Class<? extends EventI>, EventSink[]> imported = new HashMap<>();

		imported.put(
				SetProductBatteryEvent.class,
				new EventSink[] {
					new EventSink(BatteryStateModel.MIL_URI,
								  SetProductBatteryEvent.class)
				});
		imported.put(
				SetConsumeBatteryEvent.class,
				new EventSink[] {
					new EventSink(BatteryStateModel.MIL_URI,
							      SetConsumeBatteryEvent.class)
				});
		imported.put(
				SetStandByBatteryEvent.class,
				new EventSink[] {
					new EventSink(BatteryStateModel.MIL_URI,
								  SetStandByBatteryEvent.class)
				});

		Map<Class<? extends EventI>,ReexportedEvent> reexported =
				new HashMap<Class<? extends EventI>,ReexportedEvent>();

		reexported.put(
				SetProductBatteryEvent.class,
				new ReexportedEvent(BatteryStateModel.MIL_URI,
									SetProductBatteryEvent.class));
		reexported.put(
				SetStandByBatteryEvent.class,
				new ReexportedEvent(BatteryStateModel.MIL_URI,
									SetStandByBatteryEvent.class));
		reexported.put(
				SetConsumeBatteryEvent.class,
				new ReexportedEvent(BatteryStateModel.MIL_URI,
									SetConsumeBatteryEvent.class));


		Map<EventSource,EventSink[]> connections = new HashMap<EventSource,EventSink[]>();

		connections.put(
                new EventSource(BatteryStateModel.MIL_URI, SetProductBatteryEvent.class),
                new EventSink[] {
                        new EventSink(BatteryElectricityModel.MIL_URI, SetProductBatteryEvent.class),
                        new EventSink(BatteryChargeLevelModel.MIL_URI, SetProductBatteryEvent.class)
                }
        );

        connections.put(
                new EventSource(BatteryStateModel.MIL_URI, SetConsumeBatteryEvent.class),
                new EventSink[] {
                        new EventSink(BatteryElectricityModel.MIL_URI, SetConsumeBatteryEvent.class),
                        new EventSink(BatteryChargeLevelModel.MIL_URI, SetConsumeBatteryEvent.class)
                }
        );
        
        connections.put(
                new EventSource(BatteryStateModel.MIL_URI, SetStandByBatteryEvent.class),
                new EventSink[] {
                        new EventSink(BatteryElectricityModel.MIL_URI, SetStandByBatteryEvent.class),
                        new EventSink(BatteryChargeLevelModel.MIL_URI, SetStandByBatteryEvent.class)
                }
        );

		Map<VariableSource,VariableSink[]> bindings =
							new HashMap<VariableSource,VariableSink[]>();

							 bindings.put(
					                    new VariableSource("currentChargeLevel", Double.class, BatteryChargeLevelModel.MIL_URI),
					                    new VariableSink[] {
					                            new VariableSink("currentChargeLevel", Double.class, BatteryElectricityModel.MIL_URI)
					                    }
					            );

		// coupled model descriptor
		coupledModelDescriptors.put(
				BatteryCoupledModel.MIL_URI,
				new CoupledHIOA_Descriptor(
						BatteryCoupledModel.class,
						BatteryCoupledModel.MIL_URI,
						submodels,
						imported,
						reexported,
						connections,
						null,
						null,
						null,
						bindings));

		// simulation architecture
		Architecture architecture =
				new Architecture(
						architectureURI,
						BatteryCoupledModel.MIL_URI,
						atomicModelDescriptors,
						coupledModelDescriptors,
						simulatedTimeUnit);

		return architecture;
	}

	public static Architecture	createBattery_RT_LocalArchitecture4IntegrationTest(
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

		String batteryStateModelURI = null;
		String batteryChargeLevelModelURI = null;
		String batteryElectricityModelURI = null;
		String batteryCoupledModelURI = null;
		
		switch (currentSimulationType) {
			case MIL_RT_SIMULATION:
				batteryStateModelURI = BatteryStateModel.MIL_RT_URI;
				batteryChargeLevelModelURI = BatteryChargeLevelModel.MIL_RT_URI;
				batteryElectricityModelURI = BatteryElectricityModel.MIL_RT_URI;
				batteryCoupledModelURI = BatteryCoupledModel.MIL_RT_URI;
				break;
				
			case SIL_SIMULATION:
				batteryStateModelURI = BatteryStateModel.SIL_URI;
				batteryChargeLevelModelURI = BatteryChargeLevelModel.SIL_URI;
				batteryElectricityModelURI = BatteryElectricityModel.SIL_URI;
				batteryCoupledModelURI = BatteryCoupledModel.SIL_URI;
				break;
			
			default:
		}

		Map<String,AbstractAtomicModelDescriptor> atomicModelDescriptors = new HashMap<>();

		atomicModelDescriptors.put(
				batteryStateModelURI,
				RTAtomicModelDescriptor.create(
						BatteryStateModel.class,
						batteryStateModelURI,
						simulatedTimeUnit,
						null,
						accelerationFactor));

		atomicModelDescriptors.put(
				batteryChargeLevelModelURI,
				RTAtomicHIOA_Descriptor.create(
						BatteryChargeLevelModel.class,
						batteryChargeLevelModelURI,
						simulatedTimeUnit,
						null,
						accelerationFactor));
		atomicModelDescriptors.put(
				batteryElectricityModelURI,
				RTAtomicHIOA_Descriptor.create(
						BatteryElectricityModel.class,
						batteryElectricityModelURI,
						simulatedTimeUnit,
						null,
						accelerationFactor));

		Map<String,CoupledModelDescriptor> coupledModelDescriptors = new HashMap<>();

		Set<String> submodels = new HashSet<String>();
		submodels.add(batteryStateModelURI);
		submodels.add(batteryChargeLevelModelURI);
		submodels.add(batteryElectricityModelURI);

		Map<Class<? extends EventI>, EventSink[]> imported = new HashMap<>();

		imported.put(
				SetProductBatteryEvent.class,
				new EventSink[] {
					new EventSink(batteryStateModelURI,
								  SetProductBatteryEvent.class)
				});
		imported.put(
				SetConsumeBatteryEvent.class,
				new EventSink[] {
					new EventSink(batteryStateModelURI,
							      SetConsumeBatteryEvent.class)
				});
		imported.put(
				SetStandByBatteryEvent.class,
				new EventSink[] {
					new EventSink(batteryStateModelURI,
								  SetStandByBatteryEvent.class)
				});

		// events emitted by submodels that are reexported towards other models
		Map<Class<? extends EventI>,ReexportedEvent> reexported =
				new HashMap<Class<? extends EventI>,ReexportedEvent>();

		reexported.put(
				SetProductBatteryEvent.class,
				new ReexportedEvent(batteryStateModelURI,
									SetProductBatteryEvent.class));
		reexported.put(
				SetStandByBatteryEvent.class,
				new ReexportedEvent(batteryStateModelURI,
									SetStandByBatteryEvent.class));
		reexported.put(
				SetConsumeBatteryEvent.class,
				new ReexportedEvent(batteryStateModelURI,
									SetConsumeBatteryEvent.class));

		Map<EventSource,EventSink[]> connections =
									new HashMap<EventSource,EventSink[]>();

		connections.put(
                new EventSource(batteryStateModelURI, SetProductBatteryEvent.class),
                new EventSink[] {
                        new EventSink(batteryChargeLevelModelURI, SetProductBatteryEvent.class),
                        new EventSink(batteryElectricityModelURI, SetProductBatteryEvent.class)
                }
        );

        connections.put(
                new EventSource(batteryStateModelURI, SetConsumeBatteryEvent.class),
                new EventSink[] {
                        new EventSink(batteryChargeLevelModelURI, SetConsumeBatteryEvent.class),
                        new EventSink(batteryElectricityModelURI, SetConsumeBatteryEvent.class)
                }
        );
        
        connections.put(
                new EventSource(batteryStateModelURI, SetStandByBatteryEvent.class),
                new EventSink[] {
                        new EventSink(batteryChargeLevelModelURI, SetStandByBatteryEvent.class),
                        new EventSink(batteryElectricityModelURI, SetStandByBatteryEvent.class)
                }
        );

		// variable bindings between exporting and importing models
		Map<VariableSource,VariableSink[]> bindings =
							new HashMap<VariableSource,VariableSink[]>();

		bindings.put(
                new VariableSource("currentChargeLevel", Double.class, batteryChargeLevelModelURI),
                new VariableSink[] {
                        new VariableSink("currentChargeLevel", Double.class, batteryElectricityModelURI)
                }
		);

		// coupled model descriptor
		coupledModelDescriptors.put(
				batteryCoupledModelURI,
				new RTCoupledHIOA_Descriptor(
						BatteryCoupledModel.class,
						batteryCoupledModelURI,
						submodels,
						imported,
						reexported,
						connections,
						null,
						null,
						null,
						bindings,
						accelerationFactor));

		// simulation architecture
		Architecture architecture =
				new RTArchitecture(
						architectureURI,
						batteryCoupledModelURI,
						atomicModelDescriptors,
						coupledModelDescriptors,
						simulatedTimeUnit,
						accelerationFactor);

		return architecture;
	}
}
