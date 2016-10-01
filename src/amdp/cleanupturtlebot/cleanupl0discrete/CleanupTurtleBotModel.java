package amdp.cleanupturtlebot.cleanupl0discrete;

//import amdp.cleanupturtlebot.cleanupl0discrete.state.*;
import amdp.cleanup.state.*;
import burlap.mdp.core.StateTransitionProb;
import burlap.mdp.core.action.Action;
import burlap.mdp.core.oo.state.ObjectInstance;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.model.statemodel.FullStateModel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import static amdp.cleanupturtlebot.cleanupl0discrete.CleanupTurtleBotL0Domain.*;

/**
 * Created by ngopalan on 8/27/16.
 */
public class CleanupTurtleBotModel implements FullStateModel {
    private static Random rand;
    private static double lockProb = 0.5;

    public CleanupTurtleBotModel(Random rand, double lockProb){
        this.rand = rand;
        this.lockProb = lockProb;
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
                //forward
                int dx = 0;
                int dy = 1;
                return moveForward(ns);
                // if north then conditional for current direction - if facing north then just move
                // else turn and when turning check for walls if walls then can't turn north
                // now check for boxes boxes in the one neighbourhood, is collision possible?

                //check for walls for the turn get the shortest direction...
                //check for blocks in the move, in the gripper and outside of the gripper

//                return move(ns,dx,dy);
            }
            else if(actionInd==1){
                //back
//                int dx = 0;
//                int dy = -1;
//                return move(ns,dx,dy);
                return moveBackward(ns);
            }
            else if(actionInd==2){
                //turn cw
//                int dx = 1;
//                int dy = 0;
//                return move(ns,dx,dy);
//                return turnClockwiseReal(ns);
                return turnClockwise(ns);

            }
            else if(actionInd==3){
                //turn ccw
//                int dx = -1;
//                int dy = 0;
//                return move(ns,dx,dy);
//                return turnClockwiseReal(ns);
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

        CleanupAgent agent = ns.touchAgent();
        int ax = agent.x;
        int ay = agent.y;

        String dir = agent.currentDirection;

        int xdelta = 0;
        int ydelta = 0;
        int wallXdelta = 0;
        int wallYdelta = 0;

        if(dir.equals("north")){
            ydelta = 1;
            wallYdelta = 2;
        }
        else if(dir.equals("south")){
            ydelta = -1;
            wallYdelta = -2;
        }
        else if(dir.equals("east")){
            xdelta = 1;
            wallXdelta = +2;
        }
        else if(dir.equals("west")){
            xdelta = -1;
            wallXdelta = -2;
        }
        else if(dir.equals("notCardinal")){
            xdelta = 0;
            wallXdelta = 0;
        }
        else{
            throw new RuntimeException("Error, cannot move because of unknown direction index: " + dir);
        }

        // check for wall at x+2 and block at x+1 and move!
        int nx = ax+xdelta;
        int ny = ay+ydelta;
        // get new position

        // check for walls 2 ahead!
        int nwx = ax+wallXdelta;
        int nwy = ay+wallYdelta;

        //ObjectInstance roomContaining = roomContainingPoint(s, ax, ay);
        CleanupRoom roomContaining = (CleanupRoom)regionContainingPoint(ns.objectsOfClass(CLASS_ROOM), ax, ay, true);

        CleanupDoor conflictDoor = null;

        boolean permissibleMove = false;
        CleanupBlock pushedBlockOld = blockAtPoint(ns, nx, ny);


        boolean pathClear = true;

        if(blockAtPoint(ns, nx, ny) != null){
            if(blockAtPoint(ns, nwx, nwy) != null){
                pathClear = false;
            }
        }


        if(!wallAt(ns, roomContaining, nwx, nwy) && pathClear){//(blockAtPoint(ns, nwx, nwy) == null && blockAtPoint(ns, nx, ny) != null)){


            //is there a possible door that can be locked?
            CleanupDoor doorAtNewPointOld = doorContainingPoint(ns, nwx, nwy);
            if(doorAtNewPointOld != null){
                CleanupDoor doorAtNewPoint = ns.touchDoor(ns.doorInd(doorAtNewPointOld.name()));
                if(doorAtNewPoint.canBeLocked) {
                    conflictDoor = doorAtNewPoint;
                }

            }

            if(pushedBlockOld != null) {
                CleanupBlock pushedBlock = ns.touchBlock(ns.blockInd(pushedBlockOld.name()));
                int bx = pushedBlock.x;
                int by = pushedBlock.y;

                int nbx = bx + xdelta;
                int nby = by + ydelta;
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

        CleanupAgent agent = ns.touchAgent();
        int ax = agent.x;
        int ay = agent.y;

        String dir = agent.currentDirection;

        int xdelta = 0;
        int ydelta = 0;
//        int wallXdelta = 0;
//        int wallYdelta = 0;

        if(dir.equals("north")){
            ydelta = -1;
//            wallYdelta = 2;
        }
        else if(dir.equals("south")){
            ydelta = 1;
//            wallYdelta = -2;
        }
        else if(dir.equals("east")){
            xdelta = -1;
//            wallXdelta = +2;
        }
        else if(dir.equals("west")){
            xdelta = 1;
//            wallXdelta = -2;
        }
        else if(dir.equals("notCardinal")){
            xdelta = 0;
        }
        else{
            throw new RuntimeException("Error, cannot move because of unknown direction index: " + dir);
        }

        // check for wall at x+2 and block at x+1 and move!
        int nx = ax+xdelta;
        int ny = ay+ydelta;
        // get new position

//        // check for walls 2 ahead!
//        int nwx = ax+wallXdelta;
//        int nwy = ay+wallYdelta;

        //ObjectInstance roomContaining = roomContainingPoint(s, ax, ay);
        CleanupRoom roomContaining = (CleanupRoom)regionContainingPoint(ns.objectsOfClass(CLASS_ROOM), ax, ay, true);

        CleanupDoor conflictDoor = null;

        boolean permissibleMove = false;
        CleanupBlock pushedBlockOld = blockAtPoint(ns, nx, ny);


        if(pushedBlockOld != null){
            CleanupBlock pushedBlock = ns.touchBlock(ns.blockInd(pushedBlockOld.name()));
            int bx = pushedBlock.x;
            int by = pushedBlock.y;

            int nbx = bx + xdelta;
            int nby = by + ydelta;

            if(!wallAt(ns, roomContaining, nbx, nby) && blockAtPoint(ns, nbx, nby) == null){


                //is there a possible door that can be locked?
                CleanupDoor doorAtNewPointOld = doorContainingPoint(ns, nbx, nby);
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
        else if(!wallAt(ns, roomContaining, nx, ny)){
            permissibleMove = true;
        }

        if(permissibleMove){


            //if doors are lockable, we must check whether there is special handling
            ObjectInstance doorAtNewPointOld = doorContainingPoint(ns, nx, ny);

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

        CleanupAgent agent = ns.touchAgent();
        int ax = agent.x;
        int ay = agent.y;

        String dir = agent.currentDirection;

        int currentBlockLocXDelta = 0;
        int currentBlockLocYDelta = 0;

        int bxdelta = 0;
        int bydelta = 0;
//        int wallXdelta = 0;
//        int wallYdelta = 0;

        List<Integer> xposForCollision = new ArrayList<Integer>();
        List<Integer> yposForCollision = new ArrayList<Integer>();

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
                int xCheck = xposForCollision.get(i);
                int yCheck = yposForCollision.get(i);


                if (wallAt(ns, roomContaining, xCheck, yCheck) || blockAtPoint(ns, xCheck, yCheck) != null) {
                    possibleMove = false;
                    break;
                }
            }
        }
        if(possibleMove){
            // check for block
            CleanupBlock block  = blockAtPoint(ns, ax+ currentBlockLocXDelta, ay+currentBlockLocYDelta);
            if(block!=null){
                CleanupBlock pushedBlock = ns.touchBlock(ns.blockInd(block.name()));
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

        CleanupAgent agent = ns.touchAgent();
        int ax = agent.x;
        int ay = agent.y;

        String dir = agent.currentDirection;

        int currentBlockLocXDelta = 0;
        int currentBlockLocYDelta = 0;

        int bxdelta = 0;
        int bydelta = 0;
//        int wallXdelta = 0;
//        int wallYdelta = 0;

        List<Integer> xposForCollision = new ArrayList<Integer>();
        List<Integer> yposForCollision = new ArrayList<Integer>();


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
                int xCheck = xposForCollision.get(i);
                int yCheck = yposForCollision.get(i);

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
            CleanupBlock block  = blockAtPoint(ns, ax+ currentBlockLocXDelta, ay+currentBlockLocYDelta);
            if(block!=null){
                CleanupBlock pushedBlock = ns.touchBlock(ns.blockInd(block.name()));
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

        CleanupAgent agent = ns.touchAgent();
        int ax = agent.x;
        int ay = agent.y;

        String dir = agent.currentDirection;

        int currentBlockLocXDelta = 0;
        int currentBlockLocYDelta = 0;

        int bxdelta = 0;
        int bydelta = 0;
//        int wallXdelta = 0;
//        int wallYdelta = 0;

        List<Integer> xposForCollision = new ArrayList<Integer>();
        List<Integer> yposForCollision = new ArrayList<Integer>();

        int blockOnTheSideFinalPositionX = 0;
        int blockOnTheSideFinalPositionY = 0;

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

        List<CleanupBlock> blocksOnTheSide = new ArrayList<CleanupBlock>();

        for(int i=0;i<2;i++){
            int xCheck = xposForCollision.get(i);
            int yCheck = yposForCollision.get(i);

            // check if block here - if block then move in the direction of move

            // if east west then move all blocks to

            CleanupBlock bTemp = blockAtPoint(ns, xCheck, yCheck);
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
            CleanupBlock block  = blockAtPoint(ns, ax+ currentBlockLocXDelta, ay+currentBlockLocYDelta);

            // move block

            if(block!=null){
                CleanupBlock pushedBlock = ns.touchBlock(ns.blockInd(block.name()));
                pushedBlock.x = pushedBlock.x + bxdelta;
                pushedBlock.y = pushedBlock.y + bydelta;

            }


            // move blocks in the list


            if(blocksOnTheSide.size()>0){
                CleanupBlock pushedBlock = ns.touchBlock(ns.blockInd(blocksOnTheSide.get(0).name()));
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

        CleanupAgent agent = ns.touchAgent();
        int ax = agent.x;
        int ay = agent.y;

        String dir = agent.currentDirection;

        int currentBlockLocXDelta = 0;
        int currentBlockLocYDelta = 0;

        int bxdelta = 0;
        int bydelta = 0;
//        int wallXdelta = 0;
//        int wallYdelta = 0;

        List<Integer> xposForCollision = new ArrayList<Integer>();
        List<Integer> yposForCollision = new ArrayList<Integer>();

        // block on the side final position
        int blockOnTheSideFinalPositionX = 0;
        int blockOnTheSideFinalPositionY = 0;

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

        List<CleanupBlock> blocksOnTheSide = new ArrayList<CleanupBlock>();

        for(int i=0;i<2;i++){
            int xCheck = xposForCollision.get(i);
            int yCheck = yposForCollision.get(i);

            CleanupBlock bTemp = blockAtPoint(ns, xCheck, yCheck);
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
            CleanupBlock block  = blockAtPoint(ns, ax+ currentBlockLocXDelta, ay+currentBlockLocYDelta);
            if(block!=null){
                CleanupBlock pushedBlock = ns.touchBlock(ns.blockInd(block.name()));
                pushedBlock.x = pushedBlock.x + bxdelta;
                pushedBlock.y = pushedBlock.y + bydelta;

            }

            // move blocks in the list


            if(blocksOnTheSide.size()>0){
                CleanupBlock pushedBlock = ns.touchBlock(ns.blockInd(blocksOnTheSide.get(0).name()));
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