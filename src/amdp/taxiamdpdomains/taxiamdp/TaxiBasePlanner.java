package amdp.taxiamdpdomains.taxiamdp;

import amdp.amdpframework.AMDPAgent;
import amdp.amdpframework.AMDPPolicyGenerator;
import amdp.amdpframework.GroundedTask;
import amdp.amdpframework.TaskNode;
import amdp.taxi.TaxiDomain;
import amdp.taxi.TaxiRewardFunction;
import amdp.taxi.TaxiTerminationFunction;
import amdp.taxi.TaxiVisualizer;
import amdp.taxiamdpdomains.taxiamdplevel1.TaxiL1Domain;
import amdp.taxiamdpdomains.taxiamdplevel1.TaxiL1TerminalFunction;
import amdp.taxiamdpdomains.taxiamdplevel1.taxil1state.L1StateMapper;
import amdp.taxiamdpdomains.taxiamdplevel2.TaxiL2Domain;
import amdp.taxiamdpdomains.taxiamdplevel2.TaxiL2TerminalFunction;
import amdp.taxiamdpdomains.taxiamdplevel2.taxil2state.L2StateMapper;
import amdp.taxiamdpdomains.testingtools.BoundedRTDPForTests;
import amdp.taxiamdpdomains.testingtools.MutableGlobalInteger;
import burlap.behavior.policy.Policy;
import burlap.behavior.policy.PolicyUtils;
import burlap.behavior.singleagent.Episode;
import burlap.behavior.singleagent.auxiliary.EpisodeSequenceVisualizer;
import burlap.behavior.singleagent.planning.stochastic.rtdp.BoundedRTDP;
import burlap.behavior.singleagent.planning.stochastic.valueiteration.ValueIteration;
import burlap.behavior.valuefunction.ConstantValueFunction;
import burlap.debugtools.DPrint;
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
import burlap.mdp.stochasticgames.agent.AgentFactory;
import burlap.statehashing.simple.SimpleHashableStateFactory;
import burlap.visualizer.Visualizer;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by ngopalan on 8/14/16.
 */
public class TaxiBasePlanner {

    public static List<BoundedRTDPForTests> brtdpList= new ArrayList<BoundedRTDPForTests>();
//    DPrint.(3214986, false);


    static protected MutableGlobalInteger bellmanBudget = new MutableGlobalInteger(-1);


    static int  maxTrajectoryLength = 102;


    public static void main(String[] args) {

        DPrint.toggleCode(3214986, false);
        RandomFactory randomFactory = new RandomFactory();
        Random rand = randomFactory.getMapped(0);

        boolean randomStart = false;
        boolean singlePassenger = true;

        bellmanBudget.setValue(4000);

        for(int i =0;i<args.length;i++){
            String str = args[i];
//            System.out.println(str);
            if(str.equals("-r")){
                randomStart = Boolean.parseBoolean(args[i+1]);
            }
            if(str.equals("-b")){
                bellmanBudget.setValue(Integer.parseInt(args[i+1]));
            }
            if(str.equals("-s")){
                singlePassenger = Boolean.parseBoolean(args[i+1]);
            }
        }

        TerminalFunction tf = new TaxiTerminationFunction();
        RewardFunction rf = new TaxiRewardFunction(1,tf);

        TaxiDomain tdGen = new TaxiDomain(rf,tf);



        tdGen.setTransitionDynamicsLikeFickleTaxiProlem();
        tdGen.setFickleTaxi(true);
        tdGen.setIncludeFuel(false);


        OOSADomain td = tdGen.generateDomain();

        OOSADomain tdEnv = tdGen.generateDomain();


        State startState;

        if(randomStart){
            if(singlePassenger){
                startState = TaxiDomain.getRandomClassicState(rand, td, false);
            }
            else{
                startState = TaxiDomain.getComplexState(false);
            }

        }
        else{
            if(singlePassenger) {
                startState = TaxiDomain.getClassicState(td, false);
            }
            else{
                startState = TaxiDomain.getComplexState(false);
            }
        }


//        startState = TaxiDomain.getComplexState(false);

        BoundedRTDPForTests brtdp = new BoundedRTDPForTests(td, 0.99,  new SimpleHashableStateFactory(false),new ConstantValueFunction(0.),
                new ConstantValueFunction(1.), 0.01, 500);
        brtdp.setRemainingNumberOfBellmanUpdates(bellmanBudget);


        brtdp.setMaxRolloutDepth(25);//5
        brtdp.toggleDebugPrinting(false);
        Policy p = brtdp.planFromState(startState);
        brtdpList.add(brtdp);



        SimulatedEnvironment envN = new SimulatedEnvironment(tdEnv, startState);

        Episode e = PolicyUtils.rollout(p, startState, td.getModel(),maxTrajectoryLength);

//        Visualizer v = TaxiVisualizer.getVisualizer(5, 5);
//        List<Episode> eaList = new ArrayList<Episode>();
//        eaList.add(e);
//        new EpisodeSequenceVisualizer(v, td, eaList);




//        System.out.println("actions taken: " + e.actionSequence.size());
//        System.out.println("rewards: " + e.discountedReturn(1.));
//        System.out.println("Total updates used: " + count);
        System.out.println(e.actionSequence.size());
        System.out.println(e.discountedReturn(1.));
        System.out.println( brtdp.getNumberOfBellmanUpdates());
//        System.out.println("Total planners used: " + brtdpList.size());
        System.out.println("Base level Taxi");
//        for(BoundedRTDPForTests b:brtdpList){
//            System.out.println(b.getNumberOfBellmanUpdates());
//        }
        System.out.println("random start state: " + randomStart);
        System.out.println("single start state: " + singlePassenger);






    }



}
