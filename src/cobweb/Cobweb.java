/*
	Cobweb
	Copyright (C) 2010 Joachim von Eichborn
 
    This file is part of Cobweb.

    Cobweb is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    Cobweb is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with Cobweb.  If not, see <http://www.gnu.org/licenses/>.
 */

package cobweb;

import guicomponents.G4P;
import guicomponents.GButton;
import guicomponents.GCheckbox;
import guicomponents.GLabel;
import guicomponents.GPanel;
import guicomponents.GSlider;
import guicomponents.GTextField;
import guicomponents.GWSlider;

import java.awt.Cursor;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.Random;

import javax.imageio.ImageIO;

import netscape.javascript.JSObject;

import org.apache.commons.codec.binary.Base64;

import particlesystem.Edge;
import particlesystem.Node;
import particlesystem.ParticleSystem;
import particlesystem.Vector2D;
import processing.core.PApplet;
import processing.core.PFont;

/**
 * Main class of the applet. It's setup function gets called when the applet is
 * loaded. Handles the drawing of the network, the user interface and the
 * interaction with the user.
 * 
 * @author Joachim von Eichborn
 * @author http://bioinformatics.charite.de/cobweb
 */
@SuppressWarnings("serial")
public class Cobweb extends PApplet {
	/**
	 * Window object that is used to execute Javascript functions on a webpage
	 * if available
	 */
	JSObject window = null;

	/**
	 * Defines the strength of the edge between two nodes that are connected
	 */
	static float edgeStrength;

	/**
	 * Defines the strength of the repulsion between any two nodes
	 */
	static float repulsionStrength;

	/**
	 * Defines the size of each node
	 */
	static final float NODE_SIZE = 36;

	/**
	 * Defines the size of the arrow heads
	 */
	static final float ARROW_HEAD = 9;

	/**
	 * Defines the radius in which a node is regarded to be clicked on
	 */
	static final int DISTANCE_CUTOFF = (int) (NODE_SIZE / 2 + 5);

	/**
	 * Sets the transparency for all semi-transparent parts
	 */
	static final int ALPHA = 180;

	// the control elements' labels
	final String SMOOTH_BUTTON_FASTER_TEXT = "Faster  ";
	final String SMOOTH_BUTTON_NICER_TEXT = "Smoother  ";

	/**
	 * The particle systems that contains all nodes and edges and does the
	 * layout of the network
	 */
	ParticleSystem particleSys = null;

	/**
	 * The currently selected node
	 */
	private Node selectedNode = null;

	/**
	 * Wether the selected node was fixed at the time he was clicked on
	 */
	private boolean selectedNodeFixed = false;

	/**
	 * Wether a selection frame is drawn
	 */
	boolean selectionFrame = false;

	/**
	 * Whether the nodes are fixed or not
	 */
	boolean fixed = false;

	/**
	 * Whether verbose information like FPS, number of nodes should be drawn
	 */
	boolean verbose = false;

	/**
	 * The format in which the network was given (xgmml or graphml). When
	 * neighbourhood nodes are loaded, they are expected to be in the same
	 * format
	 */
	private String networkType = null;

	/**
	 * Contains the parameters given in the XGMML-file
	 */
	Parameters params = null;

	// variables to calculate the correct viewport after zooming and translating
	// the view
	private float translateMouseX = 0f;
	private float translateMouseY = 0f;
	private float translateZoomX = 0f;
	private float translateZoomY = 0f;
	private float origX = 0f;
	private float origY = 0f;

	/**
	 * Zoom factor for the network view
	 */
	public float scaleFactor;

	/**
	 * Length of the edges in the network
	 */
	public float edgeLength;

	/**
	 * The color of the applets background
	 */
	private int backgroundColor = (255 << 24) | Integer.parseInt("FFFFFF", 16);

	/**
	 * The color that is used to draw the node names
	 */
	private int textColor = (255 << 24) | Integer.parseInt("000000", 16);

	// control elements to change the display style
	GPanel displayPanel = null;

	GCheckbox displayNamesBox = null;
	GCheckbox displaySymbolsBox = null;
	GCheckbox displayPicturesBox = null;
	GWSlider scaleSlider = null;
	GButton fixButton = null;
	GWSlider edgeLengthSlider = null;
	GButton smoothButton = null;
	// control elements to perform graph functions
	GPanel graphPanel = null;

	GTextField nodeSearchTextfield = null;
	GButton selectCommonNeighboursButton = null;
	GButton selectNeighboursButton = null;
	GButton statisticsButton = null;
	GButton onePathButton = null;
	GButton allPathButton = null;
	GButton invertButton = null;
	GButton addNodeButton = null;
	GButton addEdgeButton = null;
	GButton shuffleButton = null;
	GButton relaxButton = null;
	GButton fitButton = null;
	/**
	 * The font used to draw the node names
	 */
	PFont font = null;

	/**
	 * If two nodes are selected, connect them with an edge to each other
	 */
	public void addEdgeByButton () {
		if (particleSys.numberOfSelectedNodes() == 1)
			particleSys.makeEdge(particleSys.getSelectedNode(0), particleSys.getSelectedNode(0), 1.0f, null, null, null, edgeStrength, edgeStrength, edgeLength);
		else if (particleSys.numberOfSelectedNodes() == 2)
			particleSys.makeEdge(particleSys.getSelectedNode(0), particleSys.getSelectedNode(1), 1.0f, null, null, null, edgeStrength, edgeStrength, edgeLength);
		else {
			String[] args = new String[] { "Please select exactely two nodes (by selecting the source node and then keeping the CONTROL-key pressed while clicking on the target node)." };
			callJavascriptFunction("show_warning", args);
		}
	}

	/**
	 * Add the specified network to the displayed network
	 * 
	 * @param network
	 *            The network that is added
	 */
	public void addNetwork (String network) {
		addNetwork(network, width / 2 - translateMouseX, height / 2 - translateMouseY);
	}
	
	/**
	 * Add the specified network to the displayed network
	 * 
	 * @param network
	 *            The specified network
	 * @param x
	 *            The x coordinate where new nodes are added
	 * @param y
	 *            The y coordinate where new nodes are added
	 */
	public void addNetwork (String network, float x, float y) {
		callJavascriptFunctionStatusMessage("adding network", true);

		if (networkType != null) {
			if (networkType.equals("xgmml")) {
				XGMMLParser parser = new XGMMLParser(this, network);
				parser.setParameters(params);

				if (fixed)
					parser.parseNodes(particleSys, x, y, true);
				else
					parser.parseNodes(particleSys, x, y);

				parser.parseEdges(particleSys, edgeLength);
			} else if (networkType.equals("graphml")) {
				GraphMLParser parser = new GraphMLParser(this, network);
				parser.setParameters(params);

				if (fixed)
					parser.parseNodes(particleSys, x, y, true);
				else
					parser.parseNodes(particleSys, x, y);

				parser.parseEdges(particleSys, edgeLength);
			} else
				System.out.println("unknown network type: " + networkType);
		} else
			System.out.println("no network type defined");

		System.out.println(network);

		callJavascriptFunctionStatusMessage("added network");
	}

	/**
	 * Add a new node with default values to the particle system
	 */
	public void addNodeByButton () {
		String id = "new1";

		for (int i = 1; i < MAX_INT; ++i)
			if (!particleSys.containsNode("new" + i)) {
				id = "new" + i;
				break;
			}

		String name = id;

		Node n = particleSys.makeNode(id, name, null, null, params.getServerAdress() + params.getPicturePath(), null, null);

		if (n != null) {
			for (int i = 0; i < particleSys.numberOfNodes(); ++i) {
				Node q = particleSys.getNode(i);
				if (n != q)
					particleSys.makeRepulsion(n, q, repulsionStrength, 20);
			}

			Random random = new Random();
			n.getPosition()
					.set(width / 2 - translateMouseX + (random.nextFloat() * NODE_SIZE * 6 - NODE_SIZE * 3), height / 2 - translateMouseY + (random.nextFloat() * NODE_SIZE * 6 - NODE_SIZE * 3));
			n.fix();

			particleSys.deselectAllNodes();
			selectNode(n);
		}
	}

	/**
	 * Select a node by its Id without deselecting previously selected nodes
	 * 
	 * @param id
	 *            The id of the node that is to be selected
	 */
	public void addNodeToSelectionById (String id) {
		for (int i = 0; i < particleSys.numberOfNodes(); ++i)
			if (particleSys.getNode(i).getId().equals(id)) {
				selectNode(particleSys.getNode(i));
				break;
			}
	}

	/**
	 * Call the given javascript-function
	 * 
	 * @param function
	 *            Name of the function to be executed
	 */
	public void callJavascriptFunction (String function) {
		callJavascriptFunction(function, null);
	}

