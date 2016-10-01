package amdp.cleanupamdpdomains.cleanupamdp;

import amdp.amdpframework.GroundedPropSC;
import amdp.cleanup.CleanupDomain;
import amdp.cleanup.CleanupVisualiser;
import amdp.cleanup.FixedDoorCleanupEnv;
import amdp.cleanup.PullCostGoalRF;
import amdp.cleanup.state.CleanupAgent;
import amdp.cleanup.state.CleanupBlock;
import amdp.cleanup.state.CleanupDoor;
import amdp.cleanup.state.CleanupState;
import amdp.taxiamdpdomains.testingtools.BoundedRTDPForTests;
import amdp.taxiamdpdomains.testingtools.GreedyReplan;
import amdp.taxiamdpdomains.testingtools.MutableGlobalInteger;
import burlap.behavior.policy.Policy;
import burlap.behavior.policy.PolicyUtils;
import burlap.behavior.singleagent.Episode;
import burlap.behavior.singleagent.auxiliary.EpisodeSequenceVisualizer;
import burlap.behavior.singleagent.planning.stochastic.rtdp.BoundedRTDP;
import burlap.behavior.valuefunction.ConstantValueFunction;
import burlap.behavior.valuefunction.ValueFunction;
import burlap.debugtools.RandomFactory;
import burlap.mdp.auxiliary.common.GoalConditionTF;
import burlap.mdp.core.oo.propositional.GroundedProp;
import burlap.mdp.core.oo.propositional.PropositionalFunction;
import burlap.mdp.core.oo.state.ObjectInstance;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.common.GoalBasedRF;
import burlap.mdp.singleagent.model.RewardFunction;
import burlap.mdp.singleagent.oo.OOSADomain;
import burlap.statehashing.simple.SimpleHashableStateFactory;
import burlap.visualizer.Visualizer;

import java.util.Arrays;
import java.util.Random;

/**
 * Created by ngopalan on 8/30/16.
 */
public class CleanupBaseLevelTest {


    static protected MutableGlobalInteger bellmanBudget = new MutableGlobalInteger(-1);

    public static void main(String[] args) {
        Random rand = RandomFactory.getMapped(0);
        double lockProb = 0.5;

        Integer rooms = 1;
        Integer numberOfObjects = 3;

        int numEnvSteps = 255;

        bellmanBudget.setValue(2560000*4);

        for(int i =0;i<args.length;i++){
            String str = args[i];
//            System.out.println(str);
            if(str.equals("-r")){
                rooms = Integer.parseInt(args[i+1]);
            }
            if(str.equals("-b")){
                bellmanBudget.setValue(Integer.parseInt(args[i+1]));
            }
            if(str.equals("-o")){
                numberOfObjects = Integer.parseInt(args[i+1]);
            }
        }



//            StateConditionTest sc = new GroundedPropSC(new GroundedProp(domain.getPropFunction(CleanupWorld.PF_BLOCK_IN_ROOM),  new String[]{"block0", "room1"}));

//            RewardFunction heuristicRF = new PullCostGoalRF(sc, 1., 0.);

        PropositionalFunction pf = new CleanupDomain.PF_InRegion(CleanupDomain.PF_BLOCK_IN_ROOM, new String[]{CleanupDomain.CLASS_BLOCK, CleanupDomain.CLASS_ROOM}, false);


        State s;

        GroundedProp gp;

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

        gp =  new GroundedProp(pf,new String[]{"block0", goalRoom});


        GroundedPropSC l0sc = new GroundedPropSC(gp);
        RewardFunction heuristicRF = new PullCostGoalRF(l0sc, 1., 0.);
        GoalBasedRF l0rf = new GoalBasedRF(l0sc, 1., 0.);
        GoalConditionTF l0tf = new GoalConditionTF(l0sc);

        CleanupDomain dgen = new CleanupDomain(l0rf,l0tf, rand);
        dgen.includeDirectionAttribute(true);
        dgen.includePullAction(true);
        dgen.includeWallPF_s(true);
        dgen.includeLockableDoors(true);
        dgen.setLockProbability(lockProb);
        OOSADomain domain = dgen.generateDomain();




        FixedDoorCleanupEnv env = new FixedDoorCleanupEnv(domain, s);
        if(rooms==3) {
            env.addLockedDoor("door0");
        }

        int numRollouts = -1;
        int numSteps = 150;
        long startTime = System.currentTimeMillis();

        ValueFunction heuristic = getL0Heuristic(s, heuristicRF, lockProb);
        BoundedRTDPForTests brtd = new BoundedRTDPForTests(domain, 0.99, new SimpleHashableStateFactory(false), new ConstantValueFunction(0.0), heuristic, 0.01, numRollouts);
        brtd.setMaxRolloutDepth(numSteps);
        brtd.toggleDebugPrinting(false);
        brtd.setRemainingNumberOfBellmanUpdates(bellmanBudget);

        Policy P = brtd.planFromState(s);

        Policy p = new GreedyReplan(brtd);


        long endTime = System.currentTimeMillis();
        long duration = (endTime - startTime);
//        System.out.println("total time: " + duration);
        Episode ea = PolicyUtils.rollout(p,env,numEnvSteps);

//        System.out.println(brtd.getNumberOfBellmanUpdates());

        System.out.println(ea.actionSequence.size());
        System.out.println(ea.discountedReturn(1.));
        System.out.println( brtd.getNumberOfBellmanUpdates());
//        System.out.println("Total planners used: " + brtdpList.size());
        System.out.println("Base level Cleanup");
        System.out.println("room number: "+rooms);

//        Visualizer v = CleanupVisualiser.getVisualizer("amdp/data/resources/robotImages");
//        //		System.out.println(ea.getState(0).toString());
//        new EpisodeSequenceVisualizer(v, domain, Arrays.asList(ea));


    }


