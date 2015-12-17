package amdp.hardcoded.cleanup;

import burlap.domain.singleagent.cleanup.CleanupWorld;
import burlap.oomdp.core.Domain;
import burlap.oomdp.core.TerminalFunction;
import burlap.oomdp.core.objects.ObjectInstance;
import burlap.oomdp.core.states.State;
import burlap.oomdp.singleagent.GroundedAction;
import burlap.oomdp.singleagent.RewardFunction;
import burlap.oomdp.singleagent.environment.EnvironmentOutcome;
import burlap.oomdp.singleagent.environment.SimulatedEnvironment;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author James MacGlashan.
 */
public class FixedDoorCleanupEnv extends SimulatedEnvironment {

	protected Set<String> lockedDoors = new HashSet<String>();

	public FixedDoorCleanupEnv(Domain domain, RewardFunction rf, TerminalFunction tf, State initialState) {
		super(domain, rf, tf, initialState);
	}

	public void addLockedDoor(String doorName){
		this.lockedDoors.add(doorName);
	}


	@Override
	public EnvironmentOutcome executeAction(GroundedAction ga) {

		GroundedAction simGA = (GroundedAction)ga.copy();
		simGA.action = this.domain.getAction(ga.actionName());
		if(simGA.action == null){
			throw new RuntimeException("Cannot execute action " + ga.toString() + " in this SimulatedEnvironment because the action is to known in this Environment's domain");
		}
		State nextState;
		if(this.allowActionFromTerminalStates || !this.isInTerminalState()) {
			do{
				nextState = simGA.executeIn(this.curState);
			}while(!this.validDoors(nextState));
			this.lastReward = this.rf.reward(this.curState, simGA, nextState);
		}
		else{
			nextState = this.curState;
			this.lastReward = 0.;
		}

		EnvironmentOutcome eo = new EnvironmentOutcome(this.curState.copy(), simGA, nextState.copy(), this.lastReward, this.tf.isTerminal(nextState));

		this.curState = nextState;

		return eo;
	}

	protected boolean validDoors(State s){
		List<ObjectInstance> doors = s.getObjectsOfClass(CleanupWorld.CLASS_DOOR);
		for(ObjectInstance d : doors){
			int lVal = d.getIntValForAttribute(CleanupWorld.ATT_LOCKED);
			if(lVal == 1 && this.lockedDoors.contains(d.getName())){
				return false;
			}
			else if(lVal == 2 && !this.lockedDoors.contains(d.getName())){
				return false;
			}
		}
		return true;
	}

}
