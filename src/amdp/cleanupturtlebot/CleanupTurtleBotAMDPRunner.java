package amdp.cleanupturtlebot;

import amdp.amdpframework.*;
import amdp.cleanup.CleanupVisualiser;
import amdp.cleanup.FixedDoorCleanupEnv;
import amdp.cleanup.PullCostGoalRF;


import amdp.cleanup.state.CleanupAgent;
import amdp.cleanup.state.CleanupDoor;
import amdp.cleanup.state.CleanupState;
import amdp.cleanupamdpdomains.cleanupamdp.RootTaskNode;
import amdp.cleanupamdpdomains.cleanuplevel1.CleanupL1Domain;
import amdp.cleanupamdpdomains.cleanuplevel1.state.CleanupL1StateMapper;
import amdp.cleanupamdpdomains.cleanuplevel2.CleanupL2Domain;
import amdp.cleanupamdpdomains.cleanuplevel2.state.CleanupBlockL2;
import amdp.cleanupamdpdomains.cleanuplevel2.state.CleanupL2State;
import amdp.cleanupamdpdomains.cleanuplevel2.state.CleanupL2StateMapper;
import amdp.cleanupturtlebot.cleanupcontinuous.CleanupContinuousDomain;
import amdp.cleanupturtlebot.cleanupcontinuous.CleanupContinuousVisualiser;
import amdp.cleanupturtlebot.cleanupcontinuous.state.CleanupContinuousState;
import amdp.cleanupturtlebot.cleanupl0discrete.CleanupTurtleBotL0Domain;
import amdp.cleanupturtlebot.cleanupl0discrete.state.CleanupContinuousToDiscreteStateMapper;
import amdp.taxiamdpdomains.testingtools.BoundedRTDPForTests;
import amdp.taxiamdpdomains.testingtools.GreedyReplan;
import amdp.taxiamdpdomains.testingtools.MutableGlobalInteger;
import burlap.behavior.policy.Policy;
import burlap.behavior.policy.PolicyUtils;
import burlap.behavior.singleagent.Episode;
import burlap.behavior.singleagent.auxiliary.EpisodeSequenceVisualizer;
import burlap.behavior.valuefunction.ConstantValueFunction;
import burlap.behavior.valuefunction.QProvider;
import burlap.behavior.valuefunction.ValueFunction;
import burlap.debugtools.DPrint;
import burlap.debugtools.RandomFactory;
import burlap.mdp.auxiliary.StateMapping;
import burlap.mdp.auxiliary.common.GoalConditionTF;
import burlap.mdp.auxiliary.stateconditiontest.StateConditionTest;
import burlap.mdp.core.Domain;
import burlap.mdp.core.TerminalFunction;
import burlap.mdp.core.action.Action;
import burlap.mdp.core.action.ActionType;
import burlap.mdp.core.oo.propositional.GroundedProp;
import burlap.mdp.core.oo.propositional.PropositionalFunction;
import burlap.mdp.core.oo.state.OOState;
import burlap.mdp.core.oo.state.ObjectInstance;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.common.GoalBasedRF;
import burlap.mdp.singleagent.environment.SimulatedEnvironment;
import burlap.mdp.singleagent.model.FactoredModel;
import burlap.mdp.singleagent.model.RewardFunction;
import burlap.mdp.singleagent.oo.OOSADomain;
import burlap.statehashing.simple.SimpleHashableStateFactory;
import burlap.visualizer.Visualizer;
import org.apache.commons.lang3.tuple.MutablePair;

import java.util.*;

/**
 * Created by ngopalan on 9/12/16.
 */
public class CleanupTurtleBotAMDPRunner {



    static protected MutableGlobalInteger bellmanBudget = new MutableGlobalInteger(-1);
    static protected MutableGlobalInteger bellmanBudgetL0 = new MutableGlobalInteger(-1);
    static protected MutableGlobalInteger bellmanBudgetL1 = new MutableGlobalInteger(-1);
    static protected MutableGlobalInteger bellmanBudgetL2 = new MutableGlobalInteger(-1);

    static double L2Ratio = 0.05;
    static double L1Ratio = 0.65;
    static double L0Ratio = 0.3;

    static int maxTrajectoryLength = 255;

    public static List<BoundedRTDPForTests> brtdpList= new ArrayList<BoundedRTDPForTests>();

    public static Map<String, BoundedRTDPForTests> brtdpMap =  new HashMap<String, BoundedRTDPForTests>();

    public static void main(String[] args) {

        DPrint.toggleCode(3214986, true);
        RandomFactory randomFactory = new RandomFactory();
        Random rand = randomFactory.getMapped(0);

        double lockProb = 0.5;

        Integer rooms = 3;
        Integer numberOfObjects = 3;

        int numEnvSteps = 255;

        int totalBudget=-1;
//        bellmanBudget.setValue(80000);

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
            if(str.equals("-l0")){
                L0Ratio = Double.parseDouble(args[i+1]);
            }
            if(str.equals("-l1")){
                L1Ratio = Double.parseDouble(args[i+1]);
            }
            if(str.equals("-l2")){
                L2Ratio = Double.parseDouble(args[i+1]);
            }
        }


