package fr.sorbonne_u.components.equipments.windTurbine.mil;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import fr.sorbonne_u.components.equipments.windTurbine.mil.events.StartWindTurbineEvent;
import fr.sorbonne_u.components.equipments.windTurbine.mil.events.StopWindTurbineEvent;
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

		public static Architecture createWindTurbineMILLocalArchitecture4UnitTest(String architectureURI, TimeUnit simulatedTimeUnit) throws Exception {
			Map<String,AbstractAtomicModelDescriptor> atomicModelDescriptors = new HashMap<>();

			// atom model descriptors
			atomicModelDescriptors.put(
                    WindTurbineElectricityModel.MIL_URI,
                    AtomicHIOA_Descriptor.create(
                            WindTurbineElectricityModel.class,
                            WindTurbineElectricityModel.MIL_URI,
                            simulatedTimeUnit,
                            null
                    )
            );
            atomicModelDescriptors.put(
                    ExternalWindModel.MIL_URI,
                    AtomicHIOA_Descriptor.create(
                            ExternalWindModel.class,
                            ExternalWindModel.MIL_URI,
                            simulatedTimeUnit,
                            null
                    )
            );
            atomicModelDescriptors.put(
                    WindTurbineStateModel.MIL_URI,
                    AtomicModelDescriptor.create(
                    		WindTurbineStateModel.class,
                    		WindTurbineStateModel.MIL_URI,
                    		simulatedTimeUnit,
                            null
                    )
            );
	        
	        // Submodels
			Map<String,CoupledModelDescriptor> coupledModelDescriptors =new HashMap<>();

			Set<String> submodels = new HashSet<String>();
			submodels.add(WindTurbineElectricityModel.MIL_URI);
	        submodels.add(WindTurbineUserModel.MIL_URI);
	        submodels.add(ExternalWindModel.MIL_URI);
	        submodels.add(WindTurbineStateModel.MIL_URI);

	        // Imported
			Map<Class<? extends EventI>, EventSink[]> imported = new HashMap<>();

			imported.put(
					StartWindTurbineEvent.class,
					new EventSink[] {
						new EventSink(WindTurbineStateModel.MIL_URI,
										StartWindTurbineEvent.class)
					});
			imported.put(
					StopWindTurbineEvent.class,
					new EventSink[] {
						new EventSink(WindTurbineStateModel.MIL_URI,
									StopWindTurbineEvent.class)
					});

			
			// Connections
			Map<EventSource,EventSink[]> connections = new HashMap<EventSource,EventSink[]>();

			connections.put(
                    new EventSource(WindTurbineStateModel.MIL_URI, StartWindTurbineEvent.class),
                    new EventSink[] {
                            new EventSink(WindTurbineElectricityModel.MIL_URI, StartWindTurbineEvent.class)
                    }
            );

            connections.put(
                    new EventSource(WindTurbineStateModel.MIL_URI, StopWindTurbineEvent.class),
                    new EventSink[] {
                            new EventSink(WindTurbineElectricityModel.MIL_URI, StopWindTurbineEvent.class)
                    }
            );
            
            Map<VariableSource, VariableSink[]> bindings = new HashMap<>();

            bindings.put(
                    new VariableSource("externalWindSpeed",
                            Double.class,
                            ExternalWindModel.MIL_URI),
                    new VariableSink[] {
                            new VariableSink("externalWindSpeed",
                                    Double.class,
                                    WindTurbineElectricityModel.MIL_URI)
                    });

			// coupled model descriptor
			coupledModelDescriptors.put(
	        		WindTurbineCoupledModel.MIL_URI,
	                new CoupledHIOA_Descriptor(
	                		WindTurbineCoupledModel.class,
	                		WindTurbineCoupledModel.MIL_URI,
	                        submodels,
	                        imported,
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
							WindTurbineCoupledModel.MIL_URI,
							atomicModelDescriptors,
							coupledModelDescriptors,
							simulatedTimeUnit);

			return architecture;
		}
		
		public static Architecture createWindTurbineMIL_RT_Architecture4UnitTest(
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
			String windTurbineStateModelURI = null;
			String externalWindModelURI = null;
			String windTurbineElectricityModelURI = null;
			String windTurbineCoupledModelURI = null;
			
			switch (currentSimulationType) {
				case MIL_RT_SIMULATION:
					windTurbineStateModelURI = WindTurbineStateModel.MIL_RT_URI;
					externalWindModelURI = ExternalWindModel.MIL_RT_URI;
					windTurbineElectricityModelURI = WindTurbineElectricityModel.MIL_RT_URI;
					windTurbineCoupledModelURI = WindTurbineCoupledModel.MIL_RT_URI;
					break;
					
				case SIL_SIMULATION:
					windTurbineStateModelURI = WindTurbineStateModel.SIL_URI;
					externalWindModelURI = ExternalWindModel.SIL_URI;
					windTurbineElectricityModelURI = WindTurbineElectricityModel.SIL_URI;
					windTurbineCoupledModelURI = WindTurbineCoupledModel.SIL_URI;
					break;
				
				default:
			}
			
			// Atomic models
			Map<String,AbstractAtomicModelDescriptor> atomicModelDescriptors = new HashMap<>();

			atomicModelDescriptors.put(
					windTurbineStateModelURI,
					RTAtomicModelDescriptor.create(
							WindTurbineStateModel.class,
							windTurbineStateModelURI,
							simulatedTimeUnit,
							null,
							accelerationFactor));
			atomicModelDescriptors.put(
					externalWindModelURI,
					RTAtomicHIOA_Descriptor.create(
							ExternalWindModel.class,
							externalWindModelURI,
							simulatedTimeUnit,
							null,
							accelerationFactor));
			atomicModelDescriptors.put(
					windTurbineElectricityModelURI,
					RTAtomicHIOA_Descriptor.create(
							WindTurbineElectricityModel.class,
							windTurbineElectricityModelURI,
							simulatedTimeUnit,
							null,
							accelerationFactor));

			// Submodels
			Map<String,CoupledModelDescriptor> coupledModelDescriptors = new HashMap<>();

			Set<String> submodels = new HashSet<String>();
			submodels.add(externalWindModelURI);
			submodels.add(windTurbineElectricityModelURI);
			submodels.add(windTurbineStateModelURI);

			// Imported
			Map<Class<? extends EventI>, EventSink[]> imported = new HashMap<>();

			imported.put(
					StartWindTurbineEvent.class,
					new EventSink[] {
						new EventSink(windTurbineStateModelURI,
										StartWindTurbineEvent.class)
					});
			imported.put(
					StopWindTurbineEvent.class,
					new EventSink[] {
						new EventSink(windTurbineStateModelURI,
									StopWindTurbineEvent.class)
					});

			// Connections
			Map<EventSource,EventSink[]> connections = new HashMap<EventSource,EventSink[]>();

			connections.put(
                    new EventSource(windTurbineStateModelURI, StartWindTurbineEvent.class),
                    new EventSink[] {
                            new EventSink(windTurbineElectricityModelURI, StartWindTurbineEvent.class)
                    }
            );

            connections.put(
                    new EventSource(windTurbineStateModelURI, StopWindTurbineEvent.class),
                    new EventSink[] {
                            new EventSink(windTurbineElectricityModelURI, StopWindTurbineEvent.class)
                    }
            );
            
            Map<VariableSource, VariableSink[]> bindings = new HashMap<>();

            bindings.put(
                    new VariableSource("externalWindSpeed",
                            Double.class,
                            externalWindModelURI),
                    new VariableSink[] {
                            new VariableSink("externalWindSpeed",
                                    Double.class,
                                    windTurbineElectricityModelURI)
                    });

			// coupled model descriptor
			coupledModelDescriptors.put(
					windTurbineCoupledModelURI,
					new RTCoupledHIOA_Descriptor(
							WindTurbineCoupledModel.class,
							windTurbineCoupledModelURI,
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
							windTurbineCoupledModelURI,
							atomicModelDescriptors,
							coupledModelDescriptors,
							simulatedTimeUnit,
							accelerationFactor);

			return architecture;
		}
		
		public static Architecture	createWindTurbineUserMILLocalArchitecture(
			String architectureURI,
			TimeUnit simulatedTimeUnit
			) throws Exception
		{
			Map<String,AbstractAtomicModelDescriptor> atomicModelDescriptors = new HashMap<>();

			atomicModelDescriptors.put(
					WindTurbineUserModel.MIL_URI,
					AtomicModelDescriptor.create(
							WindTurbineUserModel.class,
							WindTurbineUserModel.MIL_URI,
							simulatedTimeUnit,
							null));

			Map<String,CoupledModelDescriptor> coupledModelDescriptors = new HashMap<>();

			Architecture architecture =
					new Architecture(
							architectureURI,
							WindTurbineUserModel.MIL_URI,
							atomicModelDescriptors,
							coupledModelDescriptors,
							simulatedTimeUnit);

			return architecture;
		}
		
		public static Architecture	createWindTurbineUserMILRT_LocalArchitecture(
			String architectureURI,
			TimeUnit simulatedTimeUnit,
			double accelerationFactor
			) throws Exception
		{
			Map<String,AbstractAtomicModelDescriptor> atomicModelDescriptors = new HashMap<>();

			atomicModelDescriptors.put(
					WindTurbineUserModel.MIL_RT_URI,
					RTAtomicModelDescriptor.create(
							WindTurbineUserModel.class,
							WindTurbineUserModel.MIL_RT_URI,
							simulatedTimeUnit,
							null,
							accelerationFactor));

			Map<String,CoupledModelDescriptor> coupledModelDescriptors = new HashMap<>();


			Architecture architecture =
					new RTArchitecture(
							architectureURI,
							WindTurbineUserModel.MIL_RT_URI,
							atomicModelDescriptors,
							coupledModelDescriptors,
							simulatedTimeUnit,
							accelerationFactor);

			return architecture;
		}
		
		public static Architecture	createWindTurbineMILLocalArchitecture4IntegrationTest(
			String architectureURI,
			TimeUnit simulatedTimeUnit
			) throws Exception
		{

			Map<String,AbstractAtomicModelDescriptor> atomicModelDescriptors = new HashMap<>();

			atomicModelDescriptors.put(
					WindTurbineStateModel.MIL_URI,
					AtomicModelDescriptor.create(
							WindTurbineStateModel.class,
							WindTurbineStateModel.MIL_URI,
							simulatedTimeUnit,
							null));
			atomicModelDescriptors.put(
					ExternalWindModel.MIL_URI,
					AtomicHIOA_Descriptor.create(
							ExternalWindModel.class,
							ExternalWindModel.MIL_URI,
							simulatedTimeUnit,
							null));

			Map<String,CoupledModelDescriptor> coupledModelDescriptors = new HashMap<>();

			Set<String> submodels = new HashSet<String>();
			submodels.add(WindTurbineStateModel.MIL_URI);
			submodels.add(ExternalWindModel.MIL_URI);

			// events received by the coupled model transmitted to its submodels
			Map<Class<? extends EventI>, EventSink[]> imported = new HashMap<>();

			imported.put(
					StartWindTurbineEvent.class,
					new EventSink[] {
						new EventSink(WindTurbineStateModel.MIL_URI,
										StartWindTurbineEvent.class)
					});
			imported.put(
					StopWindTurbineEvent.class,
					new EventSink[] {
						new EventSink(WindTurbineStateModel.MIL_URI,
									StopWindTurbineEvent.class)
					});

			Map<Class<? extends EventI>,ReexportedEvent> reexported =
					new HashMap<Class<? extends EventI>,ReexportedEvent>();

			reexported.put(
					StartWindTurbineEvent.class,
					new ReexportedEvent(WindTurbineStateModel.MIL_URI,
							StartWindTurbineEvent.class));
			reexported.put(
					StopWindTurbineEvent.class,
					new ReexportedEvent(WindTurbineStateModel.MIL_URI,
							StopWindTurbineEvent.class));

			// coupled model descriptor
			coupledModelDescriptors.put(
					WindTurbineCoupledModel.MIL_URI,
					new CoupledHIOA_Descriptor(
							WindTurbineCoupledModel.class,
							WindTurbineCoupledModel.MIL_URI,
							submodels,
							imported,
							reexported,
							null,
							null,
							null,
							null,
							null));

			// simulation architecture
			Architecture architecture =
					new Architecture(
							architectureURI,
							WindTurbineCoupledModel.MIL_URI,
							atomicModelDescriptors,
							coupledModelDescriptors,
							simulatedTimeUnit);

			return architecture;
		}
		
		public static Architecture	createWindTurbine_RT_LocalArchitecture4IntegrationTest(
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

				String windTurbineStateModelURI = null;
				String externalWindModelURI = null;
				String windTurbineCoupledModelURI = null;
				
				switch (currentSimulationType) {
					case MIL_RT_SIMULATION:
						windTurbineStateModelURI = WindTurbineStateModel.MIL_RT_URI;
						externalWindModelURI = ExternalWindModel.MIL_RT_URI;
						windTurbineCoupledModelURI = WindTurbineCoupledModel.MIL_RT_URI;
						break;
						
					case SIL_SIMULATION:
						windTurbineStateModelURI = WindTurbineStateModel.SIL_URI;
						externalWindModelURI = ExternalWindModel.SIL_URI;
						windTurbineCoupledModelURI = WindTurbineCoupledModel.SIL_URI;
						break;
					
					default:
				}

				Map<String,AbstractAtomicModelDescriptor> atomicModelDescriptors = new HashMap<>();

				atomicModelDescriptors.put(
						windTurbineStateModelURI,
						RTAtomicModelDescriptor.create(
								WindTurbineStateModel.class,
								windTurbineStateModelURI,
								simulatedTimeUnit,
								null,
								accelerationFactor));

				atomicModelDescriptors.put(
						externalWindModelURI,
						RTAtomicHIOA_Descriptor.create(
								ExternalWindModel.class,
								externalWindModelURI,
								simulatedTimeUnit,
								null,
								accelerationFactor));

				Map<String,CoupledModelDescriptor> coupledModelDescriptors = new HashMap<>();

				Set<String> submodels = new HashSet<String>();
				submodels.add(windTurbineStateModelURI);
				submodels.add(externalWindModelURI);

				Map<Class<? extends EventI>, EventSink[]> imported = new HashMap<>();

				imported.put(
						StartWindTurbineEvent.class,
						new EventSink[] {
							new EventSink(windTurbineStateModelURI,
									StartWindTurbineEvent.class)
						});
				imported.put(
						StopWindTurbineEvent.class,
						new EventSink[] {
							new EventSink(windTurbineStateModelURI,
											StopWindTurbineEvent.class)
						});

				// events emitted by submodels that are reexported towards other models
				Map<Class<? extends EventI>,ReexportedEvent> reexported =
						new HashMap<Class<? extends EventI>,ReexportedEvent>();

				reexported.put(
						StartWindTurbineEvent.class,
						new ReexportedEvent(windTurbineStateModelURI,
											StartWindTurbineEvent.class));
				reexported.put(
						StopWindTurbineEvent.class,
						new ReexportedEvent(windTurbineStateModelURI,
											StopWindTurbineEvent.class));


				// coupled model descriptor
				coupledModelDescriptors.put(
						windTurbineCoupledModelURI,
						new RTCoupledHIOA_Descriptor(
								WindTurbineCoupledModel.class,
								windTurbineCoupledModelURI,
								submodels,
								imported,
								reexported,
								null,
								null,
								null,
								null,
								null,
								accelerationFactor));

				// simulation architecture
				Architecture architecture =
						new RTArchitecture(
								architectureURI,
								windTurbineCoupledModelURI,
								atomicModelDescriptors,
								coupledModelDescriptors,
								simulatedTimeUnit,
								accelerationFactor);

				return architecture;
			}
}
