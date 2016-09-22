package amdp.cleanup;

import amdp.amdpframework.GroundedPropSC;
import amdp.cleanup.state.*;
import burlap.behavior.policy.Policy;
import burlap.behavior.policy.PolicyUtils;
import burlap.behavior.singleagent.Episode;
import burlap.behavior.singleagent.auxiliary.EpisodeSequenceVisualizer;
import burlap.behavior.singleagent.planning.stochastic.rtdp.BoundedRTDP;
import burlap.behavior.valuefunction.ConstantValueFunction;
import burlap.debugtools.RandomFactory;
import burlap.mdp.auxiliary.DomainGenerator;
import burlap.mdp.auxiliary.common.GoalConditionTF;
import burlap.mdp.auxiliary.common.NullTermination;
import burlap.mdp.auxiliary.stateconditiontest.StateConditionTest;
import burlap.mdp.core.Domain;
import burlap.mdp.core.StateTransitionProb;
import burlap.mdp.core.TerminalFunction;
import burlap.mdp.core.action.Action;
import burlap.mdp.core.action.ActionType;
import burlap.mdp.core.action.UniversalActionType;
import burlap.mdp.core.oo.OODomain;
import burlap.mdp.core.oo.propositional.GroundedProp;
import burlap.mdp.core.oo.propositional.PropositionalFunction;
import burlap.mdp.core.oo.state.OOState;
import burlap.mdp.core.oo.state.ObjectInstance;
import burlap.mdp.core.state.MutableState;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.common.GoalBasedRF;
import burlap.mdp.singleagent.common.UniformCostRF;
import burlap.mdp.singleagent.environment.EnvironmentOutcome;
import burlap.mdp.singleagent.model.FactoredModel;
import burlap.mdp.singleagent.model.RewardFunction;
import burlap.mdp.singleagent.model.statemodel.FullStateModel;
import burlap.mdp.singleagent.oo.OOSADomain;
import burlap.shell.visual.VisualExplorer;
import burlap.statehashing.simple.SimpleHashableStateFactory;
import burlap.visualizer.Visualizer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import static burlap.domain.singleagent.mountaincar.MountainCar.ATT_X;

/**
 * Created by ngopalan on 8/24/16.
 */
public class CleanupDomain implements DomainGenerator {


    public static final String VAR_X = "x";
    public static final String VAR_Y = "y";
    public static final String VAR_DIR = "direction"; //optionally added attribute to include the agent's direction
    public static final String VAR_TOP = "top";
    public static final String VAR_LEFT = "left";
    public static final String VAR_BOTTOM = "bottom";
    public static final String VAR_RIGHT = "right";
    public static final String VAR_COLOUR = "colour";
    public static final String VAR_SHAPE = "shape";
    public static final String VAR_LOCKED = "locked";
    public static final String VAR_CAN_BE_LOCKED = "canBeLocked";


    public static final String CLASS_AGENT = "agent";
    public static final String CLASS_BLOCK = "block";
    public static final String CLASS_ROOM = "room";
    public static final String CLASS_DOOR = "door";


    public static final String ACTION_NORTH = "north";
    public static final String ACTION_SOUTH = "south";
    public static final String ACTION_EAST = "east";
    public static final String ACTION_WEST = "west";
    public static final String ACTION_PULL = "pull";

    public static final String PF_AGENT_IN_ROOM = "agentInRoom";
    public static final String PF_BLOCK_IN_ROOM = "blockInRoom";
    public static final String PF_AGENT_IN_DOOR = "agentInDoor";
    public static final String PF_BLOCK_IN_DOOR = "blockInDoor";

    public static final String PF_WALL_NORTH = "wallNorth";
    public static final String PF_WALL_SOUTH = "wallSouth";
    public static final String PF_WALL_EAST = "wallEast";
    public static final String PF_WALL_WEST = "wallWest";


