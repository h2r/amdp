package amdp.cleanupamdpdomains.cleanuplevel1.state;

/**
 * Created by ngopalan on 8/24/16.
 */
import burlap.mdp.core.oo.state.OOStateUtilities;
import burlap.mdp.core.oo.state.ObjectInstance;
import burlap.mdp.core.state.State;
import burlap.mdp.core.state.annotations.DeepCopyState;

import java.util.Arrays;
import java.util.List;

import amdp.cleanup.CleanupDomain;
import static amdp.cleanupamdpdomains.cleanuplevel1.CleanupL1Domain.*;

@DeepCopyState
public class CleanupDoorL1 implements ObjectInstance {

    public int locked;
    public boolean canBeLocked;
    public String name;
    public List<String> connectedRegions;


    private final static List<Object> keys = Arrays.<Object>asList(CleanupDomain.VAR_LOCKED, CleanupDomain.VAR_CAN_BE_LOCKED, VAR_CONNECTED);
    public CleanupDoorL1(String name, int locked, boolean canBeLocked, List<String> connectedRegions) {
        this.locked = locked;
        this.name = name;
        this.canBeLocked = canBeLocked;
        this.connectedRegions = connectedRegions;
    }

    @Override
    public String className() {
        return CleanupDomain.CLASS_DOOR;
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public CleanupDoorL1 copyWithName(String objectName) {
        return new CleanupDoorL1(objectName, locked, canBeLocked, connectedRegions);
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
         if(key.equals(CleanupDomain.VAR_LOCKED)){
            return locked;
        }
        else if(key.equals(CleanupDomain.VAR_CAN_BE_LOCKED)){
            return canBeLocked;
        }
        else if(key.equals(VAR_CONNECTED)){
             return connectedRegions;
         }

        throw new RuntimeException("Unknown key for Cleanup Door: " + key);
    }

    @Override
    public CleanupDoorL1 copy() {
        return new CleanupDoorL1(name, locked, canBeLocked, connectedRegions);
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
        CleanupDoorL1 that = (CleanupDoorL1) o;
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
        return (this.locked == (that.locked)) && (this.canBeLocked == (that.canBeLocked))
                && (this.name == that.name()) ;
    }

}
