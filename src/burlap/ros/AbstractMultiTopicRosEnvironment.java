package burlap.ros;

import burlap.debugtools.DPrint;
import burlap.mdp.auxiliary.common.NullTermination;
import burlap.mdp.core.TerminalFunction;
import burlap.mdp.core.action.Action;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.SADomain;
import burlap.mdp.singleagent.common.NullRewardFunction;
import burlap.mdp.singleagent.model.RewardFunction;
import ros.RosBridge;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ngopalan on 10/6/16.
 */
public abstract class AbstractMultiTopicRosEnvironment extends AbstractRosEnvironment{


    /**
     * The BURLAP {@link SADomain} into which states will be parsed
     */
    protected SADomain domain;

    // Different RosListeners listening to individual topics to create state from
    protected List<RosDistributedListenDelegate> rosListenDelegates = new ArrayList<RosDistributedListenDelegate>();

//    State currentState;
    protected List<State> partialStates = new ArrayList<State>();
    protected boolean receivedFirstState = false;
    protected State curState;

    /**
     * The optional {@link RewardFunction} used to generate reward signals
     */
    protected RewardFunction rf = new NullRewardFunction();

    /**
     * The optional {@link TerminalFunction} used to specify terminal states of the environment
     */
    protected TerminalFunction tf = new NullTermination();


    /**
     * Debug flag indicating whether states should be printed to the terminal as they are received. Default value is false.
     */
    protected boolean printStateAsReceived = false;


    /**
     * The debug code used for debug prints.
     */
    protected int debugCode = 435687;

    public AbstractMultiTopicRosEnvironment(RosBridge rosBridge) {
        super(rosBridge);
    }

    public AbstractMultiTopicRosEnvironment(String rosBridgeURI) {
        super(rosBridgeURI);
    }
    @Override
    protected double getMostRecentRewardSignal(State state, Action action, State state1) {
        return 0;
    }

    @Override
    protected void handleEnterTerminalState() {

    }

    public void addListener(RosDistributedListenDelegate listener){
        this.rosListenDelegates.add(listener);
    }
    @Override
    public State currentObservation() {
        this.blockUntilStateReceived();
        this.createSingleState();
        try {
            Thread.sleep(20);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return this.curState.copy();
    }

    /**
     * Returns the domain of this environment.
     * @return the domain of this environment.
     */
    public SADomain getDomain() {
        return domain;
    }



    @Override
    public boolean isInTerminalState() {
        return false;
    }

    /**
     * create a single state by combining state information from individual delegates
     */
    public abstract void createSingleState();

    public void overrideFirstReceivedState(State s){
        this.curState = s;
        this.receivedFirstState = true;
        synchronized (this){
            this.notifyAll();
        }
    }

    public synchronized void blockUntilStateReceived(){

        checkIfAllStatesReceived();

        if(!this.receivedFirstState) {
            DPrint.cl(this.debugCode, "Blocking until state received.");
        }
        boolean oldReceived = this.receivedFirstState;
        while(!this.receivedFirstState){
//            System.out.println("within wait loop!");
            try {
//                this.wait();
                Thread.sleep(1000);
                checkIfAllStatesReceived();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        if(!oldReceived) {
            DPrint.cl(this.debugCode, "State received");
        }

    }

    private void checkIfAllStatesReceived() {
//        System.out.println("db0" + receivedFirstState);
        if(!receivedFirstState) {
            boolean returnBool = true;
            for (RosDistributedListenDelegate rl : rosListenDelegates) {
                synchronized (rl) {
                    returnBool = returnBool && rl.receivedFirstState();
//                    System.out.println("was here" + rl.receivedFirstState);
                }
            }
            receivedFirstState = returnBool;
        }
//        System.out.println("db1" + receivedFirstState);
    }

}
