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

import java.util.ArrayList;
import java.util.List;

/**
 * Task node for the get task, parameters are passenger names.
 * Created by ngopalan on 5/24/16.
 */
public class GetTaskNode extends NonPrimitiveTaskNode {

    List<String[]> params = new ArrayList<String[]>();
    List<GroundedTask> groundedTasks = new ArrayList<GroundedTask>();

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
}
