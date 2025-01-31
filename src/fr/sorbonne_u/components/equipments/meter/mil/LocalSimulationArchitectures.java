package fr.sorbonne_u.components.equipments.meter.mil;


import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import fr.sorbonne_u.components.equipments.battery.mil.BatteryElectricityModel;
import fr.sorbonne_u.components.equipments.battery.mil.events.SetConsumeBatteryEvent;
import fr.sorbonne_u.components.equipments.battery.mil.events.SetProductBatteryEvent;
import fr.sorbonne_u.components.equipments.battery.mil.events.SetStandByBatteryEvent;
import fr.sorbonne_u.components.equipments.fridge.mil.FridgeElectricityModel;
import fr.sorbonne_u.components.equipments.fridge.mil.events.CloseDoorFridge;
import fr.sorbonne_u.components.equipments.fridge.mil.events.CoolFridge;
import fr.sorbonne_u.components.equipments.fridge.mil.events.DoNotCoolFridge;
import fr.sorbonne_u.components.equipments.fridge.mil.events.OpenDoorFridge;
import fr.sorbonne_u.components.equipments.fridge.mil.events.SetPowerFridge;
import fr.sorbonne_u.components.equipments.fridge.mil.events.SwitchOffFridge;
import fr.sorbonne_u.components.equipments.fridge.mil.events.SwitchOnFridge;
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
import fr.sorbonne_u.components.equipments.windTurbine.mil.ExternalWindModel;
import fr.sorbonne_u.components.equipments.windTurbine.mil.WindTurbineElectricityModel;
import fr.sorbonne_u.components.equipments.windTurbine.mil.events.StartWindTurbineEvent;
import fr.sorbonne_u.components.equipments.windTurbine.mil.events.StopWindTurbineEvent;
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
		
		atomicModelDescriptors.put(
				WindTurbineElectricityModel.MIL_URI,
				AtomicHIOA_Descriptor.create(
						WindTurbineElectricityModel.class,
						WindTurbineElectricityModel.MIL_URI,
						simulatedTimeUnit,
						null));
		
		atomicModelDescriptors.put(
				ExternalWindModel.MIL_URI,
				AtomicHIOA_Descriptor.create(
						ExternalWindModel.class,
						ExternalWindModel.MIL_URI,
						simulatedTimeUnit,
						null));
		
		atomicModelDescriptors.put(
				BatteryElectricityModel.MIL_URI,
				AtomicHIOA_Descriptor.create(
						BatteryElectricityModel.class,
						BatteryElectricityModel.MIL_URI,
						simulatedTimeUnit,
						null));

		Map<String,CoupledModelDescriptor> coupledModelDescriptors =
				new HashMap<>();

		Set<String> submodels = new HashSet<String>();
		submodels.add(ElectricMeterElectricityModel.MIL_URI);
		submodels.add(FridgeElectricityModel.MIL_URI);
		submodels.add(IronElectricityModel.MIL_URI);
		submodels.add(WindTurbineElectricityModel.MIL_URI);
		submodels.add(ExternalWindModel.MIL_URI);
		submodels.add(BatteryElectricityModel.MIL_URI);

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
		
		// Wind turbine
		imported.put(
				StartWindTurbineEvent.class,
				new EventSink[] {
					new EventSink(WindTurbineElectricityModel.MIL_URI,
							StartWindTurbineEvent.class)
				});
		imported.put(
				StopWindTurbineEvent.class,
				new EventSink[] {
					new EventSink(WindTurbineElectricityModel.MIL_URI,
							StopWindTurbineEvent.class)
				});
		
		// Battery
		imported.put(
				SetConsumeBatteryEvent.class,
				new EventSink[] {
					new EventSink(BatteryElectricityModel.MIL_URI,
							SetConsumeBatteryEvent.class)
				});
		imported.put(
				SetProductBatteryEvent.class,
				new EventSink[] {
					new EventSink(BatteryElectricityModel.MIL_URI,
							SetProductBatteryEvent.class)
				});
		imported.put(
				SetStandByBatteryEvent.class,
				new EventSink[] {
					new EventSink(BatteryElectricityModel.MIL_URI,
							SetStandByBatteryEvent.class)
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
		bindings.put(
				new VariableSource("currentProduction",
								   Double.class,
								   WindTurbineElectricityModel.MIL_URI),
				new VariableSink[] {
					new VariableSink("currentWindTurbineProduction",
									 Double.class,
									 ElectricMeterElectricityModel.MIL_URI)
				});
		bindings.put(
				new VariableSource("currentProduction",
								   Double.class,
								   BatteryElectricityModel.MIL_URI),
				new VariableSink[] {
					new VariableSink("currentBatteryProduction",
									 Double.class,
									 ElectricMeterElectricityModel.MIL_URI)
				});
		bindings.put(
				new VariableSource("currentConsumption",
								   Double.class,
								   BatteryElectricityModel.MIL_URI),
				new VariableSink[] {
					new VariableSink("currentBatteryConsumption",
									 Double.class,
									 ElectricMeterElectricityModel.MIL_URI)
				});
		bindings.put(
				new VariableSource("externalWindSpeed",
								   Double.class,
								   ExternalWindModel.MIL_URI),
				new VariableSink[] {
					new VariableSink("externalWindSpeed",
									 Double.class,
									 WindTurbineElectricityModel.MIL_URI)
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
		String ironElectricityModelURI = null;
		String fridgeElectricityModelURI = null;
		String windTurbineElectricityModelURI = null;
		String externalWindModelURI = null;
		String electricMeterCoupledModelURI = null;
		String batteryElectricityModelURI = null;
		switch (currentSimulationType) {
		case MIL_RT_SIMULATION:
			electricMeterElectricityModelURI = ElectricMeterElectricityModel.MIL_RT_URI;
			electricMeterElectricityModelClass = ElectricMeterElectricityModel.class;
			electricMeterCoupledModelURI = ElectricMeterCoupledModel.MIL_RT_URI;
			ironElectricityModelURI = IronElectricityModel.MIL_RT_URI;
			fridgeElectricityModelURI = FridgeElectricityModel.MIL_RT_URI;
			windTurbineElectricityModelURI = WindTurbineElectricityModel.MIL_RT_URI;
			externalWindModelURI = ExternalWindModel.MIL_RT_URI;
			batteryElectricityModelURI = BatteryElectricityModel.MIL_RT_URI;
			break;
		case SIL_SIMULATION:
			electricMeterElectricityModelURI = ElectricMeterElectricitySILModel.SIL_URI;
			electricMeterElectricityModelClass = ElectricMeterElectricitySILModel.class;
			electricMeterCoupledModelURI = ElectricMeterCoupledModel.SIL_URI;
			ironElectricityModelURI = IronElectricityModel.SIL_URI;
			fridgeElectricityModelURI = FridgeElectricityModel.SIL_URI;
			windTurbineElectricityModelURI = WindTurbineElectricityModel.SIL_URI;
			externalWindModelURI = ExternalWindModel.SIL_URI;
			batteryElectricityModelURI = BatteryElectricityModel.SIL_URI;
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
				ironElectricityModelURI,
				RTAtomicHIOA_Descriptor.create(
						IronElectricityModel.class,
						ironElectricityModelURI,
						simulatedTimeUnit,
						null,
						accelerationFactor));
		atomicModelDescriptors.put(
				fridgeElectricityModelURI,
				RTAtomicHIOA_Descriptor.create(
						FridgeElectricityModel.class,
						fridgeElectricityModelURI,
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
		atomicModelDescriptors.put(
				externalWindModelURI,
				RTAtomicHIOA_Descriptor.create(
						ExternalWindModel.class,
						externalWindModelURI,
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
		
		Map<String,CoupledModelDescriptor> coupledModelDescriptors =
				new HashMap<>();

		// the set of submodels of the coupled model, given by their URIs
		Set<String> submodels = new HashSet<String>();
		submodels.add(electricMeterElectricityModelURI);
		submodels.add(ironElectricityModelURI);
		submodels.add(fridgeElectricityModelURI);
		submodels.add(windTurbineElectricityModelURI);
		submodels.add(externalWindModelURI);
		submodels.add(batteryElectricityModelURI);

		Map<Class<? extends EventI>,EventSink[]> imported = new HashMap<>();
		
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

		
		// Fridge
		
		imported.put(
				CloseDoorFridge.class,
				new EventSink[] {
					new EventSink(fridgeElectricityModelURI,
							CloseDoorFridge.class)
				});
		imported.put(
				OpenDoorFridge.class,
				new EventSink[] {
					new EventSink(fridgeElectricityModelURI,
							OpenDoorFridge.class)
				});
		imported.put(
				DoNotCoolFridge.class,
				new EventSink[] {
					new EventSink(fridgeElectricityModelURI,
							DoNotCoolFridge.class)
				});
		imported.put(
				CoolFridge.class,
				new EventSink[] {
					new EventSink(fridgeElectricityModelURI,
							CoolFridge.class)
				});
		imported.put(
				SetPowerFridge.class,
				new EventSink[] {
					new EventSink(fridgeElectricityModelURI,
							SetPowerFridge.class)
				});
		imported.put(
				SwitchOffFridge.class,
				new EventSink[] {
					new EventSink(fridgeElectricityModelURI,
							SwitchOffFridge.class)
				});
		imported.put(
				SwitchOnFridge.class,
				new EventSink[] {
					new EventSink(fridgeElectricityModelURI,
							SwitchOnFridge.class)
				});
		
		// Wind turbine
		imported.put(
				StartWindTurbineEvent.class,
				new EventSink[] {
					new EventSink(windTurbineElectricityModelURI,
							StartWindTurbineEvent.class)
				});
		imported.put(
				StopWindTurbineEvent.class,
				new EventSink[] {
					new EventSink(windTurbineElectricityModelURI,
							StopWindTurbineEvent.class)
				});
		
		// Battery 
		imported.put(
				SetConsumeBatteryEvent.class,
				new EventSink[] {
					new EventSink(batteryElectricityModelURI,
							SetConsumeBatteryEvent.class)
				});
		imported.put(
				SetProductBatteryEvent.class,
				new EventSink[] {
					new EventSink(batteryElectricityModelURI,
							SetProductBatteryEvent.class)
				});
		imported.put(
				SetStandByBatteryEvent.class,
				new EventSink[] {
					new EventSink(batteryElectricityModelURI,
							SetStandByBatteryEvent.class)
				});

		Map<VariableSource,VariableSink[]> bindings =
								new HashMap<VariableSource,VariableSink[]>();
		bindings.put(
				new VariableSource("currentIntensity",
								   Double.class,
								   ironElectricityModelURI),
				new VariableSink[] {
					new VariableSink("currentIronIntensity",
									 Double.class,
									 electricMeterElectricityModelURI)
				});
		bindings.put(
				new VariableSource("currentIntensity",
								   Double.class,
								   fridgeElectricityModelURI),
				new VariableSink[] {
					new VariableSink("currentFridgeIntensity",
									 Double.class,
									 electricMeterElectricityModelURI)
				});
		bindings.put(
				new VariableSource("currentProduction",
								   Double.class,
								   windTurbineElectricityModelURI),
				new VariableSink[] {
					new VariableSink("currentWindTurbineProduction",
									 Double.class,
									 electricMeterElectricityModelURI)
				});
		bindings.put(
				new VariableSource("currentProduction",
								   Double.class,
								   batteryElectricityModelURI),
				new VariableSink[] {
					new VariableSink("currentBatteryProduction",
									 Double.class,
									 electricMeterElectricityModelURI)
				});
		bindings.put(
				new VariableSource("currentConsumption",
								   Double.class,
								   batteryElectricityModelURI),
				new VariableSink[] {
					new VariableSink("currentBatteryConsumption",
									 Double.class,
									 electricMeterElectricityModelURI)
				});
		bindings.put(
				new VariableSource("externalWindSpeed",
								   Double.class,
								   externalWindModelURI),
				new VariableSink[] {
					new VariableSink("externalWindSpeed",
									 Double.class,
									 windTurbineElectricityModelURI)
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
