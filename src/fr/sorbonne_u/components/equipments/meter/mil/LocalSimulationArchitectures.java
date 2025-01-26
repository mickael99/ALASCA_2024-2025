package fr.sorbonne_u.components.equipments.meter.mil;

import fr.sorbonne_u.components.equipments.fridge.mil.FridgeElectricityModel;
import fr.sorbonne_u.components.equipments.fridge.mil.events.*;
import fr.sorbonne_u.components.equipments.iron.mil.IronElectricityModel;
import fr.sorbonne_u.components.equipments.iron.mil.events.*;
import fr.sorbonne_u.components.utils.SimulationType;
import fr.sorbonne_u.devs_simulation.architectures.Architecture;
import fr.sorbonne_u.devs_simulation.architectures.RTArchitecture;
import fr.sorbonne_u.devs_simulation.hioa.architectures.*;
import fr.sorbonne_u.devs_simulation.hioa.models.AtomicHIOA;
import fr.sorbonne_u.devs_simulation.hioa.models.vars.VariableSink;
import fr.sorbonne_u.devs_simulation.hioa.models.vars.VariableSource;
import fr.sorbonne_u.devs_simulation.models.architectures.AbstractAtomicModelDescriptor;
import fr.sorbonne_u.devs_simulation.models.architectures.CoupledModelDescriptor;
import fr.sorbonne_u.devs_simulation.models.events.EventI;
import fr.sorbonne_u.devs_simulation.models.events.EventSink;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public abstract class LocalSimulationArchitectures {

    public static Architecture createElectricMeterMILArchitecture4IntegrationTests(
            String architectureURI,
            TimeUnit simulatedTimeUnit
                                                                                  ) throws Exception{
        Map<String, AbstractAtomicModelDescriptor> atomicModelDescriptors =
                new HashMap<>();

        atomicModelDescriptors.put(
                MeterElectricityModel.MIL_URI,
                AtomicHIOA_Descriptor.create(
                        MeterElectricityModel.class,
                        MeterElectricityModel.MIL_URI,
                        simulatedTimeUnit,
                        null));

//        atomicModelDescriptors.put(
//                SmartLightingElectricityModel.URI,
//                AtomicHIOA_Descriptor.create(
//                        SmartLightingElectricityModel.class,
//                        SmartLightingElectricityModel.URI,
//                        simulatedTimeUnit,
//                        null));
//
//        atomicModelDescriptors.put(
//                ToasterElectricityModel.URI,
//                AtomicHIOA_Descriptor.create(
//                        ToasterElectricityModel.class,
//                        ToasterElectricityModel.URI,
//                        simulatedTimeUnit,
//                        null));

        atomicModelDescriptors.put(
                FridgeElectricityModel.MIL_URI,
                AtomicHIOA_Descriptor.create(
                        FridgeElectricityModel.class,
                        FridgeElectricityModel.MIL_URI,
                        simulatedTimeUnit,
                        null));

        atomicModelDescriptors.put(
                IronElectricityModel.MIL_URI,
                AtomicHIOA_Descriptor.create(
                        IronElectricityModel.class,
                        IronElectricityModel.MIL_URI,
                        simulatedTimeUnit,
                        null));

        Map<String, CoupledModelDescriptor> coupledModelDescriptors =
                new HashMap<>();

        Set<String> submodels = new HashSet<String>();
        submodels.add(MeterElectricityModel.MIL_URI);
        submodels.add(FridgeElectricityModel.MIL_URI);
        submodels.add(IronElectricityModel.MIL_URI);

        Map<Class<? extends EventI>, EventSink[]> imported = new HashMap<>();

        imported.put(
                CloseDoorFridge.class,
                new EventSink[]{
                        new EventSink(FridgeElectricityModel.MIL_URI,
                                      CloseDoorFridge.class)
                });
        imported.put(
                CoolFridge.class,
                new EventSink[]{
                        new EventSink(FridgeElectricityModel.MIL_URI,
                                      CoolFridge.class)
                });
        imported.put(
                DoNotCoolFridge.class,
                new EventSink[]{
                        new EventSink(FridgeElectricityModel.MIL_URI,
                                      DoNotCoolFridge.class)
                });
        imported.put(
                OpenDoorFridge.class,
                new EventSink[]{
                        new EventSink(FridgeElectricityModel.MIL_URI,
                                      OpenDoorFridge.class)
                });
        imported.put(
                SetPowerFridge.class,
                new EventSink[]{
                        new EventSink(FridgeElectricityModel.MIL_URI,
                                      SetPowerFridge.class)
                });
        imported.put(
                SwitchOffFridge.class,
                new EventSink[]{
                        new EventSink(FridgeElectricityModel.MIL_URI,
                                      SwitchOffFridge.class)
                });
        imported.put(
                SwitchOnFridge.class,
                new EventSink[]{
                        new EventSink(
                                FridgeElectricityModel.MIL_URI,
                                SwitchOnFridge.class
                        )
                });
        imported.put(
                DisableEnergySavingModeIron.class,
                new EventSink[]{
                        new EventSink(IronElectricityModel.MIL_URI,
                                      DisableEnergySavingModeIron.class)
                });
        imported.put(
                EnableSteamModeIron.class,
                new EventSink[]{
                        new EventSink(IronElectricityModel.MIL_URI,
                                      EnableSteamModeIron.class)
                });
        imported.put(
                EnableCottonModeIron.class,
                new EventSink[]{
                        new EventSink(IronElectricityModel.MIL_URI,
                                      EnableCottonModeIron.class)
                });
        imported.put(
                EnableDelicateModeIron.class,
                new EventSink[]{
                        new EventSink(IronElectricityModel.MIL_URI,
                                      EnableDelicateModeIron.class)
                });
        imported.put(
                EnableEnergySavingModeIron.class,
                new EventSink[]{
                        new EventSink(IronElectricityModel.MIL_URI,
                                      EnableEnergySavingModeIron.class)
                });
        imported.put(
                EnableLinenModeIron.class,
                new EventSink[]{
                        new EventSink(IronElectricityModel.MIL_URI,
                                      EnableLinenModeIron.class)
                });
        imported.put(
                DisableSteamModeIron.class,
                new EventSink[]{
                        new EventSink(IronElectricityModel.MIL_URI,
                                      DisableSteamModeIron.class)
                });
        imported.put(
                TurnOnIron.class,
                new EventSink[]{
                        new EventSink(IronElectricityModel.MIL_URI,
                                      TurnOnIron.class)
                });
        imported.put(
                TurnOffIron.class,
                new EventSink[]{
                        new EventSink(IronElectricityModel.MIL_URI,
                                      TurnOffIron.class)
                });

        Map<VariableSource,VariableSink[]> bindings =
                new HashMap<VariableSource, VariableSink[]>();

        bindings.put(
                new VariableSource("currentIntensity", Double.class, FridgeElectricityModel.MIL_URI),
                new VariableSink[]{
                        new VariableSink("currentFridgeIntensity",
                                         Double.class,
                                         MeterElectricityModel.MIL_URI)
                });
        bindings.put(
                new VariableSource("currentIntensity", Double.class, IronElectricityModel.MIL_URI),
                new VariableSink[]{
                        new VariableSink("currentIronIntensity",
                                         Double.class,
                                         MeterElectricityModel.MIL_URI)
                });

        coupledModelDescriptors.put(
                MeterElectricityModel.MIL_URI,
                new CoupledHIOA_Descriptor(
                        MeterCoupledModel.class,
                        MeterCoupledModel.MIL_URI,
                        submodels,
                        imported,
                        null,
                        null,
                        null,
                        null,
                        null,
                        bindings,
                        new HIOA_Composer()
                ));

        Architecture architecture = new Architecture(
                architectureURI,
                MeterCoupledModel.MIL_URI,
                atomicModelDescriptors,
                coupledModelDescriptors,
                simulatedTimeUnit);

        return architecture;
    }

    public static Architecture	createElectricMeter_RT_Architecture4IntegrationTests(
            SimulationType currentSimulationType,
            String architectureURI,
            TimeUnit simulatedTimeUnit,
            double accelerationFactor
                                                                                      ) throws Exception{
        String electricMeterElectricityModelURI = null;
        Class<? extends AtomicHIOA> electricMeterElectricityModelClass = null;
        String fridgeElectricityModelURI = null;
        String ironElectricityModelURI = null;
        String electricMeterCoupledModelURI = null;

        switch (currentSimulationType) {
            case MIL_RT_SIMULATION:
                electricMeterElectricityModelURI = MeterElectricityModel.MIL_RT_URI;
                electricMeterElectricityModelClass = MeterElectricityModel.class;
                fridgeElectricityModelURI = FridgeElectricityModel.MIL_RT_URI;
                ironElectricityModelURI = IronElectricityModel.MIL_RT_URI;
                electricMeterCoupledModelURI = MeterCoupledModel.MIL_RT_URI;
                break;
            case SIL_SIMULATION:
                electricMeterElectricityModelURI = MeterElectricityModel.SIL_URI;
                electricMeterElectricityModelClass = MeterElectricitySILModel.class;
                fridgeElectricityModelURI = FridgeElectricityModel.SIL_URI;
                ironElectricityModelURI = IronElectricityModel.SIL_URI;
                electricMeterCoupledModelURI = MeterCoupledModel.SIL_URI;
                break;
            default:
        }

        Map<String,AbstractAtomicModelDescriptor> atomicModelDescriptors =
                new HashMap<>();
        atomicModelDescriptors.put(
                fridgeElectricityModelURI,
                RTAtomicHIOA_Descriptor.create(
                        FridgeElectricityModel.class,
                        fridgeElectricityModelURI,
                        simulatedTimeUnit,
                        null,
                        accelerationFactor));
        atomicModelDescriptors.put(
                ironElectricityModelURI,
                RTAtomicHIOA_Descriptor.create(
                        IronElectricityModel.class,
                        ironElectricityModelURI,
                        simulatedTimeUnit,
                        null,
                        accelerationFactor));

        Map<String,CoupledModelDescriptor> coupledModelDescriptors =
                new HashMap<>();

        Set<String> submodels = new HashSet<String>();
        submodels.add(fridgeElectricityModelURI);
        submodels.add(ironElectricityModelURI);
        submodels.add(electricMeterElectricityModelURI);

        Map<Class<? extends EventI>,EventSink[]> imported =
                new HashMap<>();
        imported.put(
                CloseDoorFridge.class,
                new EventSink[]{
                        new EventSink(fridgeElectricityModelURI,
                                      CloseDoorFridge.class)
                });
        imported.put(
                CoolFridge.class,
                new EventSink[]{
                        new EventSink(fridgeElectricityModelURI,
                                      CoolFridge.class)
                });
        imported.put(
                DoNotCoolFridge.class,
                new EventSink[]{
                        new EventSink(fridgeElectricityModelURI,
                                      DoNotCoolFridge.class)
                });
        imported.put(
                OpenDoorFridge.class,
                new EventSink[]{
                        new EventSink(fridgeElectricityModelURI,
                                      OpenDoorFridge.class)
                });
        imported.put(
                SetPowerFridge.class,
                new EventSink[]{
                        new EventSink(fridgeElectricityModelURI,
                                      SetPowerFridge.class)
                });
        imported.put(
                SwitchOffFridge.class,
                new EventSink[]{
                        new EventSink(fridgeElectricityModelURI,
                                      SwitchOffFridge.class)
                });
        imported.put(
                SwitchOnFridge.class,
                new EventSink[]{
                        new EventSink(
                                fridgeElectricityModelURI,
                                SwitchOnFridge.class
                        )
                });
        imported.put(
                DisableEnergySavingModeIron.class,
                new EventSink[]{
                        new EventSink(ironElectricityModelURI,
                                      DisableEnergySavingModeIron.class)
                });
        imported.put(
                EnableSteamModeIron.class,
                new EventSink[]{
                        new EventSink(ironElectricityModelURI,
                                      EnableSteamModeIron.class)
                });
        imported.put(
                EnableCottonModeIron.class,
                new EventSink[]{
                        new EventSink(ironElectricityModelURI,
                                      EnableCottonModeIron.class)
                });
        imported.put(
                EnableDelicateModeIron.class,
                new EventSink[]{
                        new EventSink(ironElectricityModelURI,
                                      EnableDelicateModeIron.class)
                });
        imported.put(
                EnableEnergySavingModeIron.class,
                new EventSink[]{
                        new EventSink(ironElectricityModelURI,
                                      EnableEnergySavingModeIron.class)
                });
        imported.put(
                EnableLinenModeIron.class,
                new EventSink[]{
                        new EventSink(ironElectricityModelURI,
                                      EnableLinenModeIron.class)
                });
        imported.put(
                DisableSteamModeIron.class,
                new EventSink[]{
                        new EventSink(ironElectricityModelURI,
                                      DisableSteamModeIron.class)
                });
        imported.put(
                TurnOnIron.class,
                new EventSink[]{
                        new EventSink(ironElectricityModelURI,
                                      TurnOnIron.class)
                });
        imported.put(
                TurnOffIron.class,
                new EventSink[]{
                        new EventSink(ironElectricityModelURI,
                                      TurnOffIron.class)
                });

        Map<VariableSource,VariableSink[]> bindings = new HashMap<VariableSource,VariableSink[]>();
        bindings.put(
                new VariableSource("currentIntensity", Double.class, fridgeElectricityModelURI),
                new VariableSink[]{
                        new VariableSink("currentFridgeIntensity",
                                         Double.class,
                                         electricMeterElectricityModelURI)
                });
        bindings.put(
                new VariableSource("currentIntensity", Double.class, ironElectricityModelURI),
                new VariableSink[]{
                        new VariableSink("currentIronIntensity",
                                         Double.class,
                                         electricMeterElectricityModelURI)
                });

        coupledModelDescriptors.put(
                electricMeterElectricityModelURI,
                new RTCoupledHIOA_Descriptor(
                        MeterCoupledModel.class,
                        electricMeterCoupledModelURI,
                        submodels,
                        imported,
                        null,
                        null,
                        null,
                        null,
                        null,
                        bindings,
                        new HIOA_Composer(),
                        accelerationFactor));

        Architecture architecture = new RTArchitecture(
                architectureURI,
                electricMeterCoupledModelURI,
                atomicModelDescriptors,
                coupledModelDescriptors,
                simulatedTimeUnit,
                accelerationFactor);

        return architecture;
    }
}
