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

import java.time.Instant;
import java.io.Serializable;
import java.time.Duration;

// -----------------------------------------------------------------------------
/**
 * The interface <code>TimingI</code> defines operation which are used to
 * retrieve timing information associated to some entity, like the time
 * at which some data has been retrieved or computed.
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
 * <p>Created on : 2023-11-28</p>
 * 
 * @author	<a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
 */
public interface		TimingI
extends		Serializable
{
	/** value used when an exception is raised in accessing the current
	 *  host.																*/
	public static final String	UNKNOWN_TIMESTAMPER = "unknown-timestamper";

	/**
	 * return the {@code Instant} at which a timed action has been taken that
	 * is interpretable by the context.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code true}	// no precondition.
	 * post	{@code return != null}
	 * </pre>
	 *
	 * @return	the {@code Instant} at which a timed action has been taken.
	 */
	public Instant		getTimestamp();

	/**
	 * return the identity of the time stamper that can be interpreted in the
	 * context; an empty string represents an unknown timing entity.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code true}	// no precondition.
	 * post	{@code return != null}
	 * </pre>
	 *
	 * @return	the identity of the time stamper that can be recognised by the context.
	 */
	public String 		getTimestamper();

	/**
	 * the time reference to be used to interpret the timing data; two time
	 * references are currently defined: if the host hardware clock is directly
	 * used, this method returns the same value as {@code getTimestamper()} but
	 * if an {@code AcceleratedClock} is used, the URI of this clock is returned
	 * and {@code getTimestamper()} returns the identity of the host which
	 * hardware clock that supports the {@code AcceleratedClock}.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code true}	// no precondition.
	 * post	{@code return != null && !return.isEmpty()}
	 * </pre>
	 *
	 * @return	the time reference of this timed entity.
	 */
	public String		getTimeReference();

	/**
	 * return the freshness of the timed entity; if only one timing exists,
	 * the freshness is the duration between its time stamp and the current
	 * instant but when there are many timings, the freshness is the largest
	 * between the time stamps and the current time, both to be interpreted
	 * under the time reference.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code true}	// no precondition.
	 * post	{@code return != null}
	 * </pre>
	 *
	 * @return	the freshness of the timed entity.
	 */
	public Duration		freshness();

	/**
	 * compute the time coherence among the timings; if only one timing exists,
	 * as in scalar measures, the coherence is {@code Duration.ZERO}, otherwise
	 * it is the largest duration between distinct timings (<i>e.g.</i>, for
	 * compound measures).
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code true}	// no precondition.
	 * post	{@code return != null}
	 * </pre>
	 *
	 * @return	the time coherence among the timings.
	 */
	public Duration		timeCoherence();
}
// -----------------------------------------------------------------------------
