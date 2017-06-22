package amdp.cleanupturtlebot;

import amdp.amdpframework.*;
//import amdp.cleanup.CleanupDomain;
import amdp.cleanup.CleanupVisualiser;
import amdp.cleanup.FixedDoorCleanupEnv;
import amdp.cleanup.PullCostGoalRF;
import amdp.cleanup.state.CleanupAgent;
import amdp.cleanup.state.CleanupDoor;
import amdp.cleanup.state.CleanupState;
import amdp.cleanupamdpdomains.cleanupamdp.RootTaskNode;
import amdp.cleanupamdpdomains.cleanuplevel1.CleanupL1Domain;
import amdp.cleanupamdpdomains.cleanuplevel1.state.CleanupAgentL1;
import amdp.cleanupamdpdomains.cleanuplevel1.state.CleanupBlockL1;
import amdp.cleanupamdpdomains.cleanuplevel1.state.CleanupL1State;
import amdp.cleanupamdpdomains.cleanuplevel1.state.CleanupL1StateMapper;
import amdp.cleanupamdpdomains.cleanuplevel2.state.CleanupAgentL2;
import amdp.cleanupamdpdomains.cleanuplevel2.state.CleanupBlockL2;
import amdp.cleanupamdpdomains.cleanuplevel2.state.CleanupL2State;
import amdp.cleanupamdpdomains.cleanuplevel2.state.CleanupL2StateMapper;
import amdp.cleanupamdpdomains.cleanuplevel2.*;
import amdp.cleanupturtlebot.cleanupcontinuous.CleanupContinuousDomain;
import amdp.cleanupturtlebot.cleanupcontinuous.CleanupContinuousVisualiser;
import amdp.cleanupturtlebot.cleanupcontinuous.state.CleanupContinuousState;
import amdp.cleanupturtlebot.cleanupl0discrete.CleanupTurtleBotL0Domain;
import amdp.cleanupturtlebot.cleanupl0discrete.state.CleanupContinuousToDiscreteStateMapper;
import amdp.cleanupturtlebot.robotturtlebot.TurtleBotDiscreteEnvironment;
import amdp.cleanupturtlebot.robotturtlebot.TurtleBotEnvironment;
import amdp.taxiamdpdomains.testingtools.BoundedRTDPForTests;
import amdp.taxiamdpdomains.testingtools.GreedyReplan;
import amdp.taxiamdpdomains.testingtools.MutableGlobalInteger;
import burlap.behavior.policy.Policy;
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
import burlap.mdp.core.TerminalFunction;
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
import com.fasterxml.jackson.databind.JsonNode;
import org.apache.commons.lang3.StringUtils;

import java.io.*;
import java.net.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Scanner;


import burlap.ros.actionpub.RepeatingActionPublisher;
import ros.RosBridge;
import ros.RosListenDelegate;
import ros.SubscriptionRequestMsg;
import ros.msgs.geometry_msgs.Twist;
import ros.msgs.geometry_msgs.Vector3;
import ros.msgs.geometry_msgs.Twist;
import ros.msgs.std_msgs.PrimitiveMsg;
import ros.tools.MessageUnpacker;
//import ros.msgs.std_msgs;



import java.util.*;

import static amdp.cleanupturtlebot.cleanupcontinuous.CleanupContinuousDomain.*;

/**
 * Created by ngopalan on 8/31/16.
 */
public class CleanupLanguageSingleRNNCode {

    static protected MutableGlobalInteger bellmanBudget = new MutableGlobalInteger(-1);
    static protected MutableGlobalInteger bellmanBudgetL0 = new MutableGlobalInteger(-1);
    static protected MutableGlobalInteger bellmanBudgetL1 = new MutableGlobalInteger(-1);
    static protected MutableGlobalInteger bellmanBudgetL2 = new MutableGlobalInteger(-1);

    static double L2Ratio = 0.05;
    static double L1Ratio = 0.65;
    static double L0Ratio = 0.3;

    static int maxTrajectoryLength = 1000;

    public static List<BoundedRTDPForTests> brtdpList= new ArrayList<BoundedRTDPForTests>();

    public static Map<String, BoundedRTDPForTests> brtdpMap =  new HashMap<String, BoundedRTDPForTests>();
    static String test = "";

