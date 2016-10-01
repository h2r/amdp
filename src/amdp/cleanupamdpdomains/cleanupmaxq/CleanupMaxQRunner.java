package amdp.cleanupamdpdomains.cleanupmaxq;

import amdp.amdpframework.GroundedPropSC;
import amdp.cleanup.CleanupDomain;
import amdp.cleanup.CleanupVisualiser;
import amdp.cleanup.FixedDoorCleanupEnv;
import amdp.cleanupamdpdomains.cleanupamdp.CleanupDriver;
import amdp.cleanupamdpdomains.cleanuplevel1.CleanupL1Domain;
import amdp.cleanupamdpdomains.cleanuplevel1.state.CleanupL1StateMapper;
import amdp.cleanupamdpdomains.cleanuplevel2.CleanupL2Domain;
import amdp.cleanupamdpdomains.cleanuplevel2.state.CleanupL2StateMapper;
import amdp.maxq.framework.MAXQCleanupTesting;
import amdp.maxq.framework.TaskNode;
import amdp.taxiamdpdomains.testingtools.MutableGlobalInteger;
import amdp.utilities.BoltzmannQPolicyWithCoolingSchedule;
import burlap.behavior.singleagent.Episode;
import burlap.behavior.singleagent.auxiliary.EpisodeSequenceVisualizer;
import burlap.debugtools.DPrint;
import burlap.debugtools.RandomFactory;
import burlap.mdp.auxiliary.common.GoalConditionTF;
import burlap.mdp.auxiliary.stateconditiontest.StateConditionTest;
import burlap.mdp.core.TerminalFunction;
import burlap.mdp.core.oo.propositional.GroundedProp;
import burlap.mdp.core.oo.propositional.PropositionalFunction;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.common.GoalBasedRF;
import burlap.mdp.singleagent.environment.SimulatedEnvironment;
import burlap.mdp.singleagent.model.RewardFunction;
import burlap.mdp.singleagent.oo.OOSADomain;
import burlap.shell.visual.VisualExplorer;
import burlap.statehashing.simple.SimpleHashableStateFactory;
import burlap.visualizer.Visualizer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

/**
 * Created by ngopalan on 9/6/16.
 */
public class CleanupMaxQRunner {

    static protected MutableGlobalInteger bellmanBudget = new MutableGlobalInteger(-1);

