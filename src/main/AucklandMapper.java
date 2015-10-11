package main;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import utils.Location;
import utils.Node;
import utils.Segment;

/**
 * AucklandMapper: A program that uses data from the NZ Open GPS Project to
 * represent the transport infrastructure of Auckland and its surrounding areas.
 * 
 * @author harryross
 * @version 1.1
 */
public class AucklandMapper {

	private JFrame frame;
	private JComponent drawing;
	private JTextArea textOutput;
	private JTextField nameEntry;
	private int windowSize = 700;

	private RoadGraph roadGraph;

	private Node selectedNode;
	private Node endNode; // AStar goal
	private List<Segment> selectedSegments; // the currently selected road or
	// path
	private List<Node> artPoints = new ArrayList<Node>();
	private double finalPathLength;
	private double finalTime;
	private boolean loaded = false;
	private String aStarMode = "distance";
	private boolean shown;
	private String transportMode = "car";

	// Dimensions for drawing
	double westBoundary;
	double eastBoundary;
	double southBoundary;
	double northBoundary;
	Location origin;
	double scale;

	public AucklandMapper(String dataDir) {
		setupInterface();
		endNode = null;
		selectedNode = null;
		roadGraph = new RoadGraph();

		setText("Loading data...");
		appendText(roadGraph.loadData("../graph-data/"));
		setupScaling();
		loaded = true;
		drawing.repaint();
	}

	private void setupScaling() {
		double[] b = roadGraph.getBoundaries();
		westBoundary = b[0];
		eastBoundary = b[1];
		southBoundary = b[2];
		northBoundary = b[3];
		resetOrigin();
	}

