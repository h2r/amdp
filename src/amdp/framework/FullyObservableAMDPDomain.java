package amdp.framework;

import java.util.List;

import burlap.oomdp.auxiliary.StateMapping;
import burlap.oomdp.core.Domain;
import burlap.oomdp.singleagent.Action;
import burlap.oomdp.stochasticgames.agentactions.SGAgentAction;

public class FullyObservableAMDPDomain extends Domain implements AMDPDomain{

	//TODO: fill this up but how????
	
	@Override
	public StateMapping getStateMapper() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected Domain newInstance() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void addAction(Action act) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void addSGAgentAction(SGAgentAction sa) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public List<Action> getActions() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<SGAgentAction> getAgentActions() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Action getAction(String name) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public SGAgentAction getSingleAction(String name) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public SGAgentAction getSGAgentAction(String name) {
		// TODO Auto-generated method stub
		return null;
	}

}
