package amdp.taxiamdpdomains.taxiamdp;

import amdp.amdpframework.AMDPAgent;
import amdp.amdpframework.AMDPPolicyGenerator;
import amdp.amdpframework.GroundedTask;
import amdp.amdpframework.TaskNode;
import amdp.taxi.TaxiDomain;
import amdp.taxi.TaxiRewardFunction;
import amdp.taxi.TaxiTerminationFunction;
import amdp.taxiamdpdomains.taxiamdplevel1.TaxiL1Domain;
import amdp.taxiamdpdomains.taxiamdplevel1.TaxiL1TerminalFunction;
import amdp.taxiamdpdomains.taxiamdplevel1.taxil1state.L1StateMapper;
import amdp.taxiamdpdomains.taxiamdplevel2.TaxiL2Domain;
import amdp.taxiamdpdomains.taxiamdplevel2.TaxiL2TerminalFunction;
import amdp.taxiamdpdomains.taxiamdplevel2.taxil2state.L2StateMapper;
import burlap.behavior.policy.Policy;
import burlap.behavior.singleagent.planning.stochastic.rtdp.BoundedRTDP;
import burlap.behavior.singleagent.planning.stochastic.valueiteration.ValueIteration;
import burlap.behavior.valuefunction.ConstantValueFunction;
import burlap.debugtools.RandomFactory;
import burlap.mdp.auxiliary.StateMapping;
import burlap.mdp.core.Domain;
import burlap.mdp.core.TerminalFunction;
import burlap.mdp.core.action.ActionType;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.SADomain;
import burlap.mdp.singleagent.common.UniformCostRF;
import burlap.mdp.singleagent.environment.SimulatedEnvironment;
import burlap.mdp.singleagent.model.FactoredModel;
import burlap.mdp.singleagent.model.RewardFunction;
import burlap.mdp.singleagent.oo.OOSADomain;
import burlap.statehashing.simple.SimpleHashableStateFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by ngopalan on 8/14/16.
 */
public class TaxiAMDPDriver {

    public static List<BoundedRTDP> brtdpList= new ArrayList<BoundedRTDP>();

    static int l0Budget = 50;
    static int l1Budget = 5;
    static int l2Budget = 5;

    static int l0Depth = 15;
    static int l1Depth = 5;
    static int l2Depth = 15;


    public static void main(String[] args) {

        RandomFactory randomFactory = new RandomFactory();
        Random rand = randomFactory.getMapped(0);



        TerminalFunction tf = new TaxiTerminationFunction();
        RewardFunction rf = new TaxiRewardFunction(1,tf);

        TaxiDomain tdGen = new TaxiDomain(rf,tf);



        tdGen.setTransitionDynamicsLikeFickleTaxiProlem();
        tdGen.setFickleTaxi(true);
        tdGen.setIncludeFuel(false);


        OOSADomain td = tdGen.generateDomain();

        OOSADomain tdEnv = tdGen.generateDomain();


        State startState = TaxiDomain.getRandomClassicState(rand, td, false);

        TerminalFunction tfL1 = new TaxiL1TerminalFunction();
        RewardFunction rfL1 = new UniformCostRF();

        TaxiL1Domain tdL1Gen = new TaxiL1Domain(rfL1, tfL1, rand);

        OOSADomain tdL1 = tdL1Gen.generateDomain();


        L1StateMapper testMapper = new L1StateMapper();

        State sL1 = testMapper.mapState(startState);

        L2StateMapper l2StateMapper = new L2StateMapper();

        State sL2 = l2StateMapper.mapState(sL1);


        TerminalFunction tfL2 = new TaxiL2TerminalFunction();
        RewardFunction rfL2 = new UniformCostRF();

        TaxiL2Domain taxiL2DomainGen = new TaxiL2Domain(rfL2, tfL2,rand);


        OOSADomain tdL2 = taxiL2DomainGen.generateDomain();


        ActionType east = td.getAction(TaxiDomain.ACTION_EAST);
        ActionType west = td.getAction(TaxiDomain.ACTION_WEST);
        ActionType south = td.getAction(TaxiDomain.ACTION_SOUTH);
        ActionType north = td.getAction(TaxiDomain.ACTION_NORTH);
        ActionType pickup = td.getAction(TaxiDomain.ACTION_PICKUP);
        ActionType dropoff = td.getAction(TaxiDomain.ACTION_DROPOFF);


        ActionType navigate = tdL1.getAction(TaxiL1Domain.ACTION_NAVIGATE);
        ActionType pickupL1 = tdL1.getAction(TaxiL1Domain.ACTION_PICKUPL1);
        ActionType putdownL1 = tdL1.getAction(TaxiL1Domain.ACTION_PUTDOWNL1);

        ActionType get = tdL2.getAction(TaxiL2Domain.ACTION_GET);
        ActionType put = tdL2.getAction(TaxiL2Domain.ACTION_PUT);

        TaskNode et = new MoveTaskNodes(east);
        TaskNode wt = new MoveTaskNodes(west);
        TaskNode st = new MoveTaskNodes(south);
        TaskNode nt = new MoveTaskNodes(north);
        TaskNode pt = new PickupL0TaskNode(pickup);
        TaskNode dt = new PutDownL0TaskNode(dropoff);


        TaskNode[] navigateSubTasks = new TaskNode[]{et,wt,st,nt};
        TaskNode[] dropOffL1SubTasks = new TaskNode[]{dt};
        TaskNode[] pickupL1SubTasks = new TaskNode[]{pt};

        TaskNode navigateTaskNode = new NavigateTaskNode(navigate,tdL1,td,navigateSubTasks);
        TaskNode putDownL1TaskNode = new PutDownL1TaskNode(putdownL1,tdL1,td,dropOffL1SubTasks);
        TaskNode pickupL1TaskNode = new PickupL1TaskNode(pickupL1,tdL1,td,pickupL1SubTasks);

        TaskNode[] getSubTasks = new TaskNode[]{navigateTaskNode, pickupL1TaskNode};
        TaskNode[] putSubTasks = new TaskNode[]{navigateTaskNode, putDownL1TaskNode};

        TaskNode getTaskNode = new GetTaskNode(get, tdL2, tdL1, getSubTasks);
        TaskNode putTaskNode = new PutTaskNode(put, tdL2, tdL1, putSubTasks);


        TaskNode[] rootSubTasks = new TaskNode[]{getTaskNode,putTaskNode};

        TaskNode root = new RootTaskNode("root",rootSubTasks,tdL2, tfL2,rfL2);

        List<AMDPPolicyGenerator> pgList = new ArrayList<AMDPPolicyGenerator>();
        pgList.add(0,new l0PolicyGenerator(td));
        pgList.add(1,new l1PolicyGenerator(tdL1));
        pgList.add(2,new l2PolicyGenerator(tdL2));

        AMDPAgent agent = new AMDPAgent(root.getApplicableGroundedTasks(sL2).get(0),pgList);

        SimulatedEnvironment envN = new SimulatedEnvironment(tdEnv, startState);

        agent.actUntilTermination(envN,100);





    }

