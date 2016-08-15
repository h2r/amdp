package amdp.taxiamdpdomains.taxiamdplevel1.taxil1state;

import amdp.taxi.state.TaxiLocation;
import amdp.taxi.state.TaxiPassenger;
import amdp.taxi.state.TaxiState;
import burlap.mdp.auxiliary.StateMapping;
import burlap.mdp.core.state.State;

import java.util.ArrayList;
import java.util.List;

import static amdp.taxiamdpdomains.taxiamdplevel1.TaxiL1Domain.*;

/**
 * Created by ngopalan on 8/12/16.
 */
public class L1StateMapper implements StateMapping{
    @Override
    public State mapState(State s) {
        TaxiState sL0 = (TaxiState)s;
        TaxiL1Agent taxiL1 = new TaxiL1Agent(TAXIL1CLASS+0, sL0.taxi.taxiOccupied ,ON_ROAD);
        List<TaxiL1Location> locationsL1 = new ArrayList<TaxiL1Location>();

        int taxiX = sL0.taxi.x;
        int taxiY = sL0.taxi.y;

        for(TaxiLocation l0 : sL0.locations){
            if(taxiX==l0.x && taxiY==l0.y){
                taxiL1.currentLocation = l0.colour;
            }
//            TaxiL1Location l1 = new TaxiL1Location(l0.name()+"-L1",l0.colour);
            TaxiL1Location l1 = new TaxiL1Location(l0.name(),l0.colour);
            locationsL1.add(l1);
        }

        List<TaxiL1Passenger> passengersL1 = new ArrayList<TaxiL1Passenger>();

        for(TaxiPassenger p0 : sL0.passengers){
            int xp = p0.x;
            int yp = p0.y;
            // initialize the passenger to be on road
//            TaxiL1Passenger p1 = new TaxiL1Passenger(p0.name()+"-L1", p0.inTaxi, p0.goalLocation, ON_ROAD, p0.pickedUpAtLeastOnce);
            TaxiL1Passenger p1 = new TaxiL1Passenger(p0.name(), p0.inTaxi, p0.goalLocation, ON_ROAD, p0.pickedUpAtLeastOnce);
            for(TaxiLocation l0 : sL0.locations){
                if(xp==l0.x && yp==l0.y){
                    p1.currentLocation = l0.colour;
                    break;
                }
            }
            passengersL1.add(p1);
        }

        return new TaxiL1State(passengersL1,locationsL1,taxiL1);
    }
}
