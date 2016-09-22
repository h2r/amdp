package amdp.cleanupamdpdomains.cleanuplevel2.state;


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
public class CleanupL2State  implements MutableOOState {

    public CleanupAgentL2 agent;
    public List<CleanupBlockL2> blocks = new ArrayList<CleanupBlockL2>();
    public List<CleanupRoomL2> rooms = new ArrayList<CleanupRoomL2>();

    public CleanupL2State(CleanupAgentL2 agent, List<CleanupBlockL2> blocks, List<CleanupRoomL2> rooms) {
        this.agent = agent;
        this.blocks = blocks;
        this.rooms = rooms;
    }

    @Override
    public MutableOOState addObject(ObjectInstance o) {
        throw new RuntimeException("Cannot add objects to state.");
    }

    @Override
    public MutableOOState removeObject(String objectName) {
        if(objectName.equals(agent.name())){
            new RuntimeException("Cannot remove agent object from state.");
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


        return this;
    }

    @Override
    public MutableOOState renameObject(String objectName, String newName) {
        if(objectName.equals(agent.name())){
            CleanupAgentL2 nagent = agent.copyWithName(newName);
            this.agent= nagent;
            return this;
        }

        int indL = this.roomInd(objectName);
        if(indL != -1) {
            //copy on write
            CleanupRoomL2 nloc = this.rooms.get(indL).copyWithName(newName);
            touchRooms().remove(indL);
            rooms.add(indL, nloc);
            return this;
        }


        int indW = this.blockInd(objectName);
        if(indW == -1) {
            //copy on write
            CleanupBlockL2 nWall = this.blocks.get(indW).copyWithName(newName);
            touchBlocks().remove(indW);
            blocks.add(indW, nWall);
            return this;
        }

        throw new RuntimeException("Cannot find object: " + objectName);
    }

    @Override
    public int numObjects() {
        return 1+blocks.size()+rooms.size();
    }

    @Override
    public ObjectInstance object(String objectName) {
        if(objectName.equals(agent.name())){
            CleanupAgentL2 nagent = agent.copy();
            return nagent;
        }

        int indL = this.roomInd(objectName);
        if(indL != -1) {
            //copy on write
            CleanupRoomL2 nroom = this.rooms.get(indL).copy();
            return nroom;
        }


        int indW = this.blockInd(objectName);
        if(indW != -1) {
            //copy on write
            CleanupBlockL2 nBlock = this.blocks.get(indW).copy();
            return nBlock;
        }

        throw new RuntimeException("Cannot find object: " + objectName);
    }

    @Override
    public List<ObjectInstance> objects() {
        List<ObjectInstance> obs = new ArrayList<ObjectInstance>(1+rooms.size()+blocks.size());
        obs.add(agent);
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



        int indW = this.blockInd(key.obName);
        if(indW == -1) {
            //copy on write
            return this.blocks.get(indW).get(key.obVarKey);
        }

        throw new RuntimeException("Cleanup State L2: cannot find object " + key.obName);
    }

    @Override
    public CleanupL2State copy() {
        return new CleanupL2State(agent,blocks,rooms);
    }

    @Override
    public String toString() {
        return OOStateUtilities.ooStateToString(this);
    }

    public CleanupAgentL2 touchAgent(){
        this.agent = agent.copy();
        return agent;
    }

    public List<CleanupRoomL2> touchRooms(){
        this.rooms = new ArrayList<CleanupRoomL2>(rooms);
        return rooms;
    }


    public List<CleanupBlockL2> touchBlocks(){
        this.blocks = new ArrayList<CleanupBlockL2>(blocks);
        return blocks;
    }

    public CleanupBlockL2 touchBlock(int ind){
        CleanupBlockL2 n = blocks.get(ind).copy();
        touchBlocks().remove(ind);
        blocks.add(ind, n);
        return n;
    }

    public CleanupRoomL2 touchRoom(int ind){
        CleanupRoomL2 n = rooms.get(ind).copy();
        touchRooms().remove(ind);
        rooms.add(ind, n);
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



}
