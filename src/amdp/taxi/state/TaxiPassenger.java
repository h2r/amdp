package amdp.taxi.state;

import amdp.taxi.TaxiDomain;
import burlap.mdp.core.oo.state.OOStateUtilities;
import burlap.mdp.core.oo.state.ObjectInstance;
import burlap.mdp.core.state.annotations.DeepCopyState;

import java.util.Arrays;
import java.util.List;


import static amdp.taxi.TaxiDomain.*;

/**
 * Created by ngopalan on 6/14/16.
 */
@DeepCopyState
public class TaxiPassenger implements ObjectInstance {

    public int x;
    public int y;
    public boolean inTaxi;
    public boolean justPickedUp;
    // in the original domain passenger needs to be picked up and dropped at least once for goal condition
    public boolean pickedUpAtLeastOnce;
    public String goalLocation;


    protected String name;



    private final static List<Object> keys = Arrays.<Object>asList(VAR_X, VAR_Y, VAR_JUSTPICKEDUP, VAR_INTAXI, VAR_GOALLOCATION, VAR_PICKEDUPATLEASTONCE);



    public TaxiPassenger(String name, int x, int y,  boolean inTaxi,
                         boolean justPickedUp, String goalLocation, boolean pickedUpAtleastOnce) {
        this.x = x;
        this.y = y;
        this.name = name;
        this.justPickedUp = justPickedUp;
        this.inTaxi = inTaxi;
        this.goalLocation = goalLocation;
        this.pickedUpAtLeastOnce = pickedUpAtleastOnce;
    }

    public TaxiPassenger(String name, int x, int y,  String goalLocation) {
        this.x = x;
        this.y = y;
        this.name = name;
        this.justPickedUp = false;
        this.inTaxi = false;
        this.goalLocation = goalLocation;
    }


    @Override
    public String className() {
        return TaxiDomain.PASSENGERCLASS;
    }

    @Override
    public String name() {
        return this.name;
    }

    @Override
    public TaxiPassenger  copyWithName(String objectName) {
        TaxiPassenger nPassenger = this.copy();
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
        if(key.equals(VAR_X)){
            return x;
        }
        else if(key.equals(VAR_Y)){
            return y;
        }
        else if(key.equals(VAR_GOALLOCATION)){
            return goalLocation;
        }
        else if(key.equals(VAR_INTAXI)){
            return inTaxi;
        }
        else if(key.equals(VAR_JUSTPICKEDUP)){
            return justPickedUp;
        }
        else if(key.equals(VAR_PICKEDUPATLEASTONCE)){
            return pickedUpAtLeastOnce;
        }

        throw new RuntimeException("Unknown key for TaxiPassenger " + key);
    }

    @Override
    public TaxiPassenger copy() {
        return new TaxiPassenger(name, x, y, inTaxi, justPickedUp,
                goalLocation, pickedUpAtLeastOnce);
    }

    @Override
    public String toString() {
        return OOStateUtilities.objectInstanceToString(this);
    }
}
