package amdp.maxq.taximaxq;

import amdp.maxq.framework.GroundedTask;
import amdp.maxq.framework.NonPrimitiveTaskNode;
import amdp.maxq.framework.TaskNode;
import amdp.taxi.TaxiDomain;
import amdp.taxi.state.TaxiAgent;
import amdp.taxi.state.TaxiLocation;
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
 * Task node for the put down task, parameters are passenger names.
 * Created by ngopalan on 5/24/16.
 */
public class PutTaskNode extends NonPrimitiveTaskNode {


    List<String[]> params = new ArrayList<String[]>();
    List<GroundedTask> groundedTasks = new ArrayList<GroundedTask>();

    HashableStateFactory navHsf = new HashableStateFactory() {
        @Override
        public HashableState hashState(State s) {
//            System.out.println("calling the nav method?");
            return new NavHashState(s);
        }
    };

    HashableStateFactory dropoffHsf = new HashableStateFactory() {
        @Override
        public HashableState hashState(State s) {
            return new PutHashState(s);
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
//            System.out.println("was here");
            return this.navHsf.hashState(s);
        }
        return this.dropoffHsf.hashState(s);
    }

    public PutTaskNode(String name, List<String[]> params, TaskNode[] children) {
        this.name = name;
        this.params = params;
        this.setTaskNodes(children);
        for(String[] param:params){
            groundedTasks.add(new GroundedTask(this, new SimpleAction(name+":"+param[0])));
        }
    }

    @Override
    public Object parametersSet(State s) {
        // params are passenger names
        return params;
    }

    @Override
    public boolean terminal(State s, Action action) {
        // check if the passenger in taxi and taxi with a passenger

        String passengerName = action.actionName().split(":")[1];//((String[])parameters)[0];
        TaxiPassenger passenger = (TaxiPassenger)((TaxiState)s).object(passengerName);
        // check if passenger at goal location and out of taxi
        boolean inTaxi = passenger.inTaxi;
//        return !inTaxi;
        if(inTaxi){
            return false;
        }
//        //TODO: test if passenger at goal location!
//        String passgengerGoalLocation = passenger.getStringValForAttribute(TaxiDomain.GOALLOCATIONATT);

        List<TaxiLocation> locationList = ((TaxiState)s).locations;
        String goalLocation = passenger.goalLocation;
        for(TaxiLocation l :locationList){
//                System.out.println("goal: " + goalLocation);
//                System.out.println("location attribute: " + l.getStringValForAttribute(TaxiDomain.LOCATIONATT));
            if(goalLocation.equals(l.colour)){
                if(l.x==passenger.x
                        && l.y==passenger.y){
                    return true;
                }
                else{
                    return false;
                }
            }
        }
        return false;
    }

    @Override
    public List<GroundedTask> getApplicableGroundedTasks(State s) {
        List<GroundedTask> gtList = new ArrayList<GroundedTask>();
        for(GroundedTask gt:this.groundedTasks){
//            Object parameters = gt.getParams();
            String passengerName = gt.getAction().actionName().split(":")[1];//((String[])parameters)[0];
            TaxiPassenger passenger = (TaxiPassenger)((TaxiState)s).object(passengerName);
            boolean inTaxi = passenger.inTaxi;
            TaxiAgent taxi = ((TaxiState)s).taxi;
            boolean taxiOccupied = taxi.taxiOccupied;
            if(inTaxi&&taxiOccupied){
                gtList.add(gt);
            }
        }

        return gtList;
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
                hashC = 31 * hashC + p.goalLocation.hashCode();
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

    public class PutHashState extends WrappedHashableState {
        // original state
        State state;

        public PutHashState(State s){
            this.state = s;
        }

        private int createHash(){
            int x = ((TaxiState)state).taxi.x;
            int y = ((TaxiState)state).taxi.y;
            int hashC =0;
            for(TaxiPassenger p:((TaxiState)state).passengers){
                hashC = 31 * hashC + p.goalLocation.hashCode();
            }
            return 31*x + 17* y + hashC;
        }

        @Override
        public int hashCode() {
            // boolean true or false
            return createHash();
        }

        @Override
        public boolean equals(Object obj) {
            // check hash of both obj and our, if equal then return true else false!
//            System.out.println("2 was here too!");
            if (obj == null) {
                return false;
            }

            if (getClass() != obj.getClass()) {
                return false;
            }

            PutHashState otherObj = (PutHashState)obj;

            // if legal then equal
            return (otherObj.createHash() == this.createHash()) ? true : false;
        }
    }

}
