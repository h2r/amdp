package amdp.cleanupamdpdomains.cleanuplevel1.state;


import amdp.cleanup.CleanupDomain;
import burlap.mdp.core.oo.state.MutableOOState;
import burlap.mdp.core.oo.state.OOStateUtilities;
import burlap.mdp.core.oo.state.OOVariableKey;
import burlap.mdp.core.oo.state.ObjectInstance;
import burlap.mdp.core.state.MutableState;
import burlap.mdp.core.state.StateUtilities;
import burlap.mdp.core.state.annotations.ShallowCopyState;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static amdp.cleanupamdpdomains.cleanuplevel1.CleanupL1Domain.VAR_CONNECTED;
import static amdp.cleanupamdpdomains.cleanuplevel1.CleanupL1Domain.VAR_IN_REGION;

/**
 * Created by ngopalan on 8/28/16.
 */
@ShallowCopyState
public class CleanupL1State  implements MutableOOState {

    public CleanupAgentL1 agent;
    public List<CleanupBlockL1> blocks = new ArrayList<CleanupBlockL1>();
    public List<CleanupDoorL1> doors = new ArrayList<CleanupDoorL1>();
    public List<CleanupRoomL1> rooms = new ArrayList<CleanupRoomL1>();

    public CleanupL1State(CleanupAgentL1 agent, List<CleanupBlockL1> blocks, List<CleanupDoorL1> doors, List<CleanupRoomL1> rooms) {
        this.agent = agent;
        this.blocks = blocks;
        this.doors = doors;
        this.rooms = rooms;
    }

    @Override
    public MutableOOState addObject(ObjectInstance o) {
        throw new RuntimeException("Cannot add objects to state.");
    }

    @Override
    public MutableOOState removeObject(String objectName) {
        if(objectName.equals(agent.name())){
            new RuntimeException("Cannot remove taxi agent object from state.");
        }
        int indL = this.roomInd(objectName);
        if(indL != -1) {
            //copy on write
            touchRooms().remove(indL);
            return this;
        }

        int indP = this.blockInd(objectName);
        if(indP != -1){
            //copy on write
            touchBlocks().remove(indP);
            return this;
        }

        int indW = this.doorInd(objectName);
        if(indW == -1) {
            //copy on write
            touchDoors().remove(indW);
            return this;
        }

        return this;
    }

    @Override
    public MutableOOState renameObject(String objectName, String newName) {
        if(objectName.equals(agent.name())){
            CleanupAgentL1 nagent = agent.copyWithName(newName);
            this.agent= nagent;
            return this;
        }

        int indL = this.roomInd(objectName);
        if(indL != -1) {
            //copy on write
            CleanupRoomL1 nloc = this.rooms.get(indL).copyWithName(newName);
            touchRooms().remove(indL);
            rooms.add(indL, nloc);
            return this;
        }

        int indP = this.doorInd(objectName);
        if(indP != -1){
            //copy on write
            CleanupDoorL1 nloc = this.doors.get(indP).copyWithName(newName);
            touchDoors().remove(indP);
            doors.add(indP, nloc);
            return this;
        }

        int indW = this.blockInd(objectName);
        if(indW == -1) {
            //copy on write
            CleanupBlockL1 nWall = this.blocks.get(indW).copyWithName(newName);
            touchBlocks().remove(indW);
            blocks.add(indW, nWall);
            return this;
        }

        throw new RuntimeException("Cannot find object: " + objectName);
    }

    @Override
    public int numObjects() {
        return 1+doors.size()+blocks.size()+rooms.size();
    }

    @Override
    public ObjectInstance object(String objectName) {
        if(objectName.equals(agent.name())){
            CleanupAgentL1 nagent = agent.copy();
            return nagent;
        }

        int indL = this.roomInd(objectName);
        if(indL != -1) {
            //copy on write
            CleanupRoomL1 nroom = this.rooms.get(indL).copy();
            return nroom;
        }

        int indP = this.doorInd(objectName);
        if(indP != -1){
            //copy on write
            CleanupDoorL1 ndoor = this.doors.get(indP).copy();
            return ndoor;
        }

        int indW = this.blockInd(objectName);
        if(indW != -1) {
            //copy on write
            CleanupBlockL1 nBlock = this.blocks.get(indW).copy();
            return nBlock;
        }

        throw new RuntimeException("Cannot find object: " + objectName);
    }