	/**
	 * Call the given javascript-function
	 * 
	 * @param function
	 *            Name of the function to be executed
	 * @param args
	 *            The parameters that are given to the javascript function
	 */
	public void callJavascriptFunction (String function, String[] args) {
		if (window != null) {
			try {
				window.call(function, args);
			} catch (Exception ex) {
				println(function + " exception " + args);
			}
		} else
			println(function + " " + args);
	}

	/**
	 * Call the Javascript-function to clear the sidebar
	 */
	public void callJavascriptFunctionClearSidebar () {
		String[] args = new String[] { params.getLabel(), getBackgroundColorAsHexString(), getTextColorAsHexString(), String.valueOf(edgeStrength), String.valueOf(repulsionStrength) };
		callJavascriptFunction("clear_sidebar", args);
	}

	/**
	 * Call a java-script function to display the given message
	 * 
	 * @param message
	 *            the message is to be displayed
	 */
	public void callJavascriptFunctionStatusMessage (String message) {
		callJavascriptFunctionStatusMessage(message, false);
	}

	/**
	 * Call a java-script function to display the given message
	 * 
	 * @param message
	 *            the message that is to be displayed
	 * @param spinner
	 *            whether to show a loading-spinner or not
	 */
	public void callJavascriptFunctionStatusMessage (String message, Boolean spinner) {
		String[] args = null;
		if (spinner)
			args = new String[] { message, "true" };
		else
			args = new String[] { message, "false" };

		callJavascriptFunction("set_status_message", args);
	}

	/**
	 * Create the control elements (buttons and sliders) in the GUI
	 */
	void createControlElements () {
		// Set the color scheme for the control elements. You can use one of the
		// predefined schemes (values 0 to 6). Alternatively a file named
		// user_col_schema.png with own schemes can be provided in the jar.
		// Please look at http://www.lagers.org.uk/g4p/colorscheme.html for
		// details on how this file has to look like.
		G4P.setColorScheme(this, 7);

		displayPanel = new GPanel(this, "  Display Panel   ", 0, 0, 662, 80);
		displayPanel.setCollapsed(false);
		displayPanel.setOpaque(true);
		displayPanel.setBorder(1);

		GLabel display = new GLabel(this, "Display style:", 0, 5, 85);
		displayPanel.add(display);
		displayNamesBox = new GCheckbox(this, "Names", 5, 24, 70);
		displayNamesBox.setSelected(true);
		displayPanel.add(displayNamesBox);
		displaySymbolsBox = new GCheckbox(this, "Symbols", 5, 44, 70);
		displayPanel.add(displaySymbolsBox);
		displayPicturesBox = new GCheckbox(this, "Pictures", 5, 64, 70);
		displayPicturesBox.setSelected(true);
		displayPanel.add(displayPicturesBox);

		GLabel zoom = new GLabel(this, "Zoom:", 95, 5, 45);
		displayPanel.add(zoom);
		scaleSlider = new GWSlider(this, 95, 34, 150);
		scaleSlider.setLimits(scaleFactor, 0.1f, 2.0f);
		scaleSlider.setValueType(GSlider.DECIMAL);
		displayPanel.add(scaleSlider);

		GLabel nodes = new GLabel(this, "Nodes:", 267, 5, 45);
		displayPanel.add(nodes);
		fixButton = new GButton(this, "Fix/Release Nodes  ", 267, 27, 95, 20);
		displayPanel.add(fixButton);

		GLabel length = new GLabel(this, "Edge length:", 400, 5, 80);
		displayPanel.add(length);
		edgeLengthSlider = new GWSlider(this, 400, 34, 150);
		edgeLengthSlider.setLimits(edgeLength, 1, 600);
		displayPanel.add(edgeLengthSlider);

		GLabel performance = new GLabel(this, "Performance:", 570, 5, 82);
		displayPanel.add(performance);
		smoothButton = new GButton(this, SMOOTH_BUTTON_FASTER_TEXT, 570, 27, 85, 20);
		displayPanel.add(smoothButton);

		graphPanel = new GPanel(this, "  Graph Panel   ", 0, 115, 662, 102);
		graphPanel.setCollapsed(false);
		graphPanel.setOpaque(true);
		graphPanel.setBorder(1);

		GLabel search = new GLabel(this, "Search Node:", 5, 5, 82);
		graphPanel.add(search);
		nodeSearchTextfield = new GTextField(this, "node name", 5, 25, 82, 20);
		graphPanel.add(nodeSearchTextfield);

		GLabel select = new GLabel(this, "Selection:", 94, 5, 120);
		graphPanel.add(select);
		selectCommonNeighboursButton = new GButton(this, "Common Neighbours  ", 94, 25, 136, 20);
		graphPanel.add(selectCommonNeighboursButton);
		selectNeighboursButton = new GButton(this, "All Neighbours  ", 94, 50, 136, 20);
		graphPanel.add(selectNeighboursButton);
		invertButton = new GButton(this, "Invert Selection  ", 94, 75, 136, 20);
		graphPanel.add(invertButton);

		GLabel path = new GLabel(this, "Shortest Path:", 236, 5, 90);
		graphPanel.add(path);
		onePathButton = new GButton(this, "Show one Path  ", 236, 25, 103, 20);
		graphPanel.add(onePathButton);
		allPathButton = new GButton(this, "Show all Paths  ", 236, 50, 103, 20);
		graphPanel.add(allPathButton);

		GLabel manipulate = new GLabel(this, "Manipulate:", 346, 5, 95);
		graphPanel.add(manipulate);
		addNodeButton = new GButton(this, "Add Node  ", 346, 25, 95, 20);
		graphPanel.add(addNodeButton);
		addEdgeButton = new GButton(this, "Add Edge  ", 346, 50, 95, 20);
		graphPanel.add(addEdgeButton);

		GLabel statistics = new GLabel(this, "Statistics:", 447, 5, 100);
		graphPanel.add(statistics);
		statisticsButton = new GButton(this, "Show Statistics  ", 447, 25, 104, 20);
		graphPanel.add(statisticsButton);

		GLabel shuffle = new GLabel(this, "Node Positions:", 559, 5, 95);
		graphPanel.add(shuffle);
		shuffleButton = new GButton(this, "Shuffle  ", 559, 25, 95, 20);
		graphPanel.add(shuffleButton);
		relaxButton = new GButton(this, "Relax  ", 559, 50, 95, 20);
		graphPanel.add(relaxButton);
		fitButton = new GButton(this, "Fit in Window  ", 559, 75, 95, 20);
		graphPanel.add(fitButton);

		displayPanel.setCollapsed(true);
		graphPanel.setCollapsed(true);

	}

	/**
	 * Decrease the length of the edge between the given nodes by the given
	 * value
	 * 
	 * @param sourceId
	 *            The source node of the edge that is shortened
	 * @param targetId
	 *            The target node of the edge that is shortened
	 * @param amount
	 *            How much the edge is shortened
	 */
	public void decreaseEdgeLength (String sourceId, String targetId, Float amount) {
		for (int i = 0; i < particleSys.numberOfEdges(); ++i)
			if (particleSys.getEdge(i).getSource().getId().equals(sourceId) && particleSys.getEdge(i).getTarget().getId().equals(targetId))
				particleSys.getEdge(i).decreaseEdgeLength(amount);
	}

	/**
	 * Remove the selected Edge
	 */
	public void deleteEdge () {
		for (int i = 0; i < particleSys.numberOfEdges(); ++i)
			if (particleSys.getEdge(i).isHighlighted()) {
				particleSys.removeEdge(i);
				break;
			}
	}

	/**
	 * Delete a given node
	 * 
	 * @param n
	 *            Node to be deleted
	 */
	public void deleteNode (Node n) {
		callJavascriptFunctionStatusMessage("removing " + n.getName(), true);

		String msg = "removed " + n.getName();

		particleSys.removeNode(n);

		callJavascriptFunctionStatusMessage(msg);
		callJavascriptFunctionClearSidebar();
	}

	/**
	 * Delete a given node
	 * 
	 * @param id
	 *            Id of the node to be deleted
	 */
	public void deleteNode (String id) {
		Node n = particleSys.getNodeById(id);

		if (n != null)
			deleteNode(n);
	}

	/**
	 * Delete all selected nodes
	 */
	public void deleteSelectedNodes () {
		ArrayList<Node> removeNode = new ArrayList<Node>();

		for (int i = 0; i < particleSys.numberOfSelectedNodes(); ++i)
			removeNode.add(particleSys.getSelectedNode(i));

		for (Iterator<Node> partIt = removeNode.iterator(); partIt.hasNext();)
			deleteNode(partIt.next());
	}

