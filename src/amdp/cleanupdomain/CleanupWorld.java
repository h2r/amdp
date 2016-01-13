package amdp.cleanupdomain;

import burlap.behavior.policy.Policy;
import burlap.behavior.singleagent.EpisodeAnalysis;
import burlap.behavior.singleagent.auxiliary.EpisodeSequenceVisualizer;
import burlap.behavior.singleagent.planning.stochastic.rtdp.BoundedRTDP;
import burlap.behavior.valuefunction.ValueFunctionInitialization;
import burlap.debugtools.RandomFactory;
import burlap.domain.singleagent.cleanup.CleanupVisualizer;
import burlap.oomdp.auxiliary.DomainGenerator;
import burlap.oomdp.auxiliary.common.GoalConditionTF;
import burlap.oomdp.auxiliary.stateconditiontest.StateConditionTest;
import burlap.oomdp.core.*;
import burlap.oomdp.core.objects.MutableObjectInstance;
import burlap.oomdp.core.objects.ObjectInstance;
import burlap.oomdp.core.states.MutableState;
import burlap.oomdp.core.states.State;
import burlap.oomdp.singleagent.FullActionModel;
import burlap.oomdp.singleagent.GroundedAction;
import burlap.oomdp.singleagent.RewardFunction;
import burlap.oomdp.singleagent.SADomain;
import burlap.oomdp.singleagent.common.GoalBasedRF;
import burlap.oomdp.singleagent.common.SimpleAction;
import burlap.oomdp.singleagent.explorer.VisualExplorer;
import burlap.oomdp.statehashing.SimpleHashableStateFactory;
import burlap.oomdp.visualizer.Visualizer;

import java.util.Arrays;
import java.util.List;

import amdp.cleanupdomain.CleanupL1AMDPDomain.GroundedPropSC;
import amdp.hardcoded.cleanup.FixedDoorCleanupEnv;

/**
 * @author James MacGlashan.
 */
public class CleanupWorld implements DomainGenerator{


	public static final String ATT_X = "x";
	public static final String ATT_Y = "y";
	public static final String ATT_DIR = "direction"; //optionally added attribute to include the agent's direction
	public static final String ATT_TOP = "top";
	public static final String ATT_LEFT = "left";
	public static final String ATT_BOTTOM = "bottom";
	public static final String ATT_RIGHT = "right";
	public static final String ATT_COLOR = "color";
	public static final String ATT_SHAPE = "shape";
	public static final String ATT_LOCKED = "locked";


	public static final String CLASS_AGENT = "agent";
	public static final String					CLASS_BLOCK = "block";
	public static final String					CLASS_ROOM = "room";
	public static final String					CLASS_DOOR = "door";


	public static final String					ACTION_NORTH = "north";
	public static final String					ACTION_SOUTH = "south";
	public static final String					ACTION_EAST = "east";
	public static final String					ACTION_WEST = "west";
	public static final String					ACTION_PULL = "pull";

	public static final String					PF_AGENT_IN_ROOM = "agentInRoom";
	public static final String					PF_BLOCK_IN_ROOM = "blockInRoom";
	public static final String					PF_AGENT_IN_DOOR = "agentInDoor";
	public static final String					PF_BLOCK_IN_DOOR = "blockInDoor";

	public static final String					PF_WALL_NORTH = "wallNorth";
	public static final String					PF_WALL_SOUTH = "wallSouth";
	public static final String					PF_WALL_EAST = "wallEast";
	public static final String					PF_WALL_WEST = "wallWest";


	public static final String[] 				COLORS = new String[]{"blue",
			"green", "magenta",
			"red", "yellow"};

	public static final String[]				SHAPES = new String[]{"chair", "bag",
			"backpack", "basket"};


	public static final String[]				DIRECTIONS = new String[]{"north", "south", "east", "west"};

	public static final String []				LOCKABLE_STATES = new String[]{"unknown", "unlocked", "locked"};

	protected static final String				PF_RCOLORBASE = "roomIs";
	protected static final String				PF_BCOLORBASE = "blockIs";
	protected static final String				PF_BSHAPEBASE = "shape";




	protected int								maxX = 24;
	protected int								maxY = 24;
	protected boolean							includeDirectionAttribute = false;
	protected boolean							includePullAction = false;
	protected boolean							includeWallPF_s = false;
	protected boolean							lockableDoors = false;
	protected double							lockProb = 0.5;


	public void includeWallPF_s(boolean includeWallPF_s){
		this.includeWallPF_s = includeWallPF_s;
	}

