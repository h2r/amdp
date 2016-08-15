package amdp.taxiamdpdomains.taxiamdplevel1;

import amdp.taxi.TaxiDomain;
import amdp.taxi.TaxiRewardFunction;
import amdp.taxi.TaxiTerminationFunction;
import amdp.taxiamdpdomains.taxiamdplevel1.taxil1state.*;
import burlap.behavior.policy.Policy;
import burlap.behavior.policy.PolicyUtils;
import burlap.behavior.singleagent.Episode;
import burlap.behavior.singleagent.planning.stochastic.rtdp.BoundedRTDP;
import burlap.behavior.valuefunction.ConstantValueFunction;
import burlap.debugtools.RandomFactory;
import burlap.mdp.auxiliary.DomainGenerator;
import burlap.mdp.auxiliary.common.NullTermination;
import burlap.mdp.core.Domain;
import burlap.mdp.core.StateTransitionProb;
import burlap.mdp.core.TerminalFunction;
import burlap.mdp.core.action.Action;
import burlap.mdp.core.action.ActionType;
import burlap.mdp.core.action.UniversalActionType;
import burlap.mdp.core.oo.OODomain;
import burlap.mdp.core.oo.propositional.PropositionalFunction;
import burlap.mdp.core.oo.state.OOState;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.common.UniformCostRF;
import burlap.mdp.singleagent.environment.EnvironmentOutcome;
import burlap.mdp.singleagent.model.FactoredModel;
import burlap.mdp.singleagent.model.RewardFunction;
import burlap.mdp.singleagent.model.statemodel.FullStateModel;
import burlap.mdp.singleagent.oo.OOSADomain;
import burlap.statehashing.simple.SimpleHashableStateFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static amdp.taxi.TaxiDomain.TAXICLASS;

/**
 * Created by ngopalan on 8/10/16.
 */
public class TaxiL1Domain implements DomainGenerator {

    public static final String								VAR_INTAXI = "inTaxiAtt";
    public static final String								VAR_OCCUPIEDTAXI = "occupiedTaxiAtt";

    public static final String								VAR_PICKEDUPATLEASTONCE = "pickedUpAtLeastOnce";

    // this is the current location attribute
    public static final String								VAR_LOCATION = "locationAtt";
    public static final String								VAR_CURRENTLOCATION = "locationAtt";
    public static final String								VAR_GOALLOCATION = "goalLocationAtt";
    public static final String								VAR_ORIGINALSOURCELOCATION = "originalSourceLocationAtt";

    public static final String								VAR_JUSTPICKEDUP = "justPickedupAtt";

//
//    public static final String								TAXIL1CLASS = "taxiL1";
//    public static final String								LOCATIONL1CLASS = "locationL1";
//    public static final String								PASSENGERL1CLASS = "passengerL1";


    public static final String								TAXIL1CLASS = "taxi";
    public static final String								LOCATIONL1CLASS = "location";
    public static final String								PASSENGERL1CLASS = "passenger";


    public static final String								ACTION_NAVIGATE = "navigate";
    public static final String								ACTION_PICKUPL1 = "pickupL1";
    public static final String								ACTION_PUTDOWNL1 = "putdownL1";

    public static final String                              ON_ROAD = "on_road";
    public static final String                              RED = "red";
    public static final String                              GREEN = "green";
    public static final String                              BLUE = "blue";
    public static final String                              YELLOW = "yellow";
    public static final String                              DARKGREY = "darkgrey";
    public static final String                              MAGENTA = "magenta";
    public static final String                              PINK = "pink";
    public static final String                              ORANGE = "orange";
    public static final String                              CYAN = "cyan";
    public static final String                              FUEL = "fuel";



    public static final String								PASSENGERATLOCATIONPF = "passengerAt";
    public static final String								TAXIATPASSENGERPF = "taxiAtPassenger";
    public static final String								PASSENGERATGOALLOCATIONPF = "passengerAtGoal";

    protected RandomFactory randomFactory = new RandomFactory();
    protected Random rand;


    protected RewardFunction rf;
    protected TerminalFunction tf;


    public TaxiL1Domain(RewardFunction rf, TerminalFunction tf, Random rand) {
        this.rf = rf;
        this.tf = tf;
        this.rand = rand;
    }

    public TaxiL1Domain(RewardFunction rf, TerminalFunction tf) {
        this.rf = rf;
        this.tf = tf;
        this.rand = this.randomFactory.ingetOrSeedMapped(0,0);
    }

