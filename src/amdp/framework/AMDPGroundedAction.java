package amdp.framework;

import burlap.oomdp.core.AbstractGroundedAction;
import burlap.oomdp.core.TerminalFunction;
import burlap.oomdp.singleagent.RewardFunction;
/**
 * An AMDP action must be able to return a reward function and a terminal function to the lower state abstraction. 
 * This interface mandates the methods returning the terminal function and reward function stored by AMDP grounded actions.
 * @author ngopalan
 *
 */
public interface AMDPGroundedAction extends AbstractGroundedAction{

	public RewardFunction getRF();
	public TerminalFunction getTF();
	
}
