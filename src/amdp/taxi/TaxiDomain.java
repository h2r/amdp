package amdp.taxi;

import amdp.taxi.state.*;
import amdp.utilities.BoltzmannQPolicyWithCoolingSchedule;
import burlap.behavior.policy.*;
import burlap.behavior.singleagent.Episode;
import burlap.behavior.singleagent.auxiliary.EpisodeSequenceVisualizer;
import burlap.behavior.singleagent.learning.tdmethods.QLearning;
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
import burlap.mdp.core.oo.propositional.PropositionalFunction;
import burlap.mdp.core.oo.state.OOState;
import burlap.mdp.core.oo.state.ObjectInstance;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.common.UniformCostRF;
import burlap.mdp.singleagent.environment.SimulatedEnvironment;
import burlap.mdp.singleagent.model.FactoredModel;
import burlap.mdp.singleagent.model.RewardFunction;
import burlap.mdp.singleagent.model.statemodel.FullStateModel;
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
import java.util.Arrays;
import java.util.List;
import java.util.Random;


/**
 * Created by ngopalan on 6/14/16.
 */


//fickle works only after passenger has moved one stop
//fickle passenger is sampled randomly from the start state
//taxi is placed randomly at start states
//passenger needs to be picked and dropped before termination
//TODO: add proposition functions

public class TaxiDomain implements DomainGenerator{

    public static final String								VAR_X = "xAtt";
    public static final String								VAR_Y = "yAtt";
    public static final String								VAR_FUEL = "fuelAtt";
    public static final String								VAR_INTAXI = "inTaxiAtt";
    public static final String								VAR_OCCUPIEDTAXI = "occupiedTaxiAtt";
    public static final String								VAR_WALLMIN = "wallMinAtt";
    public static final String								VAR_WALLMAX = "wallMaxAtt";
    public static final String								VAR_WALLOFFSET = "wallOffsetAtt";
    public static final String								VAR_PICKEDUPATLEASTONCE = "pickedUpAtLeastOnce";

    // this is the current location attribute
    public static final String								VAR_LOCATION = "locationAtt";
    public static final String								VAR_GOALLOCATION = "goalLocationAtt";

    public static final String								VAR_JUSTPICKEDUP = "justPickedupAtt";


    public static final String								TAXICLASS = "taxi";
    public static final String								LOCATIONCLASS = "location";
    public static final String								PASSENGERCLASS = "passenger";
    public static final String								HWALLCLASS = "horizontalWall";
    public static final String								VWALLCLASS = "verticalWall";


    public static final String								ACTION_NORTH = "north";
    public static final String								ACTION_SOUTH = "south";
    public static final String								ACTION_EAST = "east";
    public static final String								ACTION_WEST = "west";
    public static final String								ACTION_PICKUP = "pickup";
    public static final String								ACTION_DROPOFF = "dropoff";
    public static final String								ACTION_FILLUP = "fillup";


    public static final String								TAXIATLOCATIONPF = "taxiAt";
    public static final String								PASSENGERATGOALLOCATIONPF = "passengerAtGoal";
    public static final String								TAXIATPASSENGERPF = "taxiAtPassenger";
    public static final String								WALLNORTHPF = "wallNorth";
    public static final String								WALLSOUTHPF = "wallSouth";
    public static final String								WALLEASTPF = "wallEast";
    public static final String								WALLWESTPF = "wallWest";

    public static final String								PASSENGERPICKUPPF = "passengerPickUpPF";
    public static final String								PASSENGERPUTDOWNPF = "passengerPutDownPF";

    public static final String                              FUELLOCATION = "fuel";



    public static int												maxX = 5;
    public static int												maxY = 5;
    public static int												maxFuel = 12;


    public static final String                              RED = "red";
    public static final String                              GREEN = "green";
    public static final String                              BLUE = "blue";
    public static final String                              YELLOW = "yellow";
    public static final String                              DARKGREY = "darkgrey";
    //    public static final String                              RED = "red";
//    public static final String                              RED = "red";
    public static final String                              MAGENTA = "magenta";
    public static final String                              PINK = "pink";
    public static final String                              ORANGE = "orange";
    public static final String                              CYAN = "cyan";
    public static final String                              FUEL = "fuel";


    /**
     * Matrix specifying the transition dynamics in terms of movement directions. The first index
     * indicates the action direction attempted (ordered north, south, east, west) the second index
     * indicates the actual resulting direction the agent will go (assuming there is no wall in the way).
     * The value is the probability of that outcome. The existence of walls does not affect the probability
     * of the direction the agent will actually go, but if a wall is in the way, it will affect the outcome.
     * For instance, if the agent selects north, but there is a 0.2 probability of actually going east and
     * there is a wall to the east, then with 0.2 probability, the agent will stay in place.
     */
    protected double[][]								moveTransitionDynamics;
    protected double[][]								fickleLocationDynamics;

    public boolean											includeFuel = true;
    public boolean                                          fickleTaxi = false;
    public double                                           fickleProbability = 0.3;


    protected RandomFactory randomFactory = new RandomFactory();
    protected Random rand;


    protected RewardFunction rf;
    protected TerminalFunction tf;

    public RewardFunction getRf() {
        return rf;
    }

    public void setRf(RewardFunction rf) {
        this.rf = rf;
    }

    public TerminalFunction getTf() {
        return tf;
    }

    public void setTf(TerminalFunction tf) {
        this.tf = tf;
    }
    public double getFickleProbability() {
        return fickleProbability;
    }

    public void setFickleProbability(double fickleProbability) {
        if(fickleProbability <=1. && fickleProbability >=0.) {
            this.fickleProbability = fickleProbability;
        }
        else {
            throw new RuntimeException("Value is not within 0.0 and 1.0 : " + fickleProbability);
        }
    }

    public boolean isFickleTaxi() {
        return fickleTaxi;
    }

    public void setFickleTaxi(boolean fickleTaxi) {
        this.fickleTaxi = fickleTaxi;
    }

    public boolean isIncludeFuel() {
        return includeFuel;
    }

