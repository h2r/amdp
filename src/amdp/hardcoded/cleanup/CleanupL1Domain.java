package amdp.hardcoded.cleanup;

import burlap.behavior.policy.Policy;
import burlap.behavior.singleagent.EpisodeAnalysis;
import burlap.behavior.singleagent.planning.stochastic.valueiteration.ValueIteration;
import burlap.debugtools.RandomFactory;
import burlap.domain.singleagent.cleanup.CleanupWorld;
import burlap.oomdp.auxiliary.DomainGenerator;
import burlap.oomdp.auxiliary.common.GoalConditionTF;
import burlap.oomdp.auxiliary.stateconditiontest.StateConditionTest;
import burlap.oomdp.core.*;
import burlap.oomdp.core.objects.MutableObjectInstance;
import burlap.oomdp.core.objects.ObjectInstance;
import burlap.oomdp.core.states.MutableState;
import burlap.oomdp.core.states.State;
import burlap.oomdp.singleagent.*;
import burlap.oomdp.singleagent.common.GoalBasedRF;
import burlap.oomdp.statehashing.SimpleHashableStateFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author James MacGlashan.
 */
public class CleanupL1Domain implements DomainGenerator {

	public static final String ATT_CONNECTED = "connectedObjects";
	public static final String ATT_IN_REGION = "inRegion";

	public static final String ACTION_AGENT_TO_DOOR = "agentToDoor";
	public static final String ACTION_AGENT_TO_ROOM = "agentToRoom";

	public static final String ACTION_BLOCK_TO_DOOR = "blockToDoor";
	public static final String ACTION_BLOCK_TO_ROOM = "blockToRoom";


	protected boolean lockableDoors = false;
	protected double lockProb = 0.5;

	public void setLockableDoors(boolean lockableDoors) {
		this.lockableDoors = lockableDoors;
	}

	public void setLockProb(double lockProb) {
		this.lockProb = lockProb;
	}

	@Override
	public Domain generateDomain() {

		Domain domain = new SADomain();

		Attribute inRegion = new Attribute(domain, ATT_IN_REGION, Attribute.AttributeType.RELATIONAL);
		Attribute conn = new Attribute(domain, ATT_CONNECTED, Attribute.AttributeType.MULTITARGETRELATIONAL);

		Attribute colAtt = new Attribute(domain, CleanupWorld.ATT_COLOR, Attribute.AttributeType.DISC);
		colAtt.setDiscValues(CleanupWorld.COLORS);

		Attribute shapeAtt = new Attribute(domain, CleanupWorld.ATT_SHAPE, Attribute.AttributeType.DISC);
		shapeAtt.setDiscValues(CleanupWorld.SHAPES);

		if(this.lockableDoors){
			Attribute lockAtt = new Attribute(domain, CleanupWorld.ATT_LOCKED, Attribute.AttributeType.DISC);
			lockAtt.setDiscValues(Arrays.asList(CleanupWorld.LOCKABLE_STATES));
		}


		ObjectClass agent = new ObjectClass(domain, CleanupWorld.CLASS_AGENT);
		agent.addAttribute(inRegion);

		ObjectClass room = new ObjectClass(domain, CleanupWorld.CLASS_ROOM);
		room.addAttribute(conn);
		room.addAttribute(colAtt);

		ObjectClass door = new ObjectClass(domain, CleanupWorld.CLASS_DOOR);
		door.addAttribute(conn);
		if(this.lockableDoors){
			door.addAttribute(domain.getAttribute(CleanupWorld.ATT_LOCKED));
		}

		ObjectClass block = new ObjectClass(domain, CleanupWorld.CLASS_BLOCK);
		block.addAttribute(inRegion);
		block.addAttribute(colAtt);
		block.addAttribute(shapeAtt);


		new GoToRegion(ACTION_AGENT_TO_ROOM, domain, new String[]{CleanupWorld.CLASS_ROOM}, this.lockProb);
		new GoToRegion(ACTION_AGENT_TO_DOOR, domain, new String[]{CleanupWorld.CLASS_DOOR}, this.lockProb);

		new BlockToRegion(ACTION_BLOCK_TO_ROOM, domain, new String[]{CleanupWorld.CLASS_BLOCK, CleanupWorld.CLASS_ROOM}, this.lockProb);
		new BlockToRegion(ACTION_BLOCK_TO_DOOR, domain, new String[]{CleanupWorld.CLASS_BLOCK, CleanupWorld.CLASS_DOOR}, this.lockProb);

		return domain;
	}



