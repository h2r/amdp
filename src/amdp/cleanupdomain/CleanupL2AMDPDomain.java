package amdp.cleanupdomain;

import burlap.domain.singleagent.cleanup.CleanupWorld;
import burlap.oomdp.auxiliary.DomainGenerator;
import burlap.oomdp.auxiliary.StateMapping;
import burlap.oomdp.auxiliary.common.GoalConditionTF;
import burlap.oomdp.auxiliary.stateconditiontest.StateConditionTest;
import burlap.oomdp.core.Attribute;
import burlap.oomdp.core.Domain;
import burlap.oomdp.core.ObjectClass;
import burlap.oomdp.core.TerminalFunction;
import burlap.oomdp.core.TransitionProbability;
import burlap.oomdp.core.objects.MutableObjectInstance;
import burlap.oomdp.core.objects.ObjectInstance;
import burlap.oomdp.core.states.MutableState;
import burlap.oomdp.core.states.State;
import burlap.oomdp.singleagent.FullActionModel;
import burlap.oomdp.singleagent.GroundedAction;
import burlap.oomdp.singleagent.ObjectParameterizedAction;
import burlap.oomdp.singleagent.RewardFunction;
import burlap.oomdp.singleagent.SADomain;
import burlap.oomdp.singleagent.common.GoalBasedRF;
import burlap.oomdp.singleagent.explorer.TerminalExplorer;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import amdp.framework.FullyObservableSingleAgentAMDPDomain;
import amdp.framework.ObjectParameterizedAMDPAction;

/**
 * @author James MacGlashan.
 */
public class CleanupL2AMDPDomain implements DomainGenerator {