	public void setMaxX(int maxX){
		this.maxX = maxX;
	}

	public void setMaxY(int maxY){
		this.maxY = maxY;
	}

	public void includeDirectionAttribute(boolean includeDirectionAttribute){
		this.includeDirectionAttribute = includeDirectionAttribute;
	}

	public void includePullAction(boolean includePullAction){
		this.includePullAction = includePullAction;
	}

	public void includeLockableDoors(boolean lockableDoors){
		this.lockableDoors = lockableDoors;
	}

	public void setLockProbability(double lockProb){
		this.lockProb = lockProb;
	}


	@Override
	public Domain generateDomain() {

		SADomain domain = new SADomain();

		Attribute xatt = new Attribute(domain, ATT_X, Attribute.AttributeType.DISC);
		xatt.setDiscValuesForRange(0, maxX, 1);

		Attribute yatt = new Attribute(domain, ATT_Y, Attribute.AttributeType.DISC);
		yatt.setDiscValuesForRange(0, maxY, 1);

		Attribute topAtt = new Attribute(domain, ATT_TOP, Attribute.AttributeType.DISC);
		topAtt.setDiscValuesForRange(0, maxY, 1);

		Attribute leftAtt = new Attribute(domain, ATT_LEFT, Attribute.AttributeType.DISC);
		leftAtt.setDiscValuesForRange(0, maxX, 1);

		Attribute bottomAtt = new Attribute(domain, ATT_BOTTOM, Attribute.AttributeType.DISC);
		bottomAtt.setDiscValuesForRange(0, maxY, 1);

		Attribute rightAtt = new Attribute(domain, ATT_RIGHT, Attribute.AttributeType.DISC);
		rightAtt.setDiscValuesForRange(0, maxX, 1);

		Attribute colAtt = new Attribute(domain, ATT_COLOR, Attribute.AttributeType.DISC);
		colAtt.setDiscValues(COLORS);

		Attribute shapeAtt = new Attribute(domain, ATT_SHAPE, Attribute.AttributeType.DISC);
		shapeAtt.setDiscValues(SHAPES);

		if(this.includeDirectionAttribute){
			Attribute dirAtt = new Attribute(domain, ATT_DIR, Attribute.AttributeType.DISC);
			dirAtt.setDiscValues(DIRECTIONS);
		}

		if(this.lockableDoors){
			Attribute lockAtt = new Attribute(domain, ATT_LOCKED, Attribute.AttributeType.DISC);
			lockAtt.setDiscValues(Arrays.asList(LOCKABLE_STATES));
		}


		ObjectClass agent = new ObjectClass(domain, CLASS_AGENT);
		agent.addAttribute(xatt);
		agent.addAttribute(yatt);
		if(this.includeDirectionAttribute){
			agent.addAttribute(domain.getAttribute(ATT_DIR));
		}

		ObjectClass block = new ObjectClass(domain, CLASS_BLOCK);
		block.addAttribute(xatt);
		block.addAttribute(yatt);
		block.addAttribute(colAtt);
		block.addAttribute(shapeAtt);

		ObjectClass room = new ObjectClass(domain, CLASS_ROOM);
		this.addRectAtts(domain, room);
		room.addAttribute(colAtt);

		ObjectClass door = new ObjectClass(domain, CLASS_DOOR);
		this.addRectAtts(domain, door);

		if(this.lockableDoors){
			door.addAttribute(domain.getAttribute(ATT_LOCKED));
		}


		new MovementAction(ACTION_NORTH, domain, 0, 1, this.lockProb);
		new MovementAction(ACTION_SOUTH, domain, 0, -1, this.lockProb);
		new MovementAction(ACTION_EAST, domain, 1, 0, this.lockProb);
		new MovementAction(ACTION_WEST, domain, -1, 0, this.lockProb);
		if(this.includePullAction){
			new PullAction(domain);
		}


		new PF_InRegion(PF_AGENT_IN_ROOM, domain, new String[]{CLASS_AGENT, CLASS_ROOM}, false);
		new PF_InRegion(PF_BLOCK_IN_ROOM, domain, new String[]{CLASS_BLOCK, CLASS_ROOM}, false);

		new PF_InRegion(PF_AGENT_IN_DOOR, domain, new String[]{CLASS_AGENT, CLASS_DOOR}, true);
		new PF_InRegion(PF_BLOCK_IN_DOOR, domain, new String[]{CLASS_BLOCK, CLASS_DOOR}, true);

		for(String col : COLORS){
			new PF_IsColor(PF_RoomColorName(col), domain, new String[]{CLASS_ROOM}, col);
			new PF_IsColor(PF_BlockColorName(col), domain, new String[]{CLASS_BLOCK}, col);
		}

		for(String shape : SHAPES){
			new PF_IsShape(PF_BlockShapeName(shape), domain, new String[]{CLASS_BLOCK}, shape);
		}

		if(this.includeWallPF_s){
			new PF_WallTest(PF_WALL_NORTH, domain, 0, 1);
			new PF_WallTest(PF_WALL_SOUTH, domain, 0, -1);
			new PF_WallTest(PF_WALL_EAST, domain, 1, 0);
			new PF_WallTest(PF_WALL_WEST, domain, -1, 0);
		}


		return domain;
	}



