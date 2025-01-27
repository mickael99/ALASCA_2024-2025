package fr.sorbonne_u.components.equipments.toaster.unittests;

import fr.sorbonne_u.components.AbstractComponent;
import fr.sorbonne_u.components.CoordinatorComponent;
import fr.sorbonne_u.components.cvm.AbstractCVM;
import fr.sorbonne_u.components.cyphy.utils.aclocks.ClocksServerWithSimulation;
import fr.sorbonne_u.components.equipments.toaster.Toaster;
import fr.sorbonne_u.components.equipments.toaster.ToasterUser;
import fr.sorbonne_u.components.equipments.toaster.mil.ToasterCoupledModel;
import fr.sorbonne_u.components.equipments.toaster.mil.ToasterUnitTesterModel;
import fr.sorbonne_u.components.utils.ExecutionType;
import fr.sorbonne_u.components.utils.SimulationType;
import fr.sorbonne_u.devs_simulation.exceptions.NeoSim4JavaException;
import fr.sorbonne_u.exceptions.VerboseException;
import fr.sorbonne_u.utils.aclocks.ClocksServer;

import java.time.Instant;
import java.util.concurrent.TimeUnit;

public class CVM_ToasterUnitTest extends AbstractCVM {
    // -------------------------------------------------------------------------
    // Constants and variables
    // -------------------------------------------------------------------------
    public static long				DELAY_TO_START = 3000L;
    /** duration of the execution when simulation is *not* used.			*/
    public static long				EXECUTION_DURATION = 5000L;
    /** delay before the CVM must stop the execution after the execution
     *  of the tests scenarios and, possibly, the attached simulations.		*/
    public static long				DELAY_TO_STOP = 2000L;
    /** duration of the sleep at the end of the execution before exiting
     *  the JVM.															*/
    public static long				END_SLEEP_DURATION = 10000L;

    public static long				DELAY_TO_START_SIMULATION = 3000L;
    /** start time of the simulation, in simulated logical time, if
     *  relevant.															*/
    public static double 			SIMULATION_START_TIME = 0.0;
    /** duration of the simulation, in simulated time.						*/
    public static double			SIMULATION_DURATION = 3.0;
    /** time unit in which {@code SIMULATION_DURATION} is expressed.		*/
    public static TimeUnit SIMULATION_TIME_UNIT = TimeUnit.HOURS;
    /** for real time simulations, the acceleration factor applied to the
     *  the simulated time to get the execution time of the simulations. 	*/
    public static double			ACCELERATION_FACTOR = 360.0;

    /** the type of execution, to select among the values of the
     *  enumeration {@code ExecutionType}.									*/
    public static ExecutionType CURRENT_EXECUTION_TYPE =
            //ExecutionType.STANDARD;
            ExecutionType.UNIT_TEST;

    public static SimulationType	CURRENT_SIMULATION_TYPE =
            //SimulationType.NO_SIMULATION;
            //SimulationType.MIL_RT_SIMULATION;
            SimulationType.SIL_SIMULATION;

    public static String			CLOCK_URI = "hem-clock";
    /** start instant in test scenarios.									*/
    public static String			START_INSTANT = "2024-10-18T00:00:00.00Z";

    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------
    public				CVM_ToasterUnitTest() throws Exception{
        ClocksServer.VERBOSE = true;
        ClocksServer.X_RELATIVE_POSITION = 0;
        ClocksServer.Y_RELATIVE_POSITION = 0;
        ToasterUnitTestSupervisor.VERBOSE = true;
        ToasterUnitTestSupervisor.X_RELATIVE_POSITION = 1;
        ToasterUnitTestSupervisor.Y_RELATIVE_POSITION = 0;
        CoordinatorComponent.VERBOSE = true;
        CoordinatorComponent.X_RELATIVE_POSITION = 2;
        CoordinatorComponent.Y_RELATIVE_POSITION = 0;

        Toaster.VERBOSE = true;
        Toaster.X_RELATIVE_POSITION = 1;
        Toaster.Y_RELATIVE_POSITION = 1;
        ToasterUser.VERBOSE = true;
        ToasterUser.X_RELATIVE_POSITION = 0;
        ToasterUser.Y_RELATIVE_POSITION = 1;
    }

