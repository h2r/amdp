package amdp.cleanupturtlebot.cleanupl0Continuous;

//import amdp.cleanupturtlebot.cleanupl0discrete.state.*;
//import amdp.cleanup.state.*;
import amdp.cleanupturtlebot.cleanupl0Continuous.state.*;
import burlap.mdp.core.StateTransitionProb;
import burlap.mdp.core.action.Action;
import burlap.mdp.core.oo.state.ObjectInstance;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.model.statemodel.FullStateModel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import static amdp.cleanupturtlebot.cleanupl0Continuous.CleanupContinuousGridL0Domain.*;

/**
 * Created by ngopalan on 8/27/16.
 */
public class CleanupL0ContinuousModel implements FullStateModel {
    private static Random rand;
    private static double lockProb = 0.5;



    private double moveDelta = 1.;
    private double wallDelta = 1.;

    public CleanupL0ContinuousModel(Random rand, double lockProb){
        this.rand = rand;
        this.lockProb = lockProb;
    }


    public void setMoveDelta(double moveDelta) {
        this.moveDelta = moveDelta;
    }

    public void setWallDelta(double wallDelta) {
        this.wallDelta = wallDelta;
    }

    // the easiest planning is by the rotate and move commands
    // for move check if the neighbouring cells are empty
    //checks n>s, s>n; e>w, w>e; n>e, e>n; e>s,s>e; s>w,w>s; n>w,w>n; n>n; s>s; e>e;w>w

