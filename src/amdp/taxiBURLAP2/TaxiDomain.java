package amdp.taxiBURLAP2;

import burlap.oomdp.auxiliary.DomainGenerator;
import burlap.oomdp.core.Attribute;
import burlap.oomdp.core.Domain;
import burlap.oomdp.core.ObjectClass;
import burlap.oomdp.core.PropositionalFunction;
import burlap.oomdp.core.objects.MutableObjectInstance;
import burlap.oomdp.core.objects.ObjectInstance;
import burlap.oomdp.core.states.MutableState;
import burlap.oomdp.core.states.State;
import burlap.oomdp.singleagent.GroundedAction;
import burlap.oomdp.singleagent.SADomain;
import burlap.oomdp.singleagent.common.SimpleAction;
import burlap.oomdp.singleagent.explorer.VisualExplorer;
import burlap.oomdp.visualizer.Visualizer;

import java.util.ArrayList;
import java.util.List;

/**
 * @author James MacGlashan.
 */
public class TaxiDomain implements DomainGenerator{

	public static final String								XATT = "xAtt";
	public static final String								YATT = "yAtt";
	public static final String								FUELATT = "fuelAtt";
	public static final String								INTAXIATT = "inTaxiAtt";
	public static final String								OCCUPIEDTAXIATT = "occupiedTaxiAtt";
	public static final String								WALLMINATT = "wallMinAtt";
	public static final String								WALLMAXATT = "wallMaxAtt";
	public static final String								WALLOFFSETATT = "wallOffsetAtt";

	// this is the current location attribute
	public static final String								LOCATIONATT = "locationAtt";
	public static final String								GOALLOCATIONATT = "goalLocationAtt";

	public static final String								BEENPICKEDUPATT = "beenPickedupAtt";


	public static final String								TAXICLASS = "taxi";
	public static final String								LOCATIONCLASS = "location";
	public static final String								PASSENGERCLASS = "passenger";
	public static final String								HWALLCLASS = "horizontalWall";
	public static final String								VWALLCLASS = "verticalWall";


	public static final String								NORTHACTION = "north";
	public static final String								SOUTHACTION = "south";
	public static final String								EASTACTION = "east";
	public static final String								WESTACTION = "west";
	public static final String								PICKUPACTION = "pickup";
	public static final String								DROPOFFACTION = "dropoff";
	public static final String								FILLUPACTION = "fillup";


	public static final String								TAXIATLOCATIONPF = "taxiAt";
	public static final String								PASSENGERATGOALLOCATIONPF = "passengerAtGoal";
	public static final String								TAXIATPASSENGERPF = "taxiAtPassenger";
	public static final String								WALLNORTHPF = "wallNorth";
	public static final String								WALLSOUTHPF = "wallSouth";
	public static final String								WALLEASTPF = "wallEast";
	public static final String								WALLWESTPF = "wallWest";

	public static final String								PASSENGERPICKUPPF = "passengerPickUpPF";
	public static final String								PASSENGERPUTDOWNPF = "passengerPutDownPF";



	public int												maxX = 5;
	public int												maxY = 5;
	public int												maxFuel = 12;

	public boolean											includeFuel = true;
	public boolean											includePickedup = false;




