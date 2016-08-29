package amdp.cleanupamdpdomains.cleanuplevel2.state;

import amdp.cleanup.CleanupDomain;
import amdp.cleanup.state.*;
import amdp.cleanupamdpdomains.cleanuplevel1.state.CleanupAgentL1;
import amdp.cleanupamdpdomains.cleanuplevel1.state.CleanupBlockL1;
import amdp.cleanupamdpdomains.cleanuplevel1.state.CleanupL1State;
import amdp.cleanupamdpdomains.cleanuplevel1.state.CleanupRoomL1;
import burlap.mdp.auxiliary.StateMapping;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.oo.OOSADomain;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ngopalan on 8/28/16.
 */
public class CleanupL2StateMapper implements StateMapping{

    public CleanupL2StateMapper() {
    }

    @Override
    public State mapState(State sIn) {

        CleanupL1State s = (CleanupL1State)sIn;



        //set agent position
        //first try room
        CleanupAgentL1 agent = s.agent;

        CleanupAgentL2 agentL2 = new CleanupAgentL2(agent.name(),agent.inRegion);


        List<CleanupRoomL1> rooms = s.rooms;
        List<CleanupRoomL2> aarooms = new ArrayList<CleanupRoomL2>();
        for(CleanupRoomL1 r : rooms){
            CleanupRoomL2 rL2 = new CleanupRoomL2(r.name(), r.colour);
            aarooms.add(rL2);
        }

        List<CleanupBlockL1> blocks = s.blocks;
        List<CleanupBlockL2> aablocks = new ArrayList<CleanupBlockL2>();
        for(CleanupBlockL1 b : blocks){
            CleanupBlockL2 ab  = new CleanupBlockL2(b.name, b.shape, b.colour, b.inRegion);
            aablocks.add(ab);
        }

        return new CleanupL2State(agentL2,aablocks,aarooms);
    }


}
