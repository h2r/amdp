package amdp.taxiamdpdomains.taxiamdplevel1.taxil1state;

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

import static amdp.taxiamdpdomains.taxiamdplevel1.TaxiL1Domain.*;

/**
 * Created by ngopalan on 6/14/16.
 */
@ShallowCopyState
public class TaxiL1State implements MutableOOState{

    public TaxiL1Agent taxi;
    public List<TaxiL1Location> locations = new ArrayList<TaxiL1Location>();
    public List<TaxiL1Passenger> passengers = new ArrayList<TaxiL1Passenger>();


//    public boolean pickUpLeagal = false;
//    public boolean dropOffLeagal = false;

    public TaxiL1State(List<TaxiL1Passenger> passengers, List<TaxiL1Location> locations, TaxiL1Agent taxi) {
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
            touchLocations().remove(indP);
            return this;
        }


        return this;
    }

    @Override
    public MutableOOState renameObject(String objectName, String newName) {
        if(objectName.equals(taxi.name())){
            TaxiL1Agent nagent = taxi.copyWithName(newName);
            this.taxi= nagent;
            return this;
        }

        int indL = this.locationInd(objectName);
        if(indL != -1) {
            //copy on write
            TaxiL1Location nloc = this.locations.get(indL).copyWithName(newName);
            touchLocations().remove(indL);
            locations.add(indL, nloc);
            return this;
        }

        int indP = this.passengerInd(objectName);
        if(indP != -1){
            //copy on write
            TaxiL1Passenger nloc = this.passengers.get(indP).copyWithName(newName);
            touchLocations().remove(indP);
            passengers.add(indP, nloc);
            return this;
        }


        throw new RuntimeException("Cannot find object: " + objectName);
    }

    @Override
    public int numObjects() {
        return 1+locations.size()+passengers.size();
    }

    @Override
    public ObjectInstance object(String objectName) {
        if(objectName.equals(taxi.name())){
            TaxiL1Agent nagent = taxi.copy();
            return nagent;
        }

        int indL = this.locationInd(objectName);
        if(indL != -1) {
            //copy on write
            TaxiL1Location nloc = this.locations.get(indL).copy();
            return nloc;
        }

        int indP = this.passengerInd(objectName);
        if(indP != -1){
            //copy on write
            TaxiL1Passenger nloc = this.passengers.get(indP).copy();
            return nloc;
        }

        throw new RuntimeException("Cannot find object: " + objectName);

    }

    @Override
    public List<ObjectInstance> objects() {
        List<ObjectInstance> obs = new ArrayList<ObjectInstance>(1+locations.size()+passengers.size());
        obs.add(taxi);
        obs.addAll(locations);
        obs.addAll(passengers);
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

        throw new RuntimeException("Unknown class type " + oclass);
    }

    @Override
    public MutableState set(Object variableKey, Object value) {
        OOVariableKey key = OOStateUtilities.generateKey(variableKey);

        if(key.obName.equals(taxi.name())){

            if(key.obVarKey.equals(VAR_OCCUPIEDTAXI)){
                boolean vBool = StateUtilities.stringOrBoolean(value).booleanValue();
                touchTaxi().taxiOccupied = vBool;
            }
            else if(key.obVarKey.equals(VAR_CURRENTLOCATION)){
                if(value instanceof String){
                    touchTaxi().currentLocation = (String) value;
                }
                else throw new RuntimeException("Variable value must be String for key VAR_CURRENTLOCATION in TaxiState for the Taxi agent: " + value.toString());
            }
            else{
                throw new RuntimeException("Unknown variable key in TaxiState for TaxiAgent: " + variableKey);
            }
            return this;
        }
        int indL = locationInd(key.obName);
        if(indL!=-1){
            if(key.obVarKey.equals(VAR_LOCATION)){
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
            if(key.obVarKey.equals(VAR_GOALLOCATION)){
                if(value instanceof String){
                    touchPassenger(indP).goalLocation = (String) value;
                }
                else throw new RuntimeException("Variable value must be String for key VAR_GOALLOCATION in TaxiState for TaxiPassenger: " + value.toString());

            }
            else if(key.obVarKey.equals(VAR_INTAXI)){
                touchPassenger(indP).inTaxi = StateUtilities.stringOrBoolean(value).booleanValue();
            }
            else if(key.obVarKey.equals(VAR_PICKEDUPATLEASTONCE)){
                touchPassenger(indP).pickUpOnce = StateUtilities.stringOrBoolean(value).booleanValue();
            }
            else if(key.obVarKey.equals(VAR_CURRENTLOCATION)){
                if(value instanceof String){
                    touchPassenger(indP).currentLocation = (String) value;
                }
                else throw new RuntimeException("Variable value must be String for key VAR_CURRENTLOCATION in TaxiState for the Taxi passenger: " + value.toString());
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
        throw new RuntimeException("Cannot find object " + key.obName);
    }

    @Override
    public TaxiL1State copy() {
        return new TaxiL1State(passengers,locations,taxi);
    }

    @Override
    public String toString() {
        return OOStateUtilities.ooStateToString(this);
    }

    public TaxiL1Agent touchTaxi(){
        this.taxi = taxi.copy();
        return taxi;
    }

    public List<TaxiL1Location> touchLocations(){
        this.locations = new ArrayList<TaxiL1Location>(locations);
        return locations;
    }

    public List<TaxiL1Location> deepTouchLocations(){
        List<TaxiL1Location> nlocs = new ArrayList<TaxiL1Location>(locations.size());
        for(TaxiL1Location loc : locations){
            nlocs.add(loc.copy());
        }
        locations = nlocs;
        return locations;
    }

    public TaxiL1Location touchLocation(int ind){
        TaxiL1Location n = locations.get(ind).copy();
        touchLocations().remove(ind);
        locations.add(ind, n);
        return n;
    }

    public List<TaxiL1Passenger> touchPassengers(){
        this.passengers = new ArrayList<TaxiL1Passenger>(passengers);
        return passengers;
    }

    public List<TaxiL1Passenger> deepTouchPassengers(){
        List<TaxiL1Passenger> nps = new ArrayList<TaxiL1Passenger>(passengers.size());
        for(TaxiL1Passenger p : passengers){
            nps.add(p.copy());
        }
        passengers= nps;
        return passengers;
    }

    public TaxiL1Passenger touchPassenger(int ind){
        TaxiL1Passenger n = passengers.get(ind).copy();
        touchPassengers().remove(ind);
        passengers.add(ind, n);
        return n;
    }

    public TaxiL1Passenger touchPassenger(String passName){
        int ind = passengerInd(passName);
        TaxiL1Passenger n = passengers.get(ind).copy();
        touchPassengers().remove(ind);
        passengers.add(ind, n);
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

}
