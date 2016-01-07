package amdp.cleanupdomain;

import burlap.domain.singleagent.cleanup.CleanupWorld;
import burlap.oomdp.auxiliary.stateconditiontest.StateConditionTest;
import burlap.oomdp.core.TerminalFunction;
import burlap.oomdp.core.states.State;
import burlap.oomdp.singleagent.GroundedAction;
import burlap.oomdp.singleagent.common.GoalBasedRF;

/**
 * @author James MacGlashan.
 */
public class PullCostGoalRF extends GoalBasedRF{

	double pullCost = -0.0001;

	public PullCostGoalRF(StateConditionTest gc) {
		super(gc);
	}

	public PullCostGoalRF(StateConditionTest gc, double goalReward) {
		super(gc, goalReward);
	}

	public PullCostGoalRF(StateConditionTest gc, double goalReward, double defaultReward) {
		super(gc, goalReward, defaultReward);
	}

	public PullCostGoalRF(TerminalFunction tf) {
		super(tf);
	}

	public PullCostGoalRF(TerminalFunction tf, double goalReward) {
		super(tf, goalReward);
	}

	public PullCostGoalRF(TerminalFunction tf, double goalReward, double defaultReward) {
		super(tf, goalReward, defaultReward);
	}

	public double getPullCost() {
		return pullCost;
	}

	public void setPullCost(double pullCost) {
		this.pullCost = pullCost;
	}
	
	public StateConditionTest getStateCondition(){
		return this.gc;
	}

	@Override
	public double reward(State s, GroundedAction a, State sprime) {
		double superR = super.reward(s, a, sprime);
		double r = superR;
		if(a.actionName().equals(CleanupWorld.ACTION_PULL)){
			r += this.pullCost;
		}
		return r;
	}
}
