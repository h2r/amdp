package amdp.taxiamdpdomains.taxiamdplevel1.taxil1state;


import burlap.mdp.core.oo.state.OOStateUtilities;
import burlap.mdp.core.oo.state.ObjectInstance;
import burlap.mdp.core.state.State;

import java.util.Arrays;
import java.util.List;

import static amdp.taxiamdpdomains.taxiamdplevel1.TaxiL1Domain.*;

/**
 * Created by ngopalan on 8/10/16.
 */
public class TaxiL1Location implements ObjectInstance {

    public String colour;
    String name;
    private final static List<Object> keys = Arrays.<Object>asList(VAR_LOCATION);


    public TaxiL1Location(String name, String colour) {
        this.name = name;
        this.colour = colour;
    }

    @Override
    public String className() {
        return LOCATIONL1CLASS;
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public TaxiL1Location copyWithName(String objectName) {
        TaxiL1Location nLocation = this.copy();
        nLocation.name = objectName;
        return nLocation;
    }

    @Override
    public List<Object> variableKeys() {
        return keys;
    }

    @Override
    public Object get(Object variableKey) {
        if(!(variableKey instanceof String)){
            throw new RuntimeException("TaxiLocation variable key must be a string");
        }
        String key = (String)variableKey;
        if(key.equals(VAR_LOCATION)){
            return colour;
        }

        throw new RuntimeException("Unknown key for TaxiLocation " + key);
    }

    @Override
    public TaxiL1Location copy() {
        return new TaxiL1Location(name,colour);
    }

    @Override
    public String toString() {
        return OOStateUtilities.objectInstanceToString(this);
    }
}
