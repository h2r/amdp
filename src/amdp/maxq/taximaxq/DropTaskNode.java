package amdp.maxq.taximaxq;


import amdp.maxq.framework.PrimitiveTaskNode;
import amdp.taxi.TaxiDomain;
import amdp.taxi.state.TaxiLocation;
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
public class DropTaskNode extends PrimitiveTaskNode {


    HashableStateFactory hsf = new HashableStateFactory() {
        @Override
        public HashableState hashState(State s) {
            return new DropHashState(s);
        }
    };

    public DropTaskNode (ActionType a) {
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


    private class DropHashState extends WrappedHashableState{
        // original state
        State state;

        public DropHashState(State s){
            this.state = s;
        }

        private boolean legalDrop(){
            boolean flag = false;
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
            return flag;
        }

        @Override
        public int hashCode() {
            // boolean true or false
            return legalDrop() ? 0 : 1;
        }

        @Override
        public boolean equals(Object obj) {
            // check hash of both obj and our, if equal then return true else false!
//            System.out.println("was here too 4!");
            if (obj == null) {
                return false;
            }

            if (getClass() != obj.getClass()) {
                return false;
            }

            DropHashState otherObj = (DropHashState)obj;

            // if legal then equal
            return (otherObj.legalDrop() == this.legalDrop()) ? true : false;
        }
    }


}
