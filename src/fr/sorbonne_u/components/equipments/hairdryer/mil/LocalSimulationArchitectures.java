package fr.sorbonne_u.components.equipments.hairdryer.mil;

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
import fr.sorbonne_u.components.equipments.hairdryer.mil.events.*;
import fr.sorbonne_u.devs_simulation.architectures.Architecture;
import fr.sorbonne_u.devs_simulation.architectures.RTArchitecture;
import fr.sorbonne_u.devs_simulation.hioa.architectures.AtomicHIOA_Descriptor;
import fr.sorbonne_u.devs_simulation.hioa.architectures.RTAtomicHIOA_Descriptor;
import fr.sorbonne_u.devs_simulation.models.architectures.AbstractAtomicModelDescriptor;
import fr.sorbonne_u.devs_simulation.models.architectures.AtomicModelDescriptor;
import fr.sorbonne_u.devs_simulation.models.architectures.CoupledModelDescriptor;
import fr.sorbonne_u.devs_simulation.models.architectures.RTAtomicModelDescriptor;
import fr.sorbonne_u.devs_simulation.models.architectures.RTCoupledModelDescriptor;
import fr.sorbonne_u.devs_simulation.models.events.EventI;
import fr.sorbonne_u.devs_simulation.models.events.EventSink;
import fr.sorbonne_u.devs_simulation.models.events.EventSource;
import fr.sorbonne_u.exceptions.PreconditionException;

