package amdp.taxibadlocation;

import java.util.ArrayList;
import java.util.List;

import amdp.framework.AMDPSimpleDeterministicAction;
import amdp.framework.FullyObservableSingleAgentAMDPDomain;
import amdp.framework.ObjectParameterizedAMDPAction;
import amdp.taxibadlocation.TaxiDomain.PF_PickUp;
import amdp.taxibadlocation.TaxiDomain.PF_TaxiAtLoc;
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
import burlap.oomdp.core.PropositionalFunction;
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

public class TaxiL1AMDPDomain implements DomainGenerator {

	// states here have locations, taxis in locations and passengers in them or not
	// actions navigate, pick, put
	// we are avoiding the whole fuel attribute 

	public static final String								INTAXIATT = "inTaxiAtt";
	public static final String								OCCUPIEDTAXIATT = "occupiedTaxiAtt";
	public static final String								LOCATIONATT = "locationAtt";

	public static final String								TAXICLASS = "taxi1L";
	public static final String								LOCATIONCLASS = "location1L";
	public static final String								PASSENGERCLASS = "passenger1L";

	//	public static final String								TAXIATLOCATIONPF = "taxiAt";
	public static final String								PASSENGERATLOCATIONPF = "passengerAt";
	public static final String								TAXIATPASSENGERPF = "taxiAtPassenger";
	public static final String								PASSENGERINTAXI = "inTaxi";

	public static final String								PICKUPACTION = "pickup";
	public static final String								PUTDOWNACTION = "putdown";
	public static final String								NAVIGATEACTION = "navigate";


	protected Domain d0 = null;

	public TaxiL1AMDPDomain(Domain d0In){
		d0 = d0In;
	}

	@Override
	public Domain generateDomain() {
		Domain domain = new FullyObservableSingleAgentAMDPDomain();
		Attribute inTaxiAtt = new Attribute(domain, INTAXIATT, Attribute.AttributeType.BOOLEAN);

		Attribute occupiedTaxiAtt = new Attribute(domain, OCCUPIEDTAXIATT, Attribute.AttributeType.BOOLEAN);

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

		ObjectClass taxi = new ObjectClass(domain, TAXICLASS);
		taxi.addAttribute(locationAtt);
		taxi.addAttribute(occupiedTaxiAtt);

		ObjectClass location = new ObjectClass(domain, LOCATIONCLASS);
		location.addAttribute(locationAtt);

		ObjectClass passenger = new ObjectClass(domain, PASSENGERCLASS);
		passenger.addAttribute(locationAtt);
		passenger.addAttribute(inTaxiAtt);

		new PutDownAction(PUTDOWNACTION, domain);
		new PickupAction(PICKUPACTION, domain);
		new NavigateAction(NAVIGATEACTION, domain ,new String[]{LOCATIONCLASS});


		new PF_PassengerInTaxi(TAXIATPASSENGERPF, domain, new String[]{PASSENGERCLASS});
		new PF_PassengerInLocation(PASSENGERATLOCATIONPF, domain, new String[]{PASSENGERCLASS, LOCATIONCLASS});


		StateMapping sm = new StateMapperL1(domain);
		((FullyObservableSingleAgentAMDPDomain)domain).setStateMapper(sm);
		return domain;
	}




	public class StateMapperL1 implements StateMapping{
		private Domain d;
		public StateMapperL1(Domain dIn){
			super();
			d = dIn;
		}

		@Override
		public State mapState(State s) {
			return getMappedState(s, d);
		}

	}


	public class PickupAction extends AMDPSimpleDeterministicAction{

		public PickupAction(String actionName, Domain domain){
			super(actionName , domain);
		}

		@Override
		protected State performActionHelper(State s, GroundedAction ga) {

			// check if taxi in the same location as any of the passengers
			ObjectInstance taxi = s.getFirstObjectOfClass(TAXICLASS);
			String tLoc = taxi.getStringValForAttribute(LOCATIONATT);

			List<ObjectInstance> passengers = s.getObjectsOfClass(PASSENGERCLASS);
			for(ObjectInstance p : passengers){
				String pLoc = p.getStringValForAttribute(LOCATIONATT);

				if(tLoc.equals(pLoc)){
					p.setValue(INTAXIATT, 1);
					taxi.setValue(OCCUPIEDTAXIATT, 1);
					break;
				}

			}

			return s;
		}

		@Override
		public RewardFunction getRF() {
			StateConditionTest sc =  new GroundedPropSC(new GroundedProp(d0.getPropFunction(TaxiDomain.PASSENGERPICKUPPF), new String[]{}));
			return new GoalBasedRF(sc, 1.0,0.);
		}

