package amdp.cleanupamdpdomains.cleanuplevel1;

import amdp.amdpframework.GroundedPropSC;
import amdp.cleanup.CleanupDomain;
import amdp.cleanup.state.CleanupDoor;
import amdp.cleanup.state.CleanupState;
import amdp.cleanupamdpdomains.cleanuplevel1.state.CleanupL1StateMapper;
import amdp.cleanupamdpdomains.cleanuplevel1.state.*;
import amdp.taxiamdpdomains.testingtools.BoundedRTDPForTests;
import burlap.behavior.policy.Policy;
import burlap.behavior.policy.PolicyUtils;
import burlap.behavior.singleagent.Episode;
import burlap.behavior.valuefunction.ConstantValueFunction;
import burlap.debugtools.RandomFactory;
import burlap.mdp.auxiliary.DomainGenerator;
import burlap.mdp.auxiliary.common.GoalConditionTF;
import burlap.mdp.auxiliary.common.NullTermination;
import burlap.mdp.auxiliary.stateconditiontest.StateConditionTest;
import burlap.mdp.core.TerminalFunction;
import burlap.mdp.core.action.Action;
import burlap.mdp.core.action.ActionType;
import burlap.mdp.core.oo.ObjectParameterizedAction;
import burlap.mdp.core.oo.propositional.GroundedProp;
import burlap.mdp.core.oo.propositional.PropositionalFunction;
import burlap.mdp.core.oo.state.ObjectInstance;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.common.GoalBasedRF;
import burlap.mdp.singleagent.common.UniformCostRF;
import burlap.mdp.singleagent.environment.SimulatedEnvironment;
import burlap.mdp.singleagent.model.FactoredModel;
import burlap.mdp.singleagent.model.RewardFunction;
import burlap.mdp.singleagent.oo.OOSADomain;
import burlap.mdp.singleagent.oo.ObjectParameterizedActionType;
import burlap.statehashing.simple.SimpleHashableStateFactory;

import java.util.List;
import java.util.Random;

/**
 * Created by ngopalan on 8/28/16.
 */
public class CleanupL1Domain implements DomainGenerator{
    public static final String VAR_CONNECTED = "connectedObjects";
    public static final String VAR_IN_REGION = "inRegion";

    public static final String ACTION_AGENT_TO_DOOR = "agentToDoor";
    public static final String ACTION_AGENT_TO_ROOM = "agentToRoom";

    public static final String ACTION_BLOCK_TO_DOOR = "blockToDoor";
    public static final String ACTION_BLOCK_TO_ROOM = "blockToRoom";

    protected boolean lockableDoors = false;
    protected double lockProb = 0.5;

    // this is the lower level domain from which we need propositional functions
    protected OOSADomain l0;

    public CleanupL1Domain(OOSADomain l0In, RewardFunction rf, TerminalFunction tf){
        l0 = l0In;
        this.rf = rf;
        this.tf= tf;
    }

    public void setLockableDoors(boolean lockableDoors) {
        this.lockableDoors = lockableDoors;
    }

    public void setLockProb(double lockProb) {
        this.lockProb = lockProb;
    }

    private static RewardFunction rf;
    private static TerminalFunction tf;
    private static Random rand = RandomFactory.getMapped(0);


    @Override
    public OOSADomain generateDomain() {
        OOSADomain d = new OOSADomain();


        d.addStateClass(CleanupDomain.CLASS_AGENT, CleanupAgentL1.class).addStateClass(CleanupDomain.CLASS_BLOCK, CleanupBlockL1.class)
                .addStateClass(CleanupDomain.CLASS_DOOR, CleanupDoorL1.class).addStateClass(CleanupDomain.CLASS_ROOM, CleanupRoomL1.class);


        if (rf == null) {
            rf = new UniformCostRF();
        }
        if (tf == null) {
            tf = new NullTermination();
        }


        d.addActionTypes(
                new GoToRegionActionType(ACTION_AGENT_TO_ROOM, new String[]{CleanupDomain.CLASS_ROOM}, this.lockProb, l0),
                new GoToRegionActionType(ACTION_AGENT_TO_DOOR, new String[]{CleanupDomain.CLASS_DOOR}, this.lockProb, l0),
                new BlockToRegionActionType(ACTION_BLOCK_TO_ROOM, new String[]{CleanupDomain.CLASS_BLOCK,
                        CleanupDomain.CLASS_ROOM}, this.lockProb, l0),
                new BlockToRegionActionType(ACTION_BLOCK_TO_DOOR, new String[]{CleanupDomain.CLASS_BLOCK,
                        CleanupDomain.CLASS_DOOR}, this.lockProb, l0));

        CleanupL1Model smodel = new CleanupL1Model( lockProb,rand);
        FactoredModel model = new FactoredModel(smodel, rf, tf);
        d.setModel(model);


        return d;
    }


    public static class GoToRegionActionType extends ObjectParameterizedActionType{


        protected double lockedProb;
        protected OOSADomain l0;



        public GoToRegionActionType(String name, String[] parameterClasses, double lockedProb, OOSADomain l0In) {
            super(name, parameterClasses);
            l0 = l0In;
            this.lockedProb = lockedProb;
        }

