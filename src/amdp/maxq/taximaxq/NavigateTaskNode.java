package amdp.maxq.taximaxq;

import amdp.maxq.framework.GroundedTask;
import amdp.maxq.framework.NonPrimitiveTaskNode;
import amdp.maxq.framework.TaskNode;
import amdp.taxi.TaxiDomain;
import amdp.taxi.state.TaxiAgent;
import amdp.taxi.state.TaxiLocation;
import amdp.taxi.state.TaxiState;
import burlap.behavior.valuefunction.QValue;
import burlap.mdp.core.action.Action;
import burlap.mdp.core.action.SimpleAction;
import burlap.mdp.core.oo.state.ObjectInstance;
import burlap.mdp.core.state.State;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ngopalan on 5/23/16.
 */
public class NavigateTaskNode extends NonPrimitiveTaskNode {


    // all possible bindings for the current node
    List<String[]> params = new ArrayList<String[]>();
    List<GroundedTask> groundedTasks = new ArrayList<GroundedTask>();

    public NavigateTaskNode(String name, List<String[]> params, TaskNode[] children){
        this.name = name;
        this.params = params;
        this.setTaskNodes(children);
        for(String[] param:params){
            // param[0] is the goal location!
            groundedTasks.add(new GroundedTask(this, new SimpleAction(name+":"+param[0])));
        }
    }

    @Override
    public boolean terminal(State s, Action navAction){
        // the parameters are from a grounded task of the type String[]
        List<TaxiLocation> locations = ((TaxiState)s).locations;
        TaxiAgent taxi = ((TaxiState)s).taxi;
        String goalLocation = navAction.actionName().split(":")[1];//((String[])parameters)[0];
        for(TaxiLocation location:locations){
            if(location.name().equals(goalLocation)){
                if(taxi.x==location.x &&
                        taxi.y==location.y){
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
            if(!this.terminal(s,gt.getAction())){
                gtList.add(gt);
            }

        }
        return gtList;
    }

    @Override
    public List<String[]> parametersSet(State s) {
        return params;
    }

}
