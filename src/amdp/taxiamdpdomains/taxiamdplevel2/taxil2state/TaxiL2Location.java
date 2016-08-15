package amdp.taxiamdpdomains.taxiamdplevel2.taxil2state;


import burlap.mdp.core.oo.state.OOStateUtilities;
import burlap.mdp.core.oo.state.ObjectInstance;

import java.util.Arrays;
import java.util.List;

import static amdp.taxiamdpdomains.taxiamdplevel2.TaxiL2Domain.*;

/**
 * Created by ngopalan on 8/10/16.
 */
public class TaxiL2Location implements ObjectInstance {

    public String colour;
    String name;
    private final static List<Object> keys = Arrays.<Object>asList(VAR_LOCATION);


    public TaxiL2Location(String name, String colour) {
        this.name = name;
        this.colour = colour;
    }

    @Override
    public String className() {
        return LOCATIONL2CLASS;
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public TaxiL2Location copyWithName(String objectName) {
        TaxiL2Location nLocation = this.copy();
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
    public TaxiL2Location copy() {
        return new TaxiL2Location(name,colour);
    }

    @Override
    public String toString() {
        return OOStateUtilities.objectInstanceToString(this);
    }
}
