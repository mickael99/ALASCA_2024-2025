package fr.sorbonne_u.components.equipments.meter.connections;

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
import fr.sorbonne_u.components.equipments.meter.interfaces.ElectricMeterCI;
import fr.sorbonne_u.components.ports.AbstractOutboundPort;
import fr.sorbonne_u.components.utils.Measure;
import fr.sorbonne_u.components.utils.SensorData;

// -----------------------------------------------------------------------------
/**
 * The class <code>ElectricMeterOutboundPort</code> implements an outbound port
 * for the {@code ElectricMeterCI} component interface.
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
public class			ElectricMeterOutboundPort
extends		AbstractOutboundPort
implements	ElectricMeterCI
{
	// -------------------------------------------------------------------------
	// Constants and variables
	// -------------------------------------------------------------------------

	private static final long serialVersionUID = 1L;

	// -------------------------------------------------------------------------
	// Constructors
	// -------------------------------------------------------------------------

	/**
	 * create the outbound port.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code owner != null}
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @param owner			owner of this port.
	 * @throws Exception	<i>to do</i>.
	 */
	public				ElectricMeterOutboundPort(ComponentI owner)
	throws Exception
	{
		super(ElectricMeterCI.class, owner);
	}

	/**
	 * create the outbound port.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code uri != null && !uri.isEmpty()}
	 * pre	{@code owner != null}
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @param uri			uri of the port.
	 * @param owner			owner of this port.
	 * @throws Exception	<i>to do</i>.
	 */
	public				ElectricMeterOutboundPort(String uri, ComponentI owner)
	throws Exception
	{
		super(uri, ElectricMeterCI.class, owner);
	}

	// -------------------------------------------------------------------------
	// Methods
	// -------------------------------------------------------------------------

	/**
	 * @see fr.sorbonne_u.components.equipments.meter.interfaces.ElectricMeterCI#getCurrentConsumption()
	 */
	@Override
	public SensorData<Measure<Double>>	getCurrentConsumption() throws Exception
	{
		return ((ElectricMeterCI)this.getConnector()).getCurrentConsumption();
	}

	/**
	 * @see fr.sorbonne_u.components.equipments.meter.interfaces.ElectricMeterCI#getCurrentProduction()
	 */
	@Override
	public SensorData<Measure<Double>>	getCurrentProduction() throws Exception
	{
		return ((ElectricMeterCI)this.getConnector()).getCurrentProduction();
	}
}
// -----------------------------------------------------------------------------