	protected void addRectAtts(Domain domain, ObjectClass oc){
		oc.addAttribute(domain.getAttribute(ATT_TOP));
		oc.addAttribute(domain.getAttribute(ATT_LEFT));
		oc.addAttribute(domain.getAttribute(ATT_BOTTOM));
		oc.addAttribute(domain.getAttribute(ATT_RIGHT));
	}



	public static String PF_RoomColorName(String color){
		String capped = firstLetterCapped(color);
		return PF_RCOLORBASE + capped;
	}
	public static String PF_BlockColorName(String color){
		String capped = firstLetterCapped(color);
		return PF_BCOLORBASE + capped;
	}
	public static String PF_BlockShapeName(String shape){
		String capped = firstLetterCapped(shape);
		return PF_BSHAPEBASE + capped;
	}


	public static State getCleanState(Domain domain, int nRooms, int nDoors, int nBlocks){

		State s = new MutableState();

		//create  rooms
		createNInstances(domain, s, CLASS_ROOM, nRooms);

		//now create doors
		createNInstances(domain, s, CLASS_DOOR, nDoors);

		//now create blocks
		createNInstances(domain, s, CLASS_BLOCK, nBlocks);

		//create agent
		ObjectInstance o = new MutableObjectInstance(domain.getObjectClass(CLASS_AGENT), CLASS_AGENT +0);
		s.addObject(o);

		Attribute dirAtt = o.getObjectClass().getAttribute(ATT_DIR);
		if(dirAtt != null){
			o.setValue(ATT_DIR, "south");
		}

		return s;

	}

	public static State getClassicState(Domain domain){

		State s = getCleanState(domain, 3, 2, 1);

		setRoom(s, 0, 4, 0, 0, 8, "red");
		setRoom(s, 1, 8, 0, 4, 4, "green");
		setRoom(s, 2, 8, 4, 4, 8, "blue");

		setDoor(s, 0, 4, 6, 4, 6);
		setDoor(s, 1, 4, 2, 4, 2);

		setAgent(s, 6, 6);
		setBlock(s, 0, 2, 2, "basket", "red");


		return s;

	}


	public static void setAgent(State s, int x, int y){
		ObjectInstance o = s.getFirstObjectOfClass(CLASS_AGENT);
		o.setValue(ATT_X, x);
		o.setValue(ATT_Y, y);
	}

	public static void setAgent(State s, int x, int y, int dir){
		ObjectInstance o = s.getFirstObjectOfClass(CLASS_AGENT);
		o.setValue(ATT_X, x);
		o.setValue(ATT_Y, y);
		o.setValue(ATT_DIR, dir);
	}

	public static void setBlockPos(State s, int i, int x, int y){
		ObjectInstance o = s.getObjectsOfClass(CLASS_BLOCK).get(i);
		o.setValue(ATT_X, x);
		o.setValue(ATT_Y, y);
	}

	public static void setBlock(State s, int i, int x, int y, String shape, String color){
		ObjectInstance o = s.getObjectsOfClass(CLASS_BLOCK).get(i);
		setBlock(o, x, y, shape, color);
	}

	public static void setBlock(ObjectInstance o, int x, int y, String shape, String color){
		o.setValue(ATT_X, x);
		o.setValue(ATT_Y, y);
		o.setValue(ATT_SHAPE, shape);
		o.setValue(ATT_COLOR, color);
	}

	public static void setRoom(State s, int i, int top, int left, int bottom, int right, String color){
		ObjectInstance o = s.getObjectsOfClass(CLASS_ROOM).get(i);
		setRegion(o, top, left, bottom, right);
		o.setValue(ATT_COLOR, color);
	}

