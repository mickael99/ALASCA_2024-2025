package fr.sorbonne_u.components;



import java.util.HashMap;
import java.util.concurrent.TimeUnit;

import fr.sorbonne_u.components.annotations.RequiredInterfaces;
import fr.sorbonne_u.components.cyphy.AbstractCyPhyComponent;
import fr.sorbonne_u.components.cyphy.plugins.devs.SupervisorPlugin;
import fr.sorbonne_u.components.cyphy.plugins.devs.architectures.ComponentModelArchitecture;
import fr.sorbonne_u.components.cyphy.utils.aclocks.AcceleratedAndSimulationClock;
import fr.sorbonne_u.components.cyphy.utils.aclocks.ClocksServerWithSimulationCI;
import fr.sorbonne_u.components.cyphy.utils.aclocks.ClocksServerWithSimulationConnector;
import fr.sorbonne_u.components.cyphy.utils.aclocks.ClocksServerWithSimulationOutboundPort;
import fr.sorbonne_u.components.utils.SimulationType;
import fr.sorbonne_u.exceptions.PreconditionException;
import fr.sorbonne_u.utils.aclocks.ClocksServer;

@RequiredInterfaces(required = {ClocksServerWithSimulationCI.class})
public class			GlobalSupervisor
extends		AbstractCyPhyComponent
{
	// -------------------------------------------------------------------------
	// Constants and variables
	// -------------------------------------------------------------------------

	public static boolean		VERBOSE = false;
	public static int			X_RELATIVE_POSITION = 0;
	public static int			Y_RELATIVE_POSITION = 0;
															
	public static final String	MIL_SIM_ARCHITECTURE_URI = "hem-mil-simulator";											
	public static final String	MIL_RT_SIM_ARCHITECTURE_URI =
														"hem-mil-rt-simulator";
														
	public static final String	SIL_SIM_ARCHITECTURE_URI = "hem-sil-simulator";

	// Execution/Simulation

	protected final SimulationType	currentSimulationType;
	protected final String			simArchitectureURI;

	// -------------------------------------------------------------------------
	// Constructors
	// -------------------------------------------------------------------------

	/**
	 * create a supervisor component.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code true}	// no precondition.
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @param currentSimulationType			simulation type for the next run.
	 * @param simArchitectureURI			URI of the simulation architecture to be created or the empty string if the component does not execute as a simulation.
	 */
	protected			GlobalSupervisor(
		SimulationType currentSimulationType,
		String simArchitectureURI
		)
	{
		// one thread for execute and one for report reception
		super(2, 0);
		
		assert	currentSimulationType != null :
				new PreconditionException("currentExecutionType != null");
		assert	currentSimulationType.isSimulated() || 
										(simArchitectureURI != null &&
												!simArchitectureURI.isEmpty()) :
				new PreconditionException(
					"currentExecutionType.isSimulated() ||  "
					+ "(simArchitectureURI != null && "
					+ "!simArchitectureURI.isEmpty())");

		this.currentSimulationType = currentSimulationType;
		this.simArchitectureURI = simArchitectureURI;

		this.tracer.get().setTitle("Global supervisor");
		this.tracer.get().setRelativePosition(X_RELATIVE_POSITION,
											  Y_RELATIVE_POSITION);
		this.toggleTracing();
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
		// First, get the clock and wait until the start time that it specifies.
		AcceleratedAndSimulationClock ac = null;
		ClocksServerWithSimulationOutboundPort clocksServerOutboundPort =
							new ClocksServerWithSimulationOutboundPort(this);
		clocksServerOutboundPort.publishPort();
		this.doPortConnection(
					clocksServerOutboundPort.getPortURI(),
					ClocksServer.STANDARD_INBOUNDPORT_URI,
					ClocksServerWithSimulationConnector.class.getCanonicalName());
		this.logMessage("HEM gets the clock.");
		ac = clocksServerOutboundPort.
						getClockWithSimulation(CVMIntegrationTest.CLOCK_URI);
		this.doPortDisconnection(clocksServerOutboundPort.getPortURI());
		clocksServerOutboundPort.unpublishPort();
		clocksServerOutboundPort.destroyPort();
		this.logMessage("HEM waits until start time.");
		ac.waitUntilStart();
		this.logMessage("HEM starts.");

		switch (this.currentSimulationType) {
		case MIL_SIMULATION:
			ComponentModelArchitecture cma =
					ComponentSimulationArchitectures.
						createMILComponentSimulationArchitectures(
													this.simArchitectureURI,
													ac.getSimulatedTimeUnit());
			SupervisorPlugin sp = new SupervisorPlugin(cma);
			sp.setPluginURI(GlobalSupervisor.MIL_SIM_ARCHITECTURE_URI);
			this.installPlugin(sp);
			this.logMessage("plug-in installed.");
			sp.constructSimulator();
			this.logMessage("simulator constructed, simulation begins.");
			sp.setSimulationRunParameters(new HashMap<>());

			sp.doStandAloneSimulation(0.0, ac.getSimulatedDuration());
			this.logMessage("simulation ends.");
			break;
		case MIL_RT_SIMULATION:
			cma = ComponentSimulationArchitectures.
							createMILRTComponentSimulationArchitectures(
													this.simArchitectureURI,
													ac.getSimulatedTimeUnit(),
													ac.getAccelerationFactor());
			sp = new SupervisorPlugin(cma);
			sp.setPluginURI(GlobalSupervisor.MIL_SIM_ARCHITECTURE_URI);
			this.installPlugin(sp);
			this.logMessage("plug-in installed.");
			sp.constructSimulator();
			this.logMessage("simulator constructed, simulation begins.");
			sp.setSimulationRunParameters(new HashMap<>());
			sp.startRTSimulation(TimeUnit.NANOSECONDS.toMillis(
											ac.getSimulationStartEpochNanos()),
								 ac.getSimulatedStartTime(),
								 ac.getSimulatedDuration());
			// wait for the end of the simulation
			ac.waitUntilSimulationEnd();
			// leave some time for the simulators end of simulation catering
			// tasks
			Thread.sleep(250L);
			this.logMessage(sp.getFinalReport().toString());
			break;
		case SIL_SIMULATION:
			cma = ComponentSimulationArchitectures.
							createSILComponentSimulationArchitectures(
													this.simArchitectureURI,
													ac.getSimulatedTimeUnit(),
													ac.getAccelerationFactor());
			sp = new SupervisorPlugin(cma);
			sp.setPluginURI(GlobalSupervisor.SIL_SIM_ARCHITECTURE_URI);
			this.installPlugin(sp);
			this.logMessage("plug-in installed.");
			sp.constructSimulator();
			this.logMessage("simulator constructed, simulation begins.");
			sp.setSimulationRunParameters(new HashMap<>());
			sp.startRTSimulation(TimeUnit.NANOSECONDS.toMillis(
											ac.getSimulationStartEpochNanos()),
								 ac.getSimulatedStartTime(),
								 ac.getSimulatedDuration());
			// wait for the end of the simulation
			ac.waitUntilSimulationEnd();
			// leave some time for the simulators end of simulation catering
			// tasks
			Thread.sleep(250L);
			this.logMessage(sp.getFinalReport().toString());
			break;
		default:
		}		
	}
}
// -----------------------------------------------------------------------------
