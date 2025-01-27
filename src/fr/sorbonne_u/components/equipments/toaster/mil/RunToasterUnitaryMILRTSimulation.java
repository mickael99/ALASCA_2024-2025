package fr.sorbonne_u.components.equipments.toaster.mil;

import fr.sorbonne_u.components.equipments.toaster.mil.events.SetToasterBrowningLevel;
import fr.sorbonne_u.components.equipments.toaster.mil.events.TurnOffToaster;
import fr.sorbonne_u.components.equipments.toaster.mil.events.TurnOnToaster;
import fr.sorbonne_u.devs_simulation.architectures.Architecture;
import fr.sorbonne_u.devs_simulation.architectures.RTArchitecture;
import fr.sorbonne_u.devs_simulation.hioa.architectures.RTCoupledHIOA_Descriptor;
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

public class RunToasterUnitaryMILRTSimulation {
    /** the time unit used in the real time MIL simulations.			 	*/
    public static final TimeUnit TIME_UNIT = TimeUnit.HOURS;
    /** the acceleration factor used in the real time MIL simulations.	 	*/
    public static final double		ACCELERATION_FACTOR = 3600.0;

    public static void	main(String[] args){
        try {
            Map<String, AbstractAtomicModelDescriptor> atomicModelDescriptors =
                    new HashMap<>();
            atomicModelDescriptors.put(
                    ToasterStateModel.MIL_RT_URI,
                    RTAtomicModelDescriptor.create(
                            ToasterStateModel.class,
                            ToasterStateModel.MIL_RT_URI,
                            TIME_UNIT,
                            null,
                            ACCELERATION_FACTOR));
            atomicModelDescriptors.put(
                    ToasterElectricityModel.MIL_RT_URI,
                    RTAtomicModelDescriptor.create(
                            ToasterElectricityModel.class,
                            ToasterElectricityModel.MIL_RT_URI,
                            TIME_UNIT,
                            null,
                            ACCELERATION_FACTOR));
            atomicModelDescriptors.put(
                    ToasterUnitTesterModel.MIL_RT_URI
                    , RTAtomicModelDescriptor.create(
                            ToasterUnitTesterModel.class,
                            ToasterUnitTesterModel.MIL_RT_URI,
                            TIME_UNIT,
                            null,
                            ACCELERATION_FACTOR));

            Map<String, CoupledModelDescriptor> coupledModelDescriptors = new HashMap<>();

            // the set of submodels of the coupled model, given by their URIs
            Set<String> submodels = new HashSet<String>();
            submodels.add(ToasterStateModel.MIL_RT_URI);
            submodels.add(ToasterElectricityModel.MIL_RT_URI);
            submodels.add(ToasterUnitTesterModel.MIL_RT_URI);

            Map<EventSource, EventSink[]> connections =
                    new HashMap<EventSource, EventSink[]>();
            connections.put(
                    new EventSource(ToasterUnitTesterModel.MIL_RT_URI, SetToasterBrowningLevel.class),
                    new EventSink[]{
                            new EventSink(ToasterStateModel.MIL_RT_URI, SetToasterBrowningLevel.class)
                    }
                           );
            connections.put(
                    new EventSource(ToasterUnitTesterModel.MIL_RT_URI, TurnOnToaster.class),
                    new EventSink[]{
                            new EventSink(ToasterStateModel.MIL_RT_URI, TurnOnToaster.class)
                    }
                           );
            connections.put(
                    new EventSource(ToasterUnitTesterModel.MIL_RT_URI, TurnOffToaster.class),
                    new EventSink[]{
                            new EventSink(ToasterStateModel.MIL_RT_URI, TurnOffToaster.class)
                    }
                           );
            connections.put(
                    new EventSource(ToasterStateModel.MIL_RT_URI, TurnOffToaster.class),
                    new EventSink[]{
                            new EventSink(ToasterElectricityModel.MIL_RT_URI, TurnOffToaster.class)
                    }
                           );
            connections.put(
                    new EventSource(ToasterStateModel.MIL_RT_URI, SetToasterBrowningLevel.class),
                    new EventSink[]{
                            new EventSink(ToasterElectricityModel.MIL_RT_URI, SetToasterBrowningLevel.class)
                    }
                           );
            connections.put(
                    new EventSource(ToasterStateModel.MIL_RT_URI, TurnOnToaster.class),
                    new EventSink[]{
                            new EventSink(ToasterElectricityModel.MIL_RT_URI, TurnOnToaster.class)
                    }
                           );

            coupledModelDescriptors.put(
                    ToasterCoupledModel.MIL_RT_URI,
                    new RTCoupledHIOA_Descriptor(
                            ToasterCoupledModel.class,
                            ToasterCoupledModel.MIL_RT_URI,
                            submodels,
                            null,
                            null,
                            connections,
                            null,
                            null,
                            null,
                            null,
                            ACCELERATION_FACTOR
                    ));

            Architecture architecture = new RTArchitecture(
                    ToasterCoupledModel.MIL_RT_URI,
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