	public static void setDoor(State s, int i, int top, int left, int bottom, int right){
		ObjectInstance o = s.getObjectsOfClass(CLASS_DOOR).get(i);
		setRegion(o, top, left, bottom, right);
		if(o.getObjectClass().hasAttribute(ATT_LOCKED)){
			o.setValue(ATT_LOCKED, 0);
		}
	}

	public static void setDoorLocked(State s, int i, int lVal){
		ObjectInstance o = s.getObjectsOfClass(CLASS_DOOR).get(i);
		o.setValue(ATT_LOCKED, lVal);
	}

	public static void setRoom(ObjectInstance o, int top, int left, int bottom, int right, String color){
		setRegion(o, top, left, bottom, right);
		o.setValue(ATT_COLOR, color);
	}

	public static void setRegion(ObjectInstance o, int top, int left, int bottom, int right){
		o.setValue(ATT_TOP, top);
		o.setValue(ATT_LEFT, left);
		o.setValue(ATT_BOTTOM, bottom);
		o.setValue(ATT_RIGHT, right);
	}

	protected static void createNInstances(Domain domain, State s, String className, int n){
		for(int i = 0; i < n; i++){
			ObjectInstance o = new MutableObjectInstance(domain.getObjectClass(className), className+i);
			s.addObject(o);
		}
	}


	public static int maxRoomXExtent(State s){

		int max = 0;
		List<ObjectInstance> rooms = s.getObjectsOfClass(CLASS_ROOM);
		for(ObjectInstance r : rooms){
			int right = r.getIntValForAttribute(ATT_RIGHT);
			if(right > max){
				max = right;
			}
		}

		return max;
	}

	public static int maxRoomYExtent(State s){

		int max = 0;
		List <ObjectInstance> rooms = s.getObjectsOfClass(CLASS_ROOM);
		for(ObjectInstance r : rooms){
			int top = r.getIntValForAttribute(ATT_TOP);
			if(top > max){
				max = top;
			}
		}

		return max;
	}


	protected static String firstLetterCapped(String s){
		String firstLetter = s.substring(0, 1);
		String remainder = s.substring(1);
		return firstLetter.toUpperCase() + remainder;
	}

	public static ObjectInstance roomContainingPoint(State s, int x, int y){
		List<ObjectInstance> rooms = s.getObjectsOfClass(CLASS_ROOM);
		return regionContainingPoint(rooms, x, y, false);
	}

	public static ObjectInstance roomContainingPointIncludingBorder(State s, int x, int y){
		List<ObjectInstance> rooms = s.getObjectsOfClass(CLASS_ROOM);
		return regionContainingPoint(rooms, x, y, true);
	}

	public static ObjectInstance doorContainingPoint(State s, int x, int y){
		List<ObjectInstance> doors = s.getObjectsOfClass(CLASS_DOOR);
		return regionContainingPoint(doors, x, y, true);
	}

	protected static ObjectInstance regionContainingPoint(List <ObjectInstance> objects, int x, int y, boolean countBoundary){
		for(ObjectInstance o : objects){
			if(regionContainsPoint(o, x, y, countBoundary)){
				return o;
			}

		}

		return null;
	}

	public static boolean regionContainsPoint(ObjectInstance o, int x, int y, boolean countBoundary){
		int top = o.getIntValForAttribute(ATT_TOP);
		int left = o.getIntValForAttribute(ATT_LEFT);
		int bottom = o.getIntValForAttribute(ATT_BOTTOM);
		int right = o.getIntValForAttribute(ATT_RIGHT);

		if(countBoundary){
			if(y >= bottom && y <= top && x >= left && x <= right){
				return true;
			}
		}
		else{
			if(y > bottom && y < top && x > left && x < right){
				return true;
			}
		}

		return false;
	}

	public static ObjectInstance blockAtPoint(State s, int x, int y){

		List<ObjectInstance> blocks = s.getObjectsOfClass(CLASS_BLOCK);
		for(ObjectInstance b : blocks){
			int bx = b.getIntValForAttribute(ATT_X);
			int by = b.getIntValForAttribute(ATT_Y);

			if(bx == x && by == y){
				return b;
			}
		}

		return null;

	}


	public static boolean wallAt(State s, ObjectInstance r, int x, int y){

		int top = r.getIntValForAttribute(ATT_TOP);
		int left = r.getIntValForAttribute(ATT_LEFT);
		int bottom = r.getIntValForAttribute(ATT_BOTTOM);
		int right = r.getIntValForAttribute(ATT_RIGHT);

		//agent along wall of room check
		if(((x == left || x == right) && y >= bottom && y <= top) || ((y == bottom || y == top) && x >= left && x <= right)){

			//then only way for this to be a valid pos is if a door contains this point
			ObjectInstance door = doorContainingPoint(s, x, y);
			if(door == null){
				return true;
			}

		}

		return false;
	}

