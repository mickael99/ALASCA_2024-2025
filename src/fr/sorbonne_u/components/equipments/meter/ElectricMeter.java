package fr.sorbonne_u.components.equipments.meter;

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

import fr.sorbonne_u.components.annotations.OfferedInterfaces;
import fr.sorbonne_u.components.annotations.RequiredInterfaces;
import fr.sorbonne_u.components.cyphy.AbstractCyPhyComponent;
import fr.sorbonne_u.components.cyphy.plugins.devs.AtomicSimulatorPlugin;
import fr.sorbonne_u.components.cyphy.plugins.devs.RTAtomicSimulatorPlugin;
import fr.sorbonne_u.components.equipments.meter.connections.ElectricMeterInboundPort;
import fr.sorbonne_u.components.equipments.meter.interfaces.ElectricMeterCI;
import fr.sorbonne_u.components.equipments.meter.interfaces.ElectricMeterImplementationI;
import fr.sorbonne_u.components.equipments.meter.mil.LocalSimulationArchitectures;
import fr.sorbonne_u.components.exceptions.ComponentShutdownException;
import fr.sorbonne_u.components.exceptions.ComponentStartException;
import fr.sorbonne_u.components.utils.*;
import fr.sorbonne_u.devs_simulation.architectures.Architecture;
import fr.sorbonne_u.devs_simulation.utils.InvariantChecking;
import fr.sorbonne_u.exceptions.ImplementationInvariantException;
import fr.sorbonne_u.exceptions.InvariantException;
import fr.sorbonne_u.exceptions.PostconditionException;
import fr.sorbonne_u.exceptions.PreconditionException;
import fr.sorbonne_u.utils.aclocks.*;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

// -----------------------------------------------------------------------------

@OfferedInterfaces(offered={ElectricMeterCI.class})
@RequiredInterfaces(required = {ClocksServerCI.class})
public class ElectricMeter extends AbstractCyPhyComponent implements ElectricMeterImplementationI {
	
	// -------------------------------------------------------------------------
	// Constants and variables
	// -------------------------------------------------------------------------

	public static final Measure<Double>	ELECTRIC_METER_VOLTAGE =
			new Measure<Double>(220.0, MeasurementUnit.VOLTS);

	public static final String	ELECTRIC_METER_INBOUND_PORT_URI =
															"ELECTRIC-METER";
	public static final String CONSUMPTION_INBOUND_PORT_URI = "CONSUMPTION_INBOUND_PORT_URI";
	public static final String PRODUCTION_INBOUND_PORT_URI = "PRODUCTION_INBOUND_PORT_URI";

	public static final String	REFLECTION_INBOUND_PORT_URI =
			"ELECTRIC-METER-RIP-URI";

	/** when true, methods trace their actions.								*/
	public static boolean		VERBOSE = true;
	/** when tracing, x coordinate of the window relative position.			*/
	public static int			X_RELATIVE_POSITION = 0;
	/** when tracing, y coordinate of the window relative position.			*/
	public static int			Y_RELATIVE_POSITION = 0;

	/** inbound port offering the <code>ElectricMeterCI</code> interface.	*/
	protected ElectricMeterInboundPort	emip;

	/** current total electric power consumption measured at the electric
	 *  meter in amperes.												 	*/
	protected AtomicReference<SensorData<Measure<Double>>>
			currentPowerConsumption;
	/** current total electric power production measured at the electric
	 *  meter in watts.													 	*/
	protected AtomicReference<SensorData<Measure<Double>>>
			currentPowerProduction;

	// Execution/Simulation

	/** current type of execution.											*/
	protected ExecutionType currentExecutionType;
	/** current type of simulation.											*/
	protected SimulationType currentSimulationType;

	/** URI of the clock to be used to synchronise the test scenarios and
	 *  the simulation.														*/
	protected String				clockURI;
	/** accelerated clock, in integration and SIL simulation tests.			*/
	protected AcceleratedClock clock;
	/** URI of the simulation architecture to be created or the empty string
	 *  if the component does not execute as a SIL simulation.				*/
	protected String				globalArchitectureURI;
	/** URI of the local simulator used to compose the global simulation
	 *  architecture.														*/
	protected String				localArchitectureURI;
	/** time unit in which times and durations are expressed in the
	 *  simulators.															*/
	protected TimeUnit simulationTimeUnit;
	/** acceleration factor to be used when running the real time
	 *  simulation.															*/
	protected double					accFactor;

	// -------------------------------------------------------------------------
	// Invariants
	// -------------------------------------------------------------------------