    @Override
    public List<StateTransitionProb> stateTransitions(State s, Action a) {
        CleanupState ns = ((CleanupState)s).copy();
        int actionInd = actionInd(a.actionName());
        if(actionInd<4){
            if(actionInd==0){

                return moveForward(ns);
            }
            else if(actionInd==1){
                //back
                return moveBackward(ns);
            }
            else if(actionInd==2){
                //turn cw
                return turnClockwise(ns);

            }
            else if(actionInd==3){
                //turn ccw
                return turnCounterClockwise(ns);

            }
        }
//        else if(actionInd==4){
//            // pull action!!
//
//            // confirmed that pull action is legal to make!!
//            CleanupContinuousAgent agent = ns.touchAgent();
//            int ax = agent.x;
//            int ay = agent.y;
//            String dir = agent.currentDirection;
//
//            CleanupContinuousBlock blockOld = blockToSwap(ns, ax, ay, dir);
//            CleanupContinuousBlock block = ns.touchBlock(ns.blockInd(blockOld.name()));
//            int bx = block.x;
//            int by = block.y;
//
//            agent.x= bx;
//            agent.y= by;
//
//            block.x= ax;
//            block.y= ay;
//
//            if(agent.directional){
//
//                //face in direction of the block movement
//                if(by - ay > 0){
//                    agent.currentDirection = "south";
//                }
//                else if(by - ay < 0){
//                    agent.currentDirection = "north";
//                }
//                else if(bx - ax > 0){
//                    agent.currentDirection = "west";
//                }
//                else if(bx - ax < 0){
//                    agent.currentDirection = "east";
//                }
//
//            }
//
//            return Arrays.asList(new StateTransitionProb(ns, 1.));
//
//        }

        throw new RuntimeException("Unknown action ind: " +actionInd );
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



    protected static int actionInd(String name){
        if(name.equals(ACTION_MOVE_FORWARD)){
            return 0;
        }
        else if(name.equals(ACTION_MOVE_BACK)){
            return 1;
        }
        else if(name.equals(ACTION_TURN_CW)){
            return 2;
        }
        else if(name.equals(ACTION_TURN_CCW)){
            return 3;
        }
//        else if(name.equals(ACTION_PULL)){
//            return 4;
//        }
        throw new RuntimeException("Unknown action " + name);
    }




    protected List<StateTransitionProb> moveForward(State state) {


        CleanupState initialState = ((CleanupState) state).copy();

        CleanupState ns = ((CleanupState) state);

        CleanupGridContinuousAgent agent = ns.touchAgent();
        double ax = agent.x;
        double ay = agent.y;

        String dir = agent.currentDirection;

        double xdelta = 0;
        double ydelta = 0;
        double wallXdelta = 0;
        double wallYdelta = 0;



        if(dir.equals("north")){
            ydelta = moveDelta;
            wallYdelta = 2*wallDelta;
        }
        else if(dir.equals("south")){
            ydelta = -1 * moveDelta;
            wallYdelta = -2 * wallDelta;
        }
        else if(dir.equals("east")){
            xdelta = moveDelta;
            wallXdelta = +2 * wallDelta;
        }
        else if(dir.equals("west")){
            xdelta = -1 * moveDelta;
            wallXdelta = -2 * wallDelta;
        }
        else if(dir.equals("notCardinal")){
            xdelta = 0;
            wallXdelta = 0;
        }
        else{
            throw new RuntimeException("Error, cannot move because of unknown direction index: " + dir);
        }

        // check for wall at x+2 and block at x+1 and move!
        double nx = ax+xdelta;
        double ny = ay+ydelta;
        // get new position

        // check for walls 2 ahead!
        double nwx = ax+wallXdelta;
        double nwy = ay+wallYdelta;

        //ObjectInstance roomContaining = roomContainingPoint(s, ax, ay);
        CleanupRoom roomContaining = (CleanupRoom)regionContainingPoint(ns.objectsOfClass(CLASS_ROOM), ax, ay, true);

        CleanupDoor conflictDoor = null;

        boolean permissibleMove = false;

        CleanupGridContinuousBlock pushedBlockOld = blockAtPoint(ns, nx, ny);


        boolean pathClear = true;

        if(pushedBlockOld!=null) {
            if (dir.equals("north") || dir.equals("south")) {
                if (Math.abs(pushedBlockOld.x - agent.x) > 0.4) {
                    pushedBlockOld = null;
                }
            } else {
                if (Math.abs(pushedBlockOld.y - agent.y) > 0.4) {
                    pushedBlockOld = null;
                }
            }

            if(blockAtPoint(ns, nwx, nwy) != null){
                if(!blockAtPoint(ns, nwx, nwy).equals(blockAtPoint(ns, nx, ny))){
                    pathClear = false;
                }
            }
        }


//        if(blockAtPoint(ns, nx, ny) != null){
//
//        }


        if(!wallAt(ns, roomContaining, (int)nwx, (int)nwy) && pathClear){//(blockAtPoint(ns, nwx, nwy) == null && blockAtPoint(ns, nx, ny) != null)){


            //is there a possible door that can be locked?
            CleanupDoor doorAtNewPointOld = doorContainingPoint(ns, (int)nwx, (int)nwy);
            if(doorAtNewPointOld != null){
                CleanupDoor doorAtNewPoint = ns.touchDoor(ns.doorInd(doorAtNewPointOld.name()));
                if(doorAtNewPoint.canBeLocked) {
                    conflictDoor = doorAtNewPoint;
                }

            }

            if(pushedBlockOld != null) {
                CleanupGridContinuousBlock pushedBlock = ns.touchBlock(ns.blockInd(pushedBlockOld.name()));
                double bx = pushedBlock.x;
                double by = pushedBlock.y;

                double nbx = bx + xdelta;
                double nby = by + ydelta;
                pushedBlock.x = nbx;
                pushedBlock.y = nby;
            }


            permissibleMove = true;


        }


        if(permissibleMove){


            //if doors are lockable, we must check whether there is special handling
//            ObjectInstance doorAtNewPointOld = doorContainingPoint(ns, nx, ny);
//
//            if(doorAtNewPointOld != null){
//                CleanupContinuousDoor doorAtNewPoint = ns.touchDoor(ns.doorInd(doorAtNewPointOld.name()));
//                if(doorAtNewPoint.canBeLocked){
//                    conflictDoor = doorAtNewPoint;
//                }
//            }

            agent.x = nx;
            agent.y=  ny;

        }


        if(conflictDoor == null){
            return Arrays.asList(new StateTransitionProb(ns, 1.));
        }
        if(conflictDoor.locked == 2){
            return Arrays.asList(new StateTransitionProb(initialState, 1.));
        }
        if(conflictDoor.locked == 0){

            conflictDoor.locked = 1; //open with touch working

            CleanupState lockedState = initialState.copy();
            lockedState.touchDoor(lockedState.doorInd(conflictDoor.name())).locked= 2;

            return Arrays.asList(new StateTransitionProb(ns, 1.-this.lockProb), new StateTransitionProb(lockedState, lockProb));
        }

        return Arrays.asList(new StateTransitionProb(ns, 1.));


    }


    protected List<StateTransitionProb> moveBackward(State state) {


        CleanupState initialState = ((CleanupState) state).copy();

        CleanupState ns = ((CleanupState) state);

        CleanupGridContinuousAgent agent = ns.touchAgent();
        double ax = agent.x;
        double ay = agent.y;

        String dir = agent.currentDirection;

        double xdelta = 0;
        double ydelta = 0;
//        double wallXdelta = 0;
//        double wallYdelta = 0;

        if(dir.equals("north")){
            ydelta = -1*moveDelta;
//            wallYdelta = 2;
        }
        else if(dir.equals("south")){
            ydelta = 1*moveDelta;
//            wallYdelta = -2;
        }
        else if(dir.equals("east")){
            xdelta = -1*moveDelta;
//            wallXdelta = +2;
        }
        else if(dir.equals("west")){
            xdelta = 1*moveDelta;
//            wallXdelta = -2;
        }
        else if(dir.equals("notCardinal")){
            xdelta = 0.;
        }
        else{
            throw new RuntimeException("Error, cannot move because of unknown direction index: " + dir);
        }

        // check for wall at x+2 and block at x+1 and move!
        double  nx = ax+xdelta;
        double ny = ay+ydelta;
        // get new position

//        // check for walls 2 ahead!
//        int nwx = ax+wallXdelta;
//        int nwy = ay+wallYdelta;

        //ObjectInstance roomContaining = roomContainingPoint(s, ax, ay);
        CleanupRoom roomContaining = (CleanupRoom)regionContainingPoint(ns.objectsOfClass(CLASS_ROOM), ax, ay, true);

        CleanupDoor conflictDoor = null;

        boolean permissibleMove = false;
        CleanupGridContinuousBlock pushedBlockOld = blockAtPoint(ns, nx, ny);


        if(pushedBlockOld != null){
            CleanupGridContinuousBlock pushedBlock = ns.touchBlock(ns.blockInd(pushedBlockOld.name()));
            double bx = pushedBlock.x;
            double by = pushedBlock.y;

            double nbx = bx + xdelta;
            double nby = by + ydelta;

            if(!wallAt(ns, roomContaining, (int)nbx, (int)nby) && blockAtPoint(ns, nbx, nby) == null){


                //is there a possible door that can be locked?
                CleanupDoor doorAtNewPointOld = doorContainingPoint(ns, (int)nbx, (int)nby);
                if(doorAtNewPointOld != null){
                    CleanupDoor doorAtNewPoint = ns.touchDoor(ns.doorInd(doorAtNewPointOld.name()));
                    if(doorAtNewPoint.canBeLocked) {
                        conflictDoor = doorAtNewPoint;
                    }

                }

                pushedBlock.x = nbx;
                pushedBlock.y = nby;
                permissibleMove = true;


            }

        }
        else if(!wallAt(ns, roomContaining, (int)nx, (int)ny)){
            permissibleMove = true;
        }

        if(permissibleMove){


            //if doors are lockable, we must check whether there is special handling
            ObjectInstance doorAtNewPointOld = doorContainingPoint(ns, (int)nx, (int)ny);

            if(doorAtNewPointOld != null){
                CleanupDoor doorAtNewPoint = ns.touchDoor(ns.doorInd(doorAtNewPointOld.name()));
                if(doorAtNewPoint.canBeLocked){
                    conflictDoor = doorAtNewPoint;
                }
            }

            agent.x = nx;
            agent.y=  ny;

        }


//        if (agent.directional) {
//            if (xdelta == 1) {
//                agent.currentDirection = "east";
//            } else if (xdelta == -1) {
//                agent.currentDirection = "west";
//            } else if (ydelta == 1) {
//                agent.currentDirection = "north";
//            } else if (ydelta == -1) {
//                agent.currentDirection = "south";
//            }
//        }

        if(conflictDoor == null){
            return Arrays.asList(new StateTransitionProb(ns, 1.));
        }
        if(conflictDoor.locked == 2){
            return Arrays.asList(new StateTransitionProb(initialState, 1.));
        }
        if(conflictDoor.locked == 0){

            conflictDoor.locked = 1; //open with touch working

            CleanupState lockedState = initialState.copy();
            lockedState.touchDoor(lockedState.doorInd(conflictDoor.name())).locked= 2;

            return Arrays.asList(new StateTransitionProb(ns, 1.-this.lockProb), new StateTransitionProb(lockedState, lockProb));
        }

        return Arrays.asList(new StateTransitionProb(ns, 1.));

    }



    protected List<StateTransitionProb> turnClockwise(State state) {


        CleanupState initialState = ((CleanupState) state).copy();

        CleanupState ns = ((CleanupState) state);

        CleanupGridContinuousAgent agent = ns.touchAgent();
        double ax = agent.x;
        double ay = agent.y;

        String dir = agent.currentDirection;

        int currentBlockLocXDelta = 0;
        int currentBlockLocYDelta = 0;

        int bxdelta = 0;
        int bydelta = 0;
//        int wallXdelta = 0;
//        int wallYdelta = 0;

        List<Double> xposForCollision = new ArrayList<Double>();
        List<Double> yposForCollision = new ArrayList<Double>();

        if(dir.equals("notCardinal")){
            //x and y remain the same dir moves to north and return don't move blocks or anything for now
            agent.currentDirection = "north";
            return Arrays.asList(new StateTransitionProb(ns, 1.));
        }

        if (dir.equals("north")) {
            // going east
            bydelta = -1;
            bxdelta = 1;

            currentBlockLocYDelta = 1;

            //collision checks
            xposForCollision.add(ax+1);
            yposForCollision.add(ay);
            xposForCollision.add(ax+1);
            yposForCollision.add(ay+1);

        } else if (dir.equals("south")) {
            // going west
            bydelta = 1;
            bxdelta = -1;

            currentBlockLocYDelta = -1;

            //collision checks
            xposForCollision.add(ax-1);
            yposForCollision.add(ay);
            xposForCollision.add(ax-1);
            yposForCollision.add(ay-1);

        } else if (dir.equals("east")) {
            // going south
            bydelta = -1;
            bxdelta = -1;
            currentBlockLocXDelta = 1;

            //collision checks
            xposForCollision.add(ax);
            yposForCollision.add(ay-1);
            xposForCollision.add(ax+1);
            yposForCollision.add(ay-1);
        } else if (dir.equals("west")) {
            // going north
            bydelta = 1;
            bxdelta = 1;
            currentBlockLocXDelta = -1;

            //collision checks
            xposForCollision.add(ax);
            yposForCollision.add(ay+1);
            xposForCollision.add(ax-1);
            yposForCollision.add(ay+1);
        } else {
            throw new RuntimeException("Error, cannot move because of unknown direction index: " + dir);
        }


        // check collision for all blocks now

        boolean possibleMove = true;

        CleanupRoom roomContaining = (CleanupRoom)regionContainingPoint(ns.objectsOfClass(CLASS_ROOM), ax, ay, true);

        if(roomContaining==null){
            possibleMove = false;
        }
        else {
            for (int i = 0; i < 2; i++) {
                int xCheck = xposForCollision.get(i).intValue();
                int yCheck = yposForCollision.get(i).intValue();


                if (wallAt(ns, roomContaining, xCheck, yCheck) || blockAtPoint(ns, xCheck, yCheck) != null) {
                    possibleMove = false;
                    break;
                }
            }
        }
        if(possibleMove){
            // check for block
            CleanupGridContinuousBlock block  = blockAtPoint(ns, ax+ currentBlockLocXDelta, ay+currentBlockLocYDelta);
            if(block!=null){
                CleanupGridContinuousBlock pushedBlock = ns.touchBlock(ns.blockInd(block.name()));
                pushedBlock.x = pushedBlock.x + bxdelta;
                pushedBlock.y = pushedBlock.y + bydelta;

            }


            // move block

            // change dir.

            if (dir.equals("north")) {
                // going east
                agent.currentDirection = "east";

            } else if (dir.equals("south")) {
                // going west
                agent.currentDirection = "west";

            } else if (dir.equals("east")) {
                // going south
                agent.currentDirection = "south";
            } else if (dir.equals("west")) {
                // going north
                agent.currentDirection = "north";
//            } else if (dir.equals("notCardinal")) {
//                // going north
//                agent.currentDirection = "north";
            }else {
                throw new RuntimeException("Error, cannot move because of unknown direction index: " + dir);
            }


        }

        return Arrays.asList(new StateTransitionProb(ns, 1.));

    }


    protected List<StateTransitionProb> turnCounterClockwise(State state) {

        CleanupState initialState = ((CleanupState) state).copy();

        CleanupState ns = ((CleanupState) state);

        CleanupGridContinuousAgent agent = ns.touchAgent();
        double ax = agent.x;
        double ay = agent.y;

        String dir = agent.currentDirection;

        int currentBlockLocXDelta = 0;
        int currentBlockLocYDelta = 0;

        int bxdelta = 0;
        int bydelta = 0;
//        int wallXdelta = 0;
//        int wallYdelta = 0;

        List<Double> xposForCollision = new ArrayList<>();
        List<Double> yposForCollision = new ArrayList<>();


        if(dir.equals("notCardinal")){
            //x and y remain the same dir moves to north and return don't move blocks or anything for now
            agent.currentDirection = "north";
            return Arrays.asList(new StateTransitionProb(ns, 1.));
        }

        if (dir.equals("north")) {
            // going west
            bydelta = -1;
            bxdelta = -1;

            currentBlockLocYDelta = 1;

            //collision checks
            xposForCollision.add(ax-1);
            yposForCollision.add(ay);
            xposForCollision.add(ax-1);
            yposForCollision.add(ay+1);

        } else if (dir.equals("south")) {
            // going east
            bydelta = 1;
            bxdelta = 1;

            currentBlockLocYDelta = -1;

            //collision checks
            xposForCollision.add(ax+1);
            yposForCollision.add(ay);
            xposForCollision.add(ax+1);
            yposForCollision.add(ay-1);

        } else if (dir.equals("east")) {
            // going north
            bydelta = 1;
            bxdelta = -1;
            currentBlockLocXDelta = 1;

            //collision checks
            xposForCollision.add(ax);
            yposForCollision.add(ay+1);
            xposForCollision.add(ax+1);
            yposForCollision.add(ay+1);
        } else if (dir.equals("west")) {
            // going south
            bydelta = -1;
            bxdelta = 1;
            currentBlockLocXDelta = -1;

            //collision checks
            xposForCollision.add(ax);
            yposForCollision.add(ay-1);
            xposForCollision.add(ax-1);
            yposForCollision.add(ay-1);
        } else {
            throw new RuntimeException("Error, cannot move because of unknown direction index: " + dir);
        }


        // check collision for all blocks now

        boolean possibleMove = true;

        CleanupRoom roomContaining = (CleanupRoom)regionContainingPoint(ns.objectsOfClass(CLASS_ROOM), ax, ay, true);

        if(roomContaining==null){
            possibleMove = false;
        }
        else {
            for (int i = 0; i < 2; i++) {
                int xCheck = xposForCollision.get(i).intValue();
                int yCheck = yposForCollision.get(i).intValue();

                // check if block here - if block then move in the direction of move

                // if east west then move all blocks to

                if (wallAt(ns, roomContaining, xCheck, yCheck) || blockAtPoint(ns, xCheck, yCheck) != null) {
                    possibleMove = false;
                    break;
                }
            }
        }
        if(possibleMove){
            // check for block
            CleanupGridContinuousBlock block  = blockAtPoint(ns, ax+ currentBlockLocXDelta, ay+currentBlockLocYDelta);
            if(block!=null){
                CleanupGridContinuousBlock pushedBlock = ns.touchBlock(ns.blockInd(block.name()));
                pushedBlock.x = pushedBlock.x + bxdelta;
                pushedBlock.y = pushedBlock.y + bydelta;

            }


            // move block

            // change dir.

            if (dir.equals("north")) {
                // going west
                agent.currentDirection = "west";

            } else if (dir.equals("south")) {
                // going east
                agent.currentDirection = "east";

            } else if (dir.equals("east")) {
                // going north
                agent.currentDirection = "north";
            } else if (dir.equals("west")) {
                // going south
                agent.currentDirection = "south";
//            } else if (dir.equals("notCardinal")) {
//                // going north
//                agent.currentDirection = "north";
            }else {
                throw new RuntimeException("Error, cannot move because of unknown direction index: " + dir);
            }



        }

        return Arrays.asList(new StateTransitionProb(ns, 1.));

    }


    protected List<StateTransitionProb> turnCounterClockwiseReal(State state) {

        CleanupState initialState = ((CleanupState) state).copy();

        CleanupState ns = ((CleanupState) state);

        CleanupGridContinuousAgent agent = ns.touchAgent();
        double ax = agent.x;
        double ay = agent.y;

        String dir = agent.currentDirection;

        int currentBlockLocXDelta = 0;
        int currentBlockLocYDelta = 0;

        int bxdelta = 0;
        int bydelta = 0;
//        int wallXdelta = 0;
//        int wallYdelta = 0;

        List<Double> xposForCollision = new ArrayList<>();
        List<Double> yposForCollision = new ArrayList<>();

        double blockOnTheSideFinalPositionX = 0;
        double blockOnTheSideFinalPositionY = 0;

        if (dir.equals("north")) {
            // going west
            bydelta = -1;
            bxdelta = -1;

            currentBlockLocYDelta = 1;

            //collision checks
            xposForCollision.add(ax-1);
            yposForCollision.add(ay);
            xposForCollision.add(ax-1);
            yposForCollision.add(ay+1);

            // block on the side final position
            blockOnTheSideFinalPositionX = ax-1;
            blockOnTheSideFinalPositionY = ay-1;

        } else if (dir.equals("south")) {
            // going east
            bydelta = 1;
            bxdelta = 1;

            currentBlockLocYDelta = -1;

            //collision checks
            xposForCollision.add(ax+1);
            yposForCollision.add(ay);
            xposForCollision.add(ax+1);
            yposForCollision.add(ay-1);

            // block on the side final position
            blockOnTheSideFinalPositionX = ax+1;
            blockOnTheSideFinalPositionY = ay+1;

        } else if (dir.equals("east")) {
            // going north
            bydelta = 1;
            bxdelta = -1;
            currentBlockLocXDelta = 1;

            //collision checks
            xposForCollision.add(ax);
            yposForCollision.add(ay+1);
            xposForCollision.add(ax+1);
            yposForCollision.add(ay+1);

            // block on the side final position
            blockOnTheSideFinalPositionX = ax-1;
            blockOnTheSideFinalPositionY = ay+1;

        } else if (dir.equals("west")) {
            // going south
            bydelta = -1;
            bxdelta = 1;
            currentBlockLocXDelta = -1;

            //collision checks
            xposForCollision.add(ax);
            yposForCollision.add(ay-1);
            xposForCollision.add(ax-1);
            yposForCollision.add(ay-1);

            // block on the side final position
            blockOnTheSideFinalPositionX = ax+1;
            blockOnTheSideFinalPositionY = ay-1;

        } else {
            throw new RuntimeException("Error, cannot move because of unknown direction index: " + dir);
        }


        // check collision for all blocks now

        boolean possibleMove = true;

        CleanupRoom roomContaining = (CleanupRoom)regionContainingPoint(ns.objectsOfClass(CLASS_ROOM), ax, ay, true);

        boolean blocksInBothLocations = true;

        List<CleanupGridContinuousBlock> blocksOnTheSide = new ArrayList<CleanupGridContinuousBlock>();

        for(int i=0;i<2;i++){
            int xCheck = xposForCollision.get(i).intValue();
            int yCheck = yposForCollision.get(i).intValue();

            // check if block here - if block then move in the direction of move

            // if east west then move all blocks to

            CleanupGridContinuousBlock bTemp = blockAtPoint(ns, xCheck, yCheck);
            if(bTemp!=null){
                blocksOnTheSide.add(bTemp);
            }
            else{
                blocksInBothLocations = false;
            }

            if(wallAt(ns, roomContaining, xCheck, yCheck) ){//|| bTemp != null
                possibleMove = false;
                break;
            }
        }

        if(blocksInBothLocations){
            possibleMove = false;
        }

        if(possibleMove){
            // check for block
            CleanupGridContinuousBlock block  = blockAtPoint(ns, ax+ currentBlockLocXDelta, ay+currentBlockLocYDelta);

            // move block

            if(block!=null){
                CleanupGridContinuousBlock pushedBlock = ns.touchBlock(ns.blockInd(block.name()));
                pushedBlock.x = pushedBlock.x + bxdelta;
                pushedBlock.y = pushedBlock.y + bydelta;

            }


            // move blocks in the list


            if(blocksOnTheSide.size()>0){
                CleanupGridContinuousBlock pushedBlock = ns.touchBlock(ns.blockInd(blocksOnTheSide.get(0).name()));
                pushedBlock.x = blockOnTheSideFinalPositionX;
                pushedBlock.y = blockOnTheSideFinalPositionY;
            }




            // change dir.

            if (dir.equals("north")) {
                // going west
                agent.currentDirection = "west";

            } else if (dir.equals("south")) {
                // going east
                agent.currentDirection = "east";

            } else if (dir.equals("east")) {
                // going north
                agent.currentDirection = "north";
            } else if (dir.equals("west")) {
                // going south
                agent.currentDirection = "south";
            } else {
                throw new RuntimeException("Error, cannot move because of unknown direction index: " + dir);
            }



        }

        return Arrays.asList(new StateTransitionProb(ns, 1.));

    }


    protected List<StateTransitionProb> turnClockwiseReal(State state) {


        CleanupState initialState = ((CleanupState) state).copy();

        CleanupState ns = ((CleanupState) state);

        CleanupGridContinuousAgent agent = ns.touchAgent();
        double ax = agent.x;
        double ay = agent.y;

        String dir = agent.currentDirection;

        int currentBlockLocXDelta = 0;
        int currentBlockLocYDelta = 0;

        int bxdelta = 0;
        int bydelta = 0;
//        int wallXdelta = 0;
//        int wallYdelta = 0;

        List<Double> xposForCollision = new ArrayList<>();
        List<Double> yposForCollision = new ArrayList<>();

        // block on the side final position
        double blockOnTheSideFinalPositionX = 0;
        double blockOnTheSideFinalPositionY = 0;

        if (dir.equals("north")) {
            // going east
            bydelta = -1;
            bxdelta = 1;

            currentBlockLocYDelta = 1;

            //collision checks
            xposForCollision.add(ax+1);
            yposForCollision.add(ay);
            xposForCollision.add(ax+1);
            yposForCollision.add(ay+1);

            // block on the side final position
            blockOnTheSideFinalPositionX = ax+1;
            blockOnTheSideFinalPositionY = ay-1;

        } else if (dir.equals("south")) {
            // going west
            bydelta = 1;
            bxdelta = -1;

            currentBlockLocYDelta = -1;

            //collision checks
            xposForCollision.add(ax-1);
            yposForCollision.add(ay);
            xposForCollision.add(ax-1);
            yposForCollision.add(ay-1);

            // block on the side final position
            blockOnTheSideFinalPositionX = ax-1;
            blockOnTheSideFinalPositionY = ay+1;

        } else if (dir.equals("east")) {
            // going south
            bydelta = -1;
            bxdelta = -1;
            currentBlockLocXDelta = 1;

            //collision checks
            xposForCollision.add(ax);
            yposForCollision.add(ay-1);
            xposForCollision.add(ax+1);
            yposForCollision.add(ay-1);

            // block on the side final position
            blockOnTheSideFinalPositionX = ax-1;
            blockOnTheSideFinalPositionY = ay-1;

        } else if (dir.equals("west")) {
            // going north
            bydelta = 1;
            bxdelta = 1;
            currentBlockLocXDelta = -1;

            //collision checks
            xposForCollision.add(ax);
            yposForCollision.add(ay+1);
            xposForCollision.add(ax-1);
            yposForCollision.add(ay+1);

            // block on the side final position
            blockOnTheSideFinalPositionX = ax+1;
            blockOnTheSideFinalPositionY = ay+1;

        } else {
            throw new RuntimeException("Error, cannot move because of unknown direction index: " + dir);
        }


        // check collision for all blocks now

        boolean possibleMove = true;

        CleanupRoom roomContaining = (CleanupRoom)regionContainingPoint(ns.objectsOfClass(CLASS_ROOM), ax, ay, true);


        boolean blocksInBothLocations = true;

        List<CleanupGridContinuousBlock> blocksOnTheSide = new ArrayList<CleanupGridContinuousBlock>();

        for(int i=0;i<2;i++){
            int xCheck = xposForCollision.get(i).intValue();
            int yCheck = yposForCollision.get(i).intValue();

            CleanupGridContinuousBlock bTemp = blockAtPoint(ns, xCheck, yCheck);
            if(bTemp!=null){
                blocksOnTheSide.add(bTemp);
            }
            else{
                blocksInBothLocations = false;
            }


            if(wallAt(ns, roomContaining, xCheck, yCheck)){// || blockAtPoint(ns, xCheck, yCheck) != null){
                possibleMove = false;
                break;
            }
        }

        if(blocksInBothLocations){
            possibleMove = false;
        }

        if(possibleMove){
            // check for block
            CleanupGridContinuousBlock block  = blockAtPoint(ns, ax+ currentBlockLocXDelta, ay+currentBlockLocYDelta);
            if(block!=null){
                CleanupGridContinuousBlock pushedBlock = ns.touchBlock(ns.blockInd(block.name()));
                pushedBlock.x = pushedBlock.x + bxdelta;
                pushedBlock.y = pushedBlock.y + bydelta;

            }

            // move blocks in the list


            if(blocksOnTheSide.size()>0){
                CleanupGridContinuousBlock pushedBlock = ns.touchBlock(ns.blockInd(blocksOnTheSide.get(0).name()));
                pushedBlock.x = blockOnTheSideFinalPositionX;
                pushedBlock.y = blockOnTheSideFinalPositionY;
            }

            // move block

            // change dir.

            if (dir.equals("north")) {
                // going east
                agent.currentDirection = "east";

            } else if (dir.equals("south")) {
                // going west
                agent.currentDirection = "west";

            } else if (dir.equals("east")) {
                // going south
                agent.currentDirection = "south";
            } else if (dir.equals("west")) {
                // going north
                agent.currentDirection = "north";
            } else {
                throw new RuntimeException("Error, cannot move because of unknown direction index: " + dir);
            }


        }

        return Arrays.asList(new StateTransitionProb(ns, 1.));

    }




}