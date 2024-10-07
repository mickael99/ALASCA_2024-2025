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

import fr.sorbonne_u.components.AbstractComponent;
import fr.sorbonne_u.components.annotations.RequiredInterfaces;
import fr.sorbonne_u.components.equipments.meter.connections.ElectricMeterConnector;
import fr.sorbonne_u.components.equipments.meter.connections.ElectricMeterConsumptionConnector;
import fr.sorbonne_u.components.equipments.meter.connections.ElectricMeterConsumptionOutboundPort;
import fr.sorbonne_u.components.equipments.meter.connections.ElectricMeterOutboundPort;
import fr.sorbonne_u.components.equipments.meter.connections.ElectricMeterProductionConnector;
import fr.sorbonne_u.components.equipments.meter.connections.ElectricMeterProductionOutboundPort;
import fr.sorbonne_u.components.equipments.meter.interfaces.ElectricMeterCI;
import fr.sorbonne_u.components.exceptions.ComponentShutdownException;
import fr.sorbonne_u.components.exceptions.ComponentStartException;
import fr.sorbonne_u.components.utils.Measure;
import fr.sorbonne_u.components.utils.MeasurementUnit;
import fr.sorbonne_u.components.utils.SensorData;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

// -----------------------------------------------------------------------------
/**
 * The class <code>ElectricMeterUnitTester</code> performs unit tests for
 * the electric meter component.
 *
 * <p><strong>Description</strong></p>
 * 
 * <p><strong>White-box Invariant</strong></p>
 * 
 * <pre>
 * invariant	{@code true}	// no more invariant
 * </pre>
 * 
 * <p><strong>Black-box Invariant</strong></p>
 * 
 * <pre>
 * invariant	{@code true}	// no more invariant
 * </pre>
 * 
 * <p>Created on : 2023-09-19</p>
 * 
 * @author	<a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
 */