	protected static boolean	glassBoxInvariants(ElectricMeter instance)
	{
		assert 	instance != null : new PreconditionException("instance != null");

		boolean ret = true;
		ret &= InvariantChecking.checkGlassBoxInvariant(
				instance.currentExecutionType != null,
				ElectricMeter.class, instance,
				"currentExecutionType != null");
		ret &= InvariantChecking.checkGlassBoxInvariant(
				instance.currentSimulationType != null,
				ElectricMeter.class, instance,
				"hcurrentSimulationType != null");
		ret &= InvariantChecking.checkGlassBoxInvariant(
				!instance.currentExecutionType.isStandard() ||
				instance.currentSimulationType.isNoSimulation(),
				ElectricMeter.class, instance,
				"!currentExecutionType.isStandard() || "
				+ "currentSimulationType.isNoSimulation()");
		ret &= InvariantChecking.checkGlassBoxInvariant(
				instance.currentSimulationType.isNoSimulation() ||
				(instance.globalArchitectureURI != null &&
				 !instance.globalArchitectureURI.isEmpty()),
				ElectricMeter.class, instance,
				"currentSimulationType.isNoSimulation() || "
				+ "(globalArchitectureURI != null && "
				+ "!globalArchitectureURI.isEmpty())");
		ret &= InvariantChecking.checkGlassBoxInvariant(
				instance.currentSimulationType.isNoSimulation() ||
				(instance.localArchitectureURI != null &&
				 !instance.localArchitectureURI.isEmpty()),
				ElectricMeter.class, instance,
				"currentSimulationType.isNoSimulation() || "
				+ "(localArchitectureURI != null && "
				+ "!localArchitectureURI.isEmpty())");
		ret &= InvariantChecking.checkGlassBoxInvariant(
				!instance.currentSimulationType.isSimulated() ||
				instance.simulationTimeUnit != null,
				ElectricMeter.class, instance,
				"!currentSimulationType.isSimulated() || "
				+ "simulationTimeUnit != null");
		ret &= InvariantChecking.checkGlassBoxInvariant(
				!instance.currentSimulationType.isRealTimeSimulation() ||
				instance.accFactor > 0.0,
				ElectricMeter.class, instance,
				"!hd.currentSimulationType.isRealTimeSimulation() || "
				+ "hd.accFactor > 0.0");
		return ret;
	}

	protected static boolean	blackBoxInvariants(ElectricMeter instance)
	{
		assert 	instance != null : new PreconditionException("instance != null");

		boolean ret = true;
		ret &= InvariantChecking.checkBlackBoxInvariant(
				ELECTRIC_METER_VOLTAGE != null &&
				ELECTRIC_METER_VOLTAGE.getData() > 0.0,
				ElectricMeter.class, instance,
				"ELECTRIC_METER_VOLTAGE != null && "
				+ "ELECTRIC_METER_VOLTAGE.getData() > 0.0");
		ret &= InvariantChecking.checkBlackBoxInvariant(
				REFLECTION_INBOUND_PORT_URI != null &&
				!REFLECTION_INBOUND_PORT_URI.isEmpty(),
				ElectricMeter.class, instance,
				"REFLECTION_INBOUND_PORT_URI != null && "
				+ "!REFLECTION_INBOUND_PORT_URI.isEmpty()");
		ret &= InvariantChecking.checkBlackBoxInvariant(
				ELECTRIC_METER_INBOUND_PORT_URI != null &&
				!ELECTRIC_METER_INBOUND_PORT_URI.isEmpty(),
				ElectricMeter.class, instance,
				"ELECTRIC_METER_INBOUND_PORT_URI != null && "
				+ "!ELECTRIC_METER_INBOUND_PORT_URI.isEmpty()");
		ret &= InvariantChecking.checkBlackBoxInvariant(
				X_RELATIVE_POSITION >= 0,
				ElectricMeter.class, instance,
				"X_RELATIVE_POSITION >= 0");
		ret &= InvariantChecking.checkBlackBoxInvariant(
				Y_RELATIVE_POSITION >= 0,
				ElectricMeter.class, instance,
				"Y_RELATIVE_POSITION >= 0");
		return ret;
	}



	// -------------------------------------------------------------------------
	// Constructors
	// -------------------------------------------------------------------------

