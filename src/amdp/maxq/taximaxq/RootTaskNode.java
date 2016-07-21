package amdp.maxq.taximaxq;


import amdp.maxq.framework.GroundedTask;
import amdp.maxq.framework.NonPrimitiveTaskNode;
import amdp.maxq.framework.TaskNode;
import burlap.behavior.valuefunction.QValue;
import burlap.mdp.core.TerminalFunction;
import burlap.mdp.core.action.Action;
import burlap.mdp.core.action.SimpleAction;
import burlap.mdp.core.state.State;

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

}
