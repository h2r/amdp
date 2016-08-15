package amdp.taxiamdpdomains.taxiamdplevel1;

import amdp.taxiamdpdomains.taxiamdplevel1.taxil1state.TaxiL1Location;
import amdp.taxiamdpdomains.taxiamdplevel1.taxil1state.TaxiL1Passenger;
import amdp.taxiamdpdomains.taxiamdplevel1.taxil1state.TaxiL1State;
import burlap.mdp.core.TerminalFunction;
import burlap.mdp.core.state.State;

import java.util.List;

/**
 * Created by ngopalan on 8/13/16.
 */
public class TaxiL1TerminalFunction implements TerminalFunction {


    @Override
    public boolean isTerminal(State state) {
        List<TaxiL1Passenger> passengerList = ((TaxiL1State)state).passengers;
        List<TaxiL1Location> locationList = ((TaxiL1State)state).locations;
        for(TaxiL1Passenger p:passengerList){
            if(p.inTaxi){
                return false;
            }
            String goalLocation = p.goalLocation;
            for(TaxiL1Location l :locationList){
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
