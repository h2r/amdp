package amdp.maxq.framework;


import burlap.statehashing.HashableState;

import java.util.HashMap;
import java.util.Map;

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
//        if (!this.internalMap.get(parentGroundedTask).get(currentGroundedSubTask).containsKey(hs)) {
//            this.internalMap.get(parentGroundedTask).get(currentGroundedSubTask).put(hs, value);
//        }
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
                numParams+= internalMap.get(pgt).get(cgt).size();
            }
        }
        return numParams;
    }


}