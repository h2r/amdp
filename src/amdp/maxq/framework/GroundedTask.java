package amdp.maxq.framework;

import burlap.mdp.core.TerminalFunction;
import burlap.mdp.core.action.Action;
import burlap.mdp.core.action.ActionType;
import burlap.mdp.core.oo.ObjectParameterizedAction;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.oo.ObjectParameterizedActionType;
import org.apache.commons.lang3.builder.HashCodeBuilder;


/**
 * Created by ngopalan on 5/14/16.
 */

public class GroundedTask {
    TaskNode t;

    Action action;

    int level = 0;

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

    public GroundedTask(TaskNode t, Action a, int level){
        this.t = t;
        this.action = a;
        this.level = level;
    }

    public void setLevel(int level){
        this.level  =level;
    }

    public String actionName(){
        String s = action.actionName();
        if(action instanceof ObjectParameterizedAction){
            String[] params = ((ObjectParameterizedAction)action).getObjectParameters();
            for(String param :params){
                s+="_"+param;
            }
        }
        s+=level;
        return s;
    }

    public TerminalFunction getTerminalFunction(){
        return new TerminalFunction() {
            @Override
            public boolean isTerminal(State state) {
                return t.terminal(state,action);
            }
        };
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


        if(this.level!=o.level){
                return false;
        }

        // check if same task node
        if (!this.t.getName().equals(o.t.getName())) {
            return false;
        }

//        if(!this.action.equals(o.action)){
//            return false;
//        }

        if(!this.action.actionName().equals(o.action.actionName())){
            return false;
        }

        if(this.action instanceof ObjectParameterizedAction){
            if(!(o.action instanceof ObjectParameterizedAction)){
                return false;
            }

            String[] params_this = ((ObjectParameterizedAction)this.action).getObjectParameters();
            String[] params_other = ((ObjectParameterizedAction)o.action).getObjectParameters();

            if(params_other.length!=params_this.length){
                return false;
            }

            boolean flag = true;

            for(int i=0;i<params_other.length;i++){
                if(!params_other[i].equals(params_this[i])){
                    flag = false;
                    break;
                }
            }

            return flag;
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
