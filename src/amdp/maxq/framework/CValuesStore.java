package amdp.maxq.framework;


import amdp.utilities.BoltzmannQPolicyWithCoolingSchedule;
import burlap.behavior.policy.SolverDerivedPolicy;
import burlap.behavior.singleagent.Episode;
import burlap.behavior.singleagent.learning.LearningAgent;
import burlap.behavior.valuefunction.QValue;
import burlap.debugtools.RandomFactory;
import burlap.mdp.core.action.Action;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.environment.Environment;
import burlap.mdp.singleagent.environment.EnvironmentOutcome;
import burlap.statehashing.HashableState;
import burlap.statehashing.HashableStateFactory;
import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.util.*;

/**
 * this class is just a wrapper around MAP to store the C values so get and put operations have a clean implementation
 */
public class CValuesStore{
    protected Map<GroundedTask ,HashMap<GroundedTask,HashMap<HashableState, Double>>> internalMap =
            new HashMap<GroundedTask ,HashMap<GroundedTask,HashMap<HashableState, Double>>>();
    protected double defaultValue = 0.;

    public CValuesStore(){};

    public CValuesStore(double defaultValue){
        this.defaultValue=defaultValue;
    }

    public void put(GroundedTask parentGroundedTask, GroundedTask currentGroundedSubTask, HashableState hs, double value){
        if (!this.internalMap.containsKey(parentGroundedTask)) {
            this.internalMap.put(parentGroundedTask, new HashMap<GroundedTask, HashMap<HashableState, Double>>());
        }
        if (!this.internalMap.get(parentGroundedTask).containsKey(currentGroundedSubTask)) {
            this.internalMap.get(parentGroundedTask).put(currentGroundedSubTask, new HashMap<HashableState, Double>());
        }
        this.internalMap.get(parentGroundedTask).get(currentGroundedSubTask).put(hs, value);
    }

    public double get(GroundedTask parentGroundedTask, GroundedTask currentGroundedSubTask, HashableState hs){
        if (!this.internalMap.containsKey(parentGroundedTask)) {
            this.internalMap.put(parentGroundedTask, new HashMap<GroundedTask, HashMap<HashableState, Double>>());
        }
        if (!this.internalMap.get(parentGroundedTask).containsKey(currentGroundedSubTask)) {
            this.internalMap.get(parentGroundedTask).put(currentGroundedSubTask, new HashMap<HashableState, Double>());
        }
        if (!this.internalMap.get(parentGroundedTask).get(currentGroundedSubTask).containsKey(hs)) {
            this.internalMap.get(parentGroundedTask).get(currentGroundedSubTask).put(hs, defaultValue);
            return defaultValue;
        }
        return this.internalMap.get(parentGroundedTask).get(currentGroundedSubTask).get(hs);

    }

    public int numberOfParams(){
        int numParams =0;
        for(GroundedTask pgt : internalMap.keySet()){
            for(GroundedTask cgt : internalMap.get(pgt).keySet()){
                int temp = internalMap.get(pgt).get(cgt).size();
                numParams+= temp;
//                System.out.println("Parent task: " + pgt.action.actionName()
//                        + "; Child task: " + cgt.action.actionName() + "; num States: " +temp);
            }
        }
        return numParams;
    }



}