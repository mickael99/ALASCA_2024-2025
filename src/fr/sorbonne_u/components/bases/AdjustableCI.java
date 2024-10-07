package fr.sorbonne_u.components.bases;

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

import fr.sorbonne_u.components.interfaces.RequiredCI;

// -----------------------------------------------------------------------------
/**
 * The component interface <code>AdjustableCI</code> defines the standardised
 * operation that an equipment implements to let a household energy manager
 * adjust or suspend them <i>i.e.</i>, making their electric power consumption
 * the lowest possible.
 *
 * <p><strong>Description</strong></p>
 * 
 * <p>
 * An adjustable equipment is an electric equipment that can be suspended and
 * resumed. When suspended, it operates at the lowest possible level of power
 * consumption. In some cases, they may provide several distinct operative
 * modes that are designated by integers from 1 to {@code maxMode()} that
 * make it operate at increasing levels of power consumption.
 * </p>
 * 
 * <p><strong>Black-box Invariant</strong></p>
 * 
 * <pre>
 * invariant	{@code true}	// no more invariant
 * </pre>
 * 
 * <p>Created on : 2023-09-12</p>
 * 
 * @author	<a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
 */
public interface		AdjustableCI
extends		RequiredCI
{
	/**
	 * return the largest mode value that can be returned by
	 * {@code currentMode}.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code true}	// no precondition.
	 * post	{@code return > 0}
	 * </pre>
	 *
	 * @return				the largest mode value that can be returned by {@code currentMode}.
	 * @throws Exception	<i>to do</i>.
	 */
	public int			maxMode() throws Exception;

	/**
	 * force the equipment to the next more energy consuming mode of operation,
	 * returning true if the operation succeeded or false otherwise.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code !suspended()}
	 * pre	{@code currentMode() < maxMode()}
	 * post	{@code currentMode() > currentMode()@pre}
	 * </pre>
	 *
	 * @return				true if the operation succeeded or false otherwise.
	 * @throws Exception	<i>to do</i>.
	 */
	public boolean		upMode() throws Exception;

	/**
	 * force the equipment to the next less energy consuming mode of operation,
	 * returning true if the operation succeeded or false otherwise.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code !suspended()}
	 * pre	{@code currentMode() > 1}
	 * post	{@code currentMode() < currentMode()@pre}
	 * </pre>
	 *
	 * @return				true if the operation succeeded or false otherwise.
	 * @throws Exception	<i>to do</i>.
	 */
	public boolean		downMode() throws Exception;

	/**
	 * set the equipment to the given mode of operation, returning true if the
	 * operation succeeded or false otherwise.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code !suspended()}
	 * pre	{@code modeIndex > 0 && modeIndex <= maxMode()}
	 * post	{@code currentMode() == modeIndex}
	 * </pre>
	 *
	 * @param modeIndex		index of the new mode of operation.
	 * @return				true if the operation succeeded or false otherwise.
	 * @throws Exception	<i>to do</i>.
	 */
	public boolean		setMode(int modeIndex) throws Exception;

	/**
	 * return the current mode of operation of the equipment.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code true}	// no precondition.
	 * post	{@code return > 0 && return <= maxMode()}
	 * </pre>
	 *
	 * @return				the current mode of operation of the equipment.
	 * @throws Exception	<i>to do</i>.
	 */
	public int			currentMode() throws Exception;

	/**
	 * return true if the equipment has been suspended and false otherwise.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code true}	// no precondition.
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @return				true if the equipment has been suspended and false otherwise.
	 * @throws Exception	<i>to do</i>.
	 */
	public boolean		suspended() throws Exception;

	/**
	 * suspend the equipment, returning true if the suspension succeeded or
	 * false otherwise.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code !suspended()}
	 * post	{@code !return || suspended()}
	 * </pre>
	 *
	 * @return				true if the suspension succeeded or false otherwise.
	 * @throws Exception	<i>to do</i>.
	 */
	public boolean		suspend() throws Exception;

	/**
	 * resume the previously suspended equipment, returning true if the
	 * resumption succeeded or false otherwise.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code suspended()}
	 * post	{@code !return || !suspended()}
	 * </pre>
	 *
	 * @return				true if the resumption succeeded or false otherwise.
	 * @throws Exception	<i>to do</i>.
	 */
	public boolean		resume() throws Exception;

	/**
	 * return the degree of emergency of a resumption for the previously
	 * suspended equipment.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code suspended()}
	 * post	{@code return >= 0.0 && return <= 1.0}
	 * </pre>
	 *
	 * @return				the degree of emergency of a resumption for the previously suspended equipment.
	 * @throws Exception	<i>to do</i>.
	 */
	public double		emergency() throws Exception;
}
// -----------------------------------------------------------------------------
