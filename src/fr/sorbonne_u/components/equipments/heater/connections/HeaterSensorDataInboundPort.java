package fr.sorbonne_u.components.equipments.heater.connections;

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

import fr.sorbonne_u.components.ComponentI;
import fr.sorbonne_u.components.equipments.heater.Heater;
import fr.sorbonne_u.components.equipments.heater.HeaterSensorDataCI;
import fr.sorbonne_u.components.equipments.heater.measures.HeaterCompoundMeasure;
import fr.sorbonne_u.components.equipments.heater.measures.HeaterSensorData;
import fr.sorbonne_u.components.interfaces.DataOfferedCI;
import fr.sorbonne_u.components.ports.AbstractDataInboundPort;
import fr.sorbonne_u.components.utils.Measure;
import fr.sorbonne_u.exceptions.PreconditionException;
import java.util.concurrent.TimeUnit;

// -----------------------------------------------------------------------------
/**
 * The class <code>HeaterSensorDataInboundPort</code> implements the inbound
 * port for the {@code HeaterSensorDataCI} component data interface, and as
 * such must implement the
 * {@code HeaterSensorDataCI.HeaterSensorOfferedPullCI} pull interface as well
 * as the method {@code get} from the {@code DataOfferedCI.PullCI} pull
 * interface it extends to pull data from the server.
 *
 * <p><strong>Description</strong></p>
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
 * <p>Created on : 2023-11-27</p>
 * 
 * @author	<a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
 */
public class			HeaterSensorDataInboundPort
extends		AbstractDataInboundPort
implements	HeaterSensorDataCI.HeaterSensorOfferedPullCI
{
	// -------------------------------------------------------------------------
	// Constants and variables
	// -------------------------------------------------------------------------

	private static final long serialVersionUID = 1L;

	// -------------------------------------------------------------------------
	// Constructors
	// -------------------------------------------------------------------------

	/**
	 * create the inbound port.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code owner instanceof HeaterInternalControlI}
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @param owner			component that owns this port.
	 * @throws Exception 	<i>to do</i>.
	 */
	public				HeaterSensorDataInboundPort(ComponentI owner)
	throws Exception
	{
		super(HeaterSensorDataCI.HeaterSensorOfferedPullCI.class,
			  DataOfferedCI.PushCI.class, owner);

		assert	owner instanceof HeaterInternalControlI :
				new PreconditionException(
						"owner instanceof HeaterInternalControlI");
	}

	/**
	 * create the inbound port.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code owner instanceof HeaterInternalControlI}
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @param uri			unique identifier of the port.
	 * @param owner			component that owns this port.
	 * @throws Exception 	<i>to do</i>.
	 */
	public				HeaterSensorDataInboundPort(
		String uri, 
		ComponentI owner
		) throws Exception
	{
		super(uri, HeaterSensorDataCI.HeaterSensorOfferedPullCI.class,
			  DataOfferedCI.PushCI.class, owner);

		assert	owner instanceof HeaterInternalControlI :
				new PreconditionException(
						"owner instanceof HeaterInternalControlI");
	}

	// -------------------------------------------------------------------------
	// Methods
	// -------------------------------------------------------------------------

	/**
	 * @see fr.sorbonne_u.components.hem2024e3.equipments.heater.HeaterSensorDataCI.HeaterSensorCI#heatingPullSensor()
	 */
	@Override
	public HeaterSensorData<Measure<Boolean>>	heatingPullSensor()
	throws Exception
	{
		return this.getOwner().handleRequest(
							o -> ((Heater)o).heatingPullSensor());
	}

	/**
	 * @see fr.sorbonne_u.components.hem2024e3.equipments.heater.HeaterSensorDataCI.HeaterSensorCI#targetTemperaturePullSensor()
	 */
	@Override
	public HeaterSensorData<Measure<Double>>	targetTemperaturePullSensor()
	throws Exception
	{
		return this.getOwner().handleRequest(
							o -> ((Heater)o).targetTemperaturePullSensor());
	}

	/**
	 * @see fr.sorbonne_u.components.hem2024e3.equipments.heater.HeaterSensorDataCI.HeaterSensorCI#currentTemperaturePullSensor()
	 */
	@Override
	public HeaterSensorData<Measure<Double>>	currentTemperaturePullSensor()
	throws Exception
	{
		return this.getOwner().handleRequest(
							o -> ((Heater)o).currentTemperaturePullSensor());
	}

	/**
	 * @see fr.sorbonne_u.components.hem2024e3.equipments.heater.HeaterSensorDataCI.HeaterSensorCI#startTemperaturesPushSensor(long, java.util.concurrent.TimeUnit)
	 */
	@Override
	public void			startTemperaturesPushSensor(
		long controlPeriod,
		TimeUnit tu
		) throws Exception
	{
		this.getOwner().handleRequest(
			o -> { ((Heater)o).startTemperaturesPushSensor(controlPeriod, tu);
					return null;
				 });
	}

	/**
	 * @see fr.sorbonne_u.components.interfaces.DataOfferedCI.PullCI#get()
	 */
	@Override
	public DataOfferedCI.DataI		get() throws Exception
	{
		return new HeaterSensorData<HeaterCompoundMeasure>(
					new HeaterCompoundMeasure(
							this.targetTemperaturePullSensor().getMeasure(),
							this.currentTemperaturePullSensor().getMeasure()));
	}
}
// -----------------------------------------------------------------------------
