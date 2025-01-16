package fr.sorbonne_u.components.equipments.fridge.mil;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import fr.sorbonne_u.components.equipments.fridge.mil.FridgeElectricityModel.FridgeState;
import fr.sorbonne_u.components.equipments.fridge.mil.events.CloseDoorFridge;
import fr.sorbonne_u.components.equipments.fridge.mil.events.CoolFridge;
import fr.sorbonne_u.components.equipments.fridge.mil.events.DoNotCoolFridge;
import fr.sorbonne_u.components.equipments.fridge.mil.events.FridgeEventI;
import fr.sorbonne_u.components.equipments.fridge.mil.events.OpenDoorFridge;
import fr.sorbonne_u.components.equipments.fridge.mil.events.SetPowerFridge;
import fr.sorbonne_u.components.equipments.fridge.mil.events.SwitchOffFridge;
import fr.sorbonne_u.components.equipments.fridge.mil.events.SwitchOnFridge;
import fr.sorbonne_u.components.equipments.hem.mil.HEM_ReportI;
import fr.sorbonne_u.devs_simulation.exceptions.NeoSim4JavaException;
import fr.sorbonne_u.devs_simulation.hioa.annotations.ImportedVariable;
import fr.sorbonne_u.devs_simulation.hioa.annotations.InternalVariable;
import fr.sorbonne_u.devs_simulation.hioa.annotations.ModelImportedVariable;
import fr.sorbonne_u.devs_simulation.hioa.models.AtomicHIOA;
import fr.sorbonne_u.devs_simulation.hioa.models.vars.DerivableValue;
import fr.sorbonne_u.devs_simulation.hioa.models.vars.Value;
import fr.sorbonne_u.devs_simulation.models.annotations.ModelExternalEvents;
import fr.sorbonne_u.devs_simulation.models.events.Event;
import fr.sorbonne_u.devs_simulation.models.events.EventI;
import fr.sorbonne_u.devs_simulation.models.time.Duration;
import fr.sorbonne_u.devs_simulation.models.time.Time;
import fr.sorbonne_u.devs_simulation.simulators.interfaces.AtomicSimulatorI;
import fr.sorbonne_u.devs_simulation.simulators.interfaces.SimulationReportI;
import fr.sorbonne_u.devs_simulation.utils.InvariantChecking;
import fr.sorbonne_u.devs_simulation.utils.Pair;
import fr.sorbonne_u.devs_simulation.utils.StandardLogger;

@ModelExternalEvents(imported = {SwitchOffFridge.class,
								 SwitchOnFridge.class,
								 CoolFridge.class,
								 DoNotCoolFridge.class,
								 SetPowerFridge.class,
								 CloseDoorFridge.class,
								 OpenDoorFridge.class})
@ModelImportedVariable(name = "externalTemperature", type = Double.class)
public class FridgeTemperatureModel extends AtomicHIOA implements FridgeOperationI {
	
	// -------------------------------------------------------------------------
	// Constants and variables
	// -------------------------------------------------------------------------
	
	private static final long serialVersionUID = 1L;
	
	public static String MIL_URI = FridgeTemperatureModel.class.getSimpleName() + "-MIL";													
	public static String MIL_RT_URI = FridgeTemperatureModel.class.getSimpleName() + "-MIL-RT";
	public static String SIL_URI = FridgeTemperatureModel.class.getSimpleName() + "-SIL";
	
	public static double INITIAL_TEMPERATURE = 22.0;
	protected static double MIN_INTERNAL_TEMPERATURE = 0.0;
	protected static double INSULATION_TRANSFER_CONSTANT = 8.0;
											
	protected static double MAX_COOLING_RATE_CONSTANT  = 50.0;
	
	protected static final double DOOR_OPEN_TRANSFER_MULTIPLIER = 2.0;
	protected static final double ON_STATE_INSULATION_MULTIPLIER = 2.0;
	protected static double	TEMPERATURE_UPDATE_TOLERANCE = 0.0001;
	protected static double	COOLING_POWER_TRANSFER_TOLERANCE = 0.0001;
	protected static double	STEP = 15.0 / 24.0;	// 15 minutes

	protected FridgeState currentState = FridgeState.OFF;

	protected final Duration integrationStep;
	protected double temperatureAcc;
	protected Time start;
	protected double meanTemperature;

	@ImportedVariable(type = Double.class)
	protected Value<Double>	externalTemperature;
	
	@InternalVariable(type = Double.class)
	protected Value<Double> currentCoolingPower = new Value<Double>(this);
	