    public static void main(String[] args) throws Exception{

//        DPrint.toggleCode(3214986, false);
        RandomFactory randomFactory = new RandomFactory();
        Random rand = randomFactory.getMapped(0);
        double lockProb = 0.5;

        Integer level;
        String[] responses;

        if(true) {
            if (false) {


                String uri = "ws://localhost:9090";

                RosBridge bridge = new RosBridge();
                bridge.connect(uri, true);


                bridge.subscribe(SubscriptionRequestMsg.generate("/speech_recognition")
                                .setType("std_msgs/String")
                                .setThrottleRate(1)
                                .setQueueLength(1),
                        new RosListenDelegate() {
                            @Override
                            public void receive(JsonNode data, String stringRep) {
                                MessageUnpacker<PrimitiveMsg<String>> unpacker = new MessageUnpacker<PrimitiveMsg<String>>(PrimitiveMsg.class);
                                PrimitiveMsg<String> msg = unpacker.unpackRosMessage(data);
                                test = msg.data;
//                        System.out.println(msg.data);
                            }
                        }
                );


                while (test.equals("")) {
                    try {
                        Thread.sleep(10);
                    } catch (InterruptedException ex) {
                        Thread.currentThread().interrupt();
                    }
                }



            } else {
                Scanner reader = new Scanner(System.in);
                System.out.print("Enter a Natural Language Command: ");
                test = reader.nextLine();
            }
            Scanner reader = new Scanner(System.in);
//        while (true) {
//            System.out.print("Enter a Natural Language Command: ");
//            String command = reader.nextLine();

            String command = test;
            System.out.println(command);


            long startTime1 = System.currentTimeMillis();

            URL commandURL = new URL("http://138.16.161.191:5000/model?command=" +
                    URLEncoder.encode(command, "UTF-8"));//192.168.160.180
            BufferedReader in = new BufferedReader(new InputStreamReader(commandURL.openStream()));
            responses = in.readLine().split("\\| ");
            for (int i = 0; i < responses.length; i++) {
                System.out.println(i + ": " + responses[i]);
            }

            long totalTime = System.currentTimeMillis() - startTime1;
//        System.out.println("time taken: "+ totalTime );
            level = Integer.parseInt(responses[0].substring(1));
        }
        else{
            responses = new String[]{"0","west"};
            level = 0;
        }
        System.out.println(responses.length);
        String rewardFunction;
        String newString = Arrays.toString(responses);

        String[] response = responses;

        if(true) {


            List<String> goalBlocks = new ArrayList<>();//Arrays.asList("block0");//"block1"
            List<String> goalRooms = new ArrayList<>();//Arrays.asList(goalRoom);//"room3"


            State sCont = CleanupContinuousDomain.getClassicState(true);

            String rosURI = "ws://192.168.160.162:9090";
//                    TurtleBotDiscreteEnvironment tb_env = new TurtleBotDiscreteEnvironment(rosURI, (CleanupState) s);

            TurtleBotEnvironment tb_env = new TurtleBotEnvironment(rosURI, (CleanupContinuousState) sCont);
            System.out.println(tb_env.currentObservation().toString());


            State s = new CleanupContinuousToDiscreteStateMapper().mapState(tb_env.currentObservation());


            // create the propositional function to be grounded

            PropositionalFunction pf;
            GroundedProp gp = null;// = new GroundedProp(pf, new String[]{"block0", goalRoom});


            if (response.length < 5) {
                // it is not agent and block in regions!
                if (response.length < 4) {
                    if (response[1].toLowerCase().contains("up")) {
                        pf = new CleanupTurtleBotL0Domain.PF_InDirection(CleanupTurtleBotL0Domain.PF_GO_NORTH, new String[]{}, s, "north");
                        gp = new GroundedProp(pf, new String[]{});
                    } else if (response[1].toLowerCase().contains("down")) {
                        pf = new CleanupTurtleBotL0Domain.PF_InDirection(CleanupTurtleBotL0Domain.PF_GO_SOUTH, new String[]{}, s, "south");
                        gp = new GroundedProp(pf, new String[]{});
                    } else if (response[1].toLowerCase().contains("right")) {
                        pf = new CleanupTurtleBotL0Domain.PF_InDirection(CleanupTurtleBotL0Domain.PF_GO_EAST, new String[]{}, s, "east");
                        gp = new GroundedProp(pf, new String[]{});
                    } else if (response[1].toLowerCase().contains("left")) {
                        pf = new CleanupTurtleBotL0Domain.PF_InDirection(CleanupTurtleBotL0Domain.PF_GO_WEST, new String[]{}, s, "west");
                        gp = new GroundedProp(pf, new String[]{});
                    } else {
                        System.err.println("something wrong: " + response[1].toLowerCase());
                        System.exit(-1);
                    }
                } else {
                    if (response[1].toLowerCase().contains("agent")) {
                        // is an agent in region
                        if (response[3].toLowerCase().contains("room")) {
                            // agent to room
                            pf = new CleanupTurtleBotL0Domain.PF_InRegion(CleanupTurtleBotL0Domain.PF_AGENT_IN_ROOM, new String[]{CleanupTurtleBotL0Domain.CLASS_AGENT, CleanupTurtleBotL0Domain.CLASS_ROOM}, false);
                            gp = new GroundedProp(pf, new String[]{response[2], response[3]});
                        } else {
                            // agent to door prop
                            pf = new CleanupTurtleBotL0Domain.PF_InRegion(CleanupTurtleBotL0Domain.PF_AGENT_IN_DOOR, new String[]{CleanupTurtleBotL0Domain.CLASS_AGENT, CleanupTurtleBotL0Domain.CLASS_DOOR}, true);
                            gp = new GroundedProp(pf, new String[]{response[2], response[3]});
                        }
                    } else {
                        // is a block in region
                        if (response[3].toLowerCase().contains("room")) {
                            // block to room
                            pf = new CleanupTurtleBotL0Domain.PF_InRegion(CleanupTurtleBotL0Domain.PF_BLOCK_IN_ROOM, new String[]{CleanupTurtleBotL0Domain.CLASS_BLOCK, CleanupTurtleBotL0Domain.CLASS_ROOM}, false);
                            gp = new GroundedProp(pf, new String[]{response[2], response[3]});
                        } else {
                            // block to door prop
                            pf = new CleanupTurtleBotL0Domain.PF_InRegion(CleanupTurtleBotL0Domain.PF_BLOCK_IN_DOOR, new String[]{CleanupTurtleBotL0Domain.CLASS_BLOCK, CleanupTurtleBotL0Domain.CLASS_DOOR}, true);
                            gp = new GroundedProp(pf, new String[]{response[2], response[3]});
                        }
                    }

                    goalBlocks.add(response[2]);
                    goalRooms.add(response[3]);
                }


            } else {
                // it is agent and block in region
                if (response[3].toLowerCase().contains("room")) {
                    if (response[6].toLowerCase().contains("room")) {
                        pf = new CleanupTurtleBotL0Domain.PF_TwoObjectsInRegions(CleanupTurtleBotL0Domain.PF_BLOCK_IN_ROOM_AGENT_IN_ROOM, new String[]
                                {CleanupTurtleBotL0Domain.CLASS_BLOCK, CleanupTurtleBotL0Domain.CLASS_ROOM, CleanupTurtleBotL0Domain.CLASS_AGENT, CleanupTurtleBotL0Domain.CLASS_ROOM}, false, false);
                        gp = new GroundedProp(pf, new String[]{response[5], response[6], response[2], response[3]});
                    } else {
                        // block to door
                        pf = new CleanupTurtleBotL0Domain.PF_TwoObjectsInRegions(CleanupTurtleBotL0Domain.PF_BLOCK_IN_DOOR_AGENT_IN_ROOM, new String[]
                                {CleanupTurtleBotL0Domain.CLASS_BLOCK, CleanupTurtleBotL0Domain.CLASS_DOOR, CleanupTurtleBotL0Domain.CLASS_AGENT, CleanupTurtleBotL0Domain.CLASS_ROOM}, true, false);
                        gp = new GroundedProp(pf, new String[]{response[5], response[6], response[2], response[3]});
                    }

                } else {
                    if (response[6].toLowerCase().contains("room")) {
                        // block to door
                        pf = new CleanupTurtleBotL0Domain.PF_TwoObjectsInRegions(CleanupTurtleBotL0Domain.PF_BLOCK_IN_ROOM_AGENT_IN_DOOR, new String[]
                                {CleanupTurtleBotL0Domain.CLASS_BLOCK, CleanupTurtleBotL0Domain.CLASS_ROOM, CleanupTurtleBotL0Domain.CLASS_AGENT, CleanupTurtleBotL0Domain.CLASS_DOOR}, false, true);
                        gp = new GroundedProp(pf, new String[]{response[5], response[6], response[2], response[3]});
                    } else {
                        pf = new CleanupTurtleBotL0Domain.PF_TwoObjectsInRegions(CleanupTurtleBotL0Domain.PF_BLOCK_IN_DOOR_AGENT_IN_DOOR, new String[]
                                {CleanupTurtleBotL0Domain.CLASS_BLOCK, CleanupTurtleBotL0Domain.CLASS_DOOR, CleanupTurtleBotL0Domain.CLASS_AGENT, CleanupTurtleBotL0Domain.CLASS_DOOR}, true, true);
                        gp = new GroundedProp(pf, new String[]{response[5], response[6], response[2], response[3]});
                    }

                }
                goalBlocks.add(response[2]);
                goalRooms.add(response[3]);
                goalBlocks.add(response[5]);
                goalRooms.add(response[6]);

            }


            System.out.println("goal prop: " + gp.toString());


            newString = newString.substring(3, newString.length() - 1);
            rewardFunction = newString;//StringUtils.join(" ", Arrays.copyOfRange(response, 1, response.length));
            System.out.println("Level: " + level + " RF: " + rewardFunction);


            if (true) {


//                double lockProb = 0.5;

                Integer rooms = 1;
                Integer numberOfObjects = 3;

                int numEnvSteps = 255;

                int totalBudget = 500000;
//        bellmanBudget.setValue(80000);

                for (int i = 0; i < args.length; i++) {
                    String str = args[i];
//            System.out.println(str);
                    if (str.equals("-r")) {
                        rooms = Integer.parseInt(args[i + 1]);
                    }
                    if (str.equals("-b")) {
                        totalBudget = Integer.parseInt(args[i + 1]);
                    }
                    if (str.equals("-o")) {
                        numberOfObjects = Integer.parseInt(args[i + 1]);
                    }
                    if (str.equals("-l0")) {
                        L0Ratio = Double.parseDouble(args[i + 1]);
                    }
                    if (str.equals("-l1")) {
                        L1Ratio = Double.parseDouble(args[i + 1]);
                    }
                    if (str.equals("-l2")) {
                        L2Ratio = Double.parseDouble(args[i + 1]);
                    }
                }


                bellmanBudget.setValue(totalBudget);
                bellmanBudgetL0.setValue((int) (totalBudget * L0Ratio));
                bellmanBudgetL1.setValue((int) (totalBudget * L1Ratio));
                bellmanBudgetL2.setValue((int) (totalBudget * L2Ratio));

//                PropositionalFunction pf = new CleanupTurtleBotL0Domain.PF_InRegion(CleanupTurtleBotL0Domain.PF_BLOCK_IN_ROOM, new String[]{CleanupTurtleBotL0Domain.CLASS_BLOCK, CleanupTurtleBotL0Domain.CLASS_ROOM}, false);


//            State s;

//                String goalRoom;

//                if (false) {
//
//                    if (rooms == 1) {
//                        s = CleanupDomain.getClassicState(true);
//                        goalRoom = "room1";
////            gp =  new GroundedProp(pf,new String[]{"block0", "room1"});
//
//                    } else if (rooms == 2) {
//                        s = CleanupDomain.getState(true, true, 3, 3);
//                        goalRoom = "room2";
//
//                    } else {
//                        s = CleanupDomain.getState(true, true, 3, 4);
//                        goalRoom = "room3";
//                    }
//                }


//        s = CleanupDomain.getTenRoomState(true);
//        goalRoom = "room9";


                List<AMDPPolicyGenerator> pgList = new ArrayList<AMDPPolicyGenerator>();
                TaskNode root;
                AMDPAgent agent;


                PropositionalFunction pfL0 = new PF_TestFinalL0Goal("ContinousTempGoal", new String[]{}, gp);

                GroundedProp gpContinuous = new GroundedProp(pfL0, new String[]{});

                GroundedPropSC lcsc = new GroundedPropSC(gpContinuous);
                GoalBasedRF lcrf = new GoalBasedRF(lcsc, 1., 0.);

                GoalConditionTF lctf = new GoalConditionTF(lcsc);
                CleanupContinuousDomain dgenContinuous = new CleanupContinuousDomain(lcrf, lctf, rand);
//            CleanupContinuousDomain dgen = new CleanupContinuousDomain();
                dgenContinuous.includeDirectionAttribute(true);
//            dgen.includePullAction(true);
                dgenContinuous.includeWallPF_s(true);
                dgenContinuous.includeLockableDoors(true);
                dgenContinuous.setLockProbability(0.5);
                OOSADomain domainLContinuous = dgenContinuous.generateDomain();


//            State sCont = CleanupContinuousDomain.getParameterizedClassicState(true,1);


//                s = CleanupTurtleBotL0Domain.getClassicState(true);
//            s = CleanupTurtleBotL0Domain.getParameterizedClassicState(true, 1);
//                goalRoom = "room1";


                GroundedPropSC l0sc = new GroundedPropSC(gp);
                GoalBasedRF l0rf = new GoalBasedRF(l0sc, 1., 0.);
                GoalConditionTF l0tf = new GoalConditionTF(l0sc);
                CleanupTurtleBotL0Domain dgen = new CleanupTurtleBotL0Domain(l0rf, l0tf, rand);
                dgen.includeDirectionAttribute(true);
//                dgen.includePullAction(true);
                dgen.includeWallPF_s(true);
                dgen.includeLockableDoors(true);
                dgen.setLockProbability(lockProb);
                OOSADomain domain = dgen.generateDomain();


                //Action types

//                ActionType north = domain.getAction(CleanupTurtleBotL0Domain.ACTION_NORTH);
//                ActionType east = domain.getAction(CleanupTurtleBotL0Domain.ACTION_EAST);
//                ActionType west = domain.getAction(CleanupTurtleBotL0Domain.ACTION_WEST);
//                ActionType south = domain.getAction(CleanupTurtleBotL0Domain.ACTION_SOUTH);
//                // pull is assumed true
//                ActionType pull = domain.getAction(CleanupTurtleBotL0Domain.ACTION_PULL);


                ActionType fwd = domainLContinuous.getAction(ACTION_MOVE_FORWARD);
                ActionType back = domainLContinuous.getAction(ACTION_MOVE_BACK);
                ActionType turncw = domainLContinuous.getAction(ACTION_TURN_CW);
                ActionType turnccw = domainLContinuous.getAction(ACTION_TURN_CCW);


                TaskNode ft = new ContinuousTaskNode(fwd);
                TaskNode bt = new ContinuousTaskNode(back);
                TaskNode cwt = new ContinuousTaskNode(turncw);
                TaskNode ccwt = new ContinuousTaskNode(turnccw);


                TaskNode[] L0Subtasks = new TaskNode[]{ft, bt, cwt, ccwt};


                ActionType fwdL1 = domain.getAction(CleanupTurtleBotL0Domain.ACTION_MOVE_FORWARD);
//            ActionType backL1 = domain.getAction(CleanupTurtleBotL0Domain.ACTION_MOVE_BACK);
                ActionType turncwL1 = domain.getAction(CleanupTurtleBotL0Domain.ACTION_TURN_CW);
                ActionType turnccwL1 = domain.getAction(CleanupTurtleBotL0Domain.ACTION_TURN_CCW);

                //TaskNodes


                TaskNode ftL0 = new L0TaskNode(fwdL1, dgenContinuous.generateDomain(), L0Subtasks);
//            TaskNode btL0 = new L0TaskNode(backL1, dgenContinuous.generateDomain(), L0Subtasks);
                TaskNode cwtL0 = new L0TaskNode(turncwL1, dgenContinuous.generateDomain(), L0Subtasks);
                TaskNode ccwtL0 = new L0TaskNode(turnccwL1, dgenContinuous.generateDomain(), L0Subtasks);

//            TaskNode ftL0 = new L0DiscretePrimitiveNode(fwdL1);
//            TaskNode cwtL0 = new L0DiscretePrimitiveNode(turncwL1);
//            TaskNode ccwtL0 = new L0DiscretePrimitiveNode(turnccwL1);

                TaskNode[] L1Subtasks = new TaskNode[]{ftL0, cwtL0, ccwtL0};

                if (level == 0) {
                    // make sure rf is a pull cost rf
                    RewardFunction pullCostRFL0 = new PullCostGoalRF(l0sc, 1., 0.);
                    root = new RootTaskNode("root", L1Subtasks, domain, l0tf, pullCostRFL0);
                    pgList.add(0, new CleanupTurtleBotAMDPRunner.continuousPolicyGenerator(domainLContinuous, lockProb));
                    pgList.add(1, new l0PolicyGenerator(domain, lockProb));
                    agent = new AMDPAgent(root.getApplicableGroundedTasks(s).get(0), pgList);
                } else {


                    StateConditionTest l1sc = new L1Goal(goalBlocks, goalRooms);//new CleanupL1Domain.InRegionSC("block0", goalRoom);
                    RewardFunction l1rf = new GoalBasedRF(l1sc, 1.);
                    TerminalFunction l1tf = new GoalConditionTF(l1sc);

                    CleanupL1Domain adgen = new CleanupL1Domain(domain, l1rf, l1tf);
                    adgen.setLockableDoors(true);
                    adgen.setLockProb(lockProb);
                    OOSADomain adomain = adgen.generateDomain();

                    State as = new CleanupL1StateMapper().mapState(s);
//        System.out.println(as.toString());
//
//        List<Action> a = adomain.getAction(ACTION_BLOCK_TO_DOOR).allApplicableActions(as);
//        System.out.println("actions: " + a.size());

                    ActionType a2d = adomain.getAction(CleanupL1Domain.ACTION_AGENT_TO_DOOR);
                    ActionType a2r = adomain.getAction(CleanupL1Domain.ACTION_AGENT_TO_ROOM);
                    ActionType b2d = adomain.getAction(CleanupL1Domain.ACTION_BLOCK_TO_DOOR);
                    ActionType b2r = adomain.getAction(CleanupL1Domain.ACTION_BLOCK_TO_ROOM);


                    TaskNode a2dt = new L1TaskNode(a2d, dgen.generateDomain(), L1Subtasks);
                    TaskNode a2rt = new L1TaskNode(a2r, dgen.generateDomain(), L1Subtasks);
                    TaskNode b2dt = new L1TaskNode(b2d, dgen.generateDomain(), L1Subtasks);
                    TaskNode b2rt = new L1TaskNode(b2r, dgen.generateDomain(), L1Subtasks);

                    TaskNode[] L2Subtasks = new TaskNode[]{a2dt, a2rt, b2dt, b2rt};


                    if (level == 1) {
                        System.out.println("in level 1!!!");
                        root = new RootTaskNode("root", L2Subtasks, adomain, l1tf, l1rf);
                        pgList.add(0, new CleanupTurtleBotAMDPRunner.continuousPolicyGenerator(domainLContinuous, lockProb));
                        pgList.add(1, new l0PolicyGenerator(domain, lockProb));
                        pgList.add(2, new l1PolicyGenerator(adomain));
                        agent = new AMDPAgent(root.getApplicableGroundedTasks(as).get(0), pgList);
                    } else {

                        StateConditionTest l2sc = new L2Goal(goalBlocks, goalRooms);
                        GoalBasedRF l2rf = new GoalBasedRF(l2sc, 1., 0.);
                        GoalConditionTF l2tf = new GoalConditionTF(l2sc);


                        CleanupL2Domain aadgen = new CleanupL2Domain(l2rf, l2tf);
                        OOSADomain aadomain = aadgen.generateDomain();

                        State aas = new CleanupL2StateMapper().mapState(as);


                        ActionType a2r_l2 = aadomain.getAction(CleanupL1Domain.ACTION_AGENT_TO_ROOM);
                        ActionType b2r_l2 = aadomain.getAction(CleanupL1Domain.ACTION_BLOCK_TO_ROOM);


                        TaskNode a2r_l2t = new L2TaskNode(a2r_l2, adgen.generateDomain(), L2Subtasks);
                        TaskNode b2r_l2t = new L2TaskNode(b2r_l2, adgen.generateDomain(), L2Subtasks);


                        root = new RootTaskNode("root", new TaskNode[]{a2r_l2t, b2r_l2t}, aadomain, l2tf, l2rf);


                        pgList.add(0, new CleanupTurtleBotAMDPRunner.continuousPolicyGenerator(domainLContinuous, lockProb));
                        pgList.add(1, new l0PolicyGenerator(domain, lockProb));
                        pgList.add(2, new l1PolicyGenerator(adomain));
                        pgList.add(3, new l2PolicyGenerator(aadomain));

                        agent = new AMDPAgent(root.getApplicableGroundedTasks(aas).get(0), pgList);

                    }

                }

//            if(false) {
//
//
////        SimulatedEnvironment envN = new SimulatedEnvironment(dgen.generateDomain(), s);
////
////                FixedDoorCleanupEnv env = new FixedDoorCleanupEnv(dgen.generateDomain(), s);
////                if (false) {
////                    if (rooms == 3) {
////                        env.addLockedDoor("door0");
////                    }
////                }
////
////
//////                long startTime = System.currentTimeMillis();
////                Episode e = agent.actUntilTermination(env, maxTrajectoryLength);
////
////                long endTime = System.currentTimeMillis();
////
////                long duration = endTime - startTime1;
////
////                Visualizer v = CleanupVisualiser.getVisualizer("amdp/data/resources/robotImages");
////                //		System.out.println(ea.getState(0).toString());
////                new EpisodeSequenceVisualizer(v, domain, Arrays.asList(e));
//
//
//
//
//
//                SimulatedEnvironment env = new SimulatedEnvironment(dgenContinuous.generateDomain(), sCont);
////        if(rooms==3) {
////            env.addLockedDoor("door0");
////        }
//
//
//
//                agent.recordTestEpisodes(10, "amdp/out/record.episode");
//                Episode e = agent.actUntilTermination(env, 30000);//30000
//
//                long tEnd = System.currentTimeMillis();
//                long tDelta = tEnd - startTime1;
//                double elapsedSeconds = tDelta / 1000.0;
//                System.out.println("time: " + tDelta + "  " + elapsedSeconds);
//                Visualizer v = CleanupContinuousVisualiser.getVisualizer("amdp/data/resources/robotImages");
//                //		System.out.println(ea.getState(0).toString());
//                new EpisodeSequenceVisualizer(v, domainLContinuous, Arrays.asList(e));
//
//
////                new EpisodeSequenceVisualizer(v, domainLContinuous, "amdp/out/");
//
//
//                System.out.println(e.actionSequence.size());
//                System.out.println(e.discountedReturn(1.));
//                //TODO: add counters to count updates!
//
//                int count = 0;
//                for (int i = 0; i < brtdpList.size(); i++) {
//                    int numUpdates = brtdpList.get(i).getNumberOfBellmanUpdates();
//                    count += numUpdates;
//                }
//
//                System.out.println(count);
////        System.out.println( brtd.getNumberOfBellmanUpdates());
////        System.out.println("Total planners used: " + brtdpList.size());
//                System.out.println("AMDP Cleanup");
//                System.out.println("room number: " + rooms);
//                System.out.println("backups: " + totalBudget);
//
//
//                System.out.println("Total planners used: " + brtdpList.size());
////        System.out.println("CleanUp with AMDPs \n Backups by individual planners:");
//                System.out.println("CleanUp with AMDPs \n Backups by individual planners:");
//                for (BoundedRTDPForTests b : brtdpList) {
//                    System.out.println(b.getNumberOfBellmanUpdates());
//                }
//
//
//                System.out.println("total duration: " + tDelta);
//            }
//            else
                {
//            CleanupContinuousState s = (CleanupContinuousState)CleanupContinuousDomain.getClassicState(true);

                    if (true) {
                        // run experiment

//                System.out.println("state original: " + s.toString());

                        String actionTopic = "/mobile_base/commands/velocity"; //set this to the appropriate topic for your robot!
                        String actionMsg = "geometry_msgs/Twist";

                        //3.28084

                        //define the relevant twist messages that we'll use for our actions
                        Twist fTwist = new Twist(new Vector3(0.1, 0, 0.), new Vector3()); //forward
                        Twist bTwist = new Twist(new Vector3(-0.1, 0, 0.), new Vector3()); //backward
                        Twist rTwist = new Twist(new Vector3(), new Vector3(0, 0, -0.5)); //clockwise rotate
                        Twist rccwTwist = new Twist(new Vector3(), new Vector3(0, 0, 0.5)); //counter-clockwise rotate


                        int period = 100; //publish every 500 milliseconds...
                        int nPublishes = 1; //...for 5 times for each action execution...
                        boolean sync = true; //...and use synchronized action execution
                        tb_env.setActionPublisher(CleanupTurtleBotL0Domain.ACTION_MOVE_FORWARD, new RepeatingActionPublisher(actionTopic, actionMsg, tb_env.getRosBridge(), fTwist, period, nPublishes, sync));
                        tb_env.setActionPublisher(CleanupTurtleBotL0Domain.ACTION_MOVE_BACK, new RepeatingActionPublisher(actionTopic, actionMsg, tb_env.getRosBridge(), bTwist, period, nPublishes, sync));
                        tb_env.setActionPublisher(CleanupTurtleBotL0Domain.ACTION_TURN_CW, new RepeatingActionPublisher(actionTopic, actionMsg, tb_env.getRosBridge(), rTwist, period, nPublishes, sync));
                        tb_env.setActionPublisher(CleanupTurtleBotL0Domain.ACTION_TURN_CCW, new RepeatingActionPublisher(actionTopic, actionMsg, tb_env.getRosBridge(), rccwTwist, period, nPublishes, sync));


                        agent.recordTestEpisodes(10, "amdp/out/record.episode");
                        Episode e = agent.actUntilTermination(tb_env, 1000);
                        List<Episode> episodes = new ArrayList<>();
                        episodes.add(e);
                        Episode.writeEpisodes(episodes, "amdp/out", "episode");

                        Visualizer v = CleanupVisualiser.getVisualizer("amdp/data/resources/robotImages");


                        //		System.out.println(ea.getState(0).toString());
                        new EpisodeSequenceVisualizer(v, domain, Arrays.asList(e));
                        System.out.println("steps:" + e.numTimeSteps());


//                Visualizer v1 = CleanupContinuousVisualiser.getVisualizer("amdp/data/resources/robotImages");


                        //		System.out.println(ea.getState(0).toString());
//                new EpisodeSequenceVisualizer(v1, domain, "amdp/out/");


                    } else {
                        // run previous episode!
                        Visualizer v = CleanupVisualiser.getVisualizer("amdp/data/resources/robotImages");

                        new EpisodeSequenceVisualizer(v, domain, "amdp/out/");
                    }
                }
            }

            //TODO: remove this brace when not needed!
        }


    }


//    if(false){
//        // now to test the base level planner:
//
//
//        GroundedPropSC l0sc = new GroundedPropSC(gp);
//        RewardFunction heuristicRF = new PullCostGoalRF(l0sc, 1., 0.);
//        GoalBasedRF l0rf = new GoalBasedRF(l0sc, 1., 0.);
//        GoalConditionTF l0tf = new GoalConditionTF(l0sc);
//
//        CleanupTurtleBotL0Domain dgen = new CleanupTurtleBotL0Domain(l0rf,l0tf, rand);
//        dgen.includeDirectionAttribute(true);
////                dgen.includePullAction(true);
//        dgen.includeWallPF_s(true);
//        dgen.includeLockableDoors(true);
//        dgen.setLockProbability(lockProb);
//        OOSADomain domain = dgen.generateDomain();
//    }


//        }
//}



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

