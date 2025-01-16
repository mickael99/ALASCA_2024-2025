package fr.sorbonne_u.components.equipments.heater.connections;

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

import fr.sorbonne_u.components.connectors.AbstractConnector;

// -----------------------------------------------------------------------------
/**
 * The class <code>HeaterConnector</code> implements a connector for the
 * {@code HeaterUserCI} component interface.
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
 * <p>Created on : 2021-09-13</p>
 * 
 * @author	<a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
 */
public class			HeaterUserConnector
extends		AbstractConnector
implements	HeaterUserCI
{
	/**
	 * @see fr.sorbonne_u.components.hem2024e1.equipments.heater.HeaterUserCI#on()
	 */
	@Override
	public boolean		on() throws Exception
	{
		return ((HeaterUserCI)this.offering).on();
	}

	/**
	 * @see fr.sorbonne_u.components.hem2024e1.equipments.heater.HeaterUserCI#switchOn()
	 */
	@Override
	public void			switchOn() throws Exception
	{
		((HeaterUserCI)this.offering).switchOn();
	}

	/**
	 * @see fr.sorbonne_u.components.hem2024e1.equipments.heater.HeaterUserCI#switchOff()
	 */
	@Override
	public void			switchOff() throws Exception
	{
		((HeaterUserCI)this.offering).switchOff();
	}

	/**
	 * @see fr.sorbonne_u.components.hem2024e1.equipments.heater.HeaterUserCI#setTargetTemperature(double)
	 */
	@Override
	public void			setTargetTemperature(double target) throws Exception
	{
		((HeaterUserCI)this.offering).setTargetTemperature(target);
	}

	/**
	 * @see fr.sorbonne_u.components.hem2024e1.equipments.heater.HeaterUserCI#getTargetTemperature()
	 */
	@Override
	public double		getTargetTemperature() throws Exception
	{
		return ((HeaterUserCI)this.offering).getTargetTemperature();
	}

	/**
	 * @see fr.sorbonne_u.components.hem2024e1.equipments.heater.HeaterUserCI#getCurrentTemperature()
	 */
	@Override
	public double		getCurrentTemperature() throws Exception
	{
		return ((HeaterUserCI)this.offering).getCurrentTemperature();
	}

	/**
	 * @see fr.sorbonne_u.components.hem2024e1.equipments.heater.HeaterUserCI#getMaxPowerLevel()
	 */
	@Override
	public double		getMaxPowerLevel() throws Exception
	{
		return ((HeaterUserCI)this.offering).getMaxPowerLevel();
	}

	/**
	 * @see fr.sorbonne_u.components.hem2024e1.equipments.heater.HeaterUserCI#setCurrentPowerLevel(double)
	 */
	@Override
	public void			setCurrentPowerLevel(double powerLevel) throws Exception
	{
		((HeaterUserCI)this.offering).setCurrentPowerLevel(powerLevel);
	}

	/**
	 * @see fr.sorbonne_u.components.hem2024e1.equipments.heater.HeaterUserCI#getCurrentPowerLevel()
	 */
	@Override
	public double		getCurrentPowerLevel() throws Exception
	{
		return ((HeaterUserCI)this.offering).getCurrentPowerLevel();
	}
}
// -----------------------------------------------------------------------------
