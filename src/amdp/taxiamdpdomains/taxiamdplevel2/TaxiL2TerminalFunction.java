package amdp.taxiamdpdomains.taxiamdplevel2;

import amdp.taxiamdpdomains.taxiamdplevel2.taxil2state.TaxiL2Location;
import amdp.taxiamdpdomains.taxiamdplevel2.taxil2state.TaxiL2Passenger;
import amdp.taxiamdpdomains.taxiamdplevel2.taxil2state.TaxiL2State;
import burlap.mdp.core.TerminalFunction;
import burlap.mdp.core.state.State;

import java.util.List;

/**
 * Created by ngopalan on 8/13/16.
 */
public class TaxiL2TerminalFunction implements TerminalFunction {


    @Override
    public boolean isTerminal(State state) {
        List<TaxiL2Passenger> passengerList = ((TaxiL2State)state).passengers;
        List<TaxiL2Location> locationList = ((TaxiL2State)state).locations;
        for(TaxiL2Passenger p:passengerList){
            if(p.inTaxi){
                return false;
            }
            String goalLocation = p.goalLocation;
            for(TaxiL2Location l :locationList){
//                System.out.println("goal: " + goalLocation);
//                System.out.println("location attribute: " + l.getStringValForAttribute(TaxiDomain.LOCATIONATT));
                if(goalLocation.equals(l.colour)){
                    if(p.currentLocation.equals(l.colour) && p.pickUpOnce){
                        break;
                    }
                    else{
                        return false;
                    }
                }
            }
        }

        return true;
    }


}
