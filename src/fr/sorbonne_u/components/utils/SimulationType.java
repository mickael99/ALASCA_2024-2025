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
 * The class <code>SimulationType</code> defines the different types of
 * simulations that can be run during an execution of the components in the
 * HEM project.
 *
 * <p><strong>Description</strong></p>
 * 
 * <p>
 * {@code NO_SIMULATION} means that no simulation is run, usually in case of a
 * standard type of execution <i>i.e.</i>, a real world deployment of the
 * application, or in a test (unit or integration) where only basic software
 * testing is performed.
 * </p>
 * <p>
 * {@code MIL_SIMULATION} means that the components will not run their normal
 * code but only simulations. This is normally used in early phases of the
 * project to test the feasibility of some choices or to tune some parameters.
 * </p>
 * <p>
 * {@code MIL_RT_SIMULATION} means the same as {@code MIL_SIMULATION}, except
 * that the simulation runs in real time (usually accelerated). The only real
 * interest of this is as an intermediate step when developing simulation models
 * from MIL models to SIL models to ensure the correctness of their real time
 * execution.
 * </p>
 * <p>
 * {@code SIL_SIMULATION} means that the components will run both their code
 * and their simulators in such a way that the code may call the simulators to
 * get replacement values for the ones that sensors would provide in a real
 * world deployment.
 * </p>
 * <p>
 * {@code HIL_SIMULATION} means that the components are deployed on the actual
 * target hardware, hence part of the simulated inputs from the sensors maybe
 * be replaced by actual sensors but others (typically environment sensors)
 * remain simulated. This is not currently used in the HEM project.
 * </p>
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
 * <p>Created on : 2024-09-16</p>
 * 
 * @author	<a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
 */
public enum				SimulationType
{
	NO_SIMULATION, 		// execution without simulation
	MIL_SIMULATION,		// model-in-the-loop simulation
	MIL_RT_SIMULATION,	// model-in-the-loop real time simulation
	SIL_SIMULATION,		// software-in-the-loop real time simulation
	HIL_SIMULATION;		// hardware-in-the-loop real time simulation

	/**
	 * return true if the simulation type is {@code NO_SIMULATION}.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code true}	// no precondition.
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @return	true if the simulation type is {@code NO_SIMULATION}.
	 */
	public boolean		isNoSimulation()
	{
		return this == NO_SIMULATION;
	}

	/**
	 * return true if the simulation type is {@code MIL_SIMULATION}.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code true}	// no precondition.
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @return	true if the simulation type is {@code MIL_SIMULATION}.
	 */
	public boolean		isMilSimulation()
	{
		return this == MIL_SIMULATION;
	}

	/**
	 * return true if the simulation type is {@code MIL_RT_SIMULATION}.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code true}	// no precondition.
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @return	true if the simulation type is {@code MIL_RT_SIMULATION}.
	 */
	public boolean		isMILRTSimulation()
	{
		return this == MIL_RT_SIMULATION;
	}

	/**
	 * return true if the simulation type is {@code SIL_SIMULATION}.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code true}	// no precondition.
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @return	true if the simulation type is {@code SIL_SIMULATION}.
	 */
	public boolean		isSILSimulation()
	{
		return this == SIL_SIMULATION;
	}

	/**
	 * return true if the simulation type is {@code HIL_SIMULATION}.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code true}	// no precondition.
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @return	true if the simulation type is {@code HIL_SIMULATION}.
	 */
	public boolean		isHILSimulation()
	{
		return this == HIL_SIMULATION;
	}

	/**
	 * return true if the simulation type is {@code MIL_SIMULATION},
	 * {@code MIL_RT_SIMULATION}, {@code SIL_SIMULATION} or
	 * {@code HIL_SIMULATION}.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code true}	// no precondition.
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @return	true if the simulation type is {@code MIL_SIMULATION}, {@code MIL_RT_SIMULATION}, {@code SIL_SIMULATION} or {@code HIL_SIMULATION}.
	 */
	public boolean		isSimulated()
	{
		return this.isMilSimulation() || this.isMILRTSimulation() ||
			   this.isSILSimulation() || this.isHILSimulation();
	}

	/**
	 * return true if the simulation type a form of real time simulation
	 * <i>i.e.</i>, is {@code MIL_RT_SIMULATION}, {@code SIL_SIMULATION} or
	 * {@code HIL_SIMULATION}.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code true}	// no precondition.
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @return	true if the simulation type is a form of real time simulation.
	 */
	public boolean		isRealTimeSimulation()
	{
		return this.isMilSimulation() || this.isSILSimulation() ||
			   this.isHILSimulation();
	}
}
// -----------------------------------------------------------------------------
