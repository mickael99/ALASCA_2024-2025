package fr.sorbonne_u.components.equipments.fridge.measures;

import fr.sorbonne_u.components.utils.MeasureI;

public interface FridgeMeasureI extends MeasureI {
	
	default boolean isStateMeasure() { return false; }
	default boolean isTemperatureMeasures()	{ return false; }
}
