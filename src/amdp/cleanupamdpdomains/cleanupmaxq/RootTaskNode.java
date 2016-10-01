package amdp.cleanupamdpdomains.cleanupmaxq;


import amdp.cleanupamdpdomains.cleanuplevel1.state.CleanupL1StateMapper;
import amdp.cleanupamdpdomains.cleanuplevel2.state.CleanupL2StateMapper;
import amdp.maxq.framework.GroundedTask;
import amdp.maxq.framework.NonPrimitiveTaskNode;
import amdp.maxq.framework.TaskNode;
import amdp.taxi.state.TaxiPassenger;
import amdp.taxi.state.TaxiState;
import burlap.behavior.valuefunction.QValue;
import burlap.mdp.auxiliary.StateMapping;
import burlap.mdp.core.TerminalFunction;
import burlap.mdp.core.action.Action;
import burlap.mdp.core.action.SimpleAction;
import burlap.mdp.core.state.State;
import burlap.statehashing.HashableState;
import burlap.statehashing.HashableStateFactory;
import burlap.statehashing.WrappedHashableState;
import burlap.statehashing.simple.SimpleHashableStateFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Task node for the get task, parameters are passenger names.
 * Created by ngopalan on 5/24/16.
 */
public class RootTaskNode extends NonPrimitiveTaskNode {

    List<String[]> params = new ArrayList<String[]>();
    List<GroundedTask> groundedTasks = new ArrayList<GroundedTask>();
    TerminalFunction tf;


    StateMapping l2sm = new CleanupL2StateMapper();
    StateMapping l1sm = new CleanupL1StateMapper();

    HashableStateFactory hsf = new SimpleHashableStateFactory();
//            new HashableStateFactory() {
//        @Override
//        public HashableState hashState(State s) {
//            return new RootHashState(s);
//        }
//    };

    public RootTaskNode(String name, TaskNode[] children, TerminalFunction tfIn) {
        this.name = name;
        this.params.add(new String[]{"1"});
        this.setTaskNodes(children);
        for(String[] param:params){
            groundedTasks.add(new GroundedTask(this, new SimpleAction(name+":"+param), 0));
        }
        this.tf = tfIn;
    }

    @Override
    public boolean hasHashingFactory(){
        return true;
    }

    @Override
    public HashableState hashedState(State s, GroundedTask childTask){
        State L2State =l2sm.mapState(l1sm.mapState(s));
        return this.hsf.hashState(L2State);
    }

    @Override
    public Object parametersSet(State s) {
        return params;
    }

    @Override
    public boolean terminal(State s, Action action) {
        return tf.isTerminal(l2sm.mapState(l1sm.mapState(s)));
    }



    @Override
    public List<GroundedTask> getApplicableGroundedTasks(State s) {
        return this.groundedTasks;
    }


}
