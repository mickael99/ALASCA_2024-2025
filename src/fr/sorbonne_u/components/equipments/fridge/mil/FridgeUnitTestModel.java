package fr.sorbonne_u.components.equipments.fridge.mil;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import fr.sorbonne_u.components.equipments.fridge.mil.events.*;
import fr.sorbonne_u.components.equipments.fridge.mil.events.SetPowerFridge.PowerValue;
import fr.sorbonne_u.devs_simulation.models.AtomicModel;
import fr.sorbonne_u.devs_simulation.models.annotations.ModelExternalEvents;
import fr.sorbonne_u.devs_simulation.models.events.EventI;
import fr.sorbonne_u.devs_simulation.models.time.Duration;
import fr.sorbonne_u.devs_simulation.models.time.Time;
import fr.sorbonne_u.devs_simulation.simulators.interfaces.AtomicSimulatorI;
import fr.sorbonne_u.devs_simulation.simulators.interfaces.SimulationReportI;
import fr.sorbonne_u.devs_simulation.utils.StandardLogger;

@ModelExternalEvents(exported = {SwitchOnFridge.class,
								 SwitchOffFridge.class,
								 Cool.class,
								 DoNotCool.class,
								 SetPowerFridge.class})
public class FridgeUnitTestModel extends AtomicModel {

	// -------------------------------------------------------------------------
	// Constants and variables
	// -------------------------------------------------------------------------

	private static final long serialVersionUID = 1L;
	public static final String	URI = FridgeUnitTestModel.class.getSimpleName();
		
	protected int	step;

	
	// -------------------------------------------------------------------------
	// Constructors
	// -------------------------------------------------------------------------

	public FridgeUnitTestModel(String uri, TimeUnit simulatedTimeUnit, AtomicSimulatorI simulationEngine) throws Exception {
		super(uri, simulatedTimeUnit, simulationEngine);
		this.getSimulationEngine().setLogger(new StandardLogger());
	}
	
	
	// -------------------------------------------------------------------------
	// METHODES
	// -------------------------------------------------------------------------

	@Override
	public void initialiseState(Time initialTime) {
		super.initialiseState(initialTime);
		
		this.step = 1;
		this.getSimulationEngine().toggleDebugMode();
		this.logMessage("simulation begins.\n");
	}
	
	@Override
	public ArrayList<EventI> output() {
		if (this.step > 0 && this.step < 10) {
			ArrayList<EventI> ret = new ArrayList<EventI>();
			switch (this.step) {
			case 1:
				ret.add(new SwitchOnFridge(this.getTimeOfNextEvent()));
				break;
			case 2:
				ret.add(new Cool(this.getTimeOfNextEvent()));
				break;
			case 3:
				ret.add(new DoNotCool(this.getTimeOfNextEvent()));
				break;
			case 4:
				ret.add(new Cool(this.getTimeOfNextEvent()));
				break;
			case 5:
				ret.add(new SetPowerFridge(this.getTimeOfNextEvent(),
										   new PowerValue(500.0)));
				break;
			case 6:
				ret.add(new SetPowerFridge(this.getTimeOfNextEvent(),
										   new PowerValue(400.0)));
				break;
			case 7:
				ret.add(new SetPowerFridge(this.getTimeOfNextEvent(),
										   new PowerValue(300.0)));
				break;
			case 8:
				ret.add(new SetPowerFridge(this.getTimeOfNextEvent(),
										   new PowerValue(200.0)));
				break;
			case 9:
				ret.add(new SwitchOffFridge(this.getTimeOfNextEvent()));
				break;
			}
			return ret;
		} 
		else 
			return null;
	}
	
	@Override
	public Duration timeAdvance() {
		if (this.step < 10) 
			return new Duration(1.0, this.getSimulatedTimeUnit());
		else 
			return Duration.INFINITY;
	}
	
	@Override
	public void userDefinedInternalTransition(Duration elapsedTime) {
		super.userDefinedInternalTransition(elapsedTime);

		this.step++;
	}
	
	@Override
	public void endSimulation(Time endTime) {
		this.logMessage("simulation ends.\n");
		super.endSimulation(endTime);
	}

	@Override
	public SimulationReportI getFinalReport() {
		return null;
	}
}
