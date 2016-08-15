package amdp.amdpframework;


import burlap.mdp.core.action.Action;
import burlap.mdp.core.action.ActionType;
import burlap.mdp.core.action.ActionUtils;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.common.UniformCostRF;
import burlap.mdp.singleagent.model.RewardFunction;
import burlap.statehashing.HashableState;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by ngopalan on 5/6/16.
 */
public abstract class PrimitiveTaskNode implements TaskNode {
    protected ActionType actionType;

    public ActionType getActionType() {
        return actionType;
    }

    public void setActionType(ActionType actionType) {
        this.actionType = actionType;
    }

    @Override
    public boolean isTaskPrimitive(){
        return true;
    }

    @Override
    public String getName(){
        return actionType.typeName();
    }


    @Override
    public boolean terminal(State s, Action action) {
        return true;
    }


    @Override
    public RewardFunction rewardFunction(Action action){
        return new UniformCostRF();
    }

    @Override
    public List<GroundedTask> getApplicableGroundedTasks(State s){
        List<Action> gaList = ActionUtils.allApplicableActionsForTypes(Arrays.asList((ActionType)actionType), s);
        List<GroundedTask> gtList = new ArrayList<GroundedTask>();
        for(Action ga:gaList){
            // this action is an ObjectParameterizedAction
//            if(ga.applicableInState(s)){
            gtList.add(new GroundedTask(this, ga));
//            }
        }
        return gtList;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PrimitiveTaskNode that = (PrimitiveTaskNode) o;

        return actionType != null ? actionType.equals(that.actionType) : that.actionType== null;

    }

    @Override
    public int hashCode() {
        return actionType != null ? actionType.hashCode() : 0;
    }

    public boolean hasHashingFactory(){
        return false;
    }

    public HashableState hashedState(State s){
        System.err.println("Tried to get hashable state when not set at the node!");
        return null;
    }
}
