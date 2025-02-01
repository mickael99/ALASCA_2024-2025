package fr.sorbonne_u.components.equipments.generator.mil;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import fr.sorbonne_u.components.equipments.generator.mil.events.AbstractGeneratorEvents;
import fr.sorbonne_u.components.equipments.generator.mil.events.ActivateGeneratorEvent;
import fr.sorbonne_u.components.equipments.generator.mil.events.StopGeneratorEvent;
import fr.sorbonne_u.components.utils.Electricity;
import fr.sorbonne_u.devs_simulation.exceptions.NeoSim4JavaException;
import fr.sorbonne_u.devs_simulation.hioa.annotations.ExportedVariable;
import fr.sorbonne_u.devs_simulation.hioa.annotations.ImportedVariable;
import fr.sorbonne_u.devs_simulation.hioa.annotations.ModelExportedVariable;
import fr.sorbonne_u.devs_simulation.hioa.annotations.ModelImportedVariable;
import fr.sorbonne_u.devs_simulation.hioa.models.AtomicHIOA;
import fr.sorbonne_u.devs_simulation.hioa.models.vars.Value;
import fr.sorbonne_u.devs_simulation.models.annotations.ModelExternalEvents;
import fr.sorbonne_u.devs_simulation.models.events.Event;
import fr.sorbonne_u.devs_simulation.models.events.EventI;
import fr.sorbonne_u.devs_simulation.models.time.Duration;
import fr.sorbonne_u.devs_simulation.models.time.Time;
import fr.sorbonne_u.devs_simulation.simulators.interfaces.AtomicSimulatorI;
import fr.sorbonne_u.devs_simulation.utils.InvariantChecking;
import fr.sorbonne_u.devs_simulation.utils.Pair;
import fr.sorbonne_u.devs_simulation.utils.StandardLogger;

@ModelExternalEvents(imported = {
        ActivateGeneratorEvent.class,
        StopGeneratorEvent.class
})
@ModelExportedVariable(name = "currentProduction", type = Double.class)
@ModelImportedVariable(name = "currentFuelLevel", type = Double.class)
public class GeneratorElectricityModel extends AtomicHIOA implements GeneratorOperationI {

    private static final long serialVersionUID = 1L;
	
    public static final String URI = GeneratorElectricityModel.class.getSimpleName();
    public static final String MIL_URI = GeneratorElectricityModel.class.getSimpleName() + "-MIL";
    public static final String MIL_RT_URI = GeneratorElectricityModel.class.getSimpleName() + "-MIL_RT";
    public static final String SIL_URI = GeneratorElectricityModel.class.getSimpleName() + "-MIL_RT";

    protected static final double PRODUCTION = 100.0;
    protected double totalProduction;

    protected boolean isRunning; 
    protected boolean productionHasChanged;
    
    @ImportedVariable(type = Double.class)
    protected Value<Double> currentFuelLevel;
    
    @ExportedVariable(type = Double.class)
    protected final Value<Double> currentProduction = new Value<Double>(this);

    // -------------------------------------------------------------------------
    // Invariants
    // -------------------------------------------------------------------------

    protected static boolean glassBoxInvariants(GeneratorElectricityModel m) {
        assert	m != null :
                new NeoSim4JavaException("Precondition violation: "
                                         + "m != null");

        boolean ret = true;
        ret &= InvariantChecking.checkGlassBoxInvariant(
                !m.currentProduction.isInitialised() || m.currentProduction.getValue() >= 0.0,
                GeneratorElectricityModel.class,
                m,
                "currentProduction >= 0.0");
//        ret &= InvariantChecking.checkGlassBoxInvariant(
//                !m.currentFuelLevel.isInitialised() || m.currentFuelLevel.getValue() >= 0.0,
//                GeneratorElectricityModel.class,
//                m,
//                "currentFuelLevel >= 0.0");
        ret &= InvariantChecking.checkGlassBoxInvariant(
                PRODUCTION >= 0.0,
                GeneratorElectricityModel.class,
                m,
                "currentProduction time >= 0.0");
        ret &= InvariantChecking.checkGlassBoxInvariant(
                m.totalProduction >= 0.0,
                GeneratorElectricityModel.class,
                m,
                "totalProduction >= 0.0");

        return ret;
    }

    protected static boolean blackBoxInvariants(GeneratorElectricityModel m) {
        assert	m != null :
                new NeoSim4JavaException("Precondition violation: "
                                         + "m != null");

        boolean ret = true;
        ret &= InvariantChecking.checkBlackBoxInvariant(
                MIL_URI != null && !MIL_URI.isEmpty(),
                GeneratorElectricityModel.class,
                m,
                "MIL_URI != null && !MIL_URI.isEmpty()");
        ret &= InvariantChecking.checkBlackBoxInvariant(
                MIL_RT_URI != null && !MIL_RT_URI.isEmpty(),
                GeneratorElectricityModel.class,
                m,
                "MIL_RT_URI != null && !MIL_RT_URI.isEmpty()");
        ret &= InvariantChecking.checkBlackBoxInvariant(
                SIL_URI != null && !SIL_URI.isEmpty(),
                GeneratorElectricityModel.class,
                m,
                "SIL_URI != null && !SIL_URI.isEmpty()");

        return ret;
    }

    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------
	
