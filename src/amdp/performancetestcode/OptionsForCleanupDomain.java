package amdp.performancetestcode;

import amdp.cleanupdomain.*;
import amdp.cleanupdomain.CleanupDomainDriverWithBaseLevelComparison;
import amdp.framework.GroundedPropSC;
import amdp.taxi.TaxiDomain;
import burlap.behavior.policy.Policy;
import burlap.behavior.singleagent.EpisodeAnalysis;
import burlap.behavior.singleagent.auxiliary.EpisodeSequenceVisualizer;
import burlap.behavior.singleagent.options.DeterministicTerminationOption;
import burlap.behavior.singleagent.options.Option;
import burlap.behavior.singleagent.options.support.OptionEvaluatingRF;
import burlap.behavior.valuefunction.QValue;
import burlap.behavior.valuefunction.ValueFunctionInitialization;
import burlap.debugtools.DPrint;
import burlap.debugtools.RandomFactory;
import burlap.domain.singleagent.cleanup.CleanupWorld;
import burlap.oomdp.auxiliary.common.GoalConditionTF;
import burlap.oomdp.auxiliary.common.NullTermination;
import burlap.oomdp.auxiliary.stateconditiontest.StateConditionTest;
import burlap.oomdp.core.Domain;
import burlap.oomdp.core.GroundedProp;
import burlap.oomdp.core.PropositionalFunction;
import burlap.oomdp.core.TerminalFunction;
import burlap.oomdp.core.objects.ObjectInstance;
import burlap.oomdp.core.states.State;
import burlap.oomdp.singleagent.*;
import burlap.oomdp.singleagent.common.GoalBasedRF;
import burlap.oomdp.singleagent.common.UniformCostRF;
import burlap.oomdp.singleagent.common.VisualActionObserver;
import burlap.oomdp.singleagent.environment.EnvironmentOutcome;
import burlap.oomdp.statehashing.SimpleHashableStateFactory;
import burlap.oomdp.visualizer.Visualizer;

import java.util.*;

/**
 * Created by ngopalan on 4/21/16.
 */
public class OptionsForCleanupDomain {

    public static double discount = 0.99;
    public static MutableGlobalInteger mgi = new MutableGlobalInteger();
    static HashMap<String, BoundedRTDPForTests> plannerMap = new HashMap<>();
    static List<BoundedRTDPForTests> plannerList = new ArrayList<>();
    static int numSteps = 251;
    static double lockProb = 0.5;


    static int optionRolloutDepth = 100;

    private static Domain cleanupWorldL1;

    public static Policy testPolicy;

    public static Option goToRoomOption(String optionName, Domain domain, final int roomNo, MutableGlobalInteger bellmanUpdateIn){



        final PropositionalFunction pf = domain.getPropFunction(CleanupWorld.PF_AGENT_IN_ROOM);
        final String roomName = CleanupWorld.CLASS_ROOM + roomNo;
        final String agentName = CleanupWorld.CLASS_AGENT + 0;

        final String[] params = new String[]{agentName,roomName};
        final StateConditionTest initiationConditions= new StateConditionTest(){
            @Override
            // here the passenger is not in the t
            public boolean satisfies(State s){
                State as = CleanupL1AMDPDomain.projectToAMDPState(s, cleanupWorldL1);
                ObjectInstance agent = as.getFirstObjectOfClass(amdp.cleanupdomain.CleanupWorld.CLASS_AGENT);
                ObjectInstance curRegion = as.getObject(agent.getStringValForAttribute(CleanupL1AMDPDomain.ATT_IN_REGION));

                //is the param connected to this region?
                if(curRegion.getAllRelationalTargets(CleanupL1AMDPDomain.ATT_CONNECTED).contains(roomName)){
                    return true;
                }

                return false;
//                return !pf.isTrue(s,params) && agentInConnectedDoorRegion(roomName, s);
            }

        };

        final StateConditionTest rewardCondition = new StateConditionTest(){
            @Override
            // here the passenger is not in the t
            public boolean satisfies(State s){
                return pf.isTrue(s,params);
            }

        };

        final StateConditionTest terminationCondition = new StateConditionTest(){
            @Override
            // here the passenger is not in the t
            public boolean satisfies(State s){
                return pf.isTrue(s,params) || !initiationConditions.satisfies(s);
            }

        };



        RewardFunction rf = new PullCostGoalRF(rewardCondition);
        TerminalFunction tf = new GoalConditionTF(terminationCondition);

        ValueFunctionInitialization heuristic = new CleanupDomainDriverWithBaseLevelComparison.AgentToRegionHeuristic(roomName, discount, lockProb);

        BoundedRTDPForTests brtdp = new BoundedRTDPForTests(domain, rf, tf, discount, new SimpleHashableStateFactory(false),
                new ValueFunctionInitialization.ConstantValueFunctionInitialization(0.), heuristic,
//                new ValueFunctionInitialization.ConstantValueFunctionInitialization(1.),
                0.001,
                -1);//10
        brtdp.setRemainingNumberOfBellmanUpdates(bellmanUpdateIn);
//        plannerList.add(brtdp);

        brtdp.setMaxRolloutDepth(optionRolloutDepth);//10
        brtdp.toggleDebugPrinting(false);



        Policy optionPolicy = new GreedyReplan(brtdp, bellmanUpdateIn);
        plannerList.add(brtdp);
        plannerMap.put(optionName,brtdp);

        //now that we have the parts of our option, instantiate it
        DeterministicTerminationOption option = new DeterministicTerminationOption(optionName, optionPolicy, initiationConditions, terminationCondition);
        option.setExpectationHashingFactory(new SimpleHashableStateFactory(false));

        return option;


    }


