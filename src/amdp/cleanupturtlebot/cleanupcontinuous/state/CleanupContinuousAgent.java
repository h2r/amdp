package amdp.cleanupturtlebot.cleanupcontinuous.state;


import burlap.mdp.core.oo.state.OOStateUtilities;
import burlap.mdp.core.oo.state.ObjectInstance;
import burlap.mdp.core.state.annotations.DeepCopyState;

import java.util.Arrays;
import java.util.List;

import static amdp.cleanupturtlebot.cleanupl0discrete.CleanupTurtleBotL0Domain.*;


/**
 * Created by ngopalan on 8/24/16.
 */
@DeepCopyState
public class CleanupContinuousAgent implements ObjectInstance {

    public String name;
    public double x;
    public double y;
    // angle of direction measured from a fixed frame of north going clockwise, east is -pi/2 and west is pi/2.
    public double direction;
    public double length = 2;
    public double width = 1;
    public double rangeForEquality = 0.1;

    public final static List<Object> keys = Arrays.<Object>asList(VAR_X, VAR_Y, VAR_DIR, VAR_LENGTH, VAR_WIDTH);


    public CleanupContinuousAgent(String name, double x, double y, double startDirection, double length, double width){
        this.name = name;
        this.x= x;
        this.y= y;
        this.length=length;
        this.width= width;
        this.direction = startDirection;
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
    public CleanupContinuousAgent copyWithName(String objectName) {
        return new CleanupContinuousAgent(objectName,x,y, direction, length, width);
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
            return direction;
        }
        else if(key.equals(VAR_LENGTH)){
            return length;
        }
        else if(key.equals(VAR_WIDTH)){
            return width;
        }

        throw new RuntimeException("Unknown key for Cleanup Agent: " + key);
    }

    @Override
    public CleanupContinuousAgent copy() {
        return new CleanupContinuousAgent(name,x,y, direction, length, width);
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
        CleanupContinuousAgent that = (CleanupContinuousAgent) o;
        // field comparison
        // maybe the double checks should be within a range
        return (this.x==that.x)
                && (this.y==that.y) && (this.direction == that.direction)
                && (this.name == that.name()) && (that.width==this.width) && (this.length == that.length);

//        return (checkDouble(this.x, that.x))
//                && checkDouble(this.y,that.y) && checkDouble(this.direction, that.direction)
//                && (this.name == that.name()) && (that.width==this.width) && (this.length == that.length);
    }

    public boolean checkDouble(double d1, double d2){
        if(Math.abs(d1-d2)<rangeForEquality){
            return true;
        }
        return false;
    }



    @Override
    public String toString() {
        return OOStateUtilities.objectInstanceToString(this);
    }


}
