package amdp.maxq.taximaxq;

import amdp.maxq.framework.*;
import amdp.taxi.TaxiDomain;
import amdp.taxi.TaxiRewardFunction;
import amdp.taxi.TaxiTerminationFunction;
import amdp.taxi.TaxiVisualizer;
import amdp.taxi.state.TaxiLocation;
import amdp.taxi.state.TaxiPassenger;
import amdp.taxi.state.TaxiState;
import amdp.utilities.BoltzmannQPolicyWithCoolingSchedule;
import amdp.utilities.QLearningForMaxQ;
import burlap.behavior.policy.GreedyQPolicy;
import burlap.behavior.policy.PolicyUtils;
import burlap.behavior.singleagent.Episode;
import burlap.behavior.singleagent.auxiliary.EpisodeSequenceVisualizer;
import burlap.behavior.singleagent.learning.tdmethods.QLearning;
import burlap.debugtools.DPrint;
import burlap.debugtools.RandomFactory;
import burlap.mdp.core.TerminalFunction;
import burlap.mdp.core.action.ActionType;
import burlap.mdp.core.oo.state.ObjectInstance;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.environment.SimulatedEnvironment;
import burlap.mdp.singleagent.model.RewardFunction;
import burlap.mdp.singleagent.oo.OOSADomain;
import burlap.statehashing.simple.SimpleHashableStateFactory;
import burlap.visualizer.Visualizer;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.ui.ApplicationFrame;


import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Runner for the taxi max Q runner
 * Created by ngopalan on 5/24/16.
 */
public class TaxiMaxQTest {


