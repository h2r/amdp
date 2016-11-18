package burlap.ros.posestamped;

import burlap.mdp.core.oo.state.OOStateUtilities;
import burlap.mdp.core.oo.state.ObjectInstance;
import burlap.mdp.core.state.State;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by ngopalan on 11/5/16.
 */
public class Position implements ObjectInstance{

    public String name;
    public double x;
    public double y;
    public double z;

    public final static String VAR_X = "x";
    public final static String VAR_Y = "y";
    public final static String VAR_Z = "z";

    public final static List<Object> keys = Arrays.<Object>asList(VAR_X, VAR_Y, VAR_Z);


    public Position(){};

    public Position(String name, double x, double y, double z){
        this.name = name;
        this.x = x;
        this.y = y;
        this.z = z;
    }

    @Override
    public String className() {
        return "position";
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public ObjectInstance copyWithName(String s) {
        return new Position(s,x,y,z);
    }

    @Override
    public List<Object> variableKeys() {
        return keys;
    }

    @Override
    public Object get(Object variableKey) {
        if(!(variableKey instanceof String)){
            throw new RuntimeException("For position variable key must be a string");
        }

        String key = (String)variableKey;
        if(key.equals(VAR_X)){
            return x;
        }
        else if(key.equals(VAR_Y)){
            return y;
        }
        else if(key.equals(VAR_Z)){
            return z;
        }

        throw new RuntimeException("Unknown key for Cleanup Agent: " + key);

    }

    @Override
    public State copy() {
        return new Position(name,x,y,z);
    }

    @Override
    public String toString() {
        return OOStateUtilities.objectInstanceToString(this);
    }
}
