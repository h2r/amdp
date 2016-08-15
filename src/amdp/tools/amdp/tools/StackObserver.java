package amdp.tools;


import burlap.mdp.core.action.Action;

import java.util.List;

/**
 * @author James MacGlashan.
 */
public interface StackObserver {
	void updatePolicyStack(List<List<Action>> policyStack);
}