	public static State projectToAMDPState(State s, Domain aDomain){

		State as = new MutableState();

		ObjectInstance aagent = new MutableObjectInstance(aDomain.getObjectClass(CleanupWorld.CLASS_AGENT), CleanupWorld.CLASS_AGENT);
		as.addObject(aagent);


		List<ObjectInstance> rooms = s.getObjectsOfClass(CleanupWorld.CLASS_ROOM);
		for(ObjectInstance r : rooms){
			ObjectInstance ar = new MutableObjectInstance(aDomain.getObjectClass(CleanupWorld.CLASS_ROOM), r.getName());
			ar.setValue(CleanupWorld.ATT_COLOR, r.getIntValForAttribute(CleanupWorld.ATT_COLOR));
			as.addObject(ar);
		}

		List<ObjectInstance> doors = s.getObjectsOfClass(CleanupWorld.CLASS_DOOR);
		for(ObjectInstance d : doors){
			ObjectInstance ad = new MutableObjectInstance(aDomain.getObjectClass(CleanupWorld.CLASS_DOOR), d.getName());
			as.addObject(ad);
		}

		List<ObjectInstance> blocks = s.getObjectsOfClass(CleanupWorld.CLASS_BLOCK);
		for(ObjectInstance b : blocks){
			ObjectInstance ab = new MutableObjectInstance(aDomain.getObjectClass(CleanupWorld.CLASS_BLOCK), b.getName());
			ab.setValue(CleanupWorld.ATT_COLOR, b.getIntValForAttribute(CleanupWorld.ATT_COLOR));
			ab.setValue(CleanupWorld.ATT_SHAPE, b.getIntValForAttribute(CleanupWorld.ATT_SHAPE));
			as.addObject(ab);
		}


		//set agent position
		//first try room
		ObjectInstance agent = s.getFirstObjectOfClass(CleanupWorld.CLASS_AGENT);
		int ax = agent.getIntValForAttribute(CleanupWorld.ATT_X);
		int ay = agent.getIntValForAttribute(CleanupWorld.ATT_Y);
		ObjectInstance inRoom = CleanupWorld.roomContainingPoint(s, ax, ay);

		if(inRoom != null){
			aagent.setValue(ATT_IN_REGION, inRoom.getName());
		}
		else{
			ObjectInstance inDoor = CleanupWorld.doorContainingPoint(s, ax, ay);
			aagent.setValue(ATT_IN_REGION, inDoor.getName());
		}

		//next blocks
		for(ObjectInstance b : blocks){
			ObjectInstance ab = as.getObject(b.getName());
			int bx = b.getIntValForAttribute(CleanupWorld.ATT_X);
			int by = b.getIntValForAttribute(CleanupWorld.ATT_Y);
			ObjectInstance bInRoom = CleanupWorld.roomContainingPoint(s, bx, by);
			if(bInRoom != null){
				ab.setValue(ATT_IN_REGION, bInRoom.getName());
			}
			else{
				ObjectInstance inDoor = CleanupWorld.doorContainingPoint(s, bx, by);
				ab.setValue(ATT_IN_REGION, inDoor.getName());
			}
		}


		//now set room and door connections
		for(ObjectInstance r : rooms){

			int rt = r.getIntValForAttribute(CleanupWorld.ATT_TOP);
			int rl = r.getIntValForAttribute(CleanupWorld.ATT_LEFT);
			int rb = r.getIntValForAttribute(CleanupWorld.ATT_BOTTOM);
			int rr = r.getIntValForAttribute(CleanupWorld.ATT_RIGHT);

			ObjectInstance ar = as.getObject(r.getName());

			for(ObjectInstance d : doors){

				int dt = d.getIntValForAttribute(CleanupWorld.ATT_TOP);
				int dl = d.getIntValForAttribute(CleanupWorld.ATT_LEFT);
				int db = d.getIntValForAttribute(CleanupWorld.ATT_BOTTOM);
				int dr = d.getIntValForAttribute(CleanupWorld.ATT_RIGHT);

				if(rectanglesIntersect(rt, rl, rb, rr, dt, dl, db, dr)){
					ObjectInstance ad = as.getObject(d.getName());
					ar.addRelationalTarget(ATT_CONNECTED, ad.getName());
					ad.addRelationalTarget(ATT_CONNECTED, ar.getName());
				}

			}

		}

		//also set door lock states if needed
		if(aDomain.getAttribute(CleanupWorld.ATT_LOCKED) != null){

			for(ObjectInstance d : doors){
				as.getObject(d.getName()).setValue(CleanupWorld.ATT_LOCKED, d.getIntValForAttribute(CleanupWorld.ATT_LOCKED));
			}

		}


		return as;

	}


