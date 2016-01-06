package amdp.framework;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import amdp.framework.AMDPSimpleAction.SimpleGroundedAMDPAction;
import burlap.oomdp.core.AbstractObjectParameterizedGroundedAction;
import burlap.oomdp.core.Domain;
import burlap.oomdp.core.TerminalFunction;
import burlap.oomdp.core.states.State;
import burlap.oomdp.singleagent.Action;
import burlap.oomdp.singleagent.GroundedAction;
import burlap.oomdp.singleagent.ObjectParameterizedAction;
import burlap.oomdp.singleagent.RewardFunction;
import burlap.oomdp.singleagent.ObjectParameterizedAction.ObjectParameterizedGroundedAction;
import burlap.oomdp.singleagent.common.SimpleGroundedAction;

public abstract class ObjectParameterizedAMDPAction extends ObjectParameterizedAction{

	
	public ObjectParameterizedAMDPAction(String name, Domain domain, String[] parameterClasses) {
		super(name, domain, parameterClasses);
		// TODO Auto-generated constructor stub 
	}
	

	public abstract RewardFunction getRF(ObjectParameterizedAMDPGroundedAction ga);
	public abstract TerminalFunction getTF(ObjectParameterizedAMDPGroundedAction ga);
	
	@Override
	public GroundedAction getAssociatedGroundedAction() {
		return new ObjectParameterizedAMDPGroundedAction(this);
	}
	
	@Override
	public List<GroundedAction> getAllApplicableGroundedActions(State s) {
		List <GroundedAction> res = new ArrayList<GroundedAction>();


		if(this.parameterClasses.length == 0){
			//parameterless action for some reason...
			GroundedAction ga = new SimpleGroundedAMDPAction(this);
			if(this.applicableInState(s, ga)){
				res.add(new SimpleGroundedAMDPAction(this));
			}
			return res; //no parameters to ground
		}



		//otherwise need to do parameter binding
		List <List <String>> bindings = s.getPossibleBindingsGivenParamOrderGroups(this.getParameterClasses(), this.getParameterOrderGroups());

		for(List <String> params : bindings){
			String [] aprams = params.toArray(new String[params.size()]);
			ObjectParameterizedAMDPGroundedAction ga = new ObjectParameterizedAMDPGroundedAction(this, aprams);
			if(this.applicableInState(s, ga)){
				res.add(ga);
			}
		}

		return res;
	}
	
	
	public static class ObjectParameterizedAMDPGroundedAction extends GroundedAction implements AbstractObjectParameterizedGroundedAction, AMDPGroundedAction {

		public String [] params;
		
		public ObjectParameterizedAMDPGroundedAction(Action action) {
			super(action);
			//TODO: check this constructor
		}
		
		public ObjectParameterizedAMDPGroundedAction(Action action, String [] params) {
			super(action);
			this.params = params;
		}

		
		
		@Override
		public void initParamsWithStringRep(String[] inputParams) {
			this.params = inputParams;
		}

		@Override
		public String[] getParametersAsString() {
			return this.params;
		}

		@Override
		public RewardFunction getRF() {
			return ((ObjectParameterizedAMDPAction)this.action).getRF(this);
		}

		@Override
		public TerminalFunction getTF() {
			return ((ObjectParameterizedAMDPAction)this.action).getTF(this);
		}

		@Override
		public String[] getObjectParameters() {
			return params;
		}

		@Override
		public void setObjectParameters(String[] inputParams) {
			this.params = inputParams;
		}

		@Override
		public boolean actionDomainIsObjectIdentifierIndependent() {
			return ((ObjectParameterizedAMDPAction)this.action).parametersAreObjectIdentifierIndependent();
		}
		
		@Override
		public String toString() {
			StringBuilder buf = new StringBuilder();
			buf.append(action.getName());
			for(int i = 0; i < params.length; i++){
				buf.append(" ").append(params[i]);
			}

			return buf.toString();
		}

		@Override
		public boolean equals(Object other) {
			if(this == other){
				return true;
			}

			if(!(other instanceof ObjectParameterizedAMDPGroundedAction)){
				return false;
			}

			ObjectParameterizedAMDPGroundedAction go = (ObjectParameterizedAMDPGroundedAction)other;

			if(!this.action.getName().equals(go.action.getName())){
				return false;
			}

			String [] pog = ((ObjectParameterizedAMDPAction)this.action).getParameterOrderGroups();

			for(int i = 0; i < this.params.length; i++){
				String p = this.params[i];
				String orderGroup = pog[i];
				boolean foundMatch = false;
				for(int j = 0; j < go.params.length; j++){
					if(p.equals(go.params[j]) && orderGroup.equals(pog[j])){
						foundMatch = true;
						break;
					}
				}
				if(!foundMatch){
					return false;
				}
			}

			return true;
		}
		
		@Override
		public GroundedAction copy() {
			return new ObjectParameterizedAMDPGroundedAction(this.action, params.clone());
		}
		
	}

}
