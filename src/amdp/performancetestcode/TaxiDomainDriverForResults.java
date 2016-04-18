package amdp.performancetestcode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

//import amdp.cleanupdomain.CleanupDomainDriverWithBaseLevelComparison.GreedyReplan;
import amdp.framework.AMDPAgent;
import amdp.framework.AMDPPolicyGenerator;
import amdp.performancetestcode.BoundedRTDPForTests.StateSelectionMode;
import amdp.taxi.TaxiDomain;
import amdp.taxi.TaxiL1AMDPDomain;
import amdp.taxi.TaxiL2AMDPDomain;
import amdp.taxi.TaxiL2AMDPDomain.InLocationSC;
import amdp.taxi.TaxiVisualizer;
import burlap.behavior.policy.GreedyQPolicy;
import burlap.behavior.policy.Policy;
import burlap.behavior.singleagent.EpisodeAnalysis;
import burlap.behavior.singleagent.auxiliary.EpisodeSequenceVisualizer;
import burlap.behavior.singleagent.planning.Planner;
import burlap.behavior.valuefunction.QValue;
import burlap.behavior.valuefunction.ValueFunctionInitialization;
import burlap.debugtools.DPrint;
import burlap.debugtools.RandomFactory;
import burlap.oomdp.auxiliary.common.GoalConditionTF;
import burlap.oomdp.auxiliary.stateconditiontest.StateConditionTest;
import burlap.oomdp.core.AbstractGroundedAction;
import burlap.oomdp.core.Domain;
import burlap.oomdp.core.GroundedProp;
import burlap.oomdp.core.PropositionalFunction;
import burlap.oomdp.core.TerminalFunction;
import burlap.oomdp.core.states.MutableState;
import burlap.oomdp.core.states.State;
import burlap.oomdp.singleagent.GroundedAction;
import burlap.oomdp.singleagent.RewardFunction;
import burlap.oomdp.singleagent.common.GoalBasedRF;
import burlap.oomdp.singleagent.environment.SimulatedEnvironment;
import burlap.oomdp.statehashing.SimpleHashableStateFactory;
import burlap.oomdp.visualizer.Visualizer;

public class TaxiDomainDriverForResults {
	//TODO: cap runs at 100 two plots then -> % completeness and number of steps taken for completed trajectories.
	
	
	public static List<BoundedRTDPForTests> brtdpList= new ArrayList<BoundedRTDPForTests>();
	public static List<Integer> brtdpLevelList= new ArrayList<Integer>();
	
	static int l0Budget = 50;
	static int l1Budget = 5;
	static int l2Budget = 5;

	private static int l0Depth = 30;
	private  static int l1Depth = 5;
	private static int l2Depth = 15;
	
	static int  maxTrajectoryLength = 101;
	
	static int baselevelDepth = 30;
	