	/**
	 * create an electric meter component.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code ELECTRIC_METER_INBOUND_PORT_URI != null && !ELECTRIC_METER_INBOUND_PORT_URI.isEmpty()}
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 * 
	 * @throws Exception	<i>to do</i>.
	 */
	protected			ElectricMeter() throws Exception
	{
		this(ELECTRIC_METER_INBOUND_PORT_URI);

		assert	ELECTRIC_METER_INBOUND_PORT_URI != null &&
								!ELECTRIC_METER_INBOUND_PORT_URI.isEmpty() :
				new PreconditionException(
						"ELECTRIC_METER_INBOUND_PORT_URI != null && "
						+ "!ELECTRIC_METER_INBOUND_PORT_URI.isEmpty()");
	}

	/**
	 * create an electric meter component.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code electricMeterInboundPortURI != null && !electricMeterInboundPortURI.isEmpty()}
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @param electricMeterInboundPortURI	URI of the electric meter inbound port.
	 * @throws Exception					<i>to do</i>.
	 */
	protected			ElectricMeter(
		String electricMeterInboundPortURI
		) throws Exception
	{
		this(REFLECTION_INBOUND_PORT_URI, electricMeterInboundPortURI,
			 ExecutionType.STANDARD, SimulationType.NO_SIMULATION,
			 null, null, null, 0.0, null);
	}

	protected			ElectricMeter(
			String electricMeterInboundPortURI,
			ExecutionType currentExecutionType
									  ) throws Exception
	{
		this(REFLECTION_INBOUND_PORT_URI, electricMeterInboundPortURI,
			 currentExecutionType, SimulationType.NO_SIMULATION,
			 null, null, null, 0.0, null);

		assert	currentExecutionType.isTest() :
				new PreconditionException("currentExecutionType.isTest()");
	}

	protected			ElectricMeter(
			String reflectionInboundPortURI,
			String electricMeterInboundPortURI,
			ExecutionType currentExecutionType,
			SimulationType currentSimulationType,
			String globalArchitectureURI,
			String localArchitectureURI,
			TimeUnit simulationTimeUnit,
			double accFactor,
			String clockURI
									  ) throws Exception
	{
		super(reflectionInboundPortURI, 2, 0);

		assert	electricMeterInboundPortURI != null &&
				  !electricMeterInboundPortURI.isEmpty() :
				new PreconditionException(
						"electricMeterInboundPortURI != null && "
						+ "!electricMeterInboundPortURI.isEmpty()");
		assert	currentExecutionType != null :
				new PreconditionException("currentExecutionType != null");
		assert	currentSimulationType.isNoSimulation() ||
				  (globalArchitectureURI != null &&
				   !globalArchitectureURI.isEmpty()) :
				new PreconditionException(
						"currentSimulationType.isNoSimulation() ||  "
						+ "(globalArchitectureURI != null && "
						+ "!globalArchitectureURI.isEmpty())");
		assert	currentSimulationType.isNoSimulation() ||
				  (localArchitectureURI != null &&
				   !localArchitectureURI.isEmpty()) :
				new PreconditionException(
						"currentSimulationType.isNoSimulation() ||  "
						+ "(localArchitectureURI != null && "
						+ "!localArchitectureURI.isEmpty())");
		assert	!currentSimulationType.isSimulated() ||
				  simulationTimeUnit != null :
				new PreconditionException(
						"!currentSimulationType.isSimulated() || "
						+ "simulationTimeUnit != null");
		assert	!currentSimulationType.isRealTimeSimulation() || accFactor > 0.0 :
				new PreconditionException(
						"!currentSimulationType.isRealTimeSimulation() || "
						+ "accFactor > 0.0");
		// doing simulations for unit test makes no sense as the meter must
		// gather the power consumption of other appliances and the power
		// production of production units
		assert	!currentExecutionType.isUnitTest() ||
				  currentSimulationType.isNoSimulation() :
				new PreconditionException(
						"!currentExecutionType.isUnitTest() || "
						+ "currentSimulationType.isNoSimulation()");

		this.currentExecutionType = currentExecutionType;
		this.currentSimulationType = currentSimulationType;
		this.globalArchitectureURI = globalArchitectureURI;
		this.localArchitectureURI = localArchitectureURI;
		this.simulationTimeUnit = simulationTimeUnit;
		this.clockURI = clockURI;
		this.accFactor = accFactor;

		this.initialise(electricMeterInboundPortURI);

		assert	ElectricMeter.glassBoxInvariants(this) :
				new ImplementationInvariantException(
						"ElectricMeter.glassBoxInvariants(this)");
		assert	ElectricMeter.blackBoxInvariants(this) :
				new InvariantException("ElectricMeter.blackBoxInvariants(this)");
	}

