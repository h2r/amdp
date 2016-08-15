package amdp.taxiamdpdomains.taxiamdp;

import amdp.amdpframework.PrimitiveTaskNode;
import burlap.mdp.core.action.ActionType;

/**
 * Created by ngopalan on 8/14/16.
 */
public class PutDownL0TaskNode extends PrimitiveTaskNode{

    public PutDownL0TaskNode(ActionType a){
        this.setActionType(a);
    }

}