    public void setIncludeFuel(boolean includeFuel) {
        this.includeFuel = includeFuel;
    }

    public RandomFactory getRandomFactory() {
        return randomFactory;
    }

    public void setRandom(Random rand){
        this.rand =rand;
    }

    public void setRandomFactory(RandomFactory randomFactory) {
        this.randomFactory = randomFactory;
    }

    public TaxiDomain(RewardFunction rf, TerminalFunction tf) {
        this.rf = rf;
        this.tf = tf;
        this.setDeterministicTransitionDynamics();
        this.rand = randomFactory.ingetMapped(0);
    }

    private void setDeterministicTransitionDynamics() {
        int directions = 4;
        int na = 4;
        moveTransitionDynamics = new double[na][na];
        for(int i = 0; i < na; i++){
            for(int j = 0; j < na; j++){
                if(i != j){
                    moveTransitionDynamics[i][j] = 0.;
                }
                else{
                    moveTransitionDynamics[i][j] = 1.;
                }
            }
        }
    }

    /**
     * Will set the movement direction probabilities, based on the action chosen and passenger's goal location change probability.
     * The index (0,1,2,3) indicates the direction north,south,east,west, respectively and the matrix is organized by
     * transitionDynamics[selectedDirection][actualDirection].
     * For instance, the probability of the agent moving east when selecting north would be specified in the entry transitionDynamics[0][2]
     * The index (0,1,...n) indicates the locations the passenger which might be the passenger's destinations, and the matrix is organized
     * by the fickle passengers intial location preference when picked up, to the final location preference, chosen when the taxi
     * has taken one move action location TransitionDynamics[initialGoalLocation][finalGoalLocation].
     * @param transitionDynamics entries indicate the probability of movement in the given direction (second index) for the given action selected (first index).
     * @param locationTransitionDynamics entries indicate the probability of new goal location(second index) for a passenger's original goal location (first index).
     */
    public void setTransitionDynamics(double [][] transitionDynamics, double[][] locationTransitionDynamics){
        this.moveTransitionDynamics = transitionDynamics.clone();
        this.fickleLocationDynamics = locationTransitionDynamics.clone();
    }

    /**
     * Will set the movement direction probabilities, based on the action chosen and passenger's goal location change probability.
     * The index (0,1,2,3) indicates the direction north,south,east,west, respectively and the matrix is organized by
     * transitionDynamics[selectedDirection][actualDirection].
     * For instance, the probability of the agent moving east when selecting north would be specified in the entry transitionDynamics[0][2]
     * The index (0,1,...n) indicates the locations the passenger which might be the passenger's destinations, and the matrix is organized
     * by the fickle passengers intial location preference when picked up, to the final location preference, chosen when the taxi
     * has taken one move action location TransitionDynamics[initialGoalLocation][finalGoalLocation].
     */
    public void setTransitionDynamicsLikeFickleTaxiProlem(){
        moveTransitionDynamics = new double[][]{{0.8, 0., 0.1, 0.1},
                {0., 0.8, 0.1, 0.1},
                {0.1, 0.1, 0.8, 0.0},
                {0.1, 0.1, 0., 0.8}};

        fickleLocationDynamics = new double[4][4];
        for(int i = 0; i < 4; i++){
            for(int j = 0; j < 4; j++){
                if(i != j){
                    fickleLocationDynamics[i][j] = 0.075;
                }
                else{
                    fickleLocationDynamics[i][j] = 0.775;
                }
            }
        }
    }


    /**
     * Will set the movement direction probabilities based on the action chosen. The index (0,1,2,3) indicates the
     * direction north,south,east,west, respectively and the matrix is organized by transitionDynamics[selectedDirection][actualDirection].
     * For instance, the probability of the agent moving east when selecting north would be specified in the entry transitionDynamics[0][2]
     *This setup is for when the taxi is not fickle
     * @param transitionDynamics entries indicate the probability of movement in the given direction (second index) for the given action selected (first index).
     */
    public void setTransitionDynamics(double [][] transitionDynamics){
        this.moveTransitionDynamics = transitionDynamics.clone();
    }


    @Override
    public OOSADomain generateDomain() {
        // need a boolean for the fickle nature and another for the transition dynamics
//        Random rand = randomFactory.ingetMapped(0);

        OOSADomain domain = new OOSADomain();

        RewardFunction rf = this.rf;
        TerminalFunction tf = this.tf;

        if(rf == null){
            rf = new UniformCostRF();
        }
        if(tf == null){
            tf = new NullTermination();
        }

        if(fickleTaxi){
            TaxiModel smodel = new TaxiModel(rand, moveTransitionDynamics,
                    fickleLocationDynamics, fickleTaxi, fickleProbability, includeFuel );
            FactoredModel model = new FactoredModel(smodel, rf, tf);
            domain.setModel(model);
        }
        else{
            TaxiModel smodel = new TaxiModel(rand, moveTransitionDynamics, includeFuel );
            FactoredModel model = new FactoredModel(smodel, rf, tf);
            domain.setModel(model);
        }


        if(includeFuel){
            domain.addActionTypes(
                    new UniversalActionType(ACTION_NORTH),
                    new UniversalActionType(ACTION_SOUTH),
                    new UniversalActionType(ACTION_EAST),
                    new UniversalActionType(ACTION_WEST),
                    new UniversalActionType(ACTION_DROPOFF),
                    new UniversalActionType(ACTION_FILLUP),
                    new UniversalActionType(ACTION_PICKUP));
        }
        else{
            domain.addActionTypes(
                    new UniversalActionType(ACTION_NORTH),
                    new UniversalActionType(ACTION_SOUTH),
                    new UniversalActionType(ACTION_EAST),
                    new UniversalActionType(ACTION_WEST),
                    new UniversalActionType(ACTION_DROPOFF),
                    new UniversalActionType(ACTION_PICKUP));
        }


        return domain;
    }