	/**
	 * Calculate the euclidian distance between two points
	 * 
	 * @param x1
	 *            x-coordinate of the first point
	 * @param y1
	 *            y-coordinate of the second point
	 * @param x2
	 *            x-coordinate of the first point
	 * @param y2
	 *            y-coordinate of the second point
	 * @return The distance between the two points
	 */
	float distance (float x1, float y1, float x2, float y2) {
		float dx = x2 - x1;
		float dy = y2 - y1;
		return sqrt((dx * dx) + (dy * dy));
	}

	/**
	 * Initiates that the particle system gets updated and draws the network
	 */
	public void draw () {
		textFont(font);
		particleSys.tick();

		background(backgroundColor);

		if (verbose)
			text(particleSys.numberOfNodes() + " nodes\n" + particleSys.numberOfEdges() + " edges\n" + (int) frameRate + " FPS", 40, 220);

		scale(scaleFactor);
		translate(translateMouseX + translateZoomX, translateMouseY + translateZoomY);

		drawEdges();
		drawNodes();

		// draw selection frame
		if (selectionFrame) {
			stroke(219, 139, 13);
			fill(219, 139, 13, 80);
			float width = Math.abs(getMouseX() - origX);
			float height = Math.abs(getMouseY() - origY);
			rect(origX + ((getMouseX() - origX) / 2), origY + ((getMouseY() - origY) / 2), width, height);
		}

		resetMatrix();
	}

	/**
	 * Draw the caps of straight edges
	 * 
	 * @param ax
	 *            the x position of the source node
	 * @param ay
	 *            the y position of the source node
	 * @param bx
	 *            the x position of the target node
	 * @param by
	 *            the x position of the target node
	 * @param shape
	 *            the shape of the edge
	 */
	void drawEdgeEnding (float ax, float ay, float bx, float by, int shape) {
		float vx, vy, dist, xArrow, yArrow;

		float nodeCorona = DISTANCE_CUTOFF + 15;

		switch (shape) {
			case 0: // none
				break;
			case 1: // arrow
				vx = bx - ax;
				vy = by - ay;
				dist = (float) Math.sqrt(vx * vx + vy * vy);
				vx = vx / dist;
				vy = vy / dist;

				xArrow = (-vy - vx) * ARROW_HEAD;
				yArrow = (vx - vy) * ARROW_HEAD;
				line(bx - vx * nodeCorona, by - vy * nodeCorona, bx + xArrow - vx * nodeCorona, by + yArrow - vy * nodeCorona);

				xArrow = (vy - vx) * ARROW_HEAD;
				yArrow = (-vx - vy) * ARROW_HEAD;
				line(bx - vx * nodeCorona, by - vy * nodeCorona, bx + xArrow - vx * nodeCorona, by + yArrow - vy * nodeCorona);
				break;
			case 2: // dash
				vx = bx - ax;
				vy = by - ay;
				dist = (float) Math.sqrt(vx * vx + vy * vy);
				vx = vx / dist;
				vy = vy / dist;

				line(bx - vx * nodeCorona + vy * ARROW_HEAD, by - vy * nodeCorona - vx * ARROW_HEAD, bx - vy * ARROW_HEAD - vx * nodeCorona, by + vx * ARROW_HEAD - vy * nodeCorona);
				break;
			case 3: // circle
				vx = bx - ax;
				vy = by - ay;
				dist = (float) Math.sqrt(vx * vx + vy * vy);
				vx = vx / dist;
				vy = vy / dist;

				fill(backgroundColor);
				ellipse(bx - vx * nodeCorona, by - vy * nodeCorona, ARROW_HEAD, ARROW_HEAD);
				noFill();
				break;
		}
	}

	/**
	 * Draw the edges of the network
	 */
	void drawEdges () {
		Node a = null;
		Node b = null;
		Edge e = null;

		float ax, ay, bx, by;

		float halfNodeSize = NODE_SIZE / 2;

		noFill();

		for (int i = particleSys.numberOfEdges() - 1; i >= 0; i--) {
			e = particleSys.getEdge(i);
			if (e.isVisible()) {
				a = e.getSource();
				b = e.getTarget();

				ax = a.getPosition().getX();
				ay = a.getPosition().getY();
				bx = b.getPosition().getX();
				by = b.getPosition().getY();

				strokeWeight(e.getStrokeWeight());

				if (e.isHighlighted())
					stroke(219, 139, 13);
				else if (a.isHighlighted() || b.isHighlighted()) {
					if (!particleSys.isDirected())
						stroke(219, 139, 13);
					else {
						if (a.isHighlighted())
							stroke(219, 65, 13);
						else
							stroke(219, 187, 13);
					}
				} else
					stroke(e.getColor());

				if (a.equals(b)) { // self-edges
					arc(ax - halfNodeSize, ay - halfNodeSize, NODE_SIZE, NODE_SIZE, PI / 2, TWO_PI);

					drawSelfEdgeEnding(ax, ay, e.getShape());
				} else { // edges on other nodes
					line(ax, ay, bx, by);

					drawEdgeEnding(ax, ay, bx, by, e.getShape());

					if (!particleSys.isDirected())
						drawEdgeEnding(b.getPosition().getX(), b.getPosition().getY(), a.getPosition().getX(), a.getPosition().getY(), e.getShape());
				}
			}
		}

		strokeWeight(2);
	}

	/**
	 * Draw the nodes of the network
	 */
	void drawNodes () {
		int downShift = 4;
		if (displayPicturesBox.isSelected())
			downShift = 33;

		Boolean drawSymbols = displaySymbolsBox.isSelected();
		Boolean drawImages = displayPicturesBox.isSelected();
		Boolean drawNames = displayNamesBox.isSelected();

		stroke(200);

		Node v = null;
		for (int i = particleSys.numberOfNodes() - 1; i >= 0; i--) {
			v = particleSys.getNode(i);
			if (v.isVisible()) {
				if (v.isHighlighted()) {
					noStroke();
					fill(255, 255, 70, ALPHA);
					ellipse(v.getPosition().getX(), v.getPosition().getY(), NODE_SIZE * 1.5f, NODE_SIZE * 1.5f);

					stroke(219, 139, 13, ALPHA);
					textSize(18);
				}

				if (drawSymbols)
					drawSymbol(v.getPosition().getX(), v.getPosition().getY(), v.getShape(), v.getFillColor());

				if (drawImages) {
					if (v.hasImage())
						image(v.getImage(), v.getPosition().getX(), v.getPosition().getY());
					else
						drawSymbol(v.getPosition().getX(), v.getPosition().getY(), v.getShape(), v.getFillColor());
				}

				if (drawNames) {
					fill(textColor);
					text(v.getName(), v.getPosition().getX(), v.getPosition().getY() + downShift);
				}

				if (v.isHighlighted()) {
					stroke(200);
					textSize(14);
				}
			}
		}
	}

	/**
	 * Draw the caps of self-edges
	 * 
	 * @param x
	 *            the nodes x position
	 * @param y
	 *            the nodes y position
	 * @param shape
	 *            the shape of the edge
	 */
	void drawSelfEdgeEnding (float x, float y, int shape) {
		float halfNodeSize = NODE_SIZE / 2;

		switch (shape) {
			case 0: // none
				break;
			case 1: // arrow
				line(x, y - halfNodeSize, x - ARROW_HEAD - 1, y - halfNodeSize - ARROW_HEAD);
				line(x, y - halfNodeSize, x + ARROW_HEAD - 1, y - halfNodeSize - ARROW_HEAD);
				break;
			case 2: // dash
				line(x - ARROW_HEAD, y - halfNodeSize, x + ARROW_HEAD, y - halfNodeSize);
				break;
			case 3: // circle
				fill(backgroundColor);
				ellipse(x, y - halfNodeSize, ARROW_HEAD, ARROW_HEAD);
				noFill();
				break;
		}
	}

