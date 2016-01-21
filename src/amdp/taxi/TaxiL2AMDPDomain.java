package amdp.taxi;

import java.util.ArrayList;
import java.util.List;

import amdp.framework.FullyObservableSingleAgentAMDPDomain;
import amdp.framework.ObjectParameterizedAMDPAction;
import amdp.framework.ObjectParameterizedAMDPAction.ObjectParameterizedAMDPGroundedAction;
import amdp.taxi.TaxiL1AMDPDomain.GroundedPropSC;
import burlap.behavior.policy.Policy;
import burlap.behavior.singleagent.EpisodeAnalysis;
import burlap.behavior.singleagent.planning.stochastic.valueiteration.ValueIteration;
import burlap.oomdp.auxiliary.DomainGenerator;
import burlap.oomdp.auxiliary.StateMapping;
import burlap.oomdp.auxiliary.common.GoalConditionTF;
import burlap.oomdp.auxiliary.stateconditiontest.StateConditionTest;
import burlap.oomdp.core.Attribute;
import burlap.oomdp.core.Domain;
import burlap.oomdp.core.GroundedProp;
import burlap.oomdp.core.ObjectClass;
import burlap.oomdp.core.TerminalFunction;
import burlap.oomdp.core.TransitionProbability;
import burlap.oomdp.core.objects.MutableObjectInstance;
import burlap.oomdp.core.objects.ObjectInstance;
import burlap.oomdp.core.states.MutableState;
import burlap.oomdp.core.states.State;
import burlap.oomdp.singleagent.FullActionModel;
import burlap.oomdp.singleagent.GroundedAction;
import burlap.oomdp.singleagent.RewardFunction;
import burlap.oomdp.singleagent.common.GoalBasedRF;
import burlap.oomdp.statehashing.SimpleHashableStateFactory;

public class TaxiL2AMDPDomain implements DomainGenerator {

	// states here have locations, taxis in locations and passengers in them or not
	// actions navigate, pick, put
	// problem at this level - get to the red location

	//TODO: locations do not change in the passengers at the base level need to fix that
	//TODO:

	public static final String								INTAXIATT = "inTaxiAtt";
	//	public static final String								OCCUPIEDTAXIATT = "occupiedTaxiAtt";
	public static final String								LOCATIONATT = "locationAtt";

	//	public static final String								TAXICLASS = "taxi2L";
	public static final String								LOCATIONCLASS = "location2L";
	public static final String								PASSENGERCLASS = "passenger2L";

	//	public static final String								TAXIATLOCATIONPF = "taxiAt";
	public static final String								PASSENGERATLOCATIONPF = "passengerAt";
	public static final String								TAXIATPASSENGERPF = "taxiAtPassenger";
	public static final String								PASSENGERINTAXI = "inTaxi";

	public static final String								GETACTION = "getAction";
	public static final String								PUTACTION = "putAction";


	protected Domain d1 = null;

	public TaxiL2AMDPDomain(Domain d1In){
		d1 = d1In;
	}

	@Override
	public Domain generateDomain() {
		//TODO: implement code here
		Domain domain = new FullyObservableSingleAgentAMDPDomain();

		Attribute inTaxiAtt = new Attribute(domain, INTAXIATT, Attribute.AttributeType.BOOLEAN);

		//		Attribute occupiedTaxiAtt = new Attribute(domain, OCCUPIEDTAXIATT, Attribute.AttributeType.BOOLEAN);

		Attribute locationAtt = new Attribute(domain, LOCATIONATT, Attribute.AttributeType.DISC);
		List<String> locationTypes = new ArrayList<String>();
		locationTypes.add("fuel");
		locationTypes.add("red");
		locationTypes.add("green");
		locationTypes.add("blue");
		locationTypes.add("yellow");
		locationTypes.add("magenta");
		locationTypes.add("pink");
		locationTypes.add("orange");
		locationTypes.add("cyan");
		locationTypes.add("onRoad");
		locationAtt.setDiscValues(locationTypes);

		//		ObjectClass taxi = new ObjectClass(domain, TAXICLASS);

		//		taxi.addAttribute(occupiedTaxiAtt);
		//		taxi.addAttribute(locationAtt);

		ObjectClass location = new ObjectClass(domain, LOCATIONCLASS);
		location.addAttribute(locationAtt);

		ObjectClass passenger = new ObjectClass(domain, PASSENGERCLASS);
		passenger.addAttribute(locationAtt);
		passenger.addAttribute(inTaxiAtt);

		new GetAction(GETACTION, domain, new String[]{PASSENGERCLASS});
		new PutAction(PUTACTION, domain, new String[]{PASSENGERCLASS, LOCATIONCLASS});

		StateMapping sm = new StateMapperL2(domain);
		((FullyObservableSingleAgentAMDPDomain)domain).setStateMapper(sm);

		return domain;
	}

