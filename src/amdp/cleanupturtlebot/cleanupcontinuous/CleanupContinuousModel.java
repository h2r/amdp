package amdp.cleanupturtlebot.cleanupcontinuous;


import amdp.cleanupturtlebot.cleanupcontinuous.state.*;
import burlap.mdp.core.StateTransitionProb;
import burlap.mdp.core.action.Action;
import burlap.mdp.core.oo.state.ObjectInstance;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.model.statemodel.FullStateModel;
import org.apache.commons.lang3.tuple.MutablePair;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import static amdp.cleanupturtlebot.cleanupcontinuous.CleanupContinuousDomain.*;

/**
 * Created by ngopalan on 8/27/16.
 */

// the collision checking is very crude in this model. From the center of the bot, we go up a "cell"
// and check if there is any thing in the left and right cells of the cell on top.
public class CleanupContinuousModel implements FullStateModel {
    private static Random rand;
    private static double lockProb = 0.5;


    // we check into next cell
    public static double							rangeForTurnCollisionChecksLength = 1.;

    // we check into next cell
    public static double							rangeForTurnCollisionChecksWidth = 0.65;

    // front check for one unit
    public static double                            oneUnitCheckBlocks = 0.51;

    // front check for two units for walls
    public static double                            twoUnitCheckBlocks = 1.49;


    public static double edgeRadius = Math.sqrt(rangeForTurnCollisionChecksLength*rangeForTurnCollisionChecksLength +
            rangeForTurnCollisionChecksWidth*rangeForTurnCollisionChecksWidth);// since the right and left check points are 1 cell above
    // and 1 cells to the left and right


    // left edge is on the positive side of counting towards the west when facing north
    public static double leftEdgeAngle = Math.atan(rangeForTurnCollisionChecksWidth/rangeForTurnCollisionChecksLength);
    // right edge is towards east!
    public static double rightEdgeAngle = -Math.atan(rangeForTurnCollisionChecksWidth/rangeForTurnCollisionChecksLength);

    public static double deltaMove = 0.1;
    public static double deltaTheta = 0.1;//Math.PI/16;//0.001;

    public CleanupContinuousModel(Random rand, double lockProb){
        this.rand = rand;
        this.lockProb = lockProb;
    }

    public void setDeltaMove(double deltaMove){
        this.deltaMove = deltaMove;
    }

    public void setDeltaTheta(double deltaTheta){
        this.deltaTheta = deltaTheta;
    }

    // the easiest planning is by the rotate and move commands
    // for move check if the neighbouring cells are empty
    //checks n>s, s>n; e>w, w>e; n>e, e>n; e>s,s>e; s>w,w>s; n>w,w>n; n>n; s>s; e>e;w>w

