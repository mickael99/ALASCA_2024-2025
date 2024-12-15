package fr.sorbonne_u.components.equipments.iron.sil;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import fr.sorbonne_u.components.equipments.iron.mil.IronCoupledModel;
import fr.sorbonne_u.components.equipments.iron.mil.IronElectricityModel;
import fr.sorbonne_u.components.equipments.iron.mil.IronUserModel;
import fr.sorbonne_u.components.equipments.iron.mil.events.DisableEnergySavingModeIron;
import fr.sorbonne_u.components.equipments.iron.mil.events.DisableSteamModeIron;
import fr.sorbonne_u.components.equipments.iron.mil.events.EnableCottonModeIron;
import fr.sorbonne_u.components.equipments.iron.mil.events.EnableDelicateModeIron;
import fr.sorbonne_u.components.equipments.iron.mil.events.EnableEnergySavingModeIron;
import fr.sorbonne_u.components.equipments.iron.mil.events.EnableLinenModeIron;
import fr.sorbonne_u.components.equipments.iron.mil.events.EnableSteamModeIron;
import fr.sorbonne_u.components.equipments.iron.mil.events.TurnOffIron;
import fr.sorbonne_u.components.equipments.iron.mil.events.TurnOnIron;
import fr.sorbonne_u.devs_simulation.architectures.Architecture;
import fr.sorbonne_u.devs_simulation.architectures.RTArchitecture;
import fr.sorbonne_u.devs_simulation.hioa.architectures.AtomicHIOA_Descriptor;
import fr.sorbonne_u.devs_simulation.models.architectures.AbstractAtomicModelDescriptor;
import fr.sorbonne_u.devs_simulation.models.architectures.AtomicModelDescriptor;
import fr.sorbonne_u.devs_simulation.models.architectures.CoupledModelDescriptor;
import fr.sorbonne_u.devs_simulation.models.architectures.RTAtomicModelDescriptor;
import fr.sorbonne_u.devs_simulation.models.architectures.RTCoupledModelDescriptor;
import fr.sorbonne_u.devs_simulation.models.events.EventI;
import fr.sorbonne_u.devs_simulation.models.events.EventSink;
import fr.sorbonne_u.devs_simulation.models.events.EventSource;
import fr.sorbonne_u.exceptions.PreconditionException;

public class LocalSimulationArchitectures {