	protected static boolean rectanglesIntersect(int t1, int l1, int b1, int r1, int t2, int l2, int b2, int r2){

		return t2 >= b1 && b2 <= t1 && r2 >= l1 && l2 <= r1;

	}



	public static class GoToRegion extends ObjectParameterizedAction implements FullActionModel{

		protected double lockedProb;

		public GoToRegion(String name, Domain domain, String[] parameterClasses, double lockedProb) {
			super(name, domain, parameterClasses);
			this.lockedProb = lockedProb;
		}

		@Override
		public boolean parametersAreObjectIdentifierIndependent() {
			return false;
		}

		@Override
		public boolean applicableInState(State s, GroundedAction groundedAction) {
			ObjectParameterizedGroundedAction oga = (ObjectParameterizedGroundedAction)groundedAction;

			//get the region where the agent currently is
			ObjectInstance agent = s.getFirstObjectOfClass(CleanupWorld.CLASS_AGENT);
			ObjectInstance curRegion = s.getObject(agent.getStringValForAttribute(ATT_IN_REGION));

			//is the param connected to this region?
			if(curRegion.getAllRelationalTargets(ATT_CONNECTED).contains(oga.params[0])){
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

			ObjectParameterizedGroundedAction oga = (ObjectParameterizedGroundedAction)groundedAction;
			ObjectInstance agent = s.getFirstObjectOfClass(CleanupWorld.CLASS_AGENT);

			ObjectInstance region = s.getObject(oga.params[0]);
			if(region.getClassName().equals(CleanupWorld.CLASS_ROOM)){
				agent.addRelationalTarget(ATT_IN_REGION, oga.params[0]);
			}
			else{ //door
				if(!region.getObjectClass().hasAttribute(CleanupWorld.ATT_LOCKED)){
					//then always add if not lockable
					agent.addRelationalTarget(ATT_IN_REGION, oga.params[0]);
				}
				else{
					int lockVal = region.getIntValForAttribute(CleanupWorld.ATT_LOCKED);
					if(lockVal == 1){
						//unlocked
						agent.addRelationalTarget(ATT_IN_REGION, oga.params[0]);
					}
					else if(lockVal == 0){ //unknown
						double roll = RandomFactory.getMapped(0).nextDouble();
						if(roll < 1. - this.lockedProb){
							//unlocks itself
							agent.addRelationalTarget(ATT_IN_REGION, oga.params[0]);
							s.getObject(oga.params[0]).setValue(CleanupWorld.ATT_LOCKED, 1);
						}
						else{
							s.getObject(oga.params[0]).setValue(CleanupWorld.ATT_LOCKED, 2);
						}
					}
				}
			}


			return s;
		}

		@Override
		public List<TransitionProbability> getTransitions(State s, GroundedAction groundedAction) {

			s = s.copy();

			List<TransitionProbability> tps = new ArrayList<TransitionProbability>();

			ObjectParameterizedGroundedAction oga = (ObjectParameterizedGroundedAction)groundedAction;

			State sp = s.copy();

			ObjectInstance region = sp.getObject(oga.params[0]);
			if(region.getClassName().equals(CleanupWorld.CLASS_ROOM)){

				ObjectInstance agent = sp.getFirstObjectOfClass(CleanupWorld.CLASS_AGENT);
				agent.addRelationalTarget(ATT_IN_REGION, oga.params[0]);
				tps.add(new TransitionProbability(sp, 1.));
			}
			else{ //door

				ObjectInstance agent = sp.getFirstObjectOfClass(CleanupWorld.CLASS_AGENT);

				if(!region.getObjectClass().hasAttribute(CleanupWorld.ATT_LOCKED)){
					//always open
					agent.addRelationalTarget(ATT_IN_REGION, oga.params[0]);
					tps.add(new TransitionProbability(sp, 1.));
				}
				else{
					int lockVal = region.getIntValForAttribute(CleanupWorld.ATT_LOCKED);
					if(lockVal == 1){
						//unlocked
						agent.addRelationalTarget(ATT_IN_REGION, oga.params[0]);
						tps.add(new TransitionProbability(sp, 1.));
					}
					else if(lockVal == 2){
						//locked
						tps.add(new TransitionProbability(sp, 1.)); //no op
					}
					else if(lockVal == 0){

						//then could go either way
						State sp2 = sp.copy();

						//unlock case
						agent.addRelationalTarget(ATT_IN_REGION, oga.params[0]);
						region.setValue(CleanupWorld.ATT_LOCKED, 1);
						tps.add(new TransitionProbability(sp, 1.-lockedProb));

						//lock case
						sp2.getObject(region.getName()).setValue(CleanupWorld.ATT_LOCKED, 2);
						tps.add(new TransitionProbability(sp2, lockedProb));


					}
				}

			}

			return tps;
		}
	}