	@Override
	public Domain generateDomain() {

		Domain domain = new SADomain();

		Attribute xAtt = new Attribute(domain, XATT, Attribute.AttributeType.DISC);
		xAtt.setDiscValuesForRange(0, maxX, 1);

		Attribute yAtt = new Attribute(domain, YATT, Attribute.AttributeType.DISC);
		yAtt.setDiscValuesForRange(0, maxY, 1);

		Attribute fuelAtt = null;;
		if(this.includeFuel){
			fuelAtt = new Attribute(domain, FUELATT, Attribute.AttributeType.DISC);
			fuelAtt.setDiscValuesForRange(0, maxFuel, 1);
		}

		Attribute inTaxiAtt = new Attribute(domain, INTAXIATT, Attribute.AttributeType.BOOLEAN);

		Attribute occupiedTaxiAtt = new Attribute(domain, OCCUPIEDTAXIATT, Attribute.AttributeType.BOOLEAN);

		Attribute wallMinAtt = new Attribute(domain, WALLMINATT, Attribute.AttributeType.DISC);
		wallMinAtt.setDiscValuesForRange(0, Math.max(this.maxX, this.maxY)+1, 1);

		Attribute wallMaxAtt = new Attribute(domain, WALLMAXATT, Attribute.AttributeType.DISC);
		wallMaxAtt.setDiscValuesForRange(0, Math.max(this.maxX, this.maxY)+1, 1);

		Attribute wallOffsetAtt = new Attribute(domain, WALLOFFSETATT, Attribute.AttributeType.DISC);
		wallOffsetAtt.setDiscValuesForRange(0, Math.max(this.maxX, this.maxY)+1, 1);


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
		locationAtt.setDiscValues(locationTypes);


		Attribute goalLocationAtt = new Attribute(domain, GOALLOCATIONATT, Attribute.AttributeType.DISC);
		List<String> goalLocationTypes = new ArrayList<String>();
		goalLocationTypes.add("red");
		goalLocationTypes.add("green");
		goalLocationTypes.add("blue");
		goalLocationTypes.add("yellow");
		goalLocationTypes.add("magenta");
		goalLocationTypes.add("pink");
		goalLocationTypes.add("orange");
		goalLocationTypes.add("cyan");
		goalLocationAtt.setDiscValues(goalLocationTypes);

		Attribute beenPickedupAtt = null;
		if(this.includePickedup){
			beenPickedupAtt = new Attribute(domain, BEENPICKEDUPATT, Attribute.AttributeType.BOOLEAN);
		}

		ObjectClass taxi = new ObjectClass(domain, TAXICLASS);
		taxi.addAttribute(xAtt);
		taxi.addAttribute(yAtt);
		taxi.addAttribute(occupiedTaxiAtt);
		if(this.includeFuel){
			taxi.addAttribute(fuelAtt);
		}

		ObjectClass location = new ObjectClass(domain, LOCATIONCLASS);
		location.addAttribute(xAtt);
		location.addAttribute(yAtt);
		location.addAttribute(locationAtt);

		ObjectClass passenger = new ObjectClass(domain, PASSENGERCLASS);
		passenger.addAttribute(xAtt);
		passenger.addAttribute(yAtt);
		passenger.addAttribute(inTaxiAtt);
		passenger.addAttribute(goalLocationAtt);
		if(this.includePickedup){
			passenger.addAttribute(beenPickedupAtt);
		}

		ObjectClass horizontalWall = new ObjectClass(domain, HWALLCLASS);
		horizontalWall.addAttribute(wallMinAtt);
		horizontalWall.addAttribute(wallMaxAtt);
		horizontalWall.addAttribute(wallOffsetAtt);

		ObjectClass verticalWall = new ObjectClass(domain, VWALLCLASS);
		verticalWall.addAttribute(wallMinAtt);
		verticalWall.addAttribute(wallMaxAtt);
		verticalWall.addAttribute(wallOffsetAtt);



		new MoveAction(NORTHACTION, domain, 0, 1);
		new MoveAction(SOUTHACTION, domain, 0, -1);
		new MoveAction(EASTACTION, domain, 1, 0);
		new MoveAction(WESTACTION, domain, -1, 0);
		new PickupAction(domain);
		new DropoffAction(domain);
		if(this.includeFuel){
			new FillupAction(domain);
		}

		new PF_TaxiAtLoc(TAXIATLOCATIONPF, domain, new String[]{LOCATIONCLASS});
		new PF_PassengerAtLoc(PASSENGERATGOALLOCATIONPF, domain, new String[]{PASSENGERCLASS});
		new PF_PickUp(PASSENGERPICKUPPF, domain, new String[]{});
		new PF_PutDown(PASSENGERPUTDOWNPF, domain, new String[]{});


		return domain;
	}


	public static State getClassicState(Domain domain){
		State s = new MutableState();

		ObjectInstance taxi = new MutableObjectInstance(domain.getObjectClass(TAXICLASS), "taxi");
		s.addObject(taxi);

		boolean usesFuel = domain.getAttribute(FUELATT) != null;

		if(usesFuel){
			addNInstances(domain, s, LOCATIONCLASS, 5);
		}
		else{
			addNInstances(domain, s, LOCATIONCLASS, 4);
		}

		addNInstances(domain, s, PASSENGERCLASS, 1);
		addNInstances(domain, s, HWALLCLASS, 2);
		addNInstances(domain, s, VWALLCLASS, 5);

		if(usesFuel){
			setTaxi(s, 0, 3, 12);
		}
		else{
			setTaxi(s, 0, 3);
		}

		setLocation(s, 0, 0, 0, 4);
		setLocation(s, 1, 0, 4, 1);
		setLocation(s, 2, 3, 0, 3);
		setLocation(s, 3, 4, 4, 2);

		if(usesFuel){
			setLocation(s, 4, 2, 1, 0);
		}

		setPassenger(s, 0, 3, 0, 1);

		setHWall(s, 0, 0, 5, 0);
		setHWall(s, 1, 0, 5, 5);

		setVWall(s, 0, 0, 5, 0);
		setVWall(s, 1, 0, 5, 5);
		setVWall(s, 2, 0, 2, 1);
		setVWall(s, 3, 3, 5, 2);
		setVWall(s, 4, 0, 2, 3);

		return s;

	}
	
