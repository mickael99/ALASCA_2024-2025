package fr.sorbonne_u.components.equipments.hairdryer.connections;

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

import fr.sorbonne_u.components.ComponentI;
import fr.sorbonne_u.components.ports.AbstractInboundPort;
import fr.sorbonne_u.exceptions.PreconditionException;

// -----------------------------------------------------------------------------
/**
 * The class <code>HairDryerInboundPort</code> implements an inbound port for
 * the <code>HairDryerUserCI</code> component interface.
 *
 * <p><strong>Description</strong></p>
 * 
 * <p><strong>Glass-box Invariants</strong></p>
 * 
 * <pre>
 * invariant	{@code getOwner() instanceof HairDryerImplementationI}
 * </pre>
 * 
 * <p><strong>Black-box Invariants</strong></p>
 * 
 * <pre>
 * invariant	{@code true}	// no more invariant
 * </pre>
 * 
 * <p>Created on : 2023-09-19</p>
 * 
 * @author	<a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
 */
public class			HairDryerInboundPort
extends		AbstractInboundPort
implements	HairDryerUserCI
{
	// -------------------------------------------------------------------------
	// Constants and variables
	// -------------------------------------------------------------------------

	private static final long serialVersionUID = 1L;

	// -------------------------------------------------------------------------
	// Constructors
	// -------------------------------------------------------------------------

	/**
	 * create an inbound port instance.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code owner != null}
	 * pre	{@code owner instanceof HairDryerImplementationI}
	 * post	{@code true}	// no more postcondition.
	 * </pre>
	 *
	 * @param owner			component owning the port.
	 * @throws Exception	<i>to do</i>.
	 */
	public				HairDryerInboundPort(ComponentI owner) throws Exception
	{
		super(HairDryerUserCI.class, owner);
		assert	owner instanceof HairDryerImplementationI :
				new PreconditionException(
						"owner instanceof HairDryerImplementationI");
	}

	/**
	 * create an inbound port instance.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code uri != null && !uri.isEmpty()}
	 * pre	{@code owner != null}
	 * pre	{@code owner instanceof HairDryerImplementationI}
	 * post	{@code true}	// no more postcondition.
	 * </pre>
	 *
	 * @param uri			URI of the port.
	 * @param owner			component owning the port.
	 * @throws Exception	<i>to do</i>.
	 */
	public				HairDryerInboundPort(
		String uri,
		ComponentI owner
		) throws Exception
	{
		super(uri, HairDryerUserCI.class, owner);
		assert	owner instanceof HairDryerImplementationI :
				new PreconditionException(
						"owner instanceof HairDryerImplementationI");
	}

	// -------------------------------------------------------------------------
	// Methods
	// -------------------------------------------------------------------------

	/**
	 * @see fr.sorbonne_u.components.hem2024e1.equipments.hairdryer.HairDryerUserCI#getState()
	 */
	@Override
	public HairDryerState	getState() throws Exception
	{
		return this.getOwner().handleRequest(
							o -> ((HairDryerImplementationI)o).getState());
	}

	/**
	 * @see fr.sorbonne_u.components.hem2024e1.equipments.hairdryer.HairDryerUserCI#getMode()
	 */
	@Override
	public HairDryerMode	getMode() throws Exception
	{
		return this.getOwner().handleRequest(
							o -> ((HairDryerImplementationI)o).getMode());
	}

	/**
	 * @see fr.sorbonne_u.components.hem2024e1.equipments.hairdryer.HairDryerUserCI#turnOn()
	 */
	@Override
	public void			turnOn() throws Exception
	{
		this.getOwner().handleRequest(
							o -> {	((HairDryerImplementationI)o).turnOn();
									return null;
							});
	}

	/**
	 * @see fr.sorbonne_u.components.hem2024e1.equipments.hairdryer.HairDryerUserCI#turnOff()
	 */
	@Override
	public void			turnOff() throws Exception
	{
		this.getOwner().handleRequest(
							o -> {	((HairDryerImplementationI)o).turnOff();
									return null;
							});
	}

	/**
	 * @see fr.sorbonne_u.components.hem2024e1.equipments.hairdryer.HairDryerUserCI#setHigh()
	 */
	@Override
	public void			setHigh() throws Exception
	{
		this.getOwner().handleRequest(
							o -> {	((HairDryerImplementationI)o).setHigh();;
									return null;
							});
	}

	/**
	 * @see fr.sorbonne_u.components.hem2024e1.equipments.hairdryer.HairDryerUserCI#setLow()
	 */
	@Override
	public void			setLow() throws Exception
	{
		this.getOwner().handleRequest(
							o -> {	((HairDryerImplementationI)o).setLow();
									return null;
							});
	}
}
// -----------------------------------------------------------------------------
