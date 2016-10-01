package amdp.cleanupamdpdomains.cleanupmaxq;

import amdp.amdpframework.GroundedPropSC;
import amdp.cleanup.CleanupDomain;
import amdp.cleanupamdpdomains.cleanupamdp.L2TaskNode;
import amdp.cleanupamdpdomains.cleanuplevel1.CleanupL1Domain;
import amdp.cleanupamdpdomains.cleanuplevel1.state.CleanupL1StateMapper;
import amdp.cleanupamdpdomains.cleanuplevel2.state.CleanupL2StateMapper;
import amdp.maxq.framework.GroundedTask;
import amdp.maxq.framework.NonPrimitiveTaskNode;

import amdp.maxq.framework.TaskNode;
import burlap.mdp.auxiliary.StateMapping;
import burlap.mdp.auxiliary.common.GoalConditionTF;
import burlap.mdp.auxiliary.stateconditiontest.StateConditionTest;
import burlap.mdp.core.TerminalFunction;
import burlap.mdp.core.action.Action;
import burlap.mdp.core.action.ActionType;
import burlap.mdp.core.oo.ObjectParameterizedAction;
import burlap.mdp.core.oo.propositional.GroundedProp;
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
public class CleanupL1TaskNode extends NonPrimitiveTaskNode {

    ActionType ogaType;
    StateMapping l1sm = new CleanupL1StateMapper();
    OOSADomain l1Domain;
    OOSADomain oosaDomain;
    HashableStateFactory hsf = new SimpleHashableStateFactory();

    public CleanupL1TaskNode(String actionTypeName, OOSADomain L1Domain, OOSADomain l0Domain, TaskNode[] children){
        this.ogaType = L1Domain.getAction(actionTypeName);
//        this.actionType = ogaType;
        l1Domain = L1Domain;
        oosaDomain = l0Domain;
        this.setTaskNodes(children);
    }

    @Override
    public List<String[]> parametersSet(State s) {
        List<String[]> params = new ArrayList<String[]>();
        State sMap = l1sm.mapState(s);
        List<Action> actionList = ogaType.allApplicableActions(sMap);
        for(Action a : actionList){
            params.add(((ObjectParameterizedAction)a).getObjectParameters());
        }
        return params;
    }

    @Override
    public boolean terminal(State s, Action action) {
        return getL0Tf((ObjectParameterizedAction)action).isTerminal(s);
    }



    @Override
    public List<GroundedTask> getApplicableGroundedTasks(State s) {
        State sMap = l1sm.mapState(s);
        List<Action> actionList = ogaType.allApplicableActions(sMap);
        List<GroundedTask> gtList = new ArrayList<GroundedTask>();
        for(Action a:actionList){
            gtList.add(new GroundedTask(this, a, 2));
        }
        return gtList;
    }

    @Override
    public boolean hasHashingFactory(){
        return true;
    }


    @Override
    public HashableState hashedState(State s, GroundedTask childTask){
        return this.hsf.hashState(s);
    }

    public TerminalFunction getL0Tf(ObjectParameterizedAction oga){
        StateConditionTest sc = null;
        if(oga.actionName().equals(CleanupL1Domain.ACTION_AGENT_TO_ROOM)){
            sc = new GroundedPropSC(new GroundedProp(this.oosaDomain.propFunction(CleanupDomain.PF_AGENT_IN_ROOM), new String[]{CleanupDomain.CLASS_AGENT + 0, oga.getObjectParameters()[0]}));
        }
        else if(oga.actionName().equals(CleanupL1Domain.ACTION_AGENT_TO_DOOR)){
            sc = new CleanupL1Domain.DoorLockedSC(oga.getObjectParameters()[0], new GroundedPropSC(new GroundedProp(this.oosaDomain.propFunction(CleanupDomain.PF_AGENT_IN_DOOR), new String[]{CleanupDomain.CLASS_AGENT + 0, oga.getObjectParameters()[0]})));
        }
        else if(oga.actionName().equals(CleanupL1Domain.ACTION_BLOCK_TO_ROOM)){
            sc = new GroundedPropSC(new GroundedProp(this.oosaDomain.propFunction(CleanupDomain.PF_BLOCK_IN_ROOM), new String[]{oga.getObjectParameters()[0], oga.getObjectParameters()[1]}));
        }
        else if(oga.actionName().equals(CleanupL1Domain.ACTION_BLOCK_TO_DOOR)){
            sc = new CleanupL1Domain.DoorLockedSC(oga.getObjectParameters()[1], new GroundedPropSC(new GroundedProp(this.oosaDomain.propFunction(CleanupDomain.PF_BLOCK_IN_DOOR), new String[]{oga.getObjectParameters()[0], oga.getObjectParameters()[1]})));
        }
        return new GoalConditionTF(sc);
    }


}