	public class MovementAction extends SimpleAction implements FullActionModel {

		protected int xdelta;
		protected int ydelta;
		protected double lockProb;

		public MovementAction(String name, Domain domain, int xdelta, int ydelta, double lockProb){
			super(name, domain);
			this.xdelta = xdelta;
			this.ydelta = ydelta;
			this.lockProb = lockProb;
		}

		@Override
		protected State performActionHelper(State s, GroundedAction ga) {

			ObjectInstance agent = s.getFirstObjectOfClass(CLASS_AGENT);
			int ax = agent.getIntValForAttribute(ATT_X);
			int ay = agent.getIntValForAttribute(ATT_Y);

			int nx = ax+xdelta;
			int ny = ay+ydelta;

			//ObjectInstance roomContaining = roomContainingPoint(s, ax, ay);
			ObjectInstance roomContaining = regionContainingPoint(s.getObjectsOfClass(CLASS_ROOM), ax, ay, true);


			boolean permissibleMove = false;
			ObjectInstance pushedBlock = blockAtPoint(s, nx, ny);
			if(pushedBlock != null){
				int bx = pushedBlock.getIntValForAttribute(ATT_X);
				int by = pushedBlock.getIntValForAttribute(ATT_Y);

				int nbx = bx + xdelta;
				int nby = by + ydelta;

				if(!wallAt(s, roomContaining, nbx, nby) && blockAtPoint(s, nbx, nby) == null){


					//is there a possible door that can be locked?
					boolean updatePosition = true;
					ObjectInstance doorAtNewPoint = doorContainingPoint(s, nbx, nby);
					if(doorAtNewPoint != null){
						if(doorAtNewPoint.getObjectClass().hasAttribute(ATT_LOCKED)) {
							int val = doorAtNewPoint.getIntValForAttribute(ATT_LOCKED);
							if(val == 2) { //locked door
								updatePosition = false;
							}
							else if(val == 0){ //unknown door
								double roll = RandomFactory.getMapped(0).nextDouble();
								if(roll < this.lockProb){
									//lock the dor
									updatePosition = false;
									doorAtNewPoint.setValue(ATT_LOCKED, 2);
								}
								else{
									//unlock the door
									doorAtNewPoint.setValue(ATT_LOCKED, 1);
								}
							}
						}
					}
					if(updatePosition) {
						pushedBlock.setValue(ATT_X, nbx);
						pushedBlock.setValue(ATT_Y, nby);
						permissibleMove = true;
					}

				}

			}
			else if(!wallAt(s, roomContaining, nx, ny)){
				permissibleMove = true;
			}

			if(permissibleMove){

				boolean updatePosition = true;

				//if doors are lockable, we must check whether there is special handling
				ObjectInstance doorAtNewPoint = doorContainingPoint(s, nx, ny);
				if(doorAtNewPoint != null){
					if(doorAtNewPoint.getObjectClass().hasAttribute(ATT_LOCKED)){
						int val = doorAtNewPoint.getIntValForAttribute(ATT_LOCKED);
						if(val == 2){ //locked door
							updatePosition = false;
						}
						else if(val == 0){ //unknown door
							double roll = RandomFactory.getMapped(0).nextDouble();
							if(roll < this.lockProb){
								//lock the dor
								updatePosition = false;
								doorAtNewPoint.setValue(ATT_LOCKED, 2);
							}
							else{
								//unlock the door
								doorAtNewPoint.setValue(ATT_LOCKED, 1);
							}
						}
					}
				}


				if(updatePosition) {
					agent.setValue(ATT_X, nx);
					agent.setValue(ATT_Y, ny);
				}
			}


			if(CleanupWorld.this.includeDirectionAttribute){
				if(this.xdelta == 1){
					agent.setValue(ATT_DIR, "east");
				}
				else if(this.xdelta == -1){
					agent.setValue(ATT_DIR, "west");
				}
				else if(this.ydelta == 1){
					agent.setValue(ATT_DIR, "north");
				}
				else if(this.ydelta == -1){
					agent.setValue(ATT_DIR, "south");
				}
			}


			return s;
		}