		@Override
		public TerminalFunction getTF() {
			StateConditionTest sc =  new GroundedPropSC(new GroundedProp(d0.getPropFunction(TaxiDomain.PASSENGERPICKUPPF), new String[]{}));
			return new GoalConditionTF(sc);
		}


	}

	public class PutDownAction extends AMDPSimpleDeterministicAction{

		public PutDownAction(String actionName, Domain domain){
			super(actionName, domain);
		}

		@Override
		protected State performActionHelper(State s, GroundedAction ga) {

			ObjectInstance taxi = s.getFirstObjectOfClass(TAXICLASS);
			List<ObjectInstance> passengers = s.getObjectsOfClass(PASSENGERCLASS);
			for(ObjectInstance p : passengers){
				int in = p.getIntValForAttribute(INTAXIATT);
				if(in == 1){
					p.setValue(INTAXIATT, 0);
					taxi.setValue(OCCUPIEDTAXIATT, 0);
					p.setValue(LOCATIONATT, taxi.getStringValForAttribute(LOCATIONATT));
					break;
				}

			}

			return s;
		}



		@Override
		public RewardFunction getRF() {
			StateConditionTest sc =  new GroundedPropSC(new GroundedProp(d0.getPropFunction(TaxiDomain.PASSENGERPUTDOWNPF), new String[]{}));
			return new GoalBasedRF(sc, 1.0,0.);
		}

		@Override
		public TerminalFunction getTF() {
			StateConditionTest sc =  new GroundedPropSC(new GroundedProp(d0.getPropFunction(TaxiDomain.PASSENGERPUTDOWNPF), new String[]{}));
			return new GoalConditionTF(sc);
		}




	}

	public class NavigateAction extends ObjectParameterizedAMDPAction implements FullActionModel {

		public NavigateAction(String actionName, Domain domain, String[] parameterClasses){
			super(actionName, domain, parameterClasses);
		}

		@Override
		protected State performActionHelper(State s, GroundedAction ga) {

			ObjectParameterizedAMDPGroundedAction oga = (ObjectParameterizedAMDPGroundedAction)ga;
			// oga.prams[0] is the name of the location - like location1

			ObjectInstance taxi = s.getFirstObjectOfClass(TAXICLASS);
			int i = s.getObject(oga.params[0]).getIntValForAttribute(LOCATIONATT);
			taxi.setValue(LOCATIONATT, i);
			boolean inTaxi = taxi.getBooleanValForAttribute(OCCUPIEDTAXIATT);
			if(inTaxi){
				List<ObjectInstance> passengers = s.getObjectsOfClass(PASSENGERCLASS);
				for(ObjectInstance p : passengers){
					int in = p.getIntValForAttribute(INTAXIATT);
					if(in == 1){
						p.setValue(LOCATIONATT, i);
						break;
					}

				}

			}

			return s;
		}

		@Override
		public RewardFunction getRF(ObjectParameterizedAMDPGroundedAction ga) {
			ObjectParameterizedAMDPGroundedAction oga = (ObjectParameterizedAMDPGroundedAction)ga;
			StateConditionTest sc =  new GroundedPropSC(new GroundedProp(d0.getPropFunction(TaxiDomain.TAXIATLOCATIONPF), new String[]{oga.params[0]}));
			return new GoalBasedRF(sc, 1.0,0.);
		}