	public static Architecture createIronMILLocalArchitecture4UnitTest(String architectureURI, 
																		TimeUnit simulatedTimeUnit) throws Exception 
	{
			Map<String,AbstractAtomicModelDescriptor> atomicModelDescriptors =
																new HashMap<>();

			atomicModelDescriptors.put(
					IronElectricityModel.MIL_URI,
					AtomicHIOA_Descriptor.create(
							IronElectricityModel.class,
							IronElectricityModel.MIL_URI,
							simulatedTimeUnit,
							null));

			atomicModelDescriptors.put(
					IronStateModel.MIL_URI,
					AtomicModelDescriptor.create(
							IronStateModel.class,
							IronStateModel.MIL_URI,
							simulatedTimeUnit,
							null));

			Map<String,CoupledModelDescriptor> coupledModelDescriptors = new HashMap<>();

			Set<String> submodels = new HashSet<String>();
			submodels.add(IronElectricityModel.MIL_URI);
			submodels.add(IronStateModel.MIL_URI);

			Map<Class<? extends EventI>,EventSink[]> imported = new HashMap<Class<? extends EventI>,EventSink[]>();

			imported.put(
				TurnOnIron.class,
				new EventSink[] {
					new EventSink(IronStateModel.MIL_URI,
								  TurnOnIron.class)
				});
			imported.put(
					TurnOffIron.class,
					new EventSink[] {
						new EventSink(IronStateModel.MIL_URI,
								TurnOffIron.class)
					});
			imported.put(
					DisableEnergySavingModeIron.class,
					new EventSink[] {
						new EventSink(IronStateModel.MIL_URI,
								DisableEnergySavingModeIron.class)
					});
			imported.put(
					DisableSteamModeIron.class,
					new EventSink[] {
						new EventSink(IronStateModel.MIL_URI,
								DisableSteamModeIron.class)
					});
			imported.put(
					EnableCottonModeIron.class,
					new EventSink[] {
						new EventSink(IronStateModel.MIL_URI,
								EnableCottonModeIron.class)
					});
			imported.put(
					EnableDelicateModeIron.class,
					new EventSink[] {
						new EventSink(IronStateModel.MIL_URI,
								EnableDelicateModeIron.class)
					});
			imported.put(
					EnableEnergySavingModeIron.class,
					new EventSink[] {
						new EventSink(IronStateModel.MIL_URI,
								EnableEnergySavingModeIron.class)
					});
			imported.put(
					EnableLinenModeIron.class,
					new EventSink[] {
						new EventSink(IronStateModel.MIL_URI,
								EnableLinenModeIron.class)
					});
			imported.put(
					EnableSteamModeIron.class,
					new EventSink[] {
						new EventSink(IronStateModel.MIL_URI,
								EnableSteamModeIron.class)
					});

			Map<EventSource,EventSink[]> connections =
										new HashMap<EventSource,EventSink[]>();

			connections.put(
				new EventSource(IronStateModel.MIL_URI,
						TurnOnIron.class),
				new EventSink[] {
					new EventSink(IronElectricityModel.MIL_URI,
							TurnOnIron.class)
				});
			connections.put(
				new EventSource(IronStateModel.MIL_URI,
						TurnOffIron.class),
				new EventSink[] {
					new EventSink(IronElectricityModel.MIL_URI,
							TurnOffIron.class)
				});
			connections.put(
				new EventSource(IronStateModel.MIL_URI,
						DisableEnergySavingModeIron.class),
				new EventSink[] {
					new EventSink(IronElectricityModel.MIL_URI,
								DisableEnergySavingModeIron.class)
				});
			connections.put(
				new EventSource(IronStateModel.MIL_URI,
						DisableSteamModeIron.class),
				new EventSink[] {
					new EventSink(IronElectricityModel.MIL_URI,
							DisableSteamModeIron.class)
				});
			connections.put(
					new EventSource(IronStateModel.MIL_URI,
							EnableCottonModeIron.class),
					new EventSink[] {
						new EventSink(IronElectricityModel.MIL_URI,
								EnableCottonModeIron.class)
					});
			connections.put(
					new EventSource(IronStateModel.MIL_URI,
							EnableDelicateModeIron.class),
					new EventSink[] {
						new EventSink(IronElectricityModel.MIL_URI,
								EnableDelicateModeIron.class)
					});
			connections.put(
					new EventSource(IronStateModel.MIL_URI,
							EnableLinenModeIron.class),
					new EventSink[] {
						new EventSink(IronElectricityModel.MIL_URI,
								EnableLinenModeIron.class)
					});
			connections.put(
					new EventSource(IronStateModel.MIL_URI,
							EnableEnergySavingModeIron.class),
					new EventSink[] {
						new EventSink(IronElectricityModel.MIL_URI,
								EnableEnergySavingModeIron.class)
					});
			connections.put(
					new EventSource(IronStateModel.MIL_URI,
							EnableSteamModeIron.class),
					new EventSink[] {
						new EventSink(IronElectricityModel.MIL_URI,
								EnableSteamModeIron.class)
					});


			coupledModelDescriptors.put(
					IronCoupledModel.MIL_URI,
					new CoupledModelDescriptor(
							IronCoupledModel.class,
							IronCoupledModel.MIL_URI,
							submodels,
							imported,
							null,
							connections,
							null));

			Architecture architecture =
					new Architecture(
							architectureURI,
							IronCoupledModel.MIL_URI,
							atomicModelDescriptors,
							coupledModelDescriptors,
							simulatedTimeUnit);

			return architecture;
		}
	
		
	public static Architecture createIronMIL_RT_Architecture4UnitTest(
			String architectureURI, 
			TimeUnit simulatedTimeUnit,
			double accelerationFactor
			) throws Exception
	{
		Map<String,AbstractAtomicModelDescriptor> atomicModelDescriptors =
				new HashMap<>();


		atomicModelDescriptors.put(
			IronElectricityModel.MIL_RT_URI,
			AtomicHIOA_Descriptor.create(
				IronElectricityModel.class,
				IronElectricityModel.MIL_RT_URI,
				simulatedTimeUnit,
				null));
		
		atomicModelDescriptors.put(
			IronStateModel.MIL_RT_URI,
			AtomicModelDescriptor.create(
				IronStateModel.class,
				IronStateModel.MIL_RT_URI,
				simulatedTimeUnit,
				null));
		
		Map<String,CoupledModelDescriptor> coupledModelDescriptors = new HashMap<>();
		
		Set<String> submodels = new HashSet<String>();
		submodels.add(IronElectricityModel.MIL_RT_URI);
		submodels.add(IronStateModel.MIL_RT_URI);
		
		Map<Class<? extends EventI>,EventSink[]> imported = new HashMap<Class<? extends EventI>,EventSink[]>();
		
		imported.put(
			TurnOnIron.class,
			new EventSink[] {
			new EventSink(IronStateModel.MIL_RT_URI,
				TurnOnIron.class)
		});
		
		imported.put(
			TurnOffIron.class,
			new EventSink[] {
			new EventSink(IronStateModel.MIL_RT_URI,
					TurnOffIron.class)
		});
		imported.put(
			DisableEnergySavingModeIron.class,
			new EventSink[] {
			new EventSink(IronStateModel.MIL_RT_URI,
					DisableEnergySavingModeIron.class)
		});
		imported.put(
			DisableSteamModeIron.class,
			new EventSink[] {
			new EventSink(IronStateModel.MIL_RT_URI,
					DisableSteamModeIron.class)
		});
		imported.put(
			EnableCottonModeIron.class,
			new EventSink[] {
			new EventSink(IronStateModel.MIL_RT_URI,
					EnableCottonModeIron.class)
		});
		imported.put(
			EnableDelicateModeIron.class,
			new EventSink[] {
			new EventSink(IronStateModel.MIL_RT_URI,
					EnableDelicateModeIron.class)
		});
		imported.put(
			EnableEnergySavingModeIron.class,
			new EventSink[] {
			new EventSink(IronStateModel.MIL_RT_URI,
					EnableEnergySavingModeIron.class)
		});
		imported.put(
			EnableLinenModeIron.class,
			new EventSink[] {
			new EventSink(IronStateModel.MIL_RT_URI,
					EnableLinenModeIron.class)
		});
		imported.put(
			EnableSteamModeIron.class,
			new EventSink[] {
			new EventSink(IronStateModel.MIL_RT_URI,
					EnableSteamModeIron.class)
		});
		
		Map<EventSource,EventSink[]> connections =
		new HashMap<EventSource,EventSink[]>();
		
		connections.put(
			new EventSource(IronStateModel.MIL_RT_URI,
					TurnOnIron.class),
			new EventSink[] {
				new EventSink(IronElectricityModel.MIL_RT_URI,
						TurnOnIron.class)
		});
		connections.put(
			new EventSource(IronStateModel.MIL_RT_URI,
					TurnOffIron.class),
			new EventSink[] {
				new EventSink(IronElectricityModel.MIL_RT_URI,
						TurnOffIron.class)
		});
		connections.put(
			new EventSource(IronStateModel.MIL_RT_URI,
					DisableEnergySavingModeIron.class),
			new EventSink[] {
					new EventSink(IronElectricityModel.MIL_RT_URI,
							DisableEnergySavingModeIron.class)
		});
		connections.put(
			new EventSource(IronStateModel.MIL_RT_URI,
					DisableSteamModeIron.class),
			new EventSink[] {
				new EventSink(IronElectricityModel.MIL_RT_URI,
						DisableSteamModeIron.class)
		});
		connections.put(
			new EventSource(IronStateModel.MIL_RT_URI,
					EnableCottonModeIron.class),
			new EventSink[] {
				new EventSink(IronElectricityModel.MIL_RT_URI,
						EnableCottonModeIron.class)
		});
		connections.put(
			new EventSource(IronStateModel.MIL_RT_URI,
					EnableDelicateModeIron.class),
			new EventSink[] {
				new EventSink(IronElectricityModel.MIL_RT_URI,
						EnableDelicateModeIron.class)
		});
		connections.put(
			new EventSource(IronStateModel.MIL_RT_URI,
					EnableLinenModeIron.class),
			new EventSink[] {
				new EventSink(IronElectricityModel.MIL_RT_URI,
						EnableLinenModeIron.class)
		});
		connections.put(
			new EventSource(IronStateModel.MIL_RT_URI,
					EnableEnergySavingModeIron.class),
			new EventSink[] {
				new EventSink(IronElectricityModel.MIL_RT_URI,
						EnableEnergySavingModeIron.class)
		});
		connections.put(
			new EventSource(IronStateModel.MIL_RT_URI,
					EnableSteamModeIron.class),
			new EventSink[] {
				new EventSink(IronElectricityModel.MIL_RT_URI,
						EnableSteamModeIron.class)
		});
		
		
		coupledModelDescriptors.put(
			IronCoupledModel.MIL_RT_URI,
			new RTCoupledModelDescriptor(
				IronCoupledModel.class,
				IronCoupledModel.MIL_RT_URI,
				submodels,
				imported,
				null,
				connections,
				null,
				accelerationFactor));
		
		Architecture architecture =
			new RTArchitecture(
				architectureURI,
				IronCoupledModel.MIL_URI,
				atomicModelDescriptors,
				coupledModelDescriptors,
				simulatedTimeUnit,
				accelerationFactor);
		
		return architecture;
	}
	
