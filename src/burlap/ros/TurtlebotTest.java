package burlap.ros;

import burlap.mdp.core.action.UniversalActionType;
import burlap.mdp.core.state.NullState;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.SADomain;
import burlap.ros.RosEnvironment;
import burlap.ros.actionpub.CentralizedPeriodicActionPub;
import burlap.ros.actionpub.RepeatingActionPublisher;
import burlap.ros.posestamped.Position;
import burlap.shell.EnvironmentShell;
import com.fasterxml.jackson.databind.JsonNode;
import ros.msgs.geometry_msgs.Twist;
import ros.msgs.geometry_msgs.Vector3;
import ros.tools.MessageUnpacker;

/**
 * Created by ngopalan on 9/8/16.
 */
public class TurtlebotTest {

    public static void main(String [] args) {

        //create a new domain with no state representation
        SADomain domain = new SADomain();

        //create action specification
        domain.addActionTypes(
                new UniversalActionType("forward"),
                new UniversalActionType("backward"),
                new UniversalActionType("rotate"),
                new UniversalActionType("rotate_ccw"));

        //setup ROS information
        String uri = "ws://192.168.160.162:9090";
        String stateTopic = "/state_turtlebot"; //we won't need this in this example, so set it to anything
        String stateMsg = "geometry_msgs/Vector3";//"std_msgs/String"; //we won't need this in this example, so set it to anything
        String actionTopic = "/mobile_base/commands/velocity"; //set this to the appropriate topic for your robot!
        String actionMsg = "geometry_msgs/Twist";


        //define the relevant twist messages that we'll use for our actions
        Twist fTwist = new Twist(new Vector3(0.1, 0, 0.), new Vector3()); //forward
        Twist bTwist = new Twist(new Vector3(-0.1, 0, 0.), new Vector3()); //backward
        Twist rTwist = new Twist(new Vector3(), new Vector3(0, 0, -0.5)); //clockwise rotate
        Twist rccwTwist = new Twist(new Vector3(), new Vector3(0, 0, 0.5)); //counter-clockwise rotate

        //create environment
        RosEnvironment env = new RosEnvironment(domain, uri, stateTopic, stateMsg) {
            @Override
            public State unpackStateFromMsg(JsonNode data, String stringRep) {
                MessageUnpacker<Position> unpacker = new MessageUnpacker<Position>(Position.class);
                return unpacker.unpackRosMessage(data);
            }
        };

        int period = 500; //publish every 500 milliseconds...
        int nPublishes = 10; //...for 5 times for each action execution...
        boolean sync = true; //...and use synchronized action execution
        env.setActionPublisher("forward", new RepeatingActionPublisher(actionTopic, actionMsg, env.getRosBridge(), fTwist, period, nPublishes, sync));
        env.setActionPublisher("backward", new RepeatingActionPublisher(actionTopic, actionMsg, env.getRosBridge(), bTwist, period, nPublishes, sync));
        env.setActionPublisher("rotate", new RepeatingActionPublisher(actionTopic, actionMsg, env.getRosBridge(), rTwist, period, nPublishes, sync));
        env.setActionPublisher("rotate_ccw", new RepeatingActionPublisher(actionTopic, actionMsg, env.getRosBridge(), rccwTwist, period, nPublishes, sync));
        //TODO: use a centralized periodic publisher!!!
//        env.setActionPublisher("rotate_ccw", new CentralizedPeriodicActionPub(actionTopic, actionMsg, env.getRosBridge(), rccwTwist, period, nPublishes, sync));

        //force the environment state to a null state so we don't have to setup a burlap_state topic on ROS
//        env.overrideFirstReceivedState(NullState.instance);


        //create a terminal controlled explorer to run on our environment
        //so that we can control the robot with the keyboard
        EnvironmentShell shell = new EnvironmentShell(domain, env);
        shell.start();

    }
}
