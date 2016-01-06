package amdp.framework;

import burlap.oomdp.auxiliary.StateMapping;
import burlap.oomdp.singleagent.pomdp.PODomain;

public class POAMDPDomain extends PODomain implements AMDPDomain{

	protected StateMapping stateMapper;

	@Override
	public StateMapping getStateMapper() {
		return stateMapper;
	}

	public void setStateMapper(StateMapping sm){
		stateMapper = sm;
	}


}