	public static Architecture createIronMILArchitecture4IntegrationTest(
			String architectureURI, 
			TimeUnit simulatedTimeUnit
			) throws Exception 
	{
		Map<String,AbstractAtomicModelDescriptor> atomicModelDescriptors =
				new HashMap<>();

		atomicModelDescriptors.put(
			IronStateModel.MIL_URI,
			AtomicModelDescriptor.create(
				IronStateModel.class,
				IronStateModel.MIL_URI,
				simulatedTimeUnit,
				null));
		
		Map<String,CoupledModelDescriptor> coupledModelDescriptors =
						new HashMap<>();
		
		Architecture architecture =
		new Architecture(
			architectureURI,
			IronStateModel.MIL_URI,
			atomicModelDescriptors,
			coupledModelDescriptors,
			simulatedTimeUnit);
		
		return architecture;
	}
	
	public static Architecture	createIronMIL_RT_Architecture4IntegrationTest(
			String architectureURI, 
			TimeUnit simulatedTimeUnit,
			double accelerationFactor
			) throws Exception
	{
		assert	accelerationFactor > 0.0 :
			new PreconditionException("accelerationFactor > 0.0");

		Map<String,AbstractAtomicModelDescriptor> atomicModelDescriptors =
															new HashMap<>();
	
		atomicModelDescriptors.put(
				IronStateModel.MIL_RT_URI,
				RTAtomicModelDescriptor.create(
						IronStateModel.class,
						IronStateModel.MIL_RT_URI,
						simulatedTimeUnit,
						null,
						accelerationFactor));
	
		Map<String,CoupledModelDescriptor> coupledModelDescriptors =
															new HashMap<>();
	
		Architecture architecture =
				new RTArchitecture(
						architectureURI,
						IronStateModel.MIL_RT_URI,
						atomicModelDescriptors,
						coupledModelDescriptors,
						simulatedTimeUnit,
						accelerationFactor);
	
		return architecture;
	}
	