	@InternalVariable(type = Double.class)
	protected final DerivableValue<Double> currentTemperature = new DerivableValue<Double>(this);

	
	// -------------------------------------------------------------------------
	// Constructors
	// -------------------------------------------------------------------------

	public FridgeTemperatureModel(String uri, TimeUnit simulatedTimeUnit, AtomicSimulatorI simulationEngine) throws Exception {
		super(uri, simulatedTimeUnit, simulationEngine);
		
		this.integrationStep = new Duration(STEP, simulatedTimeUnit);
		this.getSimulationEngine().setLogger(new StandardLogger());

		assert	glassBoxInvariants(this) :
				new NeoSim4JavaException("White-box invariants violation!");
		assert	blackBoxInvariants(this) :
				new NeoSim4JavaException("Black-box invariants violation!");
	}
	
	
	// -------------------------------------------------------------------------
	// Invariants
	// -------------------------------------------------------------------------

	protected static boolean glassBoxInvariants(FridgeTemperatureModel instance) {
		assert	instance != null :
				new AssertionError("Precondition violation: instance != null");

		boolean ret = true;
		ret &= InvariantChecking.checkGlassBoxInvariant(
				TEMPERATURE_UPDATE_TOLERANCE >= 0.0,
				FridgeTemperatureModel.class,
				instance,
				"TEMPERATURE_UPDATE_TOLERANCE >= 0.0");
		ret &= InvariantChecking.checkGlassBoxInvariant(
				COOLING_POWER_TRANSFER_TOLERANCE >= 0.0,
				FridgeTemperatureModel.class,
				instance,
				"COOLING_POWER_TRANSFER_TOLERANCE >= 0.0");
		ret &= InvariantChecking.checkGlassBoxInvariant(
				INSULATION_TRANSFER_CONSTANT > 0.0,
				FridgeTemperatureModel.class,
				instance,
				"INSULATION_TRANSFER_CONSTANT > 0.0");
		ret &= InvariantChecking.checkGlassBoxInvariant(
				MAX_COOLING_RATE_CONSTANT  > 0.0,
				FridgeTemperatureModel.class,
				instance,
				"MIN_COOLING_TRANSFER_CONSTANT > 0.0");
		ret &= InvariantChecking.checkGlassBoxInvariant(
				STEP > 0.0,
				FridgeTemperatureModel.class,
				instance,
				"STEP > 0.0");
		ret &= InvariantChecking.checkGlassBoxInvariant(
				instance.currentState != null,
				FridgeTemperatureModel.class,
				instance,
				"currentState != null");
		ret &= InvariantChecking.checkGlassBoxInvariant(
				instance.integrationStep.getSimulatedDuration() > 0.0,
				FridgeTemperatureModel.class,
				instance,
				"integrationStep.getSimulatedDuration() > 0.0");
		ret &= InvariantChecking.checkGlassBoxInvariant(
				!instance.isStateInitialised() || instance.start != null,
				FridgeTemperatureModel.class,
				instance,
				"!isStateInitialised() || start != null");
		ret &= InvariantChecking.checkGlassBoxInvariant(
				instance.currentCoolingPower == null ||
					(!instance.currentCoolingPower.isInitialised() ||
								instance.currentCoolingPower.getValue() >= 0.0),
				FridgeTemperatureModel.class,
				instance,
				"currentCoolingPower == null || "
				+ "(!currentCoolingPower.isInitialised() || "
				+ "currentCoolingPower.getValue() >= 0.0)");
		ret &= InvariantChecking.checkGlassBoxInvariant(
				instance.currentTemperature != null,
				FridgeTemperatureModel.class,
				instance,
				"currentTemperature != null");
		return ret;
	}

	protected static boolean blackBoxInvariants(FridgeTemperatureModel instance) {
		assert	instance != null :
				new AssertionError("Precondition violation: instance != null");

		boolean ret = true;
		ret &= InvariantChecking.checkBlackBoxInvariant(
				MIL_URI != null && !MIL_URI.isEmpty(),
				FridgeTemperatureModel.class,
				instance,
				"MIL_URI != null && !MIL_URI.isEmpty()");
		ret &= InvariantChecking.checkBlackBoxInvariant(
				MIL_RT_URI != null && !MIL_RT_URI.isEmpty(),
				FridgeTemperatureModel.class,
				instance,
				"MIL_RT_URI != null && !MIL_RT_URI.isEmpty()");
		ret &= InvariantChecking.checkBlackBoxInvariant(
				SIL_URI != null && !SIL_URI.isEmpty(),
				FridgeTemperatureModel.class,
				instance,
				"SIL_URI != null && !SIL_URI.isEmpty()");
		return ret;
	}
	
	
	// -------------------------------------------------------------------------
	// Methods
	// -------------------------------------------------------------------------