	@SuppressWarnings("serial")
	private void setupInterface() {
		frame = new JFrame("Auckland Mapper");
		frame.setSize(windowSize, windowSize);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		drawing = new JComponent() {
			protected void paintComponent(Graphics g) {
				redraw(g);
			}
		};
		frame.add(drawing, BorderLayout.CENTER);

		// Setup a text area for output
		textOutput = new JTextArea(5, 100);
		textOutput.setEditable(false);
		JScrollPane textSP = new JScrollPane(textOutput);
		frame.add(textSP, BorderLayout.SOUTH);

		// Set up a panel for some buttons.
		// To get nicer layout, we would need a LayoutManager on the panel.
		JPanel panel = new JPanel();
		frame.add(panel, BorderLayout.NORTH);

		// Add buttons to the panel.
		JButton button = new JButton("+");
		panel.add(button);
		button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ev) {
				zoomIn();
			}
		});

		button = new JButton("-");
		panel.add(button);
		button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ev) {
				zoomOut();
			}
		});

		button = new JButton("<");
		panel.add(button);
		button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ev) {
				pan("left");
			}
		});

		button = new JButton(">");
		panel.add(button);
		button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ev) {
				pan("right");
			}
		});

		button = new JButton("^");
		panel.add(button);
		button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ev) {
				pan("up");
			}
		});

		button = new JButton("v");
		panel.add(button);
		button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ev) {
				pan("down");
			}
		});

		button = new JButton("Show/Hide ArtPts");
		panel.add(button);
		button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ev) {
				if (shown) {
					artPoints.clear();
					shown = false;
				} else {
					ArtPts a = new ArtPts(roadGraph.getNodes());
					artPoints = a.getList();
					shown = true;
				}
				drawing.repaint();

			}
		});

		button = new JButton("Toggle Search Mode");
		panel.add(button);
		button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ev) {
				if (aStarMode.equals("distance"))
					aStarMode = "time";
				else if (aStarMode.equals("time"))
					aStarMode = "distance";

				setText("");
			}
		});

		button = new JButton("Toggle Transport mode");
		panel.add(button);
		button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ev) {
				if (transportMode.equals("car")) {
					transportMode = "bike";
				} else if (transportMode.equals("bike")) {
					transportMode = "walking";
				} else if (transportMode.equals("walking")) {
					transportMode = "car";
				}
				setText("");
			}
		});

		button = new JButton("Reset A*");
		panel.add(button);
		button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ev) {
				selectedSegments = null;
				selectedNode = null;
				endNode = null;

				drawing.repaint();
				setText("");
			}
		});

		nameEntry = new JTextField(10);
		panel.add(nameEntry);
		nameEntry.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				lookupName(nameEntry.getText());
				drawing.repaint();
			}
		});

		// Add a mouselistener to the drawing JComponent to respond to mouse
		// clicks.
		drawing.addMouseListener(new MouseAdapter() {
			public void mouseReleased(MouseEvent e) {
				if (selectedNode == null || endNode != null) {
					setSelectedNode(e);
				} else {
					setText("Selected road = " + selectedNode.toString());
					setEndNode(e);
				}
				drawing.repaint();
			}
		});

		// Now that it is all set up, make the interface visible
		frame.setVisible(true);

	}

	private double zoomFactor = 1.25;
	private double panFraction = 0.2;

	/**
	 * Sets origin and scale for the whole map.
	 */
	private void resetOrigin() {
		origin = new Location(westBoundary, northBoundary);
		scale = Math.min(windowSize / (eastBoundary - westBoundary), windowSize / (northBoundary - southBoundary));
	}

	/**
	 * Shrinks the scale (pixels/per km) by zoomFactor and move origin.
	 */
	private void zoomOut() {
		scale = scale / zoomFactor;
		double deltaOrig = windowSize / scale * (zoomFactor - 1) / zoomFactor / 2;
		origin = new Location(origin.x - deltaOrig, origin.y + deltaOrig);
		drawing.repaint();
	}

	/**
	 * Expands the scale (pixels/per km) by zoomFactor and move origin.
	 */
	private void zoomIn() {
		double deltaOrig = windowSize / scale * (zoomFactor - 1) / zoomFactor / 2;
		origin = new Location(origin.x + deltaOrig, origin.y - deltaOrig);
		scale = scale * zoomFactor;
		drawing.repaint();
	}

	private void pan(String dir) {

		double delta = windowSize * panFraction / scale;
		switch (dir) {
		case "left": {
			origin = new Location(origin.x - delta, origin.y);
			break;
		}
		case "right": {
			origin = new Location(origin.x + delta, origin.y);
			break;
		}
		case "up": {
			origin = new Location(origin.x, origin.y + delta);
			break;
		}
		case "down": {
			origin = new Location(origin.x, origin.y - delta);
			break;
		}
		}
		drawing.repaint();

	}

	/**
	 * Finds the node that the mouse was clicked on
	 * 
	 * @param mouse
	 * @return Node
	 */
	private Node findNode(Point mouse) {
		return roadGraph.findNode(mouse, origin, scale);
	}

	private void lookupName(String query) {
		List<String> names = new ArrayList<String>(roadGraph.lookupName(query));
		if (names.isEmpty()) {
			selectedSegments = null;
			setText("Not found");
		} else if (names.size() == 1) {
			String fullName = names.get(0);
			nameEntry.setText(fullName);
			setText("Found");
			selectedSegments = roadGraph.getRoadSegments(fullName);
		} else {
			selectedSegments = null;
			String prefix = maxCommonPrefix(query, names);
			nameEntry.setText(prefix);
			setText("Options: ");
			for (int i = 0; i < 10 && i < names.size(); i++) {
				appendText(names.get(i));
				appendText(", ");
			}
			if (names.size() > 10) {
				appendText("...\n");
			} else {
				appendText("\n");
			}
		}
	}

	private String maxCommonPrefix(String query, List<String> names) {
		String ans = query;
		for (int i = query.length();; i++) {
			if (names.get(0).length() < i)
				return ans;
			String cand = names.get(0).substring(0, i);
			for (String name : names) {
				if (name.length() < i)
					return ans;
				if (name.charAt(i - 1) != cand.charAt(i - 1))
					return ans;
			}
			ans = cand;
		}
	}

	/**
	 * The redraw method that will be called from the drawing JComponent and
	 * will draw the map at the current scale and shift.
	 * 
	 * @param graphics
	 */
	public void redraw(Graphics g) {
		if (roadGraph != null && loaded) {
			roadGraph.redraw(g, origin, scale);
			if (selectedNode != null) {
				g.setColor(Color.red);
				selectedNode.draw(g, origin, scale);
			}
			if (endNode != null) {
				g.setColor(Color.red);
				endNode.draw(g, origin, scale);
			}
			if (selectedSegments != null) {
				g.setColor(Color.red);
				for (Segment seg : selectedSegments) {
					seg.draw(g, origin, scale);
				}
			}
			if (!artPoints.isEmpty()) {
				g.setColor(Color.green);
				for (Node n : artPoints) {
					n.draw(g, origin, scale);
				}
			}
		}
	}

	public void setSelectedNode(MouseEvent e) {
		selectedSegments = null;
		endNode = null;
		selectedNode = findNode(e.getPoint());
		setText(selectedNode.toString());
	}

	public void setEndNode(MouseEvent e) {
		endNode = findNode(e.getPoint());
		if (endNode == selectedNode) {
			appendText("\nStart and Goal node are equal");
			return;
		}
		appendText("\nGoal Node: " + endNode.toString());
		Searcher temp = new Searcher(selectedNode, endNode, roadGraph.getNodes(), aStarMode, transportMode,
				roadGraph.getRestrictionSet());
		selectedSegments = buildSegList(temp.getNodePath());
		appendRoute();
		drawing.repaint();
	}

	public List<Segment> buildSegList(List<Node> n) {
		if (n == null) {
			return null;
		}
		List<Segment> s = new ArrayList<Segment>();
		finalPathLength = 0;
		finalTime = 0;
		for (int i = 1; i < n.size(); i++) {
			Node a = n.get(i - 1);
			Node b = n.get(i);
			Segment seg = findSeg(a, b);
			if (seg != null) {
				s.add(seg);
				finalTime = finalTime + (seg.getWeight() / seg.getRoad().getSpeedLimit());
				finalPathLength = finalPathLength + seg.getWeight();
			}
		}
		return s;
	}

	public void appendRoute() {
		if (selectedSegments == null) {
			setText("Route not available");
			return;
		}
		setText(selectedNode.getRoadNames() + "\n");
		StringBuilder b = new StringBuilder();
		List<String> roadNames = new ArrayList<String>();
		List<Double> roadWeights = new ArrayList<Double>();
		for (Segment s : selectedSegments) {
			roadNames.add(s.getRoad().getName());
			roadWeights.add(s.getWeight());
		}
		double temp = 0;
		String prevName = "";
		String currentName = "";
		for (int i = 1; i < roadNames.size(); i++) {
			temp = temp + roadWeights.get(i);
			prevName = roadNames.get(i - 1);
			currentName = roadNames.get(i);
			while (prevName.equals(currentName)) {
				if (i < roadNames.size()) {
					temp = temp + roadWeights.get(i);
					prevName = currentName;
					currentName = roadNames.get(i++);
				} else {
					break;
				}
			}
			b.append(prevName + ": " + temp + "km\n");
			temp = 0;
		}
		appendText("\n" + b.toString());
		appendText("\n" + endNode.getEndRoadNames() + "\n");
		if (aStarMode.equals("distance"))
			appendText("\nTotal Distance = " + finalPathLength + "km");
		else if (aStarMode.equals("time"))
			appendText("\nTotal Time = " + finalTime + " hours");
	}

	public Segment findSeg(Node a, Node b) {

		for (Segment s : a.getOutNeighbours()) {
			if (s.getEndNode() == b) {
				return s;
			}
		}
		return null;
	}

	public void setText(String s) {
		textOutput.setText("Search mode: " + aStarMode + "\n");
		textOutput.append("Transport mode: " + transportMode + "\n");
		textOutput.append(s);
	}

	public void appendText(String s) {
		textOutput.append(s);
	}

	public String getTransportMode() {
		return transportMode;
	}

	public static void main(String[] arguments) {
		if (arguments.length > 0) {
			new AucklandMapper(arguments[0]);
		} else {
			new AucklandMapper(null);
		}
	}

}