	public static class BlockToRegion extends ObjectParameterizedAction implements FullActionModel{

		protected double lockedProb;

		public BlockToRegion(String name, Domain domain, String[] parameterClasses, double lockedProb) {
			super(name, domain, parameterClasses);
			this.lockedProb = lockedProb;
		}

		@Override
		public boolean parametersAreObjectIdentifierIndependent() {
			return false;
		}

		@Override
		public boolean applicableInState(State s, GroundedAction groundedAction) {
			ObjectParameterizedGroundedAction oga = (ObjectParameterizedGroundedAction)groundedAction;

			//get the region where the agent currently is
			ObjectInstance agent = s.getFirstObjectOfClass(CleanupWorld.CLASS_AGENT);
			ObjectInstance block = s.getObject(oga.params[0]);
			ObjectInstance curRegion = s.getObject(agent.getStringValForAttribute(ATT_IN_REGION));
			ObjectInstance blockRegion = s.getObject(block.getStringValForAttribute(ATT_IN_REGION));

			if(curRegion != blockRegion){
				if(blockRegion.getClassName().equals(CleanupWorld.CLASS_ROOM)){
					return false; //if block is a room, then agent must be in that room to move it
				}
				else if(curRegion.getClassName().equals(CleanupWorld.CLASS_DOOR)){
					return false; //if block and agent are in different doors, cannot move block
				}
				else if(!curRegion.getAllRelationalTargets(ATT_CONNECTED).contains(blockRegion.getName())){
					//if agent is in room and block is in door, then agent can only move if door is connected
					return false;
				}

			}

			//is the target region connected to the block region?
			if(blockRegion.getAllRelationalTargets(ATT_CONNECTED).contains(oga.params[1])){
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

			ObjectParameterizedGroundedAction oga = (ObjectParameterizedGroundedAction)groundedAction;
			ObjectInstance agent = s.getFirstObjectOfClass(CleanupWorld.CLASS_AGENT);
			ObjectInstance block = s.getObject(oga.params[0]);
			String curBlockRegion = block.getStringValForAttribute(ATT_IN_REGION);

			ObjectInstance region = s.getObject(oga.params[1]);
			if(region.getClassName().equals(CleanupWorld.CLASS_ROOM)){
				block.addRelationalTarget(ATT_IN_REGION, oga.params[1]);
				agent.addRelationalTarget(ATT_IN_REGION, curBlockRegion);
			}
			else{ //door
				if(!region.getObjectClass().hasAttribute(CleanupWorld.ATT_LOCKED)){
					//then always add if not lockable
					block.addRelationalTarget(ATT_IN_REGION, oga.params[1]);
				}
				else{
					int lockVal = region.getIntValForAttribute(CleanupWorld.ATT_LOCKED);
					if(lockVal == 1){
						//unlocked
						block.addRelationalTarget(ATT_IN_REGION, oga.params[1]);
					}
					else if(lockVal == 0){ //unknown
						double roll = RandomFactory.getMapped(0).nextDouble();
						if(roll < 1. - this.lockedProb){
							//unlocks itself
							block.addRelationalTarget(ATT_IN_REGION, oga.params[1]);
							s.getObject(oga.params[1]).setValue(CleanupWorld.ATT_LOCKED, 1);
						}
						else{
							s.getObject(oga.params[1]).setValue(CleanupWorld.ATT_LOCKED, 2);
						}
					}
				}
			}


			return s;
		}

		@Override
		public List<TransitionProbability> getTransitions(State s, GroundedAction groundedAction) {

			s = s.copy();

			List<TransitionProbability> tps = new ArrayList<TransitionProbability>();

			ObjectParameterizedGroundedAction oga = (ObjectParameterizedGroundedAction)groundedAction;

			State sp = s.copy();
			ObjectInstance block = sp.getObject(oga.params[0]);
			String curBlockRegion = block.getStringValForAttribute(ATT_IN_REGION);

			ObjectInstance region = s.getObject(oga.params[1]);
			if(region.getClassName().equals(CleanupWorld.CLASS_ROOM)){

				ObjectInstance agent = sp.getFirstObjectOfClass(CleanupWorld.CLASS_AGENT);
				block.addRelationalTarget(ATT_IN_REGION, oga.params[1]);
				agent.addRelationalTarget(ATT_IN_REGION, curBlockRegion);
				tps.add(new TransitionProbability(sp, 1.));
			}
			else{ //door

				//ObjectInstance agent = sp.getFirstObjectOfClass(CleanupWorld.CLASS_AGENT);

				if(!region.getObjectClass().hasAttribute(CleanupWorld.ATT_LOCKED)){
					//always open
					block.addRelationalTarget(ATT_IN_REGION, oga.params[1]);
					tps.add(new TransitionProbability(sp, 1.));
				}
				else{
					int lockVal = region.getIntValForAttribute(CleanupWorld.ATT_LOCKED);
					if(lockVal == 1){
						//unlocked
						block.addRelationalTarget(ATT_IN_REGION, oga.params[1]);
						tps.add(new TransitionProbability(sp, 1.));
					}
					else if(lockVal == 2){
						//locked
						tps.add(new TransitionProbability(sp, 1.)); //no op
					}
					else if(lockVal == 0){

						//then could go either way
						State sp2 = sp.copy();

						//unlock case
						block.addRelationalTarget(ATT_IN_REGION, oga.params[1]);
						region.setValue(CleanupWorld.ATT_LOCKED, 1);
						tps.add(new TransitionProbability(sp, 1.-lockedProb));

						//lock case
						sp2.getObject(region.getName()).setValue(CleanupWorld.ATT_LOCKED, 2);
						tps.add(new TransitionProbability(sp2, lockedProb));


					}
				}

			}

			return tps;
		}
	}


