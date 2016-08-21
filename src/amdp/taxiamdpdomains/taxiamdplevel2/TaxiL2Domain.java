package amdp.taxiamdpdomains.taxiamdplevel2;

import amdp.taxi.TaxiDomain;
import amdp.taxi.TaxiRewardFunction;
import amdp.taxi.TaxiTerminationFunction;
import amdp.taxi.state.TaxiLocation;
import amdp.taxiamdpdomains.taxiamdplevel1.taxil1state.L1StateMapper;
import amdp.taxiamdpdomains.taxiamdplevel2.taxil2state.*;
import burlap.behavior.policy.Policy;
import burlap.behavior.policy.PolicyUtils;
import burlap.behavior.singleagent.Episode;
import burlap.behavior.singleagent.planning.stochastic.rtdp.BoundedRTDP;
import burlap.behavior.valuefunction.ConstantValueFunction;
import burlap.debugtools.RandomFactory;
import burlap.mdp.auxiliary.DomainGenerator;
import burlap.mdp.auxiliary.common.NullTermination;
import burlap.mdp.core.StateTransitionProb;
import burlap.mdp.core.TerminalFunction;
import burlap.mdp.core.action.Action;
import burlap.mdp.core.action.ActionType;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.common.UniformCostRF;
import burlap.mdp.singleagent.model.FactoredModel;
import burlap.mdp.singleagent.model.RewardFunction;
import burlap.mdp.singleagent.model.statemodel.FullStateModel;
import burlap.mdp.singleagent.oo.OOSADomain;
import burlap.statehashing.simple.SimpleHashableStateFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by ngopalan on 8/10/16.
 */
public class TaxiL2Domain implements DomainGenerator {


    public static final String VAR_INTAXI = "inTaxiAtt";
    public static final String VAR_OCCUPIEDTAXI = "occupiedTaxiAtt";

    public static final String VAR_PICKEDUPATLEASTONCE = "pickedUpAtLeastOnce";

    // this is the current location attribute
    public static final String VAR_LOCATION = "locationAtt";
    public static final String VAR_CURRENTLOCATION = "locationAtt";
    public static final String VAR_GOALLOCATION = "goalLocationAtt";
    public static final String VAR_ORIGINALSOURCELOCATION = "originalSourceLocationAtt";

    public static final String VAR_JUSTPICKEDUP = "justPickedupAtt";


    public static final String LOCATIONL2CLASS = "location";
    public static final String PASSENGERL2CLASS = "passenger";


    public static final String ACTION_GET = "get";
    public static final String ACTION_PUT = "put";

    public static final String ON_ROAD = "on_road";
    public static final String RED = "red";
    public static final String GREEN = "green";
    public static final String BLUE = "blue";
    public static final String YELLOW = "yellow";
    public static final String DARKGREY = "darkgrey";
    public static final String MAGENTA = "magenta";
    public static final String PINK = "pink";
    public static final String ORANGE = "orange";
    public static final String CYAN = "cyan";
    public static final String FUEL = "fuel";


    protected RandomFactory randomFactory = new RandomFactory();
    protected Random rand;


    protected RewardFunction rf;
    protected TerminalFunction tf;


    public TaxiL2Domain(RewardFunction rf, TerminalFunction tf, Random rand) {
        this.rf = rf;
        this.tf = tf;
        this.rand = rand;
    }

    public TaxiL2Domain(RewardFunction rf, TerminalFunction tf) {
        this.rf = rf;
        this.tf = tf;
        this.rand = this.randomFactory.ingetOrSeedMapped(0, 0);
    }

    @Override
    public OOSADomain generateDomain() {
        OOSADomain domain = new OOSADomain();


        RewardFunction rf = this.rf;
        TerminalFunction tf = this.tf;

        if (rf == null) {
            rf = new UniformCostRF();
        }
        if (tf == null) {
            tf = new NullTermination();
        }

        domain.addStateClass(PASSENGERL2CLASS, TaxiL2Passenger.class)
                .addStateClass(LOCATIONL2CLASS, TaxiL2Location.class);

        // create model
        TaxiL2Model tmodel = new TaxiL2Model(rand);

        FactoredModel model = new FactoredModel(tmodel, rf, tf);
        domain.setModel(model);

        domain.addActionTypes(
                new PutType(),
                new GetType());

        return domain;
    }


    public static class TaxiL2Model implements FullStateModel {

        protected Random rand;

        public TaxiL2Model(Random rand) {
            this.rand = rand;
        }

        @Override
        public List<StateTransitionProb> stateTransitions(State s, Action a) {
            int actionInd = actionInd(a.actionName().split("_")[0]);

            List<StateTransitionProb> transitions = new ArrayList<StateTransitionProb>();
            if (actionInd == 0) {
                // navigate action!
                getAction(s, a, transitions);
            } else if (actionInd == 1) {
                // pick up
                putAction(s, a, transitions);
            }
            return transitions;
        }


        @Override
        public State sample(State s, Action a) {
            List<StateTransitionProb> stpList = this.stateTransitions(s, a);
            double roll = rand.nextDouble();
            double curSum = 0.;
            for (int i = 0; i < stpList.size(); i++) {
                curSum += stpList.get(i).p;
                if (roll < curSum) {
                    return stpList.get(i).s;
                }
            }
            throw new RuntimeException("Probabilities don't sum to 1.0: " + curSum);
        }

        private void putAction(State s, Action a, List<StateTransitionProb> transitions) {
            TaxiL2State ns = (TaxiL2State) s.copy();
//            List<TaxiL2Passenger> passengers = ns.passengers;
            String passengerName = ((PutType.PutAction) a).passenger;
//            String locationName = ((PutType.PutAction) a).location;
            TaxiL2Passenger p1 = ns.touchPassenger(passengerName);
            if (p1.inTaxi) {
                p1.inTaxi = false;
//                p1.currentLocation = locationName;
                p1.currentLocation = p1.goalLocation;
            }

            transitions.add(new StateTransitionProb(ns, 1.));

        }