    public static Option goToDoorOption(String optionName, Domain domain, final int doorNo, MutableGlobalInteger bellmanUpdateIn){



        final PropositionalFunction pf = domain.getPropFunction(CleanupWorld.PF_AGENT_IN_DOOR);
        final String doorName = CleanupWorld.CLASS_DOOR + doorNo;
        final String agentName = CleanupWorld.CLASS_AGENT + 0;

        final String[] params = new String[]{agentName,doorName};
        final StateConditionTest initiationConditions= new StateConditionTest(){
            @Override
            // here the passenger is not in the t
            public boolean satisfies(State s){
                State as = CleanupL1AMDPDomain.projectToAMDPState(s, cleanupWorldL1);
                ObjectInstance agent = as.getFirstObjectOfClass(amdp.cleanupdomain.CleanupWorld.CLASS_AGENT);
                ObjectInstance curRegion = as.getObject(agent.getStringValForAttribute(CleanupL1AMDPDomain.ATT_IN_REGION));

                int doorResults =s.getObject(doorName).getIntValForAttribute(CleanupWorld.ATT_LOCKED);
                if(doorResults==2){
                    return false;
                }


                //is the param connected to this region?
                if(curRegion.getAllRelationalTargets(CleanupL1AMDPDomain.ATT_CONNECTED).contains(doorName)){
                    return true;
                }

                return false;
//                return !pf.isTrue(s,params) && agentInConnectedRoomRegion(doorName, s);
            }

        };


        final StateConditionTest terminationCondition = new StateConditionTest(){
            @Override
            // here the passenger is not in the t
            public boolean satisfies(State s){
                int doorResults =s.getObject(doorName).getIntValForAttribute(CleanupWorld.ATT_LOCKED);
                return pf.isTrue(s,params) || (doorResults == 2) || !initiationConditions.satisfies(s);
            }

        };

        final StateConditionTest rewardCondition = new StateConditionTest(){
            @Override
            // here the passenger is not in the t
            public boolean satisfies(State s){
//                int doorResults =s.getObject(doorName).getIntValForAttribute(CleanupWorld.ATT_LOCKED);
                return pf.isTrue(s,params);
            }

        };






        RewardFunction rf = new PullCostGoalRF(rewardCondition);
        TerminalFunction tf = new GoalConditionTF(terminationCondition);

        ValueFunctionInitialization heuristic = new CleanupDomainDriverWithBaseLevelComparison.AgentToRegionHeuristic(doorName, discount, lockProb);
        BoundedRTDPForTests brtdp = new BoundedRTDPForTests(domain, rf, tf, discount, new SimpleHashableStateFactory(false),
                new ValueFunctionInitialization.ConstantValueFunctionInitialization(0.),heuristic,
//                new ValueFunctionInitialization.ConstantValueFunctionInitialization(1.),
                0.001,
                -1);//10
        brtdp.setRemainingNumberOfBellmanUpdates(bellmanUpdateIn);

        brtdp.setMaxRolloutDepth(optionRolloutDepth);//10
        brtdp.toggleDebugPrinting(false);



        Policy optionPolicy = new GreedyReplan(brtdp, bellmanUpdateIn);
        plannerList.add(brtdp);
        plannerMap.put(optionName,brtdp);

        //now that we have the parts of our option, instantiate it
        DeterministicTerminationOption option = new DeterministicTerminationOption(optionName, optionPolicy, initiationConditions, terminationCondition);
        option.setExpectationHashingFactory(new SimpleHashableStateFactory(false));

        return option;


    }