    public static final String[] COLORS = new String[]{"blue",
            "green", "magenta",
            "red", "yellow"};

    public static final String[] SHAPES = new String[]{"chair", "bag",
            "backpack", "basket"};


    public static final String[] DIRECTIONS = new String[]{"north", "south", "east", "west"};

    public static final String[] LOCKABLE_STATES = new String[]{"unknown", "unlocked", "locked"};

    protected static final String PF_RCOLORBASE = "roomIs";
    protected static final String PF_BCOLORBASE = "blockIs";
    protected static final String PF_BSHAPEBASE = "shape";


    private static RewardFunction rf;
    private static TerminalFunction tf;
    private static Random rand = RandomFactory.getMapped(0);




    protected int								maxX = 24;
    protected int								maxY = 24;
    protected boolean							includeDirectionAttribute = false;
    protected boolean							includePullAction = false;
    protected boolean							includeWallPF_s = false;
    protected boolean							lockableDoors = false;
    protected double							lockProb = 0.5;


    public void includeWallPF_s(boolean includeWallPF_s){
        this.includeWallPF_s = includeWallPF_s;
    }

    public void includePullAction(boolean includePullAction){
        this.includePullAction = includePullAction;
    }

    public void includeLockableDoors(boolean lockableDoors){
        this.lockableDoors = lockableDoors;
    }

    public void setLockProbability(double lockProb){
        this.lockProb = lockProb;
    }

    public void includeDirectionAttribute(boolean includeDirectionAttribute){
        this.includeDirectionAttribute = includeDirectionAttribute;
    }

    public CleanupDomain(RewardFunction rf, TerminalFunction tf, Random rand){
        this.rf = rf;
        this.tf = tf;
        this.rand = rand;
    }
    public CleanupDomain(){};

    @Override
    public OOSADomain generateDomain() {
        OOSADomain d = new OOSADomain();


        d.addStateClass(CLASS_AGENT, CleanupAgent.class).addStateClass(CLASS_BLOCK, CleanupBlock.class)
                .addStateClass(CLASS_DOOR, CleanupDoor.class).addStateClass(CLASS_ROOM, CleanupRoom.class);


        if(rf == null){
            rf = new UniformCostRF();
        }
        if(tf == null){
            tf = new NullTermination();
        }

        CleanupModel smodel = new CleanupModel(rand, lockProb);
        FactoredModel model = new FactoredModel(smodel, rf, tf);
        d.setModel(model);

        if(includePullAction) {
            d.addActionTypes(
                    new UniversalActionType(ACTION_NORTH),
                    new UniversalActionType(ACTION_SOUTH),
                    new UniversalActionType(ACTION_EAST),
                    new UniversalActionType(ACTION_WEST),
                    new PullActionType(ACTION_PULL));
        }
        else{
            d.addActionTypes(
                    new UniversalActionType(ACTION_NORTH),
                    new UniversalActionType(ACTION_SOUTH),
                    new UniversalActionType(ACTION_EAST),
                    new UniversalActionType(ACTION_WEST));
        }


        List<PropositionalFunction> pfs = new ArrayList<PropositionalFunction>();


        pfs.add(new PF_InRegion(PF_AGENT_IN_ROOM, new String[]{CLASS_AGENT, CLASS_ROOM}, false));
        pfs.add(new PF_InRegion(PF_BLOCK_IN_ROOM, new String[]{CLASS_BLOCK, CLASS_ROOM}, false));

        pfs.add(new PF_InRegion(PF_AGENT_IN_DOOR, new String[]{CLASS_AGENT, CLASS_DOOR}, true));
        pfs.add(new PF_InRegion(PF_BLOCK_IN_DOOR, new String[]{CLASS_BLOCK, CLASS_DOOR}, true));

        for(String col : COLORS){
            pfs.add(new PF_IsColor(PF_RoomColorName(col),  new String[]{CLASS_ROOM}, col));
            pfs.add(new PF_IsColor(PF_BlockColorName(col),  new String[]{CLASS_BLOCK}, col));
        }

        for(String shape : SHAPES){
            pfs.add(new PF_IsShape(PF_BlockShapeName(shape),  new String[]{CLASS_BLOCK}, shape));
        }

        if(this.includeWallPF_s){
            pfs.add(new PF_WallTest(PF_WALL_NORTH, d, 0, 1));
            pfs.add(new PF_WallTest(PF_WALL_SOUTH, d, 0, -1));
            pfs.add(new PF_WallTest(PF_WALL_EAST, d, 1, 0));
            pfs.add(new PF_WallTest(PF_WALL_WEST, d, -1, 0));
        }



        OODomain.Helper.addPfsToDomain(d,pfs);
        return d;
    }




