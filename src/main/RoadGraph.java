package main;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import utils.Location;
import utils.Node;
import utils.Restriction;
import utils.Road;
import utils.Segment;

/**
 * RoadGraph data structure containing nodes and their connections.
 * 
 * @author harryross
 * @version 1.1
 */
public class RoadGraph {

	// the map containing the graph of nodes (and roadsegments), hashed by the
	// nodeID
	private Map<Integer, Node> nodes = new HashMap<Integer, Node>();

	// the map of roads, hashed by the roadID
	private Map<Integer, Road> roads = new HashMap<Integer, Road>();;

	// the map of roads, hashed by name (for the trie)
	private Map<String, Set<Road>> roadsByName = new HashMap<String, Set<Road>>();;

	private Set<String> roadNames = new HashSet<String>();

	private Set<Restriction> restrictionSet = new HashSet<Restriction>();

	public String loadData(String dataDirectory) {
		String report = "";
		System.out.println("Loading roads...");
		loadRoads(dataDirectory);
		report += String.format("Loaded %,d roads, with %,d distinct road names%n", roads.entrySet().size(),
				roadNames.size());
		System.out.println("Loading intersections...");
		loadNodes(dataDirectory);
		report += String.format("Loaded %,d intersections%n", nodes.entrySet().size());
		System.out.println("Loading road segments...");
		loadSegments(dataDirectory);
		report += String.format("Loaded %,d road segments%n", numSegments());
		return report;
	}

	public void loadRoads(String dataDirectory) {
		File roadFile = new File(dataDirectory + "roadID-roadInfo.tab");
		if (!roadFile.exists()) {
			System.out.println("roadID-roadInfo.tab not found");
			return;
		}
		BufferedReader data;
		try {
			data = new BufferedReader(new FileReader(roadFile));
			data.readLine(); // throw away header line.
			while (true) {
				String line = data.readLine();
				if (line == null) {
					break;
				}
				Road road = new Road(line);
				roads.put(road.getID(), road);
				String fullName = road.getFullName();
				roadNames.add(fullName);
				Set<Road> rds = roadsByName.get(fullName);
				if (rds == null) {
					rds = new HashSet<Road>(4);
					roadsByName.put(fullName, rds);
				}
				rds.add(road);
			}
		} catch (IOException e) {
			System.out.println("Failed to open roadID-roadInfo.tab: " + e);
		}
	}

	public void loadNodes(String dataDirectory) {
		File nodeFile = new File(dataDirectory + "nodeID-lat-lon.tab");
		if (!nodeFile.exists()) {
			System.out.println("nodeID-lat-lon.tab not found");
			return;
		}
		BufferedReader data;
		try {
			data = new BufferedReader(new FileReader(nodeFile));
			while (true) {
				String line = data.readLine();
				if (line == null) {
					break;
				}
				Node node = new Node(line);
				nodes.put(node.getID(), node);
			}
		} catch (IOException e) {
			System.out.println("Failed to open roadID-roadInfo.tab: " + e);
		}
	}

	public void loadSegments(String dataDirectory) {
		File segFile = new File(dataDirectory + "roadSeg-roadID-length-nodeID-nodeID-coords.tab");
		if (!segFile.exists()) {
			System.out.println("roadSeg-roadID-length-nodeID-nodeID-coords.tab not found");
			return;
		}
		BufferedReader data;
		try {
			data = new BufferedReader(new FileReader(segFile));
			data.readLine(); // get rid of headers
			while (true) {
				String line = data.readLine();
				if (line == null) {
					break;
				}
				Segment seg = new Segment(line, roads, nodes);
				Node node1 = seg.getStartNode();
				Node node2 = seg.getEndNode();
				node1.addOutSegment(seg);
				node2.addInSegment(seg);
				Road road = seg.getRoad();
				road.addSegment(seg);
				if (!road.isOneWay()) {
					Segment revSeg = seg.reverse();
					node2.addOutSegment(revSeg);
					node1.addInSegment(revSeg);
				}
			}
		} catch (IOException e) {
			System.out.println("Failed to open roadID-roadInfo.tab: " + e);
		}
	}