    public static class TaxiModel implements FullStateModel{
        protected Random rand;
        protected double[][] movementTransitionDynamics;
        protected double[][] locationTransitionDynamics;
        protected boolean fickleTaxi;
        protected double fickleProbability = 0.;
        protected boolean includeFuel;

        public TaxiModel(Random rand, double[][] movementTransitionDynamics, boolean includeFuel) {
            this.rand = rand;
            this.movementTransitionDynamics = movementTransitionDynamics;
            this.fickleTaxi = false;
            this.includeFuel = includeFuel;
        }

        public TaxiModel(Random rand, double[][] movementTransitionDynamics, double[][] locationTransitionDynamics,
                         boolean fickleTaxi, double fickleProbability, boolean includeFuel) {
            this.rand = rand;
            this.movementTransitionDynamics = movementTransitionDynamics;
            this.fickleTaxi = fickleTaxi;
            this.fickleProbability = fickleProbability;
            if(fickleTaxi && locationTransitionDynamics==null){
                throw new RuntimeException("transition dynamics for switching locations are null but the passenger is fickle.");
            }
            this.includeFuel = includeFuel;
            this.locationTransitionDynamics = locationTransitionDynamics;
        }

        @Override
        public List<StateTransitionProb> stateTransitions(State s, Action a) {
            int actionInd = actionInd(a.actionName());

            List <StateTransitionProb> transitions = new ArrayList<StateTransitionProb>();
            if(actionInd<4){
                double [] directionProbs = movementTransitionDynamics[actionInd(a.actionName())];

                for(int i = 0; i < directionProbs.length; i++) {
                    double p = directionProbs[i];
                    if (p == 0.) {
                        continue; //cannot transition in this direction
                    }
                    State ns = s.copy();
                    int[] dcomps = movementDirectionFromIndex(i);
                    ns = move(ns, dcomps[0], dcomps[1]);


                    if (fickleTaxi) {
                        boolean passengersNotChangingDestinationFlag = true;
                        List<ObjectInstance> passengers = ((TaxiState)ns).objectsOfClass(PASSENGERCLASS);
                        for(ObjectInstance pass : passengers){
                            if(!((TaxiPassenger)pass).justPickedUp){
                                continue;
                            }
                            else{
                                // if passenger has not moved then continue
//                                System.out.println("passenger was just picked up!");
                                if(!passengerMoved(s,ns, (TaxiPassenger)pass)){
                                    continue;
                                }
                                passengersNotChangingDestinationFlag  = false;
                                // ns has the move information nns will have the new changed destination
                                // get current location index
                                int locationIndex = ((TaxiState) ns).locationIndWithColour(((TaxiPassenger)pass).goalLocation);
                                if(locationIndex==-1){
                                    throw new RuntimeException("Unknown location as passenger goal: " + ((TaxiPassenger)pass).goalLocation);
                                }
                                double[] locationChangeProbabilities =locationTransitionDynamics[locationIndex];
                                List<ObjectInstance> locations = ((TaxiState) ns).objectsOfClass(LOCATIONCLASS);

                                for (int j = 0; j <locationChangeProbabilities.length;j++){
                                    State nns = ns.copy();
                                    TaxiPassenger passN = ((TaxiState)nns).touchPassenger(pass.name());
                                    String newGoal = new String(((TaxiLocation) locations.get(j)).colour);
                                    passN.goalLocation = newGoal;
                                    passN.justPickedUp=false;
                                    transitions.add(new StateTransitionProb(nns,p*locationChangeProbabilities[j]));
                                }


                            }

                        }
                        if(passengersNotChangingDestinationFlag){
                            transitions.add(new StateTransitionProb(ns,p));

                        }
                    }
                    else{
                        transitions.add(new StateTransitionProb(ns,p));
                    }

                }

                return transitions;
            }
            // not a move action!
            if(actionInd==4){
                //pick

                TaxiState ns = (TaxiState)s.copy();
                TaxiAgent taxi = ns.touchTaxi();
                int tx = taxi.x;
                int ty = taxi.y;
                boolean taxiOccupied = taxi.taxiOccupied;

                if(!taxiOccupied){
                    List<ObjectInstance> passengers = ((TaxiState)s).objectsOfClass(PASSENGERCLASS);
                    for(ObjectInstance p : passengers){
                        int px = ((TaxiPassenger)p).x;
                        int py = ((TaxiPassenger)p).y;

                        if(tx == px && ty == py ){
                            int passID = ns.passengerInd(((TaxiPassenger)p).name());
                            TaxiPassenger np = ns.touchPassenger(passID);
                            np.inTaxi = true;
                            if(fickleTaxi) {
                                np.justPickedUp = true;
                            }
                            taxi.taxiOccupied = true;
                            np.pickedUpAtLeastOnce = true;
                            break;
                        }

                    }
                }
                transitions.add(new StateTransitionProb(ns, 1.0));
                return transitions;

            }

            if(actionInd==5){
                //drop
                TaxiState ns = (TaxiState)s.copy();
                TaxiAgent taxi = ns.touchTaxi();
                boolean taxiOccupied = taxi.taxiOccupied;




                if(taxiOccupied){
                    List<ObjectInstance> passengers = ns.objectsOfClass(PASSENGERCLASS);
                    List<ObjectInstance> locationList = ns.objectsOfClass(LOCATIONCLASS);
                    for(ObjectInstance p : passengers){
                        boolean in = ((TaxiPassenger)p).inTaxi;
                        if(in){
                            // we can only drop a passenger in the goal location, every other dropoff is illegal!
                            String goalLocation = ((TaxiPassenger)p).goalLocation;
                            for(ObjectInstance l :locationList){
                                if(goalLocation.equals(((TaxiLocation)l).colour)){
                                    if(((TaxiLocation)l).x==((TaxiPassenger)p).x
                                            && ((TaxiLocation)l).y==((TaxiPassenger)p).y){
                                        int passID = ns.passengerInd(((TaxiPassenger)p).name());
                                        TaxiPassenger np = ns.touchPassenger(passID);
                                        np.inTaxi = false;
                                        taxi.taxiOccupied = false;
                                        break;
                                    }
                                }
                            }


                        }
                    }
                }
                transitions.add(new StateTransitionProb(ns, 1.0));
                return transitions;

            }

            if(actionInd==6){
                // fillup
                if(!includeFuel){
                    transitions.add(new StateTransitionProb(s,1.));
                    return transitions;
                }
                TaxiState ns = (TaxiState)s.copy();
                TaxiAgent taxi = (TaxiAgent)((TaxiState)s).objectsOfClass(TAXICLASS).get(0);
                int tx = taxi.x;
                int ty = taxi.y;

                List<ObjectInstance> locations = ((TaxiState)s).objectsOfClass(LOCATIONCLASS);
                for(ObjectInstance l : locations){
                    if(((TaxiLocation)l).colour.equals(FUELLOCATION)){
                        int lx = ((TaxiLocation)l).x;
                        int ly = ((TaxiLocation)l).y;
                        if(tx == lx && ty == ly){
                            TaxiAgent ntaxi = ns.touchTaxi();
                            ntaxi.fuel = maxFuel;
                        }
                    }
                }
                transitions.add(new StateTransitionProb(ns,1.0));
                return transitions;

            }

            return transitions;
        }

