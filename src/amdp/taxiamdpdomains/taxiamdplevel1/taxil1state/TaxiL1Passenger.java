package amdp.taxiamdpdomains.taxiamdplevel1.taxil1state;


import burlap.mdp.core.oo.state.OOStateUtilities;
import burlap.mdp.core.oo.state.ObjectInstance;

import java.util.Arrays;
import java.util.List;

import static amdp.taxiamdpdomains.taxiamdplevel1.TaxiL1Domain.*;


/**
 * Created by ngopalan on 8/10/16.
 */
public class TaxiL1Passenger implements ObjectInstance{

    public boolean inTaxi;
    public String goalLocation;
    public String currentLocation;
    public boolean pickUpOnce;
    protected String name;



    private final static List<Object> keys = Arrays.<Object>asList(VAR_INTAXI, VAR_GOALLOCATION, VAR_CURRENTLOCATION, VAR_PICKEDUPATLEASTONCE);



    public TaxiL1Passenger(String name, boolean inTaxi,
                         String goalLocation, String currentLocation, boolean pickUpOnce) {
        this.name = name;
        this.inTaxi = inTaxi;
        this.goalLocation = goalLocation;
        this.currentLocation = currentLocation;
        this.pickUpOnce = pickUpOnce;
    }

    public TaxiL1Passenger(String name, String goalLocation, String currentLocation) {
        this.name = name;
        this.inTaxi = false;
        this.goalLocation = goalLocation;
        this.currentLocation = currentLocation;
        this.pickUpOnce = false;
    }


    @Override
    public String className() {
        return PASSENGERL1CLASS;
    }

    @Override
    public String name() {
        return this.name;
    }

    @Override
    public TaxiL1Passenger copyWithName(String objectName) {
        TaxiL1Passenger nPassenger = this.copy();
        nPassenger.name = objectName;
        return nPassenger;
    }

    @Override
    public List<Object> variableKeys() {
        return keys;
    }

    @Override
    public Object get(Object variableKey) {
        if(!(variableKey instanceof String)){
            throw new RuntimeException("TaxiPassenger variable key must be a string");
        }

        String key = (String)variableKey;
        if(key.equals(VAR_GOALLOCATION)){
            return goalLocation;
        }
        else if(key.equals(VAR_INTAXI)){
            return inTaxi;
        }
        else if(key.equals(VAR_CURRENTLOCATION)){
            return currentLocation;
        }
        else if(key.equals(VAR_PICKEDUPATLEASTONCE)){
            return pickUpOnce;
        }


        throw new RuntimeException("Unknown key for TaxiPassenger " + key);
    }

    @Override
    public TaxiL1Passenger copy() {
        return new TaxiL1Passenger(name, inTaxi, goalLocation, currentLocation, pickUpOnce);
    }

    @Override
    public String toString() {
        return OOStateUtilities.objectInstanceToString(this);
    }

}
