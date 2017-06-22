package amdp.cleanupturtlebot.cleanupl0Continuous.state;


import burlap.mdp.core.oo.state.OOStateUtilities;
import burlap.mdp.core.oo.state.ObjectInstance;
import burlap.mdp.core.state.annotations.DeepCopyState;

import java.util.Arrays;
import java.util.List;

import static amdp.cleanupturtlebot.cleanupl0Continuous.CleanupContinuousGridL0Domain.*;


/**
 * Created by ngopalan on 8/24/16.
 */
@DeepCopyState
public class CleanupGridContinuousAgent implements ObjectInstance {

    public String name;
    public double x;
    public double y;
    public boolean directional = false;
    public String currentDirection;

    private final static List<Object> keys = Arrays.<Object>asList(VAR_X, VAR_Y, VAR_DIR);

    public CleanupGridContinuousAgent(String name, double x, double y){
        this.name = name;
        this.x= x;
        this.y= y;
    }

    public CleanupGridContinuousAgent(String name, double x, double y, String currentDirection){
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
    public CleanupGridContinuousAgent copyWithName(String objectName) {
        if(directional){
            new CleanupGridContinuousAgent(objectName,x,y, currentDirection);
        }
        return new CleanupGridContinuousAgent(objectName, x, y);
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
    public CleanupGridContinuousAgent copy() {
        if(directional){
            return new CleanupGridContinuousAgent(name,x,y, currentDirection);
        }
        return new CleanupGridContinuousAgent(name,x,y);
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
        CleanupGridContinuousAgent that = (CleanupGridContinuousAgent) o;
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
