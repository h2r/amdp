package amdp.maxq.taximaxq;

import amdp.maxq.framework.GroundedTask;
import amdp.maxq.framework.NonPrimitiveTaskNode;
import amdp.maxq.framework.TaskNode;
import amdp.taxi.state.TaxiAgent;
import amdp.taxi.state.TaxiPassenger;
import amdp.taxi.state.TaxiState;
import burlap.behavior.valuefunction.QValue;
import burlap.mdp.core.action.Action;
import burlap.mdp.core.action.SimpleAction;
import burlap.mdp.core.state.State;
import burlap.statehashing.HashableState;
import burlap.statehashing.HashableStateFactory;
import burlap.statehashing.WrappedHashableState;

import java.util.ArrayList;
import java.util.List;

/**
 * Task node for the get task, parameters are passenger names.
 * Created by ngopalan on 5/24/16.
 */
public class GetTaskNode extends NonPrimitiveTaskNode {



    List<String[]> params = new ArrayList<String[]>();
    List<GroundedTask> groundedTasks = new ArrayList<GroundedTask>();

    //we have two hashing factories -  one for pickup and one for navigate!

    HashableStateFactory navHsf = new HashableStateFactory() {
        @Override
        public HashableState hashState(State s) {
            return new NavHashState(s);
        }
    };

    HashableStateFactory pickupHsf = new HashableStateFactory() {
        @Override
        public HashableState hashState(State s) {
            return new PickHashState(s);
        }
    };


    @Override
    public boolean hasHashingFactory(){
        return true;
    }

    @Override
    public HashableState hashedState(State s, GroundedTask childTask){
        // if navigate return the navigate states else return the pickup hash states
        if(childTask.getT().getName().split(":")[0].equals("navigate")){
            return this.navHsf.hashState(s);
        }
        return this.pickupHsf.hashState(s);
    }




    public GetTaskNode(String name, List<String[]> params, TaskNode[] children) {
        this.name = name;
        this.params = params;
        this.setTaskNodes(children);
        for(String[] param:params){
            groundedTasks.add(new GroundedTask(this, new SimpleAction(name+":"+param[0])));
        }
    }

    @Override
    public Object parametersSet(State s) {
        // here I am going to presume that a passenger is present in the task node
        return params;
    }

    @Override
    public boolean terminal(State s, Action action) {
        // check if the passenger in taxi and taxi with a passenger
        //[0] is the name of the simple action or the parent task!
        String passengerName = action.actionName().split(":")[1] ;//((String[])parameters)[0];
        TaxiPassenger passenger = (TaxiPassenger)((TaxiState)s).object(passengerName);
        boolean inTaxi = passenger.inTaxi;
        TaxiAgent taxi = ((TaxiState)s).taxi;
        boolean taxiOccupied = taxi.taxiOccupied;
        return inTaxi&&taxiOccupied;
    }

    @Override
    public List<GroundedTask> getApplicableGroundedTasks(State s) {
        List<GroundedTask> applicableGroundedTasks = new ArrayList<GroundedTask>();
        TaxiAgent taxi = ((TaxiState)s).taxi;
        boolean taxiOccupied = taxi.taxiOccupied;
        if(!taxiOccupied){
            return this.groundedTasks;
        }
        // empty return
        return applicableGroundedTasks;
    }



    public class NavHashState extends WrappedHashableState {
        // original state
        State state;

        public NavHashState(State s){
            this.state = s;
        }

        private int createHash(){
            // check source and return state!
            int hashC =0;
            for(TaxiPassenger p:((TaxiState)state).passengers){
                hashC = 31 * hashC + p.originalSourceLocation.hashCode();
            }
            return hashC;
        }

        @Override
        public int hashCode() {
            // boolean true or false
            return createHash();
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

            NavHashState otherObj = (NavHashState)obj;

            // if legal then equal
            return (otherObj.createHash() == this.createHash()) ? true : false;
        }
    }

    public class PickHashState extends WrappedHashableState {
        // original state
        State state;

        public PickHashState(State s){
            this.state = s;
        }

        private int createHash(){
            int x = ((TaxiState)state).taxi.x;
            int y = ((TaxiState)state).taxi.y;
            int hashC =0;
            for(TaxiPassenger p:((TaxiState)state).passengers){
                hashC = 31 * hashC + p.originalSourceLocation.hashCode();
            }
            return 10*x + y + hashC;
        }

        @Override
        public int hashCode() {
            // boolean true or false
            return createHash();
        }

        @Override
        public boolean equals(Object obj) {
//            System.out.println("was here too 6!");
            // check hash of both obj and our, if equal then return true else false!
            if (obj == null) {
                return false;
            }

            if (getClass() != obj.getClass()) {
                return false;
            }

            PickHashState otherObj = (PickHashState)obj;

            // if legal then equal
            return (otherObj.createHash() == this.createHash()) ? true : false;
        }
    }
}