        bellmanBudget.setValue(100000);
        bellmanBudgetL0.setValue(100000);
        bellmanBudgetL1.setValue(100000);
        bellmanBudgetL2.setValue(100000);



//        State s;

        String goalRoom;

//        if(rooms == 1){
//            s = CleanupTurtleBotL0Domain.getClassicState(true);

        State s = CleanupContinuousDomain.getClassicState(true);
        goalRoom = "room1";
//            gp =  new GroundedProp(pf,new String[]{"block0", "room1"});

//        }
// else if(rooms == 2){
//            s = CleanupDomain.getState(true, true, 3, 3);
//            goalRoom = "room2";
//
//        }else{
//            s = CleanupDomain.getState(true, true, 3, 4);
//            goalRoom = "room3";
//        }

        PropositionalFunction pfL0 = new CleanupContinuousDomain.PF_InRegion(CleanupTurtleBotL0Domain.PF_BLOCK_IN_ROOM, new String[]{CleanupContinuousDomain.CLASS_BLOCK, CleanupContinuousDomain.CLASS_ROOM}, false);

        GroundedProp gp =  new GroundedProp(pfL0,new String[]{"block0", goalRoom});

        GroundedPropSC lcsc = new GroundedPropSC(gp);
        GoalBasedRF lcrf = new GoalBasedRF(lcsc, 1., 0.);

        GoalConditionTF lctf = new GoalConditionTF(lcsc);
        CleanupContinuousDomain dgen = new CleanupContinuousDomain(lcrf,lctf, rand);
//            CleanupContinuousDomain dgen = new CleanupContinuousDomain();
        dgen.includeDirectionAttribute(true);
//            dgen.includePullAction(true);
        dgen.includeWallPF_s(true);
        dgen.includeLockableDoors(true);
        dgen.setLockProbability(0.5);
        OOSADomain domain = dgen.generateDomain();
//
//        State s = CleanupContinuousDomain.getClassicState(true);



        PropositionalFunction pf = new CleanupTurtleBotL0Domain.PF_InRegion(CleanupTurtleBotL0Domain.PF_BLOCK_IN_ROOM, new String[]{CleanupContinuousDomain.CLASS_BLOCK, CleanupContinuousDomain.CLASS_ROOM}, false);

        GroundedProp gpL1 =  new GroundedProp(pf,new String[]{"block0", goalRoom});

        GroundedPropSC l0sc = new GroundedPropSC(gpL1);
        GoalBasedRF l0rf = new GoalBasedRF(l0sc, 1., 0.);
        GoalConditionTF l0tf = new GoalConditionTF(l0sc);
        CleanupTurtleBotL0Domain dgenLD = new CleanupTurtleBotL0Domain(l0rf,l0tf, rand);
        dgenLD.includeDirectionAttribute(true);
//        dgen.includePullAction(true);
        dgenLD.includeWallPF_s(true);
        dgenLD.includeLockableDoors(false);
//        dgen.setLockProbability(lockProb);
        OOSADomain domainLD = dgenLD.generateDomain();

        State sL1 = new CleanupContinuousToDiscreteStateMapper().mapState(s);


        StateConditionTest sc = new CleanupL1Domain.InRegionSC("block0", goalRoom);
        RewardFunction rf = new GoalBasedRF(sc, 1.);
        TerminalFunction tf = new GoalConditionTF(sc);

        CleanupL1Domain adgen = new CleanupL1Domain(domain, rf, tf);
        adgen.setLockableDoors(true);
        adgen.setLockProb(lockProb);
        OOSADomain adomain = adgen.generateDomain();

        State as = new CleanupL1StateMapper().mapState(sL1);
//        System.out.println(as.toString());
//
//        List<Action> a = adomain.getAction(ACTION_BLOCK_TO_DOOR).allApplicableActions(as);
//        System.out.println("actions: " + a.size());


        List<String> goalBlocks = Arrays.asList("block0" );//"block1"
        List<String> goalRooms = Arrays.asList(goalRoom);//"room3"
        StateConditionTest l2sc = new L2Goal(goalBlocks, goalRooms);
        GoalBasedRF l2rf = new GoalBasedRF(l2sc, 1., 0.);
        GoalConditionTF l2tf = new GoalConditionTF(l2sc);


        CleanupL2Domain aadgen = new CleanupL2Domain(l2rf,l2tf);
        OOSADomain aadomain = aadgen.generateDomain();