	/**
	 * Draw a circle representing a node
	 * 
	 * @param x
	 *            x-coordinate of the circle
	 * @param y
	 *            y-coordinate of the circle
	 * @param shape
	 *            the shape of the symbol
	 * @param col
	 *            Fill-color for the circle
	 */
	void drawSymbol (float x, float y, int shape, int col) {
		fill(col, ALPHA);

		switch (shape) {
			case 0: // circle
				ellipse(x, y, NODE_SIZE, NODE_SIZE);
				break;
			case 1: // triangle
				triangle(x, y - NODE_SIZE / 2, x + NODE_SIZE / 2, y + NODE_SIZE / 2, x - NODE_SIZE / 2, y + NODE_SIZE / 2);
				break;
			case 2: // box
				rect(x, y, NODE_SIZE, NODE_SIZE);
				break;
			case 3: // rectangle
				rect(x, y, NODE_SIZE, NODE_SIZE / 2);
				break;
			case 4: // rhombus
				quad(x - NODE_SIZE / 2, y - NODE_SIZE / 2, x, y - NODE_SIZE / 2, x + NODE_SIZE / 2, y + NODE_SIZE / 2, x, y + NODE_SIZE / 2);
				break;
			case 5: // hexagon
				beginShape();
				vertex(x - NODE_SIZE / 2, y);
				vertex(x - NODE_SIZE / 4, y - NODE_SIZE / 2);
				vertex(x + NODE_SIZE / 4, y - NODE_SIZE / 2);
				vertex(x + NODE_SIZE / 2, y);
				vertex(x + NODE_SIZE / 4, y + NODE_SIZE / 2);
				vertex(x - NODE_SIZE / 4, y + NODE_SIZE / 2);
				endShape(CLOSE);
				break;
			case 6: // octagon
				beginShape();
				vertex(x - NODE_SIZE / 2, y - NODE_SIZE / 4);
				vertex(x - NODE_SIZE / 4, y - NODE_SIZE / 2);
				vertex(x + NODE_SIZE / 4, y - NODE_SIZE / 2);
				vertex(x + NODE_SIZE / 2, y - NODE_SIZE / 4);
				vertex(x + NODE_SIZE / 2, y + NODE_SIZE / 4);
				vertex(x + NODE_SIZE / 4, y + NODE_SIZE / 2);
				vertex(x - NODE_SIZE / 4, y + NODE_SIZE / 2);
				vertex(x - NODE_SIZE / 2, y + NODE_SIZE / 4);
				endShape(CLOSE);
				break;
			case 7: // horizontal ellipsis
				ellipse(x, y, NODE_SIZE, NODE_SIZE / 2);
				break;
			case 8: // vertical ellipsis
				ellipse(x, y, NODE_SIZE / 2, NODE_SIZE);
				break;
		}
	}

	/**
	 * Scales and translates the view to make the network fit into the window
	 */
	public void fitNetworkInWindow () {
		Node n = null;
		float xMin = MAX_FLOAT;
		float xMax = MIN_FLOAT;
		float yMin = MAX_FLOAT;
		float yMax = MIN_FLOAT;

		for (int i = 0; i < particleSys.numberOfNodes(); ++i) {
			n = particleSys.getNode(i);
			if (n.isVisible()) {
				if (n.getPosition().getX() > xMax)
					xMax = n.getPosition().getX();
				if (n.getPosition().getX() < xMin)
					xMin = n.getPosition().getX();

				if (n.getPosition().getY() > yMax)
					yMax = n.getPosition().getY();
				if (n.getPosition().getY() < yMin)
					yMin = n.getPosition().getY();
			}
		}

		translateMouseX = width / 2 - (xMin + xMax) / 2;
		translateMouseY = height / 2 - (yMin + yMax) / 2;

		float newScaleFactor = width / (xMax - xMin + 2 * NODE_SIZE);
		if (width / (yMax - yMin) < newScaleFactor)
			newScaleFactor = height / (yMax - yMin + 2 * NODE_SIZE);

		if (newScaleFactor < 1.0f) {
			scaleSlider.setValue(newScaleFactor);
			// the values have to be set manually, otherwise fitting the network
			// when the applet opens will not work because both panels are
			// collapsed at that time
			scaleFactor = newScaleFactor;
			translateZoomX = width / scaleFactor / 2 - width / 2;
			translateZoomY = height / scaleFactor / 2 - height / 2;
		}
	}

	/**
	 * Fix all selected nodes
	 */
	public void fixSelectedNodePositions () {
		if (selectedNode != null) // if one node is dragged at the moment, don't
			// change its status but remember to change
			// it after dragging has ended
			selectedNodeFixed = !selectedNodeFixed;
		else
			// if no node is dragged
			for (int i = 0; i < particleSys.numberOfSelectedNodes(); ++i) {
				if (particleSys.getSelectedNode(i).isFixed()) {
					particleSys.getSelectedNode(i).free();
					callJavascriptFunctionStatusMessage("released node " + particleSys.getSelectedNode(i).getName());
				} else {
					particleSys.getSelectedNode(i).fix();
					callJavascriptFunctionStatusMessage("fixed node " + particleSys.getSelectedNode(i).getName());
				}
			}
	}

	/**
	 * Return the applets background color as a hexadecimal string
	 * 
	 * @return The background color as a hexadecimal string
	 */
	public String getBackgroundColorAsHexString () {
		return "#" + Integer.toHexString(backgroundColor).substring(2).toUpperCase();
	}

	/**
	 * Return the x-coordinate of the mouse-pointer
	 * 
	 * @return The x-coordinate of the mouse pointer scaled and translated in
	 *         accordance with the current view
	 */
	public float getMouseX () {
		return (mouseX / scaleFactor - translateMouseX - translateZoomX);
	}

	/**
	 * Return the y-coordinate of the mouse-pointer
	 * 
	 * @return The y-coordinate of the mouse pointer scaled and translated in
	 *         accordance with the current view
	 */
	public float getMouseY () {
		return (mouseY / scaleFactor - translateMouseY - translateZoomY);
	}

	/**
	 * Return the edge on which the user has clicked, if any
	 * 
	 * @return The selected edge or null if no visible edge is nearby the
	 *         clicked position
	 */
	public Edge getNearestEdge () {
		float curX = getMouseX();
		float curY = getMouseY();

		Edge e = null;
		Edge nearest = null;
		float minDevSquared = 100;
		float devSquared = 0;

		for (int i = 0; i < particleSys.numberOfEdges(); ++i) {
			e = particleSys.getEdge(i);

			if (!e.isVisible())
				continue;

			if (e.getSource().equals(e.getTarget()))
				devSquared = e.getDistanceToPoint(curX, curY, NODE_SIZE);
			else
				devSquared = e.getDistanceToPoint(curX, curY);

			if (devSquared < minDevSquared) {
				minDevSquared = devSquared;
				nearest = e;
			}

		}
		return nearest;
	}

	/**
	 * Return the node on which the user has clicked, if any
	 * 
	 * @return The selected node or null if no node is nearby the clicked
	 *         position
	 */
	public Node getNearestNode () {
		float mx = getMouseX();
		float my = getMouseY();

		Node n = null;
		float d = DISTANCE_CUTOFF;

		for (int i = 0; i < particleSys.numberOfNodes(); ++i) {
			Node v = particleSys.getNode(i);
			float dTemp = distance(mx, my, v.getPosition().getX(), v.getPosition().getY());
			if ((dTemp < d) && dTemp < DISTANCE_CUTOFF) {
				d = dTemp;
				n = v;
			}
		}

		return n;
	}

	/**
	 * Return an Base64 encoded GraphML-representation of the current network
	 * 
	 * @return The Base64 encoded GraphML-string representing the current
	 *         network
	 */
	public String getNetworkAsGraphMLBase64String () {
		return Base64.encodeBase64String(getNetworkAsGraphMLString().getBytes());
	}

