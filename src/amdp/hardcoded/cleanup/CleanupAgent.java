package amdp.hardcoded.cleanup;

import burlap.behavior.policy.GreedyQPolicy;
import burlap.behavior.policy.Policy;
import burlap.behavior.singleagent.EpisodeAnalysis;
import burlap.behavior.singleagent.auxiliary.EpisodeSequenceVisualizer;
import burlap.behavior.singleagent.planning.Planner;
import burlap.behavior.singleagent.planning.deterministic.DDPlannerPolicy;
import burlap.behavior.singleagent.planning.deterministic.uninformed.bfs.BFS;
import burlap.behavior.singleagent.planning.stochastic.rtdp.BoundedRTDP;
import burlap.behavior.valuefunction.QFunction;
import burlap.behavior.valuefunction.QValue;
import burlap.behavior.valuefunction.ValueFunctionInitialization;
import burlap.domain.singleagent.cleanup.CleanupVisualizer;
import burlap.domain.singleagent.cleanup.CleanupWorld;
import burlap.oomdp.auxiliary.common.GoalConditionTF;
import burlap.oomdp.auxiliary.stateconditiontest.StateConditionTest;
import burlap.oomdp.auxiliary.stateconditiontest.TFGoalCondition;
import burlap.oomdp.core.AbstractGroundedAction;
import burlap.oomdp.core.Domain;
import burlap.oomdp.core.GroundedProp;
import burlap.oomdp.core.TerminalFunction;
import burlap.oomdp.core.objects.ObjectInstance;
import burlap.oomdp.core.states.State;
import burlap.oomdp.singleagent.GroundedAction;
import burlap.oomdp.singleagent.ObjectParameterizedAction;
import burlap.oomdp.singleagent.RewardFunction;
import burlap.oomdp.singleagent.common.GoalBasedRF;
import burlap.oomdp.singleagent.environment.Environment;
import burlap.oomdp.singleagent.environment.EnvironmentOutcome;
import burlap.oomdp.statehashing.SimpleHashableStateFactory;
import burlap.oomdp.visualizer.Visualizer;

import java.util.Arrays;
import java.util.List;

/**
 * @author James MacGlashan.
 */
public class CleanupAgent {


	protected Domain l0;
	protected Domain l1;
	protected Domain l2;

	protected RewardFunction rf;
	protected TerminalFunction tf;

	protected double lockProb = 0.5;
	protected double discount = 0.99;


	public CleanupAgent(Domain l0, Domain l1, Domain l2, RewardFunction rf, TerminalFunction tf, double lockProb) {
		this.l0 = l0;
		this.l1 = l1;
		this.l2 = l2;
		this.rf = rf;
		this.tf = tf;
		this.lockProb = lockProb;
	}


	public EpisodeAnalysis actUntilTermination(Environment env){

		return this.actUntilTermination(env, -1);

	}


	public EpisodeAnalysis actUntilTermination(Environment env, int maxSteps){

		State l0State = env.getCurrentObservation();
		State l1State = CleanupL1Domain.projectToAMDPState(l0State, l1);
		State l2State = CleanupL2Domain.projectToAMDPState(l1State, l2);

		Policy l2P = this.generateL2Policy(l2State);

		EpisodeAnalysis ea = new EpisodeAnalysis(l0State);

		int steps = 0;
		while(!env.isInTerminalState() && (steps < maxSteps || maxSteps == -1)){

			GroundedAction l2A = (GroundedAction)l2P.getAction(l2State);
			System.out.println("Level 2: " + l2A.toString());

			Policy l1P = this.generateL1Policy(l2A, l1State);
			TerminalFunction l1TF = this.getL1Tf(l1State, l2A);

			while(!l1TF.isTerminal(l1State) && !env.isInTerminalState() && (steps < maxSteps || maxSteps == -1)){

				GroundedAction l1A = (GroundedAction)l1P.getAction(l1State);
				System.out.println("    Level 1: " + l1A.toString());

				Policy l0P = this.generateL0Policy(l1A, l0State);
				TerminalFunction l0TF = this.getL0Tf(l0State, l1A);
				while(!l0TF.isTerminal(l0State) && !env.isInTerminalState() && (steps < maxSteps || maxSteps == -1)){
					GroundedAction l0A = (GroundedAction)l0P.getAction(l0State);
					EnvironmentOutcome eo = env.executeAction(l0A);
					ea.recordTransitionTo(l0A, eo.op, eo.r);
					l0State = env.getCurrentObservation();
					System.out.println("        Level 0: " + l0A.toString());
					steps++;
				}

				l1State = CleanupL1Domain.projectToAMDPState(l0State, l1);

			}

			l2State = CleanupL2Domain.projectToAMDPState(l1State, l2);

		}


		return ea;

	}