        private boolean passengerMoved(State s, State ns, TaxiPassenger pass) {
            TaxiPassenger pOld = (TaxiPassenger)((TaxiState)s).object(pass.name());
            TaxiPassenger pNew = (TaxiPassenger)((TaxiState)ns).object(pass.name());
            double distance = Math.abs(pOld.x - pNew.x) +Math.abs(pOld.y - pNew.y);
            return distance>0;

        }

        @Override
        public State sample(State s, Action a) {
            List<StateTransitionProb> stpList = this.stateTransitions(s,a);
            double roll = rand.nextDouble();
            double curSum = 0.;
            int dir = 0;
            for(int i = 0; i < stpList.size(); i++){
                curSum += stpList.get(i).p;
                if(roll < curSum){
                    return stpList.get(i).s;
                }
            }
            throw new RuntimeException("Probabilities don't sum to 1.0: " + curSum);
        }


        /**
         * Attempts to move the agent into the given position, taking into account walls and blocks
         * @param sIn the current state
         * @param dx the attempted new X position of the agent
         * @param dy the attempted new Y position of the agent
         * @return input state s, after modification
         */
        protected State move(State sIn, int dx, int dy){
            Random rand = RandomFactory.getMapped(0);

            TaxiState ts = (TaxiState)sIn;

            int tx = ts.taxi.x;
            int ty = ts.taxi.y;

            int nx = tx+dx;
            int ny = ty+dy;

            //using fuel?
            TaxiAgent taxi = ts.touchTaxi();
            if(includeFuel){
                int fuel = taxi.fuel;
                if(fuel == 0){
                    //no movement possible
                    return ts;
                }
                taxi.fuel-=1;
            }

            //hit wall, so do not change position
//        if(nx < 0 || nx >= map.length || ny < 0 || ny >= map[0].length || map[nx][ny] == 1 ||
//                (dx > 0 && (map[ax][ay] == 3 || map[ax][ay] == 4)) || (dx < 0 && (map[nx][ny] == 3 || map[nx][ny] == 4)) ||
//                (dy > 0 && (map[ax][ay] == 2 || map[ax][ay] == 4)) || (dy < 0 && (map[nx][ny] == 2 || map[nx][ny] == 4)) ){
//            nx = ax;
//            ny = ay;
//        }



            //check for all wall boundings

            if(dx > 0){
                List<ObjectInstance> vwalls = ts.objectsOfClass(VWALLCLASS);
                for(ObjectInstance wall : vwalls){
                    if(wallEast(tx, ty, (TaxiMapWall) wall)){
                        nx = tx;
                        break;
                    }
                }
            }
            else if(dx < 0){
                List<ObjectInstance> vwalls = ts.objectsOfClass(VWALLCLASS);
                for(ObjectInstance wall : vwalls){
                    if(wallWest(tx, ty, (TaxiMapWall)wall)){
                        nx = tx;
                        break;
                    }
                }
            }
            else if(dy > 0){
                List<ObjectInstance> hwalls = ts.objectsOfClass(HWALLCLASS);
                for(ObjectInstance wall : hwalls){
                    if(wallNorth(tx, ty, (TaxiMapWall)wall)){
                        ny = ty;
                        break;
                    }
                }
            }
            else if(dy < 0){
                List<ObjectInstance> hwalls = ts.objectsOfClass(HWALLCLASS);
                for(ObjectInstance wall : hwalls){
                    if(wallSouth(tx, ty, (TaxiMapWall)wall)){
                        ny = ty;
                        break;
                    }
                }
            }

            taxi.x = nx;
            taxi.y = ny;


            List<ObjectInstance> passengers = ts.objectsOfClass(PASSENGERCLASS);
            for(ObjectInstance p : passengers){
                boolean inTaxi = ((TaxiPassenger)p).inTaxi;
//                    p.getIntValForAttribute(INTAXIATT);
                if(inTaxi){
                    TaxiPassenger pN = ts.touchPassenger(p.name());
                    pN.x = nx;
                    pN.y = ny;
                }
            }

            return ts;
        }


//        @Override
//        public List<StateTransitionProb> stateTransitions(State s, Action a) {
//            return null;
//        }
    }


    //propositional function for taxi at location for navigate
    public class PF_TaxiAtLoc extends PropositionalFunction {
        public PF_TaxiAtLoc(String name, String [] params){
            super(name, params);
        }

        @Override
        public boolean isTrue(OOState s, String... params) {
            TaxiAgent taxi = ((TaxiState)s).taxi;
//            ObjectInstance o = s.getFirstObjectOfClass(TAXICLASS);
            int xt = taxi.x;
            int yt = taxi.y;
            // params here are the name of a location like Location 1

            boolean returnValue = false;
            TaxiLocation location = (TaxiLocation)((TaxiState)s).object(params[0]);

            int xl = location.x;
            int yl = location.y;
            if(xt==xl && yt==yl ){
                returnValue = true;
            }

            return returnValue;
        }

    }


