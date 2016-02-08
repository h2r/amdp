package burlap.domain.singleagent;
/**
 * Created by ngopalan on 2/6/16.
 */

import burlap.behavior.policy.Policy;
import burlap.behavior.singleagent.auxiliary.EpisodeSequenceVisualizer;
import burlap.behavior.singleagent.planning.deterministic.DeterministicPlanner;
import burlap.behavior.singleagent.planning.deterministic.informed.Heuristic;
import burlap.behavior.singleagent.planning.deterministic.informed.astar.AStar;
import burlap.behavior.singleagent.planning.deterministic.uninformed.bfs.BFS;
import burlap.behavior.singleagent.planning.deterministic.uninformed.dfs.DFS;
import burlap.domain.singleagent.frostbite.FrostbiteDomain;
import burlap.domain.singleagent.frostbite.FrostbiteRF;
import burlap.domain.singleagent.frostbite.FrostbiteTF;
import burlap.domain.singleagent.frostbite.FrostbiteVisualizer;
import burlap.oomdp.auxiliary.stateconditiontest.StateConditionTest;
import burlap.oomdp.core.Domain;
import burlap.oomdp.core.PropositionalFunction;
import burlap.oomdp.core.states.State;
import burlap.oomdp.singleagent.RewardFunction;
import burlap.oomdp.statehashing.HashableStateFactory;
import burlap.oomdp.auxiliary.stateconditiontest.TFGoalCondition;
import burlap.oomdp.statehashing.SimpleHashableStateFactory;
import burlap.oomdp.visualizer.Visualizer;




public class FrosbiteTest {
	
	private static Heuristic mdistHeuristic = new Heuristic() {
		@Override
		public double h(State s) {
 
			return 0.;
		}
	};


	public static class goalTest implements StateConditionTest{


		
		private PropositionalFunction onIce;
		private PropositionalFunction inWater;
		private PropositionalFunction iglooBuilt;
		
		public goalTest(Domain domain){
			this.inWater = domain.getPropFunction(FrostbiteDomain.PFINWATER);
			this.onIce = domain.getPropFunction(FrostbiteDomain.PFONICE);
			this.iglooBuilt = domain.getPropFunction(FrostbiteDomain.PFIGLOOBUILT);
		}
		
		@Override
		public boolean satisfies(State s) {
			return iglooBuilt.somePFGroundingIsTrue(s) && onIce.somePFGroundingIsTrue(s);
		}
		
	}
	
    public static void main(String[] args){
        FrostbiteDomain fd = new FrostbiteDomain();
        Domain d = fd.generateDomain();
        State s = FrostbiteDomain.getCleanState(d);
        FrostbiteRF frf = new FrostbiteRF(d);
        FrostbiteTF  ftf = new FrostbiteTF(d);
        
        

//        StateConditionTest goalCondition = new TFGoalCondition(ftf);
        
        StateConditionTest goalCondition = new FrosbiteTest.goalTest(d);

        HashableStateFactory hashingFactory  = new SimpleHashableStateFactory();;


        
        
//        DeterministicPlanner planner = new BFS(d, goalCondition, hashingFactory);
//        
        
    	
//    	DeterministicPlanner planner = new DFS(d, goalCondition, hashingFactory);
    	
    	
    	DeterministicPlanner planner = new AStar(d, frf, goalCondition, hashingFactory, mdistHeuristic);
    	
    	Policy p = planner.planFromState(s);
        String outputPath = "/home/ng/workspace/outputFiles/";
        
        p.evaluateBehavior(s, frf, ftf).writeToFile(outputPath + "AStar");

        Visualizer v = FrostbiteVisualizer.getVisualizer(fd);
        new EpisodeSequenceVisualizer(v, d, outputPath);



    }
}
