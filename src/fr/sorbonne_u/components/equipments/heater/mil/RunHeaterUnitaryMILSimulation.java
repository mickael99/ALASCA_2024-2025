package fr.sorbonne_u.components.equipments.heater.mil;

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

import fr.sorbonne_u.components.equipments.heater.mil.events.*;
import fr.sorbonne_u.devs_simulation.architectures.Architecture;
import fr.sorbonne_u.devs_simulation.architectures.ArchitectureI;
import fr.sorbonne_u.devs_simulation.hioa.architectures.AtomicHIOA_Descriptor;
import fr.sorbonne_u.devs_simulation.hioa.architectures.CoupledHIOA_Descriptor;
import fr.sorbonne_u.devs_simulation.hioa.models.vars.VariableSink;
import fr.sorbonne_u.devs_simulation.hioa.models.vars.VariableSource;
import fr.sorbonne_u.devs_simulation.models.architectures.AbstractAtomicModelDescriptor;
import fr.sorbonne_u.devs_simulation.models.architectures.AtomicModelDescriptor;
import fr.sorbonne_u.devs_simulation.models.architectures.CoupledModelDescriptor;
import fr.sorbonne_u.devs_simulation.models.events.EventSink;
import fr.sorbonne_u.devs_simulation.models.events.EventSource;
import fr.sorbonne_u.devs_simulation.simulators.SimulationEngine;
import fr.sorbonne_u.devs_simulation.simulators.interfaces.SimulatorI;

// -----------------------------------------------------------------------------
/**
 * The class <code>RunHeaterUnitarySimulation</code> creates a simulator
 * for the heater and then runs a typical MIL simulation.
 *
 * <p><strong>Description</strong></p>
 * 
 * <p>
 * This simulation execution class creates the following simulation architecture
 * and then executes it for one run:
 * </p>
 * <p><img src="../../../../../../../../images/hem-2024-e3/HeaterMILModel.png"/></p> 
 * <p>
 * In this simulation architecture, the heater simulator consists of five
 * atomic models:
 * </p>
 * <ol>
 * <li>The {@code HeaterStateModel} keeps track of the state (switched on,
 *   switched off, etc.) of the heater and its current power level. The state
 *   changes are triggered by the reception of external events; whenever a state
 *   change occurs, the triggering event is reemitted towards the
 *   {@code HeaterElectricityModel} and the {@code HeaterTemperatureModel}
 *   (except for {@code SwitchOnHeater} that does not influence the temperature
 *   model).</li> and .
 * <li>The {@code HeaterElectricityModel} keeps track of the electric power
 *   consumed by the heater in a variable <code>currentIntensity</code> which is
 *   exported but not used in this simulation of the heater in isolation.</li>
 * <li>The {@code ExternalTemperatureModel} simulates the temperature outside
 *   the room, a part of the environment. The simulated temperature is put in
 *   an exported variable {@code externalTemperature} that is imported with
 *   the same name by the {@code HeaterTemperatureModel}.</li>
 * <li>The {@code HeaterTemperatureModel} simulates the temperature inside the
 *   heated room, using the external temperature provided by the
 *   {@code ExternalTemperatureModel} and the current power of the heater,
 *   which it keeps track of through the {@code SetPowerHeater} and
 *   {@code SwitchOffHeater} events. The evolution of the inside temperature
 *   obviously depends upon the fact that the heater actually is heating or
 *   not, a state which is kept track of through the events {@code Heat} and
 *   {@code DoNotHeat}.</li>
 * <li>The {@code HeaterUnitTesterModel} simulates a heater user and a heater
 *   controller by emitting state changing events towards the
 *   {@code HairDryerStateModel}.</li>
 * </ol>
 * <p>
 * This class shows how to use simulation model descriptors to create the
 * description of a simulation architecture and then create an instance of this
 * architecture by instantiating and connecting the models. Note how models
 * are described by atomic model descriptors and coupled model descriptors and
 * then the connections between coupled models and their submodels as well as
 * exported events and variables to imported ones are described by different
 * maps. In this example, only connections of events and bindings of variables
 * between models within this architecture are necessary, but when creating
 * coupled models, they can also import and export events and variables
 * consumed and produced by their submodels.
 * </p>
 * <p>
 * The architecture object is the root of this description and it provides
 * the method {@code constructSimulator} that instantiate the models and
 * connect them. This method returns the reference on the simulator attached
 * to the root coupled model in the architecture instance, which is then used
 * to perform simulation runs by calling the method
 * {@code doStandAloneSimulation}.
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
 * invariant	{@code true}	// no more invariant
 * </pre>
 * 
 * <p><strong>Black-box Invariants</strong></p>
 * 
 * <pre>
 * invariant	{@code true}	// no more invariant
 * </pre>
 * 
 * <p>Created on : 2023-09-29</p>
 * 
 * @author	<a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
 */
