package fr.sorbonne_u.components.equipments.hairdryer.unittests;

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

import fr.sorbonne_u.components.CVMIntegrationTest;
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
import fr.sorbonne_u.components.equipments.hairdryer.HairDryer;
import fr.sorbonne_u.components.equipments.hairdryer.HairDryerUser;
import fr.sorbonne_u.components.equipments.hairdryer.mil.HairDryerCoupledModel;
import fr.sorbonne_u.components.equipments.hairdryer.mil.HairDryerUserModel;
import fr.sorbonne_u.components.equipments.hairdryer.mil.events.SetHighHairDryer;
import fr.sorbonne_u.components.equipments.hairdryer.mil.events.SetLowHairDryer;
import fr.sorbonne_u.components.equipments.hairdryer.mil.events.SwitchOffHairDryer;
import fr.sorbonne_u.components.equipments.hairdryer.mil.events.SwitchOnHairDryer;
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
 * The class <code>HairDryerUnitTestsSupervisor</code> implements the supervisor
 * component for simulated runs of the hair dryer unit tests.
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
 * <p>
 * This component is used in test execution types in conjunction with simulation
 * types other than {@code NO_SIMULATION}. Specifically for the hair dryer, only
 * MIL and MIL real time simulations use a supervisor component because in SIL
 * simulations only the {@code HairDryer} component runs a simulator which it
 * starts by itself without the need for a supervisor (and a coordinator)
 * component.
 * </p>
 * 
 * <p><strong>Glass-box Invariants</strong></p>
 * 
 * <pre>
 * invariant	{@code currentSimulationType != null}
 * invariant	{@code !currentSimulationType.isSimulated() || (simArchitectureURI != null && !simArchitectureURI.isEmpty())}
 * invariant	{@code !currentSimulationType.isSimulated() || simulatedStartTime >= 0.0}
 * invariant	{@code !currentSimulationType.isSimulated() || simulationDuration > simulatedStartTime}
 * invariant	{@code !currentSimulationType.isSimulated() || simulationTimeUnit != null}
 * invariant	{@code !currentSimulationType.isMILRTSimulation() && !currentSimulationType.isSILSimulation() || accelerationFactor > 0.0}
 * </pre>
 * 
 * <p><strong>Black-box Invariants</strong></p>
 * 
 * <pre>
 * invariant	{@code DELAY_TO_GET_REPORT > 0 && DELAY_TO_GET_REPORT < CVM_HairDryerUnitTest.DELAY_TO_STOP}
 * invariant	{@code MIL_ARCHITECTURE_URI != null && !MIL_ARCHITECTURE_URI.isEmpty()}
 * invariant	{@code MIL_RT_ARCHITECTURE_URI != null && !MIL_RT_ARCHITECTURE_URI.isEmpty()}
 * invariant	{@code SIL_ARCHITECTURE_URI != null && !SIL_ARCHITECTURE_URI.isEmpty()}
 * invariant	{@code X_RELATIVE_POSITION >= 0}
 * invariant	{@code Y_RELATIVE_POSITION #= 0}
 * </pre>
 * 
 * <p>Created on : 2023-11-13</p>
 * 
 * @author	<a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
 */
@RequiredInterfaces(required = {ClocksServerWithSimulationCI.class})
public class			HairDryerUnitTestsSupervisor
extends		AbstractCyPhyComponent
{
	// -------------------------------------------------------------------------
	// Constants and variables
	// -------------------------------------------------------------------------

	/** delay after the end of the simulation execution before getting the
	 *  simulation reports in real time simulation to let the simulators
	 *  perform their catering activities ending the simulation.		 	*/
	public static final long		DELAY_TO_GET_REPORT = 1000L;

	/** URI of the simulation architecture when a MIL simulation is
	 *  executed.															*/
	public static final String		MIL_ARCHITECTURE_URI =
												"hair-dryer-mil-simulator";
	/** URI of the simulation architecture when a MIL real time
	 *  simulation is executed.												*/
	public static final String		MIL_RT_ARCHITECTURE_URI =
												"hair-dryer-mil-rt-simulator";
	/** URI of the simulation architecture when a SIL simulation is
	 *  executed.															*/
	public static final String		SIL_ARCHITECTURE_URI =
												"hair-dryer-sil-simulator";

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
		HairDryerUnitTestsSupervisor instance
		)
	{
		assert instance != null : new PreconditionException("instance != null");

		boolean ret = true;
		// The glass-box invariants are ensured by the fact that the fields are
		// final and that the constructor verifies the same  assertions as
		// preconditions
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
		HairDryerUnitTestsSupervisor instance
		)
	{
		assert instance != null : new PreconditionException("instance != null");

		boolean ret = true;
		ret &= InvariantChecking.checkBlackBoxInvariant(
					DELAY_TO_GET_REPORT > 0 &&
						DELAY_TO_GET_REPORT <
										CVM_HairDryerUnitTest.DELAY_TO_STOP,
					HairDryerUnitTestsSupervisor.class, instance,
					"DELAY_TO_GET_REPORT > 0 && DELAY_TO_GET_REPORT < "
					+ "CVM_HairDryerUnitTest.DELAY_TO_STOP");
		ret &= InvariantChecking.checkBlackBoxInvariant(
					MIL_ARCHITECTURE_URI != null && 
										!MIL_ARCHITECTURE_URI.isEmpty(),
					HairDryerUnitTestsSupervisor.class, instance,
					"MIL_ARCHITECTURE_URI != null && "
					+ "!MIL_ARCHITECTURE_URI.isEmpty()");
		ret &= InvariantChecking.checkBlackBoxInvariant(
					MIL_RT_ARCHITECTURE_URI != null && 
										!MIL_RT_ARCHITECTURE_URI.isEmpty(),
					HairDryerUnitTestsSupervisor.class, instance,
					"MIL_RT_ARCHITECTURE_URI != null && "
					+ "!MIL_RT_ARCHITECTURE_URI.isEmpty()");
		ret &= InvariantChecking.checkBlackBoxInvariant(
					SIL_ARCHITECTURE_URI != null && 
										!SIL_ARCHITECTURE_URI.isEmpty(),
					HairDryerUnitTestsSupervisor.class, instance,
					"SIL_ARCHITECTURE_URI != null && "
					+ "!SIL_ARCHITECTURE_URI.isEmpty()");
		ret &= InvariantChecking.checkBlackBoxInvariant(
					X_RELATIVE_POSITION >= 0,
					HairDryerUnitTestsSupervisor.class, instance,
					"X_RELATIVE_POSITION >= 0");
		ret &= InvariantChecking.checkBlackBoxInvariant(
					Y_RELATIVE_POSITION >= 0,
					HairDryerUnitTestsSupervisor.class, instance,
					"Y_RELATIVE_POSITION >= 0");
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
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @param currentSimulationType	simulation type for the next run.
	 * @param simArchitectureURI	URI of the simulation architecture to be created or the empty string if the component does not execute as a simulation.
	 */
	protected			HairDryerUnitTestsSupervisor(
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

		this.tracer.get().setTitle("HairDryer unit test supervisor");
		this.tracer.get().setRelativePosition(X_RELATIVE_POSITION,
											  Y_RELATIVE_POSITION);
		this.toggleTracing();

		assert	HairDryerUnitTestsSupervisor.glassBoxInvariants(this) :
				new ImplementationInvariantException(
						"HairDryerUnitTestsSupervisor.glassBoxInvariants(this)");
		assert	HairDryerUnitTestsSupervisor.blackBoxInvariants(this) :
				new InvariantException(
						"HairDryerUnitTestsSupervisor.blackBoxInvariants(this)");
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
		this.logMessage("HairDryerUnitTestsSupervisor gets the clock.");
		AcceleratedAndSimulationClock acceleratedClock =
			clocksServerOutboundPort.getClockWithSimulation(
												CVMIntegrationTest.CLOCK_URI);
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
		
		this.logMessage("HairDryerUnitTestsSupervisor waits until start time.");
		acceleratedClock.waitUntilStart();
		this.logMessage("HairDryerUnitTestsSupervisor starts.");

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
			sp.setPluginURI(HairDryerUnitTestsSupervisor.MIL_ARCHITECTURE_URI);
			this.installPlugin(sp);
			this.logMessage("plug-in installed.");
			// construct the global simulator
			sp.constructSimulator();
			this.logMessage("simulator constructed.");
			// initialise the simulation run parameters (it will set the model
			// logger to the component logger)
			sp.setSimulationRunParameters(new HashMap<>());
			// execute the MIL simulation
			acceleratedClock.waitUntilSimulationStart();
			logMessage("simulation begins.");
			sp.doStandAloneSimulation(
								0.0, acceleratedClock.getSimulatedDuration());
			logMessage("simulation ends.");				
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
			sp.setPluginURI(HairDryerUnitTestsSupervisor.MIL_ARCHITECTURE_URI);
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
			// wait until the end of the simulation
			acceleratedClock.waitUntilSimulationEnd();
			// give some time for the end of simulation catering tasks
			Thread.sleep(200L);
			this.logMessage(sp.getFinalReport().toString());
			break;
		case SIL_SIMULATION:
			// For SIL simulations in hair dryer unit tests, there is only one
			// component, HairDryer, that executes a simulation; the component
			// starts it itself in its execute method
		default:
		}		
	}

	// -------------------------------------------------------------------------
	// Methods
	// -------------------------------------------------------------------------

	/**
	 * create the MIL component simulation architecture for the hair dryer
	 * unit tests.
	 * 
	 * <p><strong>Description</strong></p>
	 * 
	 * <p>
	 * In hair dryer unit tests, the component assembly has two components:
	 * {@code HairDryer} and {@code HairDryerUser}. The unit test component
	 * MIL simulation architecture is:
	 * </p>
	 * <p><img src="../../../../../../../../images/hem-2024-e3/HairDryerUnitTestMILComponentArchitecture.png"/></p>
	 * <p>
	 * Compared to the MIL simulation architecture, the
	 * {@code HairDryerUserModel} becomes the sole atomic model in the
	 * {@code HairDryerUser} local simulation architecture while the
	 * {@code HairDryer} local architecture sees its
	 * {@code HairDryerCoupledModel} import the hair dryer events and transmit
	 * them to a new atomic model {@code HairDryerStateModel}. This latter model
	 * is introduced in preparation for the integration testing SIL simulation
	 * where the {@code HairDryerElectricityModel} will have to be co-localised
	 * with the {@code ElectricMeterElectricityModel} in the
	 * {@code ElectricMeter} component. It keeps track of the hair dryer state
	 * and mode as well as forwarding the hair dryer events to the
	 * {@code HairDryerElectricityModel}.
	 * </p>
	 * <p>
	 * The {@code HairDryerUnitTestSupervisor} is introduced to compose and
	 * supervise the global simulation architecture. This method creates the
	 * component simulation architecture 
	 * </p>
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code true}	// no precondition.
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @param architectureURI	URI of the component model architecture to be created.
	 * @param simulatedTimeUnit	simulated time unit used in the architecture.
	 * @return					the global MIL simulation  architecture for the hair dryer.
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

		// The HairDryer simulator is the composition of two atomic models, the
		// HairDryerStateModel and the HairDryerElectricityModel, to get the
		// HairDryerCoupledModel. In the overall unit test component simulation
		// model, this coupled model is seen as an atomic model, hiding the
		// internal local simulation architecture of the HairDryer behind the
		// closure of DEVS models composition principle (a coupled model can be
		// seen as an atomic model).
		atomicModelDescriptors.put(
				HairDryerCoupledModel.MIL_URI,
				ComponentAtomicModelDescriptor.create(
						HairDryerCoupledModel.MIL_URI,
						(Class<? extends EventI>[]) new Class<?>[]{
							SwitchOnHairDryer.class,
							SwitchOffHairDryer.class,
							SetLowHairDryer.class,
							SetHighHairDryer.class},
						(Class<? extends EventI>[]) new Class<?>[]{},
						simulatedTimeUnit,
						HairDryer.REFLECTION_INBOUND_PORT_URI
						));
		// The HairDryerUser simulator is made of only one atomic model, which
		// is therefore seen as an atomic model also at the component simulation
		// architecture level.
		atomicModelDescriptors.put(
				HairDryerUserModel.MIL_URI,
				ComponentAtomicModelDescriptor.create(
						HairDryerUserModel.MIL_URI,
						(Class<? extends EventI>[]) new Class<?>[]{},
						(Class<? extends EventI>[]) new Class<?>[]{
							SwitchOnHairDryer.class,
							SwitchOffHairDryer.class,
							SetLowHairDryer.class,
							SetHighHairDryer.class},
						simulatedTimeUnit,
						HairDryerUser.REFLECTION_INBOUND_PORT_URI));

		// map that will contain the coupled model descriptors to construct
		// the simulation architecture
		Map<String,CoupledModelDescriptor> coupledModelDescriptors =
															new HashMap<>();

		// the set of submodels of the coupled model, given by their URIs
		Set<String> submodels = new HashSet<String>();
		submodels.add(HairDryerCoupledModel.MIL_URI);
		submodels.add(HairDryerUserModel.MIL_URI);

		// event exchanging connections between exporting and importing
		// models
		Map<EventSource,EventSink[]> connections =
									new HashMap<EventSource,EventSink[]>();
		connections.put(
			new EventSource(HairDryerUserModel.MIL_URI,
							SwitchOnHairDryer.class),
			new EventSink[] {
				new EventSink(HairDryerCoupledModel.MIL_URI,
							  SwitchOnHairDryer.class)
			});
		connections.put(
			new EventSource(HairDryerUserModel.MIL_URI,
							SwitchOffHairDryer.class),
			new EventSink[] {
				new EventSink(HairDryerCoupledModel.MIL_URI,
							  SwitchOffHairDryer.class)
			});
		connections.put(
			new EventSource(HairDryerUserModel.MIL_URI,
							SetLowHairDryer.class),
				new EventSink[] {
				new EventSink(HairDryerCoupledModel.MIL_URI,
							  SetLowHairDryer.class)
			});
		connections.put(
			new EventSource(HairDryerUserModel.MIL_URI,
							SetHighHairDryer.class),
			new EventSink[] {
				new EventSink(HairDryerCoupledModel.MIL_URI,
							  SetHighHairDryer.class)
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
	 * create the MIL real time component simulation architecture for the hair
	 * dryer unit tests.
	 * 
	 * <p><strong>Description</strong></p>
	 * 
	 * <p>
	 * The unit test component MIL real time simulation architecture is the
	 * same as the component MIL simulation architecture created by
	 * {@code createMILComponentSimulationArchitectures} except that they
	 * are real time. 
	 * </p>
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code true}	// no precondition.
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @param architectureURI		URI of the component model architecture to be created.
	 * @param simulatedTimeUnit		simulated time unit used in the architecture.
	 * @param accelerationFactor	acceleration factor for this run.
	 * @return						the global MIL real time component simulation architecture for the hair dryer unit tests.
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

		// The HairDryer simulator is the composition of two atomic models, the
		// HairDryerStateModel and the HairDryerElectricityModel, to get the
		// HairDryerCoupledModel. In the overall unit test component simulation
		// model, this coupled model is seen as an atomic model, hiding the
		// internal local simulation architecture of the HairDryer behind the
		// closure of DEVS models composition principle (a coupled model can be
		// seen as an atomic model).
		atomicModelDescriptors.put(
				HairDryerCoupledModel.MIL_RT_URI,
				RTComponentAtomicModelDescriptor.create(
						HairDryerCoupledModel.MIL_RT_URI,
						(Class<? extends EventI>[]) new Class<?>[]{
							SwitchOnHairDryer.class,
							SwitchOffHairDryer.class,
							SetLowHairDryer.class,
							SetHighHairDryer.class},
						(Class<? extends EventI>[]) new Class<?>[]{},
						simulatedTimeUnit,
						HairDryer.REFLECTION_INBOUND_PORT_URI
						));
		// The HairDryerUser simulator is made of only one atomic model, which
		// is therefore seen as an atomic model also at the component simulation
		// architecture level.
		atomicModelDescriptors.put(
				HairDryerUserModel.MIL_RT_URI,
				RTComponentAtomicModelDescriptor.create(
						HairDryerUserModel.MIL_RT_URI,
						(Class<? extends EventI>[]) new Class<?>[]{},
						(Class<? extends EventI>[]) new Class<?>[]{
							SwitchOnHairDryer.class,
							SwitchOffHairDryer.class,
							SetLowHairDryer.class,
							SetHighHairDryer.class},
						simulatedTimeUnit,
						HairDryerUser.REFLECTION_INBOUND_PORT_URI));

		// map that will contain the coupled model descriptors to construct
		// the simulation architecture
		Map<String,CoupledModelDescriptor> coupledModelDescriptors =
															new HashMap<>();

		// the set of submodels of the coupled model, given by their URIs
		Set<String> submodels = new HashSet<String>();
		submodels.add(HairDryerCoupledModel.MIL_RT_URI);
		submodels.add(HairDryerUserModel.MIL_RT_URI);

		// event exchanging connections between exporting and importing
		// models
		Map<EventSource,EventSink[]> connections =
									new HashMap<EventSource,EventSink[]>();
		connections.put(
			new EventSource(HairDryerUserModel.MIL_RT_URI,
							SwitchOnHairDryer.class),
			new EventSink[] {
				new EventSink(HairDryerCoupledModel.MIL_RT_URI,
							  SwitchOnHairDryer.class)
			});
		connections.put(
			new EventSource(HairDryerUserModel.MIL_RT_URI,
							SwitchOffHairDryer.class),
			new EventSink[] {
				new EventSink(HairDryerCoupledModel.MIL_RT_URI,
							  SwitchOffHairDryer.class)
			});
		connections.put(
			new EventSource(HairDryerUserModel.MIL_RT_URI,
							SetLowHairDryer.class),
				new EventSink[] {
				new EventSink(HairDryerCoupledModel.MIL_RT_URI,
							  SetLowHairDryer.class)
			});
		connections.put(
			new EventSource(HairDryerUserModel.MIL_RT_URI,
							SetHighHairDryer.class),
			new EventSink[] {
				new EventSink(HairDryerCoupledModel.MIL_RT_URI,
							  SetHighHairDryer.class)
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