	/**
	 * create an electric meter component.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code electricMeterInboundPortURI != null && !electricMeterInboundPortURI.isEmpty()}
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @param reflectionInboundPortURI		URI of the reflection innbound port of the component.
	 * @param electricMeterInboundPortURI	URI of the electric meter inbound port.
	 * @param nbThreads						number of standard threads.
	 * @param nbSchedulableThreads			number of schedulable threads.
	 * @throws Exception					<i>to do</i>.
	 */
	protected			ElectricMeter(
		String reflectionInboundPortURI,
		String electricMeterInboundPortURI,
		int nbThreads,
		int nbSchedulableThreads
		) throws Exception
	{
		super(reflectionInboundPortURI, nbThreads, nbSchedulableThreads);

		assert	electricMeterInboundPortURI != null &&
										!electricMeterInboundPortURI.isEmpty() :
				new PreconditionException(
						"electricMeterInboundPortURI != null && "
						+ "!electricMeterInboundPortURI.isEmpty()");

		this.initialise(electricMeterInboundPortURI);
	}

	/**
	 * initialise an electric meter component.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code electricMeterInboundPortURI != null && !electricMeterInboundPortURI.isEmpty()}
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @param electricMeterInboundPortURI	URI of the electric meter inbound port.
	 * @throws Exception					<i>to do</i>.
	 */
	protected void		initialise(String electricMeterInboundPortURI)
	throws Exception
	{
		assert	electricMeterInboundPortURI != null &&
										!electricMeterInboundPortURI.isEmpty() :
				new PreconditionException(
						"electricMeterInboundPortURI != null && "
						+ "!electricMeterInboundPortURI.isEmpty()");

		this.emip =
				new ElectricMeterInboundPort(electricMeterInboundPortURI, this);
		this.emip.publishPort();

		this.currentPowerConsumption =
				new AtomicReference<>(
						new SensorData<>(
								new Measure<Double>(0.0, MeasurementUnit.AMPERES)));
		this.currentPowerProduction =
				new AtomicReference<>(
						new SensorData<>(
								new Measure<Double>(0.0, MeasurementUnit.AMPERES)));

		switch (this.currentSimulationType) {
			case MIL_SIMULATION:
				Architecture architecture =
						LocalSimulationArchitectures.
								createElectricMeterMILArchitecture4IntegrationTests(
										this.globalArchitectureURI,
										this.simulationTimeUnit);
				assert	architecture.getRootModelURI().equals(this.localArchitectureURI) :
						new AssertionError(
								"local simulator " + this.localArchitectureURI
								+ " does not exist!");
				this.addLocalSimulatorArchitecture(architecture);
				this.global2localSimulationArchitectureURIS.
						put(this.globalArchitectureURI, this.localArchitectureURI);
				break;
			case MIL_RT_SIMULATION:
				architecture =
						LocalSimulationArchitectures.
								createElectricMeter_RT_Architecture4IntegrationTests(
										SimulationType.MIL_RT_SIMULATION,
										this.globalArchitectureURI,
										this.simulationTimeUnit,
										this.accFactor);
				assert	architecture.getRootModelURI().equals(this.localArchitectureURI) :
						new AssertionError(
								"local simulator " + this.localArchitectureURI
								+ " does not exist!");
				this.addLocalSimulatorArchitecture(architecture);
				this.global2localSimulationArchitectureURIS.
						put(this.globalArchitectureURI, this.localArchitectureURI);
				break;
			case SIL_SIMULATION:
				architecture =
						LocalSimulationArchitectures.
								createElectricMeter_RT_Architecture4IntegrationTests(
										SimulationType.SIL_SIMULATION,
										this.globalArchitectureURI,
										this.simulationTimeUnit,
										this.accFactor);
				assert	architecture.getRootModelURI().equals(
						this.localArchitectureURI) :
						new AssertionError(
								"local simulator " + this.localArchitectureURI
								+ " does not exist!");
				this.addLocalSimulatorArchitecture(architecture);
				this.global2localSimulationArchitectureURIS.
						put(this.globalArchitectureURI, this.localArchitectureURI);
				break;
			case NO_SIMULATION:
			default:
		}

		if (VERBOSE) {
			this.tracer.get().setTitle("Electric meter component");
			this.tracer.get().setRelativePosition(X_RELATIVE_POSITION,
												  Y_RELATIVE_POSITION);
			this.toggleTracing();
		}
	}

	// -------------------------------------------------------------------------
	// Component internal methods
	// -------------------------------------------------------------------------

