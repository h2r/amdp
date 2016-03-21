package amdp.cleanupdomain;

import amdp.cleanupdomain.CleanupL1AMDPDomain.GroundedPropSC;
import amdp.framework.AMDPAgent;
import amdp.framework.AMDPPolicyGenerator;
import amdp.hardcoded.cleanup.FixedDoorCleanupEnv;
import amdp.tools.VisualEnvStackObserver;
import burlap.behavior.policy.GreedyQPolicy;
import burlap.behavior.policy.Policy;
import burlap.behavior.singleagent.EpisodeAnalysis;
import burlap.behavior.singleagent.auxiliary.EpisodeSequenceVisualizer;
import burlap.behavior.singleagent.planning.Planner;
import burlap.behavior.singleagent.planning.deterministic.DDPlannerPolicy;
import burlap.behavior.singleagent.planning.deterministic.uninformed.bfs.BFS;
import burlap.behavior.singleagent.planning.stochastic.rtdp.BoundedRTDP;
import burlap.behavior.valuefunction.QFunction;
import burlap.behavior.valuefunction.ValueFunctionInitialization;
import burlap.domain.singleagent.cleanup.CleanupVisualizer;
import burlap.domain.singleagent.cleanup.CleanupWorld;
import burlap.oomdp.auxiliary.common.GoalConditionTF;
import burlap.oomdp.auxiliary.stateconditiontest.StateConditionTest;
import burlap.oomdp.auxiliary.stateconditiontest.TFGoalCondition;
import burlap.oomdp.core.*;
import burlap.oomdp.core.objects.ObjectInstance;
import burlap.oomdp.core.states.State;
import burlap.oomdp.singleagent.GroundedAction;
import burlap.oomdp.singleagent.RewardFunction;
import burlap.oomdp.singleagent.common.GoalBasedRF;
import burlap.oomdp.singleagent.environment.EnvironmentServer;
import burlap.oomdp.statehashing.SimpleHashableStateFactory;
import burlap.oomdp.visualizer.Visualizer;
import burlap.oomdp.singleagent.explorer.VisualExplorer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

// TODO: Add disjunction for alternative solutions
// TODO: Remove unneeded items / rooms for curriculum (causes crash?)

/**
 * All “lessons” assume following initial state (from sample CleanupDomainDriver):
 * Green, blue, red, and yellow room
 * Green room contains purple bag and red basket
 * Red room contains blue chair
 * Red room connected to blue room via open door; and yellow room via locked door
 * Green room connected to blue and yellow room via open doors
 * All exact placements (i.e., location in room) are considered arbitrary unless otherwise specified.
 *
 * The curriculum below outlines the initial location of the agent in the above state, the state that provides a positive reward, and the goal of the lesson.
 *
 * Sequence follows B.F. Skinner’s “shaping” approach of progressively more specific tasks, where initial reward / goals are vague representations of intended behavior. Successive “lessons” reward only more precise approximations of intended behavior.
 *
 * “Understanding” may be simply learning a policy for an option or learning transition for propositional functions. Exact method of “understanding” to be determined.
 *
 * SEARCH FOR "NEWLESSON" WHEN ADDING A LESSON
 *
 * TODO: Add disjunction junction, what's your function
 * Lesson 1:
 * Init: Green Room
 * Reward: Enter any door
 * Goal: Understand transitioning to an open door
 *
 * Lesson 2:
 * Init: Green Room
 * Reward: Enter door to blue room
 * Goal: Understand transitioning to specific door
 *
 * Lesson 3:
 * Init: Green Room
 * Reward: Enter blue room
 * Goal: Understand transitioning to an adjacent room
 *
 * Lesson 4:
 * Init: Green Room
 * Reward: Enter red room
 * Goal: Understand transitioning to non-adjacent room
 *
 * Lesson 5:
 * Init: Red Room, by locked door to yellow room
 * Reward: Enter yellow room
 * Goal: Understand avoiding locked door to enter adjacent room
 *
 * Lesson 6:
 * Init: Red Room, by blue chair (does this matter?), blue chair next to door to blue room
 * Reward: Blue chair in door
 * Goal: Understand pushing object into a doorway
 *
 * Lesson 7:
 * Init: Red Room, by blue chair, blue chair away from door to blue room
 * Reward: Blue chair in blue room
 * Goal: Understand pushing adjacent object into adjacent room
 *
 * Lesson 8:
 * Init: Red Room, by blue chair, blue chair against wall opposite blue room door
 * Reward: Blue chair in blue room
 * Goal: Understand pulling adjacent object away from wall
 *
 * Lesson 9:
 * Init: Red Room, by blue chair, blue chair next to door to blue room
 * Reward: Blue chair in green room
 * Goal: Understand pushing adjacent object into non-adjacent room
 *
 * TODO: Fix wonky bad behavior
 * Lesson 10:
 * Init: Red room, purple bag in blue-green door
 * Reward Blue chair in green room
 * Goal: Understand pushing adjacent object into non-adjacent room with object in way
 * (might need one prior with adjacent room?)
 *
 * Lesson 11:
 * Init: Red Room
 * Reward: Red basket in yellow room
 * Goal: Understand pushing non-adjacent object into non-adjacent room, avoiding locked door
 *
 * Lesson 12:
 * Init: Red room, blue chair in green-yellow door
 * Reward: Purple bag in yellow room
 * Goal: Understand pushing non-adjacent object into non-adjacent room with object in way, avoiding locked door
 *
 * Lesson 13:
 * init: Red room, Purple bag in Blue-Green door followed by red basket
 * Reward: Blue chair in Green room
 * Goal: Understanding removing two consecutive obstacles from the way one in doorway one
 * followed by another (potentially more complex tasks?)
**/



