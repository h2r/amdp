package amdp.cleanupturtlebot.robotturtlebot;

import amdp.cleanupturtlebot.cleanupcontinuous.state.CleanupContinuousAgent;
import amdp.cleanupturtlebot.cleanupcontinuous.state.CleanupContinuousBlock;
import amdp.cleanupturtlebot.cleanupcontinuous.state.CleanupContinuousState;
import burlap.ros.AbstractMultiTopicRosEnvironment;
import burlap.ros.RosDistributedListenDelegate;
import burlap.ros.posestamped.Position;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ngopalan on 11/11/16.
 */
public class TurtleBotEnvironment extends AbstractMultiTopicRosEnvironment {

    RosDistributedListenDelegate TurtleBotListener = new PositionListener();
    RosDistributedListenDelegate BlockListener = new PositionListener();
    CleanupContinuousState stateForRooms;



    public TurtleBotEnvironment(String rosBridgeURI, CleanupContinuousState startState) {
//        RosBridge rosBridge = new RosBridge(rosBridgeURI);
        super(rosBridgeURI);
        rosBridge.subscribe("/state_turtlebot", "geometry_msgs/Vector3" , TurtleBotListener);
        rosBridge.subscribe("/state_block", "geometry_msgs/Vector3" , BlockListener);
        this.addListener(TurtleBotListener);
        this.addListener(BlockListener);
        stateForRooms = startState;
    }


    @Override
    public void createSingleState() {
        Position blockPosition = (Position) BlockListener.currentState();
        Position turtlebotPosition = (Position) TurtleBotListener.currentState();
//        System.out.println(turtlebotPosition.toString());
//        System.out.println(blockPosition.toString());
        CleanupContinuousAgent agentOld = stateForRooms.agent;
        CleanupContinuousBlock blockOld = stateForRooms.blocks.get(0);
        CleanupContinuousAgent agent = new CleanupContinuousAgent(agentOld.name(),
                turtlebotPosition.x * 3.28084, turtlebotPosition.y * 3.28084, turtlebotPosition.z,
                agentOld.length, agentOld.width);
        CleanupContinuousBlock block = new CleanupContinuousBlock(blockOld.name(),
                blockPosition.x * 3.28084, blockPosition.y * 3.28084, blockOld.shape,
                blockOld.colour);
        List<CleanupContinuousBlock > blocks = new ArrayList<CleanupContinuousBlock>();
        blocks.add(block);
        CleanupContinuousState s = new CleanupContinuousState(agent, blocks, stateForRooms.doors, stateForRooms.rooms);
        this.curState = s.copy();
    }
}