	@Override
	public void setState(FridgeState s) {
		this.currentState = s;

		assert	glassBoxInvariants(this) :
				new NeoSim4JavaException("White-box invariants violation!");
		assert	blackBoxInvariants(this) :
				new NeoSim4JavaException("Black-box invariants violation!");
	}

	@Override
	public FridgeState getState() {
		return this.currentState;
	}
	
	@Override
	public void setCurrentCoolingPower(double newPower, Time t) {
		assert	newPower >= 0.0 &&
				newPower <= FridgeElectricityModel.MAX_COOLING_POWER :
			new NeoSim4JavaException(
					"Precondition violation: newPower >= 0.0 && "
					+ "newPower <= FridgeElectricityModel.MAX_COOLING_POWER,"
					+ " but newPower = " + newPower);

		this.currentCoolingPower.setNewValue(newPower, t);

		assert	glassBoxInvariants(this) :
				new NeoSim4JavaException("White-box invariants violation!");
		assert	blackBoxInvariants(this) :
				new NeoSim4JavaException("Black-box invariants violation!");
	}
	
	public double getCurrentTemperature() {
		return this.currentTemperature.getValue();
	}
	
	protected double currentCoolTransfertConstant()  {
		double c = 1.0 / (MAX_COOLING_RATE_CONSTANT  * FridgeElectricityModel.MAX_COOLING_POWER);
	    return c * this.currentCoolingPower.getValue();
	}
	
	protected double computeDerivatives(Double current) {
	    double currentTempDerivative = 0.0;
	    Time t = this.getCurrentStateTime();

	    switch (this.currentState) {
	        case COOLING:
	            if (this.currentCoolingPower.getValue() > COOLING_POWER_TRANSFER_TOLERANCE) {
	                currentTempDerivative = -current / this.currentCoolTransfertConstant();
	            }
	            break;

	        case DOOR_OPEN:
	            currentTempDerivative = 
	                (this.externalTemperature.evaluateAt(t) - current) / 
	                (INSULATION_TRANSFER_CONSTANT / DOOR_OPEN_TRANSFER_MULTIPLIER);
	            break;

	        case ON:
	            currentTempDerivative = 
	                (this.externalTemperature.evaluateAt(t) - current) / 
	                (INSULATION_TRANSFER_CONSTANT * ON_STATE_INSULATION_MULTIPLIER);
	            break;

	        case OFF:
	            currentTempDerivative = 
	                (this.externalTemperature.evaluateAt(t) - current) / 
	                INSULATION_TRANSFER_CONSTANT;
	            break;

	        default:
	            throw new IllegalStateException("Unhandled state: " + this.currentState);
	    }

	    return currentTempDerivative;
	}


	
	protected double computeNewTemperature(double deltaT) {
		Time t = this.currentTemperature.getTime();
		double oldTemp = this.currentTemperature.evaluateAt(t);
		double newTemp;

		if (deltaT > TEMPERATURE_UPDATE_TOLERANCE) {
			double derivative = this.currentTemperature.getFirstDerivative();
			newTemp = oldTemp + derivative * deltaT;
		} 
		else 
			newTemp = oldTemp;
		
		// The internal temperature cannot be lower than the minimum
		if(newTemp < MIN_INTERNAL_TEMPERATURE)
			newTemp = MIN_INTERNAL_TEMPERATURE;
		
		this.temperatureAcc += ((oldTemp + newTemp)/2.0) * deltaT;
		
		return newTemp;
	}
	
	@Override
	public void initialiseState(Time initialTime) {
		this.temperatureAcc = 0.0;
		this.start = initialTime;

		this.getSimulationEngine().toggleDebugMode();
		this.logMessage("simulation begins.\n");

		super.initialiseState(initialTime);

		assert	glassBoxInvariants(this) :
				new NeoSim4JavaException("White-box invariants violation!");
		assert	blackBoxInvariants(this) :
				new NeoSim4JavaException("Black-box invariants violation!");
	}
	
	@Override
	public boolean useFixpointInitialiseVariables() {
		return true;
	}
	