public class CleanupDomainDriverCurriculum {


	static String imagePathNakul = "amdp/data/resources/robotImages";
	static String imagePathJames = "data/resources/robotImages";
	static String imagePath = imagePathJames;

	static double lockProb = 0.5;
	public static void main(String[] args) {
		int run = 0;

		if(args.length > 0) {
			run = Integer.parseInt(args[0]);
		}

		//designWorld();
		generateBehavior(run);
		//debug();

	}

	public static void generateBehavior(int run){

		CleanupWorld cw = new CleanupWorld();
		cw.includeDirectionAttribute(true);
		cw.includePullAction(true);
		cw.includeWallPF_s(true);
		cw.includeLockableDoors(true);
		cw.setLockProbability(lockProb);

		Domain domain = cw.generateDomain();

		CleanupL1AMDPDomain a1dgen = new CleanupL1AMDPDomain(domain);
		a1dgen.setLockableDoors(true);
		a1dgen.setLockProb(lockProb);
		Domain adomain = a1dgen.generateDomain();

		CleanupL2AMDPDomain a2dgen = new CleanupL2AMDPDomain();
		Domain a2domain = a2dgen.generateDomain();

		State s = getState(domain, run);

		List<String> goalBlocks = null;
		List<String> goalRooms = null;
		// NEWLESSON
		switch (run) {
			case 1:
				// TODO: Make transition to disjunction of doors
				goalBlocks = Arrays.asList("agent");
				goalRooms = Arrays.asList("door2");
				break;
			case 2:
				goalBlocks = Arrays.asList("agent");
				goalRooms = Arrays.asList("door2");
				break;
			case 3:
				goalBlocks = Arrays.asList("agent");
				goalRooms = Arrays.asList("room1");
				break;
			case 4:
				goalBlocks = Arrays.asList("agent");
				goalRooms = Arrays.asList("room0");
				break;
			case 5:
				goalBlocks = Arrays.asList("agent");
				goalRooms = Arrays.asList("room3");
				break;
			case 6:
				goalBlocks = Arrays.asList("block0");
				goalRooms = Arrays.asList("door1");
				break;
			case 7:
			case 8:
				goalBlocks = Arrays.asList("block0");
				goalRooms = Arrays.asList("room1");
				break;
			case 9:
			case 10:
				goalBlocks = Arrays.asList("block0");
				goalRooms = Arrays.asList("room2");
				break;
			case 11:
				goalBlocks = Arrays.asList("block1");
				goalRooms = Arrays.asList("room3");
				break;
			case 12:
				goalBlocks = Arrays.asList("block2");
				goalRooms = Arrays.asList("room3");
				break;
			case 13:
				goalBlocks = Arrays.asList("block0");
				goalRooms = Arrays.asList("room2");
				break;
			default:
				goalBlocks = Arrays.asList("block0");
				goalRooms = Arrays.asList("room3");
		}

		StateConditionTest l0sc = null;
		// NEWLESSON
		switch (run) {
			case 1:
			case 2:
				l0sc = new L0Goal(goalBlocks, goalRooms, domain.getPropFunction(CleanupWorld.PF_AGENT_IN_DOOR));
				break;
			case 3:
			case 4:
			case 5:
				l0sc = new L0Goal(goalBlocks, goalRooms, domain.getPropFunction(CleanupWorld.PF_AGENT_IN_ROOM));
				break;
			case 6:
				l0sc = new L0Goal(goalBlocks, goalRooms, domain.getPropFunction(CleanupWorld.PF_BLOCK_IN_ROOM));
				break;
			default:
				l0sc = new L0Goal(goalBlocks, goalRooms, domain.getPropFunction(CleanupWorld.PF_BLOCK_IN_ROOM));
		}
		GoalBasedRF l0rf = new GoalBasedRF(l0sc, 1., 0.);
		GoalConditionTF l0tf = new GoalConditionTF(l0sc);

		StateConditionTest l2sc = new L2Goal(goalBlocks, goalRooms);
		GoalBasedRF l2rf = new GoalBasedRF(l2sc, 1., 0.);
		GoalConditionTF l2tf = new GoalConditionTF(l2sc);

		FixedDoorCleanupEnv env = new FixedDoorCleanupEnv(domain, l0rf, l0tf, s);
		env.addLockedDoor("door0");

		List<Domain> domainList = new ArrayList<Domain>();
		domainList.add(0,domain);
		domainList.add(1,adomain);
		domainList.add(2,a2domain);

		List<AMDPPolicyGenerator> pgList = new ArrayList<AMDPPolicyGenerator>();
		pgList.add(0,new l0PolicyGenerator(domain));
		pgList.add(1,new l1PolicyGenerator(adomain));
		pgList.add(2,new l2PolicyGenerator(a2domain));


		AMDPAgent agent = new AMDPAgent(domainList, pgList, l2rf, l2tf);

		Visualizer v = CleanupVisualizer.getVisualizer(imagePath);
		if(run > 0) {
			VisualEnvStackObserver so = new VisualEnvStackObserver(CleanupVisualizer.getVisualizer(imagePath), agent, 1000);
			agent.setOnlineStackObserver(so);
			so.updateState(env.getCurrentObservation());
			EnvironmentServer envServer = new EnvironmentServer(env, so);

			EpisodeAnalysis ea = agent.actUntilTermination(envServer, 500);
			new EpisodeSequenceVisualizer(v, domain, Arrays.asList(ea));
		} else {
			VisualExplorer exp = new VisualExplorer(domain, v, s);

			exp.addKeyAction("w", CleanupWorld.ACTION_NORTH);
			exp.addKeyAction("s", CleanupWorld.ACTION_SOUTH);
			exp.addKeyAction("d", CleanupWorld.ACTION_EAST);
			exp.addKeyAction("a", CleanupWorld.ACTION_WEST);
			exp.addKeyAction("r", CleanupWorld.ACTION_PULL);

			exp.initGUI();
		}
		
	}