		@Override
		public List<TransitionProbability> getTransitions(State s, GroundedAction ga){

			State initialState = s;

			s = s.copy();

			ObjectInstance agent = s.getFirstObjectOfClass(CLASS_AGENT);
			int ax = agent.getIntValForAttribute(ATT_X);
			int ay = agent.getIntValForAttribute(ATT_Y);

			int nx = ax+xdelta;
			int ny = ay+ydelta;

			//ObjectInstance roomContaining = roomContainingPoint(s, ax, ay);
			ObjectInstance roomContaining = regionContainingPoint(s.getObjectsOfClass(CLASS_ROOM), ax, ay, true);

			ObjectInstance conflictDoor = null;

			boolean permissibleMove = false;
			ObjectInstance pushedBlock = blockAtPoint(s, nx, ny);
			if(pushedBlock != null){
				int bx = pushedBlock.getIntValForAttribute(ATT_X);
				int by = pushedBlock.getIntValForAttribute(ATT_Y);

				int nbx = bx + xdelta;
				int nby = by + ydelta;

				if(!wallAt(s, roomContaining, nbx, nby) && blockAtPoint(s, nbx, nby) == null){


					//is there a possible door that can be locked?
					ObjectInstance doorAtNewPoint = doorContainingPoint(s, nbx, nby);
					if(doorAtNewPoint != null){
						if(doorAtNewPoint.getObjectClass().hasAttribute(ATT_LOCKED)) {
							conflictDoor = doorAtNewPoint;
						}

					}

					pushedBlock.setValue(ATT_X, nbx);
					pushedBlock.setValue(ATT_Y, nby);
					permissibleMove = true;


				}

			}
			else if(!wallAt(s, roomContaining, nx, ny)){
				permissibleMove = true;
			}

			if(permissibleMove){


				//if doors are lockable, we must check whether there is special handling
				ObjectInstance doorAtNewPoint = doorContainingPoint(s, nx, ny);
				if(doorAtNewPoint != null){
					if(doorAtNewPoint.getObjectClass().hasAttribute(ATT_LOCKED)){
						conflictDoor = doorAtNewPoint;
					}
				}

				agent.setValue(ATT_X, nx);
				agent.setValue(ATT_Y, ny);

			}


			if(CleanupWorld.this.includeDirectionAttribute){
				if(this.xdelta == 1){
					agent.setValue(ATT_DIR, "east");
				}
				else if(this.xdelta == -1){
					agent.setValue(ATT_DIR, "west");
				}
				else if(this.ydelta == 1){
					agent.setValue(ATT_DIR, "north");
				}
				else if(this.ydelta == -1){
					agent.setValue(ATT_DIR, "south");
				}
			}

			if(conflictDoor == null){
				return Arrays.asList(new TransitionProbability(s, 1.));
			}
			if(conflictDoor.getIntValForAttribute(ATT_LOCKED) == 2){
				return Arrays.asList(new TransitionProbability(initialState, 1.));
			}
			if(conflictDoor.getIntValForAttribute(ATT_LOCKED) == 0){
				conflictDoor.setValue(ATT_LOCKED, 1); //open

				State lockedState = initialState.copy();
				lockedState.getObject(conflictDoor.getName()).setValue(ATT_LOCKED, 2);

				return Arrays.asList(new TransitionProbability(s, 1.-this.lockProb), new TransitionProbability(lockedState, lockProb));
			}


			return Arrays.asList(new TransitionProbability(s, 1.));
		}


	}

	public class PullAction extends SimpleAction implements FullActionModel{

		public PullAction(Domain domain){
			super(ACTION_PULL, domain);
		}

		@Override
		public boolean applicableInState(State s, GroundedAction ga){
			ObjectInstance agent = s.getFirstObjectOfClass(CLASS_AGENT);
			int ax = agent.getIntValForAttribute(ATT_X);
			int ay = agent.getIntValForAttribute(ATT_Y);
			int dir = agent.getIntValForAttribute(ATT_DIR);

			return this.blockToSwap(s, ax, ay, dir) != null;
		}

