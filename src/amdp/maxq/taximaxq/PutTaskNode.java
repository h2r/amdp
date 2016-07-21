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

import java.util.ArrayList;
import java.util.List;

/**
 * Task node for the put down task, parameters are passenger names.
 * Created by ngopalan on 5/24/16.
 */
public class PutTaskNode extends NonPrimitiveTaskNode {


    List<String[]> params = new ArrayList<String[]>();
    List<GroundedTask> groundedTasks = new ArrayList<GroundedTask>();

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
//
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

}
