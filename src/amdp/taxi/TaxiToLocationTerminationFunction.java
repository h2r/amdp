package amdp.taxi;

/**
 * Created by ngopalan on 6/18/16.
 */


import amdp.taxi.state.TaxiAgent;
import amdp.taxi.state.TaxiLocation;
import amdp.taxi.state.TaxiPassenger;
import amdp.taxi.state.TaxiState;
import burlap.mdp.core.TerminalFunction;
import burlap.mdp.core.oo.state.ObjectInstance;
import burlap.mdp.core.state.State;

import java.util.List;

/**
 * Created by ngopalan.
 */
public class TaxiToLocationTerminationFunction implements TerminalFunction {

    TaxiLocation lEnd;
    public TaxiToLocationTerminationFunction(TaxiLocation l) {
        this.lEnd = l;
    }

    @Override
    public boolean isTerminal(State state) {
        TaxiState sTemp = (TaxiState)state;
        TaxiAgent taxi = sTemp.taxi;
        int taxiX = taxi.x;
        int taxiY = taxi.y;

        if(taxiX==lEnd.x && taxiY==lEnd.y ){
            return true;
        }
        return false;
    }


}
