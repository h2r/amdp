package amdp.cleanupturtlebot.cleanupl0discrete.state;

import amdp.cleanup.state.*;
import amdp.cleanupturtlebot.cleanupcontinuous.CleanupContinuousDomain;
import amdp.cleanupturtlebot.cleanupcontinuous.state.*;
import burlap.mdp.auxiliary.StateMapping;
import burlap.mdp.core.state.State;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ngopalan on 9/12/16.
 */
public class CleanupContinuousToDiscreteStateMapper implements StateMapping{
    @Override
    public State mapState(State s) {
        CleanupContinuousState sIn = (CleanupContinuousState) s;

        List<CleanupContinuousDoor> doorsC = sIn.doors;
        List<CleanupContinuousBlock> blocksC = sIn.blocks;
        List<CleanupContinuousRoom> roomsC = sIn.rooms;
        CleanupContinuousAgent agentC = sIn.agent;



        List<CleanupDoor> doorsL0 = new ArrayList<CleanupDoor>();
        List<CleanupBlock> blocksL0 = new ArrayList<CleanupBlock>();
        List<CleanupRoom> roomsL0 = new ArrayList<CleanupRoom>();
        for(CleanupContinuousDoor d:doorsC){
            doorsL0.add(new CleanupDoor(d.name(),d.locked,(int)d.top,(int)d.left,(int)d.bottom,(int)d.right,d.canBeLocked));
        }

        for(CleanupContinuousRoom r:roomsC){
            roomsL0.add(new CleanupRoom(r.name(),(int)r.top,(int)r.left,(int)r.bottom,(int)r.right, r.colour));
        }

        for(CleanupContinuousBlock b:blocksC){
            blocksL0.add(new CleanupBlock(b.name(),(int)b.x,(int)b.y,b.shape,b.colour));
        }
        double dir = agentC.direction;
        String direction = "notCardinal";
        if(Math.abs(CleanupContinuousDomain.angularDistance(dir,0))<CleanupContinuousDomain.rangeForDirectionChecks){
            direction = "north";
        }
        else if(Math.abs(CleanupContinuousDomain.angularDistance(dir,Math.PI/2))<CleanupContinuousDomain.rangeForDirectionChecks){
            direction = "west";
        }
        else if(Math.abs(CleanupContinuousDomain.angularDistance(dir,Math.PI))<CleanupContinuousDomain.rangeForDirectionChecks){
            direction = "south";
        }
        else if(Math.abs(CleanupContinuousDomain.angularDistance(dir,-Math.PI/2))<CleanupContinuousDomain.rangeForDirectionChecks){
            direction = "east";
        }

        CleanupAgent agentL0 = new CleanupAgent(agentC.name(),(int)agentC.x,(int)agentC.y,direction);//,(int)agentC.length,(int)agentC.width);

        return new CleanupState(agentL0,blocksL0,doorsL0,roomsL0);
    }
}