@RequiredInterfaces(required={ElectricMeterCI.class})
public class			ElectricMeterUnitTester
extends		AbstractComponent
{
	// -------------------------------------------------------------------------
	// Constants and variables
	// -------------------------------------------------------------------------

	/** when true, methods trace their actions.								*/
	public static boolean		VERBOSE = false;
	/** when tracing, x coordinate of the window relative position.			*/
	public static int			X_RELATIVE_POSITION = 0;
	/** when tracing, y coordinate of the window relative position.			*/
	public static int			Y_RELATIVE_POSITION = 0;

	protected ElectricMeterOutboundPort emop;
	
	protected ElectricMeterConsumptionOutboundPort consumptionPort;
	protected ElectricMeterProductionOutboundPort productionPort;

	// -------------------------------------------------------------------------
	// Constructors
	// -------------------------------------------------------------------------

	/**
	 * create an electric meter unit tester component.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code true}	// no precondition.
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @throws Exception	<i>to do</i>.
	 */
	protected			ElectricMeterUnitTester() throws Exception
	{
		super(1, 0);

		this.emop = new ElectricMeterOutboundPort(this);
		this.emop.publishPort();
		
		this.consumptionPort = new ElectricMeterConsumptionOutboundPort(this);
		this.consumptionPort.publishPort();
		
		this.productionPort = new ElectricMeterProductionOutboundPort(this);
		this.productionPort.publishPort();

		if(VERBOSE) {
			this.tracer.get().setTitle("Electric meter tester component");
			this.tracer.get().setRelativePosition(X_RELATIVE_POSITION,
												  Y_RELATIVE_POSITION);
			this.toggleTracing();
		}
	}

	// -------------------------------------------------------------------------
	// Component internal methods
	// -------------------------------------------------------------------------

	protected void		testGetCurrentConsumption()
	{
		this.traceMessage("testGetCurrentConsumption()...\n");
		try {
			this.traceMessage("Electric meter current consumption? " +
									this.emop.getCurrentConsumption() + "\n");
		} catch (Exception e) {
			this.traceMessage("...KO.\n");
			assertTrue(false);
		}
		this.traceMessage("...done.\n");
	}

	protected void		testGetCurrentProduction()
	{
		this.traceMessage("testGetCurrentProduction()...\n");
		try {
			this.traceMessage("Electric meter current production? " +
									this.emop.getCurrentProduction() + "\n");
		} catch (Exception e) {
			this.traceMessage("...KO.\n");
			assertTrue(false);
		}
		this.traceMessage("...done.\n");
	}
	
	protected void testAddElectricConsumption() {
		this.traceMessage("testAddElectricConsumption()...\n");
		try {
			this.consumptionPort.addElectricConsumption(50.0);
			
			Measure<Double> measure = new Measure<>(50.0, MeasurementUnit.WATTS);
	        SensorData<Measure<Double>> sensorData = new SensorData<>(measure);
	        
	        assertEquals(sensorData.getMeasure().getData(), this.emop.getCurrentConsumption().getMeasure().getData());
		} catch(Exception e) {
			this.traceMessage("...KO.\n");
			assertTrue(false);
		}
		this.traceMessage("...done.\n");
	}
	
	protected void testAddElectricProduction() {
		this.traceMessage("testAddElectricProduction()...\n");
		try {
			this.productionPort.addElectricProduction(50.0);
			
			Measure<Double> measure = new Measure<>(50.0, MeasurementUnit.WATTS);
	        SensorData<Measure<Double>> sensorData = new SensorData<>(measure);
	        
	        assertEquals(sensorData.getMeasure().getData(), this.emop.getCurrentProduction().getMeasure().getData());
		} catch(Exception e) {
			this.traceMessage("...KO.\n");
			assertTrue(false);
		}
		this.traceMessage("...done.\n");
	}

	protected void runAllTests() {
		this.testGetCurrentConsumption();
		this.testGetCurrentProduction();
		this.testAddElectricConsumption();
		this.testAddElectricProduction();
	}

	// -------------------------------------------------------------------------
	// Component life-cycle
	// -------------------------------------------------------------------------

	/**
	 * @see fr.sorbonne_u.components.AbstractComponent#start()
	 */
	@Override
	public synchronized void	start() throws ComponentStartException
	{
		super.start();

		try {
			this.doPortConnection(
					this.emop.getPortURI(),
					ElectricMeter.ELECTRIC_METER_INBOUND_PORT_URI,
					ElectricMeterConnector.class.getCanonicalName());
			
			this.doPortConnection(
					this.consumptionPort.getPortURI(),
					ElectricMeter.CONSUMPTION_INBOUND_PORT_URI,
					ElectricMeterConsumptionConnector.class.getCanonicalName());
			
			this.doPortConnection(
					this.productionPort.getPortURI(),
					ElectricMeter.PRODUCTION_INBOUND_PORT_URI,
					ElectricMeterProductionConnector.class.getCanonicalName());
		} catch (Exception e) {
			throw new ComponentStartException(e) ;
		}
	}

	/**
	 * @see fr.sorbonne_u.components.AbstractComponent#execute()
	 */
	@Override
	public synchronized void	execute() throws Exception
	{
		this.runAllTests();
	}

	/**
	 * @see fr.sorbonne_u.components.AbstractComponent#finalise()
	 */
	@Override
	public synchronized void	finalise() throws Exception
	{
		this.doPortDisconnection(this.emop.getPortURI());
		super.finalise();
	}

	/**
	 * @see fr.sorbonne_u.components.AbstractComponent#shutdown()
	 */
	@Override
	public synchronized void	shutdown() throws ComponentShutdownException
	{
		try {
			this.emop.unpublishPort();
			this.consumptionPort.unpublishPort();
			this.productionPort.unpublishPort();
		} catch (Exception e) {
			throw new ComponentShutdownException(e) ;
		}
		super.shutdown();
	}
}
// -----------------------------------------------------------------------------