	public Policy generateL2Policy(State l2State){

		BFS bfs = new BFS(l2, new TFGoalCondition(this.tf), new SimpleHashableStateFactory(false));
		bfs.toggleDebugPrinting(false);
		DDPlannerPolicy policy = new DDPlannerPolicy(bfs);

		return policy;
	}

	public Policy generateL1Policy(GroundedAction l2Action, State l1State){

		RewardFunction l1rf = this.getL1Rf(l1State, l2Action);
		TerminalFunction l1tf = this.getL1Tf(l1State, l2Action);
		//StateReachability.getReachableStates(l1State, (SADomain)l1, new SimpleHashableStateFactory(false));

		//ValueIteration vi = new ValueIteration(l1, l1rf, l1tf, this.discount, new SimpleHashableStateFactory(false), 0.01, 100);
		//return vi.planFromState(l1State);

		BoundedRTDP brtdp = new BoundedRTDP(l1, l1rf, l1tf, this.discount, new SimpleHashableStateFactory(false),
				new ValueFunctionInitialization.ConstantValueFunctionInitialization(0.),
				new ValueFunctionInitialization.ConstantValueFunctionInitialization(1.),
				0.001,
				1000);
		brtdp.setMaxRolloutDepth(20);
		brtdp.toggleDebugPrinting(false);
		brtdp.planFromState(l1State);

		return new GreedyReplan(brtdp);


	}

	public Policy generateL0Policy(GroundedAction l1Action, State l0State){

		RewardFunction l0rf = this.getL0Rf(l0State, l1Action);
		TerminalFunction l0tf = this.getL0Tf(l0State, l1Action);
		ValueFunctionInitialization heuristic = this.getL0Heuristic(l0State, l1Action);

		BoundedRTDP brtd = new BoundedRTDP(l0, l0rf, l0tf, this.discount, new SimpleHashableStateFactory(false), new ValueFunctionInitialization.ConstantValueFunctionInitialization(0.), heuristic, 0.01, 500);
		brtd.setMaxRolloutDepth(50);
		brtd.toggleDebugPrinting(false);
		return brtd.planFromState(l0State);

	}


	protected TerminalFunction getL1Tf(State s, GroundedAction l2Action){
		ObjectParameterizedAction.ObjectParameterizedGroundedAction oga = (ObjectParameterizedAction.ObjectParameterizedGroundedAction)l2Action;
		StateConditionTest sc = null;
		if(l2Action.actionName().equals(CleanupL1Domain.ACTION_AGENT_TO_ROOM)){
			sc = new CleanupL1Domain.InRegionSC(CleanupWorld.CLASS_AGENT + 0, oga.params[0]);
		}
		else{
			sc = new DoorLockedSC(oga.params[1], new CleanupL1Domain.InRegionSC(oga.params[0], oga.params[1]));
		}
		return new GoalConditionTF(sc);
	}

	public TerminalFunction getL0Tf(State s, GroundedAction l1Action){
		ObjectParameterizedAction.ObjectParameterizedGroundedAction oga = (ObjectParameterizedAction.ObjectParameterizedGroundedAction)l1Action;
		StateConditionTest sc = null;
		if(l1Action.actionName().equals(CleanupL1Domain.ACTION_AGENT_TO_ROOM)){
			sc = new GroundedPropSC(new GroundedProp(l0.getPropFunction(CleanupWorld.PF_AGENT_IN_ROOM), new String[]{CleanupWorld.CLASS_AGENT + 0, oga.params[0]}));
		}
		else if(l1Action.actionName().equals(CleanupL1Domain.ACTION_AGENT_TO_DOOR)){
			sc = new DoorLockedSC(oga.params[0], new GroundedPropSC(new GroundedProp(l0.getPropFunction(CleanupWorld.PF_AGENT_IN_DOOR), new String[]{CleanupWorld.CLASS_AGENT + 0, oga.params[0]})));
		}
		else if(l1Action.actionName().equals(CleanupL1Domain.ACTION_BLOCK_TO_ROOM)){
			sc = new GroundedPropSC(new GroundedProp(l0.getPropFunction(CleanupWorld.PF_BLOCK_IN_ROOM), new String[]{oga.params[0], oga.params[1]}));
		}
		else if(l1Action.actionName().equals(CleanupL1Domain.ACTION_BLOCK_TO_DOOR)){
			sc = new DoorLockedSC(oga.params[1], new GroundedPropSC(new GroundedProp(l0.getPropFunction(CleanupWorld.PF_BLOCK_IN_DOOR), new String[]{oga.params[0], oga.params[1]})));
		}

		return new GoalConditionTF(sc);
	}

