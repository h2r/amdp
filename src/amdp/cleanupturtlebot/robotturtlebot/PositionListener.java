package amdp.cleanupturtlebot.robotturtlebot;

import burlap.mdp.core.state.State;
import burlap.ros.RosDistributedListenDelegate;
import burlap.ros.posestamped.Position;
import com.fasterxml.jackson.databind.JsonNode;
import ros.tools.MessageUnpacker;

/**
 * Created by ngopalan on 11/11/16.
 */
public class PositionListener extends RosDistributedListenDelegate {




    public boolean printStateAsReceived = false;



    @Override
    public void receive(JsonNode data, String stringRep) {


        State s = unpackStateFromMsg(data, stringRep);
        this.currentState = this.onStateReceive(s);

        if(!this.receivedFirstState){
            synchronized (this){
                this.receivedFirstState = true;
                this.notifyAll();
            }
        }


    }

    public State unpackStateFromMsg(JsonNode data, String stringRep) {
        MessageUnpacker<Position> unpacker = new MessageUnpacker<Position>(Position.class);
        return unpacker.unpackRosMessage(data);
    }

    protected State onStateReceive(State s){
        if(printStateAsReceived) {
            System.out.println(s.toString() + "\n-------------------------");
        }
        return s;
    }
}

