package amdp.taxiamdpdomains.taxiamdp;

import amdp.amdpframework.GroundedPropSC;
import amdp.amdpframework.GroundedTask;
import amdp.amdpframework.NonPrimitiveTaskNode;
import amdp.amdpframework.TaskNode;
import amdp.taxi.TaxiDomain;
import amdp.taxiamdpdomains.taxiamdplevel1.TaxiL1Domain;
import burlap.mdp.auxiliary.common.GoalConditionTF;
import burlap.mdp.auxiliary.stateconditiontest.StateConditionTest;
import burlap.mdp.core.action.Action;
import burlap.mdp.core.action.ActionType;
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
public class NavigateTaskNode extends NonPrimitiveTaskNode{


//    OOSADomain l0Domain;

    ActionType navigateType;

    public NavigateTaskNode(ActionType navigateType, OOSADomain taxiL1Domain, OOSADomain taxiL0Domain, TaskNode[] children){
        this.navigateType = navigateType;
        this.oosaDomain = taxiL0Domain;
//        this.l0Domain = taxiL0Domain;
        this.childTaskNodes = children;
    }

    @Override
    public Object parametersSet(State s) {
        List<String[]> params = new ArrayList<String[]>();
        List<Action> gtActions = navigateType.allApplicableActions(s);
        for(Action a:gtActions){
            params.add(new String[]{a.actionName().split("_")[1]});
        }
        return params;
    }


    @Override
    public boolean terminal(State s, Action action) {
        String location = ((TaxiL1Domain.NavigateType.NavigateAction)action).location;
        StateConditionTest sc =  new GroundedPropSC(new GroundedProp(oosaDomain.propFunction(TaxiDomain.TAXIATLOCATIONPF), new String[]{location}));
        return new GoalConditionTF(sc).isTerminal(s);
    }

    @Override
    public RewardFunction rewardFunction(Action action) {
        String location = ((TaxiL1Domain.NavigateType.NavigateAction)action).location;
        StateConditionTest sc =  new GroundedPropSC(new GroundedProp(oosaDomain.propFunction(TaxiDomain.TAXIATLOCATIONPF), new String[]{location}));
        return new GoalBasedRF(sc);
    }

    @Override
    public List<GroundedTask> getApplicableGroundedTasks(State s) {
        List<GroundedTask> gtList = new ArrayList<GroundedTask>();
        List<Action> gtActions = navigateType.allApplicableActions(s);
        for(Action a:gtActions){
            gtList.add(new GroundedTask(this,a));
        }
        return gtList;
    }

}
