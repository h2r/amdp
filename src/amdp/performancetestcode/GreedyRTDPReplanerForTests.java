package amdp.performancetestcode;

import burlap.behavior.policy.GreedyQPolicy;
import burlap.behavior.singleagent.planning.Planner;
import burlap.behavior.valuefunction.QFunction;
import burlap.oomdp.core.AbstractGroundedAction;
import burlap.oomdp.core.states.State;
import burlap.oomdp.singleagent.GroundedAction;

/**
 * This is a BRTDP specific greedy replanner that is for testing. The planner's makes 
 * sure that it doesn't surpass the total number of backups allowed.
 * @author ngopalan
 *
 */
public class GreedyRTDPReplanerForTests extends GreedyQPolicy{

	MutableGlobalInteger bellmanValue;
	
	
	public GreedyRTDPReplanerForTests(BoundedRTDPForTests planner) {
		super(planner);
		this.bellmanValue = bellmanValue;
	}

	@Override
	public AbstractGroundedAction getAction(State s) {
		GroundedAction ga = (GroundedAction) super.getAction(s);
		int count = 0;
//		int bellmanUpdatesBeforePlanning = ((BoundedRTDPForTests)this.qplanner).getNumberOfBellmanUpdates();
		while(this.qplanner.getQ(s, ga).q == 0. && count<100){
			((Planner)this.qplanner).planFromState(s);
			ga = (GroundedAction) super.getAction(s);
			count++;
		}
		return ga;
	}
}
