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
 * Created by ngopalan on 5/6/16.
 */
public class TaxiMAXQL0CardinalMoveTaskNode extends PrimitiveTaskNode {

    HashableStateFactory hsf = new HashableStateFactory() {
        @Override
        public HashableState hashState(State s) {
            return new MoveHashState(s);
        }
    };

    public TaxiMAXQL0CardinalMoveTaskNode(ActionType a){
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


    public class MoveHashState extends WrappedHashableState {
        // original state
        State state;

        public MoveHashState(State s){
            this.state = s;
        }


        @Override
        public int hashCode() {
            // boolean true or false
            return 1;
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

            return true;
        }
    }

}