// -----------------------------------------------------------------------------
/**
 * The class <code>LocalSimulationArchitectures</code> defines the local
 * simulation architectures pertaining to the hair dryer components.
 *
 * <p><strong>Description</strong></p>
 * 
 * <p>
 * The class provides static methods that create the local MIL and the MIL
 * real time simulation architectures for the {@code HairDryer} and the
 * {@code HairDryerUser} components. The overall simulation architecture
 * for the hair dryer can be seen as follows:
 * </p>
 * <p><img src="../../../../../../../../images/hem-2024-e3/HairDryerMILModel.png"/></p> 
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
 * {@code HairDryer} component hair to go from MIL real time to SIL simulations,
 * the MIL real time architectures can be used to execute SIL simulations. For
 * the {@code HairDryerUser}, there is no need for a simulator in SIL
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
	 * create the local MIL simulation architecture for the {@code HairDryer}
	 * component when doing unit tests.
	 * 
	 * <p><strong>Description</strong></p>
	 * 
	 * <p>
	 * In hair dryer unit tests, the component architecture has two
	 * components: {@code HairDryer} and {@code HairDryerUser}. The local
	 * simulation architecture for the {@code HairDryer} component is as
	 * follows:
	 * </p>
	 * <p><img src="../../../../../../../../images/hem-2024-e3/HairDryerUnitTestLocalArchitecture.png"/></p>
	 * <p>
	 * There are two atomic simulation models: 
	 * </p>
	 * <ol>
	 * <li>The <code>HairDryerStateModel</code> keeps track of the state
	 *   (switched on, switched off, etc.) of the hair dryer. The state changes
	 *   are triggered by the reception of external events that it imports;
	 *   whenever a state change occurs, the triggering event is emitted towards
	 *   the <code>HairDryerElectricityModel</code></li>, hence it also exports
	 *   them.
	 * <li>The <code>HairDryerElectricityModel</code> keeps track of the
	 *   electric power consumed by the hair dryer in a variable
	 *   <code>currentIntensity</code> which is exported but not used in this
	 *   simulation of the hair dryer in isolation. It changes this power
	 *   consumption upon the reception of hair dryer events, which it
	 *   imports.</li>
	 * </ol>
	 * <p>
	 * The coupled model <code>HairDryerCoupledModel</code> composes the two
	 * atomic models so that the exported events emitted by the
	 * <code>HairDryerStateModel</code> are received by the
	 * <code>HairDryerElectricityModel</code> that imports them. It also
	 * indicates that it imports the hair dryer events that will trigger the
	 * state changes and transmit them to the <code>HairDryerStateModel</code>
	 * importing them.
	 * </p>
	 * <p>
	 * A third atomic model, <code>HairDryerUserModel</code>, which simulates a
	 * hair dryer user by emitting state changing events towards the
	 * <code>HairDryerStateModel.</code></li>. This third model will reside in
	 * the {@code HairDryerUser} component and it will be connected through
	 * the overall multi-component simulation architecture.
	 * </p>
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
	 * @return					the local MIL simulation architecture for the {@code HairDryer} component.
	 * @throws Exception		<i>to do</i>.
	 */
	public static Architecture	createHairDryerMILLocalArchitecture4UnitTest(
		String architectureURI, 
		TimeUnit simulatedTimeUnit
		) throws Exception
	{
		// map that will contain the atomic model descriptors to construct
		// the simulation architecture
		Map<String,AbstractAtomicModelDescriptor> atomicModelDescriptors =
															new HashMap<>();

		// the hair dyer model simulating its electricity consumption, an
		// atomic HIOA model hence we use an AtomicHIOA_Descriptor
		atomicModelDescriptors.put(
				HairDryerElectricityModel.MIL_URI,
				AtomicHIOA_Descriptor.create(
						HairDryerElectricityModel.class,
						HairDryerElectricityModel.MIL_URI,
						simulatedTimeUnit,
						null));
		// for atomic models, we use an AtomicModelDescriptor
		atomicModelDescriptors.put(
				HairDryerStateModel.MIL_URI,
				AtomicModelDescriptor.create(
						HairDryerStateModel.class,
						HairDryerStateModel.MIL_URI,
						simulatedTimeUnit,
						null));

		// map that will contain the coupled model descriptors to construct
		// the simulation architecture
		Map<String,CoupledModelDescriptor> coupledModelDescriptors =
															new HashMap<>();

		// the set of submodels of the coupled model, given by their URIs
		Set<String> submodels = new HashSet<String>();
		submodels.add(HairDryerElectricityModel.MIL_URI);
		submodels.add(HairDryerStateModel.MIL_URI);

		// Event types imported by the coupled model and transmitted to
		// its submodels
		Map<Class<? extends EventI>,EventSink[]> imported =
				new HashMap<Class<? extends EventI>,EventSink[]>();

		imported.put(
			SwitchOnHairDryer.class,
			new EventSink[] {
				new EventSink(HairDryerStateModel.MIL_URI,
							  SwitchOnHairDryer.class)
			});
		imported.put(
				SwitchOffHairDryer.class,
				new EventSink[] {
					new EventSink(HairDryerStateModel.MIL_URI,
								  SwitchOffHairDryer.class)
				});
		imported.put(
				SetHighHairDryer.class,
				new EventSink[] {
					new EventSink(HairDryerStateModel.MIL_URI,
								  SetHighHairDryer.class)
				});
		imported.put(
				SetLowHairDryer.class,
				new EventSink[] {
					new EventSink(HairDryerStateModel.MIL_URI,
								  SetLowHairDryer.class)
				});

		// event exchanging connections between exporting and importing
		// models
		Map<EventSource,EventSink[]> connections =
									new HashMap<EventSource,EventSink[]>();

		connections.put(
			new EventSource(HairDryerStateModel.MIL_URI,
							SwitchOnHairDryer.class),
			new EventSink[] {
				new EventSink(HairDryerElectricityModel.MIL_URI,
							  SwitchOnHairDryer.class)
			});
		connections.put(
			new EventSource(HairDryerStateModel.MIL_URI,
							SwitchOffHairDryer.class),
			new EventSink[] {
				new EventSink(HairDryerElectricityModel.MIL_URI,
							  SwitchOffHairDryer.class)
			});
		connections.put(
			new EventSource(HairDryerStateModel.MIL_URI,
							SetHighHairDryer.class),
			new EventSink[] {
				new EventSink(HairDryerElectricityModel.MIL_URI,
							  SetHighHairDryer.class)
			});
		connections.put(
			new EventSource(HairDryerStateModel.MIL_URI,
							SetLowHairDryer.class),
			new EventSink[] {
				new EventSink(HairDryerElectricityModel.MIL_URI,
							  SetLowHairDryer.class)
			});

		// coupled model descriptor
		coupledModelDescriptors.put(
				HairDryerCoupledModel.MIL_URI,
				new CoupledModelDescriptor(
						HairDryerCoupledModel.class,
						HairDryerCoupledModel.MIL_URI,
						submodels,
						imported,
						null,
						connections,
						null));

		// simulation architecture
		Architecture architecture =
				new Architecture(
						architectureURI,
						HairDryerCoupledModel.MIL_URI,
						atomicModelDescriptors,
						coupledModelDescriptors,
						simulatedTimeUnit);

		return architecture;
	}

	/**
	 * create the local MIL simulation architecture for the {@code HairDryer}
	 * component when doing real time unit tests.
	 * 
	 * <p><strong>Description</strong></p>
	 * 
	 * <p>
	 * The simulation architecture created for {@code HairDryer} real time
	 * unit tests is the same as for non real time unit tests except that the
	 * simulation engines has to be real time, hence using the corresponding
	 * model and architecture descriptors. See
	 * {@code createHairDryerMIL_ArchitectureUnitTest} for more detailed
	 * explanations about the architecture itself.
	 * </p>
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
	 * @return						the local MIL real time simulation architecture for the unit tests of the {@code HairDryer} component.
	 * @throws Exception			<i>to do</i>.
	 */
	public static Architecture	createHairDryerMIL_RT_Architecture4UnitTest(
		String architectureURI, 
		TimeUnit simulatedTimeUnit,
		double accelerationFactor
		) throws Exception
	{
		// map that will contain the atomic model descriptors to construct
		// the simulation architecture
		Map<String,AbstractAtomicModelDescriptor> atomicModelDescriptors =
															new HashMap<>();

		// the hair dyer model simulating its electricity consumption, an
		// atomic HIOA model hence we use an AtomicHIOA_Descriptor
		atomicModelDescriptors.put(
				HairDryerElectricityModel.MIL_RT_URI,
				RTAtomicHIOA_Descriptor.create(
						HairDryerElectricityModel.class,
						HairDryerElectricityModel.MIL_RT_URI,
						simulatedTimeUnit,
						null,
						accelerationFactor));
		// for atomic models, we use an AtomicModelDescriptor
		atomicModelDescriptors.put(
				HairDryerStateModel.MIL_RT_URI,
				RTAtomicModelDescriptor.create(
						HairDryerStateModel.class,
						HairDryerStateModel.MIL_RT_URI,
						simulatedTimeUnit,
						null,
						accelerationFactor));

		// map that will contain the coupled model descriptors to construct
		// the simulation architecture
		Map<String,CoupledModelDescriptor> coupledModelDescriptors =
															new HashMap<>();

		// the set of submodels of the coupled model, given by their URIs
		Set<String> submodels = new HashSet<String>();
		submodels.add(HairDryerElectricityModel.MIL_RT_URI);
		submodels.add(HairDryerStateModel.MIL_RT_URI);

		// Event types imported by the coupled model and transmitted to
		// its submodels
		Map<Class<? extends EventI>,EventSink[]> imported =
				new HashMap<Class<? extends EventI>,EventSink[]>();

		imported.put(
			SwitchOnHairDryer.class,
			new EventSink[] {
				new EventSink(HairDryerStateModel.MIL_RT_URI,
							  SwitchOnHairDryer.class)
			});
		imported.put(
				SwitchOffHairDryer.class,
				new EventSink[] {
					new EventSink(HairDryerStateModel.MIL_RT_URI,
								  SwitchOffHairDryer.class)
				});
		imported.put(
				SetHighHairDryer.class,
				new EventSink[] {
					new EventSink(HairDryerStateModel.MIL_RT_URI,
								  SetHighHairDryer.class)
				});
		imported.put(
				SetLowHairDryer.class,
				new EventSink[] {
					new EventSink(HairDryerStateModel.MIL_RT_URI,
								  SetLowHairDryer.class)
				});

		// event exchanging connections between exporting and importing
		// models
		Map<EventSource,EventSink[]> connections =
									new HashMap<EventSource,EventSink[]>();

		connections.put(
			new EventSource(HairDryerStateModel.MIL_RT_URI,
							SwitchOnHairDryer.class),
			new EventSink[] {
				new EventSink(HairDryerElectricityModel.MIL_RT_URI,
							  SwitchOnHairDryer.class)
			});
		connections.put(
			new EventSource(HairDryerStateModel.MIL_RT_URI,
							SwitchOffHairDryer.class),
			new EventSink[] {
				new EventSink(HairDryerElectricityModel.MIL_RT_URI,
							  SwitchOffHairDryer.class)
			});
		connections.put(
			new EventSource(HairDryerStateModel.MIL_RT_URI,
							SetHighHairDryer.class),
			new EventSink[] {
				new EventSink(HairDryerElectricityModel.MIL_RT_URI,
							  SetHighHairDryer.class)
			});
		connections.put(
			new EventSource(HairDryerStateModel.MIL_RT_URI,
							SetLowHairDryer.class),
			new EventSink[] {
				new EventSink(HairDryerElectricityModel.MIL_RT_URI,
							  SetLowHairDryer.class)
			});

		// coupled model descriptor
		coupledModelDescriptors.put(
				HairDryerCoupledModel.MIL_RT_URI,
				new RTCoupledModelDescriptor(
						HairDryerCoupledModel.class,
						HairDryerCoupledModel.MIL_RT_URI,
						submodels,
						imported,
						null,
						connections,
						null,
						accelerationFactor));

		// simulation architecture
		Architecture architecture =
				new RTArchitecture(
						architectureURI,
						HairDryerCoupledModel.MIL_RT_URI,
						atomicModelDescriptors,
						coupledModelDescriptors,
						simulatedTimeUnit,
						accelerationFactor);

		return architecture;
	}

	/**
	 * create the local MIL simulation architecture for the {@code HairDryer}
	 * component when doing an integration test.
	 * 
	 * <p><strong>Description</strong></p>
	 * 
	 * <p>
	 * In integration tests, the {@code HairDryer} component defines only one
	 * model, the {@code HairDryerStateModel}, that makes the state and mode
	 * changes for the hair dryer simulator when receiving events typically
	 * from the {@code HairDryerUserModel} in MIL simulations or, for SIL
	 * SIL simulations, from the {@code HairDryer} component methods that turn
	 * on, turn off, set to high or set to low the hair dryer. Hence, the
	 * created architecture contains this sole atomic model.
	 * </p>
	 * <p>
	 * In these tests, the {@code HairDryerElectricityModel}, to which the
	 * {@code HairDryerStateModel} resends the events to make it keep track of
	 * the corresponding electric power consumption changes, is located in
	 * the {@code ElectricMeter} component simulator to share its variable
	 * {@code currentIntensity} with the meter electricity model. Hence, the
	 * events will be reexported by the local simulator to the local simulator
	 * of the {@code ElectricMeter} component that will have the received by
	 * the {@code HairDryerElectricityModel}.
	 * </p>
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
	 * @return					the local MIL simulation architecture for the {@code HairDryer} component.
	 * @throws Exception		<i>to do</i>.
	 */
	public static Architecture	createHairDryerMILArchitecture4IntegrationTest(
		String architectureURI, 
		TimeUnit simulatedTimeUnit
		) throws Exception
	{
		// map that will contain the atomic model descriptors to construct
		// the simulation architecture
		Map<String,AbstractAtomicModelDescriptor> atomicModelDescriptors =
															new HashMap<>();

		// the hair dyer model simulating its electricity consumption, an
		// atomic HIOA model hence we use an AtomicHIOA_Descriptor
		atomicModelDescriptors.put(
				HairDryerStateModel.MIL_URI,
				AtomicModelDescriptor.create(
						HairDryerStateModel.class,
						HairDryerStateModel.MIL_URI,
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
						HairDryerStateModel.MIL_URI,
						atomicModelDescriptors,
						coupledModelDescriptors,
						simulatedTimeUnit);

		return architecture;
	}

	/**
	 * create the local MIL real time simulation architecture for the
	 * {@code HairDryer} component; this simulation architecture is also
	 * used for integration tests using MIL real time and SIL simulations.
	 * 
	 * <p><strong>Description</strong></p>
	 * 
	 * <p>
	 * In integration tests, the {@code HairDryer} component defines only one
	 * model, the {@code HairDryerStateModel}, that makes the state and mode
	 * changes for the hair dryer simulator when receiving events typically
	 * from the {@code HairDryerUserModel} in MIL simulations or, for SIL
	 * SIL simulations, from the {@code HairDryer} component methods that turn
	 * on, turn off, set to high or set to low the hair dryer. Hence, the
	 * created architecture contains this sole atomic model.
	 * </p>
	 * <p>
	 * In these tests, the {@code HairDryerElectricityModel}, to which the
	 * {@code HairDryerStateModel} resends the events to make it keep track of
	 * the corresponding electric power consumption changes, is located in
	 * the {@code ElectricMeter} component simulator. Hence, the events will
	 * be reexported by the local simulator to the local simulator of
	 * the {@code ElectricMeter} component that will have the received by
	 * the {@code HairDryerElectricityModel}.
	 * </p>
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code accelerationFactor > 0.0}
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @param architectureURI		URI to be given to the created simulation architecture.
	 * @param simulatedTimeUnit		simulated time unit used in the architecture.
	 * @param accelerationFactor	acceleration factor used in this run.
	 * @return						the local MIL real time simulation architecture for the {@code HairDryer} component.
	 * @throws Exception			<i>to do</i>.
	 */
	public static Architecture	createHairDryerMIL_RT_Architecture4IntegrationTest(
		String architectureURI, 
		TimeUnit simulatedTimeUnit,
		double accelerationFactor
		) throws Exception
	{
		assert	accelerationFactor > 0.0 :
				new PreconditionException("accelerationFactor > 0.0");

		// map that will contain the atomic model descriptors to construct
		// the simulation architecture
		Map<String,AbstractAtomicModelDescriptor> atomicModelDescriptors =
															new HashMap<>();

		// the hair dyer model simulating its electricity consumption, an
		// atomic HIOA model hence we use an AtomicHIOA_Descriptor
		atomicModelDescriptors.put(
				HairDryerStateModel.MIL_RT_URI,
				RTAtomicModelDescriptor.create(
						HairDryerStateModel.class,
						HairDryerStateModel.MIL_RT_URI,
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
						HairDryerStateModel.MIL_RT_URI,
						atomicModelDescriptors,
						coupledModelDescriptors,
						simulatedTimeUnit,
						accelerationFactor);

		return architecture;
	}

	/**
	 * create the local MIL simulation architecture for the {@code HairDryerUser}
	 * component that is used both for unit and integration tests.
	 * 
	 * <p><strong>Description</strong></p>
	 * 
	 * <p>
	 * For both the unit and the integration tests, the local architecture of
	 * the {@code HairDryerUser} component contains only one atomic model, the
	 * <code>HairDryerUserModel</code> that implements a user simulation by
	 * sending at given times events to the {@code HairDryerStateModel}
	 * that turn on, turn off, set to high or set to low the hair dryer.
	 * Hence, the created architecture contains this sole atomic model.
	 * </p>
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
	 * @return					the local MIL simulation architecture for the {@code HairDryerUser} component.
	 * @throws Exception		<i>to do</i>.
	 */
	public static Architecture	createHairDryerUserMIL_Architecture(
		String architectureURI, 
		TimeUnit simulatedTimeUnit
		) throws Exception
	{
		// map that will contain the atomic model descriptors to construct
		// the simulation architecture
		Map<String,AbstractAtomicModelDescriptor> atomicModelDescriptors =
															new HashMap<>();

		// for atomic model, we use an AtomicModelDescriptor
		atomicModelDescriptors.put(
				HairDryerUserModel.MIL_URI,
				AtomicModelDescriptor.create(
						HairDryerUserModel.class,
						HairDryerUserModel.MIL_URI,
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
						HairDryerUserModel.MIL_URI,
						atomicModelDescriptors,
						coupledModelDescriptors,
						simulatedTimeUnit);

		return architecture;
	}

	/**
	 * create the local MIL real time simulation architecture for the
	 * {@code HairDryerUser} component used in both the unit and
	 * the integration tests.
	 * 
	 * <p><strong>Description</strong></p>
	 * 
	 * <p>
	 * The {@code HairDryerUser} component defines only one model, the
	 * {@code HairDryerUserModel}, that implements a user simulation by
	 * sending at given times events to the {@code HairDryerStateModel}
	 * that turn on, turn off, set to high or set to low the hair dryer.
	 * Hence, the created architecture contains this sole atomic model.
	 * </p>
	 * <p>
	 * For SIL simulations, the {@code HairDryerUser} component does not
	 * have a simulator but rather its code calls the {@code HairDryer}
	 * component methods directly to operate the hair dryer.
	 * </p>
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code accelerationFactor > 0.0}
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @param architectureURI		URI to be given to the created simulation architecture.
	 * @param simulatedTimeUnit		simulated time unit used in the architecture.
	 * @param accelerationFactor	acceleration factor used in this run.
	 * @return						the local MIL real time simulation architecture for the {@code HairDryerUser} component.
	 * @throws Exception			<i>to do</i>.
	 */
	public static Architecture	createHairDryerUserMIL_RT_Architecture(
		String architectureURI, 
		TimeUnit simulatedTimeUnit,
		double accelerationFactor
		) throws Exception
	{
		assert	accelerationFactor > 0.0 :
				new PreconditionException("accelerationFactor > 0.0");

		// map that will contain the atomic model descriptors to construct
		// the simulation architecture
		Map<String,AbstractAtomicModelDescriptor> atomicModelDescriptors =
															new HashMap<>();

		// for atomic model, we use an AtomicModelDescriptor
		atomicModelDescriptors.put(
				HairDryerUserModel.MIL_RT_URI,
				RTAtomicModelDescriptor.create(
						HairDryerUserModel.class,
						HairDryerUserModel.MIL_RT_URI,
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
						HairDryerUserModel.MIL_RT_URI,
						atomicModelDescriptors,
						coupledModelDescriptors,
						simulatedTimeUnit,
						accelerationFactor);

		return architecture;
	}
}
// -----------------------------------------------------------------------------
