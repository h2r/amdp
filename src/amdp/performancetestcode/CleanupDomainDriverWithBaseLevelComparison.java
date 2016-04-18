package amdp.performancetestcode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import amdp.framework.GroundedPropSC;
import amdp.cleanupdomain.CleanupDomainDriver;
import amdp.cleanupdomain.CleanupL1AMDPDomain;
import amdp.cleanupdomain.CleanupL1AMDPDomain.InRegionSC;
import amdp.cleanupdomain.CleanupL2AMDPDomain;
import amdp.cleanupdomain.FixedDoorCleanupEnv;
import amdp.framework.AMDPAgent;
import amdp.framework.AMDPPolicyGenerator;
import amdp.cleanupdomain.PullCostGoalRF;



import burlap.behavior.policy.GreedyQPolicy;
import burlap.behavior.policy.Policy;
import burlap.behavior.singleagent.EpisodeAnalysis;
import burlap.behavior.singleagent.auxiliary.EpisodeSequenceVisualizer;
import burlap.behavior.singleagent.planning.Planner;
import burlap.behavior.singleagent.planning.deterministic.DDPlannerPolicy;
import burlap.behavior.singleagent.planning.deterministic.uninformed.bfs.BFS;
import burlap.behavior.valuefunction.QFunction;
import burlap.behavior.valuefunction.ValueFunctionInitialization;
import burlap.debugtools.DPrint;
import burlap.domain.singleagent.cleanup.CleanupVisualizer;
import burlap.domain.singleagent.cleanup.CleanupWorld;
import burlap.oomdp.auxiliary.common.GoalConditionTF;
import burlap.oomdp.auxiliary.stateconditiontest.StateConditionTest;
import burlap.oomdp.auxiliary.stateconditiontest.TFGoalCondition;
import burlap.oomdp.core.AbstractGroundedAction;
import burlap.oomdp.core.Domain;
import burlap.oomdp.core.GroundedProp;
import burlap.oomdp.core.PropositionalFunction;
import burlap.oomdp.core.TerminalFunction;
import burlap.oomdp.core.objects.ObjectInstance;
import burlap.oomdp.core.states.State;
import burlap.oomdp.singleagent.GroundedAction;
import burlap.oomdp.singleagent.RewardFunction;
import burlap.oomdp.singleagent.common.GoalBasedRF;
import burlap.oomdp.statehashing.SimpleHashableStateFactory;
import burlap.oomdp.visualizer.Visualizer;





public class CleanupDomainDriverWithBaseLevelComparison {

	public static List<BoundedRTDPForTests> brtdpList= new ArrayList<BoundedRTDPForTests>();
	public static List<Integer> brtdpLevelList= new ArrayList<Integer>();

	private static int numRollouts = -1;

	static protected MutableGlobalInteger bellmanBudget = new MutableGlobalInteger(-1);
	static protected MutableGlobalInteger bellmanBudgetL0 = new MutableGlobalInteger(-1);
	static protected MutableGlobalInteger bellmanBudgetL1 = new MutableGlobalInteger(-1);
	static protected MutableGlobalInteger bellmanBudgetL2 = new MutableGlobalInteger(-1);

	static double L2Ratio = 0.05;
	static double L1Ratio = 0.65;
	static double L0Ratio = 0.3;

	static double lockProb = 0.5;

	static int numSteps = 251;