    public class PF_PassengerAtLoc extends PropositionalFunction{

        public PF_PassengerAtLoc(String name, Domain domain, String [] params){
            super(name, params);
        }


        @Override
        public boolean isTrue(OOState s, String... params) {
            TaxiPassenger p = (TaxiPassenger)s.object(params[0]);
            int xp = p.x;
            int yp = p.y;
            boolean inTaxi = p.inTaxi;
            String goalLocation = p.goalLocation;
            // params here are the name of a location like Location 1

            boolean returnValue = false;
            List<ObjectInstance> locations = s.objectsOfClass(LOCATIONCLASS);
            for(ObjectInstance location : locations){
                if(((TaxiLocation)location).colour.equals(goalLocation)){
                    int xl = ((TaxiLocation)location).x;
                    int yl = ((TaxiLocation)location).y;
                    if(xp==xl && yp==yl && !inTaxi ){
                        returnValue = true;
                    }
                    break;
                }

            }


            return returnValue;
        }
    }

    //propositional function for pick up
    public class PF_PickUp extends PropositionalFunction{



        public PF_PickUp(String name, Domain domain, String [] params){
            super(name, params);
        }

        @Override
        public boolean isTrue(OOState s, String[] params) {
            TaxiAgent taxiAgent = (TaxiAgent)s.objectsOfClass(TAXICLASS).get(0);
            int xt = taxiAgent.x;
            int yt = taxiAgent.y;
            boolean taxiOccupied = taxiAgent.taxiOccupied;
            // params here are the location colour - red, green, blue, yellow, magenta

            boolean returnValue = false;
            List<ObjectInstance> passengers = s.objectsOfClass(PASSENGERCLASS);
            for(int i=0;i<passengers.size();i++){
                int xp = ((TaxiPassenger)passengers.get(i)).x;
                int yp = ((TaxiPassenger)passengers.get(i)).y;
                boolean inTaxi = ((TaxiPassenger) passengers.get(i)).inTaxi;
                if(xt==xp && yt==yp && inTaxi && taxiOccupied){
                    returnValue = true;
                    break;
                }
            }
            return returnValue;
        }
    }

    //propositional function for put down

    public class PF_PutDown extends PropositionalFunction{


        public PF_PutDown(String name, String [] params){
            super(name, params);
        }

        @Override
        public boolean isTrue(OOState s, String[] params) {
            TaxiAgent taxiAgent = (TaxiAgent)s.objectsOfClass(TAXICLASS).get(0);
            int xt = taxiAgent.x;
            int yt = taxiAgent.y;
            boolean taxiOccupied = taxiAgent.taxiOccupied;
            // params here are the location colour - red, green, blue, yellow, magenta

            boolean returnValue = false;
            List<ObjectInstance> passengers = s.objectsOfClass(PASSENGERCLASS);
            for(int i=0;i<passengers.size();i++){
                int xp = ((TaxiPassenger)passengers.get(i)).x;
                int yp = ((TaxiPassenger)passengers.get(i)).y;
                boolean inTaxi = ((TaxiPassenger)passengers.get(i)).inTaxi;
                if(xt==xp && yt==yp && !inTaxi && !taxiOccupied){
                    returnValue = true;
                    break;
                }
            }
            return returnValue;
        }
    }



    /**
     * Returns the change in x and y position for a given direction number.
     * @param i the direction number (0,1,2,3 indicates north,south,east,west, respectively)
     * @return the change in direction for x and y; the first index of the returned double is change in x, the second index is change in y.
     */
    protected static int [] movementDirectionFromIndex(int i){

        int [] result = null;

        switch (i) {
            case 0:
                result = new int[]{0,1};
                break;

            case 1:
                result = new int[]{0,-1};
                break;

            case 2:
                result = new int[]{1,0};
                break;

            case 3:
                result = new int[]{-1,0};
                break;

            default:
                break;
        }

        return result;
    }



    protected static int actionInd(String name){
        if(name.equals(ACTION_NORTH)){
            return 0;
        }
        else if(name.equals(ACTION_SOUTH)){
            return 1;
        }
        else if(name.equals(ACTION_EAST)){
            return 2;
        }
        else if(name.equals(ACTION_WEST)){
            return 3;
        }
        else if(name.equals(ACTION_PICKUP)){
            return 4;
        }
        else if(name.equals(ACTION_DROPOFF)){
            return 5;
        }
        else if(name.equals(ACTION_FILLUP)){
            return 6;
        }
        throw new RuntimeException("Unknown action " + name);
    }


    protected static boolean wallEast(int tx, int ty, TaxiMapWall wall){
        int wallo = wall.wallOffset;
        if(wallo == tx+1){
            int wallmin = wall.wallMin;
            int wallmax = wall.wallMax;
            return ty >= wallmin && ty < wallmax;
        }
        return false;
    }

    protected static boolean wallWest(int tx, int ty, TaxiMapWall wall){
        int wallo = wall.wallOffset;
        if(wallo == tx){
            int wallmin = wall.wallMin;
            int wallmax = wall.wallMax;
            return ty >= wallmin && ty < wallmax;
        }
        return false;
    }


    protected static boolean wallNorth(int tx, int ty, TaxiMapWall wall){
        int wallo = wall.wallOffset;
        if(wallo == ty+1){
            int wallmin = wall.wallMin;
            int wallmax = wall.wallMax;
            return tx >= wallmin && tx < wallmax;
        }
        return false;
    }

    protected static boolean wallSouth(int tx, int ty, TaxiMapWall wall){
        int wallo = wall.wallOffset;
        if(wallo == ty){
            int wallmin = wall.wallMin;
            int wallmax = wall.wallMax;
            return tx >= wallmin && tx < wallmax;
        }
        return false;
    }



