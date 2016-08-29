package amdp.taxiamdpdomains.taxiamdp;

import amdp.amdpframework.GroundedPropSC;
import amdp.amdpframework.GroundedTask;
import amdp.amdpframework.NonPrimitiveTaskNode;
import amdp.amdpframework.TaskNode;
import amdp.taxi.TaxiDomain;
import burlap.mdp.auxiliary.common.GoalConditionTF;
import burlap.mdp.auxiliary.stateconditiontest.StateConditionTest;
import burlap.mdp.core.action.Action;
import burlap.mdp.core.action.ActionType;
import burlap.mdp.core.action.UniversalActionType;
import burlap.mdp.core.oo.propositional.GroundedProp;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.common.GoalBasedRF;
import burlap.mdp.singleagent.model.RewardFunction;
import burlap.mdp.singleagent.oo.OOSADomain;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ngopalan on 8/14/16.
 */
public class PutDownL1TaskNode extends NonPrimitiveTaskNode{
//    OOSADomain l0Domain;

    ActionType putDownType;

    public PutDownL1TaskNode(ActionType putDownType, OOSADomain taxiL1Domain, OOSADomain taxiL0Domain, TaskNode[] children){
        this.putDownType = putDownType;
        this.oosaDomain = taxiL0Domain;
//        this.l0Domain = taxiL0Domain;
//        this.oosaDomain = taxiL0Domain;
        this.oosaDomain.clearActionTypes();
        this.oosaDomain.addActionTypes(
//                new UniversalActionType(ACTION_NORTH),
//                new UniversalActionType(ACTION_SOUTH),
//                new UniversalActionType(ACTION_EAST),
//                new UniversalActionType(ACTION_WEST),
                new UniversalActionType(TaxiDomain.ACTION_DROPOFF)
//                new UniversalActionType(ACTION_FILLUP),
//                new UniversalActionType(TaxiDomain.ACTION_PICKUP)
            );
        this.childTaskNodes = children;
    }

    @Override
    public Object parametersSet(State s) {
        List<String[]> params = new ArrayList<String[]>();
        params.add(new String[]{});
        return params;
    }

    @Override
    public boolean terminal(State s, Action action) {
        StateConditionTest sc =  new GroundedPropSC(new GroundedProp(oosaDomain.propFunction(TaxiDomain.PASSENGERPUTDOWNPF), new String[]{}));
        return new GoalConditionTF(sc).isTerminal(s);
    }

    @Override
    public RewardFunction rewardFunction(Action action) {
        StateConditionTest sc =  new GroundedPropSC(new GroundedProp(oosaDomain.propFunction(TaxiDomain.PASSENGERPUTDOWNPF), new String[]{}));
        return new GoalBasedRF(sc);
    }

    @Override
    public List<GroundedTask> getApplicableGroundedTasks(State s) {
        List<GroundedTask> gtList = new ArrayList<GroundedTask>();
        List<Action> gtActions = putDownType.allApplicableActions(s);
        for(Action a:gtActions){
            gtList.add(new GroundedTask(this,a));
        }
        return gtList;
    }
}
