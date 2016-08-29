package amdp.cleanupamdpdomains.cleanuplevel2;

import amdp.cleanupamdpdomains.cleanuplevel1.CleanupL1Domain;
import amdp.cleanupamdpdomains.cleanuplevel2.state.CleanupAgentL2;
import amdp.cleanupamdpdomains.cleanuplevel2.state.CleanupBlockL2;
import amdp.cleanupamdpdomains.cleanuplevel2.state.CleanupL2State;
import burlap.mdp.core.StateTransitionProb;
import burlap.mdp.core.action.Action;
import burlap.mdp.core.oo.ObjectParameterizedAction;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.model.statemodel.FullStateModel;

import java.util.List;

/**
 * Created by ngopalan on 8/28/16.
 */
public class CleanupL2Model implements FullStateModel{
    @Override
    public List<StateTransitionProb> stateTransitions(State s, Action a) {
        return FullStateModel.Helper.deterministicTransition(this,s,a);
    }

    @Override
    public State sample(State s, Action a) {
        CleanupL2State ns = ((CleanupL2State)s).copy();
        if(a.actionName().equals(CleanupL1Domain.ACTION_AGENT_TO_ROOM)){
            return agentToRoom(ns, (ObjectParameterizedAction) a);
        }
        return blockToRoom(ns,(ObjectParameterizedAction) a);
    }

    private State agentToRoom(CleanupL2State ns, ObjectParameterizedAction a) {
        CleanupAgentL2 agent = ns.touchAgent();
        agent.inRegion = a.getObjectParameters()[0];
        return ns;
    }

    public State blockToRoom(CleanupL2State s, ObjectParameterizedAction a){
        CleanupAgentL2 agent = s.touchAgent();
        CleanupBlockL2 block = s.touchBlock(s.blockInd(a.getObjectParameters()[0]));
        agent.inRegion = a.getObjectParameters()[1];
        block.inRegion = a.getObjectParameters()[1];
        return s;
    }
}
