package amdp.cleanupamdpdomains.cleanupamdp;

import amdp.amdpframework.GroundedTask;
import amdp.amdpframework.NonPrimitiveTaskNode;
import amdp.amdpframework.TaskNode;
import amdp.cleanup.CleanupDomain;
import amdp.cleanupamdpdomains.cleanuplevel1.CleanupL1Domain;
import amdp.cleanupamdpdomains.cleanuplevel1.state.CleanupDoorL1;
import amdp.cleanupamdpdomains.cleanuplevel1.state.CleanupL1State;
import burlap.mdp.auxiliary.common.GoalConditionTF;
import burlap.mdp.auxiliary.stateconditiontest.StateConditionTest;
import burlap.mdp.core.TerminalFunction;
import burlap.mdp.core.action.Action;
import burlap.mdp.core.action.ActionType;
import burlap.mdp.core.oo.ObjectParameterizedAction;
import burlap.mdp.core.oo.state.ObjectInstance;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.common.GoalBasedRF;
import burlap.mdp.singleagent.model.RewardFunction;
import burlap.mdp.singleagent.oo.OOSADomain;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ngopalan on 8/30/16.
 */
public class L2TaskNode extends NonPrimitiveTaskNode {

    ActionType actionType;

    public L2TaskNode(ActionType actionType, OOSADomain l0Domain, TaskNode[] children) {
        this.childTaskNodes =children;
        this.oosaDomain = l0Domain;
        this.actionType = actionType;
    }

    @Override
    public Object parametersSet(State s) {
        List<String[]> params = new ArrayList<String[]>();
        List<Action> gtActions = actionType.allApplicableActions(s);
        for(Action a:gtActions){
            params.add(((ObjectParameterizedAction)a).getObjectParameters());
        }
        return params;
    }

    @Override
    public boolean terminal(State s, Action action) {
        return getL1Tf((ObjectParameterizedAction)action).isTerminal(s);
    }

    @Override
    public List<GroundedTask> getApplicableGroundedTasks(State s) {
        List<GroundedTask> gtList = new ArrayList<GroundedTask>();
        List<Action> gtActions = actionType.allApplicableActions(s);
        for(Action a:gtActions){
            gtList.add(new GroundedTask(this,a));
        }
        return gtList;
    }

    @Override
    public RewardFunction rewardFunction(Action action) {
        return getL1Rf((ObjectParameterizedAction) action );
    }

    public static RewardFunction getL1Rf(ObjectParameterizedAction oga){
        StateConditionTest sc = null;
        if(oga.actionName().equals(CleanupL1Domain.ACTION_AGENT_TO_ROOM)){
            sc = new CleanupL1Domain.InRegionSC(CleanupDomain.CLASS_AGENT+0, oga.getObjectParameters()[0]);
        }
        else{
            sc = new CleanupL1Domain.InRegionSC(oga.getObjectParameters()[0], oga.getObjectParameters()[1]);
        }
        return new GoalBasedRF(sc, 1., 0.);
    }


    public static TerminalFunction getL1Tf(ObjectParameterizedAction oga){
        StateConditionTest sc = null;
        if(oga.actionName().equals(CleanupL1Domain.ACTION_AGENT_TO_ROOM)){
            sc = new CleanupL1Domain.InRegionSC(CleanupDomain.CLASS_AGENT + 0, oga.getObjectParameters()[0]);
        }
        else{
            sc = new DoorLockedSC(oga.getObjectParameters()[1], new CleanupL1Domain.InRegionSC(oga.getObjectParameters()[0], oga.getObjectParameters()[1]));
        }
        return new GoalConditionTF(sc);
    }

    public static class DoorLockedSC implements StateConditionTest{

        public String door;
        public StateConditionTest otherSC;

        public DoorLockedSC(String door, StateConditionTest otherSC) {
            this.door = door;
            this.otherSC = otherSC;
        }

        @Override
        public boolean satisfies(State s) {
            if(otherSC.satisfies(s)){
                return true;
            }

            ObjectInstance doorOb =  ((CleanupL1State)s).object(door);
            if(doorOb instanceof CleanupDoorL1) {
                if (((CleanupDoorL1)doorOb).canBeLocked) {
                    int lockedVal = ((CleanupDoorL1)doorOb).locked;
                    return lockedVal == 2;
                }
            }

            return false;
        }
    }
}