    public static class PullActionType implements ActionType {
        String name;

        public PullActionType(String name) {
            this.name = name;
        }

        @Override
        public String typeName() {
            return ACTION_PULL;
        }

        @Override
        public Action associatedAction(String strRep) {
            return new PullAction();
        }

        @Override
        public List<Action> allApplicableActions(State s) {
            CleanupAgent agent = ((CleanupState)s).agent;
            int ax = agent.x;
            int ay = agent.y;
            String dir = agent.currentDirection;

            if(blockToSwap(s, ax, ay, dir) != null){
                List<Action> actionList = new ArrayList<Action>();
                actionList.add(new PullAction());
                return actionList;
            }
            return new ArrayList<Action>();
        }

    }



    protected static CleanupBlock blockToSwap(State s, int ax, int ay, String dir){


        if(dir.equals(DIRECTIONS[0])){
            //north
            return blockAtPoint(s, ax, ay+1);
        }
        else if(dir.equals(DIRECTIONS[1])){
            //south
            return blockAtPoint(s, ax, ay-1);
        }
        else if(dir.equals(DIRECTIONS[2])){
            //east
            return blockAtPoint(s, ax+1, ay);
        }
        else if(dir.equals(DIRECTIONS[3])){
            return blockAtPoint(s, ax-1, ay);
        }

        return null;

    }


    protected CleanupBlock blockToSwap(State s, int ax, int ay){
        //ObjectInstance roomContaining = roomContainingPoint(s, ax, ay);
        CleanupRoom roomContaining = (CleanupRoom)regionContainingPoint(((CleanupState)s).objectsOfClass(CLASS_ROOM), ax, ay, true);


        CleanupBlock blockToSwap = null;
        //check if there is a block against the wall to the north south east or west
        if(wallAt(s, roomContaining, ax, ay+2)){
            blockToSwap = blockAtPoint(s, ax, ay+1);
            if(blockToSwap != null){
                return blockToSwap;
            }
        }
        if(wallAt(s, roomContaining, ax, ay-2)){
            blockToSwap = blockAtPoint(s, ax, ay-1);
            if(blockToSwap != null){
                return blockToSwap;
            }
        }
        if(wallAt(s, roomContaining, ax+2, ay)){
            blockToSwap = blockAtPoint(s, ax+1, ay);
            if(blockToSwap != null){
                return blockToSwap;
            }
        }
        if(wallAt(s, roomContaining, ax-2, ay)){
            blockToSwap = blockAtPoint(s, ax-1, ay);
            if(blockToSwap != null){
                return blockToSwap;
            }
        }



        return blockToSwap;
    }


    public static boolean wallAt(State s, ObjectInstance r, int x, int y){

        int top = (Integer)r.get(VAR_TOP);
        int left = (Integer)r.get(VAR_LEFT);
        int bottom = (Integer)r.get(VAR_BOTTOM);
        int right = (Integer) r.get(VAR_RIGHT);

        //agent along wall of room check
        if(((x == left || x == right) && y >= bottom && y <= top) || ((y == bottom || y == top) && x >= left && x <= right)){

            //then only way for this to be a valid pos is if a door contains this point
            ObjectInstance door = doorContainingPoint(s, x, y);
            if(door == null){
                return true;
            }

        }

        return false;
    }