    public static Option blockToDoorOption(String optionName, Domain domain, final int blockNo,final int doorNo, MutableGlobalInteger bellmanUpdateIn){



        final PropositionalFunction pf = domain.getPropFunction(CleanupWorld.PF_BLOCK_IN_DOOR);
        final String doorName = CleanupWorld.CLASS_DOOR + doorNo;
        final String blockName = CleanupWorld.CLASS_BLOCK + blockNo;

        final String[] params = new String[]{blockName,doorName};
        final StateConditionTest initiationConditions= new StateConditionTest(){
            @Override
            // here the passenger is not in the t
            public boolean satisfies(State s){
                State as = CleanupL1AMDPDomain.projectToAMDPState(s, cleanupWorldL1);
                ObjectInstance agent = as.getFirstObjectOfClass(amdp.cleanupdomain.CleanupWorld.CLASS_AGENT);
                ObjectInstance block = as.getObject(blockName);
                ObjectInstance curRegion = as.getObject(agent.getStringValForAttribute(CleanupL1AMDPDomain.ATT_IN_REGION));
                ObjectInstance blockRegion = as.getObject(block.getStringValForAttribute(CleanupL1AMDPDomain.ATT_IN_REGION));

                int doorResults =s.getObject(doorName).getIntValForAttribute(CleanupWorld.ATT_LOCKED);
                if(doorResults==2){
                    return false;
                }

                if(curRegion != blockRegion){
                    if(blockRegion.getClassName().equals(amdp.cleanupdomain.CleanupWorld.CLASS_ROOM)){
                        return false; //if block is a room, then agent must be in that room to move it
                    }
                    else if(curRegion.getClassName().equals(amdp.cleanupdomain.CleanupWorld.CLASS_DOOR)){
                        return false; //if block and agent are in different doors, cannot move block
                    }
                    else if(!curRegion.getAllRelationalTargets(CleanupL1AMDPDomain.ATT_CONNECTED).contains(blockRegion.getName())){
                        //if agent is in room and block is in door, then agent can only move if door is connected
                        return false;
                    }

                }

                //is the target region connected to the block region?
                if(blockRegion.getAllRelationalTargets(CleanupL1AMDPDomain.ATT_CONNECTED).contains(doorName)){
                    return true;
                }

                return false;

//                return !pf.isTrue(s,params)&& blockInConnectedRoomRegion(doorName, blockName, s);
            }

        };

        final StateConditionTest rewardCondition = new StateConditionTest(){
            @Override
            // here the passenger is not in the t
            public boolean satisfies(State s){

                return pf.isTrue(s,params);
            }

        };

        final StateConditionTest terminalCondition = new StateConditionTest(){
            @Override
            // here the passenger is not in the t
            public boolean satisfies(State s){
                int doorResults =s.getObject(doorName).getIntValForAttribute(CleanupWorld.ATT_LOCKED);
                boolean inInitiationSet = initiationConditions.satisfies(s);
                return pf.isTrue(s,params) || (doorResults == 2) || !inInitiationSet;
            }

        };



        RewardFunction rf = new PullCostGoalRF(rewardCondition);
        TerminalFunction tf = new GoalConditionTF(terminalCondition);

        ValueFunctionInitialization heuristic = new CleanupDomainDriverWithBaseLevelComparison.BlockToRegionHeuristic(blockName,doorName, discount, lockProb);

        BoundedRTDPForTests brtdp = new BoundedRTDPForTests(domain, rf, tf, discount, new SimpleHashableStateFactory(false),
                new ValueFunctionInitialization.ConstantValueFunctionInitialization(0.),heuristic,
//                new ValueFunctionInitialization.ConstantValueFunctionInitialization(1.),
                0.001,
                -1);//10
        brtdp.setRemainingNumberOfBellmanUpdates(bellmanUpdateIn);

        brtdp.setMaxRolloutDepth(optionRolloutDepth);//10
        brtdp.toggleDebugPrinting(false);




        Policy optionPolicy = new GreedyReplan(brtdp, bellmanUpdateIn);
        testPolicy = optionPolicy;
        plannerList.add(brtdp);
        plannerMap.put(optionName,brtdp);

        //now that we have the parts of our option, instantiate it
        DeterministicTerminationOption option = new DeterministicTerminationOption(optionName, optionPolicy, initiationConditions, terminalCondition);
        option.setExpectationHashingFactory(new SimpleHashableStateFactory(false));

        return option;


    }

    public static Option blockToRoomOption(String optionName, Domain domain, final int blockNo,final int roomNo, MutableGlobalInteger bellmanUpdateIn){



        final PropositionalFunction pf = domain.getPropFunction(CleanupWorld.PF_BLOCK_IN_ROOM);
        final String roomName = CleanupWorld.CLASS_ROOM + roomNo;
        final String blockName = CleanupWorld.CLASS_BLOCK + blockNo;

        final String[] params = new String[]{blockName,roomName};
        final StateConditionTest initiationConditions= new StateConditionTest(){
            @Override
            public boolean satisfies(State s){
                State as = CleanupL1AMDPDomain.projectToAMDPState(s, cleanupWorldL1);
                ObjectInstance agent = as.getFirstObjectOfClass(amdp.cleanupdomain.CleanupWorld.CLASS_AGENT);
                ObjectInstance block = as.getObject(blockName);
                ObjectInstance curRegion = as.getObject(agent.getStringValForAttribute(CleanupL1AMDPDomain.ATT_IN_REGION));
                ObjectInstance blockRegion = as.getObject(block.getStringValForAttribute(CleanupL1AMDPDomain.ATT_IN_REGION));

                if(curRegion != blockRegion){
                    if(blockRegion.getClassName().equals(amdp.cleanupdomain.CleanupWorld.CLASS_ROOM)){
                        return false; //if block is a room, then agent must be in that room to move it
                    }
                    else if(curRegion.getClassName().equals(amdp.cleanupdomain.CleanupWorld.CLASS_DOOR)){
                        return false; //if block and agent are in different doors, cannot move block
                    }
                    else if(!curRegion.getAllRelationalTargets(CleanupL1AMDPDomain.ATT_CONNECTED).contains(blockRegion.getName())){
                        //if agent is in room and block is in door, then agent can only move if door is connected
                        return false;
                    }

                }

                //is the target region connected to the block region?
                if(blockRegion.getAllRelationalTargets(CleanupL1AMDPDomain.ATT_CONNECTED).contains(roomName)){
                    return true;
                }

                return false;
//                return !pf.isTrue(s,params) && blockInConnectedDoorRegion(roomName, blockName, s);
            }

        };

        final StateConditionTest rewardCondition = new StateConditionTest(){
            @Override
            public boolean satisfies(State s){
//                boolean inInitiationSet = initiationConditions.satisfies(s);
                return pf.isTrue(s,params);
            }

        };

        final StateConditionTest terminalCondition = new StateConditionTest(){
            @Override
            public boolean satisfies(State s){
                boolean inInitiationSet = initiationConditions.satisfies(s);
                return pf.isTrue(s,params) || !inInitiationSet;
            }

        };


        RewardFunction rf = new PullCostGoalRF(rewardCondition);
        TerminalFunction tf = new GoalConditionTF(terminalCondition);


        ValueFunctionInitialization heuristic = new CleanupDomainDriverWithBaseLevelComparison.BlockToRegionHeuristic(blockName,roomName, discount, lockProb);

        BoundedRTDPForTests brtdp = new BoundedRTDPForTests(domain, rf, tf, discount, new SimpleHashableStateFactory(false),
                new ValueFunctionInitialization.ConstantValueFunctionInitialization(0.),heuristic,
//                new ValueFunctionInitialization.ConstantValueFunctionInitialization(1.),
                0.001,
                -1);//10
        brtdp.setRemainingNumberOfBellmanUpdates(bellmanUpdateIn);
//        plannerList.add(brtdp);

        brtdp.setMaxRolloutDepth(optionRolloutDepth);//10
        brtdp.toggleDebugPrinting(false);



        Policy optionPolicy = new GreedyReplan(brtdp, bellmanUpdateIn);
//        testPolicy = optionPolicy;
        plannerList.add(brtdp);
        plannerMap.put(optionName,brtdp);

        //now that we have the parts of our option, instantiate it
        DeterministicTerminationOption option = new DeterministicTerminationOption(optionName, optionPolicy, initiationConditions, terminalCondition);
        option.setExpectationHashingFactory(new SimpleHashableStateFactory(false));

        return option;


    }