	public static class L0Goal implements StateConditionTest{

		List<String> blocks;
		List<String> rooms;
		PropositionalFunction pf;


		public L0Goal(List<String> blocks, List<String> rooms, PropositionalFunction pf) {
			this.blocks = blocks;
			this.rooms = rooms;
			this.pf = pf;
		}

		@Override
		public boolean satisfies(State s) {

			for(int i = 0; i < this.blocks.size(); i++){
				String b = this.blocks.get(i);
				String r = this.rooms.get(i);
				// TODO: Determine b or b + 0
				if( ! Character.isDigit(b.charAt(b.length()-1))) { b = b + 0; }
				GroundedProp gp = new GroundedProp(pf, new String[]{b, r});

				if(!gp.isTrue(s)){
					return false;
				}
			}

			return true;
		}

	}

	public static class L2Goal implements StateConditionTest{

		List<String> blocks;
		List<String> rooms;


		public L2Goal(List<String> blocks, List<String> rooms) {
			this.blocks = blocks;
			this.rooms = rooms;
		}

		@Override
		public boolean satisfies(State s) {

			for(int i = 0; i < this.blocks.size(); i++){
				ObjectInstance b = s.getObject(this.blocks.get(i));
				String r = this.rooms.get(i);

				if(!b.getAllRelationalTargets(CleanupL1AMDPDomain.ATT_IN_REGION).contains(r)){
					return false;
				}
			}

			return true;
		}
	}