		@Override
		public TerminalFunction getTF(ObjectParameterizedAMDPGroundedAction ga) {
			ObjectParameterizedAMDPGroundedAction oga = (ObjectParameterizedAMDPGroundedAction)ga;
			StateConditionTest sc =  new GroundedPropSC(new GroundedProp(d0.getPropFunction(TaxiDomain.TAXIATLOCATIONPF), new String[]{oga.params[0]}));
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

	//TODO: prop function to check if passenger picked by taxi
	public class PF_PassengerInTaxi extends PropositionalFunction{

		public PF_PassengerInTaxi(String name, Domain domain, String [] params){
			super(name, domain, params);
		}

		@Override
		public boolean isTrue(State s, String[] params) {
			ObjectInstance taxi = s.getFirstObjectOfClass(TAXICLASS);
			int tLocation = taxi.getIntValForAttribute(LOCATIONATT);
			boolean taxiOccupied = taxi.getBooleanValForAttribute(OCCUPIEDTAXIATT);

			// params here are the name of a location like Location 1

			boolean returnValue = false;
			ObjectInstance passenger = s.getObject(params[0]);
			int pLocation = passenger.getIntValForAttribute(LOCATIONATT);
			boolean inTaxi = passenger.getBooleanValForAttribute(INTAXIATT);
			if(tLocation==pLocation && inTaxi && taxiOccupied ){
				returnValue = true;
			}

			return returnValue;
		}
	}


	//TODO: prop function to check if passenger at location
	public class PF_PassengerInLocation extends PropositionalFunction{

		public PF_PassengerInLocation(String name, Domain domain, String [] params){
			super(name, domain, params);
		}

		@Override
		public boolean isTrue(State s, String[] params) {
			//			ObjectInstance taxi = s.getFirstObjectOfClass(TAXICLASS);
			//			boolean taxiOccupied = taxi.getBooleanValForAttribute(OCCUPIEDTAXIATT);

			ObjectInstance location = s.getObject(params[1]);
			int lLocation = location.getIntValForAttribute(LOCATIONATT);

			// params here are the name of a location like Location 1

			boolean returnValue = false;
			ObjectInstance passenger = s.getObject(params[0]);
			int pLocation = passenger.getIntValForAttribute(LOCATIONATT);
			boolean inTaxi = passenger.getBooleanValForAttribute(INTAXIATT);
			if(lLocation==pLocation && !inTaxi){
				returnValue = true;
			}

			return returnValue;
		}
	}


	public static State getMappedState(State s, Domain cd){

		State as = new MutableState();
		// get taxi keep empty and location attributes
		// get locations
		// get passenger and set them in locations and in taxi attributes to x and y
		//		ObjectClass oc = cd.getObjectClass(TAXICLASS);
		ObjectInstance taxiC = new MutableObjectInstance(cd.getObjectClass(TAXICLASS), TAXICLASS+0);
		taxiC.setValue(LOCATIONATT, 9);



		ObjectInstance taxi = s.getFirstObjectOfClass(TaxiDomain.TAXICLASS);
		int xt = taxi.getIntValForAttribute(TaxiDomain.XATT);
		int yt = taxi.getIntValForAttribute(TaxiDomain.YATT);

		taxiC.setValue(OCCUPIEDTAXIATT, taxi.getBooleanValForAttribute(TaxiDomain.OCCUPIEDTAXIATT));


		List<ObjectInstance> locations = s.getObjectsOfClass(TaxiDomain.LOCATIONCLASS);
		for(ObjectInstance l : locations){
			int xl = l.getIntValForAttribute(TaxiDomain.XATT);
			int yl = l.getIntValForAttribute(TaxiDomain.YATT);
			if(xl==xt && yl==yt){
				taxiC.setValue(LOCATIONATT, l.getIntValForAttribute(TaxiDomain.LOCATIONATT));
			}
			ObjectInstance al = new MutableObjectInstance(cd.getObjectClass(LOCATIONCLASS), l.getName());
			al.setValue(LOCATIONATT, l.getIntValForAttribute(TaxiDomain.LOCATIONATT));
			as.addObject(al);
		}

		List<ObjectInstance> passengers = s.getObjectsOfClass(TaxiDomain.PASSENGERCLASS);
		for(ObjectInstance p : passengers){
			ObjectInstance ap = new MutableObjectInstance(cd.getObjectClass(PASSENGERCLASS), p.getName());
			ap.setValue(LOCATIONATT, p.getIntValForAttribute(TaxiDomain.LOCATIONATT));
			ap.setValue(INTAXIATT, p.getIntValForAttribute(TaxiDomain.INTAXIATT));
			as.addObject(ap);
		}

		as.addObject(taxiC);

		return as;
	}

	public static class GroundedPropSC implements StateConditionTest{

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

		StateConditionTest sc = new InLocationSC("passenger0", "yellow");
		RewardFunction rf = new GoalBasedRF(sc, 1.);
		TerminalFunction tf = new GoalConditionTF(sc);

		PropositionalFunction pfTest = d1.getPropFunction(TAXIATPASSENGERPF);
		
		ValueIteration vi = new ValueIteration(d1, rf, tf, 0.99, new SimpleHashableStateFactory(false), 0.01, 100);
		Policy p = vi.planFromState(as);
		EpisodeAnalysis ea = p.evaluateBehavior(as, rf, tf, 15);
		System.out.println(ea.getActionSequenceString("\n"));
		for(int i =0;i<ea.stateSequence.size();i++){
			
			System.out.println(ea.getState(i));
			String[] params = new String[1];
			params[0] = "passenger0";

			boolean test = pfTest.isTrue(ea.getState(i), params);
			System.out.println(test);
		}


	}


}