	public void			setCurrentPowerConsumption(Measure<Double> power)
	{

		assert	power != null : new PreconditionException("power != null");
		assert	power.getMeasurementUnit().equals(MeasurementUnit.AMPERES) :
				new PreconditionException(
						"power.getMeasurementUnit().equals("
						+ "MeasurementUnit.AMPERES)");

		double old = 0.0;
		double i;
		if (VERBOSE) {
			old = ((Measure<Double>)this.currentPowerConsumption.get().
																getMeasure()).getData();
		}

		SensorData<Measure<Double>> sd = null;
		if (this.currentSimulationType.isSILSimulation()) {
			// in SIL simulation, an accelerated clock is used as time reference
			// for measurements and sensor data time stamps
			sd = new SensorData<>(this.clock,
								  new Measure<Double>(
										  this.clock,
										  power.getData(),
										  power.getMeasurementUnit()));
		} else {
			sd = new SensorData<>(power);
		}
		this.currentPowerConsumption.set(sd);

		if (VERBOSE) {
			i = power.getData();
			if (Math.abs(old - i) > 0.000001) {
				this.traceMessage(
						"Electric meter sets its current consumption at "
						+ this.currentPowerConsumption.get().getMeasure() + ".\n");
			}
		}
	}

	public void			setCurrentPowerProduction(Measure<Double> power)
	{
		assert	power != null : new PreconditionException("power != null");
		assert	power.getMeasurementUnit().equals(MeasurementUnit.AMPERES) :
				new PreconditionException(
						"power.getMeasurementUnit().equals("
						+ "MeasurementUnit.AMPERES)");

		SensorData<Measure<Double>> sd = null;
		if (this.currentSimulationType.isSILSimulation()) {
			// in SIL simulation, an accelerated clock is used as time reference
			// for measurements and sensor data time stamps
			sd = new SensorData<>(this.clock,
								  new Measure<Double>(
										  this.clock,
										  power.getData(),
										  power.getMeasurementUnit()));
		} else {
			sd = new SensorData<>(power);
		}
		this.currentPowerProduction.set(sd);

		if (VERBOSE) {
			this.traceMessage(
					"Electric meter sets its current production at "
					+ this.currentPowerProduction.get().getMeasure() + ".\n");
		}
	}


	// -------------------------------------------------------------------------
	// Component life-cycle
	// -------------------------------------------------------------------------


	@Override
	public synchronized void	start() throws ComponentStartException
	{
		super.start();

		try {
			switch (this.currentSimulationType) {
				case MIL_SIMULATION:
					AtomicSimulatorPlugin asp = new AtomicSimulatorPlugin();
					String uri = this.global2localSimulationArchitectureURIS.
							get(this.globalArchitectureURI);
					Architecture architecture =
							(Architecture) this.localSimulationArchitectures.get(uri);
					asp.setPluginURI(uri);
					asp.setSimulationArchitecture(architecture);
					this.installPlugin(asp);
					break;
				case MIL_RT_SIMULATION:
					RTAtomicSimulatorPlugin rtasp = new RTAtomicSimulatorPlugin();
					uri = this.global2localSimulationArchitectureURIS.
							get(this.globalArchitectureURI);
					architecture =
							(Architecture) this.localSimulationArchitectures.get(uri);
					rtasp.setPluginURI(architecture.getRootModelURI());
					rtasp.setSimulationArchitecture(architecture);
					this.installPlugin(rtasp);
					break;
				case SIL_SIMULATION:
					rtasp = new RTAtomicSimulatorPlugin();
					uri = this.global2localSimulationArchitectureURIS.
							get(this.globalArchitectureURI);
					architecture =
							(Architecture) this.localSimulationArchitectures.get(uri);
					rtasp.setPluginURI(uri);
					rtasp.setSimulationArchitecture(architecture);
					this.installPlugin(rtasp);
					break;
				case NO_SIMULATION:
				default:
			}
		} catch (Exception e) {
			throw new ComponentStartException(e) ;
		}
	}