    @Override
    public OOSADomain generateDomain() {
        OOSADomain domain = new OOSADomain();


        RewardFunction rf = this.rf;
        TerminalFunction tf = this.tf;

        if(rf == null){
            rf = new UniformCostRF();
        }
        if(tf == null){
            tf = new NullTermination();
        }

        domain.addStateClass(PASSENGERL1CLASS, TaxiL1Passenger.class).addStateClass(TAXIL1CLASS, TaxiL1Agent.class)
                .addStateClass(LOCATIONL1CLASS, TaxiL1Location.class);

        // create model
        TaxiL1Model tmodel = new TaxiL1Model(rand);

        FactoredModel model = new FactoredModel(tmodel, rf, tf);
        domain.setModel(model);

        domain.addActionTypes(
                new UniversalActionType(ACTION_PUTDOWNL1),
                new UniversalActionType(ACTION_PICKUPL1),
                new NavigateType());



        OODomain.Helper.addPfsToDomain(domain, this.generatePfs(domain));

        return domain;
    }

    private List<PropositionalFunction> generatePfs(OOSADomain domain) {
        List<PropositionalFunction> pfs = new ArrayList<PropositionalFunction>();
        pfs.add(new PF_PassengerInGoalLocation(PASSENGERATGOALLOCATIONPF,domain, new String[]{PASSENGERL1CLASS}));
        pfs.add(new PF_PassengerInLocation(PASSENGERATLOCATIONPF,domain, new String[]{PASSENGERL1CLASS, LOCATIONL1CLASS}));
        pfs.add(new PF_PassengerInTaxi(TAXIATPASSENGERPF,domain, new String[]{PASSENGERL1CLASS}));

        return pfs;
    }


    public static class TaxiL1Model implements FullStateModel {

        protected Random rand;

        public TaxiL1Model(Random rand){
            this.rand = rand;
        }

        @Override
        public List<StateTransitionProb> stateTransitions(State s, Action a) {
            int actionInd = actionInd(a.actionName().split("_")[0]);

            List <StateTransitionProb> transitions = new ArrayList<StateTransitionProb>();
            if(actionInd==0){
                // navigate action!
                navigateAction(s,a,transitions);
            }
            else if(actionInd==1){
                // pick up
                pickupAction(s,transitions);
            }
            else if(actionInd==2){
                // putdown
                putDownAction(s,transitions);
            }
            return transitions;
        }


        @Override
        public State sample(State s, Action a) {
            List<StateTransitionProb> stpList = this.stateTransitions(s,a);
            double roll = rand.nextDouble();
            double curSum = 0.;
            for(int i = 0; i < stpList.size(); i++){
                curSum += stpList.get(i).p;
                if(roll < curSum){
                    return stpList.get(i).s;
                }
            }
            throw new RuntimeException("Probabilities don't sum to 1.0: " + curSum);
        }

        private void putDownAction(State s, List<StateTransitionProb> transitions) {
            TaxiL1State ns = (TaxiL1State)s.copy();
            TaxiL1Agent taxi = ns.touchTaxi();
            List<TaxiL1Passenger> passengers = ns.passengers;
            for(TaxiL1Passenger p : passengers){
                boolean in = p.inTaxi;
                if(in){
                    TaxiL1Passenger np = ns.touchPassenger(p.name());
                    np.inTaxi  = false;
                    taxi.taxiOccupied = false;
//                    p.setValue(LOCATIONATT, taxi.getStringValForAttribute(LOCATIONATT));
                    break;
                }

            }

            transitions.add(new StateTransitionProb(ns,1.));

        }

        private void pickupAction(State s,  List<StateTransitionProb> transitions) {
            TaxiL1State ns = (TaxiL1State)s.copy();
            TaxiL1Agent taxi = ns.touchTaxi();
            String taxiLocation = taxi.currentLocation;
            List<TaxiL1Passenger> passengers = ns.passengers;

            for(TaxiL1Passenger p : passengers){
                String pLoc = p.currentLocation;

                if(taxiLocation.equals(pLoc) && !taxi.taxiOccupied){
                    TaxiL1Passenger np = ns.touchPassenger(p.name());
                    np.inTaxi = true;
                    np.pickUpOnce = true;
                    taxi.taxiOccupied = true;
                    break;
                }

            }

            transitions.add(new StateTransitionProb(ns,1.));

        }