	/**
	 * Return an GraphML-representation of the current network
	 * 
	 * @return The GraphML-string representing the current network
	 */
	public String getNetworkAsGraphMLString () {
		String graphml = "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n";

		graphml += "<graphml xmlns=\"http://graphml.graphdrawing.org/xmlns\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://graphml.graphdrawing.org/xmlns http://graphml.graphdrawing.org/xmlns/1.0/graphml.xsd\">\n";

		graphml += "<key id=\"picturepath\" for=\"graph\" attr.name=\"picturepath\" attr.type=\"string\" />\n";
		graphml += "<key id=\"neighbourhoodscript\" for=\"graph\" attr.name=\"neighbourhoodscript\" attr.type=\"string\" />\n";
		graphml += "<key id=\"name\" for=\"node\" attr.name=\"label\" attr.type=\"string\" />\n";
		graphml += "<key id=\"shape\" for=\"node\" attr.name=\"shape\" attr.type=\"string\" />\n";
		graphml += "<key id=\"r\" for=\"node\" attr.name=\"r\" attr.type=\"string\" />\n";
		graphml += "<key id=\"g\" for=\"node\" attr.name=\"g\" attr.type=\"string\" />\n";
		graphml += "<key id=\"b\" for=\"node\" attr.name=\"b\" attr.type=\"string\" />\n";
		graphml += "<key id=\"description\" for=\"node\" attr.name=\"description\" attr.type=\"string\" />\n";
		graphml += "<key id=\"picture\" for=\"node\" attr.name=\"picture\" attr.type=\"string\" />\n";
		graphml += "<key id=\"x\" for=\"node\" attr.name=\"x\" attr.type=\"float\" />\n";
		graphml += "<key id=\"y\" for=\"node\" attr.name=\"y\" attr.type=\"float\" />\n";
		graphml += "<key id=\"edgelabel\" for=\"edge\" attr.name=\"label\" attr.type=\"string\" />\n";
		graphml += "<key id=\"edgeshape\" for=\"edge\" attr.name=\"shape\" attr.type=\"string\" />\n";
		graphml += "<key id=\"weight\" for=\"edge\" attr.name=\"weight\" attr.type=\"float\" />\n";
		graphml += "<key id=\"edger\" for=\"edge\" attr.name=\"r\" attr.type=\"string\" />\n";
		graphml += "<key id=\"edgeg\" for=\"edge\" attr.name=\"g\" attr.type=\"string\" />\n";
		graphml += "<key id=\"edgeb\" for=\"edge\" attr.name=\"b\" attr.type=\"string\" />\n";

		graphml += "<graph id=\"" + params.getLabel() + "\" edgedefault=\"" + ((particleSys.isDirected()) ? "directed" : "undirected") + "\">\n";

		graphml += params.toGraphML();

		for (int i = 0; i < particleSys.numberOfNodes(); i++)
			graphml += particleSys.getNode(i).toGraphML();

		for (int i = 0; i < particleSys.numberOfEdges(); i++)
			graphml += particleSys.getEdge(i).toGraphML();

		graphml += "</graph>\n</graphml>\n";

		try {
			return new String(graphml.getBytes(), "UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			return graphml;
		}
	}

	/**
	 * Return an Base64 encoded XGMML-representation of the current network
	 * 
	 * @return The Base64 encoded XGMML-string representing the current network
	 */
	public String getNetworkAsXGMMLBase64String () {
		return Base64.encodeBase64String(getNetworkAsXGMMLString().getBytes());
	}

	/**
	 * Return an XGMML-representation of the current network
	 * 
	 * @return The XGMML-string representing the current network
	 */
	public String getNetworkAsXGMMLString () {
		String xgmml = "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n";

		xgmml += "<graph label=\"" + params.getLabel() + "\" directed=\"" + ((particleSys.isDirected()) ? "1" : "0")
				+ "\" xmlns=\"http://www.cs.rpi.edu/XGMML\" schemaLocation=\"http://www.cs.rpi.edu/~puninj/XGMML/xgmml.xsd\">\n";

		xgmml += params.toXGMML();

		for (int i = 0; i < particleSys.numberOfNodes(); i++)
			xgmml += particleSys.getNode(i).toXGMML();

		for (int i = 0; i < particleSys.numberOfEdges(); i++)
			xgmml += particleSys.getEdge(i).toXGMML();

		xgmml += "</graph>\n";

		try {
			return new String(xgmml.getBytes(), "UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			return xgmml;
		}
	}

	/**
	 * Returns the particle system
	 * 
	 * @return the particle system
	 */
	public ParticleSystem getParticleSystem () {
		return particleSys;
	}

	/**
	 * Return an Base64 encoded String containing a screenshot of the applet
	 * content in gif format
	 * 
	 * @return The Base64 encoded String containing a screenshot of the applet
	 *         content in gif format
	 */
	public String getScreenshotAsBase64String () {
		try {
			// stop automatic redrawing and draw three time to get a complete
			// image of the network without the panels
			noLoop();
			draw();
			draw();
			draw();

			ByteArrayOutputStream baos = new ByteArrayOutputStream();

			ImageIO.write((BufferedImage) this.g.image, "gif", baos);

			byte[] bytes = baos.toByteArray();

			loop(); // restart automatic drawing

			return Base64.encodeBase64String(bytes);
		} catch (Exception e) {
			loop(); // restart automatic drawing

			String[] args = new String[] { "Generating screenshot failed." };
			callJavascriptFunction("show_warning", args);

			return "";
		}
	}

	/**
	 * Return the text color as a hexadecimal string
	 * 
	 * @return The text color as a hexadecimal string
	 */
	public String getTextColorAsHexString () {
		return "#" + Integer.toHexString(textColor).substring(2).toUpperCase();
	}

	/**
	 * Handle events generated by the buttons in the GUI
	 * 
	 * @param button
	 *            The button event that was generated
	 */
	public void handleButtonEvents (GButton button) {
		if (!(displayPanel.isCollapsed() && graphPanel.isCollapsed())) {
			if (button == fixButton && button.eventType == GButton.CLICKED) {
				if (fixed) {
					for (int i = 0; i < particleSys.numberOfNodes(); ++i)
						particleSys.getNode(i).free();
				} else {
					for (int i = 0; i < particleSys.numberOfNodes(); ++i)
						particleSys.getNode(i).fix();
				}
				fixed = !fixed;
			} else if (button == smoothButton && button.eventType == GButton.CLICKED) {
				if (smoothButton.getText().equals(SMOOTH_BUTTON_FASTER_TEXT)) {
					smoothButton.setText(SMOOTH_BUTTON_NICER_TEXT);
					noSmooth();
				} else {
					smoothButton.setText(SMOOTH_BUTTON_FASTER_TEXT);
					smooth();
				}
			} else if (button == selectCommonNeighboursButton && button.eventType == GButton.CLICKED) {
				selectCommonNeighboursOfSelectedNodes();
			} else if (button == selectNeighboursButton && button.eventType == GButton.CLICKED) {
				selectALLNeighboursOfSelectedNodes();
			} else if (button == statisticsButton && button.eventType == GButton.CLICKED) {
				showStatistics();
			} else if (button == onePathButton && button.eventType == GButton.CLICKED) {
				showOnePath();
			} else if (button == allPathButton && button.eventType == GButton.CLICKED) {
				showAllPath();
			} else if (button == invertButton && button.eventType == GButton.CLICKED) {
				invertSelection();
			} else if (button == addNodeButton && button.eventType == GButton.CLICKED) {
				addNodeByButton();
			} else if (button == addEdgeButton && button.eventType == GButton.CLICKED) {
				addEdgeByButton();
			} else if (button == shuffleButton && button.eventType == GButton.CLICKED) {
				Random rand = new Random();
				for (int i = 0; i < particleSys.numberOfNodes(); ++i) {
					particleSys.getNode(i).free();
					particleSys.getNode(i).setPosition(new Vector2D(rand.nextFloat() + width / 2 - translateMouseX, rand.nextFloat() + height / 2 - translateMouseY));
				}

				for (int i = 0; i < 2000; ++i)
					particleSys.tick();
				fitNetworkInWindow();
			} else if (button == relaxButton && button.eventType == GButton.CLICKED) {
				for (int i = 0; i < 2000; ++i)
					particleSys.tick();
			} else if (button == fitButton && button.eventType == GButton.CLICKED) {
				fitNetworkInWindow();
			}
		}

	}

	/**
	 * Handle events generated by checkboxes in the GUI
	 * 
	 * @param checkbox
	 *            the checkbox that generated the event
	 */
	public void handleCheckboxEvents (GCheckbox checkbox) {
		if (displayPanel.isCollapsed() && graphPanel.isCollapsed())
			checkbox.setSelected(!checkbox.isSelected());
	}

	/**
	 * Handle events generated by the sliders in the GUI
	 * 
	 * @param slider
	 *            The slider event that was generated
	 */
	public void handleSliderEvents (GSlider slider) {
		if (!displayPanel.isCollapsed()) {
			if (slider == scaleSlider) {
				scaleFactor = slider.getValuef();
				translateZoomX = width / scaleFactor / 2 - width / 2;
				translateZoomY = height / scaleFactor / 2 - height / 2;
			} else if (slider == edgeLengthSlider) {
				edgeLength = slider.getValuef();
				for (int i = 0; i < particleSys.numberOfEdges(); ++i)
					particleSys.getEdge(i).setRestLength(edgeLength);
			}
		}
	}

	/**
	 * Handle events generated by the textfields in the gui
	 * 
	 * @param textfield
	 *            the textfield event that was generated
	 */
	public void handleTextFieldEvents (GTextField textfield) {
		if (!graphPanel.isCollapsed())
			if (textfield == nodeSearchTextfield)
				selectNodesByName(textfield.getText());
	}

	/**
	 * Increase the length of the edge between the given nodes by the given
	 * value
	 * 
	 * @param sourceId
	 *            The source node of the edge that is elongated
	 * @param targetId
	 *            The target node of the edge that is elongated
	 * @param amount
	 *            How much the edge is elongated
	 */
	public void increaseEdgeLength (String sourceId, String targetId, Float amount) {
		for (int i = 0; i < particleSys.numberOfEdges(); ++i)
			if (particleSys.getEdge(i).getSource().getId().equals(sourceId) && particleSys.getEdge(i).getTarget().getId().equals(targetId))
				particleSys.getEdge(i).increaseEdgeLength(amount);
	}

	/**
	 * Invert the current selection of nodes
	 */
	public void invertSelection () {
		Node n = null;

		ArrayList<Node> unselectedNodes = new ArrayList<Node>();

		for (int i = 0; i < particleSys.numberOfNodes(); i++) {
			n = particleSys.getNode(i);

			if (n.isHighlighted())
				particleSys.deselectNode(n);
			else
				unselectedNodes.add(n);
		}

		callJavascriptFunctionClearSidebar();

		for (int i = 0; i < unselectedNodes.size(); ++i)
			selectNode(unselectedNodes.get(i));
	}

	/**
	 * Handle key-press events
	 */
	public void keyPressed () {
		if (key == ' ')
			fixSelectedNodePositions();
		else if (key == 'v')
			verbose = !verbose;
	}

	/**
	 * Load the neighbourhood (conected nodes) of the given node by sending a
	 * request to the server using the defined neighbourhood-script
	 * 
	 * @param n
	 *            The node whose neighbourhood is to be loaded
	 */
	public void loadNeighbourhood (Node n) {
		if (params.getNeighbourhoodScript() != null) {
			this.setCursor(new Cursor(Cursor.WAIT_CURSOR));
			callJavascriptFunctionStatusMessage("loading neighbourhood for " + n.getName(), true);

			String lines[] = loadStrings(params.getServerAdress() + params.getNeighbourhoodScript() + "?id=" + n.getId());
			StringBuffer stringBuf = new StringBuffer();
			for (int i = 0; i < lines.length; i++)
				stringBuf.append(lines[i]);
			String content = stringBuf.toString();

			addNetwork(content, n.getPosition().getX(), n.getPosition().getY());

			this.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));

			callJavascriptFunctionStatusMessage("loaded neighbourhood for " + n.getName());
		}
	}

