package amdp.cleanupamdpdomains.cleanuplevel2;

import amdp.amdpframework.GroundedPropSC;
import amdp.cleanup.CleanupDomain;
import amdp.cleanupamdpdomains.cleanuplevel1.CleanupL1Domain;
import amdp.cleanupamdpdomains.cleanuplevel1.CleanupL1Model;
import amdp.cleanupamdpdomains.cleanuplevel1.state.*;
import amdp.cleanupamdpdomains.cleanuplevel2.state.*;
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
import burlap.mdp.core.Domain;
import burlap.mdp.core.TerminalFunction;
import burlap.mdp.core.oo.ObjectParameterizedAction;
import burlap.mdp.core.oo.propositional.GroundedProp;
import burlap.mdp.core.oo.propositional.PropositionalFunction;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.common.GoalBasedRF;
import burlap.mdp.singleagent.common.UniformCostRF;
import burlap.mdp.singleagent.environment.SimulatedEnvironment;
import burlap.mdp.singleagent.model.FactoredModel;
import burlap.mdp.singleagent.model.RewardFunction;
import burlap.mdp.singleagent.oo.OOSADomain;
import burlap.mdp.singleagent.oo.ObjectParameterizedActionType;
import burlap.statehashing.simple.SimpleHashableStateFactory;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

/**
 * Created by ngopalan on 8/28/16.
 */
public class CleanupL2Domain implements DomainGenerator {

    private static RewardFunction rf;
    private static TerminalFunction tf;
    private static Random rand = RandomFactory.getMapped(0);

    public CleanupL2Domain(RewardFunction rf, TerminalFunction tf) {
        this.rf = rf;
        this.tf = tf;
    }

    public CleanupL2Domain() {}

    @Override
    public OOSADomain generateDomain() {
        OOSADomain d = new OOSADomain();


        d.addStateClass(CleanupDomain.CLASS_AGENT, CleanupAgentL2.class).addStateClass(CleanupDomain.CLASS_BLOCK, CleanupBlockL2.class)
                .addStateClass(CleanupDomain.CLASS_ROOM, CleanupRoomL2.class);


        if (this.rf == null) {
            rf = new UniformCostRF();
        }
        if (tf == null) {
            tf = new NullTermination();
        }


        d.addActionTypes(
                new GoToRoomActionType(CleanupL1Domain.ACTION_AGENT_TO_ROOM,
                        new String[]{CleanupDomain.CLASS_ROOM}),
                new BlockToRoomActionType(CleanupL1Domain.ACTION_BLOCK_TO_ROOM, new String[]{CleanupDomain.CLASS_BLOCK,
                        CleanupDomain.CLASS_ROOM}));

        CleanupL2Model smodel = new CleanupL2Model();
        FactoredModel model = new FactoredModel(smodel, rf, tf);
        d.setModel(model);


        return d;
    }

    public static class GoToRoomActionType extends ObjectParameterizedActionType {

        public GoToRoomActionType(String name, String[] parameterClasses) {
            super(name, parameterClasses);
        }

        @Override
        protected boolean applicableInState(State s, ObjectParameterizedAction a) {

            String curRoom = ((CleanupL2State)s).agent.inRegion;
            if(!curRoom.equals(a.getObjectParameters()[0])){
                return true;
            }

            return false;
        }
    }


    public static class BlockToRoomActionType extends ObjectParameterizedActionType {

        public BlockToRoomActionType(String name, String[] parameterClasses) {
            super(name, parameterClasses);
        }
        @Override
        protected boolean applicableInState(State s, ObjectParameterizedAction a) {
            String curRoom = ((CleanupBlockL2)((CleanupL2State)s).object(a.getObjectParameters()[0])).inRegion;
            if(!curRoom.equals(a.getObjectParameters()[0])){
                return true;
            }
            return false;
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

        StateConditionTest sc = new CleanupL1Domain.InRegionSC("block0", "room1");
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


        List<String> goalBlocks = Arrays.asList("block0" );//"block1"
        List<String> goalRooms = Arrays.asList("room1" );//"room3"
        StateConditionTest l2sc = new L2Goal(goalBlocks, goalRooms);
        GoalBasedRF l2rf = new GoalBasedRF(l2sc, 1., 0.);
        GoalConditionTF l2tf = new GoalConditionTF(l2sc);

        CleanupL2Domain aadgen = new CleanupL2Domain(l2rf,l2tf);
        OOSADomain aadomain = aadgen.generateDomain();

        State aas = new CleanupL2StateMapper().mapState(as);



        if(true) {


            //StateReachability.getReachableStates(as, (SADomain)adomain, new SimpleHashableStateFactory(false));

            SimpleHashableStateFactory shf = new SimpleHashableStateFactory(false);
            BoundedRTDPForTests planner = new BoundedRTDPForTests(aadomain, 0.99, shf,
                    new ConstantValueFunction(0.),
                    new ConstantValueFunction(1.),
                    0.01,
                    -1);
            planner.setMaxRolloutDepth(150);
            Policy p = planner.planFromState(aas);

            SimulatedEnvironment env = new SimulatedEnvironment(aadomain, aas);
            Episode ea = PolicyUtils.rollout(p, env, 100);
            System.out.println(ea.actionString("\n"));

        }

    }


    }
