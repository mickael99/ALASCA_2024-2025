package fr.sorbonne_u.components.equipments.fridge.measures;

import fr.sorbonne_u.components.equipments.fridge.mil.FridgeElectricityModel.FridgeState;
import fr.sorbonne_u.components.utils.Measure;
import fr.sorbonne_u.utils.aclocks.AcceleratedClock;

public class FridgeStateMeasure extends Measure<FridgeState> implements FridgeMeasureI {

	private static final long serialVersionUID = 1L;

	public FridgeStateMeasure(AcceleratedClock ac, FridgeState data) {
		super(ac, data);			
	}

	public FridgeStateMeasure(FridgeState data) {
		super(data);
	}

	@Override
	public boolean	isStateMeasure(){ 
		return true; 
	}
}
