package fr.sorbonne_u.components.equipments.heater.unittests;

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

import fr.sorbonne_u.components.CoordinatorComponent;
import fr.sorbonne_u.components.GlobalCoupledModel;
import fr.sorbonne_u.components.annotations.RequiredInterfaces;
import fr.sorbonne_u.components.cyphy.AbstractCyPhyComponent;
import fr.sorbonne_u.components.cyphy.plugins.devs.CoordinatorPlugin;
import fr.sorbonne_u.components.cyphy.plugins.devs.SupervisorPlugin;
import fr.sorbonne_u.components.cyphy.plugins.devs.architectures.ComponentAtomicModelDescriptor;
import fr.sorbonne_u.components.cyphy.plugins.devs.architectures.ComponentCoupledModelDescriptor;
import fr.sorbonne_u.components.cyphy.plugins.devs.architectures.ComponentModelArchitecture;
import fr.sorbonne_u.components.cyphy.plugins.devs.architectures.RTComponentAtomicModelDescriptor;
import fr.sorbonne_u.components.cyphy.plugins.devs.architectures.RTComponentCoupledModelDescriptor;
import fr.sorbonne_u.components.cyphy.plugins.devs.architectures.RTComponentModelArchitecture;
import fr.sorbonne_u.components.cyphy.utils.aclocks.AcceleratedAndSimulationClock;
import fr.sorbonne_u.components.cyphy.utils.aclocks.ClocksServerWithSimulationCI;
import fr.sorbonne_u.components.cyphy.utils.aclocks.ClocksServerWithSimulationConnector;
import fr.sorbonne_u.components.cyphy.utils.aclocks.ClocksServerWithSimulationOutboundPort;
import fr.sorbonne_u.components.equipments.heater.Heater;
import fr.sorbonne_u.components.equipments.heater.HeaterUser;
import fr.sorbonne_u.components.equipments.heater.mil.HeaterCoupledModel;
import fr.sorbonne_u.components.equipments.heater.mil.HeaterUnitTesterModel;
import fr.sorbonne_u.components.equipments.heater.mil.events.*;
import fr.sorbonne_u.components.exceptions.BCMException;
import fr.sorbonne_u.components.utils.SimulationType;
import fr.sorbonne_u.devs_simulation.models.architectures.AbstractAtomicModelDescriptor;
import fr.sorbonne_u.devs_simulation.models.architectures.CoupledModelDescriptor;
import fr.sorbonne_u.devs_simulation.models.events.EventI;
import fr.sorbonne_u.devs_simulation.models.events.EventSink;
import fr.sorbonne_u.devs_simulation.models.events.EventSource;
import fr.sorbonne_u.exceptions.ImplementationInvariantException;
import fr.sorbonne_u.exceptions.InvariantChecking;
import fr.sorbonne_u.exceptions.InvariantException;
import fr.sorbonne_u.exceptions.PreconditionException;
import fr.sorbonne_u.utils.aclocks.ClocksServer;

// -----------------------------------------------------------------------------
/**
 * The class <code>HeaterUnitTestsSupervisor</code> implements the supervisor
 * component for simulated runs of the heater appliance.
 *
 * <p><strong>Description</strong></p>
 * 
 * <p>
 * In BCM-CyPhy-Components, simulated runs execute both the components and their
 * DEVS simulators. In this case, the supervisor component is responsible for
 * the creation, initialisation and execution of the global component simulation
 * architecture using models disseminated into the different application
 * components.
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
 * invariant	{@code MIL_ARCHITECTURE_URI != null && !MIL_ARCHITECTURE_URI.isEmpty()}
 * invariant	{@code MIL_RT_ARCHITECTURE_URI != null && !MIL_RT_ARCHITECTURE_URI.isEmpty()}
 * invariant	{@code SIL_ARCHITECTURE_URI != null && !SIL_ARCHITECTURE_URI.isEmpty()}
 * invariant	{@code X_RELATIVE_POSITION >= 0}
 * invariant	{@code Y_RELATIVE_POSITION #= 0}
 * invariant	{@code currentSimulationType != null}
 * invariant	{@code !currentSimulationType.isSimulated() || (simArchitectureURI != null && !simArchitectureURI.isEmpty())}
 * </pre>
 * 
 * <p>Created on : 2023-11-13</p>
 * 
 * @author	<a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
 */
