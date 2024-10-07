package fr.sorbonne_u.components.equipments.meter.interfaces;

import fr.sorbonne_u.components.utils.Measure;
import fr.sorbonne_u.components.utils.SensorData;

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



// -----------------------------------------------------------------------------
/**
 * The interface <code>ElectricMeterImplementationI</code> defines the services
 * implemented by an electric meter component.
 *
 * <p><strong>Description</strong></p>
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
public interface		ElectricMeterImplementationI 
{
	/**
	 * return the current total electric consumption in watts.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code true}	// no precondition.
	 * post	{@code return != null}
	 * post	{@code return.getMeasure().isScalar()}
	 * post	{@code ((Measure<?>)return.getMeasure()).getData() instanceof Double}
	 * post	{@code ((Measure<Double>)return.getMeasure()).getData() >= 0.0}
	 * post	{@code ((Measure<?>)return.getMeasure()).getMeasurementUnit().equals(MeasurementUnit.AMPERES)}
	 * </pre>
	 *
	 * @return				the current total electric consumption in watts.
	 * @throws Exception	<i>to do</i>.
	 */
	public SensorData<Measure<Double>>	getCurrentConsumption() throws Exception;

	/**
	 * return the current total electric power production in watts.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code true}	// no precondition.
	 * post	{@code return != null}
	 * post	{@code return.getMeasure().isScalar()}
	 * post	{@code ((Measure<?>)return.getMeasure()).getData() instanceof Double}
	 * post	{@code ((Measure<Double>)return.getMeasure()).getData() >= 0.0}
	 * post	{@code ((Measure<?>)return.getMeasure()).getMeasurementUnit().equals(MeasurementUnit.WATTS)}
	 * </pre>
	 *
	 * @return				the current total electric power production in watts.
	 * @throws Exception	<i>to do</i>.
	 */
	public SensorData<Measure<Double>>	getCurrentProduction() throws Exception;
}
// -----------------------------------------------------------------------------
