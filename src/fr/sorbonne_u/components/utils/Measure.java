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
import fr.sorbonne_u.exceptions.PostconditionException;
import fr.sorbonne_u.exceptions.PreconditionException;
import fr.sorbonne_u.utils.aclocks.AcceleratedClock;

// -----------------------------------------------------------------------------
/**
 * The class <code>Measure</code> implements a scalar measure made on some
 * phenomenon having a value of generic type T and a {@code MeasurementUnit}.
 *
 * <p><strong>Description</strong></p>
 * 
 * <p><strong>White-box Invariant</strong></p>
 * 
 * <pre>
 * invariant	{@code measurementUnit != null}
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
public class			Measure<T extends Serializable>
extends		TimedEntity
implements	MeasureI
{
	// -------------------------------------------------------------------------
	// Constants and variables
	// -------------------------------------------------------------------------

	private static final long			serialVersionUID = 1L;
	/** the measured data.													*/
	protected final T					data;
	/** the measurement unit in which {@code data} is expressed.			*/
	protected final MeasurementUnit		measurementUnit;

	// -------------------------------------------------------------------------
	// Constructors
	// -------------------------------------------------------------------------

	/**
	 * create a scalar measure.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code true}	// no precondition.
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @param data	the measurement data.
	 */
	public				Measure(T data)
	{
		this(data, MeasurementUnit.RAW);
	}

	/**
	 * create a scalar measure with the given {@code AcceleratedClock} as time
	 * reference.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code ac != null}
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @param ac	an accelerated clock giving the time reference.
	 * @param data	the measurement data.
	 */
	public				Measure(AcceleratedClock ac, T data)
	{
		this(ac, data, MeasurementUnit.RAW);
	}

	/**
	 * create a scalar measure.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code measurementUnit != null}
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @param data					the measurement data.
	 * @param measurementUnit		the measurement unit used to expressed {@code data}.
	 */
	public				Measure(
		T data,
		MeasurementUnit measurementUnit
		)
	{
		super();

		assert	measurementUnit != null :
				new PreconditionException("measurementUnit != null");

		this.data = data;
		this.measurementUnit = measurementUnit;

		assert	this.isScalar() : new PostconditionException("isScalar()");
	}

	/**
	 * create a scalar measure.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code ac != null}
	 * pre	{@code measurementUnit != null}
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @param ac					an accelerated clock giving the time reference.
	 * @param data					the measurement data.
	 * @param measurementUnit		the measurement unit used to expressed {@code data}.
	 */
	public				Measure(
		AcceleratedClock ac,
		T data,
		MeasurementUnit measurementUnit
		)
	{
		super(ac);

		assert	ac != null : new PreconditionException("ac != null");
		assert	measurementUnit != null :
				new PreconditionException("measurementUnit != null");

		this.data = data;
		this.measurementUnit = measurementUnit;

		assert	this.isScalar() : new PostconditionException("isScalar()");
	}

	// -------------------------------------------------------------------------
	// Methods
	// -------------------------------------------------------------------------

	/**
	 * @see fr.sorbonne_u.components.hem2024e1.utils.MeasureI#isScalar()
	 */
	@Override
	public boolean		isScalar()
	{
		return true;
	}

	/**
	 * get the scalar data.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code true}	// no precondition.
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @return	the scalar data held by this measure.
	 */
	public T			getData()
	{
		return this.data;
	}

	/**
	 * return the measurement unit of this scalar measure.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code true}	// no precondition.
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @return	the measurement unit of this scalar measure.
	 */
	public MeasurementUnit	getMeasurementUnit()
	{
		return this.measurementUnit;
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
		local.append(this.data);
		local.append(", ");
		local.append(this.measurementUnit);
		local.append(", ");
		super.contentAsString(local);
		sb.append(local);
		return local.toString();
	}
}
// -----------------------------------------------------------------------------
