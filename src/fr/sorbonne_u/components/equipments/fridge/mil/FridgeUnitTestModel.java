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
								 CoolFridge.class,
								 DoNotCoolFridge.class,
								 SetPowerFridge.class,
								 OpenDoorFridge.class,
								 CloseDoorFridge.class})
public class FridgeUnitTestModel extends AtomicModel {

	// -------------------------------------------------------------------------
	// Constants and variables
	// -------------------------------------------------------------------------

	private static final long serialVersionUID = 1L;

	public static final String MIL_URI = FridgeUnitTestModel.class.getSimpleName() + "-MIL";
	public static final String MIL_RT_URI = FridgeUnitTestModel.class.getSimpleName() + "-MIL-RT";
	public static final String SIL_URI = FridgeUnitTestModel.class.getSimpleName() + "-SIL";
			
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
		if (this.step > 0 && this.step < 12) {
			ArrayList<EventI> ret = new ArrayList<EventI>();
			switch (this.step) {
				case 1:
					ret.add(new SwitchOnFridge(this.getTimeOfNextEvent()));
					break;
				case 2:
					ret.add(new OpenDoorFridge(this.getTimeOfNextEvent()));
					break;
				case 3:
					ret.add(new CloseDoorFridge(this.getTimeOfNextEvent()));
					break;
				case 4:
					ret.add(new CoolFridge(this.getTimeOfNextEvent()));
					break;
				case 5:
					ret.add(new DoNotCoolFridge(this.getTimeOfNextEvent()));
					break;
				case 6:
					ret.add(new CoolFridge(this.getTimeOfNextEvent()));
					break;
				case 7:
					ret.add(new SetPowerFridge(this.getTimeOfNextEvent(),
											   new PowerValue(500.0)));
					break;
				case 8:
					ret.add(new SetPowerFridge(this.getTimeOfNextEvent(),
											   new PowerValue(400.0)));
					break;
				case 9:
					ret.add(new SetPowerFridge(this.getTimeOfNextEvent(),
											   new PowerValue(300.0)));
					break;
				case 10:
					ret.add(new SetPowerFridge(this.getTimeOfNextEvent(),
											   new PowerValue(200.0)));
					break;
				case 11:
					ret.add(new SwitchOffFridge(this.getTimeOfNextEvent()));
					break;
			}
			this.logMessage("emitting " + ret.get(ret.size() - 1) + ".");
			return ret;
		} 
		else 
			return null;
	}
	
	@Override
	public Duration timeAdvance() {
		if (this.step < 12) 
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
