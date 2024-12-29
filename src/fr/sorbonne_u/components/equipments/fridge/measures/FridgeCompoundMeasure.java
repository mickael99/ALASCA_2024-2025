package fr.sorbonne_u.components.equipments.fridge.measures;

import fr.sorbonne_u.components.utils.CompoundMeasure;
import fr.sorbonne_u.components.utils.Measure;
import fr.sorbonne_u.components.utils.MeasureI;
import fr.sorbonne_u.components.utils.MeasurementUnit;
import fr.sorbonne_u.devs_simulation.utils.InvariantChecking;
import fr.sorbonne_u.exceptions.ImplementationInvariantException;
import fr.sorbonne_u.exceptions.InvariantException;
import fr.sorbonne_u.exceptions.PreconditionException;

public class FridgeCompoundMeasure extends CompoundMeasure implements FridgeMeasureI {


	// -------------------------------------------------------------------------
	// Constants and variables
	// -------------------------------------------------------------------------

	private static final long	serialVersionUID = 1L;
	protected static final int	TARGET_TEMPERATURE_INDEX = 0;
	protected static final int	CURRENT_TEMPERATURE_INDEX = 1;

	// -------------------------------------------------------------------------
	// Invariants
	// -------------------------------------------------------------------------

	protected static boolean glassBoxInvariants(FridgeCompoundMeasure instance) {
		assert	instance != null :
				new PreconditionException("instance != null");

		boolean ret = true;
		ret &= InvariantChecking.checkGlassBoxInvariant(
					TARGET_TEMPERATURE_INDEX >= 0,
					FridgeCompoundMeasure.class,
					instance,
					"TARGET_TEMPERATURE_INDEX >= 0");
		ret &= InvariantChecking.checkGlassBoxInvariant(
					CURRENT_TEMPERATURE_INDEX >= 0,
					FridgeCompoundMeasure.class,
					instance,
					"CURRENT_TEMPERATURE_INDEX >= 0");
		ret &= InvariantChecking.checkGlassBoxInvariant(
					TARGET_TEMPERATURE_INDEX != CURRENT_TEMPERATURE_INDEX,
					FridgeCompoundMeasure.class,
					instance,
					"TARGET_TEMPERATURE_INDEX != CURRENT_TEMPERATURE_INDEX");
		return ret;
	}

	protected static boolean blackBoxInvariants(FridgeCompoundMeasure instance) {
		assert	instance != null :
				new PreconditionException("instance != null");

		boolean ret = true;
		return ret;
	}

	// -------------------------------------------------------------------------
	// Constructors
	// -------------------------------------------------------------------------

	public FridgeCompoundMeasure(Measure<Double> targetTemperature, Measure<Double> currentTemperature) {
		super(new MeasureI[]{targetTemperature, currentTemperature});

		assert	targetTemperature.getData() == this.getTargetTemperature() :
				new PreconditionException(
						"targetTemperature.getData() == "
						+ "getCurrentTemperature()");
		assert	targetTemperature.getMeasurementUnit() ==
								this.getTargetTemperatureMeasurementUnit() :
				new PreconditionException(
						"targetTemperature.getMeasurementUnit() == "
						+ "getTargetTemperatureMeasurementUnit()");
		assert	currentTemperature.getData() == this.getCurrentTemperature() :
				new PreconditionException(
						"currentTemperature.getData() == "
						+ "getCurrentTemperature()");
		assert	currentTemperature.getMeasurementUnit() ==
								this.getCurrentTemperatureMeasurementUnit() :
				new PreconditionException(
						"currentTemperature.getMeasurementUnit() == "
						+ "getCurrentTemperatureMeasurementUnit()");

		// Invariant checking
		assert	glassBoxInvariants(this) :
				new ImplementationInvariantException("glassBoxInvariants(this)");
		assert	blackBoxInvariants(this) :
				new InvariantException("blackBoxInvariants(this)");
	}

	
	// -------------------------------------------------------------------------
	// Methods
	// -------------------------------------------------------------------------

	@Override
	public boolean isTemperatureMeasures() {
		return true;
	}

	@SuppressWarnings("unchecked")
	public double getTargetTemperature() {
		return ((Measure<Double>)this.getMeasure(TARGET_TEMPERATURE_INDEX)).getData();
	}

	@SuppressWarnings("unchecked")
	public MeasurementUnit	getTargetTemperatureMeasurementUnit() {
		return ((Measure<Double>)this.getMeasure(TARGET_TEMPERATURE_INDEX)).
														getMeasurementUnit();
	}

	@SuppressWarnings("unchecked")
	public double getCurrentTemperature() {
		return ((Measure<Double>)this.getMeasure(CURRENT_TEMPERATURE_INDEX)).getData();
	}

	@SuppressWarnings("unchecked")
	public MeasurementUnit	getCurrentTemperatureMeasurementUnit() {
		return ((Measure<Double>)this.getMeasure(CURRENT_TEMPERATURE_INDEX)).
														getMeasurementUnit();
	}
}
