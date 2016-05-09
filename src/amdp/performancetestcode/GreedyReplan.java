package amdp.performancetestcode;

import amdp.performancetestcode.BoundedRTDPForTests;
import burlap.behavior.policy.GreedyQPolicy;
import burlap.behavior.policy.Policy;
import burlap.behavior.singleagent.planning.Planner;
import burlap.behavior.valuefunction.QFunction;
import burlap.behavior.valuefunction.QValue;
import burlap.oomdp.core.AbstractGroundedAction;
import burlap.oomdp.core.states.MutableState;
import burlap.oomdp.core.states.State;
import burlap.oomdp.singleagent.GroundedAction;

import java.util.List;

/**
 * Created by James MacGlashan and ngopalan on 4/20/16.
 */

public class GreedyReplan extends GreedyQPolicy {

    MutableGlobalInteger mgi = new MutableGlobalInteger();
    boolean setFlag = false;
    public GreedyReplan(QFunction planner) {
        super(planner);
    }

    public GreedyReplan(QFunction planner, MutableGlobalInteger mgiIn) {

        super(planner);
        mgi = mgiIn;
        setFlag = true;
    }

    @Override
    public AbstractGroundedAction getAction(State s) {
        GroundedAction ga = (GroundedAction) super.getAction(s);
        int count = 0;
        while(this.qplanner.getQ(s, ga).q == 0. && count<100 && (!setFlag || (mgi.getValue()>0))){
            ((Planner)this.qplanner).planFromState(s);
            ga = (GroundedAction) super.getAction(s);
            count++;
        }
        return ga;
    }

    @Override
    public List<ActionProb> getActionDistributionForState(State s) {


        int count =0;
        while(count<100 && (!setFlag || (mgi.getValue()>0))){
            List<QValue> qValues = this.qplanner.getQs(s);
            for(QValue q:qValues) {
                if (q.q != 0) {
                    return super.getActionDistributionForState(s);
                }

                ((Planner) this.qplanner).planFromState(s);

            }

            count++;

        }
        return super.getActionDistributionForState(s);
    }
}
