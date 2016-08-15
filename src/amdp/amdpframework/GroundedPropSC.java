package amdp.amdpframework;


import burlap.mdp.auxiliary.stateconditiontest.StateConditionTest;
import burlap.mdp.core.oo.propositional.GroundedProp;
import burlap.mdp.core.oo.state.OOState;
import burlap.mdp.core.state.State;

/**
 * Checks a Grounded Proposition function for certain grounded conditions in states. They can
 * be used in planners to specify a 
 * @author James MacGlashan.
 *
 */
public class GroundedPropSC implements StateConditionTest {

		public GroundedProp gp;

		public GroundedPropSC(GroundedProp gp) {
			this.gp = gp;
		}

		@Override
		public boolean satisfies(State s) {
			//			if(gp.isTrue(s)){
			//				System.out.println("goal!!");
			//			}
			return gp.isTrue((OOState) s);
		}
	}
	