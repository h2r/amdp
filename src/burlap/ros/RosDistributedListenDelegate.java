package burlap.ros;

//import com.fasterxml.jackson.databind.JsonNode;
import burlap.debugtools.DPrint;
import burlap.mdp.core.state.State;
import ros.RosListenDelegate;

/**
 * Created by ngopalan on 10/14/16.
 */

public abstract class RosDistributedListenDelegate implements RosListenDelegate{



    protected boolean receivedFirstState = false;


    protected State currentState = null;

    protected int debugCode = 43059832;

    public boolean receivedFirstState(){
        return this.receivedFirstState;
    }

    public State currentState() {
        return currentState;
    }

    /**
     * A method you can call that forces the calling thread to wait until the first state from ROS has been received.
     */
    public synchronized void blockUntilStateReceived(){
        if(!this.receivedFirstState) {
            DPrint.cl(this.debugCode, "Blocking until state received.");
        }
        boolean oldReceived = this.receivedFirstState;
        while(!this.receivedFirstState){
            try {
                this.wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        if(!oldReceived) {
            DPrint.cl(this.debugCode, "State received");
        }

    }

}
