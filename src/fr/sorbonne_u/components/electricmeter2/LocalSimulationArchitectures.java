package fr.sorbonne_u.components.electricmeter2;


import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import fr.sorbonne_u.components.equipments.fridge.mil.FridgeElectricityModel;
import fr.sorbonne_u.components.equipments.fridge.mil.events.CloseDoorFridge;
import fr.sorbonne_u.components.equipments.fridge.mil.events.CoolFridge;
import fr.sorbonne_u.components.equipments.fridge.mil.events.DoNotCoolFridge;
import fr.sorbonne_u.components.equipments.fridge.mil.events.OpenDoorFridge;
import fr.sorbonne_u.components.equipments.fridge.mil.events.SetPowerFridge;
import fr.sorbonne_u.components.equipments.fridge.mil.events.SwitchOffFridge;
import fr.sorbonne_u.components.equipments.fridge.mil.events.SwitchOnFridge;
import fr.sorbonne_u.components.equipments.hairdryer.mil.HairDryerElectricityModel;
import fr.sorbonne_u.components.equipments.hairdryer.mil.events.SetHighHairDryer;
import fr.sorbonne_u.components.equipments.hairdryer.mil.events.SetLowHairDryer;
import fr.sorbonne_u.components.equipments.hairdryer.mil.events.SwitchOffHairDryer;
import fr.sorbonne_u.components.equipments.hairdryer.mil.events.SwitchOnHairDryer;
import fr.sorbonne_u.components.equipments.heater.mil.HeaterElectricityModel;
import fr.sorbonne_u.components.equipments.heater.mil.events.DoNotHeat;
import fr.sorbonne_u.components.equipments.heater.mil.events.Heat;
import fr.sorbonne_u.components.equipments.heater.mil.events.SetPowerHeater;
import fr.sorbonne_u.components.equipments.heater.mil.events.SwitchOffHeater;
import fr.sorbonne_u.components.equipments.heater.mil.events.SwitchOnHeater;
import fr.sorbonne_u.components.equipments.iron.mil.IronElectricityModel;
import fr.sorbonne_u.components.equipments.iron.mil.events.DisableEnergySavingModeIron;
import fr.sorbonne_u.components.equipments.iron.mil.events.DisableSteamModeIron;
import fr.sorbonne_u.components.equipments.iron.mil.events.EnableCottonModeIron;
import fr.sorbonne_u.components.equipments.iron.mil.events.EnableDelicateModeIron;
import fr.sorbonne_u.components.equipments.iron.mil.events.EnableEnergySavingModeIron;
import fr.sorbonne_u.components.equipments.iron.mil.events.EnableLinenModeIron;
import fr.sorbonne_u.components.equipments.iron.mil.events.EnableSteamModeIron;
import fr.sorbonne_u.components.equipments.iron.mil.events.TurnOffIron;
import fr.sorbonne_u.components.equipments.iron.mil.events.TurnOnIron;
import fr.sorbonne_u.components.equipments.meter.mil.ElectricMeterCoupledModel;
import fr.sorbonne_u.components.utils.SimulationType;
import fr.sorbonne_u.devs_simulation.architectures.Architecture;
import fr.sorbonne_u.devs_simulation.architectures.RTArchitecture;
import fr.sorbonne_u.devs_simulation.hioa.architectures.AtomicHIOA_Descriptor;
import fr.sorbonne_u.devs_simulation.hioa.architectures.CoupledHIOA_Descriptor;
import fr.sorbonne_u.devs_simulation.hioa.architectures.HIOA_Composer;
import fr.sorbonne_u.devs_simulation.hioa.architectures.RTAtomicHIOA_Descriptor;
import fr.sorbonne_u.devs_simulation.hioa.architectures.RTCoupledHIOA_Descriptor;
import fr.sorbonne_u.devs_simulation.hioa.models.AtomicHIOA;
import fr.sorbonne_u.devs_simulation.hioa.models.vars.VariableSink;
import fr.sorbonne_u.devs_simulation.hioa.models.vars.VariableSource;
import fr.sorbonne_u.devs_simulation.models.architectures.AbstractAtomicModelDescriptor;
import fr.sorbonne_u.devs_simulation.models.architectures.CoupledModelDescriptor;
import fr.sorbonne_u.devs_simulation.models.events.EventI;
import fr.sorbonne_u.devs_simulation.models.events.EventSink;

