package amdp.framework;

import java.util.ArrayList;
import java.util.List;

import amdp.tools.StackObserver;
import burlap.oomdp.core.AbstractGroundedAction;
import burlap.oomdp.core.Domain;
import burlap.oomdp.core.TerminalFunction;
import burlap.oomdp.core.states.State;
import burlap.oomdp.singleagent.RewardFunction;
import burlap.oomdp.singleagent.pomdp.PODomain;

public class POAMDPAgent {
	List<PODomain> DomainList;
	List<AMDPPolicyGenerator> PolicyGenerators;

	// This is a stack of states storing states at each level.
	List<State> StateStack = new ArrayList<State>();
	
	RewardFunction rf;
	TerminalFunction tf;
	
	protected int debugCode = 3214986;
	
	protected int stepCount = 0;
	
	protected int maxLevel;

	protected List<List<AbstractGroundedAction>> policyStack;

	protected StackObserver onlineStackObserver;

}
