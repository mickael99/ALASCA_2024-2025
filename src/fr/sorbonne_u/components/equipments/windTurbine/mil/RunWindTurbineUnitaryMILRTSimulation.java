package fr.sorbonne_u.components.equipments.windTurbine.mil;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import fr.sorbonne_u.components.equipments.windTurbine.mil.events.StartWindTurbineEvent;
import fr.sorbonne_u.components.equipments.windTurbine.mil.events.StopWindTurbineEvent;
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
import fr.sorbonne_u.devs_simulation.simulators.interfaces.SimulatorI;

public class RunWindTurbineUnitaryMILRTSimulation {

	public static final double ACCELERATION_FACTOR = 1800.0;
	
	public static void main(String[] args) {
		try {
			 Map<String, AbstractAtomicModelDescriptor> atomicModelDescriptors = new HashMap<>();

            atomicModelDescriptors.put(
                    WindTurbineElectricityModel.MIL_RT_URI,
                    RTAtomicHIOA_Descriptor.create(
                            WindTurbineElectricityModel.class,
                            WindTurbineElectricityModel.MIL_RT_URI,
                            TimeUnit.HOURS,
                            null,
                            ACCELERATION_FACTOR
                    )
            );
            atomicModelDescriptors.put(
                    ExternalWindModel.MIL_RT_URI,
                    RTAtomicHIOA_Descriptor.create(
                            ExternalWindModel.class,
                            ExternalWindModel.MIL_RT_URI,
                            TimeUnit.HOURS,
                            null,
                            ACCELERATION_FACTOR
                    )
            );
            atomicModelDescriptors.put(
                    WindTurbineUserModel.MIL_RT_URI,
                    RTAtomicModelDescriptor.create(
                            WindTurbineUserModel.class,
                            WindTurbineUserModel.MIL_RT_URI,
                            TimeUnit.HOURS,
                            null,
                            ACCELERATION_FACTOR
                    )
            );
            atomicModelDescriptors.put(
                    WindTurbineStateModel.MIL_RT_URI,
                    RTAtomicModelDescriptor.create(
                    		WindTurbineStateModel.class,
                    		WindTurbineStateModel.MIL_RT_URI,
                            TimeUnit.HOURS,
                            null,
                            ACCELERATION_FACTOR
                    )
            );
            
            Map<String, CoupledModelDescriptor> coupledModelDescriptors = new HashMap<>();
            Set<String> subModel = new HashSet<>();
            subModel.add(WindTurbineElectricityModel.MIL_RT_URI);
            subModel.add(WindTurbineStateModel.MIL_RT_URI);
            subModel.add(ExternalWindModel.MIL_RT_URI);
            subModel.add(WindTurbineUserModel.MIL_RT_URI);
            
            
            Map<EventSource, EventSink[]> connections = new HashMap<EventSource,EventSink[]>();

            connections.put(
                    new EventSource(WindTurbineUserModel.MIL_RT_URI, StartWindTurbineEvent.class),
                    new EventSink[] {
                            new EventSink(WindTurbineStateModel.MIL_RT_URI, StartWindTurbineEvent.class)
                    }
            );

            connections.put(
                    new EventSource(WindTurbineUserModel.MIL_RT_URI, StopWindTurbineEvent.class),
                    new EventSink[] {
                            new EventSink(WindTurbineStateModel.MIL_RT_URI, StopWindTurbineEvent.class)
                    }
            );
            connections.put(
                    new EventSource(WindTurbineStateModel.MIL_RT_URI, StartWindTurbineEvent.class),
                    new EventSink[] {
                            new EventSink(WindTurbineElectricityModel.MIL_RT_URI, StartWindTurbineEvent.class)
                    }
            );

            connections.put(
                    new EventSource(WindTurbineStateModel.MIL_RT_URI, StopWindTurbineEvent.class),
                    new EventSink[] {
                            new EventSink(WindTurbineElectricityModel.MIL_RT_URI, StopWindTurbineEvent.class)
                    }
            );
            
            Map<VariableSource, VariableSink[]> bindings = new HashMap<>();

            bindings.put(
                new VariableSource("externalWindSpeed",
                        Double.class,
                        ExternalWindModel.MIL_RT_URI),
                new VariableSink[] {
                        new VariableSink("externalWindSpeed",
                                Double.class,
                                WindTurbineElectricityModel.MIL_RT_URI)
            });
            
            coupledModelDescriptors.put(
            		WindTurbineCoupledModel.MIL_RT_URI,
                    new RTCoupledHIOA_Descriptor(
                    		WindTurbineCoupledModel.class,
                    		WindTurbineCoupledModel.MIL_RT_URI,
                            subModel,
                            null,
                            null,
                            connections,
                            null,
                            null,
                            null,
                            bindings,
                            ACCELERATION_FACTOR
                    )
            );
            
            ArchitectureI architecture = new RTArchitecture(
            		WindTurbineCoupledModel.MIL_RT_URI,
                    atomicModelDescriptors,
                    coupledModelDescriptors,
                    TimeUnit.HOURS
            );
            
            SimulatorI se = architecture.constructSimulator();
			long start = System.currentTimeMillis() + 100;
			double simulationDuration = 40.0;
			se.startRTSimulation(start, 0.0, simulationDuration);
			long sleepTime =
				(long)(TimeUnit.HOURS.toMillis(1) *
								(simulationDuration/ACCELERATION_FACTOR));
			Thread.sleep(sleepTime + 10000L);
			System.exit(0);
            
		}
		catch(Exception e) {
			throw new RuntimeException(e);
		}

	}

}