	/**
	 * Load the neighbourhood (conected nodes) of the given node by sending a
	 * request to the server using the defined neighbourhood-script
	 * 
	 * @param id
	 *            The id of the node whose neighbourhood is to be loaded
	 */
	public void loadNeighbourhood (String id) {
		Node n = particleSys.getNodeById(id);

		if (n != null)
			loadNeighbourhood(n);
	}

	/**
	 * Handle mouse-click events
	 */
	public void mouseClicked () {
		if (displayPanel.isOverPanel(mouseX, mouseY))
			return;

		if (mouseEvent.getClickCount() == 1)
			mouseSingleClicked();
		else if (mouseEvent.getClickCount() == 2)
			mouseDoubleClicked();
	}

	/**
	 * Check whether a node was double-clicked. If so, it's neighbourhood is
	 * loaded
	 */
	public void mouseDoubleClicked () {
		if (mouseButton == LEFT) {
			Node n = getNearestNode();

			if ((n != null) && (params.getNeighbourhoodScript() != null))
				loadNeighbourhood(n);
		}
	}

	/**
	 * Handle mouse drags. If the left mouse-button is pressed, the currently
	 * selected node is moved to the mouse-pointer position. If the right
	 * mouse-button is pressed, the view is translated.
	 */
	public void mouseDragged () {
		if (mouseButton == LEFT) {
			if (selectedNode != null) {
				cursor(CROSS);
				selectedNode.getPosition().set(getMouseX(), getMouseY());
			}
		} else if (mouseButton == RIGHT || mouseButton == CENTER) {
			cursor(MOVE);
			translateMouseX = getMouseX() + translateMouseX - origX;
			translateMouseY = getMouseY() + translateMouseY - origY;
		}
	}

	/**
	 * Make the panels transparent, if the mouse is not over them
	 */
	public void mouseMoved () {
		if (displayPanel.isOverPanel(mouseX, mouseY)) {
			displayPanel.setAlpha(250);
			displayPanel.setCollapsed(false);
		} else {
			displayPanel.setAlpha(100);
			displayPanel.setCollapsed(true);
		}

		if (graphPanel.isOverPanel(mouseX, mouseY)) {
			graphPanel.setAlpha(250);
			graphPanel.setCollapsed(false);
		} else {
			graphPanel.setAlpha(100);
			graphPanel.setCollapsed(true);
		}
	}

	/**
	 * Handle mouse-pressed events. Either selects a node, an edge or
	 * initializes the drawing of a selection frame.
	 */
	public void mousePressed () {
		if (displayPanel.isOverPanel(mouseX, mouseY) || graphPanel.isOverPanel(mouseX, mouseY))
			return;

		if (mouseButton == LEFT) {
			for (int j = 0; j < particleSys.numberOfEdges(); j++)
				particleSys.getEdge(j).dehighlight();

			if (!(keyPressed && keyCode == CONTROL))
				particleSys.deselectAllNodes();

			selectedNode = getNearestNode();

			if (selectedNode != null) {
				selectedNodeFixed = selectedNode.isFixed();
				selectedNode.fix();
				selectNode(selectedNode);
				cursor(CROSS);
			} else {
				Edge clickedEdge = getNearestEdge();

				if (clickedEdge != null) {
					selectEdge(clickedEdge);
				} else {
					origX = getMouseX();
					origY = getMouseY();
					selectionFrame = true;
					callJavascriptFunctionClearSidebar();
				}
			}
		} else if (mouseButton == RIGHT || mouseButton == CENTER) {
			cursor(MOVE);
			origX = getMouseX();
			origY = getMouseY();
		}
	}

	/**
	 * Handle mouse-released events. Releases the selected node if a node was
	 * dragged. If a selection frame was active, the nodes inside the frame are
	 * selected.
	 */
	public void mouseReleased () {
		if (mouseButton == LEFT) {
			if (selectedNode != null) {
				if (!selectedNodeFixed)
					selectedNode.free();
				selectedNode = null;
			} else if (selectionFrame) {
				selectionFrame = false;
				selectNodesByFrame();
			}
		}
		cursor(Cursor.DEFAULT_CURSOR);
	}

	/**
	 * Delete a node if it was right-clicked while the SHIFT-key was pressed
	 */
	public void mouseSingleClicked () {
		if ((mouseButton == RIGHT) && mouseEvent.isShiftDown()) {
			Node n = getNearestNode();

			if (n != null)
				deleteNode(n);
		}
	}

	/**
	 * Change the selection to all neighbours of the currently selected nodes
	 */
	public void selectALLNeighboursOfSelectedNodes () {
		if (particleSys.numberOfSelectedNodes() == 0) {
			String[] args = new String[] { "Please select at least one node" };
			callJavascriptFunction("show_warning", args);
		}

		ArrayList<Node> neighbours = GraphFunctions.getAllNeighbours(particleSys);

		particleSys.deselectAllNodes();

		if (neighbours.isEmpty()) {
			String[] args = new String[] { "No neighbours found." };
			callJavascriptFunction("show_warning", args);
		} else {
			for (int i = 0; i < neighbours.size(); ++i)
				particleSys.selectNode(neighbours.get(i));
		}
	}

	/**
	 * Change the selection to the common neighbours of the currently selected
	 * nodes
	 */
	public void selectCommonNeighboursOfSelectedNodes () {
		if (particleSys.numberOfSelectedNodes() == 0) {
			String[] args = new String[] { "Please select at least one node" };
			callJavascriptFunction("show_warning", args);
		}

		ArrayList<Node> neighbours = GraphFunctions.getCommonNeighbours(particleSys);

		particleSys.deselectAllNodes();

		if (neighbours.isEmpty()) {
			String[] args = new String[] { "No common neighbours found." };
			callJavascriptFunction("show_warning", args);
		} else {
			for (int i = 0; i < neighbours.size(); ++i)
				particleSys.selectNode(neighbours.get(i));
		}
	}

	/**
	 * Select the given Edge, i.e. highlight it and call a javascript-function
	 * with details about the edge
	 * 
	 * @param e
	 *            The edge that is to be selected
	 */
	public void selectEdge (Edge e) {
		e.highlight();
		Node a = e.getSource();
		Node b = e.getTarget();
		String[] args = new String[] { a.getId(), a.getName(), a.getDescription(), b.getId(), b.getName(), b.getDescription(), e.getAnnotation(), String.valueOf(e.getShape()),
				String.valueOf(e.getWeight()), e.getColorAsHexString() };
		callJavascriptFunction("show_edge_details", args);
	}