        @Override
        protected boolean applicableInState(State s, ObjectParameterizedAction a) {

            //get the region where the agent currently is
            CleanupAgentL1 agent = ((CleanupL1State)s).agent;
            ObjectInstance curRegion = (ObjectInstance)((CleanupL1State)s).object(agent.inRegion);

            if(curRegion instanceof CleanupDoorL1){
                //is the param connected to this region?

                if(((CleanupDoorL1)curRegion).connectedRegions.contains(a.getObjectParameters()[0])){
                    return true;
                }

            }
            else if(curRegion instanceof CleanupRoomL1){
                //is the param connected to this region?
                if(((CleanupRoomL1)curRegion).connectedRegions.contains(a.getObjectParameters()[0])){
                    return true;
                }
            }
            return false;
        }
    }



    public static class BlockToRegionActionType extends ObjectParameterizedActionType{

        protected double lockedProb;
        protected OOSADomain l0;

        public BlockToRegionActionType(String name, String[] parameterClasses, double lockedProb, OOSADomain l0) {
            super(name, parameterClasses);
            this.lockedProb = lockedProb;
            this.l0 = l0;
        }

        @Override
        protected boolean applicableInState(State s, ObjectParameterizedAction a) {
            CleanupAgentL1 agent = ((CleanupL1State)s).agent;
            CleanupBlockL1 block = ((CleanupL1State)s).blocks.get(((CleanupL1State)s).blockInd(a.getObjectParameters()[0]));
            ObjectInstance curRegion =((CleanupL1State)s).object(agent.inRegion);
            ObjectInstance blockRegion = ((CleanupL1State)s).object(block.inRegion);

            if(!curRegion.equals(blockRegion)){
                if(blockRegion.className().equals(CleanupDomain.CLASS_ROOM)){
                    return false; //if block is a room, then agent must be in that room to move it
                }
                else if(curRegion.className().equals(CleanupDomain.CLASS_DOOR)){
                    return false; //if block and agent are in different doors, cannot move block
                }
                else if(blockRegion.className().equals(CleanupDomain.CLASS_DOOR) ){
                    //if agent is in room and block is in door, then agent can only move if door is connected
                    if(!((CleanupDoorL1)blockRegion).connectedRegions.contains(curRegion.name())) {
                        return false;
                    }
                }

            }

            //is the target region connected to the block region?
            if(blockRegion instanceof CleanupDoorL1){
                //is the param connected to this region?

                if(((CleanupDoorL1)blockRegion).connectedRegions.contains(a.getObjectParameters()[1])){
                    return true;
                }

            }
            else if(blockRegion instanceof CleanupRoomL1){
                //is the param connected to this region?
                if(((CleanupRoomL1)blockRegion).connectedRegions.contains(a.getObjectParameters()[1])){
                    return true;
                }
            }


            return false;
        }
    }


    public static class InRegionSC implements StateConditionTest {

        public String srcOb;
        public String targetOb;

        public InRegionSC(String srcOb, String targetOb) {
            this.srcOb = srcOb;
            this.targetOb = targetOb;
        }

        @Override
        public boolean satisfies(State s) {
            ObjectInstance src = ((CleanupL1State)s).object(this.srcOb);
            return src.get(VAR_IN_REGION).equals(targetOb);
        }
    }


    public static class DoorLockedSC implements StateConditionTest{

        public String door;
        public StateConditionTest otherSC;

        public DoorLockedSC(String door, StateConditionTest otherSC) {
            this.door = door;
            this.otherSC = otherSC;
        }

        @Override
        public boolean satisfies(State s) {
            if(otherSC.satisfies(s)){
                return true;
            }

            ObjectInstance doorOb =  ((CleanupState)s).object(door);
            if(doorOb instanceof CleanupDoor) {
                if (((CleanupDoor)doorOb).canBeLocked) {
                    int lockedVal = ((CleanupDoor)doorOb).locked;
                    return lockedVal == 2;
                }
            }

            return false;
        }
    }


    public static void main(String[] args) {

        double lockProb = 0.5;

        PropositionalFunction pf = new CleanupDomain.PF_InRegion(CleanupDomain.PF_BLOCK_IN_ROOM, new String[]{CleanupDomain.CLASS_BLOCK, CleanupDomain.CLASS_ROOM}, false);


        GroundedProp gp =  new GroundedProp(pf,new String[]{"block0", "room1"});

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



        State s = CleanupDomain.getClassicState(true);

        StateConditionTest sc = new InRegionSC("block0", "room1");
        RewardFunction rf = new GoalBasedRF(sc, 1.);
        TerminalFunction tf = new GoalConditionTF(sc);

        CleanupL1Domain adgen = new CleanupL1Domain(domain, rf, tf);
        adgen.setLockableDoors(true);
        adgen.setLockProb(lockProb);
        OOSADomain adomain = adgen.generateDomain();

        State as = new CleanupL1StateMapper().mapState(s);
//        System.out.println(as.toString());
//
//        List<Action> a = adomain.getAction(ACTION_BLOCK_TO_DOOR).allApplicableActions(as);
//        System.out.println("actions: " + a.size());




        if(true) {



            SimpleHashableStateFactory shf = new SimpleHashableStateFactory(false);
            BoundedRTDPForTests planner = new BoundedRTDPForTests(adomain, 0.99, shf,
                    new ConstantValueFunction(0.),
                    new ConstantValueFunction(1.),
                    0.01,
                    -1);
            planner.setMaxRolloutDepth(150);
            Policy p = planner.planFromState(as);

            SimulatedEnvironment env = new SimulatedEnvironment(adomain, as);
            Episode ea = PolicyUtils.rollout(p, env, 100);
            System.out.println(ea.actionString("\n"));

        }

    }





}