    public static State getState(Domain domain){


        int y1 = 3;
        int y2 = 7;
        int y3 = 12;

        int x1 = 4;
        int x2 = 8;
        int x3 = 12;

        State s = CleanupWorld.getCleanState(domain, 4, 4, 3);
        CleanupWorld.setRoom(s, 0, y2, x1, 0, x2, "red");
        CleanupWorld.setRoom(s, 1, y2, 0, y1, x1, "blue");
        //		CleanupWorld.setRoom(s, 1, y2, 0, 0, x1, "blue");
        CleanupWorld.setRoom(s, 2, y3, 0, y2, x3, "green");
        CleanupWorld.setRoom(s, 3, y2, x2, 0, x3, "yellow");

        CleanupWorld.setDoor(s, 0, 1, x2, 1, x2);
        CleanupWorld.setDoor(s, 1, 5, x1, 5, x1);

        CleanupWorld.setDoor(s, 2, y2, 2, y2, 2);
        CleanupWorld.setDoor(s, 3, y2, 10, y2, 10);

        CleanupWorld.setAgent(s, 7, 1);

        CleanupWorld.setBlock(s, 0, 5, 4, "chair", "blue");
        CleanupWorld.setBlock(s, 1, 6, 10, "basket", "red");
        CleanupWorld.setBlock(s, 2, 2, 10, "bag", "magenta");

        return s;

    }




    /**
     * call when trying to get to a door
     * @param doorToTravelTo
     * @param s
     * @return
     */
    public static boolean agentInConnectedRoomRegion(String doorToTravelTo, State s){

        ObjectInstance door = s.getObject(doorToTravelTo);
        ObjectInstance agent = s.getFirstObjectOfClass(amdp.cleanupdomain.CleanupWorld.CLASS_AGENT);
        int ax = agent.getIntValForAttribute(amdp.cleanupdomain.CleanupWorld.ATT_X);
        int ay = agent.getIntValForAttribute(amdp.cleanupdomain.CleanupWorld.ATT_Y);
        ObjectInstance inRoom = amdp.cleanupdomain.CleanupWorld.roomContainingPoint(s, ax, ay);

        if(inRoom != null){
            // agent is in a room and need to get to a door
            int rt = inRoom.getIntValForAttribute(amdp.cleanupdomain.CleanupWorld.ATT_TOP);
            int rl = inRoom.getIntValForAttribute(amdp.cleanupdomain.CleanupWorld.ATT_LEFT);
            int rb = inRoom.getIntValForAttribute(amdp.cleanupdomain.CleanupWorld.ATT_BOTTOM);
            int rr = inRoom.getIntValForAttribute(amdp.cleanupdomain.CleanupWorld.ATT_RIGHT);


            int dt = door.getIntValForAttribute(amdp.cleanupdomain.CleanupWorld.ATT_TOP);
            int dl = door.getIntValForAttribute(amdp.cleanupdomain.CleanupWorld.ATT_LEFT);
            int db = door.getIntValForAttribute(amdp.cleanupdomain.CleanupWorld.ATT_BOTTOM);
            int dr = door.getIntValForAttribute(amdp.cleanupdomain.CleanupWorld.ATT_RIGHT);

            return rectanglesIntersect(rt, rl, rb, rr, dt, dl, db, dr);
        }
        else{
            // agent is in a door and needs to get to a door which is unconnected!
            return false;
        }

    }

    /**
     * call when trying to get to a room
     * @param roomToTravelTo
     * @param s
     * @return
     */
    public static boolean agentInConnectedDoorRegion(String roomToTravelTo, State s){

        // get agent's room -> if room to room then deny?
        ObjectInstance room = s.getObject(roomToTravelTo);
        ObjectInstance agent = s.getFirstObjectOfClass(amdp.cleanupdomain.CleanupWorld.CLASS_AGENT);
        int ax = agent.getIntValForAttribute(amdp.cleanupdomain.CleanupWorld.ATT_X);
        int ay = agent.getIntValForAttribute(amdp.cleanupdomain.CleanupWorld.ATT_Y);
        ObjectInstance inRoom = amdp.cleanupdomain.CleanupWorld.roomContainingPoint(s, ax, ay);

        if(inRoom != null){
            // agent is in a room and needs to get to a room which is unconnected!
            return false;
        }
        else{
            ObjectInstance inDoor = amdp.cleanupdomain.CleanupWorld.doorContainingPoint(s, ax, ay);
            int rt = room.getIntValForAttribute(amdp.cleanupdomain.CleanupWorld.ATT_TOP);
            int rl = room.getIntValForAttribute(amdp.cleanupdomain.CleanupWorld.ATT_LEFT);
            int rb = room.getIntValForAttribute(amdp.cleanupdomain.CleanupWorld.ATT_BOTTOM);
            int rr = room.getIntValForAttribute(amdp.cleanupdomain.CleanupWorld.ATT_RIGHT);


            int dt = inDoor.getIntValForAttribute(amdp.cleanupdomain.CleanupWorld.ATT_TOP);
            int dl = inDoor.getIntValForAttribute(amdp.cleanupdomain.CleanupWorld.ATT_LEFT);
            int db = inDoor.getIntValForAttribute(amdp.cleanupdomain.CleanupWorld.ATT_BOTTOM);
            int dr = inDoor.getIntValForAttribute(amdp.cleanupdomain.CleanupWorld.ATT_RIGHT);

            return rectanglesIntersect(rt, rl, rb, rr, dt, dl, db, dr);
        }

    }

