package amdp.framework;

import burlap.behavior.policy.Policy;
import burlap.behavior.singleagent.EpisodeAnalysis;
import burlap.debugtools.DPrint;
import burlap.oomdp.core.AbstractGroundedAction;
import burlap.oomdp.core.Domain;
import burlap.oomdp.core.TerminalFunction;
import burlap.oomdp.core.states.MutableState;
import burlap.oomdp.core.states.State;
import burlap.oomdp.singleagent.GroundedAction;
import burlap.oomdp.singleagent.RewardFunction;
import burlap.oomdp.singleagent.environment.Environment;
import burlap.oomdp.singleagent.environment.EnvironmentOutcome;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class AMDPAgent{

	List<Domain> DomainList;
	List<AMDPPolicyGenerator> PolicyGenerators;

	// This is a stack of states storing states at each level.
	List<State> StateStack = new ArrayList<State>();
	
	RewardFunction rf;
	TerminalFunction tf;
	
	protected int debugCode = 3214986;
	
	protected int stepCount = 0;
	
	protected int maxLevel;

	protected List<List<AbstractGroundedAction>> policyStack;
	



	public AMDPAgent(List<Domain> inputDomainList, List<AMDPPolicyGenerator> inputPolicyGenerators, RewardFunction inputRF, TerminalFunction inputTF){
		
		if(inputDomainList.size()!=inputPolicyGenerators.size()){
			System.err.print("The number of domains ("+ inputDomainList + "), is not equal to the number of policy generators(" + inputPolicyGenerators+")");
			System.exit(-10);
		}
		
		this.DomainList = inputDomainList;
		this.PolicyGenerators = inputPolicyGenerators;
		this.rf = inputRF;
		this.tf = inputTF;
		for (int i = 0; i < this.DomainList.size(); i++) {
			StateStack.add(new MutableState());
			}
		this.maxLevel = this.DomainList.size()-1;

		this.policyStack = new ArrayList<List<AbstractGroundedAction>>(inputDomainList.size());
		for(int i = 0; i < inputDomainList.size(); i++){
			this.policyStack.add(new ArrayList<AbstractGroundedAction>());
		}
	}

	public List<List<AbstractGroundedAction>> getPolicyStack() {
		return policyStack;
	}

	public EpisodeAnalysis actUntilTermination(Environment env){

		return this.actUntilTermination(env, -1);

	}
	
	public EpisodeAnalysis actUntilTermination(Environment env, int maxSteps){
		
		State baseState = env.getCurrentObservation();

		StateStack.set(0, baseState);
		

		
		EpisodeAnalysis ea = new EpisodeAnalysis(baseState);
		
		for(int i = 1; i< DomainList.size();i++){
			AMDPDomain d = (AMDPDomain)DomainList.get(i);
			baseState = d.getStateMapper().mapState(baseState);
			StateStack.set(i, baseState);
		}
		
		
		
		decompose(env, 2, rf, tf, maxSteps, ea);
		
		
		return ea;
	}
	
	protected void decompose(Environment env, int level, RewardFunction rf, TerminalFunction tf, int maxSteps, EpisodeAnalysis ea){
		State s = StateStack.get(level);
		Policy pi = PolicyGenerators.get(level).generatePolicy(s, rf, tf);
		if(level !=0){
			while(!tf.isTerminal(s) && (stepCount < maxSteps || maxSteps == -1)){
				AMDPGroundedAction a = (AMDPGroundedAction)pi.getAction(s);
				String str = StringUtils.repeat("	", maxLevel - level);
				str = str + a.toString();
				DPrint.cl(debugCode , str);
				this.policyStack.get(maxLevel - level).add(a);
				decompose(env, level - 1, a.getRF(), a.getTF(), maxSteps, ea);
				s = StateStack.get(level);
			}
		}
		else{
			while((!env.isInTerminalState() && !tf.isTerminal(s) )&& (stepCount < maxSteps || maxSteps == -1)){
				// this is a grounded action at the base level
				GroundedAction ga = (GroundedAction) pi.getAction(s);
				this.policyStack.get(maxLevel - level).add(ga);
				
				EnvironmentOutcome eo = env.executeAction(ga);

				String str = StringUtils.repeat("	", maxLevel - level);
				str = str + ga.toString();
				DPrint.cl(debugCode , str);
				ea.recordTransitionTo(ga, eo.op, eo.r);
				StateStack.set(level, eo.op);
				s = eo.op;	
				stepCount++;
			}
			
		}
		
		if(level < DomainList.size() -1){
			// project state up and getting new next state after running a policy to termination
			StateStack.set(level+1, ((AMDPDomain)DomainList.get(level+1)).getStateMapper().mapState(StateStack.get(level)));
		}

		this.policyStack.get(maxLevel - level).clear();
	}


}
