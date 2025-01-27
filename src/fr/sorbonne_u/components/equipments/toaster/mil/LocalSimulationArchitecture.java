package fr.sorbonne_u.components.equipments.toaster.mil;

import fr.sorbonne_u.components.equipments.toaster.mil.events.SetToasterBrowningLevel;
import fr.sorbonne_u.components.equipments.toaster.mil.events.TurnOffToaster;
import fr.sorbonne_u.components.equipments.toaster.mil.events.TurnOnToaster;
import fr.sorbonne_u.components.hem2024e2.equipments.heater.mil.HeaterCoupledModel;
import fr.sorbonne_u.components.utils.SimulationType;
import fr.sorbonne_u.devs_simulation.architectures.Architecture;
import fr.sorbonne_u.devs_simulation.architectures.RTArchitecture;
import fr.sorbonne_u.devs_simulation.hioa.architectures.CoupledHIOA_Descriptor;
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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class LocalSimulationArchitecture {
    public static Architecture createToasterMILLocalArchitecture4UnitTest(
        String architectureURI,
        TimeUnit simulatedTimeUnit
         ) throws Exception {
        Map<String, AbstractAtomicModelDescriptor> atomicModelDescriptors =
                new HashMap<>();
        atomicModelDescriptors.put(
                ToasterStateModel.MIL_URI,
                AtomicModelDescriptor.create(
                        ToasterStateModel.class,
                        ToasterStateModel.MIL_URI,
                        simulatedTimeUnit,
                        null));
        atomicModelDescriptors.put(
                ToasterElectricityModel.MIL_URI,
                AtomicModelDescriptor.create(
                        ToasterElectricityModel.class,
                        ToasterElectricityModel.MIL_URI,
                        simulatedTimeUnit,
                        null));

        Map<String, CoupledModelDescriptor> coupledModelDescriptors =
                new HashMap<>();
        Set<String> submodels = new HashSet<String>();
        submodels.add(ToasterStateModel.MIL_URI);
        submodels.add(ToasterElectricityModel.MIL_URI);

        Map<Class<? extends EventI>, EventSink[]> imported = new HashMap<>();
        imported.put(
                TurnOnToaster.class,
                new EventSink[] {
                        new EventSink(ToasterStateModel.MIL_URI,
                                      TurnOnToaster.class)
                });
        imported.put(
                TurnOffToaster.class,
                new EventSink[] {
                        new EventSink(ToasterStateModel.MIL_URI,
                                      TurnOffToaster.class)
                });
        imported.put(
                SetToasterBrowningLevel.class,
                new EventSink[]{
                        new EventSink(
                                ToasterStateModel.MIL_URI,
                                SetToasterBrowningLevel.class
                        )
                });


        Map<EventSource,EventSink[]> connections =
                new HashMap<EventSource,EventSink[]>();
        connections.put(
                new EventSource(ToasterStateModel.MIL_URI, TurnOnToaster.class),
                new EventSink[] {
                        new EventSink(ToasterElectricityModel.MIL_URI,
                                      TurnOnToaster.class)
                });
        connections.put(
                new EventSource(ToasterStateModel.MIL_URI, TurnOffToaster.class),
                new EventSink[] {
                        new EventSink(ToasterElectricityModel.MIL_URI,
                                      TurnOffToaster.class)
                });
        connections.put(
                new EventSource(ToasterStateModel.MIL_URI, SetToasterBrowningLevel.class),
                new EventSink[] {
                        new EventSink(ToasterElectricityModel.MIL_URI,
                                      SetToasterBrowningLevel.class)
                });

        coupledModelDescriptors.put(
                ToasterCoupledModel.MIL_URI,
                new CoupledHIOA_Descriptor(
                        ToasterCoupledModel.class,
                        ToasterCoupledModel.MIL_URI,
                        submodels,
                        imported,
                        null,
                        connections,
                        null,
                        null,
                        null,
                        null));
        Architecture architecture =
                new Architecture(
                        architectureURI,
                        ToasterCoupledModel.MIL_URI,
                        atomicModelDescriptors,
                        coupledModelDescriptors,
                        simulatedTimeUnit);
        return architecture;
    }

    public static Architecture	createToaster_RT_LocalArchitecture4UnitTest(
            SimulationType currentSimulationType,
            String architectureURI,
            TimeUnit simulatedTimeUnit,
            double accelerationFactor
                                                                            ) throws Exception{
        assert	currentSimulationType.isMILRTSimulation() ||
                  currentSimulationType.isSILSimulation() :
                new PreconditionException(
                        "currentSimulationType.isMILRTSimulation() || "
                        + "currentSimulationType.isSILSimulation()");

        String toasterStateModelURI = null;
        String toasterElectricityModelURI = null;
        String toasterCoupledModelURI = null;
        String toasterUnitTesterModelURI = null;

        switch (currentSimulationType) {
            case MIL_RT_SIMULATION:
                toasterStateModelURI = ToasterStateModel.MIL_RT_URI;
                toasterElectricityModelURI = ToasterElectricityModel.MIL_RT_URI;
                toasterCoupledModelURI = ToasterCoupledModel.MIL_RT_URI;
                toasterUnitTesterModelURI = ToasterUnitTesterModel.MIL_RT_URI;
                break;
            case SIL_SIMULATION:
                toasterStateModelURI = ToasterStateModel.SIL_URI;
                toasterElectricityModelURI = ToasterElectricityModel.SIL_URI;
                toasterCoupledModelURI = ToasterCoupledModel.SIL_URI;
                toasterUnitTesterModelURI = ToasterUnitTesterModel.SIL_URI;
                break;
            default:
        }

        Map<String,AbstractAtomicModelDescriptor> atomicModelDescriptors =
                new HashMap<>();
        atomicModelDescriptors.put(
                toasterStateModelURI,
                RTAtomicModelDescriptor.create(
                        ToasterStateModel.class,
                        toasterStateModelURI,
                        simulatedTimeUnit,
                        null,
                        accelerationFactor));

        atomicModelDescriptors.put(
                toasterElectricityModelURI,
                RTAtomicModelDescriptor.create(
                        ToasterElectricityModel.class,
                        toasterElectricityModelURI,
                        simulatedTimeUnit,
                        null,
                        accelerationFactor));

        Map<String,CoupledModelDescriptor> coupledModelDescriptors =
                new HashMap<>();

        Set<String> submodels = new HashSet<String>();
        submodels.add(toasterStateModelURI);
        submodels.add(toasterElectricityModelURI);

        Map<Class<? extends EventI>, EventSink[]> imported = new HashMap<>();
        imported.put(
                TurnOnToaster.class,
                new EventSink[] {
                        new EventSink(toasterStateModelURI,
                                      TurnOnToaster.class)
                });
        imported.put(
                TurnOffToaster.class,
                new EventSink[] {
                        new EventSink(toasterStateModelURI,
                                      TurnOffToaster.class)
                });
        imported.put(
                SetToasterBrowningLevel.class,
                new EventSink[]{
                        new EventSink(
                                toasterStateModelURI,
                                SetToasterBrowningLevel.class
                        )
                });

        Map<EventSource,EventSink[]> connections =
                new HashMap<EventSource,EventSink[]>();
        connections.put(
                new EventSource(toasterStateModelURI, TurnOnToaster.class),
                new EventSink[] {
                        new EventSink(toasterElectricityModelURI,
                                      TurnOnToaster.class)
                });
        connections.put(
                new EventSource(toasterStateModelURI, TurnOffToaster.class),
                new EventSink[] {
                        new EventSink(toasterElectricityModelURI,
                                      TurnOffToaster.class)
                });
        connections.put(
                new EventSource(toasterStateModelURI, SetToasterBrowningLevel.class),
                new EventSink[] {
                        new EventSink(toasterElectricityModelURI,
                                      SetToasterBrowningLevel.class)
                });

        coupledModelDescriptors.put(
                toasterCoupledModelURI,
                new RTCoupledHIOA_Descriptor(
                        ToasterCoupledModel.class,
                        toasterCoupledModelURI,
                        submodels,
                        imported,
                        null,
                        connections,
                        null,
                        null,
                        null,
                        null,
                        accelerationFactor));

        Architecture architecture =
                new RTArchitecture(
                        architectureURI,
                        toasterCoupledModelURI,
                        atomicModelDescriptors,
                        coupledModelDescriptors,
                        simulatedTimeUnit,
                        accelerationFactor);

        return architecture;
    }

    public static Architecture createToasterUserMILLocalArchitecture(
        String architectureURI,
        TimeUnit simulatedTimeUnit
		) throws Exception{

        Map<String,AbstractAtomicModelDescriptor> atomicModelDescriptors =
                new HashMap<>();
        atomicModelDescriptors.put(
                ToasterUnitTesterModel.MIL_URI,
                AtomicModelDescriptor.create(
                        ToasterUnitTesterModel.class,
                        ToasterUnitTesterModel.MIL_URI,
                        simulatedTimeUnit,
                        null));

        Map<String,CoupledModelDescriptor> coupledModelDescriptors =
                new HashMap<>();

        Architecture architecture =
                new Architecture(
                        architectureURI,
                        ToasterUnitTesterModel.MIL_URI,
                        atomicModelDescriptors,
                        coupledModelDescriptors,
                        simulatedTimeUnit);
        return architecture;
    }

    public static Architecture	createToasterUserMILRT_LocalArchitecture(
        String architectureURI,
        TimeUnit simulatedTimeUnit,
        double accelerationFactor
          ) throws Exception{

        Map<String,AbstractAtomicModelDescriptor> atomicModelDescriptors =
                new HashMap<>();

        atomicModelDescriptors.put(
                ToasterUnitTesterModel.MIL_RT_URI,
                RTAtomicModelDescriptor.create(
                        ToasterUnitTesterModel.class,
                        ToasterUnitTesterModel.MIL_RT_URI,
                        simulatedTimeUnit,
                        null,
                        accelerationFactor));

        Map<String,CoupledModelDescriptor> coupledModelDescriptors =
                new HashMap<>();

        Architecture architecture =
                new RTArchitecture(
                        architectureURI,
                        ToasterUnitTesterModel.MIL_RT_URI,
                        atomicModelDescriptors,
                        coupledModelDescriptors,
                        simulatedTimeUnit,
                        accelerationFactor);

        return architecture;
    }

    public static Architecture	createToasterMILLocalArchitecture4IntegrationTest(
            String architectureURI,
            TimeUnit simulatedTimeUnit
                                                                                  ) throws Exception{
        Map<String,AbstractAtomicModelDescriptor> atomicModelDescriptors = new HashMap<>();

        atomicModelDescriptors.put(
                ToasterStateModel.MIL_URI,
                AtomicModelDescriptor.create(
                        ToasterStateModel.class,
                        ToasterStateModel.MIL_URI,
                        simulatedTimeUnit,
                        null));

        Map<String,CoupledModelDescriptor> coupledModelDescriptors =
                new HashMap<>();

        Set<String> submodels = new HashSet<String>();
        submodels.add(ToasterStateModel.MIL_URI);

        Map<Class<? extends EventI>, EventSink[]> imported = new HashMap<>();
        imported.put(
                TurnOnToaster.class,
                new EventSink[] {
                        new EventSink(ToasterStateModel.MIL_URI,
                                      TurnOnToaster.class)
                });
        imported.put(
                TurnOffToaster.class,
                new EventSink[] {
                        new EventSink(ToasterStateModel.MIL_URI,
                                      TurnOffToaster.class)
                });
        imported.put(
                SetToasterBrowningLevel.class,
                new EventSink[]{
                        new EventSink(
                                ToasterStateModel.MIL_URI,
                                SetToasterBrowningLevel.class
                        )
                });

        Map<Class<? extends EventI>,ReexportedEvent> reexported =
                new HashMap<Class<? extends EventI>, ReexportedEvent>();

        reexported.put(
                TurnOnToaster.class,
                new ReexportedEvent(
                        ToasterStateModel.MIL_URI,
                        TurnOnToaster.class));
        reexported.put(
                TurnOffToaster.class,
                new ReexportedEvent(
                        ToasterStateModel.MIL_URI,
                        TurnOffToaster.class));
        reexported.put(
                SetToasterBrowningLevel.class,
                new ReexportedEvent(
                        ToasterStateModel.MIL_URI,
                        SetToasterBrowningLevel.class));

        Map<EventSource,EventSink[]> connections = new HashMap<EventSource,EventSink[]>();
        connections.put(
                new EventSource(ToasterStateModel.MIL_URI, TurnOnToaster.class),
                new EventSink[] {
                        new EventSink(ToasterStateModel.MIL_URI,
                                      TurnOnToaster.class)
                });
        connections.put(
                new EventSource(ToasterStateModel.MIL_URI, TurnOffToaster.class),
                new EventSink[] {
                        new EventSink(ToasterStateModel.MIL_URI,
                                      TurnOffToaster.class)
                });
        connections.put(
                new EventSource(ToasterStateModel.MIL_URI, SetToasterBrowningLevel.class),
                new EventSink[] {
                        new EventSink(ToasterStateModel.MIL_URI,
                                      SetToasterBrowningLevel.class)
                });

        coupledModelDescriptors.put(
                ToasterCoupledModel.MIL_URI,
                new CoupledHIOA_Descriptor(
                        ToasterCoupledModel.class,
                        ToasterCoupledModel.MIL_URI,
                        submodels,
                        imported,
                        reexported,
                        connections,
                        null,
                        null,
                        null,
                        null));

        Architecture architecture =
                new Architecture(
                        architectureURI,
                        ToasterCoupledModel.MIL_URI,
                        atomicModelDescriptors,
                        coupledModelDescriptors,
                        simulatedTimeUnit);

        return architecture;
    }

    public static Architecture	createToaster_RT_LocalArchitecture4IntegrationTest(
            SimulationType currentSimulationType,
            String architectureURI,
            TimeUnit simulatedTimeUnit,
            double accelerationFactor
                                                                                   ) throws Exception{
        assert	currentSimulationType.isMILRTSimulation() || currentSimulationType.isSILSimulation() :
                new PreconditionException(
                        "currentSimulationType.isMILRTSimulation() || "
                        + "currentSimulationType.isSILSimulation()");

        String toasterStateModelURI = null;
        String toasterElectricityModelURI = null;
        String toasterCoupledModelURI = null;

        switch (currentSimulationType) {
            case MIL_RT_SIMULATION:
                toasterStateModelURI = ToasterStateModel.MIL_RT_URI;
                toasterElectricityModelURI = ToasterElectricityModel.MIL_RT_URI;
                toasterCoupledModelURI = ToasterCoupledModel.MIL_RT_URI;
                break;
            case SIL_SIMULATION:
                toasterStateModelURI = ToasterStateModel.SIL_URI;
                toasterElectricityModelURI = ToasterElectricityModel.SIL_URI;
                toasterCoupledModelURI = ToasterCoupledModel.SIL_URI;
                break;
            default:
        }

        Map<String,AbstractAtomicModelDescriptor> atomicModelDescriptors =
                new HashMap<>();
        atomicModelDescriptors.put(
                toasterStateModelURI,
                RTAtomicModelDescriptor.create(
                        ToasterStateModel.class,
                        toasterStateModelURI,
                        simulatedTimeUnit,
                        null,
                        accelerationFactor));

        Map<String,CoupledModelDescriptor> coupledModelDescriptors =
                new HashMap<>();
        Set<String> submodels = new HashSet<String>();
        submodels.add(toasterStateModelURI);

        Map<Class<? extends EventI>, EventSink[]> imported = new HashMap<>();
        imported.put(
                TurnOnToaster.class,
                new EventSink[] {
                        new EventSink(toasterStateModelURI,
                                      TurnOnToaster.class)
                });
        imported.put(
                TurnOffToaster.class,
                new EventSink[] {
                        new EventSink(toasterStateModelURI,
                                      TurnOffToaster.class)
                });
        imported.put(
                SetToasterBrowningLevel.class,
                new EventSink[]{
                        new EventSink(
                                toasterStateModelURI,
                                SetToasterBrowningLevel.class
                        )
                });

        Map<Class<? extends EventI>,ReexportedEvent> reexported =
                new HashMap<Class<? extends EventI>, ReexportedEvent>();
        reexported.put(
                TurnOnToaster.class,
                new ReexportedEvent(
                        toasterStateModelURI,
                        TurnOnToaster.class));
        reexported.put(
                TurnOffToaster.class,
                new ReexportedEvent(
                        toasterStateModelURI,
                        TurnOffToaster.class));
        reexported.put(
                SetToasterBrowningLevel.class,
                new ReexportedEvent(
                        toasterStateModelURI,
                        SetToasterBrowningLevel.class));

        Map<EventSource,EventSink[]> connections = new HashMap<EventSource,EventSink[]>();
        connections.put(
                new EventSource(toasterStateModelURI, TurnOnToaster.class),
                new EventSink[] {
                        new EventSink(toasterElectricityModelURI,
                                      TurnOnToaster.class)
                });
        connections.put(
                new EventSource(toasterStateModelURI, TurnOffToaster.class),
                new EventSink[] {
                        new EventSink(toasterElectricityModelURI,
                                      TurnOffToaster.class)
                });
        connections.put(
                new EventSource(toasterStateModelURI, SetToasterBrowningLevel.class),
                new EventSink[] {
                        new EventSink(toasterElectricityModelURI,
                                      SetToasterBrowningLevel.class)
                });

        coupledModelDescriptors.put(
                toasterCoupledModelURI,
                new RTCoupledHIOA_Descriptor(
                        ToasterCoupledModel.class,
                        toasterCoupledModelURI,
                        submodels,
                        imported,
                        reexported,
                        connections,
                        null,
                        null,
                        null,
                        null,
                        accelerationFactor));

        Architecture architecture =
                new RTArchitecture(
                        architectureURI,
                        toasterCoupledModelURI,
                        atomicModelDescriptors,
                        coupledModelDescriptors,
                        simulatedTimeUnit,
                        accelerationFactor);

        return architecture;
    }
}
