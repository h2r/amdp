package amdp.cleanupamdpdomains.cleanuplevel1;

import amdp.cleanup.CleanupDomain;
import amdp.cleanup.state.CleanupDoor;
import amdp.cleanupamdpdomains.cleanuplevel1.state.CleanupAgentL1;
import amdp.cleanupamdpdomains.cleanuplevel1.state.CleanupBlockL1;
import amdp.cleanupamdpdomains.cleanuplevel1.state.CleanupDoorL1;
import amdp.cleanupamdpdomains.cleanuplevel1.state.CleanupL1State;
import burlap.mdp.core.StateTransitionProb;
import burlap.mdp.core.action.Action;
import burlap.mdp.core.oo.ObjectParameterizedAction;
import burlap.mdp.core.oo.state.ObjectInstance;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.model.statemodel.FullStateModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by ngopalan on 8/28/16.
 */
public class CleanupL1Model implements FullStateModel{

    double lockedProb;
    Random rand;

    public CleanupL1Model(double lockedProb, Random rand) {
        this.lockedProb = lockedProb;
        this.rand  =rand;
    }

    @Override
    public List<StateTransitionProb> stateTransitions(State s, Action a) {
        CleanupL1State ns = ((CleanupL1State) s).copy();

        if(a.actionName().equals(CleanupL1Domain.ACTION_AGENT_TO_DOOR) ||
                a.actionName().equals(CleanupL1Domain.ACTION_AGENT_TO_ROOM)){
            return goToRegion(ns, (ObjectParameterizedAction)a);
        }
        else if(a.actionName().equals(CleanupL1Domain.ACTION_BLOCK_TO_DOOR) ||
                a.actionName().equals(CleanupL1Domain.ACTION_BLOCK_TO_ROOM) ){
            return blockToRegion(ns, (ObjectParameterizedAction)a);
        }


        throw new RuntimeException("unknown action queried: " + a.actionName());

    }

    private List<StateTransitionProb> blockToRegion(CleanupL1State ns, ObjectParameterizedAction a) {


        List<StateTransitionProb> tps = new ArrayList<StateTransitionProb>();


        CleanupBlockL1 block = ns.touchBlock(ns.blockInd(a.getObjectParameters()[0]));
        String curBlockRegion = block.inRegion;

        ObjectInstance region = ns.object(a.getObjectParameters()[1]);
        if(region.className().equals(CleanupDomain.CLASS_ROOM)){

            CleanupAgentL1 agent = ns.touchAgent();
            block.inRegion  =  a.getObjectParameters()[1];
            agent.inRegion =  curBlockRegion;
            tps.add(new StateTransitionProb(ns, 1.));
        }
        else{ //door

            //ObjectInstance agent = sp.getFirstObjectOfClass(CleanupWorld.CLASS_AGENT);


            if(!((CleanupDoorL1)region).canBeLocked){
                //always open
                block.inRegion = a.getObjectParameters()[1];
                tps.add(new StateTransitionProb(ns, 1.));
            }
            else{
                int lockVal = ((CleanupDoorL1)region).locked;
                if(lockVal == 1){
                    //unlocked
                    block.inRegion =  a.getObjectParameters()[1];
                    tps.add(new StateTransitionProb(ns, 1.));
                }
                else if(lockVal == 2){
                    //locked
                    tps.add(new StateTransitionProb(ns, 1.)); //no op
                }
                else if(lockVal == 0){

                    //then could go either way
                    CleanupL1State ns2 = ((CleanupL1State)ns).copy();

                    //unlock case
                    block.inRegion = a.getObjectParameters()[1];
                    CleanupDoorL1 regionNs = ns.touchDoor(ns.doorInd(region.name()));
                    regionNs.locked = 1;
//                    region.setValue(CleanupWorld.ATT_LOCKED, 1);
                    tps.add(new StateTransitionProb(ns, 1.-lockedProb));

                    //lock case
                    CleanupDoorL1 region2 = ns2.touchDoor(ns2.doorInd(region.name()));
                    region2.locked  = 2;
//                    ns2.object(region.name()).set(CleanupWorld.ATT_LOCKED, 2);
                    tps.add(new StateTransitionProb(ns2, lockedProb));

                }
            }

        }

        return tps;
    }

    private List<StateTransitionProb> goToRegion(CleanupL1State ns, ObjectParameterizedAction a) {



        List<StateTransitionProb> tps = new ArrayList<StateTransitionProb>();

        ObjectInstance region = (ObjectInstance) ns.object(a.getObjectParameters()[0]);
        if(region.className().equals(CleanupDomain.CLASS_ROOM)){
            CleanupAgentL1 agent = ns.touchAgent();
            agent.inRegion = a.getObjectParameters()[0];
            tps.add(new StateTransitionProb(ns, 1.));
        }
        else{ //door

            CleanupAgentL1 agent = ns.touchAgent();
            if(!((CleanupDoorL1)region).canBeLocked){
                //always open
                agent.inRegion = a.getObjectParameters()[0];
                tps.add(new StateTransitionProb(ns, 1.));
            }
            else{
                int lockVal = ((CleanupDoorL1)region).locked;
                if(lockVal == 1){
                    //unlocked
                    agent.inRegion =  a.getObjectParameters()[0];
                    tps.add(new StateTransitionProb(ns, 1.));
                }
                else if(lockVal == 2){
                    //locked
                    tps.add(new StateTransitionProb(ns, 1.)); //no op
                }
                else if(lockVal == 0){

                    //then could go either way
                    CleanupL1State sp2 = ns.copy();

                    //unlock case
                    agent.inRegion = a.getObjectParameters()[0];
                    CleanupDoorL1 regionNs = ns.touchDoor(ns.doorInd(region.name()));
                    regionNs.locked = 1;
//                    region.setValue(CleanupWorld.ATT_LOCKED, 1);
                    tps.add(new StateTransitionProb(ns, 1.-lockedProb));

                    //lock case
                    CleanupDoorL1 regionSp2 = sp2.touchDoor(sp2.doorInd(region.name()));
                    regionSp2.locked = 2;
                    tps.add(new StateTransitionProb(sp2, lockedProb));


                }
            }

        }

        return tps;

    }

    @Override
    public State sample(State s, Action a) {
        List<StateTransitionProb> stpList = this.stateTransitions(s,a);
        double roll = rand.nextDouble();
        double curSum = 0.;
        for(int i = 0; i < stpList.size(); i++){
            curSum += stpList.get(i).p;
            if(roll < curSum){
                return stpList.get(i).s;
            }
        }
        throw new RuntimeException("Probabilities don't sum to 1.0: " + curSum);
    }
}