        State aas = new CleanupL2StateMapper().mapState(as);

//        l0 = ((NonPrimitiveTaskNode)gt.getT()).domain();
//        domainL1.setModel(new FactoredModel(((FactoredModel)domainL1.getModel()).getStateModel(),l0rf, l0tf));
//
//        ValueFunction heuristic = new ConstantValueFunction(1.);//getL0Heuristic(s, gt.rewardFunction(), lockProb);
//        BoundedRTDPForTests brtd = new BoundedRTDPForTests(domainL1, 0.99,
//                new SimpleHashableStateFactory(false),
//                new ConstantValueFunction(0.),
//                heuristic, 0.01, -1);
//        brtd.setRemainingNumberOfBellmanUpdates(bellmanBudgetL0);
//        brtd.setMaxRolloutDepth(100);
//        brtd.toggleDebugPrinting(false);
//        brtdpList.add(brtd);
////        brtd.planFromState(sL1);
//        SimulatedEnvironment env1 = new SimulatedEnvironment(domainL1,sL1);
//        Episode e1 = PolicyUtils.rollout(brtd.planFromState(sL1),env1);
//        Visualizer v1 =  CleanupVisualiser.getVisualizer("amdp/data/resources/robotImages");
//        //		System.out.println(ea.getState(0).toString());
//        new EpisodeSequenceVisualizer(v1, domain, Arrays.asList(e1));
////        return new GreedyReplan(brtd);





//        StateConditionTest sc = new CleanupTurtleBotL0Domain.InRegionSC("block0", goalRoom);
//        RewardFunction rf = new GoalBasedRF(sc, 1.);
//        TerminalFunction tf = new GoalConditionTF(sc);

//        CleanupL1Domain adgen = new CleanupL1Domain(domain, rf, tf);
//        adgen.setLockableDoors(true);
//        adgen.setLockProb(lockProb);
//        OOSADomain adomain = adgen.generateDomain();

//        State as = new CleanupL1StateMapper().mapState(s);
//        System.out.println(as.toString());
//
//        List<Action> a = adomain.getAction(ACTION_BLOCK_TO_DOOR).allApplicableActions(as);
//        System.out.println("actions: " + a.size());


//        List<String> goalBlocks = Arrays.asList("block0" );//"block1"
//        List<String> goalRooms = Arrays.asList(goalRoom);//"room3"
//        StateConditionTest l2sc = new amdp.cleanupamdpdomains.cleanupamdp.CleanupDriver.L2Goal(goalBlocks, goalRooms);
//        GoalBasedRF l2rf = new GoalBasedRF(l2sc, 1., 0.);
//        GoalConditionTF l2tf = new GoalConditionTF(l2sc);
//
//
//        CleanupL2Domain aadgen = new CleanupL2Domain(l2rf,l2tf);
//        OOSADomain aadomain = aadgen.generateDomain();
//
//        State aas = new CleanupL2StateMapper().mapState(as);


        //Action types

        ActionType fwd = domain.getAction(CleanupContinuousDomain.ACTION_MOVE_FORWARD);
        ActionType back = domain.getAction(CleanupContinuousDomain.ACTION_MOVE_BACK);
        ActionType turncw = domain.getAction(CleanupContinuousDomain.ACTION_TURN_CW);
        ActionType turnccw = domain.getAction(CleanupContinuousDomain.ACTION_TURN_CCW);
        // pull is assumed true
//        ActionType pull = domain.getAction(CleanupDomain.ACTION_PULL);

        ActionType fwdL1 = domainLD.getAction(CleanupTurtleBotL0Domain.ACTION_MOVE_FORWARD);
//        ActionType backL1 = domainL1.getAction(CleanupTurtleBotL0Domain.ACTION_MOVE_BACK);
        ActionType turncwL1 = domainLD.getAction(CleanupTurtleBotL0Domain.ACTION_TURN_CW);
        ActionType turnccwL1 = domainLD.getAction(CleanupTurtleBotL0Domain.ACTION_TURN_CCW);


        ActionType a2d = adomain.getAction(CleanupL1Domain.ACTION_AGENT_TO_DOOR);
        ActionType a2r = adomain.getAction(CleanupL1Domain.ACTION_AGENT_TO_ROOM);
        ActionType b2d = adomain.getAction(CleanupL1Domain.ACTION_BLOCK_TO_DOOR);
        ActionType b2r = adomain.getAction(CleanupL1Domain.ACTION_BLOCK_TO_ROOM);

        ActionType a2r_l2 = aadomain.getAction(CleanupL1Domain.ACTION_AGENT_TO_ROOM);
        ActionType b2r_l2 = aadomain.getAction(CleanupL1Domain.ACTION_BLOCK_TO_ROOM);


        //TaskNodes
        TaskNode ft = new ContinuousTaskNode(fwd);
        TaskNode bt = new ContinuousTaskNode(back);
        TaskNode cwt = new ContinuousTaskNode(turncw);
        TaskNode ccwt = new ContinuousTaskNode(turnccw);


        TaskNode[] L0Subtasks = new TaskNode[]{ft, bt, cwt, ccwt};

        TaskNode ftL0 = new L0TaskNode(fwdL1, dgen.generateDomain(), L0Subtasks);
//        TaskNode btL0 = new L0TaskNode(backL1, dgen.generateDomain(), L0Subtasks);
        TaskNode cwtL0 = new L0TaskNode(turncwL1, dgen.generateDomain(), L0Subtasks);
        TaskNode ccwtL0 = new L0TaskNode(turnccwL1, dgen.generateDomain(), L0Subtasks);