    public static void main(String[] args) {


        int debugCode = 12344356;
        DPrint.toggleCode(3214986, false);
        DPrint.toggleCode(12344356, false);
        RandomFactory randomFactory = new RandomFactory();
        Random rand = randomFactory.getMapped(0);

        double lockProb = 0.5;

        Integer rooms = 1;
        Integer numberOfObjects = 1;

        int numEnvSteps = 255;

        int totalBudget=8000000;

        for(int i =0;i<args.length;i++){
            String str = args[i];
//            System.out.println(str);
            if(str.equals("-r")){
                rooms = Integer.parseInt(args[i+1]);
            }
            if(str.equals("-b")){
                totalBudget = Integer.parseInt(args[i+1]);
            }
            if(str.equals("-o")){
                numberOfObjects = Integer.parseInt(args[i+1]);
            }
        }


        bellmanBudget.setValue(totalBudget);

        PropositionalFunction pf = new CleanupDomain.PF_InRegion(CleanupDomain.PF_BLOCK_IN_ROOM, new String[]{CleanupDomain.CLASS_BLOCK, CleanupDomain.CLASS_ROOM}, false);


        State s;

        String goalRoom;

        if(rooms == 1){
            s = CleanupDomain.getClassicState(true);
            goalRoom = "room1";
//            gp =  new GroundedProp(pf,new String[]{"block0", "room1"});

        }else if(rooms == 2){
            s = CleanupDomain.getState(true, true, 3, 3);
            goalRoom = "room2";

        }else{
            s = CleanupDomain.getState(true, true, 3, 4);
            goalRoom = "room3";
        }


        GroundedProp gp =  new GroundedProp(pf,new String[]{"block0", goalRoom});

        GroundedPropSC l0sc = new GroundedPropSC(gp);
        GoalBasedRF l0rf = new GoalBasedRF(l0sc, 1., 0.);
        GoalConditionTF l0tf = new GoalConditionTF(l0sc);
        CleanupDomain dgen = new CleanupDomain(l0rf,l0tf, rand);
        dgen.includeDirectionAttribute(true);
        dgen.includePullAction(true);
        dgen.includeWallPF_s(true);
        dgen.includeLockableDoors(true);
        dgen.setLockProbability(lockProb);
        OOSADomain domain = dgen.generateDomain();


        StateConditionTest sc = new CleanupL1Domain.InRegionSC("block0", goalRoom);
        RewardFunction rf = new GoalBasedRF(sc, 1.);
        TerminalFunction tf = new GoalConditionTF(sc);

        CleanupL1Domain adgen = new CleanupL1Domain(domain, rf, tf);
        adgen.setLockableDoors(true);
        adgen.setLockProb(lockProb);
        OOSADomain adomain = adgen.generateDomain();

        State as = new CleanupL1StateMapper().mapState(s);

        List<String> goalBlocks = Arrays.asList("block0" );//"block1"
        List<String> goalRooms = Arrays.asList(goalRoom);//"room3"
        StateConditionTest l2sc = new CleanupDriver.L2Goal(goalBlocks, goalRooms);
        GoalBasedRF l2rf = new GoalBasedRF(l2sc, 1., 0.);
        GoalConditionTF l2tf = new GoalConditionTF(l2sc);


        CleanupL2Domain aadgen = new CleanupL2Domain(l2rf,l2tf);
        OOSADomain aadomain = aadgen.generateDomain();

        State aas = new CleanupL2StateMapper().mapState(as);


        //TaskNodes
        TaskNode nt = new CleanupL0TaskNode(CleanupDomain.ACTION_NORTH, dgen.generateDomain());
        TaskNode et = new CleanupL0TaskNode(CleanupDomain.ACTION_EAST, dgen.generateDomain());
        TaskNode st = new CleanupL0TaskNode(CleanupDomain.ACTION_SOUTH, dgen.generateDomain());
        TaskNode wt = new CleanupL0TaskNode(CleanupDomain.ACTION_WEST, dgen.generateDomain());
        TaskNode pt = new CleanupL0TaskNode(CleanupDomain.ACTION_PULL, dgen.generateDomain());

        TaskNode[] L1Subtasks = new TaskNode[]{nt, et, st, pt, wt};

        TaskNode a2dt = new CleanupL1TaskNode(CleanupL1Domain.ACTION_AGENT_TO_DOOR,adgen.generateDomain(), dgen.generateDomain(), L1Subtasks);
        TaskNode a2rt = new CleanupL1TaskNode(CleanupL1Domain.ACTION_AGENT_TO_ROOM, adgen.generateDomain(), dgen.generateDomain(), L1Subtasks);
        TaskNode b2dt = new CleanupL1TaskNode(CleanupL1Domain.ACTION_BLOCK_TO_DOOR, adgen.generateDomain(), dgen.generateDomain(), L1Subtasks);
        TaskNode b2rt = new CleanupL1TaskNode(CleanupL1Domain.ACTION_BLOCK_TO_ROOM, adgen.generateDomain(), dgen.generateDomain(), L1Subtasks);

        TaskNode[] L2Subtasks = new TaskNode[]{a2dt, a2rt, b2dt, b2rt};

        TaskNode a2r_l2t = new CleanupL2TaskNode(CleanupL1Domain.ACTION_AGENT_TO_ROOM, aadgen.generateDomain(),L2Subtasks);
        TaskNode b2r_l2t = new CleanupL2TaskNode(CleanupL1Domain.ACTION_AGENT_TO_ROOM, aadgen.generateDomain(),L2Subtasks);

        TaskNode rootNode = new RootTaskNode("root", new TaskNode[]{a2r_l2t,b2r_l2t},l2tf);

        String str = "-------- MAXQ Test! ----------";
        DPrint.cl(debugCode,str);
//
//            State state = TaxiDomain.getClassicState(d, false);

        int numberOfTests = 1;
        int numberOfLearningEpisodes = 100;
        int takeModOf = 10;
        int startTest = 200;


        int maxSteps = 101;



        List<Episode> episodesMAXQ = new ArrayList<Episode>();
        List<Episode> testEpisodesMAXQ = new ArrayList<Episode>();
//                MAXQLearningAgent maxqLearningAgent = new MAXQLearningAgent(rootNode, new SimpleHashableStateFactory(), 1.0, 1.0);
//                MAXQStateAbstractionAgent maxqLearningAgent = new MAXQStateAbstractionAgent(rootNode, new SimpleHashableStateFactory(), 1.0, 0.25);
//        MaxQForTesting maxqLearningAgent = new MaxQForTesting(rootNode, new SimpleHashableStateFactory(), 1.0, 0.25, bellmanUpdateBudget);
        MAXQCleanupTesting maxqLearningAgent = new MAXQCleanupTesting(rootNode, new SimpleHashableStateFactory(), 1.0, 0.25, totalBudget);
//                MAX0LearningAgent maxqLearningAgent = new MAX0LearningAgent(rootNode, new SimpleHashableStateFactory(), 0.95, 0.5);
//                MAX0FasterLearningAgent maxqLearningAgent = new MAX0FasterLearningAgent(rootNode, new SimpleHashableStateFactory(), 0.95, 0.70);
//                maxqLearningAgent.setRmax(0.123 * (1 - 0.95));
        DPrint.toggleCode(maxqLearningAgent.debugCode,false);
        maxqLearningAgent.setVmax(0.123);
        maxqLearningAgent.setQProviderForTaskNode(rootNode);
        maxqLearningAgent.setQProviderForTaskNode(a2r_l2t);
        maxqLearningAgent.setQProviderForTaskNode(b2r_l2t);
        maxqLearningAgent.setQProviderForTaskNode(a2dt);
        maxqLearningAgent.setQProviderForTaskNode(b2dt);
        maxqLearningAgent.setQProviderForTaskNode(a2rt);
        maxqLearningAgent.setQProviderForTaskNode(b2rt);

        maxqLearningAgent.setSolverDerivedPolicyForTaskNode(rootNode, new BoltzmannQPolicyWithCoolingSchedule(50, 0.99996));
        maxqLearningAgent.setSolverDerivedPolicyForTaskNode(a2r_l2t, new BoltzmannQPolicyWithCoolingSchedule(50, 0.99939));
        maxqLearningAgent.setSolverDerivedPolicyForTaskNode(b2r_l2t, new BoltzmannQPolicyWithCoolingSchedule(50, 0.99996));
        maxqLearningAgent.setSolverDerivedPolicyForTaskNode(a2dt, new BoltzmannQPolicyWithCoolingSchedule(50, 0.99879));
        maxqLearningAgent.setSolverDerivedPolicyForTaskNode(a2rt, new BoltzmannQPolicyWithCoolingSchedule(50, 0.99879));
        maxqLearningAgent.setSolverDerivedPolicyForTaskNode(b2dt, new BoltzmannQPolicyWithCoolingSchedule(50, 0.99879));
        maxqLearningAgent.setSolverDerivedPolicyForTaskNode(b2rt, new BoltzmannQPolicyWithCoolingSchedule(50, 0.99879));
//            maxqLearningAgent.setQProviderForTaskNode(rootNode);


        int episodeNum = 0;
        while(maxqLearningAgent.getNumberOfBackups()<totalBudget) {

            str = "MAXQ learning episode: " + episodeNum;
            DPrint.cl(debugCode,str);
            str = "-------------------------------------------------------------";
            DPrint.cl(debugCode,str);
            State sNew;// = TaxiDomain.getRandomClassicState(rand, d, false);

            if(rooms == 1){
                sNew = CleanupDomain.getClassicState(true);

//            gp =  new GroundedProp(pf,new String[]{"block0", "room1"});

            }else if(rooms == 2){
                sNew = CleanupDomain.getState(true, true, 3, 3);


            }else{
                sNew = CleanupDomain.getState(true, true, 3, 4);

            }


//            State sNew = TaxiDomain.getComplexState(false);
            SimulatedEnvironment envN = new SimulatedEnvironment(dgen.generateDomain(), sNew);

            Episode ea = maxqLearningAgent.runLearningEpisode(envN, 5000);
            episodesMAXQ.add(ea);

            episodeNum++;


        }


        maxqLearningAgent.setFreezeLearning(true);
        str = "-------------------------------------------------------------";
        DPrint.cl(debugCode,str);
        State sNew1;
        if(rooms == 1){
            sNew1 = CleanupDomain.getClassicState(true);

//            gp =  new GroundedProp(pf,new String[]{"block0", "room1"});

        }else if(rooms == 2){
            sNew1 = CleanupDomain.getState(true, true, 3, 3);


        }else{
            sNew1 = CleanupDomain.getState(true, true, 3, 4);

        }

        FixedDoorCleanupEnv envN1 = new FixedDoorCleanupEnv(dgen.generateDomain(), sNew1);
        if(rooms==3) {
            envN1.addLockedDoor("door0");
        }
        Episode ea1 = maxqLearningAgent.runLearningEpisode(envN1, maxSteps);
//                    episodesMAXQ.add(ea1);

        maxqLearningAgent.setFreezeLearning(false);
        int numActions = 0;
        for (Episode eaTemp : episodesMAXQ) {
            numActions += eaTemp.actionSequence.size();
        }

        int testActions = ea1.numActions();
        str = "test actions:" + testActions;
//        DPrint.cl(debugCode,str);
        testEpisodesMAXQ.add(ea1);
        System.out.println(testActions);
        System.out.println(ea1.discountedReturn(1.0));
        str = "number of backups: " + maxqLearningAgent.getNumberOfBackups();
//        DPrint.cl(debugCode,str);
//        System.out.println(str);
        System.out.println(maxqLearningAgent.getNumberOfBackups());
//        System.out.println("random start: " +randomStart);
        System.out.println("MAXQ!");
        System.out.println("room: " + rooms);

        str = "number of params MAXQQ = " + maxqLearningAgent.numberOfParams();
//        DPrint.cl(debugCode,str);
        System.out.println(str);
//
//        Visualizer v = CleanupVisualiser.getVisualizer("amdp/data/resources/robotImages");
//        new EpisodeSequenceVisualizer(v, dgen.generateDomain(), testEpisodesMAXQ);
//        new EpisodeSequenceVisualizer(v, dgen.generateDomain(), episodesMAXQ);



    }
}