	public class StateMapperL2 implements StateMapping{
		private Domain d;
		public StateMapperL2(Domain dIn){
			super();
			d = dIn;
		}

		@Override
		public State mapState(State s) {
			return getMappedState(s, d);
		}

	}

	public static State getMappedState(State s, Domain cd){

		State as = new MutableState();
		// get taxi keep empty and location attributes
		// get locations
		// get passenger and set them in locations and in taxi attributes to x and y
		//		ObjectClass oc = cd.getObjectClass(TAXICLASS);


		List<ObjectInstance> locations = s.getObjectsOfClass(TaxiL1AMDPDomain.LOCATIONCLASS);
		for(ObjectInstance l : locations){
			ObjectInstance al = new MutableObjectInstance(cd.getObjectClass(LOCATIONCLASS), l.getName());
			al.setValue(LOCATIONATT, l.getIntValForAttribute(TaxiL1AMDPDomain.LOCATIONATT));
			as.addObject(al);
		}

		List<ObjectInstance> passengers = s.getObjectsOfClass(TaxiL1AMDPDomain.PASSENGERCLASS);
		for(ObjectInstance p : passengers){
			ObjectInstance ap = new MutableObjectInstance(cd.getObjectClass(PASSENGERCLASS), p.getName());
			ap.setValue(LOCATIONATT, p.getIntValForAttribute(TaxiL1AMDPDomain.LOCATIONATT));
			ap.setValue(INTAXIATT, p.getIntValForAttribute(TaxiL1AMDPDomain.INTAXIATT));
			as.addObject(ap);
		}

		//		ObjectInstance taxiC = new MutableObjectInstance(cd.getObjectClass(TAXICLASS), TAXICLASS+0);

		ObjectInstance taxi = s.getFirstObjectOfClass(TaxiL1AMDPDomain.TAXICLASS);

		//		taxiC.setValue(OCCUPIEDTAXIATT, taxi.getBooleanValForAttribute(TaxiL1AMDPDomain.OCCUPIEDTAXIATT));

		//		taxiC.setValue(LOCATIONATT, taxi.getIntValForAttribute(TaxiL1AMDPDomain.LOCATIONATT));


		//		as.addObject(taxiC);

		return as;
	}

	public class GetAction extends ObjectParameterizedAMDPAction implements FullActionModel {

		public GetAction(String actionName, Domain domain, String[] parameterClasses){
			super(actionName, domain, parameterClasses);
		}

		@Override
		protected State performActionHelper(State s, GroundedAction ga) {

			ObjectParameterizedAMDPGroundedAction oga = (ObjectParameterizedAMDPGroundedAction)ga;
			// oga.prams[0] is the name of the passenger - like passenger1

			//			ObjectInstance taxi = s.getFirstObjectOfClass(TAXICLASS);
			ObjectInstance passenger = s.getObject(oga.params[0]);
//			int i = passenger.getIntValForAttribute(LOCATIONATT);
			//			taxi.setValue(LOCATIONATT, i);

			//			taxi.setValue(OCCUPIEDTAXIATT, 1);
			passenger.setValue(INTAXIATT, 1);


			return s;
		}

		@Override
		public RewardFunction getRF(ObjectParameterizedAMDPGroundedAction ga) {
			ObjectParameterizedAMDPGroundedAction oga = (ObjectParameterizedAMDPGroundedAction)ga;
			StateConditionTest sc =  new GroundedPropSC(new GroundedProp(d1.getPropFunction(TaxiL1AMDPDomain.TAXIATPASSENGERPF), new String[]{oga.params[0]}));
			return new GoalBasedRF(sc, 1.0,0.);
		}

		@Override
		public TerminalFunction getTF(ObjectParameterizedAMDPGroundedAction ga) {
			ObjectParameterizedAMDPGroundedAction oga = (ObjectParameterizedAMDPGroundedAction)ga;
			StateConditionTest sc =  new GroundedPropSC(new GroundedProp(d1.getPropFunction(TaxiDomain.TAXIATPASSENGERPF), new String[]{oga.params[0]}));
			return new GoalConditionTF(sc);
		}

		@Override
		public boolean parametersAreObjectIdentifierIndependent() {
			return false;
		}

		@Override
		public boolean applicableInState(State s, GroundedAction groundedAction) {
			return true;
		}

		@Override
		public boolean isPrimitive() {
			return true;
		}
		@Override
		public List<TransitionProbability> getTransitions(State s, GroundedAction groundedAction) {
			List<TransitionProbability> tpList = new ArrayList<TransitionProbability> ();
			tpList.add(new TransitionProbability(this.performAction(s, groundedAction), 1.));
			return tpList;
		}



	}

