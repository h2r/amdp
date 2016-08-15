package amdp.taxiamdpdomains.taxiamdp;

import amdp.amdpframework.PrimitiveTaskNode;
import burlap.mdp.core.action.Action;
import burlap.mdp.core.action.ActionType;
import burlap.mdp.singleagent.model.RewardFunction;

/**
 * Created by ngopalan on 8/14/16.
 */
public class MoveTaskNodes extends PrimitiveTaskNode{

    public MoveTaskNodes(ActionType a){
        this.setActionType(a);
    }

}
