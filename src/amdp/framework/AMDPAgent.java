package amdp.framework;

import java.util.List;

import burlap.behavior.policy.Policy;
import burlap.behavior.singleagent.EpisodeAnalysis;
import burlap.oomdp.core.Domain;
import burlap.oomdp.core.TerminalFunction;
import burlap.oomdp.core.states.State;
import burlap.oomdp.singleagent.GroundedAction;
import burlap.oomdp.singleagent.RewardFunction;
import burlap.oomdp.singleagent.environment.Environment;
import burlap.oomdp.singleagent.environment.EnvironmentOutcome;

public class AMDPAgent{

	List<Domain> DomainList;
	List<AMDPPolicyGenerator> PolicyGenerators;

	// This is a stack of states storing states at each level.
	List<State> StateStack;
	Environment env;
	
	RewardFunction rf;
	TerminalFunction tf;
	
	int stepCount = 0;
	
	



	public AMDPAgent(List<Domain> inputDomainList, List<AMDPPolicyGenerator> inputPolicyGenerators, RewardFunction inputRF, TerminalFunction inputTF){
		
		if(inputDomainList.size()!=inputPolicyGenerators.size()){
			System.err.print("The number of domains ("+ inputDomainList + "), is not equal to the number of policy generators(" + inputPolicyGenerators+")");
			System.exit(-10);
		}
		
		this.DomainList = inputDomainList;
		this.PolicyGenerators = inputPolicyGenerators;
		this.rf = inputRF;
		this.tf = inputTF;
		
	}
	
	public EpisodeAnalysis actUntilTermination(Environment env){

		return this.actUntilTermination(env, -1);

	}
	
	public EpisodeAnalysis actUntilTermination(Environment env, int maxSteps){
		
		State baseState = env.getCurrentObservation();
		StateStack.add(0, baseState);
		
		
		
		for(int i = 1; i< DomainList.size();i++){
			AMDPDomain d = (AMDPDomain)DomainList.get(i);
			baseState = d.getStateMapper().mapState(baseState);
			StateStack.add(i, baseState);
		}
		
		
		
		EpisodeAnalysis ea = new EpisodeAnalysis(baseState);
		
		
		
		return ea;
	}
	
	protected void decompose(Environment env, int level, RewardFunction rf, TerminalFunction tf, int maxSteps, EpisodeAnalysis ea){
		State s = StateStack.get(level);
		Policy pi = PolicyGenerators.get(level).generatePolicy(s, rf, tf);
		if(level !=0){
			while(!tf.isTerminal(s)){
				AMDPGroundedAction a = (AMDPGroundedAction)pi.getAction(s);
				decompose(env, level-1, a.getRF(), a.getTF(), maxSteps, ea);
				s = StateStack.get(level);
			}
		}
		else{
			while((!env.isInTerminalState() || !tf.isTerminal(s) )&& (stepCount < maxSteps || maxSteps == -1)){
				// this is a grounded action at the base level
				GroundedAction ga = (GroundedAction) pi.getAction(s);
				EnvironmentOutcome eo = env.executeAction(ga);
				ea.recordTransitionTo(ga, eo.op, eo.r);
				StateStack.add(level, eo.op);
				s = eo.op;	
				stepCount++;
			}
			
		}
		
		if(level < StateStack.size()-1){
			// project state up and getting new next state after running a policy to termination
			StateStack.add(level+1, ((AMDPDomain)DomainList.get(level+1)).getStateMapper().mapState(StateStack.get(level)));
		}
	}


}