    @Override
    public List<StateTransitionProb> stateTransitions(State s, Action a) {
        CleanupContinuousState ns = ((CleanupContinuousState)s).copy();
        int actionInd = actionInd(a.actionName());
        if(actionInd<4){
            if(actionInd==0){
                //forward
                int dx = 0;
                int dy = 1;
                return moveForward(ns);
                // if north then conditional for current direction - if facing north then just move
                // else turn and when turning check focr walls if walls then can't turn north
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

        int test =0 ;

        CleanupContinuousState initialState = ((CleanupContinuousState) state).copy();

        CleanupContinuousState ns = ((CleanupContinuousState) state);

        CleanupContinuousAgent agent = ns.touchAgent();
        double ax = agent.x;
        double ay = agent.y;

        double dir = agent.direction;

//        double xdelta = 0;
//        double ydelta = 0;
        double wallXdelta = 0;
        double wallYdelta = 0;



        MutablePair<Double,Double> xydelta = newCentre(agent,true);
        double xdelta = xydelta.getLeft();
        double ydelta = xydelta.getRight();



        MutablePair<Double,Double> conflictPosTwoStep = checkAhead(agent,true,twoUnitCheckBlocks);

        //TODO: calculate x and y delta according to theta!




        // check for wall at x+2 and block at x+1 and move!
        double nx = ax+xdelta;
        double ny = ay+ydelta;
        // get new position

        // check for walls 2 ahead!
        double nwx = ax+conflictPosTwoStep.getLeft();
        double nwy = ay+conflictPosTwoStep.getRight();

        //ObjectInstance roomContaining = roomContainingPoint(s, ax, ay);
        CleanupContinuousRoom roomContaining = (CleanupContinuousRoom)regionContainingPoint(ns.objectsOfClass(CLASS_ROOM), ax, ay, true);

        MutablePair<Double,Double> conflictPosOneStep = checkAhead(agent,true,oneUnitCheckBlocks);
//        CleanupContinuousBlock pushedBlockOld = blockAtPoint(ns, nx+conflictPosOneStep.getLeft(), ny+conflictPosOneStep.getRight());

        CleanupContinuousBlock pushedBlockOld = blockAtPoint(ns, ax+conflictPosOneStep.getLeft(), ay+conflictPosOneStep.getRight());


        CleanupContinuousDoor conflictDoor = null;

        boolean permissibleMove = false;



        boolean pathClear = true;

        boolean wallAt = true;
        if(roomContaining!=null){
            wallAt = wallAt(ns, roomContaining, nwx, nwy);
        }

        if(wallAt){
            System.out.println("wall ahead!!!");
        }

        if(pushedBlockOld != null){
            CleanupContinuousBlock tempBlock = blockAtPoint(ns, nwx, nwy);
            if( tempBlock!= null){
                if(!tempBlock.name().equals(pushedBlockOld.name())){
                    pathClear = false;
                    System.out.println("block 2 units away!");
                }

            }
        }


//        if(roomContaining==null && pathClear){
//            permissibleMove = true;
//        }


        if(!wallAt && pathClear){//(blockAtPoint(ns, nwx, nwy) == null && blockAtPoint(ns, nx, ny) != null)){


            //is there a possible door that can be locked?
            CleanupContinuousDoor doorAtNewPointOld = doorContainingPoint(ns, nwx, nwy);
            if(doorAtNewPointOld != null){
                CleanupContinuousDoor doorAtNewPoint = ns.touchDoor(ns.doorInd(doorAtNewPointOld.name()));
                if(doorAtNewPoint.canBeLocked) {
                    conflictDoor = doorAtNewPoint;
                }

            }

            if(pushedBlockOld != null) {
                CleanupContinuousBlock pushedBlock = ns.touchBlock(ns.blockInd(pushedBlockOld.name()));
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

            CleanupContinuousState lockedState = initialState.copy();
            lockedState.touchDoor(lockedState.doorInd(conflictDoor.name())).locked= 2;

            return Arrays.asList(new StateTransitionProb(ns, 1.-this.lockProb), new StateTransitionProb(lockedState, lockProb));
        }

        return Arrays.asList(new StateTransitionProb(ns, 1.));


    }


    public static MutablePair<Double,Double> getLeftEdge(CleanupContinuousAgent a){
        // left edge is positive if north is 0 rad. anti-clockwise!
        double deltax = -edgeRadius * Math.sin(a.direction + leftEdgeAngle);
        double deltay = edgeRadius * Math.cos(a.direction + leftEdgeAngle);
        return new MutablePair<Double, Double>(deltax+a.x,deltay+a.y);
    }

    public static MutablePair<Double,Double> getRightEdge(CleanupContinuousAgent a){
        // right edge is negative if north is 0 rad.
        double deltax = -edgeRadius * Math.sin(a.direction - rightEdgeAngle);
        double deltay = edgeRadius * Math.cos(a.direction - rightEdgeAngle);
        return new MutablePair<Double, Double>(deltax+a.x,deltay+a.y);
    }


    public static MutablePair<Double,Double> getRightCellCord(CleanupContinuousAgent a){
        // right edge is negative if north is 0 rad.
        double deltax = -edgeRadius * Math.sin(a.direction - (Math.PI/2));
        double deltay = edgeRadius * Math.cos(a.direction - (Math.PI/2));
        return new MutablePair<Double, Double>(deltax+a.x,deltay+a.y);
    }

    public static MutablePair<Double,Double> getLeftCellCord(CleanupContinuousAgent a){
        // left edge is positive if north is 0 rad. anti-clockwise!
        double deltax = -edgeRadius * Math.sin(a.direction + Math.PI/2);
        double deltay = edgeRadius * Math.cos(a.direction + Math.PI/2);
        return new MutablePair<Double, Double>(deltax+a.x,deltay+a.y);
    }


    public static MutablePair<Double,Double> newCentre(CleanupContinuousAgent a, boolean moveFwd){
        double deltax = -deltaMove * Math.sin(a.direction);
//        System.out.println("----------------------");
//        System.out.println("direction: " + a.direction + "sine: "+Math.sin(a.direction));
//        System.out.println("----------------------");
        double deltay = deltaMove * Math.cos(a.direction);
        if(moveFwd){
            return new MutablePair<Double, Double>(deltax,deltay);
        }

        // as sin(pi+ theta) = -sin(pi+ theta) and same for cos!
        return new MutablePair<Double, Double>(-deltax,-deltay);
    }

    public static MutablePair<Double,Double> newBlockCentre(CleanupContinuousAgent a, boolean moveFwd, double dir){
        double deltax = -1 * Math.sin(dir);
//        System.out.println("----------------------");
//        System.out.println("direction: " + a.direction + "sine: "+Math.sin(a.direction));
//        System.out.println("----------------------");
        double deltay = 1 * Math.cos(dir);
        if(moveFwd){
            return new MutablePair<Double, Double>(deltax,deltay);
        }

        // as sin(pi+ theta) = -sin(pi+ theta) and same for cos!
        return new MutablePair<Double, Double>(-deltax,-deltay);
    }




    public static MutablePair<Double,Double> checkAhead(CleanupContinuousAgent a, boolean moveFwd, double distance){
        double deltax = -distance * Math.sin(a.direction);
        double deltay = distance * Math.cos(a.direction);
        if(moveFwd){
            return new MutablePair<Double, Double>(deltax,deltay);
        }

        // as sin(pi+ theta) = -sin(pi+ theta) and same for cos!
        return new MutablePair<Double, Double>(-deltax,-deltay);
    }


    protected List<StateTransitionProb> moveBackward(State state) {


        CleanupContinuousState initialState = ((CleanupContinuousState) state).copy();

        CleanupContinuousState ns = ((CleanupContinuousState) state);

        CleanupContinuousAgent agent = ns.touchAgent();
        double ax = agent.x;
        double ay = agent.y;

        double dir = agent.direction;

        MutablePair<Double,Double> xydelta = newCentre(agent,false);
        double xdelta = xydelta.getLeft();
        double ydelta = xydelta.getRight();

        // calculate x and y delta according to theta!


        double nx = ax+xdelta;
        double ny = ay+ydelta;
        // get new position

//        // check for walls 2 ahead!
//        int nwx = ax+wallXdelta;
//        int nwy = ay+wallYdelta;

        //ObjectInstance roomContaining = roomContainingPoint(s, ax, ay);
        CleanupContinuousRoom roomContaining = (CleanupContinuousRoom)regionContainingPoint(ns.objectsOfClass(CLASS_ROOM), ax, ay, true);

        CleanupContinuousDoor conflictDoor = null;

        boolean permissibleMove = false;

        MutablePair<Double,Double> conflictPos = checkAhead(agent,false,oneUnitCheckBlocks);
        CleanupContinuousBlock pushedBlockOld = blockAtPoint(ns, nx+conflictPos.getLeft(), ny+conflictPos.getRight());


        if(pushedBlockOld != null){
            CleanupContinuousBlock pushedBlock = ns.touchBlock(ns.blockInd(pushedBlockOld.name()));
            double bx = pushedBlock.x;
            double by = pushedBlock.y;

            double nbx = bx + xdelta;
            double nby = by + ydelta;

            MutablePair<Double,Double> possibleWallPos = checkAhead(agent,false,twoUnitCheckBlocks);

            if(!wallAt(ns, roomContaining, nx+possibleWallPos.getLeft(), nx+possibleWallPos.getRight())
                    && blockAtPoint(ns, nx + possibleWallPos.getLeft(), nx+possibleWallPos.getRight()) == null){


                //is there a possible door that can be locked?
                CleanupContinuousDoor doorAtNewPointOld = doorContainingPoint(ns, nbx, nby);
                if(doorAtNewPointOld != null){
                    CleanupContinuousDoor doorAtNewPoint = ns.touchDoor(ns.doorInd(doorAtNewPointOld.name()));
                    if(doorAtNewPoint.canBeLocked) {
                        conflictDoor = doorAtNewPoint;
                    }

                }

                pushedBlock.x = nbx;
                pushedBlock.y = nby;
                permissibleMove = true;


            }

        }
//        else if(roomContaining==null){
//            permissibleMove = true;
//        }
        else if(!wallAt(ns, roomContaining, nx+conflictPos.getLeft(), nx+conflictPos.getRight())){
            permissibleMove = true;
        }

        if(permissibleMove){


            //if doors are lockable, we must check whether there is special handling
            ObjectInstance doorAtNewPointOld = doorContainingPoint(ns, nx, ny);

            if(doorAtNewPointOld != null){
                CleanupContinuousDoor doorAtNewPoint = ns.touchDoor(ns.doorInd(doorAtNewPointOld.name()));
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

            CleanupContinuousState lockedState = initialState.copy();
            lockedState.touchDoor(lockedState.doorInd(conflictDoor.name())).locked= 2;

            return Arrays.asList(new StateTransitionProb(ns, 1.-this.lockProb), new StateTransitionProb(lockedState, lockProb));
        }

        return Arrays.asList(new StateTransitionProb(ns, 1.));

    }



    protected List<StateTransitionProb> turnClockwise(State state) {


        CleanupContinuousState initialState = ((CleanupContinuousState) state).copy();

        CleanupContinuousState ns = ((CleanupContinuousState) state);

        CleanupContinuousAgent agent = ns.touchAgent();


//        double dir = agent.direction;
        double ax = agent.x;
        double ay = agent.y;
//        double currentBlockLocXDelta = 0;
//        double currentBlockLocYDelta = 0;
//
//        int bxdelta = 0;
//        int bydelta = 0;
////        int wallXdelta = 0;
////        int wallYdelta = 0;
//
//        List<Double> xposForCollision = new ArrayList<Double>();
//        List<Double> yposForCollision = new ArrayList<Double>();


        // need to check the right edge. Only direction changes not the x,y co-ordinates of the agent


        //one step block location


        MutablePair<Double,Double> conflictPos = checkAhead(agent,true,oneUnitCheckBlocks);
        CleanupContinuousBlock pushedBlockOld = blockAtPointSlightlyLargeRange(ns, ax+conflictPos.getLeft(), ay+conflictPos.getRight());

        List<MutablePair<Double,Double>> cellsToCheck = new ArrayList<MutablePair<Double, Double>>();
        MutablePair<Double,Double> rightEdge = getRightEdge(agent);
        MutablePair<Double,Double> rightCell = getRightCellCord(agent);
        cellsToCheck.add(rightCell);
        cellsToCheck.add(rightEdge);




        // check collision for all blocks now

        boolean possibleMove = true;

        CleanupContinuousRoom roomContaining = (CleanupContinuousRoom)regionContainingPoint(ns.objectsOfClass(CLASS_ROOM), ax, ay, true);

        for(int i=0;i<2;i++){
            double xCheck = cellsToCheck.get(i).getLeft();
            double yCheck = cellsToCheck.get(i).getRight();

            CleanupContinuousBlock tempBlock = blockAtPoint(ns, xCheck, yCheck);
            if(wallAt(ns, roomContaining, xCheck, yCheck) ||  (tempBlock!= null) ){
                if (pushedBlockOld != null) {
                    if(tempBlock.name().equals(pushedBlockOld.name())){
                        continue;
                    }
                }
                possibleMove = false;
                break;
            }
        }



        if(possibleMove){
            // check for block
//            CleanupContinuousBlock block  = blockAtPoint(ns, ax+ currentBlockLocXDelta, ay+currentBlockLocYDelta);
            if(pushedBlockOld!=null){
                CleanupContinuousBlock pushedBlock = ns.touchBlock(ns.blockInd(pushedBlockOld.name()));
                MutablePair<Double,Double> cord = newBlockCentre(ns.agent,true,agent.direction-deltaTheta);
                //getBlockLoc(pushedBlock,agent,-deltaTheta);
                pushedBlock.x =ax+ cord.getLeft(); // pushedBlock.x + bxdelta;
                pushedBlock.y =ay+ cord.getRight();  //pushedBlock.y + bydelta;

            }


            // move block

            // change dir.

            agent.direction -= deltaTheta;



        }

        return Arrays.asList(new StateTransitionProb(ns, 1.));

    }

    private MutablePair<Double,Double> getBlockLoc(CleanupContinuousBlock b, CleanupContinuousAgent a, double deltaT) {
        double radius = Math.sqrt((b.x-a.x)*(b.x-a.x) + (b.y-a.y)*(b.y-a.y));
        //block clearly at 0 since north
        double newbx = radius * Math.sin(deltaT) + a.x;//a.direction+
        double newby = radius * Math.cos(deltaT) + a.y;

        return new MutablePair<Double, Double>(newbx, newby);

    }


    protected List<StateTransitionProb> turnCounterClockwise(State state) {

        CleanupContinuousState initialState = ((CleanupContinuousState) state).copy();

        CleanupContinuousState ns = ((CleanupContinuousState) state);

        CleanupContinuousAgent agent = ns.touchAgent();
        double ax = agent.x;
        double ay = agent.y;

        double dir = agent.direction;



        MutablePair<Double,Double> conflictPos = checkAhead(agent,true,oneUnitCheckBlocks);
        CleanupContinuousBlock pushedBlockOld = blockAtPointSlightlyLargeRange(ns, ax+conflictPos.getLeft(), ay+conflictPos.getRight());

        List<MutablePair<Double,Double>> cellsToCheck = new ArrayList<MutablePair<Double, Double>>();
        MutablePair<Double,Double> leftEdge = getLeftEdge(agent);
        MutablePair<Double,Double> leftCell = getLeftCellCord(agent);
        cellsToCheck.add(leftCell);
        cellsToCheck.add(leftEdge);


        int currentBlockLocXDelta = 0;
        int currentBlockLocYDelta = 0;

        int bxdelta = 0;
        int bydelta = 0;
//        int wallXdelta = 0;
//        int wallYdelta = 0;


        // check collision for all blocks now

        boolean possibleMove = true;

        CleanupContinuousRoom roomContaining = (CleanupContinuousRoom)regionContainingPoint(ns.objectsOfClass(CLASS_ROOM), ax, ay, true);

        for(int i=0;i<2;i++){
            double xCheck = cellsToCheck.get(i).getLeft();
            double yCheck = cellsToCheck.get(i).getRight();

            // check if block here - if block then move in the direction of move

            // if east west then move all blocks to

            if(wallAt(ns, roomContaining, xCheck, yCheck) || blockAtPoint(ns, xCheck, yCheck) != null){
                possibleMove = false;
                break;
            }
        }

        if(possibleMove){
            // check for block
//            CleanupContinuousBlock block  = blockAtPoint(ns, ax+ currentBlockLocXDelta, ay+currentBlockLocYDelta);
            if(pushedBlockOld!=null){
                CleanupContinuousBlock pushedBlock = ns.touchBlock(ns.blockInd(pushedBlockOld.name()));
                MutablePair<Double,Double> cord = newBlockCentre(ns.agent,true,agent.direction+deltaTheta);//getBlockLoc(pushedBlock,agent, deltaTheta);
                pushedBlock.x = ax+ cord.getLeft(); // pushedBlock.x + bxdelta;
                pushedBlock.y = ay+cord.getRight();  //pushedBlock.y + bydelta;
            }


            // move block

            // change dir.

            agent.direction+=deltaTheta;


        }

        return Arrays.asList(new StateTransitionProb(ns, 1.));

    }

//
//    protected List<StateTransitionProb> turnCounterClockwiseReal(State state) {
//
//        CleanupContinuousState initialState = ((CleanupContinuousState) state).copy();
//
//        CleanupContinuousState ns = ((CleanupContinuousState) state);
//
//        CleanupContinuousAgent agent = ns.touchAgent();
//        int ax = agent.x;
//        int ay = agent.y;
//
//        String dir = agent.currentDirection;
//
//        int currentBlockLocXDelta = 0;
//        int currentBlockLocYDelta = 0;
//
//        int bxdelta = 0;
//        int bydelta = 0;
////        int wallXdelta = 0;
////        int wallYdelta = 0;
//
//        List<Integer> xposForCollision = new ArrayList<Integer>();
//        List<Integer> yposForCollision = new ArrayList<Integer>();
//
//        int blockOnTheSideFinalPositionX = 0;
//        int blockOnTheSideFinalPositionY = 0;
//
//        if (dir.equals("north")) {
//            // going west
//            bydelta = -1;
//            bxdelta = -1;
//
//            currentBlockLocYDelta = 1;
//
//            //collision checks
//            xposForCollision.add(ax-1);
//            yposForCollision.add(ay);
//            xposForCollision.add(ax-1);
//            yposForCollision.add(ay+1);
//
//            // block on the side final position
//            blockOnTheSideFinalPositionX = ax-1;
//            blockOnTheSideFinalPositionY = ay-1;
//
//        } else if (dir.equals("south")) {
//            // going east
//            bydelta = 1;
//            bxdelta = 1;
//
//            currentBlockLocYDelta = -1;
//
//            //collision checks
//            xposForCollision.add(ax+1);
//            yposForCollision.add(ay);
//            xposForCollision.add(ax+1);
//            yposForCollision.add(ay-1);
//
//            // block on the side final position
//            blockOnTheSideFinalPositionX = ax+1;
//            blockOnTheSideFinalPositionY = ay+1;
//
//        } else if (dir.equals("east")) {
//            // going north
//            bydelta = 1;
//            bxdelta = -1;
//            currentBlockLocXDelta = 1;
//
//            //collision checks
//            xposForCollision.add(ax);
//            yposForCollision.add(ay+1);
//            xposForCollision.add(ax+1);
//            yposForCollision.add(ay+1);
//
//            // block on the side final position
//            blockOnTheSideFinalPositionX = ax-1;
//            blockOnTheSideFinalPositionY = ay+1;
//
//        } else if (dir.equals("west")) {
//            // going south
//            bydelta = -1;
//            bxdelta = 1;
//            currentBlockLocXDelta = -1;
//
//            //collision checks
//            xposForCollision.add(ax);
//            yposForCollision.add(ay-1);
//            xposForCollision.add(ax-1);
//            yposForCollision.add(ay-1);
//
//            // block on the side final position
//            blockOnTheSideFinalPositionX = ax+1;
//            blockOnTheSideFinalPositionY = ay-1;
//
//        } else {
//            throw new RuntimeException("Error, cannot move because of unknown direction index: " + dir);
//        }
//
//
//        // check collision for all blocks now
//
//        boolean possibleMove = true;
//
//        CleanupContinuousRoom roomContaining = (CleanupContinuousRoom)regionContainingPoint(ns.objectsOfClass(CLASS_ROOM), ax, ay, true);
//
//        boolean blocksInBothLocations = true;
//
//        List<CleanupContinuousBlock> blocksOnTheSide = new ArrayList<CleanupContinuousBlock>();
//
//        for(int i=0;i<2;i++){
//            int xCheck = xposForCollision.get(i);
//            int yCheck = yposForCollision.get(i);
//
//            // check if block here - if block then move in the direction of move
//
//            // if east west then move all blocks to
//
//            CleanupContinuousBlock bTemp = blockAtPoint(ns, xCheck, yCheck);
//            if(bTemp!=null){
//                blocksOnTheSide.add(bTemp);
//            }
//            else{
//                blocksInBothLocations = false;
//            }
//
//            if(wallAt(ns, roomContaining, xCheck, yCheck) ){//|| bTemp != null
//                possibleMove = false;
//                break;
//            }
//        }
//
//        if(blocksInBothLocations){
//            possibleMove = false;
//        }
//
//        if(possibleMove){
//            // check for block
//            CleanupContinuousBlock block  = blockAtPoint(ns, ax+ currentBlockLocXDelta, ay+currentBlockLocYDelta);
//
//            // move block
//
//            if(block!=null){
//                CleanupContinuousBlock pushedBlock = ns.touchBlock(ns.blockInd(block.name()));
//                pushedBlock.x = pushedBlock.x + bxdelta;
//                pushedBlock.y = pushedBlock.y + bydelta;
//
//            }
//
//
//            // move blocks in the list
//
//
//            if(blocksOnTheSide.size()>0){
//                CleanupContinuousBlock pushedBlock = ns.touchBlock(ns.blockInd(blocksOnTheSide.get(0).name()));
//                pushedBlock.x = blockOnTheSideFinalPositionX;
//                pushedBlock.y = blockOnTheSideFinalPositionY;
//            }
//
//
//
//
//            // change dir.
//
//            if (dir.equals("north")) {
//                // going west
//                agent.currentDirection = "west";
//
//            } else if (dir.equals("south")) {
//                // going east
//                agent.currentDirection = "east";
//
//            } else if (dir.equals("east")) {
//                // going north
//                agent.currentDirection = "north";
//            } else if (dir.equals("west")) {
//                // going south
//                agent.currentDirection = "south";
//            } else {
//                throw new RuntimeException("Error, cannot move because of unknown direction index: " + dir);
//            }
//
//
//
//        }
//
//        return Arrays.asList(new StateTransitionProb(ns, 1.));
//
//    }
//
//
//    protected List<StateTransitionProb> turnClockwiseReal(State state) {
//
//
//        CleanupContinuousState initialState = ((CleanupContinuousState) state).copy();
//
//        CleanupContinuousState ns = ((CleanupContinuousState) state);
//
//        CleanupContinuousAgent agent = ns.touchAgent();
//        int ax = agent.x;
//        int ay = agent.y;
//
//        String dir = agent.currentDirection;
//
//        int currentBlockLocXDelta = 0;
//        int currentBlockLocYDelta = 0;
//
//        int bxdelta = 0;
//        int bydelta = 0;
////        int wallXdelta = 0;
////        int wallYdelta = 0;
//
//        List<Integer> xposForCollision = new ArrayList<Integer>();
//        List<Integer> yposForCollision = new ArrayList<Integer>();
//
//        // block on the side final position
//        int blockOnTheSideFinalPositionX = 0;
//        int blockOnTheSideFinalPositionY = 0;
//
//        if (dir.equals("north")) {
//            // going east
//            bydelta = -1;
//            bxdelta = 1;
//
//            currentBlockLocYDelta = 1;
//
//            //collision checks
//            xposForCollision.add(ax+1);
//            yposForCollision.add(ay);
//            xposForCollision.add(ax+1);
//            yposForCollision.add(ay+1);
//
//            // block on the side final position
//            blockOnTheSideFinalPositionX = ax+1;
//            blockOnTheSideFinalPositionY = ay-1;
//
//        } else if (dir.equals("south")) {
//            // going west
//            bydelta = 1;
//            bxdelta = -1;
//
//            currentBlockLocYDelta = -1;
//
//            //collision checks
//            xposForCollision.add(ax-1);
//            yposForCollision.add(ay);
//            xposForCollision.add(ax-1);
//            yposForCollision.add(ay-1);
//
//            // block on the side final position
//            blockOnTheSideFinalPositionX = ax-1;
//            blockOnTheSideFinalPositionY = ay+1;
//
//        } else if (dir.equals("east")) {
//            // going south
//            bydelta = -1;
//            bxdelta = -1;
//            currentBlockLocXDelta = 1;
//
//            //collision checks
//            xposForCollision.add(ax);
//            yposForCollision.add(ay-1);
//            xposForCollision.add(ax+1);
//            yposForCollision.add(ay-1);
//
//            // block on the side final position
//            blockOnTheSideFinalPositionX = ax-1;
//            blockOnTheSideFinalPositionY = ay-1;
//
//        } else if (dir.equals("west")) {
//            // going north
//            bydelta = 1;
//            bxdelta = 1;
//            currentBlockLocXDelta = -1;
//
//            //collision checks
//            xposForCollision.add(ax);
//            yposForCollision.add(ay+1);
//            xposForCollision.add(ax-1);
//            yposForCollision.add(ay+1);
//
//            // block on the side final position
//            blockOnTheSideFinalPositionX = ax+1;
//            blockOnTheSideFinalPositionY = ay+1;
//
//        } else {
//            throw new RuntimeException("Error, cannot move because of unknown direction index: " + dir);
//        }
//
//
//        // check collision for all blocks now
//
//        boolean possibleMove = true;
//
//        CleanupContinuousRoom roomContaining = (CleanupContinuousRoom)regionContainingPoint(ns.objectsOfClass(CLASS_ROOM), ax, ay, true);
//
//
//        boolean blocksInBothLocations = true;
//
//        List<CleanupContinuousBlock> blocksOnTheSide = new ArrayList<CleanupContinuousBlock>();
//
//        for(int i=0;i<2;i++){
//            int xCheck = xposForCollision.get(i);
//            int yCheck = yposForCollision.get(i);
//
//            CleanupContinuousBlock bTemp = blockAtPoint(ns, xCheck, yCheck);
//            if(bTemp!=null){
//                blocksOnTheSide.add(bTemp);
//            }
//            else{
//                blocksInBothLocations = false;
//            }
//
//
//            if(wallAt(ns, roomContaining, xCheck, yCheck)){// || blockAtPoint(ns, xCheck, yCheck) != null){
//                possibleMove = false;
//                break;
//            }
//        }
//
//        if(blocksInBothLocations){
//            possibleMove = false;
//        }
//
//        if(possibleMove){
//            // check for block
//            CleanupContinuousBlock block  = blockAtPoint(ns, ax+ currentBlockLocXDelta, ay+currentBlockLocYDelta);
//            if(block!=null){
//                CleanupContinuousBlock pushedBlock = ns.touchBlock(ns.blockInd(block.name()));
//                pushedBlock.x = pushedBlock.x + bxdelta;
//                pushedBlock.y = pushedBlock.y + bydelta;
//
//            }
//
//            // move blocks in the list
//
//
//            if(blocksOnTheSide.size()>0){
//                CleanupContinuousBlock pushedBlock = ns.touchBlock(ns.blockInd(blocksOnTheSide.get(0).name()));
//                pushedBlock.x = blockOnTheSideFinalPositionX;
//                pushedBlock.y = blockOnTheSideFinalPositionY;
//            }
//
//            // move block
//
//            // change dir.
//
//            if (dir.equals("north")) {
//                // going east
//                agent.currentDirection = "east";
//
//            } else if (dir.equals("south")) {
//                // going west
//                agent.currentDirection = "west";
//
//            } else if (dir.equals("east")) {
//                // going south
//                agent.currentDirection = "south";
//            } else if (dir.equals("west")) {
//                // going north
//                agent.currentDirection = "north";
//            } else {
//                throw new RuntimeException("Error, cannot move because of unknown direction index: " + dir);
//            }
//
//
//        }
//
//        return Arrays.asList(new StateTransitionProb(ns, 1.));
//
//    }




}