    public static boolean blockInConnectedDoorRegion(String roomToTravelTo, String blockName, State s){
        // get agent's room -> if room to room then deny?
        ObjectInstance room = s.getObject(roomToTravelTo);
        ObjectInstance block = s.getObject(blockName);
        int ax = block.getIntValForAttribute(amdp.cleanupdomain.CleanupWorld.ATT_X);
        int ay = block.getIntValForAttribute(amdp.cleanupdomain.CleanupWorld.ATT_Y);
        ObjectInstance inRoom = amdp.cleanupdomain.CleanupWorld.roomContainingPoint(s, ax, ay);

        if(inRoom != null){
            // agent is in a room and needs to get to a room which is unconnected!
            return false;
        }
        else{
            ObjectInstance inDoor = amdp.cleanupdomain.CleanupWorld.doorContainingPoint(s, ax, ay);
            int rt = room.getIntValForAttribute(amdp.cleanupdomain.CleanupWorld.ATT_TOP);
            int rl = room.getIntValForAttribute(amdp.cleanupdomain.CleanupWorld.ATT_LEFT);
            int rb = room.getIntValForAttribute(amdp.cleanupdomain.CleanupWorld.ATT_BOTTOM);
            int rr = room.getIntValForAttribute(amdp.cleanupdomain.CleanupWorld.ATT_RIGHT);


            int dt = inDoor.getIntValForAttribute(amdp.cleanupdomain.CleanupWorld.ATT_TOP);
            int dl = inDoor.getIntValForAttribute(amdp.cleanupdomain.CleanupWorld.ATT_LEFT);
            int db = inDoor.getIntValForAttribute(amdp.cleanupdomain.CleanupWorld.ATT_BOTTOM);
            int dr = inDoor.getIntValForAttribute(amdp.cleanupdomain.CleanupWorld.ATT_RIGHT);

            return rectanglesIntersect(rt, rl, rb, rr, dt, dl, db, dr);
        }
    }


    public static boolean blockInConnectedRoomRegion(String doorToTravelTo, String blockName, State s){
        ObjectInstance door = s.getObject(doorToTravelTo);
        ObjectInstance agent = s.getObject(blockName);
//        System.out.println("block name: " + blockName);
//        System.out.println("block state: " + agent.toString());
        int ax = agent.getIntValForAttribute(amdp.cleanupdomain.CleanupWorld.ATT_X);
        int ay = agent.getIntValForAttribute(amdp.cleanupdomain.CleanupWorld.ATT_Y);
        ObjectInstance inRoom = amdp.cleanupdomain.CleanupWorld.roomContainingPoint(s, ax, ay);

        if(inRoom != null){
            // agent is in a room and need to get to a door
            int rt = inRoom.getIntValForAttribute(amdp.cleanupdomain.CleanupWorld.ATT_TOP);
            int rl = inRoom.getIntValForAttribute(amdp.cleanupdomain.CleanupWorld.ATT_LEFT);
            int rb = inRoom.getIntValForAttribute(amdp.cleanupdomain.CleanupWorld.ATT_BOTTOM);
            int rr = inRoom.getIntValForAttribute(amdp.cleanupdomain.CleanupWorld.ATT_RIGHT);


            int dt = door.getIntValForAttribute(amdp.cleanupdomain.CleanupWorld.ATT_TOP);
            int dl = door.getIntValForAttribute(amdp.cleanupdomain.CleanupWorld.ATT_LEFT);
            int db = door.getIntValForAttribute(amdp.cleanupdomain.CleanupWorld.ATT_BOTTOM);
            int dr = door.getIntValForAttribute(amdp.cleanupdomain.CleanupWorld.ATT_RIGHT);

            return rectanglesIntersect(rt, rl, rb, rr, dt, dl, db, dr);
        }
        else{
            // agent is in a door and needs to get to a door which is unconnected!
            return false;
        }
    }


    protected static boolean rectanglesIntersect(int t1, int l1, int b1, int r1, int t2, int l2, int b2, int r2){

        return t2 >= b1 && b2 <= t1 && r2 >= l1 && l2 <= r1;

    }