    public static CleanupRoom roomContainingPoint(State s, int x, int y){
        List<ObjectInstance> rooms = ((CleanupState) s).objectsOfClass(CLASS_ROOM);
        return (CleanupRoom)regionContainingPoint(rooms, x, y, false);
    }

    public static CleanupRoom roomContainingPointIncludingBorder(State s, int x, int y){
        List<ObjectInstance> rooms = ((CleanupState) s).objectsOfClass(CLASS_ROOM);
        return (CleanupRoom)regionContainingPoint(rooms, x, y, true);
    }

    public static CleanupDoor doorContainingPoint(State s, int x, int y){
        List<ObjectInstance> doors = ((CleanupState) s).objectsOfClass(CLASS_DOOR);
        return (CleanupDoor)regionContainingPoint(doors, x, y, true);
    }

    protected static ObjectInstance regionContainingPoint(List <ObjectInstance> objects, int x, int y, boolean countBoundary){
        for(ObjectInstance o : objects){
            if(regionContainsPoint(o, x, y, countBoundary)){
                return o;
            }

        }

        return null;
    }

    public static boolean regionContainsPoint(ObjectInstance o, int x, int y, boolean countBoundary){
        int top = (Integer) o.get(VAR_TOP);
        int left = (Integer)o.get(VAR_LEFT);
        int bottom = (Integer)o.get(VAR_BOTTOM);
        int right = (Integer)o.get(VAR_RIGHT);

        if(countBoundary){
            if(y >= bottom && y <= top && x >= left && x <= right){
                return true;
            }
        }
        else{
            if(y > bottom && y < top && x > left && x < right){
                return true;
            }
        }

        return false;
    }

    public static CleanupBlock blockAtPoint(State s, int x, int y){

        List<CleanupBlock> blocks = ((CleanupState)s).blocks;
        for(CleanupBlock b : blocks){
            int bx = b.x;
            int by = b.y;

            if(bx == x && by == y){
                return b;
            }
        }

        return null;

    }



    public static class PullAction implements Action {


        public PullAction() {
        }

        @Override
        public String actionName() {
            return ACTION_PULL;
        }

        @Override
        public Action copy() {
            return new PullAction();
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            PullAction that = (PullAction)o;
            if(this.actionName().equals((that).actionName()))
            {return true;}

            return false;

        }

        @Override
        public int hashCode() {
            String str = ACTION_PULL;
            return str.hashCode();
        }

        @Override
        public String toString() {
            return this.actionName();
        }
    }



    public static class PF_InRegion extends PropositionalFunction {

        protected boolean countBoundary;

        public PF_InRegion(String name, String [] params, boolean countBoundary){
            super(name, params);
            this.countBoundary = countBoundary;
        }

        @Override
        public boolean isTrue(OOState s, String... params) {

            ObjectInstance o = ((CleanupState)s).object(params[0]);
            int x = (Integer) o.get(VAR_X);
            int y = (Integer)o.get(VAR_Y);


            ObjectInstance region = s.object(params[1]);
            return regionContainsPoint(region, x, y, countBoundary);

        }

    }


    public static class PF_IsColor extends PropositionalFunction{

        protected String colorName;

        public PF_IsColor(String name, String [] params, String color){
            super(name, params);
            this.colorName = color;
        }

        @Override
        public boolean isTrue(OOState s, String[] params) {

            ObjectInstance o = s.object(params[0]);
            String col = (String) o.get(VAR_COLOUR);

            return this.colorName.equals(col);

        }

    }


    public static class PF_IsShape extends PropositionalFunction{

        protected String shapeName;

        public PF_IsShape(String name, String [] params, String shape){
            super(name, params);
            this.shapeName = shape;
        }

