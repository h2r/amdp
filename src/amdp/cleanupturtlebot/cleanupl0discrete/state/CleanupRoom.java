//package amdp.cleanupturtlebot.cleanupl0discrete.state;
//
///**
// * Created by ngopalan on 8/24/16.
// */
//import burlap.mdp.core.oo.state.OOStateUtilities;
//import burlap.mdp.core.oo.state.ObjectInstance;
//import burlap.mdp.core.state.annotations.DeepCopyState;
//
//import java.util.Arrays;
//import java.util.List;
//
//import static amdp.cleanup.CleanupDomain.*;
//
//@DeepCopyState
//public class CleanupRoom implements ObjectInstance {
//    public String name;
//    public int top;
//    public int left;
//    public int bottom;
//    public int right;
//    public String colour;
//
//
//    private final static List<Object> keys = Arrays.<Object>asList(VAR_TOP, VAR_LEFT, VAR_BOTTOM, VAR_RIGHT, VAR_COLOUR);
//    public CleanupRoom(String name, int top, int left, int bottom, int right, String colour) {
//        this.name = name;
//        this.top = top;
//        this.left = left;
//        this.bottom = bottom;
//        this.right = right;
//        this.colour = colour;
//    }
//
//    @Override
//    public String className() {
//        return CLASS_ROOM;
//    }
//
//    @Override
//    public String name() {
//        return name;
//    }
//
//    @Override
//    public CleanupRoom copyWithName(String objectName) {
//        return new CleanupRoom(objectName, top, left, bottom, right, colour);
//    }
//
//    @Override
//    public List<Object> variableKeys() {
//        return keys;
//    }
//
//    @Override
//    public Object get(Object variableKey) {
//        if(!(variableKey instanceof String)){
//            throw new RuntimeException("Cleanup Room variable key must be a string");
//        }
//
//        String key = (String)variableKey;
//        if(key.equals(VAR_TOP)){
//            return top;
//        }
//        else if(key.equals(VAR_LEFT)){
//            return left;
//        }
//        else if(key.equals(VAR_BOTTOM)){
//            return bottom;
//        }
//        else if(key.equals(VAR_RIGHT)){
//            return right;
//        }
//        else if(key.equals(VAR_COLOUR)){
//            return colour;
//        }
//
//        throw new RuntimeException("Unknown key for Cleanup Room: " + key);
//    }
//
//    @Override
//    public CleanupRoom copy() {
//        return new CleanupRoom(name, top, left, bottom, right, colour);
//    }
//
//    @Override
//    public String toString() {
//        return OOStateUtilities.objectInstanceToString(this);
//    }
//}
