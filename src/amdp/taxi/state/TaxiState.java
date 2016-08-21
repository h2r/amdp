package amdp.taxi.state;

import amdp.taxi.TaxiDomain;
import burlap.mdp.core.oo.state.MutableOOState;
import burlap.mdp.core.oo.state.OOStateUtilities;
import burlap.mdp.core.oo.state.OOVariableKey;
import burlap.mdp.core.oo.state.ObjectInstance;
import burlap.mdp.core.state.MutableState;
import burlap.mdp.core.state.State;
import burlap.mdp.core.state.StateUtilities;
import burlap.mdp.core.state.annotations.ShallowCopyState;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static amdp.taxi.TaxiDomain.*;

/**
 * Created by ngopalan on 6/14/16.
 */
@ShallowCopyState
public class TaxiState implements MutableOOState{

    public TaxiAgent taxi;
    public List<TaxiLocation> locations = new ArrayList<TaxiLocation>();
    public List<TaxiPassenger> passengers = new ArrayList<TaxiPassenger>();
    public List<TaxiMapWall> walls = new ArrayList<TaxiMapWall>();

//    public boolean pickUpLeagal = false;
//    public boolean dropOffLeagal = false;

    public TaxiState(List<TaxiMapWall> walls, List<TaxiPassenger> passengers, List<TaxiLocation> locations, TaxiAgent taxi) {
        this.walls = walls;
        this.passengers = passengers;
        this.locations = locations;
        this.taxi = taxi;
    }

    @Override
    public MutableOOState addObject(ObjectInstance o) {
        throw new RuntimeException("Cannot add objects to state.");
    }

    @Override
    public MutableOOState removeObject(String objectName) {
        if(objectName.equals(taxi.name())){
            new RuntimeException("Cannot remove taxi agent object from state.");
        }

        int indL = this.locationInd(objectName);
        if(indL != -1) {
            //copy on write
            touchLocations().remove(indL);
            return this;
        }

        int indP = this.passengerInd(objectName);
        if(indP != -1){
            //copy on write
            touchPassengers().remove(indP);
            return this;
        }

        int indW = this.wallInd(objectName);
        if(indW == -1) {
            //copy on write
            touchWalls().remove(indW);
            return this;
        }

        return this;
    }

    @Override
    public MutableOOState renameObject(String objectName, String newName) {
        if(objectName.equals(taxi.name())){
            TaxiAgent nagent = taxi.copyWithName(newName);
            this.taxi= nagent;
            return this;
        }

        int indL = this.locationInd(objectName);
        if(indL != -1) {
            //copy on write
            TaxiLocation nloc = this.locations.get(indL).copyWithName(newName);
            touchLocations().remove(indL);
            locations.add(indL, nloc);
            return this;
        }

        int indP = this.passengerInd(objectName);
        if(indP != -1){
            //copy on write
            TaxiPassenger nloc = this.passengers.get(indP).copyWithName(newName);
            touchLocations().remove(indP);
            passengers.add(indP, nloc);
            return this;
        }

        int indW = this.wallInd(objectName);
        if(indW == -1) {
            //copy on write
            TaxiMapWall nWall = this.walls.get(indW).copyWithName(newName);
            touchLocations().remove(indW);
            walls.add(indW, nWall);
            return this;
        }

        throw new RuntimeException("Cannot find object: " + objectName);
    }

    @Override
    public int numObjects() {
        return 1+locations.size()+passengers.size()+walls.size();
    }

    @Override
    public ObjectInstance object(String objectName) {
        if(objectName.equals(taxi.name())){
            TaxiAgent nagent = taxi.copy();
            return nagent;
        }

        int indL = this.locationInd(objectName);
        if(indL != -1) {
            //copy on write
            TaxiLocation nloc = this.locations.get(indL).copy();
            return nloc;
        }

        int indP = this.passengerInd(objectName);
        if(indP != -1){
            //copy on write
            TaxiPassenger nloc = this.passengers.get(indP).copy();
            return nloc;
        }

        int indW = this.wallInd(objectName);
        if(indW != -1) {
            //copy on write
            TaxiMapWall nWall = this.walls.get(indW).copy();
            return nWall;
        }

        throw new RuntimeException("Cannot find object: " + objectName);

    }

    @Override
    public List<ObjectInstance> objects() {
        List<ObjectInstance> obs = new ArrayList<ObjectInstance>(1+locations.size()+walls.size()+passengers.size());
        obs.add(taxi);
        obs.addAll(locations);
        obs.addAll(passengers);
        obs.addAll(walls);
        return obs;
    }

