package amdp.cleanupamdpdomains.cleanuplevel1.state;

/**
 * Created by ngopalan on 8/24/16.
 */
import amdp.cleanup.CleanupDomain;
import burlap.mdp.core.oo.state.OOStateUtilities;
import burlap.mdp.core.oo.state.ObjectInstance;
import burlap.mdp.core.state.State;
import burlap.mdp.core.state.annotations.DeepCopyState;

import java.util.Arrays;
import java.util.List;


import static amdp.cleanupamdpdomains.cleanuplevel1.CleanupL1Domain.*;

@DeepCopyState
public class CleanupRoomL1 implements ObjectInstance {
    public String name;
    public String colour;
    public List<String> connectedRegions;


    private final static List<Object> keys = Arrays.<Object>asList(VAR_CONNECTED, CleanupDomain.VAR_COLOUR);

    public CleanupRoomL1(String name, String colour, List<String> connectedRegions) {
        this.name = name;
        this.colour = colour;
        this.connectedRegions = connectedRegions;
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
    public CleanupRoomL1 copyWithName(String objectName) {
        return new CleanupRoomL1(objectName, colour, connectedRegions);
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
        if(key.equals(VAR_CONNECTED)){
            return connectedRegions;
        }
        else if(key.equals(CleanupDomain.VAR_COLOUR)){
            return colour;
        }

        throw new RuntimeException("Unknown key for Cleanup Door: " + key);
    }

    @Override
    public CleanupRoomL1 copy() {
        return new CleanupRoomL1(name, colour, connectedRegions);
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
        CleanupRoomL1 that = (CleanupRoomL1) o;
        // field comparison
        if(this.connectedRegions.size()!=that.connectedRegions.size()){
            return false;
        }

        // check same connected
        for(int i=0;i<connectedRegions.size();i++){
            if(!this.connectedRegions.get(i).equals(that.connectedRegions.get(i))){
                return false;
            }
        }
        return (this.colour.equals(that.colour)) && (this.name == that.name()) ;
    }
}