	public static void main(String[] args) {

		DPrint.toggleCode(3214986, false);

		boolean runGroundLevelBoundedRTDP = false;

		//20,000,000 is what the base level planner needs to plan right
		//		bellmanBudget.setValue(500000);
		int totalBudget=100000;


		for(int i =0;i<args.length;i++){
			String str = args[i];

			if(str.equals("-r")){
				runGroundLevelBoundedRTDP = Boolean.parseBoolean(args[i+1]);
			}
			if(str.equals("-b")){
				totalBudget = Integer.parseInt(args[i+1]);
			}
			if(str.equals("-l0")){
				L0Ratio = Double.parseDouble(args[i+1]);
			}
			if(str.equals("-l1")){
				L1Ratio = Double.parseDouble(args[i+1]);
			}
			if(str.equals("-l2")){
				L2Ratio = Double.parseDouble(args[i+1]);
			}
		}

		bellmanBudget.setValue(totalBudget);
		bellmanBudgetL0.setValue((int)(totalBudget*L0Ratio));
		bellmanBudgetL1.setValue((int)(totalBudget*L1Ratio));
		bellmanBudgetL2.setValue((int)(totalBudget*L2Ratio));



		if(runGroundLevelBoundedRTDP){

			double lockProb = 0.5;


			CleanupWorld dgen = new CleanupWorld();
			dgen.includeDirectionAttribute(true);
			dgen.includePullAction(true);
			dgen.includeWallPF_s(true);
			dgen.includeLockableDoors(true);
			dgen.setLockProbability(lockProb);
			Domain domain = dgen.generateDomain();


			State s = getState(domain);

			StateConditionTest sc = new GroundedPropSC(new GroundedProp(domain.getPropFunction(CleanupWorld.PF_BLOCK_IN_ROOM),  new String[]{"block0", "room3"}));

			RewardFunction heuristicRF = new PullCostGoalRF(sc, 1., 0.);


			GroundedPropSC l0sc = new GroundedPropSC(new GroundedProp(domain.getPropFunction(CleanupWorld.PF_BLOCK_IN_ROOM), new String[]{"block0", "room3"}));
			GoalBasedRF l0rf = new GoalBasedRF(l0sc, 1., 0.);
			GoalConditionTF l0tf = new GoalConditionTF(l0sc);

			FixedDoorCleanupEnv env = new FixedDoorCleanupEnv(domain, l0rf, l0tf, s);
			env.addLockedDoor("door0");

			long startTime = System.currentTimeMillis();




			ValueFunctionInitialization heuristic = CleanupDomainDriver.getL0Heuristic(s, heuristicRF);
			BoundedRTDPForTests brtd = new BoundedRTDPForTests(domain, l0rf, l0tf,0.99, new SimpleHashableStateFactory(false), new ValueFunctionInitialization.ConstantValueFunctionInitialization(0.), heuristic, 0.01, numRollouts);
			brtd.setDebugCode(39502748);
			DPrint.toggleCode(39502748, true);
			brtd.setMaxRolloutDepth(150);
			brtd.setRemainingNumberOfBellmanUpdates(bellmanBudget);
			brtd.toggleDebugPrinting(false);
			Policy p = brtd.planFromState(s);

			long endTime = System.currentTimeMillis();
			long duration = (endTime - startTime);

			EpisodeAnalysis ea = p.evaluateBehavior(env, numSteps+1);

			//num actions
			System.out.println(ea.stateSequence.size()-1);
			// num bellman updates
			System.out.println(brtd.getNumberOfBellmanUpdates());
			// num steps
			if(ea.stateSequence.size()>numSteps){
				System.out.println(0);
			}
			else{
				System.out.println(1);
			}
			//total time
			System.out.println( duration);
			System.out.println("ground level brtdp clean up domain");
			//			Visualizer v = CleanupVisualizer.getVisualizer("amdp/data/resources/robotImages");
			//			System.out.println(ea.getState(0).toString());
			//			new EpisodeSequenceVisualizer(v, domain, Arrays.asList(ea));

			//			System.out.println("total actions:" + ea.actionSequence.size() + ", number of backups: " + brtd.getNumberOfBellmanUpdates());
		}
		else{

			//designWorld();
			List<Integer> runInfo = generateBehavior();
			//debug();
			int count = 0;
			int count0 =0;
			int count1 =0;
			int count2 =0;
			for(int i=0;i<brtdpList.size();i++){
				int numUpdates = brtdpList.get(i).getNumberOfBellmanUpdates();
				count+= numUpdates;
				//				System.out.println("Level: " + brtdpLevelList.get(i) + ", count: " + numUpdates);
				if(brtdpLevelList.get(i)==0){
					count0 += numUpdates;
				}
				else if(brtdpLevelList.get(i)==1){
					count1 += numUpdates;
				}
				else{
					count2 += numUpdates;
				}
			}

			// steps taken
			System.out.println(runInfo.get(0)-1);
			// bellman updates used
			System.out.println(count);
			if(runInfo.get(0)>numSteps){
				System.out.println(0);
			}
			else{
				System.out.println(1);
			}
			//time
			System.out.println(runInfo.get(1));


			System.out.println("cleanup domain amdp run ");


			System.out.println(count0);
			System.out.println(count1);
			System.out.println(count2);
		}

	}

