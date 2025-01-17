package fr.sorbonne_u.components.equipments.hairdryer.mil;

// Copyright Jacques Malenfant, Sorbonne Universite.
// Jacques.Malenfant@lip6.fr
//
// This software is a computer program whose purpose is to implement a mock-up
// of household energy management system.
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
import fr.sorbonne_u.devs_simulation.architectures.ArchitectureI;
import fr.sorbonne_u.devs_simulation.hioa.architectures.AtomicHIOA_Descriptor;
import fr.sorbonne_u.devs_simulation.models.architectures.AbstractAtomicModelDescriptor;
import fr.sorbonne_u.devs_simulation.models.architectures.AtomicModelDescriptor;
import fr.sorbonne_u.devs_simulation.models.architectures.CoupledModelDescriptor;
import fr.sorbonne_u.devs_simulation.models.events.EventSink;
import fr.sorbonne_u.devs_simulation.models.events.EventSource;
import fr.sorbonne_u.devs_simulation.models.time.Duration;
import fr.sorbonne_u.devs_simulation.models.time.Time;
import fr.sorbonne_u.devs_simulation.simulators.SimulationEngine;
import fr.sorbonne_u.devs_simulation.simulators.interfaces.SimulationReportI;
import fr.sorbonne_u.devs_simulation.simulators.interfaces.SimulatorI;

// -----------------------------------------------------------------------------
/**
 * The class <code>RunHairDryerUnitarySimulation</code> is the main class used
 * to run MIL simulations on the models of the hair dryer in a stand alone way.
 *
 * <p><strong>Description</strong></p>
 * 
 * <p>
 * This simulation execution class creates the following simulation architecture
 * and then executes it for one run:
 * </p>
 * <p><img src="../../../../../../../../images/hem-2024-e3/HairDryerMILModel.png"/></p> 
 * <p>
 * In this simulation architecture, the hair dryer simulator consists of three
 * atomic models:
 * </p>
 * <ol>
 * <li>The <code>HairDryerStateModel</code> keeps track of the state (switched
 *   on, switched off, etc.) of the hair dryer. The state changes are triggered
 *   by the reception of external events; whenever a state change occurs, the
 *   triggering event is emitted towards the
 *   <code>HairDryerElectricityModel</code></li>.
 * <li>The <code>HairDryerElectricityModel</code> keeps track of the electric
 *   power consumed by the hair dryer in a variable
 *   <code>currentIntensity</code> which is exported but not used in this
 *   simulation of the hair dryer in isolation.</li>
 * <li>The <code>HairDryerUserModel</code> simulates a hair dryer user by
 *   emitting state changing events towards the
 *   <code>HairDryerStateModel.</code></li>
 * </ol>
 * <p>
 * This class shows how to use simulation model descriptors to create the
 * description of the above simulation architecture and then create an instance
 * of this architecture by instantiating and connecting the models. Note how
 * models are described by atomic model descriptors and coupled model
 * descriptors and then the connections between coupled models and their
 * submodels as well as exported events to imported ones are described by
 * different maps. In this example, only connections between models within this
 * architecture are necessary, but when creating coupled models, they can also
 * import and export events consumed and produced by their submodels.
 * </p>
 * <p>
 * The architecture object is the root of this description and it provides
 * the method {@code constructSimulator} that instantiate the models and
 * connect them. This method returns the reference on the simulator attached
 * to the root coupled model in the architecture instance, which is then used
 * to perform simulation runs by calling the method
 * {@code doStandAloneSimulation}
 * </p>
 * <p>
 * The descriptors and maps can be viewed as kinds of nodes in the abstract
 * syntax tree of an architectural language that does not have a concrete
 * syntax yet.
 * </p>
 * 
 * <p><strong>Glass-box Invariants</strong></p>
 * 
 * <pre>
 * invariant	{@code true}	// no invariant
 * </pre>
 * 
 * <p><strong>Black-box Invariants</strong></p>
 * 
 * <pre>
 * invariant	{@code true}	// no invariant
 * </pre>
 * 
 * <p>Created on : 2023-09-29</p>
 * 
 * @author	<a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
 */
public class			RunHairDryerUnitaryMILSimulation
{
	/** the time unit used in the MIL simulations.						 	*/
	public static final TimeUnit	TIME_UNIT = TimeUnit.HOURS;

	public static void	main(String[] args)
	{
		Time.setPrintPrecision(4);
		Duration.setPrintPrecision(4);

		try {
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
							TIME_UNIT,
							null));
			// for atomic models, we use an AtomicModelDescriptor
			atomicModelDescriptors.put(
					HairDryerStateModel.MIL_URI,
					AtomicModelDescriptor.create(
							HairDryerStateModel.class,
							HairDryerStateModel.MIL_URI,
							TIME_UNIT,
							null));
			atomicModelDescriptors.put(
					HairDryerUserModel.MIL_URI,
					AtomicModelDescriptor.create(
							HairDryerUserModel.class,
							HairDryerUserModel.MIL_URI,
							TIME_UNIT,
							null));

			// map that will contain the coupled model descriptors to construct
			// the simulation architecture
			Map<String,CoupledModelDescriptor> coupledModelDescriptors =
																new HashMap<>();

			// the set of submodels of the coupled model, given by their URIs
			Set<String> submodels = new HashSet<String>();
			submodels.add(HairDryerElectricityModel.MIL_URI);
			submodels.add(HairDryerStateModel.MIL_URI);
			submodels.add(HairDryerUserModel.MIL_URI);

			// event exchanging connections between exporting and importing
			// models
			Map<EventSource,EventSink[]> connections =
										new HashMap<EventSource,EventSink[]>();

			connections.put(
					new EventSource(HairDryerUserModel.MIL_URI,
									SwitchOnHairDryer.class),
					new EventSink[] {
							new EventSink(HairDryerStateModel.MIL_URI,
										  SwitchOnHairDryer.class)
					});
			connections.put(
					new EventSource(HairDryerUserModel.MIL_URI,
									SwitchOffHairDryer.class),
					new EventSink[] {
							new EventSink(HairDryerStateModel.MIL_URI,
										  SwitchOffHairDryer.class)
					});
			connections.put(
					new EventSource(HairDryerUserModel.MIL_URI,
									SetHighHairDryer.class),
					new EventSink[] {
							new EventSink(HairDryerStateModel.MIL_URI,
										  SetHighHairDryer.class)
					});
			connections.put(
					new EventSource(HairDryerUserModel.MIL_URI,
									SetLowHairDryer.class),
					new EventSink[] {
							new EventSink(HairDryerStateModel.MIL_URI,
										  SetLowHairDryer.class)
					});

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
							null,
							null,
							connections,
							null));

			// simulation architecture
			ArchitectureI architecture =
					new Architecture(
							HairDryerCoupledModel.MIL_URI,
							atomicModelDescriptors,
							coupledModelDescriptors,
							TIME_UNIT);

			// create the simulator from the simulation architecture
			SimulatorI se = architecture.constructSimulator();
			// this add additional time at each simulation step in
			// standard simulations (useful when debugging)
			SimulationEngine.SIMULATION_STEP_SLEEP_TIME = 0L;
			// run a simulation with the simulation beginning at 0.0 and
			// ending at 24.0
			se.doStandAloneSimulation(0.0, 24.0);
			SimulationReportI sr = se.getSimulatedModel().getFinalReport();
			System.out.println(sr);
			System.exit(0);
		} catch (Exception e) {
			throw new RuntimeException(e) ;
		}
	}
}
// -----------------------------------------------------------------------------
