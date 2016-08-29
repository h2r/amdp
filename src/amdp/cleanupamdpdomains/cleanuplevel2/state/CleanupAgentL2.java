package amdp.cleanupamdpdomains.cleanuplevel2.state;


import amdp.cleanup.CleanupDomain;
import burlap.mdp.core.oo.state.OOStateUtilities;
import burlap.mdp.core.oo.state.ObjectInstance;
import burlap.mdp.core.state.annotations.DeepCopyState;

import java.util.Arrays;
import java.util.List;

import static amdp.cleanupamdpdomains.cleanuplevel1.CleanupL1Domain.VAR_IN_REGION;


/**
 * Created by ngopalan on 8/24/16.
 */
@DeepCopyState
public class CleanupAgentL2 implements ObjectInstance {

    public String name;

    public String inRegion;

    private final static List<Object> keys = Arrays.<Object>asList(VAR_IN_REGION);

    public CleanupAgentL2(String name, String inRegion){
        this.name = name;
        this.inRegion = inRegion;
    }

    @Override
    public String className() {
        return CleanupDomain.CLASS_AGENT;
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public CleanupAgentL2 copyWithName(String objectName) {
        return new CleanupAgentL2(objectName, inRegion);
    }

    @Override
    public List<Object> variableKeys() {
        return keys;
    }

    @Override
    public Object get(Object variableKey) {

        if(!(variableKey instanceof String)){
            throw new RuntimeException("Cleanup Agent variable key must be a string");
        }

        String key = (String)variableKey;
        if(key.equals(VAR_IN_REGION)){
            return inRegion;
        }


        throw new RuntimeException("Unknown key for Cleanup Agent: " + key);
    }

    @Override
    public CleanupAgentL2 copy() {
        return new CleanupAgentL2(name,inRegion);
    }

    @Override
    public String toString() {
        return OOStateUtilities.objectInstanceToString(this);
    }

    @Override
    public boolean equals(Object o) {
        // self check
        if (this == o)
            return true;
        // null check
        if (o == null)
            return false;
        // type check and cast
        if (getClass() != o.getClass())
            return false;
        CleanupAgentL2 that = (CleanupAgentL2) o;
        // field comparison
        return (this.inRegion.equals(that.inRegion))
                && (this.name == that.name());
    }
}
