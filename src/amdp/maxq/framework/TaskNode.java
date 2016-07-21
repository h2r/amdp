package amdp.maxq.framework;



import burlap.mdp.core.action.Action;
import burlap.mdp.core.state.State;

import java.util.List;

/**
 * These are the V nodes from the Dietterich MaxQ paper
 * Created by ngopalan on 5/5/16.
 */
public interface TaskNode {
    String getName();
    boolean isTaskPrimitive();
    boolean terminal(State s, Action action);

    // here each grounded task comes with a fake action that we create
    List<GroundedTask> getApplicableGroundedTasks(State s);


}
