package amdp.taximodellearning;

import amdp.amdpframework.AMDPAgent;
import burlap.behavior.policy.Policy;
import burlap.behavior.singleagent.Episode;
import burlap.behavior.singleagent.learning.modellearning.KWIKModel;
import burlap.behavior.singleagent.learning.modellearning.rmax.PotentialShapedRMax;
import burlap.mdp.core.action.Action;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.SADomain;
import burlap.mdp.singleagent.environment.Environment;
import burlap.mdp.singleagent.environment.EnvironmentOutcome;
import burlap.statehashing.HashableStateFactory;

/**
 * Created by ngopalan on 10/10/16.
 */
public class AMDPTaskNodeModelLearningAgent extends PotentialShapedRMax {

private AMDPAgent amdpAgent;
    public AMDPTaskNodeModelLearningAgent(SADomain domain, double gamma,
                                          HashableStateFactory hashingFactory, double maxReward,
                                          int nConfident, double maxVIDelta, int maxVIPasses,
                                          AMDPAgent amdpAgent) {
        super(domain, gamma, hashingFactory, maxReward, nConfident, maxVIDelta, maxVIPasses);
        this.amdpAgent = amdpAgent;
    }


@Override
public Episode runLearningEpisode(Environment env) {
    return this.runLearningEpisode(env, -1);
}


    @Override
    public Episode runLearningEpisode(Environment env, int maxSteps) {

        //TODO: fix this to make an agent use update the models locally

        State initialState = env.currentObservation();

        this.modelPlanner.initializePlannerIn(initialState);

        Episode ea = new Episode(initialState);

        Policy policy = this.createUnmodeledFavoredPolicy();

        State curState = initialState;
        int steps = 0;
        while(!env.isInTerminalState() && (steps < maxSteps || maxSteps == -1)){

            Action ga = policy.action(curState);
            EnvironmentOutcome eo = env.executeAction(ga);
            ea.transition(ga, eo.op, eo.r);

            boolean modeledTerminal = this.model.terminal(eo.op);

            if(!this.model.transitionIsModeled(curState, ga)
                    || (!KWIKModel.Helper.stateTransitionsModeled(model, this.getActionTypes(), eo.op) && !modeledTerminal)){
                this.model.updateModel(eo);
                if(this.model.transitionIsModeled(curState, ga) || (eo.terminated != modeledTerminal && modeledTerminal != this.model.terminal(eo.op))){
                    this.modelPlanner.modelChanged(curState);
                    policy = this.createUnmodeledFavoredPolicy();
                }
            }


            curState = env.currentObservation();

            steps++;
        }

        if(episodeHistory.size() >= numEpisodesToStore){
            episodeHistory.poll();
        }
        episodeHistory.offer(ea);


        return ea;

    }



}