    public static ValueFunction getL0Heuristic(State s, RewardFunction rf, double lockProb){

        double discount = 0.99;
        // prop name if block -> block and room if
        GroundedPropSC rfCondition = (GroundedPropSC)((PullCostGoalRF)rf).getGoalCondition();
        String PFName = rfCondition.gp.pf.getName();
        String[] params = rfCondition.gp.params;
        if(PFName.equals(CleanupDomain.PF_AGENT_IN_ROOM)){
            return new AgentToRegionHeuristic(params[1], discount, lockProb);
        }
        else if(PFName.equals(CleanupDomain.PF_AGENT_IN_DOOR)){
            return new AgentToRegionHeuristic(params[1], discount, lockProb);
        }
        else if(PFName.equals(CleanupDomain.PF_BLOCK_IN_ROOM)){
            return new BlockToRegionHeuristic(params[0], params[1], discount, lockProb);
        }
        else if(PFName.equals(CleanupDomain.PF_BLOCK_IN_DOOR)){
            return new BlockToRegionHeuristic(params[0], params[1], discount, lockProb);
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
            if(region.className().equals(CleanupDomain.CLASS_DOOR)){
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


            int l = (Integer) region.get(CleanupDomain.VAR_LEFT);
            int r = (Integer)region.get(CleanupDomain.VAR_RIGHT);
            int b = (Integer)region.get(CleanupDomain.VAR_BOTTOM);
            int t = (Integer)region.get(CleanupDomain.VAR_TOP);

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
            if(region.className().equals(CleanupDomain.CLASS_DOOR)){
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
            int ax = (Integer) agent.get(CleanupDomain.VAR_X);
            int ay = (Integer) agent.get(CleanupDomain.VAR_Y);


            int l = (Integer) region.get(CleanupDomain.VAR_LEFT);
            int r = (Integer) region.get(CleanupDomain.VAR_RIGHT);
            int b = (Integer) region.get(CleanupDomain.VAR_BOTTOM);
            int t = (Integer) region.get(CleanupDomain.VAR_TOP);

            //get the block
            ObjectInstance block = ((CleanupState)s).object(this.blockName);
            int bx = (Integer) block.get(CleanupDomain.VAR_X);
            int by = (Integer) block.get(CleanupDomain.VAR_Y);

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
}
