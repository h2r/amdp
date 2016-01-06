package amdp.hardcoded.cleanup;

import burlap.behavior.policy.Policy;
import burlap.behavior.singleagent.EpisodeAnalysis;
import burlap.behavior.singleagent.auxiliary.EpisodeSequenceVisualizer;
import burlap.behavior.singleagent.planning.stochastic.rtdp.BoundedRTDP;
import burlap.behavior.valuefunction.QFunction;
import burlap.behavior.valuefunction.QValue;
import burlap.behavior.valuefunction.ValueFunctionInitialization;
import burlap.domain.singleagent.cleanup.CleanupVisualizer;
import burlap.domain.singleagent.cleanup.CleanupWorld;
import burlap.oomdp.auxiliary.common.GoalConditionTF;
import burlap.oomdp.auxiliary.common.NullTermination;
import burlap.oomdp.auxiliary.stateconditiontest.StateConditionTest;
import burlap.oomdp.core.Domain;
import burlap.oomdp.core.GroundedProp;
import burlap.oomdp.core.PropositionalFunction;
import burlap.oomdp.core.TerminalFunction;
import burlap.oomdp.core.objects.ObjectInstance;
import burlap.oomdp.core.states.State;
import burlap.oomdp.singleagent.GroundedAction;
import burlap.oomdp.singleagent.ObjectParameterizedAction;
import burlap.oomdp.singleagent.RewardFunction;
import burlap.oomdp.singleagent.common.GoalBasedRF;
import burlap.oomdp.singleagent.common.NullRewardFunction;
import burlap.oomdp.singleagent.explorer.VisualExplorer;
import burlap.oomdp.statehashing.SimpleHashableStateFactory;
import burlap.oomdp.visualizer.Visualizer;

import java.util.Arrays;
import java.util.List;

/**
 * @author James MacGlashan.
 */
public class CleanupVideo {

	public static void main(String[] args) {

		//designWorld();
		generateBehavior();
		//debug();

	}


	public static void generateBehavior(){


		double lockProb = 0.5;

		CleanupWorld cw = new CleanupWorld();
		cw.includeDirectionAttribute(true);
		cw.includePullAction(true);
		cw.includeWallPF_s(true);
		cw.includeLockableDoors(true);
		cw.setLockProbability(lockProb);

		Domain domain = cw.generateDomain();

		CleanupL1Domain a1dgen = new CleanupL1Domain();
		a1dgen.setLockableDoors(true);
		a1dgen.setLockProb(lockProb);
		Domain adomain = a1dgen.generateDomain();

		CleanupL2Domain a2dgen = new CleanupL2Domain();
		Domain a2domain = a2dgen.generateDomain();

		State s = getState(domain);

		List<String> goalBlocks = Arrays.asList("block0", "block1");
		List<String> goalRooms = Arrays.asList("room3", "room3");



		StateConditionTest l0sc = new L0Goal(goalBlocks, goalRooms, domain.getPropFunction(CleanupWorld.PF_BLOCK_IN_ROOM));
		GoalBasedRF l0rf = new GoalBasedRF(l0sc, 1., 0.);
		GoalConditionTF l0tf = new GoalConditionTF(l0sc);

		StateConditionTest l2sc = new L2Goal(goalBlocks, goalRooms);
		GoalBasedRF l2rf = new GoalBasedRF(l2sc, 1., 0.);
		GoalConditionTF l2tf = new GoalConditionTF(l2sc);

		FixedDoorCleanupEnv env = new FixedDoorCleanupEnv(domain, l0rf, l0tf, s);
		env.addLockedDoor("door0");

		CleanupAgent agent = new CleanupAgent(domain, adomain, a2domain, l2rf, l2tf, 0.5);
		EpisodeAnalysis ea = agent.actUntilTermination(env, 500);

		Visualizer v = CleanupVisualizer.getVisualizer("data/resources/robotImages");
		new EpisodeSequenceVisualizer(v, domain, Arrays.asList(ea));



	}

