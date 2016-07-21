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
public class TaxiMapWall implements ObjectInstance {



    public int wallMin;
    public int wallMax;
    public int wallOffset;

    public String name;

    /*
    a variable for book keeping so we have a common class for horizontal and vertical walls
     */
    boolean verticalWall;

    private final static List<Object> keys = Arrays.<Object>asList(VAR_WALLOFFSET, VAR_WALLMIN, VAR_WALLMAX);

    public TaxiMapWall(String name, int wallMin, int wallMax, int wallOffset, boolean verticalWall) {
        this.name = name;
        this.wallMax = wallMax;
        this.wallMin = wallMin;
        this.wallOffset = wallOffset;
        this.verticalWall =verticalWall;
    }

    @Override
    public String className() {
        if(verticalWall){
            return TaxiDomain.VWALLCLASS;
        }
        return TaxiDomain.HWALLCLASS;
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public TaxiMapWall copyWithName(String objectName) {
        TaxiMapWall nWall = this.copy();
        nWall.name = objectName;
        return nWall;
    }

    @Override
    public List<Object> variableKeys() {
        return keys;
    }

    @Override
    public Object get(Object variableKey) {
        if(!(variableKey instanceof String)){
            throw new RuntimeException("TaxiMapWall variable key must be a string");
        }
        String key = (String)variableKey;
        if(key.equals(VAR_WALLMAX)){
            return wallMax;
        }
        else if(key.equals(VAR_WALLMIN)){
            return wallMin;
        }
        else if(key.equals(VAR_WALLOFFSET)){
            return wallOffset;
        }

        throw new RuntimeException("Unknown key for TaxiMap Wall: " + key);
    }

    @Override
    public TaxiMapWall copy() {
        return new TaxiMapWall(name,  wallMin, wallMax,wallOffset, verticalWall);
    }

    @Override
    public String toString() {
        return OOStateUtilities.objectInstanceToString(this);
    }
}
