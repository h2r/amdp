package amdp.cleanup;

import amdp.cleanup.state.CleanupDoor;
import amdp.cleanup.state.CleanupState;
import burlap.mdp.core.TerminalFunction;
import burlap.mdp.core.action.Action;
import burlap.mdp.core.oo.state.ObjectInstance;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.environment.EnvironmentOutcome;
import burlap.mdp.singleagent.environment.SimulatedEnvironment;
import burlap.mdp.singleagent.environment.extensions.EnvironmentObserver;
import burlap.mdp.singleagent.model.RewardFunction;
import burlap.mdp.singleagent.oo.OOSADomain;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author James MacGlashan.
 */
public class FixedDoorCleanupEnv extends SimulatedEnvironment{

    protected Set<String> lockedDoors = new HashSet<String>();

    public FixedDoorCleanupEnv(OOSADomain domain, State initialState) {
        super(domain, initialState);
    }

    public void addLockedDoor(String doorName){
        this.lockedDoors.add(doorName);
    }


    @Override
    public EnvironmentOutcome executeAction(Action a) {

        Action simGA = a.copy();
//        simGA.action = this.domain.getAction(ga.actionName());
//        if(simGA.action == null){
//            throw new RuntimeException("Cannot execute action " + ga.toString() + " in this SimulatedEnvironment because the action is to known in this Environment's domain");
//        }
        for(EnvironmentObserver observer : this.observers){
            observer.observeEnvironmentActionInitiation(this.currentObservation(), a);
        }

        EnvironmentOutcome eo;
//        State nextState;
        if(this.allowActionFromTerminalStates || !this.isInTerminalState()) {
            do{
                eo= model.sample(this.curState, a);
//                nextState = simGA.executeIn(this.curState);
            }while(!this.validDoors(eo.op));
//            this.lastReward = this.rf.reward(this.curState, simGA, nextState);
        }
        else{
            eo = new EnvironmentOutcome(this.curState, a, this.curState.copy(), 0., true);
        }

        this.lastReward = eo.r;
        this.terminated = eo.terminated;
        this.curState = eo.op;

        for(EnvironmentObserver observer : this.observers){
            observer.observeEnvironmentInteraction(eo);
        }

        return eo;
    }

    protected boolean validDoors(State s){
        List<CleanupDoor> doors = ((CleanupState)s).doors;
        for(CleanupDoor d : doors){
            int lVal = d.locked;
            if(lVal == 1 && this.lockedDoors.contains(d.name())){
                return false;
            }
            else if(lVal == 2 && !this.lockedDoors.contains(d.name())){
                return false;
            }
        }
        return true;
    }

}
