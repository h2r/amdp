package amdp.maxq.taximaxq;

import amdp.maxq.framework.PrimitiveTaskNode;
import burlap.mdp.core.action.ActionType;

/**
 * Created by ngopalan on 5/6/16.
 */
public class TaxiMAXQL0CardinalMoveTaskNode extends PrimitiveTaskNode {
    public TaxiMAXQL0CardinalMoveTaskNode(ActionType a){
        this.setActionType(a);
    }

}
