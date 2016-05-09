package amdp.performancetestcode;


import burlap.behavior.policy.Policy;
import burlap.behavior.singleagent.EpisodeAnalysis;
import burlap.behavior.singleagent.auxiliary.EpisodeSequenceVisualizer;
import burlap.behavior.singleagent.auxiliary.StateReachability;
import burlap.behavior.singleagent.options.DeterministicTerminationOption;
import burlap.behavior.singleagent.options.Option;
import burlap.behavior.singleagent.options.support.OptionEvaluatingRF;
import burlap.behavior.valuefunction.ValueFunctionInitialization;
import burlap.debugtools.DPrint;
import burlap.debugtools.RandomFactory;
import burlap.oomdp.auxiliary.common.GoalConditionTF;
import burlap.oomdp.auxiliary.stateconditiontest.StateConditionTest;
import burlap.oomdp.core.Domain;
import burlap.oomdp.core.TerminalFunction;
import burlap.oomdp.core.TransitionProbability;
import burlap.oomdp.core.objects.ObjectInstance;
import burlap.oomdp.core.states.State;
import burlap.oomdp.singleagent.Action;
import burlap.oomdp.singleagent.GroundedAction;
import burlap.oomdp.singleagent.RewardFunction;
import burlap.oomdp.singleagent.common.GoalBasedRF;
import burlap.oomdp.singleagent.environment.SimulatedEnvironment;
import burlap.oomdp.statehashing.HashableState;
import burlap.oomdp.statehashing.SimpleHashableStateFactory;
import burlap.oomdp.visualizer.Visualizer;

import amdp.taxi.TaxiDomain;
import amdp.taxi.TaxiVisualizer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;


/**
 * Created by ngopalan on 4/15/16.
 */
public class OptionsForTaxi {

    private static Domain d;
    private static double discount = 0.99;
    private MutableGlobalInteger bellmanBudget;
    static List<BoundedRTDPForTests> plannerList = new ArrayList<>();

    public OptionsForTaxi(Domain d, MutableGlobalInteger bellmanUpdate){
        this.d = d;
        bellmanBudget = bellmanUpdate;
    }

    public void setDiscount(double disc){
        discount = disc;
    }


    public static Option navigateLocation(String optionName, Domain domain, final int locationNumber, MutableGlobalInteger bellmanUpdateIn){

        final StateConditionTest initiationConditions = new StateConditionTest() {
            @Override
            // here the passenger is not in the taxi and the taxi is empty
            public boolean satisfies(State s) {
                ObjectInstance taxi = s.getFirstObjectOfClass(TaxiDomain.TAXICLASS);
                int xTaxi = taxi.getIntValForAttribute(TaxiDomain.XATT);
                int yTaxi = taxi.getIntValForAttribute(TaxiDomain.YATT);

                List<ObjectInstance> locations = s.getObjectsOfClass(TaxiDomain.LOCATIONCLASS);
                ObjectInstance location = locations.get(locationNumber);

                int xLoc = location.getIntValForAttribute(TaxiDomain.XATT);
                int yLoc = location.getIntValForAttribute(TaxiDomain.YATT);

                boolean taxiAtLoc = xTaxi==xLoc && yTaxi==yLoc;
                return !taxiAtLoc;
            }
        };

        final StateConditionTest goalCondition = new StateConditionTest() {
            @Override
            // here the passenger is not in the taxi and the taxi is empty
            public boolean satisfies(State s) {
                ObjectInstance taxi = s.getFirstObjectOfClass(TaxiDomain.TAXICLASS);
                int xTaxi = taxi.getIntValForAttribute(TaxiDomain.XATT);
                int yTaxi = taxi.getIntValForAttribute(TaxiDomain.YATT);

                List<ObjectInstance> locations = s.getObjectsOfClass(TaxiDomain.LOCATIONCLASS);
                ObjectInstance location = locations.get(locationNumber);

                int xLoc = location.getIntValForAttribute(TaxiDomain.XATT);
                int yLoc = location.getIntValForAttribute(TaxiDomain.YATT);

                boolean taxiAtLoc = xTaxi==xLoc && yTaxi==yLoc;
                return taxiAtLoc;
            }
        };

        RewardFunction rf = new TaxiNavigateOptionRewardFunction(goalCondition);
        TerminalFunction tf = new GoalConditionTF(goalCondition);


        BoundedRTDPForTests brtdp = new BoundedRTDPForTests(domain, rf, tf, discount, new SimpleHashableStateFactory(false),
                new ValueFunctionInitialization.ConstantValueFunctionInitialization(0.),
                new ValueFunctionInitialization.ConstantValueFunctionInitialization(1.),
                0.001,
                -1);//10
        brtdp.setRemainingNumberOfBellmanUpdates(bellmanUpdateIn);
//        plannerList.add(brtdp);

        brtdp.setMaxRolloutDepth(15);//10
        brtdp.toggleDebugPrinting(false);



        Policy optionPolicy = new GreedyReplan(brtdp);
        plannerList.add(brtdp);

        //now that we have the parts of our option, instantiate it
        DeterministicTerminationOption option = new DeterministicTerminationOption(optionName, optionPolicy, initiationConditions, goalCondition);
        option.setExpectationHashingFactory(new SimpleHashableStateFactory(false));

        return option;

    }