        @Override
        public boolean isTrue(OOState s, String[] params) {
            ObjectInstance o = s.object(params[0]);
            String shape = (String) o.get(VAR_SHAPE);

            return this.shapeName.equals(shape);
        }



    }


    public static class PF_WallTest extends PropositionalFunction{

        protected int dx;
        protected int dy;

        public PF_WallTest(String name, Domain domain, int dx, int dy){
            super(name, new String[]{});
            this.dx = dx;
            this.dy = dy;
        }

        @Override
        public boolean isTrue(OOState s, String[] params) {
            CleanupAgent agent = ((CleanupState)s).agent;
            int ax = agent.x;
            int ay = agent.y;
            ObjectInstance agentRoom = roomContainingPoint(s, ax, ay);
            if(agentRoom == null){
                return false;
            }
            return wallAt(s, agentRoom, ax+this.dx, ay+this.dy);

        }



    }


    public static String PF_RoomColorName(String color){
        String capped = firstLetterCapped(color);
        return PF_RCOLORBASE + capped;
    }
    public static String PF_BlockColorName(String color){
        String capped = firstLetterCapped(color);
        return PF_BCOLORBASE + capped;
    }
    public static String PF_BlockShapeName(String shape){
        String capped = firstLetterCapped(shape);
        return PF_BSHAPEBASE + capped;
    }

    protected static String firstLetterCapped(String s){
        // capitalizes the first letter of the word :)
        String firstLetter = s.substring(0, 1);
        String remainder = s.substring(1);
        return firstLetter.toUpperCase() + remainder;
    }



    public static State getState(boolean includeDirectionAttribute, boolean lockableDoors, int numObjects, int numRooms){

        // these doors can be locked
        // the number of objects

        int y1 = 3;
        int y2 = 7;
        int y3 = 12;

        int x1 = 4;
        int x2 = 8;
        int x3 = 12;


        CleanupRoom r1 = new CleanupRoom(CLASS_ROOM+0,y2, x1, 0, x2, "red");
        CleanupRoom r2 = new CleanupRoom(CLASS_ROOM+1, y2, 0, y1, x1, "blue");
        CleanupRoom r3 = new CleanupRoom(CLASS_ROOM+2, y3, 0, y2, x3, "green");
        CleanupRoom r4 = new CleanupRoom(CLASS_ROOM+3, y2, x2, 0, x3, "yellow");
        List<CleanupRoom> rooms = new ArrayList<CleanupRoom>();
        rooms.add(r1);

        rooms.add(r3);
        rooms.add(r4);
        if(numRooms>=4){
            rooms.add(r2);
        }

        CleanupDoor d1 = new CleanupDoor(CLASS_DOOR+0,0,1, x2, 1, x2,lockableDoors);
        CleanupDoor d2 = new CleanupDoor(CLASS_DOOR+1,0,5, x1, 5, x1,lockableDoors);
        CleanupDoor d3 = new CleanupDoor(CLASS_DOOR+2,0,y2, 2, y2, 2,lockableDoors);
        CleanupDoor d4 = new CleanupDoor(CLASS_DOOR+3,0,y2, 10, y2, 10,lockableDoors);
        List<CleanupDoor> doors = new ArrayList<CleanupDoor>();
        doors.add(d1);
        doors.add(d4);
        if(numRooms>=4){
            doors.add(d2);
            doors.add(d3);
        }

        CleanupAgent agent = new CleanupAgent(CLASS_AGENT+0, 7, 1);
        if(includeDirectionAttribute){
            agent.directional = true;
            agent.currentDirection = "south";
        }

        CleanupBlock block1 = new CleanupBlock(CLASS_BLOCK+0,5, 4,"chair", "blue");
        CleanupBlock block2 = new CleanupBlock(CLASS_BLOCK+1,6,10,"basket", "red");
        CleanupBlock block3 = new CleanupBlock(CLASS_BLOCK+2,2,10,"bag", "magenta");

        List<CleanupBlock> blocks = new ArrayList<CleanupBlock>();
        blocks.add(block1);

        if(numObjects>=2)        blocks.add(block2);
        if(numObjects>=3)        blocks.add(block3);


        CleanupState s = new CleanupState(agent,blocks, doors, rooms);
        return s;

    }