    public static class l2PolicyGenerator implements AMDPPolicyGenerator {

        private OOSADomain l2;
        double gamma = 0.99;
        private StateMapping sm = new L2StateMapper();
        public l2PolicyGenerator(OOSADomain l2In){
            l2 = l2In;
        }


        @Override
        public Policy generatePolicy(State s, GroundedTask groundedTask) {
            l2.setModel(new FactoredModel(((FactoredModel)l2.getModel()).getStateModel(),groundedTask.rewardFunction(), groundedTask.terminalFunction()));
            BoundedRTDP brtdp = new BoundedRTDP(l2, gamma,  new SimpleHashableStateFactory(false),new ConstantValueFunction(0.),
                    new ConstantValueFunction(1.), 0.01, 100);
            Policy p = brtdp.planFromState(s);
            brtdpList.add(brtdp);

            brtdp.setMaxRolloutDepth(l2Depth);//5
            brtdp.toggleDebugPrinting(false);

            return p;
        }

        @Override
        public State generateAbstractState(State s) {
            return sm.mapState(s);
        }
    }


    public static class l1PolicyGenerator implements AMDPPolicyGenerator{

        private OOSADomain l1;
        protected final double discount = 0.99;
        StateMapping sm = new L1StateMapper();

        public l1PolicyGenerator(OOSADomain l2In){
            l1 = l2In;
        }

        @Override
        public Policy generatePolicy(State s, GroundedTask gt) {

            l1.setModel(new FactoredModel(((FactoredModel)l1.getModel()).getStateModel(),gt.rewardFunction(), gt.terminalFunction()));


            BoundedRTDP brtdp = new BoundedRTDP(l1, discount, new SimpleHashableStateFactory(false),
                    new ConstantValueFunction(0.),   new ConstantValueFunction(1.),
                    0.001,
                    l1Budget);//10
            brtdpList.add(brtdp);

            brtdp.setMaxRolloutDepth(l1Depth);//10
            brtdp.toggleDebugPrinting(false);
            return brtdp.planFromState(s);

        }

        @Override
        public State generateAbstractState(State s) {
            return sm.mapState(s);
        }

    }

    public static class l0PolicyGenerator implements AMDPPolicyGenerator{

        private OOSADomain l0;
        private final double discount = 0.99;
        public l0PolicyGenerator(OOSADomain l0In){
            l0 = l0In;
        }

        @Override
        public Policy generatePolicy(State s, GroundedTask gt) {

            l0.setModel(new FactoredModel(((FactoredModel)l0.getModel()).getStateModel(),gt.rewardFunction(), gt.terminalFunction()));

            BoundedRTDP brtdp = new BoundedRTDP(l0, discount, new SimpleHashableStateFactory(false),
                    new ConstantValueFunction(0.),
                    new ConstantValueFunction(1.),
                    0.001,
                    l0Budget);//50

            brtdpList.add(brtdp);

            brtdp.setMaxRolloutDepth(l0Depth);//15
            brtdp.toggleDebugPrinting(false);
            return brtdp.planFromState(s);


        }

        @Override
        public State generateAbstractState(State s) {
            return null;
        }


    }

}
