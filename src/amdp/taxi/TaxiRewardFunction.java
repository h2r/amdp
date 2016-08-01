package amdp.taxi;

import amdp.taxi.state.TaxiAgent;
import amdp.taxi.state.TaxiLocation;
import amdp.taxi.state.TaxiPassenger;
import amdp.taxi.state.TaxiState;
import burlap.mdp.core.action.Action;
import burlap.mdp.core.TerminalFunction;
import burlap.mdp.core.oo.state.ObjectInstance;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.model.RewardFunction;

import java.util.List;

/**
 * Created by ngopalan on 5/25/16.
 */
public class TaxiRewardFunction implements RewardFunction {

    public double stepReward = -1.0;
    public double illegalAction = -10;
    public double goalReward = +20;

    TerminalFunction tf;
    Integer numPass = 0;
    public TaxiRewardFunction(Integer numPassengers, TerminalFunction tf) {
        numPass = numPassengers;
        this.tf = tf;

    }

    @Override
    public double reward(State state, Action groundedAction, State state1) {

        if(tf.isTerminal(state1)){
            return numPass * goalReward + stepReward;
        }
        // illegal dropoff
        if(groundedAction.actionName().equals(TaxiDomain.ACTION_DROPOFF)){
            boolean flag = false;
            ObjectInstance taxi = ((TaxiState)state).objectsOfClass(TaxiDomain.TAXICLASS).get(0);
//            boolean taxiOccupied = ((TaxiAgent)taxi).taxiOccupied;
            List<ObjectInstance> passengers = ((TaxiState)state).objectsOfClass(TaxiDomain.PASSENGERCLASS);
            List<ObjectInstance> locationList = ((TaxiState)state).objectsOfClass(TaxiDomain.LOCATIONCLASS);
            for(ObjectInstance p : passengers){
                if(((TaxiPassenger)p).inTaxi){
                    String goalLocation = ((TaxiPassenger)p).goalLocation;
                    for(ObjectInstance l :locationList){
                        if(goalLocation.equals(((TaxiLocation)l).colour)){
                            if(((TaxiLocation)l).x==((TaxiPassenger)p).x
                                    && ((TaxiLocation)l).y==((TaxiPassenger)p).y){
                                flag = true;
                                break;
                            }
                        }
                    }


                }
            }
            if(!flag){
                return illegalAction+stepReward;
            }

        }
        // illegal pickup when picking up when a passenger is not present at taxi's location
        if(groundedAction.actionName().equals(TaxiDomain.ACTION_PICKUP)){
            boolean flag = false;
            ObjectInstance taxi = ((TaxiState)state).objectsOfClass(TaxiDomain.TAXICLASS).get(0);
            boolean taxiOccupied = ((TaxiAgent)taxi).taxiOccupied;

            if(taxiOccupied){
                return illegalAction + stepReward;
            }
            List<ObjectInstance> passengers = ((TaxiState)state).objectsOfClass(TaxiDomain.PASSENGERCLASS);
            int taxiX = ((TaxiAgent)taxi).x;
            int taxiY = ((TaxiAgent)taxi).y;
//            List<ObjectInstance> locationList = state.getObjectsOfClass(TaxiDomain.LOCATIONCLASS);
            for(ObjectInstance p : passengers){
                if(!((TaxiPassenger)p).inTaxi){
                    if(taxiX==((TaxiPassenger)p).x
                            && taxiY==((TaxiPassenger)p).y){
                        flag = true;
                        break;
                    }
                }
            }
            if(!flag){
                return illegalAction+stepReward;
            }
        }

        return stepReward;
    }
}
