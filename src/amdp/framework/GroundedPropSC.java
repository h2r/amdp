package amdp.framework;
import burlap.oomdp.auxiliary.stateconditiontest.StateConditionTest;
import burlap.oomdp.core.GroundedProp;
import burlap.oomdp.core.states.State;


/**
 * Checks a Grounded Proposition function for certain grounded conditions in states. They can
 * be used in planners to specify a 
 * @author James MacGlashan.
 *
 */
public class GroundedPropSC implements StateConditionTest{

		public GroundedProp gp;

		public GroundedPropSC(GroundedProp gp) {
			this.gp = gp;
		}

		@Override
		public boolean satisfies(State s) {
			//			if(gp.isTrue(s)){
			//				System.out.println("goal!!");
			//			}
			return gp.isTrue(s);
		}
	}
	