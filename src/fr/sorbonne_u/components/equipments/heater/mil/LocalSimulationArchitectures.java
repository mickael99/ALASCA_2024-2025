package fr.sorbonne_u.components.equipments.heater.mil;

// Copyright Jacques Malenfant, Sorbonne Universite.
// Jacques.Malenfant@lip6.fr
//
// This software is a computer program whose purpose is to provide a
// basic component programming model to program with components
// real time distributed applications in the Java programming language.
//
// This software is governed by the CeCILL-C license under French law and
// abiding by the rules of distribution of free software.  You can use,
// modify and/ or redistribute the software under the terms of the
// CeCILL-C license as circulated by CEA, CNRS and INRIA at the following
// URL "http://www.cecill.info".
//
// As a counterpart to the access to the source code and  rights to copy,
// modify and redistribute granted by the license, users are provided only
// with a limited warranty  and the software's author,  the holder of the
// economic rights,  and the successive licensors  have only  limited
// liability. 
//
// In this respect, the user's attention is drawn to the risks associated
// with loading,  using,  modifying and/or developing or reproducing the
// software by the user in light of its specific status of free software,
// that may mean  that it is complicated to manipulate,  and  that  also
// therefore means  that it is reserved for developers  and  experienced
// professionals having in-depth computer knowledge. Users are therefore
// encouraged to load and test the software's suitability as regards their
// requirements in conditions enabling the security of their systems and/or 
// data to be ensured and,  more generally, to use and operate it in the 
// same conditions as regards security. 
//
// The fact that you are presently reading this means that you have had
// knowledge of the CeCILL-C license and that you accept its terms.

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import fr.sorbonne_u.components.equipments.heater.mil.events.*;
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

// -----------------------------------------------------------------------------
/**
 * The class <code>LocalSimulationArchitectures</code> defines the local
 * MIL simulation architectures pertaining to the heater appliance.
 *
 * <p><strong>Description</strong></p>
 * 
 * <p>
 * The class provides static methods that create the local MIL, MIL real time
 * and SIL simulation architectures for the {@code Heater} and the
 * {@code HeaterUser} components. The overall simulation architecture
 * for the heater appliance can be seen as follows:
 * </p>
 * <p><img src="../../../../../../../../images/hem-2024-e3/HeaterMILModel.png"/></p> 
 * <p>
 * But, to fit this architecture onto separate components, it has to be split
 * into component local simulation architectures and then composed into a global
 * simulation architecture by the simulation supervisor component.
 * </p>
 * <p>
 * The simulation architectures created in this class are local to components
 * in the sense that they define the simulators that are created and run by
 * each component. These are integrated in more global component simulation
 * architectures where they are seen as atomic models that are composed into
 * coupled models that will reside in coordinator components.
 * </p>
 * <p>
 * As there is nothing to change to the simulation architectures of the
 * {@code Heater} component hair to go from MIL real time to SIL simulations,
 * the MIL real time architectures can be used to execute SIL simulations. For
 * the {@code HeaterUser}, there is no need for a simulator in SIL
 * simulations as its entire behaviour lies in its component code.
 * </p>
 * 
 * <p><strong>Glass-box Invariants</strong></p>
 * 
 * <pre>
 * invariant	{@code true}	// no more invariant
 * </pre>
 * 
 * <p><strong>Black-box Invariants</strong></p>
 * 
 * <pre>
 * invariant	{@code true}	// no more invariant
 * </pre>
 * 
 * <p>Created on : 2023-11-13</p>
 * 
 * @author	<a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
 */