	public static State getState(Domain domain, int lesson){


		int y1 = 3;
		int y2 = 7;
		int y3 = 12;

		int x1 = 4;
		int x2 = 8;
		int x3 = 12;

		State s = CleanupWorld.getCleanState(domain, 4, 4, 3);
		CleanupWorld.setRoom(s, 0, y2, x1, 0, x2, "red");
		CleanupWorld.setRoom(s, 1, y2, 0, y1, x1, "blue");
//		CleanupWorld.setRoom(s, 1, y2, 0, 0, x1, "blue");
		CleanupWorld.setRoom(s, 2, y3, 0, y2, x3, "green");
		CleanupWorld.setRoom(s, 3, y2, x2, 0, x3, "yellow");

		CleanupWorld.setDoor(s, 0, 1, x2, 1, x2);
		CleanupWorld.setDoor(s, 1, 5, x1, 5, x1);

		CleanupWorld.setDoor(s, 2, y2, 2, y2, 2);
		CleanupWorld.setDoor(s, 3, y2, 10, y2, 10);

		// NEWLESSON
		switch (lesson) {
			case 1:
			case 2:
			case 3:
			case 4:
				CleanupWorld.setAgent(s, 6, 9);
				break;
			// case 5: default
			case 6:
			case 7:
			case 8:
			case 9:
			case 10:
				CleanupWorld.setAgent(s, 6, 5);
				break;
			default:
				CleanupWorld.setAgent(s, 7, 1);
		}

		// NEWLESSON
		switch (lesson) {
			case 6:
			case 7:
			case 9:
				CleanupWorld.setBlock(s, 0, 5, 5, "chair", "blue");
				CleanupWorld.setBlock(s, 1, 6, 10, "basket", "red");
				CleanupWorld.setBlock(s, 2, 2, 10, "bag", "magenta");
				break;
			case 8:
				CleanupWorld.setBlock(s, 0, 7, 5, "chair", "blue");
				CleanupWorld.setBlock(s, 1, 6, 10, "basket", "red");
				CleanupWorld.setBlock(s, 2, 2, 10, "bag", "magenta");
				break;
			case 10:
				CleanupWorld.setBlock(s, 0, 5, 5, "chair", "blue");
				CleanupWorld.setBlock(s, 1, 6, 10, "basket", "red");
				CleanupWorld.setBlock(s, 2, 4, 5, "bag", "magenta");
				break;
			case 12:
				CleanupWorld.setBlock(s, 0, 2, 7, "chair", "blue");
				CleanupWorld.setBlock(s, 1, 6, 10, "basket", "red");
				CleanupWorld.setBlock(s, 2, 2, 10, "bag", "magenta");
				break;
			case 13:
				CleanupWorld.setBlock(s, 0, 5, 4, "chair", "blue");
				CleanupWorld.setBlock(s, 1, 2, 8, "basket", "red");
				CleanupWorld.setBlock(s, 2, 2, 7, "bag", "magenta");
				break;
			default:
				CleanupWorld.setBlock(s, 0, 5, 4, "chair", "blue");
				CleanupWorld.setBlock(s, 1, 6, 10, "basket", "red");
				CleanupWorld.setBlock(s, 2, 2, 10, "bag", "magenta");
		}

		return s;

	}


	public static class l2PolicyGenerator implements AMDPPolicyGenerator{

		private Domain l2;
		public l2PolicyGenerator(Domain l2In){
			l2 = l2In;
		}

		@Override
		public Policy generatePolicy(State s, RewardFunction rf, TerminalFunction tf) {
			BFS bfs = new BFS(l2, new TFGoalCondition(tf), new SimpleHashableStateFactory(false));
			bfs.toggleDebugPrinting(false);
			DDPlannerPolicy policy = new DDPlannerPolicy(bfs);
			return policy;
		}

	}

	public static class l1PolicyGenerator implements AMDPPolicyGenerator{
		private Domain l1;
		protected double discount = 0.99;
		public l1PolicyGenerator(Domain l1In){
			l1 = l1In;
		}
		@Override
		public Policy generatePolicy(State s, RewardFunction rf, TerminalFunction tf) {
			RewardFunction l1rf = rf;
			TerminalFunction l1tf = tf;
			//StateReachability.getReachableStates(l1State, (SADomain)l1, new SimpleHashableStateFactory(false));

			//ValueIteration vi = new ValueIteration(l1, l1rf, l1tf, this.discount, new SimpleHashableStateFactory(false), 0.01, 100);
			//return vi.planFromState(l1State);

			BoundedRTDP brtdp = new BoundedRTDP(l1, l1rf, l1tf, discount, new SimpleHashableStateFactory(false),
					new ValueFunctionInitialization.ConstantValueFunctionInitialization(0.),
					new ValueFunctionInitialization.ConstantValueFunctionInitialization(1.),
					0.001,
					1000);
			brtdp.setMaxRolloutDepth(20);
			brtdp.toggleDebugPrinting(false);
			brtdp.planFromState(s);

			return new GreedyReplan(brtdp);
		}


	}