        TaskNode[] L1Subtasks = new TaskNode[]{ftL0, cwtL0, ccwtL0};

        TaskNode a2dt = new L1TaskNode(a2d, dgenLD.generateDomain(), L1Subtasks);
        TaskNode a2rt = new L1TaskNode(a2r, dgenLD.generateDomain(), L1Subtasks);
        TaskNode b2dt = new L1TaskNode(b2d, dgenLD.generateDomain(), L1Subtasks);
        TaskNode b2rt = new L1TaskNode(b2r, dgenLD.generateDomain(), L1Subtasks);

        TaskNode[] L2Subtasks = new TaskNode[]{a2dt, a2rt, b2dt, b2rt};

        TaskNode a2r_l2t = new L2TaskNode(a2r_l2, adgen.generateDomain(),L2Subtasks);
        TaskNode b2r_l2t = new L2TaskNode(b2r_l2, adgen.generateDomain(),L2Subtasks);


        TaskNode root = new RootTaskNode("root",new TaskNode[]{a2r_l2t,b2r_l2t},aadomain, l2tf,l2rf);

        //btL0,
//        TaskNode root = new RootTaskNode("root", new TaskNode[]{ftL0,  ccwtL0, cwtL0}, dgenL1.generateDomain(), l0tf, l0rf);
        List<AMDPPolicyGenerator> pgList = new ArrayList<AMDPPolicyGenerator>();
        pgList.add(0, new continuousPolicyGenerator(domain, lockProb));
        pgList.add(1, new l0PolicyGenerator(domain, lockProb));
        pgList.add(2,new amdp.cleanupamdpdomains.cleanupamdp.CleanupDriver.l1PolicyGenerator(adomain));
        pgList.add(3,new amdp.cleanupamdpdomains.cleanupamdp.CleanupDriver.l2PolicyGenerator(aadomain));

        GroundedTask gtRoot = root.getApplicableGroundedTasks(sL1).get(0);

        long tStart = System.currentTimeMillis();


        AMDPAgent agent = new AMDPAgent(gtRoot, pgList);

//        SimulatedEnvironment envN = new SimulatedEnvironment(dgen.generateDomain(), s);

        SimulatedEnvironment env = new SimulatedEnvironment(dgen.generateDomain(), s);
//        if(rooms==3) {
//            env.addLockedDoor("door0");
//        }


        Episode e = agent.actUntilTermination(env, 30000);

        long tEnd = System.currentTimeMillis();
        long tDelta = tEnd - tStart;
        double elapsedSeconds = tDelta / 1000.0;
        System.out.println("time: " + tDelta + "  " + elapsedSeconds);
        Visualizer v = CleanupContinuousVisualiser.getVisualizer("amdp/data/resources/robotImages");
        //		System.out.println(ea.getState(0).toString());
        new EpisodeSequenceVisualizer(v, domain, Arrays.asList(e));

        System.out.println(e.actionSequence.size());
        System.out.println(e.discountedReturn(1.));
        //TODO: add counters to count updates!

        int count = 0;
        for (int i = 0; i < brtdpList.size(); i++) {
            int numUpdates = brtdpList.get(i).getNumberOfBellmanUpdates();
            count += numUpdates;
        }

        System.out.println(count);
//        System.out.println( brtd.getNumberOfBellmanUpdates());
//        System.out.println("Total planners used: " + brtdpList.size());
        System.out.println("AMDP Cleanup");
        System.out.println("room number: " + rooms);
        System.out.println("backups: " + totalBudget);


