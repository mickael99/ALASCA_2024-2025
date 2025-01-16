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

// -----------------------------------------------------------------------------
/**
 * The interface <code>HeaterExternalControlI</code> declares the
 * signatures of service implementations accessible to the external controller.
 *
 * <p><strong>Description</strong></p>
 * 
 * <p><strong>Black-box Invariants</strong></p>
 * 
 * <pre>
 * invariant	{@code getCurrentPowerLevel() <= getMaxPowerLevel()}
 * </pre>
 * 
 * <p>Created on : 2023-09-18</p>
 * 
 * @author	<a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
 */
public interface		HeaterExternalControlI
extends		HeaterTemperatureI
{
	/**
	 * return the maximum power of the heater in watts.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code true}	// no precondition.
	 * post	{@code return > 0.0}
	 * </pre>
	 *
	 * @return				the maximum power of the heater in watts.
	 * @throws Exception	<i>to do</i>.
	 */
	public double		getMaxPowerLevel() throws Exception;

	/**
	 * set the power level of the heater; if
	 * {@code powerLevel > getMaxPowerLevel()} then set the power level to the
	 * maximum.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code powerLevel >= 0.0}
	 * post	{@code powerLevel > getMaxPowerLevel() || getCurrentPowerLevel() == powerLevel}
	 * </pre>
	 *
	 * @param powerLevel	the powerLevel to be set.
	 * @throws Exception	<i>to do</i>.
	 */
	public void			setCurrentPowerLevel(double powerLevel)
	throws Exception;

	/**
	 * return the current power level of the heater.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code true}	// no precondition.
	 * post	{@code return >= 0.0 && return <= getMaxPowerLevel()}
	 * </pre>
	 *
	 * @return				the current power level of the heater.
	 * @throws Exception	<i>to do</i>.
	 */
	public double		getCurrentPowerLevel() throws Exception;
}
// -----------------------------------------------------------------------------