    public static State getClassicState(Domain domain, boolean usesFuel){

        TaxiAgent taxiAgent = new TaxiAgent(TAXICLASS+0,0,3);

        TaxiPassenger p1 = new TaxiPassenger(PASSENGERCLASS+0,3, 0, RED);

        TaxiLocation l0 = new TaxiLocation(0, 0,LOCATIONCLASS+0,YELLOW);
        TaxiLocation l1 = new TaxiLocation(0, 4,LOCATIONCLASS+1,RED);
        TaxiLocation l2 = new TaxiLocation(3, 0,LOCATIONCLASS+2,BLUE);
        TaxiLocation l3 = new TaxiLocation(4, 4,LOCATIONCLASS+3,GREEN);

        List<TaxiLocation> taxiLocations = new ArrayList<TaxiLocation>();
        List<TaxiPassenger> taxiPassengers= new ArrayList<TaxiPassenger>();

        if(usesFuel){
            TaxiLocation lFuel = new TaxiLocation(2,1,LOCATIONCLASS+4,FUEL);
            taxiLocations.add(lFuel);
        }
        taxiLocations.add(l0);
        taxiLocations.add(l1);
        taxiLocations.add(l2);
        taxiLocations.add(l3);

        taxiPassengers.add(p1);

        TaxiMapWall h1 = new TaxiMapWall(HWALLCLASS+0,0, 5, 0,false);
        TaxiMapWall h2 = new TaxiMapWall(HWALLCLASS+1,0, 5, 5,false);

        TaxiMapWall v1 = new TaxiMapWall(VWALLCLASS+0,0, 5, 0,true);
        TaxiMapWall v2 = new TaxiMapWall(VWALLCLASS+1,0, 5, 5,true);
        TaxiMapWall v3 = new TaxiMapWall(VWALLCLASS+2,0, 2, 1,true);
        TaxiMapWall v4 = new TaxiMapWall(VWALLCLASS+3,3, 5, 2,true);
        TaxiMapWall v5 = new TaxiMapWall(VWALLCLASS+4,0, 2, 3,true);

        List<TaxiMapWall> walls = new ArrayList<TaxiMapWall>();
        walls.add(h1);
        walls.add(h2);
        walls.add(v1);
        walls.add(v2);
        walls.add(v3);
        walls.add(v4);
        walls.add(v5);


        State s = new TaxiState(walls,taxiPassengers,taxiLocations,taxiAgent);


        return s;

    }

    public static State getComplexState(boolean usesFuel){

        TaxiAgent taxiAgent = new TaxiAgent(TAXICLASS+0,0,3);

        TaxiPassenger p1 = new TaxiPassenger(PASSENGERCLASS+0,0, 0, BLUE);
        TaxiPassenger p2 = new TaxiPassenger(PASSENGERCLASS+1,3, 0, GREEN);

        TaxiLocation l0 = new TaxiLocation(0, 0,LOCATIONCLASS+0,YELLOW);
        TaxiLocation l1 = new TaxiLocation(0, 4,LOCATIONCLASS+1,RED);
        TaxiLocation l2 = new TaxiLocation(3, 0,LOCATIONCLASS+2,BLUE);
        TaxiLocation l3 = new TaxiLocation(4, 4,LOCATIONCLASS+3,GREEN);

        List<TaxiLocation> taxiLocations = new ArrayList<TaxiLocation>();
        List<TaxiPassenger> taxiPassengers= new ArrayList<TaxiPassenger>();

        if(usesFuel){
            TaxiLocation lFuel = new TaxiLocation(2,1,LOCATIONCLASS+4,FUEL);
            taxiLocations.add(lFuel);
        }
        taxiLocations.add(l0);
        taxiLocations.add(l1);
        taxiLocations.add(l2);
        taxiLocations.add(l3);

        taxiPassengers.add(p1);
        taxiPassengers.add(p2);

        TaxiMapWall h1 = new TaxiMapWall(HWALLCLASS+0,0, 5, 0,false);
        TaxiMapWall h2 = new TaxiMapWall(HWALLCLASS+1,0, 5, 5,false);

        TaxiMapWall v1 = new TaxiMapWall(VWALLCLASS+0,0, 5, 0,true);
        TaxiMapWall v2 = new TaxiMapWall(VWALLCLASS+1,0, 5, 5,true);
        TaxiMapWall v3 = new TaxiMapWall(VWALLCLASS+2,0, 2, 1,true);
        TaxiMapWall v4 = new TaxiMapWall(VWALLCLASS+3,3, 5, 2,true);
        TaxiMapWall v5 = new TaxiMapWall(VWALLCLASS+4,0, 2, 3,true);

        List<TaxiMapWall> walls = new ArrayList<TaxiMapWall>();
        walls.add(h1);
        walls.add(h2);
        walls.add(v1);
        walls.add(v2);
        walls.add(v3);
        walls.add(v4);
        walls.add(v5);


        State s = new TaxiState(walls,taxiPassengers,taxiLocations,taxiAgent);


        return s;



    }

