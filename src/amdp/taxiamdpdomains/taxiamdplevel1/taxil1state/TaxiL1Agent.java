package amdp.taxiamdpdomains.taxiamdplevel1.taxil1state;

import burlap.mdp.core.oo.state.OOStateUtilities;
import burlap.mdp.core.oo.state.ObjectInstance;
import burlap.mdp.core.state.annotations.DeepCopyState;

import java.util.Arrays;
import java.util.List;

import static amdp.taxiamdpdomains.taxiamdplevel1.TaxiL1Domain.*;


/**
 * Created by ngopalan on 8/10/16.
 */
@DeepCopyState
public class TaxiL1Agent implements ObjectInstance {
    public boolean taxiOccupied;
    public String currentLocation;
    protected String name;

    private final static List<Object> keys = Arrays.<Object>asList(VAR_CURRENTLOCATION,  VAR_OCCUPIEDTAXI);


    public TaxiL1Agent(String name, boolean taxiOccupied, String currentLocation) {
        this.name = name;
        this.taxiOccupied =taxiOccupied;
        this.currentLocation = currentLocation;
//        this.fuel = fuel;
    }



    public TaxiL1Agent(String name) {
        this.name = name;
        this.taxiOccupied =false;
        this.currentLocation = ON_ROAD;
    }

    @Override
    public String className() {
        return TAXIL1CLASS;
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public TaxiL1Agent copyWithName(String objectName) {
        TaxiL1Agent nagent = this.copy();
        nagent.name = objectName;
        return nagent;
    }

    @Override
    public List<Object> variableKeys() {
        return keys;
    }

    @Override
    public Object get(Object variableKey) {
        if(!(variableKey instanceof String)){
            throw new RuntimeException("TaxiAgent variable key must be a string");
        }
        String key = (String)variableKey;
        if(key.equals(VAR_OCCUPIEDTAXI)){
            return taxiOccupied;
        }
        else if(key.equals(VAR_CURRENTLOCATION)){
            return currentLocation;
        }

        throw new RuntimeException("Unknown key for TaxiAgent: " + key);
    }

    @Override
    public TaxiL1Agent copy() {
        return new TaxiL1Agent(name,taxiOccupied,currentLocation);
    }

    @Override
    public String toString() {
        return OOStateUtilities.objectInstanceToString(this);
    }
}