    public static void main(String[] args) {

        //32 options alone and we need to branch them when specifying the MGI

//        MutableGlobalInteger mgi = new MutableGlobalInteger();

//        RandomFactory.seedMapped(0,123334);//12345567


        DPrint.toggleCode(3214986, false);

        int totalBellmanUpdates = 40000;

        int bellmanUpdatesAllocated = 0;


        boolean onlyOptions = false;


        for(int i =0;i<args.length;i++) {
            String str = args[i];

            if (str.equals("-r")) {
                onlyOptions = Boolean.parseBoolean(args[i + 1]);
            }
            if (str.equals("-b")) {
                totalBellmanUpdates  = (Integer.parseInt(args[i + 1]));
            }
        }

        lockProb = 0.5;




        CleanupWorld dgen = new CleanupWorld();
        dgen.includeDirectionAttribute(true);
        dgen.includePullAction(true);
        dgen.includeWallPF_s(true);
        dgen.includeLockableDoors(true);
        dgen.setLockProbability(lockProb);
        final Domain domain = dgen.generateDomain();

        final GroundedPropSC l0sc = new GroundedPropSC(new GroundedProp(domain.getPropFunction(CleanupWorld.PF_BLOCK_IN_ROOM), new String[]{"block0", "room3"}));// room3

        TerminalFunction l0tf = new TerminalFunction() {
            @Override
            public boolean isTerminal(State state) {
                ObjectInstance agent = state.getFirstObjectOfClass(CleanupWorld.CLASS_AGENT);
                ObjectInstance r = CleanupWorld.roomContainingPoint(state,agent.getIntValForAttribute(CleanupWorld.ATT_X),agent.getIntValForAttribute(CleanupWorld.ATT_Y));
                List<ObjectInstance> doors = state.getObjectsOfClass(CleanupWorld.CLASS_DOOR);
                if(r!=null) {
                    boolean connectedDoorOpen = false;
                    int rt = r.getIntValForAttribute(amdp.cleanupdomain.CleanupWorld.ATT_TOP);
                    int rl = r.getIntValForAttribute(amdp.cleanupdomain.CleanupWorld.ATT_LEFT);
                    int rb = r.getIntValForAttribute(amdp.cleanupdomain.CleanupWorld.ATT_BOTTOM);
                    int rr = r.getIntValForAttribute(amdp.cleanupdomain.CleanupWorld.ATT_RIGHT);


                    for (ObjectInstance d : doors) {

                        int dt = d.getIntValForAttribute(amdp.cleanupdomain.CleanupWorld.ATT_TOP);
                        int dl = d.getIntValForAttribute(amdp.cleanupdomain.CleanupWorld.ATT_LEFT);
                        int db = d.getIntValForAttribute(amdp.cleanupdomain.CleanupWorld.ATT_BOTTOM);
                        int dr = d.getIntValForAttribute(amdp.cleanupdomain.CleanupWorld.ATT_RIGHT);

                        if (rectanglesIntersect(rt, rl, rb, rr, dt, dl, db, dr)) {
                            if(d.getIntValForAttribute(CleanupWorld.ATT_LOCKED)!=2){
                                connectedDoorOpen = true;
                                break;
                            }

                        }


                    }
                    if(!connectedDoorOpen){
                        return true;
                    }
                }

//                StateConditionTest sc = new GroundedPropSC(new GroundedProp(domain.getPropFunction(CleanupWorld.PF_BLOCK_IN_ROOM), new String[]{"block0", "room1"}));
                return l0sc.satisfies(state);
            }
        };


        GoalBasedRF l0rf = new GoalBasedRF(l0sc, 1., 0.);

        CleanupL1AMDPDomain  L1DGen = new CleanupL1AMDPDomain(domain);
        L1DGen.setLockableDoors(true);
        L1DGen.setLockProb(lockProb);
        cleanupWorldL1 = L1DGen.generateDomain();



//        int door = 1;
//        int block = 0;

        State s = getState(domain);
//        State s = CleanupWorld.getClassicState(domain);
//
//        System.out.println("is terminal: " + l0tf.isTerminal(s));

        FixedDoorCleanupEnv env = new FixedDoorCleanupEnv(domain, l0rf, l0tf, s);
//        System.out.println("env: " + env.isInTerminalState());

        env.addLockedDoor("door0");

//        boolean test = false;
//
        List<ObjectInstance> rooms = s.getObjectsOfClass(CleanupWorld.CLASS_ROOM);
        List<ObjectInstance> doors = s.getObjectsOfClass(CleanupWorld.CLASS_DOOR);
        List<ObjectInstance> blocks = s.getObjectsOfClass(CleanupWorld.CLASS_BLOCK);


//        String name = "goToDoor" + 1;
//        Double temp = (totalBellmanUpdates * 0.025);
        if(false) {
            Option d1 = goToDoorOption("goToDoor" + 1, domain, 1, new MutableGlobalInteger(100));
            d1.keepTrackOfRewardWith(l0rf, discount);

            Option r1 = goToRoomOption("goToRoom" + 1, domain, 1, new MutableGlobalInteger(100));
            r1.keepTrackOfRewardWith(l0rf, discount);


            Option d2 = goToDoorOption("goToDoor" + 2, domain, 2, new MutableGlobalInteger(100));
            d2.keepTrackOfRewardWith(l0rf, discount);

            Option r2 = goToRoomOption("goToRoom" + 2, domain, 2, new MutableGlobalInteger(100));
            r2.keepTrackOfRewardWith(l0rf, discount);


            Option d3 = goToDoorOption("goToDoor" + 3, domain, 3, new MutableGlobalInteger(100));
            d3.keepTrackOfRewardWith(l0rf, discount);

            Option r3 = goToRoomOption("goToRoom" + 3, domain, 3, new MutableGlobalInteger(100));
            r3.keepTrackOfRewardWith(l0rf, discount);


//
//            Visualizer vTest = CleanupVisualizer.getVisualizer("amdp/data/resources/robotImages");
////        new EpisodeSequenceVisualizer(vTest, domain, Arrays.asList(eaTest));
//        VisualActionObserver vao = new VisualActionObserver(domain, vTest);
//        vao.initGUI();
//        ((SADomain)domain).addActionObserverForAllAction(vao);
//        ((SADomain)domain).addActionObserverForAllAction(new ActionObserver() {
//            @Override
//            public void actionEvent(State state, GroundedAction groundedAction, State state1) {
//                System.out.println(groundedAction.toString());
//            }
//        });

////

            EpisodeAnalysis eaTest = new EpisodeAnalysis(s);
////
            d1.getAssociatedGroundedAction().getTransitions(env.getCurrentObservation());
            EnvironmentOutcome eo = d1.performInEnvironment(env, d1.getAssociatedGroundedAction());
            r1.getAssociatedGroundedAction().getTransitions(env.getCurrentObservation());
            EnvironmentOutcome eo2 = r1.performInEnvironment(env, r1.getAssociatedGroundedAction());


            d2.getAssociatedGroundedAction().getTransitions(env.getCurrentObservation());
            EnvironmentOutcome eo3 = d2.performInEnvironment(env, d2.getAssociatedGroundedAction());
            r2.getAssociatedGroundedAction().getTransitions(env.getCurrentObservation());
            EnvironmentOutcome eo4 = r2.performInEnvironment(env, r2.getAssociatedGroundedAction());


            d3.getAssociatedGroundedAction().getTransitions(env.getCurrentObservation());
            EnvironmentOutcome eo5 = d3.performInEnvironment(env, d3.getAssociatedGroundedAction());

            r3.getAssociatedGroundedAction().getTransitions(env.getCurrentObservation());
            EnvironmentOutcome eo6 = r3.performInEnvironment(env, r3.getAssociatedGroundedAction());


            eaTest.recordTransitionTo(eo.a, eo.op, eo.r);
            eaTest.recordTransitionTo(eo2.a, eo2.op, eo2.r);
            eaTest.recordTransitionTo(eo3.a, eo3.op, eo3.r);
            eaTest.recordTransitionTo(eo4.a, eo4.op, eo4.r);
            eaTest.recordTransitionTo(eo5.a, eo5.op, eo5.r);
            eaTest.recordTransitionTo(eo6.a, eo6.op, eo6.r);
//        eaTest.recordTransitionTo(a.getGroundedAction(),nextS,0);
//        State nextS1 = a.performAction(nextS,a1.getAssociatedGroundedAction());
//
//        eaTest.recordTransitionTo(a1.getGroundedAction(),nextS1,0);
//

//        eaTest.recordTransitionTo(a.getGroundedAction(),nextS,0);

//            new EpisodeSequenceVisualizer(vTest, domain, Arrays.asList(eaTest));


        }


        if(true) {

            // agent to doors
            List<Option> navigateOptionList = new ArrayList<>();
            for (int i = 0; i < doors.size(); i++) {
                String name = "goToDoor" + i;
                Double temp = (totalBellmanUpdates * 0.025);
//                Double temp = -1.;
                bellmanUpdatesAllocated += temp.intValue();
                navigateOptionList.add(goToDoorOption(name, domain, i, new MutableGlobalInteger(temp.intValue())));

            }

            for (int i = 0; i < rooms.size(); i++) {
                String name = "goToRoom" + i;
                Double temp = (totalBellmanUpdates * 0.025);
//                Double temp = -1.;
                bellmanUpdatesAllocated += temp.intValue();
                navigateOptionList.add(goToRoomOption(name, domain, i, new MutableGlobalInteger(temp.intValue())));

            }

            for (int i = 0; i < doors.size(); i++) {
                for (int j = 0; j < blocks.size(); j++) {
                    String name = "block" + j + "ToDoor" + i;
                    Double temp = (totalBellmanUpdates * 0.025);
//                    Double temp = -1.;
                    bellmanUpdatesAllocated += temp.intValue();
                    navigateOptionList.add(blockToDoorOption(name, domain, j, i, new MutableGlobalInteger(temp.intValue())));
                }
            }

            for (int i = 0; i < rooms.size(); i++) {
                for (int j = 0; j < blocks.size(); j++) {
                    String name = "block" + j + "ToRoom" + i;
                    Double temp = (totalBellmanUpdates * 0.025);
//                    Double temp = -1.;
                    bellmanUpdatesAllocated += temp.intValue();
                    navigateOptionList.add(blockToRoomOption(name, domain, j, i, new MutableGlobalInteger(temp.intValue())));
                }
            }
//        System.out.println(navigateOptionList.size());
//        Option o = blockToRoomOption("navigateRoom0",domain,block,door,mgi);
//        if(!test) {
////
//            State ns = o.performAction(s, o.getAssociatedGroundedAction());
//            EpisodeAnalysis ea = new EpisodeAnalysis();
//            ea.addState(s);
//            ea.addAction(o.getAssociatedGroundedAction());
//            ea.addState(ns);
//            Visualizer v = CleanupVisualizer.getVisualizer("amdp/data/resources/robotImages");
//            new EpisodeSequenceVisualizer(v, domain, Arrays.asList(ea));
//
//        }
//            StateConditionTest sc = new GroundedPropSC(new GroundedProp(domain.getPropFunction(CleanupWorld.PF_BLOCK_IN_ROOM), new String[]{"block0", "room3"}));
//
            RewardFunction heuristicRF = new PullCostGoalRF(l0sc, 1., 0.);
//
//
//


            long startTime = System.currentTimeMillis();


            BoundedRTDPForTests brtd;
            Policy p;
            EpisodeAnalysis ea;
            String optionStr = "";

            if (!onlyOptions) {
                ValueFunctionInitialization heuristic = CleanupDomainDriver.getL0Heuristic(s, heuristicRF);
                brtd = new BoundedRTDPForTests(domain, l0rf, l0tf, 0.99, new SimpleHashableStateFactory(false), new ValueFunctionInitialization.ConstantValueFunctionInitialization(0.), heuristic, 0.01, 2000);
                brtd.setDebugCode(39502748);
                DPrint.toggleCode(39502748, true);
                brtd.setMaxRolloutDepth(53);
//                System.out.println("updates remaining when calling global: " + (totalBellmanUpdates - bellmanUpdatesAllocated));
                brtd.setRemainingNumberOfBellmanUpdates(new MutableGlobalInteger(totalBellmanUpdates - bellmanUpdatesAllocated));
//                brtd.setRemainingNumberOfBellmanUpdates(new MutableGlobalInteger(-1));
                brtd.toggleDebugPrinting(false);
                plannerList.add(brtd);
                for (Option o : navigateOptionList) {
                    brtd.addNonDomainReferencedAction(o);
                }

                optionStr = "options with base actions";

            } else {
                ValueFunctionInitialization heuristic = CleanupDomainDriver.getL0Heuristic(s, heuristicRF);
                for (Option o : navigateOptionList) {
                    o.keepTrackOfRewardWith(l0rf, discount);
                    o.setExernalTermination(l0tf);
                }

                brtd = new BoundedRTDPForTests(domain, new OptionEvaluatingRF(l0rf), l0tf, discount,
                        new SimpleHashableStateFactory(false),
                        new ValueFunctionInitialization.ConstantValueFunctionInitialization(0.),heuristic,
//                        new ValueFunctionInitialization.ConstantValueFunctionInitialization(1.),
                        0.001,
                        -1);
                brtd.setMaxRolloutDepth(200);
                brtd.setRemainingNumberOfBellmanUpdates(new MutableGlobalInteger(totalBellmanUpdates - bellmanUpdatesAllocated));

//                brtd.setRemainingNumberOfBellmanUpdates(new MutableGlobalInteger(-1));
                brtd.toggleDebugPrinting(false);
                plannerList.add(brtd);

                List<Action> actionList = new ArrayList<Action>(navigateOptionList);
//            actionList.add(taxiDomain.getAction(TaxiDomain.PICKUPACTION));
//            actionList.add(taxiDomain.getAction(TaxiDomain.DROPOFFACTION));
                brtd.setActions(actionList);
                optionStr = "options with base actions";

            }

            p = brtd.planFromState(s);
            ea = p.evaluateBehavior(env, numSteps + 1);

            long endTime = System.currentTimeMillis();
            long duration = (endTime - startTime);
//
//        if(test) {
//            final PropositionalFunction pf = domain.getPropFunction(CleanupWorld.PF_BLOCK_IN_DOOR);
//            final String doorName = CleanupWorld.CLASS_DOOR + door;
//            final String blockName = CleanupWorld.CLASS_BLOCK + block;
//
//            final String[] params = new String[]{blockName,doorName};
//            final StateConditionTest goalCondition = new StateConditionTest(){
//                @Override
//                // here the passenger is not in the t
//                public boolean satisfies(State s){
//                    return pf.isTrue(s,params);
//                }
//
//            };
//
//
//
//            RewardFunction rf = new GoalBasedRF(goalCondition);
//            TerminalFunction tf = new GoalConditionTF(goalCondition);
//
//
//
//            FixedDoorCleanupEnv env = new FixedDoorCleanupEnv(domain, rf, tf, s);
//            EpisodeAnalysis ea = testPolicy.evaluateBehavior(env, numSteps + 1);
//            Visualizer v = CleanupVisualizer.getVisualizer("amdp/data/resources/robotImages");
//            new EpisodeSequenceVisualizer(v, domain, Arrays.asList(ea));
//        }
//            for(int i=0;i<ea.stateSequence.size()-1;i++){
//                State tempS = ea.getState(i);
//                String tempChar = "\\(";
//                String startStr = ea.actionSequence.get(i).toString();
//                String actionName1 = startStr.split("\\*")[1];
//                String actionName =  actionName1.split(tempChar)[0];
//                BoundedRTDPForTests bTemp = plannerMap.get(actionName);
//
//                List<QValue> qValuesOuter = brtd.getQs(tempS);
//                String tempStr1 = "outer value- ";
//                for (QValue q:qValuesOuter) {
//                    tempStr1+=q.a.actionName() +": " + q.q + "; ";
//                }
//                System.out.println(tempStr1);
//
//
//                List<QValue> qValues = bTemp.getQs(tempS);
//                String tempStr = actionName + "- ";
//                for (QValue q:qValues) {
//                    tempStr+=q.a.actionName() +": " + q.q + "; ";
//                }
//                System.out.println(tempStr);
//            }
//
            //num actions
            System.out.println(ea.stateSequence.size() - 1);
            // num bellman updates
//            System.out.println("numUpdates: " + brtd.getNumberOfBellmanUpdates());
            // num steps

            //total time

            int sumBellman = 0;
            for(String optionName : plannerMap.keySet()){
//            for (BoundedRTDPForTests b : plannerList) {
                BoundedRTDPForTests b = plannerMap.get(optionName);
//                System.out.println(optionName + ": " +b.getNumberOfBellmanUpdates());
                sumBellman += b.getNumberOfBellmanUpdates();
            }
//            System.out.println("outer bellman updates used: " + brtd.getNumberOfBellmanUpdates());
            sumBellman+=brtd.getNumberOfBellmanUpdates();
            System.out.println(sumBellman);

            if (ea.stateSequence.size() >= numSteps) {
                System.out.println(0);
            } else {
                System.out.println(1);
            }

            System.out.println(duration);
            System.out.println(optionStr);

            for(String optionName : plannerMap.keySet()){
//            for (BoundedRTDPForTests b : plannerList) {
                BoundedRTDPForTests b = plannerMap.get(optionName);
                System.out.println(optionName + ": " +b.getNumberOfBellmanUpdates());
//                sumBellman += b.getNumberOfBellmanUpdates();
            }
        }




    }
}