    public static State getClassicState(boolean includeDirectionAttribute){

        CleanupRoom r1 = new CleanupRoom(CLASS_ROOM+0,4, 0, 0, 8,"red");
        CleanupRoom r2 = new CleanupRoom(CLASS_ROOM+1,8, 0, 4, 4, "green");
        CleanupRoom r3 = new CleanupRoom(CLASS_ROOM+2, 8, 4, 4, 8, "blue");
        List<CleanupRoom> rooms = new ArrayList<CleanupRoom>();
        rooms.add(r1);
        rooms.add(r2);
        rooms.add(r3);
//        setRoom(s, 0, 4, 0, 0, 8, "red");
//        setRoom(s, 1, 8, 0, 4, 4, "green");
//        setRoom(s, 2, 8, 4, 4, 8, "blue");

        CleanupDoor d1 = new CleanupDoor(CLASS_DOOR+0,0,4, 6, 4, 6,false);
        CleanupDoor d2 = new CleanupDoor(CLASS_DOOR+1,0,4, 2, 4, 2,false);
        List<CleanupDoor> doors = new ArrayList<CleanupDoor>();
        doors.add(d1);
        doors.add(d2);

//        setDoor(s, 0, 4, 6, 4, 6);
//        setDoor(s, 1, 4, 2, 4, 2);


        CleanupAgent agent = new CleanupAgent(CLASS_AGENT+0, 6, 6);
        if(includeDirectionAttribute){
            agent.directional = true;
            agent.currentDirection = "south";
        }

        CleanupBlock block1 = new CleanupBlock(CLASS_BLOCK+0,2,2,"basket", "red");
        List<CleanupBlock> blocks = new ArrayList<CleanupBlock>();
        blocks.add(block1);

//        setAgent(s, 6, 6);
//        setBlock(s, 0, 2, 2, "basket", "red");


        CleanupState s = new CleanupState(agent,blocks, doors, rooms);
        return s;

    }





    public static int maxRoomXExtent(State s){

        int max = 0;
        List<CleanupRoom> rooms = ((CleanupState)s).rooms;
        for(CleanupRoom r : rooms){
            int right = r.right;
            if(right > max){
                max = right;
            }
        }

        return max;
    }

    public static int maxRoomYExtent(State s){

        int max = 0;
        List<CleanupRoom> rooms = ((CleanupState)s).rooms;
        for(CleanupRoom r : rooms){
            int top = r.top;
            if(top > max){
                max = top;
            }
        }

        return max;
    }