	/**
	 * Select the given Node, i.e. highlight it and call a javascript-funtion
	 * with details about the node
	 * 
	 * @param n
	 *            The node that is to be selected
	 */
	public void selectNode (Node n) {
		if (!n.isVisible())
			return;

		if (n.isHighlighted())
			return;

		particleSys.selectNode(n);

		if (particleSys.numberOfSelectedNodes() == 1) {
			String[] args = new String[] { n.getId(), n.getName(), n.getDescription(), n.getPictureName(), String.valueOf(n.getShape()), n.getFillColorAsHexString(), params.getNeighbourhoodScript() };
			callJavascriptFunction("show_node_details", args);
		} else if (particleSys.numberOfSelectedNodes() == 2) {
			String[] args = new String[] { n.getId(), n.getName(), n.getPictureName(), String.valueOf(n.getShape()), n.getFillColorAsHexString(), params.getNeighbourhoodScript() };
			callJavascriptFunction("create_node_list", args);

			for (int i = 0; i < particleSys.numberOfSelectedNodes(); i++)
				if (!particleSys.getSelectedNode(i).equals(n)) {
					Node n2 = particleSys.getSelectedNode(i);
					args = new String[] { n2.getId(), n2.getName(), n2.getPictureName(), String.valueOf(n2.getShape()), n2.getFillColorAsHexString(), params.getNeighbourhoodScript() };
					callJavascriptFunction("add_node_to_list", args);
				}
		} else {
			String[] args = new String[] { n.getId(), n.getName(), String.valueOf(n.getShape()), n.getFillColorAsHexString(), params.getNeighbourhoodScript() };
			callJavascriptFunction("add_node_to_list", args);
		}
	}

	/**
	 * Select a node by its Id
	 * 
	 * @param id
	 *            The id of the node that is to be selected
	 */
	public void selectNodeById (String id) {
		particleSys.deselectAllNodes();

		for (int i = 0; i < particleSys.numberOfNodes(); ++i)
			if (particleSys.getNode(i).getId().equals(id)) {
				selectNode(particleSys.getNode(i));
				break;
			}
	}

	/**
	 * Select all nodes that are in the current selection frame
	 */
	public void selectNodesByFrame () {
		float otherX = getMouseX();
		float otherY = getMouseY();

		float left = 0;
		float right = 0;
		float top = 0;
		float bottom = 0;

		if (otherX < origX) {
			left = otherX;
			right = origX;
		} else {
			left = origX;
			right = otherX;
		}

		if (otherY < origY) {
			top = otherY;
			bottom = origY;
		} else {
			top = origY;
			bottom = otherY;
		}

		Node n = null;
		for (int i = 0; i < particleSys.numberOfNodes(); i++) {
			n = particleSys.getNode(i);
			if (left < n.getPosition().getX() && n.getPosition().getX() < right && top < n.getPosition().getY() && n.getPosition().getY() < bottom)
				selectNode(n);
		}

	}

	/**
	 * Select nodes by their name
	 * 
	 * @param name
	 *            The name of the nodes that is to be selected. All nodes whose
	 *            name start with the given string are selected.
	 */
	public void selectNodesByName (String name) {
		int nameLength = name.length();

		if (nameLength == 0) {
			particleSys.deselectAllNodes();
			return;
		}

		Node n = null;

		particleSys.deselectAllNodes();

		for (int i = 0; i < particleSys.numberOfNodes(); i++) {
			n = particleSys.getNode(i);

			if (n.getName().length() < nameLength)
				continue;

			if (particleSys.getNode(i).getName().substring(0, nameLength).equalsIgnoreCase(name))
				selectNode(particleSys.getNode(i));
		}
	}

	/**
	 * Set the background color for the applet
	 * 
	 * @param c
	 *            The background color as a hexadecimal string
	 */
	public void setBackgroundColor (String c) {
		try {
			if (c.charAt(0) == '#')
				c = c.substring(1);
			this.backgroundColor = (255 << 24) | Integer.parseInt(c, 16);
		} catch (Exception e) {
			this.backgroundColor = 255;
		}
	}

