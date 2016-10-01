package amdp.cleanupamdpdomains.cleanupmaxq;

import amdp.amdpframework.GroundedPropSC;
import amdp.cleanup.CleanupDomain;
import amdp.cleanupamdpdomains.cleanupamdp.L2TaskNode;
import amdp.cleanupamdpdomains.cleanuplevel1.CleanupL1Domain;
import amdp.cleanupamdpdomains.cleanuplevel1.state.CleanupL1StateMapper;
import amdp.cleanupamdpdomains.cleanuplevel2.state.CleanupL2StateMapper;
import amdp.maxq.framework.GroundedTask;
import amdp.maxq.framework.NonPrimitiveTaskNode;

import amdp.maxq.framework.PrimitiveTaskNode;
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
import burlap.statehashing.WrappedHashableState;
import burlap.statehashing.simple.SimpleHashableStateFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ngopalan on 9/5/16.
 */
public class CleanupL0TaskNode extends PrimitiveTaskNode {

    ActionType ogaType;
    StateMapping l1sm = new CleanupL1StateMapper();
//    OOSADomain l1Domain;
    OOSADomain oosaDomain;

    HashableStateFactory hsf = new HashableStateFactory() {
        @Override
        public HashableState hashState(State s) {
            return new MoveHashState(s);
        }
    };

    public CleanupL0TaskNode(String actionTypeName, OOSADomain l0Domain){
        this.ogaType = l0Domain.getAction(actionTypeName);
        this.actionType = ogaType;
//        l1Domain = L1Domain;
        oosaDomain = l0Domain;

    }


    @Override
    public List<GroundedTask> getApplicableGroundedTasks(State s) {
//        State sMap = l1sm.mapState(s);
        List<Action> actionList = ogaType.allApplicableActions(s);
        List<GroundedTask> gtList = new ArrayList<GroundedTask>();
        for(Action a:actionList){
            gtList.add(new GroundedTask(this, a, 3));
        }
        return gtList;
    }

    @Override
    public boolean hasHashingFactory(){
        return true;
    }

    @Override
    public HashableState hashedState(State s){
        return this.hsf.hashState(s);
    }


    public class MoveHashState extends WrappedHashableState {
        // original state
        State state;

        public MoveHashState(State s){
            this.state = s;
        }


        @Override
        public int hashCode() {
            // boolean true or false
            return 1;
        }

        @Override
        public boolean equals(Object obj) {
            // check hash of both obj and our, if equal then return true else false!
            if (obj == null) {
                return false;
            }

            if (getClass() != obj.getClass()) {
                return false;
            }

            return true;
        }
    }


}