    public static State getRandomClassicState(Random rand, Domain domain, boolean usesFuel){

        TaxiAgent taxiAgent = new TaxiAgent(TAXICLASS+0, rand.nextInt(maxX),rand.nextInt(maxY));


        TaxiLocation l0 = new TaxiLocation(0, 0,LOCATIONCLASS+0,YELLOW);
        TaxiLocation l1 = new TaxiLocation(0, 4,LOCATIONCLASS+1,RED);
        TaxiLocation l2 = new TaxiLocation(3, 0,LOCATIONCLASS+2,BLUE);
        TaxiLocation l3 = new TaxiLocation(4, 4,LOCATIONCLASS+3,GREEN);

        List<TaxiLocation> taxiLocations = new ArrayList<TaxiLocation>();
        List<TaxiPassenger> taxiPassengers= new ArrayList<TaxiPassenger>();

        if(usesFuel){
            TaxiLocation lFuel = new TaxiLocation(2,1,LOCATIONCLASS+4,FUEL);
            taxiLocations.add(lFuel);
        }
        taxiLocations.add(l0);
        taxiLocations.add(l1);
        taxiLocations.add(l2);
        taxiLocations.add(l3);

        TaxiLocation tempStartLocation = taxiLocations.get(rand.nextInt(taxiLocations.size()));
        TaxiLocation tempGoalLocation = taxiLocations.get(rand.nextInt(taxiLocations.size()));


        TaxiPassenger p1 = new TaxiPassenger(PASSENGERCLASS+0,tempStartLocation.x, tempStartLocation.y, tempGoalLocation.colour);
        taxiPassengers.add(p1);

        TaxiMapWall h1 = new TaxiMapWall(HWALLCLASS+0,0, 5, 0,false);
        TaxiMapWall h2 = new TaxiMapWall(HWALLCLASS+1,0, 5, 5,false);

        TaxiMapWall v1 = new TaxiMapWall(VWALLCLASS+0,0, 5, 0,true);
        TaxiMapWall v2 = new TaxiMapWall(VWALLCLASS+1,0, 5, 5,true);
        TaxiMapWall v3 = new TaxiMapWall(VWALLCLASS+2,0, 2, 1,true);
        TaxiMapWall v4 = new TaxiMapWall(VWALLCLASS+3,3, 5, 2,true);
        TaxiMapWall v5 = new TaxiMapWall(VWALLCLASS+4,0, 2, 3,true);

        List<TaxiMapWall> walls = new ArrayList<TaxiMapWall>();
        walls.add(h1);
        walls.add(h2);
        walls.add(v1);
        walls.add(v2);
        walls.add(v3);
        walls.add(v4);
        walls.add(v5);


        State s = new TaxiState(walls,taxiPassengers,taxiLocations,taxiAgent);


        return s;

    }