        System.out.println("Total planners used: " + brtdpList.size());
//        System.out.println("CleanUp with AMDPs \n Backups by individual planners:");
        System.out.println("CleanUp with AMDPs \n Backups by individual planners:");
        for (BoundedRTDPForTests b : brtdpList) {
            System.out.println(b.getNumberOfBellmanUpdates());
        }





    }



    public static class l2PolicyGenerator implements AMDPPolicyGenerator{

        private OOSADomain l2;
        protected double discount = 0.99;
        StateMapping sm = new CleanupL2StateMapper();

        public l2PolicyGenerator(OOSADomain l2In){
            l2 = l2In;
        }

        @Override
        public Policy generatePolicy(State s, GroundedTask gt) {
            //			BFS bfs = new BFS(l2, new TFGoalCondition(tf), new SimpleHashableStateFactory(false));
            //			bfs.toggleDebugPrinting(false);
            //			DDPlannerPolicy policy = new DDPlannerPolicy(bfs);
            //			return policy;

            l2 = ((NonPrimitiveTaskNode)gt.getT()).domain();
            l2.setModel(new FactoredModel(((FactoredModel)l2.getModel()).getStateModel(),gt.rewardFunction(), gt.terminalFunction()));


            BoundedRTDPForTests brtdp = new BoundedRTDPForTests(l2, discount, new SimpleHashableStateFactory(false),
                    new ConstantValueFunction(0.),
                    new ConstantValueFunction(1.),
                    0.01,
                    -1);
            brtdp.setRemainingNumberOfBellmanUpdates(bellmanBudgetL2);
            brtdpList.add(brtdp);

            brtdp.setMaxRolloutDepth(50);
            brtdp.toggleDebugPrinting(false);
            brtdp.planFromState(s);
            return new GreedyReplan(brtdp);

        }

        @Override
        public State generateAbstractState(State s) {
            return sm.mapState(s);
        }

        @Override
        public QProvider getQProvider(State s, GroundedTask gt) {
            l2 = ((NonPrimitiveTaskNode)gt.getT()).domain();
            l2.setModel(new FactoredModel(((FactoredModel)l2.getModel()).getStateModel(),gt.rewardFunction(), gt.terminalFunction()));

            BoundedRTDPForTests brtdp = new BoundedRTDPForTests(l2, discount, new SimpleHashableStateFactory(false),
                    new ConstantValueFunction(0.),
                    new ConstantValueFunction(1.),
                    0.01,
                    -1);
            brtdp.setRemainingNumberOfBellmanUpdates(bellmanBudgetL2);
            brtdpList.add(brtdp);
            brtdp.setMaxRolloutDepth(50);
            brtdp.toggleDebugPrinting(false);
            brtdp.planFromState(s);
            return brtdp;
        }

    }

    public static class l1PolicyGenerator implements AMDPPolicyGenerator{
        private OOSADomain l1;
        protected double discount = 0.99;

        protected StateMapping sm ;

        public l1PolicyGenerator(OOSADomain l1In){
            l1 = l1In;
            sm = new CleanupL1StateMapper();
        }
        @Override
        public Policy generatePolicy(State s, GroundedTask gt) {

            l1 = ((NonPrimitiveTaskNode)gt.getT()).domain();
            l1.setModel(new FactoredModel(((FactoredModel)l1.getModel()).getStateModel(),gt.rewardFunction(), gt.terminalFunction()));


            SimpleHashableStateFactory shf = new SimpleHashableStateFactory(false);
            BoundedRTDPForTests brtdp = new BoundedRTDPForTests(l1, discount, shf,
                    new ConstantValueFunction(0.),
                    new ConstantValueFunction(1.),
                    0.01,
                    2000);

            brtdp.setRemainingNumberOfBellmanUpdates(bellmanBudgetL1);
            brtdp.setMaxRolloutDepth(200);
            brtdp.toggleDebugPrinting(false);

            Policy p = brtdp.planFromState(s);
            brtdpList.add(brtdp);
            return new GreedyReplan(brtdp);
        }

        @Override
        public State generateAbstractState(State s) {
            return sm.mapState(s);
        }

        @Override
        public QProvider getQProvider(State s, GroundedTask gt) {

            l1 = ((NonPrimitiveTaskNode)gt.getT()).domain();
            l1.setModel(new FactoredModel(((FactoredModel)l1.getModel()).getStateModel(),gt.rewardFunction(), gt.terminalFunction()));

            SimpleHashableStateFactory shf = new SimpleHashableStateFactory(false);
            BoundedRTDPForTests brtdp = new BoundedRTDPForTests(l1, discount, shf,
                    new ConstantValueFunction(0.),
                    new ConstantValueFunction(1.),
                    0.01,
                    2000);

            brtdp.setRemainingNumberOfBellmanUpdates(bellmanBudgetL1);
            brtdp.setMaxRolloutDepth(200);
            brtdp.toggleDebugPrinting(false);

            Policy p = brtdp.planFromState(s);
            brtdpList.add(brtdp);
            return brtdp;
        }


    }


    public static class l0PolicyGenerator implements AMDPPolicyGenerator{
        private OOSADomain l0;
        protected double discount = 0.99;
        private double lockProb;
        StateMapping sm = new CleanupContinuousToDiscreteStateMapper();
        public l0PolicyGenerator(OOSADomain l0In, double lockProb){
            l0 = l0In;
            this.lockProb = lockProb;
        }

        @Override
        public Policy generatePolicy(State s, GroundedTask gt) {

            l0 = ((NonPrimitiveTaskNode)gt.getT()).domain();
            l0.setModel(new FactoredModel(((FactoredModel)l0.getModel()).getStateModel(),gt.rewardFunction(), gt.terminalFunction()));

            ValueFunction heuristic = getL0Heuristic(s, gt.rewardFunction(), lockProb);//new ConstantValueFunction(1.);//
            BoundedRTDPForTests brtd = new BoundedRTDPForTests(l0, this.discount,
                    new SimpleHashableStateFactory(false),
                    new ConstantValueFunction(0.),
                    heuristic, 0.01, -1);
            brtd.setRemainingNumberOfBellmanUpdates(bellmanBudgetL0);
            brtd.setMaxRolloutDepth(100);
            brtd.toggleDebugPrinting(false);
            brtdpList.add(brtd);
            brtd.planFromState(s);
            return new GreedyReplan(brtd);
        }

        @Override
        public State generateAbstractState(State s) {
            return sm.mapState(s);
        }

        @Override
        public QProvider getQProvider(State s, GroundedTask gt) {
            l0 = ((NonPrimitiveTaskNode)gt.getT()).domain();
            l0.setModel(new FactoredModel(((FactoredModel)l0.getModel()).getStateModel(),gt.rewardFunction(), gt.terminalFunction()));

            ValueFunction heuristic = new ConstantValueFunction(1.);//getL0Heuristic(s, gt.rewardFunction(), lockProb);
            BoundedRTDPForTests brtd = new BoundedRTDPForTests(l0, this.discount,
                    new SimpleHashableStateFactory(false),
                    new ConstantValueFunction(0.),
                    heuristic, 0.01, -1);
            brtd.setRemainingNumberOfBellmanUpdates(bellmanBudgetL0);
            brtd.setMaxRolloutDepth(100);
            brtd.toggleDebugPrinting(false);
            brtdpList.add(brtd);
            brtd.planFromState(s);
            return brtd;
        }

    }


    public static class continuousPolicyGenerator implements AMDPPolicyGenerator{
        private OOSADomain l0;
        protected double discount = 0.99;
        private double lockProb;
        private Map<String, Double> angleMap = new HashMap<String, Double>();


        public continuousPolicyGenerator(OOSADomain l0In, double lockProb){
            l0 = l0In;
            this.lockProb = lockProb;
            angleMap.put("north",0.);
            angleMap.put("south",Math.PI);
            angleMap.put("east",-Math.PI/2);
            angleMap.put("west",Math.PI/2);
        }

        @Override
        public Policy generatePolicy(State s, GroundedTask gt) {

            final GroundedTask gtTemp = gt;
            State sc = (CleanupContinuousState)s;
            Policy p = new Policy()
            {
                @Override
                public Action action(State s) {
                    // check state and gt
                    // if gt move fwd first set yourself in the correct direction
                    CleanupContinuousState sTemp = (CleanupContinuousState)s;
                    if(gtTemp.getAction().actionName().equals(CleanupTurtleBotL0Domain.ACTION_MOVE_FORWARD)){
                        // get direction from action
                        // next make sure that current direction is lined up to the current direction
//                        MutablePair<Integer,Integer> goal = ((CleanupTurtleBotL0Domain.MoveForwardType.MoveForwardAction)gtTemp.getAction()).goalLocation();
//                        String dir = ((CleanupTurtleBotL0Domain.MoveForwardType.MoveForwardAction)gtTemp.getAction()).dir;
//                        Integer x =((CleanupTurtleBotL0Domain.MoveForwardType.MoveForwardAction)gtTemp.getAction()).x;
//                        Integer y = ((CleanupTurtleBotL0Domain.MoveForwardType.MoveForwardAction)gtTemp.getAction()).y;

//                        Double xStart = x+0.5;
//                        Double yStart = y+0.5;




                        // first get to centre


//                        double signedDist = CleanupContinuousDomain.signedAngularDistance(sTemp.agent.direction,angleMap.get(dir));
//                        if(Math.abs(signedDist)< CleanupContinuousDomain.smallerRangeForDirectionChecks){
//                            if(signedDist<0.){
//                                return l0.getAction(CleanupContinuousDomain.ACTION_TURN_CCW).allApplicableActions(s).get(0);
//                            }
//                            else {
//                                return l0.getAction(CleanupContinuousDomain.ACTION_TURN_CW).allApplicableActions(s).get(0);
//                            }
//                        }

//                        Double truex = sTemp.agent.x;
//                        Double truey = sTemp.agent.y;
//
//                        Double dirToStart = Math.atan2((truey-goal.getRight()),(truex-goal.getLeft()))-Math.PI/2;
                        String dirToStart = ((CleanupTurtleBotL0Domain.MoveForwardType.MoveForwardAction)gtTemp.getAction()).dir;

                        double angDistToPoint = CleanupContinuousDomain.signedAngularDistance(sTemp.agent.direction,angleMap.get(dirToStart));
                        if(Math.abs(angDistToPoint)> CleanupContinuousDomain.smallerRangeForDirectionChecks){
                            if(angDistToPoint<0.){
                                return l0.getAction(CleanupContinuousDomain.ACTION_TURN_CCW).allApplicableActions(s).get(0);
                            }
                            else {
                                return l0.getAction(CleanupContinuousDomain.ACTION_TURN_CW).allApplicableActions(s).get(0);
                            }
                        }


                        return l0.getAction(CleanupContinuousDomain.ACTION_MOVE_FORWARD).allApplicableActions(s).get(0);
                    }
                    else if(gtTemp.getAction().actionName().equals(CleanupTurtleBotL0Domain.ACTION_MOVE_BACK)){
//                        MutablePair<Integer,Integer> goal = ((CleanupTurtleBotL0Domain.MoveForwardType.MoveForwardAction)gtTemp.getAction()).goalLocation();

                        String dir = ((CleanupTurtleBotL0Domain.MoveBackType.MoveBackAction)gtTemp.getAction()).dir;
                        double signedDist = CleanupContinuousDomain.signedAngularDistance(sTemp.agent.direction,angleMap.get(dir));
                        if(Math.abs(signedDist)< CleanupContinuousDomain.smallerRangeForDirectionChecks){
                            if(signedDist<0.){
                                return l0.getAction(CleanupContinuousDomain.ACTION_TURN_CCW).allApplicableActions(s).get(0);
                            }
                            else {
                                return l0.getAction(CleanupContinuousDomain.ACTION_TURN_CW).allApplicableActions(s).get(0);
                            }
                        }
                        return l0.getAction(CleanupContinuousDomain.ACTION_MOVE_BACK).allApplicableActions(s).get(0);
                    }
                    else if(gtTemp.getAction().actionName().equals(CleanupTurtleBotL0Domain.ACTION_TURN_CW)){
//                        MutablePair<Integer,Integer> goal = ((CleanupTurtleBotL0Domain.MoveForwardType.MoveForwardAction)gtTemp.getAction()).goalLocation();
                        return l0.getAction(CleanupContinuousDomain.ACTION_TURN_CW).allApplicableActions(s).get(0);
                    }
                    else if(gtTemp.getAction().actionName().equals(CleanupTurtleBotL0Domain.ACTION_TURN_CCW)){
//                        MutablePair<Integer,Integer> goal = ((CleanupTurtleBotL0Domain.MoveForwardType.MoveForwardAction)gtTemp.getAction()).goalLocation();
                        return l0.getAction(CleanupContinuousDomain.ACTION_TURN_CCW).allApplicableActions(s).get(0);
                    }
                    return null;
                }

                @Override
                public double actionProb(State s, Action a) {
                    Action action = this.action(s);
                    if(action.actionName().equals(a.actionName())) {
                        return 1.;
                    }

                    return 0.;
                }

                @Override
                public boolean definedFor(State s) {
                    return true;
                }
            };

            return p;

        }

        @Override
        public State generateAbstractState(State s) {
            return null;
        }

        @Override
        public QProvider getQProvider(State s, GroundedTask gt) {
            return null;
        }

    }


    public static ValueFunction getL0Heuristic(State s, RewardFunction rf, double lockProb){

        double discount = 0.99;
        // prop name if block -> block and room if
        GroundedPropSC rfCondition = (GroundedPropSC)((PullCostGoalRF)rf).getGoalCondition();
        String PFName = rfCondition.gp.pf.getName();
        String[] params = rfCondition.gp.params;
        if(PFName.equals(CleanupTurtleBotL0Domain.PF_AGENT_IN_ROOM)){
            return new amdp.cleanupamdpdomains.cleanupamdp.CleanupDriver.AgentToRegionHeuristic(params[1], discount, lockProb);
        }
        else if(PFName.equals(CleanupTurtleBotL0Domain.PF_AGENT_IN_DOOR)){
            return new amdp.cleanupamdpdomains.cleanupamdp.CleanupDriver.AgentToRegionHeuristic(params[1], discount, lockProb);
        }
        else if(PFName.equals(CleanupTurtleBotL0Domain.PF_BLOCK_IN_ROOM)){
            return new amdp.cleanupamdpdomains.cleanupamdp.CleanupDriver.BlockToRegionHeuristic(params[0], params[1], discount, lockProb);
        }
        else if(PFName.equals(CleanupTurtleBotL0Domain.PF_BLOCK_IN_DOOR)){
            return new amdp.cleanupamdpdomains.cleanupamdp.CleanupDriver.BlockToRegionHeuristic(params[0], params[1], discount, lockProb);
        }
        throw new RuntimeException("Unknown Reward Function with propositional function " + PFName + ". Cannot construct l0 heuristic.");
    }

    public static class AgentToRegionHeuristic implements ValueFunction{

        String goalRegion;
        double discount;
        double lockProb;

        public AgentToRegionHeuristic(String goalRegion, double discount, double lockProb) {
            this.goalRegion = goalRegion;
            this.discount = discount;
            this.lockProb = lockProb;
        }

        //@Override
        //public double qValue(State s, AbstractGroundedAction a) {
        //    return value(s);
        //}

        @Override
        public double value(State s) {

            int delta = 1;
            boolean freeRegion = true;
            ObjectInstance region = ((CleanupState)s).object(this.goalRegion);
            if(region.className().equals(CleanupContinuousDomain.CLASS_DOOR)){
                delta = 0;
                if(((CleanupDoor)region).canBeLocked){
                    int lockedVal = ((CleanupDoor) region).locked;
                    if(lockedVal == 2){
                        return 0.; //impossible to reach because locked
                    }
                    if(lockedVal == 0){
                        freeRegion = false; //unknown is not free
                    }
                }
            }


            //get the agent
            CleanupAgent agent = ((CleanupState)s).agent;
            int ax = agent.x;
            int ay = agent.y;


            int l = (Integer) region.get(CleanupContinuousDomain.VAR_LEFT);
            int r = (Integer)region.get(CleanupContinuousDomain.VAR_RIGHT);
            int b = (Integer)region.get(CleanupContinuousDomain.VAR_BOTTOM);
            int t = (Integer)region.get(CleanupContinuousDomain.VAR_TOP);

            int dist = toRegionManDistance(ax, ay, l, r, b, t, delta);

            double fullChanceV = Math.pow(discount, dist-1);
            double v = freeRegion ? fullChanceV : lockProb * fullChanceV + (1. - lockProb)*0;

            return v;
        }


    }



    public static class BlockToRegionHeuristic implements ValueFunction{

        String blockName;
        String goalRegion;
        double discount;
        double lockProb;

        public BlockToRegionHeuristic(String blockName, String goalRegion, double discount, double lockProb) {
            this.blockName = blockName;
            this.goalRegion = goalRegion;
            this.discount = discount;
            this.lockProb = lockProb;
        }
//
//        @Override
//        public double qValue(State s, AbstractGroundedAction a) {
//            return value(s);
//        }

        @Override
        public double value(State s) {

            int delta = 1;
            boolean freeRegion = true;
            ObjectInstance region = ((CleanupState)s).object(this.goalRegion);
            if(region.className().equals(CleanupContinuousDomain.CLASS_DOOR)){
                delta = 0;
                if(((CleanupDoor)region).canBeLocked){
                    int lockedVal = ((CleanupDoor) region).locked;
                    if(lockedVal == 2){
                        return 0.; //impossible to reach because locked
                    }
                    if(lockedVal == 0){
                        freeRegion = false; //unknown is not free
                    }
                }
            }



            //get the agent
            CleanupAgent agent = ((CleanupState)s).agent;
            int ax = (Integer) agent.get(CleanupContinuousDomain.VAR_X);
            int ay = (Integer) agent.get(CleanupContinuousDomain.VAR_Y);


            int l = (Integer) region.get(CleanupContinuousDomain.VAR_LEFT);
            int r = (Integer) region.get(CleanupContinuousDomain.VAR_RIGHT);
            int b = (Integer) region.get(CleanupContinuousDomain.VAR_BOTTOM);
            int t = (Integer) region.get(CleanupContinuousDomain.VAR_TOP);

            //get the block
            ObjectInstance block = ((CleanupState)s).object(this.blockName);
            int bx = (Integer) block.get(CleanupContinuousDomain.VAR_X);
            int by = (Integer) block.get(CleanupContinuousDomain.VAR_Y);

            int dist = manDistance(ax, ay, bx, by)-1; //need to be one step away from block to push it

            //and then block needs to be at room
            dist += toRegionManDistance(bx, by, l, r, b, t, delta);

            double fullChanceV = Math.pow(discount, dist-1);
            double v = freeRegion ? fullChanceV : lockProb * fullChanceV + (1. - lockProb)*0.;

            return v;
        }
    }


    public static int manDistance(int x0, int y0, int x1, int y1){
        return Math.abs(x0-x1) + Math.abs(y0-y1);
    }


    /**
     * Manhatten distance to a room or door.
     * @param x
     * @param y
     * @param l
     * @param r
     * @param b
     * @param t
     * @param delta set to 1 for rooms because boundaries are walls which are not sufficient to be in room; 0 for doors
     * @return
     */
    public static int toRegionManDistance(int x, int y, int l, int r, int b, int t, int delta){
        int dist = 0;

        //use +1s because boundaries define wall, which is not sufficient to be in the room
        if(x <= l){
            dist += l-x + delta;
        }
        else if(x >= r){
            dist += x - r + delta;
        }

        if(y <= b){
            dist += b - y + delta;
        }
        else if(y >= t){
            dist += y - t + delta;
        }

        return dist;
    }


    public static class L0Goal implements StateConditionTest {

        List<String> blocks;
        List<String> rooms;
        PropositionalFunction pf;


        public L0Goal(List<String> blocks, List<String> rooms, PropositionalFunction pf) {
            this.blocks = blocks;
            this.rooms = rooms;
            this.pf = pf;
        }

        @Override
        public boolean satisfies(State s) {

            for(int i = 0; i < this.blocks.size(); i++){
                String b = this.blocks.get(i);
                String r = this.rooms.get(i);
                GroundedProp gp = new GroundedProp(pf, new String[]{b, r});
                if(!gp.isTrue((OOState)s)){
                    return false;
                }
            }

            return true;
        }

    }

    public static class L2Goal implements StateConditionTest{

        List<String> blocks;
        List<String> rooms;


        public L2Goal(List<String> blocks, List<String> rooms) {
            this.blocks = blocks;
            this.rooms = rooms;
        }

        @Override
        public boolean satisfies(State s) {

            for(int i = 0; i < this.blocks.size(); i++){
                CleanupBlockL2 b = (CleanupBlockL2)((CleanupL2State)s).object(this.blocks.get(i));
                String r = this.rooms.get(i);
                if(!b.inRegion.equals(r)){
                    return false;
                }
            }

            return true;
        }
    }



}
