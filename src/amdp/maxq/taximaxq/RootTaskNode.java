package amdp.maxq.taximaxq;


import amdp.maxq.framework.GroundedTask;
import amdp.maxq.framework.NonPrimitiveTaskNode;
import amdp.maxq.framework.TaskNode;
import amdp.taxi.state.TaxiPassenger;
import amdp.taxi.state.TaxiState;
import burlap.behavior.valuefunction.QValue;
import burlap.mdp.core.TerminalFunction;
import burlap.mdp.core.action.Action;
import burlap.mdp.core.action.SimpleAction;
import burlap.mdp.core.state.State;
import burlap.statehashing.HashableState;
import burlap.statehashing.HashableStateFactory;
import burlap.statehashing.WrappedHashableState;

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

    HashableStateFactory hsf = new HashableStateFactory() {
        @Override
        public HashableState hashState(State s) {
            return new RootHashState(s);
        }
    };

    public RootTaskNode(String name, TaskNode[] children, TerminalFunction tfIn) {
        this.name = name;
        this.params.add(new String[]{"1"});
        this.setTaskNodes(children);
        for(String[] param:params){
            groundedTasks.add(new GroundedTask(this, new SimpleAction(name+":"+param)));
        }
        this.tf = tfIn;
    }

    @Override
    public boolean hasHashingFactory(){
        return true;
    }

    @Override
    public HashableState hashedState(State s, GroundedTask childTask){
        // if navigate return the navigate states else return the pickup hash states
        return this.hsf.hashState(s);
    }

    @Override
    public Object parametersSet(State s) {
        // here I am going to presume that a passenger is present in the task node
        return params;
    }

    @Override
    public boolean terminal(State s, Action action) {
       return tf.isTerminal(s);
    }



    @Override
    public List<GroundedTask> getApplicableGroundedTasks(State s) {
        return this.groundedTasks;
    }

    public class RootHashState extends WrappedHashableState {
        // original state
        State state;

        public RootHashState(State s){
            this.state = s;
        }

        private int createHash(){
            // check source and return state!
            int hashC =0;
            for(TaxiPassenger p:((TaxiState)state).passengers){
                hashC = 31 * hashC + 17* p.originalSourceLocation.hashCode() + p.goalLocation.hashCode();
            }
            return hashC;
        }

        @Override
        public int hashCode() {
            // boolean true or false
            return createHash();
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

            RootHashState otherObj = (RootHashState)obj;

            // if legal then equal
            return (otherObj.createHash() == this.createHash()) ? true : false;
        }
    }

}
