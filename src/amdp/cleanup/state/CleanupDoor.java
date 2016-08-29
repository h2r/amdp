package amdp.cleanup.state;

/**
 * Created by ngopalan on 8/24/16.
 */
import burlap.mdp.core.oo.state.OOStateUtilities;
import burlap.mdp.core.oo.state.ObjectInstance;
import burlap.mdp.core.state.State;
import burlap.mdp.core.state.annotations.DeepCopyState;

import java.util.Arrays;
import java.util.List;

import static amdp.cleanup.CleanupDomain.*;

@DeepCopyState
public class CleanupDoor implements ObjectInstance {

    public int locked;
    public boolean canBeLocked;
    public String name;
    public int top;
    public int left;
    public int bottom;
    public int right;


    private final static List<Object> keys = Arrays.<Object>asList(VAR_LOCKED, VAR_TOP, VAR_LEFT, VAR_BOTTOM, VAR_RIGHT, VAR_CAN_BE_LOCKED);
    public CleanupDoor(String name, int locked, int top, int left, int bottom, int right, boolean canBeLocked) {
        this.locked = locked;
        this.name = name;
        this.top = top;
        this.left = left;
        this.bottom = bottom;
        this.right = right;
        this.canBeLocked = canBeLocked;
    }

    @Override
    public String className() {
        return CLASS_DOOR;
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public CleanupDoor copyWithName(String objectName) {
        return new CleanupDoor(objectName, locked, top, left, bottom, right, canBeLocked);
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
        if(key.equals(VAR_TOP)){
            return top;
        }
        else if(key.equals(VAR_RIGHT)){
            return right;
        }
        else if(key.equals(VAR_BOTTOM)){
            return bottom;
        }
        else if(key.equals(VAR_LEFT)){
            return left;
        }
        else if(key.equals(VAR_LOCKED)){
            return locked;
        }
        else if(key.equals(VAR_CAN_BE_LOCKED)){
            return canBeLocked;
        }

        throw new RuntimeException("Unknown key for Cleanup Door: " + key);
    }

    @Override
    public CleanupDoor copy() {
        return new CleanupDoor(name, locked, top, left, bottom, right, canBeLocked);
    }

    @Override
    public String toString() {
        return OOStateUtilities.objectInstanceToString(this);
    }
}