	public static List<Integer> generateBehavior(){

		//		double lockProb = 0.5;

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

		State s = getState(domain);

		//		List<String> goalBlocks = Arrays.asList("block0", "block1");
		//		List<String> goalRooms = Arrays.asList("room3", "room3");

		List<String> goalBlocks = Arrays.asList("block0");
		List<String> goalRooms = Arrays.asList("room3");


		StateConditionTest l0sc = new L0Goal(goalBlocks, goalRooms, domain.getPropFunction(CleanupWorld.PF_BLOCK_IN_ROOM));
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

		long startTime = System.currentTimeMillis();

		EpisodeAnalysis ea = agent.actUntilTermination(env, numSteps);

		long endTime = System.currentTimeMillis();
		long duration = (endTime - startTime);
//		System.out.println("total time: " + duration);


		//		Visualizer v = CleanupVisualizer.getVisualizer("amdp/data/resources/robotImages");
		//		System.out.println(ea.getState(0).toString());
		//		new EpisodeSequenceVisualizer(v, domain, Arrays.asList(ea));

		//		System.out.println("total actions:" + ea.actionSequence.size());
		List<Integer> returnList = new ArrayList<Integer>();
		returnList.add(ea.stateSequence.size());
		returnList.add((int)duration);
		return returnList;

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


	public static State getState(Domain domain){


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

		CleanupWorld.setAgent(s, 7, 1);

		CleanupWorld.setBlock(s, 0, 5, 4, "chair", "blue");
		CleanupWorld.setBlock(s, 1, 6, 10, "basket", "red");
		CleanupWorld.setBlock(s, 2, 2, 10, "bag", "magenta");

		return s;

	}


	public static class l2PolicyGenerator implements AMDPPolicyGenerator{

		private Domain l2;
		protected double discount = 0.99;

		public l2PolicyGenerator(Domain l2In){
			l2 = l2In;
		}

		@Override
		public Policy generatePolicy(State s, RewardFunction rf, TerminalFunction tf) {
			//			BFS bfs = new BFS(l2, new TFGoalCondition(tf), new SimpleHashableStateFactory(false));
			//			bfs.toggleDebugPrinting(false);
			//			DDPlannerPolicy policy = new DDPlannerPolicy(bfs);
			//			return policy;

			BoundedRTDPForTests brtdp = new BoundedRTDPForTests(l2, rf, tf, discount, new SimpleHashableStateFactory(false),
					new ValueFunctionInitialization.ConstantValueFunctionInitialization(0.),
					new ValueFunctionInitialization.ConstantValueFunctionInitialization(1.),
					0.001,
					numRollouts);
			brtdp.setRemainingNumberOfBellmanUpdates(bellmanBudgetL2);
			brtdpList.add(brtdp);
			brtdpLevelList.add(2);
			brtdp.setMaxRolloutDepth(50);
			brtdp.toggleDebugPrinting(false);
			return brtdp.planFromState(s);

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

			//			ValueFunctionInitialization heuristic = getL1Heuristic(s, rf);



			SimpleHashableStateFactory shf = new SimpleHashableStateFactory(false);
			BoundedRTDPForTests brtdp = new BoundedRTDPForTests(l1, l1rf, l1tf, discount, shf,
					new ValueFunctionInitialization.ConstantValueFunctionInitialization(0.),
					new ValueFunctionInitialization.ConstantValueFunctionInitialization(1.),
					0.001,
					2000);

			//			BoundedRTDP brtdp = new BoundedRTDP(l1, l1rf, l1tf, discount, new SimpleHashableStateFactory(false),
			//					new ValueFunctionInitialization.ConstantValueFunctionInitialization(0.),
			//					heuristic,
			//					0.001,
			//					1000);
			brtdp.setRemainingNumberOfBellmanUpdates(bellmanBudgetL1);
			brtdp.setMaxRolloutDepth(200);
			brtdp.toggleDebugPrinting(false);

			//			System.out.println("planner gap: " + brtdp.getGap(shf.hashState(s)));
			Policy p = brtdp.planFromState(s);
			//			System.out.println("planner gap after: " + brtdp.getGap(shf.hashState(s)));
			//			brtdp.planFromState(s);
			brtdpList.add(brtdp);
			brtdpLevelList.add(1);

			//			return new GreedyReplan(brtdp);
			return p;
		}


	}



	public static ValueFunctionInitialization getL1Heuristic(State s, RewardFunction rf){
		return new L1Heuristic(rf);
	}

	public static class L1Heuristic implements ValueFunctionInitialization{

		String srcObj;

		String targetRoom;

		public L1Heuristic(RewardFunction rf) {
			srcObj = ((InRegionSC)((GoalBasedRF)rf).getGoalCondition()).srcOb;
			targetRoom = ((InRegionSC)((GoalBasedRF)rf).getGoalCondition()).targetOb;
		}

		@Override
		public double qValue(State s, AbstractGroundedAction a) {
			return value(s);
		}

		@Override
		public double value(State s) {

			String srcObjRegion = s.getObject(srcObj).getStringValForAttribute(CleanupL1AMDPDomain.ATT_IN_REGION);

			if(srcObjRegion.equals(targetRoom)){
				return 0.;
			}
			else{
				return 1.0;
			}


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
			BoundedRTDPForTests brtd = new BoundedRTDPForTests(l0, rf, tf, this.discount, 
					new SimpleHashableStateFactory(false), 
					new ValueFunctionInitialization.ConstantValueFunctionInitialization(0.), 
					heuristic, 0.01, numRollouts);
			brtd.setRemainingNumberOfBellmanUpdates(bellmanBudgetL0);
			brtd.setMaxRolloutDepth(100);
			brtd.toggleDebugPrinting(false);
			brtdpList.add(brtd);
			brtdpLevelList.add(0);
			//			brtd.planFromState(s);
			//			return new GreedyReplan(brtd);
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



