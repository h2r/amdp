package amdp.framework;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import burlap.oomdp.core.Domain;
import burlap.oomdp.core.TerminalFunction;
import burlap.oomdp.core.states.State;

import burlap.oomdp.singleagent.Action;
import burlap.oomdp.singleagent.GroundedAction;
import burlap.oomdp.singleagent.RewardFunction;
import burlap.oomdp.singleagent.common.SimpleAction;
import burlap.oomdp.singleagent.common.SimpleGroundedAction;

public abstract class AMDPSimpleAction extends SimpleAction{

	public static class SimpleGroundedAMDPAction extends SimpleGroundedAction implements AMDPGroundedAction{
		public SimpleGroundedAMDPAction(Action inputAction){
			super(inputAction);
		}
		

		
		@Override
		public GroundedAction copy() {
			return new SimpleGroundedAMDPAction(this.action);
		}


		@Override
		public RewardFunction getRF() {
			return ((AMDPSimpleAction)this.action).getRF();
		}


		@Override
		public TerminalFunction getTF() {
			return ((AMDPSimpleAction)this.action).getTF();
		}
	}
	
	public AMDPSimpleAction() {
		
	}
	
	public AMDPSimpleAction(String name, Domain domain){
		super(name, domain);
	}
	
	public abstract RewardFunction getRF();
	public abstract TerminalFunction getTF();
		
	@Override
	public List<GroundedAction> getAllApplicableGroundedActions(State s) {
		GroundedAction ga = new SimpleGroundedAMDPAction(this);
		return this.applicableInState(s, ga) ? Arrays.asList(ga) : new ArrayList<GroundedAction>(0);
	}
	
	@Override
	public GroundedAction getAssociatedGroundedAction() {
		return new SimpleGroundedAMDPAction(this);
	}

}
