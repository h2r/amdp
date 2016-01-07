package amdp.tools;

import burlap.oomdp.core.AbstractGroundedAction;

import java.util.List;

/**
 * @author James MacGlashan.
 */
public interface StackObserver {
	void updatePolicyStack(List<List<AbstractGroundedAction>> policyStack);
}
