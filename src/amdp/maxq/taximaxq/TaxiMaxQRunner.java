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
public class TaxiMaxQRunner {


    public static void main(String[] args) {



        RandomFactory randomFactory = new RandomFactory();
        Random rand = randomFactory.getMapped(0);


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

        State s = TaxiDomain.getRandomClassicState(rand, d, false);



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


        if(true) {

            System.out.println("-------- MAXQ Test! ----------");
//
//            State state = TaxiDomain.getClassicState(d, false);

            int numberOfTests = 1;
            int numberOfLearningEpisodes = 100;
            int takeModOf = 10;
            int startTest = 200;
            int numBackups = 32000;

//            int numberOfTests = 1;
//            int numberOfLearningEpisodes = 10;
//            int takeModOf = 100;
//            int startTest = 200;


            Integer[][] stepsMaxQ = new Integer[numberOfTests][numberOfLearningEpisodes/takeModOf];
            Integer[][] stepsTestQLearning = new Integer[numberOfTests][numberOfLearningEpisodes/takeModOf];

            Integer[][] numberOfActionsMAXQLearning = new Integer[numberOfTests][numberOfLearningEpisodes/takeModOf];
            Integer[][] numberOfActionsQLearning = new Integer[numberOfTests][numberOfLearningEpisodes/takeModOf];

            Double[][] rewardsMaxQ = new Double[numberOfTests][numberOfLearningEpisodes/takeModOf];
            Double[][] rewardsTestQLearning = new Double[numberOfTests][numberOfLearningEpisodes/takeModOf];



//        SimulatedEnvironment env = new SimulatedEnvironment(d,taxiRF, navigate.getGroundedTasks(state).get(0).getTerminalFunction(), state);

//            SimulatedEnvironment env = new SimulatedEnvironment(d, state);

//        System.out.println("state: " + state.getCompleteStateDescription());


            for (int i = 0; i < numberOfTests; i++) {
                List<Episode> episodesMAXQ = new ArrayList<Episode>();
                List<Episode> testEpisodesMAXQ = new ArrayList<Episode>();
//                MAXQLearningAgent maxqLearningAgent = new MAXQLearningAgent(rootNode, new SimpleHashableStateFactory(), 1.0, 1.0);
                MAXQStateAbstractionAgent maxqLearningAgent = new MAXQStateAbstractionAgent(rootNode, new SimpleHashableStateFactory(), 1.0, 0.25);
//                MaxQForTesting maxqLearningAgent = new MaxQForTesting(rootNode, new SimpleHashableStateFactory(), 1.0, 0.25,numBackups);
//                MAX0LearningAgent maxqLearningAgent = new MAX0LearningAgent(rootNode, new SimpleHashableStateFactory(), 0.95, 0.5);
//                MAX0FasterLearningAgent maxqLearningAgent = new MAX0FasterLearningAgent(rootNode, new SimpleHashableStateFactory(), 0.95, 0.70);
//                maxqLearningAgent.setRmax(0.123 * (1 - 0.95));
                maxqLearningAgent.setVmax(0.123 );
                maxqLearningAgent.setQProviderForTaskNode(rootNode);
                maxqLearningAgent.setQProviderForTaskNode(getNode);
                maxqLearningAgent.setQProviderForTaskNode(putNode);
                maxqLearningAgent.setQProviderForTaskNode(navigate);

                maxqLearningAgent.setSolverDerivedPolicyForTaskNode(rootNode, new BoltzmannQPolicyWithCoolingSchedule(50, 0.9996));
                maxqLearningAgent.setSolverDerivedPolicyForTaskNode(getNode, new BoltzmannQPolicyWithCoolingSchedule(50, 0.9939));
                maxqLearningAgent.setSolverDerivedPolicyForTaskNode(putNode, new BoltzmannQPolicyWithCoolingSchedule(50, 0.9996));
                maxqLearningAgent.setSolverDerivedPolicyForTaskNode(navigate, new BoltzmannQPolicyWithCoolingSchedule(50, 0.9879));
//            maxqLearningAgent.setQProviderForTaskNode(rootNode);


                for (int j = 0; j < numberOfLearningEpisodes; j++) {

                    System.out.println("MAXQ learning episode: " + j);
                    System.out.println("-------------------------------------------------------------");
                    State sNew = TaxiDomain.getRandomClassicState(rand, d, false);
                    SimulatedEnvironment envN = new SimulatedEnvironment(d, sNew);

                    Episode ea = maxqLearningAgent.runLearningEpisode(envN,5000);
                    episodesMAXQ.add(ea);

//                    env.resetEnvironment();

                    if (j % takeModOf == 0 && j>0) {
                        maxqLearningAgent.setFreezeLearning(true);
                        System.out.println("MAXQ test episode: " + j / takeModOf);
                        System.out.println("-------------------------------------------------------------");
                        State sNew1 = TaxiDomain.getRandomClassicState(rand, d, false);
                        SimulatedEnvironment envN1 = new SimulatedEnvironment(d, sNew1);
                        Episode ea1 = maxqLearningAgent.runLearningEpisode(envN1, 1000);
//                    episodesMAXQ.add(ea1);
                        stepsMaxQ[i][(j / takeModOf) -1] = ea1.actionSequence.size();
                        rewardsMaxQ[i][(j / takeModOf) -1] = ea1.discountedReturn(1.0);
//                        env.resetEnvironment();
                        maxqLearningAgent.setFreezeLearning(false);
                        int numActions = 0;
                        for (Episode eaTemp : episodesMAXQ) {
                            numActions += eaTemp.actionSequence.size();
                        }
                        numberOfActionsMAXQLearning[i][(j / takeModOf) -1] = numActions;
                        System.out.println("i: " + i + " j: " + j + " actions:" + numActions);
                        testEpisodesMAXQ.add(ea1);
//                        System.out.println("number of backups: "+maxqLearningAgent.getNumberOfBackups());
                    }
                }

                System.out.println("number of params MAXQQ = " + maxqLearningAgent.numberOfParams());
                Visualizer v = TaxiVisualizer.getVisualizer(5,5);
//                new EpisodeSequenceVisualizer(v, d, episodesMAXQ);
                new EpisodeSequenceVisualizer(v,d,testEpisodesMAXQ);

                System.out.println("num backups: " +maxqLearningAgent.getNumberOfBackups());

            }




//            for (int i = 0; i < numberOfTests; i++) {
//                List<Episode> episodesQ = new ArrayList<Episode>();
//                QLearning q = new QLearning(d, 0.95, new SimpleHashableStateFactory(), 0.123, 0.25);
//                q.setLearningPolicy(new EpsilonGreedy(q, 0.2));
//
//                for (int j = 0; j < 100; j++) {
//
//                    System.out.println("learning episode: " + j);
//                    System.out.println("-------------------------------------------------------------");
//                    Episode ea = q.runLearningEpisode(env);
//                    episodesQ.add(ea);
//                    env.resetEnvironment();
//                    if (j % 10 == 0) {
//                        System.out.println("test episode: " + j / 10);
//                        System.out.println("-------------------------------------------------------------");
//                        Episode ea1 = PolicyUtils.rollout(new GreedyQPolicy(q), env, 100);
////                    episodesQ.add(ea1);
//                        stepsQLearning[i][j / 10] = ea1.stateSequence.size();
//                        rewardsQLearning[i][j / 10] = ea1.discountedReturn(1.0);
//                        env.resetEnvironment();
//                        int numActions = 0;
//                        for (Episode eaTemp : episodesQ) {
//                            numActions += eaTemp.numTimeSteps();
//                        }
//                        numberOfActionsQLearning[i][j / 10] = numActions;
//                        System.out.println("i: " + i + " j: " + j + " actions:" + numActions);
//                    }
//                }
//
//            }


//        List<EpisodeAnalysis> episodesQ = new ArrayList<>();
//        for(int i = 0; i < 50; i++){
//            System.out.println("learning episode: "+i);
//            System.out.println("-------------------------------------------------------------");
//            episodesQ.add(q.runLearningEpisode(env));
//            env.resetEnvironment();
//        }
//        q.setLearningPolicy(new EpsilonGreedy(q,0.));
//        env.resetEnvironment();
//        episodesQ.add(q.runLearningEpisode(env));

//        Visualizer v = TaxiVisualizer.getVisualizer(5,5);
//        new EpisodeSequenceVisualizer(v, d, episodesMAXQ);
//        new EpisodeSequenceVisualizer(v, d, episodesQ);

            double[] maxQAverageSteps = new double[numberOfLearningEpisodes/takeModOf];
//            double[] qAverageSteps = new double[numberOfLearningEpisodes/takeModOf];
            double[] averageNumberOfActionsMaxQ = new double[numberOfLearningEpisodes/takeModOf];
//            double[] averageNumberOfActionsQ = new double[10];

            double[] maxQAverageRewards = new double[numberOfLearningEpisodes/takeModOf];
//            double[] qAverageRewards = new double[10];

//        String strMaxQRewards = "MaxQ Rewards: ";
//        String strMaxQSteps = "MaxQ Steps: ";
//
//        String strQRewards = "QLearning Rewards: ";
//        String strQSteps = "QLearning Steps: ";

            final XYSeries seriesMAXQSteps = new XYSeries("MAXQ Steps");

            final XYSeries seriesMAXQRewards = new XYSeries("MAXQ Rewards");

            final XYSeries seriesQSteps = new XYSeries("QL Steps");

            final XYSeries seriesQRewards = new XYSeries("QL Rewards");


            for (int j = 0; j < stepsMaxQ[0].length-1; j++) {
                double sumStepsMaxQ = 0.;
                double sumRewardMaxQ = 0.;
                double sumStepsQ = 0.;
                double sumRewardQ = 0.;
                double sumActionsMAXQ = 0.;
                double sumActionsQ = 0.;
                for (int i = 0; i < numberOfTests; i++) {

                    sumStepsMaxQ += stepsMaxQ[i][j];
                    sumRewardMaxQ += rewardsMaxQ[i][j];

//                    sumStepsQ += stepsQLearning[i][j];
//                    sumRewardQ += rewardsQLearning[i][j];

                    sumActionsMAXQ += numberOfActionsMAXQLearning[i][j];
//                    sumActionsQ += numberOfActionsQLearning[i][j];

                }
                maxQAverageRewards[j] = sumRewardMaxQ / numberOfTests;
                seriesMAXQRewards.add(sumActionsMAXQ/numberOfTests, sumRewardMaxQ / numberOfTests);
                maxQAverageSteps[j] = sumStepsMaxQ / numberOfTests;
                seriesMAXQSteps.add(sumActionsMAXQ/numberOfTests, sumStepsMaxQ / numberOfTests);

//                qAverageRewards[j] = sumRewardQ / numberOfTests;
                seriesQRewards.add(sumActionsQ, sumRewardQ / numberOfTests);
//                qAverageSteps[j] = sumStepsQ / numberOfTests;
                seriesQSteps.add(sumActionsQ, sumStepsQ / numberOfTests);
            }

            XYSeriesCollection data = new XYSeriesCollection();
            data.addSeries(seriesMAXQRewards);
//            data.addSeries(seriesQRewards);
//            data.addSeries(seriesMAXQSteps);
//            data.addSeries(seriesQSteps);

            JFreeChart chart = ChartFactory.createXYLineChart(
                    "XY Series Demo",
                    "X",
                    "Y",
                    data,
                    PlotOrientation.VERTICAL,
                    true,
                    true,
                    false
            );
            ChartPanel chartPanel = new ChartPanel(chart);

            ApplicationFrame ap = new ApplicationFrame("plot");
            ap.setContentPane(chartPanel);
            ap.pack();
            ap.setVisible(true);

        }

        if(false) {
            System.out.println("-------- QLearning Test! ----------");

            int numberOfTests = 1;
            int numberOfLearningEpisodes = 10000;
            int takeModOf = 100;
            int startTest = 200;
            Integer[][] stepsMaxQ = new Integer[numberOfTests][numberOfLearningEpisodes/takeModOf];
            Integer[][] stepsTestQLearning = new Integer[numberOfTests][numberOfLearningEpisodes/takeModOf];

            Integer[][] numberOfActionsMAXQLearning = new Integer[numberOfTests][numberOfLearningEpisodes/takeModOf];
            Integer[][] numberOfActionsQLearning = new Integer[numberOfTests][numberOfLearningEpisodes/takeModOf];

            Double[][] rewardsMaxQ = new Double[numberOfTests][numberOfLearningEpisodes/takeModOf];
            Double[][] rewardsTestQLearning = new Double[numberOfTests][numberOfLearningEpisodes/takeModOf];


            for (int i = 0; i < numberOfTests; i++) {
//                State state = TaxiDomain.getRandomClassicState(rand, d, false);
                SimulatedEnvironment env = new SimulatedEnvironment(d, s);
                List<Episode> episodesQ = new ArrayList<Episode>();
//                QLearning q = new QLearning(td, 0.95, new SimpleHashableStateFactory(), 0.123, 0.35);
//                q.setLearningPolicy(new EpsilonGreedy(q, 0.2));
//                q.setLearningPolicy(new BoltzmannQPolicyWithCoolingSchedule(q,50,0.460));

                QLearningForMaxQ q = new QLearningForMaxQ(d, 0.95, new SimpleHashableStateFactory(), 0.123, 0.25);
                q.setLearningPolicy(new BoltzmannQPolicyWithCoolingSchedule(q,50,0.9879));


                for (int j = 1; j <= numberOfLearningEpisodes; j++) {
                    System.out.println("QL test: " +i+", "+ "QL learning episode: " + j);
                    System.out.println("-------------------------------------------------------------");
                    State sNew = TaxiDomain.getRandomClassicState(rand, d, false);
                    SimulatedEnvironment envN = new SimulatedEnvironment(d, sNew);
                    Episode ea = q.runLearningEpisode(envN);
                    episodesQ.add(ea);
                    env.resetEnvironment();
                    if (j >=startTest && j % takeModOf == 0) {
                        System.out.println("QL test episode: " + j / takeModOf);
                        System.out.println("-------------------------------------------------------------");
                        Episode ea1 = PolicyUtils.rollout(new GreedyQPolicy(q), s, d.getModel(),1000);//.evaluateBehavior(env,100);
//                    episodesQ.add(ea1);
                        stepsTestQLearning[i][(j / takeModOf)-1] = ea1.actionSequence.size();
                        rewardsTestQLearning[i][(j / takeModOf)-1] = ea1.discountedReturn(1.0);
                        env.resetEnvironment();
                        int numActions = 0;
                        for (Episode eaTemp : episodesQ) {
                            numActions += eaTemp.actionSequence.size();
                        }
                        numberOfActionsQLearning[i][(j / takeModOf)-1] = numActions;
                        System.out.println("i: " + i + " j: " + j + " actions:" + numActions);
                    }
                    if(j % takeModOf == 0 && j<startTest){
                        stepsTestQLearning[i][(j / takeModOf)-1] = 1;
                        rewardsTestQLearning[i][(j / takeModOf)-1] = -1.;
                        int numActions = 0;
                        for (Episode eaTemp : episodesQ) {
                            numActions += eaTemp.actionSequence.size();
                        }
                        numberOfActionsQLearning[i][(j / takeModOf)-1] = numActions;
                        System.out.println("i: " + i + " j: " + j + " actions:" + numActions);
                    }
                }

                System.out.println("number of params QLearning = " + q.numberOfParams());



            }

            double[] qAverageSteps = new double[numberOfLearningEpisodes/takeModOf];
            double[] qAverageRewards = new double[numberOfLearningEpisodes/takeModOf];

            double[] qTestAverageRewards = new double[numberOfLearningEpisodes/takeModOf];


            final XYSeries seriesQSteps = new XYSeries("QL Steps");

            final XYSeries seriesQRewards = new XYSeries("QL Rewards");

            final XYSeries seriesTestAverageRewardPerAction = new XYSeries("QL average rewards per step");


            for (int j = 0; j < stepsTestQLearning[0].length; j++) {
                double sumStepsMaxQ = 0.;
                double sumRewardMaxQ = 0.;
                double sumStepsQ = 0.;
                double sumRewardQ = 0.;
                double sumActionsMAXQ = 0.;
                double sumActionsQ = 0.;
                for (int i = 0; i < numberOfTests; i++) {

//                sumStepsMaxQ+=stepsMaxQ[i][j];
//                sumRewardMaxQ+=rewardsMaxQ[i][j];

                    sumStepsQ += stepsTestQLearning[i][j];
                    sumRewardQ += rewardsTestQLearning[i][j];

//                sumActionsMAXQ+=numberOfActionsMAXQLearning[i][j];
                    sumActionsQ += numberOfActionsQLearning[i][j];

                }
//            maxQAverageRewards[j]=sumRewardMaxQ/numberOfTests;
//            seriesMAXQRewards.add(sumActionsMAXQ,sumRewardMaxQ/numberOfTests);
//            maxQAverageSteps[j]=sumStepsMaxQ/numberOfTests;
//            seriesMAXQSteps.add(sumActionsMAXQ, sumStepsMaxQ/numberOfTests);

                qAverageRewards[j] = sumRewardQ / numberOfTests;
                seriesQRewards.add(sumActionsQ / numberOfTests, sumRewardQ / numberOfTests);
                qAverageSteps[j] = sumStepsQ / numberOfTests;
                seriesQSteps.add(sumActionsQ / numberOfTests, sumStepsQ / numberOfTests);

                seriesTestAverageRewardPerAction.add((j+1)*takeModOf,sumRewardQ/sumStepsQ);
            }

            XYSeriesCollection data = new XYSeriesCollection();
//        data.addSeries(seriesMAXQRewards);
            data.addSeries(seriesQRewards);
//        data.addSeries(seriesMAXQSteps);
//            data.addSeries(seriesQSteps);
//            data.addSeries(seriesTestAverageRewardPerAction);

            JFreeChart chart = ChartFactory.createXYLineChart(
                    "XY Series Demo",
                    "X",
                    "Y",
                    data,
                    PlotOrientation.VERTICAL,
                    true,
                    true,
                    false
            );
            ChartPanel chartPanel = new ChartPanel(chart);

            ApplicationFrame ap = new ApplicationFrame("plot");
            ap.setContentPane(chartPanel);
            ap.pack();
            ap.setVisible(true);



        }


//
//        LearningAgentFactory qLearningFactory = new LearningAgentFactory() {
//
//
//            public String getAgentName() {
//                return "Q-Learning";
//            }
//
//
//            public LearningAgent generateAgent() {
//                QLearning q = new QLearning(d, 0.95, new SimpleHashableStateFactory(), 0.123, 0.5);
//                q.setLearningPolicy(new EpsilonGreedy(q,0.2));
//                return q;
//            }
//        };
//
//
//        LearningAgentFactory MAXQLearningFactory = new LearningAgentFactory() {
//
//
//            public String getAgentName() {
//                return "MAXQ";
//            }
//
//
//            public LearningAgent generateAgent() {
////                QLearning q = new QLearning(d, 0.95, new SimpleHashableStateFactory(), 0.123, 0.1);
////                q.setLearningPolicy(new EpsilonGreedy(q,0.2));
//                MAX0LearningAgent maxqLearningAgent = new MAX0LearningAgent(rootNode, new SimpleHashableStateFactory(),0.2,0.95,0.5);
//                maxqLearningAgent.setRmax(0.123*(1-0.95));
//                return maxqLearningAgent;
//            }
//        };
//
//        LearningAlgorithmExperimenter exp = new LearningAlgorithmExperimenter(env, 10, 200, qLearningFactory, MAXQLearningFactory);
//        exp.setUpPlottingConfiguration(500, 250, 2, 1000,
//                TrialMode.MOSTRECENTANDAVERAGE,
//                PerformanceMetric.CUMULATIVEREWARDPERSTEP,
//                PerformanceMetric.AVERAGEEPISODEREWARD,
//                PerformanceMetric.CUMULATIVESTEPSPEREPISODE);
//
//        exp.startExperiment();
//        exp.writeStepAndEpisodeDataToCSV("expData");
//
    }

}