    public static State getStartStateFromTaxiPosition(int TaxiX, int TaxiY, Random rand, Domain domain, boolean usesFuel){

        TaxiAgent taxiAgent = new TaxiAgent(TAXICLASS+0, TaxiX, TaxiY);


        TaxiLocation l0 = new TaxiLocation(0, 0,LOCATIONCLASS+0,YELLOW);
        TaxiLocation l1 = new TaxiLocation(0, 4,LOCATIONCLASS+1,RED);
        TaxiLocation l2 = new TaxiLocation(3, 0,LOCATIONCLASS+2,BLUE);
        TaxiLocation l3 = new TaxiLocation(4, 4,LOCATIONCLASS+3,GREEN);

        List<TaxiLocation> taxiLocations = new ArrayList<TaxiLocation>();
        List<TaxiPassenger> taxiPassengers= new ArrayList<TaxiPassenger>();

        if(usesFuel){
            TaxiLocation lFuel = new TaxiLocation(2,1,LOCATIONCLASS+4,FUEL);
            taxiLocations.add(lFuel);
        }
        taxiLocations.add(l0);
        taxiLocations.add(l1);
        taxiLocations.add(l2);
        taxiLocations.add(l3);

        TaxiLocation tempStartLocation = taxiLocations.get(rand.nextInt(taxiLocations.size()));
        TaxiLocation tempGoalLocation = taxiLocations.get(rand.nextInt(taxiLocations.size()));


        TaxiPassenger p1 = new TaxiPassenger(PASSENGERCLASS+0,tempStartLocation.x, tempStartLocation.y, tempGoalLocation.colour);
        taxiPassengers.add(p1);

        TaxiMapWall h1 = new TaxiMapWall(HWALLCLASS+0,0, 5, 0,false);
        TaxiMapWall h2 = new TaxiMapWall(HWALLCLASS+1,0, 5, 5,false);

        TaxiMapWall v1 = new TaxiMapWall(VWALLCLASS+0,0, 5, 0,true);
        TaxiMapWall v2 = new TaxiMapWall(VWALLCLASS+1,0, 5, 5,true);
        TaxiMapWall v3 = new TaxiMapWall(VWALLCLASS+2,0, 2, 1,true);
        TaxiMapWall v4 = new TaxiMapWall(VWALLCLASS+3,3, 5, 2,true);
        TaxiMapWall v5 = new TaxiMapWall(VWALLCLASS+4,0, 2, 3,true);

        List<TaxiMapWall> walls = new ArrayList<TaxiMapWall>();
        walls.add(h1);
        walls.add(h2);
        walls.add(v1);
        walls.add(v2);
        walls.add(v3);
        walls.add(v4);
        walls.add(v5);


        State s = new TaxiState(walls,taxiPassengers,taxiLocations,taxiAgent);


        return s;

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


//        State s = TaxiDomain.getComplexState(false);


        double discount = 0.99;
        SimpleHashableStateFactory shf = new SimpleHashableStateFactory(false);

        if(false) {
            State s = TaxiDomain.getRandomClassicState(rand, td, false);
            BoundedRTDP brtdp = new BoundedRTDP(td, discount, shf,
                    new ConstantValueFunction(0.),
                    new ConstantValueFunction(1.),
                    0.1,
                    100);


//            td.getModel();

            brtdp.setModel(td.getModel());
            List<Episode> eaList = new ArrayList<Episode>();


            for(int i=0;i<1;i++) {
                Policy p = brtdp.planFromState(s);


                Episode ea = PolicyUtils.rollout(p, s, td.getModel());
                boolean testFlag=false;
                for(int j=1;j<ea.stateSequence.size();j++){
                    TaxiState oldState = (TaxiState) ea.stateSequence.get(j-1);
                    TaxiState currentState = (TaxiState) ea.stateSequence.get(j);
                    String oldGoal = ((TaxiPassenger)oldState.objectsOfClass(PASSENGERCLASS).get(0)).goalLocation;
                    String newGoal = ((TaxiPassenger)currentState.objectsOfClass(PASSENGERCLASS).get(0)).goalLocation;
                    if(!newGoal.equals(oldGoal)){
                        testFlag = true;

                    }

                }




//                if(testFlag) {
                Visualizer v = TaxiVisualizer.getVisualizer(5, 5);
                new EpisodeSequenceVisualizer(v, td, Arrays.asList(ea));
//                    System.out.println("in test: " + i);
//                }
                System.out.println("numSteps: " +ea.actionSequence.size() + ", reward sum: " + ea.discountedReturn(1.));

            }

        }
        if(false){
            //measure how well the average cost
            /**
             *
             */
            //sample start state


            //create terminal state with a terminal function
            int count =0;
            double sum =0.;
            List<Episode> eaList = new ArrayList<Episode>();
            for(int startx = 0;startx<maxX;startx++){
                for(int starty = 0;starty<maxY;starty++){
//            int startx =0;
//            int starty =0;
                    State startState = getStartStateFromTaxiPosition(startx,starty, rand, td, false);
//            State startStateClassic = getClassicState(td, false);
//            ObjectInstance location = ((TaxiState)startState).locations.get(3);
//            System.out.println(((TaxiLocation)location).colour);
                    for(ObjectInstance location:((TaxiState)startState).locations ){
                        TerminalFunction tfTemp = new TaxiToLocationTerminationFunction((TaxiLocation)location);
                        RewardFunction rfTemp = new UniformCostRF();
                        if(tfTemp.isTerminal(startState)){
                            count++;
                            continue;
                        }

                        FactoredModel fm = (FactoredModel )td.getModel();
                        fm.setTf(tfTemp);
                        fm.setRf(rfTemp);
                        td.setModel(fm);

                        BoundedRTDP brtdp = new BoundedRTDP(td, discount, shf,
                                new ConstantValueFunction(0.),
                                new ConstantValueFunction(1.),
                                0.1,
                                100);
                        brtdp.setModel(td.getModel());
                        Policy p = brtdp.planFromState(startState);
                        Episode ea = PolicyUtils.rollout(p, startState, td.getModel(),1000);
                        sum+=ea.actionSequence.size();
                        count++;
                        eaList.add(ea);


//                        Visualizer v = TaxiVisualizer.getVisualizer(5, 5);
//                        new EpisodeSequenceVisualizer(v, td, Arrays.asList(ea));
                    }
                }
            }

                        Visualizer v = TaxiVisualizer.getVisualizer(5, 5);
                        new EpisodeSequenceVisualizer(v, td, eaList);

            System.out.println("average steps per start configuration: " + sum/count);
            System.out.println("number of start states: " + count);

            int count2=0;
            double sum2 =0.;
            State exampleStartState = getStartStateFromTaxiPosition(0,0, rand, td, false);
            for(ObjectInstance locationStart:((TaxiState)exampleStartState).locations ){

                State startState = getStartStateFromTaxiPosition(((TaxiLocation)locationStart).x,((TaxiLocation)locationStart).y, rand, td, false);
//            ObjectInstance location = ((TaxiState)startState).locations.get(3);
//            System.out.println(((TaxiLocation)location).colour);
                for(ObjectInstance endLocation:((TaxiState)startState).locations ){
                    TerminalFunction tfTemp = new TaxiToLocationTerminationFunction((TaxiLocation)endLocation);
                    RewardFunction rfTemp = new UniformCostRF();
                    if(tfTemp.isTerminal(startState)){
                        count2++;
                        continue;
                    }

                    FactoredModel fm = (FactoredModel )td.getModel();
                    fm.setTf(tfTemp);
                    fm.setRf(rfTemp);
                    td.setModel(fm);

                    BoundedRTDP brtdp = new BoundedRTDP(td, discount, shf,
                            new ConstantValueFunction(0.),
                            new ConstantValueFunction(1.),
                            0.1,
                            100);
                    brtdp.setModel(td.getModel());
                    Policy p = brtdp.planFromState(startState);
                    Episode ea = PolicyUtils.rollout(p, startState, td.getModel(),1000);
                    sum2+=ea.actionSequence.size();
                    count2++;

//                        Visualizer v = TaxiVisualizer.getVisualizer(5, 5);
//                        new EpisodeSequenceVisualizer(v, td, Arrays.asList(ea));
                }

            }

            System.out.println("average steps per start configuration: " + sum2/count2);
            System.out.println("number of start states: " + count2);


            System.out.println("average steps per start configuration: " + sum/count);
            System.out.println("number of start states: " + count);




        }

        if(true) {

            int numberOfTests = 2;
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
                State s = TaxiDomain.getRandomClassicState(rand, td, false);
//                SimulatedEnvironment env = new SimulatedEnvironment(td, s);
                List<Episode> episodesQ = new ArrayList<Episode>();
//                QLearning q = new QLearning(td, 0.95, new SimpleHashableStateFactory(), 0.123, 0.35);
//                q.setLearningPolicy(new EpsilonGreedy(q, 0.2));
//                q.setLearningPolicy(new BoltzmannQPolicyWithCoolingSchedule(q,50,0.460));

                QLearning q = new QLearning(td, 0.95, new SimpleHashableStateFactory(), 0.123, 0.25);
                q.setLearningPolicy(new BoltzmannQPolicyWithCoolingSchedule(q,50,0.9879));

//
                for (int j = 1; j <= numberOfLearningEpisodes; j++) {
                    System.out.println("test: " +i+", "+ "learning episode: " + j);
                    System.out.println("-------------------------------------------------------------");
                    State sNew = TaxiDomain.getRandomClassicState(rand, td, false);
                    SimulatedEnvironment envN = new SimulatedEnvironment(td, sNew);
                    Episode ea = q.runLearningEpisode(envN);
                    episodesQ.add(ea);
//                    env.resetEnvironment();
                    if (j >=startTest && j % takeModOf == 0) {
                        System.out.println("test episode: " + j / takeModOf);
                        System.out.println("-------------------------------------------------------------");
                        Episode ea1 = PolicyUtils.rollout(new GreedyQPolicy(q), s, td.getModel(),1000);//.evaluateBehavior(env,100);
//                    episodesQ.add(ea1);
                        stepsTestQLearning[i][(j / takeModOf)-1] = ea1.actionSequence.size();
                        rewardsTestQLearning[i][(j / takeModOf)-1] = ea1.discountedReturn(1.0);
//                        env.resetEnvironment();
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
    }
}