    // -------------------------------------------------------------------------
    // Component life-cycle
    // -------------------------------------------------------------------------
    @Override
    public void			deploy() throws Exception {
        assert	!CURRENT_EXECUTION_TYPE.isIntegrationTest() :
                new RuntimeException(
                        "!CURRENT_EXECUTION_TYPE.isIntegrationTest()");
        assert	!CURRENT_EXECUTION_TYPE.isStandard() ||
                  CURRENT_SIMULATION_TYPE.isNoSimulation() :
                new RuntimeException(
                        "!CURRENT_EXECUTION_TYPE.isStandard() || "
                        + "CURRENT_SIMULATION_TYPE.isNoSimulation()");

        String globalArchitectureURI = "";
        String toasterLocalArchitectureURI = "";
        String toasterUserLocalArchitectureURI = "";

        long current = System.currentTimeMillis();
        long unixEpochStartTimeInMillis = current + DELAY_TO_START;

        switch (CURRENT_SIMULATION_TYPE) {
            case MIL_SIMULATION:
                globalArchitectureURI = ToasterUnitTestSupervisor.MIL_ARCHITECTURE_URI;
                toasterLocalArchitectureURI = ToasterCoupledModel.MIL_URI;
                toasterUserLocalArchitectureURI = ToasterUnitTesterModel.MIL_URI;
                break;
            case MIL_RT_SIMULATION:
                globalArchitectureURI = ToasterUnitTestSupervisor.MIL_RT_ARCHITECTURE_URI;
                toasterLocalArchitectureURI = ToasterCoupledModel.MIL_RT_URI;
                toasterUserLocalArchitectureURI = ToasterUnitTesterModel.MIL_RT_URI;
                break;
            case SIL_SIMULATION:
                globalArchitectureURI = ToasterUnitTestSupervisor.SIL_ARCHITECTURE_URI;
                toasterLocalArchitectureURI =  ToasterCoupledModel.SIL_URI;
                toasterUserLocalArchitectureURI = "not-used";
                break;
            case NO_SIMULATION:
            default:
        }

        AbstractComponent.createComponent(
                Toaster.class.getCanonicalName(),
                new Object[]{Toaster.REFLECTION_INBOUND_PORT_URI,
                             Toaster.INBOUND_PORT_URI,
                             CURRENT_EXECUTION_TYPE,
                             CURRENT_SIMULATION_TYPE,
                             globalArchitectureURI,
                             toasterLocalArchitectureURI,
                             SIMULATION_TIME_UNIT,
                             ACCELERATION_FACTOR,
                             CLOCK_URI});
        AbstractComponent.createComponent(
                ToasterUser.class.getCanonicalName(),
                new Object[]{ToasterUser.REFLECTION_INBOUND_PORT_URI,
                             Toaster.INBOUND_PORT_URI,
                             CURRENT_EXECUTION_TYPE,
                             CURRENT_SIMULATION_TYPE,
                             globalArchitectureURI,
                             toasterUserLocalArchitectureURI,
                             SIMULATION_TIME_UNIT,
                             ACCELERATION_FACTOR,
                             CLOCK_URI});

        AbstractComponent.createComponent(
                ClocksServerWithSimulation.class.getCanonicalName(),
                new Object[]{
                        CLOCK_URI,
                        TimeUnit.MILLISECONDS.toNanos(
                                unixEpochStartTimeInMillis),
                        Instant.parse(START_INSTANT),
                        ACCELERATION_FACTOR,
                        DELAY_TO_START_SIMULATION,
                        SIMULATION_START_TIME,
                        SIMULATION_DURATION,
                        SIMULATION_TIME_UNIT});

        if (CURRENT_SIMULATION_TYPE.isMilSimulation() ||
            CURRENT_SIMULATION_TYPE.isMILRTSimulation()) {
            AbstractComponent.createComponent(
                    CoordinatorComponent.class.getCanonicalName(),
                    new Object[]{});
            AbstractComponent.createComponent(
                    ToasterUnitTestSupervisor.class.getCanonicalName(),
                    new Object[]{CURRENT_SIMULATION_TYPE,
                                 globalArchitectureURI});
        }
        super.deploy();
    }

    public static void main(String[] args) {
        VerboseException.VERBOSE = true;
        VerboseException.PRINT_STACK_TRACE = true;
        NeoSim4JavaException.VERBOSE = true;
        NeoSim4JavaException.PRINT_STACK_TRACE = true;
        try {
            CVM_ToasterUnitTest cvm = new CVM_ToasterUnitTest();
            long executionDurationInMillis = 0L;
            switch (CURRENT_SIMULATION_TYPE) {
                case MIL_SIMULATION:
                    executionDurationInMillis =
                            DELAY_TO_START + DELAY_TO_START_SIMULATION
                            + EXECUTION_DURATION + DELAY_TO_STOP;
                    break;
                case MIL_RT_SIMULATION:
                case SIL_SIMULATION:
                    executionDurationInMillis =
                            DELAY_TO_START + DELAY_TO_START_SIMULATION
                            + ((long)(((double)SIMULATION_TIME_UNIT.toMillis(1))
                                      * (SIMULATION_DURATION/ACCELERATION_FACTOR)))
                            + DELAY_TO_STOP;
                    break;
                case NO_SIMULATION:
                    executionDurationInMillis =
                            DELAY_TO_START + EXECUTION_DURATION + DELAY_TO_STOP;
                    break;
                default:
            }
            System.out.println("starting for " + executionDurationInMillis);
            cvm.startStandardLifeCycle(executionDurationInMillis);
            Thread.sleep(END_SLEEP_DURATION);
            System.exit(0);
        } catch (Exception e) {
            throw new RuntimeException(e) ;
        }
    }
}