public abstract class	LocalSimulationArchitectures
{
	/**
	 * create the local MIL simulation architecture for the {@code Heater}
	 * component.
	 * 
	 * <p><strong>Description</strong></p>
	 * 
	 * <p><img src="../../../../../../../../images/hem-2024-e3/HeaterUnitTestLocalArchitecture.png"/></p> 
	 * <p>
	 * In this simulation architecture, the heater simulator consists of five
	 * atomic models:
	 * </p>
	 * <ol>
	 * <li>The {@code HeaterStateModel} keeps track of the state (switched on,
	 *   switched off, etc.) of the heater and its current power level. The
	 *   state changes are triggered by the reception of external events through
	 *   {@code HeaterCoupledModel} that imports and transmit them; whenever a
	 *   state change occurs, the triggering event is reemitted towards the
	 *   {@code HeaterElectricityModel} and the {@code HeaterTemperatureModel}
	 *   (except for {@code SwitchOnHeater} that does not influence the
	 *   temperature model).</li>
	 * <li>The {@code HeaterElectricityModel} keeps track of the electric power
	 *   consumed by the heater in a variable <code>currentIntensity</code>,
	 *   which is exported but not used in this simulation of the heater in
	 *   isolation.</li>
	 * <li>The {@code ExternalTemperatureModel} simulates the temperature
	 *   outside the room, a part of the environment. The simulated temperature
	 *   is put in an exported variable {@code externalTemperature} that is
	 *   imported with the same name by the {@code HeaterTemperatureModel}.</li>
	 * <li>The {@code HeaterTemperatureModel} simulates the temperature inside
	 *   the heated room, using the external temperature provided by the
	 *   {@code ExternalTemperatureModel} and the current power of the heater,
	 *   which it keeps track of through the {@code SetPowerHeater} and
	 *   {@code SwitchOffHeater} events. The evolution of the inside temperature
	 *   also obviously depends upon the fact that the heater actually is
	 *   heating or not, a state which is kept track of through the events
	 *   {@code Heat} and {@code DoNotHeat}.</li>
	 * <li>The {@code HeaterUnitTesterModel} simulates a heater user and a
	 *   heater controller by emitting state changing events towards the
	 *   {@code HeaterStateModel}.</li>
	 * </ol>
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code true}	// no precondition.
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @param architectureURI	URI to be given to the created simulation architecture.
	 * @param simulatedTimeUnit	simulated time unit used in the architecture.
	 * @return					the local MIL simulation architecture for the {@code Heater} component.
	 * @throws Exception		<i>to do</i>.
	 */
	public static Architecture	createHeaterMILLocalArchitecture4UnitTest(
		String architectureURI,
		TimeUnit simulatedTimeUnit
		) throws Exception
	{
		// map that will contain the atomic model descriptors to construct
		// the simulation architecture
		Map<String,AbstractAtomicModelDescriptor> atomicModelDescriptors =
															new HashMap<>();

		atomicModelDescriptors.put(
				HeaterStateModel.MIL_URI,
				AtomicModelDescriptor.create(
						HeaterStateModel.class,
						HeaterStateModel.MIL_URI,
						simulatedTimeUnit,
						null));
		// the heater models simulating its electricity consumption, its
		// temperatures and the external temperature are atomic HIOA models
		// hence we use an AtomicHIOA_Descriptor(s)
		atomicModelDescriptors.put(
				ExternalTemperatureModel.MIL_URI,
				AtomicHIOA_Descriptor.create(
						ExternalTemperatureModel.class,
						ExternalTemperatureModel.MIL_URI,
						simulatedTimeUnit,
						null));
		atomicModelDescriptors.put(
				HeaterTemperatureModel.MIL_URI,
				AtomicHIOA_Descriptor.create(
						HeaterTemperatureModel.class,
						HeaterTemperatureModel.MIL_URI,
						simulatedTimeUnit,
						null));
		atomicModelDescriptors.put(
				HeaterElectricityModel.MIL_URI,
				AtomicHIOA_Descriptor.create(
						HeaterElectricityModel.class,
						HeaterElectricityModel.MIL_URI,
						simulatedTimeUnit,
						null));

		// map that will contain the coupled model descriptors to construct
		// the simulation architecture
		Map<String,CoupledModelDescriptor> coupledModelDescriptors =
															new HashMap<>();

		// the set of submodels of the coupled model, given by their URIs
		Set<String> submodels = new HashSet<String>();
		submodels.add(HeaterStateModel.MIL_URI);
		submodels.add(ExternalTemperatureModel.MIL_URI);
		submodels.add(HeaterTemperatureModel.MIL_URI);
		submodels.add(HeaterElectricityModel.MIL_URI);

		// events received by the coupled model transmitted to its submodels
		Map<Class<? extends EventI>, EventSink[]> imported = new HashMap<>();

		imported.put(
				SwitchOnHeater.class,
				new EventSink[] {
					new EventSink(HeaterStateModel.MIL_URI,
								  SwitchOnHeater.class)
				});
		imported.put(
				SetPowerHeater.class,
				new EventSink[] {
					new EventSink(HeaterStateModel.MIL_URI,
								  SetPowerHeater.class)
				});
		imported.put(
				SwitchOffHeater.class,
				new EventSink[] {
					new EventSink(HeaterStateModel.MIL_URI,
								  SwitchOffHeater.class)
				});
		imported.put(
				Heat.class,
				new EventSink[] {
					new EventSink(HeaterStateModel.MIL_URI, Heat.class)
				});
		imported.put(
				DoNotHeat.class,
				new EventSink[] {
					new EventSink(HeaterStateModel.MIL_URI, DoNotHeat.class)
				});

		// event exchanging connections between exporting and importing
		// models
		Map<EventSource,EventSink[]> connections =
									new HashMap<EventSource,EventSink[]>();

		connections.put(
				new EventSource(HeaterStateModel.MIL_URI,
								SwitchOnHeater.class),
				new EventSink[] {
						new EventSink(HeaterElectricityModel.MIL_URI,
									  SwitchOnHeater.class)
				});
		connections.put(
				new EventSource(HeaterStateModel.MIL_URI,
								SetPowerHeater.class),
				new EventSink[] {
						new EventSink(HeaterTemperatureModel.MIL_URI,
									  SetPowerHeater.class),
						new EventSink(HeaterElectricityModel.MIL_URI,
									  SetPowerHeater.class)
				});
		connections.put(
				new EventSource(HeaterStateModel.MIL_URI,
								SwitchOffHeater.class),
				new EventSink[] {
						new EventSink(HeaterTemperatureModel.MIL_URI,
									  SwitchOffHeater.class),
						new EventSink(HeaterElectricityModel.MIL_URI,
									  SwitchOffHeater.class)
				});
		connections.put(
				new EventSource(HeaterStateModel.MIL_URI, Heat.class),
				new EventSink[] {
						new EventSink(HeaterTemperatureModel.MIL_URI,
									  Heat.class),
						new EventSink(HeaterElectricityModel.MIL_URI,
									  Heat.class)
				});
		connections.put(
				new EventSource(HeaterStateModel.MIL_URI, DoNotHeat.class),
				new EventSink[] {
						new EventSink(HeaterTemperatureModel.MIL_URI,
									  DoNotHeat.class),
						new EventSink(HeaterElectricityModel.MIL_URI,
									  DoNotHeat.class)
				});

		// variable bindings between exporting and importing models
		Map<VariableSource,VariableSink[]> bindings =
							new HashMap<VariableSource,VariableSink[]>();

		bindings.put(new VariableSource("externalTemperature",
										Double.class,
										ExternalTemperatureModel.MIL_URI),
					 new VariableSink[] {
							 new VariableSink("externalTemperature",
									 		  Double.class,
									 		  HeaterTemperatureModel.MIL_URI)
					 });

		// coupled model descriptor
		coupledModelDescriptors.put(
				HeaterCoupledModel.MIL_URI,
				new CoupledHIOA_Descriptor(
						HeaterCoupledModel.class,
						HeaterCoupledModel.MIL_URI,
						submodels,
						imported,
						null,
						connections,
						null,
						null,
						null,
						bindings));

		// simulation architecture
		Architecture architecture =
				new Architecture(
						architectureURI,
						HeaterCoupledModel.MIL_URI,
						atomicModelDescriptors,
						coupledModelDescriptors,
						simulatedTimeUnit);

		return architecture;
	}