@RequiredInterfaces(required = {ClocksServerWithSimulationCI.class})
public class			HeaterUnitTestsSupervisor
extends		AbstractCyPhyComponent
{
	// -------------------------------------------------------------------------
	// Constants and variables
	// -------------------------------------------------------------------------

	/** URI of the simulation architecture when a MIL simulation is
	 *  executed.															*/
	public static final String		MIL_ARCHITECTURE_URI =
												"heater-mil-simulator";
	/** URI of the simulation architecture when a MIL real time
	 *  simulation is executed.												*/
	public static final String		MIL_RT_ARCHITECTURE_URI =
												"heater-mil-rt-simulator";
	/** URI of the simulation architecture when a SIL simulation is
	 *  executed.															*/
	public static final String		SIL_ARCHITECTURE_URI =
												"heater-sil-simulator";

	/** when true, methods trace their actions.								*/
	public static boolean			VERBOSE = false;
	/** when tracing, x coordinate of the window relative position.			*/
	public static int				X_RELATIVE_POSITION = 0;
	/** when tracing, y coordinate of the window relative position.			*/
	public static int				Y_RELATIVE_POSITION = 0;

	// Execution/Simulation

	/** current type of simulation.											*/
	protected final SimulationType	currentSimulationType;
	/** URI of the simulation architecture to be created or the empty string
	 *  if the component does not execute as a SIL simulation.				*/
	protected final String			simArchitectureURI;

	// -------------------------------------------------------------------------
	// Invariants
	// -------------------------------------------------------------------------

	/**
	 * return true if the glass-box invariants are observed, false otherwise.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code instance != null}
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @param instance	instance to be tested.
	 * @return			true if the glass-box invariants are observed, false otherwise.
	 */
	protected static boolean	glassBoxInvariants(
		HeaterUnitTestsSupervisor instance
		)
	{
		assert instance != null : new PreconditionException("instance != null");

		boolean ret = true;
		ret &= InvariantChecking.checkGlassBoxInvariant(
					MIL_ARCHITECTURE_URI != null && 
											!MIL_ARCHITECTURE_URI.isEmpty(),
					HeaterUnitTestsSupervisor.class, instance,
					"MIL_ARCHITECTURE_URI != null && "
					+ "!MIL_ARCHITECTURE_URI.isEmpty()");
		ret &= InvariantChecking.checkGlassBoxInvariant(
					MIL_RT_ARCHITECTURE_URI != null && 
											!MIL_RT_ARCHITECTURE_URI.isEmpty(),
					HeaterUnitTestsSupervisor.class, instance,
					"MIL_RT_ARCHITECTURE_URI != null && "
					+ "!MIL_RT_ARCHITECTURE_URI.isEmpty()");
		ret &= InvariantChecking.checkGlassBoxInvariant(
					SIL_ARCHITECTURE_URI != null && 
											!SIL_ARCHITECTURE_URI.isEmpty(),
					HeaterUnitTestsSupervisor.class, instance,
					"SIL_ARCHITECTURE_URI != null && "
					+ "!SIL_ARCHITECTURE_URI.isEmpty()");
		ret &= InvariantChecking.checkGlassBoxInvariant(
					X_RELATIVE_POSITION >= 0,
					HeaterUnitTestsSupervisor.class, instance,
					"X_RELATIVE_POSITION >= 0");
		ret &= InvariantChecking.checkGlassBoxInvariant(
					Y_RELATIVE_POSITION >= 0,
					HeaterUnitTestsSupervisor.class, instance,
					"Y_RELATIVE_POSITION >= 0");
		// The other glass-box invariants are ensured by the fact that the
		// fields are final and that the constructor verifies the same 
		// assertions as preconditions
		return ret;
	}

	/**
	 * return true if the black-box invariants are observed, false otherwise.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code instance != null}
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @param instance	instance to be tested.
	 * @return			true if the black-box invariants are observed, false otherwise.
	 */
	protected static boolean	blackBoxInvariants(
		HeaterUnitTestsSupervisor instance
		)
	{
		assert instance != null : new PreconditionException("instance != null");

		boolean ret = true;
		return ret;
	}

	// -------------------------------------------------------------------------
	// Constructors
	// -------------------------------------------------------------------------

	/**
	 * create a supervisor component.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code !(this instanceof ComponentInterface)}
	 * pre	{@code currentSimulationType != null}
	 * pre	{@code !currentSimulationType.isSimulated() || (simArchitectureURI != null && !simArchitectureURI.isEmpty())}
	 * pre	{@code !currentSimulationType.isSimulated() || simulatedStartTime >= 0.0}
	 * pre	{@code !currentSimulationType.isSimulated() || simulationDuration > simulatedStartTime}
	 * pre	{@code !currentSimulationType.isSimulated() || simulationTimeUnit != null}
	 * pre	{@code !currentSimulationType.isMILRTSimulation() && !currentSimulationType.isSILSimulation() || accelerationFactor > 0.0}
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @param currentSimulationType	simulation type for the next run.
	 * @param simArchitectureURI	URI of the simulation architecture to be created or the empty string if the component does not execute as a simulation.
	 */
	protected			HeaterUnitTestsSupervisor(
		SimulationType currentSimulationType,
		String simArchitectureURI
		)
	{
		// one standard thread for execute, one standard for report reception
		// and one schedulable thread to schedule the start of MIL simulations
		super(2, 1);
		
		assert	currentSimulationType != null :
				new PreconditionException("currentExecutionType != null");
		assert	!currentSimulationType.isSimulated() || 
										(simArchitectureURI != null &&
												!simArchitectureURI.isEmpty()) :
				new PreconditionException(
					"!currentExecutionType.isSimulated() ||  "
					+ "(simArchitectureURI != null && "
					+ "!simArchitectureURI.isEmpty())");

		this.currentSimulationType = currentSimulationType;
		this.simArchitectureURI = simArchitectureURI;

		this.tracer.get().setTitle("Heater unit test supervisor");
		this.tracer.get().setRelativePosition(X_RELATIVE_POSITION,
											  Y_RELATIVE_POSITION);
		this.toggleTracing();

		assert	HeaterUnitTestsSupervisor.glassBoxInvariants(this) :
				new ImplementationInvariantException(
						"HeaterUnitTestsSupervisor.glassBoxInvariants(this)");
		assert	HeaterUnitTestsSupervisor.blackBoxInvariants(this) :
				new InvariantException(
						"HeaterUnitTestsSupervisor.blackBoxInvariants(this)");
	}

	// -------------------------------------------------------------------------
	// Component life-cycle
	// -------------------------------------------------------------------------

	/**
	 * @see fr.sorbonne_u.components.AbstractComponent#execute()
	 */
	@Override
	public void			execute() throws Exception
	{
		// First, the component must synchronise with other components
		// to start the execution of the test scenario; we use a
		// time-triggered synchronisation scheme with the accelerated clock
		ClocksServerWithSimulationOutboundPort clocksServerOutboundPort =
				new ClocksServerWithSimulationOutboundPort(this);
		clocksServerOutboundPort.publishPort();
		this.doPortConnection(
				clocksServerOutboundPort.getPortURI(),
				ClocksServer.STANDARD_INBOUNDPORT_URI,
				ClocksServerWithSimulationConnector.class.getCanonicalName());
		this.logMessage("HeaterUnitTestsSupervisor gets the clock.");
		AcceleratedAndSimulationClock acceleratedClock =
				clocksServerOutboundPort.getClockWithSimulation(
												CVM_HeaterUnitTest.CLOCK_URI);
		this.doPortDisconnection(clocksServerOutboundPort.getPortURI());
		clocksServerOutboundPort.unpublishPort();
		clocksServerOutboundPort.destroyPort();

		// compute the real time of start of the simulation using the
		// accelerated clock; this time is the start time of the
		// execution defined in the accelerated clock plus a delay defined
		// in the CVM class launching this execution 
		long simulationStartTimeInMillis = 
				TimeUnit.NANOSECONDS.toMillis(
							acceleratedClock.getSimulationStartEpochNanos());
		
		this.logMessage("HeaterUnitTestsSupervisor waits until start time.");
		acceleratedClock.waitUntilStart();
		this.logMessage("HeaterUnitTestsSupervisor starts.");

		switch (this.currentSimulationType) {
		case MIL_SIMULATION:
			// create the global simulation architecture given the type of
			// simulation for the current run
			ComponentModelArchitecture cma =
					createMILComponentSimulationArchitectures(
										this.simArchitectureURI,
										acceleratedClock.getSimulatedTimeUnit());
			// create and install the supervisor plug-in
			SupervisorPlugin sp = new SupervisorPlugin(cma);
			sp.setPluginURI(HeaterUnitTestsSupervisor.MIL_ARCHITECTURE_URI);
			this.installPlugin(sp);
			this.logMessage("plug-in installed.");
			// construct the global simulator
			sp.constructSimulator();
			this.logMessage("simulator constructed.");
			// initialise the simulation run parameters (it will set the model
			// logger to the component logger)
			sp.setSimulationRunParameters(new HashMap<>());
			acceleratedClock.waitUntilSimulationStart();
			// execute the MIL simulation
			sp.doStandAloneSimulation(0.0,
									  acceleratedClock.getSimulatedDuration());
			this.logMessage(sp.getFinalReport().toString());
			this.logMessage("simulation ends.");				
			break;
		case MIL_RT_SIMULATION:
			// create the global simulation architecture given the type of
			// simulation for the current run
			cma = createMILRTComponentSimulationArchitectures(
									this.simArchitectureURI,
									acceleratedClock.getSimulatedTimeUnit(),
									acceleratedClock.getAccelerationFactor());
			// create and install the supervisor plug-in
			sp = new SupervisorPlugin(cma);
			sp.setPluginURI(HeaterUnitTestsSupervisor.MIL_ARCHITECTURE_URI);
			this.installPlugin(sp);
			this.logMessage("plug-in installed.");
			// construct the global simulator
			sp.constructSimulator();
			this.logMessage("simulator constructed, simulation begins.");
			// initialise the simulation run parameters (it will set the model
			// logger to the component logger)
			sp.setSimulationRunParameters(new HashMap<>());
			// start the MIL real time simulation
			assert	simulationStartTimeInMillis > System.currentTimeMillis() :
					new BCMException(
							"simulationStartTimeInMillis > "
							+ "System.currentTimeMillis()");
			sp.startRTSimulation(simulationStartTimeInMillis,
								 acceleratedClock.getSimulatedStartTime(),
								 acceleratedClock.getSimulatedDuration());
			acceleratedClock.waitUntilSimulationEnd();
			Thread.sleep(250L);
			this.logMessage(sp.getFinalReport().toString());
			this.logMessage("simulation ends.");				
			break;
		case SIL_SIMULATION:
			// For SIL simulations in heater unit tests, there is only one
			// component, Heater, that executes a simulation; the component
			// starts it itself in its execute method
		default:
		}		
	}

	/**
	 * create the MIL component simulation architecture for the heater
	 * unit tests.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code true}	// no precondition.
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @param architectureURI	URI of the component simulation architecture to be created.
	 * @param simulatedTimeUnit	simulated time unit used in the architecture.
	 * @return					the global MIL simulation  architecture for the heater unit tests.
	 * @throws Exception		<i>to do</i>.
	 */
	@SuppressWarnings("unchecked")
	public static ComponentModelArchitecture
									createMILComponentSimulationArchitectures(
		String architectureURI, 
		TimeUnit simulatedTimeUnit
		) throws Exception
	{
		// map that will contain the atomic model descriptors to construct
		// the simulation architecture
		Map<String,AbstractAtomicModelDescriptor> atomicModelDescriptors =
															new HashMap<>();

		atomicModelDescriptors.put(
				HeaterCoupledModel.MIL_URI,
				ComponentAtomicModelDescriptor.create(
						HeaterCoupledModel.MIL_URI,
						(Class<? extends EventI>[]) new Class<?>[]{
							SwitchOnHeater.class,
							SwitchOffHeater.class,
							SetPowerHeater.class,
							Heat.class,
							DoNotHeat.class},
						(Class<? extends EventI>[]) new Class<?>[]{},
						simulatedTimeUnit,
						Heater.REFLECTION_INBOUND_PORT_URI
						));
		atomicModelDescriptors.put(
				HeaterUnitTesterModel.MIL_URI,
				ComponentAtomicModelDescriptor.create(
						HeaterUnitTesterModel.MIL_URI,
						(Class<? extends EventI>[]) new Class<?>[]{},
						(Class<? extends EventI>[]) new Class<?>[]{
							SwitchOnHeater.class,
							SwitchOffHeater.class,
							SetPowerHeater.class,
							Heat.class,
							DoNotHeat.class},
						simulatedTimeUnit,
						HeaterUser.REFLECTION_INBOUND_PORT_URI));

		// map that will contain the coupled model descriptors to construct
		// the simulation architecture
		Map<String,CoupledModelDescriptor> coupledModelDescriptors =
															new HashMap<>();

		// the set of submodels of the coupled model, given by their URIs
		Set<String> submodels = new HashSet<String>();
		submodels.add(HeaterCoupledModel.MIL_URI);
		submodels.add(HeaterUnitTesterModel.MIL_URI);

		// event exchanging connections between exporting and importing
		// models
		Map<EventSource,EventSink[]> connections =
									new HashMap<EventSource,EventSink[]>();
		connections.put(
			new EventSource(HeaterUnitTesterModel.MIL_URI,
							SwitchOnHeater.class),
			new EventSink[] {
				new EventSink(HeaterCoupledModel.MIL_URI,
							  SwitchOnHeater.class)
			});
		connections.put(
			new EventSource(HeaterUnitTesterModel.MIL_URI,
							SwitchOffHeater.class),
			new EventSink[] {
				new EventSink(HeaterCoupledModel.MIL_URI,
							  SwitchOffHeater.class)
			});
		connections.put(
			new EventSource(HeaterUnitTesterModel.MIL_URI,
							SetPowerHeater.class),
				new EventSink[] {
				new EventSink(HeaterCoupledModel.MIL_URI,
							  SetPowerHeater.class)
			});
		connections.put(
			new EventSource(HeaterUnitTesterModel.MIL_URI, Heat.class),
			new EventSink[] {
				new EventSink(HeaterCoupledModel.MIL_URI, Heat.class)
			});
		connections.put(
			new EventSource(HeaterUnitTesterModel.MIL_URI, DoNotHeat.class),
			new EventSink[] {
				new EventSink(HeaterCoupledModel.MIL_URI, DoNotHeat.class)
			});

		// coupled model descriptor
		coupledModelDescriptors.put(
				GlobalCoupledModel.MIL_URI,
				ComponentCoupledModelDescriptor.create(
						GlobalCoupledModel.class,
						GlobalCoupledModel.MIL_URI,
						submodels,
						null,
						null,
						connections,
						null,
						CoordinatorComponent.REFLECTION_INBOUND_PORT_URI,
						CoordinatorPlugin.class,
						null));

		ComponentModelArchitecture architecture =
				new ComponentModelArchitecture(
						architectureURI,
						GlobalCoupledModel.MIL_URI,
						atomicModelDescriptors,
						coupledModelDescriptors,
						simulatedTimeUnit);

		return architecture;
	}

	/**
	 * create the MIL real time component simulation architecture for the heater
	 * unit tests.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code true}	// no precondition.
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @param architectureURI		URI of the component simulation architecture to be created.
	 * @param simulatedTimeUnit		simulated time unit used in the architecture.
	 * @param accelerationFactor	acceleration factor for this run.
	 * @return						the global MIL real time component simulation architecture for the heater unit tests.
	 * @throws Exception			<i>to do</i>.
	 */
	@SuppressWarnings("unchecked")
	public static ComponentModelArchitecture
									createMILRTComponentSimulationArchitectures(
		String architectureURI, 
		TimeUnit simulatedTimeUnit,
		double accelerationFactor
		) throws Exception
	{
		// map that will contain the atomic model descriptors to construct
		// the simulation architecture
		Map<String,AbstractAtomicModelDescriptor> atomicModelDescriptors =
															new HashMap<>();

		atomicModelDescriptors.put(
				HeaterCoupledModel.MIL_RT_URI,
				RTComponentAtomicModelDescriptor.create(
						HeaterCoupledModel.MIL_RT_URI,
						(Class<? extends EventI>[]) new Class<?>[]{
							SwitchOnHeater.class,
							SwitchOffHeater.class,
							SetPowerHeater.class,
							Heat.class,
							DoNotHeat.class},
						(Class<? extends EventI>[]) new Class<?>[]{},
						simulatedTimeUnit,
						Heater.REFLECTION_INBOUND_PORT_URI
						));
		atomicModelDescriptors.put(
				HeaterUnitTesterModel.MIL_RT_URI,
				RTComponentAtomicModelDescriptor.create(
						HeaterUnitTesterModel.MIL_RT_URI,
						(Class<? extends EventI>[]) new Class<?>[]{},
						(Class<? extends EventI>[]) new Class<?>[]{
							SwitchOnHeater.class,
							SwitchOffHeater.class,
							SetPowerHeater.class,
							Heat.class,
							DoNotHeat.class},
						simulatedTimeUnit,
						HeaterUser.REFLECTION_INBOUND_PORT_URI));

		// map that will contain the coupled model descriptors to construct
		// the simulation architecture
		Map<String,CoupledModelDescriptor> coupledModelDescriptors =
															new HashMap<>();

		// the set of submodels of the coupled model, given by their URIs
		Set<String> submodels = new HashSet<String>();
		submodels.add(HeaterCoupledModel.MIL_RT_URI);
		submodels.add(HeaterUnitTesterModel.MIL_RT_URI);

		// event exchanging connections between exporting and importing
		// models
		Map<EventSource,EventSink[]> connections =
									new HashMap<EventSource,EventSink[]>();
		connections.put(
			new EventSource(HeaterUnitTesterModel.MIL_RT_URI,
							SwitchOnHeater.class),
			new EventSink[] {
				new EventSink(HeaterCoupledModel.MIL_RT_URI,
							  SwitchOnHeater.class)
			});
		connections.put(
			new EventSource(HeaterUnitTesterModel.MIL_RT_URI,
							SwitchOffHeater.class),
			new EventSink[] {
				new EventSink(HeaterCoupledModel.MIL_RT_URI,
							  SwitchOffHeater.class)
			});
		connections.put(
			new EventSource(HeaterUnitTesterModel.MIL_RT_URI,
							SetPowerHeater.class),
				new EventSink[] {
				new EventSink(HeaterCoupledModel.MIL_RT_URI,
							  SetPowerHeater.class)
			});
		connections.put(
			new EventSource(HeaterUnitTesterModel.MIL_RT_URI, Heat.class),
			new EventSink[] {
				new EventSink(HeaterCoupledModel.MIL_RT_URI, Heat.class)
			});
		connections.put(
			new EventSource(HeaterUnitTesterModel.MIL_RT_URI, DoNotHeat.class),
			new EventSink[] {
				new EventSink(HeaterCoupledModel.MIL_RT_URI, DoNotHeat.class)
			});

		// coupled model descriptor
		coupledModelDescriptors.put(
				GlobalCoupledModel.MIL_RT_URI,
				RTComponentCoupledModelDescriptor.create(
						GlobalCoupledModel.class,
						GlobalCoupledModel.MIL_RT_URI,
						submodels,
						null,
						null,
						connections,
						null,
						CoordinatorComponent.REFLECTION_INBOUND_PORT_URI,
						CoordinatorPlugin.class,
						null,
						accelerationFactor));

		ComponentModelArchitecture architecture =
				new RTComponentModelArchitecture(
						architectureURI,
						GlobalCoupledModel.MIL_RT_URI,
						atomicModelDescriptors,
						coupledModelDescriptors,
						simulatedTimeUnit);

		return architecture;
	}
}
// -----------------------------------------------------------------------------