        protected void navigateAction(State s, Action a, List<StateTransitionProb> stateTransitions){
            TaxiL1State ns = (TaxiL1State)s.copy();
            TaxiL1Agent taxi = ns.touchTaxi();
            String location = ((NavigateType.NavigateAction)a).location;

            taxi.currentLocation = location;
            boolean taxiOccupied = taxi.taxiOccupied;

            if(taxiOccupied ){
                List<TaxiL1Passenger> passengers = ns.passengers;
                for(TaxiL1Passenger p : passengers){
                    boolean in = p.inTaxi;
                    if(in){
//                    int passID = ns.passengerInd(((TaxiL1Passenger)p).name());
                        TaxiL1Passenger np = ns.touchPassenger(p.name());
                        np.currentLocation = location;
                        break;
                    }

                }

            }
            stateTransitions.add(new StateTransitionProb(ns,1.));
        }


        protected int actionInd(String name){
            if(name.equals(ACTION_NAVIGATE)){
                return 0;
            }
            else if(name.equals(ACTION_PICKUPL1)){
                return 1;
            }
            else if(name.equals(ACTION_PUTDOWNL1)){
                return 2;
            }
            throw new RuntimeException("Unknown action " + name);
        }
    }



    /**
     * Describes the navigate action at level 1 of the AMDP
     */
    public static class NavigateType implements ActionType {


        public NavigateType() {
//            actions = new ArrayList<Action>(locations.size());
//            for(String location : locations){
//                actions.add(new NavigateAction(location));
//            }
        }

        @Override
        public String typeName() {
            return ACTION_NAVIGATE;
        }

        @Override
        public Action associatedAction(String strRep) {
            return new NavigateAction(strRep);
        }

        @Override
        public List<Action> allApplicableActions(State s) {
            List<Action> actions = new ArrayList<Action>();
            List<TaxiL1Location> locations = ((TaxiL1State)s).locations;
            for(TaxiL1Location location: locations){
                if(!location.colour.equals(ON_ROAD)){
                    actions.add(new NavigateAction(location.colour));
                }
            }
            return actions;
        }

        public static class NavigateAction implements Action{

            public String location;

            public NavigateAction(String location) {
                this.location= location;
            }

            @Override
            public String actionName() {
                return ACTION_NAVIGATE + "_" + location;
            }

            @Override
            public Action copy() {
                return new NavigateAction(location);
            }

            @Override
            public boolean equals(Object o) {
                if(this == o) return true;
                if(o == null || getClass() != o.getClass()) return false;

                NavigateAction that = (NavigateAction) o;

                return that.location.equals(location) ;

            }

            @Override
            public int hashCode() {
                String str = ACTION_NAVIGATE + "_" + location;
                return str.hashCode();
            }

            @Override
            public String toString() {
                return this.actionName();
            }
        }
    }


    /*
    Propositional functions for L2
     */
    public class PF_PassengerInTaxi extends PropositionalFunction {

        public PF_PassengerInTaxi(String name, Domain domain, String [] params){
            super(name, params);
        }

        @Override
        public boolean isTrue(OOState s, String... params) {
            TaxiL1State ns = ((TaxiL1State)s).copy();
            TaxiL1Agent taxi = ns.taxi;
            String tLocation = taxi.currentLocation;
            boolean taxiOccupied = taxi.taxiOccupied;

            // params here are the name of a location like Location 1

            boolean returnValue = false;
            TaxiL1Passenger passenger = ns.touchPassenger(params[0]);
            String pLocation = passenger.currentLocation;
            boolean inTaxi = passenger.inTaxi;
            if(tLocation.equals(pLocation) && inTaxi && taxiOccupied ){
                returnValue = true;
            }

            return returnValue;
        }
    }


    public class PF_PassengerInLocation extends PropositionalFunction{

        public PF_PassengerInLocation(String name, Domain domain, String [] params){
            super(name, params);
        }

        @Override
        public boolean isTrue(OOState s, String[] params) {
            //			ObjectInstance taxi = s.getFirstObjectOfClass(TAXICLASS);
            //			boolean taxiOccupied = taxi.getBooleanValForAttribute(OCCUPIEDTAXIATT);

            TaxiL1State ns = ((TaxiL1State)s).copy();
//
//            int i = ns.locationIndWithColour(params[1]);
//            TaxiL1Location location = ns.touchLocation(i);
//            int lLocation = location.getIntValForAttribute(LOCATIONATT);

            // params here are the name of a location like Location 1

            boolean returnValue = false;
            TaxiL1Passenger passenger = ns.touchPassenger(params[0]);
            String pLocation = passenger.currentLocation;
            boolean inTaxi = passenger.inTaxi;
            if(params[1].equals(pLocation) && !inTaxi){
                returnValue = true;
            }else if(!params[1].equals(passenger.goalLocation) && inTaxi){
                returnValue = true;
            }

            return returnValue;
        }
    }


