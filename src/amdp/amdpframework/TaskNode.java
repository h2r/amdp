package amdp.amdpframework;


import burlap.mdp.core.action.Action;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.model.RewardFunction;
import burlap.mdp.singleagent.oo.OOSADomain;

import java.util.List;

/**
 * This are MAX nodes inspired from the Dietterich paper, adapted into the AMDP format
 * Created by ngopalan on 5/5/16.
 */
public interface TaskNode {
    String getName();
    boolean isTaskPrimitive();
    boolean terminal(State s, Action action);

    // here each grounded task comes with a fake action that we create
    List<GroundedTask> getApplicableGroundedTasks(State s);


    RewardFunction rewardFunction(Action action);

}