        private void getAction(State s, Action a, List<StateTransitionProb> transitions) {
            TaxiL2State ns = (TaxiL2State) s.copy();
            String passengerName = ((GetType.GetAction) a).passenger;
            TaxiL2Passenger p1 = ns.touchPassenger(passengerName);
            boolean flag = true;
            for (TaxiL2Passenger p : ns.passengers) {
                if (p.inTaxi) {
                    flag = false;
                    break;
                }
            }

            if (flag) {
                p1.inTaxi = true;
                p1.pickUpOnce = true;
            }
            transitions.add(new StateTransitionProb(ns, 1.));
        }


        protected int actionInd(String name) {
            if (name.equals(ACTION_GET)) {
                return 0;
            } else if (name.equals(ACTION_PUT)) {
                return 1;
            }
            throw new RuntimeException("Unknown action " + name);
        }
    }


    /**
     * Describes the navigate action at level 1 of the AMDP
     */
    public static class GetType implements ActionType {


        public GetType() {
//            actions = new ArrayList<Action>(locations.size());
//            for(String location : locations){
//                actions.add(new NavigateAction(location));
//            }
        }

        @Override
        public String typeName() {
            return ACTION_GET;
        }

        @Override
        public Action associatedAction(String strRep) {
            return new GetAction(strRep);
        }

        @Override
        public List<Action> allApplicableActions(State s) {
            List<Action> actions = new ArrayList<Action>();
            List<TaxiL2Passenger> passengers = ((TaxiL2State) s).passengers;
            for (TaxiL2Passenger passenger : passengers) {
                actions.add(new GetAction(passenger.name()));

            }
            return actions;
        }

        public static class GetAction implements Action {

            public String passenger;

            public GetAction(String passenger) {
                this.passenger = passenger;
            }

            @Override
            public String actionName() {
                return ACTION_GET + "_" + passenger;
            }

            @Override
            public Action copy() {
                return new GetAction(passenger);
            }

            @Override
            public boolean equals(Object o) {
                if (this == o) return true;
                if (o == null || getClass() != o.getClass()) return false;

                GetAction that = (GetAction) o;

                return that.passenger.equals(passenger);

            }

            @Override
            public int hashCode() {
                String str = ACTION_GET + "_" + passenger;
                return str.hashCode();
            }

            @Override
            public String toString() {
                return this.actionName();
            }
        }


    }

    public static class PutType implements ActionType {


        public PutType() {
//            actions = new ArrayList<Action>(locations.size());
//            for(String location : locations){
//                actions.add(new NavigateAction(location));
//            }
        }

        @Override
        public String typeName() {
            return ACTION_PUT;
        }

        @Override
        public Action associatedAction(String strRep) {
//            return new PutAction(strRep.split("_")[0], strRep.split("_")[1]);
            return new PutAction(strRep.split("_")[0]);
        }

        @Override
        public List<Action> allApplicableActions(State s) {
            List<Action> actions = new ArrayList<Action>();
            List<TaxiL2Passenger> passengers = ((TaxiL2State) s).passengers;
            List<TaxiL2Location> locations = ((TaxiL2State) s).locations;
            for (TaxiL2Passenger passenger : passengers) {
                for (TaxiL2Location loc : locations) {
//                    actions.add(new PutAction(passenger.name(), loc.colour));
                    actions.add(new PutAction(passenger.name()));
                }

            }
            return actions;
        }

        public static class PutAction implements Action {

            public String passenger;
//            public String location;

            public PutAction(String passenger) {
                this.passenger = passenger;
//                this.location = location;
            }

            @Override
            public String actionName() {
//                return ACTION_PUT + "_" + passenger + "_" + location;
                return ACTION_PUT + "_" + passenger;
            }

            @Override
            public Action copy() {
                return new PutAction(passenger);
            }

            @Override
            public boolean equals(Object o) {
                if (this == o) return true;
                if (o == null || getClass() != o.getClass()) return false;

                PutAction that = (PutAction) o;

                return that.passenger.equals(passenger) ;//&& that.location.equals(location);

            }

            @Override
            public int hashCode() {
                String str = ACTION_PUT + "_" + passenger;// + "_" + location;
                return str.hashCode();
            }

            @Override
            public String toString() {
                return this.actionName();
            }
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

        L2StateMapper l2StateMapper = new L2StateMapper();

        State sL2 = l2StateMapper.mapState(sL1);


        TerminalFunction tfL2 = new TaxiL2TerminalFunction();
        RewardFunction rfL2 = new UniformCostRF();

        TaxiL2Domain taxiL2DomainGen = new TaxiL2Domain(rfL2, tfL2,rand);


        OOSADomain tdL2 = taxiL2DomainGen.generateDomain();

//        System.out.println(sL1.toString());

        double discount = 0.99;
        SimpleHashableStateFactory shf = new SimpleHashableStateFactory(false);

        BoundedRTDP planner = new BoundedRTDP(tdL2, discount ,shf,
                new ConstantValueFunction(0.), new ConstantValueFunction(1.),0.001,-1);

        Policy policy = planner.planFromState(sL2);
        Episode episode = PolicyUtils.rollout(policy, sL2, tdL2.getModel(),100);

        for (int i = 0; i<episode.numActions(); i++){
            System.out.println("State: " + episode.state(i).toString());
            System.out.println("action: " + episode.action(i).actionName());
        }
    }
}