	public static Architecture	createIronUserMIL_Architecture(
			String architectureURI, 
			TimeUnit simulatedTimeUnit
			) throws Exception
	{
		Map<String,AbstractAtomicModelDescriptor> atomicModelDescriptors =
															new HashMap<>();

		atomicModelDescriptors.put(
				IronUserModel.MIL_URI,
				AtomicModelDescriptor.create(
						IronUserModel.class,
						IronUserModel.MIL_URI,
						simulatedTimeUnit,
						null));

		Map<String,CoupledModelDescriptor> coupledModelDescriptors =
															new HashMap<>();

		Architecture architecture =
				new Architecture(
						architectureURI,
						IronUserModel.MIL_URI,
						atomicModelDescriptors,
						coupledModelDescriptors,
						simulatedTimeUnit);

		return architecture;
	}
	
	public static Architecture	createIronUserMIL_RT_Architecture(
			String architectureURI, 
			TimeUnit simulatedTimeUnit,
			double accelerationFactor
			) throws Exception
	{
		assert	accelerationFactor > 0.0 :
				new PreconditionException("accelerationFactor > 0.0");

		Map<String,AbstractAtomicModelDescriptor> atomicModelDescriptors =
															new HashMap<>();
		
		atomicModelDescriptors.put(
				IronUserModel.MIL_RT_URI,
				RTAtomicModelDescriptor.create(
						IronUserModel.class,
						IronUserModel.MIL_RT_URI,
						simulatedTimeUnit,
						null,
						accelerationFactor));

		Map<String,CoupledModelDescriptor> coupledModelDescriptors =
															new HashMap<>();

		Architecture architecture =
				new RTArchitecture(
						architectureURI,
						IronUserModel.MIL_RT_URI,
						atomicModelDescriptors,
						coupledModelDescriptors,
						simulatedTimeUnit,
						accelerationFactor);

		return architecture;
	}
}