	public RewardFunction getL1Rf(State s, GroundedAction l2Action){
		ObjectParameterizedAction.ObjectParameterizedGroundedAction oga = (ObjectParameterizedAction.ObjectParameterizedGroundedAction)l2Action;
		StateConditionTest sc = null;
		if(l2Action.actionName().equals(CleanupL1Domain.ACTION_AGENT_TO_ROOM)){
			sc = new CleanupL1Domain.InRegionSC(CleanupWorld.CLASS_AGENT+0, oga.params[0]);
		}
		else{
			sc = new CleanupL1Domain.InRegionSC(oga.params[0], oga.params[1]);
		}
		return new GoalBasedRF(sc, 1., 0.);
	}

	public RewardFunction getL0Rf(State s, GroundedAction l1Action){
		ObjectParameterizedAction.ObjectParameterizedGroundedAction oga = (ObjectParameterizedAction.ObjectParameterizedGroundedAction)l1Action;
		StateConditionTest sc = null;
		if(l1Action.actionName().equals(CleanupL1Domain.ACTION_AGENT_TO_ROOM)){
			sc = new GroundedPropSC(new GroundedProp(l0.getPropFunction(CleanupWorld.PF_AGENT_IN_ROOM), new String[]{CleanupWorld.CLASS_AGENT+0, oga.params[0]}));
		}
		else if(l1Action.actionName().equals(CleanupL1Domain.ACTION_AGENT_TO_DOOR)){
			sc = new GroundedPropSC(new GroundedProp(l0.getPropFunction(CleanupWorld.PF_AGENT_IN_DOOR), new String[]{CleanupWorld.CLASS_AGENT+0, oga.params[0]}));
		}
		else if(l1Action.actionName().equals(CleanupL1Domain.ACTION_BLOCK_TO_ROOM)){
			sc = new GroundedPropSC(new GroundedProp(l0.getPropFunction(CleanupWorld.PF_BLOCK_IN_ROOM), new String[]{oga.params[0], oga.params[1]}));
		}
		else if(l1Action.actionName().equals(CleanupL1Domain.ACTION_BLOCK_TO_DOOR)){
			sc = new GroundedPropSC(new GroundedProp(l0.getPropFunction(CleanupWorld.PF_BLOCK_IN_DOOR), new String[]{oga.params[0], oga.params[1]}));
		}

		return new PullCostGoalRF(sc, 1., 0.);
	}

	public ValueFunctionInitialization getL0Heuristic(State s, GroundedAction l1Action){

		ObjectParameterizedAction.ObjectParameterizedGroundedAction oga = (ObjectParameterizedAction.ObjectParameterizedGroundedAction)l1Action;
		if(l1Action.actionName().equals(CleanupL1Domain.ACTION_AGENT_TO_ROOM)){
			return new AgentToRegionHeuristic(oga.params[0], this.discount, this.lockProb);
		}
		else if(l1Action.actionName().equals(CleanupL1Domain.ACTION_AGENT_TO_DOOR)){
			return new AgentToRegionHeuristic(oga.params[0], this.discount, this.lockProb);
		}
		else if(l1Action.actionName().equals(CleanupL1Domain.ACTION_BLOCK_TO_ROOM)){
			return new BlockToRegionHeuristic(oga.params[0], oga.params[1], this.discount, this.lockProb);
		}
		else if(l1Action.actionName().equals(CleanupL1Domain.ACTION_BLOCK_TO_DOOR)){
			return new BlockToRegionHeuristic(oga.params[0], oga.params[1], this.discount, this.lockProb);
		}

		throw new RuntimeException("Unknown action " + l1Action.toString() + ". Cannot construct l0 heuristic.");
	}


	public static class GroundedPropSC implements StateConditionTest{

		public GroundedProp gp;

		public GroundedPropSC(GroundedProp gp) {
			this.gp = gp;
		}

		@Override
		public boolean satisfies(State s) {
//			if(gp.isTrue(s)){
//				System.out.println("goal!!");
//			}
			return gp.isTrue(s);
		}
	}


	public static class DoorLockedSC implements StateConditionTest{

		public String door;
		public StateConditionTest otherSC;

		public DoorLockedSC(String door, StateConditionTest otherSC) {
			this.door = door;
			this.otherSC = otherSC;
		}

