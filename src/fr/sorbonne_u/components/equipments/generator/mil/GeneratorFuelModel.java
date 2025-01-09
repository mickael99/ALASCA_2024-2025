package fr.sorbonne_u.components.equipments.generator.mil;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import fr.sorbonne_u.components.equipments.generator.mil.events.AbstractGeneratorEvents;
import fr.sorbonne_u.components.equipments.generator.mil.events.ActivateGeneratorEvent;
import fr.sorbonne_u.components.equipments.generator.mil.events.StopGeneratorEvent;
import fr.sorbonne_u.devs_simulation.exceptions.NeoSim4JavaException;
import fr.sorbonne_u.devs_simulation.hioa.annotations.ExportedVariable;
import fr.sorbonne_u.devs_simulation.hioa.models.AtomicHIOA;
import fr.sorbonne_u.devs_simulation.hioa.models.vars.Value;
import fr.sorbonne_u.devs_simulation.models.annotations.ModelExternalEvents;
import fr.sorbonne_u.devs_simulation.models.events.Event;
import fr.sorbonne_u.devs_simulation.models.events.EventI;
import fr.sorbonne_u.devs_simulation.models.time.Duration;
import fr.sorbonne_u.devs_simulation.models.time.Time;
import fr.sorbonne_u.devs_simulation.simulators.interfaces.AtomicSimulatorI;
import fr.sorbonne_u.devs_simulation.utils.InvariantChecking;
import fr.sorbonne_u.devs_simulation.utils.StandardLogger;

@ModelExternalEvents(
        imported = {
            ActivateGeneratorEvent.class,
            StopGeneratorEvent.class
        },
        exported = {
                StopGeneratorEvent.class
        })
public class GeneratorFuelModel extends AtomicHIOA {

	private static final long serialVersionUID = 1L;

	public static final String URI = GeneratorFuelModel.class.getSimpleName();
    public static final String MIL_URI = GeneratorFuelModel.class.getSimpleName() + "-MIL";
    public static final String MIL_RT_URI = GeneratorFuelModel.class.getSimpleName() + "-MIL-RT";
    public static final String SIL_URI = GeneratorFuelModel.class.getSimpleName() + "-MIL_RT";

    protected static final double STEP = 0.1;

    /** The generator fuel consumption in l/h */
    protected static final double FUEL_CONSUMPTION = 100.10;

    protected static final double MAX_CAPACITY = 200.0;
    
    protected final Duration evaluationStep;

    private boolean isRunning;

    protected boolean consumptionHasChanged;

    // -------------------------------------------------------------------------
    // HIOA model variables
    // -------------------------------------------------------------------------
    @ExportedVariable(type = Double.class)
    protected final Value<Double> currentFuelLevel = new Value<Double>(this);

    // -------------------------------------------------------------------------
    // Invariants
    // -------------------------------------------------------------------------
    protected static boolean glassBoxInvariants(GeneratorFuelModel model) {
        assert model != null:
                new NeoSim4JavaException("The model is null!");

        boolean ret = true;
        ret &= InvariantChecking.checkGlassBoxInvariant(
                STEP > 0.0,
                GeneratorFuelModel.class,
                model,
                "STEP > 0.0");
        ret &= InvariantChecking.checkGlassBoxInvariant(
                FUEL_CONSUMPTION > 0.0,
                GeneratorFuelModel.class,
                model,
                "FUEL_CONSUMPTION > 0.0");
        ret &= InvariantChecking.checkGlassBoxInvariant(
                MAX_CAPACITY > 0.0,
                GeneratorFuelModel.class,
                model,
                "MAX_CAPACITY > 0.0");

        return ret;
    }

    protected static boolean blackBoxInvarians(GeneratorFuelModel model){
        assert model != null:
                new NeoSim4JavaException("The model is null!");

        boolean ret = true;
        ret &= InvariantChecking.checkBlackBoxInvariant(
                MIL_URI != null && !MIL_URI.isEmpty(),
                GeneratorFuelModel.class,
                model,
                "MIL_URI != null && !MIL_URI.isEmpty()");
        ret &= InvariantChecking.checkBlackBoxInvariant(
                MIL_RT_URI != null && !MIL_RT_URI.isEmpty(),
                GeneratorFuelModel.class,
                model,
                "MIL_RT_URI != null && !MIL_RT_URI.isEmpty()");
        ret &= InvariantChecking.checkBlackBoxInvariant(
                SIL_URI != null && !SIL_URI.isEmpty(),
                GeneratorFuelModel.class,
                model,
                "SIL_URI != null && !SIL_URI.isEmpty()");
        return ret;
    }

    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------
    
    public GeneratorFuelModel(String uri, TimeUnit simulatedTimeUnit, AtomicSimulatorI simulationEngine) throws Exception {
		super(uri, simulatedTimeUnit, simulationEngine);
		this.evaluationStep = new Duration(STEP, this.getSimulatedTimeUnit());
		this.getSimulationEngine().setLogger(new StandardLogger());

        assert glassBoxInvariants(this) :
            new NeoSim4JavaException("GeneratorFuelModel constructor invariants");
        assert blackBoxInvarians(this) :
            new NeoSim4JavaException("GeneratorFuelModel constructor invariants");
    }

