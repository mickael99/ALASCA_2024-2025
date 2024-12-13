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

// -----------------------------------------------------------------------------
/**
 * The enumeration <code>ExecutionType</code> defines the types of execution
 * that the components in the HEM project can run.
 *
 * <p><strong>Description</strong></p>
 * 
 * <p>
 * Execution types are used to guide the scenarios that the components will
 * execute in a run.
 * </p>
 * <p>
 * {@code STANDARD} means that the components run as a real world deployment,
 * a case that is never used here.
 * </p>
 * <p>
 * {@code UNIT_TEST} must be understood in an appliance per appliance point of
 * view. Often an appliance needs several components to be completely runnable,
 * as the heater that requires the base heater component, a heater user
 * component to perform the actions of a user and a heater controller component
 * that monitor the room temperature and gives orders to the heater to heat or
 * not in order to maintain some target temperature. A unit test will therefore
 * deploy all of the required components.
 * </p>
 * <p>
 * {@code INTEGRATION_TEST} means that the entire application will be tested.
 * </p>
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
 * <p>Created on : 2023-11-14</p>
 * 
 * @author	<a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
 */
public enum				ExecutionType
{
	STANDARD,			// standard usage, no simulation
	UNIT_TEST,			// unit tests
	INTEGRATION_TEST;	// integration tests

	/**
	 * return true if the execution type is {@code STANDARD}.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code true}	// no precondition.
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @return	true if the execution type is {@code STANDARD}.
	 */
	public boolean		isStandard()
	{
		return this == STANDARD;
	}

	/**
	 * return true if the execution type is {@code UNIT_TEST}.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code true}	// no precondition.
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @return	true if the execution type is {@code UNIT_TEST}.
	 */
	public boolean		isUnitTest()
	{
		return this == UNIT_TEST;
	}

	/**
	 * return true if the execution type is {@code INTEGRATION_TEST}.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code true}	// no precondition.
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @return	true if the execution type is {@code INTEGRATION_TEST}.
	 */
	public boolean		isIntegrationTest()
	{
		return this == INTEGRATION_TEST;
	}

	/**
	 * return true if the execution type is {@code UNIT_TEST} or
	 * {@code INTEGRATION_TEST}.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code true}	// no precondition.
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @return	true if the execution type is {@code UNIT_TEST} or {@code INTEGRATION_TEST}.
	 */
	public boolean		isTest()
	{
		return this.isUnitTest() || this.isIntegrationTest();
	}
}
// -----------------------------------------------------------------------------
