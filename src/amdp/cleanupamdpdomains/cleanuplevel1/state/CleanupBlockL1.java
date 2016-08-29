package amdp.cleanupamdpdomains.cleanuplevel1.state;

import burlap.mdp.core.oo.state.OOStateUtilities;
import burlap.mdp.core.oo.state.ObjectInstance;
import burlap.mdp.core.state.State;
import burlap.mdp.core.state.annotations.DeepCopyState;

import java.util.Arrays;
import java.util.List;
import amdp.cleanup.CleanupDomain;
import static amdp.cleanupamdpdomains.cleanuplevel1.CleanupL1Domain.*;

/**
 * Created by ngopalan on 8/24/16.
 */
@DeepCopyState
public class CleanupBlockL1 implements ObjectInstance {


    public String name;
    public String shape;
    public String colour;
    public String inRegion;


    private final static List<Object> keys = Arrays.<Object>asList(CleanupDomain.VAR_SHAPE, CleanupDomain.VAR_COLOUR, VAR_IN_REGION);

    public CleanupBlockL1(String name,  String shape, String colour, String inRegion){
        this.name = name;
        this.shape = shape;
        this.colour = colour;
        this.inRegion = inRegion;
    }

    @Override
    public String className() {
        return CleanupDomain.CLASS_BLOCK;
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public CleanupBlockL1 copyWithName(String objectName) {
        return new CleanupBlockL1(objectName,  shape, colour, inRegion);
    }

    @Override
    public List<Object> variableKeys() {
        return keys;
    }

    @Override
    public Object get(Object variableKey) {

        if(!(variableKey instanceof String)){
            throw new RuntimeException("Cleanup Block variable key must be a string");
        }

        String key = (String)variableKey;

        if(key.equals(CleanupDomain.VAR_SHAPE)){
            return shape;
        }
        else if(key.equals(CleanupDomain.VAR_COLOUR)){
            return colour;
        }
        else if(key.equals(VAR_IN_REGION)){
            return inRegion;
        }

        throw new RuntimeException("Unknown key for Cleanup Block L1: " + key);

    }

    @Override
    public CleanupBlockL1 copy() {
        return new CleanupBlockL1(name, shape, colour, inRegion);
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
        CleanupBlockL1 that = (CleanupBlockL1) o;
        // field comparison
        return (this.inRegion.equals(that.inRegion)) && (this.shape.equals(that.shape))
                && (this.name == that.name()) && (this.colour.equals(that.colour));
    }
}