	public static class InRegionSC implements StateConditionTest{

		String srcOb;
		String targetOb;

		public InRegionSC(String srcOb, String targetOb) {
			this.srcOb = srcOb;
			this.targetOb = targetOb;
		}

		@Override
		public boolean satisfies(State s) {
			ObjectInstance src = s.getObject(this.srcOb);
			return src.getStringValForAttribute(ATT_IN_REGION).equals(targetOb);
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

		CleanupL1Domain adgen = new CleanupL1Domain();
		adgen.setLockableDoors(true);
		adgen.setLockProb(lockProb);
		Domain adomain = adgen.generateDomain();

		State as = CleanupL1Domain.projectToAMDPState(s, adomain);

//		TerminalExplorer exp = new TerminalExplorer(adomain, as);
//		exp.explore();

		StateConditionTest sc = new InRegionSC("block0", "room1");
		RewardFunction rf = new GoalBasedRF(sc, 1.);
		TerminalFunction tf = new GoalConditionTF(sc);


		//StateReachability.getReachableStates(as, (SADomain)adomain, new SimpleHashableStateFactory(false));

		ValueIteration vi = new ValueIteration(adomain, rf, tf, 0.99, new SimpleHashableStateFactory(false), 0.01, 100);
		Policy p = vi.planFromState(as);
		EpisodeAnalysis ea = p.evaluateBehavior(as, rf, tf, 15);
		System.out.println(ea.getActionSequenceString("\n"));


		//TerminalEpisodeExplorer texp = new TerminalEpisodeExplorer(Arrays.asList(ea));

	}

}
