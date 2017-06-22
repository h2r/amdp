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
public class CleanupGridContinuousBlock implements ObjectInstance {


    public String name;
    public double x;
    public double y;
    public String shape;
    public String colour;


    private final static List<Object> keys = Arrays.<Object>asList(VAR_X, VAR_Y, VAR_SHAPE, VAR_COLOUR);

    public CleanupGridContinuousBlock(String name, double x, double y, String shape, String colour){
        this.name = name;
        this.x= x;
        this.y= y;
        this.shape = shape;
        this.colour = colour;
    }

    @Override
    public String className() {
        return CLASS_BLOCK;
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public CleanupGridContinuousBlock copyWithName(String objectName) {
        return new CleanupGridContinuousBlock(objectName, x, y, shape, colour);
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
        if(key.equals(VAR_X)){
            return x;
        }
        else if(key.equals(VAR_Y)){
            return y;
        }
        else if(key.equals(VAR_SHAPE)){
            return shape;
        }
        else if(key.equals(VAR_COLOUR)){
            return colour;
        }

        throw new RuntimeException("Unknown key for Cleanup Block: " + key);

    }

    @Override
    public CleanupGridContinuousBlock copy() {
        return new CleanupGridContinuousBlock(name, x, y, shape, colour);
    }

    @Override
    public String toString() {
        return OOStateUtilities.objectInstanceToString(this);
    }
}