    public static void main(String [] args){
        boolean runGroundLevelBoundedRTDP = true;
        Random rand = RandomFactory.getMapped(0);

        if(false){
            double lockProb = 0.5;

            PropositionalFunction pf = new PF_InRegion(CleanupDomain.PF_BLOCK_IN_ROOM, new String[]{CLASS_BLOCK, CLASS_ROOM}, false);


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

            FixedDoorCleanupEnv env = new FixedDoorCleanupEnv(domain, s);
            EnvironmentOutcome eo0 = env.executeAction(domain.getAction(ACTION_NORTH).allApplicableActions(s).get(0));
            EnvironmentOutcome eo1 = env.executeAction(domain.getAction(ACTION_SOUTH).allApplicableActions(s).get(0));
            EnvironmentOutcome eo2 = env.executeAction(domain.getAction(ACTION_EAST).allApplicableActions(s).get(0));
            EnvironmentOutcome eo3 = env.executeAction(domain.getAction(ACTION_WEST).allApplicableActions(s).get(0));

            Episode ea = new Episode(s);
            ea.transition(eo0);
            ea.transition(eo1);
            ea.transition(eo2);
            ea.transition(eo3);
            Visualizer v = CleanupVisualiser.getVisualizer("amdp/data/resources/robotImages");
            //		System.out.println(ea.getState(0).toString());
            new EpisodeSequenceVisualizer(v, domain, Arrays.asList(ea));


        }


        if(false){
            double lockProb = 0.5;

//            StateConditionTest sc = new GroundedPropSC(new GroundedProp(domain.getPropFunction(CleanupWorld.PF_BLOCK_IN_ROOM),  new String[]{"block0", "room1"}));

//            RewardFunction heuristicRF = new PullCostGoalRF(sc, 1., 0.);

            PropositionalFunction pf = new PF_InRegion(CleanupDomain.PF_BLOCK_IN_ROOM, new String[]{CLASS_BLOCK, CLASS_ROOM}, false);


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



            FixedDoorCleanupEnv env = new FixedDoorCleanupEnv(domain, s);


            long startTime = System.currentTimeMillis();

            ConstantValueFunction heuristic = new ConstantValueFunction(1.);//CleanupDomainDriver.getL0Heuristic(s, heuristicRF);
            BoundedRTDP brtd = new BoundedRTDP(domain, 0.99, new SimpleHashableStateFactory(false), new ConstantValueFunction(0.0), heuristic, 0.01, 500);
            brtd.setMaxRolloutDepth(50);
            brtd.toggleDebugPrinting(false);
            Policy P = brtd.planFromState(s);

            long endTime = System.currentTimeMillis();
            long duration = (endTime - startTime);
            System.out.println("total time: " + duration);
            Episode ea = PolicyUtils.rollout(P,env,100);

            Visualizer v = CleanupVisualiser.getVisualizer("amdp/data/resources/robotImages");
            //		System.out.println(ea.getState(0).toString());
            new EpisodeSequenceVisualizer(v, domain, Arrays.asList(ea));
        }
        if(true){



            CleanupDomain dgen = new CleanupDomain();
            dgen.includeDirectionAttribute(true);
            dgen.includePullAction(true);
            dgen.includeWallPF_s(true);
            dgen.includeLockableDoors(true);
            dgen.setLockProbability(0.5);
            OOSADomain domain = dgen.generateDomain();

            State s = CleanupDomain.getClassicState(true);

			/*ObjectInstance b2 = new ObjectInstance(domain.getObjectClass(CLASSBLOCK), CLASSBLOCK+1);
		s.addObject(b2);
		setBlock(s, 1, 3, 2, "moon", "red");*/

            Visualizer v = CleanupVisualiser.getVisualizer("amdp/data/resources/robotImages");
            VisualExplorer exp = new VisualExplorer(domain, v, s);

            exp.addKeyAction("w", ACTION_NORTH,"");
            exp.addKeyAction("s", ACTION_SOUTH,"");
            exp.addKeyAction("d", ACTION_EAST,"");
            exp.addKeyAction("a", ACTION_WEST,"");
            exp.addKeyAction("r", ACTION_PULL,"");

            exp.initGUI();

//            List<StateTransitionProb> tps = domain.getAction(ACTION_SOUTH).getAssociatedGroundedAction().getTransitions(s);
//            for(TransitionProbability tp : tps){
//                System.out.println(tp.s.toString());
//                System.out.println("----------------");
//            }
//
//            System.out.println("========================");
//
//            State s2 = s.copy();
//            CleanupWorld.setAgent(s2, 6, 5);
//            tps = domain.getAction(ACTION_SOUTH).getAssociatedGroundedAction().getTransitions(s2);
//            for(TransitionProbability tp : tps){
//                System.out.println(tp.s.toString());
//                System.out.println("----------------");
//            }
        }

    }



}



