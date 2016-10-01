package amdp.cleanupturtlebot;

import amdp.amdpframework.GroundedPropSC;
import amdp.amdpframework.GroundedTask;
import amdp.amdpframework.NonPrimitiveTaskNode;
import amdp.amdpframework.TaskNode;
import amdp.cleanup.CleanupDomain;
import amdp.cleanup.PullCostGoalRF;
import amdp.cleanupamdpdomains.cleanuplevel1.CleanupL1Domain;
import amdp.cleanupturtlebot.cleanupcontinuous.CleanupContinuousDomain;
import amdp.cleanupturtlebot.cleanupl0discrete.CleanupTurtleBotL0Domain;
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
import org.apache.commons.lang3.tuple.MutablePair;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ngopalan on 8/30/16.
 */
public class L0TaskNode extends NonPrimitiveTaskNode {

    ActionType actionType;


    public L0TaskNode(ActionType actionType, OOSADomain l0Domain, TaskNode[] children) {
        this.childTaskNodes =children;
        this.oosaDomain = l0Domain;
        this.actionType = actionType;
    }

    @Override
    public Object parametersSet(State s) {
        List<String[]> params = new ArrayList<String[]>();
//        List<Action> gtActions = actionType.allApplicableActions(s);
//        for(Action a:gtActions){
//            params.add(((ObjectParameterizedAction)a).getObjectParameters());
//        }
        return params;
    }

    @Override
    public boolean terminal(State s, Action action) {
        return getL0Tf(action).isTerminal(s);
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
        return getL0Rf(action);
    }

    public TerminalFunction getL0Tf(Action action){

        StateConditionTest sc = null;
        if(action.actionName().equals(CleanupTurtleBotL0Domain.ACTION_TURN_CW)){
            CleanupTurtleBotL0Domain.TurnCWType.TurnCWAction a = (CleanupTurtleBotL0Domain.TurnCWType.TurnCWAction)action;
            String goalDir = a.goalDirection();
            sc = new GroundedPropSC(new GroundedProp(this.oosaDomain.propFunction(CleanupContinuousDomain.PF_AGENT_IN_DIR), new String[]{goalDir}));
        }
        else if(action.actionName().equals(CleanupTurtleBotL0Domain.ACTION_TURN_CCW)) {
            CleanupTurtleBotL0Domain.TurnCCWType.TurnCCWAction a = (CleanupTurtleBotL0Domain.TurnCCWType.TurnCCWAction) action;
            String goalDir = a.goalDirection();
            sc = new GroundedPropSC(new GroundedProp(this.oosaDomain.propFunction(CleanupContinuousDomain.PF_AGENT_IN_DIR), new String[]{goalDir}));
        }
            else if(action.actionName().equals(CleanupTurtleBotL0Domain.ACTION_MOVE_FORWARD)){
            CleanupTurtleBotL0Domain.MoveForwardType.MoveForwardAction a = (CleanupTurtleBotL0Domain.MoveForwardType.MoveForwardAction) action;
            MutablePair<Integer,Integer> goal = a.goalLocation();
            sc = new GroundedPropSC(new GroundedProp(this.oosaDomain.propFunction(CleanupContinuousDomain.PF_AGENT_IN_CELL), new String[]{""+goal.getLeft(), ""+goal.getRight()}));
        }
        else if(action.actionName().equals(CleanupTurtleBotL0Domain.ACTION_MOVE_BACK)){
            CleanupTurtleBotL0Domain.MoveBackType.MoveBackAction a = (CleanupTurtleBotL0Domain.MoveBackType.MoveBackAction) action;
            MutablePair<Integer,Integer> goal = a.goalLocation();
            sc = new GroundedPropSC(new GroundedProp(this.oosaDomain.propFunction(CleanupContinuousDomain.PF_AGENT_IN_CELL), new String[]{""+goal.getLeft(), ""+goal.getRight()}));
        }
        return new GoalConditionTF(sc);
    }


    public RewardFunction getL0Rf(Action action){
        StateConditionTest sc = null;
        if(action.actionName().equals(CleanupTurtleBotL0Domain.ACTION_TURN_CW)){
            CleanupTurtleBotL0Domain.TurnCWType.TurnCWAction a = (CleanupTurtleBotL0Domain.TurnCWType.TurnCWAction)action;
            String goalDir = a.goalDirection();
            sc = new GroundedPropSC(new GroundedProp(this.oosaDomain.propFunction(CleanupContinuousDomain.PF_AGENT_IN_DIR), new String[]{goalDir}));
        }
        else if(action.actionName().equals(CleanupTurtleBotL0Domain.ACTION_TURN_CCW)) {
            CleanupTurtleBotL0Domain.TurnCCWType.TurnCCWAction a = (CleanupTurtleBotL0Domain.TurnCCWType.TurnCCWAction) action;
            String goalDir = a.goalDirection();
            sc = new GroundedPropSC(new GroundedProp(this.oosaDomain.propFunction(CleanupContinuousDomain.PF_AGENT_IN_DIR), new String[]{goalDir}));
        }
        else if(action.actionName().equals(CleanupTurtleBotL0Domain.ACTION_MOVE_FORWARD)){
            CleanupTurtleBotL0Domain.MoveForwardType.MoveForwardAction a = (CleanupTurtleBotL0Domain.MoveForwardType.MoveForwardAction) action;
            MutablePair<Integer,Integer> goal = a.goalLocation();
            sc = new GroundedPropSC(new GroundedProp(this.oosaDomain.propFunction(CleanupContinuousDomain.PF_AGENT_IN_CELL), new String[]{""+goal.getLeft(), ""+goal.getRight()}));
        }
        else if(action.actionName().equals(CleanupTurtleBotL0Domain.ACTION_MOVE_BACK)){
            CleanupTurtleBotL0Domain.MoveBackType.MoveBackAction a = (CleanupTurtleBotL0Domain.MoveBackType.MoveBackAction) action;
            MutablePair<Integer,Integer> goal = a.goalLocation();
            sc = new GroundedPropSC(new GroundedProp(this.oosaDomain.propFunction(CleanupContinuousDomain.PF_AGENT_IN_CELL), new String[]{""+goal.getLeft(), ""+goal.getRight()}));
        }
        return new PullCostGoalRF(sc, 1., 0.);
    }
}