    public static void main(String[] args) {

        int debugCode = 12344356;
        RandomFactory randomFactory = new RandomFactory();
        Random rand = randomFactory.getMapped(0);
        DPrint.toggleCode(debugCode,true);


        boolean randomStart = false;
        boolean singlePassenger = true;
        int bellmanUpdateBudget = 256000;

        for(int i =0;i<args.length;i++){
            String str = args[i];
//            System.out.println(str);
            if(str.equals("-r")){
                randomStart = Boolean.parseBoolean(args[i+1]);
            }
            if(str.equals("-b")){
                bellmanUpdateBudget = Integer.parseInt(args[i+1]);
            }
            if(str.equals("-s")){
                singlePassenger = Boolean.parseBoolean(args[i+1]);
            }
        }



//        State state = TaxiDomain.getComplexState(d);
        TerminalFunction taxiTF = new TaxiTerminationFunction();
        RewardFunction taxiRF = new TaxiRewardFunction(1,taxiTF);

        TaxiDomain TDGen = new TaxiDomain(taxiRF, taxiTF);

//        tdGen.setDeterministicTransitionDynamics();
        TDGen.setTransitionDynamicsLikeFickleTaxiProlem();
//        tdGen.setDeterministicTransitionDynamics();
        TDGen.setFickleTaxi(true);
        TDGen.setIncludeFuel(false);
//        TDGen.includeFuel = false;
        final OOSADomain d = TDGen.generateDomain();

        State s;
        if(singlePassenger){
            //sNew = TaxiDomain.getRandomClassicState(rand, d, false);
            s = TaxiDomain.getClassicState(d, false);
        }
        else{
            s = TaxiDomain.getComplexState(false);
        }


        List<ObjectInstance> passengers = ((TaxiState)s).objectsOfClass(TaxiDomain.PASSENGERCLASS);
        List<String[]> passengersList = new ArrayList<String[]>();
        for(ObjectInstance p : passengers){
            passengersList.add(new String[]{((TaxiPassenger)p).name()});
        }

        List<ObjectInstance> locations = ((TaxiState)s).objectsOfClass(TaxiDomain.LOCATIONCLASS);
        List<String[]> locationsList = new ArrayList<String[]>();
        for(ObjectInstance l : locations){
            locationsList.add(new String[]{((TaxiLocation)l).name()});
        }

        ActionType east = d.getAction(TaxiDomain.ACTION_EAST);
        ActionType west = d.getAction(TaxiDomain.ACTION_WEST);
        ActionType south = d.getAction(TaxiDomain.ACTION_SOUTH);
        ActionType north = d.getAction(TaxiDomain.ACTION_NORTH);
        ActionType pickup = d.getAction(TaxiDomain.ACTION_PICKUP);
        ActionType dropoff = d.getAction(TaxiDomain.ACTION_DROPOFF);

        TaskNode et = new TaxiMAXQL0CardinalMoveTaskNode(east);
        TaskNode wt = new TaxiMAXQL0CardinalMoveTaskNode(west);
        TaskNode st = new TaxiMAXQL0CardinalMoveTaskNode(south);
        TaskNode nt = new TaxiMAXQL0CardinalMoveTaskNode(north);
        TaskNode[] navigateSubTasks = new TaskNode[]{et,wt,st,nt};

        TaskNode stTest = new TaxiMAXQL0CardinalMoveTaskNode(south);





        TaskNode pt = new PickupTaskNode(pickup);
        TaskNode dt = new DropTaskNode(dropoff);




        TaskNode navigate = new NavigateTaskNode("navigate",locationsList,navigateSubTasks);
        TaskNode[] getNodeSubTasks = new TaskNode[]{pt,navigate};
        TaskNode[] putNodeSubTasks = new TaskNode[]{dt,navigate};


        TaskNode getNode = new GetTaskNode("get",passengersList,getNodeSubTasks);

        TaskNode putNode = new PutTaskNode("put",passengersList,putNodeSubTasks);

        TaskNode[] rootNodeSubTasks = new TaskNode[]{getNode, putNode};




        final TaskNode rootNode = new RootTaskNode("root", rootNodeSubTasks,taxiTF);


        String str = "-------- MAXQ Test! ----------";
        DPrint.cl(debugCode,str);
//
//            State state = TaxiDomain.getClassicState(d, false);

        int numberOfTests = 1;
        int numberOfLearningEpisodes = 100;
        int takeModOf = 10;
        int startTest = 200;





        List<Episode> episodesMAXQ = new ArrayList<Episode>();
        List<Episode> testEpisodesMAXQ = new ArrayList<Episode>();
//                MAXQLearningAgent maxqLearningAgent = new MAXQLearningAgent(rootNode, new SimpleHashableStateFactory(), 1.0, 1.0);
//                MAXQStateAbstractionAgent maxqLearningAgent = new MAXQStateAbstractionAgent(rootNode, new SimpleHashableStateFactory(), 1.0, 0.25);
//        MaxQForTesting maxqLearningAgent = new MaxQForTesting(rootNode, new SimpleHashableStateFactory(), 1.0, 0.25, bellmanUpdateBudget);
        MAXQCleanupTesting maxqLearningAgent = new MAXQCleanupTesting(rootNode, new SimpleHashableStateFactory(), 1.0, 0.25, bellmanUpdateBudget);
//                MAX0LearningAgent maxqLearningAgent = new MAX0LearningAgent(rootNode, new SimpleHashableStateFactory(), 0.95, 0.5);
//                MAX0FasterLearningAgent maxqLearningAgent = new MAX0FasterLearningAgent(rootNode, new SimpleHashableStateFactory(), 0.95, 0.70);
//                maxqLearningAgent.setRmax(0.123 * (1 - 0.95));
        DPrint.toggleCode(maxqLearningAgent.debugCode,false);
        maxqLearningAgent.setVmax(0.123);
        maxqLearningAgent.setQProviderForTaskNode(rootNode);
        maxqLearningAgent.setQProviderForTaskNode(getNode);
        maxqLearningAgent.setQProviderForTaskNode(putNode);
        maxqLearningAgent.setQProviderForTaskNode(navigate);

        maxqLearningAgent.setSolverDerivedPolicyForTaskNode(rootNode, new BoltzmannQPolicyWithCoolingSchedule(50, 0.9996));
        maxqLearningAgent.setSolverDerivedPolicyForTaskNode(getNode, new BoltzmannQPolicyWithCoolingSchedule(50, 0.9939));
        maxqLearningAgent.setSolverDerivedPolicyForTaskNode(putNode, new BoltzmannQPolicyWithCoolingSchedule(50, 0.9996));
        maxqLearningAgent.setSolverDerivedPolicyForTaskNode(navigate, new BoltzmannQPolicyWithCoolingSchedule(50, 0.9879));
//            maxqLearningAgent.setQProviderForTaskNode(rootNode);


        int episodeNum = 0;
        while(maxqLearningAgent.getNumberOfBackups()<bellmanUpdateBudget) {

            str = "MAXQ learning episode: " + episodeNum;
            DPrint.cl(debugCode,str);
            str = "-------------------------------------------------------------";
            DPrint.cl(debugCode,str);
            State sNew;// = TaxiDomain.getRandomClassicState(rand, d, false);

                if(singlePassenger){
                    //sNew = TaxiDomain.getRandomClassicState(rand, d, false);
                    sNew = TaxiDomain.getClassicState(d, false);
                }
                else{
                    sNew = TaxiDomain.getComplexState(false);
                }


//            State sNew = TaxiDomain.getComplexState(false);
            SimulatedEnvironment envN = new SimulatedEnvironment(d, sNew);

            Episode ea = maxqLearningAgent.runLearningEpisode(envN, 5000);
            episodesMAXQ.add(ea);

            episodeNum++;


        }


        maxqLearningAgent.setFreezeLearning(true);
        str = "-------------------------------------------------------------";
        DPrint.cl(debugCode,str);
        State sNew1;
        if(randomStart){
            if(singlePassenger){
                sNew1 = TaxiDomain.getRandomClassicState(rand, d, false);
            }
            else{
                sNew1 = TaxiDomain.getComplexState(false);
            }

        }
        else{
            if(singlePassenger) {
                sNew1 = TaxiDomain.getClassicState(d, false);
            }
            else{
                sNew1 = TaxiDomain.getComplexState(false);
            }
        }

        SimulatedEnvironment envN1 = new SimulatedEnvironment(d, sNew1);
        Episode ea1 = maxqLearningAgent.runLearningEpisode(envN1, 100);
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
        System.out.println("random start: " +randomStart);
        System.out.println("MAXQ!");
        System.out.println("single start state: " + singlePassenger);

        str = "number of params MAXQQ = " + maxqLearningAgent.numberOfParams();
//        DPrint.cl(debugCode,str);
        System.out.println(str);
//        Visualizer v = TaxiVisualizer.getVisualizer(5, 5);
//        new EpisodeSequenceVisualizer(v, d, testEpisodesMAXQ);

//        str = "num backups: " + maxqLearningAgent.getNumberOfBackups();
//        DPrint.cl(debugCode,str);


    }

}
