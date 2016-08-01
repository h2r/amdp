package amdp.maxq.taximaxq;

import amdp.maxq.framework.PrimitiveTaskNode;
import amdp.taxi.TaxiDomain;
import amdp.taxi.state.TaxiAgent;
import amdp.taxi.state.TaxiPassenger;
import amdp.taxi.state.TaxiState;
import burlap.mdp.core.action.ActionType;
import burlap.mdp.core.oo.state.ObjectInstance;
import burlap.mdp.core.state.State;
import burlap.statehashing.HashableState;
import burlap.statehashing.HashableStateFactory;
import burlap.statehashing.WrappedHashableState;

import java.util.List;

/**
 * Created by ngopalan on 5/24/16.
 */
public class PickupTaskNode extends PrimitiveTaskNode {

    HashableStateFactory hsf = new HashableStateFactory() {
        @Override
        public HashableState hashState(State s) {
            return new PickHashState(s);
        }
    };


    public PickupTaskNode (ActionType a) {
        this.setActionType(a);
    }

    @Override
    public boolean hasHashingFactory(){
        return true;
    }

    @Override
    public HashableState hashedState(State s){
        return this.hsf.hashState(s);
    }


    public class PickHashState extends WrappedHashableState {
        // original state
        State state;

        public PickHashState(State s){
            this.state = s;
        }

        private boolean legalPickup(){

            boolean flag = false;
            ObjectInstance taxi = ((TaxiState)state).objectsOfClass(TaxiDomain.TAXICLASS).get(0);
            boolean taxiOccupied = ((TaxiAgent)taxi).taxiOccupied;

            if(taxiOccupied){
                return flag;
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

            return flag;

        }

        @Override
        public int hashCode() {
            // boolean true or false
            return legalPickup() ? 0 : 1;
        }

        @Override
        public boolean equals(Object obj) {
            // check hash of both obj and our, if equal then return true else false!
            if (obj == null) {
                return false;
            }

            if (getClass() != obj.getClass()) {
                return false;
            }

            PickHashState otherObj = (PickHashState)obj;

            // if legal then equal
            return (otherObj.legalPickup() == this.legalPickup()) ? true : false;
        }
    }

}
