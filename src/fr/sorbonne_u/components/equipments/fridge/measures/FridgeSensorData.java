package fr.sorbonne_u.components.equipments.fridge.measures;

import fr.sorbonne_u.components.interfaces.DataOfferedCI;
import fr.sorbonne_u.components.interfaces.DataRequiredCI;
import fr.sorbonne_u.components.utils.MeasureI;
import fr.sorbonne_u.components.utils.SensorData;

public class FridgeSensorData<T extends MeasureI> extends SensorData<T> implements 
	DataOfferedCI.DataI, DataRequiredCI.DataI 
{
	private static final long serialVersionUID = 1L;

	public FridgeSensorData(T m) {
		super(m);
	}
}