public class			RunHeaterUnitaryMILSimulation
{
	public static void main(String[] args)
	{
		try {
			// map that will contain the atomic model descriptors to construct
			// the simulation architecture
			Map<String,AbstractAtomicModelDescriptor> atomicModelDescriptors =
															new HashMap<>();

			atomicModelDescriptors.put(
					HeaterStateModel.MIL_URI,
					AtomicModelDescriptor.create(
							HeaterStateModel.class,
							HeaterStateModel.MIL_URI,
							TimeUnit.HOURS,
							null));
			// the heater models simulating its electricity consumption, its
			// temperatures and the external temperature are atomic HIOA models
			// hence we use an AtomicHIOA_Descriptor(s)
			atomicModelDescriptors.put(
					HeaterElectricityModel.MIL_URI,
					AtomicHIOA_Descriptor.create(
							HeaterElectricityModel.class,
							HeaterElectricityModel.MIL_URI,
							TimeUnit.HOURS,
							null));
			atomicModelDescriptors.put(
					HeaterTemperatureModel.MIL_URI,
					AtomicHIOA_Descriptor.create(
							HeaterTemperatureModel.class,
							HeaterTemperatureModel.MIL_URI,
							TimeUnit.HOURS,
							null));
			atomicModelDescriptors.put(
					ExternalTemperatureModel.MIL_URI,
					AtomicHIOA_Descriptor.create(
							ExternalTemperatureModel.class,
							ExternalTemperatureModel.MIL_URI,
							TimeUnit.HOURS,
							null));
			// the heater unit tester model only exchanges event, an
			// atomic model hence we use an AtomicModelDescriptor
			atomicModelDescriptors.put(
					HeaterUnitTesterModel.MIL_URI,
					AtomicModelDescriptor.create(
							HeaterUnitTesterModel.class,
							HeaterUnitTesterModel.MIL_URI,
							TimeUnit.HOURS,
							null));

			// map that will contain the coupled model descriptors to construct
			// the simulation architecture
			Map<String,CoupledModelDescriptor> coupledModelDescriptors =
																new HashMap<>();

			// the set of submodels of the coupled model, given by their URIs
			Set<String> submodels = new HashSet<String>();
			submodels.add(HeaterStateModel.MIL_URI);
			submodels.add(HeaterElectricityModel.MIL_URI);
			submodels.add(HeaterTemperatureModel.MIL_URI);
			submodels.add(ExternalTemperatureModel.MIL_URI);
			submodels.add(HeaterUnitTesterModel.MIL_URI);
			
			// event exchanging connections between exporting and importing
			// models
			Map<EventSource,EventSink[]> connections =
										new HashMap<EventSource,EventSink[]>();

			connections.put(
					new EventSource(HeaterUnitTesterModel.MIL_URI,
							SetPowerHeater.class),
					new EventSink[] {
							new EventSink(HeaterStateModel.MIL_URI,
										  SetPowerHeater.class)
					});
			connections.put(
					new EventSource(HeaterUnitTesterModel.MIL_URI,
									SwitchOnHeater.class),
					new EventSink[] {
							new EventSink(HeaterStateModel.MIL_URI,
										  SwitchOnHeater.class)
					});
			connections.put(
					new EventSource(HeaterUnitTesterModel.MIL_URI,
									SwitchOffHeater.class),
					new EventSink[] {
							new EventSink(HeaterStateModel.MIL_URI,
										  SwitchOffHeater.class)
					});
			connections.put(
					new EventSource(HeaterUnitTesterModel.MIL_URI,
									Heat.class),
					new EventSink[] {
							new EventSink(HeaterStateModel.MIL_URI,
										  Heat.class)
					});
			connections.put(
					new EventSource(HeaterUnitTesterModel.MIL_URI,
									DoNotHeat.class),
					new EventSink[] {
							new EventSink(HeaterStateModel.MIL_URI,
										  DoNotHeat.class)
					});

			
			
			connections.put(
					new EventSource(HeaterStateModel.MIL_URI,
									SetPowerHeater.class),
					new EventSink[] {
							new EventSink(HeaterElectricityModel.MIL_URI,
										  SetPowerHeater.class),
							new EventSink(HeaterTemperatureModel.MIL_URI,
										  SetPowerHeater.class)
					});
			connections.put(
					new EventSource(HeaterStateModel.MIL_URI,
									SwitchOnHeater.class),
					new EventSink[] {
							new EventSink(HeaterElectricityModel.MIL_URI,
										  SwitchOnHeater.class)
					});
			connections.put(
					new EventSource(HeaterStateModel.MIL_URI,
									SwitchOffHeater.class),
					new EventSink[] {
							new EventSink(HeaterElectricityModel.MIL_URI,
										  SwitchOffHeater.class),
							new EventSink(HeaterTemperatureModel.MIL_URI,
										  SwitchOffHeater.class)
					});
			connections.put(
					new EventSource(HeaterStateModel.MIL_URI, Heat.class),
					new EventSink[] {
							new EventSink(HeaterElectricityModel.MIL_URI,
										  Heat.class),
							new EventSink(HeaterTemperatureModel.MIL_URI,
										  Heat.class)
					});
			connections.put(
					new EventSource(HeaterStateModel.MIL_URI, DoNotHeat.class),
					new EventSink[] {
							new EventSink(HeaterElectricityModel.MIL_URI,
										  DoNotHeat.class),
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
							null,
							null,
							connections,
							null,
							null,
							null,
							bindings));

			// simulation architecture
			ArchitectureI architecture =
					new Architecture(
							HeaterCoupledModel.MIL_URI,
							atomicModelDescriptors,
							coupledModelDescriptors,
							TimeUnit.HOURS);

			// create the simulator from the simulation architecture
			SimulatorI se = architecture.constructSimulator();
			// this add additional time at each simulation step in
			// standard simulations (useful when debugging)
			SimulationEngine.SIMULATION_STEP_SLEEP_TIME = 0L;
			// run a simulation with the simulation beginning at 0.0 and
			// ending at 24.0
			se.doStandAloneSimulation(0.0, 24.0);
			System.exit(0);
		} catch (Exception e) {
			throw new RuntimeException(e) ;
		}
	}
}
// -----------------------------------------------------------------------------
