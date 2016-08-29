package amdp.taxiamdpdomains.taxiamdp;

import amdp.amdpframework.GroundedPropSC;
import amdp.amdpframework.GroundedTask;
import amdp.amdpframework.NonPrimitiveTaskNode;
import amdp.amdpframework.TaskNode;
import amdp.taxiamdpdomains.taxiamdplevel1.TaxiL1Domain;
import amdp.taxiamdpdomains.taxiamdplevel2.TaxiL2Domain;
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
public class GetTaskNode extends NonPrimitiveTaskNode{

//    OOSADomain l1Domain;

    ActionType getType;
    public GetTaskNode(ActionType getType, OOSADomain taxiL2Domain, OOSADomain taxiL1Domain, TaskNode[] children){
        this.getType = getType;
//        this.oosaDomain = taxiL2Domain;
        this.oosaDomain = taxiL1Domain;
        this.oosaDomain.clearActionTypes();
        this.oosaDomain.addActionTypes(
//                new UniversalActionType(TaxiL1Domain.ACTION_PUTDOWNL1),
                new UniversalActionType(TaxiL1Domain.ACTION_PICKUPL1),
                new TaxiL1Domain.NavigateType());
        this.childTaskNodes = children;
    }

    @Override
    public Object parametersSet(State s) {
        List<String[]> params = new ArrayList<String[]>();
        List<Action> gtActions = getType.allApplicableActions(s);
        for(Action a:gtActions){
            params.add(new String[]{a.actionName().split("_")[1]});
        }
        return null;
    }

    @Override
    public boolean terminal(State s, Action action) {

        String passName = ((TaxiL2Domain.GetType.GetAction)action).passenger;
        StateConditionTest sc =  new GroundedPropSC(new GroundedProp(oosaDomain.propFunction(TaxiL1Domain.TAXIATPASSENGERPF), new String[]{passName}));
        return new GoalConditionTF(sc).isTerminal(s);
    }

    @Override
    public RewardFunction rewardFunction(Action action) {
        String passName = ((TaxiL2Domain.GetType.GetAction)action).passenger;
        StateConditionTest sc =  new GroundedPropSC(new GroundedProp(oosaDomain.propFunction(TaxiL1Domain.TAXIATPASSENGERPF), new String[]{passName}));
        return new GoalBasedRF(sc);
    }

    @Override
    public List<GroundedTask> getApplicableGroundedTasks(State s) {
        List<GroundedTask> gtList = new ArrayList<GroundedTask>();
        List<Action> gtActions = getType.allApplicableActions(s);
        for(Action a:gtActions){
            gtList.add(new GroundedTask(this,a));
        }
        return gtList;
    }
}