	/**
	 * create the local MIL real time or SIL simulation architecture for the
	 * {@code Heater} component when doing real time unit tests with simulation.
	 * 
	 * <p><strong>Description</strong></p>
	 * 
	 * <p>
	 * The simulation architecture created for {@code Heater} real time
	 * unit tests is the same as for non real time unit tests except that the
	 * simulation engines has to be real time, hence using the corresponding
	 * model and architecture descriptors. See
	 * {@code createHeaterMIL_ArchitectureUnitTest} for more detailed
	 * explanations about the architecture itself.
	 * </p>
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code currentSimulationType.isMILRTSimulation() || currentSimulationType.isSILSimulation()}
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @param currentSimulationType	simulation type for the next run.
	 * @param architectureURI		URI to be given to the created simulation architecture.
	 * @param simulatedTimeUnit		simulated time unit used in the architecture.
	 * @param accelerationFactor	acceleration factor used to execute in a logical time speeding up the real time.
	 * @return						the local MIL real time simulation architecture for the unit tests of the {@code Heater} component.
	 * @throws Exception			<i>to do</i>.
	 */
	public static Architecture	createHeater_RT_LocalArchitecture4UnitTest(
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

		String heaterStateModelURI = null;
		String heaterTemperatureModelURI = null;
		String externalTemperatureModelURI = null;
		String heaterElectricityModelURI = null;
		String heaterCoupledModelURI = null;
		switch (currentSimulationType) {
		case MIL_RT_SIMULATION:
			heaterStateModelURI = HeaterStateModel.MIL_RT_URI;
			heaterTemperatureModelURI = HeaterTemperatureModel.MIL_RT_URI;
			externalTemperatureModelURI = ExternalTemperatureModel.MIL_RT_URI;
			heaterElectricityModelURI = HeaterElectricityModel.MIL_RT_URI;
			heaterCoupledModelURI = HeaterCoupledModel.MIL_RT_URI;
			break;
		case SIL_SIMULATION:
			heaterStateModelURI = HeaterStateModel.SIL_URI;
			heaterTemperatureModelURI = HeaterTemperatureModel.SIL_URI;
			externalTemperatureModelURI = ExternalTemperatureModel.SIL_URI;
			heaterElectricityModelURI = HeaterElectricityModel.SIL_URI;
			heaterCoupledModelURI = HeaterCoupledModel.SIL_URI;
			break;
		default:
		}

		// map that will contain the atomic model descriptors to construct
		// the simulation architecture
		Map<String,AbstractAtomicModelDescriptor> atomicModelDescriptors =
															new HashMap<>();

		atomicModelDescriptors.put(
				heaterStateModelURI,
				RTAtomicModelDescriptor.create(
						HeaterStateModel.class,
						heaterStateModelURI,
						simulatedTimeUnit,
						null,
						accelerationFactor));
		// the heater models simulating its electricity consumption, its
		// temperatures and the external temperature are atomic HIOA models
		// hence we use an AtomicHIOA_Descriptor(s)
		atomicModelDescriptors.put(
				externalTemperatureModelURI,
				RTAtomicHIOA_Descriptor.create(
						ExternalTemperatureModel.class,
						externalTemperatureModelURI,
						simulatedTimeUnit,
						null,
						accelerationFactor));
		atomicModelDescriptors.put(
				heaterTemperatureModelURI,
				RTAtomicHIOA_Descriptor.create(
						HeaterTemperatureModel.class,
						heaterTemperatureModelURI,
						simulatedTimeUnit,
						null,
						accelerationFactor));
		atomicModelDescriptors.put(
				heaterElectricityModelURI,
				RTAtomicHIOA_Descriptor.create(
						HeaterElectricityModel.class,
						heaterElectricityModelURI,
						simulatedTimeUnit,
						null,
						accelerationFactor));

		// map that will contain the coupled model descriptors to construct
		// the simulation architecture
		Map<String,CoupledModelDescriptor> coupledModelDescriptors =
															new HashMap<>();

		// the set of submodels of the coupled model, given by their URIs
		Set<String> submodels = new HashSet<String>();
		submodels.add(heaterStateModelURI);
		submodels.add(externalTemperatureModelURI);
		submodels.add(heaterTemperatureModelURI);
		submodels.add(heaterElectricityModelURI);

		// events received by the coupled model transmitted to its submodels
		Map<Class<? extends EventI>, EventSink[]> imported = new HashMap<>();

		imported.put(
				SwitchOnHeater.class,
				new EventSink[] {
					new EventSink(heaterStateModelURI,
								  SwitchOnHeater.class)
				});
		imported.put(
				SetPowerHeater.class,
				new EventSink[] {
					new EventSink(heaterStateModelURI,
								  SetPowerHeater.class)
				});
		imported.put(
				SwitchOffHeater.class,
				new EventSink[] {
					new EventSink(heaterStateModelURI,
								  SwitchOffHeater.class)
				});
		imported.put(
				Heat.class,
				new EventSink[] {
					new EventSink(heaterStateModelURI, Heat.class)
				});
		imported.put(
				DoNotHeat.class,
				new EventSink[] {
					new EventSink(heaterStateModelURI, DoNotHeat.class)
				});

		// event exchanging connections between exporting and importing
		// models
		Map<EventSource,EventSink[]> connections =
									new HashMap<EventSource,EventSink[]>();

		connections.put(
				new EventSource(heaterStateModelURI,
								SwitchOnHeater.class),
				new EventSink[] {
						new EventSink(heaterElectricityModelURI,
									  SwitchOnHeater.class)
				});
		connections.put(
				new EventSource(heaterStateModelURI,
								SetPowerHeater.class),
				new EventSink[] {
						new EventSink(heaterTemperatureModelURI,
									  SetPowerHeater.class),
						new EventSink(heaterElectricityModelURI,
									  SetPowerHeater.class)
				});
		connections.put(
				new EventSource(heaterStateModelURI,
								SwitchOffHeater.class),
				new EventSink[] {
						new EventSink(heaterTemperatureModelURI,
									  SwitchOffHeater.class),
						new EventSink(heaterElectricityModelURI,
									  SwitchOffHeater.class)
				});
		connections.put(
				new EventSource(heaterStateModelURI, Heat.class),
				new EventSink[] {
						new EventSink(heaterTemperatureModelURI,
									  Heat.class),
						new EventSink(heaterElectricityModelURI,
									  Heat.class)
				});
		connections.put(
				new EventSource(heaterStateModelURI, DoNotHeat.class),
				new EventSink[] {
						new EventSink(heaterTemperatureModelURI,
									  DoNotHeat.class),
						new EventSink(heaterElectricityModelURI,
									  DoNotHeat.class)
				});

		// variable bindings between exporting and importing models
		Map<VariableSource,VariableSink[]> bindings =
							new HashMap<VariableSource,VariableSink[]>();

		bindings.put(new VariableSource("externalTemperature",
										Double.class,
										externalTemperatureModelURI),
					 new VariableSink[] {
							 new VariableSink("externalTemperature",
									 		  Double.class,
									 		  heaterTemperatureModelURI)
					 });

		// coupled model descriptor
		coupledModelDescriptors.put(
				heaterCoupledModelURI,
				new RTCoupledHIOA_Descriptor(
						HeaterCoupledModel.class,
						heaterCoupledModelURI,
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
						heaterCoupledModelURI,
						atomicModelDescriptors,
						coupledModelDescriptors,
						simulatedTimeUnit,
						accelerationFactor);

		return architecture;
	}

	/**
	 * create the local MIL simulation architecture for the tests of the
	 * {@code HeaterUser} component.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code true}	// no precondition.
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @param architectureURI		URI to be given to the created simulation architecture.
	 * @param simulatedTimeUnit		simulated time unit used in the architecture.
	 * @return						the local MIL simulation architecture for the unit tests of the {@code HeaterUser} component.
	 * @throws Exception			<i>to do</i>.
	 */
	public static Architecture	createHeaterUserMILLocalArchitecture(
		String architectureURI,
		TimeUnit simulatedTimeUnit
		) throws Exception
	{
		// map that will contain the atomic model descriptors to construct
		// the simulation architecture
		Map<String,AbstractAtomicModelDescriptor> atomicModelDescriptors =
															new HashMap<>();
		// the heater unit tester model only exchanges event, an
		// atomic model hence we use an AtomicModelDescriptor
		atomicModelDescriptors.put(
				HeaterUnitTesterModel.MIL_URI,
				AtomicModelDescriptor.create(
						HeaterUnitTesterModel.class,
						HeaterUnitTesterModel.MIL_URI,
						simulatedTimeUnit,
						null));
		// map that will contain the coupled model descriptors to construct
		// the simulation architecture
		Map<String,CoupledModelDescriptor> coupledModelDescriptors =
															new HashMap<>();


		// simulation architecture
		Architecture architecture =
				new Architecture(
						architectureURI,
						HeaterUnitTesterModel.MIL_URI,
						atomicModelDescriptors,
						coupledModelDescriptors,
						simulatedTimeUnit);

		return architecture;
	}

	/**
	 * create the local MIL real time simulation architecture for the tests
	 * of the {@code HeaterUser} component.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code true}	// no precondition.
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @param architectureURI		URI to be given to the created simulation architecture.
	 * @param simulatedTimeUnit		simulated time unit used in the architecture.
	 * @param accelerationFactor	acceleration factor used to execute in a logical time speeding up the real time.
	 * @return						the local MIL real time simulation architecture for the unit tests of the {@code HeaterUser} component.
	 * @throws Exception			<i>to do</i>.
	 */
	public static Architecture	createHeaterUserMILRT_LocalArchitecture(
		String architectureURI,
		TimeUnit simulatedTimeUnit,
		double accelerationFactor
		) throws Exception
	{
		// map that will contain the atomic model descriptors to construct
		// the simulation architecture
		Map<String,AbstractAtomicModelDescriptor> atomicModelDescriptors =
															new HashMap<>();
		// the heater unit tester model only exchanges event, an
		// atomic model hence we use an AtomicModelDescriptor
		atomicModelDescriptors.put(
				HeaterUnitTesterModel.MIL_RT_URI,
				RTAtomicModelDescriptor.create(
						HeaterUnitTesterModel.class,
						HeaterUnitTesterModel.MIL_RT_URI,
						simulatedTimeUnit,
						null,
						accelerationFactor));
		// map that will contain the coupled model descriptors to construct
		// the simulation architecture
		Map<String,CoupledModelDescriptor> coupledModelDescriptors =
															new HashMap<>();


		// simulation architecture
		Architecture architecture =
				new RTArchitecture(
						architectureURI,
						HeaterUnitTesterModel.MIL_RT_URI,
						atomicModelDescriptors,
						coupledModelDescriptors,
						simulatedTimeUnit,
						accelerationFactor);

		return architecture;
	}

	/**
	 * create the local MIL simulation architecture for the {@code Heater}
	 * component.
	 * 
	 * <p><strong>Description</strong></p>
	 * 
	 * <p><img src="../../../../../../../../images/hem-2024-e3/HeaterUnitTestLocalArchitecture.png"/></p> 
	 * <p>
	 * In this simulation architecture, the heater simulator consists of five
	 * atomic models:
	 * </p>
	 * <ol>
	 * <li>The {@code HeaterStateModel} keeps track of the state (switched on,
	 *   switched off, etc.) of the heater and its current power level. The
	 *   state changes are triggered by the reception of external events through
	 *   {@code HeaterCoupledModel} that imports and transmit them; whenever a
	 *   state change occurs, the triggering event is reemitted towards the
	 *   {@code HeaterElectricityModel} and the {@code HeaterTemperatureModel}
	 *   (except for {@code SwitchOnHeater} that does not influence the
	 *   temperature model).</li>
	 * <li>The {@code ExternalTemperatureModel} simulates the temperature
	 *   outside the room, a part of the environment. The simulated temperature
	 *   is put in an exported variable {@code externalTemperature} that is
	 *   imported with the same name by the {@code HeaterTemperatureModel}.</li>
	 * <li>The {@code HeaterTemperatureModel} simulates the temperature inside
	 *   the heated room, using the external temperature provided by the
	 *   {@code ExternalTemperatureModel} and the current power of the heater,
	 *   which it keeps track of through the {@code SetPowerHeater} and
	 *   {@code SwitchOffHeater} events. The evolution of the inside temperature
	 *   also obviously depends upon the fact that the heater actually is
	 *   heating or not, a state which is kept track of through the events
	 *   {@code Heat} and {@code DoNotHeat}.</li>
	 * <li>The {@code HeaterUnitTesterModel} simulates a heater user and a
	 *   heater controller by emitting state changing events towards the
	 *   {@code HairDryerStateModel}.</li>
	 * </ol>
	 * 
	 * <p>
	 * The {@code HeaterElectricityModel} keeps track of the electric power
	 * consumed by the heater in a variable <code>currentIntensity</code>,
	 * which is exported. As this variable is imported by the
	 * {@code ElectricMeterElectricityModel} in integration tests, the
	 * {@code HeaterElectricityModel} is created in the {@code ElectricMeter}
	 * component simulator and the events emitted by the {@code HeaterStateModel}
	 * are reexported by the {@code HeaterCoupledModel} towards the
	 * {@code ElectricMeterCoupledModel} model.
	 * </li>
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code true}	// no precondition.
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @param architectureURI	URI to be given to the created simulation architecture.
	 * @param simulatedTimeUnit	simulated time unit used in the architecture.
	 * @return					the local MIL simulation architecture for the {@code Heater} component.
	 * @throws Exception		<i>to do</i>.
	 */
	public static Architecture	createHeaterMILLocalArchitecture4IntegrationTest(
		String architectureURI,
		TimeUnit simulatedTimeUnit
		) throws Exception
	{
		// map that will contain the atomic model descriptors to construct
		// the simulation architecture
		Map<String,AbstractAtomicModelDescriptor> atomicModelDescriptors =
															new HashMap<>();

		atomicModelDescriptors.put(
				HeaterStateModel.MIL_URI,
				AtomicModelDescriptor.create(
						HeaterStateModel.class,
						HeaterStateModel.MIL_URI,
						simulatedTimeUnit,
						null));
		// the heater models simulating its electricity consumption, its
		// temperatures and the external temperature are atomic HIOA models
		// hence we use an AtomicHIOA_Descriptor(s)
		atomicModelDescriptors.put(
				ExternalTemperatureModel.MIL_URI,
				AtomicHIOA_Descriptor.create(
						ExternalTemperatureModel.class,
						ExternalTemperatureModel.MIL_URI,
						simulatedTimeUnit,
						null));
		atomicModelDescriptors.put(
				HeaterTemperatureModel.MIL_URI,
				AtomicHIOA_Descriptor.create(
						HeaterTemperatureModel.class,
						HeaterTemperatureModel.MIL_URI,
						simulatedTimeUnit,
						null));

		// map that will contain the coupled model descriptors to construct
		// the simulation architecture
		Map<String,CoupledModelDescriptor> coupledModelDescriptors =
															new HashMap<>();

		// the set of submodels of the coupled model, given by their URIs
		Set<String> submodels = new HashSet<String>();
		submodels.add(HeaterStateModel.MIL_URI);
		submodels.add(ExternalTemperatureModel.MIL_URI);
		submodels.add(HeaterTemperatureModel.MIL_URI);

		// events received by the coupled model transmitted to its submodels
		Map<Class<? extends EventI>, EventSink[]> imported = new HashMap<>();

		imported.put(
				SwitchOnHeater.class,
				new EventSink[] {
					new EventSink(HeaterStateModel.MIL_URI,
								  SwitchOnHeater.class)
				});
		imported.put(
				SetPowerHeater.class,
				new EventSink[] {
					new EventSink(HeaterStateModel.MIL_URI,
								  SetPowerHeater.class)
				});
		imported.put(
				SwitchOffHeater.class,
				new EventSink[] {
					new EventSink(HeaterStateModel.MIL_URI,
								  SwitchOffHeater.class)
				});
		imported.put(
				Heat.class,
				new EventSink[] {
					new EventSink(HeaterStateModel.MIL_URI, Heat.class)
				});
		imported.put(
				DoNotHeat.class,
				new EventSink[] {
					new EventSink(HeaterStateModel.MIL_URI, DoNotHeat.class)
				});

		// events emitted by submodels that are reexported towards other models
		Map<Class<? extends EventI>,ReexportedEvent> reexported =
				new HashMap<Class<? extends EventI>,ReexportedEvent>();

		reexported.put(
				SwitchOnHeater.class,
				new ReexportedEvent(HeaterStateModel.MIL_URI,
									SwitchOnHeater.class));
		reexported.put(
				SetPowerHeater.class,
				new ReexportedEvent(HeaterStateModel.MIL_URI,
									SetPowerHeater.class));
		reexported.put(
				SwitchOffHeater.class,
				new ReexportedEvent(HeaterStateModel.MIL_URI,
									SwitchOffHeater.class));
		reexported.put(
				Heat.class,
				new ReexportedEvent(HeaterStateModel.MIL_URI,
									Heat.class));
		reexported.put(
				DoNotHeat.class,
				new ReexportedEvent(HeaterStateModel.MIL_URI,
									DoNotHeat.class));

		// event exchanging connections between exporting and importing
		// models
		Map<EventSource,EventSink[]> connections =
									new HashMap<EventSource,EventSink[]>();

		connections.put(
				new EventSource(HeaterStateModel.MIL_URI,
								SetPowerHeater.class),
				new EventSink[] {
						new EventSink(HeaterTemperatureModel.MIL_URI,
									  SetPowerHeater.class)
				});
		connections.put(
				new EventSource(HeaterStateModel.MIL_URI,
								SwitchOffHeater.class),
				new EventSink[] {
						new EventSink(HeaterTemperatureModel.MIL_URI,
									  SwitchOffHeater.class)
				});
		connections.put(
				new EventSource(HeaterStateModel.MIL_URI, Heat.class),
				new EventSink[] {
						new EventSink(HeaterTemperatureModel.MIL_URI,
									  Heat.class)
				});
		connections.put(
				new EventSource(HeaterStateModel.MIL_URI, DoNotHeat.class),
				new EventSink[] {
						new EventSink(HeaterTemperatureModel.MIL_URI,
									  DoNotHeat.class)
				});

		// variable bindings between exporting and importing models
		Map<VariableSource,VariableSink[]> bindings =
							new HashMap<VariableSource,VariableSink[]>();

		bindings.put(new VariableSource("externalTemperature",
										Double.class,
										ExternalTemperatureModel.MIL_URI),
					 new VariableSink[] {
							 new VariableSink("externalTemperature",
									 		  Double.class,
									 		  HeaterTemperatureModel.MIL_URI)
					 });

		// coupled model descriptor
		coupledModelDescriptors.put(
				HeaterCoupledModel.MIL_URI,
				new CoupledHIOA_Descriptor(
						HeaterCoupledModel.class,
						HeaterCoupledModel.MIL_URI,
						submodels,
						imported,
						reexported,
						connections,
						null,
						null,
						null,
						bindings));

		// simulation architecture
		Architecture architecture =
				new Architecture(
						architectureURI,
						HeaterCoupledModel.MIL_URI,
						atomicModelDescriptors,
						coupledModelDescriptors,
						simulatedTimeUnit);

		return architecture;
	}

	/**
	 * create the local MIL real time or SIL simulation architecture for the
	 * {@code Heater} component when doing real time integration tests.
	 * 
	 * <p><strong>Description</strong></p>
	 * 
	 * <p>
	 * The simulation architecture created for {@code Heater} real time
	 * unit tests is the same as for non real time unit tests except that the
	 * simulation engines has to be real time, hence using the corresponding
	 * model and architecture descriptors. See
	 * {@code createHeaterMIL_ArchitectureUnitTest} for more detailed
	 * explanations about the architecture itself.
	 * </p>
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code currentSimulationType.isMILRTSimulation() || currentSimulationType.isSILSimulation()}
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @param currentSimulationType	simulation type for the next run.
	 * @param architectureURI		URI to be given to the created simulation architecture.
	 * @param simulatedTimeUnit		simulated time unit used in the architecture.
	 * @param accelerationFactor	acceleration factor used to execute in a logical time speeding up the real time.
	 * @return						the local MIL real time simulation architecture for the unit tests of the {@code Heater} component.
	 * @throws Exception			<i>to do</i>.
	 */
	public static Architecture	createHeater_RT_LocalArchitecture4IntegrationTest(
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

		String heaterStateModelURI = null;
		String heaterTemperatureModelURI = null;
		String externalTemperatureModelURI = null;
		String heaterCoupledModelURI = null;
		switch (currentSimulationType) {
		case MIL_RT_SIMULATION:
			heaterStateModelURI = HeaterStateModel.MIL_RT_URI;
			heaterTemperatureModelURI = HeaterTemperatureModel.MIL_RT_URI;
			externalTemperatureModelURI = ExternalTemperatureModel.MIL_RT_URI;
			heaterCoupledModelURI = HeaterCoupledModel.MIL_RT_URI;
			break;
		case SIL_SIMULATION:
			heaterStateModelURI = HeaterStateModel.SIL_URI;
			heaterTemperatureModelURI = HeaterTemperatureModel.SIL_URI;
			externalTemperatureModelURI = ExternalTemperatureModel.SIL_URI;
			heaterCoupledModelURI = HeaterCoupledModel.SIL_URI;
			break;
		default:
		}

		// map that will contain the atomic model descriptors to construct
		// the simulation architecture
		Map<String,AbstractAtomicModelDescriptor> atomicModelDescriptors =
															new HashMap<>();

		atomicModelDescriptors.put(
				heaterStateModelURI,
				RTAtomicModelDescriptor.create(
						HeaterStateModel.class,
						heaterStateModelURI,
						simulatedTimeUnit,
						null,
						accelerationFactor));
		// the heater models simulating its electricity consumption, its
		// temperatures and the external temperature are atomic HIOA models
		// hence we use an AtomicHIOA_Descriptor(s)
		atomicModelDescriptors.put(
				externalTemperatureModelURI,
				RTAtomicHIOA_Descriptor.create(
						ExternalTemperatureModel.class,
						externalTemperatureModelURI,
						simulatedTimeUnit,
						null,
						accelerationFactor));
		atomicModelDescriptors.put(
				heaterTemperatureModelURI,
				RTAtomicHIOA_Descriptor.create(
						HeaterTemperatureModel.class,
						heaterTemperatureModelURI,
						simulatedTimeUnit,
						null,
						accelerationFactor));

		// map that will contain the coupled model descriptors to construct
		// the simulation architecture
		Map<String,CoupledModelDescriptor> coupledModelDescriptors =
															new HashMap<>();

		// the set of submodels of the coupled model, given by their URIs
		Set<String> submodels = new HashSet<String>();
		submodels.add(heaterStateModelURI);
		submodels.add(externalTemperatureModelURI);
		submodels.add(heaterTemperatureModelURI);

		// events received by the coupled model transmitted to its submodels
		Map<Class<? extends EventI>, EventSink[]> imported = new HashMap<>();

		imported.put(
				SwitchOnHeater.class,
				new EventSink[] {
					new EventSink(heaterStateModelURI,
								  SwitchOnHeater.class)
				});
		imported.put(
				SetPowerHeater.class,
				new EventSink[] {
					new EventSink(heaterStateModelURI,
								  SetPowerHeater.class)
				});
		imported.put(
				SwitchOffHeater.class,
				new EventSink[] {
					new EventSink(heaterStateModelURI,
								  SwitchOffHeater.class)
				});
		imported.put(
				Heat.class,
				new EventSink[] {
					new EventSink(heaterStateModelURI, Heat.class)
				});
		imported.put(
				DoNotHeat.class,
				new EventSink[] {
					new EventSink(heaterStateModelURI, DoNotHeat.class)
				});

		// events emitted by submodels that are reexported towards other models
		Map<Class<? extends EventI>,ReexportedEvent> reexported =
				new HashMap<Class<? extends EventI>,ReexportedEvent>();

		reexported.put(
				SwitchOnHeater.class,
				new ReexportedEvent(heaterStateModelURI,
									SwitchOnHeater.class));
		reexported.put(
				SetPowerHeater.class,
				new ReexportedEvent(heaterStateModelURI,
									SetPowerHeater.class));
		reexported.put(
				SwitchOffHeater.class,
				new ReexportedEvent(heaterStateModelURI,
									SwitchOffHeater.class));
		reexported.put(
				Heat.class,
				new ReexportedEvent(heaterStateModelURI,
									Heat.class));
		reexported.put(
				DoNotHeat.class,
				new ReexportedEvent(heaterStateModelURI,
									DoNotHeat.class));

		// event exchanging connections between exporting and importing
		// models
		Map<EventSource,EventSink[]> connections =
									new HashMap<EventSource,EventSink[]>();

		connections.put(
				new EventSource(heaterStateModelURI,
								SetPowerHeater.class),
				new EventSink[] {
						new EventSink(heaterTemperatureModelURI,
									  SetPowerHeater.class)
				});
		connections.put(
				new EventSource(heaterStateModelURI,
								SwitchOffHeater.class),
				new EventSink[] {
						new EventSink(heaterTemperatureModelURI,
									  SwitchOffHeater.class)
				});
		connections.put(
				new EventSource(heaterStateModelURI, Heat.class),
				new EventSink[] {
						new EventSink(heaterTemperatureModelURI,
									  Heat.class)
				});
		connections.put(
				new EventSource(heaterStateModelURI, DoNotHeat.class),
				new EventSink[] {
						new EventSink(heaterTemperatureModelURI,
									  DoNotHeat.class)
				});

		// variable bindings between exporting and importing models
		Map<VariableSource,VariableSink[]> bindings =
							new HashMap<VariableSource,VariableSink[]>();

		bindings.put(new VariableSource("externalTemperature",
										Double.class,
										externalTemperatureModelURI),
					 new VariableSink[] {
							 new VariableSink("externalTemperature",
									 		  Double.class,
									 		  heaterTemperatureModelURI)
					 });

		// coupled model descriptor
		coupledModelDescriptors.put(
				heaterCoupledModelURI,
				new RTCoupledHIOA_Descriptor(
						HeaterCoupledModel.class,
						heaterCoupledModelURI,
						submodels,
						imported,
						reexported,
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
						heaterCoupledModelURI,
						atomicModelDescriptors,
						coupledModelDescriptors,
						simulatedTimeUnit,
						accelerationFactor);

		return architecture;
	}
}
// -----------------------------------------------------------------------------
