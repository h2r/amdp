package amdp.cleanupturtlebot;

import amdp.amdpframework.PrimitiveTaskNode;
import burlap.mdp.core.action.ActionType;

/**
 * Created by ngopalan on 8/30/16.
 */
public class ContinuousTaskNode extends PrimitiveTaskNode {

    public ContinuousTaskNode(ActionType a){
        this.setActionType(a);
    }

}