    @Override
    public List<ObjectInstance> objectsOfClass(String oclass) {
        if(oclass.equals(TaxiDomain.TAXICLASS)){
            return Arrays.<ObjectInstance>asList(taxi);
        }
        else if(oclass.equals(TaxiDomain.LOCATIONCLASS)){
            return new ArrayList<ObjectInstance>(locations);
        }
        else if(oclass.equals(TaxiDomain.PASSENGERCLASS)){
            return new ArrayList<ObjectInstance>(passengers);
        }
        else if(oclass.equals(TaxiDomain.WALLCLASS)){
//            List<ObjectInstance> hWallObjects = new ArrayList<ObjectInstance>();
//            for(TaxiMapWall w:walls){
//                if(!w.verticalWall){
//                    hWallObjects.add(w);
//                }
//            }
//            return hWallObjects;
            return new ArrayList<ObjectInstance>(walls);
        }
//        else if(oclass.equals(TaxiDomain.VWALLCLASS)){
//            List<ObjectInstance> vWallObjects = new ArrayList<ObjectInstance>();
//            for(TaxiMapWall w:walls){
//                if(w.verticalWall){
//                    vWallObjects.add(w);
//                }
//            }
//            return vWallObjects;
//        }

        throw new RuntimeException("Unknown class type " + oclass);
    }