	public class PutAction extends ObjectParameterizedAMDPAction implements FullActionModel {

		public PutAction(String actionName, Domain domain, String[] parameterClasses){
			super(actionName, domain, parameterClasses);
		}

		@Override
		protected State performActionHelper(State s, GroundedAction ga) {

			ObjectParameterizedAMDPGroundedAction oga = (ObjectParameterizedAMDPGroundedAction)ga;
			// oga.prams[0] is the name of the location - like location1

			//			ObjectInstance taxi = s.getFirstObjectOfClass(TAXICLASS);
			ObjectInstance passenger = s.getObject(oga.params[0]);
			//			if(taxi.getBooleanValForAttribute(OCCUPIEDTAXIATT) && passenger.getBooleanValForAttribute(INTAXIATT)){
			if(passenger.getBooleanValForAttribute(INTAXIATT)){


				// param 1 is location
				int i = s.getObject(oga.params[1]).getIntValForAttribute(LOCATIONATT);
				//				taxi.setValue(LOCATIONATT, i);
				passenger.setValue(LOCATIONATT, i);
				//				taxi.setValue(OCCUPIEDTAXIATT, 0);
				passenger.setValue(INTAXIATT, 0);
			}

			return s;
		}

		@Override
		public RewardFunction getRF(ObjectParameterizedAMDPGroundedAction ga) {
			ObjectParameterizedAMDPGroundedAction oga = (ObjectParameterizedAMDPGroundedAction)ga;
			StateConditionTest sc =  new GroundedPropSC(new GroundedProp(d1.getPropFunction(TaxiL1AMDPDomain.PASSENGERATLOCATIONPF), new String[]{oga.params[0], oga.params[1]}));
			return new GoalBasedRF(sc, 1.0,0.);
		}

		@Override
		public TerminalFunction getTF(ObjectParameterizedAMDPGroundedAction ga) {
			ObjectParameterizedAMDPGroundedAction oga = (ObjectParameterizedAMDPGroundedAction)ga;
			StateConditionTest sc =  new GroundedPropSC(new GroundedProp(d1.getPropFunction(TaxiL1AMDPDomain.PASSENGERATLOCATIONPF), new String[]{oga.params[0], oga.params[1]}));
			return new GoalConditionTF(sc);
		}

		@Override
		public boolean parametersAreObjectIdentifierIndependent() {
			return false;
		}

		@Override
		public boolean applicableInState(State s, GroundedAction groundedAction) {
			return true;
		}

		@Override
		public boolean isPrimitive() {
			return true;
		}
		@Override
		public List<TransitionProbability> getTransitions(State s, GroundedAction groundedAction) {
			List<TransitionProbability> tpList = new ArrayList<TransitionProbability> ();
			tpList.add(new TransitionProbability(this.performAction(s, groundedAction), 1.));
			return tpList;
		}



	}

	public static class InLocationSC implements StateConditionTest{

		String srcOb;
		String targetOb;

		public InLocationSC(String srcOb, String targetOb) {
			this.srcOb = srcOb;
			this.targetOb = targetOb;
		}

		@Override
		public boolean satisfies(State s) {
			ObjectInstance src = s.getObject(this.srcOb);
			boolean inTaxi = src.getBooleanValForAttribute(INTAXIATT);
			return src.getStringValForAttribute(LOCATIONATT).equals(targetOb) && !inTaxi;
		}
	}


	public static void main(String[] args) {
		TaxiDomain d0Gen = new TaxiDomain();
		Domain d0 = d0Gen.generateDomain();
		State s = TaxiDomain.getClassicState(d0);


		TaxiL1AMDPDomain d1Gen = new TaxiL1AMDPDomain(d0);
		Domain d1 = d1Gen.generateDomain();
		State as = TaxiL1AMDPDomain.getMappedState(s, d1);
		System.out.println(as.getCompleteStateDescription());

		TaxiL2AMDPDomain d2Gen = new TaxiL2AMDPDomain(d1);
		Domain d2 = d2Gen.generateDomain();
		State as2 = TaxiL2AMDPDomain.getMappedState(as, d2);

		StateConditionTest sc = new InLocationSC("passenger0", "yellow");
		RewardFunction rf = new GoalBasedRF(sc, 1.);
		TerminalFunction tf = new GoalConditionTF(sc);

		ValueIteration vi = new ValueIteration(d2, rf, tf, 0.99, new SimpleHashableStateFactory(false), 0.01, 100);
		Policy p = vi.planFromState(as2);
		EpisodeAnalysis ea = p.evaluateBehavior(as2, rf, tf, 15);
		System.out.println(ea.getActionSequenceString("\n"));
		for(int i =0;i<ea.stateSequence.size();i++){
			System.out.println(ea.getState(i));
		}


	}


}
