package amdp.cleanup.state;


import burlap.mdp.core.oo.state.OOStateUtilities;
import burlap.mdp.core.oo.state.ObjectInstance;
import burlap.mdp.core.state.State;
import burlap.mdp.core.state.annotations.DeepCopyState;

import java.util.Arrays;
import java.util.List;

import static amdp.cleanup.CleanupDomain.*;


/**
 * Created by ngopalan on 8/24/16.
 */
@DeepCopyState
public class CleanupAgent implements ObjectInstance {

    public String name;
    public int x;
    public int y;
    public boolean directional = false;
    public String currentDirection;

    private final static List<Object> keys = Arrays.<Object>asList(VAR_X, VAR_Y, VAR_DIR);

    public CleanupAgent(String name, int x, int y){
        this.name = name;
        this.x= x;
        this.y= y;
    }

    public CleanupAgent(String name, int x, int y, String currentDirection){
        this.name = name;
        this.x= x;
        this.y= y;
        this.directional = true;
        this.currentDirection = currentDirection;
    }

    @Override
    public String className() {
        return CLASS_AGENT;
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public CleanupAgent copyWithName(String objectName) {
        if(directional){
            new CleanupAgent(objectName,x,y, currentDirection);
        }
        return new CleanupAgent(objectName, x, y);
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
        if(key.equals(VAR_X)){
            return x;
        }
        else if(key.equals(VAR_Y)){
            return y;
        }
        else if(key.equals(VAR_DIR)){
            return currentDirection;
        }

        throw new RuntimeException("Unknown key for Cleanup Agent: " + key);
    }

    @Override
    public CleanupAgent copy() {
        if(directional){
            return new CleanupAgent(name,x,y, currentDirection);
        }
        return new CleanupAgent(name,x,y);
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
        CleanupAgent that = (CleanupAgent) o;
        // field comparison
        return (this.directional==that.directional) && (this.x==that.x)
                && (this.y==that.y) && (this.currentDirection.equals(that.currentDirection))
                && (this.name == that.name());
    }


    @Override
    public String toString() {
        return OOStateUtilities.objectInstanceToString(this);
    }
}
