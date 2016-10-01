//package amdp.cleanupturtlebot.cleanupl0discrete.state;
//
//
//import burlap.mdp.core.oo.state.ObjectInstance;
//import burlap.mdp.core.state.annotations.DeepCopyState;
//
//import java.util.Arrays;
//import java.util.List;
//
//import static amdp.cleanupturtlebot.cleanupl0discrete.CleanupTurtleBotL0Domain.*;
//
//
///**
// * Created by ngopalan on 8/24/16.
// */
//@DeepCopyState
//public class CleanupAgent implements ObjectInstance {
//
//    public String name;
//    public int x;
//    public int y;
//    public boolean directional = false;
//    public String currentDirection;
//    public int length = 2;
//    public int width = 1;
//
//    private final static List<Object> keys = Arrays.<Object>asList(VAR_X, VAR_Y, VAR_DIR, VAR_LENGTH, VAR_WIDTH);
//
//    public CleanupAgent(String name, int x, int y){
//        this.name = name;
//        this.x= x;
//        this.y= y;
//    }
//
//    public CleanupAgent(String name, int x, int y, String currentDirection, int length, int width){
//        this.name = name;
//        this.x= x;
//        this.y= y;
//        this.directional = true;
//        this.currentDirection = currentDirection;
//        this.length=length;
//        this.width= width;
//    }
//
//    @Override
//    public String className() {
//        return CLASS_AGENT;
//    }
//
//    @Override
//    public String name() {
//        return name;
//    }
//
//    @Override
//    public CleanupAgent copyWithName(String objectName) {
//        if(directional){
//            return new CleanupAgent(objectName,x,y, currentDirection, length, width);
//        }
//        return new CleanupAgent(objectName, x, y);
//    }
//
//    @Override
//    public List<Object> variableKeys() {
//        return keys;
//    }
//
//    @Override
//    public Object get(Object variableKey) {
//
//        if(!(variableKey instanceof String)){
//            throw new RuntimeException("Cleanup Agent variable key must be a string");
//        }
//
//        String key = (String)variableKey;
//        if(key.equals(VAR_X)){
//            return x;
//        }
//        else if(key.equals(VAR_Y)){
//            return y;
//        }
//        else if(key.equals(VAR_DIR)){
//            return currentDirection;
//        }
//        else if(key.equals(VAR_LENGTH)){
//            return length;
//        }
//        else if(key.equals(VAR_WIDTH)){
//            return width;
//        }
//
//        throw new RuntimeException("Unknown key for Cleanup Agent: " + key);
//    }
//
//    @Override
//    public CleanupAgent copy() {
//        if(directional){
//            return new CleanupAgent(name,x,y, currentDirection, length, width);
//        }
//        return new CleanupAgent(name,x,y);
//    }
//
//    @Override
//    public boolean equals(Object o) {
//        // self check
//        if (this == o)
//            return true;
//        // null check
//        if (o == null)
//            return false;
//        // type check and cast
//        if (getClass() != o.getClass())
//            return false;
//        CleanupAgent that = (CleanupAgent) o;
//        // field comparison
//        return (this.directional==that.directional) && (this.x==that.x)
//                && (this.y==that.y) && (this.currentDirection.equals(that.currentDirection))
//                && (this.name == that.name()) && (that.width==this.width) && (this.length == that.length);
//    }
//}