    // -------------------------------------------------------------------------
    // Methods
    // -------------------------------------------------------------------------

    public boolean isRunning() {
        return this.isRunning;
    }

    public void activate() {
        if (!this.isRunning) {
            this.isRunning = true;
            this.toggleConsumptionHasChanged();
        }
    }

    public void stop() {
        if (this.isRunning) {
            this.isRunning = false;
            this.toggleConsumptionHasChanged();
        }
    }

    public void consume(Duration d) {
        double duration = d.getSimulatedDuration();
        currentFuelLevel.setNewValue(
                this.currentFuelLevel.getValue() - Math.max((duration / 3600) * FUEL_CONSUMPTION, 0.0),
                currentStateTime);
    }

    protected void		toggleConsumptionHasChanged()
    {
        if (this.consumptionHasChanged) {
            this.consumptionHasChanged = false;
        } else {
            this.consumptionHasChanged = true;
        }
    }

    // -------------------------------------------------------------------------
    // DEVS simulation protocol
    // -------------------------------------------------------------------------

    @Override
    public void initialiseState(Time initialTime) {
    	System.out.println("fuel model initialise state");
    	
        super.initialiseState(initialTime);
        
        this.isRunning = false;
        
        this.currentFuelLevel.initialise(MAX_CAPACITY);
		this.getSimulationEngine().toggleDebugMode();
		this.logMessage("simulation begins.\n");
		StringBuffer message =
				new StringBuffer("current fuel level: ");
		message.append(this.currentFuelLevel.getValue());
		message.append(" at ");
		message.append(this.getCurrentStateTime());
		message.append("\n");
		this.logMessage(message.toString());

        assert glassBoxInvariants(this) :
                new NeoSim4JavaException("GeneratorFuelModel constructor invariants");
        assert blackBoxInvarians(this) :
                new NeoSim4JavaException("GeneratorFuelModel constructor invariants");
    }
    

    
    @Override
    public ArrayList<EventI> output() {
        if(currentFuelLevel.getValue() <= 0.0) {
            ArrayList<EventI> res = new ArrayList<>();
            res.add(new StopGeneratorEvent(currentFuelLevel.getTime()));
            return res;
        }
        return null;
    }
    
	@Override
	public Duration timeAdvance() {
        Duration ret = null;
        // to trigger an internal transition after an external transition, the
        // variable consumptionHasChanged is set to true, hence when it is true
        // return a zero delay otherwise return an infinite delay (no internal
        // transition expected)
        if (this.consumptionHasChanged) {
            // after triggering the internal transition, toggle the boolean
            // to prepare for the next internal transition.
            this.toggleConsumptionHasChanged();
            ret = new Duration(0.0, this.getSimulatedTimeUnit());
        } else {
            ret = Duration.INFINITY;
        }

        assert	glassBoxInvariants(this) :
                new NeoSim4JavaException(
                        "GeneratorFuelModel.glassBoxInvariants(this)");
        assert	blackBoxInvariants(this) :
                new NeoSim4JavaException(
                        "GeneratorFuelModel.blackBoxInvariants(this)");

        return ret;	}

	@Override
    public void userDefinedInternalTransition(Duration elapsedTime) {
        super.userDefinedInternalTransition(elapsedTime);

        if(isRunning) {
            if(currentFuelLevel.getValue() > 0.0) {
                consume(elapsedTime);
            }

            logMessage("Generator is " + (isRunning ? "on" : "off") + " | Fuel level : " + currentFuelLevel.getValue() + " at " + currentFuelLevel.getTime() + "\n");
        }

        assert glassBoxInvariants(this) :
                new NeoSim4JavaException("GeneratorFuelModel.glassBoxInvariants(this)");
        assert blackBoxInvariants(this) :
                new NeoSim4JavaException("GeneratorFuelModel.blackBoxInvariants(this)");
    }
	
	@Override
    public void userDefinedExternalTransition(Duration elapsedTime) {
        ArrayList<EventI> currentEvents = this.getStoredEventAndReset();
        assert	currentEvents != null && currentEvents.size() == 1;
        Event currentEvent = (Event) currentEvents.get(0);

        assert currentEvent instanceof AbstractGeneratorEvents;
        currentEvent.executeOn(this);

        super.userDefinedExternalTransition(elapsedTime);

        assert glassBoxInvariants(this) :
                new NeoSim4JavaException("GeneratorFuelModel.glassBoxInvariants(this)");
        assert blackBoxInvariants(this) :
                new NeoSim4JavaException("GeneratorFuelModel.blackBoxInvariants(this)");
    }

    @Override
    public void endSimulation(Time endTime) {
        logMessage("Simulation ends!\n");
        super.endSimulation(endTime);
    }
}