	public static State getComplexState(Domain domain){
		State s = new MutableState();

		ObjectInstance taxi = new MutableObjectInstance(domain.getObjectClass(TAXICLASS), "taxi");
		s.addObject(taxi);

		boolean usesFuel = domain.getAttribute(FUELATT) != null;

		if(usesFuel){
			addNInstances(domain, s, LOCATIONCLASS, 5);
		}
		else{
			addNInstances(domain, s, LOCATIONCLASS, 4);
		}

		addNInstances(domain, s, PASSENGERCLASS, 2);
		addNInstances(domain, s, HWALLCLASS, 2);
		addNInstances(domain, s, VWALLCLASS, 5);

		if(usesFuel){
			setTaxi(s, 0, 3, 12);
		}
		else{
			setTaxi(s, 0, 3);
		}

		setLocation(s, 0, 0, 0, 4);
		setLocation(s, 1, 0, 4, 1);
		setLocation(s, 2, 3, 0, 3);
		setLocation(s, 3, 4, 4, 2);

		if(usesFuel){
			setLocation(s, 4, 2, 1, 0);
		}

		setPassenger(s, 0, 3, 0, 1);
		setPassenger(s, 1, 0, 0, 2);

		setHWall(s, 0, 0, 5, 0);
		setHWall(s, 1, 0, 5, 5);

		setVWall(s, 0, 0, 5, 0);
		setVWall(s, 1, 0, 5, 5);
		setVWall(s, 2, 0, 2, 1);
		setVWall(s, 3, 3, 5, 2);
		setVWall(s, 4, 0, 2, 3);

		return s;

	}
	

	protected static void addNInstances(Domain domain, State s, String className, int n){
		ObjectClass oc = domain.getObjectClass(className);
		for(int i = 0; i < n; i++){
			ObjectInstance o = new MutableObjectInstance(oc, className+i);
			s.addObject(o);
		}
	}


	public static void setTaxi(State s, int x, int y){
		ObjectInstance taxi = s.getFirstObjectOfClass(TAXICLASS);
		taxi.setValue(XATT, x);
		taxi.setValue(YATT, y);
		taxi.setValue(OCCUPIEDTAXIATT , 0);

	}

	public static void setTaxi(State s, int x, int y, int fuel){
		ObjectInstance taxi = s.getFirstObjectOfClass(TAXICLASS);
		setTaxi(s, x, y);
		taxi.setValue(FUELATT, fuel);
	}

	public static void setLocation(State s, int i, int x, int y, int lt){
		ObjectInstance l = s.getObjectsOfClass(LOCATIONCLASS).get(i);
		l.setValue(XATT, x);
		l.setValue(YATT, y);
		l.setValue(LOCATIONATT, lt);
	}

	public static void setPassenger(State s, int i, int x, int y, int lt){
		ObjectInstance p = s.getObjectsOfClass(PASSENGERCLASS).get(i);
		p.setValue(XATT, x);
		p.setValue(YATT, y);
		p.setValue(GOALLOCATIONATT, lt);
		p.setValue(INTAXIATT, 0);
		if(p.getObjectClass().domain.getAction(BEENPICKEDUPATT) != null){
			p.setValue(BEENPICKEDUPATT, 0);
		}

	}

	public static void setHWall(State s, int i, int wallMin, int wallMax, int wallOffset){
		ObjectInstance w = s.getObjectsOfClass(HWALLCLASS).get(i);
		w.setValue(WALLMINATT, wallMin);
		w.setValue(WALLMAXATT, wallMax);
		w.setValue(WALLOFFSETATT, wallOffset);
	}

	public static void setVWall(State s, int i, int wallMin, int wallMax, int wallOffset){
		ObjectInstance w = s.getObjectsOfClass(VWALLCLASS).get(i);
		w.setValue(WALLMINATT, wallMin);
		w.setValue(WALLMAXATT, wallMax);
		w.setValue(WALLOFFSETATT, wallOffset);
	}

