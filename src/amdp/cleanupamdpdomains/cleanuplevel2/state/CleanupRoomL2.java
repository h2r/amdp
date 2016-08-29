package amdp.cleanupamdpdomains.cleanuplevel2.state;

/**
 * Created by ngopalan on 8/24/16.
 */

import amdp.cleanup.CleanupDomain;
import burlap.mdp.core.oo.state.OOStateUtilities;
import burlap.mdp.core.oo.state.ObjectInstance;
import burlap.mdp.core.state.annotations.DeepCopyState;

import java.util.Arrays;
import java.util.List;

import static amdp.cleanupamdpdomains.cleanuplevel1.CleanupL1Domain.VAR_CONNECTED;

@DeepCopyState
public class CleanupRoomL2 implements ObjectInstance {
    public String name;
    public String colour;


    private final static List<Object> keys = Arrays.<Object>asList(CleanupDomain.VAR_COLOUR);

    public CleanupRoomL2(String name, String colour) {
        this.name = name;
        this.colour = colour;
    }

    @Override
    public String className() {
        return CleanupDomain.CLASS_ROOM;
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public CleanupRoomL2 copyWithName(String objectName) {
        return new CleanupRoomL2(objectName, colour);
    }

    @Override
    public List<Object> variableKeys() {
        return keys;
    }

    @Override
    public Object get(Object variableKey) {
        if(!(variableKey instanceof String)){
            throw new RuntimeException("Cleanup Door variable key must be a string");
        }

        String key = (String)variableKey;
        if(key.equals(CleanupDomain.VAR_COLOUR)){
            return colour;
        }

        throw new RuntimeException("Unknown key for Cleanup Door: " + key);
    }

    @Override
    public CleanupRoomL2 copy() {
        return new CleanupRoomL2(name, colour);
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
        CleanupRoomL2 that = (CleanupRoomL2) o;
        // field comparison

        return (this.colour.equals(that.colour)) && (this.name == that.name()) ;
    }
}
