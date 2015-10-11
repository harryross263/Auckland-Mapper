package utils;
import java.awt.Graphics;
import java.awt.Point;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Node: A Node is a point an intersection in an infrastructure graph.
 * 
 * @author harryross
 * @version 1.1
 */
public class Node {

	private int id;
	private Location loc;
	private List<Segment> outNeighbours = new ArrayList<Segment>(2);
	private List<Segment> inNeighbours = new ArrayList<Segment>(2);
	private boolean visited;
	private Node pathFrom;
	private double cost;
	private int depth;

	/**
	 * Constructs a new Node object from raw data, id and location.
	 * 
	 * @param id
	 * @param l
	 */
	public Node(int id, Location loc) {
		this.id = id;
		this.loc = loc;
	}

	/**
	 * Constructs a new Node object from a given data line.
	 * 
	 * @param line
	 */
	public Node(String line) {
		String[] values = line.split("\t");
		this.id = Integer.parseInt(values[0]);
		double lat = Double.parseDouble(values[1]);
		double lon = Double.parseDouble(values[2]);
		this.loc = Location.newFromLatLon(lat, lon);
	}

	public int getID() {
		return id;
	}

	public Location getLoc() {
		return this.loc;
	}

	public void addInSegment(Segment seg) {
		inNeighbours.add(seg);
	}

	public void addOutSegment(Segment seg) {
		outNeighbours.add(seg);
	}

	public List<Segment> getOutNeighbours() {
		return outNeighbours;
	}

	public List<Segment> getInNeighbours() {
		return inNeighbours;
	}

	public boolean closeTo(Location place, double dist) {
		return loc.closeTo(place, dist);
	}

	public double distanceTo(Location place) {
		return loc.distanceTo(place);
	}

	public void draw(Graphics g, Location origin, double scale) {
		Point p = loc.getPoint(origin, scale);
		g.fillRect(p.x, p.y, 2, 2);
	}

	public void setVisited(boolean b) {
		visited = b;
	}

	public boolean getVisited() {
		return visited;
	}

	public void setFrom(Node n) {
		pathFrom = n;
	}

	public Node getPath() {
		return pathFrom;
	}

	public void setCost(double c) {
		cost = c;
	}

	public void setDepth(int d) {
		depth = d;
	}

	public int getDepth() {
		return depth;
	}

	public double getCost() {
		return cost;
	}

	public static Node find(Map<Integer, Node> nodes, Location loc1,
			Location origin, double scale) {

		for (Node n : nodes.values()) {

			Point a = loc1.getPoint(origin, scale);
			Point b = n.loc.getPoint(origin, scale);

			if (Math.abs(a.x - b.x) <= Math.max(scale / 25, 1)
					&& Math.abs(a.y - b.y) <= Math.max(scale / 25, 1))
				return n;
		}
		return null;
	}

	public String toString() {
		StringBuilder b = new StringBuilder(String.format(
				"Intersection %d: at %s; Roads:  ", id, loc));
		Set<String> roadNames = new HashSet<String>();
		for (Segment neigh : inNeighbours) {
			roadNames.add(neigh.getRoad().getName());
		}
		for (Segment neigh : outNeighbours) {
			roadNames.add(neigh.getRoad().getName());
		}
		for (String name : roadNames) {
			b.append(name).append(", ");
		}
		return b.toString();
	}

	public String getRoadNames() {
		StringBuilder b = new StringBuilder(String.format("Start: "));
		Set<String> roadNames = new HashSet<String>();
		for (Segment neigh : inNeighbours) {
			roadNames.add(neigh.getRoad().getName());
		}
		for (Segment neigh : outNeighbours) {
			roadNames.add(neigh.getRoad().getName());
		}
		for (String name : roadNames) {
			b.append(name).append(", ");
		}
		return b.toString();
	}

	public String getEndRoadNames() {
		StringBuilder b = new StringBuilder(String.format("End: "));
		Set<String> roadNames = new HashSet<String>();
		for (Segment neigh : inNeighbours) {
			roadNames.add(neigh.getRoad().getName());
		}
		for (Segment neigh : outNeighbours) {
			roadNames.add(neigh.getRoad().getName());
		}
		for (String name : roadNames) {
			b.append(name).append(", ");
		}
		return b.toString();
	}

}
