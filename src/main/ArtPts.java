package main;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import utils.Node;
import utils.Segment;

/**
 * ArtPts: A class that finds all of the articulation points in a graph
 * structure. Real world applications could use this to determine optimal
 * emergency services routes or network architecture.
 * 
 * @author harryross
 * @version 1.1
 */
public class ArtPts {

	private Set<Node> artPoints;
	private int numSubTrees;
	private List<Node> nodeSetArray;
	private List<Node> listArtPoints;

	public ArtPts(Map<Integer, Node> nodes) {
		nodeSetArray = new ArrayList<Node>();
		for (Node n : nodes.values()) {
			nodeSetArray.add(n);
		}
		findArtPts(nodes);
	}

	public void findArtPts(Map<Integer, Node> nodes) {
		for (Node n : nodes.values()) {
			n.setDepth(Integer.MAX_VALUE);
		}
		artPoints = new HashSet<Node>();
		while (!nodeSetArray.isEmpty()) {
			Node start = nodeSetArray.get(0);
			nodeSetArray.remove(start);
			start.setDepth(0);
			numSubTrees = 0;
			for (Segment s : start.getOutNeighbours()) {
				Node n = s.getEndNode();
				if (n.getDepth() == Integer.MAX_VALUE) {
					recArtPts(n, 1, start);
					numSubTrees++;
				}
			}
			if (numSubTrees > 1) {
				artPoints.add(start);
			}
		}
		listArtPoints = getArtPoints();
	}

	public int recArtPts(Node n, int depth, Node from) {
		n.setDepth(depth);
		nodeSetArray.remove(n);
		nodeSetArray.remove(from);
		int reachBack = depth;
		for (Segment s : n.getOutNeighbours()) {
			Node neigh = s.getEndNode();
			if (neigh != from) {
				if (neigh.getDepth() < Integer.MAX_VALUE)
					reachBack = Math.min(neigh.getDepth(), reachBack);
				else {
					int childReach = recArtPts(neigh, depth + 1, n);
					reachBack = Math.min(childReach, reachBack);
					if (childReach >= depth)
						artPoints.add(n);
				}
			}
		}
		return reachBack;
	}

	public List<Node> getList() {
		return listArtPoints;
	}

	public List<Node> getArtPoints() {
		List<Node> l = new ArrayList<Node>();
		l.addAll(artPoints);
		return l;
	}
}
