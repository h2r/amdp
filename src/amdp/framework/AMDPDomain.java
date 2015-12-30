package amdp.framework;

import burlap.oomdp.auxiliary.StateMapping;

/**
 * This is an interface that AMDP domains should satisfy. An AMDP domain must be able to return a state 
 * mapper that returns a lower level state 
 * @author ngopalan
 *
 */
public interface AMDPDomain {
	
	public StateMapping getStateMapper();

}
