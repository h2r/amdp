package amdp.utilities;

import burlap.behavior.policy.Policy;
import burlap.behavior.singleagent.learning.tdmethods.QLearning;
import burlap.behavior.valuefunction.QFunction;
import burlap.mdp.singleagent.SADomain;
import burlap.statehashing.HashableState;
import burlap.statehashing.HashableStateFactory;

/**
 * Created by ngopalan on 7/18/16.
 */
public class QLearningForMaxQ extends QLearning{


    public QLearningForMaxQ(SADomain domain, double gamma, HashableStateFactory hashingFactory, double qInit, double learningRate) {
        super(domain, gamma, hashingFactory, qInit, learningRate);
    }

    public QLearningForMaxQ(SADomain domain, double gamma, HashableStateFactory hashingFactory, double qInit, double learningRate, int maxEpisodeSize) {
        super(domain, gamma, hashingFactory, qInit, learningRate, maxEpisodeSize);
    }

    public QLearningForMaxQ(SADomain domain, double gamma, HashableStateFactory hashingFactory, double qInit, double learningRate, Policy learningPolicy, int maxEpisodeSize) {
        super(domain, gamma, hashingFactory, qInit, learningRate, learningPolicy, maxEpisodeSize);
    }

    public QLearningForMaxQ(SADomain domain, double gamma, HashableStateFactory hashingFactory, QFunction qInit, double learningRate, Policy learningPolicy, int maxEpisodeSize) {
        super(domain, gamma, hashingFactory, qInit, learningRate, learningPolicy, maxEpisodeSize);
    }

    public int numberOfParams(){
        int numParams = 0;
        for(HashableState hs:this.qFunction.keySet()){
            numParams+=qFunction.get(hs).qEntry.size();
        }
        return numParams;
    }
}