	protected State move(State s, int dx, int dy){

		ObjectInstance taxi = s.getFirstObjectOfClass(TAXICLASS);
		int tx = taxi.getIntValForAttribute(XATT);
		int ty = taxi.getIntValForAttribute(YATT);

		int nx = tx+dx;
		int ny = ty+dy;

		//using fuel?
		if(this.includeFuel){
			int fuel = taxi.getIntValForAttribute(FUELATT);
			if(fuel == 0){
				//no movement possible
				return s;
			}
			taxi.setValue(FUELATT, fuel-1);
		}



		//check for wall bounding

		if(dx > 0){
			List<ObjectInstance> vwalls = s.getObjectsOfClass(VWALLCLASS);
			for(ObjectInstance wall : vwalls){
				if(wallEast(tx, ty, wall)){
					nx = tx;
					break;
				}
			}
		}
		else if(dx < 0){
			List<ObjectInstance> vwalls = s.getObjectsOfClass(VWALLCLASS);
			for(ObjectInstance wall : vwalls){
				if(wallWest(tx, ty, wall)){
					nx = tx;
					break;
				}
			}
		}
		else if(dy > 0){
			List<ObjectInstance> hwalls = s.getObjectsOfClass(HWALLCLASS);
			for(ObjectInstance wall : hwalls){
				if(wallNorth(tx, ty, wall)){
					ny = ty;
					break;
				}
			}
		}
		else if(dy < 0){
			List<ObjectInstance> hwalls = s.getObjectsOfClass(HWALLCLASS);
			for(ObjectInstance wall : hwalls){
				if(wallSouth(tx, ty, wall)){
					ny = ty;
					break;
				}
			}
		}

		//		boolean locationUpdate = false;
		//		int location = 0;
		//
		//		List<ObjectInstance> locations = s.getObjectsOfClass(LOCATIONCLASS);
		//		for(ObjectInstance l : locations){
		//			int xl = l.getIntValForAttribute(XATT);
		//			int yl = l.getIntValForAttribute(YATT);
		//			if(xl==nx && yl==ny){
		//				locationUpdate = true;
		//				location  = l.getIntValForAttribute(LOCATIONATT);
		//				break;
		//			}
		//		}


		taxi.setValue(XATT, nx);
		taxi.setValue(YATT, ny);


		//do we need to move a passenger as well?
		List<ObjectInstance> passengers = s.getObjectsOfClass(PASSENGERCLASS);
		for(ObjectInstance p : passengers){
			int inTaxi = p.getIntValForAttribute(INTAXIATT);
			if(inTaxi == 1){
				p.setValue(XATT, nx);
				p.setValue(YATT, ny);
				//				if(locationUpdate){ 
				//					p.setValue(LOCATIONATT, location);			
				//				}
			}
		}

		//check for location and update location of taxi and passenger



		return s;
	}



	public class MoveAction extends SimpleAction.SimpleDeterministicAction {

		int dx;
		int dy;

		public MoveAction(String name, Domain domain, int dx, int dy){
			super(name, domain);
			this.dx = dx;
			this.dy = dy;
		}

		@Override
		protected State performActionHelper(State s, GroundedAction ga) {
			return move(s, this.dx, this.dy);
		}


	}


	public class PickupAction extends SimpleAction.SimpleDeterministicAction{

		public PickupAction(Domain domain){
			super(PICKUPACTION, domain);
		}


		@Override
		protected State performActionHelper(State s, GroundedAction ga) {

			ObjectInstance taxi = s.getFirstObjectOfClass(TAXICLASS);
			int tx = taxi.getIntValForAttribute(XATT);
			int ty = taxi.getIntValForAttribute(YATT);
			boolean taxiOccupied = taxi.getBooleanValForAttribute(OCCUPIEDTAXIATT);

			if(!taxiOccupied){
				List<ObjectInstance> passengers = s.getObjectsOfClass(PASSENGERCLASS);

				for(ObjectInstance p : passengers){
					int px = p.getIntValForAttribute(XATT);
					int py = p.getIntValForAttribute(YATT);

					if(tx == px && ty == py ){
						p.setValue(INTAXIATT, 1);
						taxi.setValue(OCCUPIEDTAXIATT, 1);
						break;
					}

				}
			}

			return s;
		}


	}

	public class DropoffAction extends SimpleAction.SimpleDeterministicAction{

