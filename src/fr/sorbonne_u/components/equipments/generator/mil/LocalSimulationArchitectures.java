package fr.sorbonne_u.components.equipments.generator.mil;

import fr.sorbonne_u.components.equipments.generator.mil.events.ActivateGeneratorEvent;
import fr.sorbonne_u.components.equipments.generator.mil.events.StopGeneratorEvent;
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
import fr.sorbonne_u.exceptions.PreconditionException;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public abstract class LocalSimulationArchitectures {

    public static Architecture createGeneratorMILLocalArchitecture4UnitTest(
            String architectureURI,
            TimeUnit simulatedTimeUnit
                                                                           ) throws Exception{
        Map<String, AbstractAtomicModelDescriptor> atomicModelDescriptors =
                new HashMap<>();

        atomicModelDescriptors.put(
                GeneratorElectricityModel.MIL_URI,
                AtomicHIOA_Descriptor.create(
                        GeneratorElectricityModel.class,
                        GeneratorElectricityModel.MIL_URI,
                        simulatedTimeUnit,
                        null
                                            ));
        atomicModelDescriptors.put(
                GeneratorFuelModel.MIL_URI,
                AtomicHIOA_Descriptor.create(
                        GeneratorFuelModel.class,
                        GeneratorFuelModel.MIL_URI,
                        simulatedTimeUnit,
                        null
                                            ));

        atomicModelDescriptors.put(
                GeneratorStateModel.MIL_URI,
                AtomicModelDescriptor.create(
                        GeneratorStateModel.class,
                        GeneratorStateModel.MIL_URI,
                        simulatedTimeUnit,
                        null
                                            ));

        Map<String, CoupledModelDescriptor> coupledModelDescriptors =
                new HashMap<>();

        Set<String> submodels = new HashSet<String>();
        submodels.add(GeneratorElectricityModel.MIL_URI);
        submodels.add(GeneratorStateModel.MIL_URI);
        submodels.add(GeneratorFuelModel.MIL_URI);

        Map<Class<? extends EventI>,EventSink[]> imported =
                new HashMap<Class<? extends EventI>, EventSink[]>();
        imported.put(
                ActivateGeneratorEvent.class,
                new EventSink[]{new EventSink(GeneratorStateModel.MIL_URI,
                                                   ActivateGeneratorEvent.class)});
        imported.put(
                StopGeneratorEvent.class,
                new EventSink[]{new EventSink(GeneratorStateModel.MIL_URI,
                                                    StopGeneratorEvent.class)});

        Map<EventSource,EventSink[]> connections =
                new HashMap<EventSource,EventSink[]>();
        connections.put(new EventSource(GeneratorStateModel.MIL_URI,
                                        ActivateGeneratorEvent.class),
                        new EventSink[]{new EventSink(GeneratorElectricityModel.MIL_URI,
                                                    ActivateGeneratorEvent.class)});
        connections.put(new EventSource(GeneratorStateModel.MIL_URI,
                                        StopGeneratorEvent.class),
                        new EventSink[]{new EventSink(GeneratorElectricityModel.MIL_URI,
                                                    StopGeneratorEvent.class)});

        Map<VariableSource,VariableSink[]> bindings =
                new HashMap<VariableSource, VariableSink[]>();
        bindings.put(new VariableSource("currentFuelLevel",
                                        Double.class,
                                        GeneratorFuelModel.MIL_URI),
                        new VariableSink[]{new VariableSink("currentFuelLevel",
                                                            Double.class,
                                                            GeneratorElectricityModel.MIL_URI)});


        coupledModelDescriptors.put(
                GeneratorCoupledModel.MIL_URI,
                new CoupledHIOA_Descriptor(
                        GeneratorCoupledModel.class,
                        GeneratorCoupledModel.MIL_URI,
                        submodels,
                        imported,
                        null,
                        connections,
                        null,
                        null,
                        null,
                        bindings
                                            ));

        Architecture architecture = new Architecture(
                architectureURI,
                GeneratorCoupledModel.MIL_URI,
                atomicModelDescriptors,
                coupledModelDescriptors,
                simulatedTimeUnit
                                                    );
        return architecture;

    }

    public static Architecture	createGeneratorMIL_RT_Architecture4UnitTest(
            String architectureURI,
            TimeUnit simulatedTimeUnit,
            double accelerationFactor
                                                                             ) throws Exception{
        Map<String, AbstractAtomicModelDescriptor> atomicModelDescriptors = new HashMap<>();

        atomicModelDescriptors.put(
                GeneratorElectricityModel.MIL_RT_URI,
                RTAtomicHIOA_Descriptor.create(
                        GeneratorElectricityModel.class,
                        GeneratorElectricityModel.MIL_RT_URI,
                        simulatedTimeUnit,
                        null,
                        accelerationFactor
                                              ));
        atomicModelDescriptors.put(
                GeneratorStateModel.MIL_RT_URI,
                RTAtomicModelDescriptor.create(
                        GeneratorStateModel.class,
                        GeneratorStateModel.MIL_RT_URI,
                        simulatedTimeUnit,
                        null
                                              ));
        atomicModelDescriptors.put(
                GeneratorFuelModel.MIL_RT_URI,
                RTAtomicHIOA_Descriptor.create(
                        GeneratorFuelModel.class,
                        GeneratorFuelModel.MIL_RT_URI,
                        simulatedTimeUnit,
                        null,
                        accelerationFactor
                                              ));

        Map<String, CoupledModelDescriptor> coupledModelDescriptors = new HashMap<>();

        Set<String> submodels = new HashSet<String>();
        submodels.add(GeneratorElectricityModel.MIL_RT_URI);
        submodels.add(GeneratorStateModel.MIL_RT_URI);
        submodels.add(GeneratorFuelModel.MIL_RT_URI);

        Map<Class<? extends EventI>,EventSink[]> imported = new HashMap<Class<? extends EventI>, EventSink[]>();
        imported.put(ActivateGeneratorEvent.class,
                     new EventSink[]{new EventSink(GeneratorStateModel.MIL_RT_URI,
                                                   ActivateGeneratorEvent.class)});
        imported.put(StopGeneratorEvent.class,
                        new EventSink[]{new EventSink(GeneratorStateModel.MIL_RT_URI,
                                                    StopGeneratorEvent.class)});

        Map<EventSource,EventSink[]> connections = new HashMap<EventSource,EventSink[]>();
        connections.put(new EventSource(GeneratorStateModel.MIL_RT_URI,
                                        ActivateGeneratorEvent.class),
                        new EventSink[]{new EventSink(GeneratorElectricityModel.MIL_RT_URI,
                                                    ActivateGeneratorEvent.class)});
        connections.put(new EventSource(GeneratorStateModel.MIL_RT_URI,
                                        StopGeneratorEvent.class),
                        new EventSink[]{new EventSink(GeneratorElectricityModel.MIL_RT_URI,
                                                    StopGeneratorEvent.class)});

        Map<VariableSource,VariableSink[]> bindings = new HashMap<VariableSource, VariableSink[]>();
        bindings.put(new VariableSource("currentFuelLevel",
                                        Double.class,
                                        GeneratorFuelModel.MIL_RT_URI),
                        new VariableSink[]{new VariableSink("currentFuelLevel",
                                                            Double.class,
                                                            GeneratorElectricityModel.MIL_RT_URI)});

        coupledModelDescriptors.put(
                GeneratorCoupledModel.MIL_RT_URI,
                new RTCoupledHIOA_Descriptor(
                        GeneratorCoupledModel.class,
                        GeneratorCoupledModel.MIL_RT_URI,
                        submodels,
                        imported,
                        null,
                        connections,
                        null,
                        null,
                        null,
                        bindings
                                            ));

        Architecture architecture = new RTArchitecture(
                architectureURI,
                GeneratorCoupledModel.MIL_RT_URI,
                atomicModelDescriptors,
                coupledModelDescriptors,
                simulatedTimeUnit,
                accelerationFactor
                                                       );
        return architecture;
    }

    public static Architecture createGeneratorMILArchitecture4IntegrationTest(
            String architectureURI,
            TimeUnit simulatedTimeUnit
                                                                             ) throws Exception{
        Map<String, AbstractAtomicModelDescriptor> atomicModelDescriptors = new HashMap<>();
        atomicModelDescriptors.put(
                GeneratorStateModel.MIL_URI,
                AtomicHIOA_Descriptor.create(
                        GeneratorStateModel.class,
                        GeneratorStateModel.MIL_URI,
                        simulatedTimeUnit,
                        null
                                            ));

        Map<String, CoupledModelDescriptor> coupledModelDescriptors = new HashMap<>();
        Architecture architecture = new Architecture(
                architectureURI,
                GeneratorStateModel.MIL_URI,
                atomicModelDescriptors,
                coupledModelDescriptors,
                simulatedTimeUnit
                                                    );
        return architecture;
    }

    public static Architecture createGeneratorMIL_RT_Architecture4IntegrationTest(
            String architectureURI,
            TimeUnit simulatedTimeUnit,
            double accelerationFactor
                                                                                 ) throws Exception{
        assert	accelerationFactor > 0.0 :
                new PreconditionException("accelerationFactor > 0.0");

        Map<String, AbstractAtomicModelDescriptor> atomicModelDescriptors = new HashMap<>();

        atomicModelDescriptors.put(
                GeneratorStateModel.MIL_RT_URI,
                RTAtomicModelDescriptor.create(
                        GeneratorStateModel.class,
                        GeneratorStateModel.MIL_RT_URI,
                        simulatedTimeUnit,
                        null,
                        accelerationFactor
                                              ));

        Map<String, CoupledModelDescriptor> coupledModelDescriptors = new HashMap<>();
        Architecture architecture = new RTArchitecture(
                architectureURI,
                GeneratorStateModel.MIL_RT_URI,
                atomicModelDescriptors,
                coupledModelDescriptors,
                simulatedTimeUnit,
                accelerationFactor
                                                       );
        return architecture;
    }

    public static Architecture	createGeneratorUserMIL_Architecture(
            String architectureURI,
            TimeUnit simulatedTimeUnit
                                                                     ) throws Exception{
        Map<String, AbstractAtomicModelDescriptor> atomicModelDescriptors = new HashMap<>();

        atomicModelDescriptors.put(
                GeneratorUserModel.MIL_URI,
                AtomicModelDescriptor.create(
                        GeneratorUserModel.class,
                        GeneratorUserModel.MIL_URI,
                        simulatedTimeUnit,
                        null
                                            ));

        Map<String, CoupledModelDescriptor> coupledModelDescriptors = new HashMap<>();
        Architecture architecture = new Architecture(
                architectureURI,
                GeneratorUserModel.MIL_URI,
                atomicModelDescriptors,
                coupledModelDescriptors,
                simulatedTimeUnit
                                                    );
        return architecture;
    }

    public static Architecture	createGeneratorUserMIL_RT_Architecture(
            String architectureURI,
            TimeUnit simulatedTimeUnit,
            double accelerationFactor
                                                                        ) throws Exception{
        assert	accelerationFactor > 0.0 :
                new PreconditionException("accelerationFactor > 0.0");

        Map<String, AbstractAtomicModelDescriptor> atomicModelDescriptors = new HashMap<>();

        atomicModelDescriptors.put(
                GeneratorUserModel.MIL_RT_URI,
                RTAtomicModelDescriptor.create(
                        GeneratorUserModel.class,
                        GeneratorUserModel.MIL_RT_URI,
                        simulatedTimeUnit,
                        null,
                        accelerationFactor
                                              ));

        Map<String, CoupledModelDescriptor> coupledModelDescriptors = new HashMap<>();
        Architecture architecture = new RTArchitecture(
                architectureURI,
                GeneratorUserModel.MIL_RT_URI,
                atomicModelDescriptors,
                coupledModelDescriptors,
                simulatedTimeUnit,
                accelerationFactor
                                                       );
        return architecture;
    }
}