    public class PF_PassengerInGoalLocation extends PropositionalFunction{

        public PF_PassengerInGoalLocation(String name, Domain domain, String [] params){
            super(name, params);
        }

        @Override
        public boolean isTrue(OOState s, String[] params) {
            //			ObjectInstance taxi = s.getFirstObjectOfClass(TAXICLASS);
            //			boolean taxiOccupied = taxi.getBooleanValForAttribute(OCCUPIEDTAXIATT);

            TaxiL1State ns = ((TaxiL1State)s).copy();
//
//            int i = ns.locationIndWithColour(params[1]);
//            TaxiL1Location location = ns.touchLocation(i);
//            int lLocation = location.getIntValForAttribute(LOCATIONATT);

            // params here are the name of a location like Location 1

            boolean returnValue = false;
            TaxiL1Passenger passenger = ns.touchPassenger(params[0]);


            String pLocation = passenger.currentLocation;
            String gLocation = passenger.goalLocation;
            boolean inTaxi = passenger.inTaxi;
            if(gLocation.equals(pLocation) && !inTaxi){
                returnValue = true;
            }

            return returnValue;
        }
    }



    public static void main(String[] args) {
        RandomFactory randomFactory = new RandomFactory();
        Random rand = randomFactory.getMapped(0);



        TerminalFunction tf = new TaxiTerminationFunction();
        RewardFunction rf = new TaxiRewardFunction(1,tf);

        TaxiDomain tdGen = new TaxiDomain(rf,tf);



//        tdGen.setDeterministicTransitionDynamics();
        tdGen.setTransitionDynamicsLikeFickleTaxiProlem();
//        tdGen.setDeterministicTransitionDynamics();
        tdGen.setFickleTaxi(true);
        tdGen.setIncludeFuel(false);


        OOSADomain td = tdGen.generateDomain();


        State startState = TaxiDomain.getRandomClassicState(rand, td, false);

//        State startState = TaxiDomain.getClassicState(td, false);

        L1StateMapper testMapper = new L1StateMapper();

        State sL1 = testMapper.mapState(startState);

        TerminalFunction tfL1 = new TaxiL1TerminalFunction();
        RewardFunction rfL1 = new UniformCostRF();

        TaxiL1Domain tdL1Gen = new TaxiL1Domain(rfL1, tfL1, rand);

        OOSADomain tdL1 = tdL1Gen.generateDomain();

//        System.out.println(sL1.toString());

        double discount = 0.99;
        SimpleHashableStateFactory shf = new SimpleHashableStateFactory(false);

//        ActionType nav = tdL1.getAction(ACTION_NAVIGATE);
//        ActionType pickupAT = tdL1.getAction(ACTION_PICKUPL1);
//        ActionType putdownAT = tdL1.getAction(ACTION_PUTDOWNL1);
//
//        Action nav_blue = nav.associatedAction("blue");
//        Action nav_red = nav.associatedAction("red");
//        Action pickup = pickupAT.associatedAction(ACTION_PICKUPL1);
//        Action putdown = putdownAT.associatedAction(ACTION_PUTDOWNL1);
//
//        EnvironmentOutcome eo = tdL1.getModel().sample(sL1,nav_blue);
//        System.out.println(nav_blue.actionName());
//        System.out.println(eo.op);
//
//        EnvironmentOutcome eo1 = tdL1.getModel().sample(eo.op,pickup);
//        System.out.println(pickup.actionName());
//        System.out.println(eo1.op);
//
//        EnvironmentOutcome eo2 = tdL1.getModel().sample(eo1.op,nav_red);
//        System.out.println(nav_red.actionName());
//        System.out.println(eo2.op);
//
//        EnvironmentOutcome eo3 = tdL1.getModel().sample(eo2.op,putdown);
//        System.out.println(putdown.actionName());
//        System.out.println(eo3.op);

//        List<ActionType> actionTypes = td.getActionTypes();

//        List<Action> actionList = new ArrayList<Action>();

//        for(ActionType at : actionTypes){
//            actionList.addAll(at.allApplicableActions(sL1));
//        }



        BoundedRTDP planner = new BoundedRTDP(tdL1, discount ,shf,
                new ConstantValueFunction(0.), new ConstantValueFunction(1.),0.001,-1);



        Policy policy = planner.planFromState(sL1);
        Episode episode = PolicyUtils.rollout(policy, sL1, tdL1.getModel(),100);

        for (int i = 0; i<episode.numActions(); i++){
            System.out.println("State: " + episode.state(i).toString());
            System.out.println("action: " + episode.action(i).actionName());
        }

    }


}