		public DropoffAction(Domain domain){
			super(DROPOFFACTION, domain);
		}

		@Override
		protected State performActionHelper(State s, GroundedAction ga) {

			ObjectInstance taxi = s.getFirstObjectOfClass(TAXICLASS);
			boolean taxiOccupied = taxi.getBooleanValForAttribute(OCCUPIEDTAXIATT);

			if(taxiOccupied){
				List<ObjectInstance> passengers = s.getObjectsOfClass(PASSENGERCLASS);
				for(ObjectInstance p : passengers){
					int in = p.getIntValForAttribute(INTAXIATT);
					if(in == 1){
						p.setValue(INTAXIATT, 0);
						taxi.setValue(OCCUPIEDTAXIATT, 0);
						break;
					}
				}
			}

			return s;
		}


	}


	public class FillupAction extends SimpleAction.SimpleDeterministicAction{

		public FillupAction(Domain domain){
			super(FILLUPACTION, domain);
		}


		@Override
		protected State performActionHelper(State s, GroundedAction ga) {

			if(!includeFuel){
				return s;
			}

			ObjectInstance taxi = s.getFirstObjectOfClass(TAXICLASS);
			int tx = taxi.getIntValForAttribute(XATT);
			int ty = taxi.getIntValForAttribute(YATT);

			List<ObjectInstance> locations = s.getObjectsOfClass(LOCATIONCLASS);
			for(ObjectInstance l : locations){
				int lt = l.getIntValForAttribute(LOCATIONATT);
				if(lt == 0){
					int lx = l.getIntValForAttribute(XATT);
					int ly = l.getIntValForAttribute(YATT);
					if(tx == lx && ty == ly){
						taxi.setValue(FUELATT, maxFuel);
					}
				}
			}

			return s;
		}




	}


	protected static boolean wallEast(int tx, int ty, ObjectInstance wall){
		int wallo = wall.getIntValForAttribute(WALLOFFSETATT);
		if(wallo == tx+1){
			int wallmin = wall.getIntValForAttribute(WALLMINATT);
			int wallmax = wall.getIntValForAttribute(WALLMAXATT);
			return ty >= wallmin && ty < wallmax;
		}
		return false;
	}

	protected static boolean wallWest(int tx, int ty, ObjectInstance wall){
		int wallo = wall.getIntValForAttribute(WALLOFFSETATT);
		if(wallo == tx){
			int wallmin = wall.getIntValForAttribute(WALLMINATT);
			int wallmax = wall.getIntValForAttribute(WALLMAXATT);
			return ty >= wallmin && ty < wallmax;
		}
		return false;
	}


	protected static boolean wallNorth(int tx, int ty, ObjectInstance wall){
		int wallo = wall.getIntValForAttribute(WALLOFFSETATT);
		if(wallo == ty+1){
			int wallmin = wall.getIntValForAttribute(WALLMINATT);
			int wallmax = wall.getIntValForAttribute(WALLMAXATT);
			return tx >= wallmin && tx < wallmax;
		}
		return false;
	}

	protected static boolean wallSouth(int tx, int ty, ObjectInstance wall){
		int wallo = wall.getIntValForAttribute(WALLOFFSETATT);
		if(wallo == ty){
			int wallmin = wall.getIntValForAttribute(WALLMINATT);
			int wallmax = wall.getIntValForAttribute(WALLMAXATT);
			return tx >= wallmin && tx < wallmax;
		}
		return false;
	}

	//propositional function for taxi at location for navigate
	public class PF_TaxiAtLoc extends PropositionalFunction{



		public PF_TaxiAtLoc(String name, Domain domain, String [] params){
			super(name, domain, params);
		}

		@Override
		public boolean isTrue(State s, String[] params) {
			ObjectInstance o = s.getFirstObjectOfClass(TAXICLASS);
			int xt = o.getIntValForAttribute(XATT);
			int yt = o.getIntValForAttribute(YATT);
			// params here are the name of a location like Location 1

			boolean returnValue = false;
			ObjectInstance location = s.getObject(params[0]);

			int xl = location.getIntValForAttribute(XATT);
			int yl = location.getIntValForAttribute(YATT);
			if(xt==xl && yt==yl ){
				returnValue = true;
			}

			return returnValue;
		}
	}


	public class PF_PassengerAtLoc extends PropositionalFunction{

		public PF_PassengerAtLoc(String name, Domain domain, String [] params){
			super(name, domain, params);
		}

