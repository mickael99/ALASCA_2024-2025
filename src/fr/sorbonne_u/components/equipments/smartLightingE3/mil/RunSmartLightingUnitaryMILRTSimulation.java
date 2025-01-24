package fr.sorbonne_u.components.equipments.smartLightingE3.mil;

import fr.sorbonne_u.components.equipments.smartLightingE3.mil.events.*;
import fr.sorbonne_u.devs_simulation.architectures.Architecture;
import fr.sorbonne_u.devs_simulation.architectures.RTArchitecture;
import fr.sorbonne_u.devs_simulation.hioa.architectures.AtomicHIOA_Descriptor;
import fr.sorbonne_u.devs_simulation.hioa.architectures.RTCoupledHIOA_Descriptor;
import fr.sorbonne_u.devs_simulation.hioa.models.vars.VariableSink;
import fr.sorbonne_u.devs_simulation.hioa.models.vars.VariableSource;
import fr.sorbonne_u.devs_simulation.models.architectures.AbstractAtomicModelDescriptor;
import fr.sorbonne_u.devs_simulation.models.architectures.CoupledModelDescriptor;
import fr.sorbonne_u.devs_simulation.models.architectures.RTAtomicModelDescriptor;
import fr.sorbonne_u.devs_simulation.models.events.EventSink;
import fr.sorbonne_u.devs_simulation.models.events.EventSource;
import fr.sorbonne_u.devs_simulation.simulators.interfaces.SimulatorI;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class RunSmartLightingUnitaryMILRTSimulation {

    public static final double			ACCELERATION_FACTOR = 1800.0;

    public static void main(String[] args){
        Map<String, AbstractAtomicModelDescriptor> atomicModelDescriptors =
                new HashMap<>();

        try {
            atomicModelDescriptors.put(
                    SmartLightingElectricityModel.MIL_RT_URI,
                    RTAtomicModelDescriptor.create(
                            SmartLightingElectricityModel.class,
                            SmartLightingElectricityModel.MIL_RT_URI,
                            TimeUnit.HOURS,
                            null,
                            ACCELERATION_FACTOR
                                                  )
                                      );
            atomicModelDescriptors.put(
                    SmartLightingIlluminanceModel.MIL_RT_URI,
                    RTAtomicModelDescriptor.create(
                            SmartLightingIlluminanceModel.class,
                            SmartLightingIlluminanceModel.MIL_RT_URI,
                            TimeUnit.HOURS,
                            null,
                            ACCELERATION_FACTOR
                                                  )
                                      );
            atomicModelDescriptors.put(
                    ExternalIlluminanceModel.MIL_RT_URI,
                    RTAtomicModelDescriptor.create(
                            ExternalIlluminanceModel.class,
                            ExternalIlluminanceModel.MIL_RT_URI,
                            TimeUnit.HOURS,
                            null,
                            ACCELERATION_FACTOR
                                                  )
                                      );
            atomicModelDescriptors.put(
                    SmartLightingUnitTesterModel.MIL_RT_URI,
                    RTAtomicModelDescriptor.create(
                            SmartLightingUnitTesterModel.class,
                            SmartLightingUnitTesterModel.MIL_RT_URI,
                            TimeUnit.HOURS,
                            null,
                            ACCELERATION_FACTOR
                                                  )
                                      );
            atomicModelDescriptors.put(
                    SmartLightingStateModel.MIL_RT_URI,
                    AtomicHIOA_Descriptor.create(
                            SmartLightingUnitTesterModel.class,
                            SmartLightingStateModel.MIL_RT_URI,
                            TimeUnit.HOURS,
                            null
                                                )
                                      );

            Map<String, CoupledModelDescriptor> coupledModelDescriptors =
                    new HashMap<>();

            Set<String> submodels = new HashSet<String>();
            submodels.add(SmartLightingElectricityModel.MIL_RT_URI);
            submodels.add(SmartLightingIlluminanceModel.MIL_RT_URI);
            submodels.add(ExternalIlluminanceModel.MIL_RT_URI);
            submodels.add(SmartLightingUnitTesterModel.MIL_RT_URI);
            submodels.add(SmartLightingStateModel.MIL_RT_URI);

            Map<EventSource, EventSink[]> connections = new HashMap<EventSource, EventSink[]>();
            connections.put(
                    new EventSource(
                            SmartLightingUnitTesterModel.MIL_RT_URI,
                            TurnOnSmartLighting.class
                    ),
                    new EventSink[]{
                            new EventSink(
                                    SmartLightingStateModel.MIL_RT_URI,
                                    TurnOnSmartLighting.class
                            )
                    }
                           );
            connections.put(
                    new EventSource(
                            SmartLightingUnitTesterModel.MIL_RT_URI,
                            TurnOffSmartLighting.class
                    ),
                    new EventSink[]{
                            new EventSink(
                                    SmartLightingStateModel.MIL_RT_URI,
                                    TurnOffSmartLighting.class
                            )
                    }
                           );
            connections.put(
                    new EventSource(
                            SmartLightingUnitTesterModel.MIL_RT_URI,
                            SetPowerSmartLighting.class
                    ),
                    new EventSink[]{
                            new EventSink(
                                    SmartLightingStateModel.MIL_RT_URI,
                                    SetPowerSmartLighting.class
                            )
                    }
                           );
            connections.put(
                    new EventSource(
                            SmartLightingUnitTesterModel.MIL_RT_URI,
                            IncreaseLighting.class
                    ),
                    new EventSink[]{
                            new EventSink(
                                    SmartLightingStateModel.MIL_RT_URI,
                                    IncreaseLighting.class
                            )
                    }
                           );
            connections.put(
                    new EventSource(
                            SmartLightingUnitTesterModel.MIL_RT_URI,
                            DecreaseLighting.class
                    ),
                    new EventSink[]{
                            new EventSink(
                                    SmartLightingStateModel.MIL_RT_URI,
                                    DecreaseLighting.class
                            )
                    }
                           );
            connections.put(
                    new EventSource(
                            SmartLightingStateModel.MIL_RT_URI,
                            TurnOnSmartLighting.class
                    ),
                    new EventSink[]{
                            new EventSink(
                                    SmartLightingElectricityModel.MIL_RT_URI,
                                    TurnOnSmartLighting.class
                            )
                    }
                           );
            connections.put(
                    new EventSource(
                            SmartLightingStateModel.MIL_RT_URI,
                            TurnOffSmartLighting.class
                    ),
                    new EventSink[]{
                            new EventSink(
                                    SmartLightingElectricityModel.MIL_RT_URI,
                                    TurnOffSmartLighting.class
                            )
                    }
                           );
            connections.put(
                    new EventSource(
                            SmartLightingStateModel.MIL_RT_URI,
                            SetPowerSmartLighting.class
                    ),
                    new EventSink[]{
                            new EventSink(
                                    SmartLightingElectricityModel.MIL_RT_URI,
                                    SetPowerSmartLighting.class
                            ),
                            new EventSink(
                                    SmartLightingIlluminanceModel.MIL_RT_URI,
                                    SetPowerSmartLighting.class
                            )
                    }
                           );
            connections.put(
                    new EventSource(
                            SmartLightingStateModel.MIL_RT_URI,
                            IncreaseLighting.class
                    ),
                    new EventSink[]{
                            new EventSink(
                                    SmartLightingElectricityModel.MIL_RT_URI,
                                    IncreaseLighting.class
                            ),
                            new EventSink(
                                    SmartLightingIlluminanceModel.MIL_RT_URI,
                                    IncreaseLighting.class
                            )
                    }
                           );
            connections.put(
                    new EventSource(
                            SmartLightingStateModel.MIL_RT_URI,
                            DecreaseLighting.class
                    ),
                    new EventSink[]{
                            new EventSink(
                                    SmartLightingElectricityModel.MIL_RT_URI,
                                    DecreaseLighting.class
                            ),
                            new EventSink(
                                    SmartLightingIlluminanceModel.MIL_RT_URI,
                                    DecreaseLighting.class
                            )
                    }
                           );

            Map<VariableSource, VariableSink[]> bindings =
                    new HashMap<VariableSource, VariableSink[]>();
            bindings.put(
                    new VariableSource(
                            "externalIlluminance",
                            Double.class,
                            ExternalIlluminanceModel.MIL_RT_URI
                    ),
                    new VariableSink[]{
                            new VariableSink(
                                    "externalIlluminance",
                                    Double.class,
                                    SmartLightingIlluminanceModel.MIL_RT_URI
                            )
                    }
                        );

            coupledModelDescriptors.put(
                    SmartLightingUnitTesterModel.MIL_RT_URI,
                    new RTCoupledHIOA_Descriptor(
                            SmartLightingCoupledModel.class,
                            SmartLightingCoupledModel.MIL_RT_URI,
                            submodels,
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

            Architecture architecture =
                    new RTArchitecture(
                            SmartLightingUnitTesterModel.MIL_RT_URI,
                            atomicModelDescriptors,
                            coupledModelDescriptors,
                            TimeUnit.HOURS
                    );

            SimulatorI se = architecture.constructSimulator();
            // run a simulation with the simulation beginning at 0.0 and
            // ending at 24.0
            long start = System.currentTimeMillis() + 100;
            double simulationDuration = 24.1;
            se.startRTSimulation(start, 0.0, simulationDuration);
            long sleepTime =
                    (long) (
                            TimeUnit.HOURS.toMillis(1) *
                            (simulationDuration / ACCELERATION_FACTOR)
                    );
            Thread.sleep(sleepTime + 10000L);
            System.exit(0);
        } catch (Exception e) {
            throw new RuntimeException(e) ;
        }
    }

}
