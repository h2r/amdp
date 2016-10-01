package amdp.cleanupamdpdomains.cleanupamdp;

import amdp.amdpframework.GroundedTask;
import amdp.amdpframework.NonPrimitiveTaskNode;
import amdp.amdpframework.TaskNode;
import burlap.mdp.core.TerminalFunction;
import burlap.mdp.core.action.Action;
import burlap.mdp.core.action.SimpleAction;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.model.RewardFunction;
import burlap.mdp.singleagent.oo.OOSADomain;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ngopalan on 8/14/16.
 */
public class RootTaskNode extends NonPrimitiveTaskNode {

    List<String[]> params = new ArrayList<String[]>();
    List<GroundedTask> groundedTasks = new ArrayList<GroundedTask>();
    TerminalFunction tf;
    RewardFunction rf;

    public RootTaskNode(String name, TaskNode[] children, OOSADomain domainIn, TerminalFunction tfIn, RewardFunction rfIn) {
        this.name = name;
        this.params.add(new String[]{"1"});
        this.childTaskNodes = children;
        for(String[] param:params){
            groundedTasks.add(new GroundedTask(this, new SimpleAction(name+":"+param)));
        }
        this.tf = tfIn;
        this.rf = rfIn;
        this.oosaDomain = domainIn;
    }


    @Override
    public Object parametersSet(State s) {
        return params;
    }

    @Override
    public boolean terminal(State s, Action action) {
        return this.tf.isTerminal(s);
    }

    @Override
    public List<GroundedTask> getApplicableGroundedTasks(State s) {
        return groundedTasks;
    }

    @Override
    public RewardFunction rewardFunction(Action action) {
        return rf;
    }
}