		@Override
		protected State performActionHelper(State s, GroundedAction ga) {

			ObjectInstance agent = s.getFirstObjectOfClass(CLASS_AGENT);
			int ax = agent.getIntValForAttribute(ATT_X);
			int ay = agent.getIntValForAttribute(ATT_Y);
			int dir = agent.getIntValForAttribute(ATT_DIR);

			ObjectInstance block = this.blockToSwap(s, ax, ay, dir);
			int bx = block.getIntValForAttribute(ATT_X);
			int by = block.getIntValForAttribute(ATT_Y);

			agent.setValue(ATT_X, bx);
			agent.setValue(ATT_Y, by);

			block.setValue(ATT_X, ax);
			block.setValue(ATT_Y, ay);

			if(CleanupWorld.this.includeDirectionAttribute){

				//face in direction of the block movement
				if(by - ay > 0){
					agent.setValue(ATT_DIR, "south");
				}
				else if(by - ay < 0){
					agent.setValue(ATT_DIR, "north");
				}
				else if(bx - ax > 0){
					agent.setValue(ATT_DIR, "west");
				}
				else if(bx - ax < 0){
					agent.setValue(ATT_DIR, "east");
				}

			}

			return s;
		}


		@Override
		public List<TransitionProbability> getTransitions(State s, GroundedAction ga){
			return this.deterministicTransition(s, ga);
		}


		/**
		 * Old block to swap method
		 * @param s
		 * @param ax
		 * @param ay
		 * @return
		 */
		protected ObjectInstance blockToSwap(State s, int ax, int ay){
			//ObjectInstance roomContaining = roomContainingPoint(s, ax, ay);
			ObjectInstance roomContaining = regionContainingPoint(s.getObjectsOfClass(CLASS_ROOM), ax, ay, true);


			ObjectInstance blockToSwap = null;
			//check if there is a block against the wall to the north south east or west
			if(wallAt(s, roomContaining, ax, ay+2)){
				blockToSwap = blockAtPoint(s, ax, ay+1);
				if(blockToSwap != null){
					return blockToSwap;
				}
			}
			if(wallAt(s, roomContaining, ax, ay-2)){
				blockToSwap = blockAtPoint(s, ax, ay-1);
				if(blockToSwap != null){
					return blockToSwap;
				}
			}
			if(wallAt(s, roomContaining, ax+2, ay)){
				blockToSwap = blockAtPoint(s, ax+1, ay);
				if(blockToSwap != null){
					return blockToSwap;
				}
			}
			if(wallAt(s, roomContaining, ax-2, ay)){
				blockToSwap = blockAtPoint(s, ax-1, ay);
				if(blockToSwap != null){
					return blockToSwap;
				}
			}



			return blockToSwap;
		}

		protected ObjectInstance blockToSwap(State s, int ax, int ay, int dir){


			if(dir == 0){
				//north
				return blockAtPoint(s, ax, ay+1);
			}
			else if(dir == 1){
				//south
				return blockAtPoint(s, ax, ay-1);
			}
			else if(dir == 2){
				//east
				return blockAtPoint(s, ax+1, ay);
			}
			else if(dir == 3){
				return blockAtPoint(s, ax-1, ay);
			}

			return null;

		}



	}



	public class PF_InRegion extends PropositionalFunction {

		protected boolean countBoundary;

		public PF_InRegion(String name, Domain domain, String [] params, boolean countBoundary){
			super(name, domain, params);
			this.countBoundary = countBoundary;
		}

		@Override
		public boolean isTrue(State s, String[] params) {

			ObjectInstance o = s.getObject(params[0]);
			int x = o.getIntValForAttribute(ATT_X);
			int y = o.getIntValForAttribute(ATT_Y);


			ObjectInstance region = s.getObject(params[1]);
			return regionContainsPoint(region, x, y, countBoundary);

		}

	}


	public class PF_IsColor extends PropositionalFunction{

		protected String colorName;

		public PF_IsColor(String name, Domain domain, String [] params, String color){
			super(name, domain, params);
			this.colorName = color;
		}

		@Override
		public boolean isTrue(State s, String[] params) {

			ObjectInstance o = s.getObject(params[0]);
			String col = o.getStringValForAttribute(ATT_COLOR);

			return this.colorName.equals(col);

		}

	}


	public class PF_IsShape extends PropositionalFunction{

		protected String shapeName;

		public PF_IsShape(String name, Domain domain, String [] params, String shape){
			super(name, domain, params);
			this.shapeName = shape;
		}

		@Override
		public boolean isTrue(State s, String[] params) {
			ObjectInstance o = s.getObject(params[0]);
			String shape = o.getStringValForAttribute(ATT_SHAPE);

			return this.shapeName.equals(shape);
		}



	}


	public class PF_WallTest extends PropositionalFunction{

		protected int dx;
		protected int dy;

		public PF_WallTest(String name, Domain domain, int dx, int dy){
			super(name, domain, CLASS_AGENT);
			this.dx = dx;
			this.dy = dy;
		}

