package amdp.cleanup;

import amdp.cleanup.state.*;
import burlap.mdp.core.StateTransitionProb;
import burlap.mdp.core.action.Action;
import burlap.mdp.core.oo.state.ObjectInstance;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.model.statemodel.FullStateModel;
import static amdp.cleanup.CleanupDomain.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

/**
 * Created by ngopalan on 8/27/16.
 */
public class CleanupModel implements FullStateModel {
    private static Random rand;
    private static double lockProb = 0.5;

    public CleanupModel(Random rand, double lockProb){
        this.rand = rand;
        this.lockProb = lockProb;
    }

    @Override
    public List<StateTransitionProb> stateTransitions(State s, Action a) {
        //TODO: get an index of actions and check what the actions are
        CleanupState ns = ((CleanupState)s).copy();
        int actionInd = actionInd(a.actionName());
        if(actionInd<4){
            if(actionInd==0){
                //north
                int dx = 0;
                int dy = 1;
                return move(ns,dx,dy);
            }
            else if(actionInd==1){
                //south
                int dx = 0;
                int dy = -1;
                return move(ns,dx,dy);
            }
            else if(actionInd==2){
                //east
                int dx = 1;
                int dy = 0;
                return move(ns,dx,dy);

            }
            else if(actionInd==3){
                //west
                int dx = -1;
                int dy = 0;
                return move(ns,dx,dy);
            }
        }
        else if(actionInd==4){
            // pull action!!


            CleanupAgent agent = ns.touchAgent();
            int ax = agent.x;
            int ay = agent.y;
            String dir = agent.currentDirection;

            CleanupBlock blockOld = blockToSwap(ns, ax, ay, dir);
            CleanupBlock block = ns.touchBlock(ns.blockInd(blockOld.name()));
            int bx = block.x;
            int by = block.y;

            agent.x= bx;
            agent.y= by;

            block.x= ax;
            block.y= ay;

            if(agent.directional){

                //face in direction of the block movement
                if(by - ay > 0){
                    agent.currentDirection = "south";
                }
                else if(by - ay < 0){
                    agent.currentDirection = "north";
                }
                else if(bx - ax > 0){
                    agent.currentDirection = "west";
                }
                else if(bx - ax < 0){
                    agent.currentDirection = "east";
                }

            }

            return Arrays.asList(new StateTransitionProb(ns, 1.));

        }

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
        else if(name.equals(ACTION_PULL)){
            return 4;
        }
        throw new RuntimeException("Unknown action " + name);
    }

    protected List<StateTransitionProb> move(State state, int xdelta, int ydelta){


        CleanupState initialState = ((CleanupState)state).copy();

        CleanupState ns = ((CleanupState)state);

        CleanupAgent agent = ns.touchAgent();
        int ax = agent.x;
        int ay = agent.y;

        int nx = ax+xdelta;
        int ny = ay+ydelta;

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


        if (agent.directional) {
            if (xdelta == 1) {
                agent.currentDirection = "east";
            } else if (xdelta == -1) {
                agent.currentDirection = "west";
            } else if (ydelta == 1) {
                agent.currentDirection = "north";
            } else if (ydelta == -1) {
                agent.currentDirection = "south";
            }
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


}