    public GeneratorElectricityModel(String uri, TimeUnit simulatedTimeUnit, AtomicSimulatorI simulationEngine) {
		super(uri, simulatedTimeUnit, simulationEngine);
		
		this.getSimulationEngine().setLogger(new StandardLogger());

        assert glassBoxInvariants(this) :
                new NeoSim4JavaException("GeneratorElectricityModel.glassBoxInvariants(this)");
        assert blackBoxInvariants(this) :
                new NeoSim4JavaException("GeneratorElectricityModel.blackBoxInvariants(this)");
	}
    
    public boolean isRunning() {
        return this.isRunning;
    }
    
    public boolean hasChanged() {
        return this.productionHasChanged;
    }
    
    public void activate() {
        if(!this.isRunning) {
            this.isRunning = true;
            this.toggleProdectionHasChanged();
        }
    }
    
    public void stop() {
        if (this.isRunning) {
            this.isRunning = false;
            this.toggleProdectionHasChanged();
        }
    }
    
    public void setProductionHasChanged(boolean productionHasChanged) {
        this.productionHasChanged = productionHasChanged;
    }

    protected void toggleProdectionHasChanged() {
        if (this.productionHasChanged) {
            this.productionHasChanged = false;
        } else {
            this.productionHasChanged = true;
        }
    }

    // -------------------------------------------------------------------------
    // DEVS simulation protocol
    // -------------------------------------------------------------------------
    
    @Override
	public void initialiseVariables() {
		super.initialiseVariables();

		this.currentProduction.initialise(0.0);
		this.isRunning = false;
		this.productionHasChanged = false;
		
		this.getSimulationEngine().toggleDebugMode();
		this.logMessage("simulation begins.\n");

        assert glassBoxInvariants(this) :
                new NeoSim4JavaException("GeneratorElectricityModel.glassBoxInvariants(this)");
        assert blackBoxInvariants(this) :
                new NeoSim4JavaException("GeneratorElectricityModel.blackBoxInvariants(this)");
	}

//    public void initialiseState(Time initialTime){
//        super.initialiseState(initialTime);
//
//        this.isRunning = false;
//        this.productionHasChanged = false;
//
//        this.getSimulationEngine().toggleDebugMode();
//        this.logMessage("simulation begins.\n");
//
//        assert glassBoxInvariants(this) :
//                new NeoSim4JavaException("GeneratorElectricityModel.glassBoxInvariants(this)");
//        assert blackBoxInvariants(this) :
//                new NeoSim4JavaException("GeneratorElectricityModel.blackBoxInvariants(this)");
//    }
//
//    @Override
//    public boolean		useFixpointInitialiseVariables()
//    {
//        return true;
//    }
//
//    public Pair<Integer, Integer> fixpointInitialiseVariables(int modelId, Object globalState) {
//        Pair<Integer, Integer> ret = null;
//
//        if(!this.currentProduction.isInitialised() || !this.currentProduction.) {
//            this.currentProduction.setNewValue(0.0, this.getCurrentStateTime());
//            ret = new Pair<Integer, Integer>(1, 0);
//        }
//    }
    
    @Override
    public ArrayList<EventI> output() {
        return null;
    }
    
    @Override
    public Duration timeAdvance() {
        Duration ret = null;
        if(productionHasChanged) {
            this.toggleProdectionHasChanged();
            ret = new Duration(0.0, getSimulatedTimeUnit());
        }
        ret = Duration.INFINITY;

        assert glassBoxInvariants(this) :
                new NeoSim4JavaException("GeneratorElectricityModel.glassBoxInvariants(this)");
        assert blackBoxInvariants(this) :
                new NeoSim4JavaException("GeneratorElectricityModel.blackBoxInvariants(this)");

        return ret;
    }
    
    @Override
    public void userDefinedInternalTransition(Duration elapsedTime) {
        super.userDefinedInternalTransition(elapsedTime);

        if(isRunning) {
            if(this.currentFuelLevel.getValue() > 0.0) 
            	this.currentProduction.setNewValue(PRODUCTION, this.getCurrentStateTime());
        } 
        else 
        	this.currentProduction.setNewValue(0.0, this.getCurrentStateTime());


        this.logMessage("Current production " + currentProduction.getValue() + " at " + currentProduction.getTime() +
                " | Fuel level " + currentFuelLevel.getValue() + " l" + "\n");

        assert glassBoxInvariants(this) :
                new NeoSim4JavaException("GeneratorElectricityModel.glassBoxInvariants(this)");
        assert blackBoxInvariants(this) :
                new NeoSim4JavaException("GeneratorElectricityModel.blackBoxInvariants(this)");
    }

    @Override
    public void userDefinedExternalTransition(Duration elapsedTime) {
        ArrayList<EventI> currentEvents = this.getStoredEventAndReset();
        assert	currentEvents != null && currentEvents.size() == 1;
        Event currentEvent = (Event) currentEvents.get(0);

        this.totalProduction += this.currentProduction.getValue();
        StringBuffer message =
                new StringBuffer("executes an external transition ");
        message.append(currentEvent.toString());
        message.append(")");
        this.logMessage(message.toString());

        assert currentEvent instanceof AbstractGeneratorEvents;
        currentEvent.executeOn(this);

        super.userDefinedExternalTransition(elapsedTime);

        assert glassBoxInvariants(this) :
                new NeoSim4JavaException("GeneratorElectricityModel.glassBoxInvariants(this)");
        assert blackBoxInvariants(this) :
                new NeoSim4JavaException("GeneratorElectricityModel.blackBoxInvariants(this)");
    }
    
    @Override
    public void endSimulation(Time endTime) {
        this.totalProduction += this.currentProduction.getValue();
        logMessage("Simulation ends!\n");
        super.endSimulation(endTime);
    }
}