		@Override
		public boolean isTrue(State s, String[] params) {
			ObjectInstance agent = s.getFirstObjectOfClass(CLASS_AGENT);
			int ax = agent.getIntValForAttribute(ATT_X);
			int ay = agent.getIntValForAttribute(ATT_Y);
			ObjectInstance agentRoom = roomContainingPoint(s, ax, ay);
			if(agentRoom == null){
				return false;
			}
			return wallAt(s, agentRoom, ax+this.dx, ay+this.dy);

		}



	}




	public static void main(String [] args){
		boolean runGroundLevelBoundedRTDP = true;

		if(runGroundLevelBoundedRTDP){
			double lockProb = 0.5;

			CleanupWorld dgen = new CleanupWorld();
			dgen.includeDirectionAttribute(true);
			dgen.includePullAction(true);
			dgen.includeWallPF_s(true);
			dgen.includeLockableDoors(true);
			dgen.setLockProbability(lockProb);
			Domain domain = dgen.generateDomain();

			State s = CleanupWorld.getClassicState(domain);

			StateConditionTest sc = new GroundedPropSC(new GroundedProp(domain.getPropFunction(CleanupWorld.PF_BLOCK_IN_ROOM),  new String[]{"block0", "room1"}));
			
			RewardFunction heuristicRF = new PullCostGoalRF(sc, 1., 0.);
			

			GroundedPropSC l0sc = new GroundedPropSC(new GroundedProp(domain.getPropFunction(CleanupWorld.PF_BLOCK_IN_ROOM), new String[]{"block0", "room1"}));
			GoalBasedRF l0rf = new GoalBasedRF(l0sc, 1., 0.);
			GoalConditionTF l0tf = new GoalConditionTF(l0sc);
			
			FixedDoorCleanupEnv env = new FixedDoorCleanupEnv(domain, l0rf, l0tf, s);
			
			
			long startTime = System.currentTimeMillis();
						
			ValueFunctionInitialization heuristic = CleanupDomainDriver.getL0Heuristic(s, heuristicRF);
			BoundedRTDP brtd = new BoundedRTDP(domain, l0rf, l0tf,0.99, new SimpleHashableStateFactory(false), new ValueFunctionInitialization.ConstantValueFunctionInitialization(0.), heuristic, 0.01, 500);
			brtd.setMaxRolloutDepth(50);
			brtd.toggleDebugPrinting(false);
			Policy P = brtd.planFromState(s);
			
			long endTime = System.currentTimeMillis();
			long duration = (endTime - startTime);
			System.out.println("total time: " + duration);
			EpisodeAnalysis ea = P.evaluateBehavior(env);
			
			Visualizer v = CleanupVisualizer.getVisualizer("amdp/data/resources/robotImages");
			//		System.out.println(ea.getState(0).toString());
			new EpisodeSequenceVisualizer(v, domain, Arrays.asList(ea));
		}
		else{



			CleanupWorld dgen = new CleanupWorld();
			dgen.includeDirectionAttribute(true);
			dgen.includePullAction(true);
			dgen.includeWallPF_s(true);
			dgen.includeLockableDoors(true);
			dgen.setLockProbability(0.5);
			Domain domain = dgen.generateDomain();

			State s = CleanupWorld.getClassicState(domain);

			/*ObjectInstance b2 = new ObjectInstance(domain.getObjectClass(CLASSBLOCK), CLASSBLOCK+1);
		s.addObject(b2);
		setBlock(s, 1, 3, 2, "moon", "red");*/

			Visualizer v = CleanupVisualizer.getVisualizer("data/resources/robotImages");
			VisualExplorer exp = new VisualExplorer(domain, v, s);

			exp.addKeyAction("w", ACTION_NORTH);
			exp.addKeyAction("s", ACTION_SOUTH);
			exp.addKeyAction("d", ACTION_EAST);
			exp.addKeyAction("a", ACTION_WEST);
			exp.addKeyAction("r", ACTION_PULL);

			exp.initGUI();

			List<TransitionProbability> tps = domain.getAction(ACTION_SOUTH).getAssociatedGroundedAction().getTransitions(s);
			for(TransitionProbability tp : tps){
				System.out.println(tp.s.toString());
				System.out.println("----------------");
			}

			System.out.println("========================");

			State s2 = s.copy();
			CleanupWorld.setAgent(s2, 6, 5);
			tps = domain.getAction(ACTION_SOUTH).getAssociatedGroundedAction().getTransitions(s2);
			for(TransitionProbability tp : tps){
				System.out.println(tp.s.toString());
				System.out.println("----------------");
			}
		}

	}


}
