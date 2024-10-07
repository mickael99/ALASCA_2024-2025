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
import java.util.Set;
import java.time.Duration;
import fr.sorbonne_u.exceptions.PostconditionException;
import fr.sorbonne_u.exceptions.PreconditionException;
import fr.sorbonne_u.utils.aclocks.AcceleratedClock;
import java.net.UnknownHostException;

// -----------------------------------------------------------------------------
/**
 * The class <code>TimedEntity</code> defines the common fields and methods
 * managing the timing information of timed entities.
 *
 * <p><strong>Description</strong></p>
 * 
 * <p>
 * The major implementation decision for this functionality is to use the
 * class {@code java.time.Instant} to represent punctual times, as well as the
 * class {@code java.time.Duration} to represent elapsed times.
 * </p>
 * <p>
 * Another important decision is to use IP addresses to identify hosts, which
 * is indeed fragile as IP addresses can be local and also dynamically
 * attributed to hosts.
 * </p>
 * 
 * <p><strong>White-box Invariant</strong></p>
 * 
 * <pre>
 * invariant	{@code timestamp != null}
 * invariant	{@code timestamper != null}
 * invariant	{@code ac == null && getTimeReference().equals(getTimestamper()) || ac != null && getTimeReference().equals(ac.getClockURI())}
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
public abstract class	TimedEntity
implements	TimingI
{
	// -------------------------------------------------------------------------
	// Constants and variables
	// -------------------------------------------------------------------------

	private static final long 					serialVersionUID = 1L;
	/** time stamp as a Java {@code Instant} object.						*/
	protected final Instant						timestamp;
	/** identity of the time stamping host <i>e.g.</i>, its IP address.		*/
	protected final String						timestamper;
	/** accelerated clock giving the time reference, if any.				*/
	transient protected final AcceleratedClock	ac;

	// -------------------------------------------------------------------------
	// Constructors
	// -------------------------------------------------------------------------

	/**
	 * create a timed entity with the current time as time stamp, the current
	 * host as time stamper and its hardware clock giving the time reference.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code true}	// no precondition.
	 * post	{@code getTimeReference().equals(getTimestamper())}
	 * </pre>
	 *
	 */
	public				TimedEntity()
	{
		this(Instant.ofEpochMilli(System.currentTimeMillis()));
	}

	/**
	 * create a timed entity with the given time stamp and the current host as
	 * time stamper and its hardware clock giving the time reference.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code timestamp != null}
	 * post	{@code getTimeReference().equals(getTimestamper())}
	 * </pre>
	 *
	 * @param timestamp		time stamp as a Java {@code Instant} object.
	 */
	public				TimedEntity(
		Instant timestamp
		)
	{
		this(timestamp, getStandardTimestamper());
	}

	/**
	 * create a timed entity with the given time stamp, time stamper identity
	 * and the current host hardware clock giving the time reference.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code timestamp != null}
	 * pre	{@code timestamper != null}
	 * post	{@code getTimeReference().equals(getTimestamper())}
	 * </pre>
	 *
	 * @param timestamp		time stamp as a Java {@code Instant} object.
	 * @param timestamper	identity of the time stamping host <i>e.g.</i>, its IP address.
	 */
	public				TimedEntity(
		Instant timestamp,
		String timestamper
		)
	{
		super();

		assert	timestamp != null :
				new PreconditionException("timestamp != null");
		assert	timestamper != null :
				new PreconditionException("timestamper != null");

		this.timestamp = timestamp;
		this.timestamper = timestamper;
		this.ac = null;

		assert	this.getTimeReference().equals(this.getTimestamper()) :
				new PostconditionException(
						"getTimeReference().equals(getTimestamper())");
	}

	/**
	 * create a timed entity with the current time as time stamp, the current
	 * host as time stamper and the given accelerated clock giving the time
	 * reference.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code ac != null}
	 * post	{@code getTimeReference().equals(ac.getClockURI())}
	 * </pre>
	 *
	 * @param ac	an accelerated clock giving the time reference.
	 */
	public				TimedEntity(AcceleratedClock ac)
	{
		this(ac, ac.currentInstant());
	}

	/**
	 * create a timed entity with the given time stamp and the current host as
	 * time stamper.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code ac != null}
	 * pre	{@code timestamp != null}
	 * post	{@code getTimeReference().equals(ac.getClockURI())}
	 * </pre>
	 *
	 * @param ac			an accelerated clock giving the time reference.
	 * @param timestamp		time stamp as a Java {@code Instant} object.
	 */
	public				TimedEntity(
		AcceleratedClock ac,
		Instant timestamp
		)
	{
		super();

		assert	ac != null : new PreconditionException("ac != null");
		assert	timestamp != null :
				new PreconditionException("timestamp != null");

		this.ac = ac;
		this.timestamp = timestamp;
		String temp;
		try {
			temp = ac.getTimeReferenceIdentity();
		} catch (UnknownHostException e) {
			temp = UNKNOWN_TIMESTAMPER;
		}
		this.timestamper = temp;

		assert	this.getTimeReference().equals(ac.getClockURI()) :
				new PostconditionException(
						"getTimeReference().equals(ac.getClockURI())");
	}

	// -------------------------------------------------------------------------
	// Internal methods
	// -------------------------------------------------------------------------

	/**
	 * return the identity of the standard time stamper, to be interpreted in
	 * the context; for example, it can be the IP address of the host which
	 * hardware clock is used as time reference.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code true}	// no precondition.
	 * post	{@code return != null && !return.isEmpty()}
	 * </pre>
	 *
	 * @return	the identity of the standard time stamper.
	 */
	protected static String	getStandardTimestamper()
	{
		try {
			return java.net.Inet4Address.getLocalHost().getHostAddress();
		} catch (UnknownHostException e) {
			return UNKNOWN_TIMESTAMPER;
		}
	}

	// -------------------------------------------------------------------------
	// Internal methods
	// -------------------------------------------------------------------------

	/**
	 * return true if the hardware clock of the host is directly used as time
	 * reference, otherwise an accelerated clock is the time reference.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code true}	// no precondition.
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @return	true if the hardware clock of the host is directly used as time reference.
	 */
	protected boolean	hasHardwareTimeReference()
	{
		return this.ac == null;
	}

	/**
	 * return the current instant under the time reference.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code true}	// no precondition.
	 * post	{@code return != null}
	 * </pre>
	 *
	 * @return	the current instant under the time reference.
	 */
	protected Instant	getCurrentInstant()
	{
		if (this.hasHardwareTimeReference()) {
			return Instant.ofEpochMilli(System.currentTimeMillis());
		} else {
			return this.ac.currentInstant();
		}
	}

	/**
	 * add the information contained in this part of a timed entity.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code true}	// no precondition.
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @param sb	a buffer to put the information in.
	 * @return		a string that contains everything added locally, in this method.
	 */
	protected String	contentAsString(StringBuffer sb)
	{
		StringBuffer local = new StringBuffer();
		local.append(this.timestamp);
		local.append(", ");
		local.append(this.getTimestamper());
		local.append(", ");
		local.append(this.getTimeReference());
		sb.append(local);
		return local.toString();
	}

	// -------------------------------------------------------------------------
	// Methods
	// -------------------------------------------------------------------------

	/**
	 * @see fr.sorbonne_u.components.hem2024e1.utils.TimingI#getTimestamp()
	 */
	@Override
	public Instant		getTimestamp()
	{
		return this.timestamp;
	}

	/**
	 * @see fr.sorbonne_u.components.hem2024e1.utils.TimingI#getTimestamper()
	 */
	@Override
	public String		getTimestamper()
	{
		return this.timestamper;
	}

	/**
	 * @see fr.sorbonne_u.components.hem2024e1.utils.TimingI#getTimeReference()
	 */
	@Override
	public String		getTimeReference()
	{
		if (this.hasHardwareTimeReference()) {
			return this.getTimestamper();
		} else {
			return this.ac.getClockURI();
		}
	}

	/**
	 * @see fr.sorbonne_u.components.hem2024e1.utils.TimingI#freshness()
	 */
	@Override
	public Duration		freshness()
	{
		return this.freshness(this.getCurrentInstant());
	}

	/**
	 * return the freshness of the timed entity against {@code current}.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code current != null && current.compareTo(getTimestamp()) > 0}
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @param current	the current instant.
	 * @return			the freshness of the timed entity against {@code current}.
	 */
	protected Duration	freshness(Instant current)
	{
		assert	current != null && current.compareTo(this.getTimestamp()) > 0 :
				new PreconditionException(
						"current != null && "
						+ "current.compareTo(this.getTimestamp()) > 0");

		return Duration.between(this.timestamp, current);
	}

	/**
	 * @see fr.sorbonne_u.components.hem2024e1.utils.TimingI#timeCoherence()
	 */
	@Override
	public Duration		timeCoherence()
	{
		return Duration.ZERO;
	}

	/**
	 * collect the time stamps of this timed entity.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code s != null}
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @param s	the set of time stamps to which the time stamps of this timed entity must be added.
	 */
	protected void		collectTimestamps(Set<Instant> s)
	{
		s.add(this.getTimestamp());
	}
}
// -----------------------------------------------------------------------------
