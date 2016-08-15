package amdp.taxiamdpdomains.taxiamdplevel2.taxil2state;

import amdp.taxiamdpdomains.taxiamdplevel1.taxil1state.TaxiL1Location;
import amdp.taxiamdpdomains.taxiamdplevel1.taxil1state.TaxiL1Passenger;
import amdp.taxiamdpdomains.taxiamdplevel1.taxil1state.TaxiL1State;
import static amdp.taxiamdpdomains.taxiamdplevel2.TaxiL2Domain.*;
import burlap.mdp.auxiliary.StateMapping;
import burlap.mdp.core.state.State;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by ngopalan on 8/12/16.
 */
public class L2StateMapper implements StateMapping{
    @Override
    public State mapState(State s) {
        TaxiL1State sL1 = (TaxiL1State)s;
        List<TaxiL2Location> locationsL1 = new ArrayList<TaxiL2Location>();

        for(TaxiL1Location l0 : sL1.locations){

//            TaxiL2Location l1 = new TaxiL2Location(l0.name().split("-")[0]+"-L2",l0.colour);
            TaxiL2Location l1 = new TaxiL2Location(l0.name() ,l0.colour);
            locationsL1.add(l1);
        }

        List<TaxiL2Passenger> passengersL1 = new ArrayList<TaxiL2Passenger>();

        for(TaxiL1Passenger p1 : sL1.passengers){

            // initialize the passenger to be on road
//            TaxiL2Passenger p2 = new TaxiL2Passenger(p1.name().split("-")[0]+"-L2", p1.inTaxi, p1.goalLocation, p1.currentLocation, p1.pickUpOnce);
            TaxiL2Passenger p2 = new TaxiL2Passenger(p1.name(), p1.inTaxi, p1.goalLocation, p1.currentLocation, p1.pickUpOnce);

            passengersL1.add(p2);
        }

        return new TaxiL2State(passengersL1,locationsL1);
    }
}
