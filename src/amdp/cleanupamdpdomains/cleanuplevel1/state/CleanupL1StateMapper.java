package amdp.cleanupamdpdomains.cleanuplevel1.state;

import amdp.cleanup.CleanupDomain;
import amdp.cleanup.state.*;
import amdp.cleanupamdpdomains.cleanuplevel1.state.*;
import burlap.mdp.auxiliary.StateMapping;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.oo.OOSADomain;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ngopalan on 8/28/16.
 */
public class CleanupL1StateMapper implements StateMapping{

    public CleanupL1StateMapper() {
    }

    @Override
    public State mapState(State sIn) {

        CleanupState s = (CleanupState)sIn;



        //set agent position
        //first try room
        CleanupAgent agent = s.agent;
        int ax = agent.x;
        int ay = agent.y;
        CleanupRoom inRoom = CleanupDomain.roomContainingPoint(s, ax, ay);

        String inRegion;
        if(inRoom != null){
            inRegion = inRoom.name();
        }
        else{
            CleanupDoor inDoor = CleanupDomain.doorContainingPoint(s, ax, ay);
            inRegion = inDoor.name();
        }


        CleanupAgentL1 aagent = new CleanupAgentL1(agent.name(), inRegion);



        List<CleanupRoom> rooms = s.rooms;
        List<CleanupRoomL1> aarooms = new ArrayList<CleanupRoomL1>();
        for(CleanupRoom r : rooms){
            CleanupRoomL1 rL1 = new CleanupRoomL1(r.name(), r.colour, new ArrayList<String>());
            aarooms.add(rL1);
        }

        List<CleanupDoor> doors = s.doors;
        List<CleanupDoorL1> aadoors = new ArrayList<CleanupDoorL1>();

        for(CleanupDoor d : doors){
            CleanupDoorL1 ad = new CleanupDoorL1(d.name(),d.locked,d.canBeLocked,new ArrayList<String>());

            aadoors.add(ad);
        }

        List<CleanupBlock> blocks = s.blocks;
        List<CleanupBlockL1> aablocks = new ArrayList<CleanupBlockL1>();
        for(CleanupBlock b : blocks){

            int bx = b.x;
            int by = b.y;
            CleanupRoom blockInRoom = CleanupDomain.roomContainingPoint(s, bx, by);

            String blockInRegion;
            if(blockInRoom != null){
                blockInRegion = blockInRoom.name();
            }
            else{
                CleanupDoor blockInDoor = CleanupDomain.doorContainingPoint(s, bx, by);
                blockInRegion = blockInDoor.name();
            }

            CleanupBlockL1 ab  = new CleanupBlockL1(b.name, b.shape, b.colour, blockInRegion);
            aablocks.add(ab);
        }




        //now set room and door connections
        for(CleanupRoom r : rooms){

            int rt = r.top;
            int rl = r.left;
            int rb = r.bottom;
            int rr = r.right;

            CleanupRoomL1 rL1 = null;
            for(CleanupRoomL1 rL1Temp : aarooms){

                if(rL1Temp.name().equals(r.name())){
                    rL1 = rL1Temp;
                    break;
                }
            }


//            ObjectInstance ar = as.getObject(r.getName());

            for(CleanupDoor d : doors){

                int dt = d.top;
                int dl = d.left;
                int db = d.bottom;
                int dr = d.right;

                if(rectanglesIntersect(rt, rl, rb, rr, dt, dl, db, dr)){
                    CleanupDoorL1 dL1 = null;
                    for(CleanupDoorL1 dL1Temp : aadoors){

                        if(dL1Temp.name().equals(d.name())){
                            dL1 = dL1Temp;
                            dL1.connectedRegions.add(rL1.name());
                            break;
                        }
                    }



//                    ObjectInstance ad = as.getObject(d.getName());
                    rL1.connectedRegions.add(dL1.name());
//                    ar.addRelationalTarget(ATT_CONNECTED, ad.getName());
//                    ad.addRelationalTarget(ATT_CONNECTED, ar.getName());
                }

            }

        }




        return new CleanupL1State(aagent,aablocks,aadoors,aarooms);
    }

    protected static boolean rectanglesIntersect(int t1, int l1, int b1, int r1, int t2, int l2, int b2, int r2){

        return t2 >= b1 && b2 <= t1 && r2 >= l1 && l2 <= r1;

    }
}