		@Override
		public boolean isTrue(State s, String[] params) {
			ObjectInstance p = s.getObject(params[0]);
			//			System.out.println(params[0]);
			int xp = p.getIntValForAttribute(XATT);
			int yp = p.getIntValForAttribute(YATT);
			boolean inTaxi = p.getBooleanValForAttribute(INTAXIATT);
			String goalLocation = p.getStringValForAttribute(GOALLOCATIONATT);
			// params here are the name of a location like Location 1

			boolean returnValue = false;
			List<ObjectInstance> locations = s.getObjectsOfClass(LOCATIONCLASS);
			for(ObjectInstance location : locations){
				//				System.out.println("in taxi domain: " +location.getName());
				if(location.getStringValForAttribute(LOCATIONATT).equals(goalLocation)){
					int xl = location.getIntValForAttribute(XATT);
					int yl = location.getIntValForAttribute(YATT);
					if(xp==xl && yp==yl && !inTaxi ){
						returnValue = true;
					}
					break;
				}

			}


			return returnValue;
		}
	}

	//propositional function for pick up
	public class PF_PickUp extends PropositionalFunction{



		public PF_PickUp(String name, Domain domain, String [] params){
			super(name, domain, params);
		}

		@Override
		public boolean isTrue(State s, String[] params) {
			ObjectInstance o = s.getFirstObjectOfClass(TAXICLASS);
			int xt = o.getIntValForAttribute(XATT);
			int yt = o.getIntValForAttribute(YATT);
			boolean taxiOccupied = o.getBooleanValForAttribute(OCCUPIEDTAXIATT);
			// params here are the location colour - red, green, blue, yellow, magenta 

			boolean returnValue = false;
			List<ObjectInstance> passengers = s.getObjectsOfClass(PASSENGERCLASS);
			for(int i=0;i<passengers.size();i++){
				int xp = passengers.get(i).getIntValForAttribute(XATT);
				int yp = passengers.get(i).getIntValForAttribute(YATT);
				boolean inTaxi = passengers.get(i).getBooleanValForAttribute(INTAXIATT);
				if(xt==xp && yt==yp && inTaxi && taxiOccupied){
					returnValue = true;
					break;
				}
			}
			return returnValue;
		}
	}

	//propositional function for put down

	public class PF_PutDown extends PropositionalFunction{



		public PF_PutDown(String name, Domain domain, String [] params){
			super(name, domain, params);
		}

		@Override
		public boolean isTrue(State s, String[] params) {
			ObjectInstance o = s.getFirstObjectOfClass(TAXICLASS);
			int xt = o.getIntValForAttribute(XATT);
			int yt = o.getIntValForAttribute(YATT);
			boolean taxiOccupied = o.getBooleanValForAttribute(OCCUPIEDTAXIATT);
			// params here are the location colour - red, green, blue, yellow, magenta 

			boolean returnValue = false;
			List<ObjectInstance> passengers = s.getObjectsOfClass(PASSENGERCLASS);
			for(int i=0;i<passengers.size();i++){
				int xp = passengers.get(i).getIntValForAttribute(XATT);
				int yp = passengers.get(i).getIntValForAttribute(YATT);
				boolean inTaxi = passengers.get(i).getBooleanValForAttribute(INTAXIATT);
				if(xt==xp && yt==yp && !inTaxi && !taxiOccupied){
					returnValue = true;
					break;
				}
			}
			return returnValue;
		}
	}



	/**
	 * @param args
	 */
	public static void main(String[] args) {

		TaxiDomain dg = new TaxiDomain();
		dg.includeFuel = false;
		Domain d = dg.generateDomain();
		State s = TaxiDomain.getClassicState(d);

		//TerminalExplorer exp = new TerminalExplorer(d);
		//exp.exploreFromState(s);


		Visualizer v = TaxiVisualizer.getVisualizer(5, 5);
		VisualExplorer exp = new VisualExplorer(d, v, s);


		exp.addKeyAction("w", TaxiDomain.NORTHACTION);
		exp.addKeyAction("s", TaxiDomain.SOUTHACTION);
		exp.addKeyAction("d", TaxiDomain.EASTACTION);
		exp.addKeyAction("a", TaxiDomain.WESTACTION);
		exp.addKeyAction("p", TaxiDomain.PICKUPACTION);
		exp.addKeyAction(";", TaxiDomain.DROPOFFACTION);
		exp.addKeyAction("f", TaxiDomain.FILLUPACTION);


		exp.initGUI();


	}


}
