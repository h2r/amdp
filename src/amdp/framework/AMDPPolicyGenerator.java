package amdp.framework;

import burlap.behavior.policy.Policy;
import burlap.oomdp.core.states.State;

/**
 * This is a Policy Generating Interface for AMDPs. The purpose of such policy generators is to 
 * generate a policy for a lower level state abstraction in AMDPs given an AMDP action from a higher
 * state abstraction and a lower level state
 * @author ngopalan
 *
 */
public interface AMDPPolicyGenerator {

	public Policy generatePolicy(State s, AMDPGroundedAction a);
	
}
