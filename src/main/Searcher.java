package main;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;

import utils.AStarNode;
import utils.Node;
import utils.Restriction;
import utils.Segment;

public class Searcher {

	private Queue<AStarNode> fringe;
	private Node start;
	private Node goal;
	private AStarComparator comp;
	private List<Segment> finalPath;
	private List<Node> nodePath;
	private String transportMode;
	private Set<Restriction> restrictions;

	public Searcher(Node start, Node goal, Map<Integer, Node> nodes, String sMode, String transportMode,
			Set<Restriction> restrictions) {
		this.start = start;
		this.goal = goal;
		comp = new AStarComparator();
		fringe = new PriorityQueue<AStarNode>(10, comp);
		finalPath = new ArrayList<Segment>();
		this.transportMode = transportMode;
		this.restrictions = restrictions;
		if (sMode.equals("distance"))
			distanceSearch(nodes);
		else if (sMode.equals("time"))
			timeSearch(nodes);
		else {
			System.out.println("Unrecognised transport mode");
		}
	}

	public void distanceSearch(Map<Integer, Node> nodes) {
		for (Node n : nodes.values()) {
			n.setVisited(false);
			n.setFrom(null);
		}

		fringe.add(new AStarNode(start, null, null, 0, estimate(start, goal)));

		while (fringe.peek() != null) {
			AStarNode temp = fringe.poll();

			Node currNode = temp.getMain();
			Node prevNode = temp.getPrev();
			Double costToHere = temp.getCost();

			if (!currNode.getVisited()) {

				currNode.setVisited(true);
				currNode.setFrom(prevNode);
				currNode.setCost(costToHere);

				if (currNode.equals(goal)) {
					nodePath = temp.buildPath();
					return;
				}

				for (Segment s : currNode.getOutNeighbours()) {
					if (transportMode.equals("car")) {
						if (s.getRoad().isNotForCars())
							continue;
						else if (checkRestrictions(prevNode, currNode, s.getEndNode())) {
							continue;
						}
					} else if (transportMode.equals("bike")) {
						if (s.getRoad().isNotForBicycles())
							continue;
					} else if (transportMode.equals("walking")) {
						if (s.getRoad().isNotForPedestrians())
							continue;
					}
					Node next = s.getEndNode();

					if (!next.getVisited()) {

						double costToNeigh = costToHere + s.getWeight();
						double estTotal = costToNeigh + estimate(next, goal);

						fringe.add(new AStarNode(next, currNode, temp, costToNeigh, estTotal));
					}
				}
			}
		}
	}

	public void timeSearch(Map<Integer, Node> nodes) {
		for (Node n : nodes.values()) {

			n.setVisited(false);
			n.setFrom(null);
		}

		fringe.add(new AStarNode(start, null, null, 0, timeEstimate(start, goal)));

		while (fringe.peek() != null) {
			AStarNode temp = fringe.poll();

			Node node = temp.getMain();
			Node from = temp.getPrev();
			Double costToHere = temp.getCost();

			if (!node.getVisited()) {

				node.setVisited(true);
				node.setFrom(from);
				node.setCost(costToHere);

				if (node.equals(goal)) {
					nodePath = temp.buildPath();
					return;
				}

				for (Segment s : node.getOutNeighbours()) {
					if (transportMode.equals("car")) {
						if (s.getRoad().isNotForCars())
							continue;
					} else if (transportMode.equals("bike")) {
						if (s.getRoad().isNotForBicycles())
							continue;
					} else if (transportMode.equals("walking")) {
						if (s.getRoad().isNotForPedestrians())
							continue;
					}
					Node next = s.getEndNode();

					if (!checkRestrictions(from, node, next) && !next.getVisited()) {
						double costToNeigh = costToHere + (estimate(next, goal) / s.getRoad().getSpeedLimit());
						double estTotal = costToNeigh + timeEstimate(next, goal);

						fringe.add(new AStarNode(next, node, temp, costToNeigh, estTotal));
					}
				}
			}
		}
	}

	public double timeEstimate(Node start, Node goal) {
		double straightLine = start.getLoc().distanceTo(goal.getLoc());
		return 110 / straightLine;
	}

	public double estimate(Node start, Node goal) {
		return start.getLoc().distanceTo(goal.getLoc());
	}

	public List<Segment> getFinalPath() {
		return finalPath;
	}

	public List<Node> getNodePath() {
		return nodePath;
	}

	public boolean checkRestrictions(Node n1, Node n, Node n2) {
		for (Restriction r : restrictions) {
			if (r.getNode1() == n1.getID() && r.getNode() == n.getID() && r.getNode2() == n2.getID()) {
				return true;
			}
		}
		return false;
	}

	private static class AStarComparator implements Comparator<AStarNode> {

		public int compare(AStarNode a, AStarNode b) {

			return (int) Math.signum(a.getEstimate() - b.getEstimate());
		}
	}
}
