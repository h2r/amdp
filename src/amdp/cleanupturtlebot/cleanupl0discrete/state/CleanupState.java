//package amdp.cleanupturtlebot.cleanupl0discrete.state;
//
//import amdp.cleanup.CleanupDomain;
//import burlap.mdp.core.oo.state.MutableOOState;
//import burlap.mdp.core.oo.state.OOStateUtilities;
//import burlap.mdp.core.oo.state.OOVariableKey;
//import burlap.mdp.core.oo.state.ObjectInstance;
//import burlap.mdp.core.state.MutableState;
//import burlap.mdp.core.state.StateUtilities;
//import burlap.mdp.core.state.annotations.ShallowCopyState;
//
//import java.util.ArrayList;
//import java.util.Arrays;
//import java.util.List;
//
//import static amdp.cleanup.CleanupDomain.*;
//
///**
// * Created by ngopalan on 8/24/16.
// */
//@ShallowCopyState
//public class CleanupState implements MutableOOState {
//
//    public CleanupAgent agent;
//    public List<CleanupBlock> blocks = new ArrayList<CleanupBlock>();
//    public List<CleanupDoor> doors = new ArrayList<CleanupDoor>();
//    public List<CleanupRoom> rooms = new ArrayList<CleanupRoom>();
//
//    public CleanupState(CleanupAgent agent, List<CleanupBlock> blocks, List<CleanupDoor> doors, List<CleanupRoom> rooms) {
//        this.agent = agent;
//        this.blocks = blocks;
//        this.doors = doors;
//        this.rooms = rooms;
//    }
//
//    @Override
//    public MutableOOState addObject(ObjectInstance o) {
//        throw new RuntimeException("Cannot add objects to state.");
//    }
//
//    @Override
//    public MutableOOState removeObject(String objectName) {
//        if(objectName.equals(agent.name())){
//            new RuntimeException("Cannot remove taxi agent object from state.");
//        }
//        int indL = this.roomInd(objectName);
//        if(indL != -1) {
//            //copy on write
//            touchRooms().remove(indL);
//            return this;
//        }
//
//        int indP = this.blockInd(objectName);
//        if(indP != -1){
//            //copy on write
//            touchBlocks().remove(indP);
//            return this;
//        }
//
//        int indW = this.doorInd(objectName);
//        if(indW == -1) {
//            //copy on write
//            touchDoors().remove(indW);
//            return this;
//        }
//
//        return this;
//    }
//
//    @Override
//    public MutableOOState renameObject(String objectName, String newName) {
//        if(objectName.equals(agent.name())){
//            CleanupAgent nagent = agent.copyWithName(newName);
//            this.agent= nagent;
//            return this;
//        }
//
//        int indL = this.roomInd(objectName);
//        if(indL != -1) {
//            //copy on write
//            CleanupRoom nloc = this.rooms.get(indL).copyWithName(newName);
//            touchRooms().remove(indL);
//            rooms.add(indL, nloc);
//            return this;
//        }
//
//        int indP = this.doorInd(objectName);
//        if(indP != -1){
//            //copy on write
//            CleanupDoor nloc = this.doors.get(indP).copyWithName(newName);
//            touchDoors().remove(indP);
//            doors.add(indP, nloc);
//            return this;
//        }
//
//        int indW = this.blockInd(objectName);
//        if(indW == -1) {
//            //copy on write
//            CleanupBlock nWall = this.blocks.get(indW).copyWithName(newName);
//            touchBlocks().remove(indW);
//            blocks.add(indW, nWall);
//            return this;
//        }
//
//        throw new RuntimeException("Cannot find object: " + objectName);
//    }
//
//    @Override
//    public int numObjects() {
//        return 1+doors.size()+blocks.size()+rooms.size();
//    }
//
//    @Override
//    public ObjectInstance object(String objectName) {
//        if(objectName.equals(agent.name())){
//            CleanupAgent nagent = agent.copy();
//            return nagent;
//        }
//
//        int indL = this.roomInd(objectName);
//        if(indL != -1) {
//            //copy on write
//            CleanupRoom nroom = this.rooms.get(indL).copy();
//            return nroom;
//        }
//
//        int indP = this.doorInd(objectName);
//        if(indP != -1){
//            //copy on write
//            CleanupDoor ndoor = this.doors.get(indP).copy();
//            return ndoor;
//        }
//
//        int indW = this.blockInd(objectName);
//        if(indW != -1) {
//            //copy on write
//            CleanupBlock nBlock = this.blocks.get(indW).copy();
//            return nBlock;
//        }
//
//        throw new RuntimeException("Cannot find object: " + objectName);
//    }
//
//    @Override
//    public List<ObjectInstance> objects() {
//        List<ObjectInstance> obs = new ArrayList<ObjectInstance>(1+rooms.size()+doors.size()+blocks.size());
//        obs.add(agent);
//        obs.addAll(doors);
//        obs.addAll(blocks);
//        obs.addAll(rooms);
//        return obs;
//    }
//
//    @Override
//    public List<ObjectInstance> objectsOfClass(String oclass) {
//
//        if(oclass.equals(CleanupDomain.CLASS_AGENT)){
//            return Arrays.<ObjectInstance>asList(agent);
//        }
//        else if(oclass.equals(CleanupDomain.CLASS_BLOCK)){
//            return new ArrayList<ObjectInstance>(blocks);
//        }
//        else if(oclass.equals(CleanupDomain.CLASS_DOOR)){
//            return new ArrayList<ObjectInstance>(doors);
//        }
//        else if(oclass.equals(CleanupDomain.CLASS_ROOM)){
//            return new ArrayList<ObjectInstance>(rooms);
//        }
//        throw new RuntimeException("Unknown class type " + oclass);
//    }
//
//    @Override
//    public MutableState set(Object variableKey, Object value) {
//        OOVariableKey key = OOStateUtilities.generateKey(variableKey);
//
//        if(key.obName.equals(agent.name())){
//            if(key.obVarKey.equals(VAR_X)){
//                int iv = StateUtilities.stringOrNumber(value).intValue();
//                touchAgent().x = iv;
//            }
//            else if(key.obVarKey.equals(VAR_Y)){
//                int iv = StateUtilities.stringOrNumber(value).intValue();
//                touchAgent().y = iv;
//            }
//            else if(key.obVarKey.equals(VAR_DIR)){
//                if(value instanceof String) {
//                    touchAgent().currentDirection = (String) value;
//                    touchAgent().directional = true;
//                }
//                else throw new RuntimeException("Variable value must be String for key VAR_DIR in CleanupContinuousState for Agent Direction: " + value.toString());
//            }
//            else{
//                throw new RuntimeException("Unknown variable key in Cleanup State for Agent: " + variableKey);
//            }
//            return this;
//        }
//
//        int indL = this.roomInd(key.obName);
//        if(indL != -1) {
//            if(key.obVarKey.equals(VAR_TOP)){
//                int iv = StateUtilities.stringOrNumber(value).intValue();
//                touchRoom(indL).top = iv;
//            }
//            else if(key.obVarKey.equals(VAR_LEFT)){
//                int iv = StateUtilities.stringOrNumber(value).intValue();
//                touchRoom(indL).left = iv;
//            }
//            else if(key.obVarKey.equals(VAR_BOTTOM)){
//                int iv = StateUtilities.stringOrNumber(value).intValue();
//                touchRoom(indL).bottom = iv;
//            }
//            else if(key.obVarKey.equals(VAR_RIGHT)){
//                int iv = StateUtilities.stringOrNumber(value).intValue();
//                touchRoom(indL).right = iv;
//            }
//            else if(key.obVarKey.equals(VAR_COLOUR)){
//                if(value instanceof String) {
//                    touchRoom(indL).colour = (String) value;
//                }
//                else throw new RuntimeException("Variable value must be String for key VAR_COLOUR in CleanupContinuousState for Room colour: " + value.toString());
//            }
//            else{
//                throw new RuntimeException("Unknown variable key in Cleanup State for Room: " + variableKey);
//            }
//            return this;
//        }
//
//        int indR = this.doorInd(key.obName);
//        if(indR != -1){
//            if(key.obVarKey.equals(VAR_TOP)){
//                int iv = StateUtilities.stringOrNumber(value).intValue();
//                touchDoor(indR).top = iv;
//            }
//            else if(key.obVarKey.equals(VAR_LEFT)){
//                int iv = StateUtilities.stringOrNumber(value).intValue();
//                touchDoor(indR).left = iv;
//            }
//            else if(key.obVarKey.equals(VAR_BOTTOM)){
//                int iv = StateUtilities.stringOrNumber(value).intValue();
//                touchDoor(indR).bottom = iv;
//            }
//            else if(key.obVarKey.equals(VAR_RIGHT)){
//                int iv = StateUtilities.stringOrNumber(value).intValue();
//                touchDoor(indR).right = iv;
//            }
//            else if(key.obVarKey.equals(VAR_LOCKED)){
//                int iv = StateUtilities.stringOrNumber(value).intValue();
//                touchDoor(indR).locked = StateUtilities.stringOrNumber(value).intValue();
//            }
//            else if(key.obVarKey.equals(VAR_CAN_BE_LOCKED)){
//                int iv = StateUtilities.stringOrNumber(value).intValue();
//                touchDoor(indR).canBeLocked = StateUtilities.stringOrBoolean(value).booleanValue();
//            }
//            else{
//                throw new RuntimeException("Unknown variable key in Cleanup State for Room: " + variableKey);
//            }
//            return this;
//        }
//
//        int indB = this.blockInd(key.obName);
//        if(indB == -1) {
//            if(key.obVarKey.equals(VAR_X)){
//                int iv = StateUtilities.stringOrNumber(value).intValue();
//                touchBlock(indB).x = iv;
//            }
//            else if(key.obVarKey.equals(VAR_Y)){
//                int iv = StateUtilities.stringOrNumber(value).intValue();
//                touchBlock(indB).y = iv;
//            }
//            else if(key.obVarKey.equals(VAR_COLOUR)){
//                if(value instanceof String) {
//                    touchBlock(indB).colour = (String) value;
//                }
//                else throw new RuntimeException("Variable value must be String for key VAR_COLOUR in CleanupContinuousState for Block colour: " + value.toString());
//            }
//            else if(key.obVarKey.equals(VAR_SHAPE)){
//                if(value instanceof String) {
//                    touchBlock(indB).shape = (String) value;
//                }
//                else throw new RuntimeException("Variable value must be String for key VAR_SHAPE in CleanupContinuousState for Block shape: " + value.toString());
//            }
//            else{
//                throw new RuntimeException("Unknown variable key in Cleanup State for Block: " + variableKey);
//            }
//            return this;
//        }
//
//        throw new RuntimeException("Unknown variable key " + variableKey);
//    }
//
//    @Override
//    public List<Object> variableKeys() {
//        return OOStateUtilities.flatStateKeys(this);
//    }
//
//    @Override
//    public Object get(Object variableKey) {
//        OOVariableKey key = OOStateUtilities.generateKey(variableKey);
//
//        if(key.obName.equals(agent.name())){
//            return  agent.get(key.obVarKey);
//        }
//        int indL = this.roomInd(key.obName);
//        if(indL != -1) {
//            //copy on write
//            return this.rooms.get(indL).get(key.obVarKey);
//        }
//
//        int indP = this.doorInd(key.obName);
//        if(indP != -1){
//            //copy on write
//            return this.doors.get(indP).get(key.obVarKey);
//        }
//
//        int indW = this.blockInd(key.obName);
//        if(indW == -1) {
//            //copy on write
//            return this.blocks.get(indW).get(key.obVarKey);
//        }
//
//        throw new RuntimeException("Cleanup State : cannot find object " + key.obName);
//    }
//
//    @Override
//    public CleanupState copy() {
//        return new CleanupState(agent,blocks,doors,rooms);
//    }
//
//    @Override
//    public String toString() {
//        return OOStateUtilities.ooStateToString(this);
//    }
//
//    public CleanupAgent touchAgent(){
//        this.agent = agent.copy();
//        return agent;
//    }
//
//    public List<CleanupRoom> touchRooms(){
//        this.rooms = new ArrayList<CleanupRoom>(rooms);
//        return rooms;
//    }
//
//    public List<CleanupDoor> touchDoors(){
//        this.doors = new ArrayList<CleanupDoor>(doors);
//        return doors;
//    }
//
//    public List<CleanupBlock> touchBlocks(){
//        this.blocks = new ArrayList<CleanupBlock>(blocks);
//        return blocks;
//    }
//
//    public CleanupBlock touchBlock(int ind){
//        CleanupBlock n = blocks.get(ind).copy();
//        touchBlocks().remove(ind);
//        blocks.add(ind, n);
//        return n;
//    }
//
//    public CleanupRoom touchRoom(int ind){
//        CleanupRoom n = rooms.get(ind).copy();
//        touchRooms().remove(ind);
//        rooms.add(ind, n);
//        return n;
//    }
//
//    public CleanupDoor touchDoor(int ind){
//        CleanupDoor n = doors.get(ind).copy();
//        touchDoors().remove(ind);
//        doors.add(ind, n);
//        return n;
//    }
//
//    public int roomInd(String oname){
//        int ind = -1;
//        for(int i = 0; i < rooms.size(); i++){
//            if(rooms.get(i).name().equals(oname)){
//                ind = i;
//                break;
//            }
//        }
//        return ind;
//    }
//
//    public int blockInd(String oname){
//        int ind = -1;
//        for(int i = 0; i < blocks.size(); i++){
//            if(blocks.get(i).name().equals(oname)){
//                ind = i;
//                break;
//            }
//        }
//        return ind;
//    }
//
//    public int doorInd(String oname){
//        int ind = -1;
//        for(int i = 0; i < doors.size(); i++){
//            if(doors.get(i).name().equals(oname)){
//                ind = i;
//                break;
//            }
//        }
//        return ind;
//    }
//
//}