    @Override
    public List<ObjectInstance> objects() {
        List<ObjectInstance> obs = new ArrayList<ObjectInstance>(1+rooms.size()+doors.size()+blocks.size());
        obs.add(agent);
        obs.addAll(doors);
        obs.addAll(blocks);
        obs.addAll(rooms);
        return obs;
    }

    @Override
    public List<ObjectInstance> objectsOfClass(String oclass) {

        if(oclass.equals(CleanupDomain.CLASS_AGENT)){
            return Arrays.<ObjectInstance>asList(agent);
        }
        else if(oclass.equals(CleanupDomain.CLASS_BLOCK)){
            return new ArrayList<ObjectInstance>(blocks);
        }
        else if(oclass.equals(CleanupDomain.CLASS_DOOR)){
            return new ArrayList<ObjectInstance>(doors);
        }
        else if(oclass.equals(CleanupDomain.CLASS_ROOM)){
            return new ArrayList<ObjectInstance>(rooms);
        }
        throw new RuntimeException("Unknown class type " + oclass);
    }

    @Override
    public MutableState set(Object variableKey, Object value) {
        OOVariableKey key = OOStateUtilities.generateKey(variableKey);

        if(key.obName.equals(agent.name())){

            if(key.obVarKey.equals(VAR_IN_REGION)){
                if(value instanceof String) {
                    touchAgent().inRegion = (String) value;
                }
                else throw new RuntimeException("Variable value must be String for key VAR_DIR in CleanupContinuousState for Agent Direction: " + value.toString());
            }
            else{
                throw new RuntimeException("Unknown variable key in Cleanup State for Agent: " + variableKey);
            }
            return this;
        }

        int indL = this.roomInd(key.obName);
        if(indL != -1) {
            if(key.obVarKey.equals(CleanupDomain.VAR_COLOUR)){
                if(value instanceof String) {
                    touchRoom(indL).colour = (String) value;
                }
                else throw new RuntimeException("Variable value must be String for key VAR_COLOUR in CleanupContinuousState for Room colour: " + value.toString());
            }
            else if(key.obVarKey.equals(VAR_CONNECTED)){
                if(value instanceof String) {
                    touchRoom(indL).connectedRegions.add((String) value);
                }
                else throw new RuntimeException("Variable value must be String for key VAR_CONNECTED in CleanupContinuousState for Room region connectivity: " + value.toString());
            }
            else{
                throw new RuntimeException("Unknown variable key in Cleanup State for Room: " + variableKey);
            }
            return this;
        }

        int indR = this.doorInd(key.obName);
        if(indR != -1){
            if(key.obVarKey.equals(CleanupDomain.VAR_LOCKED)){
                int iv = StateUtilities.stringOrNumber(value).intValue();
                touchDoor(indR).locked = StateUtilities.stringOrNumber(value).intValue();
            }
            else if(key.obVarKey.equals(CleanupDomain.VAR_CAN_BE_LOCKED)){
                int iv = StateUtilities.stringOrNumber(value).intValue();
                touchDoor(indR).canBeLocked = StateUtilities.stringOrBoolean(value).booleanValue();
            }
            else if(key.obVarKey.equals(VAR_CONNECTED)){
                if(value instanceof String) {
                    touchDoor(indR).connectedRegions.add((String) value);
                }
                else throw new RuntimeException("Variable value must be String for key VAR_CONNECTED in CleanupContinuousState for Door region: " + value.toString());
            }
            else{
                throw new RuntimeException("Unknown variable key in Cleanup State for Room: " + variableKey);
            }
            return this;
        }

        int indB = this.blockInd(key.obName);
        if(indB == -1) {

            if(key.obVarKey.equals(CleanupDomain.VAR_COLOUR)){
                if(value instanceof String) {
                    touchBlock(indB).colour = (String) value;
                }
                else throw new RuntimeException("Variable value must be String for key VAR_COLOUR in CleanupContinuousState for Block colour: " + value.toString());
            }
            else if(key.obVarKey.equals(CleanupDomain.VAR_SHAPE)){
                if(value instanceof String) {
                    touchBlock(indB).shape = (String) value;
                }
                else throw new RuntimeException("Variable value must be String for key VAR_SHAPE in CleanupContinuousState for Block shape: " + value.toString());
            }
            else if(key.obVarKey.equals(VAR_IN_REGION)){
                if(value instanceof String) {
                    touchBlock(indB).inRegion = (String) value;
                }
                else throw new RuntimeException("Variable value must be String for key VAR_IN_REGION in CleanupContinuousState for Block region: " + value.toString());
            }
            else{
                throw new RuntimeException("Unknown variable key in Cleanup State for Block: " + variableKey);
            }
            return this;
        }

        throw new RuntimeException("Unknown variable key " + variableKey);
    }

