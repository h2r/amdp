package amdp.cleanupamdpdomains.cleanupamdp;

import amdp.amdpframework.GroundedPropSC;
import amdp.amdpframework.GroundedTask;
import amdp.amdpframework.NonPrimitiveTaskNode;
import amdp.amdpframework.TaskNode;
import amdp.cleanup.CleanupDomain;
import amdp.cleanup.PullCostGoalRF;
import amdp.cleanupamdpdomains.cleanuplevel1.CleanupL1Domain;
import burlap.mdp.auxiliary.common.GoalConditionTF;
import burlap.mdp.auxiliary.stateconditiontest.StateConditionTest;
import burlap.mdp.core.TerminalFunction;
import burlap.mdp.core.action.Action;
import burlap.mdp.core.action.ActionType;
import burlap.mdp.core.oo.ObjectParameterizedAction;
import burlap.mdp.core.oo.propositional.GroundedProp;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.model.RewardFunction;
import burlap.mdp.singleagent.oo.OOSADomain;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ngopalan on 8/30/16.
 */
public class L1TaskNode extends NonPrimitiveTaskNode {

    ActionType actionType;


    public L1TaskNode(ActionType actionType, OOSADomain l0Domain, TaskNode[] children) {
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
        return getL0Tf((ObjectParameterizedAction)action).isTerminal(s);
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
        return getL0Rf((ObjectParameterizedAction) action);
    }

    public TerminalFunction getL0Tf(ObjectParameterizedAction oga){
        StateConditionTest sc = null;
        if(oga.actionName().equals(CleanupL1Domain.ACTION_AGENT_TO_ROOM)){
            sc = new GroundedPropSC(new GroundedProp(this.oosaDomain.propFunction(CleanupDomain.PF_AGENT_IN_ROOM), new String[]{CleanupDomain.CLASS_AGENT + 0, oga.getObjectParameters()[0]}));
        }
        else if(oga.actionName().equals(CleanupL1Domain.ACTION_AGENT_TO_DOOR)){
            sc = new CleanupL1Domain.DoorLockedSC(oga.getObjectParameters()[0], new GroundedPropSC(new GroundedProp(this.oosaDomain.propFunction(CleanupDomain.PF_AGENT_IN_DOOR), new String[]{CleanupDomain.CLASS_AGENT + 0, oga.getObjectParameters()[0]})));
        }
        else if(oga.actionName().equals(CleanupL1Domain.ACTION_BLOCK_TO_ROOM)){
            sc = new GroundedPropSC(new GroundedProp(this.oosaDomain.propFunction(CleanupDomain.PF_BLOCK_IN_ROOM), new String[]{oga.getObjectParameters()[0], oga.getObjectParameters()[1]}));
        }
        else if(oga.actionName().equals(CleanupL1Domain.ACTION_BLOCK_TO_DOOR)){
            sc = new CleanupL1Domain.DoorLockedSC(oga.getObjectParameters()[1], new GroundedPropSC(new GroundedProp(this.oosaDomain.propFunction(CleanupDomain.PF_BLOCK_IN_DOOR), new String[]{oga.getObjectParameters()[0], oga.getObjectParameters()[1]})));
        }
        return new GoalConditionTF(sc);
    }


    public RewardFunction getL0Rf(ObjectParameterizedAction oga){
        StateConditionTest sc = null;
        if(oga.actionName().equals(CleanupL1Domain.ACTION_AGENT_TO_ROOM)){
            sc = new GroundedPropSC(new GroundedProp(this.oosaDomain.propFunction(CleanupDomain.PF_AGENT_IN_ROOM), new String[]{CleanupDomain.CLASS_AGENT+0, oga.getObjectParameters()[0]}));
        }
        else if(oga.actionName().equals(CleanupL1Domain.ACTION_AGENT_TO_DOOR)){
            sc = new GroundedPropSC(new GroundedProp(this.oosaDomain.propFunction(CleanupDomain.PF_AGENT_IN_DOOR), new String[]{CleanupDomain.CLASS_AGENT+0, oga.getObjectParameters()[0]}));
        }
        else if(oga.actionName().equals(CleanupL1Domain.ACTION_BLOCK_TO_ROOM)){
            sc = new GroundedPropSC(new GroundedProp(this.oosaDomain.propFunction(CleanupDomain.PF_BLOCK_IN_ROOM), new String[]{oga.getObjectParameters()[0], oga.getObjectParameters()[1]}));
        }
        else if(oga.actionName().equals(CleanupL1Domain.ACTION_BLOCK_TO_DOOR)){
            sc = new GroundedPropSC(new GroundedProp(this.oosaDomain.propFunction(CleanupDomain.PF_BLOCK_IN_DOOR), new String[]{oga.getObjectParameters()[0], oga.getObjectParameters()[1]}));
        }

        return new PullCostGoalRF(sc, 1., 0.);
    }
}
