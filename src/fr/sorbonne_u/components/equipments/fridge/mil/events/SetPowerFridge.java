package fr.sorbonne_u.components.equipments.fridge.mil.events;

import fr.sorbonne_u.components.equipments.fridge.mil.FridgeElectricityModel;
import fr.sorbonne_u.devs_simulation.es.events.ES_Event;
import fr.sorbonne_u.devs_simulation.models.events.EventI;
import fr.sorbonne_u.devs_simulation.models.events.EventInformationI;
import fr.sorbonne_u.devs_simulation.models.interfaces.AtomicModelI;
import fr.sorbonne_u.devs_simulation.models.time.Time;

public class SetPowerFridge extends ES_Event implements FridgeEventI {

	public static class	PowerValue implements EventInformationI {
		
		private static final long serialVersionUID = 1L;
		protected final double	power;

		public PowerValue(double power) {
			super();

			assert	power >= 0.0 &&
							power <= FridgeElectricityModel.MAX_COOLING_POWER:
					new AssertionError(
							"Precondition violation: power >= 0.0 && "
							+ "power <= FridgeElectricityModel.MAX_COOLING_POWER,"
							+ " but power = " + power);

			this.power = power;
		}

		public double getPower() { 
			return this.power; 
		}

		@Override
		public String toString() {
			StringBuffer sb = new StringBuffer(this.getClass().getSimpleName());
			sb.append('[');
			sb.append(this.power);
			sb.append(']');
			return sb.toString();
		}
	}
	
	private static final long	serialVersionUID = 1L;
	protected final PowerValue	powerValue;

	public SetPowerFridge(Time timeOfOccurrence, EventInformationI content) {
		super(timeOfOccurrence, content);

		assert	content != null && content instanceof PowerValue :
				new AssertionError(
						"Precondition violation: event content is null or"
						+ " not a PowerValue " + content);

		this.powerValue = (PowerValue)content;
	}

	@Override
	public boolean hasPriorityOver(EventI e) {
		if (e instanceof SwitchOffFridge) 
			return true;
		
		return false;
	}

	@Override
	public void	executeOn(AtomicModelI model) {
		assert	model instanceof FridgeElectricityModel :
				new AssertionError(
						"Precondition violation: model instanceof "
						+ "FridgeElectricityModel");

		FridgeElectricityModel fridge = (FridgeElectricityModel)model;
		assert	fridge.getState() == FridgeElectricityModel.State.COOLING:
				new AssertionError(
						"model not in the right state, should be "
						+ "State.COOLING but is " + fridge.getState());
		fridge.setCurrentCoolingPower(this.powerValue.getPower(),
									  this.getTimeOfOccurrence());
	}
}