	static protected MutableGlobalInteger bellmanBudget = new MutableGlobalInteger(-1);
	
	
	public static void main(String[] args) {

//		RandomFactory.seedMapped(0, 12345);//123
		DPrint.toggleCode(3214986, false);

		boolean runGroundLevelBoundedRTDP = false;

		
		
		bellmanBudget.setValue(4000);
		
		for(int i =0;i<args.length;i++){
			String str = args[i];
			if(str.equals("-r")){
				runGroundLevelBoundedRTDP = Boolean.parseBoolean(args[i+1]);
			}
			if(str.equals("-b")){
				bellmanBudget.setValue(Integer.parseInt(args[i+1]));
			}
		}
		
		String bellmanBudgetStartValue = bellmanBudget.getValue().toString();
//		System.out.println("start:");
		if(runGroundLevelBoundedRTDP ){
			TaxiDomain d0Gen = new TaxiDomain();
			d0Gen.includeFuel = false;
			Domain d0 = d0Gen.generateDomain();
			//		State s = TaxiDomain.getClassicState(d0);
			State s = TaxiDomain.getComplexState(d0);
			

			List<String> passengers  = new ArrayList<>();
			passengers.add("passenger0");
			passengers.add("passenger1");

			StateConditionTest l0sc = new L0Goal(passengers, d0.getPropFunction(TaxiDomain.PASSENGERATGOALLOCATIONPF));
			RewardFunction rfl0 = new GoalBasedRF(l0sc, 1.);
			TerminalFunction tfl0 = new GoalConditionTF(l0sc);
			double discount = 0.99;
			SimpleHashableStateFactory shf = new SimpleHashableStateFactory(false);
			
			BoundedRTDPForTests brtdp = new BoundedRTDPForTests(d0, rfl0, tfl0, discount, shf,
					new ValueFunctionInitialization.ConstantValueFunctionInitialization(0.),
					new ValueFunctionInitialization.ConstantValueFunctionInitialization(1.),
					0.001,
					-1);
			brtdp.setRemainingNumberOfBellmanUpdates(bellmanBudget);
//			brtdp.setStateSelectionMode(StateSelectionMode.WEIGHTEDMARGIN);
			brtdp.setMaxRolloutDepth(baselevelDepth);
			brtdp.toggleDebugPrinting(false);
			

			
//			brtdp.planFromState(s);
//			Policy p = new GreedyRTDPReplanerForTests(brtdp);
			Policy p = brtdp.planFromState(s);
//			System.out.println("StateHash: " + shf.hashState(s).hashCode());
//			System.out.println("Value = " + brtdp.value(s));
//			System.out.println("State: " + s.getCompleteStateDescription());
			
//			List<QValue> qvalues = brtdp.getQs(s); 
//			
//			
//			for(QValue q:qvalues){
//				System.out.println("action: " + q.a.actionName() + ", qvalue: " + q.q);
//				if(q.a.actionName().equals("east")){
//					State ns = ((GroundedAction)q.a).executeIn(s);
//					System.out.println("Next State hash: " + shf.hashState(ns).hashCode());
//					System.out.println("Next State : " + ns.getCompleteStateDescription());
//				}
//			}
			
//			if(bellmanBudget.getValue()>-1){
//				bellmanBudget.setValue(bellmanBudget.getValue()-brtdp.getNumberOfBellmanUpdates());
//			}
			
			
			SimulatedEnvironment env = new SimulatedEnvironment(d0, rfl0, tfl0, s);
			EpisodeAnalysis ea = p.evaluateBehavior(env, maxTrajectoryLength);

			Visualizer v = TaxiVisualizer.getVisualizer(5, 5);
			new EpisodeSequenceVisualizer(v, d0, Arrays.asList(ea));
			
			
			System.out.println(brtdp.getNumberOfBellmanUpdates());
			System.out.println(ea.numTimeSteps()-1);
			if(ea.numTimeSteps()-1 < maxTrajectoryLength){
				System.out.println(1);
			}
			else{
				System.out.println(0);
			}
			System.out.println(bellmanBudgetStartValue);
			System.out.println("Taxi With Baselevel");
			System.out.println(ea.stateSequence.size());
			System.out.println(ea.actionSequence.size());
//			System.out.println(bellmanBudget);
			
			
		}
		else{
			generateBehavior();
		
		}
		


	}


