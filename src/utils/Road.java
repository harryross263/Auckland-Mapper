package utils;
import java.util.ArrayList;
import java.util.List;

/**
 * Road
 * 
 * @author harryross
 * @version 1.0
 */

public class Road {

	private int id;
	private String name;
	private String city;
	private boolean oneway;
	private int speed;
	private int roadClass;
	private boolean notForCars;
	private boolean notForPedestrians;
	private boolean notForBicycles;
	private List<Segment> segments = new ArrayList<Segment>();

	/** Construct a new Road object */
	public Road(int id, String name, boolean oneway, int speed, int roadClass,
			boolean notForCars, boolean notForPedestrians,
			boolean notForBicycles) {
		this.id = id;
		this.name = name;
		this.oneway = oneway;
		this.speed = speed;
		this.roadClass = roadClass;
		this.notForCars = notForCars;
		this.notForPedestrians = notForPedestrians;
		this.notForBicycles = notForBicycles;
	}

	/** Construct a new Road object from a line from the data file */
	public Road(String line) {
		String[] values = line.split("\t");
		this.id = Integer.parseInt(values[0]); // id
		this.name = values[2];
		this.city = values[3];
		if (this.city.equals("-")) {
			this.city = "";
		}
		this.oneway = values[4].equals("1");
		this.speed = Integer.parseInt(values[5]);
		this.roadClass = Integer.parseInt(values[6]);
		this.notForCars = values[7].equals("1");
		this.notForPedestrians = values[8].equals("1");
		this.notForBicycles = values[9].equals("1");
	}

	public int getID() {
		return this.id;
	}

	public String getName() {
		return this.name;
	}

	public String getFullName() {
		if (this.city == "") {
			return this.name;
		}
		return this.name + " " + this.city;
	}

	public int getRoadclass() {
		return this.roadClass;
	}

	public int getSpeed() {
		return this.speed;
	}

	public boolean isOneWay() {
		return this.oneway;
	}

	public boolean isNotForCars() {
		return this.notForCars;
	}

	public boolean isNotForPedestrians() {
		return this.notForPedestrians;
	}

	public boolean isNotForBicycles() {
		return this.notForBicycles;
	}

	public void addSegment(Segment seg) {
		this.segments.add(seg);
	}

	public List<Segment> getSegments() {
		return this.segments;
	}

	@Override
	public String toString() {
		return "Road: " + getFullName();
	}

	public int getSpeedLimit() {
		switch (speed) {
		case 0:
			return 4;
		case 1:
			return 18;
		case 2:
			return 36;
		case 3:
			return 54;
		case 4:
			return 72;
		case 5:
			return 90;
		case 6:
			return 99;
		default:
			return 4;
		}
	}

}