	@Override
	public void			execute() throws Exception
	{
		// In the electric meter, the accelerated clock is only used to get
		// properly time stamped sensor data in simulated time instants.
		this.clock = null;
		if (this.currentExecutionType.isIntegrationTest() ||
			this.currentSimulationType.isSILSimulation()) {
			ClocksServerOutboundPort clocksServerOutboundPort =
					new ClocksServerOutboundPort(this);
			clocksServerOutboundPort.publishPort();
			this.doPortConnection(
					clocksServerOutboundPort.getPortURI(),
					ClocksServer.STANDARD_INBOUNDPORT_URI,
					ClocksServerConnector.class.getCanonicalName());
			this.logMessage("ElectricMeter gets the clock.");
			this.clock =
					clocksServerOutboundPort.getClock(this.clockURI);
			this.doPortDisconnection(clocksServerOutboundPort.getPortURI());
			clocksServerOutboundPort.unpublishPort();
			this.logMessage("ElectricMeter got the clock "
							+ this.clock.getClockURI());
		}
	}

	/**
	 * @see fr.sorbonne_u.components.AbstractComponent#shutdown()
	 */
	@Override
	public synchronized void	shutdown() throws ComponentShutdownException
	{
		try {
			this.emip.unpublishPort();
		} catch (Exception e) {
			throw new ComponentShutdownException(e) ;
		}
		super.shutdown();
	}

	// -------------------------------------------------------------------------
	// Component services implementation
	// -------------------------------------------------------------------------

	/**
	 * @see fr.sorbonne_u.components.equipments.meter.interfaces.ElectricMeterImplementationI#getCurrentConsumption()
	 */
	@Override
	public SensorData<Measure<Double>>	getCurrentConsumption() throws Exception
	{
		if (VERBOSE) {
			this.traceMessage(
					"Electric meter returns its current consumption.\n");
		}

		SensorData<Measure<Double>> ret = null;
		if (this.currentSimulationType.isSILSimulation()) {
			ret = this.currentPowerConsumption.get();
		} else {
			if (this.clock != null) {
				ret = new SensorData<>(
						this.clock,
						new Measure<Double>(this.clock,
											0.0,
											MeasurementUnit.AMPERES));
			} else {
				ret = new SensorData<>(
						new Measure<Double>(0.0, MeasurementUnit.AMPERES));
			}
		}

		assert	ret != null : new PostconditionException("return != null");
		assert	ret.getMeasure().isScalar() :
				new PostconditionException("return.getMeasure().isScalar()");
		assert	((Measure<?>)ret.getMeasure()).getData() instanceof Double :
				new PostconditionException(
						"((Measure<?>)return.getMeasure()).getData() "
						+ "instanceof Double");
		assert	((Measure<Double>)ret.getMeasure()).getData() >= 0.0 :
				new PostconditionException(
						"((Measure<Double>)return.getMeasure())."
						+ "getData() >= 0.0");
		assert	((Measure<?>)ret.getMeasure()).getMeasurementUnit().
												equals(MeasurementUnit.AMPERES) :
				new PostconditionException(
						"((Measure<?>)return.getMeasure()).getMeasurementUnit()."
						+ "equals(MeasurementUnit.AMPERES)");

		return ret;
	}

	/**
	 * @see fr.sorbonne_u.components.equipments.meter.interfaces.ElectricMeterImplementationI#getCurrentProduction()
	 */
	@Override
	public SensorData<Measure<Double>>	getCurrentProduction() throws Exception
	{
		if (VERBOSE) {
			this.traceMessage("Electric meter returns its current production.\n");
		}

		SensorData<Measure<Double>> ret = null;
		if (this.currentSimulationType.isSILSimulation()) {
			ret = this.currentPowerProduction.get();
		} else {
			if (this.clock != null) {
				ret = new SensorData<>(
						this.clock,
						new Measure<Double>(this.clock,
											0.0,
											MeasurementUnit.AMPERES));
			} else {
				ret = new SensorData<>(
						new Measure<Double>(0.0, MeasurementUnit.AMPERES));
			}
		}

		assert	ret != null : new PostconditionException("return != null");
		assert	ret.getMeasure().isScalar() :
				new PostconditionException("return.getMeasure().isScalar()");
		assert	((Measure<?>)ret.getMeasure()).getData() instanceof Double :
				new PostconditionException(
						"((Measure<?>)return.getMeasure()).getData() "
						+ "instanceof Double");
		assert	((Measure<Double>)ret.getMeasure()).getData() >= 0.0 :
				new PostconditionException(
						"((Measure<Double>)return.getMeasure())."
						+ "getData() >= 0.0");
		assert	((Measure<?>)ret.getMeasure()).getMeasurementUnit().
												equals(MeasurementUnit.AMPERES) :
				new PostconditionException(
						"((Measure<?>)return.getMeasure()).getMeasurementUnit()."
						+ "equals(MeasurementUnit.AMPERES)");

		return ret;
	}
}