    @Override
    public MutableState set(Object variableKey, Object value) {
        OOVariableKey key = OOStateUtilities.generateKey(variableKey);

        if(key.obName.equals(taxi.name())){
            if(key.obVarKey.equals(VAR_X)){
                int iv = StateUtilities.stringOrNumber(value).intValue();
                touchTaxi().x = iv;
            }
            else if(key.obVarKey.equals(VAR_Y)){
                int iv = StateUtilities.stringOrNumber(value).intValue();
                touchTaxi().y = iv;
            }
            else if(key.obVarKey.equals(VAR_FUEL)){
                int iv = StateUtilities.stringOrNumber(value).intValue();
                touchTaxi().fuel = iv;
            }
            else if(key.obVarKey.equals(VAR_OCCUPIEDTAXI)){
                boolean vBool = StateUtilities.stringOrBoolean(value).booleanValue();
                touchTaxi().taxiOccupied=vBool;
            }
            else{
                throw new RuntimeException("Unknown variable key in TaxiState for TaxiAgent: " + variableKey);
            }
            return this;
        }
        int indL = locationInd(key.obName);
        if(indL!=-1){
            if(key.obVarKey.equals(VAR_X)){
                int iv = StateUtilities.stringOrNumber(value).intValue();
                touchLocation(indL).x = iv;
            }
            else if(key.obVarKey.equals(VAR_Y)){
                int iv = StateUtilities.stringOrNumber(value).intValue();
                touchLocation(indL).y = iv;
            }
            else if(key.obVarKey.equals(VAR_LOCATION)){
                if(value instanceof String){
                    touchLocation(indL).colour = (String) value;
                }
                else throw new RuntimeException("Variable value must be String for key VAR_LOCATION in TaxiState for TaxiLocation: " + value.toString());

            }
            else{
                throw new RuntimeException("Unknown variable key in TaxiState for TaxiLocation: " + variableKey);
            }

            return this;

        }
        int indP = passengerInd(key.obName);
        if(indP!=-1){
            if(key.obVarKey.equals(VAR_X)){
                int iv = StateUtilities.stringOrNumber(value).intValue();
                touchPassenger(indP).x = iv;
            }
            else if(key.obVarKey.equals(VAR_Y)){
                int iv = StateUtilities.stringOrNumber(value).intValue();
                touchPassenger(indP).y = iv;
            }
            else if(key.obVarKey.equals(VAR_GOALLOCATION)){
                if(value instanceof String){
                    touchPassenger(indP).goalLocation = (String) value;
                }
                else throw new RuntimeException("Variable value must be String for key VAR_GOALLOCATION in TaxiState for TaxiPassenger: " + value.toString());


            }
            else if(key.obVarKey.equals(VAR_INTAXI)){
                touchPassenger(indP).inTaxi = StateUtilities.stringOrBoolean(value).booleanValue();
            }
            else if(key.obVarKey.equals(VAR_JUSTPICKEDUP)){
                touchPassenger(indP).justPickedUp = StateUtilities.stringOrBoolean(value).booleanValue();
            }
            else{
                throw new RuntimeException("Unknown variable key " + variableKey);
            }

            return this;

        }
        int indW = wallInd(key.obName);
        if(indW!=-1){
            int iv = StateUtilities.stringOrNumber(value).intValue();
            if(key.obVarKey.equals(VAR_WALLMAX)){
                touchWall(indW).wallMax = iv;
            }
            else if(key.obVarKey.equals(VAR_WALLMIN)){
                touchWall(indW).wallMin= iv;
            }
            else if(key.obVarKey.equals(VAR_WALLOFFSET)){
                touchWall(indW).wallOffset = iv;
            }
            else{
                throw new RuntimeException("Unknown variable key " + variableKey);
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
        if(key.obName.equals(taxi.name())){
            return taxi.get(key.obVarKey);
        }
        int indL = this.locationInd(key.obName);
        if(indL != -1){
            return locations.get(indL).get(key.obVarKey);
        }
        int indP = this.passengerInd(key.obName);
        if(indP != -1){
            return passengers.get(indP).get(key.obVarKey);
        }
        int indW = this.wallInd(key.obName);
        if(indW != -1){
            return walls.get(indW).get(key.obVarKey);
        }
        throw new RuntimeException("Cannot find object " + key.obName);
    }

    @Override
    public TaxiState copy() {
        return new TaxiState(walls,passengers,locations,taxi);
    }

    @Override
    public String toString() {
        return OOStateUtilities.ooStateToString(this);
    }

    public TaxiAgent touchTaxi(){
        this.taxi = taxi.copy();
        return taxi;
    }

    public List<TaxiLocation> touchLocations(){
        this.locations = new ArrayList<TaxiLocation>(locations);
        return locations;
    }

    public List<TaxiLocation> deepTouchLocations(){
        List<TaxiLocation> nlocs = new ArrayList<TaxiLocation>(locations.size());
        for(TaxiLocation loc : locations){
            nlocs.add(loc.copy());
        }
        locations = nlocs;
        return locations;
    }

    public TaxiLocation touchLocation(int ind){
        TaxiLocation n = locations.get(ind).copy();
        touchLocations().remove(ind);
        locations.add(ind, n);
        return n;
    }

    public List<TaxiPassenger> touchPassengers(){
        this.passengers = new ArrayList<TaxiPassenger>(passengers);
        return passengers;
    }

    public List<TaxiPassenger> deepTouchPassengers(){
        List<TaxiPassenger> nps = new ArrayList<TaxiPassenger>(passengers.size());
        for(TaxiPassenger p : passengers){
            nps.add(p.copy());
        }
        passengers= nps;
        return passengers;
    }

    public TaxiPassenger touchPassenger(int ind){
        TaxiPassenger n = passengers.get(ind).copy();
        touchPassengers().remove(ind);
        passengers.add(ind, n);
        return n;
    }

    public TaxiPassenger touchPassenger(String passName){
        int ind = passengerInd(passName);
        TaxiPassenger n = passengers.get(ind).copy();
        touchPassengers().remove(ind);
        passengers.add(ind, n);
        return n;
    }

    public List<TaxiMapWall> touchWalls(){
        this.walls = new ArrayList<TaxiMapWall>(walls);
        return walls;
    }

    public List<TaxiMapWall> deepTouchWalls(){
        List<TaxiMapWall> nws = new ArrayList<TaxiMapWall>(walls.size());
        for(TaxiMapWall w : walls){
            nws.add(w.copy());
        }
        walls = nws;
        return walls;
    }

    public TaxiMapWall touchWall(int ind){
        TaxiMapWall n = walls.get(ind).copy();
        touchWalls().remove(ind);
        walls.add(ind, n);
        return n;
    }

    public int locationInd(String oname){
        int ind = -1;
        for(int i = 0; i < locations.size(); i++){
            if(locations.get(i).name().equals(oname)){
                ind = i;
                break;
            }
        }
        return ind;
    }

    public int locationIndWithColour(String colour){
        int ind = -1;
        for(int i = 0; i < locations.size(); i++){
            if(locations.get(i).colour.equals(colour)){
                ind = i;
                break;
            }
        }
        return ind;
    }

    public int passengerInd(String oname){
        int ind = -1;
        for(int i = 0; i < passengers.size(); i++){
            if(passengers.get(i).name().equals(oname)){
                ind = i;
                break;
            }
        }
        return ind;
    }

    public int wallInd(String oname){
        int ind = -1;
        for(int i = 0; i < walls.size(); i++){
            if(walls.get(i).name().equals(oname)){
                ind = i;
                break;
            }
        }
        return ind;
    }
}