	@Override
	public Pair<Integer, Integer> fixpointInitialiseVariables() {
		int justInitialised = 0;
		int notInitialisedYet = 0;

		if (!this.currentTemperature.isInitialised() &&
									this.externalTemperature.isInitialised()) {
			double derivative = this.computeDerivatives(INITIAL_TEMPERATURE);
			this.currentTemperature.initialise(INITIAL_TEMPERATURE, derivative);
			
			this.currentCoolingPower.initialise(FridgeElectricityModel.MAX_COOLING_POWER);
			
			justInitialised += 2;
		}
		else if (!this.currentTemperature.isInitialised()) 
			notInitialisedYet += 2;
		

		assert	glassBoxInvariants(this) :
				new NeoSim4JavaException("White-box invariants violation!");
		assert	blackBoxInvariants(this) :
				new NeoSim4JavaException("Black-box invariants violation!");

		return new Pair<>(justInitialised, notInitialisedYet);
	}
	
	@Override
	public ArrayList<EventI> output() {
		return null;
	}

	@Override
	public Duration	timeAdvance() {
		return this.integrationStep;
	}
	
	@Override
	public void userDefinedInternalTransition(Duration elapsedTime) {
		double newTemp = this.computeNewTemperature(elapsedTime.getSimulatedDuration());
	    
	    double newDerivative = this.computeDerivatives(newTemp);
	    
	    this.currentTemperature.setNewValue(
	                    newTemp,
	                    newDerivative,
	                    new Time(this.getCurrentStateTime().getSimulatedTime(),
	                             this.getSimulatedTimeUnit()));
	    
	    String mark = this.currentState == FridgeState.COOLING ? " (c)" : " (-)";
	    StringBuffer message = new StringBuffer();
	    message.append(this.currentTemperature.getTime().getSimulatedTime());
	    message.append(mark);
	    message.append(" : ");
	    message.append(this.currentTemperature.getValue());
	    message.append('\n');
	    this.logMessage(message.toString());

	    super.userDefinedInternalTransition(elapsedTime);

	    assert glassBoxInvariants(this) :
	            new NeoSim4JavaException("White-box invariants violation!");
	    assert blackBoxInvariants(this) :
	            new NeoSim4JavaException("Black-box invariants violation!");
	}
	
	@Override
	public void userDefinedExternalTransition(Duration elapsedTime) {
	    ArrayList<EventI> currentEvents = this.getStoredEventAndReset();
	    assert currentEvents != null && currentEvents.size() == 1;

	    Event ce = (Event) currentEvents.get(0);
	    assert ce instanceof FridgeEventI;

	    StringBuffer sb = new StringBuffer("Executing external event: ");
	    sb.append(ce.eventAsString());
	    sb.append(".\n");
	    this.logMessage(sb.toString());

	    double newTemp = this.computeNewTemperature(elapsedTime.getSimulatedDuration());

	    ce.executeOn(this);

	    double newDerivative = this.computeDerivatives(newTemp);

	    this.currentTemperature.setNewValue(
	        newTemp,
	        newDerivative,
	        new Time(this.getCurrentStateTime().getSimulatedTime() + elapsedTime.getSimulatedDuration(),
	                 this.getSimulatedTimeUnit())
	    );

	    super.userDefinedExternalTransition(elapsedTime);

	    assert glassBoxInvariants(this) :
	           new NeoSim4JavaException("White-box invariants violation!");
	    assert blackBoxInvariants(this) :
	           new NeoSim4JavaException("Black-box invariants violation!");
	}
	
	@Override
	public void endSimulation(Time endTime) {
		this.meanTemperature = this.temperatureAcc / endTime.subtract(this.start).getSimulatedDuration();

		this.logMessage("simulation ends.\n");
		super.endSimulation(endTime);
	}
	
	public static class	FridgeTemperatureReport implements	SimulationReportI, HEM_ReportI {
		private static final long serialVersionUID = 1L;
		protected String	modelURI;
		protected double	meanTemperature;

		public FridgeTemperatureReport(String modelURI,double meanTemperature) {
			super();
			this.modelURI = modelURI;
			this.meanTemperature = meanTemperature;
		}

		@Override
		public String	getModelURI() {
			return this.modelURI;
		}

		@Override
		public String	printout(String indent) {
			StringBuffer ret = new StringBuffer(indent);
			ret.append("---\n");
			ret.append(indent);
			ret.append('|');
			ret.append(this.modelURI);
			ret.append(" report\n");
			ret.append(indent);
			ret.append('|');
			ret.append("mean temperature = ");
			ret.append(this.meanTemperature);
			ret.append(".\n");
			ret.append(indent);
			ret.append("---\n");
			return ret.toString();
		}
		
		public String toString() {
		    return this.printout(""); 
		}
	}

	@Override
	public SimulationReportI getFinalReport() {
		return new FridgeTemperatureReport(this.getURI(), this.meanTemperature);
	}
}
