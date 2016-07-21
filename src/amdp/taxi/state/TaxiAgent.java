package amdp.taxi.state;

import amdp.taxi.TaxiDomain;
import burlap.mdp.core.oo.state.OOStateUtilities;
import burlap.mdp.core.oo.state.ObjectInstance;
import burlap.mdp.core.state.State;
import burlap.mdp.core.state.annotations.DeepCopyState;

import java.util.Arrays;
import java.util.List;

import static amdp.taxi.TaxiDomain.*;
import static amdp.taxi.TaxiDomain.VAR_GOALLOCATION;

/**
 * Created by ngopalan on 6/14/16.
 */
@DeepCopyState
public class TaxiAgent implements ObjectInstance{

    public int x;
    public int y;
    public boolean taxiOccupied;
    public int fuel = 0;

    protected String name;

    private final static List<Object> keys = Arrays.<Object>asList(VAR_X, VAR_Y, VAR_FUEL, VAR_OCCUPIEDTAXI);


    public TaxiAgent(String name, int x, int y, boolean taxiOccupied, int fuel) {
        this.name = name;
        this.x =x;
        this.y = y;
        this.taxiOccupied =taxiOccupied;
        this.fuel = fuel;
    }



    public TaxiAgent(String name, int x, int y) {
        this.name = name;
        this.x =x;
        this.y = y;
        this.taxiOccupied =false;
    }

    @Override
    public String className() {
        return TaxiDomain.TAXICLASS;
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public TaxiAgent copyWithName(String objectName) {
        TaxiAgent nagent = this.copy();
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
        if(key.equals(VAR_X)){
            return x;
        }
        else if(key.equals(VAR_Y)){
            return y;
        }
        else if(key.equals(VAR_OCCUPIEDTAXI)){
            return taxiOccupied;
        }
        else if(key.equals(VAR_FUEL)){
            return fuel;
        }

        throw new RuntimeException("Unknown key for TaxiAgent: " + key);
    }

    @Override
    public TaxiAgent copy() {
        return new TaxiAgent(name, x,y,taxiOccupied,fuel);
    }

    @Override
    public String toString() {
        return OOStateUtilities.objectInstanceToString(this);
    }
}