            brtdp.setMaxRolloutDepth(200);
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
            brtdp.setMaxRolloutDepth(200);
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
            brtdp.setMaxRolloutDepth(500);
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
            brtdp.setMaxRolloutDepth(500);
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

            ValueFunction heuristic = getL0Heuristic(s, gt.rewardFunction(), lockProb);
            BoundedRTDPForTests brtd = new BoundedRTDPForTests(l0, this.discount,
                    new SimpleHashableStateFactory(false),
                    new ConstantValueFunction(0.),
                    heuristic,
//                    new ConstantValueFunction(1.),
                    0.1, -1);
            brtd.setRemainingNumberOfBellmanUpdates(new MutableGlobalInteger(-1));
            brtd.setMaxRolloutDepth(500);
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

            ValueFunction heuristic = getL0Heuristic(s, gt.rewardFunction(), lockProb);
            BoundedRTDPForTests brtd = new BoundedRTDPForTests(l0, this.discount,
                    new SimpleHashableStateFactory(false),
                    new ConstantValueFunction(0.),
//                    new ConstantValueFunction(1.),
                    heuristic,
                    0.1, -1);
            brtd.setRemainingNumberOfBellmanUpdates(new MutableGlobalInteger(-1));
            brtd.setMaxRolloutDepth(500);
            brtd.toggleDebugPrinting(false);
            brtdpList.add(brtd);
            brtd.planFromState(s);
            return brtd;
        }

    }


    public static ValueFunction getL0Heuristic(State s, RewardFunction rf, double lockProb){

        double discount = 0.99;
        // prop name if block -> block and room if
        GroundedPropSC rfCondition = (GroundedPropSC)((PullCostGoalRF)rf).getGoalCondition();
        String PFName = rfCondition.gp.pf.getName();
        String[] params = rfCondition.gp.params;
        System.out.println("pf name: " + PFName);
        if(PFName.equals(CleanupTurtleBotL0Domain.PF_AGENT_IN_ROOM)){
            return new AgentToRegionHeuristic(params[1], discount, lockProb);
        }
        else if(PFName.equals(CleanupTurtleBotL0Domain.PF_AGENT_IN_DOOR)){
            return new AgentToRegionHeuristic(params[1], discount, lockProb);
        }
        else if(PFName.equals(CleanupTurtleBotL0Domain.PF_BLOCK_IN_ROOM)){
            return new BlockToRegionHeuristic(params[0], params[1], discount, lockProb);
        }
        else if(PFName.equals(CleanupTurtleBotL0Domain.PF_BLOCK_IN_DOOR)){
            return new BlockToRegionHeuristic(params[0], params[1], discount, lockProb);
        }
        else if(PFName.equals(CleanupTurtleBotL0Domain.PF_BLOCK_IN_DOOR_AGENT_IN_DOOR)||PFName.equals(CleanupTurtleBotL0Domain.PF_BLOCK_IN_DOOR_AGENT_IN_ROOM)||
                PFName.equals(CleanupTurtleBotL0Domain.PF_BLOCK_IN_ROOM_AGENT_IN_DOOR)||PFName.equals(CleanupTurtleBotL0Domain.PF_BLOCK_IN_ROOM_AGENT_IN_ROOM)||
                PFName.equals(CleanupTurtleBotL0Domain.PF_GO_NORTH)||PFName.equals(CleanupTurtleBotL0Domain.PF_GO_EAST)||
                PFName.equals(CleanupTurtleBotL0Domain.PF_GO_WEST)||PFName.equals(CleanupTurtleBotL0Domain.PF_GO_SOUTH)){
            return new ConstantValueFunction(1.);
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
            if(region.className().equals(CleanupTurtleBotL0Domain.CLASS_DOOR)){
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


            int l = (Integer) region.get(CleanupTurtleBotL0Domain.VAR_LEFT);
            int r = (Integer)region.get(CleanupTurtleBotL0Domain.VAR_RIGHT);
            int b = (Integer)region.get(CleanupTurtleBotL0Domain.VAR_BOTTOM);
            int t = (Integer)region.get(CleanupTurtleBotL0Domain.VAR_TOP);

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
            if(region.className().equals(CleanupTurtleBotL0Domain.CLASS_DOOR)){
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
            int ax = (Integer) agent.get(CleanupTurtleBotL0Domain.VAR_X);
            int ay = (Integer) agent.get(CleanupTurtleBotL0Domain.VAR_Y);


            int l = (Integer) region.get(CleanupTurtleBotL0Domain.VAR_LEFT);
            int r = (Integer) region.get(CleanupTurtleBotL0Domain.VAR_RIGHT);
            int b = (Integer) region.get(CleanupTurtleBotL0Domain.VAR_BOTTOM);
            int t = (Integer) region.get(CleanupTurtleBotL0Domain.VAR_TOP);

            //get the block
            ObjectInstance block = ((CleanupState)s).object(this.blockName);
            int bx = (Integer) block.get(CleanupTurtleBotL0Domain.VAR_X);
            int by = (Integer) block.get(CleanupTurtleBotL0Domain.VAR_Y);

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
                if(this.blocks.get(i).toLowerCase().contains("agent")){
                    CleanupAgentL2 b = (CleanupAgentL2)((CleanupL2State)s).object(this.blocks.get(i));
                    String r = this.rooms.get(i);
                    if(!b.inRegion.equals(r)){
                        return false;
                    }
                }
                else{
                    CleanupBlockL2 b = (CleanupBlockL2)((CleanupL2State)s).object(this.blocks.get(i));
                    String r = this.rooms.get(i);
                    if(!b.inRegion.equals(r)){
                        return false;
                    }
                }

            }

            return true;
        }
    }


    public static class L1Goal implements StateConditionTest{

        List<String> blocks;
        List<String> rooms;


        public L1Goal(List<String> blocks, List<String> rooms) {
            this.blocks = blocks;
            this.rooms = rooms;
        }

        @Override
        public boolean satisfies(State s) {

            for(int i = 0; i < this.blocks.size(); i++){
                if(this.blocks.get(i).toLowerCase().contains("agent")){
                    CleanupAgentL1 b = (CleanupAgentL1)((CleanupL1State)s).object(this.blocks.get(i));
                    String r = this.rooms.get(i);
                    if(!b.inRegion.equals(r)){
                        return false;
                    }
                }
                else{
                    CleanupBlockL1 b = (CleanupBlockL1)((CleanupL1State)s).object(this.blocks.get(i));
                    String r = this.rooms.get(i);
                    if(!b.inRegion.equals(r)){
                        return false;
                    }
                }

            }

            return true;
        }
    }

    public static class PF_TestFinalL0Goal extends PropositionalFunction {

        protected GroundedProp gp;

        public PF_TestFinalL0Goal(String name, String [] params, GroundedProp gp){
            super(name, params);
            this.gp = gp;
        }
        @Override
        public boolean isTrue(OOState s, String... params) {
            OOState sL1 = new CleanupContinuousToDiscreteStateMapper().mapState(s);
            return gp.isTrue(sL1);
        }

    }



}
