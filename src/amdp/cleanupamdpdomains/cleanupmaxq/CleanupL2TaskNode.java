package amdp.cleanupamdpdomains.cleanupmaxq;

import amdp.cleanupamdpdomains.cleanupamdp.L2TaskNode;
import amdp.cleanupamdpdomains.cleanuplevel1.state.CleanupL1StateMapper;
import amdp.cleanupamdpdomains.cleanuplevel2.state.CleanupL2StateMapper;
import amdp.maxq.framework.GroundedTask;
import amdp.maxq.framework.NonPrimitiveTaskNode;

import amdp.maxq.framework.TaskNode;
import burlap.mdp.auxiliary.StateMapping;
import burlap.mdp.core.action.Action;
import burlap.mdp.core.action.ActionType;
import burlap.mdp.core.oo.ObjectParameterizedAction;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.oo.OOSADomain;
import burlap.statehashing.HashableState;
import burlap.statehashing.HashableStateFactory;
import burlap.statehashing.simple.SimpleHashableStateFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ngopalan on 9/5/16.
 */
public class CleanupL2TaskNode extends NonPrimitiveTaskNode {

    ActionType ogaType;
    StateMapping l2sm = new CleanupL2StateMapper();
    StateMapping l1sm = new CleanupL1StateMapper();
    OOSADomain l2Domain;
    HashableStateFactory hsf = new SimpleHashableStateFactory();

    public CleanupL2TaskNode(String actionTypeName, OOSADomain L2Domain, TaskNode[] children){
        this.ogaType = L2Domain.getAction(actionTypeName);
        l2Domain = L2Domain;
        this.setTaskNodes(children);
    }

    @Override
    public List<String[]> parametersSet(State s) {
        List<String[]> params = new ArrayList<String[]>();
        State sMap = l2sm.mapState(l1sm.mapState(s));
        List<Action> actionList = ogaType.allApplicableActions(sMap);
        for(Action a : actionList){
            params.add(((ObjectParameterizedAction)a).getObjectParameters());
        }
        return params;
    }

    @Override
    public boolean terminal(State s, Action action) {
//        State L2State =l2sm.mapState(l1sm.mapState(s));
        State L1State = l1sm.mapState(s);
        return L2TaskNode.getL1Tf((ObjectParameterizedAction) action).isTerminal(L1State);
    }

    @Override
    public List<GroundedTask> getApplicableGroundedTasks(State s) {
        State sMap = l2sm.mapState(l1sm.mapState(s));
        List<Action> actionList = ogaType.allApplicableActions(sMap);
        List<GroundedTask> gtList = new ArrayList<GroundedTask>();
        for(Action a:actionList){
            gtList.add(new GroundedTask(this, a, 1));
        }
        return gtList;
    }

    @Override
    public boolean hasHashingFactory(){
        return true;
    }


    @Override
    public HashableState hashedState(State s, GroundedTask childTask){
        return this.hsf.hashState(l1sm.mapState(s));
    }


}