		@Override
		public boolean satisfies(State s) {
			if(otherSC.satisfies(s)){
				return true;
			}

			ObjectInstance doorOb = s.getObject(door);
			if(doorOb.getObjectClass().hasAttribute(CleanupWorld.ATT_LOCKED)){
				int lockedVal = doorOb.getIntValForAttribute(CleanupWorld.ATT_LOCKED);
				return lockedVal == 2;
			}

			return false;
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



	public static class GreedyReplan extends GreedyQPolicy{

		public GreedyReplan(QFunction planner) {
			super(planner);
		}

		@Override
		public AbstractGroundedAction getAction(State s) {
			GroundedAction ga = (GroundedAction) super.getAction(s);
			if(this.qplanner.getQ(s, ga).q == 0.){
				((Planner)this.qplanner).planFromState(s);
				ga = (GroundedAction) super.getAction(s);
				if(this.qplanner.getQ(s, ga).q == 0.){
					System.out.println("Still confused!!");
				}
			}
			return ga;
		}
	}



	public static void main(String[] args) {

		double lockProb = 0.5;

		CleanupWorld dgen = new CleanupWorld();
		dgen.includeDirectionAttribute(true);
		dgen.includePullAction(true);
		dgen.includeWallPF_s(true);
		dgen.includeLockableDoors(true);
		dgen.setLockProbability(lockProb);
		Domain domain = dgen.generateDomain();

		//dgen.setLockProbability(0.);
		//Domain envDomain = dgen.generateDomain();

		State s = CleanupWorld.getClassicState(domain);

		CleanupL1Domain a1dgen = new CleanupL1Domain();
		a1dgen.setLockableDoors(true);
		a1dgen.setLockProb(lockProb);
		Domain adomain = a1dgen.generateDomain();

		CleanupL2Domain a2dgen = new CleanupL2Domain();
		Domain a2domain = a2dgen.generateDomain();



		GroundedPropSC l0sc = new GroundedPropSC(new GroundedProp(domain.getPropFunction(CleanupWorld.PF_BLOCK_IN_ROOM), new String[]{"block0", "room1"}));
		GoalBasedRF l0rf = new GoalBasedRF(l0sc, 1., 0.);
		GoalConditionTF l0tf = new GoalConditionTF(l0sc);

		StateConditionTest l2sc = new CleanupL1Domain.InRegionSC("block0", "room1");
		GoalBasedRF l2rf = new GoalBasedRF(l2sc, 1., 0.);
		GoalConditionTF l2tf = new GoalConditionTF(l2sc);

		//SimulatedEnvironment env = new SimulatedEnvironment(envDomain, l0rf, l0tf, s);
		FixedDoorCleanupEnv env = new FixedDoorCleanupEnv(domain, l0rf, l0tf, s);
		//add locked doors if any
		//env.addLockedDoor("door1");




//		GroundedPropSC l0sct = new GroundedPropSC(new GroundedProp(domain.getPropFunction(CleanupWorld.PF_BLOCK_IN_DOOR), new String[]{"block0", "door1"}));
//		GoalBasedRF l0rft = new GoalBasedRF(l0sct, 1., 0.);
//		GoalConditionTF l0tft = new GoalConditionTF(l0sct);
//
//		CleanupWorld.setAgent(s, 5, 3);
//
//		BoundedRTDP planner = new BoundedRTDP(domain, l0rft, l0tft, 0.99, new SimpleHashableStateFactory(false), new ValueFunctionInitialization.ConstantValueFunctionInitialization(0.), new BlockToRegionHeuristic("block0", "door1", 0.99, 0.5), 0.01, 500);
//		Policy p = planner.planFromState(s);
//
//		printQs(planner, s);
//
//		EpisodeAnalysis tea = p.evaluateBehavior(s, l0rft, l0tft, 15);
//		System.out.println(tea.getActionSequenceString("\n"));




//		VisualExplorer exp = new VisualExplorer(domain, CleanupVisualizer.getVisualizer("data/resources/robotImages"), s);
//		exp.initGUI();



		CleanupAgent agent = new CleanupAgent(domain, adomain, a2domain, l2rf, l2tf, 0.5);
		EpisodeAnalysis ea = agent.actUntilTermination(env, 100);

		Visualizer v = CleanupVisualizer.getVisualizer("data/resources/robotImages");
		new EpisodeSequenceVisualizer(v, domain, Arrays.asList(ea));


	}


	public static void printQs(QFunction qf, State s){
		List<QValue> qs = qf.getQs(s);
		for(QValue q : qs){
			System.out.println(q.a.toString() + ": " + q.q);
		}
	}

}