	public static class l0PolicyGenerator implements AMDPPolicyGenerator{
		private Domain l0;
		protected double discount = 0.99;
		public l0PolicyGenerator(Domain l0In){
			l0 = l0In;
		}

		@Override
		public Policy generatePolicy(State s, RewardFunction rf, TerminalFunction tf) {
			//			RewardFunction l0rf = rf;
			//			TerminalFunction l0tf = tf;

			//			System.out.println(heuristic.value(s));

			//			ValueIteration vi = new ValueIteration(l0, l0rf, l0tf, this.discount, new SimpleHashableStateFactory(false), 0.1, 50);
			//			return vi.planFromState(s);


			//			SparseSampling sp = new SparseSampling(l0, rf, tf, this.discount, new SimpleHashableStateFactory(false), 13, -1);
			//						
			//			return sp.planFromState(s);


			ValueFunctionInitialization heuristic = getL0Heuristic(s, rf);
			BoundedRTDP brtd = new BoundedRTDP(l0, rf, tf, this.discount, new SimpleHashableStateFactory(false), new ValueFunctionInitialization.ConstantValueFunctionInitialization(0.), heuristic, 0.01, 500);
			brtd.setMaxRolloutDepth(50);
			brtd.toggleDebugPrinting(false);
			return brtd.planFromState(s);
		}

	}


	public static ValueFunctionInitialization getL0Heuristic(State s, RewardFunction rf){

		double discount = 0.99;
		// prop name if block -> block and room if
		GroundedPropSC rfCondition = (GroundedPropSC)((PullCostGoalRF)rf).getGoalCondition();
		String PFName = rfCondition.gp.pf.getName();
		String[] params = rfCondition.gp.params;
		if(PFName.equals(CleanupWorld.PF_AGENT_IN_ROOM)){
			return new AgentToRegionHeuristic(params[1], discount, lockProb);
		}
		else if(PFName.equals(CleanupWorld.PF_AGENT_IN_DOOR)){
			return new AgentToRegionHeuristic(params[1], discount, lockProb);
		}
		else if(PFName.equals(CleanupWorld.PF_BLOCK_IN_ROOM)){
			return new BlockToRegionHeuristic(params[0], params[1], discount, lockProb);
		}
		else if(PFName.equals(CleanupWorld.PF_BLOCK_IN_DOOR)){
			return new BlockToRegionHeuristic(params[0], params[1], discount, lockProb);
		}
		throw new RuntimeException("Unknown Reward Function with propositional function " + PFName + ". Cannot construct l0 heuristic.");
	}

	public static class AgentToRegionHeuristic implements ValueFunctionInitialization{

		String goalRegion;
		double discount;
		double lockProb;

		public AgentToRegionHeuristic(String goalRegion, double discount, double lockProb) {
			this.goalRegion = goalRegion;
			this.discount = discount;
			this.lockProb = lockProb;
		}

		@Override
		public double qValue(State s, AbstractGroundedAction a) {
			return value(s);
		}

		@Override
		public double value(State s) {

			int delta = 1;
			boolean freeRegion = true;
			ObjectInstance region = s.getObject(this.goalRegion);
			if(region.getClassName().equals(CleanupWorld.CLASS_DOOR)){
				delta = 0;
				if(region.getObjectClass().hasAttribute(CleanupWorld.ATT_LOCKED)){
					int lockedVal = region.getIntValForAttribute(CleanupWorld.ATT_LOCKED);
					if(lockedVal == 2){
						return 0.; //impossible to reach because locked
					}
					if(lockedVal == 0){
						freeRegion = false; //unknown is not free
					}
				}
			}


			//get the agent
			ObjectInstance agent = s.getFirstObjectOfClass(CleanupWorld.CLASS_AGENT);
			int ax = agent.getIntValForAttribute(CleanupWorld.ATT_X);
			int ay = agent.getIntValForAttribute(CleanupWorld.ATT_Y);


			int l = region.getIntValForAttribute(CleanupWorld.ATT_LEFT);
			int r = region.getIntValForAttribute(CleanupWorld.ATT_RIGHT);
			int b = region.getIntValForAttribute(CleanupWorld.ATT_BOTTOM);
			int t = region.getIntValForAttribute(CleanupWorld.ATT_TOP);

			int dist = toRegionManDistance(ax, ay, l, r, b, t, delta);

			double fullChanceV = Math.pow(discount, dist-1);
			double v = freeRegion ? fullChanceV : lockProb * fullChanceV + (1. - lockProb)*0;

			return v;
		}


	}

