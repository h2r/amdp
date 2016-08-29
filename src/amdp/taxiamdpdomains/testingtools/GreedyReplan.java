package amdp.taxiamdpdomains.testingtools;

import burlap.behavior.policy.GreedyQPolicy;
import burlap.behavior.singleagent.planning.Planner;
import burlap.behavior.valuefunction.QProvider;
import burlap.mdp.core.action.Action;
import burlap.mdp.core.state.State;

/**
 * Created by ngopalan on 8/22/16.
 */
public class GreedyReplan extends GreedyQPolicy {

    public GreedyReplan(QProvider planner) {
        super(planner);
    }

    @Override
    public Action action(State s) {
        Action ga = super.action(s);
        int count = 0;
        while(this.qplanner.qValue(s, ga) == 0. && count<100){
            ((Planner)this.qplanner).planFromState(s);
            ga =  super.action(s);
            count++;
        }
        return ga;
    }
}