	public void loadRestrictions(String dataDirectory) {
		File segFile = new File(dataDirectory + "restrictions.tab");
		if (!segFile.exists()) {
			System.out.println("restrictions.tab not found");
			return;
		}
		BufferedReader data;
		try {
			data = new BufferedReader(new FileReader(segFile));
			data.readLine(); // get rid of headers
			while (true) {
				String line = data.readLine();
				if (line == null) {
					break;
				}
				Restriction r = new Restriction(data.readLine());
				restrictionSet.add(r);
			}
		} catch (IOException e) {
			System.out.println("Failed to open roadID-roadInfo.tab: " + e);
		}
	}

	public double[] getBoundaries() {
		double west = Double.POSITIVE_INFINITY;
		double east = Double.NEGATIVE_INFINITY;
		double south = Double.POSITIVE_INFINITY;
		double north = Double.NEGATIVE_INFINITY;

		for (Node node : nodes.values()) {
			Location loc = node.getLoc();
			if (loc.x < west) {
				west = loc.x;
			}
			if (loc.x > east) {
				east = loc.x;
			}
			if (loc.y < south) {
				south = loc.y;
			}
			if (loc.y > north) {
				north = loc.y;
			}
		}
		return new double[] { west, east, south, north };
	}

	public void checkNodes() {
		for (Node node : nodes.values()) {
			if (node.getOutNeighbours().isEmpty() && node.getInNeighbours().isEmpty()) {
				System.out.println("Orphan: " + node);
			}
		}
	}

	public int numSegments() {
		int ans = 0;
		for (Node node : nodes.values()) {
			ans += node.getOutNeighbours().size();
		}
		return ans;
	}

	public void redraw(Graphics g, Location origin, double scale) {
		g.setColor(Color.black);
		for (Node node : nodes.values()) {
			for (Segment seg : node.getOutNeighbours()) {
				seg.draw(g, origin, scale);
			}
		}
		g.setColor(Color.blue);
		for (Node node : nodes.values()) {
			node.draw(g, origin, scale);
		}
	}

	public Node findNode(Point point, Location origin, double scale) {
		Location mousePlace = Location.newFromPoint(point, origin, scale);
		Node closestNode = null;
		double mindist = Double.POSITIVE_INFINITY;
		for (Node node : nodes.values()) {
			double dist = node.distanceTo(mousePlace);
			if (dist < mindist) {
				mindist = dist;
				closestNode = node;
			}
		}
		return closestNode;
	}

	/**
	 * Returns a set of full road names that match the query. If the query
	 * matches a full road name exactly, then it returns just that name
	 */
	public Set<String> lookupName(String query) {
		Set<String> ans = new HashSet<String>(10);
		if (query == null)
			return null;
		query = query.toLowerCase();
		for (String name : roadNames) {
			if (name.equals(query)) { // this is the right answer
				ans.clear();
				ans.add(name);
				return ans;
			}
			if (name.startsWith(query)) { // it is an option
				ans.add(name);
			}
		}
		return ans;
	}

	/**
	 * Get Road objects associated with a full name, null if no road with that
	 * name exists.
	 * 
	 * @param fullname
	 * @return
	 */
	public Set<Road> getRoadsByName(String fullname) {
		return roadsByName.get(fullname);
	}

	/**
	 * Return a list of all the segments belonging to the road with the given
	 * (full) name.
	 * 
	 * @param fullname
	 * @return list of segments making up a road
	 */
	public List<Segment> getRoadSegments(String fullname) {
		Set<Road> rds = roadsByName.get(fullname);
		if (rds == null) {
			return null;
		}
		System.out.println("Found " + rds.size() + " road objects: " + rds.iterator().next());
		List<Segment> ans = new ArrayList<Segment>();
		for (Road road : rds) {
			ans.addAll(road.getSegments());
		}
		return ans;
	}

	public Map<Integer, Node> getNodes() {
		return nodes;
	}

	public Set<Restriction> getRestrictionSet() {
		return restrictionSet;
	}

}