	/**
	 * Set the annotation of the currently selected edge
	 * 
	 * @param a
	 *            The annotation that is set
	 */
	public void setEdgeAnnotation (String a) {
		try {
			for (int i = 0; i < particleSys.numberOfEdges(); ++i)
				if (particleSys.getEdge(i).isHighlighted()) {
					particleSys.getEdge(i).setAnnotation(a);
					break;
				}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Set the color of the currently selected edge
	 * 
	 * @param c
	 *            The color that is set
	 */
	public void setEdgeColor (String c) {
		try {
			for (int i = 0; i < particleSys.numberOfEdges(); ++i)
				if (particleSys.getEdge(i).isHighlighted()) {
					particleSys.getEdge(i).setColor(c);
					break;
				}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Set the shape of the current edge
	 * 
	 * @param s
	 *            The shape that is set
	 */
	public void setEdgeShape (String s) {
		try {
			for (int i = 0; i < particleSys.numberOfEdges(); ++i)
				if (particleSys.getEdge(i).isHighlighted()) {
					particleSys.getEdge(i).setShape(Integer.parseInt(s));
					break;
				}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void setEdgeStrength (String s) {
		try {
			float newStrength = Float.parseFloat(s);

			if (newStrength > 1)
				newStrength = 1;
			else if (newStrength < 0.005)
				newStrength = 0.005f;

			Edge e = null;
			float factor = 0;

			for (int i = 0; i < particleSys.numberOfEdges(); i++) {
				e = particleSys.getEdge(i);
				factor = edgeStrength / e.getStrength();
				e.setStrength(newStrength / factor);
			}

			edgeStrength = newStrength;
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	/**
	 * Set the weight of the currently selected edge
	 * 
	 * @param w
	 *            The weight that is set
	 */
	public void setEdgeWeight (String w) {
		try {
			for (int i = 0; i < particleSys.numberOfEdges(); ++i)
				if (particleSys.getEdge(i).isHighlighted()) {
					try {
						particleSys.getEdge(i).setWeight(Float.valueOf(w));
					} catch (Exception e) {
						particleSys.getEdge(i).setWeight(1.0f);
					}
					break;
				}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Set the graph label
	 * 
	 * @param l
	 *            The graph label that is set
	 */
	public void setGraphLabel (String l) {
		try {
			params.setLabel(l);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * If one node is selected, set its description
	 * 
	 * @param d
	 *            The description that is set
	 */
	public void setNodeDescription (String d) {
		try {
			if (particleSys.numberOfSelectedNodes() == 1)
				particleSys.getSelectedNode(0).setDescription(d);
			else {
				String[] args = new String[] { "Please select exactely one node." };
				callJavascriptFunction("show_warning", args);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * If one node is selected, set its fill color
	 * 
	 * @param c
	 *            The color that is set
	 */
	public void setNodeFillColor (String c) {
		try {
			if (particleSys.numberOfSelectedNodes() == 1)
				particleSys.getSelectedNode(0).setFillColor(c);
			else {
				String[] args = new String[] { "Please select exactely one node." };
				callJavascriptFunction("show_warning", args);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * If one node is selected, set its name
	 * 
	 * @param n
	 *            The name that is set
	 */
	public void setNodeName (String n) {
		try {
			if (particleSys.numberOfSelectedNodes() == 1)
				particleSys.getSelectedNode(0).setName(n);
			else {
				String[] args = new String[] { "Please select exactely one node." };
				callJavascriptFunction("show_warning", args);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * If one node is selected, set its picture
	 * 
	 * @param p
	 *            The picture that is set
	 */
	public void setNodePicture (String p) {
		try {
			if (particleSys.numberOfSelectedNodes() == 1)
				particleSys.getSelectedNode(0).setPicture(p, params.picturePath);
			else {
				String[] args = new String[] { "Please select exactely one node." };
				callJavascriptFunction("show_warning", args);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * If one node is selected, set its shape
	 * 
	 * @param s
	 *            The shape that is set
	 */
	public void setNodeShape (String s) {
		try {
			if (particleSys.numberOfSelectedNodes() == 1)
				particleSys.getSelectedNode(0).setShape(Integer.parseInt(s));
			else {
				String[] args = new String[] { "Please select exactely one node." };
				callJavascriptFunction("show_warning", args);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Set the strength of repulsion forces between the node
	 * 
	 * @param s
	 *            The strength of repulsion forces
	 */
	public void setRepulsionStrength (String s) {
		try {
			repulsionStrength = Float.parseFloat(s);

			if (repulsionStrength > 500000)
				repulsionStrength = 500000;
			else if (repulsionStrength < 0)
				repulsionStrength = 0;

			for (int i = 0; i < particleSys.numberOfRepulsions(); i++)
				particleSys.getRepulsion(i).setStrength(repulsionStrength);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Set the color that is used to draw the node names
	 * 
	 * @param c
	 *            The color that is used to draw the node names
	 */
	public void setTextColor (String c) {
		try {
			if (c.charAt(0) == '#')
				c = c.substring(1);
			this.textColor = (255 << 24) | Integer.parseInt(c, 16);
		} catch (Exception e) {
			this.textColor = 0;
		}
	}

	/**
	 * Initial method that get called when the applet is loaded. Sets some
	 * constants, initializes the particle system, the control elements and the
	 * handle to the javascript object, if available. Then it calls the parser
	 * to read the given XGMML-data.
	 */
	public void setup () {
		if (getParameter("edgeLength") != null)
			edgeLength = Float.valueOf(getParameter("edgeLength"));
		else
			edgeLength = 150;

		if (getParameter("scaleFactor") != null)
			scaleFactor = Float.valueOf(getParameter("scaleFactor"));
		else
			scaleFactor = 1;

		if (getParameter("edgeStrength") != null)
			edgeStrength = Float.valueOf(getParameter("edgeStrength"));
		else
			edgeStrength = 0.04f;

		if (getParameter("repulsionStrength") != null)
			repulsionStrength = Float.valueOf(getParameter("repulsionStrength"));
		else
			repulsionStrength = 4000;

		size(Integer.valueOf(getParameter("width")), Integer.valueOf(getParameter("height")));
		smooth();
		strokeWeight(2);
		ellipseMode(CENTER);
		rectMode(CENTER);
		imageMode(CENTER);
		textAlign(CENTER, CENTER);
		frameRate(30);

		particleSys = new ParticleSystem(0.2f);

		createControlElements();

		// any font in ttf format can be used to draw the node-names. Just
		// rename it to "font.ttf" and include it in the jar-file
		font = createFont("font.ttf", 14, true);
		textFont(font);

		try {
			window = JSObject.getWindow(this);
		} catch (Exception e) {
			println("window element not available");
		}

		String serverAddress = getCodeBase().toString();

		if (serverAddress.substring(0, 4).equals("file"))
			serverAddress = "http://localhost/cobweb_website/applet/";

		callJavascriptFunctionStatusMessage("parsing network data", true);

		String network = null;
		if (getParameter("networkXGMML") != null) {
			network = getParameter("networkXGMML");
			networkType = "xgmml";
		} else if (getParameter("fileXGMML") != null) {
			String lines[] = loadStrings(serverAddress + getParameter("fileXGMML"));
			StringBuffer stringBuf = new StringBuffer();
			for (int i = 0; i < lines.length; i++)
				stringBuf.append(lines[i]);
			network = stringBuf.toString();
			networkType = "xgmml";
		} else if (getParameter("networkGraphML") != null) {
			network = getParameter("networkGraphML");
			networkType = "graphml";
		} else if (getParameter("fileGraphML") != null) {
			String lines[] = loadStrings(serverAddress + getParameter("fileGraphML"));
			StringBuffer stringBuf = new StringBuffer();
			for (int i = 0; i < lines.length; i++)
				stringBuf.append(lines[i]);
			network = stringBuf.toString();
			networkType = "graphml";
		} else if (getParameter("networkSIF") != null) {
			network = getParameter("networkSIF");
			// newlines and tabs are not preserved in html PARAM-tags, so they
			// have to be encoded by <br> and <tab>
			network = network.replace("<br>", "\n");
			network = network.replace("<tab>", "\t");
			networkType = "sif";
		} else if (getParameter("fileSIF") != null) {
			String lines[] = loadStrings(serverAddress + getParameter("fileSIF"));
			StringBuffer stringBuf = new StringBuffer();
			for (int i = 0; i < lines.length; i++)
				stringBuf.append(lines[i] + "\n");
			network = stringBuf.toString();
			networkType = "sif";
		}

		if (networkType != null) {
			if (networkType.equals("xgmml")) {
				XGMMLParser parser = new XGMMLParser(this, network);
				parser.parseParameters(particleSys, serverAddress);
				parser.parseNodes(particleSys, width, height);
				parser.parseEdges(particleSys, edgeLength);
				params = parser.getParameters();
			} else if (networkType.equals("graphml")) {
				GraphMLParser parser = new GraphMLParser(this, network);
				parser.parseParameters(particleSys, serverAddress);
				parser.parseNodes(particleSys, width, height);
				parser.parseEdges(particleSys, edgeLength);
				params = parser.getParameters();
			} else if (networkType.equals("sif")) {
				SIFParser parser = new SIFParser(this, network);
				parser.parseParameters(particleSys, serverAddress);
				parser.parseNetwork(particleSys, width, height, edgeLength);
				params = parser.getParameters();
			} else
				System.out.println("unknown network type: " + networkType);
		} else
			System.out.println("no network type defined");

		// if no edges are present in the starting network, neighbours for all
		// nodes are loaded
		if (particleSys.numberOfEdges() == 0) {
			ArrayList<Node> n = new ArrayList<Node>();
			for (int i = 0; i < particleSys.numberOfNodes(); i++)
				n.add(particleSys.getNode(i));

			for (Iterator<Node> nIt = n.iterator(); nIt.hasNext();)
				loadNeighbourhood(nIt.next());
		}

		callJavascriptFunctionStatusMessage("computing node positions", true);

		for (int i = 0; i < 2000; i++)
			particleSys.tick();

		fitNetworkInWindow();

		callJavascriptFunctionStatusMessage("applet loaded");
		callJavascriptFunctionClearSidebar();
	}

	/**
	 * If two node are selected, highlight all nodes that lie on all shortest
	 * paths between the selected nodes
	 */
	public void showAllPath () {
		if (particleSys.numberOfSelectedNodes() != 2) {
			String[] args = new String[] { "Please select exactely two nodes." };
			callJavascriptFunction("show_warning", args);
		} else {
			ArrayList<Node> path = new ArrayList<Node>();

			path = GraphFunctions.breadthFirstSearchAllPaths(particleSys, particleSys.getSelectedNode(0), particleSys.getSelectedNode(1));

			if (particleSys.isDirected())
				path.addAll(GraphFunctions.breadthFirstSearchAllPaths(particleSys, particleSys.getSelectedNode(1), particleSys.getSelectedNode(0)));

			if (path.size() == 0) {
				String[] args = new String[] { "No path exist between the selected nodes." };
				callJavascriptFunction("show_warning", args);
			} else {
				for (int i = 0; i < path.size(); ++i) {
					selectNode(path.get(i));
				}
			}
		}
	}

	/**
	 * If two node are selected, highlight all nodes that lie on one shortest
	 * paths between the selected nodes
	 */
	public void showOnePath () {
		if (particleSys.numberOfSelectedNodes() != 2) {
			String[] args = new String[] { "Please select exactely two nodes (by selecting one node and then keeping the CONTROL-key pressed while clicking on the second one)." };
			callJavascriptFunction("show_warning", args);
		} else {
			ArrayList<Node> path = new ArrayList<Node>();

			path = GraphFunctions.breadthFirstSearch(particleSys, particleSys.getSelectedNode(0), particleSys.getSelectedNode(1));

			if (particleSys.isDirected())
				path.addAll(GraphFunctions.breadthFirstSearch(particleSys, particleSys.getSelectedNode(1), particleSys.getSelectedNode(0)));

			if (path.size() == 0) {
				String[] args = new String[] { "No path exist between the selected nodes." };
				callJavascriptFunction("show_warning", args);
			} else {
				for (int i = 0; i < path.size(); ++i) {
					selectNode(path.get(i));
				}
			}
		}
	}

	/**
	 * Get statistics about the network and show them in the sidebar
	 */
	public void showStatistics () {
		String numNodes = String.valueOf(particleSys.numberOfNodes());
		String numEdges = String.valueOf(particleSys.numberOfEdges());

		// connected components
		ArrayList<ArrayList<Node>> components = GraphFunctions.connectedComponents(particleSys);
		String numComponents = String.valueOf(components.size());
		ArrayList<Integer> componentSizesList = new ArrayList<Integer>();
		for (int i = 0; i < components.size(); ++i)
			componentSizesList.add(components.get(i).size());
		Collections.sort(componentSizesList);
		String componentSizes = "";
		if (componentSizesList.size() > 0) {
			componentSizes += componentSizesList.get(componentSizesList.size() - 1).toString();
			for (int i = componentSizesList.size() - 2; i >= 0; --i)
				componentSizes += ", " + componentSizesList.get(i).toString();
		}

		String[] args = new String[] { numNodes, numEdges, numComponents, componentSizes };
		callJavascriptFunction("show_statistics", args);
	}

}
