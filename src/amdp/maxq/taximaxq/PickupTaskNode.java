package amdp.maxq.taximaxq;

import amdp.maxq.framework.PrimitiveTaskNode;
import burlap.mdp.core.action.ActionType;

/**
 * Created by ngopalan on 5/24/16.
 */
public class PickupTaskNode extends PrimitiveTaskNode {
    public PickupTaskNode (ActionType a) {
        this.setActionType(a);
    }
}