public abstract class	LocalSimulationArchitectures
{
	public static Architecture	createElectricMeterMILArchitecture4IntegrationTests(
		String architectureURI, 
		TimeUnit simulatedTimeUnit
		) throws Exception
	{
		Map<String,AbstractAtomicModelDescriptor> atomicModelDescriptors =
				new HashMap<>();

		atomicModelDescriptors.put(
				ElectricMeterElectricityModel.MIL_URI,
				AtomicHIOA_Descriptor.create(
						ElectricMeterElectricityModel.class,
						ElectricMeterElectricityModel.MIL_URI,
						simulatedTimeUnit,
						null));
		
		atomicModelDescriptors.put(
				IronElectricityModel.MIL_URI,
				AtomicHIOA_Descriptor.create(
						IronElectricityModel.class,
						IronElectricityModel.MIL_URI,
						simulatedTimeUnit,
						null));
		
		atomicModelDescriptors.put(
				FridgeElectricityModel.MIL_URI,
				AtomicHIOA_Descriptor.create(
						FridgeElectricityModel.class,
						FridgeElectricityModel.MIL_URI,
						simulatedTimeUnit,
						null));

		Map<String,CoupledModelDescriptor> coupledModelDescriptors =
				new HashMap<>();

		Set<String> submodels = new HashSet<String>();
		submodels.add(ElectricMeterElectricityModel.MIL_URI);
		submodels.add(FridgeElectricityModel.MIL_URI);
		submodels.add(IronElectricityModel.MIL_URI);

		Map<Class<? extends EventI>,EventSink[]> imported = new HashMap<>();
		
		// Iron
		imported.put(
				DisableEnergySavingModeIron.class,
				new EventSink[] {
					new EventSink(IronElectricityModel.MIL_URI,
							DisableEnergySavingModeIron.class)
				});
		imported.put(
				DisableSteamModeIron.class,
				new EventSink[] {
					new EventSink(IronElectricityModel.MIL_URI,
							DisableSteamModeIron.class)
				});
		imported.put(
				EnableCottonModeIron.class,
				new EventSink[] {
					new EventSink(IronElectricityModel.MIL_URI,
							EnableCottonModeIron.class)
				});
		imported.put(
				EnableDelicateModeIron.class,
				new EventSink[] {
					new EventSink(IronElectricityModel.MIL_URI,
							EnableDelicateModeIron.class)
				});
		imported.put(
				EnableEnergySavingModeIron.class,
				new EventSink[] {
					new EventSink(IronElectricityModel.MIL_URI,
							EnableEnergySavingModeIron.class)
				});
		imported.put(
				EnableLinenModeIron.class,
				new EventSink[] {
					new EventSink(IronElectricityModel.MIL_URI,
							EnableLinenModeIron.class)
				});
		imported.put(
				EnableSteamModeIron.class,
				new EventSink[] {
					new EventSink(IronElectricityModel.MIL_URI,
							EnableSteamModeIron.class)
				});
		imported.put(
				TurnOffIron.class,
				new EventSink[] {
					new EventSink(IronElectricityModel.MIL_URI,
							TurnOffIron.class)
				});
		imported.put(
				TurnOnIron.class,
				new EventSink[] {
					new EventSink(IronElectricityModel.MIL_URI,
							TurnOnIron.class)
				});
		
		
		// Fridge
		imported.put(
				CloseDoorFridge.class,
				new EventSink[] {
					new EventSink(FridgeElectricityModel.MIL_URI,
							CloseDoorFridge.class)
				});
		imported.put(
				OpenDoorFridge.class,
				new EventSink[] {
					new EventSink(FridgeElectricityModel.MIL_URI,
							OpenDoorFridge.class)
				});
		imported.put(
				DoNotCoolFridge.class,
				new EventSink[] {
					new EventSink(FridgeElectricityModel.MIL_URI,
							DoNotCoolFridge.class)
				});
		imported.put(
				CoolFridge.class,
				new EventSink[] {
					new EventSink(FridgeElectricityModel.MIL_URI,
							CoolFridge.class)
				});
		imported.put(
				SetPowerFridge.class,
				new EventSink[] {
					new EventSink(FridgeElectricityModel.MIL_URI,
							SetPowerFridge.class)
				});
		imported.put(
				SwitchOffFridge.class,
				new EventSink[] {
					new EventSink(FridgeElectricityModel.MIL_URI,
							SwitchOffFridge.class)
				});
		imported.put(
				SwitchOnFridge.class,
				new EventSink[] {
					new EventSink(FridgeElectricityModel.MIL_URI,
							SwitchOnFridge.class)
				});

		Map<VariableSource,VariableSink[]> bindings =
								new HashMap<VariableSource,VariableSink[]>();
								
		bindings.put(
				new VariableSource("currentIntensity",
								   Double.class,
								   IronElectricityModel.MIL_URI),
				new VariableSink[] {
					new VariableSink("currentIronIntensity",
									 Double.class,
									 ElectricMeterElectricityModel.MIL_URI)
				});
		bindings.put(
				new VariableSource("currentIntensity",
								   Double.class,
								   FridgeElectricityModel.MIL_URI),
				new VariableSink[] {
					new VariableSink("currentFridgeIntensity",
									 Double.class,
									 ElectricMeterElectricityModel.MIL_URI)
				});

		coupledModelDescriptors.put(
				ElectricMeterCoupledModel.MIL_URI,
				new CoupledHIOA_Descriptor(
						ElectricMeterCoupledModel.class,
						ElectricMeterCoupledModel.MIL_URI,
						submodels,
						imported,
						null,
						null,
						null,
						null,
						null,
						bindings,
						new HIOA_Composer()));

		Architecture architecture =
				new Architecture(
						architectureURI,
						ElectricMeterCoupledModel.MIL_URI,
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
		) throws Exception
	{
		String electricMeterElectricityModelURI = null;
		Class<? extends AtomicHIOA> electricMeterElectricityModelClass = null;
		String hairDryerElectricityModelURI = null;
		String ironElectricityModelURI = null;
		String electricMeterCoupledModelURI = null;
		switch (currentSimulationType) {
		case MIL_RT_SIMULATION:
			electricMeterElectricityModelURI = ElectricMeterElectricityModel.MIL_RT_URI;
			electricMeterElectricityModelClass = ElectricMeterElectricityModel.class;
			hairDryerElectricityModelURI = HairDryerElectricityModel.MIL_RT_URI;
			electricMeterCoupledModelURI = ElectricMeterCoupledModel.MIL_RT_URI;
			ironElectricityModelURI = IronElectricityModel.MIL_RT_URI;
			break;
		case SIL_SIMULATION:
			electricMeterElectricityModelURI = ElectricMeterElectricitySILModel.SIL_URI;
			electricMeterElectricityModelClass = ElectricMeterElectricitySILModel.class;
			hairDryerElectricityModelURI = HairDryerElectricityModel.SIL_URI;
			electricMeterCoupledModelURI = ElectricMeterCoupledModel.SIL_URI;
			ironElectricityModelURI = IronElectricityModel.SIL_URI;
			break;
		default:
		}

		Map<String,AbstractAtomicModelDescriptor> atomicModelDescriptors =
				new HashMap<>();


		atomicModelDescriptors.put(
				electricMeterElectricityModelURI,
				RTAtomicHIOA_Descriptor.create(
						electricMeterElectricityModelClass,
						electricMeterElectricityModelURI,
						simulatedTimeUnit,
						null,
						accelerationFactor));
		atomicModelDescriptors.put(
				hairDryerElectricityModelURI,
				RTAtomicHIOA_Descriptor.create(
						HairDryerElectricityModel.class,
						hairDryerElectricityModelURI,
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

		// the set of submodels of the coupled model, given by their URIs
		Set<String> submodels = new HashSet<String>();
		submodels.add(electricMeterElectricityModelURI);
		submodels.add(hairDryerElectricityModelURI);
		submodels.add(ironElectricityModelURI);

		Map<Class<? extends EventI>,EventSink[]> imported = new HashMap<>();
		imported.put(
				SwitchOnHairDryer.class,
				new EventSink[] {
					new EventSink(hairDryerElectricityModelURI,
								  SwitchOnHairDryer.class)
				});
		imported.put(
				SwitchOffHairDryer.class,
				new EventSink[] {
					new EventSink(hairDryerElectricityModelURI,
								  SwitchOffHairDryer.class)
				});
		imported.put(
				SetLowHairDryer.class,
				new EventSink[] {
					new EventSink(hairDryerElectricityModelURI,
								  SetLowHairDryer.class)
				});
		imported.put(
				SetHighHairDryer.class,
				new EventSink[] {
					new EventSink(hairDryerElectricityModelURI,
								  SetHighHairDryer.class)
				});
		
		// Iron
		imported.put(
				DisableEnergySavingModeIron.class,
				new EventSink[] {
					new EventSink(ironElectricityModelURI,
							DisableEnergySavingModeIron.class)
				});
		imported.put(
				DisableSteamModeIron.class,
				new EventSink[] {
					new EventSink(ironElectricityModelURI,
							DisableSteamModeIron.class)
				});
		imported.put(
				EnableCottonModeIron.class,
				new EventSink[] {
					new EventSink(ironElectricityModelURI,
							EnableCottonModeIron.class)
				});
		imported.put(
				EnableDelicateModeIron.class,
				new EventSink[] {
					new EventSink(ironElectricityModelURI,
							EnableDelicateModeIron.class)
				});
		imported.put(
				EnableEnergySavingModeIron.class,
				new EventSink[] {
					new EventSink(ironElectricityModelURI,
							EnableEnergySavingModeIron.class)
				});
		imported.put(
				EnableLinenModeIron.class,
				new EventSink[] {
					new EventSink(ironElectricityModelURI,
							EnableLinenModeIron.class)
				});
		imported.put(
				EnableSteamModeIron.class,
				new EventSink[] {
					new EventSink(ironElectricityModelURI,
							EnableSteamModeIron.class)
				});
		imported.put(
				TurnOffIron.class,
				new EventSink[] {
					new EventSink(ironElectricityModelURI,
							TurnOffIron.class)
				});
		imported.put(
				TurnOnIron.class,
				new EventSink[] {
					new EventSink(ironElectricityModelURI,
							TurnOnIron.class)
				});



		Map<VariableSource,VariableSink[]> bindings =
								new HashMap<VariableSource,VariableSink[]>();
		bindings.put(
				new VariableSource("currentIntensity",
								   Double.class,
								   hairDryerElectricityModelURI),
				new VariableSink[] {
					new VariableSink("currentHairDryerIntensity",
									 Double.class,
									 electricMeterElectricityModelURI)
				});
		bindings.put(
				new VariableSource("currentIntensity",
								   Double.class,
								   ironElectricityModelURI),
				new VariableSink[] {
					new VariableSink("currentIronIntensity",
									 Double.class,
									 electricMeterElectricityModelURI)
				});
		

		coupledModelDescriptors.put(
				electricMeterCoupledModelURI,
				new RTCoupledHIOA_Descriptor(
						ElectricMeterCoupledModel.class,
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

		Architecture architecture =
				new RTArchitecture(
						architectureURI,
						electricMeterCoupledModelURI,
						atomicModelDescriptors,
						coupledModelDescriptors,
						simulatedTimeUnit,
						accelerationFactor);

		return architecture;
	}
}