	public static void generateBehavior(){

		String bellmanBudgetStartValue = bellmanBudget.getValue().toString();
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
		
		
		AMDPAgent agent = new AMDPAgent(domainList, pgList, rfl2, tfl2);

		SimulatedEnvironment env = new SimulatedEnvironment(d0, rfl0, tfl0, s);

		EpisodeAnalysis ea = agent.actUntilTermination(env, maxTrajectoryLength);
		
		

//		Visualizer v = TaxiVisualizer.getVisualizer(5, 5);
//		new EpisodeSequenceVisualizer(v, d0, Arrays.asList(ea));
		
		int count = 0;
		int count0 =0;
		int count1 =0;
		int count2 =0;
		for(int i=0;i<brtdpList.size();i++){
			int numUpdates = brtdpList.get(i).getNumberOfBellmanUpdates();
			count+= numUpdates;
//			System.out.println("Level: " + brtdpLevelList.get(i) + ", count: " + numUpdates);
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

		
		
//		System.out.println(ea.numTimeSteps());
		
		System.out.println(count);
		System.out.println(ea.numTimeSteps()-1);
		if(ea.numTimeSteps()-1 < maxTrajectoryLength){
			System.out.println(1);
		}
		else{
			System.out.println(0);
		}
		System.out.println(bellmanBudgetStartValue);
		System.out.println("Taxi With AMDPs");
		System.out.println(count0);
		System.out.println(count1);
		System.out.println(count2);
		System.out.println(ea.stateSequence.size());
		System.out.println(ea.actionSequence.size());

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
		private double discount = 0.99;
		public l2PolicyGenerator(Domain l2In){
			l2 = l2In;
		}

		@Override
		public Policy generatePolicy(State s, RewardFunction rf, TerminalFunction tf) {
//			ValueIteration vi = new ValueIteration(l2, rf, tf, 0.99, new SimpleHashableStateFactory(false), 0.01, 100);
////			vi.toggleDebugPrinting(false);
//			Policy p = vi.planFromState(s);
//			return p;
			
			BoundedRTDPForTests brtdp = new BoundedRTDPForTests(l2, rf, tf, discount, new SimpleHashableStateFactory(false),
					new ValueFunctionInitialization.ConstantValueFunctionInitialization(0.),
					new ValueFunctionInitialization.ConstantValueFunctionInitialization(1.),
					0.001,
					-1);
			
			

			brtdp.setRemainingNumberOfBellmanUpdates(bellmanBudget);
			brtdpList.add(brtdp);
			brtdpLevelList.add(2);
			
			brtdp.setMaxRolloutDepth(l2Depth);//5
			brtdp.toggleDebugPrinting(false);
			Policy p = brtdp.planFromState(s);
			
//			if(bellmanBudget.getValue()>-1){
//				bellmanBudget.setValue(bellmanBudget.getValue()-brtdp.getNumberOfBellmanUpdates());
//			}
			
			return p;

//			return new GreedyReplan(brtdp);
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
//			ValueIteration vi = new ValueIteration(l1, rf, tf, discount, new SimpleHashableStateFactory(false), 0.01, 100);
////			vi.toggleDebugPrinting(false);
//			Policy p = vi.planFromState(s);		
//			return p;
			BoundedRTDPForTests brtdp = new BoundedRTDPForTests(l1, rf, tf, discount, new SimpleHashableStateFactory(false),
					new ValueFunctionInitialization.ConstantValueFunctionInitialization(0.),
					new ValueFunctionInitialization.ConstantValueFunctionInitialization(1.),
					0.001,
					-1);//10
			brtdp.setRemainingNumberOfBellmanUpdates(bellmanBudget);
			brtdpList.add(brtdp);
			brtdpLevelList.add(1);

			brtdp.setMaxRolloutDepth(l1Depth);//10
			brtdp.toggleDebugPrinting(false);
			Policy p =  brtdp.planFromState(s);

//			if(bellmanBudget.getValue()>-1){
//				bellmanBudget.setValue(bellmanBudget.getValue()-brtdp.getNumberOfBellmanUpdates());
//			}
			return p;
//			return new GreedyReplan(brtdp);
			
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

			BoundedRTDPForTests brtdp = new BoundedRTDPForTests(l0, rf, tf, discount, new SimpleHashableStateFactory(false),
					new ValueFunctionInitialization.ConstantValueFunctionInitialization(0.),
					new ValueFunctionInitialization.ConstantValueFunctionInitialization(1.),
					0.001,
					-1);//50
			brtdp.setRemainingNumberOfBellmanUpdates(bellmanBudget);
			brtdpList.add(brtdp);
			brtdpLevelList.add(0);

			brtdp.setMaxRolloutDepth(l0Depth);//15
			brtdp.toggleDebugPrinting(false);
			Policy p =  brtdp.planFromState(s);
//			if(bellmanBudget.getValue()>-1){
//				bellmanBudget.setValue(bellmanBudget.getValue()-brtdp.getNumberOfBellmanUpdates());
//			}

			return p;
//			return new GreedyReplan(brtdp);
		}

	}
	
	

}