    public static void main(String[] args){

//        RandomFactory.seedMapped(0,123);

        MutableGlobalInteger mgi = new MutableGlobalInteger();

        DPrint.toggleCode(3214986, false);


        boolean onlyOptions = true;


        for(int i =0;i<args.length;i++){
            String str = args[i];
            if(str.equals("-r")){
                onlyOptions = Boolean.parseBoolean(args[i+1]);
            }
            if(str.equals("-b")){
                mgi.setValue(Integer.parseInt(args[i+1]));
            }
        }


        TaxiDomain taxiDomainGen = new TaxiDomain();
        taxiDomainGen.includeFuel = false;
        Domain taxiDomain = taxiDomainGen.generateDomain();
        State s = TaxiDomain.getComplexState(taxiDomain);


        List<String> passengers  = new ArrayList<>();
        passengers.add("passenger0");
        passengers.add("passenger1");

        List<ObjectInstance> locations = s.getObjectsOfClass(TaxiDomain.LOCATIONCLASS);

        StateConditionTest l0sc = new TaxiDomainDriverForResults.L0Goal(passengers, taxiDomain.getPropFunction(TaxiDomain.PASSENGERATGOALLOCATIONPF));
        RewardFunction rfl0 = new GoalBasedRF(l0sc, 1.);
        TerminalFunction tfl0 = new GoalConditionTF(l0sc);

        double discount = 0.99;
        SimpleHashableStateFactory shf = new SimpleHashableStateFactory(false);

//        TaxiOptions options = new TaxiOptions(taxiDomain,new MutableGlobalInteger(-1));


//        final Option pickPassenger1 = pickPassenger1Option("pickPass1", taxiDomain,mgi);
//        final Option pickPassenger2 = pickPassenger2Option("pickPass2", taxiDomain,mgi);
//        final Option dropPassenger1 = dropPassenger1Option("dropPass1", taxiDomain,mgi);
//        final Option dropPassenger2 = dropPassenger2Option("dropPass2", taxiDomain,mgi);

        List<Option> navigateOptionList = new ArrayList<>();
        for(int i =0;i<locations.size();i++){
            String locationName = " navigate_location" + i;
            navigateOptionList.add(navigateLocation(locationName, taxiDomain, i, mgi));
        }



//
//
//        for(Option oTest : navigateOptionList) {
////        Option oTest = navigateOptionList.get(0);
//            oTest.keepTrackOfRewardWith(rfl0, discount);
//        }
////        State sNext  =  oTest.performAction(s,oTest.getAssociatedGroundedAction());
////        List<TransitionProbability> tpList  = oTest.getTransitions(s,oTest.getAssociatedGroundedAction());
////        for(TransitionProbability tp:tpList){
////            System.out.println(tp.p);
////        }
////        System.out.println(tpList.size());
////        System.out.println(navigateOptionList.size());
//        EpisodeAnalysis eaTest = new EpisodeAnalysis(s);
//
//
//        List<Action> actionListTemp = new ArrayList<Action>(navigateOptionList);
//        actionListTemp.add(taxiDomain.getAction(TaxiDomain.PICKUPACTION));
//        actionListTemp.add(taxiDomain.getAction(TaxiDomain.DROPOFFACTION));
//
//        Set<HashableState> haList  =StateReachabilityTest.getReachableHashedStates(s, actionListTemp,new SimpleHashableStateFactory(), tfl0);
//        System.out.println("list states: "+haList.size());
//
//        for(HashableState hs:haList){
//            eaTest.recordTransitionTo(taxiDomain.getAction(TaxiDomain.DROPOFFACTION).getGroundedAction(), hs.s,0);
//        }
//
////        ea.addState(s);
////        ea.addState(sNext);
//
////        eaTest.recordTransitionTo(sNext, oTest.getAssociatedGroundedAction(), 0);
//        Visualizer v = TaxiVisualizer.getVisualizer(5, 5);
//        new EpisodeSequenceVisualizer(v, taxiDomain, Arrays.asList(eaTest));
//

        if(true) {

//        GreedyQPolicy p1= (GreedyQPolicy ) pickPassenger1.getPolicy();
//        BoundedRTDPForTests brtp = (BoundedRTDPForTests)p1.getQplanner();
//        brtp.planFromState(s);

//        List<QValue> qValues = brtp.getQs(s);
//
//        for(int i=0;i<qValues.size();i++){
//            System.out.println("Qvalue: " + qValues.get(i).q + ", action name: "+qValues.get(i).a.actionName() );
//        }

//        Option o1 = navigateOptionList.get(3);
//        State sNew = o1.performAction(s, o1.getAssociatedGroundedAction());
//        System.out.println(plannerList.get(3).getNumberOfBellmanUpdates());
//        System.out.println(plannerList.get(3).remainingBellmanUpdates.getValue());
////
//        System.out.println(sNew.getCompleteStateDescription());


            BoundedRTDPForTests brtdp;
//

            if (onlyOptions) {
                for (Option o : navigateOptionList) {
                    o.keepTrackOfRewardWith(rfl0, discount);
                    o.setExernalTermination(tfl0);
                }

                brtdp = new BoundedRTDPForTests(taxiDomain, new OptionEvaluatingRF(rfl0), tfl0, discount, shf,
                        new ValueFunctionInitialization.ConstantValueFunctionInitialization(0.),
                        new ValueFunctionInitialization.ConstantValueFunctionInitialization(1.),
                        0.001,
                        -1);
                brtdp.setMaxRolloutDepth(25);

                brtdp.setRemainingNumberOfBellmanUpdates(mgi);
                brtdp.toggleDebugPrinting(false);
                plannerList.add(brtdp);

                List<Action> actionList = new ArrayList<Action>(navigateOptionList);
                actionList.add(taxiDomain.getAction(TaxiDomain.PICKUPACTION));
                actionList.add(taxiDomain.getAction(TaxiDomain.DROPOFFACTION));
                brtdp.setActions(actionList);
            } else {

                brtdp = new BoundedRTDPForTests(taxiDomain, rfl0, tfl0, discount, shf,
                        new ValueFunctionInitialization.ConstantValueFunctionInitialization(0.),
                        new ValueFunctionInitialization.ConstantValueFunctionInitialization(1.),
                        0.001,
                        -1);
                brtdp.setMaxRolloutDepth(25);
                brtdp.setRemainingNumberOfBellmanUpdates(mgi);
                for (Option o : navigateOptionList) {
                    brtdp.addNonDomainReferencedAction(o);
                }
                plannerList.add(brtdp);
            }


//
            Policy p = brtdp.planFromState(s);


            SimulatedEnvironment env = new SimulatedEnvironment(taxiDomain, rfl0, tfl0, s);
            EpisodeAnalysis ea = p.evaluateBehavior(env, 101);

//        Visualizer v = TaxiVisualizer.getVisualizer(5, 5);
//        new EpisodeSequenceVisualizer(v, taxiDomain, Arrays.asList(ea));
//
//
            int sum = 0;
            for (BoundedRTDPForTests b : plannerList) {
                sum += b.getNumberOfBellmanUpdates();
            }
            System.out.println(sum);
            System.out.println(ea.numTimeSteps() - 1);
            if (ea.numTimeSteps() - 1 <= 100) {
                System.out.println(1);
            } else {
                System.out.println(0);
            }
////        System.out.println(bellmanBudgetStartValue);

            if (onlyOptions) {
                System.out.println("Taxi With Options alone!!");
            } else {
                System.out.println("Taxi With Options and base actions");
            }

//        System.out.println(ea.stateSequence.size());
//        System.out.println(ea.actionSequence.size());


            for (BoundedRTDPForTests b : plannerList) {
                System.out.println("planner update: " + b.getNumberOfBellmanUpdates());
            }

            System.out.println("top planner update: " + brtdp.getNumberOfBellmanUpdates());

        }



    }


}