	@Override
	public Domain generateDomain() {

		Domain domain = new FullyObservableSingleAgentAMDPDomain();

		Attribute inRegion = new Attribute(domain, CleanupL1AMDPDomain.ATT_IN_REGION, Attribute.AttributeType.RELATIONAL);

		Attribute colAtt = new Attribute(domain, CleanupWorld.ATT_COLOR, Attribute.AttributeType.DISC);
		colAtt.setDiscValues(CleanupWorld.COLORS);

		Attribute shapeAtt = new Attribute(domain, CleanupWorld.ATT_SHAPE, Attribute.AttributeType.DISC);
		shapeAtt.setDiscValues(CleanupWorld.SHAPES);

		ObjectClass agent = new ObjectClass(domain, CleanupWorld.CLASS_AGENT);
		agent.addAttribute(inRegion);

		ObjectClass room = new ObjectClass(domain, CleanupWorld.CLASS_ROOM);
		room.addAttribute(colAtt);

		ObjectClass block = new ObjectClass(domain, CleanupWorld.CLASS_BLOCK);
		block.addAttribute(inRegion);
		block.addAttribute(colAtt);
		block.addAttribute(shapeAtt);

		new AgentToRoomAction(CleanupL1AMDPDomain.ACTION_AGENT_TO_ROOM, domain);
		new BlockToRoomAction(CleanupL1AMDPDomain.ACTION_BLOCK_TO_ROOM, domain);


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
			return projectToAMDPState(s,d);
		}
		
	}


	public static State projectToAMDPState(State s, Domain aDomain){

		State as = new MutableState();

		ObjectInstance aagent = new MutableObjectInstance(aDomain.getObjectClass(CleanupWorld.CLASS_AGENT), CleanupWorld.CLASS_AGENT);
		as.addObject(aagent);

		List<ObjectInstance> rooms = s.getObjectsOfClass(CleanupWorld.CLASS_ROOM);
		Set<String> roomNames = new HashSet<String>(rooms.size());
		for(ObjectInstance r : rooms){
			ObjectInstance ar = new MutableObjectInstance(aDomain.getObjectClass(CleanupWorld.CLASS_ROOM), r.getName());
			ar.setValue(CleanupWorld.ATT_COLOR, r.getIntValForAttribute(CleanupWorld.ATT_COLOR));
			as.addObject(ar);

			roomNames.add(r.getName());
		}

		List<ObjectInstance> blocks = s.getObjectsOfClass(CleanupWorld.CLASS_BLOCK);
		for(ObjectInstance b : blocks){
			ObjectInstance ab = new MutableObjectInstance(aDomain.getObjectClass(CleanupWorld.CLASS_BLOCK), b.getName());
			ab.setValue(CleanupWorld.ATT_COLOR, b.getIntValForAttribute(CleanupWorld.ATT_COLOR));
			ab.setValue(CleanupWorld.ATT_SHAPE, b.getIntValForAttribute(CleanupWorld.ATT_SHAPE));
			String sourceRegion = b.getStringValForAttribute(CleanupL1AMDPDomain.ATT_IN_REGION);
			if(roomNames.contains(sourceRegion)){
				ab.addRelationalTarget(CleanupL1AMDPDomain.ATT_IN_REGION, sourceRegion);
			}
			as.addObject(ab);
		}

		ObjectInstance agent = s.getFirstObjectOfClass(CleanupWorld.CLASS_AGENT);
		if(roomNames.contains(agent.getStringValForAttribute(CleanupL1AMDPDomain.ATT_IN_REGION))){
			aagent.addRelationalTarget(CleanupL1AMDPDomain.ATT_IN_REGION, agent.getStringValForAttribute(CleanupL1AMDPDomain.ATT_IN_REGION));
		}

		return as;
	}

	public static class AgentToRoomAction extends ObjectParameterizedAMDPAction implements FullActionModel{

		public AgentToRoomAction(String name, Domain domain) {
			super(name, domain, new String[]{CleanupWorld.CLASS_ROOM});
		}

		@Override
		public boolean parametersAreObjectIdentifierIndependent() {
			return false;
		}

		@Override
		public boolean applicableInState(State s, GroundedAction groundedAction) {
			ObjectParameterizedAMDPGroundedAction oga = (ObjectParameterizedAMDPGroundedAction)groundedAction;
			String curRoom = s.getFirstObjectOfClass(CleanupWorld.CLASS_AGENT).getStringValForAttribute(CleanupL1AMDPDomain.ATT_IN_REGION);
			if(!curRoom.equals(oga.params[0])){
				return true;
			}
			return false;
		}

		@Override
		public boolean isPrimitive() {
			return true;
		}

		@Override
		protected State performActionHelper(State s, GroundedAction groundedAction) {

			ObjectParameterizedAMDPGroundedAction oga = (ObjectParameterizedAMDPGroundedAction)groundedAction;
			ObjectInstance agent = s.getFirstObjectOfClass(CleanupWorld.CLASS_AGENT);
			agent.addRelationalTarget(CleanupL1AMDPDomain.ATT_IN_REGION, oga.params[0]);

			return s;
		}


		@Override
		public List<TransitionProbability> getTransitions(State s, GroundedAction groundedAction) {
			return this.deterministicTransition(s, groundedAction);
		}

		@Override
		public RewardFunction getRF(ObjectParameterizedAMDPGroundedAction ga) {
			return getL1Rf(ga);
		}

		@Override
		public TerminalFunction getTF(ObjectParameterizedAMDPGroundedAction ga) {
			return getL1Tf(ga);
		}
	}

	public static class BlockToRoomAction extends ObjectParameterizedAMDPAction implements FullActionModel{

		public BlockToRoomAction(String name, Domain domain) {
			super(name, domain, new String[]{CleanupWorld.CLASS_BLOCK, CleanupWorld.CLASS_ROOM});
		}

		@Override
		public boolean parametersAreObjectIdentifierIndependent() {
			return false;
		}

		@Override
		public boolean applicableInState(State s, GroundedAction groundedAction) {
			ObjectParameterizedAMDPGroundedAction oga = (ObjectParameterizedAMDPGroundedAction)groundedAction;
			String curRoom = s.getObject(oga.params[0]).getStringValForAttribute(CleanupL1AMDPDomain.ATT_IN_REGION);
			if(!curRoom.equals(oga.params[1])){
				return true;
			}
			return false;
		}

		@Override
		public boolean isPrimitive() {
			return true;
		}

		@Override
		protected State performActionHelper(State s, GroundedAction groundedAction) {

			ObjectParameterizedAMDPGroundedAction oga = (ObjectParameterizedAMDPGroundedAction)groundedAction;
			ObjectInstance agent = s.getFirstObjectOfClass(CleanupWorld.CLASS_AGENT);
			ObjectInstance block = s.getObject(oga.params[0]);
			agent.clearRelationalTargets(CleanupL1AMDPDomain.ATT_IN_REGION);
			block.addRelationalTarget(CleanupL1AMDPDomain.ATT_IN_REGION, oga.params[1]);

			return s;
		}


		@Override
		public List<TransitionProbability> getTransitions(State s, GroundedAction groundedAction) {
			return this.deterministicTransition(s, groundedAction);
		}

		@Override
		public RewardFunction getRF(ObjectParameterizedAMDPGroundedAction ga) {
			return getL1Rf(ga);
		}

		@Override
		public TerminalFunction getTF(ObjectParameterizedAMDPGroundedAction ga) {
			return getL1Tf(ga);
		}
	}
	
	public static RewardFunction getL1Rf(GroundedAction l2Action){
		ObjectParameterizedAMDPAction.ObjectParameterizedAMDPGroundedAction oga = (ObjectParameterizedAMDPAction.ObjectParameterizedAMDPGroundedAction)l2Action;
		StateConditionTest sc = null;
		if(l2Action.actionName().equals(CleanupL1AMDPDomain.ACTION_AGENT_TO_ROOM)){
			sc = new CleanupL1AMDPDomain.InRegionSC(CleanupWorld.CLASS_AGENT+0, oga.params[0]);
		}
		else{
			sc = new CleanupL1AMDPDomain.InRegionSC(oga.params[0], oga.params[1]);
		}
		return new GoalBasedRF(sc, 1., 0.);
	}

	
	protected static TerminalFunction getL1Tf(GroundedAction l2Action){
		ObjectParameterizedAMDPAction.ObjectParameterizedAMDPGroundedAction oga = (ObjectParameterizedAMDPAction.ObjectParameterizedAMDPGroundedAction)l2Action;
		StateConditionTest sc = null;
		if(l2Action.actionName().equals(CleanupL1AMDPDomain.ACTION_AGENT_TO_ROOM)){
			sc = new CleanupL1AMDPDomain.InRegionSC(CleanupWorld.CLASS_AGENT + 0, oga.params[0]);
		}
		else{
			sc = new DoorLockedSC(oga.params[1], new CleanupL1AMDPDomain.InRegionSC(oga.params[0], oga.params[1]));
		}
		return new GoalConditionTF(sc);
	}


	public static class DoorLockedSC implements StateConditionTest{

		public String door;
		public StateConditionTest otherSC;

		public DoorLockedSC(String door, StateConditionTest otherSC) {
			this.door = door;
			this.otherSC = otherSC;
		}

		@Override
		public boolean satisfies(State s) {
			if(otherSC.satisfies(s)){
				return true;
			}

			ObjectInstance doorOb = s.getObject(door);
			if(doorOb.getObjectClass().hasAttribute(CleanupWorld.ATT_LOCKED)){
				int lockedVal = doorOb.getIntValForAttribute(CleanupWorld.ATT_LOCKED);
				return lockedVal == 2;
			}

			return false;
		}
	}

	public static void main(String[] args) {

		double lockProb = 0.25;

		CleanupWorld dgen = new CleanupWorld();
		dgen.includeDirectionAttribute(true);
		dgen.includePullAction(true);
		dgen.includeWallPF_s(true);
		dgen.includeLockableDoors(true);
		dgen.setLockProbability(lockProb);
		Domain domain = dgen.generateDomain();

		State s = CleanupWorld.getClassicState(domain);

		CleanupL1AMDPDomain a1dgen = new CleanupL1AMDPDomain(domain);
		a1dgen.setLockableDoors(true);
		a1dgen.setLockProb(lockProb);
		Domain adomain = a1dgen.generateDomain();

		State a1s = CleanupL1AMDPDomain.projectToAMDPState(s, adomain);

		CleanupL2AMDPDomain a2dgen = new CleanupL2AMDPDomain();
		Domain a2domain = a2dgen.generateDomain();

		State a2s = CleanupL2AMDPDomain.projectToAMDPState(a1s, a2domain);

		TerminalExplorer exp = new TerminalExplorer(a2domain, a2s);
		exp.explore();

	}

}
