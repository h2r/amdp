package amdp.amdpframework;

import burlap.mdp.core.TerminalFunction;
import burlap.mdp.core.action.Action;
import burlap.mdp.core.action.ActionType;
import burlap.mdp.core.oo.state.generic.GenericOOState;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.model.FactoredModel;
import burlap.mdp.singleagent.model.RewardFunction;
import burlap.mdp.singleagent.oo.OOSADomain;
import burlap.mdp.singleagent.oo.ObjectParameterizedActionType;
import org.apache.commons.lang3.builder.HashCodeBuilder;


/**
 * Created by ngopalan on 5/14/16.
 */

public class GroundedTask {
    TaskNode t;

    Action action;


    public TaskNode getT() {
        return t;
    }

    public Action getAction() {
        return action;
    }

//    Object params;


    public GroundedTask(TaskNode t, ActionType at, String[] params){
        this.t = t;
        this.action = new ObjectParameterizedActionType.SAObjectParameterizedAction(at.typeName(), params);
    }

    public GroundedTask(TaskNode t, Action a){
        this.t = t;
        this.action = a;
    }

    public TerminalFunction terminalFunction(){
        return new TerminalFunction() {
            @Override
            public boolean isTerminal(State state) {
                return t.terminal(state,action);
            }
        };
    }

    public RewardFunction rewardFunction(){
        return t.rewardFunction(action);
    }

    public OOSADomain groundedDomain(){
        // only non-primitive task nodes have domains
        if(!(t instanceof NonPrimitiveTaskNode)){
            System.err.println("Domain queried from primitive node which is not possible!");
        }
        //TODO: add a copy method for these domains. We do not want to corrupt the original copy!
        OOSADomain d = new OOSADomain();
        d.setModel(((NonPrimitiveTaskNode)this.t).domain().getModel());
        ((FactoredModel)d.getModel()).setRf(rewardFunction());
        ((FactoredModel)d.getModel()).setTf(terminalFunction());
        return d;
    }

    @Override
    public int hashCode() {
        HashCodeBuilder hashCodeBuilder = new HashCodeBuilder(31, 7);
        hashCodeBuilder.append(t).append(action);
        return hashCodeBuilder.toHashCode();
    }


    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }

        if (!(other instanceof GroundedTask)) {
            return false;
        }

        GroundedTask o = (GroundedTask) other;

        // check if same task node
        if (!this.t.getName().equals(o.t.getName())) {
            return false;
        }

        if(!this.action.equals(o.action)){
            return false;
        }

//        if(this.params instanceof Object[]){
//            if(!(o.params instanceof Object[])){
//                return false;
//            }
//            if(Arrays.equals((Object[])this.getParams(),(Object[])o.getParams())){
//                return true;
//            }
//            return false;
//
//        }
//
//        if(!this.params.equals(o.params)){
//            return false;
//        }
        // check if same parameters expecting same parameter order groups

        // check if arrays and then check equals

        return true;

    }

}
