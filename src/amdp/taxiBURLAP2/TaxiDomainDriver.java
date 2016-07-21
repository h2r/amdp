package amdp.taxiBURLAP2;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import amdp.cleanupdomain.CleanupDomainDriverWithBaseLevelComparison.GreedyReplan;
import amdp.framework.AMDPAgent;
import amdp.framework.AMDPPolicyGenerator;
import amdp.taxi.TaxiL2AMDPDomain.InLocationSC;
import burlap.behavior.policy.Policy;
import burlap.behavior.singleagent.EpisodeAnalysis;
import burlap.behavior.singleagent.auxiliary.EpisodeSequenceVisualizer;
import burlap.behavior.singleagent.planning.deterministic.DDPlannerPolicy;
import burlap.behavior.singleagent.planning.deterministic.uninformed.bfs.BFS;
import burlap.behavior.singleagent.planning.stochastic.rtdp.BoundedRTDP;
import burlap.behavior.singleagent.planning.stochastic.valueiteration.ValueIteration;
import burlap.behavior.valuefunction.ValueFunctionInitialization;
import burlap.domain.singleagent.cleanup.CleanupWorld;
import burlap.oomdp.auxiliary.common.GoalConditionTF;
import burlap.oomdp.auxiliary.stateconditiontest.StateConditionTest;
import burlap.oomdp.auxiliary.stateconditiontest.TFGoalCondition;
import burlap.oomdp.core.Domain;
import burlap.oomdp.core.GroundedProp;
import burlap.oomdp.core.PropositionalFunction;
import burlap.oomdp.core.TerminalFunction;
import burlap.oomdp.core.states.State;
import burlap.oomdp.singleagent.RewardFunction;
import burlap.oomdp.singleagent.common.GoalBasedRF;
import burlap.oomdp.singleagent.environment.SimulatedEnvironment;
import burlap.oomdp.statehashing.SimpleHashableStateFactory;
import burlap.oomdp.visualizer.Visualizer;

public class TaxiDomainDriver {

	public static void main(String[] args) {

		System.out.println("start:");;
		generateBehavior();


	}



	public static void generateBehavior(){

		TaxiDomain d0Gen = new TaxiDomain();
		d0Gen.includeFuel = false;
		Domain d0 = d0Gen.generateDomain();
		//		State s = TaxiDomain.getClassicState(d0);
		State s = TaxiDomain.getComplexState(d0);


		TaxiL1AMDPDomain d1Gen = new TaxiL1AMDPDomain(d0);
		Domain d1 = d1Gen.generateDomain();
		State as = TaxiL1AMDPDomain.getMappedState(s, d1);
		//		System.out.println(as.getCompleteStateDescription());

		TaxiL2AMDPDomain d2Gen = new TaxiL2AMDPDomain(d1);
		Domain d2 = d2Gen.generateDomain();
		State as2 = TaxiL2AMDPDomain.getMappedState(as, d2);

		List<String> passengers  = new ArrayList<String>();
		passengers.add("passenger0");
		passengers.add("passenger1");

		StateConditionTest l2sc = new InLocationSC(passengers);
		RewardFunction rfl2 = new GoalBasedRF(l2sc, 1.);
		TerminalFunction tfl2 = new GoalConditionTF(l2sc);

//		List<String> passengerList = Arrays.asList("passenger0");
//		List<String> locationList = Arrays.asList("location0");

		StateConditionTest l0sc = new L0Goal(passengers, d0.getPropFunction(TaxiDomain.PASSENGERATGOALLOCATIONPF));
		RewardFunction rfl0 = new GoalBasedRF(l0sc, 1.);
		TerminalFunction tfl0 = new GoalConditionTF(l0sc);


		List<Domain> domainList = new ArrayList<Domain>();
		domainList.add(0,d0);
		domainList.add(1,d1);
		domainList.add(2,d2);

		List<AMDPPolicyGenerator> pgList = new ArrayList<AMDPPolicyGenerator>();
		pgList.add(0,new l0PolicyGenerator(d0));
		pgList.add(1,new l1PolicyGenerator(d1));
		pgList.add(2,new l2PolicyGenerator(d2));


		//TODO: define L0 RF and TF
		AMDPAgent agent = new AMDPAgent(domainList, pgList, rfl2, tfl2);

		SimulatedEnvironment env = new SimulatedEnvironment(d0, rfl0, tfl0, s);

		EpisodeAnalysis ea = agent.actUntilTermination(env, 100);

		Visualizer v = TaxiVisualizer.getVisualizer(5, 5);
		new EpisodeSequenceVisualizer(v, d0, Arrays.asList(ea));


	}


	public static class L0Goal implements StateConditionTest{

		List<String> passenger;
		PropositionalFunction pf;


		public L0Goal(List<String> passIn, PropositionalFunction pf) {
			this.passenger = passIn;
			this.pf = pf;
		}

		@Override
		public boolean satisfies(State s) {

			for(int i = 0; i < this.passenger.size(); i++){
				String p = this.passenger.get(i);
				

				GroundedProp gp = new GroundedProp(pf, new String[]{p});
				if(!gp.isTrue(s)){
					return false;
				}
			}
			return true;
		}

	}


	public static class l2PolicyGenerator implements AMDPPolicyGenerator{

		private Domain l2;
		public l2PolicyGenerator(Domain l2In){
			l2 = l2In;
		}

		@Override
		public Policy generatePolicy(State s, RewardFunction rf, TerminalFunction tf) {
			ValueIteration vi = new ValueIteration(l2, rf, tf, 0.99, new SimpleHashableStateFactory(false), 0.01, 100);
			Policy p = vi.planFromState(s);
			return p;
		}

	}

	public static class l1PolicyGenerator implements AMDPPolicyGenerator{

		private Domain l1;
		protected final double discount = 0.99;
		
		public l1PolicyGenerator(Domain l2In){
			l1 = l2In;
		}

		@Override
		public Policy generatePolicy(State s, RewardFunction rf, TerminalFunction tf) {
			ValueIteration vi = new ValueIteration(l1, rf, tf, discount, new SimpleHashableStateFactory(false), 0.01, 100);
			Policy p = vi.planFromState(s);			
			return p;
			
		}

	}

	public static class l0PolicyGenerator implements AMDPPolicyGenerator{

		private Domain l0;
		private final double discount = 0.99;
		public l0PolicyGenerator(Domain l0In){
			l0 = l0In;
		}

		@Override
		public Policy generatePolicy(State s, RewardFunction rf, TerminalFunction tf) {
//			ValueIteration vi = new ValueIteration(l0, rf, tf, 0.99, new SimpleHashableStateFactory(false), 0.01, 100);
//			Policy p = vi.planFromState(s);
//			//			System.out.println(s.toString());
//			return p;

			BoundedRTDP brtdp = new BoundedRTDP(l0, rf, tf, discount, new SimpleHashableStateFactory(false),
					new ValueFunctionInitialization.ConstantValueFunctionInitialization(0.),
					new ValueFunctionInitialization.ConstantValueFunctionInitialization(1.),
					0.001,
					50);

			brtdp.setMaxRolloutDepth(50);
			brtdp.toggleDebugPrinting(false);
			brtdp.planFromState(s);

			return new GreedyReplan(brtdp);
		}

	}
}
