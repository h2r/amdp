package amdp.taxiamdpdomains.taxiamdplevel2.taxil2state;


import amdp.taxi.TaxiDomain;
import amdp.taxiamdpdomains.taxiamdplevel2.TaxiL2Domain;
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

import static amdp.taxiamdpdomains.taxiamdplevel2.TaxiL2Domain.*;

/**
 * Created by ngopalan on 6/14/16.
 */
@ShallowCopyState
public class TaxiL2State implements MutableOOState{

    public List<TaxiL2Location> locations = new ArrayList<TaxiL2Location>();
    public List<TaxiL2Passenger> passengers = new ArrayList<TaxiL2Passenger>();


//    public boolean pickUpLeagal = false;
//    public boolean dropOffLeagal = false;

    public TaxiL2State(List<TaxiL2Passenger> passengers, List<TaxiL2Location> locations) {
        this.passengers = passengers;
        this.locations = locations;
    }

    @Override
    public MutableOOState addObject(ObjectInstance o) {
        throw new RuntimeException("Cannot add objects to state.");
    }

    @Override
    public MutableOOState removeObject(String objectName) {

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


        int indL = this.locationInd(objectName);
        if(indL != -1) {
            //copy on write
            TaxiL2Location nloc = this.locations.get(indL).copyWithName(newName);
            touchLocations().remove(indL);
            locations.add(indL, nloc);
            return this;
        }

        int indP = this.passengerInd(objectName);
        if(indP != -1){
            //copy on write
            TaxiL2Passenger nloc = this.passengers.get(indP).copyWithName(newName);
            touchLocations().remove(indP);
            passengers.add(indP, nloc);
            return this;
        }


        throw new RuntimeException("Cannot find object: " + objectName);
    }

    @Override
    public int numObjects() {
        return locations.size()+passengers.size();
    }

    @Override
    public ObjectInstance object(String objectName) {

        int indL = this.locationInd(objectName);
        if(indL != -1) {
            //copy on write
            TaxiL2Location nloc = this.locations.get(indL).copy();
            return nloc;
        }

        int indP = this.passengerInd(objectName);
        if(indP != -1){
            //copy on write
            TaxiL2Passenger nloc = this.passengers.get(indP).copy();
            return nloc;
        }

        throw new RuntimeException("Cannot find object: " + objectName);

    }

    @Override
    public List<ObjectInstance> objects() {
        List<ObjectInstance> obs = new ArrayList<ObjectInstance>(locations.size()+passengers.size());
        obs.addAll(locations);
        obs.addAll(passengers);
        return obs;
    }

    @Override
    public List<ObjectInstance> objectsOfClass(String oclass) {
        if(oclass.equals(TaxiL2Domain.LOCATIONL2CLASS)){
            return new ArrayList<ObjectInstance>(locations);
        }
        else if(oclass.equals(TaxiL2Domain.PASSENGERL2CLASS)){
            return new ArrayList<ObjectInstance>(passengers);
        }

        throw new RuntimeException("Unknown class type " + oclass);
    }

    @Override
    public MutableState set(Object variableKey, Object value) {
        OOVariableKey key = OOStateUtilities.generateKey(variableKey);


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
    public State copy() {
        return new TaxiL2State(passengers,locations);
    }

    @Override
    public String toString() {
        return OOStateUtilities.ooStateToString(this);
    }



    public List<TaxiL2Location> touchLocations(){
        this.locations = new ArrayList<TaxiL2Location>(locations);
        return locations;
    }

    public List<TaxiL2Location> deepTouchLocations(){
        List<TaxiL2Location> nlocs = new ArrayList<TaxiL2Location>(locations.size());
        for(TaxiL2Location loc : locations){
            nlocs.add(loc.copy());
        }
        locations = nlocs;
        return locations;
    }

    public TaxiL2Location touchLocation(int ind){
        TaxiL2Location n = locations.get(ind).copy();
        touchLocations().remove(ind);
        locations.add(ind, n);
        return n;
    }

    public List<TaxiL2Passenger> touchPassengers(){
        this.passengers = new ArrayList<TaxiL2Passenger>(passengers);
        return passengers;
    }

    public List<TaxiL2Passenger> deepTouchPassengers(){
        List<TaxiL2Passenger> nps = new ArrayList<TaxiL2Passenger>(passengers.size());
        for(TaxiL2Passenger p : passengers){
            nps.add(p.copy());
        }
        passengers= nps;
        return passengers;
    }

    public TaxiL2Passenger touchPassenger(int ind){
        TaxiL2Passenger n = passengers.get(ind).copy();
        touchPassengers().remove(ind);
        passengers.add(ind, n);
        return n;
    }

    public TaxiL2Passenger touchPassenger(String passName){
        int ind = passengerInd(passName);
        TaxiL2Passenger n = passengers.get(ind).copy();
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
