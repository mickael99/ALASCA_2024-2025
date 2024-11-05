package fr.sorbonne_u.components.equipments.hem.adjustable;

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
import fr.sorbonne_u.components.ports.AbstractOutboundPort;

// -----------------------------------------------------------------------------
/**
 * The class <code>AdjustableOutboundPort</code> implements an outbound port for
 * the {@code AdjustableCI} component interface.
 *
 * <p><strong>Description</strong></p>
 * 
 * <p>
 * The component interface {@code AdjustableCI} is a generic interface used by
 * the home energy manager to adjust the electricity consumption of appliances.
 * Appliances usually do not offer this interface, so a customised connector
 * must be use to translate calls made on the {@code AdjustableCI} interface by
 * the HEM to calls made on the specific offered interface. Hence, in this
 * implementation, methods tests the pre- and postconditions declared in
 * {@code AdjustableCI} as they are not tested on the server side (which may
 * perform other tests that the connector will have to take into account).
 * </p>
 * 
 * <p><strong>Invariant</strong></p>
 * 
 * <pre>
 * invariant	{@code true}	// no more invariants
 * </pre>
 * 
 * <p>Created on : 2021-09-13</p>
 * 
 * @author	<a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
 */
public class			AdjustableOutboundPort
extends		AbstractOutboundPort
implements	AdjustableCI
{
	// -------------------------------------------------------------------------
	// Constants and variables
	// -------------------------------------------------------------------------

	private static final long serialVersionUID = 1L;

	// -------------------------------------------------------------------------
	// Constructors
	// -------------------------------------------------------------------------

	/**
	 * create a port.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code true}	// no more precondition.
	 * post	{@code true}	// no more postcondition.
	 * </pre>
	 *
	 * @param owner			component owning this port.
	 * @throws Exception	<i>to do</i>.
	 */
	public				AdjustableOutboundPort(
		ComponentI owner
		) throws Exception
	{
		super(AdjustableCI.class, owner);
	}

	/**
	 * create a port.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code true}	// no more precondition.
	 * post	{@code true}	// no more postcondition.
	 * </pre>
	 *
	 * @param uri			URI of the port.
	 * @param owner			component owning this port.
	 * @throws Exception	<i>to do</i>.
	 */
	public				AdjustableOutboundPort(
		String uri,
		ComponentI owner
		) throws Exception
	{
		super(uri, AdjustableCI.class, owner);
	}

	// -------------------------------------------------------------------------
	// Methods
	// -------------------------------------------------------------------------

	/**
	 * @see fr.sorbonne_u.components.equipments.hem.adjustable.hem2024.bases.AdjustableCI#maxMode()
	 */
	@Override
	public int			maxMode() throws Exception
	{
		int ret = ((AdjustableCI)this.getConnector()).maxMode();
		assert ret > 0;
		return ret;
	}

	/**
	 * @see fr.sorbonne_u.components.equipments.hem.adjustable.hem2024.bases.AdjustableCI#upMode()
	 */
	@Override
	public boolean		upMode() throws Exception
	{
		int oldMode = this.currentMode();
		assert	oldMode < this.maxMode();
		boolean ret = ((AdjustableCI)this.getConnector()).upMode();

		assert	this.currentMode() > oldMode;
		return ret;
	}

	/**
	 * @see fr.sorbonne_u.components.equipments.hem.adjustable.hem2024.bases.AdjustableCI#downMode()
	 */
	@Override
	public boolean		downMode() throws Exception
	{
		int oldMode = this.currentMode();
		assert	oldMode > 1;
		boolean ret = ((AdjustableCI)this.getConnector()).downMode();
		assert	this.currentMode() < oldMode;
		return ret;
	}

	/**
	 * @see fr.sorbonne_u.components.equipments.hem.adjustable.hem2024.bases.AdjustableCI#setMode(int)
	 */
	@Override
	public boolean		setMode(int modeIndex) throws Exception
	{
		assert	modeIndex > 0 && modeIndex <= this.maxMode();
		boolean ret = ((AdjustableCI)this.getConnector()).setMode(modeIndex);
		assert	this.currentMode() == modeIndex;
		return ret;
	}

	/**
	 * @see fr.sorbonne_u.components.equipments.hem.adjustable.hem2024.bases.AdjustableCI#currentMode()
	 */
	@Override
	public int			currentMode() throws Exception
	{
		int ret = ((AdjustableCI)this.getConnector()).currentMode();
		assert	ret > 0 && ret <= this.maxMode();
		return ret;
	}

	/**
	 * @see fr.sorbonne_u.components.equipments.hem.adjustable.hem2024.bases.AdjustableCI#suspended()
	 */
	@Override
	public boolean		suspended() throws Exception
	{
		return ((AdjustableCI)this.getConnector()).suspended();
	}

	/**
	 * @see fr.sorbonne_u.components.equipments.hem.adjustable.hem2024.bases.AdjustableCI#suspend()
	 */
	@Override
	public boolean		suspend() throws Exception
	{
		assert	!this.suspended();
		boolean ret = ((AdjustableCI)this.getConnector()).suspend();
		assert	!ret || this.suspended();
		return ret;
	}

	/**
	 * @see fr.sorbonne_u.components.equipments.hem.adjustable.hem2024.bases.AdjustableCI#resume()
	 */
	@Override
	public boolean		resume() throws Exception
	{
		assert	this.suspended();
		boolean ret = ((AdjustableCI)this.getConnector()).resume();
		assert	!ret || !this.suspended();
		return ret;
	}

	/**
	 * @see fr.sorbonne_u.components.equipments.hem.adjustable.hem2024.bases.AdjustableCI#emergency()
	 */
	@Override
	public double		emergency() throws Exception
	{
		assert	this.suspended();
		double ret = ((AdjustableCI)this.getConnector()).emergency();
		assert	ret >= 0.0 && ret <= 1.0;
		return ret;
	}
}
// -----------------------------------------------------------------------------
