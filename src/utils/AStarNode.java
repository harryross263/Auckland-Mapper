package utils;
import java.util.ArrayList;
import java.util.List;

public class AStarNode {

	private Node main;
	private Node prev;
	private AStarNode prevAStar;
	private double lengthToHere;
	private double estimate;
	private List<AStarNode> path;

	public AStarNode(Node main, Node prev, AStarNode prevAStar,
			double lengthToHere, double estimate) {
		this.main = main;
		this.prev = prev;
		this.prevAStar = prevAStar;
		if (prevAStar != null)
			path = new ArrayList<AStarNode>(prevAStar.getPath());
		else
			path = new ArrayList<AStarNode>();
		if (prevAStar != null)
			path.add(prevAStar);
		this.lengthToHere = lengthToHere;
		this.estimate = estimate;

	}

	public List<AStarNode> getPath() {

		return path;
	}

	public Node getMain() {

		return main;
	}

	public void setPrevAStar(AStarNode a) {

		prevAStar = a;
	}

	public double getCost() {

		return lengthToHere;
	}

	public double getEstimate() {

		return estimate;
	}

	public Node getPrev() {

		return prev;
	}

	public List<Node> buildPath() {
		List<Node> p = new ArrayList<Node>();
		AStarNode temp = this;
		while (temp != null) {
			p.add(0, temp.getMain());
			temp = temp.prevAStar;
		}
		return p;
	}

}