	public static void debug(){

		double lockProb = 0.5;

		CleanupWorld cw = new CleanupWorld();
		cw.includeDirectionAttribute(true);
		cw.includePullAction(true);
		cw.includeWallPF_s(true);
		cw.includeLockableDoors(true);
		cw.setLockProbability(lockProb);

		Domain domain = cw.generateDomain();

		CleanupL1Domain a1dgen = new CleanupL1Domain();
		a1dgen.setLockableDoors(true);
		a1dgen.setLockProb(lockProb);
		Domain adomain = a1dgen.generateDomain();

		CleanupL2Domain a2dgen = new CleanupL2Domain();
		Domain a2domain = a2dgen.generateDomain();

		State s = getStateDebug(domain);


		CleanupAgent agent = new CleanupAgent(domain, adomain, a2domain, null, null, 0.5);

		GroundedAction l1Action = new ObjectParameterizedAction.ObjectParameterizedGroundedAction(adomain.getAction(CleanupL1Domain.ACTION_BLOCK_TO_DOOR), new String[]{"block2", "door3"});

		RewardFunction l0rf = agent.getL0Rf(s, l1Action);
		TerminalFunction l0tf = agent.getL0Tf(s, l1Action);
		ValueFunctionInitialization heuristic = agent.getL0Heuristic(s, l1Action);

		BoundedRTDP brtd = new BoundedRTDP(domain, l0rf, l0tf, 0.99, new SimpleHashableStateFactory(false), new ValueFunctionInitialization.ConstantValueFunctionInitialization(0.), heuristic, 0.01, 500);
		brtd.setMaxRolloutDepth(50);
		//brtd.toggleDebugPrinting(false);

		Policy p = brtd.planFromState(s);


		FixedDoorCleanupEnv env = new FixedDoorCleanupEnv(domain, new NullRewardFunction(), l0tf, s);
		env.addLockedDoor("door0");

		EpisodeAnalysis ea = p.evaluateBehavior(env, 50);

		for(int i = 0; i < ea.maxTimeStep(); i++){
			System.out.println(i + " " + ea.getAction(i).toString() + " " + brtd.getQ(ea.getState(i), ea.getAction(i)).q);
		}

		new EpisodeSequenceVisualizer(CleanupVisualizer.getVisualizer("data/resources/robotImages"), domain, Arrays.asList(ea));


	}

	public static void printQs(QFunction qf, State s){
		List<QValue> qs = qf.getQs(s);
		for(QValue q : qs){
			System.out.println(q.a.toString() + ": " + q.q);
		}

	}



	public static void designWorld(){

		double lockProb = 0.5;

		CleanupWorld cw = new CleanupWorld();
		cw.includeDirectionAttribute(true);
		cw.includePullAction(true);
		cw.includeWallPF_s(true);
		cw.includeLockableDoors(true);
		cw.setLockProbability(lockProb);

		Domain domain = cw.generateDomain();

		int y1 = 3;
		int y2 = 7;
		int y3 = 12;

		int x1 = 4;
		int x2 = 8;
		int x3 = 12;

		State s = getStateDebug(domain);

		Visualizer v = CleanupVisualizer.getVisualizer("data/resources/robotImages");

		FixedDoorCleanupEnv env = new FixedDoorCleanupEnv(domain, new NullRewardFunction(), new NullTermination(), s);
		env.addLockedDoor("door0");

		VisualExplorer exp = new VisualExplorer(domain, env, v);
		exp.addKeyAction("w", CleanupWorld.ACTION_NORTH);
		exp.addKeyAction("s", CleanupWorld.ACTION_SOUTH);
		exp.addKeyAction("d", CleanupWorld.ACTION_EAST);
		exp.addKeyAction("a", CleanupWorld.ACTION_WEST);
		exp.addKeyAction("x", CleanupWorld.ACTION_PULL);

		exp.initGUI();

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
				if(!b.getAllRelationalTargets(CleanupL1Domain.ATT_IN_REGION).contains(r)){
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
		CleanupWorld.setRoom(s, 2, y3, 0, y2, x3, "green");
		CleanupWorld.setRoom(s, 3, y2, x2, 0, x3, "yellow");

		CleanupWorld.setDoor(s, 0, 1, x2, 1, x2);
		CleanupWorld.setDoor(s, 1, 5, x1, 5, x1);

		CleanupWorld.setDoor(s, 2, y2, 2, y2, 2);
		CleanupWorld.setDoor(s, 3, y2, 10, y2, 10);

		CleanupWorld.setAgent(s, 7, 1);

		CleanupWorld.setBlock(s, 0, 5, 4, "chair", "blue");
		CleanupWorld.setBlock(s, 1, 6, 10, "chair", "red");
		CleanupWorld.setBlock(s, 2, 2, 10, "bag", "magenta");

		return s;

	}

	public static State getStateDebug(Domain domain){

		int y1 = 3;
		int y2 = 7;
		int y3 = 12;

		int x1 = 4;
		int x2 = 8;
		int x3 = 12;

		State s = CleanupWorld.getCleanState(domain, 4, 4, 3);
		CleanupWorld.setRoom(s, 0, y2, x1, 0, x2, "red");
		CleanupWorld.setRoom(s, 1, y2, 0, y1, x1, "blue");
		CleanupWorld.setRoom(s, 2, y3, 0, y2, x3, "green");
		CleanupWorld.setRoom(s, 3, y2, x2, 0, x3, "yellow");

		CleanupWorld.setDoor(s, 0, 1, x2, 1, x2);
		CleanupWorld.setDoor(s, 1, 5, x1, 5, x1);

		CleanupWorld.setDoor(s, 2, y2, 2, y2, 2);
		CleanupWorld.setDoor(s, 3, y2, 10, y2, 10);

		CleanupWorld.setDoorLocked(s, 1, 1);
		CleanupWorld.setDoorLocked(s, 2, 1);

		CleanupWorld.setAgent(s, 2, 8);

		CleanupWorld.setBlock(s, 0, 5, 6, "chair", "blue");
		CleanupWorld.setBlock(s, 1, 6, 10, "chair", "red");
		CleanupWorld.setBlock(s, 2, 2, 10, "bag", "magenta");

		return s;

	}

}