    @Override
    public List<Object> variableKeys() {
        return OOStateUtilities.flatStateKeys(this);
    }

    @Override
    public Object get(Object variableKey) {
        OOVariableKey key = OOStateUtilities.generateKey(variableKey);

        if(key.obName.equals(agent.name())){
            return  agent.get(key.obVarKey);
        }
        int indL = this.roomInd(key.obName);
        if(indL != -1) {
            //copy on write
            return this.rooms.get(indL).get(key.obVarKey);
        }

        int indP = this.doorInd(key.obName);
        if(indP != -1){
            //copy on write
            return this.doors.get(indP).get(key.obVarKey);
        }

        int indW = this.blockInd(key.obName);
        if(indW == -1) {
            //copy on write
            return this.blocks.get(indW).get(key.obVarKey);
        }

        throw new RuntimeException("Cleanup State L1: cannot find object " + key.obName);
    }

    @Override
    public CleanupL1State copy() {
        return new CleanupL1State(agent,blocks,doors,rooms);
    }

    @Override
    public String toString() {
        return OOStateUtilities.ooStateToString(this);
    }

    public CleanupAgentL1 touchAgent(){
        this.agent = agent.copy();
        return agent;
    }

    public List<CleanupRoomL1> touchRooms(){
        this.rooms = new ArrayList<CleanupRoomL1>(rooms);
        return rooms;
    }

    public List<CleanupDoorL1> touchDoors(){
        this.doors = new ArrayList<CleanupDoorL1>(doors);
        return doors;
    }

    public List<CleanupBlockL1> touchBlocks(){
        this.blocks = new ArrayList<CleanupBlockL1>(blocks);
        return blocks;
    }

    public CleanupBlockL1 touchBlock(int ind){
        CleanupBlockL1 n = blocks.get(ind).copy();
        touchBlocks().remove(ind);
        blocks.add(ind, n);
        return n;
    }

    public CleanupRoomL1 touchRoom(int ind){
        CleanupRoomL1 n = rooms.get(ind).copy();
        touchRooms().remove(ind);
        rooms.add(ind, n);
        return n;
    }

    public CleanupDoorL1 touchDoor(int ind){
        CleanupDoorL1 n = doors.get(ind).copy();
        touchDoors().remove(ind);
        doors.add(ind, n);
        return n;
    }

    public int roomInd(String oname){
        int ind = -1;
        for(int i = 0; i < rooms.size(); i++){
            if(rooms.get(i).name().equals(oname)){
                ind = i;
                break;
            }
        }
        return ind;
    }

    public int blockInd(String oname){
        int ind = -1;
        for(int i = 0; i < blocks.size(); i++){
            if(blocks.get(i).name().equals(oname)){
                ind = i;
                break;
            }
        }
        return ind;
    }

    public int doorInd(String oname){
        int ind = -1;
        for(int i = 0; i < doors.size(); i++){
            if(doors.get(i).name().equals(oname)){
                ind = i;
                break;
            }
        }
        return ind;
    }

}