	public static class BlockToRegionHeuristic implements ValueFunctionInitialization{

		String blockName;
		String goalRegion;
		double discount;
		double lockProb;

		public BlockToRegionHeuristic(String blockName, String goalRegion, double discount, double lockProb) {
			this.blockName = blockName;
			this.goalRegion = goalRegion;
			this.discount = discount;
			this.lockProb = lockProb;
		}

		@Override
		public double qValue(State s, AbstractGroundedAction a) {
			return value(s);
		}

		@Override
		public double value(State s) {

			int delta = 1;
			boolean freeRegion = true;
			ObjectInstance region = s.getObject(this.goalRegion);
			if(region.getClassName().equals(CleanupWorld.CLASS_DOOR)){
				delta = 0;
				if(region.getObjectClass().hasAttribute(CleanupWorld.ATT_LOCKED)){
					int lockedVal = region.getIntValForAttribute(CleanupWorld.ATT_LOCKED);
					if(lockedVal == 2){
						return 0.; //impossible to reach because locked
					}
					if(lockedVal == 0){
						freeRegion = false; //unknown is not free
					}
				}
			}



			//get the agent
			ObjectInstance agent = s.getFirstObjectOfClass(CleanupWorld.CLASS_AGENT);
			int ax = agent.getIntValForAttribute(CleanupWorld.ATT_X);
			int ay = agent.getIntValForAttribute(CleanupWorld.ATT_Y);


			int l = region.getIntValForAttribute(CleanupWorld.ATT_LEFT);
			int r = region.getIntValForAttribute(CleanupWorld.ATT_RIGHT);
			int b = region.getIntValForAttribute(CleanupWorld.ATT_BOTTOM);
			int t = region.getIntValForAttribute(CleanupWorld.ATT_TOP);

			//get the block
			ObjectInstance block = s.getObject(this.blockName);
			int bx = block.getIntValForAttribute(CleanupWorld.ATT_X);
			int by = block.getIntValForAttribute(CleanupWorld.ATT_Y);

			int dist = manDistance(ax, ay, bx, by)-1; //need to be one step away from block to push it

			//and then block needs to be at room
			dist += toRegionManDistance(bx, by, l, r, b, t, delta);

			double fullChanceV = Math.pow(discount, dist-1);
			double v = freeRegion ? fullChanceV : lockProb * fullChanceV + (1. - lockProb)*0.;

			return v;
		}
	}


	public static int manDistance(int x0, int y0, int x1, int y1){
		return Math.abs(x0-x1) + Math.abs(y0-y1);
	}


	/**
	 * Manhatten distance to a room or door.
	 * @param x
	 * @param y
	 * @param l
	 * @param r
	 * @param b
	 * @param t
	 * @param delta set to 1 for rooms because boundaries are walls which are not sufficient to be in room; 0 for doors
	 * @return
	 */
	public static int toRegionManDistance(int x, int y, int l, int r, int b, int t, int delta){
		int dist = 0;

		//use +1s because boundaries define wall, which is not sufficient to be in the room
		if(x <= l){
			dist += l-x + delta;
		}
		else if(x >= r){
			dist += x - r + delta;
		}

		if(y <= b){
			dist += b - y + delta;
		}
		else if(y >= t){
			dist += y - t + delta;
		}

		return dist;
	}




	public static class GreedyReplan extends GreedyQPolicy{

		public GreedyReplan(QFunction planner) {
			super(planner);
		}

		@Override
		public AbstractGroundedAction getAction(State s) {
			GroundedAction ga = (GroundedAction) super.getAction(s);
			int count = 0;
			while(this.qplanner.getQ(s, ga).q == 0. && count<100){
				((Planner)this.qplanner).planFromState(s);
				ga = (GroundedAction) super.getAction(s);
				count++;
			}
			return ga;
		}
	}
}



