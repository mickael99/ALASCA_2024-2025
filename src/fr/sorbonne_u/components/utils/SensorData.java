package fr.sorbonne_u.components.utils;

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

import java.io.Serializable;
import java.time.Duration;
import java.time.Instant;
import fr.sorbonne_u.exceptions.PreconditionException;
import fr.sorbonne_u.utils.aclocks.AcceleratedClock;

// -----------------------------------------------------------------------------
/**
 * The class <code>SensorData</code> implements a simple sensor data, with
 * measures and time stamping information.
 *
 * <p><strong>Description</strong></p>
 * 
 * <p><strong>White-box Invariant</strong></p>
 * 
 * <pre>
 * invariant	{@code measure != null}
 * </pre>
 * 
 * <p><strong>Black-box Invariant</strong></p>
 * 
 * <pre>
 * invariant	{@code true}	// no more invariant
 * </pre>
 * 
 * <p>Created on : 2023-11-18</p>
 * 
 * @author	<a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
 */
public class			SensorData<T extends MeasureI>
extends		TimedEntity
implements	Serializable
{
	// -------------------------------------------------------------------------
	// Constants and variables
	// -------------------------------------------------------------------------

	private static final long serialVersionUID = 1L;

	/** measured data; can be compound.										*/
	protected final T		measure;

	// -------------------------------------------------------------------------
	// Constructors
	// -------------------------------------------------------------------------

	/**
	 * create a sensor data from a scalar measure.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code measure != null}
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @param measure	the measured data.
	 */
	public				SensorData(T measure)
	{
		this(measure, measure.getTimestamp());
	}

	/**
	 * create a sensor data from a scalar measure.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code measure != null}
	 * pre	{@code i != null}
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @param measure	the measured data.
	 * @param i			the instant at which the sensor data is created.
	 */
	public				SensorData(T measure, Instant i)
	{
		super(i, measure.getTimestamper());

		assert	measure != null : new PreconditionException("measure != null");

		this.measure = measure;
	}

	/**
	 * create a sensor data from a compound measure.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code ac != null}
	 * pre	{@code measure != null}
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @param ac		accelerated clock used as time reference.
	 * @param measure	the measured data.
	 */
	public				SensorData(
		AcceleratedClock ac,
		T measure
		)
	{
		this(ac, measure, measure.getTimestamp());
	}

	/**
	 * create a sensor data from a compound measure.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code ac != null}
	 * pre	{@code measure != null}
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @param ac		accelerated clock used as time reference.
	 * @param measure	the measured data.
	 * @param i			the instant at which the sensor data is created.
	 */
	public				SensorData(
		AcceleratedClock ac,
		T measure,
		Instant i
		)
	{
		super(ac, i);

		assert	ac != null : new PreconditionException("ac != null");
		assert	measure != null : new PreconditionException("measure != null");

		this.measure = (T) measure;
	}

	// -------------------------------------------------------------------------
	// Methods
	// -------------------------------------------------------------------------

	/** @return the data.													*/
	public T			getMeasure()		{ return this.measure; }

	/**
	 * @see fr.sorbonne_u.components.hem2024e1.utils.TimingI#getTimestamp()
	 */
	@Override
	public Instant		getTimestamp()		{ return this.timestamp; }

	/**
	 * @see fr.sorbonne_u.components.hem2024e1.utils.TimingI#getTimestamper()
	 */
	@Override
	public String 		getTimestamper()	{ return this.timestamper; }

	/**
	 * @see fr.sorbonne_u.components.hem2024e1.utils.TimedEntity#freshness()
	 */
	@Override
	public Duration		freshness()
	{
		return ((TimedEntity)this.measure).freshness(this.getCurrentInstant());
	}

	/**
	 * @see fr.sorbonne_u.components.hem2024e1.utils.TimedEntity#timeCoherence()
	 */
	@Override
	public Duration		timeCoherence()
	{
		return this.measure.timeCoherence();
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String		toString()
	{
		StringBuffer sb = new StringBuffer(this.getClass().getSimpleName());
		sb.append('[');
		this.contentAsString(sb);
		sb.append(']');
		return sb.toString();
	}

	/**
	 * @see fr.sorbonne_u.components.hem2024e1.utils.TimedEntity#contentAsString(java.lang.StringBuffer)
	 */
	@Override
	protected String	contentAsString(StringBuffer sb)
	{
		StringBuffer local = new StringBuffer();
		local.append(this.measure.toString());
		local.append(", ");
		super.contentAsString(local);
		sb.append(local);
		return local.toString();
	}
}
// -----------------------------------------------------------------------------
